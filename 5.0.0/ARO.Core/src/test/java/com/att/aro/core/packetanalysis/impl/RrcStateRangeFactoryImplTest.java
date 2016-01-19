package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.IRrcStateRangeFactory;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetreader.pojo.PacketDirection;

/*
 * 82 % coverage
 */
public class RrcStateRangeFactoryImplTest extends BaseTest{	
	
	Date date = new Date();	
	IRrcStateRangeFactory rrcStateRangeFactory;
	PacketInfo[] pktInfoArray = new PacketInfo[47];

	@Before
	public void setUp(){
		rrcStateRangeFactory = context.getBean(IRrcStateRangeFactory.class);

		for(int i = 0; i < 47; i++){
			pktInfoArray[i] = mock(PacketInfo.class);
		}
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void reset(){
		for(int i = 0; i < 47; i++){
			Mockito.reset(pktInfoArray[i]);
		}

	}
	
	
	@Test
	public void create_LTEIsIdle(){

		ProfileLTE profile02 = mock(ProfileLTE.class);
		when(profile02.getProfileType()).thenReturn(ProfileType.LTE);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		double traceDuration = 2000.0;
		List<RrcStateRange> testResult= rrcStateRangeFactory.create(packetlist1, profile02, traceDuration);	
		assertEquals(1,testResult.size());
	}
	
	
	@Test
	public void create_ProfileIsLTE(){//not sure
		
		ProfileLTE profile01 = mock(ProfileLTE.class);
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		double traceDuration = 2000.0;
		
		when(profile01.getProfileType()).thenReturn(ProfileType.LTE);
		when(profile01.getPromotionTime()).thenReturn(1000.0);
		
		when(profile01.getInactivityTimer()).thenReturn(1000.0);
		when(profile01.getDrxShortTime()).thenReturn(1000.0);
		when(profile01.getDrxLongTime()).thenReturn(1000.0);
	 	
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);

		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+100.0);

		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+500.0);

		when(pktInfoArray[3].getTimeStamp()).thenReturn(date.getTime()+2300.0);

		when(pktInfoArray[4].getTimeStamp()).thenReturn(date.getTime()+3005.0);

		when(pktInfoArray[5].getTimeStamp()).thenReturn(date.getTime()+4500.0);

		when(pktInfoArray[6].getTimeStamp()).thenReturn(date.getTime()+5501.0);

		when(pktInfoArray[7].getTimeStamp()).thenReturn(date.getTime()+6001.0);

		when(pktInfoArray[8].getTimeStamp()).thenReturn(date.getTime()+9001.0);

		when(pktInfoArray[9].getTimeStamp()).thenReturn(date.getTime()+16001.0);

		when(pktInfoArray[10].getTimeStamp()).thenReturn(date.getTime()+17001.0);

		when(pktInfoArray[11].getTimeStamp()).thenReturn(date.getTime()+19000.0);

		when(pktInfoArray[12].getTimeStamp()).thenReturn(date.getTime()+29900.0);

		when(pktInfoArray[13].getTimeStamp()).thenReturn(date.getTime()+35500.0);

		when(pktInfoArray[14].getTimeStamp()).thenReturn(date.getTime()+45005.0);

		when(pktInfoArray[15].getTimeStamp()).thenReturn(date.getTime()+46500.0);

		when(pktInfoArray[16].getTimeStamp()).thenReturn(date.getTime()+47501.0);

		when(pktInfoArray[17].getTimeStamp()).thenReturn(date.getTime()+47601.0);

		when(pktInfoArray[18].getTimeStamp()).thenReturn(date.getTime()+57001.0);
	
		when(pktInfoArray[19].getTimeStamp()).thenReturn(date.getTime()+66001.0);

		
		for(int i = 0; i < 20; i++){
			packetlist.add(pktInfoArray[i]);
		}
		List<RrcStateRange> testList = 
		rrcStateRangeFactory.create(packetlist, profile01, traceDuration);	
		assertEquals(64,testList.size());

	}
	
	
	@Test
	public void create_WIFIIsIdleTrace(){

		ProfileWiFi profile04 = mock(ProfileWiFi.class);
		when(profile04.getProfileType()).thenReturn(ProfileType.WIFI);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		double traceDuration = 2000.0;
		List<RrcStateRange> testList = 
		rrcStateRangeFactory.create(packetlist1, profile04, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	
	@Test
	public void create_ProfileIsWifi(){

		ProfileWiFi profile03 = mock(ProfileWiFi.class);
		when(profile03.getProfileType()).thenReturn(ProfileType.WIFI);
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		double traceDuration = 1000.0;
		when(profile03.getWifiTailTime()).thenReturn(1500.0);
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()-500.0);
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+500.0);
		when(pktInfoArray[3].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[4].getTimeStamp()).thenReturn(date.getTime()+2505.0);
		when(pktInfoArray[5].getTimeStamp()).thenReturn(date.getTime()+3500.0);
		when(pktInfoArray[6].getTimeStamp()).thenReturn(date.getTime()+4501.0);
		when(pktInfoArray[7].getTimeStamp()).thenReturn(date.getTime()+5001.0);
		when(pktInfoArray[8].getTimeStamp()).thenReturn(date.getTime()+6001.0);
		when(pktInfoArray[9].getTimeStamp()).thenReturn(date.getTime()+6501.0);
		when(pktInfoArray[10].getTimeStamp()).thenReturn(date.getTime()+7001.0);
		when(pktInfoArray[11].getTimeStamp()).thenReturn(date.getTime()+9000.0);
		when(pktInfoArray[12].getTimeStamp()).thenReturn(date.getTime()+9900.0);
		when(pktInfoArray[13].getTimeStamp()).thenReturn(date.getTime()+15500.0);
		when(pktInfoArray[14].getTimeStamp()).thenReturn(date.getTime()+25005.0);
		when(pktInfoArray[15].getTimeStamp()).thenReturn(date.getTime()+36500.0);
		when(pktInfoArray[16].getTimeStamp()).thenReturn(date.getTime()+47501.0);
		when(pktInfoArray[17].getTimeStamp()).thenReturn(date.getTime()+57601.0);
		when(pktInfoArray[18].getTimeStamp()).thenReturn(date.getTime()+107001.0);
		when(pktInfoArray[19].getTimeStamp()).thenReturn(date.getTime()+216001.0);
		
		for(int i = 0; i < 20; i++){
			packetlist.add(pktInfoArray[i]);
		}
		List<RrcStateRange> testList = 
		rrcStateRangeFactory.create(packetlist, profile03, traceDuration);	
		assertEquals(26,testList.size());
	}

	
	@Test
	public void create3G_(){
		Profile3G profile3g = mock(Profile3G.class);

		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(1000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(1000.0);
		
		double traceDuration = 2000.0;		
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+100.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[3].getTimeStamp()).thenReturn(date.getTime()+2300.0);
		when(pktInfoArray[3].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[3].getLen()).thenReturn(1000);
		when(pktInfoArray[3].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		
		when(pktInfoArray[4].getTimeStamp()).thenReturn(date.getTime()+3005.0);
		when(pktInfoArray[4].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[4].getLen()).thenReturn(1000);
		when(pktInfoArray[4].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		
		when(pktInfoArray[5].getTimeStamp()).thenReturn(date.getTime()+4500.0);
		when(pktInfoArray[5].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[5].getLen()).thenReturn(1000);
		when(pktInfoArray[5].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[6].getTimeStamp()).thenReturn(date.getTime()+5501.0);
		when(pktInfoArray[6].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[6].getLen()).thenReturn(1000);
		when(pktInfoArray[6].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[7].getTimeStamp()).thenReturn(date.getTime()+6001.0);
		when(pktInfoArray[7].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[7].getLen()).thenReturn(1000);
		when(pktInfoArray[7].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[8].getTimeStamp()).thenReturn(date.getTime()+9601.0);
		when(pktInfoArray[8].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[8].getLen()).thenReturn(1000);
		when(pktInfoArray[8].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		when(pktInfoArray[9].getTimeStamp()).thenReturn(date.getTime()+16001.0);
		when(pktInfoArray[9].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[9].getLen()).thenReturn(1000);
		when(pktInfoArray[9].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		when(pktInfoArray[10].getTimeStamp()).thenReturn(date.getTime()+17601.0);
		when(pktInfoArray[10].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[10].getLen()).thenReturn(1000);
		when(pktInfoArray[10].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		when(pktInfoArray[11].getTimeStamp()).thenReturn(date.getTime()+18000.0);
		when(pktInfoArray[11].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[11].getLen()).thenReturn(1000);
		when(pktInfoArray[11].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		when(pktInfoArray[12].getTimeStamp()).thenReturn(date.getTime()+29900.0);
		when(pktInfoArray[12].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[12].getLen()).thenReturn(1000);
		when(pktInfoArray[12].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		when(pktInfoArray[13].getTimeStamp()).thenReturn(date.getTime()+35500.0);
		when(pktInfoArray[13].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[13].getLen()).thenReturn(1000);
		when(pktInfoArray[13].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		when(pktInfoArray[14].getTimeStamp()).thenReturn(date.getTime()+45005.0);
		when(pktInfoArray[14].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[14].getLen()).thenReturn(1000);
		when(pktInfoArray[14].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		when(pktInfoArray[15].getTimeStamp()).thenReturn(date.getTime()+46500.0);
		when(pktInfoArray[15].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[15].getLen()).thenReturn(1000);
		when(pktInfoArray[15].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		when(pktInfoArray[16].getTimeStamp()).thenReturn(date.getTime()+47501.0);
		when(pktInfoArray[16].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[16].getLen()).thenReturn(1000);
		when(pktInfoArray[16].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		when(pktInfoArray[17].getTimeStamp()).thenReturn(date.getTime()+47601.0);
		when(pktInfoArray[17].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[17].getLen()).thenReturn(1000);
		when(pktInfoArray[17].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		when(pktInfoArray[18].getTimeStamp()).thenReturn(date.getTime()+57001.0);
		when(pktInfoArray[18].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[18].getLen()).thenReturn(1000);
		when(pktInfoArray[18].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		when(pktInfoArray[19].getTimeStamp()).thenReturn(date.getTime()+66001.0);
		when(pktInfoArray[19].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[19].getLen()).thenReturn(1000);
		when(pktInfoArray[19].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		when(pktInfoArray[20].getTimeStamp()).thenReturn(date.getTime()+66301.0);
		when(pktInfoArray[20].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[20].getLen()).thenReturn(1000);
		when(pktInfoArray[20].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		when(pktInfoArray[21].getTimeStamp()).thenReturn(date.getTime()+62301.0);
		when(pktInfoArray[21].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[21].getLen()).thenReturn(1000);
		when(pktInfoArray[21].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		when(pktInfoArray[22].getTimeStamp()).thenReturn(date.getTime()+63101.0);
		when(pktInfoArray[22].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[22].getLen()).thenReturn(1000);
		when(pktInfoArray[22].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		when(pktInfoArray[23].getTimeStamp()).thenReturn(date.getTime()+65501.0);
		when(pktInfoArray[23].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[23].getLen()).thenReturn(1000);
		when(pktInfoArray[23].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[24].getTimeStamp()).thenReturn(date.getTime()+67501.0);
		when(pktInfoArray[24].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[24].getLen()).thenReturn(1000);
		when(pktInfoArray[24].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[25].getTimeStamp()).thenReturn(date.getTime()+70501.0);
		when(pktInfoArray[25].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[25].getLen()).thenReturn(1000);
		when(pktInfoArray[25].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[26].getTimeStamp()).thenReturn(date.getTime()+80501.0);
		when(pktInfoArray[26].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[26].getLen()).thenReturn(1000);
		when(pktInfoArray[26].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[27].getTimeStamp()).thenReturn(date.getTime()+84601.0);
		when(pktInfoArray[27].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[27].getLen()).thenReturn(1000);
		when(pktInfoArray[27].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[28].getTimeStamp()).thenReturn(date.getTime()+86101.0);
		when(pktInfoArray[28].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[28].getLen()).thenReturn(1000);
		when(pktInfoArray[28].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[29].getTimeStamp()).thenReturn(date.getTime()+86301.0);
		when(pktInfoArray[29].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[29].getLen()).thenReturn(1000);
		when(pktInfoArray[29].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[30].getTimeStamp()).thenReturn(date.getTime()+87901.0);
		when(pktInfoArray[30].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[30].getLen()).thenReturn(1000);
		when(pktInfoArray[30].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[31].getTimeStamp()).thenReturn(date.getTime()+88301.0);
		when(pktInfoArray[31].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[31].getLen()).thenReturn(1000);
		when(pktInfoArray[31].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[32].getTimeStamp()).thenReturn(date.getTime()+90401.0);
		when(pktInfoArray[32].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[32].getLen()).thenReturn(1000);
		when(pktInfoArray[32].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
	
		when(pktInfoArray[33].getTimeStamp()).thenReturn(date.getTime()+91101.0);
		when(pktInfoArray[33].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[33].getLen()).thenReturn(1000);
		when(pktInfoArray[33].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		
		when(pktInfoArray[34].getTimeStamp()).thenReturn(date.getTime()+91500.0);
		when(pktInfoArray[34].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[34].getLen()).thenReturn(1000);
		when(pktInfoArray[34].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		
		when(pktInfoArray[35].getTimeStamp()).thenReturn(date.getTime()+94605.0);
		when(pktInfoArray[35].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[35].getLen()).thenReturn(1000);
		when(pktInfoArray[35].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		
		when(pktInfoArray[36].getTimeStamp()).thenReturn(date.getTime()+98700.0);
		when(pktInfoArray[36].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[36].getLen()).thenReturn(1000);
		when(pktInfoArray[36].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		
		when(pktInfoArray[37].getTimeStamp()).thenReturn(date.getTime()+98800.0);
		when(pktInfoArray[37].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[37].getLen()).thenReturn(1000);
		when(pktInfoArray[37].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[38].getTimeStamp()).thenReturn(date.getTime()+98900.0);
		when(pktInfoArray[38].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[38].getLen()).thenReturn(1000);
		when(pktInfoArray[38].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[39].getTimeStamp()).thenReturn(date.getTime()+99200.0);
		when(pktInfoArray[39].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[39].getLen()).thenReturn(1000);
		when(pktInfoArray[39].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[40].getTimeStamp()).thenReturn(date.getTime()+99300.0);
		when(pktInfoArray[40].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[40].getLen()).thenReturn(1000);
		when(pktInfoArray[40].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[41].getTimeStamp()).thenReturn(date.getTime()+109400.0);
		when(pktInfoArray[41].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[41].getLen()).thenReturn(1000);
		when(pktInfoArray[41].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[42].getTimeStamp()).thenReturn(date.getTime()+109500.0);
		when(pktInfoArray[42].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[42].getLen()).thenReturn(1000);
		when(pktInfoArray[42].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[43].getTimeStamp()).thenReturn(date.getTime()+119600.0);
		when(pktInfoArray[43].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[43].getLen()).thenReturn(1000);
		when(pktInfoArray[43].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[44].getTimeStamp()).thenReturn(date.getTime()+123600.0);
		when(pktInfoArray[44].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[44].getLen()).thenReturn(1000);
		when(pktInfoArray[44].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[45].getTimeStamp()).thenReturn(date.getTime()+124500.0);
		when(pktInfoArray[45].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[45].getLen()).thenReturn(1000);
		when(pktInfoArray[45].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		
		when(pktInfoArray[46].getTimeStamp()).thenReturn(date.getTime()+139600.0);
		when(pktInfoArray[46].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[46].getLen()).thenReturn(1000);
		when(pktInfoArray[46].getStateMachine()).thenReturn(RRCState.STATE_FACH);

		for(int i = 0; i < 47; i++){
			packetlist.add(pktInfoArray[i]);
		}
		List<RrcStateRange> testList = 
		rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}

	@Test
	public void create3G_test2(){// promotestate is TAIL_DCH
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(1000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(1000.0);
		double traceDuration = 2000.0;		
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.TAIL_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+100.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[1]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void create3G_test3(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn((double)date.getTime());
		when(profile3g.getIdleDchPromoMax()).thenReturn(1500.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(1000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(1000.0);
		double traceDuration = 2000.0;		
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+100.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[1]);		
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void create3G_test4(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn((double)date.getTime());
		when(profile3g.getIdleDchPromoMin()).thenReturn((double)date.getTime());
		when(profile3g.getIdleDchPromoMax()).thenReturn((double)date.getTime());
		when(profile3g.getFachDchPromoAvg()).thenReturn((double)date.getTime());
		when(profile3g.getFachDchPromoMin()).thenReturn((double)date.getTime());
		when(profile3g.getFachDchPromoMax()).thenReturn((double)date.getTime());
		when(profile3g.getDchFachTimer()).thenReturn((double)date.getTime());
		when(profile3g.getFachIdleTimer()).thenReturn((double)date.getTime());
		double traceDuration = 2000.0;		
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+100.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[2]);
		when(pktInfoArray[3].getTimeStamp()).thenReturn(date.getTime()+2300.0);
		when(pktInfoArray[3].getDir()).thenReturn(PacketDirection.UNKNOWN);
		when(pktInfoArray[3].getLen()).thenReturn(1000);
		when(pktInfoArray[3].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[3]);
		when(pktInfoArray[4].getTimeStamp()).thenReturn(date.getTime()+3005.0);
		when(pktInfoArray[4].getDir()).thenReturn(PacketDirection.UNKNOWN);
		when(pktInfoArray[4].getLen()).thenReturn(1000);
		when(pktInfoArray[4].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[4]);
		when(pktInfoArray[5].getTimeStamp()).thenReturn(date.getTime()+4500.0);
		when(pktInfoArray[5].getDir()).thenReturn(PacketDirection.UNKNOWN);
		when(pktInfoArray[5].getLen()).thenReturn(1000);
		when(pktInfoArray[5].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[5]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void Create3G_test5(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(5000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(10000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-5500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+5000.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void Create3G_test6(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(5000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(10000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-5500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+5000.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void Create3G_test7(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(5000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(10000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-7500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.UNKNOWN);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+10000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+15000.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.UNKNOWN);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void Create3G_test8(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(5000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(10000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-7500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.PROMO_FACH_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+10000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+15000.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.PROMO_IDLE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	 
	@Test
	public void Create3G_test9(){// promoState ==  RRCState.STATE_DCH
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(5000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(10000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-7500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+10000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+15000.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void Create3G_test10(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(5000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(10000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-3500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());

	}
	@Test
	public void Create3G_test11(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(2000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());

	}
	
	@Test
	public void Create3G_test12(){
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(2000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_DCH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);
		assertEquals(1,testList.size());

	}
	
	@Test
	public void Create3G_test13(){	//RRCState.STATE_FACH
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(2000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.UPLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}
	
	@Test
	public void Create3G_test14(){	//RRCState.STATE_FACH
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(2000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}

	@Test
	public void Create3G_test15(){	//RRCState.STATE_FACH
		Profile3G profile3g = mock(Profile3G.class);
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getIdleDchPromoAvg()).thenReturn(12000.0);
		when(profile3g.getIdleDchPromoMin()).thenReturn(1000.0);
		when(profile3g.getIdleDchPromoMax()).thenReturn(20000.0);
		when(profile3g.getFachDchPromoAvg()).thenReturn(1000.0);
		when(profile3g.getFachDchPromoMin()).thenReturn(2500.0);
		when(profile3g.getFachDchPromoMax()).thenReturn(6000.0);
		when(profile3g.getDchFachTimer()).thenReturn(1000.0);
		when(profile3g.getFachIdleTimer()).thenReturn(2000.0);
		double traceDuration = 2000.0;	
		List<PacketInfo> packetlist = new ArrayList<PacketInfo>();
		when(pktInfoArray[0].getTimeStamp()).thenReturn(date.getTime()-1500.0);
		when(pktInfoArray[0].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[0].getLen()).thenReturn(1000);
		when(pktInfoArray[0].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[0]);
		when(pktInfoArray[1].getTimeStamp()).thenReturn(date.getTime()+1000.0);
		when(pktInfoArray[1].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[1].getLen()).thenReturn(1000);
		when(pktInfoArray[1].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[1]);		
		when(pktInfoArray[2].getTimeStamp()).thenReturn(date.getTime()+1500.0);
		when(pktInfoArray[2].getDir()).thenReturn(PacketDirection.DOWNLINK);
		when(pktInfoArray[2].getLen()).thenReturn(1000);
		when(pktInfoArray[2].getStateMachine()).thenReturn(RRCState.STATE_FACH);
		packetlist.add(pktInfoArray[2]);
		
		List<RrcStateRange> testList = 
				rrcStateRangeFactory.create(packetlist, profile3g, traceDuration);	
		assertEquals(1,testList.size());
	}

}
