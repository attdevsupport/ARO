/**
 * 
 */
package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.IRequestResponseBuilder;
import com.att.aro.core.packetanalysis.ISessionManager;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.packetreader.pojo.TCPPacket;
import com.att.aro.core.packetreader.pojo.UDPPacket;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class SessionManagerImplTest extends BaseTest{
	
	@InjectMocks
	ISessionManager sessionMgr;

	@Mock
	IRequestResponseBuilder requestResponseBuilder;
	
	@Before
	public void setUp(){
		
		sessionMgr = context.getBean(ISessionManager.class);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void assembleSessionTest(){
		Date date = new Date();
	
		InetAddress iAdr = null;
		InetAddress iAdr1 = null;
		try {
			iAdr = InetAddress.getByAddress(new byte[]{89,10,1,1});
			iAdr1 = InetAddress.getByAddress(new byte[]{89,10,1,1});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		//		Mockito.when(iAdr.getAddress()).thenReturn(new byte[]{89,10,1,1});
//		Mockito.when(iAdr.getHostAddress()).thenReturn("@att");
//		Mockito.when(iAdr.getHostName()).thenReturn("att");
//		Mockito.when(iAdr1.getAddress()).thenReturn(new byte[]{89,10,1,1});
//		Mockito.when(iAdr1.getHostAddress()).thenReturn("@att");
//		Mockito.when(iAdr1.getHostName()).thenReturn("att");
		
		Set<InetAddress> inetSet = new HashSet<InetAddress>();
		inetSet.add(iAdr);
		inetSet.add(iAdr1);
		
		IPPacket ipPack = Mockito.mock(IPPacket.class);
		Mockito.when(ipPack.getDestinationIPAddress()).thenReturn(iAdr1);
		Mockito.when(ipPack.getSourceIPAddress()).thenReturn(iAdr);
		Mockito.when(ipPack.getLen()).thenReturn(20);
		Mockito.when(ipPack.getPacketLength()).thenReturn(20);
		Mockito.when(ipPack.getPayloadLen()).thenReturn(20);
		
		DomainNameSystem dns = Mockito.mock(DomainNameSystem.class);
		Mockito.when(dns.getIpAddresses()).thenReturn(inetSet);
		Mockito.when(dns.getDomainName()).thenReturn("www.att.com");
		Mockito.when(dns.isResponse()).thenReturn(true);
		Mockito.when(dns.getCname()).thenReturn("ATT");
		Mockito.when(dns.getPacket()).thenReturn(ipPack);
		
		
		//UDP Packet Mock
		UDPPacket udpPacket = Mockito.mock(UDPPacket.class);
		Mockito.when(udpPacket.isDNSPacket()).thenReturn(true);
		Mockito.when(udpPacket.getDns()).thenReturn(dns);
		Mockito.when(udpPacket.getSourcePort()).thenReturn(83);
		Mockito.when(udpPacket.getDestinationPort()).thenReturn(84);
		Mockito.when(udpPacket.getSourceIPAddress()).thenReturn(iAdr);
		Mockito.when(udpPacket.getDestinationIPAddress()).thenReturn(iAdr1);
		
		
		
		PacketInfo packetInfo1 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo1.getPacket()).thenReturn(udpPacket);
		Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		//Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UNKNOWN);
		//Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo1.getLen()).thenReturn(10);
		Mockito.when(packetInfo1.getAppName()).thenReturn("Test1");
		Mockito.when(packetInfo1.getTcpFlagString()).thenReturn("TestString");
		
/*		InetAddress iAdr2 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr2.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr2.getHostAddress()).thenReturn("@att");
		Mockito.when(iAdr2.getHostName()).thenReturn("att");*/
		
		InetAddress iAdr2 = null;
		InetAddress iAdr3 = null;
		try {
			iAdr2 = InetAddress.getByAddress(new byte[]{89,10,1,1});
			iAdr3 = InetAddress.getByAddress(new byte[]{89,10,1,1});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		TCPPacket tcpPacket = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket.getSourcePort()).thenReturn(81);
		Mockito.when(tcpPacket.getDestinationPort()).thenReturn(82);
		Mockito.when(tcpPacket.getDestinationIPAddress()).thenReturn(iAdr2);
		Mockito.when(tcpPacket.getSourceIPAddress()).thenReturn(iAdr2);
		Mockito.when(tcpPacket.getAckNumber()).thenReturn(0l);
		Mockito.when(tcpPacket.getLen()).thenReturn(50);
		Mockito.when(tcpPacket.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket.isRST()).thenReturn(true);
		Mockito.when(tcpPacket.getSequenceNumber()).thenReturn(20l);
		Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-3000);
		//Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-10000);
		
		PacketInfo packetInfo2 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo2.getPacket()).thenReturn(tcpPacket);
		Mockito.when(packetInfo2.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo2.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo2.getPayloadLen()).thenReturn(20);
		Mockito.when(packetInfo2.getLen()).thenReturn(15);
		Mockito.when(packetInfo2.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo2.getTcpFlagString()).thenReturn("Test2String");
		
/*		InetAddress iAdr3 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr3.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr3.getHostAddress()).thenReturn("@att");
		Mockito.when(iAdr3.getHostName()).thenReturn("att");
*/		
		TCPPacket tcpPacket2 = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket2.getSourcePort()).thenReturn(95);
		Mockito.when(tcpPacket2.getDestinationPort()).thenReturn(99);
		Mockito.when(tcpPacket2.getDestinationIPAddress()).thenReturn(iAdr3);
		Mockito.when(tcpPacket2.getSourceIPAddress()).thenReturn(iAdr3);
		Mockito.when(tcpPacket.getAckNumber()).thenReturn(0l);
		Mockito.when(tcpPacket2.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket2.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket2.isRST()).thenReturn(true);
		Mockito.when(tcpPacket.getSequenceNumber()).thenReturn(1l);
		Mockito.when(tcpPacket2.getTimeStamp()).thenReturn(10000d);
		
		PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK_RECOVER);
		Mockito.when(packetInfo3.getPayloadLen()).thenReturn(30);
		Mockito.when(packetInfo3.getLen()).thenReturn(20);
		Mockito.when(packetInfo3.getAppName()).thenReturn("Test3");
		Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test3String");
