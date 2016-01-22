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

package com.att.aro.datacollector.norootedandroidcollector.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.TimeoutException;
import com.att.aro.core.ILogger;
import com.att.aro.core.adb.IAdbService;
import com.att.aro.core.android.IAndroid;
import com.att.aro.core.concurrent.IThreadExecutor;
import com.att.aro.core.datacollector.DataCollectorType;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDeviceStatus;
import com.att.aro.core.datacollector.IVideoImageSubscriber;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;
import com.att.aro.core.util.GoogleAnalyticsUtil;
import com.att.aro.core.util.Util;
import com.att.aro.core.video.IVideoCapture;
import com.att.aro.datacollector.norootedandroidcollector.pojo.ErrorCodeRegistry;

public class NorootedAndroidCollectorImpl implements IDataCollector, IVideoImageSubscriber {

	private ILogger log;
	private volatile boolean running = false;
	private IReadWriteFileExtractor extractor;
	private IThreadExecutor threadexecutor;
	private List<IVideoImageSubscriber> videoImageSubscribers = new ArrayList<IVideoImageSubscriber>();
	private IVideoCapture videoCapture;
	private boolean isCapturingVideo = false;
	private IDevice device;
	private IAdbService adbservice;
	private IFileManager filemanager;
	//local directory in user machine to pull trace from device to
	private String localTraceFolder;
	private static final int SECONDSFORTIMEOUT = 30;
	
	private IAndroid android;

	//files to pull from device
	private String[] mDataDeviceCollectortraceFileNames = { "cpu", "appid", "appname", "time", "processed_events", "active_process", "battery_events", "bluetooth_events",
			"camera_events", "device_details", "device_info", "gps_events", "network_details", "prop", "radio_events", "screen_events", "screen_rotations",
			"user_input_log_events", "alarm_info_start", "alarm_info_end", "batteryinfo_dump", "dmesg", "video_time", "wifi_events", "traffic.cap" };

	@Autowired
	public void setLog(ILogger log) {
		this.log = log;
	}

	@Autowired
	public void setAndroid(IAndroid android) {
		this.android = android;
	}

	@Autowired
	public void setFileManager(IFileManager filemanager) {
		this.filemanager = filemanager;
	}

	public void setDevice(IDevice aDevice){
		this.device = aDevice;
	}

	@Autowired
	public void setAdbService(IAdbService adbservice) {
		this.adbservice = adbservice;
	}

	@Autowired
	public void setVideoCapture(IVideoCapture videocapture) {
		this.videoCapture = videocapture;
	}

	@Autowired
	public void setThreadExecutor(IThreadExecutor thread) {
		this.threadexecutor = thread;
	}

	@Autowired
	public void setFileExtactor(IReadWriteFileExtractor extractor) {
		this.extractor = extractor;
	}

	@Override
	public String getName() {
		return "VPN Android Collector";
	}

