package com.att.aro.core.configuration.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;

public class ProfileFactoryImplTest extends BaseTest{

	
	@InjectMocks
	IProfileFactory profilefactory = new ProfileFactoryImpl();
	
	Date date = new Date();
	
	@Test
	public void create_profileTypeIsLTE(){
		
		Properties property01 = Mockito.mock(Properties.class);
		Mockito.when(property01.getProperty(Profile.CARRIER)).thenReturn("AT&T");
		Mockito.when(property01.getProperty(Profile.DEVICE)).thenReturn("Captivate - ad study");
		ProfileLTE profileTest = (ProfileLTE) profilefactory.create(ProfileType.LTE, property01);
		assertEquals(0.26,profileTest.getPromotionTime(),0.0);
		
	}

	@Test
	public void create_profileTypeIsWifi(){
		Properties property01 = Mockito.mock(Properties.class);
		ProfileWiFi profileTest = (ProfileWiFi)profilefactory.create(ProfileType.WIFI, property01);
		assertEquals(0.25,profileTest.getWifiTailTime(),0.0);

	}
	
	@Test
	public void create_profileTypeIs3G(){
		Properties property01 = Mockito.mock(Properties.class);
		
		Profile3G profileTest = (Profile3G)profilefactory.create(ProfileType.T3G, property01);
		assertEquals(5,profileTest.getDchFachTimer(),0.0);

	}
	
	@Test
	public void energy3G_RRCStateIsTAIL_DCH(){
		Profile3G profile3g = Mockito.mock(Profile3G.class);
		Mockito.when(profile3g.getPowerDch()).thenReturn(0.7);
		double testResult = profilefactory
				.energy3G(date.getTime()+0.0, date.getTime()+1000.0, RRCState.TAIL_DCH, profile3g);
		assertEquals(700.0,testResult,0.0);
	}
	
	@Test
	public void energy3G_RRCStateIsTAIL_FACH(){
		Profile3G profile3g = Mockito.mock(Profile3G.class);
		Mockito.when(profile3g.getPowerFach()).thenReturn(0.35);
		double testResult = profilefactory
				.energy3G(date.getTime()+0.0, date.getTime()+1000.0, RRCState.TAIL_FACH, profile3g);
		assertEquals(350.0,testResult,0.0);
	}
	 
	@Test 
	public void energy3G_RRCStateIsSTATE_FACH(){

		Profile3G profile3g = Mockito.mock(Profile3G.class);
		double testResult = profilefactory
				.energy3G(date.getTime()+0.0, date.getTime()+1000.0, RRCState.STATE_FACH, profile3g);
		assertEquals(0.0f,testResult,0.0);
	}
	
	@Test
	public void energy3G_RRCStateIsSTATE_IDLE(){

		Profile3G profile3g = Mockito.mock(Profile3G.class);
		Mockito.when(profile3g.getPowerIdle()).thenReturn(0.2);
		double testResult = profilefactory
				.energy3G(date.getTime()+0.0, date.getTime()+1000.0, RRCState.STATE_IDLE, profile3g);
		assertEquals(200.0,testResult,0.0);
	}
	
	@Test
	public void energy3G_RRCStateIsPROMO_FACH_DCH(){

		Profile3G profile3g = Mockito.mock(Profile3G.class);
		Mockito.when(profile3g.getPowerFachDch()).thenReturn(0.3);
		double testResult = profilefactory
				.energy3G(date.getTime()+0.0, date.getTime()+1000.0, RRCState.PROMO_FACH_DCH, profile3g);
		assertEquals(300.0,testResult,0.0);
	}

	@Test
	public void save3G_Profile3g() throws IOException{
		OutputStream output = Mockito.mock(OutputStream.class);
		Profile3G profile3g = Mockito.mock(Profile3G.class);		
		when(profile3g.getCarrier()).thenReturn("AT&T");
		when(profile3g.getDevice()).thenReturn("Captivate - ad study");
		when(profile3g.getProfileType()).thenReturn(ProfileType.T3G);
		when(profile3g.getUserInputTh()).thenReturn(1.0);
		when(profile3g.getPowerGpsActive()).thenReturn(1.0);
		when(profile3g.getPowerGpsStandby()).thenReturn(0.5);
		when(profile3g.getPowerCameraOn()).thenReturn(0.3);
		when(profile3g.getPowerBluetoothActive()).thenReturn(1.0);
		when(profile3g.getPowerBluetoothStandby()).thenReturn(0.5);
		when(profile3g.getPowerScreenOn()).thenReturn(0.3);
		when(profile3g.getBurstTh()).thenReturn(1.5);
		when(profile3g.getLongBurstTh()).thenReturn(5.0);
		when(profile3g.getPeriodMinCycle()).thenReturn(10.0);
		when(profile3g.getPeriodCycleTol()).thenReturn(1.0);
		when(profile3g.getLargeBurstDuration()).thenReturn(5.0);
		when(profile3g.getLargeBurstSize()).thenReturn(100000);
		when(profile3g.getCloseSpacedBurstThreshold()).thenReturn(10.0);
		when(profile3g.getThroughputWindow()).thenReturn(0.5);
		profilefactory.save3G(output, profile3g);
		
	}
	
