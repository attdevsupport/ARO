package com.att.aro.datacollector.rootedandroidcollector.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Future;

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
import com.att.aro.core.mobiledevice.IAndroidDevice;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;
import com.att.aro.core.util.GoogleAnalyticsUtil;
import com.att.aro.core.util.Util;
import com.att.aro.core.video.IVideoCapture;
import com.att.aro.datacollector.rootedandroidcollector.pojo.ErrorCodeRegistry;

/**
 * RootedAndroidCollectorImpl provides support for performing ARO traces on
 * 'rooted' devices and emulators.<br>
 * Currently emulators must be an ARM device.<br>
 */
public class RootedAndroidCollectorImpl implements IDataCollector, IVideoImageSubscriber {

	private static final int TCPDUMP_PORT = 50999;
	private volatile boolean running = false;
	@Autowired
	private IFileManager filemanager;
	private IDevice device;
	private ILogger log;
	@Autowired
	private IAdbService adbservice;
	@Autowired
	private IAndroid android;
	@Autowired
	private IReadWriteFileExtractor extractor;
	@Autowired
	private IThreadExecutor threadexecutor;
	private TcpdumpRunner runner;
	private Future<?> task;
	@Autowired
	private IVideoCapture videocapture;
	private boolean isCapturingVideo = false;
	//local directory in user machine to pull trace from device to
	private String localTraceFolder;
	private String traceName;
	//for checking rooted or not
	@Autowired
	private IAndroidDevice androidev;
	/**
	 * Local PC time when tcpdump was started
	 */
	private long tcpdumpStartTime = 0;

	//files to pull from Emulator
	private static final String[] EMULATORCOLLECTOR_TRACEFILENAMES = { "cpu", "appid", "appname", "time", "processed_events", "traffic.cap" };
	//files to pull from device
	
	private static final String[] DEVICECOLLECTORFILENAMES = { "traffic.cap"
																, "cpu"
																, "cpu_log.txt"
																, "appid"
																, "appname"
																, "time"
																, "processed_events"
																, "active_process"
																, "battery_events"
																, "bluetooth_events"
																, "camera_events"
																, "device_details"
																, "device_info"
																, "gps_events"
																, "network_details"
																, "prop"
																, "radio_events"
																, "screen_events"
																, "screen_rotations"
																, "user_input_log_events"
																, "batteryinfo_dump"
																, "dmesg"
																, "video_time"
																, "wifi_events" 
																, "alarm_info_end"
																, "alarm_info_start"
																};

	private List<IVideoImageSubscriber> videoImageSubscribers = new ArrayList<IVideoImageSubscriber>();
	private boolean seLinuxEnforced;
	private String deviceTracePath;
	private int milliSecondsForTimeout = 30000; // 30 seconds

	public int getMilliSecondsForTimeout() {
		return milliSecondsForTimeout;
	}

	@Autowired
	public void setLogger(ILogger logger) {
		this.log = logger;
	}

	@Override
	public String getName() {
		return "Rooted Android Data Collector";
	}

