/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.IBurstCollectionAnalysis;
import com.att.aro.core.packetanalysis.IRequestResponseBuilder;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.packetreader.pojo.TCPPacket;
import com.att.aro.core.packetreader.pojo.UDPPacket;
import com.att.aro.core.peripheral.pojo.CpuActivity;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;

public class BurstCollectionAnalysisImplTest extends BaseTest{
	
	
	BurstCollectionAnalysisImpl aBurstCollectionAnalysis;

	@Mock
	IRequestResponseBuilder requestResponseBuilder;
	
	@Before
	public void setUp(){
		aBurstCollectionAnalysis = (BurstCollectionAnalysisImpl) context.getBean(IBurstCollectionAnalysis.class);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void analyzeTest(){
//		Date date = new Date();
		
		InetAddress iAdr = Mockito.mock(InetAddress.class);
		InetAddress iAdr1 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr1.getAddress()).thenReturn(new byte[]{72,12,13,1});
		
		Set<InetAddress> inetSet = new HashSet<InetAddress>();
		inetSet.add(iAdr);
		inetSet.add(iAdr1);
		
		
		DomainNameSystem dns = Mockito.mock(DomainNameSystem.class);
		Mockito.when(dns.getIpAddresses()).thenReturn(inetSet);
		Mockito.when(dns.getDomainName()).thenReturn("www.att.com");
		
		
		//UDP Packet Mock
		UDPPacket udpPacket = Mockito.mock(UDPPacket.class);
		Mockito.when(udpPacket.isDNSPacket()).thenReturn(true);
		Mockito.when(udpPacket.getDns()).thenReturn(dns);
		Mockito.when(udpPacket.getSourcePort()).thenReturn(83);
		Mockito.when(udpPacket.getDestinationPort()).thenReturn(84);
		Mockito.when(udpPacket.getDestinationIPAddress()).thenReturn(iAdr);
		
		PacketInfo packetInfo1 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo1.getPacket()).thenReturn(udpPacket);
		Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo1.getPayloadLen()).thenReturn(30);
		Mockito.when(packetInfo1.getLen()).thenReturn(10);
		Mockito.when(packetInfo1.getAppName()).thenReturn("Test1");
		Mockito.when(packetInfo1.getTcpFlagString()).thenReturn("TestString");
		Mockito.when(packetInfo1.getTimeStamp()).thenReturn(500d);
		
		InetAddress iAdr2 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr2.getAddress()).thenReturn(new byte[]{95,10,1,1});
		
		TCPPacket tcpPacket = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket.getSourcePort()).thenReturn(81);
		Mockito.when(tcpPacket.getDestinationPort()).thenReturn(82);
		Mockito.when(tcpPacket.getDestinationIPAddress()).thenReturn(iAdr2);
		Mockito.when(tcpPacket.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket.isRST()).thenReturn(true);
		Mockito.when(tcpPacket.getTimeStamp()).thenReturn(1000d);
		//Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-10000);
		
		PacketInfo packetInfo2 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo2.getPacket()).thenReturn(tcpPacket);
		Mockito.when(packetInfo2.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo2.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo2.getPayloadLen()).thenReturn(25);
		Mockito.when(packetInfo2.getLen()).thenReturn(15);
		Mockito.when(packetInfo2.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo2.getTcpFlagString()).thenReturn("Test2String");
		Mockito.when(packetInfo2.getTimeStamp()).thenReturn(10d);
		
		TCPPacket tcpPacket2 = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket2.getSourcePort()).thenReturn(95);
		Mockito.when(tcpPacket2.getDestinationPort()).thenReturn(99);
		Mockito.when(tcpPacket2.getDestinationIPAddress()).thenReturn(iAdr2);
		Mockito.when(tcpPacket2.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket2.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket2.isRST()).thenReturn(false);
		Mockito.when(tcpPacket2.getTimeStamp()).thenReturn(50d);
		
		PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK_RECOVER);
		Mockito.when(packetInfo3.getPayloadLen()).thenReturn(15);
		Mockito.when(packetInfo3.getLen()).thenReturn(20);
		Mockito.when(packetInfo3.getAppName()).thenReturn("Test3");
		Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test3String");
		Mockito.when(packetInfo3.getTimeStamp()).thenReturn(700d);
		
		PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo4.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo4.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK);
		Mockito.when(packetInfo4.getTimeStamp()).thenReturn(90d);
		Mockito.when(packetInfo4.getPayloadLen()).thenReturn(10);
		
		PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_DATA_RECOVER);
		Mockito.when(packetInfo5.getTimeStamp()).thenReturn(750d);
		Mockito.when(packetInfo5.getPayloadLen()).thenReturn(5);
		
		List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
		packetsList.add(packetInfo1); //Adding UDP Packet to the list
		packetsList.add(packetInfo2); //Adding TCP Packet to the list
		packetsList.add(packetInfo3);
		packetsList.add(packetInfo4);
		packetsList.add(packetInfo5);
		
		
		ProfileWiFi profile = Mockito.mock(ProfileWiFi.class);
		Mockito.when(profile.getBurstTh()).thenReturn(50d);
		Mockito.when(profile.getLongBurstTh()).thenReturn(40.0d);
		Mockito.when(profile.getLargeBurstDuration()).thenReturn(150d);
		Mockito.when(profile.getLargeBurstSize()).thenReturn(50);
		Mockito.when(profile.getProfileType()).thenReturn(ProfileType.WIFI);
		Mockito.when(profile.getWifiTailTime()).thenReturn(25.0d);
		Mockito.when(profile.getWifiIdlePower()).thenReturn(50.0d);
		Mockito.when(profile.getWifiTailTime()).thenReturn(75.0d);

		
		Map<Integer, Integer> packetSizeToCountMap = new HashMap<Integer, Integer>();
		packetSizeToCountMap.put(1001, 10);
		packetSizeToCountMap.put(2002, 20);
		packetSizeToCountMap.put(3003, 30);
		
		RrcStateRange rrcStateRange1 = Mockito.mock(RrcStateRange.class);
		Mockito.when(rrcStateRange1.getBeginTime()).thenReturn(500d);
		Mockito.when(rrcStateRange1.getEndTime()).thenReturn(490d);
		Mockito.when(rrcStateRange1.getState()).thenReturn(RRCState.TAIL_DCH);
		
		RrcStateRange rrcStateRange2 = Mockito.mock(RrcStateRange.class);
		Mockito.when(rrcStateRange2.getBeginTime()).thenReturn(8.30d);
		Mockito.when(rrcStateRange2.getEndTime()).thenReturn(12.30d);
		Mockito.when(rrcStateRange2.getState()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		RrcStateRange rrcStateRange3 = Mockito.mock(RrcStateRange.class);
		Mockito.when(rrcStateRange3.getBeginTime()).thenReturn(0.0d);
		Mockito.when(rrcStateRange3.getEndTime()).thenReturn(-2.0d);
		Mockito.when(rrcStateRange3.getState()).thenReturn(RRCState.TAIL_DCH);
		
		RrcStateRange rrcStateRange4 = Mockito.mock(RrcStateRange.class);
		Mockito.when(rrcStateRange4.getBeginTime()).thenReturn(25d);
		Mockito.when(rrcStateRange4.getEndTime()).thenReturn(75d);
		Mockito.when(rrcStateRange4.getState()).thenReturn(RRCState.WIFI_TAIL);
		
		RrcStateRange rrcStateRange5 = Mockito.mock(RrcStateRange.class);
		Mockito.when(rrcStateRange5.getBeginTime()).thenReturn(55d);
		Mockito.when(rrcStateRange5.getEndTime()).thenReturn(95d);
		Mockito.when(rrcStateRange5.getState()).thenReturn(RRCState.PROMO_IDLE_DCH);
		
		List<RrcStateRange> rrcstaterangelist = new ArrayList<RrcStateRange>();
		rrcstaterangelist.add(rrcStateRange1);
		rrcstaterangelist.add(rrcStateRange2);
		rrcstaterangelist.add(rrcStateRange3);
		rrcstaterangelist.add(rrcStateRange4);
		rrcstaterangelist.add(rrcStateRange5);
		
		UserEvent uEvent1 = Mockito.mock(UserEvent.class);
		Mockito.when(uEvent1.getEventType()).thenReturn(UserEventType.SCREEN_LANDSCAPE);
		Mockito.when(uEvent1.getPressTime()).thenReturn(503d);
		Mockito.when(uEvent1.getReleaseTime()).thenReturn(6d);
		
		UserEvent uEvent2 = Mockito.mock(UserEvent.class);
		Mockito.when(uEvent2.getEventType()).thenReturn(UserEventType.SCREEN_PORTRAIT);
		Mockito.when(uEvent2.getPressTime()).thenReturn(14d);
		Mockito.when(uEvent2.getReleaseTime()).thenReturn(2000d);
		
		UserEvent uEvent3 = Mockito.mock(UserEvent.class);
		Mockito.when(uEvent3.getEventType()).thenReturn(UserEventType.KEY_RED);
		Mockito.when(uEvent3.getPressTime()).thenReturn(497d);
		Mockito.when(uEvent3.getReleaseTime()).thenReturn(499d);
		
		UserEvent uEvent4 = Mockito.mock(UserEvent.class);
		Mockito.when(uEvent4.getEventType()).thenReturn(UserEventType.EVENT_UNKNOWN);
		Mockito.when(uEvent4.getPressTime()).thenReturn(25d);
		Mockito.when(uEvent4.getReleaseTime()).thenReturn(4d);
		
		UserEvent uEvent5 = Mockito.mock(UserEvent.class);
		Mockito.when(uEvent5.getEventType()).thenReturn(UserEventType.KEY_SEARCH);
		Mockito.when(uEvent5.getPressTime()).thenReturn(752d);
		Mockito.when(uEvent5.getReleaseTime()).thenReturn(30000d);
		
		List<UserEvent> uEventList = new ArrayList<UserEvent>();
		uEventList.add(uEvent1);
		uEventList.add(uEvent2);
		uEventList.add(uEvent3);
		uEventList.add(uEvent4);
		uEventList.add(uEvent5);
		
		CpuActivity cActivity1 = Mockito.mock(CpuActivity.class);
		Mockito.when(cActivity1.getTimeStamp()).thenReturn(23000d);
		Mockito.when(cActivity1.getTotalCpuUsage()).thenReturn(5000d);
		
		CpuActivity cActivity2 = Mockito.mock(CpuActivity.class);
		Mockito.when(cActivity2.getTimeStamp()).thenReturn(24000d);
		Mockito.when(cActivity2.getTotalCpuUsage()).thenReturn(6000d);
		
		CpuActivity cActivity3 = Mockito.mock(CpuActivity.class);
		Mockito.when(cActivity3.getTimeStamp()).thenReturn(25000d);
		Mockito.when(cActivity3.getTotalCpuUsage()).thenReturn(6000d);
		
		List<CpuActivity> cpuActivityList = new ArrayList<CpuActivity>();
		cpuActivityList.add(cActivity1);
		cpuActivityList.add(cActivity2);
		cpuActivityList.add(cActivity3);
		
		Session aSession = Mockito.mock(Session.class);
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(aSession);
		
		BurstCollectionAnalysisData bcaData = aBurstCollectionAnalysis.analyze(packetsList, profile, packetSizeToCountMap, rrcstaterangelist, uEventList, cpuActivityList, sessionList);
		
			assertEquals(2, bcaData.getBurstAnalysisInfo().size());
			assertEquals(3, bcaData.getBurstCollection().size());
			assertEquals(0, bcaData.getLongBurstCount());
			assertEquals(0, (int)bcaData.getTotalEnergy());
		
	}	
	
	
		@Test
		public void analyze2Test(){
//			Date date = new Date();
			
			InetAddress iAdr = Mockito.mock(InetAddress.class);
			InetAddress iAdr1 = Mockito.mock(InetAddress.class);
			Mockito.when(iAdr.getAddress()).thenReturn(new byte[]{89,10,1,1});
			Mockito.when(iAdr1.getAddress()).thenReturn(new byte[]{72,12,13,1});
			
			Set<InetAddress> inetSet = new HashSet<InetAddress>();
			inetSet.add(iAdr);
			inetSet.add(iAdr1);
			
			
			DomainNameSystem dns = Mockito.mock(DomainNameSystem.class);
			Mockito.when(dns.getIpAddresses()).thenReturn(inetSet);
			Mockito.when(dns.getDomainName()).thenReturn("www.att.com");
			
			
			//UDP Packet Mock
			UDPPacket udpPacket = Mockito.mock(UDPPacket.class);
			Mockito.when(udpPacket.isDNSPacket()).thenReturn(true);
			Mockito.when(udpPacket.getDns()).thenReturn(dns);
			Mockito.when(udpPacket.getSourcePort()).thenReturn(83);
			Mockito.when(udpPacket.getDestinationPort()).thenReturn(84);
			Mockito.when(udpPacket.getDestinationIPAddress()).thenReturn(iAdr);
			
			PacketInfo packetInfo1 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo1.getPacket()).thenReturn(udpPacket);
			Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
			Mockito.when(packetInfo1.getPayloadLen()).thenReturn(0);
			Mockito.when(packetInfo1.getLen()).thenReturn(10);
			Mockito.when(packetInfo1.getAppName()).thenReturn("Test1");
			Mockito.when(packetInfo1.getTcpFlagString()).thenReturn("TestString");
			Mockito.when(packetInfo1.getTimeStamp()).thenReturn(500d);
			
			InetAddress iAdr2 = Mockito.mock(InetAddress.class);
			Mockito.when(iAdr2.getAddress()).thenReturn(new byte[]{95,10,1,1});
			
			TCPPacket tcpPacket = Mockito.mock(TCPPacket.class);
			Mockito.when(tcpPacket.getSourcePort()).thenReturn(81);
			Mockito.when(tcpPacket.getDestinationPort()).thenReturn(82);
			Mockito.when(tcpPacket.getDestinationIPAddress()).thenReturn(iAdr2);
			Mockito.when(tcpPacket.isSYN()).thenReturn(true);
			Mockito.when(tcpPacket.isFIN()).thenReturn(true);
			Mockito.when(tcpPacket.isRST()).thenReturn(true);
			Mockito.when(tcpPacket.getTimeStamp()).thenReturn(1000d);
			//Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-10000);
			
			PacketInfo packetInfo2 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo2.getPacket()).thenReturn(tcpPacket);
			Mockito.when(packetInfo2.getDir()).thenReturn(PacketDirection.UPLINK);
			Mockito.when(packetInfo2.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
			Mockito.when(packetInfo2.getPayloadLen()).thenReturn(0);
			Mockito.when(packetInfo2.getLen()).thenReturn(15);
			Mockito.when(packetInfo2.getAppName()).thenReturn("Test2");
			Mockito.when(packetInfo2.getTcpFlagString()).thenReturn("Test2String");
			Mockito.when(packetInfo2.getTimeStamp()).thenReturn(10d);
			
			TCPPacket tcpPacket2 = Mockito.mock(TCPPacket.class);
			Mockito.when(tcpPacket2.getSourcePort()).thenReturn(95);
			Mockito.when(tcpPacket2.getDestinationPort()).thenReturn(99);
			Mockito.when(tcpPacket2.getDestinationIPAddress()).thenReturn(iAdr2);
			Mockito.when(tcpPacket2.isSYN()).thenReturn(true);
			Mockito.when(tcpPacket2.isFIN()).thenReturn(true);
			Mockito.when(tcpPacket2.isRST()).thenReturn(false);
			Mockito.when(tcpPacket2.getTimeStamp()).thenReturn(50d);
			
			PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
			Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.DOWNLINK);
			Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK_RECOVER);
			Mockito.when(packetInfo3.getPayloadLen()).thenReturn(0);
			Mockito.when(packetInfo3.getLen()).thenReturn(20);
			Mockito.when(packetInfo3.getAppName()).thenReturn("Test3");
			Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test3String");
			Mockito.when(packetInfo3.getTimeStamp()).thenReturn(0d);
			
			PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo4.getPacket()).thenReturn(tcpPacket2);
			Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
			Mockito.when(packetInfo4.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK);
			Mockito.when(packetInfo4.getTimeStamp()).thenReturn(90d);
			Mockito.when(packetInfo4.getPayloadLen()).thenReturn(0);
			
			PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket2);
			Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.DOWNLINK);
			Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_DATA_RECOVER);
			Mockito.when(packetInfo5.getTimeStamp()).thenReturn(750d);
			Mockito.when(packetInfo5.getPayloadLen()).thenReturn(0);
			
			List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
			packetsList.add(packetInfo1); //Adding UDP Packet to the list
			packetsList.add(packetInfo2); //Adding TCP Packet to the list
			packetsList.add(packetInfo3);
			packetsList.add(packetInfo4);
			packetsList.add(packetInfo5);
			
			
			Profile3G profile = Mockito.mock(Profile3G.class);
			Mockito.when(profile.getBurstTh()).thenReturn(50d);
			Mockito.when(profile.getLongBurstTh()).thenReturn(40.0d);
			Mockito.when(profile.getLargeBurstDuration()).thenReturn(150d);
			Mockito.when(profile.getLargeBurstSize()).thenReturn(50);
			Mockito.when(profile.getProfileType()).thenReturn(ProfileType.T3G);
			Mockito.when(profile.getBurstTh()).thenReturn(25.0d);
			Mockito.when(profile.getCarrier()).thenReturn("ATT");
			Mockito.when(profile.getCloseSpacedBurstThreshold()).thenReturn(45d);
			Mockito.when(profile.getDchFachTimer()).thenReturn(50.0d);
			Mockito.when(profile.getDchTimerResetSize()).thenReturn(75);
			Mockito.when(profile.getDchTimerResetWin()).thenReturn(75d);
			Mockito.when(profile.getDevice()).thenReturn("lg");
			Mockito.when(profile.getFachDchPromoAvg()).thenReturn(100d);
			Mockito.when(profile.getFachDchPromoMax()).thenReturn(200d);
			Mockito.when(profile.getIdleDchPromoMin()).thenReturn(50d);
			Mockito.when(profile.getLargeBurstDuration()).thenReturn(500d);
			Mockito.when(profile.getLargeBurstSize()).thenReturn(1000);
			Mockito.when(profile.getLongBurstTh()).thenReturn(250d);

			
			Map<Integer, Integer> packetSizeToCountMap = new HashMap<Integer, Integer>();
			packetSizeToCountMap.put(1001, 10);
			packetSizeToCountMap.put(2002, 20);
			packetSizeToCountMap.put(3003, 30);
			
			RrcStateRange rrcStateRange1 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange1.getBeginTime()).thenReturn(500d);
			Mockito.when(rrcStateRange1.getEndTime()).thenReturn(590d);
			Mockito.when(rrcStateRange1.getState()).thenReturn(RRCState.TAIL_DCH);
			
			RrcStateRange rrcStateRange2 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange2.getBeginTime()).thenReturn(8.30d);
			Mockito.when(rrcStateRange2.getEndTime()).thenReturn(12.30d);
			Mockito.when(rrcStateRange2.getState()).thenReturn(RRCState.PROMO_FACH_DCH);
			
			RrcStateRange rrcStateRange3 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange3.getBeginTime()).thenReturn(0.0d);
			Mockito.when(rrcStateRange3.getEndTime()).thenReturn(-2.0d);
			Mockito.when(rrcStateRange3.getState()).thenReturn(RRCState.TAIL_DCH);
			
			RrcStateRange rrcStateRange4 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange4.getBeginTime()).thenReturn(25d);
			Mockito.when(rrcStateRange4.getEndTime()).thenReturn(75d);
			Mockito.when(rrcStateRange4.getState()).thenReturn(RRCState.WIFI_TAIL);
			
			RrcStateRange rrcStateRange5 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange5.getBeginTime()).thenReturn(105d);
			Mockito.when(rrcStateRange5.getEndTime()).thenReturn(95d);
			Mockito.when(rrcStateRange5.getState()).thenReturn(RRCState.PROMO_IDLE_DCH);
			
			List<RrcStateRange> rrcstaterangelist = new ArrayList<RrcStateRange>();
			rrcstaterangelist.add(rrcStateRange1);
			rrcstaterangelist.add(rrcStateRange2);
			rrcstaterangelist.add(rrcStateRange3);
			rrcstaterangelist.add(rrcStateRange4);
			rrcstaterangelist.add(rrcStateRange5);
			
			UserEvent uEvent1 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent1.getEventType()).thenReturn(UserEventType.SCREEN_LANDSCAPE);
			Mockito.when(uEvent1.getPressTime()).thenReturn(503d);
			Mockito.when(uEvent1.getReleaseTime()).thenReturn(6d);
			
			UserEvent uEvent2 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent2.getEventType()).thenReturn(UserEventType.SCREEN_PORTRAIT);
			Mockito.when(uEvent2.getPressTime()).thenReturn(14d);
			Mockito.when(uEvent2.getReleaseTime()).thenReturn(2000d);
			
			UserEvent uEvent3 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent3.getEventType()).thenReturn(UserEventType.KEY_RED);
			Mockito.when(uEvent3.getPressTime()).thenReturn(497d);
			Mockito.when(uEvent3.getReleaseTime()).thenReturn(499d);
			
			UserEvent uEvent4 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent4.getEventType()).thenReturn(UserEventType.EVENT_UNKNOWN);
			Mockito.when(uEvent4.getPressTime()).thenReturn(25d);
			Mockito.when(uEvent4.getReleaseTime()).thenReturn(4d);
			
			UserEvent uEvent5 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent5.getEventType()).thenReturn(UserEventType.KEY_SEARCH);
			Mockito.when(uEvent5.getPressTime()).thenReturn(752d);
			Mockito.when(uEvent5.getReleaseTime()).thenReturn(30000d);
			
			List<UserEvent> uEventList = new ArrayList<UserEvent>();
			uEventList.add(uEvent1);
			uEventList.add(uEvent2);
			uEventList.add(uEvent3);
			uEventList.add(uEvent4);
			uEventList.add(uEvent5);
			
			CpuActivity cActivity1 = Mockito.mock(CpuActivity.class);
			Mockito.when(cActivity1.getTimeStamp()).thenReturn(23000d);
			Mockito.when(cActivity1.getTotalCpuUsage()).thenReturn(5000d);
			
			CpuActivity cActivity2 = Mockito.mock(CpuActivity.class);
			Mockito.when(cActivity2.getTimeStamp()).thenReturn(24000d);
			Mockito.when(cActivity2.getTotalCpuUsage()).thenReturn(6000d);
			
			CpuActivity cActivity3 = Mockito.mock(CpuActivity.class);
			Mockito.when(cActivity3.getTimeStamp()).thenReturn(25000d);
			Mockito.when(cActivity3.getTotalCpuUsage()).thenReturn(6000d);
			
			List<CpuActivity> cpuActivityList = new ArrayList<CpuActivity>();
			cpuActivityList.add(cActivity1);
			cpuActivityList.add(cActivity2);
			cpuActivityList.add(cActivity3);
			
			Session aSession = Mockito.mock(Session.class);
			List<Session> sessionList = new ArrayList<Session>();
			sessionList.add(aSession);
			
			BurstCollectionAnalysisData bcaData = aBurstCollectionAnalysis.analyze(packetsList, profile, packetSizeToCountMap, rrcstaterangelist, uEventList, cpuActivityList, sessionList);
			
			if(bcaData != null){
				assertEquals(3, bcaData.getBurstAnalysisInfo().size());
				assertEquals(3, bcaData.getBurstCollection().size());
				assertEquals(0, bcaData.getLongBurstCount());
				assertEquals(0, (int)bcaData.getTotalEnergy());
			}
			
	}
	
		@Test
		public void analyze3Test(){
//			Date date = new Date();
			
			InetAddress iAdr = Mockito.mock(InetAddress.class);
			InetAddress iAdr1 = Mockito.mock(InetAddress.class);
			Mockito.when(iAdr.getAddress()).thenReturn(new byte[]{89,10,1,1});
			Mockito.when(iAdr1.getAddress()).thenReturn(new byte[]{72,12,13,1});
			
			Set<InetAddress> inetSet = new HashSet<InetAddress>();
			inetSet.add(iAdr);
			inetSet.add(iAdr1);
			
			
			DomainNameSystem dns = Mockito.mock(DomainNameSystem.class);
			Mockito.when(dns.getIpAddresses()).thenReturn(inetSet);
			Mockito.when(dns.getDomainName()).thenReturn("www.att.com");
			
			
			//UDP Packet Mock
			UDPPacket udpPacket = Mockito.mock(UDPPacket.class);
			Mockito.when(udpPacket.isDNSPacket()).thenReturn(true);
			Mockito.when(udpPacket.getDns()).thenReturn(dns);
			Mockito.when(udpPacket.getSourcePort()).thenReturn(83);
			Mockito.when(udpPacket.getDestinationPort()).thenReturn(84);
			Mockito.when(udpPacket.getDestinationIPAddress()).thenReturn(iAdr);
			
			PacketInfo packetInfo1 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo1.getPacket()).thenReturn(udpPacket);
			Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
			Mockito.when(packetInfo1.getPayloadLen()).thenReturn(0);
			Mockito.when(packetInfo1.getLen()).thenReturn(10);
			Mockito.when(packetInfo1.getAppName()).thenReturn("Test1");
			Mockito.when(packetInfo1.getTcpFlagString()).thenReturn("TestString");
			Mockito.when(packetInfo1.getTimeStamp()).thenReturn(500d);
			
			InetAddress iAdr2 = Mockito.mock(InetAddress.class);
			Mockito.when(iAdr2.getAddress()).thenReturn(new byte[]{95,10,1,1});
			
			TCPPacket tcpPacket = Mockito.mock(TCPPacket.class);
			Mockito.when(tcpPacket.getSourcePort()).thenReturn(81);
			Mockito.when(tcpPacket.getDestinationPort()).thenReturn(82);
			Mockito.when(tcpPacket.getDestinationIPAddress()).thenReturn(iAdr2);
			Mockito.when(tcpPacket.isSYN()).thenReturn(true);
			Mockito.when(tcpPacket.isFIN()).thenReturn(true);
			Mockito.when(tcpPacket.isRST()).thenReturn(true);
			Mockito.when(tcpPacket.getTimeStamp()).thenReturn(1000d);
			//Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-10000);
			
			PacketInfo packetInfo2 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo2.getPacket()).thenReturn(tcpPacket);
			Mockito.when(packetInfo2.getDir()).thenReturn(PacketDirection.UPLINK);
			Mockito.when(packetInfo2.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
			Mockito.when(packetInfo2.getPayloadLen()).thenReturn(0);
			Mockito.when(packetInfo2.getLen()).thenReturn(15);
			Mockito.when(packetInfo2.getAppName()).thenReturn("Test2");
			Mockito.when(packetInfo2.getTcpFlagString()).thenReturn("Test2String");
			Mockito.when(packetInfo2.getTimeStamp()).thenReturn(10d);
			
			TCPPacket tcpPacket2 = Mockito.mock(TCPPacket.class);
			Mockito.when(tcpPacket2.getSourcePort()).thenReturn(95);
			Mockito.when(tcpPacket2.getDestinationPort()).thenReturn(99);
			Mockito.when(tcpPacket2.getDestinationIPAddress()).thenReturn(iAdr2);
			Mockito.when(tcpPacket2.isSYN()).thenReturn(true);
			Mockito.when(tcpPacket2.isFIN()).thenReturn(true);
			Mockito.when(tcpPacket2.isRST()).thenReturn(false);
			Mockito.when(tcpPacket2.getTimeStamp()).thenReturn(50d);
			
			PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
			Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.DOWNLINK);
			Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK_RECOVER);
			Mockito.when(packetInfo3.getPayloadLen()).thenReturn(0);
			Mockito.when(packetInfo3.getLen()).thenReturn(20);
			Mockito.when(packetInfo3.getAppName()).thenReturn("Test3");
			Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test3String");
			Mockito.when(packetInfo3.getTimeStamp()).thenReturn(0d);
			
			PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo4.getPacket()).thenReturn(tcpPacket2);
			Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
			Mockito.when(packetInfo4.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK);
			Mockito.when(packetInfo4.getTimeStamp()).thenReturn(90d);
			Mockito.when(packetInfo4.getPayloadLen()).thenReturn(0);
			
			PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
			Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket2);
			Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.DOWNLINK);
			Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_DATA_RECOVER);
			Mockito.when(packetInfo5.getTimeStamp()).thenReturn(750d);
			Mockito.when(packetInfo5.getPayloadLen()).thenReturn(0);
			
			List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
			packetsList.add(packetInfo1); //Adding UDP Packet to the list
			packetsList.add(packetInfo2); //Adding TCP Packet to the list
			packetsList.add(packetInfo3);
			packetsList.add(packetInfo4);
			packetsList.add(packetInfo5);
			
			
			ProfileLTE profile = Mockito.mock(ProfileLTE.class);
			Mockito.when(profile.getBurstTh()).thenReturn(50d);
			Mockito.when(profile.getLongBurstTh()).thenReturn(40.0d);
			Mockito.when(profile.getLargeBurstDuration()).thenReturn(150d);
			Mockito.when(profile.getLargeBurstSize()).thenReturn(50);
			Mockito.when(profile.getProfileType()).thenReturn(ProfileType.LTE);
			Mockito.when(profile.getBurstTh()).thenReturn(25.0d);
			Mockito.when(profile.getCarrier()).thenReturn("ATT");
			Mockito.when(profile.getCloseSpacedBurstThreshold()).thenReturn(45d);
			Mockito.when(profile.getDrxLongPingPeriod()).thenReturn(50.0d);
			Mockito.when(profile.getDrxLongPingPower()).thenReturn(75d);
			Mockito.when(profile.getDrxLongTime()).thenReturn(75d);
			Mockito.when(profile.getDevice()).thenReturn("lg");
			Mockito.when(profile.getDrxShortPingPeriod()).thenReturn(100d);
			Mockito.when(profile.getDrxShortPingPower()).thenReturn(200d);
			Mockito.when(profile.getDrxShortTime()).thenReturn(50d);
			Mockito.when(profile.getLargeBurstDuration()).thenReturn(500d);
			Mockito.when(profile.getLargeBurstSize()).thenReturn(1000);
			Mockito.when(profile.getLongBurstTh()).thenReturn(250d);

			
			Map<Integer, Integer> packetSizeToCountMap = new HashMap<Integer, Integer>();
			packetSizeToCountMap.put(1001, 10);
			packetSizeToCountMap.put(2002, 20);
			packetSizeToCountMap.put(3003, 30);
			
			RrcStateRange rrcStateRange1 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange1.getBeginTime()).thenReturn(500d);
			Mockito.when(rrcStateRange1.getEndTime()).thenReturn(590d);
			Mockito.when(rrcStateRange1.getState()).thenReturn(RRCState.TAIL_DCH);
			
			RrcStateRange rrcStateRange2 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange2.getBeginTime()).thenReturn(8.30d);
			Mockito.when(rrcStateRange2.getEndTime()).thenReturn(12.30d);
			Mockito.when(rrcStateRange2.getState()).thenReturn(RRCState.PROMO_FACH_DCH);
			
			RrcStateRange rrcStateRange3 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange3.getBeginTime()).thenReturn(0.0d);
			Mockito.when(rrcStateRange3.getEndTime()).thenReturn(-2.0d);
			Mockito.when(rrcStateRange3.getState()).thenReturn(RRCState.TAIL_DCH);
			
			RrcStateRange rrcStateRange4 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange4.getBeginTime()).thenReturn(25d);
			Mockito.when(rrcStateRange4.getEndTime()).thenReturn(75d);
			Mockito.when(rrcStateRange4.getState()).thenReturn(RRCState.WIFI_TAIL);
			
			RrcStateRange rrcStateRange5 = Mockito.mock(RrcStateRange.class);
			Mockito.when(rrcStateRange5.getBeginTime()).thenReturn(105d);
			Mockito.when(rrcStateRange5.getEndTime()).thenReturn(95d);
			Mockito.when(rrcStateRange5.getState()).thenReturn(RRCState.PROMO_IDLE_DCH);
			
			List<RrcStateRange> rrcstaterangelist = new ArrayList<RrcStateRange>();
			rrcstaterangelist.add(rrcStateRange1);
			rrcstaterangelist.add(rrcStateRange2);
			rrcstaterangelist.add(rrcStateRange3);
			rrcstaterangelist.add(rrcStateRange4);
			rrcstaterangelist.add(rrcStateRange5);
			
			UserEvent uEvent1 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent1.getEventType()).thenReturn(UserEventType.SCREEN_LANDSCAPE);
			Mockito.when(uEvent1.getPressTime()).thenReturn(503d);
			Mockito.when(uEvent1.getReleaseTime()).thenReturn(6d);
			
			UserEvent uEvent2 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent2.getEventType()).thenReturn(UserEventType.SCREEN_PORTRAIT);
			Mockito.when(uEvent2.getPressTime()).thenReturn(14d);
			Mockito.when(uEvent2.getReleaseTime()).thenReturn(2000d);
			
			UserEvent uEvent3 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent3.getEventType()).thenReturn(UserEventType.KEY_RED);
			Mockito.when(uEvent3.getPressTime()).thenReturn(497d);
			Mockito.when(uEvent3.getReleaseTime()).thenReturn(499d);
			
			UserEvent uEvent4 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent4.getEventType()).thenReturn(UserEventType.EVENT_UNKNOWN);
			Mockito.when(uEvent4.getPressTime()).thenReturn(25d);
			Mockito.when(uEvent4.getReleaseTime()).thenReturn(4d);
			
			UserEvent uEvent5 = Mockito.mock(UserEvent.class);
			Mockito.when(uEvent5.getEventType()).thenReturn(UserEventType.KEY_SEARCH);
			Mockito.when(uEvent5.getPressTime()).thenReturn(752d);
			Mockito.when(uEvent5.getReleaseTime()).thenReturn(30000d);
			
			List<UserEvent> uEventList = new ArrayList<UserEvent>();
			uEventList.add(uEvent1);
			uEventList.add(uEvent2);
			uEventList.add(uEvent3);
			uEventList.add(uEvent4);
			uEventList.add(uEvent5);
			
			CpuActivity cActivity1 = Mockito.mock(CpuActivity.class);
			Mockito.when(cActivity1.getTimeStamp()).thenReturn(23000d);
			Mockito.when(cActivity1.getTotalCpuUsage()).thenReturn(5000d);
			
			CpuActivity cActivity2 = Mockito.mock(CpuActivity.class);
			Mockito.when(cActivity2.getTimeStamp()).thenReturn(24000d);
			Mockito.when(cActivity2.getTotalCpuUsage()).thenReturn(6000d);
			
			CpuActivity cActivity3 = Mockito.mock(CpuActivity.class);
			Mockito.when(cActivity3.getTimeStamp()).thenReturn(25000d);
			Mockito.when(cActivity3.getTotalCpuUsage()).thenReturn(6000d);
			
			List<CpuActivity> cpuActivityList = new ArrayList<CpuActivity>();
			cpuActivityList.add(cActivity1);
			cpuActivityList.add(cActivity2);
			cpuActivityList.add(cActivity3);
			
			Session aSession = Mockito.mock(Session.class);
			List<Session> sessionList = new ArrayList<Session>();
			sessionList.add(aSession);
			
			BurstCollectionAnalysisData bcaData = aBurstCollectionAnalysis.analyze(packetsList, profile, packetSizeToCountMap, rrcstaterangelist, uEventList, cpuActivityList, sessionList);
			
			if(bcaData != null){
				assertEquals(3, bcaData.getBurstAnalysisInfo().size());
				assertEquals(3, bcaData.getBurstCollection().size());
				assertEquals(0, bcaData.getLongBurstCount());
				assertEquals(0, (int)bcaData.getTotalEnergy());
			}
			
	}
	
}
