package com.att.aro.core.packetreader.impl;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.*;
import com.att.aro.core.BaseTest;
import com.att.aro.core.packetreader.INativePacketSubscriber;
import com.att.aro.core.packetreader.IPacketListener;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.pcap.PCapAdapter;

public class PacketReaderImplTest extends BaseTest {

	File file;
	IPacketListener listener;
	PCapAdapter adapter;
	PacketReaderImpl reader;
	
	@Before
	public void setup(){
		adapter = Mockito.mock(PCapAdapter.class);
		Mockito.doAnswer(new Answer<Void>(){

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}}).when(adapter).setSubscriber(Mockito.any(INativePacketSubscriber.class));
		Mockito.when(adapter.readData(Mockito.anyString())).thenReturn(null);
		
		file = Mockito.mock(File.class);
		Mockito.when(file.getAbsolutePath()).thenReturn("test");
		
		listener = Mockito.mock(IPacketListener.class);
		Mockito.doNothing().when(listener).packetArrived(Mockito.anyString(), Mockito.any(Packet.class));
	}
	@Test
	public void setAroJpcapLibNameTest(){
		reader = (PacketReaderImpl) context.getBean("packetReader");
		reader.setAroJpcapLibName("Windows", "64");
		String libname = reader.getAroJpcapLibFileName();
		assertEquals("jpcap64.dll", libname);
		
		reader.setAroJpcapLibName("Windows", "86");
		libname = reader.getAroJpcapLibFileName();
		assertEquals("jpcap.dll", libname);
		
		reader.setAroJpcapLibName("Linux", "amd64");
		libname = reader.getAroJpcapLibFileName();
		assertEquals("libjpcap64.so", libname);
		
		reader.setAroJpcapLibName("Linux", "i386");
		libname = reader.getAroJpcapLibFileName();
		assertEquals("libjpcap32.so", libname);
		
		reader.setAroJpcapLibName("MacOS", "64");
		libname = reader.getAroJpcapLibFileName();
		assertEquals("libjpcap.jnilib", libname);
	}
	
	@Test
	public void readPacket() throws IOException{
		reader = (PacketReaderImpl) context.getBean("packetReader");
		reader.setAdapter(adapter);
		reader.readPacket(file.getAbsolutePath(), listener);
		
		String nativelibname = reader.getAroJpcapLibFileName();
		
		byte[] data = new byte[20];
		reader.receive(12, 1, 1, 1, data);
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void readPacketError() throws IOException{
		reader = (PacketReaderImpl) context.getBean("packetReader");
		reader.setAdapter(adapter);
		reader.readPacket(file.getAbsolutePath(), null);
		
	}
	
	@Test(expected=IOException.class)
	public void readPacketError2() throws IOException{
		reader = (PacketReaderImpl) context.getBean("packetReader");
		reader.setAdapter(adapter);
		Mockito.when(adapter.readData(Mockito.anyString())).thenReturn("not null");
		reader.readPacket(file.getAbsolutePath(), listener);
		
	}
	
}
