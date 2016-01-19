package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IWifiInfoReader;
import com.att.aro.core.peripheral.pojo.WifiInfo;
import com.att.aro.core.util.Util;

public class WifiInfoReaderImplTest extends BaseTest {

	WifiInfoReaderImpl traceDataReader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		traceDataReader = (WifiInfoReaderImpl)context.getBean(IWifiInfoReader.class);
		traceDataReader.setFileReader(filereader);
	}

	String wifi_events = "" + Util.FILE_SEPARATOR + TraceDataConst.FileName.WIFI_FILE;
	

	@Test
	public void readData() throws IOException {
		
		List<WifiInfo> wifiInfos = null;

		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(getMockedFileData());
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(11, wifiInfos.size(),0);

		assertEquals("WIFI_CONNECTED", wifiInfos.get(0).getWifiState().toString());
		assertEquals("84:db:2f:0b:fe:f0", wifiInfos.get(0).getWifiMacAddress());
		assertEquals("Elevate-FEF0", wifiInfos.get(0).getWifiSSID());
		
		assertEquals("84:db:2f:0b:fe:f0", wifiInfos.get(0).getWifiMacAddress());
		assertEquals("WIFI_DISCONNECTED", wifiInfos.get(1).getWifiState().toString());
		
	}	
	
	
	/*
	 * wifi_events is missing
	 */
	@Test
	public void readData_NoFile() throws IOException {
		
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(false);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(getMockedFileData());
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(0, wifiInfos.size(),0);
		
	}	
	
	@Test
	public void readData_Exception_readAllLine() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("Exception_readAllLine"));

		List<WifiInfo> wifiInfos = traceDataReader.readData(traceFolder, 0, 0);
		assertTrue(wifiInfos.size() == 0);

	}

