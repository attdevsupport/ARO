package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.IEnergyModelFactory;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo.BluetoothState;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo.CameraState;
import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo.GpsState;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo.ScreenState;

public class EnergyModelFactoryImplTest extends BaseTest{

	@InjectMocks
	IEnergyModelFactory eMdlFctr;
	
	@Before
	public void setUp(){
		eMdlFctr = context.getBean(IEnergyModelFactory.class);
	}
	
	@Test
	public void create_Test(){
		Date date = new Date();
		Profile profile = Mockito.mock(Profile.class);
		Mockito.when(profile.getPowerGpsActive()).thenReturn(1.0);
		GpsInfo gpsInfo01 = Mockito.mock(GpsInfo.class);
		Mockito.when(gpsInfo01.getBeginTimeStamp()).thenReturn(date.getTime()+0.0);
		Mockito.when(gpsInfo01.getEndTimeStamp()).thenReturn(date.getTime()+1000.0);
		Mockito.when(gpsInfo01.getGpsState()).thenReturn(GpsState.GPS_ACTIVE);
		
		GpsInfo gpsInfo02 = Mockito.mock(GpsInfo.class);
		Mockito.when(profile.getPowerGpsStandby()).thenReturn(0.5);
		Mockito.when(gpsInfo02.getBeginTimeStamp()).thenReturn(date.getTime()+0.0);
		Mockito.when(gpsInfo02.getEndTimeStamp()).thenReturn(date.getTime()+1000.0);
		Mockito.when(gpsInfo02.getGpsState()).thenReturn(GpsState.GPS_STANDBY);

		List<GpsInfo> gpsList = new ArrayList<GpsInfo>();
		gpsList.add(gpsInfo01);
		gpsList.add(gpsInfo02);
		
		
		CameraInfo cameraInfo01 = Mockito.mock(CameraInfo.class);
		Mockito.when(cameraInfo01.getBeginTimeStamp()).thenReturn(date.getTime()+0.0);
		Mockito.when(cameraInfo01.getEndTimeStamp()).thenReturn(date.getTime()+1000.0);
		Mockito.when(cameraInfo01.getCameraState()).thenReturn(CameraState.CAMERA_ON);
		Mockito.when(profile.getPowerCameraOn()).thenReturn(0.3);

		List<CameraInfo> cameraList = new ArrayList<CameraInfo>();
		cameraList.add(cameraInfo01);

		
		BluetoothInfo bluetoothInfo01 = Mockito.mock(BluetoothInfo.class);
		Mockito.when(bluetoothInfo01.getBeginTimeStamp()).thenReturn(date.getTime()+0.0);
		Mockito.when(bluetoothInfo01.getEndTimeStamp()).thenReturn(date.getTime()+1000.0);
		Mockito.when(bluetoothInfo01.getBluetoothState()).thenReturn(BluetoothState.BLUETOOTH_CONNECTED);
		BluetoothInfo bluetoothInfo02 = Mockito.mock(BluetoothInfo.class);
		Mockito.when(bluetoothInfo02.getBeginTimeStamp()).thenReturn(date.getTime()+0.0);
		Mockito.when(bluetoothInfo02.getEndTimeStamp()).thenReturn(date.getTime()+1000.0);
		Mockito.when(bluetoothInfo02.getBluetoothState()).thenReturn(BluetoothState.BLUETOOTH_DISCONNECTED);
		
		List<BluetoothInfo> bluetoothList = new ArrayList<BluetoothInfo>();
		bluetoothList.add(bluetoothInfo01);
		bluetoothList.add(bluetoothInfo02);

		Mockito.when(profile.getPowerBluetoothActive()).thenReturn(1.0);
		Mockito.when(profile.getPowerBluetoothStandby()).thenReturn(0.5);

		ScreenStateInfo screenStateInfo01 = Mockito.mock(ScreenStateInfo.class);
		Mockito.when(screenStateInfo01.getBeginTimeStamp()).thenReturn(date.getTime()+0.0);
		Mockito.when(screenStateInfo01.getEndTimeStamp()).thenReturn(date.getTime()+1000.0);
		Mockito.when(screenStateInfo01.getScreenState()).thenReturn(ScreenState.SCREEN_ON);
		List<ScreenStateInfo> screenStateList = new ArrayList<ScreenStateInfo>();
		screenStateList.add(screenStateInfo01);
		Mockito.when(profile.getPowerScreenOn()).thenReturn(0.3);

		EnergyModel model = eMdlFctr.create(profile, 0.0, gpsList, cameraList, bluetoothList, screenStateList);
		
		assertEquals(1000.0,model.getGpsActiveEnergy(),0.0);
		assertEquals(500.0,model.getGpsStandbyEnergy(),0.0);
		assertEquals(1500.0,model.getTotalGpsEnergy(),0.0);
		assertEquals(300.0,model.getTotalCameraEnergy(),0.0);
		assertEquals(1000.0,model.getBluetoothActiveEnergy(),0.0);
		assertEquals(500.0,model.getBluetoothStandbyEnergy(),0.0);
		assertEquals(1500.0,model.getTotalBluetoothEnergy(),0.0);
		assertEquals(300.0,model.getTotalScreenEnergy(),0.0);
		
	} 
	
}