	@Override
	public void addDeviceStatusSubscriber(IDeviceStatus devicestatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVideoImageSubscriber(IVideoImageSubscriber subscriber) {
		videocapture.addSubscriber(subscriber);
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public String getMinorVersion() {
		return "1.1.11";
	}

	@Override
	public DataCollectorType getType() {
		return DataCollectorType.ROOTED_ANDROID;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public String[] getLog() {
		return android.getShellReturn(device, "echo ''Android version :'';getprop ro.build.version.release;logcat -d");
	}

	@Override
	public StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, String password) {
		return this.startCollector(isCommandLine, folderToSaveTrace, isCapturingVideo, true, null, null, password);
	}

	/**
	 * Start the rooted collector both for device and emulator<br>
	 * 
	 * Start collector in background and returns result which indicates success
	 * or error and detail data.
	 * 
	 * @param folderToSaveTrace
	 *            directory to save trace to
	 * @param isCapturingVideo
	 *            optional flag to capture video of device. default is false
	 * @param isLiveViewVideo
	 *            this flag is ignored in Android
	 * @return a StatusResult to hold result and success or failure
	 */
	@Override
	public StatusResult startCollector(boolean isCommandLine, String folderToSaveTrace, boolean isCapturingVideo, boolean isLiveViewVideo, String deviceId, Hashtable<String, Object> extraParams, String password) {

		log.info("<" + Util.getMethod() + "> startCollector() for rooted-android-collector");
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
			log.error("Exception :", e1);
		}

		if (devlist.length < 1) {
			result.setError(ErrorCodeRegistry.getNoDeviceConnected());
			return result;
		}

		device = null;
		if (deviceId == null) {
			device = devlist[0];
		} else {
			for (IDevice devi : devlist) {
				if (deviceId.equals(devi.getSerialNumber())) {
					device = devi;
					break;
				}
			}
			if (device == null) {
				result.setError(ErrorCodeRegistry.getDeviceIdNotFound());
				return result;
			}
		}

		//device must be rooted to work
		try {
			if (androidev.isAndroidRooted(device)) {
				log.debug("device is detected to be rooted");
			} else {
				result.setError(ErrorCodeRegistry.getRootedStatus());
				return result;
			}
		} catch (Exception exception) {
			log.error("Failed to root test device ", exception);
			result.setError(ErrorCodeRegistry.getProblemAccessingDevice(exception.getMessage()));
			return result;
		}

		try {
			device.createForward(TCPDUMP_PORT, TCPDUMP_PORT);
		} catch (TimeoutException e) {
			log.error("Timeout when creating port forwading: " + e.getMessage());
		} catch (AdbCommandRejectedException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		//check for running collector first => both tcpdump and ARO android app
		if (android.checkTcpDumpRunning(device)) {
			result.setError(ErrorCodeRegistry.getCollectorAlreadyRunning());
			return result;
		}

		String tracename = folderToSaveTrace.substring(folderToSaveTrace.lastIndexOf(Util.FILE_SEPARATOR) + 1);
		this.traceName = tracename;
		this.localTraceFolder = folderToSaveTrace;
		//delete all previous ARO traces on device or emulator /sdcard/ARO
		android.removeEmulatorData(device, "/sdcard/ARO");
		android.makeAROTraceDirectory(device, tracename);
		this.isCapturingVideo = isCapturingVideo;

		//check SELinux mode: true if SELinux-Enforced, false if permissive
		try {
			seLinuxEnforced = androidev.isSeLinuxEnforced(device);
		} catch (Exception e) {
			// Failed to detect so assume not enforced
			log.info("Failed to detect SELinux mode:" + e.getMessage());
			seLinuxEnforced = false;
		}

		if (device.isEmulator()) {
			//run collector inside Emulator
			result = launchCollectorInEmulator(device, tracename, isCapturingVideo, extraParams);
		} else {
			GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getRootedCollector(),
					GoogleAnalyticsUtil.getAnalyticsEvents().getStartTrace()); //GA Request

			//run collector inside Android device
			result = launchCollectorInDevice(device, tracename, isCapturingVideo, extraParams);
		}

		if (result.isSuccess()) {
			this.running = isTrafficCaptureRunning(getMilliSecondsForTimeout());
			if (this.running) {
				log.info("collector is running successfully");
			} else {
				timeOutShutdown();
				haltCollectorInDevice();
				log.info("collector timeout, traffic capture not started in time");
				result.setSuccess(false);
				result.setError(ErrorCodeRegistry.getCollectorTimeout());
				result.setData("installed but traffic capture failed to start before timeout");
				return result;
			}
		}

		if (this.isCapturingVideo) {
			GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getRootedCollector(),
					GoogleAnalyticsUtil.getAnalyticsEvents().getVideoCheck()); //GA Request
			captureVideo();
		}

		return result;
	}

	@Override
	public void timeOutShutdown() {
		log.debug("timeOutShutdown()");
		String[] responses = android.getShellReturn(device, "am broadcast -a arodatacollector.timeout.SHUTDOWN");
		if (responses != null) {
			for (String response : responses) {
				log.info(">" + response + "<");
			}
		}

	
	}

