/**
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
package com.att.aro.core.android;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.IDevice.DeviceState;

/**
 * IAndroid is an interface to provide information about an Android/Emulator and the controls to perform a trace.
 *
 */
public interface IAndroid {

	boolean isEmulator(IDevice device);

	boolean pushFile(IDevice emulator, String local,
			String remote);
	/**
	 * get the device status from ddmlibs library 
	 * status: BOOTLOADER,OFFLINE, ONLINE, RECOVERY,UNAUTHORIZED;
	 * @param device
	 * @return DeviceState
	 */
	DeviceState getState(IDevice device);

	//check sd card
	boolean isSDCardAttached(IDevice device);

	//checkemulatorSDCard
	boolean isSDCardEnoughSpace(IDevice device, long kbs);

	/**Pull files in emulator 
	 * pullFile(String remoteFilepath, String localFilename,
	 * ISyncProgressMonitor monitor) throws TimeoutException, IOException, SyncException
	 * @param device
	 * @param remoteFilepath the full path to the remote file
	 * @param localFilename The local destination.
	 * @return
	 */
	boolean pullTraceFilesFromEmulator(IDevice device,
			String remoteFilepath, String localFilename);

	/**
	 * Pull all trace files via DDMLIB
	 * [cpu, appid, appname, time, processed_events,
	 * active_process, battery_events, bluetooth_events,
	 * camera_events, device_details, device_info,
	 * gps_events, network_details, prop, radio_events,
	 * screen_events, screen_rotations, time,
	 * user_input_log_events, alarm_info_start,
	 * alarm_info_end, batteryinfo_dump, dmesg, video_time,
	 * wifi_events, traffic.cap, keys.ssl]
	 * 
	 * @param device
	 * @param remoteFilepath
	 * @param localFilename
	 * @return
	 */
	boolean pullTraceFilesFromDevice(IDevice device,
			String remoteFilepath, String localFilename);

	/**
	 * create ARO trace directory inside Android device or Emulator
	 * @param device
	 * @param traceName
	 * @return
	 */
	boolean makeAROTraceDirectory(IDevice device,
			String traceName);

	/**
	 * create a directory in Android device sdcard
	 * @param device
	 * @param traceName
	 * @return
	 */
	boolean makeDirectory(IDevice device, String dirpath);

	/**
	 * 
	 * @param device
	 * @param property
	 * @return
	 */
	String getProperty(IDevice device, String property);

	/**
	 * @param device
	 * @return
	 */
	SyncService getSyncService(IDevice device);

	/**
	 * startTcpDump builds a command string based on potential SELinux environment
	 * 
	 * @param device 
	 * @param seLinuxEnforced true if need to launch in a SELinux enforced environment 
	 * @param traceFolderName The name of the folder in which the ARO Data Collector trace files should be stored.
	 * @return
	 */
	boolean startTcpDump(IDevice device, boolean seLinuxEnforced, String traceFolderName);

	/**
	 * Used to detect if a trace is in progress.
	 * 
	 * @return true indicates a trace is in progress, false indicates no trace is active
	 */
	boolean isTraceRunning();
	
	/**
	 * Checks for tcpdump process via the linux command ps
	 * @param device - The Android Device/Emulator
	 * @return true if process is detected
	 */
	boolean checkTcpDumpRunning(IDevice device);

	/**
	 * Sends a command to tcpdump over a socket to stop
	 * @param device - The Android Device/Emulator
	 * @return true if no errors
	 */
	boolean stopTcpDump(IDevice device);
	
	/**
	 * Removes recently collected trace file and directory.
	 */
	boolean removeEmulatorData(IDevice device,
			String deviceTracePath);
	/**
	 * set exute permission for remote file on device/emulator
	 * @param device
	 * @param remotePath
	 * @return
	 */
	boolean setExecutePermission(IDevice device, String remotePath);
	
	/**
	 * check if a package exist in the device by issuing command: adb shell pm list package
	 * @param device
	 * @param fullpackageName
	 * @return
	 */
	boolean checkPackageExist(IDevice device, String fullpackageName);

	/**
	 * run a shell command in device/emulator and get result as String[]
	 * @param device
	 * @param arocmd
	 * @return
	 */
	String[] getShellReturn(IDevice device, String arocmd);
	
	/**
	 * run VPN Android Collector APK in device
	 * @param device
	 * @return
	 */
	boolean runVpnApkInDevice(IDevice device);

	/**
	 * run Android Collector APK in device
	 * @param device
	 * @return
	 */
	boolean runApkInDevice(IDevice device, String cmd);
}