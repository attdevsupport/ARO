package com.att.aro.android.arocollector.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.Date;

import android.util.Log;

import com.att.aro.android.arocollector.IClientPacketWriter;
import com.att.aro.android.arocollector.Session;
import com.att.aro.android.arocollector.SessionManager;
import com.att.aro.android.arocollector.ip.IPPacketFactory;
import com.att.aro.android.arocollector.ip.IPv4Header;
import com.att.aro.android.arocollector.tcp.PacketHeaderException;
import com.att.aro.android.arocollector.tcp.TCPPacketFactory;
import com.att.aro.android.arocollector.udp.UDPHeader;
import com.att.aro.android.arocollector.udp.UDPPacketFactory;
import com.att.aro.android.arocollector.util.PacketUtil;

public class SocketDataWriterWorker implements Runnable{
	public static final String TAG = "AROCollector";
	private IClientPacketWriter writer;
	private TCPPacketFactory factory;
	private UDPPacketFactory udpfactory;
	private SessionManager sessionmg;
	private String sessionKey = "";
	private SocketData pdata;
	public SocketDataWriterWorker(TCPPacketFactory tcpfactory, UDPPacketFactory udpfactory, IClientPacketWriter writer){
		sessionmg = SessionManager.getInstance();
		pdata = SocketData.getInstance();
		this.factory = tcpfactory;
		this.udpfactory = udpfactory;
		this.writer = writer;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	@Override
	public void run() {
		Session sess = sessionmg.getSessionByKey(sessionKey);
		if(sess == null){
			return;
		}
		sess.setBusywrite(true);
		if(sess.getSocketchannel() != null){
			writeTCP(sess);
		}else if(sess.getUdpchannel() != null){
			writeUDP(sess);
		}
		if(sess != null){
			sess.setBusywrite(false);
			if(sess.isAbortingConnection()){
				Log.d(TAG,"removing aborted connection -> "+
						PacketUtil.intToIPAddress(sess.getDestAddress())+":"+sess.getDestPort()
						+"-"+PacketUtil.intToIPAddress(sess.getSourceIp())+":"+sess.getSourcePort());
				sess.getSelectionkey().cancel();
				if(sess.getSocketchannel() != null && sess.getSocketchannel().isConnected()){
					try {
						sess.getSocketchannel().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if(sess.getUdpchannel() != null && sess.getUdpchannel().isConnected()){
					try {
						sess.getUdpchannel().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				sessionmg.closeSession(sess);
			}
		}
	}
	void writeUDP(Session sess){
		if(!sess.hasDataToSend()){
			return;
		}
		DatagramChannel channel = sess.getUdpchannel();
		String name = PacketUtil.intToIPAddress(sess.getDestAddress())+":"+sess.getDestPort()+
				"-"+PacketUtil.intToIPAddress(sess.getSourceIp())+":"+sess.getSourcePort();
		byte[] data = sess.getSendingData();
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		try {
			String str = new String(data);
			Log.d(TAG,"****** data write to server ********");
			Log.d(TAG,str);
			Log.d(TAG,"***** end writing to server *******");
			Log.d(TAG,"writing data to remote UDP: "+name);
			channel.write(buffer);
			Date dt = new Date();
			sess.connectionStartTime = dt.getTime();
		}catch(NotYetConnectedException ex2){
			sess.setAbortingConnection(true);
			Log.e(TAG,"Error writing to unconnected-UDP server, will abort current connection: "+ex2.getMessage());
		} catch (IOException e) {
			sess.setAbortingConnection(true);
			e.printStackTrace();
			Log.e(TAG,"Error writing to UDP server, will abort connection: "+e.getMessage());
		}
	}
	
	void writeTCP(Session sess){
		SocketChannel channel = sess.getSocketchannel();

		String name = PacketUtil.intToIPAddress(sess.getDestAddress())+":"+sess.getDestPort()+
				"-"+PacketUtil.intToIPAddress(sess.getSourceIp())+":"+sess.getSourcePort();
		
		byte[] data = sess.getSendingData();
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		
		try {
			Log.d(TAG,"writing TCP data to: "+name);
			channel.write(buffer);
			//Log.d(TAG,"finished writing data to: "+name);
		}catch(NotYetConnectedException ex){
			Log.e(TAG,"failed to write to unconnected socket: "+ex.getMessage());
		} catch (IOException e) {
			Log.e(TAG,"Error writing to server: "+e.getMessage());
			
			//close connection with vpn client
			byte[] rstdata = factory.createRstData(sess.getLastIPheader(), sess.getLastTCPheader(), 0);
			try {
				writer.write(rstdata);
				pdata.addData(rstdata);
			} catch (IOException e1) {
			}
			//remove session
			Log.e(TAG,"failed to write to remote socket, aborting connection");
			sess.setAbortingConnection(true);
		}
		
	}

}
