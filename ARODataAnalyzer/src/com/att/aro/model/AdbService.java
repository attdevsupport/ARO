/*
 *  Copyright 2012 AT&T
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

package com.att.aro.model;

import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.att.aro.interfaces.Settings;

public class AdbService {
	private static final Logger logger = Logger.getLogger(AdbService.class.getName());
	Settings settings;
	public AdbService(){
		settings = new SettingsImpl();
	}
	public AdbService(Settings settings){
		this.settings = settings;
	}
	/**
	 * check if ADB location was set.
	 * @return
	 */
	public boolean hasADBpath(){
		boolean yes = false;
		String adb = settings.getProperty("adb");
		if(adb != null && adb.length() > 3){
			yes = true;
		}
		return yes;
	}
	public String getAdbPath(){
		return settings.getProperty("adb");
	}
	/**
	 * will start ADB service if not started, given that ADB path is set.
	 */
	public void ensureADBServiceStarted(){
		String adb = getAdbPath();
		if(adb != null && adb.length() > 3){
			logger.info("Creating instance of AndroidDebugBridge...");
			AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adb, false);
			logger.info("Successfully created ADB.");
		}else{
			logger.info("No ADB path found, failed to create ADB Bridge.");
		}
	}
	
}//end
