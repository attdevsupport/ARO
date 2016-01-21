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
package com.att.aro.datacollector.rootedandroidcollector.pojo;

import com.att.aro.core.pojo.ErrorCode;

/**
 * error code for rooted Android Collector start from 200
 * 
 */
public final class ErrorCodeRegistry {
	private ErrorCodeRegistry() {
	}

	public static ErrorCode getAndroidBridgeFailedToStart() {
		ErrorCode error = new ErrorCode();
		error.setCode(200);
		error.setName("Android Debug Bridge failed to start");
		error.setDescription("ARO Collector tried to start Android Debug Bridge service. The service was not started successfully.");
		return error;
	}

	public static ErrorCode getFailToInstallAPK() {
		ErrorCode err = new ErrorCode();
		err.setCode(201);
		err.setName("Failed to install Android App on device");
		err.setDescription("ARO tried to install ARO Collector on device and failed. Try to manually install it, then try again.");
		return err;
	}

	public static ErrorCode getTraceDirExist() {
		ErrorCode err = new ErrorCode();
		err.setCode(202);
		err.setName("Found existing trace directory that is not empty");
		err.setDescription("ARO found an existing directory that contains files and did not want to override it. Some files may be hidden.");
		return err;
	}

	public static ErrorCode getNoDeviceConnected() {
		ErrorCode err = new ErrorCode();
		err.setCode(203);
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
		err.setCode(204);
		err.setName("Android device Id or serial number not found.");
		err.setDescription("ARO cannot find any Android deviced plugged into the machine that matched the device Id or serial number you specified.");
		return err;
	}

	/**
	 * device does not have any space in sdcard to collect trace
	 * 
	 * @return
	 */
	public static ErrorCode getDeviceHasNoSpace() {
		ErrorCode err = new ErrorCode();
		err.setCode(205);
		err.setName("Device has no space");
		err.setDescription("Device does not have any space for saving trace");
		return err;
	}

	public static ErrorCode getCollectorAlreadyRunning() {
		ErrorCode err = new ErrorCode();
		err.setCode(206);
		err.setName("ARO rooted Android collector already running");
		err.setDescription("There is already an ARO collector running on this device. Stop it first before running another one.");
		return err;
	}

	/**
	 * failed to create local directory in user's machine to save trace data to.
	 * 
	 * @return
	 */
	public static ErrorCode getFailedToCreateLocalTraceDirectory() {
		ErrorCode err = new ErrorCode();
		err.setCode(207);
		err.setName("Failed to create local trace directory");
		err.setDescription("ARO tried to create local directory for saving trace data, but failed.");
		return err;
	}

	public static ErrorCode getFailToExtractTcpdump() {
		ErrorCode err = new ErrorCode();
		err.setCode(208);
		err.setName("Failed to extract tcpdump");
		err.setDescription("ARO failed to extract tcpdump from resource bundle and save it to local machine.");
		return err;
	}

	public static ErrorCode getFailToInstallTcpdump() {
		ErrorCode err = new ErrorCode();
		err.setCode(209);
		err.setName("Failed to install tcpdump");
		err.setDescription("ARO failed to install tcpdump on Emulator. Tcpdump is required to capture packet");
		return err;
	}

	public static ErrorCode getFailToRunApk() {
		ErrorCode err = new ErrorCode();
		err.setCode(210);
		err.setName("Failed to run ARO Data Collector");
		err.setDescription("ARO Analyzer tried to run Data Collector on device and received error from device.");
		return err;
	}

	public static ErrorCode getFailSyncService() {
		ErrorCode err = new ErrorCode();
		err.setCode(211);
		err.setName("Failed to connect to device SyncService");
		err.setDescription("ARO failed to get SyncService() from IDevice which is used for data transfer");
		return err;
	}

	public static ErrorCode getTcpdumpPermissionIssue() {
		ErrorCode err = new ErrorCode();
		err.setCode(212);
		err.setName("Failed to set execute permission on Tcpdump on device");
		err.setDescription("Error occured while trying to set permission on tcpdump file on device. Execute permission is required to run it.");
		return err;
	}

	public static ErrorCode getRootedStatus() {
		ErrorCode err = new ErrorCode();
		err.setCode(213);
		err.setName("Device not rooted");
		err.setDescription("ARO detected that device is not rooted. A rooted device is required to run this collector");
		return err;
	}

	public static ErrorCode getCollectorTimeout() {
		ErrorCode err = new ErrorCode();
		err.setCode(214);
		err.setName("Collector Failed to start traffic capture");
		err.setDescription("ARO detected that the collector failed to start capture in time");
		return err;
	}

	public static ErrorCode getProblemAccessingDevice(String message) {
		ErrorCode err = new ErrorCode();
		err.setCode(215);
		err.setName("Problem accessing device");
		err.setDescription("ARO failed to access device :"+message);
		return err;

	}

}
