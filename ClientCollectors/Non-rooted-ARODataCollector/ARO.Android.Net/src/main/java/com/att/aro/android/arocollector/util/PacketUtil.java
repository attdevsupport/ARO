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

package com.att.aro.android.arocollector.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

import android.util.Log;

import com.att.aro.android.arocollector.ip.IPv4Header;
import com.att.aro.android.arocollector.tcp.TCPHeader;
import com.att.aro.android.arocollector.udp.UDPHeader;


/**
 * Helper class to perform various useful task
 * @author Borey Sao
 * Date: May 8, 2014
 */
public class PacketUtil {
	public static final String TAG = "AROCollector";
	private volatile static boolean enabledDebugLog = false;
	private volatile static int packetid = 0;
	public synchronized static int getPacketId(){
		return packetid++;
	}
	public static boolean isEnabledDebugLog(){
		return enabledDebugLog;
	}
	public static void setEnabledDebugLog(boolean yes){
		enabledDebugLog = yes;
	}
	public static void Debug(String str){
		Log.d(TAG,str);
	}
	/**
	 * convert int to byte array
	 * @param value int value 32 bits
	 * @param buffer array of byte to write to
	 * @param offset position to write to
	 */
	public static void writeIntToBytes(int value, byte[] buffer, int offset){
		if(buffer.length - offset < 4){
			return;
		}
		buffer[offset] = (byte)((value >> 24) & 0x000000FF);
		buffer[offset + 1] = (byte)((value >> 16)&0x000000FF);
		buffer[offset + 2] = (byte)((value >> 8)&0x000000FF);
		buffer[offset + 3] = (byte)(value & 0x000000FF);
	}
	/**
	 * convert short to byte array
	 * @param value short value to convert
	 * @param buffer array of byte to put value to
	 * @param offset starting position in array
	 */
	public static void writeShortToBytes(short value, byte[] buffer, int offset){
		if(buffer.length - offset < 2){
			return;
		}
		buffer[offset] = (byte)((value >> 8)&0x00FF);
		buffer[offset + 1] = (byte)(value & 0x00FF);
	}
	/**
	 * extract short value from a byte array using Big Endian byte order
	 * @param buffer array of byte
	 * @param start position to start extracting value
	 * @return value of short
	 */
	public static short getNetworkShort(byte[] buffer, int start){
		short value = 0x0000;
		value |= buffer[start] & 0xFF;
		value <<= 8;
		value |= buffer[start+1] & 0xFF;
		return value;
	}
	/**
	 * convert array of byte to int
	 * @param buffer
	 * @param start
	 * @param length
	 * @return value of int
	 */
	public static int getNetworkInt(byte[] buffer, int start, int length){
		int value = 0x00000000;
		int end = start + (length > 4 ? 4: length);
		if(end > buffer.length){
			end = buffer.length;
		}
		for(int i =start;i<end;i++){
			value |= buffer[i] & 0xFF;
			if(i< (end - 1)){
				value <<= 8;
			}
		}
		return value;
	}
	/**
	 * validate TCP header checksum
	 * @param source
	 * @param destination
	 * @param data
	 * @param tcplength
	 * @param tcpoffset
	 * @return
	 */
	public static boolean isValidTCPChecksum(int source, int destination, byte[] data, short tcplength, int tcpoffset){
		int buffersize = tcplength + 12;
		boolean isodd = false;
		if((buffersize % 2) != 0){
			buffersize++;
			isodd = true;
		}
		ByteBuffer buffer = ByteBuffer.allocate(buffersize);
		buffer.putInt(source);
		buffer.putInt(destination);
		buffer.put((byte)0);//reserved => 0
		buffer.put((byte)6);//TCP protocol => 6
		buffer.putShort(tcplength);
		buffer.put(data, tcpoffset, tcplength);
		if(isodd){
			buffer.put((byte)0);
		}
		return isValidIPChecksum(buffer.array(), buffersize);
	}
	/**
	 * validate IP Header checksum
	 * @param data
	 * @param length
	 * @return
	 */
	public static boolean isValidIPChecksum(byte[] data, int length){
		int start = 0;
		int sum = 0;
		int value = 0;
		while(start < length){
			value = PacketUtil.getNetworkInt(data, start, 2);
			sum += value;
			start = start + 2;
		}
		
		//carry over one's complement
		while((sum >> 16) > 0){
			sum = (sum & 0xffff) + (sum >> 16);
		}
		//flip the bit to get one' complement
		sum = ~sum;
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(sum);
		short result = buffer.getShort(2);
		return (result == 0);
	}
	public static byte[] calculateChecksum(byte[] data, int offset, int length){
		int start = offset;
		int sum = 0;
		int value = 0;
		while(start < length){
			value = PacketUtil.getNetworkInt(data, start, 2);
			sum += value;
			start = start + 2;
		}
		//carry over one's complement
		while((sum >> 16) > 0){
			sum = (sum & 0xffff) + (sum >> 16);
		}
		//flip the bit to get one' complement
		sum = ~sum;
		
		//extract the last two byte of int
		byte[] checksum = new byte[2];
		checksum[0] = (byte)(sum >> 8);
		checksum[1] = (byte)sum;
		
		return checksum;
	}
	public static byte[] calculateTCPHeaderChecksum(byte[] data, int offset, int tcplength, int destip, int sourceip){
		int buffersize = tcplength + 12;
		boolean odd = false;
		if(buffersize % 2 != 0){
			buffersize++;
			odd = true;
		}
		ByteBuffer buffer = ByteBuffer.allocate(buffersize);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		//create virtual header
		buffer.putInt(sourceip);
		buffer.putInt(destip);
		buffer.put((byte)0);//reserved => 0
		buffer.put((byte)6);//tcp protocol => 6
		buffer.putShort((short)tcplength);
		
		//add actual header + data
		buffer.put(data, offset, tcplength);
		
		//padding last byte to zero
		if(odd){
			buffer.put((byte)0);
		}
		byte[] tcparray = buffer.array();
		byte[] tcpchecksum = calculateChecksum(tcparray, 0, buffersize);
		
		return tcpchecksum;
	}
	public static String intToIPAddress(int addressInt)
	{
	    StringBuffer buffer = new StringBuffer(16);
	    buffer.append((addressInt >>> 24) & 0x000000FF).append(".").
	           append((addressInt >>> 16) & 0x000000FF).append(".").
	           append((addressInt >>> 8) & 0x000000FF).append(".").
	           append(addressInt & 0x000000FF);

	    return buffer.toString();
	}
	/**
	 * get IP address of device
	 * @return
	 */
	public static String getLocalIpAddress() {
		String ip = null;
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                	ip = inetAddress.getHostAddress();
	                	break;
	                }
	            }
	        }
	        return ip;
	    } catch (SocketException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	public static String getUDPoutput(IPv4Header ipheader, UDPHeader udp){
		StringBuilder str = new StringBuilder();
		str.append("\r\nIP Version: "+ipheader.getIpVersion());
    	str.append("\r\nProtocol: "+ipheader.getProtocol());
    	str.append("\r\nID# "+ipheader.getIdenfication());
    	str.append("\r\nIP Total Length: "+ipheader.getTotalLength());
    	str.append("\r\nIP Header length: "+ipheader.getIPHeaderLength());
    	str.append("\r\nIP checksum: "+ipheader.getHeaderChecksum());
    	str.append("\r\nMay fragement? "+ipheader.isMayFragment());
    	str.append("\r\nLast fragment? "+ipheader.isLastFragment());
    	str.append("\r\nFlag: "+ipheader.getFlag());
    	str.append("\r\nFragment Offset: "+ipheader.getFragmentOffset());
    	str.append("\r\nDest: "+intToIPAddress(ipheader.getDestinationIP())+":"+udp.getDestinationPort());
    	str.append("\r\nSrc: "+intToIPAddress(ipheader.getSourceIP())+":"+udp.getSourcePort());
    	str.append("\r\nUDP Length: "+udp.getLength());
    	str.append("\r\nUDP Checksum: "+udp.getChecksum());
		return str.toString();
	}
	public static String getOutput(IPv4Header ipheader, TCPHeader tcpheader, byte[] packetdata){
		short tcplength = (short)(packetdata.length - ipheader.getIPHeaderLength());
		boolean isvalidchecksum = PacketUtil.isValidTCPChecksum(ipheader.getSourceIP(),ipheader.getDestinationIP(),
				packetdata,tcplength,ipheader.getIPHeaderLength());
		boolean isvalidIPChecsum = PacketUtil.isValidIPChecksum(packetdata, ipheader.getIPHeaderLength());
		int packetbodylength = packetdata.length - ipheader.getIPHeaderLength() - tcpheader.getTCPHeaderLength();
    	StringBuffer str = new StringBuffer();
    	str.append("\r\nIP Version: "+ipheader.getIpVersion());
    	str.append("\r\nProtocol: "+ipheader.getProtocol());
    	str.append("\r\nID# "+ipheader.getIdenfication());
    	str.append("\r\nTotal Length: "+ipheader.getTotalLength());
    	str.append("\r\nData Length: "+packetbodylength);
		str.append("\r\nDest: "+intToIPAddress(ipheader.getDestinationIP())+":"+tcpheader.getDestinationPort());
    	str.append("\r\nSrc: "+intToIPAddress(ipheader.getSourceIP())+":"+tcpheader.getSourcePort());
    	str.append("\r\nACK: "+tcpheader.getAckNumber());
    	str.append("\r\nSeq: "+tcpheader.getSequenceNumber());
    	str.append("\r\nIP Header length: "+ipheader.getIPHeaderLength());
    	str.append("\r\nTCP Header length: "+tcpheader.getTCPHeaderLength());
    	str.append("\r\nACK: "+tcpheader.isACK());
    	str.append("\r\nSYN: "+tcpheader.isSYN());
    	str.append("\r\nCWR: "+tcpheader.isCWR());
    	str.append("\r\nECE: "+ tcpheader.isECE());
    	str.append("\r\nFIN: "+tcpheader.isFIN());
    	str.append("\r\nNS: "+tcpheader.isNS());
    	str.append("\r\nPSH: "+tcpheader.isPSH());
    	str.append("\r\nRST: "+tcpheader.isRST());
    	str.append("\r\nURG: "+tcpheader.isURG());
    	str.append("\r\nIP checksum: "+ipheader.getHeaderChecksum());
    	str.append("\r\nIs Valid IP Checksum: "+isvalidIPChecsum);
    	str.append("\r\nTCP Checksum: "+tcpheader.getChecksum());
    	str.append("\r\nIs Valid TCP checksum: "+isvalidchecksum);
    	str.append("\r\nMay fragement? "+ipheader.isMayFragment());
    	str.append("\r\nLast fragment? "+ipheader.isLastFragment());
    	str.append("\r\nFlag: "+ipheader.getFlag());
    	str.append("\r\nFragment Offset: "+ipheader.getFragmentOffset());
    	str.append("\r\nWindow: "+tcpheader.getWindowSize());
    	str.append("\r\nWindow scale: "+tcpheader.getWindowScale());
    	str.append("\r\nData Offset: "+tcpheader.getDataOffset());
    	if(tcpheader.getOptions().length > 0){
    		str.append("\r\nTCP Options: \r\n..........");
    		byte[] options = tcpheader.getOptions();
    		byte kind;
    		for(int i=0;i<options.length;i++){
    			kind = options[i];
    			if(kind == 0){
    				str.append("\r\n...End of options list");
    			}else if(kind == 1){
    				str.append("\r\n...NOP");
    			}else if(kind == 2){
    				i += 2;
    				int segsize = PacketUtil.getNetworkInt(options, i, 2);
    				i++;
    				str.append("\r\n...Max Seg Size: "+segsize);
    			}else if(kind == 3){
    				i += 2;
    				int windowsize = PacketUtil.getNetworkInt(options, i, 1);
    				str.append("\r\n...Window Scale: "+windowsize);
    			}else if(kind == 4){
    				i++;
    				str.append("\r\n...Selective Ack");
    			}else if(kind == 5){
    				i = i + options[++i] - 2;
    				str.append("\r\n...selective ACK (SACK)");
    			}else if(kind == 8){
    				i += 2;
    				int tttt = PacketUtil.getNetworkInt(options, i, 4);
    				i += 4;
    				int eeee = PacketUtil.getNetworkInt(options, i, 4);
    				i += 3;
    				str.append("\r\n...Timestamp: "+tttt+"-"+eeee);
    			}else if(kind == 14){
    				i +=2;
    				str.append("\r\n...Alternative Checksum request");
    			}else if(kind == 15){
    				i = i + options[++i] - 2;
    				str.append("\r\n...TCP Alternate Checksum Data");
    			}else{
    				str.append("\r\n... unknown option# "+kind +", int: "+(int)kind);
    			}
    		}
    	}
    	return str.toString();
	}
	/**
	 * detect packet corruption flag in tcp options sent from client ACK
	 * @param tcpheader
	 * @return
	 */
	public static boolean isPacketCorrupted(TCPHeader tcpheader){
		boolean iscorrupted = false;
		byte[] options = tcpheader.getOptions();
		byte kind;
		for(int i=0;i<options.length;i++){
			kind = options[i];
			if(kind == 0 || kind == 1){
			}else if(kind == 2){
				i += 3;
			}else if(kind == 3 || kind == 14){
				i += 2;
			}else if(kind == 4){
				i++;
			}else if(kind == 5 || kind == 15){
				i = i + options[++i] - 2;
			}else if(kind == 8){
				i += 9;
			}else if(kind == (byte)23){
				iscorrupted = true;
				break;
			}else{
				//Log.e(SessionHandler.TAG,"unknown option: "+kind);
			}
		}
		return iscorrupted;
	}
	public static String bytesToStringArray(byte[] bytes) {
		StringBuilder str = new StringBuilder();
		str.append("{");
	    for(int i =0;i<bytes.length;i++){
	    	if(i == 0){
	    		str.append(bytes[i]);
	    	}else{
	    		str.append(","+bytes[i]);
	    	}
	    }
	    str.append("}");
	    return str.toString();
	}
}//end class