	@Override
	public void addDeviceStatusSubscriber(IDeviceStatus subscriber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVideoImageSubscriber(IVideoImageSubscriber subscriber) {
		videoCapture.addSubscriber(subscriber);
	}

	/**
	 * receive video frame from background capture thread, then forward it to
	 * subscribers
	 */
	@Override
	public void receiveImage(BufferedImage videoimage) {
		log.debug("receiveImage");
		for (IVideoImageSubscriber subscriber : videoImageSubscribers) {
			subscriber.receiveImage(videoimage);
		}
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public String getMinorVersion() {
		return "0.0.1";
	}

	@Override
	public DataCollectorType getType() {
		return DataCollectorType.NON_ROOTED_ANDROID;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public void stopRunning() {
		this.running = false;
	}

	/**
	 * 
	 * @return logcat dump from device
	 */
	@Override
	public String[] getLog(){
		return android.getShellReturn(device, "echo ''Android version :'';getprop ro.build.version.release;logcat -d");
	}
	
	/**
	 * Start collector in background and returns result which indicates success
	 * or error and detail data.
	 * 
	 * @param folderToSaveTrace
	 *            directory to save trace to
	 * @param isCapturingVideo
	 *            optional flag to capture video of device. default is false
	 * @param isLiveViewVideo
	 *            ignored here
	 * @param androidId
	 *            optional id of device to capture. default is the connected
	 *            device.
	 * @param extraParams
	 *            optional data to pass to collectors. required by some
	 *            collectors.
	 * @return a StatusResult to hold result and success or failure
	 */	@Override
	public StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, boolean isLiveViewVideo, String deviceId, Hashtable<String, Object> extraParams, String password) {
		log.info("startCollector() for non-rooted-android-collector");
		StatusResult result = new StatusResult();
		
		//avoid running it twice
		if (this.running) {
			return result;
		}
		
		if (filemanager.directoryExistAndNotEmpty(folderToSaveTrace)) {
			result.setError(ErrorCodeRegistry.getTraceDirExist());
			return result;
		}
		//there might be permission issue to creating dir to save trace
		filemanager.mkDir(folderToSaveTrace);
		if (!filemanager.directoryExist(folderToSaveTrace)) {
			result.setError(ErrorCodeRegistry.getFailedToCreateLocalTraceDirectory());
			return result;
		}
		//check for any connected device
		IDevice[] devlist = null;
		try {
			devlist = adbservice.getConnectedDevices();
		} catch (Exception e1) {
			if (e1.getMessage().contains("AndroidDebugBridge failed to start")) {
				result.setError(ErrorCodeRegistry.getAndroidBridgeFailedToStart());
				return result;
			}
		}
		if (devlist.length < 1) {
			result.setError(ErrorCodeRegistry.getNoDeviceConnected());
			return result;
		}
		this.device = null;
		if (deviceId != null) {
			for (IDevice aDevice : devlist) {
				if (deviceId.equals(aDevice.getSerialNumber())) {
					this.device = aDevice;
					break;
				}
			}
			if (this.device == null) {
				result.setError(ErrorCodeRegistry.getDeviceIdNotFound());
				return result;
			}
		} else {
			this.device = devlist[0];
		}
		log.debug("check VPN");
		if (isVpnActivated()){
			log.error("unknown collection still running on device");
			result.setError(ErrorCodeRegistry.getCollectorAlreadyRunning());
			return result;
		}
		
		//String tracename = folderToSaveTrace.substring(folderToSaveTrace.lastIndexOf(Util.FILE_SEPARATOR) + 1);

		this.localTraceFolder = folderToSaveTrace;
		this.isCapturingVideo = isCapturingVideo;

		//remove existing trace if presence
		android.removeEmulatorData(this.device, "/sdcard/ARO");
		android.makeDirectory(this.device, "/sdcard/ARO");

		//there might be an instance of vpn_collector running
		//to be sure it is not in memory
		this.haltCollectorInDevice();

		if (pushApk(this.device)) {
		//	if (!android.runVpnApkInDevice(this.device)) {
			String cmd = "am start -n com.att.arocollector/com.att.arocollector.AROCollectorActivity";
			if (!android.runApkInDevice(this.device, cmd)) {
				result.setError(ErrorCodeRegistry.getFaildedToRunVpnApk());
				return result;
			}
		} else {
			result.setError(ErrorCodeRegistry.getFailToInstallAPK());
			return result;
		}
		if (!isTrafficCaptureRunning(SECONDSFORTIMEOUT)) {
			//timeout while waiting for VPN to activate within 15 seconds
			timeOutShutdown();
			result.setError(ErrorCodeRegistry.getTimeoutVpnActivation(SECONDSFORTIMEOUT));
			return result;
		}
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getNonRootedCollector(), GoogleAnalyticsUtil.getAnalyticsEvents().getStartTrace()); //GA Request
		this.gotoHomeScreen();
		if (this.isCapturingVideo) {
			GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getNonRootedCollector(), GoogleAnalyticsUtil.getAnalyticsEvents().getVideoCheck()); //GA Request
			startVideoCapture();
		}
		result.setSuccess(true);
		this.running = true;
		return result;
	}

