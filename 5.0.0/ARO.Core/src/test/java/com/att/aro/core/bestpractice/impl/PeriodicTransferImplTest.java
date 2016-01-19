package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.packetreader.pojo.PacketDirection;

public class PeriodicTransferImplTest extends BaseTest{

	PeriodicTransferImpl periodicTransferImpl;
	@Before
	public void setup(){
		periodicTransferImpl = (PeriodicTransferImpl)context.getBean("periodicTransfer");		
	}
	
	@After
	public void reset(){
	}
	
	@Test
	public void runTest_AresultIsPass(){
		PacketAnalyzerResult tracedata01 = new PacketAnalyzerResult();
		Date date = new Date();

		List<Burst> burstCollection = new ArrayList<Burst>();
		BurstCollectionAnalysisData burstcollectionAnalysisData = new BurstCollectionAnalysisData();
		
		InetAddress remoteIP = null;
		try {
			remoteIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} int remotePort = 80; int localPort = 80;
		byte[] d1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -99, -87, 64, 0, 64, 6, -27, -25, 10, -27, 77, 114, 74, 125, 20, 95, -90, 2, 1, -69, -108, -18, 20, 87, -4, -110, -105, -88, -128, 16, 6, 88, 51, 24, 0, 0, 1, 1, 8, 10, -1, -1, -87, 50, 101, -15, -111, -73};
		Packet packet01 = new Packet(1, 1418242722L, 324000, 66, d1);
		byte[] d2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, 0, 0, 64, 0, 64, 6, -125, -111, 74, 125, 20, 95, 10, -27, 77, 114, 1, -69, -90, 2, 0, 0, 0, 0, -108, -18, 20, 87, -128, 4, 0, 0, 119, -98, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		Packet packet02 = new Packet(1, 1418242722L, 325000, 63, d2);
		PacketInfo packetInfo01 = new PacketInfo(packet01);
		PacketInfo packetInfo02 = new PacketInfo(packet02);
		packetInfo01.setTcpInfo(TcpInfo.TCP_ESTABLISH);
		packetInfo01.setTimestamp((double)date.getTime());
		packetInfo02.setTcpInfo(TcpInfo.TCP_ACK);
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		packetlist.add(packetInfo01);
		packetlist.add(packetInfo02);
		Burst burst01 = new Burst(packetlist);
		burst01.setFirstUplinkDataPacket(packetInfo01);
		burst01.setBurstInfo(BurstCategory.CLIENT_APP);	
		burstCollection.add(burst01);
		Session session01 = new Session(remoteIP, remotePort, localPort);
		session01.setUdpOnly(false);
		session01.setPackets(packetlist);
		HttpRequestResponseInfo httpRequestResponseInfo01 = new HttpRequestResponseInfo();
		httpRequestResponseInfo01.setFirstDataPacket(packetInfo01);
		httpRequestResponseInfo01.setDirection(HttpDirection.REQUEST);
		httpRequestResponseInfo01.setHostName("yahoo.com");
		httpRequestResponseInfo01.setObjName("");
		List<HttpRequestResponseInfo> httpRequestResponseInfolist = new ArrayList<HttpRequestResponseInfo>();
		httpRequestResponseInfolist.add(httpRequestResponseInfo01);
		session01.setRequestResponseInfo(httpRequestResponseInfolist);
		List<Session> sessionlist = new ArrayList<Session>();
		sessionlist.add(session01);
		ProfileLTE profileLTE = new ProfileLTE();
		profileLTE.setPeriodMinCycle(10.0);
		profileLTE.setPeriodCycleTol(1.0); 
		profileLTE.setPeriodMinSamples(3);
		profileLTE.setCloseSpacedBurstThreshold(0.0);
		burstcollectionAnalysisData.setBurstCollection(burstCollection);
		tracedata01.setSessionlist(sessionlist);
		tracedata01.setBurstcollectionAnalysisData(burstcollectionAnalysisData);
		tracedata01.setProfile(profileLTE);
		AbstractBestPracticeResult result01 = periodicTransferImpl.runTest(tracedata01);
		
