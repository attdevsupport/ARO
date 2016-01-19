package com.att.aro.core.packetreader.impl;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.junit.Before;
import com.att.aro.core.BaseTest;
import com.att.aro.core.packetreader.IPacketListener;
import com.att.aro.core.packetreader.IPacketReader;
import com.att.aro.core.packetreader.pojo.Packet;
@Deprecated
public class NetmonPacketReaderImplTest extends BaseTest {

	NetmonAdapter netmon;
	File file;
	IPacketListener listener;
	IPacketReader reader;
	NetmonPacketReaderImpl netmonreader;
	@Before
	public void setup(){
		netmon = Mockito.mock(NetmonAdapter.class);
		Mockito.doAnswer(new Answer<Void>(){
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}}).when(netmon).loadNativeLibs();
		Mockito.when(netmon.parseTraceFile(Mockito.anyString())).thenReturn(0);
		file = Mockito.mock(File.class);
		Mockito.when(file.getAbsolutePath()).thenReturn("test");
		listener = Mockito.mock(IPacketListener.class);
		Mockito.doNothing().when(listener).packetArrived(Mockito.anyString(), Mockito.any(Packet.class));
	}
	
	@Test
	public void readPacket() throws IOException{
		reader = (IPacketReader) context.getBean("netmonPacketReader");
		netmonreader = (NetmonPacketReaderImpl)reader;
		netmonreader.setNetmon(netmon);
		netmonreader.readPacket(file.getAbsolutePath(), listener);
		byte[] data = new byte[20];
		netmonreader.receiveNetmonPacket(12, 1, 1, 1, data, "test");
		netmonreader.receiveNetmonPacket(0xf000, 1, 1, 1, data, "test");
	}
	@Test(expected=IllegalArgumentException.class)
	public void readPacketError() throws IOException{
		reader = (IPacketReader) context.getBean("netmonPacketReader");
		netmonreader = (NetmonPacketReaderImpl)reader;
		netmonreader.setNetmon(netmon);
		netmonreader.readPacket(file.getAbsolutePath(), null);
	}
	@Test(expected=IOException.class)
	public void readPacketError2() throws IOException{
		Mockito.when(netmon.parseTraceFile(Mockito.anyString())).thenReturn(1);
		reader = (IPacketReader) context.getBean("netmonPacketReader");
		netmonreader = (NetmonPacketReaderImpl)reader;
		netmonreader.setNetmon(netmon);
		netmonreader.readPacket(file.getAbsolutePath(), listener);
		
	}
}