/*		
		PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo4.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo4.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK);
		
		PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_DATA_RECOVER);
*/		
		
		InetAddress iAdr4 = Mockito.mock(InetAddress.class);
		InetAddress iAdr5 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr4.getAddress()).thenReturn(new byte[]{89,10,5,1});
		Mockito.when(iAdr4.getHostAddress()).thenReturn("@microfoft");
		Mockito.when(iAdr4.getHostName()).thenReturn("microfoft");
		Mockito.when(iAdr5.getAddress()).thenReturn(new byte[]{72,12,23,1});
		Mockito.when(iAdr5.getHostAddress()).thenReturn("@apple");
		Mockito.when(iAdr5.getHostName()).thenReturn("apple");
		
		Set<InetAddress> inetSet1 = new HashSet<InetAddress>();
		inetSet1.add(iAdr4);
		inetSet1.add(iAdr5);
		
		IPPacket ipPack1 = Mockito.mock(IPPacket.class);
		Mockito.when(ipPack1.getDestinationIPAddress()).thenReturn(iAdr5);
		Mockito.when(ipPack1.getSourceIPAddress()).thenReturn(iAdr4);
		Mockito.when(ipPack1.getLen()).thenReturn(30);
		Mockito.when(ipPack1.getPacketLength()).thenReturn(40);
		Mockito.when(ipPack1.getPayloadLen()).thenReturn(40);
		
		DomainNameSystem dns1 = Mockito.mock(DomainNameSystem.class);
		Mockito.when(dns1.getIpAddresses()).thenReturn(inetSet);
		Mockito.when(dns1.getDomainName()).thenReturn("www.att.com");
		Mockito.when(dns1.isResponse()).thenReturn(false);
		Mockito.when(dns1.getCname()).thenReturn("ATT");
		Mockito.when(dns1.getPacket()).thenReturn(ipPack1);
		
		
		//UDP Packet Mock
		UDPPacket udpPacket1 = Mockito.mock(UDPPacket.class);
		Mockito.when(udpPacket1.isDNSPacket()).thenReturn(true);
		Mockito.when(udpPacket1.getDns()).thenReturn(dns1);
		Mockito.when(udpPacket1.getSourcePort()).thenReturn(90);
		Mockito.when(udpPacket1.getSourceIPAddress()).thenReturn(iAdr4);
		Mockito.when(udpPacket1.getDestinationPort()).thenReturn(91);
		Mockito.when(udpPacket1.getDestinationIPAddress()).thenReturn(iAdr5);
		
		
		PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo4.getPacket()).thenReturn(udpPacket);
		Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
		//Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.UNKNOWN);
		//Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		//Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo4.getPayloadLen()).thenReturn(30);
		Mockito.when(packetInfo4.getLen()).thenReturn(10);
		Mockito.when(packetInfo4.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo4.getTcpFlagString()).thenReturn("TestString2");
		
		PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket);
		Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo5.getPayloadLen()).thenReturn(20);
		Mockito.when(packetInfo5.getLen()).thenReturn(15);
		Mockito.when(packetInfo5.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo5.getTcpFlagString()).thenReturn("Test2String");
		
		//UDP Packet Mock
				UDPPacket udpPacket2 = Mockito.mock(UDPPacket.class);
				Mockito.when(udpPacket2.isDNSPacket()).thenReturn(true);
				Mockito.when(udpPacket2.getDns()).thenReturn(dns1);
				Mockito.when(udpPacket2.getSourcePort()).thenReturn(90);
				Mockito.when(udpPacket2.getSourceIPAddress()).thenReturn(iAdr4);
				Mockito.when(udpPacket2.getDestinationPort()).thenReturn(91);
				Mockito.when(udpPacket2.getDestinationIPAddress()).thenReturn(iAdr5);
		
		PacketInfo packetInfo6 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo6.getPacket()).thenReturn(udpPacket);
		//Mockito.when(packetInfo6.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo6.getDir()).thenReturn(PacketDirection.UNKNOWN);
		//Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		//Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo6.getPayloadLen()).thenReturn(30);
		Mockito.when(packetInfo6.getLen()).thenReturn(10);
		Mockito.when(packetInfo6.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo6.getTcpFlagString()).thenReturn("TestString2");
		
		List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
		packetsList.add(packetInfo1); //Adding UDP Packet to the list
		packetsList.add(packetInfo2); //Adding TCP Packet to the list
		packetsList.add(packetInfo3);
		packetsList.add(packetInfo4);
		packetsList.add(packetInfo5);
		packetsList.add(packetInfo6);
		
		List<Session> sessionsList = sessionMgr.assembleSession(packetsList);
		assertEquals(3,sessionsList.size());
		
		
	}
	
	@Test
	public void assembleSessionTest1(){
		Date date = new Date();
	
/*		InetAddress iAdr = null;
		InetAddress iAdr1 = null;
		try {
			iAdr = InetAddress.getByAddress(new byte[]{89,10,1,1});
			iAdr1 = InetAddress.getByAddress(new byte[]{89,10,1,1});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	
		InetAddress iAdr = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr.getHostAddress()).thenReturn("@att");
		Mockito.when(iAdr.getHostName()).thenReturn("att");
		InetAddress iAdr1 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr1.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr1.getHostAddress()).thenReturn("@att");
		Mockito.when(iAdr1.getHostName()).thenReturn("att");
		
		Set<InetAddress> inetSet = new HashSet<InetAddress>();
		inetSet.add(iAdr);
		inetSet.add(iAdr1);
		
		IPPacket ipPack = Mockito.mock(IPPacket.class);
		Mockito.when(ipPack.getDestinationIPAddress()).thenReturn(iAdr1);
		Mockito.when(ipPack.getSourceIPAddress()).thenReturn(iAdr);
		Mockito.when(ipPack.getLen()).thenReturn(20);
		Mockito.when(ipPack.getPacketLength()).thenReturn(20);
		Mockito.when(ipPack.getPayloadLen()).thenReturn(10);
		
		DomainNameSystem dns = Mockito.mock(DomainNameSystem.class);
		Mockito.when(dns.getIpAddresses()).thenReturn(inetSet);
		Mockito.when(dns.getDomainName()).thenReturn("www.att.com");
		Mockito.when(dns.isResponse()).thenReturn(true);
		Mockito.when(dns.getCname()).thenReturn("ATT");
		Mockito.when(dns.getPacket()).thenReturn(ipPack);
		
		
		//UDP Packet Mock
		UDPPacket udpPacket = Mockito.mock(UDPPacket.class);
		Mockito.when(udpPacket.isDNSPacket()).thenReturn(false);
		Mockito.when(udpPacket.getDns()).thenReturn(dns);
		Mockito.when(udpPacket.getSourcePort()).thenReturn(83);
		Mockito.when(udpPacket.getDestinationPort()).thenReturn(84);
		Mockito.when(udpPacket.getSourceIPAddress()).thenReturn(iAdr);
		Mockito.when(udpPacket.getDestinationIPAddress()).thenReturn(iAdr1);
		
		
		
		PacketInfo packetInfo1 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo1.getPacket()).thenReturn(udpPacket);
		Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		//Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UNKNOWN);
		//Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo1.getPayloadLen()).thenReturn(20);
		Mockito.when(packetInfo1.getLen()).thenReturn(10);
		Mockito.when(packetInfo1.getAppName()).thenReturn("Test1");
		Mockito.when(packetInfo1.getTcpFlagString()).thenReturn("TestString");
		
		InetAddress iAdr2 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr2.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr2.getHostAddress()).thenReturn("@att");
		Mockito.when(iAdr2.getHostName()).thenReturn("att");
		
/*		InetAddress iAdr2 = null;
		InetAddress iAdr3 = null;
		try {
			iAdr2 = InetAddress.getByAddress(new byte[]{89,10,1,1});
			iAdr3 = InetAddress.getByAddress(new byte[]{89,10,1,1});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		
		
		TCPPacket tcpPacket = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket.getSourcePort()).thenReturn(81);
		Mockito.when(tcpPacket.getDestinationPort()).thenReturn(82);
		Mockito.when(tcpPacket.getDestinationIPAddress()).thenReturn(iAdr2);
		Mockito.when(tcpPacket.getSourceIPAddress()).thenReturn(iAdr2);
		Mockito.when(tcpPacket.getAckNumber()).thenReturn(0l);
		Mockito.when(tcpPacket.getLen()).thenReturn(50);
		Mockito.when(tcpPacket.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket.isRST()).thenReturn(true);
		Mockito.when(tcpPacket.getSequenceNumber()).thenReturn(-3l);
		Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-3000);
		//Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-10000);
		
		PacketInfo packetInfo2 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo2.getPacket()).thenReturn(tcpPacket);
		Mockito.when(packetInfo2.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo2.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo2.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo2.getLen()).thenReturn(15);
		Mockito.when(packetInfo2.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo2.getTcpFlagString()).thenReturn("Test2String");
		
		InetAddress iAdr3 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr3.getAddress()).thenReturn(new byte[]{89,10,1,1});
		Mockito.when(iAdr3.getHostAddress()).thenReturn("@att");
		Mockito.when(iAdr3.getHostName()).thenReturn("att");
		
		
		TCPPacket tcpPacket2 = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket2.getSourcePort()).thenReturn(95);
		Mockito.when(tcpPacket2.getDestinationPort()).thenReturn(99);
		Mockito.when(tcpPacket2.getDestinationIPAddress()).thenReturn(iAdr3);
		Mockito.when(tcpPacket2.getSourceIPAddress()).thenReturn(iAdr3);
		Mockito.when(tcpPacket.getAckNumber()).thenReturn(0l);
		Mockito.when(tcpPacket2.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket2.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket2.isRST()).thenReturn(true);
		Mockito.when(tcpPacket.getSequenceNumber()).thenReturn(1l);
		Mockito.when(tcpPacket2.getTimeStamp()).thenReturn(10000d);
		
		PacketInfo packetInfo3 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo3.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo3.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo3.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK_RECOVER);
		Mockito.when(packetInfo3.getPayloadLen()).thenReturn(30);
		Mockito.when(packetInfo3.getLen()).thenReturn(20);
		Mockito.when(packetInfo3.getAppName()).thenReturn("Test3");
		Mockito.when(packetInfo3.getTcpFlagString()).thenReturn("Test3String");
/*		
		PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo4.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo4.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK);
		
		PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket2);
		Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_DATA_RECOVER);
*/		
		
		InetAddress iAdr4 = Mockito.mock(InetAddress.class);
		InetAddress iAdr5 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr4.getAddress()).thenReturn(new byte[]{89,10,5,1});
		Mockito.when(iAdr4.getHostAddress()).thenReturn("@microfoft");
		Mockito.when(iAdr4.getHostName()).thenReturn("microfoft");
		Mockito.when(iAdr5.getAddress()).thenReturn(new byte[]{72,12,23,1});
		Mockito.when(iAdr5.getHostAddress()).thenReturn("@apple");
		Mockito.when(iAdr5.getHostName()).thenReturn("apple");
		
		Set<InetAddress> inetSet1 = new HashSet<InetAddress>();
		inetSet1.add(iAdr4);
		inetSet1.add(iAdr5);
		
		IPPacket ipPack1 = Mockito.mock(IPPacket.class);
		Mockito.when(ipPack1.getDestinationIPAddress()).thenReturn(iAdr5);
		Mockito.when(ipPack1.getSourceIPAddress()).thenReturn(iAdr4);
		Mockito.when(ipPack1.getLen()).thenReturn(30);
		Mockito.when(ipPack1.getPacketLength()).thenReturn(40);
		Mockito.when(ipPack1.getPayloadLen()).thenReturn(40);
		
		DomainNameSystem dns1 = Mockito.mock(DomainNameSystem.class);
		Mockito.when(dns1.getIpAddresses()).thenReturn(inetSet);
		Mockito.when(dns1.getDomainName()).thenReturn("www.microsft.com");
		Mockito.when(dns1.isResponse()).thenReturn(false);
		Mockito.when(dns1.getCname()).thenReturn("microsoft");
		Mockito.when(dns1.getPacket()).thenReturn(ipPack1);
		
		
		//UDP Packet Mock
		UDPPacket udpPacket1 = Mockito.mock(UDPPacket.class);
		Mockito.when(udpPacket1.isDNSPacket()).thenReturn(true);
		Mockito.when(udpPacket1.getDns()).thenReturn(dns1);
		Mockito.when(udpPacket1.getSourcePort()).thenReturn(90);
		Mockito.when(udpPacket1.getSourceIPAddress()).thenReturn(iAdr4);
		Mockito.when(udpPacket1.getDestinationPort()).thenReturn(91);
		Mockito.when(udpPacket1.getDestinationIPAddress()).thenReturn(iAdr5);
		
		
		PacketInfo packetInfo4 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo4.getPacket()).thenReturn(udpPacket);
		Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.DOWNLINK);
		//Mockito.when(packetInfo4.getDir()).thenReturn(PacketDirection.UNKNOWN);
		//Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		//Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo4.getPayloadLen()).thenReturn(20);
		Mockito.when(packetInfo4.getLen()).thenReturn(10);
		Mockito.when(packetInfo4.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo4.getTcpFlagString()).thenReturn("TestString2");
		
		PacketInfo packetInfo5 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo5.getPacket()).thenReturn(tcpPacket);
		Mockito.when(packetInfo5.getDir()).thenReturn(PacketDirection.UPLINK);
		Mockito.when(packetInfo5.getTcpInfo()).thenReturn(TcpInfo.TCP_ESTABLISH);
		Mockito.when(packetInfo5.getPayloadLen()).thenReturn(20);
		Mockito.when(packetInfo5.getLen()).thenReturn(15);
		Mockito.when(packetInfo5.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo5.getTcpFlagString()).thenReturn("Test2String");
		
		//UDP Packet Mock
				UDPPacket udpPacket2 = Mockito.mock(UDPPacket.class);
				Mockito.when(udpPacket2.isDNSPacket()).thenReturn(true);
				Mockito.when(udpPacket2.getDns()).thenReturn(dns1);
				Mockito.when(udpPacket2.getSourcePort()).thenReturn(90);
				Mockito.when(udpPacket2.getSourceIPAddress()).thenReturn(iAdr4);
				Mockito.when(udpPacket2.getDestinationPort()).thenReturn(91);
				Mockito.when(udpPacket2.getDestinationIPAddress()).thenReturn(iAdr5);
		
		PacketInfo packetInfo6 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo6.getPacket()).thenReturn(udpPacket);
		//Mockito.when(packetInfo6.getDir()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(packetInfo6.getDir()).thenReturn(PacketDirection.UNKNOWN);
		//Mockito.when(packetInfo1.getDir()).thenReturn(PacketDirection.UPLINK);
		//Mockito.when(packetInfo1.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo6.getPayloadLen()).thenReturn(30);
		Mockito.when(packetInfo6.getLen()).thenReturn(10);
		Mockito.when(packetInfo6.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo6.getTcpFlagString()).thenReturn("TestString2");
	
		InetAddress iAdr6 = Mockito.mock(InetAddress.class);
		Mockito.when(iAdr6.getAddress()).thenReturn(new byte[]{60,100,1,1});
		Mockito.when(iAdr6.getHostAddress()).thenReturn("@hari");
		Mockito.when(iAdr6.getHostName()).thenReturn("hari");
		
		TCPPacket tcpPacket6 = Mockito.mock(TCPPacket.class);
		Mockito.when(tcpPacket6.getSourcePort()).thenReturn(95);
		Mockito.when(tcpPacket6.getDestinationPort()).thenReturn(96);
		Mockito.when(tcpPacket6.getDestinationIPAddress()).thenReturn(iAdr6);
		Mockito.when(tcpPacket6.getSourceIPAddress()).thenReturn(iAdr6);
		Mockito.when(tcpPacket6.getAckNumber()).thenReturn(1l);
		Mockito.when(tcpPacket6.getLen()).thenReturn(50);
		Mockito.when(tcpPacket6.isSYN()).thenReturn(true);
		Mockito.when(tcpPacket6.isFIN()).thenReturn(true);
		Mockito.when(tcpPacket6.isRST()).thenReturn(false);
		Mockito.when(tcpPacket6.getSequenceNumber()).thenReturn(5l);
		Mockito.when(tcpPacket6.getTimeStamp()).thenReturn((double)date.getTime()-20000);
		//Mockito.when(tcpPacket.getTimeStamp()).thenReturn((double)date.getTime()-10000);
		
		PacketInfo packetInfo7 = Mockito.mock(PacketInfo.class);
		Mockito.when(packetInfo7.getPacket()).thenReturn(tcpPacket);
		Mockito.when(packetInfo7.getDir()).thenReturn(PacketDirection.UNKNOWN);
		Mockito.when(packetInfo7.getTcpInfo()).thenReturn(TcpInfo.TCP_ACK_DUP);
		Mockito.when(packetInfo7.getPayloadLen()).thenReturn(10);
		Mockito.when(packetInfo7.getLen()).thenReturn(15);
		Mockito.when(packetInfo7.getAppName()).thenReturn("Test2");
		Mockito.when(packetInfo7.getTcpFlagString()).thenReturn("Test2String");
		
		
		List<PacketInfo> packetsList = new ArrayList<PacketInfo>();
		packetsList.add(packetInfo1); //Adding UDP Packet to the list
		packetsList.add(packetInfo2); //Adding TCP Packet to the list
		packetsList.add(packetInfo3);
		packetsList.add(packetInfo4);
		packetsList.add(packetInfo5);
		packetsList.add(packetInfo6);
		packetsList.add(packetInfo7);
		
		List<Session> sessionsList = sessionMgr.assembleSession(packetsList);
		assertEquals(5,sessionsList.size());
		
		
	}
	
		
}
