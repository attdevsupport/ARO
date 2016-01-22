/*
 * Copyright 2016 AT&T
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

package com.att.aro.datacollector.norootedandroidcollector.pojo;

import com.att.aro.core.pojo.ErrorCode;

/**
 * error code for none-rooted Android Collector start from 400
 * 
 * @author Borey Sao Date: March 17, 2015
 */
public final class ErrorCodeRegistry {
	private ErrorCodeRegistry() {
	}

	public static ErrorCode getAndroidBridgeFailedToStart() {
		ErrorCode error = new ErrorCode();
		error.setCode(400);
		error.setName("Android Debug Bridge failed to start");
		error.setDescription("ARO Collector tried to start Android Debug Bridge service. The service was not started successfully.");
		return error;
	}

	public static ErrorCode getFailToInstallAPK() {
		ErrorCode err = new ErrorCode();
		err.setCode(401);
		err.setName("Failed to install Android App on device");
		err.setDescription("ARO tried to install ARO Collector on device and failed.");
		return err;
	}

	public static ErrorCode getTraceDirExist() {
		ErrorCode err = new ErrorCode();
		err.setCode(402);
		err.setName("Found existing trace directory that is not empty");
		err.setDescription("ARO found an existing directory that contains files and did not want to override it. Some files may be hidden.");
		return err;
	}

	public static ErrorCode getNoDeviceConnected() {
		ErrorCode err = new ErrorCode();
		err.setCode(403);
		err.setName("No Android device found.");
		err.setDescription("ARO cannot find any Android deviced plugged into the machine.");
		return err;
	}

	/**
	 * no device id matched the device list
	 * 
	 * @return
	 */
	public static ErrorCode getDeviceIdNotFound() {
		ErrorCode err = new ErrorCode();
		err.setCode(404);
		err.setName("Android device Id or serial number not found.");
		err.setDescription("ARO cannot find any Android deviced plugged into the machine that matched the device Id or serial number you specified.");
		return err;
	}

	public static ErrorCode getFaildedToRunVpnApk() {
		ErrorCode err = new ErrorCode();
		err.setCode(405);
		err.setName("Failed to run VPN APK");
		err.setDescription("ARO failed to run Data Collector in device");
		return err;
	}

	/**
	 * failed to create local directory in user's machine to save trace data to.
	 * 
	 * @return
	 */
	public static ErrorCode getFailedToCreateLocalTraceDirectory() {
		ErrorCode err = new ErrorCode();
		err.setCode(406);
		err.setName("Failed to create local trace directory");
		err.setDescription("ARO tried to create local directory for saving trace data, but failed.");
		return err;
	}

	public static ErrorCode getTimeoutVpnActivation(int seconds) {
		ErrorCode err = new ErrorCode();
		err.setCode(407);
		err.setName("VPN activation timeout");
		err.setDescription("ARO waited " + seconds + " seconds for VPN service to activate.");
		return err;
	}

	public static ErrorCode getFailSyncService() {
		ErrorCode err = new ErrorCode();
		err.setCode(411);
		err.setName("Failed to connect to device SyncService");
		err.setDescription("ARO failed to get SyncService() from IDevice which is used for data transfer");
		return err;
	}

	public static ErrorCode getCollectorAlreadyRunning() {
		ErrorCode err = new ErrorCode();
		err.setCode(206);
		err.setName("ARO VPN Android collector is already running");
		err.setDescription("There is already an ARO collector running on this device. Stop it first, manually, before starting a new trace.");
		return err;
	}
	
	public static ErrorCode getLostConnection(String message) {
		ErrorCode err = new ErrorCode();
		err.setCode(216);
		err.setName("Lost connection");
		err.setDescription("lost AndroidDebugBridge connection" + message);
		return err;

	}

}