		assertEquals(BPResultType.PASS,result01.getResultType());
	} 
	  
	@Test
	public void runTest_resultIsFail(){
		PacketAnalyzerResult tracedata = new PacketAnalyzerResult();
		Date date = new Date();

		Profile3G profile3g = new Profile3G();
		profile3g.setPeriodMinCycle(10.0);
		profile3g.setPeriodCycleTol(1.0);
		profile3g.setPeriodMinSamples(3);
		profile3g.setCloseSpacedBurstThreshold(-50.0);
		InetAddress remoteIP = null;
		try {
			remoteIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		} 
		InetAddress remoteIP01 = null;
		try {
			remoteIP01 = InetAddress.getByName("google.com");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		} 

		int remotePort = 80; int localPort = 80;
		Session session01 = new Session(remoteIP, remotePort, localPort);
		Session session02 = new Session(remoteIP01, remotePort, localPort);
		 
		byte[] d1 =  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -87, -63, 64, 0, 64, 6, 76, -121, 10, 120, 0, 1, 74, 125, -17, -123, -104, 53, 1, -69, 91, 8, 7, 120, 25, -21, 51, -84, -128, 16, 5, 123, 105, -6, 0, 0, 1, 1, 8, 10, -1, -1, -86, -80, 53, -38, -104, 57,5,7,0,6,7};
		Packet packet01 = new Packet(1, 1418242726L, 234000, 50, d1);
		byte[] d2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, 0, 0, 64, 0, 64, 6, -125, -111, 74, 125, 20, 95, 10, -27, 77, 114, 1, -69, -90, 2, 0, 0, 0, 0, -108, -18, 20, 87, -128, 4, 0, 0, 119, -98, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		Packet packet02 = new Packet(1, 1418242722L, 325000, 50, d2);
		byte[] d3 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -97, 67, 64, 0, 64, 6, -28, 77, 10, -27, 77, 114, 74, 125, 20, 95, -58, -91, 1, -69, 22, 2, 7, 25, -69, -41, -92, -13, -128, 17, 8, 86, -98, -14, 0, 0, 1, 1, 8, 10, -1, -1, -87, 51, 96, 102, -56, 95};
		Packet packet03 = new Packet(1, 1418242722L, 325000, 50, d3);
		byte[] d4 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -97, 68, 64, 0, 64, 6, -28, 76, 10, -27, 77, 114, 74, 125, 20, 95, -58, -91, 1, -69, 22, 2, 7, 25, -69, -41, -92, -13, -128, 17, 8, 86, -98, -43, 0, 0, 1, 1, 8, 10, -1, -1, -87, 80, 96, 102, -56, 95};
		Packet packet04 = new Packet(1, 1418242722L, 625000, 50, d4);
		byte[] d5 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -97, 69, 64, 0, 64, 6, -28, 75, 10, -27, 77, 114, 74, 125, 20, 95, -58, -91, 1, -69, 22, 2, 7, 25, -69, -41, -92, -13, -128, 17, 8, 86, -98, -101, 0, 0, 1, 1, 8, 10, -1, -1, -87, -118, 96, 102, -56, 95};
		Packet packet05 = new Packet(1, 1418242723L, 226000, 50, d5);
		byte[] d6 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -97, 70, 64, 0, 64, 6, -28, 74, 10, -27, 77, 114, 74, 125, 20, 95, -58, -91, 1, -69, 22, 2, 7, 25, -69, -41, -92, -13, -128, 17, 8, 86, -98, 39, 0, 0, 1, 1, 8, 10, -1, -1, -87, -2, 96, 102, -56, 95};
		Packet packet06 = new Packet(1, 1418242724L, 328000, 50, d6);
		byte[] d7 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 60, -87, -69, 64, 0, 64, 6, 76, -123, 10, 120, 0, 1, 74, 125, -17, -123, -104, 53, 1, -69, 91, 8, 3, -57, 0, 0, 0, 0, -96, 2, -1, -1, 96, 73, 0, 0, 2, 4, 5, -76, 4, 2, 8, 10, -1, -1, -86, 124, 0, 0, 0, 0, 1, 3, 3, 6};
		Packet packet07 = new Packet(1, 1418242725L, 730000, 50, d7);
		byte[] d8 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 60, 0, 0, 64, 0, 64, 6, -10, 64, 74, 125, -17, -123, 10, 120, 0, 1, 1, -69, -104, 53, 25, -21, 49, 103, 91, 8, 3, -56, -96, 18, -1, -1, 72, 11, 0, 0, 2, 4, 5, -76, 4, 2, 8, 10, 53, -38, -105, 0, -1, -1, -86, 124, 1, 3, 3, 6};
		Packet packet08 = new Packet(1, 1418242725L, 731000, 50, d8);
		byte[] d9 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 8, 0, 69, 0, 0, 52, -87, -68, 64, 0, 64, 6, 76, -116, 10, 120, 0, 1, 74, 125, -17, -123, -104, 53, 1, -69, 91, 8, 3, -56, 25, -21, 49, 104, -128, 16, 5, 89, 113, 119, 0, 0, 1, 1, 8, 10, -1, -1, -86, -126, 53, -38, -105, 0};
		Packet packet09 = new Packet(1, 1418242725L, 731000, 50, d9);
		
		PacketInfo packetInfo01 = new PacketInfo(packet01);
		PacketInfo packetInfo02 = new PacketInfo(packet02);
		PacketInfo packetInfo03 = new PacketInfo(packet03);
		PacketInfo packetInfo04 = new PacketInfo(packet04);
		PacketInfo packetInfo05 = new PacketInfo(packet05);
		PacketInfo packetInfo06 = new PacketInfo(packet06);
		PacketInfo packetInfo07 = new PacketInfo(packet07);
		PacketInfo packetInfo08 = new PacketInfo(packet08);
		PacketInfo packetInfo09 = new PacketInfo(packet09);
		List<PacketInfo> packetlist01 = new ArrayList<PacketInfo>();
		List<PacketInfo> packetlist02 = new ArrayList<PacketInfo>();

		packetInfo01.setTcpInfo(TcpInfo.TCP_ESTABLISH);	
		packetInfo02.setTcpInfo(TcpInfo.TCP_ACK);
		packetInfo01.setDir(PacketDirection.UPLINK);
		packetInfo02.setDir(PacketDirection.UPLINK);
		packetInfo03.setDir(PacketDirection.UPLINK);
		packetInfo04.setDir(PacketDirection.UPLINK);
		packetInfo05.setDir(PacketDirection.UPLINK);
		packetInfo06.setDir(PacketDirection.UPLINK);
		packetInfo07.setDir(PacketDirection.UPLINK);
		packetInfo08.setDir(PacketDirection.UPLINK);

		packetInfo01.setTimestamp(0.0);		
		packetInfo02.setTimestamp(10.0);
		packetInfo03.setTimestamp(20.0);
		packetInfo04.setTimestamp(30.0);
		packetInfo05.setTimestamp(10.0);
		packetInfo06.setTimestamp(20.0);
		packetInfo07.setTimestamp(30.0);
		packetInfo08.setTimestamp(40.0);
		packetInfo09.setTimestamp(50.0);

		packetlist01.add(packetInfo01);		
		packetlist01.add(packetInfo02);
		packetlist01.add(packetInfo03);
		packetlist01.add(packetInfo04);
		packetlist02.add(packetInfo05);
		packetlist02.add(packetInfo06);
		packetlist02.add(packetInfo07);
		packetlist02.add(packetInfo08);
		packetlist02.add(packetInfo09);

 
		session01.setUdpOnly(false);
		session01.setPackets(packetlist01);

		session02.setUdpOnly(false);
		session02.setPackets(packetlist02);

		HttpRequestResponseInfo httpRequestResponseInfo01 = new HttpRequestResponseInfo();
		httpRequestResponseInfo01.setFirstDataPacket(packetInfo01);
		httpRequestResponseInfo01.setDirection(HttpDirection.REQUEST);
		httpRequestResponseInfo01.setHostName("yahoo.com");
		httpRequestResponseInfo01.setObjName("");
		
		HttpRequestResponseInfo httpRequestResponseInfo02 = new HttpRequestResponseInfo();
		httpRequestResponseInfo02.setFirstDataPacket(packetInfo02);
		httpRequestResponseInfo02.setDirection(HttpDirection.REQUEST);
		httpRequestResponseInfo02.setHostName("google.com");
		httpRequestResponseInfo02.setObjName("");
		
		HttpRequestResponseInfo httpRequestResponseInfo03 = new HttpRequestResponseInfo();
		httpRequestResponseInfo03.setFirstDataPacket(packetInfo04);
		httpRequestResponseInfo03.setDirection(HttpDirection.REQUEST);
		httpRequestResponseInfo03.setHostName("cnn.com");
		httpRequestResponseInfo03.setObjName("");

		List<HttpRequestResponseInfo> httpRequestResponseInfolist = new ArrayList<HttpRequestResponseInfo>();
		httpRequestResponseInfolist.add(httpRequestResponseInfo01);
		httpRequestResponseInfolist.add(httpRequestResponseInfo02);
		httpRequestResponseInfolist.add(httpRequestResponseInfo02);
		httpRequestResponseInfolist.add(httpRequestResponseInfo03);

		session01.setRequestResponseInfo(httpRequestResponseInfolist);
		Burst burst01 = new Burst(packetlist01);
		burst01.setFirstUplinkDataPacket(packetInfo01);
		burst01.setBurstInfo(BurstCategory.CLIENT_APP);	
		
		Burst burst02 = new Burst(packetlist01);
		burst02.setFirstUplinkDataPacket(packetInfo01);
		burst02.setBurstInfo(BurstCategory.PERIODICAL);
		
		Burst burst03 = new Burst(packetlist02);
		burst03.setFirstUplinkDataPacket(packetInfo02);
		burst03.setBurstInfo(BurstCategory.CLIENT_APP);
		
		Burst burst04 = new Burst(packetlist02);
		burst04.setFirstUplinkDataPacket(packetInfo02);
		burst04.setBurstInfo(BurstCategory.PERIODICAL);
		
		Burst burst05 = new Burst(packetlist02);
		burst05.setFirstUplinkDataPacket(packetInfo02);
		burst05.setBurstInfo(BurstCategory.CLIENT_APP);
		
		Burst burst06 = new Burst(packetlist01);
		burst06.setFirstUplinkDataPacket(packetInfo03);
		burst06.setBurstInfo(BurstCategory.PERIODICAL);	
		
		Burst burst07 = new Burst(packetlist01);
		burst07.setBurstInfo(BurstCategory.CLIENT_APP);	
		
		Burst burst08 = new Burst(packetlist02);
		burst08.setBurstInfo(BurstCategory.PERIODICAL);
		
		Burst burst09 = new Burst(packetlist02);
		burst09.setBurstInfo(BurstCategory.CLIENT_APP); 

		Burst burst10 = new Burst(packetlist01);
		burst10.setBurstInfo(BurstCategory.PERIODICAL); 

		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		burstCollection.add(burst02);
		burstCollection.add(burst03);
		burstCollection.add(burst04);
		burstCollection.add(burst06);
		burstCollection.add(burst07);
		burstCollection.add(burst08);
		burstCollection.add(burst09);
		burstCollection.add(burst10);

		BurstCollectionAnalysisData burstcollectionAnalysisData = new BurstCollectionAnalysisData();
		burstcollectionAnalysisData.setBurstCollection(burstCollection);
		List<Session> sessionlist = new ArrayList<Session>();
		sessionlist.add(session01);
		sessionlist.add(session02);
		sessionlist.add(session01);		
		sessionlist.add(session01);
		sessionlist.add(session01);
		sessionlist.add(session01);
		sessionlist.add(session01);
		sessionlist.add(session02);
		sessionlist.add(session01);

		tracedata.setSessionlist(sessionlist);
		tracedata.setBurstcollectionAnalysisData(burstcollectionAnalysisData);
		tracedata.setProfile(profile3g);
		AbstractBestPracticeResult result = periodicTransferImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType());
	} 
	
}
