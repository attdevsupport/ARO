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

package com.att.aro.android.arocollector.tcp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Random;

import com.att.aro.android.arocollector.Packet;
import com.att.aro.android.arocollector.ip.IPPacketFactory;
import com.att.aro.android.arocollector.ip.IPv4Header;
import com.att.aro.android.arocollector.util.PacketUtil;

import android.util.Log;

/**
 * class to create IPv4 Header, TCP header, and packet data.
 * @author Borey Sao
 * Date: May 8, 2014
 */
public class TCPPacketFactory {
	public static final String TAG = "AROCollector";
	
	public TCPHeader copyTCPHeader(TCPHeader tcpheader){
		TCPHeader tcp = new TCPHeader(tcpheader.getSourcePort(), tcpheader.getDestinationPort(), tcpheader.getSequenceNumber(), 
				tcpheader.getDataOffset(), tcpheader.isNS(), tcpheader.getTcpFlags(), tcpheader.getWindowSize(), 
				tcpheader.getChecksum(), tcpheader.getUrgentPointer(), tcpheader.getOptions(), tcpheader.getAckNumber());
		tcp.setMaxSegmentSize(tcpheader.getMaxSegmentSize());
		tcp.setWindowScale(tcpheader.getWindowScale());
		tcp.setSelectiveAckPermitted(tcpheader.isSelectiveAckPermitted());
		tcp.setTimeStampSender(tcpheader.getTimeStampSender());
		tcp.setTimeStampReplyTo(tcpheader.getTimeStampReplyTo());
		return tcp;
	}
	/**
	 * create FIN-ACK for sending to client
	 * @param ipheader
	 * @param tcpheader
	 * @param ackToClient
	 * @param seqToClient
	 * @return
	 */
	public byte[] createFinAckData(IPv4Header ipheader, TCPHeader tcpheader, int ackToClient, int seqToClient, boolean isfin, boolean isack){
		byte[] buffer = null;
		IPv4Header ip = IPPacketFactory.copyIPv4Header(ipheader);
		TCPHeader tcp = copyTCPHeader(tcpheader);
		
		//flip IP from source to dest and vice-versa
		int sourceIp = ip.getDestinationIP();
		int destIp = ip.getSourceIP();
		int sourcePort = tcp.getDestinationPort();
		int destPort = tcp.getSourcePort();
		
		int ackNumber = ackToClient;
		int seqNumber = seqToClient;
		
		ip.setDestinationIP(destIp);
		ip.setSourceIP(sourceIp);
		tcp.setDestinationPort(destPort);
		tcp.setSourcePort(sourcePort);
		
		tcp.setAckNumber(ackNumber);
		tcp.setSequenceNumber(seqNumber);
		
		ip.setIdenfication(PacketUtil.getPacketId());
		
		//ACK
		tcp.setIsACK(isack);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsFIN(isfin);
		
		//set response timestamps in options fields
		tcp.setTimeStampReplyTo(tcp.getTimeStampSender());
		Date currentdate = new Date();
		int sendertimestamp = (int)currentdate.getTime();
		tcp.setTimeStampSender(sendertimestamp);
		
		//recalculate IP length
		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setTotalLength(totalLength);
		
		buffer = this.createPacketData(ip, tcp, null);
		
		return buffer;
	}
	public byte[] createFinData(IPv4Header ip, TCPHeader tcp, int ackNumber, int seqNumber, int timeSender, int timeReplyto){
		byte[] buffer = null;
		
		//flip IP from source to dest and vice-versa
		int sourceIp = ip.getDestinationIP();
		int destIp = ip.getSourceIP();
		int sourcePort = tcp.getDestinationPort();
		int destPort = tcp.getSourcePort();
		
		tcp.setAckNumber(ackNumber);
		tcp.setSequenceNumber(seqNumber);
		
		tcp.setTimeStampReplyTo(timeReplyto);
		tcp.setTimeStampSender(timeSender);
		
		ip.setDestinationIP(destIp);
		ip.setSourceIP(sourceIp);
		tcp.setDestinationPort(destPort);
		tcp.setSourcePort(sourcePort);
		
		ip.setIdenfication(PacketUtil.getPacketId());
		
		tcp.setIsRST(false);
		tcp.setIsACK(false);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsCWR(false);
		tcp.setIsECE(false);
		tcp.setIsFIN(true);
		tcp.setIsNS(false);
		tcp.setIsURG(false);
		
		//remove any option field
		byte[] options = new byte[0];
		tcp.setOptions(options);
		
		//window size should be zero
		tcp.setWindowSize(0);
		
		//recalculate IP length
		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setTotalLength(totalLength);
		
		buffer = this.createPacketData(ip, tcp, null);
		
		return buffer;
	}
	/**
	 * create packet with RST flag for sending to client when reset is required.
	 * @param ipheader
	 * @param tcpheader
	 * @param datalength
	 * @return
	 */
	public byte[] createRstData(IPv4Header ipheader, TCPHeader tcpheader, int datalength){
		byte[] buffer = null;
		IPv4Header ip = IPPacketFactory.copyIPv4Header(ipheader);
		TCPHeader tcp = copyTCPHeader(tcpheader);
		
		//flip IP from source to dest and vice-versa
		int sourceIp = ip.getDestinationIP();
		int destIp = ip.getSourceIP();
		int sourcePort = tcp.getDestinationPort();
		int destPort = tcp.getSourcePort();
		
		int ackNumber = 0;
		int seqNumber = 0;
		
		if(tcp.getAckNumber() > 0){
			seqNumber = tcp.getAckNumber();
		}else{
			ackNumber = tcp.getSequenceNumber() + datalength;
		}
		tcp.setAckNumber(ackNumber);
		tcp.setSequenceNumber(seqNumber);
		
		ip.setDestinationIP(destIp);
		ip.setSourceIP(sourceIp);
		tcp.setDestinationPort(destPort);
		tcp.setSourcePort(sourcePort);
		
		ip.setIdenfication(0);
		
		tcp.setIsRST(true);
		tcp.setIsACK(false);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		tcp.setIsCWR(false);
		tcp.setIsECE(false);
		tcp.setIsFIN(false);
		tcp.setIsNS(false);
		tcp.setIsURG(false);
		
		//remove any option field
		byte[] options = new byte[0];
		tcp.setOptions(options);
		
		//window size should be zero
		tcp.setWindowSize(0);
		
		//recalculate IP length
		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setTotalLength(totalLength);
		
		buffer = this.createPacketData(ip, tcp, null);
		
		return buffer;
	}
	/**
	 * Acknowledgment to client that server has received request.
	 * @param ipheader
	 * @param tcpheader
	 * @param clientDataSize
	 * @return
	 */
	public byte[] createResponseAckData(IPv4Header ipheader, TCPHeader tcpheader, int ackToClient){
		byte[] buffer = null;
		IPv4Header ip = IPPacketFactory.copyIPv4Header(ipheader);
		TCPHeader tcp = copyTCPHeader(tcpheader);
		
		//flip IP from source to dest and vice-versa
		int sourceIp = ip.getDestinationIP();
		int destIp = ip.getSourceIP();
		int sourcePort = tcp.getDestinationPort();
		int destPort = tcp.getSourcePort();
		
		int ackNumber = ackToClient;
		int seqNumber = tcp.getAckNumber();
		
		ip.setDestinationIP(destIp);
		ip.setSourceIP(sourceIp);
		tcp.setDestinationPort(destPort);
		tcp.setSourcePort(sourcePort);
		
		tcp.setAckNumber(ackNumber);
		tcp.setSequenceNumber(seqNumber);
		
		ip.setIdenfication(PacketUtil.getPacketId());
		
		//ACK
		tcp.setIsACK(true);
		tcp.setIsSYN(false);
		tcp.setIsPSH(false);
		
		//set response timestamps in options fields
		tcp.setTimeStampReplyTo(tcp.getTimeStampSender());
		Date currentdate = new Date();
		int sendertimestamp = (int)currentdate.getTime();
		tcp.setTimeStampSender(sendertimestamp);
		
		//recalculate IP length
		int totalLength = ip.getIPHeaderLength() + tcp.getTCPHeaderLength();
		
		ip.setTotalLength(totalLength);
		
		buffer = this.createPacketData(ip, tcp, null);
		
		return buffer;
	}
	/**
	 * create packet data for sending back to client
	 * @param ipheader
	 * @param tcpheader
	 * @param packetdata
	 * @return
	 */
	public byte[] createResponsePacketData(IPv4Header ip, TCPHeader tcp, byte[] packetdata, boolean ispsh, 
			int ackNumber, int seqNumber, int timeSender, int timeReplyto){
		byte[] buffer = null;
		IPv4Header ipheader = IPPacketFactory.copyIPv4Header(ip);
		TCPHeader tcpheader = copyTCPHeader(tcp);
		
		//flip IP from source to dest and vice-versa
		int sourceIp = ipheader.getDestinationIP();
		int destIp = ipheader.getSourceIP();
		int sourcePort = tcpheader.getDestinationPort();
		int destPort = tcpheader.getSourcePort();
		
		
		ipheader.setDestinationIP(destIp);
		ipheader.setSourceIP(sourceIp);
		tcpheader.setDestinationPort(destPort);
		tcpheader.setSourcePort(sourcePort);
		
		tcpheader.setAckNumber(ackNumber);
		tcpheader.setSequenceNumber(seqNumber);
		
		ipheader.setIdenfication(PacketUtil.getPacketId());
		
		//ACK is always sent
		tcpheader.setIsACK(true);
		tcpheader.setIsSYN(false);
		tcpheader.setIsPSH(ispsh);
		tcpheader.setIsFIN(false);
		
		tcpheader.setTimeStampSender(timeSender);
		tcpheader.setTimeStampReplyTo(timeReplyto);
		//recalculate IP length
		int totalLength = ipheader.getIPHeaderLength() + tcpheader.getTCPHeaderLength();
		if(packetdata != null){
			totalLength += packetdata.length;
		}
		ipheader.setTotalLength(totalLength);
		
		buffer = this.createPacketData(ipheader, tcpheader, packetdata);
		return buffer;
	}
	/**
	 * create SYN-ACK packet data from writing back to client stream
	 * @param ipheader
	 * @param tcpheader
	 * @return
	 */
	public Packet createSynAckPacketData(IPv4Header ip, TCPHeader tcp){
		byte[] buffer = null;
		Packet packet = new Packet();
		
		IPv4Header ipheader = IPPacketFactory.copyIPv4Header(ip);
		TCPHeader tcpheader = copyTCPHeader(tcp);
		
		//flip IP from source to dest and vice-versa
		int sourceIp = ipheader.getDestinationIP();
		int destIp = ipheader.getSourceIP();
		int sourcePort = tcpheader.getDestinationPort();
		int destPort = tcpheader.getSourcePort();
		int ackNumber = tcpheader.getSequenceNumber() + 1;
		int seqNumber;
		Random random = new Random();
		seqNumber = random.nextInt();
		if(seqNumber < 0){
			seqNumber = seqNumber * -1;
		}
		ipheader.setDestinationIP(destIp);
		ipheader.setSourceIP(sourceIp);
		tcpheader.setDestinationPort(destPort);
		tcpheader.setSourcePort(sourcePort);
		
		//ack = received sequence + 1
		tcpheader.setAckNumber(ackNumber);
		
		//initial sequence number generated by server
		tcpheader.setSequenceNumber(seqNumber);
		Log.d(TAG,"Set Initial Sequence number: "+seqNumber);
		
		//SYN-ACK
		tcpheader.setIsACK(true);
		tcpheader.setIsSYN(true);
		
		//timestamp in options fields
		tcpheader.setTimeStampReplyTo(tcpheader.getTimeStampSender());
		Date currentdate = new Date();
		int sendertimestamp = (int)currentdate.getTime();
		tcpheader.setTimeStampSender(sendertimestamp);
		
		packet.setIpheader(ipheader);
		packet.setTcpheader(tcpheader);
		
		buffer = this.createPacketData(ipheader, tcpheader, null);
		
		packet.setBuffer(buffer);
		return packet;
	}
	/**
	 * create packet data from IP Header, TCP header and data
	 * @param ipheader IPv4Header object
	 * @param tcpheader TCPHeader object
	 * @param data array of byte (packet body)
	 * @return array of byte
	 */
	public byte[] createPacketData(IPv4Header ipheader, TCPHeader tcpheader, byte[] data){
		int datalength = 0;
		if(data != null){
			datalength = data.length;
		}
		byte[] buffer = new byte[ipheader.getIPHeaderLength() + tcpheader.getTCPHeaderLength() + datalength];
		byte[] ipbuffer = IPPacketFactory.createIPv4HeaderData(ipheader);
		byte[] tcpbuffer = createTCPHeaderData(tcpheader);
		
		System.arraycopy(ipbuffer, 0, buffer, 0, ipbuffer.length);
		System.arraycopy(tcpbuffer, 0, buffer, ipbuffer.length, tcpbuffer.length);
		if(datalength > 0){
			int offset = ipbuffer.length + tcpbuffer.length;
			System.arraycopy(data, 0, buffer, offset, datalength);
		}
		//calculate checksum for both IP and TCP header
		byte[] zero = {0,0};
		//zero out checksum first before calculation
		System.arraycopy(zero, 0, buffer, 10, 2);
		byte[] ipchecksum = PacketUtil.calculateChecksum(buffer, 0, ipbuffer.length);
		//write result of checksum back to buffer
		System.arraycopy(ipchecksum, 0, buffer, 10, 2);
		
		//zero out TCP header checksum first
		int tcpstart = ipbuffer.length;
		System.arraycopy(zero, 0, buffer,tcpstart + 16, 2);
		byte[] tcpchecksum = PacketUtil.calculateTCPHeaderChecksum(buffer, tcpstart, tcpbuffer.length + datalength , 
				ipheader.getDestinationIP(), ipheader.getSourceIP());
		
		//write new checksum back to array
		System.arraycopy(tcpchecksum, 0, buffer,tcpstart + 16, 2);
		
		return buffer;
	}
	
	
	/**
	 * create array of byte from a given TCPHeader object
	 * @param header instance of TCPHeader
	 * @return array of byte
	 */
	public byte[] createTCPHeaderData(TCPHeader header){
		byte[] buffer = new byte[header.getTCPHeaderLength()];
		byte sourcePort1 = (byte)(header.getSourcePort() >> 8);
		byte sourcePort2 = (byte)(header.getSourcePort());
		
		buffer[0] = sourcePort1;
		buffer[1] = sourcePort2;
		
		byte destPort1 = (byte)(header.getDestinationPort() >> 8);
		byte destPort2 = (byte)(header.getDestinationPort());
		buffer[2] = destPort1;
		buffer[3] = destPort2;
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.putInt(header.getSequenceNumber());
		
		//sequence number
		System.arraycopy(buf.array(), 0, buffer, 4, 4);
		
		buf.clear();
		buf.putInt(header.getAckNumber());
		System.arraycopy(buf.array(), 0, buffer, 8, 4);
		
		byte dataoffset = (byte)header.getDataOffset();
		dataoffset <<= 4;
		//set NS flag
		if(header.isNS()){
			dataoffset |= 0x1;
		}
		buffer[12] = dataoffset;
		
		byte flag = (byte)header.getTcpFlags();
		buffer[13] = flag;
		
		byte window1 = (byte)(header.getWindowSize() >> 8);
		byte window2 = (byte)header.getWindowSize();
		buffer[14] = window1;
		buffer[15] = window2;
		
		byte checksum1 = (byte)(header.getChecksum() >> 8);
		byte checksum2 = (byte)header.getChecksum();
		buffer[16] = checksum1;
		buffer[17] = checksum2;
		
		byte urgpointer1 = (byte)(header.getUrgentPointer() >> 8);
		byte urgpointer2 = (byte)header.getUrgentPointer();
		buffer[18] = urgpointer1;
		buffer[19] = urgpointer2;
		
		//set timestamp for both sender and reply to
		byte[] options = header.getOptions();
		byte kind;
		byte len;
		int timeSender = header.getTimeStampSender();
		int timeReplyto = header.getTimeStampReplyTo();
		for(int i =0;i<options.length;i++){
			kind = options[i];
			if(kind > 1){
				if(kind == 8){//timestamp
					i += 2;
					if((i + 7) < options.length){
						PacketUtil.writeIntToBytes(timeSender, options, i);
						i += 4;
						PacketUtil.writeIntToBytes(timeReplyto, options, i);
					}
					break;
				}else if((i+1) < options.length){
					len = options[i+1];
					i = i + len - 1;
				}
			}
		}
		if(header.getOptions().length > 0){
			System.arraycopy(header.getOptions(), 0, buffer, 20, header.getOptions().length);
		}
		return buffer;
	}
	/**
	 * create a TCP Header from a given byte array
	 * @param buffer array of byte
	 * @param start position to start extracting data
	 * @return a new instance of TCPHeader
	 * @throws PacketHeaderException
	 */
	public TCPHeader createTCPHeader(byte[] buffer, int start) throws PacketHeaderException{
		TCPHeader head = null;
		if(buffer.length < start + 20){
			throw new PacketHeaderException("There is not enough space for TCP header from provided starting position");
		}
		int sourcePort = PacketUtil.getNetworkInt(buffer, start, 2);
		int destPort = PacketUtil.getNetworkInt(buffer, start + 2, 2);
		int sequenceNumber = PacketUtil.getNetworkInt(buffer, start + 4, 4);
		int ackNumber = PacketUtil.getNetworkInt(buffer, start + 8, 4);
		int dataOffset = (buffer[start + 12] >> 4) & 0x0F;
		if(dataOffset < 5 && buffer.length == 60){
			dataOffset = 10;
		}else if(dataOffset < 5){
			dataOffset = 5;
		}
		if(buffer.length < (start + dataOffset * 4)){
			throw new PacketHeaderException("invalid array size for TCP header from given starting position");
		}
		
		byte nsbyte = buffer[start + 12];
		boolean isNs = (nsbyte & 0x1) > 0x0;
		
		int tcpflag = PacketUtil.getNetworkInt(buffer, start + 13, 1);
		int windowSize = PacketUtil.getNetworkInt(buffer, start + 14, 2);
		int checksum = PacketUtil.getNetworkInt(buffer, start + 16, 2);
		int urgenpointer = PacketUtil.getNetworkInt(buffer, start + 18, 2);
		byte[] options = null;
		if(dataOffset > 5){
			int optionlength = (dataOffset - 5) * 4;
			
			options = new byte[optionlength];
			System.arraycopy(buffer, start + 20, options, 0, optionlength);
		}else{
			options = new byte[0];
		}
		head = new TCPHeader(sourcePort, destPort, sequenceNumber, dataOffset, isNs, tcpflag, windowSize, checksum, urgenpointer, options, ackNumber);
		this.extractOptionData(head);
		return head;
	}
	void extractOptionData(TCPHeader head){
		byte[] options = head.getOptions();
		byte kind;
		for(int i = 0;i<options.length;i++){
			kind = options[i];
			if(kind == 2){
				i +=2;
				int segsize = PacketUtil.getNetworkInt(options, i, 2);
				head.setMaxSegmentSize(segsize);
				i++;
			}else if(kind == 3){
				i += 2;
				int scale = PacketUtil.getNetworkInt(options, i, 1);
				head.setWindowScale(scale);
			}else if(kind == 4){
				i++;
				head.setSelectiveAckPermitted(true);
			}else if(kind == 5){//SACK => selective acknowledgment
				i++;
				int sacklength = PacketUtil.getNetworkInt(options, i, 1);
				i = i + (sacklength - 2);
				//case 10, 18, 26 and 34
				//TODO: handle missing segments
				//rare case => low priority
			}else if(kind == 8){//timestamp and echo of previous timestamp
				i += 2;
				int timestampSender = PacketUtil.getNetworkInt(options, i, 4);
				i += 4;
				int timestampReplyTo = PacketUtil.getNetworkInt(options, i, 4);
				i += 3;
				head.setTimeStampSender(timestampSender);
				head.setTimeStampReplyTo(timestampReplyTo);
			}
		}
	}
	
}