	@Test
	public void saveLTE_ProfileLTE() throws IOException{
		OutputStream output = Mockito.mock(OutputStream.class);
		ProfileLTE profileLte = Mockito.mock(ProfileLTE.class);
		when(profileLte.getCarrier()).thenReturn("AT&T");
		when(profileLte.getDevice()).thenReturn("Captivate - ad study");
		when(profileLte.getProfileType()).thenReturn(ProfileType.LTE);
		when(profileLte.getUserInputTh()).thenReturn(1.0);
		when(profileLte.getPowerGpsActive()).thenReturn(1.0);
		when(profileLte.getPowerGpsStandby()).thenReturn(0.5);
		when(profileLte.getPowerCameraOn()).thenReturn(0.3);
		when(profileLte.getPowerBluetoothActive()).thenReturn(1.0);
		when(profileLte.getPowerBluetoothStandby()).thenReturn(0.5);
		when(profileLte.getPowerScreenOn()).thenReturn(0.3);
		when(profileLte.getBurstTh()).thenReturn(1.5);
		when(profileLte.getLongBurstTh()).thenReturn(5.0);
		when(profileLte.getPeriodMinCycle()).thenReturn(10.0);
		when(profileLte.getPeriodCycleTol()).thenReturn(1.0);
		when(profileLte.getLargeBurstDuration()).thenReturn(5.0);
		when(profileLte.getLargeBurstSize()).thenReturn(100000);
		when(profileLte.getCloseSpacedBurstThreshold()).thenReturn(10.0);
		when(profileLte.getThroughputWindow()).thenReturn(0.5);
		profilefactory.saveLTE(output, profileLte);
	}
	
	
	@Test
	public void saveWiFi_() throws IOException{
		OutputStream output = Mockito.mock(OutputStream.class);
		ProfileWiFi profileWifi = Mockito.mock(ProfileWiFi.class);
		when(profileWifi.getCarrier()).thenReturn("AT&T");
		when(profileWifi.getDevice()).thenReturn("Captivate - ad study");
		when(profileWifi.getProfileType()).thenReturn(ProfileType.LTE);
		when(profileWifi.getUserInputTh()).thenReturn(1.0);
		when(profileWifi.getPowerGpsActive()).thenReturn(1.0);
		when(profileWifi.getPowerGpsStandby()).thenReturn(0.5);
		when(profileWifi.getPowerCameraOn()).thenReturn(0.3);
		when(profileWifi.getPowerBluetoothActive()).thenReturn(1.0);
		when(profileWifi.getPowerBluetoothStandby()).thenReturn(0.5);
		when(profileWifi.getPowerScreenOn()).thenReturn(0.3);
		when(profileWifi.getBurstTh()).thenReturn(1.5);
		when(profileWifi.getLongBurstTh()).thenReturn(5.0);
		when(profileWifi.getPeriodMinCycle()).thenReturn(10.0);
		when(profileWifi.getPeriodCycleTol()).thenReturn(1.0);
		when(profileWifi.getLargeBurstDuration()).thenReturn(5.0);
		when(profileWifi.getLargeBurstSize()).thenReturn(100000);
		when(profileWifi.getCloseSpacedBurstThreshold()).thenReturn(10.0);
		when(profileWifi.getThroughputWindow()).thenReturn(0.5);
		profilefactory.saveWiFi(output, profileWifi);

	}
	
	@Test
	public void energyLTE_RRCStateIsLTE_DRX_SHORT(){
		ProfileLTE profileLte01 = Mockito.mock(ProfileLTE.class);
		List<PacketInfo> packets = new ArrayList<PacketInfo>();	
		when(profileLte01.getDrxShortPingPeriod()).thenReturn(5.0);
		when(profileLte01.getDrxShortPingPower()).thenReturn(1.0);
		when(profileLte01.getLteTailPower()).thenReturn(2.0);
		when(profileLte01.getDrxPingTime()).thenReturn(1.0);
		double testResult = 
		profilefactory.energyLTE(date.getTime()+0.0, date.getTime()+1000.0, RRCState.LTE_DRX_SHORT, profileLte01,packets);
		assertEquals(1800.0,testResult,0.0);
	}
	
