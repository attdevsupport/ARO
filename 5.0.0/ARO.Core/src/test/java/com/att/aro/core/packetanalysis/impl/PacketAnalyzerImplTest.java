package com.att.aro.core.packetanalysis.impl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.packetanalysis.IBurstCollectionAnalysis;
import com.att.aro.core.packetanalysis.IEnergyModelFactory;
import com.att.aro.core.packetanalysis.IPacketAnalyzer;
import com.att.aro.core.packetanalysis.IPktAnazlyzerTimeRangeUtil;
import com.att.aro.core.packetanalysis.IRrcStateMachineFactory;
import com.att.aro.core.packetanalysis.ISessionManager;
import com.att.aro.core.packetanalysis.ITraceDataReader;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceFileResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.packetreader.pojo.TCPPacket;
import com.att.aro.core.packetreader.pojo.UDPPacket;
import com.att.aro.core.peripheral.pojo.CpuActivity;
import com.att.aro.core.peripheral.pojo.CpuActivityList;
public class PacketAnalyzerImplTest extends BaseTest {
	
	@InjectMocks
	PacketAnalyzerImpl iPacketAnalyzer;
	@Mock
	private ITraceDataReader tracereader;
	@Mock
	private ISessionManager sessionmanager;
	@Mock
	private IRrcStateMachineFactory statemachinefactory;
	@Mock
	private IProfileFactory profilefactory;
	@Mock
	private IEnergyModelFactory energymodelfactory;
	@Mock
	private IBurstCollectionAnalysis burstcollectionanalyzer;
	@Mock
	private IPktAnazlyzerTimeRangeUtil pktTimeUtil;

	@Before
	public void setup() {
		iPacketAnalyzer = (PacketAnalyzerImpl)context.getBean(IPacketAnalyzer.class);
		MockitoAnnotations.initMocks(this);		

	}
	
	@After
	public void reset(){
		Mockito.reset(tracereader);
		Mockito.reset(sessionmanager);
		Mockito.reset(statemachinefactory);
		Mockito.reset(profilefactory);
		Mockito.reset(energymodelfactory);
		Mockito.reset(burstcollectionanalyzer);

	}
	
	@Test
	public void Test_analyzeTraceDirectory_returnIsPacketAnalyzerResult() throws  Exception{
		iPacketAnalyzer.setEnergyModelFactory(energymodelfactory);
		iPacketAnalyzer.setBurstCollectionAnalayzer(burstcollectionanalyzer);
		iPacketAnalyzer.setRrcStateMachineFactory(statemachinefactory);
		iPacketAnalyzer.setProfileFactory(profilefactory);
		TraceDirectoryResult mockTraceDirResult = mock(TraceDirectoryResult.class);
		AnalysisFilter filter = mock(AnalysisFilter.class);
		filter.setIpv4Sel(true);
		filter.setIpv6Sel(true);
		filter.setUdpSel(true);
		CpuActivityList cpuList = new CpuActivityList();
		cpuList.add(new CpuActivity());
		when(mockTraceDirResult.getCpuActivityList()).thenReturn(cpuList);
		when(tracereader.readTraceDirectory(any(String.class))).thenReturn(mockTraceDirResult);
		
		ProfileLTE profileLTE = new ProfileLTE();
		when(profilefactory.createLTEdefault()).thenReturn(profileLTE);

		PacketAnalyzerResult testResult = iPacketAnalyzer.analyzeTraceDirectory("", profileLTE, filter);
		assertEquals(null,testResult.getSessionlist());
	}
	