//	/*
//	 * first line WIFI_OFF
//	 */
//	@Ignore
//	@Test
//	public void readData_wifi_off() throws IOException {
//		List<WifiInfo> wifiInfos = null;
//		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
//		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
//				new String[] {
//				 "1.41142116305E9 WIFI_OFF"
//				,"1.411421163196E9 WIFI_OFF 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
//		traceDataReader.setFileReader(filereader);
//		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);
//
//		assertEquals(1, wifiInfos.size(),0);
//		assertEquals("WIFI_OFF", wifiInfos.get(0).getWifiState().toString());
//		
//	}	
	
	@Test
	public void readData_wifi_disconnected() throws IOException {
		
		
		/*
		 * first line WIFI_DISCONNECTED
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 DISCONNECTED"
				,"1.411421163196E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_DISCONNECTED", wifiInfos.get(0).getWifiState().toString());
		
	}	
	
	@Test
	public void readData_wifi_disconnected_duplicated() throws IOException {
		
		
		/*
		 * first line WIFI_DISCONNECTED
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 DISCONNECTED"
				,"1.41142116315E9 DISCONNECTED"
				,"1.411421163196E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_DISCONNECTED", wifiInfos.get(0).getWifiState().toString());
		
	}	

	@Test
	public void readData_wifi_connecting() throws IOException {
		/*
		 * first line WIFI_DISCONNECTED
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 CONNECTING"
				,"1.411421163196E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_CONNECTING", wifiInfos.get(0).getWifiState().toString());
		
	}	
	
	
	@Test
	public void readData_wifi_disconnecting() throws IOException {
		/*
		 * first line WIFI_DISCONNECTING
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 DISCONNECTING"
				,"1.411421163196E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_DISCONNECTING", wifiInfos.get(0).getWifiState().toString());
		
	}	
	
	
	@Test
	public void readData_wifi_suspended() throws IOException {
		/*
		/*
		 * first line WIFI_SUSPENDED
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 SUSPENDED"
				,"1.411421163196E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_SUSPENDED", wifiInfos.get(0).getWifiState().toString());
	}	
	
	
	@Test
	public void readData_wifi_unknown() throws IOException {
		/*
		 * first line WIFI_UNKNOWN
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 UNKNOWN"
				,"1.411421163196E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_UNKNOWN", wifiInfos.get(0).getWifiState().toString());

	
	}	
	
	
	@Test
	public void readData_wifi_disconnected_line2() throws IOException {
		/*
		 * 2nd line WIFI_DISCONNECTED
		 */
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
				,"1.411421163196E9 DISCONNECTED"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_DISCONNECTED", wifiInfos.get(1).getWifiState().toString());
		
	}	
	
	
	/*
	 * 2nd line WIFI_DISCONNECTED
	 */
	@Test
	public void readData_wifi_connecting_line2() throws IOException {
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
				,"1.411421163196E9 CONNECTING"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_CONNECTING", wifiInfos.get(1).getWifiState().toString());
	}
		/*
		 * 2nd line WIFI_DISCONNECTING
		 */
		@Test
	public void readData_wifi_disconnecting_line2() throws IOException {
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] { "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
						, "1.411421163196E9 DISCONNECTING" });
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(), 0);
		assertEquals("WIFI_DISCONNECTING", wifiInfos.get(1).getWifiState().toString());
	}	
		
		/*
		 * 2nd line WIFI_SUSPENDED
		 */
		@Test
		public void readData_wifi_suspended_line2() throws IOException {
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
				,"1.411421163196E9 SUSPENDED"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_SUSPENDED", wifiInfos.get(1).getWifiState().toString());
		}
		
		
		/*
		 * 2nd line WIFI_UNKNOWN
		 */
		@Test
		public void readData_wifi_unknown_line2() throws IOException {
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
				,"1.411421163196E9 UNKNOWN"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		assertEquals(2, wifiInfos.size(),0);
		assertEquals("WIFI_UNKNOWN", wifiInfos.get(1).getWifiState().toString());

	}
	
	@Test
	public void getWifiActiveDuration() throws IOException {
		
		List<WifiInfo> wifiInfos = null;
		Mockito.when(filereader.fileExist(wifi_events)).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {
				 "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
				,"1.412421163196E9 CONNECTED 00:24:36:a7:08:5a -47 The Rabbit Hole 5GHz"});
		traceDataReader.setFileReader(filereader);
		wifiInfos = traceDataReader.readData("", 1.412361724E12, 1412361675045.0);

		double activeDuration = traceDataReader.getWifiActiveDuration();

		assertEquals(1.412361675045E12, activeDuration, 0);
		
	}		


	/*
	 * video_time contains starttime & endtime
	 */
	private String[] getMockedFileData() {
		return new String[] {
				 "1.41142116305E9 CONNECTED 84:db:2f:0b:fe:f0 -74 Elevate-FEF0"
				,"1.411421163196E9 DISCONNECTED"
				,"1.411421163219E9 CONNECTED 84:db:2f:0b:fe:f0 -75 Elevate-FEF0"
				,"1.411691771589E9 CONNECTED 00:24:36:a7:08:5a -47 The Rabbit Hole 5GHz"
				,"1.411691771705E9 DISCONNECTED"
				,"1.411691771721E9 CONNECTED 00:24:36:a7:08:5a -47 The Rabbit Hole 5GHz"
				,"1.411691789406E9 CONNECTED 00:24:36:a7:08:5a -48 The Rabbit Hole 5GHz"
				,"1.411691789499E9 DISCONNECTED"
				,"1.411691789524E9 CONNECTED 00:24:36:a7:08:5a -49 The Rabbit Hole 5GHz"
				,"1.411691801901E9 CONNECTED 00:24:36:a7:08:5a -49 The Rabbit Hole 5GHz"
				,"1.411691801999E9 DISCONNECTED"
				,"1.411691802011E9 CONNECTED 00:24:36:a7:08:5a -48 The Rabbit Hole 5GHz"
				,"1.412361674641E9 CONNECTED 84:db:2f:0b:fe:e9 -70 Elevate-FEE9"
				,"1.412361674741E9 DISCONNECTED"
				,"1.412361674754E9 CONNECTED 84:db:2f:0b:fe:e9 -73 Elevate-FEE9"

		};  
	}       
}           
