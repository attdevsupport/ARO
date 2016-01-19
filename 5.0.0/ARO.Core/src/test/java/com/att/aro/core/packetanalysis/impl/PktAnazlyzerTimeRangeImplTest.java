package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.IPktAnazlyzerTimeRangeUtil;
import com.att.aro.core.packetanalysis.pojo.NetworkBearerTypeInfo;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.peripheral.pojo.BatteryInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo.BluetoothState;
import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo.CameraState;
import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo.GpsState;
import com.att.aro.core.peripheral.pojo.NetworkType;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo.ScreenState;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;
import com.att.aro.core.peripheral.pojo.WifiInfo;
import com.att.aro.core.peripheral.pojo.WifiInfo.WifiState;

public class PktAnazlyzerTimeRangeImplTest extends BaseTest {

	@InjectMocks
	IPktAnazlyzerTimeRangeUtil pktTimeUtil;
	
	@Before
	public void setup() {
		pktTimeUtil = (PktAnazlyzerTimeRangeImpl)context.getBean(IPktAnazlyzerTimeRangeUtil.class);
 		MockitoAnnotations.initMocks(this);		

	}
	
	@Test
	public void getTimeRangeResultTest(){
		
		
		TraceDirectoryResult result = new TraceDirectoryResult();
		
		List<UserEvent> userEvents = new ArrayList<UserEvent>();
		userEvents.add(new UserEvent(UserEventType.KEY_HOME, 4.0, 3.0));
		result.setUserEvents(userEvents);
		
		List<GpsInfo> gpsInfos = new ArrayList<GpsInfo>();
		gpsInfos.add(new GpsInfo(0.0, 0.9, GpsState.GPS_ACTIVE));
		gpsInfos.add(new GpsInfo(1.0,4.0, GpsState.GPS_ACTIVE));
		gpsInfos.add(new GpsInfo(1.4,12.0, GpsState.GPS_ACTIVE));
		gpsInfos.add(new GpsInfo(12.4,14.0, GpsState.GPS_ACTIVE));
		result.setGpsInfos(gpsInfos);
		
		List<BluetoothInfo> bluetoothInfos = new ArrayList<BluetoothInfo>();
		bluetoothInfos.add(new BluetoothInfo(0.0, 3.0, BluetoothState.BLUETOOTH_CONNECTED));
		bluetoothInfos.add(new BluetoothInfo(4.0, 10.0,BluetoothState.BLUETOOTH_CONNECTED));
		bluetoothInfos.add(new BluetoothInfo(1.0, 13.0,BluetoothState.BLUETOOTH_DISCONNECTED));
		bluetoothInfos.add(new BluetoothInfo(11.0, 16.0,BluetoothState.BLUETOOTH_CONNECTED));

		result.setBluetoothInfos(bluetoothInfos);
		
		List<CameraInfo> cameraInfos = new ArrayList<CameraInfo>();
		cameraInfos.add(new CameraInfo(0.0, 1.0, CameraState.CAMERA_ON));
		cameraInfos.add(new CameraInfo(3.0, 7.0, CameraState.CAMERA_ON));
		cameraInfos.add(new CameraInfo(8.0,14.0, CameraState.CAMERA_ON));
		cameraInfos.add(new CameraInfo(1.0,14.0, CameraState.CAMERA_ON));
		cameraInfos.add(new CameraInfo(12.0,15.0,CameraState.CAMERA_ON));
		
		result.setCameraInfos(cameraInfos);
		
		List<ScreenStateInfo> screenStateInfos = new ArrayList<ScreenStateInfo>();
		ScreenStateInfo screenInfo01 = new ScreenStateInfo(0.0, 1.0,
				ScreenState.SCREEN_ON, " ", 5);
		ScreenStateInfo screenInfo02 = new ScreenStateInfo(1.0,12.0,ScreenState.SCREEN_ON,"",3) ;
		ScreenStateInfo screenInfo03 = new ScreenStateInfo(5.0,9.0,ScreenState.SCREEN_ON,"",2) ;
		ScreenStateInfo screenInfo04 = new ScreenStateInfo(12.0,15.0,ScreenState.SCREEN_ON,"",4) ;

		screenStateInfos.add(screenInfo01);
		screenStateInfos.add(screenInfo02);
		screenStateInfos.add(screenInfo03);
		screenStateInfos.add(screenInfo04);

		result.setScreenStateInfos(screenStateInfos);
		
		List<RadioInfo> radioInfos = new ArrayList<RadioInfo>();
		radioInfos.add(new RadioInfo(0.0, 3.0));
		radioInfos.add(new RadioInfo(4.0, 8.0));
		result.setRadioInfos(radioInfos);
		
		List<BatteryInfo> batteryInfos = new ArrayList<BatteryInfo>();
		batteryInfos.add(new BatteryInfo(3.0, true, 2, 3));
		result.setBatteryInfos(batteryInfos);
		
		List<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();
		wifiInfos.add(new WifiInfo(1.0, 2.0, WifiState.WIFI_CONNECTED, "", "", ""));
		wifiInfos.add(new WifiInfo(1.0,5.0,WifiState.WIFI_CONNECTING, "","",""));
		wifiInfos.add(new WifiInfo(1.4,13.0,WifiState.WIFI_CONNECTING, "","",""));
		wifiInfos.add(new WifiInfo(9.0,13.0,WifiState.WIFI_CONNECTING, "","",""));
		
		result.setWifiInfos(wifiInfos);		
		
		List<NetworkType> networkTypesList = new ArrayList<NetworkType>();
		networkTypesList.add(NetworkType.LTE);
		result.setNetworkTypesList(networkTypesList);
		
		List<NetworkBearerTypeInfo> networkTypeInfos = new ArrayList<NetworkBearerTypeInfo>();
		networkTypeInfos.add(new NetworkBearerTypeInfo(1.0, 3.0, NetworkType.HSPA));
		networkTypeInfos.add(new NetworkBearerTypeInfo(8.0, 12.0, NetworkType.HSPA));

		result.setNetworkTypeInfos(networkTypeInfos);
		
		
		TimeRange timeRange = new TimeRange(2.00, 11.00);
		TraceDirectoryResult testResult = (TraceDirectoryResult)pktTimeUtil.getTimeRangeResult(result, timeRange);
		assertEquals(1,testResult.getBatteryInfos().size());
		assertEquals(2,testResult.getGpsInfos().size());
		assertEquals(1,testResult.getBatteryInfos().size());
		assertEquals(5,testResult.getCameraInfos().size());
		assertEquals(2,testResult.getScreenStateInfos().size());
		assertEquals(1,testResult.getRadioInfos().size());
		assertEquals(3,testResult.getWifiInfos().size());
		assertEquals(2,testResult.getNetworkTypeInfos().size());

		
	}
}
