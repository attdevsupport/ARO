/*
 * Copyright 2012 AT&T
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

package com.att.android.arodatacollector.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.activities.AROCollectorHomeActivity;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.flurry.android.FlurryAgent;

/**
 * An ARO Data Collector service class that captures trace data from device
 * peripherals traces (like Wifi and GPS) during data collection.
 * 
 * */

public class AROCollectorTraceService extends Service {

	/** Log TAG string for ARO-Data Collector service class */
	private static final String TAG = "AROCollectorTraceService";

	/** Minimum SD card space required (2 MB) before start of the trace */
	private static final int AROSDCARD_MIN_SPACEKBYTES = 2048;

	/** Wifi trace file name */
	private static final String outWifiFileName = "wifi_events";

	/** battery trace file name */
	private static final String outBatteryFileName = "battery_events";

	/** Gps event trace file name */
	private static final String outGPSFileName = "gps_events";

	/** Radio event trace file name */
	private static final String outRadioFileName = "radio_events";

	/** camera event trace file name */
	private static final String outCameraFileName = "camera_events";

	/** Bluetooth event trace file name */
	private static final String outBluetoothFileName = "bluetooth_events";

	/** screen event trace file name */
	private static final String outScreenFileName = "screen_events";
	
	/** network bearer event trace file name */
	private static final String outNetworkDetailsFileName = "network_details";

	/** device ip address trace file name */
	private static final String outDeviceInfoFileName = "device_info";

	/** device ip address,make ,model trace file name */
	private static final String outDeviceDetailsFileName = "device_details";

	/** active running process trace file name */
	private static final String outActiveProcessFileName = "active_process";

	/** screen event trace file name */
	private static final String outScreenRotationFileName = "screen_rotations";
	
	/** cpu event file name */
	private static final String outCpuFileName = "cpu";

	/**
	 * LandScape Screen orientation
	 */
	private static final String LANDSCAPE_MODE = "landscape";
	/**
	 * LandScape Screen orientation
	 */
	private static final String PORTRAIT_MODE = "portrait";

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean mIsProduction = false;

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean DEBUG = !mIsProduction;

	/**
	 * Camera/GPS/Screen trace timer repeat time value to capture camera events
	 * ( 1/2 seconds)
	 */
	private static int HALF_SECOND_TARCE_TIMER_REPATE_TIME = 1000;

	/** Timer value to check SD Card space during trace cycle every 5 seconds */
	private static int SDCARD_TARCE_TIMER_REPATE_TIME = 5000;
	
	/** Timer value to check Airplane mode enabled during trace cycle every 1 second */
	private static int AIRPLANE_TARCE_TIMER_REPATE_TIME = 1000;

	/** AROCollectorTraceService object */
	private static AROCollectorTraceService mDataCollectorTraceService;

	/**
	 * The Application context of the ARo-Data Collector to gets and sets the
	 * application data
	 **/
	private ARODataCollector mApp;

	/** Broadcast receiver for Batter events */
	private BroadcastReceiver mBatteryLevelReceiver;

	/** Active running processes package list */
	private List<RunningAppProcessInfo> mActiveProcessprocess;

	/** GPS active boolean flag */
	private Boolean mGPSActive = false;

	/** Current Camera state on/off boolean flag */
	private Boolean mCameraOn = false;

	/** Previous Camera state on/off boolean flag */
	private Boolean mPrevCameraOn = true;

	/** Screen brightness value from 0-255 */
	private float mScreencurBrightness = 0;

	/** Previous Screen brightness value */
	private float mPrevScreencurBrightness = 1;

	/** Screen timeout (Device sleep) value in seconds */
	private int mScreenTimeout = 0;

	/** Previous Screen timeout (Device sleep) value in seconds */
	private int mPrevScreenTimeout = 0;

	/** Location Manager class object */
	private LocationManager mGPSStatesManager;

	/** GPS State listener */
	private GpsStatus.Listener mGPSStatesListner;

	/** Previous GPS enabled state */
	private boolean prevGpsEnabledState = false;

	/** Timer to run every 500 milliseconds to check GPS states */
	private Timer checkLocationService = new Timer();

	/** Timer to run every 500 milliseconds to check Camera states */
	private Timer checkCameraLaunch = new Timer();

	/**
	 * Timer to run every 5 seconds to check SD card space is always greater
	 * than 5MB to continue trace
	 */
	private Timer checkSDCardSpace = new Timer();
	
	/**
	 * Timer to run every 1 second to check Airplane mode has not been enabled
	 */
	private Timer checkAirplaneModeEnabled = new Timer();

	/**
	 * Timer to run every 500 milliseconds to get screen brightness value in
	 * order to get change
	 */
	private Timer checkScreenBrightness = new Timer();

	/** Intent filter to adding action for broadcast receivers **/
	private IntentFilter mAROIntentFilter;

	/** Intent filter to adding action for bluetooth broadcast receivers **/
	private IntentFilter mAROBluetoothIntentFilter;

	/** Telephony manager class object **/
	private TelephonyManager mTelphoneManager;

	/** Phone state listener listener to get RSSI value **/
	private PhoneStateListener mPhoneStateListener;
	private ConnectivityManager mConnectivityManager;
	private WifiManager mWifiManager;
	private String mWifiMacAddress;
	private String mWifiNetworkSSID;
	private int mWifiRssi;
	private boolean isFirstBearerChange = true;
	private int mAROPrevNetworkType;
	
	/**indicates whether WIFI, MOBILE, or UNKNOWN **/
	private String mAROPrevBearer = AroTraceFileConstants.NOT_ASSIGNED_NETWORK;
	
	/** Output stream and Buffer Writer for peripherals traces files */
	private OutputStream mWifiTraceOutputFile;
	private BufferedWriter mWifiTracewriter;
	private OutputStream mBatteryTraceOutputFile;
	private BufferedWriter mBatteryTracewriter;
	private OutputStream mGPSTraceOutputFile;
	private BufferedWriter mGPSTracewriter;
	private OutputStream mRadioTraceOutputFile;
	private BufferedWriter mRadioTracewriter;
	private OutputStream mCameraTraceOutputFile;
	private BufferedWriter mCameraTracewriter;
	private OutputStream mBluetoohTraceOutputFile;
	private BufferedWriter mBluetoothTracewriter;
	private OutputStream mScreenOutputFile;
	private BufferedWriter mScreenTracewriter;
	private OutputStream mScreenRotationOutputFile;
	private BufferedWriter mScreenRotationTracewriter;
	private OutputStream mActiveProcessOutputFile;
	private BufferedWriter mActiveProcessTracewriter;
	private OutputStream mDeviceInfoOutputFile;
	private OutputStream mDeviceDetailsOutputFile;
	private BufferedWriter mDeviceInfoWriter;
	private BufferedWriter mDeviceDetailsWriter;
	private OutputStream mNetworkDetailsOutputFile;
	private BufferedWriter mNetworkTracewriter;
	private OutputStream mCpuTraceOutputFile;
	private BufferedWriter mCpuTraceWriter;
	
	/** ARO Data Collector utilities class object */
	private AROCollectorUtils mAroUtils;

	/** The broadcast receiver that listens for screen rotation. */
	private BroadcastReceiver mScreenRotationReceiver;

	/** String constants used in ARO trace files */
	private static class AroTraceFileConstants {
		static final String OFF = "OFF";
		static final String ON = "ON";
		static final String STANDBY = "STANDBY";
		static final String CONNECTED = "CONNECTED";
		static final String DISCONNCTED = "DISCONNECTED";
		static final String CONNECTED_NETWORK = "CONNECTED";
		static final String DISCONNECTING_NETWORK = "DISCONNECTING";
		static final String CONNECTING_NETWORK = "CONNECTING";
		static final String DISCONNECTED_NETWORK = "DISCONNECTED";
		static final String SUSPENDED_NETWORK = "SUSPENDED";
		static final String UNKNOWN_NETWORK = "UNKNOWN";
		static String IMPORTANCE_BACKGROUND = "Background";
		static String IMPORTANCE_FOREGROUND = "Foreground";
		static final String NOT_ASSIGNED_NETWORK = "NOTASSIGNED";
	}
	
	/** Event names, counters, maps, states for displaying and storing on Flurry Analytics*/
	public static FlurryEvent bluetoothFlurryEvent = null;
	public static FlurryEvent networkTypeFlurryEvent = null;
	public static FlurryEvent networkInterfaceFlurryEvent = null;
	public static FlurryEvent wifiFlurryEvent = null;
	public static FlurryEvent batteryFlurryEvent = null;	
	public static FlurryEvent gpsFlurryEvent = null;	
	public static FlurryEvent cameraFlurryEvent = null;	
	
	public static FlurryEvent backgroundAppsFlurryEvent = null; //log Flurry event at end of trace
	public static FlurryEvent makeModelEvent = null;			//log Flurry event at end of trace
	
	//for cpu tracing
	private boolean columnHeaderLineProcessed = false, columnIndicesSet = false, totalCpuLineProcessed = false;
	private boolean doneParsingProcessCpu = false;
	private static int processCpuColumnIndex = -1, processNameColumnIndex = -1, curProcessCount = 0, nonEmptyLineNum = 0;
	private static final int PROCESS_LIMIT = 8;
	
	private static final String CPU_HEADER = "CPU%";
	private static final String PROCESS_NAME_HEADER = "Name";
	
	//private static final Pattern totalCpuLinePattern = Pattern.compile("^User (\\d+)%, System (\\d+)%, IOW (\\d+)%, IRQ (\\d+)%");
	//total cpu line has pattern: "User 261 + Nice 1 + Sys 105 + Idle 388 + IOW 7 + IRQ 0 + SIRQ 0 = 762"
	private static final Pattern totalCpuLinePattern = Pattern.compile("^User (\\d+) \\+ Nice (\\d+) \\+ Sys (\\d+) \\+ Idle (\\d+) \\+ IOW (\\d+) \\+ IRQ (\\d+) \\+ SIRQ (\\d+) = (\\d+)");
	private static final int CPU_PATTERN_GROUP_COUNT = 8;
	private static final int TOTAL_CPU_GROUP_NUMBER = 8;
	private static final int IDLE_CPU_GROUP_NUMBER = 4;
	