	void startVideoCapture() {
		String videopath = this.localTraceFolder + Util.FILE_SEPARATOR + "video.mov";
		try {
			videoCapture.init(this.device, videopath);
			log.debug("execute videocapture Thread");
			threadexecutor.execute(videoCapture);
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	private boolean waitForVpn() {
		boolean success = true;
		int counter = 0;
		do {
			counter++;
			log.debug("waitForVpn() iteration:"+counter);
			success = isVpnActivated();
			if (!success) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		} while (counter < 150 && !success);
		return success;
	}

	private void gotoHomeScreen() {
		String cmd = "am start -a android.intent.action.MAIN -c android.intent.category.HOME";
		android.getShellReturn(this.device, cmd);
	}

	/**
	 * check if the internal traffic capture is running, in this case VPN
	 */
	@Override
	public boolean isTrafficCaptureRunning(int seconds) {
		boolean trafficCaptureActive = false;
		int timer = 1000;
		do {
			log.debug("isTrafficCaptureRunning() seconds left :"+seconds);
			trafficCaptureActive = isVpnActivated();
			if (!trafficCaptureActive) {
				try {
					Thread.sleep(timer);
				} catch (InterruptedException e) {
				}
			}
		} while (trafficCaptureActive == false && seconds-- > 0);
		return trafficCaptureActive;
	}
	

	/**
	 * check device to see if VPN was activated
	 * 
	 * @return
	 */
	public boolean isVpnActivated() {
		String cmd = "ifconfig tun0";
		String[] lines = android.getShellReturn(this.device, cmd);
		boolean success = false;
		//	log.debug("responses :" + lines.length);
		for (String line : lines) {
			//		log.debug("<" + line + ">");
			if (line.contains("tun0: ip 10.") || line.contains("UP POINTOPOINT RUNNING")) {
				log.info("tun is active :" + line);
				success = true;
				break;
			}
		}
		return success;
	}

	/**
	 * install ARO Data Collector on Device
	 * 
	 * @param device
	 * @return
	 */
	boolean pushApk(IDevice device) {
		String filepath = localTraceFolder + Util.FILE_SEPARATOR + "AROCollector.apk"; // getAROCollectorLocation();
		boolean gotlocalapk = true;
		if (!filemanager.fileExist(filepath)) {
			ClassLoader loader = NorootedAndroidCollectorImpl.class.getClassLoader();
			if (!extractor.extractFiles(filepath, "AROCollector.apk", loader)) {
				gotlocalapk = false;
			}
		}
		if (gotlocalapk) {
			try {
				device.installPackage(filepath, true);
				log.debug("installed apk in device");
				filemanager.deleteFile(filepath);
			} catch (InstallException e) {
				log.error(e.getMessage());
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public StatusResult startCollector(boolean isCommandLine, String tracepath, boolean capturevideo, String password) {
		return this.startCollector(isCommandLine, tracepath, capturevideo, false, null, null, password);
	}

	/**
	 * issue commands to stop the collector on vpn
	 * This cannot halt the vpn connection programmatically.
	 * VPN must be revoked through gestures. Best done by human interaction.
	 * With sufficient knowledge of screen size, VPN implementation, Android Version
	 * gestures can be programmatically performed to close the connection.
	 */
	@Override
	public StatusResult stopCollector() {
		StatusResult result = new StatusResult();
		int count = 0;
		boolean stillRunning = true;
		log.debug("send stop command to app");
		this.sendStopCommand();
		//wait at most 2 seconds
		while (count < 20 && stillRunning) {
			count++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			stillRunning = isCollectorRunning();
		}
		if (stillRunning) {
			log.debug("send stop command again");
			this.sendStopCommand();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			//last option is to force stop it
			if (isCollectorRunning()) {
				log.debug("Force stop app");
				this.haltCollectorInDevice();
			}
		}
		if (isCapturingVideo) {
			log.debug("stopping video capture");
			this.stopCaptureVideo();
		}
		log.debug("pulling trace to local dir");
		result = pullTrace(this.mDataDeviceCollectortraceFileNames);
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getNonRootedCollector(), GoogleAnalyticsUtil.getAnalyticsEvents().getEndTrace()); //GA Request
		//clean up data
		android.removeEmulatorData(this.device, "/sdcard/ARO");
		running = false;
		return result;
	}
	
	@Override
	public void timeOutShutdown() {
		haltCollectorInDevice();
	}
	

	@Override
	public void haltCollectorInDevice() {
		android.getShellReturn(this.device, "am force-stop com.att.arocollector");
	}

	private boolean isCollectorRunning() {
		String cmd = "ps | grep com.att";
		String[] lines = android.getShellReturn(this.device, cmd);
		boolean found = false;
		for (String line : lines) {
			if (line.contains("com.att.arocollector")) {
				found = true;
				break;
			}
		}
		return found;
	}

	private void sendStopCommand() {
		String cmdclosevpn = "am broadcast -a arovpndatacollector.service.close";
		String cmdcloseapp = "am broadcast -a arodatacollector.home.activity.close";
		android.getShellReturn(this.device, cmdclosevpn);
		android.getShellReturn(this.device, cmdcloseapp);
	}

	private StatusResult pullTrace(String[] files) {
		StatusResult result = new StatusResult();
		SyncService service = null;
		try {
			service = this.device.getSyncService();
		} catch (TimeoutException e) {
			log.error("Timeout error when getting SyncService from device");
		} catch (AdbCommandRejectedException e) {
			log.error("AdbCommandRejectionException: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}
		if (service == null) {
			result.setError(ErrorCodeRegistry.getFailSyncService());
			return result;
		}
		String deviceTracePath = "/sdcard/ARO/";
		for (String file : files) {
			try {
				service.pullFile(deviceTracePath + file, this.localTraceFolder + "/" + file, SyncService.getNullProgressMonitor());
			} catch (SyncException e) {
				log.info("file not found :" + file);
			} catch (TimeoutException e) {
				log.error("TimeoutException " + e.getMessage());
			} catch (IOException e) {
				log.error("IOException " + e.getMessage());
			}
		}
		result.setSuccess(true);
		return result;
	}

	void stopCaptureVideo() {
		//create video time file
		if (videoCapture.isVideoCaptureActive()) {
			log.debug("stopCaptureVideo(");
			String videotimepath = this.localTraceFolder + Util.FILE_SEPARATOR + "video_time";
			String data = "0.00";
			Date videoTimeStart = videoCapture.getVideoStartTime();
			if (videoTimeStart != null) {
				data = Double.toString(videoTimeStart.getTime() / 1000.0);
			}

			InputStream stream = new ByteArrayInputStream(data.getBytes());
			try {
				filemanager.saveFile(stream, videotimepath);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			videoCapture.stopRecording();
		}
	}

	/**
	 * Android does not require a password
	 */
	@Override
	public String getPassword() {
		return null;
	}

	/**
	 * Android does not require a password
	 * @return false, always false, password not used in Android
	 */
	@Override
	public boolean setPassword(String requestPassword) {
		return false;
	}
	
}
