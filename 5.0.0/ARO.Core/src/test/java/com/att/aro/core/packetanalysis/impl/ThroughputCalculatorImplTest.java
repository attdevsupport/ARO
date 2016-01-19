package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.IThroughputCalculator;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Throughput;
import com.att.aro.core.packetreader.pojo.PacketDirection;

public class ThroughputCalculatorImplTest extends BaseTest{
	
	@InjectMocks
	IThroughputCalculator throughputCalculator;

	
	 @Before 
	 public void setUp()
	 {
		 throughputCalculator = context.getBean(IThroughputCalculator.class);
	 }
	 
	 @Test
	 public void calculateThroughput_PacketListIsNull(){
		 
		 List<PacketInfo> packets = new ArrayList<PacketInfo>(); 
		 List<Throughput> testResult = new ArrayList<Throughput>();
		 testResult = throughputCalculator.calculateThroughput(0.0, 0.0, 0.0, packets);
		 assertEquals(true,testResult.isEmpty());
	 }

	 @Test
	 public void calculateThroughput_a(){
		 Date date = new Date();
		 List<Throughput> testResult = new ArrayList<Throughput>();
		 List<PacketInfo> packets = new ArrayList<PacketInfo>(); 
		 PacketInfo pktInfo01 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo01.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo01.getTimeStamp()).thenReturn(date.getTime()+5000.0);
		 packets.add(pktInfo01);
		 
		 PacketInfo pktInfo02 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo02.getDir()).thenReturn(PacketDirection.DOWNLINK);
		 Mockito.when(pktInfo02.getTimeStamp()).thenReturn(date.getTime()+8000.0);
		 packets.add(pktInfo02);
		 
		 PacketInfo pktInfo03 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo03.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo03.getTimeStamp()).thenReturn(date.getTime()+22000.0);
		 packets.add(pktInfo03);
		 
		 PacketInfo pktInfo04 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo04.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo04.getTimeStamp()).thenReturn(date.getTime()+23000.0);
		 packets.add(pktInfo04);
		 
		 PacketInfo pktInfo05 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo05.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo05.getTimeStamp()).thenReturn(date.getTime()+70000.0);
		 packets.add(pktInfo05);
		 
		 PacketInfo pktInfo06 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo06.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo06.getTimeStamp()).thenReturn(date.getTime()+72000.0);
		 packets.add(pktInfo06);		 
		 testResult = throughputCalculator.calculateThroughput(date.getTime()+0.0, date.getTime()+100000.0, 15000.0, packets);
		 assertEquals(7,testResult.size());
		 
	 }
	 
	 // condition, head.getTimeStamp() > beginTS, tail.getTimeStamp() > maxTS , adjust input the variable 
	 @Test
	 public void calculateThroughput_b(){
		 Date date = new Date();
		 List<Throughput> testResult = new ArrayList<Throughput>();
		 List<PacketInfo> packets = new ArrayList<PacketInfo>(); 
		 PacketInfo pktInfo01 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo01.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo01.getTimeStamp()).thenReturn(date.getTime()+20000.0);
		 packets.add(pktInfo01);
		 
		 PacketInfo pktInfo02 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo02.getDir()).thenReturn(PacketDirection.DOWNLINK);
		 Mockito.when(pktInfo02.getTimeStamp()).thenReturn(date.getTime()+20400.0);
		 packets.add(pktInfo02);
		 
		 PacketInfo pktInfo03 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo03.getDir()).thenReturn(PacketDirection.DOWNLINK);
		 Mockito.when(pktInfo03.getTimeStamp()).thenReturn(date.getTime()+20500.0);
		 packets.add(pktInfo03);
		 
		 PacketInfo pktInfo04 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo04.getDir()).thenReturn(PacketDirection.DOWNLINK);
		 Mockito.when(pktInfo04.getTimeStamp()).thenReturn(date.getTime()+41000.0);
		 packets.add(pktInfo04);
		 
		 PacketInfo pktInfo05 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo05.getDir()).thenReturn(PacketDirection.DOWNLINK);
		 Mockito.when(pktInfo05.getTimeStamp()).thenReturn(date.getTime()+71000.0);
		 packets.add(pktInfo05);
		 
		 PacketInfo pktInfo06 = Mockito.mock(PacketInfo.class);
		 Mockito.when(pktInfo06.getDir()).thenReturn(PacketDirection.UPLINK);
		 Mockito.when(pktInfo06.getTimeStamp()).thenReturn(date.getTime()+72000.0);
		 packets.add(pktInfo06);	 
		 
		 testResult = throughputCalculator.calculateThroughput(date.getTime()+0.0, date.getTime()+73000.0, 60000.0, packets);
		 assertEquals(2,testResult.size());
	 }

	 

}