	private static final String WHITE_SPACE_REG_EXP = "\\s+";
	private static final int CPU_TRACE_INTERVAL_MILLIS = 5000;
	private static final int CPU_TRACE_INITIAL_DELAY_MILLIS = 4000;
	
	Timer cpuProcessingTimer;
	private final String CPU_DIR = "CpuFiles/";
	private static final String CPU_SCRIPT_PID = "CPU_SCRIPT_PID";
	
	public static final String USB_BROADCAST_ACTION = "USB_BROADCAST_ACTION";
	private static Intent usbBroadcastIntent;
	public static final String USB_ACTION_EXTRA_KEY = "DataCollectorStopEnable";
	
	/**
	 * Handles processing when an AROCollectorTraceService object is created.
	 * Overrides the android.app.Service#onCreate method.
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mAroUtils = new AROCollectorUtils();
		
		if (DEBUG){
			Log.i(TAG, "starting AROCollectorTraceService at timestamp=" + mAroUtils.getDataCollectorEventTimeStamp());
		}
		
		mDataCollectorTraceService = this;
		mApp = (ARODataCollector) getApplication();
		initializeFlurryObjects();
		startARODataTraceCollection();
		
		//init the usb broadcast intent only once
		usbBroadcastIntent = new Intent(USB_BROADCAST_ACTION);
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Notification mAROnotification = mApp.getARONotification();
		NotificationManager mAROnotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mAROnotificationManager.notify(ARODataCollector.NOTIFICATION_ID, mAROnotification);
		startForeground(ARODataCollector.NOTIFICATION_ID, mAROnotification);
		
		if (DEBUG){
			Log.d(TAG, "AROCollectorTraceService started in foreground at timestamp:" + System.currentTimeMillis());
		}
		return (START_NOT_STICKY);

	}
	
	/**
	 * Handles processing when an AROCollectorTraceService object is destroyed.
	 * Overrides the android.app.Service#onDestroy method.
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if (DEBUG) {
			Log.i(TAG, "onDestroy called for AROCollectorTraceService " + mAroUtils.getSystemTimeinSeconds());
		}
		super.onDestroy();
		stopARODataTraceCollection();
		mApp.cancleAROAlertNotification();
		
		mDataCollectorTraceService = null;
	}

	/**
	 * Returns a valid instance of the AROCollectorTraceService class.
	 * 
	 * @return An AROCollectorTraceService object.
	 */
	public static AROCollectorTraceService getServiceObj() {
		return mDataCollectorTraceService;
	}

