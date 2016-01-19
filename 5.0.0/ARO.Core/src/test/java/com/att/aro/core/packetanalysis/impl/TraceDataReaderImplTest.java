package com.att.aro.core.packetanalysis.impl;


import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.packetanalysis.ITraceDataReader;
import com.att.aro.core.packetanalysis.pojo.ScheduledAlarmInfo;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceFileResult;
import com.att.aro.core.packetreader.IPacketListener;
import com.att.aro.core.packetreader.IPacketReader;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.peripheral.IAlarmAnalysisInfoParser;
import com.att.aro.core.peripheral.IAppInfoReader;
import com.att.aro.core.peripheral.IDeviceDetailReader;
import com.att.aro.core.peripheral.INetworkTypeReader;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisInfo;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisResult;
import com.att.aro.core.peripheral.pojo.AppInfo;
import com.att.aro.core.peripheral.pojo.DeviceDetail;
import com.att.aro.core.peripheral.pojo.NetworkTypeObject;
import com.att.aro.core.util.Util;


public class TraceDataReaderImplTest extends BaseTest {

//	@Spy
	@InjectMocks
	TraceDataReaderImpl traceDataReaderImpl;
	IFileManager filereader;

	@Mock
	IPacketReader packetreader;
	@Mock
	IAppInfoReader appinforeader;
	@Mock
	IDeviceDetailReader devicedetailreader;
	@Mock
	INetworkTypeReader networktypereader;
	@Mock
	IAlarmAnalysisInfoParser alarmanalysisinfoparser;
	
	@Before
	public void setUp(){
		traceDataReaderImpl = (TraceDataReaderImpl)context.getBean(ITraceDataReader.class);
		filereader = mock(IFileManager.class);
		MockitoAnnotations.initMocks(this);		
		
	}
	 
	@After
	public void reset(){
		Mockito.reset(packetreader);
		Mockito.reset(filereader);
		Mockito.reset(alarmanalysisinfoparser);
	}
	
