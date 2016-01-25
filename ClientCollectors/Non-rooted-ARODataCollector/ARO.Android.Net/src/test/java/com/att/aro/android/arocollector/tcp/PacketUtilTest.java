package com.att.aro.android.arocollector.tcp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;

import static org.junit.Assert.*;

import com.att.aro.android.arocollector.util.PacketUtil;

public class PacketUtilTest {
	//sample packet, total length: 60, ip length: 20, tcp length: 40
	//destination ip: 50.16.213.99:80 => 839964003:80
	//source ip: 10.8.0.1:47580 =>168296449:47580
	byte[] packetdata = {69,0,0,60,14,-59,64,0,64,6,26,123,10,8,0,1,50,16,-43,99,-71,-36,0,80,-110,-6,-89,41,0,0,0,0,-96,2,57,8,92,-125,0,0,2,4,5,-76,4,2,8,10,-1,-1,-84,-88,0,0,0,0,1,3,3,6};
	@Test
	public void writeIntToBytes(){
		byte[] data = new byte[4];
		int value = 1234;
		PacketUtil.writeIntToBytes(value, data, 0);
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.put(data);
		int newvalue = buffer.getInt(0);
		assertEquals(value, newvalue);
	}
	@Test
	public void getNetworkInt(){
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		int num = 123456789;
		buffer.putInt(num);
		byte[] data = buffer.array();
		int value = PacketUtil.getNetworkInt(data, 0, 4);
		assertEquals(value, num);
	}
	@Test
	public void isValidTCPChecksum(){
		int source = 168296449;
		int dest = 839964003;
		boolean ok = PacketUtil.isValidTCPChecksum(source, dest, packetdata, (short) 40, 20);
		assertEquals(true, ok);
				
	}
	@Test
	public void isValidIPChecksum(){
		boolean ok = PacketUtil.isValidIPChecksum(packetdata, 20);
		assertEquals(true,ok);
	}
	@Test
	public void isPacketCorrupted(){
		TCPPacketFactory factory = new TCPPacketFactory();
		TCPHeader tcp = null;
		try {
			tcp = factory.createTCPHeader(packetdata, 20);
			
		} catch (PacketHeaderException e) {
			e.printStackTrace();
		}
		assertNotNull(tcp);
		boolean corrupted = PacketUtil.isPacketCorrupted(tcp);
		assertFalse(corrupted);
		byte[] option = new byte[3];
		option[0] = 0x1;
		option[1] = 0x17; //23
		option[2] = 0;
		tcp.setOptions(option);
		corrupted = PacketUtil.isPacketCorrupted(tcp);
		assertEquals(true, corrupted);
	}
}
