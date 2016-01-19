package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.IRrcStateMachineFactory;
import com.att.aro.core.packetanalysis.IRrcStateRangeFactory;
import com.att.aro.core.packetanalysis.pojo.AbstractRrcStateMachine;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachine3G;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineWiFi;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetanalysis.pojo.TimeRange;


public class RrcStateMachineFactoryImplTest extends BaseTest{
	
	@Mock
	IRrcStateRangeFactory staterange;
	@Mock
	IProfileFactory profilefactory;
	@InjectMocks
	IRrcStateMachineFactory machineFactoryimpl;
	Date date = new Date();
	@Before
	public void setUp(){
		machineFactoryimpl = (RrcStateMachineFactoryImpl)context.getBean(IRrcStateMachineFactory.class);//put ahead
		MockitoAnnotations.initMocks(this);
		
	}
	
	double traceDuration = 2000.0;
	double packetDuration = 1000.0;
	double totalBytes = 100.0;
	

	@Test
	public void create_TimeRangeIsNotNull(){
		ProfileWiFi profile01 = mock(ProfileWiFi.class);
		when(profile01.getProfileType()).thenReturn(ProfileType.WIFI);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		TimeRange timeRange = mock(TimeRange.class);
		when(timeRange.getBeginTime()).thenReturn((double)date.getTime()-3000.0);
		when(timeRange.getEndTime()).thenReturn((double)date.getTime()+1000.0);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();	
		
		RrcStateRange rrc01 = mock(RrcStateRange.class);
		RrcStateRange rrc02 = mock(RrcStateRange.class);
		RrcStateRange rrc03 = mock(RrcStateRange.class);
 
		when(rrc01.getBeginTime()).thenReturn((double)date.getTime()-3000.0);
		when(rrc01.getEndTime()).thenReturn((double)date.getTime()-2000.0);
		when(rrc02.getBeginTime()).thenReturn((double)date.getTime()-1000.0);
		when(rrc02.getEndTime()).thenReturn((double)date.getTime());
		when(rrc03.getBeginTime()).thenReturn((double)date.getTime()+1000.0);
		when(rrc03.getEndTime()).thenReturn((double)date.getTime()+2000.0);
		 
		
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(2.0);
		when(rrc01.getState()).thenReturn(RRCState.TAIL_FACH);
		when(rrc02.getState()).thenReturn(RRCState.TAIL_FACH);
		when(rrc03.getState()).thenReturn(RRCState.TAIL_FACH);
		staterangelist.add(rrc01); 
		staterangelist.add(rrc02); 
		staterangelist.add(rrc03); 

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);
		AbstractRrcStateMachine rrcStateMachinewifi = machineFactoryimpl.create(packetlist1, profile01, packetDuration, traceDuration, 0.0, timeRange);
		assertEquals(0.0,rrcStateMachinewifi.getJoulesPerKilobyte(),0.0);
		
	} 
	

	@Test
	public void create_WIFIStateIsWIFI_ACTIVE(){
		ProfileWiFi profile02 = mock(ProfileWiFi.class);
		when(profile02.getProfileType()).thenReturn(ProfileType.WIFI);
		List<PacketInfo> packetlist2 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();	
		RrcStateRange rrc01 = mock(RrcStateRange.class);
		when(rrc01.getBeginTime()).thenReturn((double)date.getTime());
		when(rrc01.getEndTime()).thenReturn(date.getTime()+2000.0);
		when(profilefactory.energyWiFi(any(double.class), any(double.class), any(RRCState.class), any(ProfileWiFi.class)))
		.thenReturn(2.0);
		when(rrc01.getState()).thenReturn(RRCState.WIFI_ACTIVE);
		staterangelist.add(rrc01);
		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);
		RrcStateMachineWiFi rrcStateMachineWifi = 
				(RrcStateMachineWiFi)machineFactoryimpl.create(packetlist2, profile02, packetDuration, traceDuration, totalBytes, null);
		assertEquals(20.0,rrcStateMachineWifi.getJoulesPerKilobyte(),0.0);
		assertEquals(1000.0,rrcStateMachineWifi.getPacketsDuration(),0.0);
		assertEquals(2.0,rrcStateMachineWifi.getTotalRRCEnergy(),0.0);
		assertEquals(2000.0,rrcStateMachineWifi.getTraceDuration(),0.0);
	}
	
	@Test
	public void create_WIFIStateIsWIFI_TAIL(){
		ProfileWiFi profile17 = mock(ProfileWiFi.class);
		when(profile17.getProfileType()).thenReturn(ProfileType.WIFI);
		when(profilefactory.energyWiFi(any(double.class), any(double.class), any(RRCState.class), any(ProfileWiFi.class)))
		.thenReturn(100.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.WIFI_TAIL);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*1000);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*1000.0);

		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);
		
		RrcStateMachineWiFi rrcStateMachineWifi = (RrcStateMachineWiFi)machineFactoryimpl
		.create(packetlist1, profile17, packetDuration, traceDuration, totalBytes*100, null);
		assertEquals(50,rrcStateMachineWifi.getJoulesPerKilobyte(),0.0);
		assertEquals(500,rrcStateMachineWifi.getWifiTailEnergy(),0.0);
		assertEquals(5000,rrcStateMachineWifi.getWifiTailTime(),0.0);

	}
 
	@Test
	public void create_WIFIStateIsWIFI_IDLE(){
		ProfileWiFi profile18 = mock(ProfileWiFi.class);
		when(profile18.getProfileType()).thenReturn(ProfileType.WIFI);
		when(profilefactory.energyWiFi(any(double.class), any(double.class), any(RRCState.class), any(ProfileWiFi.class)))
		.thenReturn(100.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.WIFI_IDLE);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*1000);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*1000.0);

		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}
		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);
		
		RrcStateMachineWiFi rrcStateMachineWifi = (RrcStateMachineWiFi)machineFactoryimpl
		.create(packetlist1, profile18, packetDuration, traceDuration, totalBytes*100, null);
		assertEquals(50,rrcStateMachineWifi.getJoulesPerKilobyte(),0.0);
		assertEquals(5000,rrcStateMachineWifi.getWifiIdleTime(),0.0);
		assertEquals(500,rrcStateMachineWifi.getWifiIdleEnergy(),0.0);

	}
	
	@Test
	public void create_LTEStateIsLTE_IDLE(){
		ProfileLTE profile03 = mock(ProfileLTE.class);
		when(profile03.getProfileType()).thenReturn(ProfileType.LTE);
		when(profilefactory.energyLTE(any(double.class), any(double.class), any(RRCState.class), any(ProfileLTE.class),any(ArrayList.class)))
		.thenReturn(100.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.LTE_IDLE);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*1000);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*1000.0);

		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);
		
		RrcStateMachineLTE rrcStateMachineLTE = (RrcStateMachineLTE)machineFactoryimpl
		.create(packetlist1, profile03, packetDuration, traceDuration, totalBytes*100, null);
		assertEquals(50,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(500,rrcStateMachineLTE.getTotalRRCEnergy(),0.0);
		assertEquals(5000,rrcStateMachineLTE.getLteIdleTime(),0.0);

	}

	@Test
	public void create_LTEStateIsLTE_PROMOTION(){
		ProfileLTE profile04 = mock(ProfileLTE.class);
		when(profile04.getProfileType()).thenReturn(ProfileType.LTE);
		when(profilefactory.energyLTE(any(double.class), any(double.class), any(RRCState.class), any(ProfileLTE.class),any(ArrayList.class)))
		.thenReturn(5.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.LTE_PROMOTION);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*1000);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*2000.0);
		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachineLTE rrcStateMachineLTE = 
		(RrcStateMachineLTE)machineFactoryimpl.create(packetlist1, profile04, packetDuration, traceDuration, totalBytes*1000, null);
		assertEquals(0.25,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(25,rrcStateMachineLTE.getLteIdleToCRPromotionEnergy(),0.0);
		assertEquals(30000,rrcStateMachineLTE.getLteIdleToCRPromotionTime(),0.0);


	}
	

	@Test
	public void create_LTEStateIsLTE_CONTINUOUS(){
		ProfileLTE profile05 = mock(ProfileLTE.class);
		when(profile05.getProfileType()).thenReturn(ProfileType.LTE);
		when(profilefactory.energyLTE(any(double.class), any(double.class), any(RRCState.class), any(ProfileLTE.class),any(ArrayList.class)))
		.thenReturn(4.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.LTE_CONTINUOUS);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*1000);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*1000.0);

		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);
		
		RrcStateMachineLTE rrcStateMachineLTE = (RrcStateMachineLTE)machineFactoryimpl
				.create(packetlist1, profile05, packetDuration, traceDuration, totalBytes*100, null);
		assertEquals(2,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(20,rrcStateMachineLTE.getLteCrEnergy(),0.0);
		assertEquals(5000,rrcStateMachineLTE.getLteCrTime(),0.0);

	}
	

	@Test
	public void create_LTEStateIsLTE_CR_TAIL(){
		ProfileLTE profile06 = mock(ProfileLTE.class);
		when(profile06.getProfileType()).thenReturn(ProfileType.LTE);
		when(profilefactory.energyLTE(any(double.class), any(double.class), any(RRCState.class), any(ProfileLTE.class),any(ArrayList.class)))
		.thenReturn(5.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.LTE_CR_TAIL);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*1000);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*1000.0);

		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachineLTE rrcStateMachineLTE = (RrcStateMachineLTE)machineFactoryimpl
				.create(packetlist1, profile06, packetDuration, traceDuration, totalBytes*200, null);		
		assertEquals(1.25,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(5000,rrcStateMachineLTE.getLteCrTailTime(),0.0);
		assertEquals(25,rrcStateMachineLTE.getLteCrTailEnergy(),0.0);

	}

	@Test
	public void create_LTEStateIsLTE_DRX_SHORT(){
		ProfileLTE profile07 = mock(ProfileLTE.class);
		when(profile07.getProfileType()).thenReturn(ProfileType.LTE);
		when(profilefactory.energyLTE(any(double.class), any(double.class), any(RRCState.class), any(ProfileLTE.class),any(ArrayList.class)))
		.thenReturn(6.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.LTE_DRX_SHORT);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);

		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachineLTE rrcStateMachineLTE = (RrcStateMachineLTE)machineFactoryimpl
				.create(packetlist1, profile07, packetDuration, traceDuration, totalBytes*125, null);		
		assertEquals(2.4,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachineLTE.getLteDrxShortTime(),0.0);
		assertEquals(30,rrcStateMachineLTE.getLteDrxShortEnergy(),0.0);

	}
	@Test
	public void create_LTEStateIsLTE_DRX_LONG(){
		ProfileLTE profile08 = mock(ProfileLTE.class);
		when(profile08.getProfileType()).thenReturn(ProfileType.LTE);
		when(profilefactory.energyLTE(any(double.class), any(double.class), any(RRCState.class), any(ProfileLTE.class),any(ArrayList.class)))
		.thenReturn(6.0);
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.LTE_DRX_LONG);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);

		}
		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachineLTE rrcStateMachineLTE = (RrcStateMachineLTE)machineFactoryimpl
				.create(packetlist1, profile08, packetDuration, traceDuration, totalBytes*125, null);		
		assertEquals(2.4,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachineLTE.getLteDrxLongTime(),0.0);
		assertEquals(30,rrcStateMachineLTE.getLteDrxLongEnergy(),0.0);

	}

	@Test
	public void create_LTEStateRrcStateRangeListIsEmpty(){
		ProfileLTE profile09 = mock(ProfileLTE.class);
		when(profile09.getProfileType()).thenReturn(ProfileType.LTE);

		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachineLTE rrcStateMachineLTE = (RrcStateMachineLTE)machineFactoryimpl
				.create(packetlist1, profile09, packetDuration, traceDuration, 0*totalBytes, null);		
		assertEquals(0.0,rrcStateMachineLTE.getJoulesPerKilobyte(),0.0);
		assertEquals(1000,rrcStateMachineLTE.getPacketsDuration(),0.0);
		assertEquals(2000,rrcStateMachineLTE.getTraceDuration(),0.0);

	}

	@Test
	public void create_T3GStateIsSTATE_IDLE(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile10 = mock(Profile3G.class);		
		when(profile10.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.STATE_IDLE);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile10, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachine3G.getIdleTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getIdleEnergy(),0.0);
		
	}

	@Test
	public void create_T3GStateIsSTATE_DCH(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile11 = mock(Profile3G.class);		
		when(profile11.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.STATE_DCH);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile11, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachine3G.getDchTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getDchEnergy(),0.0);
		
	}

	@Test
	public void create_T3GStateIsTAIL_DCH(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile11 = mock(Profile3G.class);		
		when(profile11.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.TAIL_DCH);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile11, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachine3G.getDchTailTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getDchTailEnergy(),0.0);
		
	}

	@Test
	public void create_T3GStateIsSTATE_FACH(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile12 = mock(Profile3G.class);		
		when(profile12.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.STATE_FACH);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile12, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachine3G.getFachTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getFachEnergy(),0.0);
		
	}
	
	@Test
	public void create_T3GStateIsTAIL_FACH(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile13 = mock(Profile3G.class);		
		when(profile13.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.TAIL_FACH);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile13, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(2500,rrcStateMachine3G.getFachTailTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getFachTailEnergy(),0.0);
		
	}
	

	@Test
	public void create_T3GStateIsPROMO_IDLE_DCH(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile14 = mock(Profile3G.class);		
		when(profile14.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.PROMO_IDLE_DCH);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile14, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(5,rrcStateMachine3G.getIdleToDch(),0.0);
		assertEquals(2500,rrcStateMachine3G.getIdleToDchTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getIdleToDchEnergy(),0.0);
		
	}

	@Test
	public void create_T3GStateIsPROMO_FACH_DCH(){
		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		when(profilefactory.energy3G(any(double.class), any(double.class), any(RRCState.class), any(Profile3G.class)))
		.thenReturn(100.0);
		Profile3G profile15 = mock(Profile3G.class);		
		when(profile15.getProfileType()).thenReturn(ProfileType.T3G);
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		RrcStateRange[] rrcStateRangeArray = new RrcStateRange[5];
		for(int i=0;i<5;i++){
			rrcStateRangeArray[i] = mock(RrcStateRange.class);
			when(rrcStateRangeArray[i].getState()).thenReturn(RRCState.PROMO_FACH_DCH);
			when(rrcStateRangeArray[i].getBeginTime()).thenReturn((double)date.getTime()+2*i*500);
			when(rrcStateRangeArray[i].getEndTime()).thenReturn((double)date.getTime()+(2*i+1)*500.0);
		}		
		for(int i=0;i<5;i++){
			staterangelist.add(rrcStateRangeArray[i]);
		}

		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile15, packetDuration, traceDuration, totalBytes*100, null);		
		assertEquals(50.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(5,rrcStateMachine3G.getFachToDch(),0.0);
		assertEquals(2500,rrcStateMachine3G.getFachToDchTime(),0.0);
		assertEquals(500,rrcStateMachine3G.getFachToDchEnergy(),0.0);
		
	}

	@Test
	public void create_3GStateRrcStateRangeListIsEmpty(){
		Profile3G profile16 = mock(Profile3G.class);
		when(profile16.getProfileType()).thenReturn(ProfileType.T3G);

		List<PacketInfo> packetlist1 = new ArrayList<PacketInfo>();
		List<RrcStateRange> staterangelist = new ArrayList<RrcStateRange>();
		when(staterange.create(any(ArrayList.class), any(Profile.class), any(double.class))).thenReturn(staterangelist);		
		RrcStateMachine3G rrcStateMachine3G = (RrcStateMachine3G)machineFactoryimpl
				.create(packetlist1, profile16, packetDuration, traceDuration, 0*totalBytes, null);		
		assertEquals(0.0,rrcStateMachine3G.getJoulesPerKilobyte(),0.0);
		assertEquals(1000,rrcStateMachine3G.getPacketsDuration(),0.0);
		assertEquals(2000,rrcStateMachine3G.getTraceDuration(),0.0);

	}


}