	@Test
	public void readTraceFile_() throws IOException{
		Date date = new Date();
		traceDataReaderImpl.setFileReader(filereader);
		Mockito.doAnswer(new Answer<Object>(){
			public Object answer(InvocationOnMock invocation){
				byte b = 4;
				short s = 1;
				
				InetAddress address1 = null;
				InetAddress address2 = null;
				try {
					address2 = InetAddress.getByName("78.46.84.177");
					address1 = InetAddress.getByName("78.46.84.171");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Date date1 = new Date();

				IPPacket ippack01= mock(IPPacket.class);
				when(ippack01.getIPVersion()).thenReturn(b);
				when(ippack01.getFragmentOffset()).thenReturn(s);
				when(ippack01.getSourceIPAddress()).thenReturn(address1);
				when(ippack01.getDestinationIPAddress()).thenReturn(address2);
				when(ippack01.getTimeStamp()).thenReturn((double)date1.getTime());
				traceDataReaderImpl.packetArrived("flipboard.app",ippack01);//pretend jpcap lib 
				traceDataReaderImpl.packetArrived("com.google.android.youtube",ippack01);//pretend jpcap lib 
				traceDataReaderImpl.packetArrived("flipboard.app",ippack01);
				return null;
			}
		}).when(packetreader).readPacket(any(String.class), any(IPacketListener.class));
		when(filereader.fileExist(any(String.class))).thenReturn(true);
		when(filereader.getDirectory(any(String.class))).thenReturn(Util.getCurrentRunningDir());
		when(filereader.getLastModified(any(String.class))).thenReturn((long)date.getTime());
		TraceFileResult result = traceDataReaderImpl.readTraceFile(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"traffic.cap");
		assertSame(-1,result.getCaptureOffset());
		
	}
	  
	@Test
	public void readTraceFile_fileIsEmpty() throws IOException{
		Date date = new Date();
		traceDataReaderImpl.setFileReader(filereader);
		when(filereader.fileExist(any(String.class))).thenReturn(true);
		when(filereader.getDirectory(any(String.class))).thenReturn(Util.getCurrentRunningDir());
		when(filereader.getLastModified(any(String.class))).thenReturn((long)date.getTime());
		
		TraceFileResult result = traceDataReaderImpl.readTraceFile(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"traffic.cap");
		assertSame(0,result.getAllpackets().size());
	}
	
	@Test
	public void readTraceDir_()throws IOException{
		String[] time = {"Synchronized timestamps",
				"1410212153.578",
				"272927100",
				"1410213352.550"};
		String[] appId = {"5","5","13","-127"};
		traceDataReaderImpl.setFileReader(filereader);
		when(filereader.directoryExist(any(String.class))).thenReturn(true);
		when(filereader.fileExist(any(String.class))).thenReturn(true);
		when(filereader.readAllLine(any(String.class))).thenReturn(time);
		when(filereader.readAllLine(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"appid")).thenReturn(appId);
		when(filereader.fileExist(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"traffic1.cap")).thenReturn(false);
		Map<String, List<ScheduledAlarmInfo>> scheduledAlarms = new HashMap<String, List<ScheduledAlarmInfo>> ();
		List<AlarmAnalysisInfo> statistics = new ArrayList<AlarmAnalysisInfo>();
		
		AlarmAnalysisResult alarmResult = mock(AlarmAnalysisResult.class);
		when(alarmResult.getScheduledAlarms()).thenReturn(scheduledAlarms);
		when(alarmResult.getStatistics()).thenReturn(statistics);
		when(alarmanalysisinfoparser.parse(any(String.class), any(String.class), any(String.class), 
				any(double.class), any(double.class), any(Date.class))).thenReturn(alarmResult);
		Mockito.doAnswer(new Answer<Object>(){
			public Object answer(InvocationOnMock invocation){
				byte b = 3;
				short s = 1;
				
				InetAddress address1 = null;
				InetAddress address2 = null;
				try {
					address2 = InetAddress.getByName("78.46.84.177");
					address1 = InetAddress.getByName("78.46.84.171");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Date date1 = new Date();
 
				IPPacket ippack01= mock(IPPacket.class);
				when(ippack01.getIPVersion()).thenReturn(b);
				when(ippack01.getFragmentOffset()).thenReturn(s);
				when(ippack01.getSourceIPAddress()).thenReturn(address1);
				when(ippack01.getDestinationIPAddress()).thenReturn(address2);
				when(ippack01.getTimeStamp()).thenReturn((double)date1.getTime());
				traceDataReaderImpl.packetArrived("com.google.android.youtube",ippack01);//pretend jpcap lib 
				
				return null;
			} 
		}).when(packetreader).readPacket(any(String.class), any(IPacketListener.class));
		AppInfo app = new AppInfo();
		Map<String, String> appMap = new HashMap<String,String>();
		appMap.put("flipboard.app","2.3.8");
		appMap.put("com.att.android.arodatacollector", "3.1.1.6");
		appMap.put("com.google.android.youtube", "4.0.23");
		app.setAppVersionMap(appMap);
		List<String> appInfos = new ArrayList<String>();
		appInfos.add("flipboard.app");
		appInfos.add("com.att.android.arodatacollector");
		appInfos.add("com.google.android.youtube");
		app.setAppInfos(appInfos);	
		when(appinforeader.readData(any(String.class))).thenReturn(app);
		DeviceDetail device = new DeviceDetail();
		device.setTotalLines(8);
		device.setScreenSize("720*1280");
		when(devicedetailreader.readData(any(String.class))).thenReturn(device);
		NetworkTypeObject obj = new NetworkTypeObject();
		when(networktypereader
				.readData(any(String.class),any(double.class),any(double.class))).thenReturn(obj);
		TraceDirectoryResult result = traceDataReaderImpl.readTraceDirectory(Util.getCurrentRunningDir());
		assertSame(3,result.getAppIds().size());
	}
	
	@Test
	public void readTraceDir_checkExternalVideoAndTime()throws IOException{
		String[] time = {"1410212153.578 1410213352.550","272927100","1410213352.550"};
		String[] appId = {};
		traceDataReaderImpl.setFileReader(filereader);
		when(filereader.directoryExist(any(String.class))).thenReturn(true);
		when(filereader.readAllLine(any(String.class))).thenReturn(time);
		when(filereader.readAllLine(any(String.class))).thenReturn(appId);
		when(filereader.readAllLine(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"exVideo_time")).thenReturn(time);
		when(filereader.fileExist(any(String.class))).thenReturn(true);
		when(filereader.fileExist(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"traffic1.cap")).thenReturn(false);
		when(filereader.fileExist(Util.getCurrentRunningDir()+Util.FILE_SEPARATOR+"time")).thenReturn(false);
		Map<String, List<ScheduledAlarmInfo>> scheduledAlarms = new HashMap<String, List<ScheduledAlarmInfo>> ();
		List<AlarmAnalysisInfo> statistics = new ArrayList<AlarmAnalysisInfo>();
		
		AlarmAnalysisResult alarmResult = mock(AlarmAnalysisResult.class);
		when(alarmResult.getScheduledAlarms()).thenReturn(scheduledAlarms);
		when(alarmResult.getStatistics()).thenReturn(statistics);
		when(alarmanalysisinfoparser.parse(any(String.class), any(String.class), any(String.class), 
				any(double.class), any(double.class), any(Date.class))).thenReturn(alarmResult);

		Mockito.doAnswer(new Answer<Object>(){
			public Object answer(InvocationOnMock invocation){
				byte b = 3;
				short s = 1;
				
				InetAddress address1 = null;
				InetAddress address2 = null;
				try {
					address2 = InetAddress.getByName("78.46.84.177");
					address1 = InetAddress.getByName("78.46.84.171");
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Date date1 = new Date();
 
				IPPacket ippack01= mock(IPPacket.class);
				when(ippack01.getIPVersion()).thenReturn(b);
				when(ippack01.getFragmentOffset()).thenReturn(s);
				when(ippack01.getSourceIPAddress()).thenReturn(address1);
				when(ippack01.getDestinationIPAddress()).thenReturn(address2);
				when(ippack01.getTimeStamp()).thenReturn((double)date1.getTime());
				traceDataReaderImpl.packetArrived("com.google.android.youtube",ippack01);//pretend jpcap lib 
				
				return null;
			} 
		}).when(packetreader).readPacket(any(String.class), any(IPacketListener.class));
		AppInfo app = new AppInfo();
		Map<String, String> appMap = new HashMap<String,String>();
		appMap.put("flipboard.app","2.3.8");
		appMap.put("com.att.android.arodatacollector", "3.1.1.6");
		appMap.put("com.google.android.youtube", "4.0.23");
		app.setAppVersionMap(appMap);
		List<String> appInfos = new ArrayList<String>();
		appInfos.add("flipboard.app");
		appInfos.add("com.att.android.arodatacollector");
		appInfos.add("com.google.android.youtube");
		app.setAppInfos(appInfos);	
		when(appinforeader.readData(any(String.class))).thenReturn(app);
		DeviceDetail device = new DeviceDetail();
		when(devicedetailreader.readData(any(String.class))).thenReturn(device);
		NetworkTypeObject obj = new NetworkTypeObject();
		when(networktypereader
				.readData(any(String.class),any(double.class),any(double.class))).thenReturn(obj);
		TraceDirectoryResult result = traceDataReaderImpl.readTraceDirectory(Util.getCurrentRunningDir());
		assertSame(0,result.getAppIds().size());
	}
	
}
