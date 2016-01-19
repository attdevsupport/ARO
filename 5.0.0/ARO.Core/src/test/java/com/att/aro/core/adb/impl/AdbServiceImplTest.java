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
package com.att.aro.core.adb.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.att.aro.core.BaseTest;
import com.att.aro.core.commandline.IExternalProcessRunner;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.settings.IAROSettings;
import com.att.aro.core.settings.impl.AROSettingsImpl;
import com.att.aro.core.util.Util;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AndroidDebugBridge.class)
public class AdbServiceImplTest extends BaseTest {

	@InjectMocks
	AdbServiceImpl adbService;

	String fsep = Util.FILE_SEPARATOR;
	String adbPath = "mocked" + fsep + "path" + fsep + "adb";

	@Mock
	IFileManager fileManager;

	@Mock
	IAROSettings configFile;

	//@Mock
	AndroidDebugBridge androidDebugBridge;
	
	@Mock
	IExternalProcessRunner processRunner;

	@Before
	public void setup() {
		adbService = new AdbServiceImpl();

		fileManager = Mockito.mock(IFileManager.class);
		adbService.setFileManager(fileManager);

		configFile = Mockito.mock(AROSettingsImpl.class);
		adbService.setAROConfigFile(configFile);

		adbService.setLogger(new LoggerImpl("AdbServiceImpl"));
		MockitoAnnotations.initMocks(this);
		
		processRunner = Mockito.mock(IExternalProcessRunner.class);
		adbService.setExternalProcessRunner(processRunner);

		//		androidDebugBridge = Mockito.mock(AndroidDebugBridge.class);
	}

	private String getADBAttribute() {
		return configFile.getAttribute(
				AROSettingsImpl.AROConfigFileAttributes.adb.name());
	}

	@Test
	public void hasADBpath() {
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		boolean result = adbService.hasADBpath();
		assertTrue(result);

		//null
		Mockito.when(getADBAttribute()).thenReturn(null);
		result = adbService.hasADBpath();
		assertTrue(!result);

		//too short
		Mockito.when(getADBAttribute()).thenReturn("adb");
		assertTrue(!adbService.hasADBpath());
	}

	@Test
	public void getAdbPath() {
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		String path = adbService.getAdbPath();
		assertTrue(path == null);
	}

	@Test
	public void isAdbFileExist() throws Exception {
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
		boolean adbExists = adbService.isAdbFileExist();
		assertTrue(adbExists);

		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(false);
		adbExists = adbService.isAdbFileExist();
		assertTrue(!adbExists);
		//		Mockito.when(
	}

	/**
	 * pass: .getBridge() .createBridge(adbPath, false);
	 * 
	 * fail: .createBridge(adbPath, true);
	 * 
	 */
	@Test
	public void XXyensureADBServiceStarted() {
		
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
//		Mockito.when(settings.getProperty("adb")).thenReturn(adbPath);
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		androidDebugBridge = PowerMockito.mock(AndroidDebugBridge.class);

		PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
		
		// works
		PowerMockito.mockStatic(AndroidDebugBridge.class, new Answer<AndroidDebugBridge>() {

			int activated = 0;

			@Override
			public AndroidDebugBridge answer(InvocationOnMock invocation) throws Throwable {
				String method = invocation.getMethod().getName();
				Object[] args = invocation.getArguments();
				// System.out.println("invocation:" + invocation.getMethod().getName());

				if (method.matches("getBridge")) {
					if (activated == 1){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					}
					return null;
				} else if (method.matches("init")) {
					activated++;
					return null;
					
				} else if (method.matches("createBridge")) {
					if (args.length == 0){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
						return androidDebugBridge;
					} else {
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					}
				}
				return null;
			}
		});

		AndroidDebugBridge adb = adbService.ensureADBServiceStarted();
		assertTrue(adb != null);
	
	}

	/**
	 * scripted
	 *  <p>after init(...) getBridge and createBridge return androidDebugBridge object</p>
	 *  <p>before init(...) getBridge and createBridge return null</p>
	 *  <p>In reality getBridge and createBridge return an ADB but it is unstable and causes exceptions </p>
	 */
	@Test
	public void ensureADBServiceStarted() {
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
//		Mockito.when(settings.getProperty("adb")).thenReturn(adbPath);
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		androidDebugBridge = PowerMockito.mock(AndroidDebugBridge.class);

		PowerMockito.mockStatic(AndroidDebugBridge.class);
		PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);

		// works
		PowerMockito.mockStatic(AndroidDebugBridge.class, new Answer<AndroidDebugBridge>() {

			boolean activated = false;

			@Override
			public AndroidDebugBridge answer(InvocationOnMock invocation) throws Throwable {

				String method = invocation.getMethod().getName();
				// System.out.println("invocation:" + method);

				if (method.matches("getBridge")) {
					if (activated){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					}
					return null;
				} else if (method.matches("init")) {
					activated = true;
					return null;
					
				} else if (method.matches("createBridge")) {
					if (activated){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					} else {
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
						return androidDebugBridge;
					}
				}
				return null;
			}
		});

