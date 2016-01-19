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
package com.att.aro.core.datacollector.impl;

import java.util.Hashtable;

import com.att.aro.core.datacollector.DataCollectorType;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDeviceStatus;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.datacollector.pojo.StatusResult;

public class DefaultDataCollectorImpl implements IDataCollector {

	/**
	 * Friendly name of the collector. e.g: IOS Collector
	 * 
	 * @return name of the collector
	 */
	@Override
	public String getName() {
		return "Default";
	}

	/**
	 * Major version of the collector. e.g: 5, which will be used with minor
	 * version
	 * 
	 * @return
	 */
	@Override
	public int getMajorVersion() {
		return 0;
	}

	/**
	 * Minor version of data collector. e.g: 1.0, which will be combined with
	 * major version
	 * 
	 * @return
	 */
	@Override
	public String getMinorVersion() {
		return "0";
	}

	/**
	 * Type of collector: IOS, ROOTED ANDROID OR NON-ROOTED ANDROID etc.
	 * 
	 * @return
	 */
	@Override
	public DataCollectorType getType() {
		return DataCollectorType.DEFAULT;
	}

	/**
	 * Stop collecting trace
	 * 
	 * @return a StatusResult to hold result and success or failure
	 */
	@Override
	public StatusResult stopCollector() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Add VideoImageSubscriber to receive video image frame from collector.
	 * This will allow subscriber to display video image to user or use it for
	 * any other purpose.
	 * 
	 * @param subscriber
	 */
	@Override
	public void addVideoImageSubscriber(IVideoImageSubscriber subscriber) {
		// TODO Auto-generated method stub

	}

	/**
	 * Add user to receive device events.
	 * <ul>
	 * e.g: when device loses connection or is reconnected during a trace
	 * </ul>
	 * 
	 * @param subscriber
	 */
	@Override
	public void addDeviceStatusSubscriber(IDeviceStatus subscriber) {
		// TODO Auto-generated method stub

	}

	/**
	 * Start collector in background and returns result which indicates success
	 * or error and detail data.
	 * 
	 * @param folderToSaveTrace
	 *            directory to save trace to
	 * @param isCapturingVideo
	 *            optional flag to capture video of device. default is false
	 * @param deviceId
	 *            optional id of device to capture. default is the connected
	 *            device.
	 * @param extraParams
	 *            optional data to pass to collectors. required by some
	 *            collectors.
	 * @return a StatusResult to hold result and success or failure
	 */
	@Override
	public StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, boolean isLiveViewVideo, String deviceId, Hashtable<String, Object> extraParams, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Status of collector: running or not
	 * 
	 * @return true if running or false if not
	 */
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

//	/**
//	 * Start collector in background and returns result which indicates success
//	 * or error and detail data.
//	 * 
//	 * @param folderToSaveTrace
//	 *            directory to save trace to
//	 * @return a StatusResult to hold result and success or failure
//	 */
//	@Override
//	public StatusResult startCollector(String folderToSaveTrace) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
	/**
	 * Start collector in background and returns result which indicates success
	 * or error and detail data.
	 * 
	 * @param folderToSaveTrace
	 *            directory to save trace to
	 * @param isCapturingVideo
	 *            optional flag to capture video of device. default is false
	 * @return a StatusResult to hold result and success or failure
	 */
	@Override
	public StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTrafficCaptureRunning(int milliSecondsForTimeOut) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 
	 * @return logcat dump from device
	 */
	@Override
	public String[] getLog() {
		return new String[]{};
	}

	/**
	 * force-stop rooted collector
	 */
	@Override
	public void haltCollectorInDevice() {
		// default collector does not use this
	}

	/**
	 * message the app to exit upon timeout
	 */
	@Override
	public void timeOutShutdown() {
		// default collector does not use this
	}

	/**
	 * Does not require a password
	 */
	@Override
	public String getPassword() {
		return null;
	}

	/**
	 * Does not require a password
	 * @return false, always false, password not used here
	 */
	@Override
	public boolean setPassword(String requestPassword) {
		return false;
	}


}
