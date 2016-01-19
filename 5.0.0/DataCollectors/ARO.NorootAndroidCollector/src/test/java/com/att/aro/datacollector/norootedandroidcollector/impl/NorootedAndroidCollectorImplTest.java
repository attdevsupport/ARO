/*
 *  Copyright 2015 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.datacollector.norootedandroidcollector.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.SyncService.ISyncProgressMonitor;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.ILogger;
import com.att.aro.core.adb.IAdbService;
import com.att.aro.core.android.IAndroid;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;
import com.att.aro.core.video.IVideoCapture;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class NorootedAndroidCollectorImplTest{
	
	@Mock
	ILogger logMock;
	@Mock
	IReadWriteFileExtractor extractor;
	@Mock
	IThreadExecutor threadExecutor;
	@Mock
	IVideoCapture videoCapture;
	@Mock
	IDevice device;
	@Mock
	IAdbService adbService;
	@Mock
	IFileManager fileManager;
	@Mock
	IAndroid android;
	
	String[] noVpnCon = {"some ip "};
	String[] vpnActive = {"some ip ", "tun0: ip 10. some"};
	
	NorootedAndroidCollectorImpl nonRootedAndroidCollector;
	
	
	@Before
	public void setup(){
		
	//	logMock = (ILogger)Mockito.mock(LoggerImpl.class);
/*		extractor = Mockito.mock(IReadWriteFileExtractor.class);
		threadExecutor = Mockito.mock(IThreadExecutor.class);
		videoCapture = Mockito.mock(IVideoCapture.class);
		device = Mockito.mock(IDevice.class);
		adbService = Mockito.mock(IAdbService.class);
		fileManager = Mockito.mock(IFileManager.class);
		android = Mockito.mock(IAndroid.class);*/
		
		
		nonRootedAndroidCollector = new NorootedAndroidCollectorImpl();
		
		MockitoAnnotations.initMocks(this);
		
		nonRootedAndroidCollector.setLog(logMock);
		nonRootedAndroidCollector.setFileManager(fileManager);
		nonRootedAndroidCollector.setAdbService(adbService);
		nonRootedAndroidCollector.setAndroid(android);
		nonRootedAndroidCollector.setFileExtactor(extractor);
		nonRootedAndroidCollector.setThreadExecutor(threadExecutor);
		nonRootedAndroidCollector.setVideoCapture(videoCapture);
		nonRootedAndroidCollector.setDevice(device);
		
		Mockito.doNothing().when(logMock).info(Mockito.anyString());
		Mockito.doNothing().when(logMock).debug(Mockito.anyString());
	}
	
	@Test
	public void startCollectorTest(){
	//	Hashtable extranalParams = Mockito.mock(Hashtable.class); 
		Hashtable<String, Object> extranalParams = new Hashtable<String, Object>(); 
		

		Mockito.when(fileManager.directoryExistAndNotEmpty(Mockito.anyString())).thenReturn(true);
		
		StatusResult sResult= null;
		
		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "testDeice", extranalParams, null);
		
		assertEquals(402, sResult.getError().getCode());
		
		Mockito.when(fileManager.directoryExistAndNotEmpty(Mockito.anyString())).thenReturn(false);
		Mockito.doNothing().when(fileManager).mkDir(Mockito.anyString());
		
		Mockito.when(fileManager.directoryExist(Mockito.anyString())).thenReturn(false);
		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "testDeice", extranalParams, null);
		assertEquals(406, sResult.getError().getCode());
		
		Mockito.when(fileManager.directoryExist(Mockito.anyString())).thenReturn(true);
		IDevice aDevice1 = Mockito.mock(IDevice.class);
		Mockito.when(aDevice1.getSerialNumber()).thenReturn("device1");
		IDevice[] devices = {aDevice1};
		IDevice[] returnDevices = {};
		
		try {
			Mockito.when(adbService.getConnectedDevices()).thenReturn(returnDevices);
		} catch (Exception exp) {
		
			exp.printStackTrace();
		}
		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "testDeice", extranalParams, null);
		assertEquals(403, sResult.getError().getCode());
		
		try {
			Mockito.when(adbService.getConnectedDevices()).thenReturn(devices);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "testDeice", extranalParams, null);
		assertEquals(404, sResult.getError().getCode());
		
		Mockito.when(android.removeEmulatorData(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(true);
		Mockito.when(android.makeDirectory(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(true);
		String[] str1 = {};
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(str1);
//		Mockito.doNothing().when(android).getShellReturn(Mockito.any(IDevice.class), Mockito.anyString());
	
		try {
			//Mockito.when(aDevice1.installPackage(Mockito.anyString(), Mockito.anyBoolean()).;
			Mockito.when(aDevice1.installPackage(Mockito.anyString(), Mockito.anyBoolean())).thenReturn("some thing");
		} catch (InstallException iExp) {
			iExp.printStackTrace();
		}
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(android.runApkInDevice(Mockito.any(IDevice.class),Mockito.anyString())).thenReturn(false);
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "device1", extranalParams, null);
		
		assertEquals(405, sResult.getError().getCode());
		
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(false);
		Mockito.when(extractor.extractFiles(Mockito.anyString(), Mockito.anyString(), Mockito.any(ClassLoader.class))).thenReturn(false);
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "device1", extranalParams, null);
		assertEquals(401, sResult.getError().getCode());
		
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(noVpnCon);
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(android.runApkInDevice(Mockito.any(IDevice.class),Mockito.anyString())).thenReturn(true);
	//	sResult = nonRootedAndroidCollector.startCollector(true, "test", false, "device1", extranalParams, null);
	//	assertEquals(407, sResult.getError().getCode());
		
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(noVpnCon).thenReturn(vpnActive);
		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, false, "device1", extranalParams, null);
		
		assertTrue(sResult.isSuccess());
		

		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(vpnActive).thenReturn(noVpnCon);		
		nonRootedAndroidCollector.stopRunning();
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(noVpnCon).thenReturn(vpnActive);		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", false, null);
		assertTrue(sResult.isSuccess());
		
		nonRootedAndroidCollector.stopRunning();
		try {
			Mockito.doNothing().when(videoCapture).init(Mockito.any(IDevice.class), Mockito.anyString());
			Mockito.doNothing().when(videoCapture).addSubscriber(Mockito.any(IVideoImageSubscriber.class));
			Mockito.doNothing().when(threadExecutor).execute(Mockito.any(IVideoCapture.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(noVpnCon).thenReturn(vpnActive);		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", true, false, "device1", extranalParams, null);
		
		assertTrue(sResult.isSuccess());
		
		nonRootedAndroidCollector.stopRunning();
		
		try {
			Mockito.when(adbService.getConnectedDevices()).thenThrow(new Exception("AndroidDebugBridge failed to start"));
		} catch (Exception exp) {
		
			exp.printStackTrace();
		}
		
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(noVpnCon).thenReturn(vpnActive);		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", true, false, "device1", extranalParams, null);
		assertEquals(400, sResult.getError().getCode());
		
	}


	@Test
	public void stopCollector(){
		String[] str1 = {};
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(str1);
		
		String[] str2 = {"some ip "};
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(str2);
		
		try {
			Mockito.doNothing().when(fileManager).saveFile(Mockito.any(InputStream.class), Mockito.anyString());
		} catch (IOException IOExp) {
		
			IOExp.printStackTrace();
		}
		
		Mockito.doNothing().when(videoCapture).stopRecording();
		Mockito.when(android.removeEmulatorData(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(true);
		
		StatusResult sResult= null;
		
		SyncService sycService = null;
		
		try {
			Mockito.when(device.getSyncService()).thenReturn(sycService);
		} catch (TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sResult = nonRootedAndroidCollector.stopCollector();
		assertEquals(411, sResult.getError().getCode());
		
		
		sycService = Mockito.mock(SyncService.class);
	
			try {
				Mockito.when(device.getSyncService()).thenReturn(sycService);
			} catch (TimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (AdbCommandRejectedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			try {
				Mockito.doNothing().when(sycService).pullFile(Mockito.anyString(), Mockito.anyString(), Mockito.any(ISyncProgressMonitor.class));
			} catch (SyncException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	
		sResult = nonRootedAndroidCollector.stopCollector();
		
		assertTrue(sResult.isSuccess());

		Hashtable<String, Object> extranalParams = new Hashtable<String, Object>(); 
		
		Mockito.when(fileManager.directoryExistAndNotEmpty(Mockito.anyString())).thenReturn(false);
		Mockito.doNothing().when(fileManager).mkDir(Mockito.anyString());

		Mockito.when(fileManager.directoryExist(Mockito.anyString())).thenReturn(true);
		IDevice aDevice1 = Mockito.mock(IDevice.class);
		Mockito.when(aDevice1.getSerialNumber()).thenReturn("device1");
		IDevice[] devices = {aDevice1};

	try {
			Mockito.when(adbService.getConnectedDevices()).thenReturn(devices);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(android.runApkInDevice(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(true);

		String[] str3 = {"some ip ", "tun0: ip 10. some"};
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(str3);
		
		try {
			Mockito.doNothing().when(videoCapture).init(Mockito.any(IDevice.class), Mockito.anyString());
			Mockito.doNothing().when(videoCapture).addSubscriber(Mockito.any(IVideoImageSubscriber.class));
			Mockito.doNothing().when(threadExecutor).execute(Mockito.any(IVideoCapture.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		nonRootedAndroidCollector.stopRunning();
		Mockito.when(android.getShellReturn(Mockito.any(IDevice.class), Mockito.anyString())).thenReturn(noVpnCon).thenReturn(vpnActive);		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", true, false, "device1", extranalParams, null);
		
		assertTrue(sResult.isSuccess());
		
		Date aDate = new Date();
		Mockito.when(videoCapture.getVideoStartTime()).thenReturn(aDate);
		
		sResult = nonRootedAndroidCollector.stopCollector();
		
		assertEquals(411, sResult.getError().getCode());
		

		
	}
/*	
	@Test
	public void startCollectorTest1(){

		StatusResult sResult= null;
		Hashtable<String, Object> extranalParams = new Hashtable<String, Object>(); 

		IDevice aDevice1 = Mockito.mock(IDevice.class);
		Mockito.when(aDevice1.getSerialNumber()).thenReturn("device1");
		IDevice[] devices = {aDevice1};

		
		try {
			Mockito.doNothing().when(videoCapture).init(Mockito.any(IDevice.class), Mockito.anyString());
			Mockito.doNothing().when(videoCapture).addSubscriber(Mockito.any(IVideoImageSubscriber.class));
			Mockito.doNothing().when(threadExecutor).execute(Mockito.any(IVideoCapture.class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sResult = nonRootedAndroidCollector.startCollector(true, "test", true, "device1", extranalParams, null);
		
		assertTrue(sResult.isSuccess());


		
	}
*/

}