		AndroidDebugBridge adb = adbService.ensureADBServiceStarted();
		assertTrue(adb != null);
	}

	/**
	 * pass: .getBridge() .createBridge(adbPath, false);
	 * 
	 * fail: .createBridge(adbPath, true);
	 * 
	 */
	@Test
	public void _02_ensureADBServiceStarted() {
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
//		Mockito.when(settings.getProperty("adb")).thenReturn(adbPath);
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		androidDebugBridge = PowerMockito.mock(AndroidDebugBridge.class);

		PowerMockito.mockStatic(AndroidDebugBridge.class);
		PowerMockito.when(AndroidDebugBridge.getBridge()).thenReturn(null);
		PowerMockito.when(AndroidDebugBridge.createBridge(adbPath, false)).thenReturn(androidDebugBridge);

		AndroidDebugBridge adb = adbService.ensureADBServiceStarted();
		assertTrue(adb == null);
	}

	/**
	 * AndroidDebugBridge always returns null
	 */
	@Test
	public void Null_ensureADBServiceStarted() {
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
//		Mockito.when(settings.getProperty("adb")).thenReturn(adbPath);
		Mockito.when(getADBAttribute()).thenReturn(adbPath);

		PowerMockito.mockStatic(AndroidDebugBridge.class);
		PowerMockito.when(AndroidDebugBridge.getBridge()).thenReturn(null);
		AndroidDebugBridge adb = adbService.ensureADBServiceStarted();
		assertTrue(adb == null);
	}

	@Test
	public void runAdbCommandTest() throws IOException{
		Mockito.when(processRunner.runGetString(Mockito.anyString())).thenReturn("Test Command");
		boolean adbCmdFlg = adbService.runAdbCommand();
		assertTrue(adbCmdFlg);
		
		Mockito.when(processRunner.runGetString(Mockito.anyString())).thenReturn("Cannot run");
		boolean adbCmdFlg1 = adbService.runAdbCommand();
		assertTrue(!adbCmdFlg1);
		
		Mockito.when(processRunner.runGetString(Mockito.anyString())).thenReturn("");
		boolean adbCmdFlg2 = adbService.runAdbCommand();
		assertTrue(!adbCmdFlg2);
	}
	
	/**
	 * scripted
	 *  <p>after init(...) getBridge and createBridge return androidDebugBridge object</p>
	 *  <p>before init(...) getBridge and createBridge return null</p>
	 *  <p>In reality getBridge and createBridge return an ADB but it is unstable and causes exceptions </p>
	 */
	@Test
	public void getConnectedDevicesTest() {
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(true);
//		Mockito.when(settings.getProperty("adb")).thenReturn(adbPath);
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		androidDebugBridge = PowerMockito.mock(AndroidDebugBridge.class);

		IDevice aDevice1 = Mockito.mock(IDevice.class);
		IDevice[] devices = {aDevice1};
		
		PowerMockito.mockStatic(AndroidDebugBridge.class);
		PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
		PowerMockito.when(androidDebugBridge.getDevices()).thenReturn(devices);

		// works
		PowerMockito.mockStatic(AndroidDebugBridge.class, new Answer<AndroidDebugBridge>() {

			boolean activated = false;

			@Override
			public AndroidDebugBridge answer(InvocationOnMock invocation) throws Throwable {

				String method = invocation.getMethod().getName();
				System.out.println("invocation:" + method);

				if (method.matches("getBridge")) {
					if (activated){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					}
					return null;
				} else if (method.matches("init")) {
					activated = true;
					return null;
					
				} else if (method.matches("createBridge")) {
					if (activated){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					} else {
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
						return androidDebugBridge;
					}
				}
				return null;
			}
		});

		IDevice[] getDevices = null;
		try {
			getDevices = adbService.getConnectedDevices();
		} catch (Exception e) {
			
		}
		assertTrue(getDevices == devices);
	}

	/**
	 * scripted
	 *  <p>after init(...) getBridge and createBridge return androidDebugBridge object</p>
	 *  <p>before init(...) getBridge and createBridge return null</p>
	 *  <p>In reality getBridge and createBridge return an ADB but it is unstable and causes exceptions </p>
	 */
	@Test
	public void getConnectedDevicesTestFailsConnect() {
		Mockito.when(fileManager.fileExist(Mockito.anyString())).thenReturn(false);
//		Mockito.when(settings.getProperty("adb")).thenReturn(adbPath);
		Mockito.when(getADBAttribute()).thenReturn(adbPath);
		androidDebugBridge = PowerMockito.mock(AndroidDebugBridge.class);

		IDevice aDevice1 = Mockito.mock(IDevice.class);
		IDevice[] devices = {aDevice1};
		
		PowerMockito.mockStatic(AndroidDebugBridge.class);
		PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
		PowerMockito.when(androidDebugBridge.getDevices()).thenReturn(devices);

		// works
		PowerMockito.mockStatic(AndroidDebugBridge.class, new Answer<AndroidDebugBridge>() {

			boolean activated = false;

			@Override
			public AndroidDebugBridge answer(InvocationOnMock invocation) throws Throwable {

				String method = invocation.getMethod().getName();
				System.out.println("invocation:" + method);

				if (method.matches("getBridge")) {
					if (activated){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					}
					return null;
				} else if (method.matches("init")) {
					activated = true;
					return null;
					
				} else if (method.matches("createBridge")) {
					if (activated){
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(true);
						return androidDebugBridge;
					} else {
						PowerMockito.when(androidDebugBridge.hasInitialDeviceList()).thenReturn(false);
						return androidDebugBridge;
					}
				}
				return null;
			}
		});

		IDevice[] getDevices = null;
		try {
			getDevices = adbService.getConnectedDevices();
		} catch (Exception e) {
			
		}
		assertTrue(getDevices == null);
	}


}
