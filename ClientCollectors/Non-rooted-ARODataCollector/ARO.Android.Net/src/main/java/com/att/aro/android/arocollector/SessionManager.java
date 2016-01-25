/*
 *  Copyright 2014 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.att.aro.android.arocollector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import com.att.aro.android.arocollector.ip.IPv4Header;
import com.att.aro.android.arocollector.socket.DataConst;
import com.att.aro.android.arocollector.socket.SocketNIODataService;
import com.att.aro.android.arocollector.socket.SocketProtector;
import com.att.aro.android.arocollector.tcp.TCPHeader;
import com.att.aro.android.arocollector.udp.UDPHeader;
import com.att.aro.android.arocollector.util.PacketUtil;

import android.util.Log;

/**
 * Manage in-memory storage for VPN client session.
 * @author Borey Sao
 * Date: May 20, 2014
 */
public class SessionManager {
	public static final String TAG = "AROCollector";
	private static Object syncObj = new Object();
	private static volatile SessionManager instance = null;
	private Hashtable<String, Session> table = null;
	public static Object syncTable = new Object();
	private SocketProtector protector = null;
	Selector selector;
	private SessionManager(){
		table = new Hashtable<String,Session>(10);
		protector = SocketProtector.getInstance();
		try {
			selector = Selector.open();
		} catch (IOException e) {
			Log.e(TAG,"Failed to create Socket Selector");
		}
	}
	public static SessionManager getInstance(){
		if(instance == null){
			synchronized(syncObj){
				if(instance == null){
					instance = new SessionManager();
				}
			}
		}
		return instance;
	}
	public Selector getSelector(){
		return selector;
	}
	/**
	 * keep java garbage collector from collecting a session
	 * @param sess
	 */
	public void keepSessionAlive(Session sess){
		if(sess != null){
			String key = this.createKey(sess.getDestAddress(), sess.getDestPort(), sess.getSourceIp(), sess.getSourcePort());
			synchronized(syncTable){
				table.put(key, sess);
			}
		}
	}
	public Iterator<Session> getAllSession(){
		return table.values().iterator();
	}
	public int addClientUDPData(IPv4Header ip, UDPHeader udp, byte[] buffer, Session session){
		int start = ip.getIPHeaderLength() + 8;
		int len = udp.getLength() - 8;//exclude header size
		if(len < 1){
			return 0;
		}
		if((buffer.length - start) < len){
			len = buffer.length - start;
		}
		byte[] data = new byte[len];
		System.arraycopy(buffer, start, data, 0, len);
		session.setSendingData(data);
		return len;
	}
	/**
	 * add data from client which will be sending to the destination server later one when receiving PSH flag.
	 * @param ip
	 * @param tcp
	 * @param buffer
	 */
	public int addClientData(IPv4Header ip, TCPHeader tcp, byte[] buffer){
		Session session = getSession(ip.getDestinationIP(), tcp.getDestinationPort(), ip.getSourceIP(), tcp.getSourcePort());
		if(session == null){
			return 0;
		}
		int len = 0;
		//check for duplicate data
		if(session.getRecSequence() != 0 && tcp.getSequenceNumber() < session.getRecSequence()){
			return len;
		}
		int start = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		len = buffer.length - start;
		byte[] data = new byte[len];
		System.arraycopy(buffer, start, data, 0, len);
		//appending data to buffer
		session.setSendingData(data);
		return len;
	}
	public boolean hasSession(int ipaddress, int port, int srcIp, int srcPort){
		String key = createKey(ipaddress, port, srcIp, srcPort);
		return table.containsKey(key);
	}
	public Session getSession(int ipaddress, int port, int srcIp, int srcPort){
		String key = createKey(ipaddress, port, srcIp, srcPort);
		Session session = null;
		synchronized(syncTable){
			if(table.containsKey(key)){
				session = table.get(key);
			}
		}
		return session;
	}
	public Session getSessionByKey(String key){
		Session session = null;
		synchronized(syncTable){
			if(table.containsKey(key)){
				session = table.get(key);
			}
		}
		return session;
	}
	public Session getSessionByDatagramChannel(DatagramChannel channel){
		Session session = null;
		synchronized(syncTable){
			Iterator<Session> it = table.values().iterator();
			while(it.hasNext()){
				Session sess = it.next();
				if(sess.getUdpchannel() == channel){
					session = sess;
					break;
				}
			}
		}
		return session;
	}
	public Session getSessionByChannel(SocketChannel channel){
		Session session = null;
		synchronized(syncTable){
			Iterator<Session> it = table.values().iterator();
			while(it.hasNext()){
				Session sess = it.next();
				if(sess.getSocketchannel() == channel){
					session = sess;
					break;
				}
			}
		}
		return session;
	}
	public void removeSessionByChannel(SocketChannel channel){
		String key = null;
		String tmp = null;
		Session session = null;
		synchronized(syncTable){
			Iterator<String> it = table.keySet().iterator();
			while(it.hasNext()){
				tmp = it.next();
				Session sess = table.get(tmp);
				if(sess != null && sess.getSocketchannel() == channel){
					key = tmp;
					session = sess;
					break;
				}
				
			}
		}
		if(key != null){
			synchronized(syncTable){
				table.remove(key);
			}
		}
		if(session != null){
			Log.d(TAG,"closed session -> "+PacketUtil.intToIPAddress(session.getDestAddress())+":"+session.getDestPort()
					+"-"+PacketUtil.intToIPAddress(session.getSourceIp())+":"+session.getSourcePort());
			session = null;
		}
	}
	/**
	 * remove session from memory, then close socket connection.
	 * @param ip
	 * @param port
	 * @param srcIp
	 * @param srcPort
	 */
	public void closeSession(int ip, int port, int srcIp, int srcPort){
		String keys = createKey(ip,port, srcIp, srcPort);
		Session session = null; //getSession(ip, port, srcIp, srcPort);
		synchronized(syncTable){
			session = table.remove(keys);
		}
		if(session != null){
			try {
				SocketChannel chan = session.getSocketchannel();
				if(chan != null){
					chan.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(TAG,"closed session -> "+PacketUtil.intToIPAddress(session.getDestAddress())+":"+session.getDestPort()
					+"-"+PacketUtil.intToIPAddress(session.getSourceIp())+":"+session.getSourcePort());
			session = null;
		}
	}
	public void closeSession(Session session){
		if(session == null){
			return;
		}
		String key = createKey(session.getDestAddress(), session.getDestPort(), session.getSourceIp(), session.getSourcePort());
		synchronized(syncTable){
			table.remove(key);
		}
		if(session != null){
			try {
				SocketChannel chan = session.getSocketchannel();
				if(chan != null){
					chan.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.d(TAG,"closed session -> "+PacketUtil.intToIPAddress(session.getDestAddress())+":"+session.getDestPort()
					+"-"+PacketUtil.intToIPAddress(session.getSourceIp())+":"+session.getSourcePort());
			session = null;
		}
	}
	public Session createNewUDPSession(int ip, int port, int srcIp, int srcPort){
		String keys = createKey(ip,port, srcIp, srcPort);
		boolean found = false;
		synchronized(syncTable){
			found = table.containsKey(keys);
		}
		if(found){
			return null;
		}
		Session ses = new Session();
		ses.setDestAddress(ip);
		ses.setDestPort(port);
		ses.setSourceIp(srcIp);
		ses.setSourcePort(srcPort);
		ses.setConnected(false);
		
		DatagramChannel channel = null;
		
		try {
			channel = DatagramChannel.open();
			channel.socket().setSoTimeout(0);
			channel.configureBlocking(false);
			
		}catch(SocketException ex){
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		protector.protect(channel.socket());
		
		//initiate connection to redude latency
		String ips = PacketUtil.intToIPAddress(ip);
		String srcips = PacketUtil.intToIPAddress(srcIp);
		SocketAddress addr = new InetSocketAddress(ips,port);
		Log.d(TAG,"initialized connection to remote UDP server: "+ips+":"+port+" from "+srcips+":"+srcPort);
		
		
		try{
			channel.connect(addr);
			ses.setConnected(channel.isConnected());
		}catch(ClosedChannelException ex){
		}catch(UnresolvedAddressException ex2){
		}catch(UnsupportedAddressTypeException ex3){
		}catch(SecurityException ex4){
		}catch(IOException ex5){
		}
		
				
		Object isudp = new Object();
		try {
			
			synchronized(SocketNIODataService.syncSelector2){
				selector.wakeup();
				synchronized(SocketNIODataService.syncSelector){
					SelectionKey selectkey = null;
					if(!channel.isConnected()){
						selectkey = channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE, isudp);
					}else{
						selectkey = channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, isudp);
					}
					ses.setSelectionkey(selectkey);
					Log.d(TAG,"Registered udp selector successfully");
				}
			}
		} catch (ClosedChannelException e1) {
			e1.printStackTrace();
			Log.e(TAG,"failed to register udp channel with selector: "+e1.getMessage());
			return null;
		}
		
		ses.setUdpchannel(channel);
		
		synchronized(syncTable){
			if(!table.containsKey(keys)){
				table.put(keys, ses);
			}else{
				found = true;
			}
		}
		if(found){
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(ses != null){
			Log.d(TAG,"new UDP session successfully created.");
		}
		return ses;
	}
	public Session createNewSession(int ip, int port, int srcIp, int srcPort){
		String keys = createKey(ip,port, srcIp, srcPort);
		boolean found = false;
		synchronized(syncTable){
			found = table.containsKey(keys);
		}
		if(found){
			return null;
		}
		Session ses = new Session();
		ses.setDestAddress(ip);
		ses.setDestPort(port);
		ses.setSourceIp(srcIp);
		ses.setSourcePort(srcPort);
		ses.setConnected(false);
		
		SocketChannel channel = null;
		try {
			channel = SocketChannel.open();
			channel.socket().setKeepAlive(true);
			channel.socket().setTcpNoDelay(true);
			channel.socket().setSoTimeout(0);
			channel.socket().setReceiveBufferSize(DataConst.MAX_RECEIVE_BUFFER_SIZE);
			channel.configureBlocking(false);
		}catch(SocketException ex){
			return null;
		} catch (IOException e) {
			Log.e(TAG,"Failed to create SocketChannel: "+e.getMessage());
			return null;
		}
		String ips = PacketUtil.intToIPAddress(ip);
		Log.d(TAG,"created new socketchannel for "+ips+":"+port+"-"+PacketUtil.intToIPAddress(srcIp)+":"+srcPort);
		
		protector.protect(channel.socket());
		
		Log.d(TAG,"Protected new socketchannel");
		
		//initiate connection to redude latency
		SocketAddress addr = new InetSocketAddress(ips,port);
		Log.d(TAG,"initiate connecting to remote tcp server: "+ips+":"+port);
		boolean connected = false;
		try{
			connected = channel.connect(addr);
			
		}catch(ClosedChannelException ex){
		}catch(UnresolvedAddressException ex2){
		}catch(UnsupportedAddressTypeException ex3){
		}catch(SecurityException ex4){
		}catch(IOException ex5){
		}
		
		ses.setConnected(connected);
		
		//register for non-blocking operation
		try {
			synchronized(SocketNIODataService.syncSelector2){
				selector.wakeup();
				synchronized(SocketNIODataService.syncSelector){
					SelectionKey selectkey = channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					ses.setSelectionkey(selectkey);
					Log.d(TAG,"Registered tcp selector successfully");
				}
			}
		} catch (ClosedChannelException e1) {
			e1.printStackTrace();
			Log.e(TAG,"failed to register tcp channel with selector: "+e1.getMessage());
			return null;
		}
		
		ses.setSocketchannel(channel);
		
		synchronized(syncTable){
			if(!table.containsKey(keys)){
				table.put(keys, ses);
			}else{
				found = true;
			}
		}
		if(found){
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ses = null;
		}
		
		return ses;
	}
	/**
	 * create session key based on destination ip+port and source ip+port
	 * @param ip
	 * @param port
	 * @param srcIp
	 * @param srcPort
	 * @return
	 */
	public String createKey(int ip, int port, int srcIp, int srcPort){
		return ip + ":" + port+"-"+srcIp+":"+srcPort;
	}
}