	@Test
	public void energyLTE_RRCStateIsLTE_DRX_LONG(){
		ProfileLTE profileLte05 = Mockito.mock(ProfileLTE.class);
		List<PacketInfo> packets = new ArrayList<PacketInfo>();
		when(profileLte05.getDrxLongPingPeriod()).thenReturn(5.0);
		when(profileLte05.getDrxLongPingPower()).thenReturn(1.0);
		when(profileLte05.getLteTailPower()).thenReturn(2.0);
		when(profileLte05.getDrxPingTime()).thenReturn(1.0);

		double testResult =
		profilefactory.energyLTE(date.getTime()+0.0, date.getTime()+1000.0, RRCState.LTE_DRX_LONG, profileLte05,packets);
		assertEquals(1800.0,testResult,0.0);

	}
	
	@Test
	public void energyLTE_RRCStateIsLTE_IDLE(){
		ProfileLTE profileLte02 = Mockito.mock(ProfileLTE.class);
		List<PacketInfo> packets = new ArrayList<PacketInfo>();
		when(profileLte02.getIdlePingTime()).thenReturn(1.0);
		when(profileLte02.getLteIdlePingPower()).thenReturn(1.0);
		when(profileLte02.getIdlePingPeriod()).thenReturn(2.0);
		when(profileLte02.getLteIdlePower()).thenReturn(1.0);
		double testResult = 
		profilefactory.energyLTE(date.getTime()+0.0, date.getTime()+1000.0, RRCState.LTE_IDLE, profileLte02,packets);
		assertEquals(1000.0,testResult,0.0);
	}
	
	@Test
	public void energyLTE_RRCStateIsLTE_IDLE_a(){
		ProfileLTE profileLte03 = Mockito.mock(ProfileLTE.class);
		List<PacketInfo> packets = new ArrayList<PacketInfo>();
		when(profileLte03.getIdlePingTime()).thenReturn(1.0);
		when(profileLte03.getLteIdlePingPower()).thenReturn(1.0);
		when(profileLte03.getIdlePingPeriod()).thenReturn(9.0);
		when(profileLte03.getLteIdlePower()).thenReturn(1.0);
		double testResult =
		profilefactory.energyLTE(date.getTime()+0.0, date.getTime()+14750.0, RRCState.LTE_IDLE, profileLte03,packets);
		assertEquals(14750.0,testResult,0.0);

	}
	@Test
	public void energyLTE_RRCStateIsLTE_CR_TAIL(){
		ProfileLTE profileLte04 = Mockito.mock(ProfileLTE.class);
		List<PacketInfo> packets = new ArrayList<PacketInfo>();
		when(profileLte04.getIdlePingTime()).thenReturn(1.0);
		when(profileLte04.getLteIdlePingPower()).thenReturn(1.0);
		when(profileLte04.getIdlePingPeriod()).thenReturn(2.0);
		when(profileLte04.getLteIdlePower()).thenReturn(1.0);
		double testResult =
		profilefactory.energyLTE(date.getTime()+0.0, date.getTime()+1000.0, RRCState.LTE_CR_TAIL, profileLte04,packets);
		assertEquals(0.0,testResult,0.0);

	}

	
	@Test
	public void energyLTE_RRCStateIsLTE_PROMOTION(){
		ProfileLTE profileLte06 = Mockito.mock(ProfileLTE.class);
		List<PacketInfo> packets = new ArrayList<PacketInfo>();

		when(profileLte06.getLtePromotionPower()).thenReturn(3.0);
		double testResult =
		profilefactory.energyLTE(date.getTime()+0.0, date.getTime()+1000.0, RRCState.LTE_PROMOTION, profileLte06,packets);
		assertEquals(3000.0,testResult,0.0);

	}
	
	
	@Test
	public void energyWifi_RRCStateIsWIFI_ACTIVE(){
		ProfileWiFi profileWifi = Mockito.mock(ProfileWiFi.class);	
		when(profileWifi.getWifiActivePower()).thenReturn(2.0);
		double testResult =
		profilefactory.energyWiFi(date.getTime()+0.0, date.getTime()+1000.0, RRCState.WIFI_ACTIVE, profileWifi);
		assertEquals(2000.0,testResult,0.0);

	}
	@Test
	public void energyWifi_RRCStateIsWIFI_IDLE(){
		ProfileWiFi profileWifi02 = Mockito.mock(ProfileWiFi.class);	
		when(profileWifi02.getWifiActivePower()).thenReturn(3.0);
		double testResult =
		profilefactory.energyWiFi(date.getTime()+0.0, date.getTime()+1000.0, RRCState.WIFI_IDLE, profileWifi02);
		assertEquals(0.0,testResult,0.0);

	}

	
}
