package com.att.aro.android.arocollector.udp;

import android.util.Log;

import com.att.aro.android.arocollector.ip.IPPacketFactory;
import com.att.aro.android.arocollector.ip.IPv4Header;
import com.att.aro.android.arocollector.tcp.PacketHeaderException;
import com.att.aro.android.arocollector.util.PacketUtil;

public class UDPPacketFactory {
	public UDPPacketFactory(){}
	
	public UDPHeader createUDPHeader(byte[] buffer, int start) throws PacketHeaderException{
		UDPHeader header = null;
		if((buffer.length - start) < 8){
			throw new PacketHeaderException("Minimum UDP header is 8 bytes.");
		}
		int srcPort = PacketUtil.getNetworkInt(buffer, start, 2);
		int destPort = PacketUtil.getNetworkInt(buffer, start + 2, 2);
		int length = PacketUtil.getNetworkInt(buffer, start + 4,2);
		int checksum = PacketUtil.getNetworkInt(buffer, start + 6,2);
		StringBuilder str = new StringBuilder();
		str.append("\r\n..... new UDP header .....");
		str.append("\r\nstarting position in buffer: "+start);
		str.append("\r\nSrc port: "+srcPort);
		str.append("\r\nDest port: "+destPort);
		str.append("\r\nLength: "+length);
		str.append("\r\nChecksum: "+checksum);
		str.append("\r\n...... end UDP header .....");
		Log.d("AROCollector",str.toString());
		header = new UDPHeader(srcPort, destPort, length, checksum);
		return header;
	}
	public UDPHeader copyHeader(UDPHeader header){
		UDPHeader newh = new UDPHeader(header.getSourcePort(), header.getDestinationPort(), header.getLength(), header.getChecksum());
		return newh;
	}
	/**
	 * create packet data for responding to vpn client
	 * @param ip IPv4Header sent from VPN client, will be used as the template for response
	 * @param udp UDPHeader sent from VPN client
	 * @param packetdata packet data to be sent to client
	 * @return array of byte
	 */
	public byte[] createResponsePacket(IPv4Header ip, UDPHeader udp, byte[] packetdata){
		byte[] buffer = null;
		int udplen = 8;
		if(packetdata != null){
			udplen += packetdata.length;
		}
		int srcPort = udp.getDestinationPort();
		int destPort = udp.getSourcePort();
		short checksum = 0;
		
		IPv4Header ipheader = IPPacketFactory.copyIPv4Header(ip);
		
		int srcIp = ip.getDestinationIP();
		int destIp = ip.getSourceIP();
		ipheader.setMayFragment(false);
		ipheader.setSourceIP(srcIp);
		ipheader.setDestinationIP(destIp);
		ipheader.setIdenfication(PacketUtil.getPacketId());
		
		//ip's length is the length of the entire packet => IP header length + UDP header length (8) + UDP body length
		int totallength = ipheader.getIPHeaderLength() + udplen;
		
		ipheader.setTotalLength(totallength);
		buffer = new byte[totallength];
		byte[] ipdata = IPPacketFactory.createIPv4HeaderData(ipheader);
		//calculate checksum for IP header
		byte[] zero = {0,0};
		//zero out checksum first before calculation
		System.arraycopy(zero, 0, ipdata, 10, 2);
		byte[] ipchecksum = PacketUtil.calculateChecksum(ipdata, 0, ipdata.length);
		//write result of checksum back to buffer
		System.arraycopy(ipchecksum, 0, ipdata, 10, 2);
		System.arraycopy(ipdata, 0, buffer, 0, ipdata.length);
		
		//copy UDP header to buffer
		int start = ipdata.length;
		byte[] intcontainer = new byte[4];
		PacketUtil.writeIntToBytes(srcPort, intcontainer, 0);
		//extract the last two bytes of int value
		System.arraycopy(intcontainer,2,buffer,start,2);
		start += 2;
		
		PacketUtil.writeIntToBytes(destPort, intcontainer, 0);
		System.arraycopy(intcontainer, 2, buffer, start, 2);
		start += 2;
		
		PacketUtil.writeIntToBytes(udplen, intcontainer, 0);
		System.arraycopy(intcontainer, 2, buffer, start, 2);
		start += 2;
		
		PacketUtil.writeIntToBytes(checksum, intcontainer, 0);
		System.arraycopy(intcontainer, 2, buffer, start, 2);
		start += 2;
		
		//now copy udp data
		System.arraycopy(packetdata, 0, buffer, start, packetdata.length);
		
		return buffer;
	}
	
}//end