	/**
	 * force-stop rooted collector
	 */
	@Override
	public void haltCollectorInDevice() {

		android.getShellReturn(device, "am force-stop com.att.android.arodatacollector");
		if (task != null) {
			task.cancel(true);
			task = null;
		}
		runner = null;

	}

	/**
	 * check if the internal collector method is running, in this case tcpdump
	 */
	@Override
	public boolean isTrafficCaptureRunning(int seconds) {
		log.info("Wait for tcpdump to launch");
		boolean tcpdumpActive = false;
		int count = 30;
		int timer = seconds / count;
		do {
			tcpdumpActive = android.checkTcpDumpRunning(device);
			if (!tcpdumpActive) {
				try {
					log.info("waiting " + timer + ", for tcpdump to launch:" + count);
					Thread.sleep(timer);
				} catch (InterruptedException e) {
				}
			}
		} while (!tcpdumpActive && count-- > 0);
		return tcpdumpActive;
	}

	void captureVideo() {
		String videopath = this.localTraceFolder + Util.FILE_SEPARATOR + "video.mov";
		try {
			videocapture.init(device, videopath);
			threadexecutor.execute(videocapture);
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	StatusResult launchCollectorInEmulator(IDevice device, String tracename, boolean isCapturingVideo, Hashtable<String, Object> extraParams) {
		StatusResult result = new StatusResult();
		//just in case tcpdump was not shutdown propertly
		android.stopTcpDump(device);

		//check if device got some space to collect trace > 5 Mb
		if (!android.isSDCardEnoughSpace(device, 5120)) {
			result.setError(ErrorCodeRegistry.getDeviceHasNoSpace());
			return result;
		}

		String tcpdumpBinary = seLinuxEnforced ? "tcpdump_pie" : "tcpdump";
		String tcpdumpLocationOnDevice = "/data/data/com.att.android.arodatacollector/" + tcpdumpBinary;
		if (!installPayloadFile(device, localTraceFolder, tcpdumpBinary, tcpdumpLocationOnDevice)) {
			result.setError(ErrorCodeRegistry.getFailToInstallTcpdump());
			return result;
		}

		// set execute permission for tcpdump on remote device
		if (!android.setExecutePermission(device, tcpdumpLocationOnDevice)) {
			result.setError(ErrorCodeRegistry.getTcpdumpPermissionIssue());
			return result;
		}

		installPayloadFile(device, localTraceFolder, "key.db", "/data/data/com.att.android.arodatacollector/key.db");

		//launch tcpdump in background since it is a blocking operation
		runTcpdumpInBackground(device, tracename);
		result.setSuccess(true);
		return result;
	}

	void runTcpdumpInBackground(IDevice device, String traceName) {
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getEmulator(),
				GoogleAnalyticsUtil.getAnalyticsEvents().getStartTrace()); //GA Request

		
		runner = new TcpdumpRunner(device, traceName, android, seLinuxEnforced);
		task = threadexecutor.executeFuture(runner);
		//log.debug("executed tcpdump in background");
		tcpdumpStartTime = System.currentTimeMillis();
	}

	StatusResult launchCollectorInDevice(IDevice device, String folderToSaveTrace, boolean isCapturingVideo, Hashtable<String, Object> extraParams) {
		StatusResult result = new StatusResult();
		//just in case previous collector was not shutdown
		if (android.checkTcpDumpRunning(device)) {
			log.error("unknown collection still running on device");
			result.setError(ErrorCodeRegistry.getCollectorAlreadyRunning());
			return result;
		}
		haltCollectorInDevice();

		//check for ARO Collector on device, if not try installing it
		if (!android.checkPackageExist(device, "com.att.android.arodatacollector") && !pushApk(device)) {
			result.setError(ErrorCodeRegistry.getFailToInstallAPK());
			return result;
		}

		String cmd = "am start -n " + "com.att.android.arodatacollector/com.att.android.arodatacollector.activities"
				+ ".AROCollectorSplashActivity -e ERRORDIALOGID 100 -e TraceFolderName " + folderToSaveTrace;

		if (android.runApkInDevice(device, cmd)) {
			result.setSuccess(true);
		} else {
			result.setError(ErrorCodeRegistry.getFailToRunApk());
		}

		return result;
	}

	/**
	 * Extract a payload file from jar then push payload to Android. The payload
	 * is temporarily extracted to a folder then deleted after pushing to
	 * Android.
	 * 
	 * @param device
	 *            - Android device
	 * @param tempFolder
	 *            - traceFolder
	 * @param payloadFileName
	 *            - binary(payload) file to be injected into device
	 * @param remotepath
	 *            - path on Android for payload installation
	 * @return true is success - false if failed to extract or install
	 */
	boolean installPayloadFile(IDevice device, String tempFolder, String payloadFileName, String remotepath) {

		ClassLoader loader = RootedAndroidCollectorImpl.class.getClassLoader();
		boolean success = extractor.extractFiles(tempFolder + Util.FILE_SEPARATOR + payloadFileName, payloadFileName, loader);
		String payloadTempPath = localTraceFolder + Util.FILE_SEPARATOR + payloadFileName;

		if (success) {
			success = android.pushFile(device, payloadTempPath, remotepath);
			filemanager.deleteFile(payloadTempPath);
		}

		return success;
	}

	String getAROCollectorLocation() {
		return localTraceFolder + Util.FILE_SEPARATOR + "ARODataCollector.apk";
	}

	/**
	 * install ARO Data Collector on Device
	 * 
	 * @param device
	 * @return
	 */
	boolean pushApk(IDevice device) {
		String filepath = getAROCollectorLocation();
		boolean gotLocalAPK = true;
		if (!filemanager.fileExist(filepath)) {
			ClassLoader loader = RootedAndroidCollectorImpl.class.getClassLoader();
			if (!extractor.extractFiles(filepath, "rooted-ARODataCollector.apk", loader)) {
				gotLocalAPK = false;
			}
		}
		if (gotLocalAPK) {
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
	public StatusResult stopCollector() {
		log.info(Util.getMethod() + " stopCollector() for rooted-android-collector");

		StatusResult result = new StatusResult();
		this.running = false;
		if (device == null) {
			return result;
		}
		if (device.isEmulator()) {
			GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getEmulator(),
					GoogleAnalyticsUtil.getAnalyticsEvents().getEndTrace()); //GA Request
			android.stopTcpDump(device);
			while (android.isTraceRunning()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}

			result = pullTraceFromEmulator();
		} else {
			GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getRootedCollector(),
					GoogleAnalyticsUtil.getAnalyticsEvents().getEndTrace()); //GA Request

			stopCollectorInDevice();
			result = pullTrace(DEVICECOLLECTORFILENAMES);
		}
		if (this.isCapturingVideo) {
			stopCaptureVideo();
		}
		return result;
	}

	void stopCaptureVideo() {
		//create video time file
		if (videocapture.isVideoCaptureActive()) {
			String videotimepath = this.localTraceFolder + Util.FILE_SEPARATOR + "video_time";
			String data = Double.toString(videocapture.getVideoStartTime().getTime() / 1000.0);
			StringBuffer sBufferData = new StringBuffer(data);
			if (device.isEmulator()) {
				sBufferData.append(" " + Double.toString(tcpdumpStartTime / 1000.0));
			}
			InputStream stream = new ByteArrayInputStream(data.getBytes());
			try {
				filemanager.saveFile(stream, videotimepath);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			videocapture.stopRecording();
		}
	}

	/**
	 * <pre>
	 * Signals rooted collector to finish the collection, if active.
	 * A true response will indicate that a collection has happened and been finished (closed).
	 * 
	 * @return true - if collector was active and has been stopped.
	 * false - if collector was not active, therefore not stopped
	 */
	private boolean stopCollectorInDevice() {
		boolean result = false;
		if (android.checkTcpDumpRunning(device)) {
			String cmd = "am start -n " 
						+ "com.att.android.arodatacollector/com.att.android.arodatacollector.activities" 
						+ ".AROCollectorHomeActivity -e StopCollector yes";
			String[] responses = null;
			log.info("adb command :" + cmd);
			responses = android.getShellReturn(device, cmd);
			if (responses != null) {
				for (String response : responses) {
					log.info(">" + response + "<");
				}
			}
			int count = 10;
			while (!result && count-- > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				result = !android.checkTcpDumpRunning(device);
			}
		}
		return result;
	}

	/**
	 * Pulls the trace files from the emulator
	 * 
	 * @return StatusResult for success/failure
	 */
	private StatusResult pullTraceFromEmulator() {
		//create device_details file for emulator
		createDeviceDetails();
		//pull trace file from emulator
		return pullTrace(EMULATORCOLLECTOR_TRACEFILENAMES);
	}

	/**
	 * Pulls trace files
	 * 
	 * @param files
	 *            String array of trace files to pull
	 * @return StatusResult for success/failure
	 */
	private StatusResult pullTrace(String[] files) {
		StatusResult result = new StatusResult();
		SyncService service = null;
		try {
			service = device.getSyncService();
		} catch (TimeoutException e) {
			log.error("Timeout error when getting SyncService from device");
		} catch (AdbCommandRejectedException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error("IOException", e);
		}
		if (service == null) {
			result.setError(ErrorCodeRegistry.getFailSyncService());
			return result;
		}

		// first pull the last file to be prepared by AroDataCollector.APK
		String alarmInfoStart = "alarm_info_start";
		int count = 5;
		File traceAlarmInfoStart = new File(this.localTraceFolder + "/" + alarmInfoStart);
		while (!(traceAlarmInfoStart.exists() && traceAlarmInfoStart.length() > 0) && count-- > 0) {
			try {
				log.debug("alarm_info_start was missing or zero length, will sleep then pull again");
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.error("InterruptedException", e);
			}
			pullFile(service, alarmInfoStart);
		}

		// now pull the rest of the trace files
		deviceTracePath = "/sdcard/ARO/" + this.traceName + "/";
		for (String file : files) {
			pullFile(service, file);
		}
		
		result.setSuccess(true);
		return result;
	}

	/**
	 * @param service
	 * @param deviceTracePath
	 * @param file
	 */
	private void pullFile(SyncService service, String file) {
		try {
			log.info(Util.getMethod() + " pull :" + file);
			service.pullFile(deviceTracePath + file, this.localTraceFolder + "/" + file, SyncService.getNullProgressMonitor());
		} catch (SyncException e) {
			log.info("file not found :" + file);
		} catch (TimeoutException e) {
			log.error("TimeoutException " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException " + e.getMessage());
		}
	}

	/**
	 * for emulator. we need to create device details and save it to local
	 * folder.
	 */
	private void createDeviceDetails() {
		StringBuilder details = new StringBuilder(47);
		String eol = System.getProperty("line.separator");
		details.append("com.att.android.arodatacollector");
		details.append(eol);
		details.append("emulator");
		details.append(eol);
		String deviceManufacturer = device.getProperty("ro.product.manufacturer");
		if (deviceManufacturer == null) {
			deviceManufacturer = "";
		}
		details.append(deviceManufacturer);
		details.append(eol);
		details.append("android");
		details.append(eol);
		details.append(device.getProperty("ro.build.version.release"));
		details.append(eol);
		details.append("");//Similarly to 4.1.1
		details.append(eol);
		int deviceNetworkType = "UMTS".equalsIgnoreCase(device.getProperty("gsm.network.type")) ? 3 : -1;
		details.append(deviceNetworkType);
		details.append(eol);
		InputStream stream = new ByteArrayInputStream(details.toString().getBytes());
		String deviceDetails = this.localTraceFolder + Util.FILE_SEPARATOR + "device_details";
		try {
			filemanager.saveFile(stream, deviceDetails);
		} catch (IOException e) {
			log.error("Failed to create device_details");
		}
	}

	/**
	 * receive video frame from background capture thread, then forward it to
	 * subscribers
	 */
	@Override
	public void receiveImage(BufferedImage videoimage) {
		for (IVideoImageSubscriber subscriber : videoImageSubscribers) {
			subscriber.receiveImage(videoimage);
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
