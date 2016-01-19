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

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.att.aro.core.ILogger;
import com.att.aro.core.adb.IAdbService;
import com.att.aro.core.commandline.IExternalProcessRunner;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.settings.IAROSettings;
import com.att.aro.core.settings.impl.AROSettingsImpl;
import com.att.aro.core.util.Util;

/** 
 * Provides access to AndroidDebugBridge (ADB)
 * 
 * Designed to work with ddmlib r24.0.1 current release as of Jan 2015
 * 
 * Current dependencies are:
 *	   libs/ddmlib-24.0.1.jar
 * 	   libs/guava-17.0.jar
 *	   libs/kxml2-2.3.0.jar
 *	   libs/common-24.0.1.jar
 *
 * <p>see: http://mvnrepository.com/artifact/com.android.tools.ddms/ddmlib</p>
 * 
 */
public class AdbServiceImpl implements IAdbService {

	@InjectLogger
	private ILogger logger;
	private IExternalProcessRunner extrunner;
	private IFileManager fileManager;
	private IAROSettings configFile;
	String adbPath = null;
	
	@Autowired
	public void setExternalProcessRunner(IExternalProcessRunner runner) {
		this.extrunner = runner;
	}

	@Autowired
	public void setFileManager(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	@Autowired
	public void setAROConfigFile(IAROSettings configFile) {
		this.configFile = configFile;
	}

	private String getADBAttributeName() {
		return AROSettingsImpl.AROConfigFileAttributes.adb.name();
	}
	private String getAROConfigFileLocation() {
		return configFile.getAttribute(getADBAttributeName());
	}

	/**
	 * check if ADB location was set in settings.properties
	 * 
	 * @return
	 */
	@Override
	public boolean hasADBpath() {
		boolean result = false;
		String adbPath = getAROConfigFileLocation();
		if (adbPath != null && adbPath.length() > 3) {
			result = true;
		}
		return result;
	}

	/**
	 * Confirms and returns adb path
	 * @return the adb file path
	 */
	@Override
	public String getAdbPath() {
		return verifyAdbPath(getAROConfigFileLocation());
	}

	/**
	 * <pre>Confirm adbPath, attempt repair from environmental variables. 
	 *  ANDROID_HOME - path to the android sdk
	 *  ANDROID_ADB - path directly to the executable adb or adb.exe
	 * @param adbPath path where adb should be found
	 * @return path if adb is found, false if adb cannot be located
	 */
	private String verifyAdbPath(String adbPath) {

		if (adbPath == null || !fileManager.fileExist(adbPath)) {

			String[] paths = { System.getenv("ANDROID_ADB"), System.getProperty("ANDROID_ADB")
					, System.getenv("ANDROID_HOME") + Util.FILE_SEPARATOR + "platform-tools" + Util.FILE_SEPARATOR + "adb" 
					, System.getenv("ANDROID_HOME") + Util.FILE_SEPARATOR + "platform-tools" + Util.FILE_SEPARATOR + "adb.exe" 
					};

			for (String path : paths) {
				if (path != null && fileManager.fileExist(path)) {
					logger.debug(path);
					configFile.setAndSaveAttribute(getADBAttributeName(), path);
					return path;
				}
			}

			logger.error("failed to repair ADB path, no usefull environmental variables (see: ANDROID_ADB, ANDROID_HOME)");
			return null;

		}

		return adbPath;

	}
	
	/**
	 * adb path might have been set but the file does not exist or has been
	 * deleted
	 * 
	 * @return true if exists
	 */
	@Override
	public boolean isAdbFileExist() {
		return fileManager.fileExist(getAROConfigFileLocation());
	}

	/**
	 * will retrieve ADB service if active
	 * 
	 * @return true if launched successfully
	 */
	private boolean checkAdb(AndroidDebugBridge adb) {

		boolean result = false;

		// watch for device
		int attempt = 10;
		while (attempt-- > 0 && !adb.hasInitialDeviceList()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		if (adb.hasInitialDeviceList()) {
			result = true;
		}
		return result;
	}

	/**
	 * Perform an AndroidDebugBridge.init if needed.
	 *
	 * @param adbPath path to location of adb
	 * @return adb - AndroidDebugBridge
	 */
	@Override
	public AndroidDebugBridge initCreateBridge(String adbPath) {

		AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();

		if (bridge == null) {
			AndroidDebugBridge.init(false);
			bridge = AndroidDebugBridge.createBridge(adbPath, false);
		}
		return bridge;
	}

	/*
	 * Note: The following code is to be avoided, it only instantiates
	 * AndroidDebugBridge It does not make a connection to the process 'adb' nor
	 * does it launch it if not running.
	 * 
	 * adb = AndroidDebugBridge.createBridge();
	 */

	/**
	 * will start ADB service if not started, given that ADB path is set.
	 * 
	 * @return adb object if launched successfully
	 */
	@Override
	public AndroidDebugBridge ensureADBServiceStarted() {
		//try to connect to a running ADB service first.
		AndroidDebugBridge adb = null;

		adb = AndroidDebugBridge.getBridge();
		if (adb != null && adb.isConnected()) {
			return adb;
		}

		String adbPath = getAdbPath();
		if (adbPath != null && adbPath.length() > 3) {

			adb = initCreateBridge(adbPath);

			if (adb != null) {
				int attempt = 1;
				while (attempt-- > 0 && !checkAdb(adb)) {
					adb = AndroidDebugBridge.createBridge(adbPath, true);
				}
				if (adb == null || !adb.hasInitialDeviceList()) {
					logger.error("ADB bridge not working or failed to connect.");
					adb = null;
				}
			} else {
				logger.info("Failed to create ADB Bridge, unable to connect.");
			}

		} else {
			logger.info("No ADB path found, failed to create ADB Bridge.");
		}
		return adb;
	}

	boolean runAdbCommand() {
		//assume that user has adb environment set, try running command line: adb devices
		String lines = "";
		try {
			lines = extrunner.runGetString("adb devices");
			//logger.debug("result of command 'adb devices':"+ lines);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		if (lines.contains("command not found") 
				|| lines.contains("not recognized") 
				|| lines.contains("No such file") 
				|| lines.contains("Cannot run")) {
			return false;
		}
		if (lines.length() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Find and return an array of (IDevice) Android devices and emulators
	 * 
	 * @return array of IDevice
	 * @throws Exception if AndroidDebugBridge is invalid or fails to connect
	 */
	@Override
	public IDevice[] getConnectedDevices() throws Exception {
		AndroidDebugBridge adb = ensureADBServiceStarted();
		if (adb == null) {
			logger.debug("failed to connect to existing bridge, now trying running adb from environment");
			throw new Exception("AndroidDebugBridge failed to start");
		}

		int waitcount = 1;
		while (waitcount <= 20 && !adb.hasInitialDeviceList()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
			waitcount++;
		}

		return adb.getDevices();
	}

	@Autowired
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}
}