	/**
	 * Initializes and starts the ARO-Data Collector peripherals trace
	 * collection
	 * 
	 * @throws FileNotFoundException
	 */
	private void startARODataTraceCollection() {
		try {
			if (DEBUG){
				Log.i(TAG, "starting ARO peripheral trace at timestamp=" + mAroUtils.getDataCollectorEventTimeStamp());
			}
			initAROTraceFile();
			startAROCpuTrace();
			startAROScreenTraceMonitor();
			startAROGpsTraceMonitor();
			startAROBatteryLevelMonitor();
			startARORadioTraceMonitor();
			startAROBluetoothTraceMonitor();
			startAROWifiTraceMonitor();
			startAROActiveProcessTrace();
			startCameraTrace();
			startARODataBearerChangeNotification();
			startARODeviceSDCardSpaceMidTrace();
			startAROAirplaneModeMidTrace();
			startAroScreenRotationMonitor();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "exception in initAROTraceFile: Failed to start ARO-Data Collector Trace", e);
		}
	}
	

	/**
	 * save the pid of the cpu script
	 * @param pid
	 */
	public void setCpuScriptPid(String pid) {
		int pidInt = -1;
		try {
			pidInt = Integer.parseInt(pid);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		final SharedPreferences prefs = getSharedPreferences(ARODataCollector.PREFS, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(CPU_SCRIPT_PID, pidInt);
		editor.commit();
	}
	
	/**
	 * get the full path of the directory of the cpu files
	 * @return full path of the cpu dir
	 */
	private String getCpuDirFullPath(){
		return mApp.getTcpDumpTraceFolderName() + CPU_DIR;
	}
	

	/**
	 * get the pid of the cpu script
	 * 
	 * @return pid of the cpu script
	 */
	public int getCpuScriptPid() {
		final SharedPreferences prefs = getSharedPreferences(ARODataCollector.PREFS, 0);
		return prefs.getInt(CPU_SCRIPT_PID, -1);
	}
	
	/**
	 * Start the script that tracks the cpu usage stats
	 * 1. give script executable permission
	 * 2. make output dir
	 * 3. execute script
	 */
	private void startAROCpuTraceScript(){
		final int cpuScriptPid = getCpuScriptPid();
		
		if (cpuScriptPid == -1){
			//script NOT already running,  need to start it
			String cpuDirFullPath = getCpuDirFullPath();
			
			DataOutputStream os = null;
			try {
				File cpuDir = new File(cpuDirFullPath);
				if (!cpuDir.exists()){
					cpuDir.mkdir();
				}
					
				Process cpuMonProcess = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(cpuMonProcess.getOutputStream());
				String Command = "chmod 777 " + ARODataCollector.INTERNAL_DATA_PATH + ARODataCollector.PROCESS_CPU_MON
						+ "\n";
				os.writeBytes(Command);
					
				Command = "sh " + ARODataCollector.INTERNAL_DATA_PATH + ARODataCollector.PROCESS_CPU_MON + " " + cpuDirFullPath + "\n";
				os.writeBytes(Command);
				os.flush();
					
				BufferedReader reader = new BufferedReader(new InputStreamReader(cpuMonProcess.getInputStream()));
				//the pid is the first line of the output
				String pid = reader.readLine();
				setCpuScriptPid(pid);
			} catch (Exception e){
				e.printStackTrace();
			}
			finally {
				if (os != null){
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			Log.d(TAG, "script already running under pid=" + cpuScriptPid);
			writeTraceLineToAROTraceFile(mCpuDebugWriter, "script already running under pid=" + cpuScriptPid, true);
		}
	}
	
	/**
	 * method to start the cpu trace
	 */
	private void startAROCpuTrace(){
		//start the script
		startAROCpuTraceScript();
		
		//start timer to process the script output files
		cpuProcessingTimer = new Timer();
				
		TimerTask checkCpuUsageTask = new TimerTask(){
		//Runnable cpuTrace = new Runnable(){
			public void run(){
				processCpuDirectory(); 
			}

		};
		
		cpuProcessingTimer.scheduleAtFixedRate(checkCpuUsageTask, CPU_TRACE_INITIAL_DELAY_MILLIS, CPU_TRACE_INTERVAL_MILLIS);
	}
	
	/**
	 * Method to process all the files in the cpu directory.
	 * needs synchronization here so that in case the timertask is already in progress,
	 * the stop code calling this method will need to wait
	 */
	private synchronized void processCpuDirectory() {
		long start = System.currentTimeMillis();
		Log.i(TAG, "processCpuDirectory() started at " + start);
		writeTraceLineToAROTraceFile(mCpuDebugWriter, "cpu file processing started", true);
		try {
			File cpuDir = new File(getCpuDirFullPath());
			if (cpuDir.exists() && cpuDir.listFiles() != null){
				for (File cpuFile : cpuDir.listFiles()){
					processCpuFile(cpuFile);
				}
			}
			
		} catch (Exception e) {
			writeTraceLineToAROTraceFile(mCpuDebugWriter, "Exception occurred in checkCpuUsageTask: " + e.getMessage(), true);
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis();
		Log.i(TAG, "processCpuDirectory() ended at " + end + ". Duration: " + (end - start));
	}

	
	/**
	 * Method to process the cpu statistic file, which is the output of the 'top' command
	 * @param cpuFile
	 */
	private void processCpuFile(File cpuFile){
		writeTraceLineToAROTraceFile(mCpuDebugWriter, "start processing " + cpuFile.getName(), true);
		long start = System.currentTimeMillis();
		//reset columnHeaderLineProcessed, totalCpuLineProcessed
		columnHeaderLineProcessed = false;
		totalCpuLineProcessed = false;
		curProcessCount = 0;
		nonEmptyLineNum = 0;
		
		doneParsingProcessCpu = false;
		
		String line = "";
		boolean isDelete = true;
		StringBuffer content = new StringBuffer();
		BufferedReader input =  null;
		try {
			input =  new BufferedReader(new FileReader(cpuFile));
			while ((!doneParsingProcessCpu && curProcessCount < PROCESS_LIMIT) 
					&& ( line = input.readLine()) != null){
				parseCpuInfo(content, line);
			}
			String contentStr = content.toString().trim();
			if (contentStr.length() > 0){
				double traceFileTimestamp = Double.parseDouble(cpuFile.getName());
				writeTraceLineToAROTraceFile(mCpuTraceWriter, traceFileTimestamp + " " + contentStr, false);
			}
			else {
				isDelete = false;
			}
			
			long end = System.currentTimeMillis();
			writeTraceLineToAROTraceFile(mCpuDebugWriter, (end - start) + "ms| " + contentStr, true);
			
		} catch (Exception e){
			//this file has error, allow processing to continue to the next file
			writeTraceLineToAROTraceFile(mCpuDebugWriter, cpuFile.getName() + ": Exception: " + e.getMessage() + ". Line: " + line, true);
			e.printStackTrace();
		}
		finally {
			try {
				input.close();
				
				if (isDelete){
					cpuFile.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
				writeTraceLineToAROTraceFile(mCpuDebugWriter, cpuFile.getName() + ": Exception: " + e.getMessage(), true);
			}
		}
	}
	
	
	/**
	 * Method to parse the cpu info from a line of text outputted by the 'top' command
	 * @param content
	 * @param line
	 */
	private void parseCpuInfo(StringBuffer content, String line) {
		//writeTraceLineToAROTraceFile(mCpuDebugWriter, "parseCpuInfo for line=: " + line, false);

		line = line.replaceAll("\r", "").replaceAll("\n", "").trim();
		if (line.length() > 0){
			++nonEmptyLineNum;
			
			if (!columnHeaderLineProcessed){
				if (!totalCpuLineProcessed && parseTotalCpuLine(content, line)){
					totalCpuLineProcessed = true;
				}
					
				else if (totalCpuLineProcessed && isColumnHeaderLine(line)){
					if (!columnIndicesSet){
						//columnIndices are set just once per app run
						setColumnHeaderIndices(line);
					}
					
					columnHeaderLineProcessed = true;
				}
			}
	
			else if (!doneParsingProcessCpu && curProcessCount < PROCESS_LIMIT){
				//get process cpu info
				getProcessCpuInfo(content, line);
			}
		} 
	}

	/**
	 * get the process name and cpu percentage from the line outputted by
	 * the 'top' command
	 * @param content
	 * @param line
	 */
	private void getProcessCpuInfo(StringBuffer content, String line) {
		String[] cpuInfoComp = line.split(WHITE_SPACE_REG_EXP);
		
		String processName = "";
		String processCpu = cpuInfoComp[processCpuColumnIndex].replace("%", "");
		
		//workaround for blank column values
		if (cpuInfoComp.length > processNameColumnIndex){
			processName = cpuInfoComp[processNameColumnIndex];
		}
		else {
			processName = cpuInfoComp[cpuInfoComp.length - 1];
		}
		
		if (Integer.parseInt(processCpu) == 0){
			//the top command returns process ordered by cpu usage,
			//we only want non-zero processes, so we can stop when the processCpu is 0.
			doneParsingProcessCpu = true;
		}
		else {
			content.append(processName + "=" + processCpu + " ");
			++curProcessCount;
		}
	}

	/**
	 * need to set the col header indices because different
	 * android devices/platform returns the header in different order
	 * @param line
	 */
	private void setColumnHeaderIndices(String line) {
		String[] colHeaders = line.split(WHITE_SPACE_REG_EXP);
		int colHeaderIndex = 0;
		
		for (int i = 0; i < colHeaders.length; ++i){
			String curHeader = colHeaders[i].trim();
			
			if (curHeader.length() > 0){
				if (curHeader.equalsIgnoreCase(CPU_HEADER)){
					processCpuColumnIndex = colHeaderIndex;
				}
				else if (curHeader.equalsIgnoreCase(PROCESS_NAME_HEADER)){
					processNameColumnIndex = colHeaderIndex;
				}
				
				++colHeaderIndex;
			}
		}
		
		if (processCpuColumnIndex != -1 && processNameColumnIndex != -1){
			columnIndicesSet = true;
		}
		else {
			Log.w(TAG, "could not set processCpuColumnIndex/processNameColumnIndex for line=" + line);
			writeTraceLineToAROTraceFile(mCpuDebugWriter, "could not set processCpuColumnIndex/processNameColumnIndex for line=" + line, false);
		}
	}

	
	/**
	 * check if the line matches the total cpu line.
	 * if yes, parse the cpu line and return true.
	 * Otherwise, return false.
	 * @param content
	 * @param line
	 */
	private boolean parseTotalCpuLine(StringBuffer content, String line) {

		Matcher totalCpuLineMatcher = totalCpuLinePattern.matcher(line);
		
		boolean isTotalCpuLine = totalCpuLineMatcher.find();
		if (isTotalCpuLine){
			//capturing group is numbered starting at 1 from left to right based on placement of opening parenthesis
			//total cpu group = 8, idle cpu group = 4
			
			if (totalCpuLineMatcher.groupCount() == CPU_PATTERN_GROUP_COUNT){
				
				int total = Integer.parseInt(totalCpuLineMatcher.group(TOTAL_CPU_GROUP_NUMBER));
				int idle = Integer.parseInt(totalCpuLineMatcher.group(IDLE_CPU_GROUP_NUMBER));
				int usage = total - idle;
				int usagePercent = usage * 100 / total;
				content.append(usagePercent + " ");
				
			}
			else {
				Log.w(TAG, "totalCpuLineMatcher doesn't return 8 capturing groups for line=" + line);
				writeTraceLineToAROTraceFile(mCpuDebugWriter, "totalCpuLineMatcher doesn't return 8 capturing groups for line= " + line, true);
				isTotalCpuLine = false;
			}
		}
		else if (nonEmptyLineNum == 2){
			//2nd line isnt a parsable total cpu line
			writeTraceLineToAROTraceFile(mCpuDebugWriter, "2nd line is not totalCpuLine; line= " + line, true);
		}
		
		return isTotalCpuLine;
	}
	
	/**
	 * check if the line contains the column header
	 * @param line
	 * @return
	 */
	private boolean isColumnHeaderLine(String line) {
		return line.contains("PID") && line.contains("CPU%") && line.contains("Name");
	}

	/**
	 * method to stop the cpu trace
	 * 1. stop the cpu script
	 * 2. process the remaining cpu files
	 */
	private void stopAROCpuTrace(){
		/*if (cpuTimeOutTimer != null){
			cpuTimeOutTimer.cancel();
			cpuTimeOutTimer = null;
		}*/
		
		/*if (scheduler != null){
			cpuTraceHandle.cancel(false);
			scheduler.shutdown();
		}*/
		try {
			Process sh = null;
			DataOutputStream os = null;
			int pid = getCpuScriptPid();
			
			if (pid != -1) {
				sh = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(sh.getOutputStream());
				String command = "kill -15 " + pid + "\n";
				os.writeBytes(command);
				
				command = "exit\n";
				os.writeBytes(command);
				os.flush();
				
				//clear out the sharedpref
				setCpuScriptPid("-1");
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		if (cpuProcessingTimer != null){
			cpuProcessingTimer.cancel();
			cpuProcessingTimer = null;
		}
		
		//process the directory for any files that hasnt been processed
		processCpuDirectory();
		
		//delete the directory
		File cpuDir = new File(getCpuDirFullPath());
		if (cpuDir.listFiles() != null){
			for (File cpuFile : cpuDir.listFiles()){
				cpuFile.delete();
			}
		}
		cpuDir.delete();

	}

	/** 
	 * Initializes Flurry Event objects
	 */
	private void initializeFlurryObjects() {
		
		//tests need to maintain states
		networkTypeFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_networkType), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		networkInterfaceFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_networkInterface), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		wifiFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_wifi), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		batteryFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_battery), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		gpsFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_gps), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		cameraFlurryEvent= new FlurryEvent(this.getString(R.string.flurry_camera), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		bluetoothFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_bluetooth), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		//log events at end; do not need states
		backgroundAppsFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_backgroundApps), 0, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING); 
		makeModelEvent = new FlurryEvent(this.getString(R.string.flurry_makeModel), 0, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING); 	
	}
	
	/**
	 * Captures the device information
	 * @param mCurrentNetworkType network info class object to get current network type 
	 */
	private void captureDeviceInfo(NetworkInfo mCurrentNetworkType) {
		final String ipAddress;
		final String deviceModel = Build.MODEL;
		final String deviceMake = Build.MANUFACTURER;
		final String osVersion = Build.VERSION.RELEASE;
		final String appVersion = mApp.getVersion();
		try {
			ipAddress = mAroUtils.getLocalIpAddress();
			if (ipAddress != null) {
				writeTraceLineToAROTraceFile(mDeviceInfoWriter, ipAddress, false);
			}
		} catch (SocketException e) {
			Log.e(TAG, "exception in getLocalIpAddress", e);
		}
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter,
				getApplicationContext().getPackageName(), false);
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, deviceModel, false);
		
		mApp.writeToFlurry(makeModelEvent.getMapToWrite(), makeModelEvent.getEventName(), 
				deviceMake + "/" + deviceModel, makeModelEvent.getEventName(), 
				AROCollectorUtils.NOT_APPLICABLE, AROCollectorUtils.EMPTY_STRING);
	
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, deviceMake, false);
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, "android", false);
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, osVersion, false);
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, appVersion, false);
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, Integer.toString(getDeviceNetworkType(mCurrentNetworkType)), false);
		writeTraceLineToAROTraceFile(mDeviceDetailsWriter, Integer.toString(mApp.getDeviceScreenWidth())+"*"+Integer.toString(mApp.getDeviceScreenHeight()), false);
		final String tempNetworkTypeFlurryState = (getifCurrentBearerWifi() ? AROCollectorUtils.NOT_APPLICABLE : mCurrentNetworkType.getSubtypeName());
		writeToFlurryAndMaintainStateAndLogEvent(networkTypeFlurryEvent, this.getString(R.string.flurry_param_status), tempNetworkTypeFlurryState, true);
		
		final String tempNetworkInterfaceFlurryState = getifCurrentBearerWifi() ? "WIFI" : "MOBILE";
		writeToFlurryAndMaintainStateAndLogEvent(networkInterfaceFlurryEvent, this.getString(R.string.flurry_param_status), tempNetworkInterfaceFlurryState, true);
	}

	/**
	 * Stops the ARO-Data Collector peripherals trace collection
	 */
	private void stopARODataTraceCollection() {
		if (DEBUG){
			Log.i(TAG, "stopping aro trace collection");
		}
		stopAROScreenTraceMonitor();
		stopAROGpsTraceMonitor();
		stopAROBatteryLevelMonitor();
		stopARORadioTraceMonitor();
		stopAROBluetoothTraceMonitor();
		stopAROWifiTraceMonitor();
		stopCameraTrace();
		stopARODataBearerChangeNotification();
		stopAroScreenRotationMonitor();
		stopAROCpuTrace();
		try {
			closeAROTraceFile();
		} catch (IOException e) {
			Log.e(TAG, "exception in closeAROTraceFile", e);
		}
		stopARODeviceSDCardSpaceMidTrace();
		stopAROAirplaneModeMidTrace();
	}



	/**
	 * Gets the current connected data network type of device i.e 3G/LTE/Wifi
	 * @param mCurrentNetworkType network info class object to get current network type 
	 * @return mCellNetworkType Current network type
	 */
	private int getDeviceNetworkType(NetworkInfo mCurrentNetworkType) {
		if (DEBUG) {
			Log.i(TAG, "getting device network type" + mCurrentNetworkType);
		}
		final TelephonyManager mAROtelManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mAROtelManager.getNetworkType();
				
		// Check if the current network is WiFi *//
		if (mCurrentNetworkType.getType() == 1) {
			networkType = -1;
		}
		return networkType;
			
	}

	 
	private OutputStream mCpuTraceDebugFile;
	private BufferedWriter mCpuDebugWriter; 
	
	/**
	 * Method will initialize all the trace files Streams and open it for
	 * writing (i.e wifi/ Baterry/Gps trace files etc)
	 * 
	 * @throws FileNotFoundException
	 */
	private void initAROTraceFile() throws FileNotFoundException {

		final String mAroTraceDatapath = mApp.getTcpDumpTraceFolderName();
		if (DEBUG) {
			Log.d(TAG, "mAroTraceDatapath=" + mAroTraceDatapath);
		}
		mWifiTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outWifiFileName ,true);
		mWifiTracewriter = new BufferedWriter(new OutputStreamWriter(mWifiTraceOutputFile));
		mRadioTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outRadioFileName , true);
		mRadioTracewriter = new BufferedWriter(new OutputStreamWriter(mRadioTraceOutputFile));
		mCameraTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outCameraFileName , true);
		mCameraTracewriter = new BufferedWriter(new OutputStreamWriter(mCameraTraceOutputFile));
		mBatteryTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outBatteryFileName , true);
		mBatteryTracewriter = new BufferedWriter(new OutputStreamWriter(mBatteryTraceOutputFile));
		mGPSTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outGPSFileName ,true);
		mGPSTracewriter = new BufferedWriter(new OutputStreamWriter(mGPSTraceOutputFile));
		mScreenOutputFile = new FileOutputStream(mAroTraceDatapath + outScreenFileName,true);
		mNetworkDetailsOutputFile = new FileOutputStream(mAroTraceDatapath + outNetworkDetailsFileName ,true);
		mCpuTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outCpuFileName ,true);
		mCpuTraceDebugFile = new FileOutputStream(mAroTraceDatapath + "cpu_log.txt" ,true);
		
		mScreenRotationOutputFile = new FileOutputStream(mAroTraceDatapath+ outScreenRotationFileName,true);
		mScreenTracewriter = new BufferedWriter(new OutputStreamWriter(mScreenOutputFile));
		mNetworkTracewriter = new BufferedWriter(new OutputStreamWriter(mNetworkDetailsOutputFile));
		mScreenRotationTracewriter = new BufferedWriter(new OutputStreamWriter(mScreenRotationOutputFile));
		mActiveProcessOutputFile = new FileOutputStream(mAroTraceDatapath+ outActiveProcessFileName, true);
		mActiveProcessTracewriter = new BufferedWriter(new OutputStreamWriter(mActiveProcessOutputFile));
		mBluetoohTraceOutputFile = new FileOutputStream(mAroTraceDatapath + outBluetoothFileName, true);
		mBluetoothTracewriter = new BufferedWriter(new OutputStreamWriter(mBluetoohTraceOutputFile));
		mDeviceInfoOutputFile = new FileOutputStream(mAroTraceDatapath + outDeviceInfoFileName, true);
		mDeviceDetailsOutputFile = new FileOutputStream(mAroTraceDatapath+ outDeviceDetailsFileName, false);
		mDeviceInfoWriter = new BufferedWriter(new OutputStreamWriter(mDeviceInfoOutputFile));
		mDeviceDetailsWriter = new BufferedWriter(new OutputStreamWriter(mDeviceDetailsOutputFile));
		mCpuTraceWriter = new BufferedWriter(new OutputStreamWriter(mCpuTraceOutputFile));
		mCpuDebugWriter = new BufferedWriter(new OutputStreamWriter(mCpuTraceDebugFile));

	}

	/**
	 * Method will close all trace file Streams and closes the files.
	 * Should set the writer to null after closing, so that a check to 
	 * verify an open writer can be performed in writeTraceLineToAROTraceFile
	 * 
	 * @throws IOException
	 */
	private void closeAROTraceFile() throws IOException {
		if (mWifiTracewriter != null) {
			mWifiTracewriter.close();
			mWifiTracewriter = null;
			mWifiTraceOutputFile.close();
		}
		if (mRadioTracewriter != null) {
			mRadioTracewriter.close();
			mRadioTracewriter = null;
			mRadioTraceOutputFile.close();
		}
		if (mCameraTracewriter != null) {
			mCameraTracewriter.close();
			mCameraTracewriter = null;
			mCameraTraceOutputFile.close();
		}
		if (mBluetoothTracewriter != null) {

			mBluetoothTracewriter.close();
			mBluetoothTracewriter = null;
			mBluetoohTraceOutputFile.close();
		}
		if (mBatteryTracewriter != null) {
			mBatteryTracewriter.close();
			mBatteryTracewriter = null;
			mBatteryTraceOutputFile.close();
		}
		if (mGPSTracewriter != null) {
			mGPSTracewriter.close();
			mGPSTracewriter = null;
			mGPSTraceOutputFile.close();
		}
		if (mScreenTracewriter != null) {
			mScreenTracewriter.close();
			mScreenTracewriter = null;
			mScreenOutputFile.close();
		}
		if (mScreenRotationTracewriter != null) {
			mScreenRotationTracewriter.close();
			mScreenRotationTracewriter = null;
			mScreenRotationOutputFile.close();
		}
		if (mActiveProcessTracewriter != null) {
			mActiveProcessTracewriter.close();
			mActiveProcessTracewriter = null;
			mActiveProcessOutputFile.close();
		}
		if (mDeviceInfoWriter != null) {
			mDeviceInfoWriter.close();
			mDeviceInfoWriter = null;
			mDeviceInfoOutputFile.close();
		}
		if (mDeviceDetailsWriter != null) {
			mDeviceDetailsWriter.close();
			mDeviceDetailsWriter = null;
			mDeviceDetailsOutputFile.close();
		}
		if (mNetworkTracewriter != null) {
			mNetworkTracewriter.close();
			mNetworkTracewriter = null;
			mNetworkDetailsOutputFile.close();
			
		}
		
		if (mCpuTraceWriter != null){
			mCpuTraceOutputFile.close();
			mCpuTraceWriter.close();
			mCpuTraceWriter = null;
		}
		
		if (mCpuDebugWriter != null){
			mCpuTraceDebugFile.close();
			mCpuDebugWriter.close();
			mCpuDebugWriter = null;
		}
	}


	/**
	 * Notify the SD card error during trace collection and displays the error
	 * dialog by calling Main Activity
	 */
	private void aroSDCardErrorUIUpdate() {
		if (AROCollectorService.getServiceObj() != null) {
			mApp.setMediaMountedMidAROTrace(mAroUtils.checkSDCardMounted());
			AROCollectorService.getServiceObj().requestDataCollectorStop();
		}
	}

	/**
	 * Method write given String message to trace file passed as an argument
	 * outputfilewriter : Name of Trace File writer to which trace has to be
	 * written content : Trace message to be written
	 */
	private void writeTraceLineToAROTraceFile(BufferedWriter outputfilewriter, String content,
			boolean timestamp) {
		try {
			if (outputfilewriter != null){
				
				final String eol = System.getProperty("line.separator");
				if (timestamp) {
					outputfilewriter.write(mAroUtils.getDataCollectorEventTimeStamp() + " " + content + eol);
					outputfilewriter.flush();
				} else {
					outputfilewriter.write(content + eol);
					outputfilewriter.flush();
				}
			}
		} catch (IOException e) {
			// TODO: Need to display the exception error instead of Mid Trace
			// mounted error
			mApp.setMediaMountedMidAROTrace(mAroUtils.checkSDCardMounted());
			if (DEBUG) {
				Log.i(TAG, "Exception in writeTraceLineToAROTraceFile");
				Log.e(TAG, "exception in writeTraceLineToAROTraceFile", e);
			}
		}
	}

	/**
	 * Checks the device SD Card space every seconds if its less than 1 MB to
	 * write traces
	 */
	private void startARODeviceSDCardSpaceMidTrace() {

		checkSDCardSpace.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (mAroUtils.checkSDCardMemoryAvailable() < AROSDCARD_MIN_SPACEKBYTES) {
					aroSDCardErrorUIUpdate();
					checkSDCardSpace.cancel();
					if (DEBUG) {
						Log.i(TAG,
								"startARODeviceSDCardSpaceMidTrace="
										+ mAroUtils.checkSDCardMemoryAvailable());
					}
					return;
				}
			}
		}, SDCARD_TARCE_TIMER_REPATE_TIME, SDCARD_TARCE_TIMER_REPATE_TIME);

	}
	
	/**
	 * Stops the SD Card memory check timer during the trace
	 */
	private void stopARODeviceSDCardSpaceMidTrace() {
		if (checkSDCardSpace != null){
			checkSDCardSpace.cancel();
			checkSDCardSpace = null;
		}

	}
	
	/**
	 * Notify that the Airplane Mode was enabled during trace collection and displays the error
	 * dialog by calling Main Activity
	 */
	private void aroAirplaneModeUIUpdate() {
		if (AROCollectorService.getServiceObj() != null) {
			mApp.setAirplaneModeEnabledMidAROTrace(true);
			if (AROCollectorService.getServiceObj() != null) {
				// Sends the STOP Command to tcpdump socket 
				try {
					//We will sleep for 10 seconds to give time for tcpdump bearer change before request STOP
					//TODO: Need to find async call update here and not sleep
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					
				}
				AROCollectorService.getServiceObj().requestDataCollectorStop();
				mApp.cancleAROAlertNotification();
			}
		}
	}
	
	/**
	 * Checks if Airplane mode has been turned on ever every second.
	 * Only exists the trace if wifi mode is also off.
	 */
	private void startAROAirplaneModeMidTrace() {
		
		final ConnectivityManager mAROConnectiviyMgr;
		mAROConnectiviyMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		checkAirplaneModeEnabled.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				final NetworkInfo.State wifiState = mAROConnectiviyMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
				if( mAroUtils.isAirplaneModeOn(getApplicationContext())
						&& (wifiState == NetworkInfo.State.UNKNOWN || wifiState == NetworkInfo.State.DISCONNECTED)){
					//We should cancel the timer here as we detected Air plane mode was turned on during trace cyle.
					aroAirplaneModeUIUpdate();
					checkAirplaneModeEnabled.cancel();
					if (DEBUG) {
						Log.i(TAG,
								"startAROAirplaneMidMidTrace= Airplane Mode was turned on Mid Trace");
					}
					return;
				}
			}
		}, AIRPLANE_TARCE_TIMER_REPATE_TIME, AIRPLANE_TARCE_TIMER_REPATE_TIME);

	}
	
	/**
	 * Stops the Airplane mode enabled check timer during the trace
	 */
	private void stopAROAirplaneModeMidTrace() {
		if (checkAirplaneModeEnabled != null){
			checkAirplaneModeEnabled.cancel();
			checkAirplaneModeEnabled = null;
		}
	}

	/**
	 * Starts the camera trace collection
	 */
	private void startCameraTrace() {
		checkCameraLaunch.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				final String recentTaskName = getRecentTaskInfo().toLowerCase();
				if (recentTaskName.contains("camera")
						|| checkCurrentProcessStateForGround("camera")) {
					mCameraOn = true;
				} else
					mCameraOn = false;
				if (checkCurrentProcessState("camera"))
					mCameraOn = false;
				if (mCameraOn && !mPrevCameraOn) {
					if (DEBUG)
						Log.i(TAG, "Camera Turned on");
					writeTraceLineToAROTraceFile(mCameraTracewriter, "ON", true);
					writeToFlurryAndMaintainStateAndLogEvent(cameraFlurryEvent, getString(R.string.flurry_param_status), "ON", true);
					mCameraOn = true;
					mPrevCameraOn = true;
				} else if (!mCameraOn && mPrevCameraOn) {
					if (DEBUG)
						Log.i(TAG, "Camera Turned Off");
					writeTraceLineToAROTraceFile(mCameraTracewriter, AroTraceFileConstants.OFF, true);
					writeToFlurryAndMaintainStateAndLogEvent(cameraFlurryEvent, getString(R.string.flurry_param_status), AroTraceFileConstants.OFF, true);
					mCameraOn = false;
					mPrevCameraOn = false;
				}
			}
		}, HALF_SECOND_TARCE_TIMER_REPATE_TIME, HALF_SECOND_TARCE_TIMER_REPATE_TIME);
	}

	/**
	 * Stops the camera trace collection
	 */
	private void stopCameraTrace() {
		if (checkCameraLaunch != null) {
			checkCameraLaunch.cancel();
			checkCameraLaunch = null;
		}
	}

	/**
	 * Screen trace data broadcast receiver
	 */
	private BroadcastReceiver mAROScreenTraceReceiver = new BroadcastReceiver() {

		// Screen state on-off boolean flag
		Boolean mScreenOn = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				mScreenOn = false;
			} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
				mScreenOn = true;
			}
			getScreenBrigthnessTimeout();
			if (mScreenOn) {
				writeTraceLineToAROTraceFile(mScreenTracewriter, AroTraceFileConstants.ON + " "
						+ mScreenTimeout + " " + mScreencurBrightness, true);
				mPrevScreencurBrightness = mScreencurBrightness;
				mPrevScreenTimeout = mScreenTimeout;
			} else {
				writeTraceLineToAROTraceFile(mScreenTracewriter, AroTraceFileConstants.OFF, true);
				mPrevScreencurBrightness = mScreencurBrightness;
				mPrevScreenTimeout = mScreenTimeout;
			}

			if (DEBUG) {
				Log.d(TAG, "Screen brightness: " + mScreencurBrightness);
				Log.d(TAG, "Screen Timeout: " + mScreenTimeout);
			}
		}
	};

	/**
	 * Gets the screen brightness and timeout value from Settings file
	 * 
	 * @throws SettingNotFoundException
	 */
	private void getScreenBrigthnessTimeout() {
		try {
			mScreencurBrightness = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
			if (mScreencurBrightness >= 255)
				mScreencurBrightness = 240;
			// Brightness Min value 15 and Max 255
			mScreencurBrightness = Math.round((mScreencurBrightness / 240) * 100);
			mScreenTimeout = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_OFF_TIMEOUT);
			mScreenTimeout = mScreenTimeout / 1000; // In Seconds
		} catch (SettingNotFoundException e) {
			Log.e(TAG, "exception in getScreenBrigthnessTimeout", e);
		}

	}
	
	
	/**
	 * called by the mAROBearerChangeReceiver and mPhoneStateListener to record:
	 * 		1. bearer change between Wifi-Mobile
	 * 		2. network change between 4G-3G-2G
	 * @param mAROActiveNetworkInfo
	 * @param isNetworkConnected
	 */
	private void recordBearerAndNetworkChange(final NetworkInfo mAROActiveNetworkInfo, final boolean isNetworkConnected){
		
		if (DEBUG){
			Log.d(TAG, "enter recordBearerAndNetworkChange()");
		}
		if (mAROActiveNetworkInfo != null && isNetworkConnected 
				&& getDeviceNetworkType(mAROActiveNetworkInfo) != TelephonyManager.NETWORK_TYPE_UNKNOWN){
			
			String currentBearer = getCurrentBearer();
			final int currentNetworkType = getDeviceNetworkType(mAROActiveNetworkInfo);
			if (DEBUG){
				Log.i(TAG, "mAROActiveNetworkInfo.state=" + mAROActiveNetworkInfo.getState());
				Log.i(TAG, "mAROPrevBearer=" + mAROPrevBearer + "; currentBearer=" + currentBearer);
				Log.i(TAG, "mAROPrevNetworkType=" + mAROPrevNetworkType + "; currentNetworkType=" + currentNetworkType);
			}
			if(!mAROPrevBearer.equals(currentBearer)) {
				//bearer change, signaling a failover
				mAROPrevBearer = currentBearer;
				writeTraceLineToAROTraceFile(mNetworkTracewriter,Integer.toString(currentNetworkType), true);
				
				if (DEBUG){
					Log.i(TAG, "failover, wrote networkType=" + currentNetworkType + " to networkdetails completed at timestamp: " + mAroUtils.getDataCollectorEventTimeStamp());
				}
				mAROPrevNetworkType = currentNetworkType;
				//Flurry logs
				final String tempNetworkTypeFlurryState = (getifCurrentBearerWifi() ? AROCollectorUtils.NOT_APPLICABLE : mAROActiveNetworkInfo.getSubtypeName());
				final String tempNetworkInterfaceFlurryState = getifCurrentBearerWifi() ? "WIFI" : "MOBILE";
				
				writeToFlurryAndMaintainStateAndLogEvent(networkTypeFlurryEvent, getString(R.string.flurry_param_status), tempNetworkTypeFlurryState, true);
				writeToFlurryAndMaintainStateAndLogEvent(networkInterfaceFlurryEvent, getString(R.string.flurry_param_status), tempNetworkInterfaceFlurryState, true);
			}
			//We need to handle case when we switch between 4G-3G-2G ( This is not as handover)
			//-1 - Wifi (We don't want to check for wifi network for 4G-3G-2G transition)
			else if( currentNetworkType != -1 && mAROPrevNetworkType != currentNetworkType){
				writeTraceLineToAROTraceFile(mNetworkTracewriter,Integer.toString(currentNetworkType), true);
				if (DEBUG){
					Log.i(TAG, "4g-3g-2g switch, wrote networkType=" + currentNetworkType + " to networkdetails completed at timestamp: " + mAroUtils.getDataCollectorEventTimeStamp());
				}
				//log the 4G-3G-2G network switch
				final String tempNetworkFlurryState = mAROActiveNetworkInfo.getSubtypeName();
				writeToFlurryAndMaintainStateAndLogEvent(networkTypeFlurryEvent, getString(R.string.flurry_param_status), tempNetworkFlurryState, true);
				
				mAROPrevNetworkType = currentNetworkType;
			}
			// device_details trace file
			if (isFirstBearerChange) {
				captureDeviceInfo(mAROActiveNetworkInfo);
				isFirstBearerChange = false;
			}
		}
		else {
			if (DEBUG){
				Log.i(TAG, "mAROActiveNetworkInfo is null, network is not CONNECTED, or networkType is unknown...exiting recordBearerAndNetworkChange()");
			}
		}
	}
	
	/**
	 * Records the data connection bearer change during the life time of
	 * trace collection 
	 */
	private BroadcastReceiver mAROBearerChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (DEBUG) {
				Log.d(TAG, "entered mAROBearerChangeReceiver ");
			}

			final String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				
				final boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY , false);
				final boolean isNetworkConnected = !noConnectivity;
				
				final ConnectivityManager mAROConnectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				final NetworkInfo mAROActiveNetworkInfo = mAROConnectivityMgr.getActiveNetworkInfo();
				if (!isFirstBearerChange) {
					recordBearerAndNetworkChange(mAROActiveNetworkInfo, isNetworkConnected);
				}
			}
		}
	};

	/**
	 * Wifi trace data broadcast receiver
	 */
	private BroadcastReceiver mAROWifiTraceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				if (DEBUG) {
					Log.d(TAG, "entered WIFI_STATE_CHANGED_ACTION");
				}
				if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
					if (DEBUG) {
						Log.d(TAG, "entered WIFI_STATE_CHANGED_ACTION--DISCONNECTED");
					}
					writeTraceLineToAROTraceFile(mWifiTracewriter,
							AroTraceFileConstants.DISCONNECTED_NETWORK, true);
					
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.DISCONNECTED_NETWORK, true);

				} else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
					if (DEBUG) {
						Log.d(TAG, "entered WIFI_STATE_CHANGED_ACTION--OFF");
					}
					writeTraceLineToAROTraceFile(mWifiTracewriter, AroTraceFileConstants.OFF, true);
					
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.OFF, true);
				}
			}
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

				final NetworkInfo info = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				final NetworkInfo.State state = info.getState();

				switch (state) {

				case CONNECTING:
					writeTraceLineToAROTraceFile(mWifiTracewriter,
							AroTraceFileConstants.CONNECTING_NETWORK, true);
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.CONNECTING_NETWORK, true);
					break;
				case CONNECTED:
					recordAndLogConnectedWifiDetails();					
					break;
				case DISCONNECTING:
					writeTraceLineToAROTraceFile(mWifiTracewriter,
							AroTraceFileConstants.DISCONNECTING_NETWORK, true);
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.DISCONNECTING_NETWORK, true);				
					break;
				case DISCONNECTED:
					writeTraceLineToAROTraceFile(mWifiTracewriter,
							AroTraceFileConstants.DISCONNECTED_NETWORK, true);
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.DISCONNECTED_NETWORK, true);

					break;
				case SUSPENDED:
					writeTraceLineToAROTraceFile(mWifiTracewriter,
							AroTraceFileConstants.SUSPENDED_NETWORK, true);
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.SUSPENDED_NETWORK, true);					
					break;
				case UNKNOWN:
					writeTraceLineToAROTraceFile(mWifiTracewriter,
							AroTraceFileConstants.UNKNOWN_NETWORK, true);
					writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.UNKNOWN_NETWORK, true);					
					break;
				}
			}

		}//

	};

	/**
	 * Bluetooth trace data broadcast receiver
	 */
	private BroadcastReceiver mAROBluetoothTraceReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

				switch (BluetoothAdapter.getDefaultAdapter().getState()) {
				case BluetoothAdapter.STATE_ON:
					writeTraceLineToAROTraceFile(mBluetoothTracewriter,
							AroTraceFileConstants.DISCONNCTED, true);
					writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.DISCONNCTED, true);
					break;

				case BluetoothAdapter.STATE_OFF:
					writeTraceLineToAROTraceFile(mBluetoothTracewriter, AroTraceFileConstants.OFF,
							true);
					writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.OFF, true);
					break;
				}
			}
			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)
					|| BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)
					|| BluetoothDevice.ACTION_FOUND.equals(action)) {

				final BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					writeTraceLineToAROTraceFile(mBluetoothTracewriter,
							AroTraceFileConstants.DISCONNCTED, true);
					writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.DISCONNCTED, true);
				} else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					writeTraceLineToAROTraceFile(mBluetoothTracewriter,
							AroTraceFileConstants.CONNECTED, true);
					writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, getString(R.string.flurry_param_status), 
							AroTraceFileConstants.CONNECTED, true);
				}
			}
		}
	};

	/**
	 * Capture the device radio RSSI(signal strength) during the trace
	 * 
	 */
	private void setARORadioSignalListener() {
		mPhoneStateListener = new PhoneStateListener() {
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);

				// GSM Radio signal strength in integer value which will be
				// converted to dDm (This is default considered network type)
				String mRadioSignalStrength = String.valueOf(0);
				mTelphoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (signalStrength.isGsm() || mTelphoneManager.getNetworkType() == 13) {

					int mLteSignalStrength = 0;
					int mLteRsrp = 0;
					int mLteRsrq = 0;
					int mLteRssnr = 0;
					int mLteCqi = 0;
					if (mTelphoneManager.getNetworkType() == 13) {
						try {
							mLteSignalStrength = Integer.parseInt(mAroUtils
									.getSpecifiedFieldValues(SignalStrength.class, signalStrength,
											"mLteSignalStrength"));
						} catch (NumberFormatException nmb) {
							Log.e(TAG, "mLteSignalStrength not found in LTE Signal Strength");
						}

						try {
							mLteRsrp = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteRsrp"));
						} catch (NumberFormatException nmb) {
							Log.e(TAG, "mLteRsrp not found in LTE Signal Strength");
						}

						try {
							mLteRsrq = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteRsrq"));
						} catch (NumberFormatException nmb) {
							Log.e(TAG, "mLteRsrq not found in LTE Signal Strength");
						}
						try {
							mLteRssnr = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteRssnr"));
						} catch (NumberFormatException nmb) {
							Log.e(TAG, "mLteRssnr not found in LTE Signal Strength");
						}
						try {
							mLteCqi = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteCqi"));
						} catch (NumberFormatException nmb) {
							Log.e(TAG, "mLteCqi not found in LTE Signal Strength");
						}

					}

					// Check to see if LTE parameters are set
					if ((mLteSignalStrength == 0 && mLteRsrp == 0 && mLteRsrq == 0 && mLteCqi == 0)
							|| (mLteSignalStrength == -1 && mLteRsrp == -1 && mLteRsrq == -1 && mLteCqi == -1)) {

						// No LTE parameters set. Use GSM signal strength
						final int gsmSignalStrength = signalStrength.getGsmSignalStrength();
						if (signalStrength.isGsm() && gsmSignalStrength != 99) {
							mRadioSignalStrength = String.valueOf(-113 + (gsmSignalStrength * 2));
						}
					} else {

						// If hidden LTE parameters were defined and not set to
						// default values, then used them
						mRadioSignalStrength = mLteSignalStrength + " " + mLteRsrp + " " + mLteRsrq
								+ " " + mLteRssnr + " " + mLteCqi;
					}
				}
				/**
				 * If the network type is CDMA then look for CDMA signal
				 * strength values.
				 */
				else if ((mTelphoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA)) {

					mRadioSignalStrength = String.valueOf(signalStrength.getCdmaDbm());
				}
				/**
				 * If the network type is EVDO O/A then look for EVDO signal
				 * strength values.
				 */
				else if (mTelphoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0
						|| mTelphoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A) {

					mRadioSignalStrength = String.valueOf(signalStrength.getEvdoDbm());
				}

				if (DEBUG) {
					Log.i(TAG, "signal strength changed to " + mRadioSignalStrength);
				}
				writeTraceLineToAROTraceFile(mRadioTracewriter, mRadioSignalStrength, true);
			}

			//added to listen for 4g-3g-2g transitions
			@Override
			public void onDataConnectionStateChanged (int state, int networkType){
				if (DEBUG) {
					Log.d(TAG, "entered onDataConnectionStateChanged ");
					Log.d(TAG, "state=" + state + "; networkType=" + networkType);
				}
				
				final ConnectivityManager mAROConnectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				final NetworkInfo mAROActiveNetworkInfo = mAROConnectivityMgr.getActiveNetworkInfo();
				
				final boolean isNetworkConnected = (state == TelephonyManager.DATA_CONNECTED);
				if (!isFirstBearerChange) {
					recordBearerAndNetworkChange(mAROActiveNetworkInfo, isNetworkConnected);
				}
				
			}
		};
	}

	/**
	 * Captures the GPS trace data during the trace cycle
	 * 
	 */
	private class GPSStatesListener implements GpsStatus.Listener {

		@Override
		public void onGpsStatusChanged(int event) {

			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				writeTraceLineToAROTraceFile(mGPSTracewriter, "ACTIVE", true);
				writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, getString(R.string.flurry_param_status), "ACTIVE", true);
				mGPSActive = true;
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				writeTraceLineToAROTraceFile(mGPSTracewriter, AroTraceFileConstants.STANDBY, true);
				writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, getString(R.string.flurry_param_status), AroTraceFileConstants.STANDBY, true);
				mGPSActive = false;
				break;
			}
		}
	}

	/**
	 * Checks if the GPS radio is turned on and receiving fix
	 * 
	 * @return boolean value to represent if the location service is enabled or
	 *         not
	 */
	private boolean isLocationServiceEnabled() {
		boolean enabled = false;
		// first, make sure at least one provider actually exists
		final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		final boolean gpsExists = (lm.getProvider(LocationManager.GPS_PROVIDER) != null);
		final boolean networkExists = (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null);
		if (gpsExists || networkExists) {
			enabled = ((!gpsExists || lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) && (!networkExists || lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
		}
		return enabled;
	}

	/**
	 * Starts the GPS peripherals trace collection
	 */
	private void startAROGpsTraceMonitor() {
		mGPSStatesManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mGPSStatesListner = new GPSStatesListener();
		mGPSStatesManager.addGpsStatusListener(mGPSStatesListner);
		
		//write the initial gps state to the trace file
		final boolean initialGpsState = isLocationServiceEnabled();
		writeGpsStateToTraceFile(initialGpsState);
		prevGpsEnabledState = initialGpsState;
		
		checkLocationService.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				// Current GPS enabled state
				final boolean currentGpsEnabledState = isLocationServiceEnabled();
				if (currentGpsEnabledState != prevGpsEnabledState) {
					writeGpsStateToTraceFile(currentGpsEnabledState);
				}
				prevGpsEnabledState = currentGpsEnabledState;
			}
		}, HALF_SECOND_TARCE_TIMER_REPATE_TIME, HALF_SECOND_TARCE_TIMER_REPATE_TIME);
	}
	
	/**
	 * write the gps state to trace file
	 * @param currentGpsEnabledState
	 */
	private void writeGpsStateToTraceFile(final boolean currentGpsEnabledState) {
		if (currentGpsEnabledState) {
			if (DEBUG) {
				Log.d(TAG, "gps enabled: ");
			}
			if (!mGPSActive) {
				writeTraceLineToAROTraceFile(mGPSTracewriter, AroTraceFileConstants.STANDBY, true);
				writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, 
						getString(R.string.flurry_param_status), AroTraceFileConstants.STANDBY, true);
			}
		} else {
			if (DEBUG) {
				Log.d(TAG, "gps Disabled: ");
			}
			writeTraceLineToAROTraceFile(mGPSTracewriter, AroTraceFileConstants.OFF, true);
			writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, 
					getString(R.string.flurry_param_status), AroTraceFileConstants.OFF, true);
		}
	}

	/**
	 * Stop the GPS peripherals trace collection
	 */
	private void stopAROGpsTraceMonitor() {
		if (mGPSStatesListner != null) {
			mGPSStatesManager.removeGpsStatusListener(mGPSStatesListner);
			mGPSStatesManager = null;
		}
		checkLocationService.cancel();
	}

	/**
	 * Starts the device radio trace collection
	 */
	private void startARORadioTraceMonitor() {
		mTelphoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		setARORadioSignalListener();
		mTelphoneManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
				| PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
	}

	/**
	 * Stops the device radio trace collection
	 */
	private void stopARORadioTraceMonitor() {
		if (mPhoneStateListener != null) {
			mTelphoneManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
			mTelphoneManager = null;
			mPhoneStateListener = null;
		}

	}

	/**
	 * Starts the device screen trace collection
	 */
	private void startAROScreenTraceMonitor() {
		mAROIntentFilter = new IntentFilter();
		mAROIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		mAROIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
		checkScreenBrightness.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				getScreenBrigthnessTimeout();
				if ((mScreencurBrightness != mPrevScreencurBrightness)
						|| (mScreenTimeout != mPrevScreenTimeout)) {
					writeTraceLineToAROTraceFile(mScreenTracewriter, AroTraceFileConstants.ON + " "
							+ mScreenTimeout + " " + mScreencurBrightness, true);
					mPrevScreencurBrightness = mScreencurBrightness;
					mPrevScreenTimeout = mScreenTimeout;

				}
			}
		}, HALF_SECOND_TARCE_TIMER_REPATE_TIME, HALF_SECOND_TARCE_TIMER_REPATE_TIME);
		registerReceiver(mAROScreenTraceReceiver, mAROIntentFilter);
	}

	/**
	 * Stop the device screen trace collection
	 */
	private void stopAROScreenTraceMonitor() {
		try {
			if (mAROScreenTraceReceiver != null) {
				unregisterReceiver(mAROScreenTraceReceiver);
				checkScreenBrightness.cancel();
			}
			checkScreenBrightness = null;
			mAROIntentFilter = null;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException at unregister mAROScreenTraceReceiver");
		}
	}

	/**
	 * Starts the bluetooth peripherals trace collection
	 */
	private void startAROBluetoothTraceMonitor() {
		switch (BluetoothAdapter.getDefaultAdapter().getState()) {
		case BluetoothAdapter.STATE_ON:
			if (BluetoothAdapter.getDefaultAdapter().getBondedDevices().isEmpty()) {
				writeTraceLineToAROTraceFile(mBluetoothTracewriter,
						AroTraceFileConstants.DISCONNCTED, true);
				writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, this.getString(R.string.flurry_param_status), 
						AroTraceFileConstants.DISCONNCTED, true);
			} else {
				writeTraceLineToAROTraceFile(mBluetoothTracewriter,
						AroTraceFileConstants.CONNECTED, true);
				writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, this.getString(R.string.flurry_param_status), 
						AroTraceFileConstants.CONNECTED, true);
			}
			break;

		case BluetoothAdapter.STATE_OFF:
			writeTraceLineToAROTraceFile(mBluetoothTracewriter, AroTraceFileConstants.OFF, true);
			writeToFlurryAndMaintainStateAndLogEvent(bluetoothFlurryEvent, this.getString(R.string.flurry_param_status), 
					AroTraceFileConstants.OFF, true);
			break;
		}

		mAROBluetoothIntentFilter = new IntentFilter();
		mAROBluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mAROBluetoothIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		mAROBluetoothIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mAROBluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mAROBluetoothTraceReceiver, mAROBluetoothIntentFilter);

	}

	/**
	 * Stops the bluetooth peripherals trace collection
	 */
	private void stopAROBluetoothTraceMonitor() {
		try {
			if (mAROBluetoothTraceReceiver != null) {
				unregisterReceiver(mAROBluetoothTraceReceiver);
				mAROBluetoothIntentFilter = null;
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException at unregister mAROBluetoothTraceReceiver");
		}
		
	}

	/**
	 * Gets the current connected bearer
	 * 
	 * @return boolean value to validate if current bearer is wifi
	 */
	private Boolean getifCurrentBearerWifi() {
		int type = 0;
		if (mConnectivityManager == null)
			return false;
		if (mConnectivityManager.getActiveNetworkInfo() != null) {
			type = mConnectivityManager.getActiveNetworkInfo().getType();
		}
		if (type == ConnectivityManager.TYPE_MOBILE) {
			if (DEBUG) {
				Log.i(TAG, " Connection Type :  Mobile");
			}
			return false;
		} else {
			if (DEBUG) {
				Log.i(TAG, " Connection Type :  Wifi");
			}
			return true;
		}
	}
	
	/**
	 * returns the value of the current bearer, either WIFI or MOBILE
	 */
	private String getCurrentBearer(){
		
		return getifCurrentBearerWifi() ? "WIFI" : "MOBILE";
	}

	/**
	 * Collects the wifi network trace data
	 */
	private void collectWifiNetworkData() {
		/* Get WiFi status
		 * DE9556: removed getifCurrentBearerWifi() call from if condition because when this function is called, 
		 * the wifi is already connected
		 */
		if (mWifiManager != null) {
			mWifiMacAddress = mWifiManager.getConnectionInfo().getBSSID();
			mWifiNetworkSSID = mWifiManager.getConnectionInfo().getSSID();
			mWifiRssi = mWifiManager.getConnectionInfo().getRssi();
			
			if (DEBUG){
				Log.d(TAG, "mWifiMac=" + mWifiMacAddress + ", ssid=" + mWifiNetworkSSID + ", rssi:" + mWifiRssi);
			}
		}
	}

	/**
	 * Starts the bearer change notification broadcast
	 */
	private void startARODataBearerChangeNotification() {
		mAROIntentFilter = new IntentFilter();
		mAROIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mAROBearerChangeReceiver, mAROIntentFilter);
		recordInitialBearerInfo();
	}

	/**
	 * Stops the bearer change notification broadcast
	 */
	private void stopARODataBearerChangeNotification() {
		try {
			if (mAROBearerChangeReceiver != null){
				unregisterReceiver(mAROBearerChangeReceiver);
				mAROBearerChangeReceiver = null;
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException at unregister mAROBearerChangeReceiver");
		}
	}

	/**
	 * Starts the wifi trace collection
	 */
	private void startAROWifiTraceMonitor() {
		IntentFilter mAROWifiIntentFilter;
		// Setup WiFi
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mAROWifiIntentFilter = new IntentFilter();
		mAROWifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mAROWifiIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		mAROWifiIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mAROWifiIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mAROWifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mAROWifiTraceReceiver, mAROWifiIntentFilter);

	}

	/**
	 * Stops the wifi trace collection
	 */
	private void stopAROWifiTraceMonitor() {
		try {
			if (mAROWifiTraceReceiver != null) {
				unregisterReceiver(mAROWifiTraceReceiver);
				mWifiManager = null;
			}
			mConnectivityManager = null;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException at unregister mAROWifiTraceReceiver");
		}
		
	}

	/**
	 * Gets the recent opened package name
	 * 
	 * @return recent launched package name
	 */
	private String getRecentTaskInfo() {
		/** Package name of recent launched application */
		String mLastLaucnhedProcess = " ";
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final List<?> l = mActivityManager.getRecentTasks(5, ActivityManager.RECENT_WITH_EXCLUDED);
		final RecentTaskInfo rti = (RecentTaskInfo) l.get(0);
		if (!mLastLaucnhedProcess.equalsIgnoreCase(rti.baseIntent.getComponent().getPackageName())
				&& !rti.baseIntent.getComponent().getPackageName()
						.equalsIgnoreCase("com.att.android.arodatacollector.main")) {
			if (DEBUG)
				Log.i(TAG, "New Task=" + rti.baseIntent.getComponent().getPackageName());
			mLastLaucnhedProcess = rti.baseIntent.getComponent().getPackageName();
			return mLastLaucnhedProcess;
		}
		mLastLaucnhedProcess = rti.baseIntent.getComponent().getPackageName();
		return mLastLaucnhedProcess;

	}

	/**
	 * Starts the active process trace by logging all running process in the
	 * trace file
	 */
	private void startAROActiveProcessTrace() {
		// mActiveProcessStates //
		String[] mActiveProcessStates;
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActiveProcessprocess = mActivityManager.getRunningAppProcesses();
		mActiveProcessStates = new String[mActiveProcessprocess.size()];
		for (final Iterator<RunningAppProcessInfo> iterator = mActiveProcessprocess.iterator(); iterator
				.hasNext();) {
			final RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) iterator
					.next();
			final int pImportance = runningAppProcessInfo.importance;
			int Index = 0;
			switch (pImportance) {

			case RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
				mActiveProcessStates[Index] = "Name:" + runningAppProcessInfo.processName
						+ " State:" + AroTraceFileConstants.IMPORTANCE_BACKGROUND;
				writeTraceLineToAROTraceFile(mActiveProcessTracewriter,
						mActiveProcessStates[Index], true);

				//Flurry only allows max of 10 parameters to an event; if exceed, event is not logged.
				if (backgroundAppsFlurryEvent.getCounter() < 10) {
				mApp.writeToFlurry(backgroundAppsFlurryEvent.getMapToWrite(), runningAppProcessInfo.processName, 
						AROCollectorUtils.EMPTY_STRING + backgroundAppsFlurryEvent.incrementCounter(), 
						backgroundAppsFlurryEvent.getEventName(), AROCollectorUtils.NOT_APPLICABLE, AROCollectorUtils.EMPTY_STRING);
				}

				Index++;
				break;

			case RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
				mActiveProcessStates[Index] = "Name:" + runningAppProcessInfo.processName
						+ " State:" + AroTraceFileConstants.IMPORTANCE_FOREGROUND;
				writeTraceLineToAROTraceFile(mActiveProcessTracewriter, mActiveProcessStates[Index], true);
				Index++;
				break;
			}
		}
	}

	/**
	 * Checks the state of process is background
	 * 
	 * @param process
	 * 
	 *            name
	 * @return boolean value to represent the if package state is background
	 */
	private boolean checkCurrentProcessState(String processname) {
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActiveProcessprocess = mActivityManager.getRunningAppProcesses();
		for (final Iterator<RunningAppProcessInfo> iterator = mActiveProcessprocess.iterator(); iterator
				.hasNext();) {
			final RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) iterator
					.next();
			final String pSname = runningAppProcessInfo.processName.toLowerCase();
			final int pImportance = runningAppProcessInfo.importance;
			if (pSname.contains(processname.toLowerCase()) && !pSname.contains(":")) {
				switch (pImportance) {
				case RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks the state of process is foreground
	 * 
	 * @param process
	 *            name
	 * 
	 * @return boolean value to represent the if package state is foreground
	 */
	private boolean checkCurrentProcessStateForGround(String processname) {
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActiveProcessprocess = mActivityManager.getRunningAppProcesses();
		for (final Iterator<RunningAppProcessInfo> iterator = mActiveProcessprocess.iterator(); iterator
				.hasNext();) {
			final RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) iterator
					.next();
			final String pSname = runningAppProcessInfo.processName.toLowerCase();
			final int pImportance = runningAppProcessInfo.importance;
			if (pSname.contains(processname.toLowerCase())) {
				switch (pImportance) {
				case RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Method will get Current power source state and Battery level for the
	 * device s * @param intent
	 */
	private void getPowerSourceStateandBatteryLevel(Intent intent) {
		// AC Power Source boolean flag
		Boolean mPowerSource = false;
		/** Battery level */
		int mBatteryLevel = 0;
		// Battery temperature //
		int mBatteryTemp;
		int status = -1;
		final String action = intent.getAction();
		mBatteryTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				status = extras.getInt(BatteryManager.EXTRA_PLUGGED, -1);
				final int rawlevel = intent.getIntExtra("level", -1);
				final int scale = intent.getIntExtra("scale", -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				mBatteryLevel = level;
			}
			if (status != -1) {
				switch (status) {
				
				case 0: //USB Unplugged
					if(mApp.isCollectorLaunchfromAnalyzer()){
						Log.i(TAG, "usb disconnected, set dataCollectorStopEnable to true");
						mApp.setDataCollectorStopEnable(true);
						broadcastUsbAction(true);
					}
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					mPowerSource = true;
					break;
				case BatteryManager.BATTERY_PLUGGED_AC:
					mPowerSource = true;
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					mPowerSource = false;
				default:
					mPowerSource = false;
					break;
				}
			}
		}
		if (DEBUG) {
			Log.d(TAG, "received battery level: " + mBatteryLevel);
			Log.d(TAG, "received battery temp: " + mBatteryTemp / 10 + "C");
			Log.d(TAG, "received power source " + mPowerSource);
		}
		writeTraceLineToAROTraceFile(mBatteryTracewriter, mBatteryLevel + " " + mBatteryTemp / 10
				+ " " + mPowerSource, true);
		
		//write to Flurry only if the values change
		final String tempBatteryString = "level: " + mBatteryLevel + "%" + " " + 
				   "temp: " + mBatteryTemp / 10 + "C" + " " + 
				   "power source: " + mPowerSource;
		writeToFlurryAndMaintainStateAndLogEvent(batteryFlurryEvent, this.getString(R.string.flurry_param_status), 
				tempBatteryString, true);
	}	

	/**
	 * 
	 * @param CollectorStopEnable
	 */
	private void broadcastUsbAction(boolean collectorStopEnable) {
		if (DEBUG) {
			Log.i(TAG, "broadcasting usbAction, collectorStopEnable:"
					+ collectorStopEnable);
		}
		usbBroadcastIntent.putExtra(USB_ACTION_EXTRA_KEY, collectorStopEnable);
    	sendBroadcast(usbBroadcastIntent);
	}


	/**
	 * Starts the battery trace
	 */
	private void startAROBatteryLevelMonitor() {
		if (mBatteryLevelReceiver == null) {
			mBatteryLevelReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					getPowerSourceStateandBatteryLevel(intent);

				}
			};
		}
		if (mBatteryLevelReceiver != null) {
			registerReceiver(mBatteryLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	}

	/**
	 * Stop the battery trace
	 */
	private void stopAROBatteryLevelMonitor() {
		try {
			if (mBatteryLevelReceiver != null) {
				unregisterReceiver(mBatteryLevelReceiver);
				mBatteryLevelReceiver = null;
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException at unregister mBatteryLevelReceiver");
		}
	}

	/**
	 * method to record the screen rotation. Called when:
	 * 1. on trace start to record initial rotation
	 * 2. during trace, upon screen rotation changes
	 */
	private void recordScreenRotation() {
		final Configuration newConfig = getResources().getConfiguration();
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			writeTraceLineToAROTraceFile(mScreenRotationTracewriter,LANDSCAPE_MODE, true);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			writeTraceLineToAROTraceFile(mScreenRotationTracewriter, PORTRAIT_MODE,
					true);
		}
	}
	
	/**
	 * Creates and registers the broad cast receiver that listens for the screen
	 * rotation and and writes the screen rotation time to the
	 * "screen_rotations" file.
	 */
	private void startAroScreenRotationMonitor() {
		
		//record the initial screen rotation - uncomment to capture initial state.
		//recordScreenRotation();
		
		if (mScreenRotationReceiver == null) {

			mScreenRotationReceiver = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {

						recordScreenRotation();
					}
				}
			};
		}
		registerReceiver(mScreenRotationReceiver, new IntentFilter(
				Intent.ACTION_CONFIGURATION_CHANGED));

	}

	/**
	 * Unregisters the screen rotation broadcast receiver.
	 */
	private void stopAroScreenRotationMonitor() {
		try {
			if (mScreenRotationReceiver != null){
				unregisterReceiver(mScreenRotationReceiver);
				mScreenRotationReceiver = null;
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "IllegalArgumentException at unregister mScreenRotationReceiver");
		}
	}

	/**
	 * Handles processing when an AROCollectorTraceService object is binded to
	 * content. Overrides the android.app.Service#onBind method.
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void writeToFlurryAndMaintainState(FlurryEvent fe, String key, String currentValue) {
		mApp.writeToFlurry(fe.getMapToWrite(), key, 
				currentValue, fe.getEventName(), fe.getState(), currentValue );
		if (!fe.getState().equals(currentValue)) {
			fe.setState(currentValue);
			if (DEBUG) {
				Log.d(TAG, "writeToFlurryAndMaintainState()-flurry state updated to: " + fe.getState());
			}
		}
	}
	
	/**
	 * Used by peripheral usage devices where a change in the Flurry state tracked will trigger logging of a Flurry event.
	 * Not used to log time.
	 * 
	 * @param fe Name of a Flurry event.
	 * @param key Map key of an event.
	 * @param currentValue Current state value of the event.  If it compares differently than the former state, 
	 * then log the event and becomes the current state.
	 * @param timeStamp Log time stamp as an event parameter if true.  Do not log time stamp if false.
	 */
	public void writeToFlurryAndMaintainStateAndLogEvent(FlurryEvent aFlurryEvent, String key, String currentValue, boolean timeStamp) {
		if (aFlurryEvent.getMapToWrite() != null) {
			if (timeStamp) {
				mApp.writeToFlurry(aFlurryEvent.getMapToWrite(), getString(R.string.flurry_param_time), 
						Calendar.getInstance().getTime().toString(), aFlurryEvent.getEventName(), AROCollectorUtils.NOT_APPLICABLE, AROCollectorUtils.EMPTY_STRING);
			}
			mApp.writeToFlurry(aFlurryEvent.getMapToWrite(), key, 
					currentValue, aFlurryEvent.getEventName(), aFlurryEvent.getState(), currentValue );
			if (!aFlurryEvent.getState().equals(currentValue)) {
				aFlurryEvent.setState(currentValue);
				if (DEBUG) {
					Log.d(TAG, "writeToFlurryAndMaintainStateAndLogEvent()-flurry state updated to: " + aFlurryEvent.getState() + 
							" and logged now");
				}
				FlurryAgent.logEvent(aFlurryEvent.getEventName(), aFlurryEvent.getMapToWrite());

				//reset hashmap status after logging event
				aFlurryEvent.setMapToWrite(new HashMap<String, String>());
			}
		} else {//hashmap is empty-new map is ready-log will be empty 
			if (DEBUG) {
				Log.d(TAG, "writeToFlurryAndMaintainStateAndLogEvent()-did not log-map is null-Event: " + 
						aFlurryEvent.getEventName() + "-key: " + key + "-value: " + currentValue);
			}
		}
	}
	
	/**
	 * record the bearer and network info at the start
	 */
	private void recordInitialBearerInfo(){
		
		final ConnectivityManager mAROConnectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mAROActiveNetworkInfo = mAROConnectivityMgr.getActiveNetworkInfo();
		
		boolean isConnected = false;
		if (mAROActiveNetworkInfo != null){
			isConnected = mAROActiveNetworkInfo.isConnected();
		}
		
		if (DEBUG){
			Log.d(TAG, "recordInitialBearerInfo: isConnected=" + isConnected + "; currentBearerWifi=" + getifCurrentBearerWifi());
		}
		//call to record the initial bearer
		recordBearerAndNetworkChange(mAROActiveNetworkInfo, isConnected);
		//log the wifi network details if current bearer is wifi
		if (getifCurrentBearerWifi()){
			recordAndLogConnectedWifiDetails();
		}
	}

	/**
	 * record the connected wifi information
	 */
	private void recordAndLogConnectedWifiDetails() {
		collectWifiNetworkData();
		writeTraceLineToAROTraceFile(mWifiTracewriter,
				AroTraceFileConstants.CONNECTED_NETWORK + " " + mWifiMacAddress + " "
				+ mWifiRssi + " " + mWifiNetworkSSID, true);
		
		if (DEBUG){
			Log.i(TAG, "connected to " + mWifiNetworkSSID + " write to mWifiTracewriter completed at timestamp: " + mAroUtils.getDataCollectorEventTimeStamp());
		}
		
		writeToFlurryAndMaintainStateAndLogEvent(wifiFlurryEvent, getString(R.string.flurry_param_status), 
				AroTraceFileConstants.CONNECTED_NETWORK, true);
	}
}