	@Test
	public void  Test_analyzeTraceFile_returnIsPacketAnalyzerResult() throws  Exception{
		iPacketAnalyzer.setProfileFactory(profilefactory);

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
		Inet4Address address = (Inet4Address) InetAddress.getByName("192.168.1.4");
		

		PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo3.getPayloadLen()).thenReturn(0);
		Mockito.when(packetInfo3.getLen()).thenReturn(15);
		Mockito.when(packetInfo3.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test2String");
		Mockito.when(packetInfo3.getTimeStamp()).thenReturn(10d);
		Mockito.when(packetInfo3.getRemoteIPAddress()).thenReturn(address);

		List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
		packetsList.add(packetInfo1); //Adding UDP Packet to the list
		packetsList.add(packetInfo2);
		packetsList.add(packetInfo3);
		TraceFileResult mockTraceResult = mock(TraceFileResult.class);
		List<PacketInfo> filteredPackets = new ArrayList<PacketInfo>();
		filteredPackets.add(mock(PacketInfo.class));
		when(mockTraceResult.getAllpackets()).thenReturn(packetsList);		
		
		CpuActivityList cpuList = new CpuActivityList();
		cpuList.add(new CpuActivity());
		when(mockTraceResult.getCpuActivityList()).thenReturn(cpuList);
		when(tracereader.readTraceFile(any(String.class))).thenReturn(mockTraceResult);
		when(mockTraceResult.getTraceResultType()).thenReturn(TraceResultType.TRACE_FILE);
		ProfileLTE profileLTE = new ProfileLTE();
		when(profilefactory.createLTEdefault()).thenReturn(profileLTE);
		AnalysisFilter filter = mock(AnalysisFilter.class);
		filter.setIpv4Sel(false);
		filter.setIpv6Sel(false);
		filter.setUdpSel(false);
		iPacketAnalyzer.setEnergyModelFactory(energymodelfactory);
		iPacketAnalyzer.setBurstCollectionAnalayzer(burstcollectionanalyzer);
		iPacketAnalyzer.setRrcStateMachineFactory(statemachinefactory);
		RrcStateMachineLTE rrcstate = mock(RrcStateMachineLTE.class);
		EnergyModel energymodel = mock(EnergyModel.class);
		List<RrcStateRange> rrcstatelist = new ArrayList<RrcStateRange>();
		when(statemachinefactory.create
				(any(List.class), any(Profile.class), any(double.class), any(double.class), any(double.class),
						any(TimeRange.class))).thenReturn(rrcstate);
						
		when(rrcstate.getStaterangelist()).thenReturn(rrcstatelist);
		when(rrcstate.getTotalRRCEnergy()).thenReturn(1.0);
		when(energymodelfactory.
				create(any(Profile.class), any(double.class), any(List.class), 
						any(List.class), any(List.class), any(List.class))).
				thenReturn(energymodel);
		BurstCollectionAnalysisData burstvalue = mock(BurstCollectionAnalysisData.class);
		when(burstcollectionanalyzer.analyze(any(List.class), any(Profile.class), 
				any(Map.class), any(List.class), any(List.class), any(List.class), any(List.class)))
				.thenReturn(burstvalue);
		
//		when(pktTimeUtil.getTimeRangeResult(any(AbstractTraceResult.class), any(AnalysisFilter.class)))
//		.thenReturn(value);
		PacketAnalyzerResult testResult = iPacketAnalyzer.analyzeTraceFile("", null, filter);		
		assertSame(false,testResult.getFilter().isIpv4Sel());

	}

	@Test
	public void test_getStatisticResult() throws UnknownHostException{
		
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
		Inet4Address address = (Inet4Address) InetAddress.getByName("192.168.1.4");
		

		PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo3.getPayloadLen()).thenReturn(0);
		Mockito.when(packetInfo3.getLen()).thenReturn(15);
		Mockito.when(packetInfo3.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test2String");
		Mockito.when(packetInfo3.getTimeStamp()).thenReturn(10d);
		Mockito.when(packetInfo3.getRemoteIPAddress()).thenReturn(address);

		List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
		packetsList.add(packetInfo1); //Adding UDP Packet to the list
		packetsList.add(packetInfo2);
		packetsList.add(packetInfo3);
		
		Statistic testResult = iPacketAnalyzer.getStatistic(packetsList);
		assertEquals(3,testResult.getTotalPackets());
	}

}
