package com.att.aro.android.arocollector.ip;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.att.aro.android.arocollector.tcp.PacketHeaderException;
import com.att.aro.android.arocollector.util.PacketUtil;

/**
 * class for creating packet data, header etc related to IP
 * @author Borey Sao
 * Date: June 30, 2014
 */
public class IPPacketFactory {
	public static IPv4Header copyIPv4Header(IPv4Header ipheader){
		IPv4Header ip = new IPv4Header(ipheader.getIpVersion(), ipheader.getInternetHeaderLength(), 
				ipheader.getDscpOrTypeOfService(), ipheader.getEcn(), ipheader.getTotalLength(), ipheader.getIdenfication(), 
				ipheader.isMayFragment(),ipheader.isLastFragment(), ipheader.getFragmentOffset(), ipheader.getTimeToLive(), 
				ipheader.getProtocol(), ipheader.getHeaderChecksum(),ipheader.getSourceIP(), ipheader.getDestinationIP(), 
				ipheader.getOptionBytes());
		return ip;
	}
	/**
	 * create IPv4 Header array of byte from a given IPv4Header object
	 * @param header instance of IPv4Header
	 * @return array of byte
	 */
	public static byte[] createIPv4HeaderData(IPv4Header header){
		byte[] buffer = new byte[header.getIPHeaderLength()];
		byte first = (byte)(header.getInternetHeaderLength() & 0xF);
		first = (byte)(first | 0x40);
		buffer[0] = first;
		byte second = (byte) (header.getDscpOrTypeOfService() << 2);
		byte ecnMask = (byte)(header.getEcn() & 0xFF);
		second = (byte) (second & ecnMask);
		buffer[1] = second;
		
		byte totallength1 = (byte)(header.getTotalLength() >> 8);
		byte totallength2 = (byte)header.getTotalLength();
		buffer[2] = totallength1;
		buffer[3] = totallength2;
		
		byte id1 = (byte)(header.getIdenfication() >> 8);
		byte id2 = (byte)header.getIdenfication();
		buffer[4] = id1;
		buffer[5] = id2;
		
		//combine flags and partial fragment offset
		byte leftfrag = (byte)((header.getFragmentOffset() >> 8) & 0x1F);
		byte flag = (byte) (header.getFlag() | leftfrag);
		buffer[6] = flag;
		byte rightfrag = (byte)header.getFragmentOffset();
		buffer[7] = rightfrag;
		
		byte timeToLive = header.getTimeToLive();
		buffer[8] = timeToLive;
		
		byte protocol = header.getProtocol();
		buffer[9] = protocol;
		
		byte checksum1 = (byte) (header.getHeaderChecksum() >> 8);
		byte checksum2 = (byte)header.getHeaderChecksum();
		buffer[10] = checksum1;
		buffer[11] = checksum2;
		
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.order(ByteOrder.BIG_ENDIAN);
		buf.putInt(0,header.getSourceIP());
		buf.putInt(4,header.getDestinationIP());
		
		//souce ip
		System.arraycopy(buf.array(), 0, buffer, 12, 4);
		//dest ip
		System.arraycopy(buf.array(), 4, buffer, 16, 4);
		
		if(header.getOptionBytes().length > 0){
			System.arraycopy(header.getOptionBytes(), 0, buffer, 20, header.getOptionBytes().length);
		}
		
		return buffer;
	}
	/**
	 * create IPv4 Header from a given array of byte
	 * @param buffer array of byte
	 * @param start position to start extracting data
	 * @return a new instance of IPv4Header
	 * @throws PacketHeaderException
	 */
	public static IPv4Header createIPv4Header(byte[] buffer, int start) throws PacketHeaderException{
		IPv4Header head = null;
		
		//avoid Index out of range
		if( (buffer.length - start) < 20){
			throw new PacketHeaderException("Mininum IPv4 header is 20 bytes. There are less than 20 bytes"
					+ " from start position to the end of array.");
		}
		byte ipversion = (byte) (buffer[start] >> 4);
		if (ipversion != 0x04)
	    {
			throw new PacketHeaderException("Invalid IPv4 header. IP version should be 4.");
	    }
		byte internetHeaderLength = (byte) (buffer[start] & 0x0F);
		if(buffer.length < (start + internetHeaderLength * 4)){
			throw new PacketHeaderException("Not enough space in array for IP header");
		}
		byte dscp = (byte) (buffer[start + 1] >> 2);
		byte ecn = (byte) (buffer[start + 1] & 0x03);
		int totalLength = PacketUtil.getNetworkInt(buffer, start + 2, 2);
		int identification = PacketUtil.getNetworkInt(buffer, start + 4, 2);
		byte flag = buffer[start + 6];
		boolean mayFragment = (flag & 0x40) > 0x00;
		boolean lastFragment = (flag & 0x20) > 0x00;
		int fragmentBits = PacketUtil.getNetworkInt(buffer, start + 6, 2);
		int fragset = fragmentBits & 0x1FFF;
		short fragmentOffset = (short)fragset;
		byte timeToLive = buffer[start + 8];
		byte protocol = buffer[start + 9];
		int checksum = PacketUtil.getNetworkInt(buffer, start + 10, 2);
		int sourceIp = PacketUtil.getNetworkInt(buffer, start + 12, 4);
		int desIp = PacketUtil.getNetworkInt(buffer, start + 16, 4);
		byte[] options = null;
		if(internetHeaderLength == 5){
			options = new byte[0];
		}else{
			int optionlength = (internetHeaderLength - 5) * 4;
			options = new byte[optionlength];
			System.arraycopy(buffer, start + 20, options, 0, optionlength);
		}
		head = new IPv4Header(ipversion, internetHeaderLength, dscp, ecn, totalLength, identification, 
				mayFragment, lastFragment, fragmentOffset, timeToLive, protocol, checksum, sourceIp, 
				desIp, options);
		return head;
	}
}
