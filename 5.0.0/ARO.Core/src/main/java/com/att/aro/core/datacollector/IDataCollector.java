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
package com.att.aro.core.datacollector;

import java.util.Hashtable;

import com.att.aro.core.datacollector.pojo.StatusResult;

/**
 * Common interface for data collector to implement
 * 
 * @author Borey Sao February 17, 2015
 */
public interface IDataCollector {
	
	/**
	 * Friendly name of the collector. e.g: IOS Collector
	 * 
	 * @return name of the collector
	 */
	String getName();

	/**
	 * <pre>Major version of the collector. e.g: 5, which will be used with minor
	 * version.
	 * 
	 * NOTE: Hard-coded version number needs to match version in the APK project.
	 * </pre>
	 * @return Major version of the collector
	 */
	int getMajorVersion();

	/**
	 * <pre>Minor version of data collector. e.g: 1.0, which will be combined with major version.
	 * 
	 * NOTE: Hard-coded version number needs to match version in the APK project.
	 * </pre>
	 * @return Minor version of data collector
	 */
	String getMinorVersion();

	/**
	 * Type of collector: IOS, ROOTED ANDROID OR NON-ROOTED ANDROID etc.
	 * 
	 * @return an enumeration ie. IOS, ROOTED ANDROID OR NON-ROOTED ANDROID
	 */
	DataCollectorType getType();

//	/**
//	 * Start collector in background and returns result which indicates success
//	 * or error and detail data.
//	 * 
//	 * @param folderToSaveTrace
//	 *            directory to save trace to
//	 * @return a StatusResult to hold result and success or failure
//	 */
//	StatusResult startCollector(String folderToSaveTrace);

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
	StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, String password);

	/**
	 * Start collector in background and returns result which indicates success
	 * or error and detail data.
	 * 
	 * @param folderToSaveTrace
	 *            directory to save trace to
	 * @param isCapturingVideo
	 *            optional flag to capture video of device. default is false
	 * @param isLiveViewVideo
	 *            optional flag display video live while capturing
	 * @param androidId
	 *            optional id of device to capture. default is the connected
	 *            device.
	 * @param extraParams
	 *            optional data to pass to collectors. required by some
	 *            collectors.
	 * @return a StatusResult to hold result and success or failure
	 */
	StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, boolean isLiveViewVideo, String androidId, Hashtable<String, Object> extraParams, String password);

	/**
	 * Status of collector: running or not
	 * 
	 * @return true if running or false if not
	 */
	boolean isRunning();

	/**
	 * Status of the internal collector method or capability such as tcpdump or vpn
	 * 
	 * @param milliSecondsForTimeOut - The amount of time to determine a failure do to timeout
	 * @return true if running or false if not
	 */
	boolean isTrafficCaptureRunning(int milliSecondsForTimeOut);

	/**
	 * Stop collecting trace
	 * 
	 * @return a StatusResult to hold result and success or failure
	 */
	StatusResult stopCollector();

	/**
	 * Issue a force-stop to collector
	 */
	void haltCollectorInDevice();
	
	/**
	 * Add VideoImageSubscriber to receive video image frame from collector.
	 * This will allow subscriber to display video image to user or use it for
	 * any other purpose.
	 * 
	 * @param subscriber - VideoImageSubscriber to receive video image
	 */
	void addVideoImageSubscriber(IVideoImageSubscriber subscriber);

	/**
	 * Add user to receive device events.
	 * <pre>
	 * e.g: when device loses connection or is reconnected during a trace
	 * </pre>
	 * 
	 * @param subscriber - user to receive device events
	 */
	void addDeviceStatusSubscriber(IDeviceStatus subscriber);

	/**
	 * 
	 * @return logcat dump from device
	 */
	String[] getLog();


	/**
	 * send shutdown command to phone
	 */
	void timeOutShutdown();


	/**
	 * Retrieve the required password.
	 * 
	 * @return password
	 */
	String getPassword();

	/**
	 * Store the password
	 * 
	 * @param requestPassword
	 * @return true if password is valid, otherwise return false
	 */
	boolean setPassword(String requestPassword);

}
