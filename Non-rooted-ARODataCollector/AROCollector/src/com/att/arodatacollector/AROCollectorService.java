package com.att.arodatacollector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.att.arocollector.utils.AROCollectorUtils;

/**
 * This Service is for monitoring activity on a non rooted Android device
 * 
 * @author Barry Nelson
 * 
 */
public class AROCollectorService extends Service {

	/** A string for logging an ARO Data Collector service. */
	public static final String TAG = "AROCollectorService_SRV";
	
	public static final String ARO_COLLECTOR_SERVICE = "com.att.arocollector.ARO_COLLECTOR_SERVICE";

	/**
	 * The name of the device_info file
	 */
	public static final String DEVICEINFO_FILE = "device_info";

	/**
	 * The name of the device_info file
	 */
	public static final String DEVICEDETAILS_FILE = "device_details";

	private int maxNum = 500;
	private File traceDir;

	private ArrayList<String> missingFiles;

	private ArrayList<InetAddress> localIPAddresses;

	private DeviceDetails deviceInfo;

	/** ARO Data Collector utilities class object */
	private AROCollectorUtils mAroUtils;

	/** Event names, counters, maps, states for displaying and storing on Flurry Analytics */
	/*
	public static FlurryEvent bluetoothFlurryEvent = null;
	public static FlurryEvent networkTypeFlurryEvent = null;
	public static FlurryEvent networkInterfaceFlurryEvent = null;
	public static FlurryEvent wifiFlurryEvent = null;
	public static FlurryEvent batteryFlurryEvent = null;
	public static FlurryEvent gpsFlurryEvent = null;
	public static FlurryEvent cameraFlurryEvent = null;

	public static FlurryEvent backgroundAppsFlurryEvent = null; // log Flurry event at end of trace
	public static FlurryEvent makeModelEvent = null; // log Flurry event at end of trace
*/
	/** Output stream and Buffer Writer for peripherals traces files */

	private OutputStream mActiveProcessOutputFile;
	private BufferedWriter mActiveProcessTraceWriter;;
	private OutputStream mBatteryTraceOutputFile;
	private BufferedWriter mBatteryTraceWriter;
	private AROBatteryReceiver receiverBattery;
	private OutputStream mBluetoohTraceOutputFile;
	private BufferedWriter mBluetoothTraceWriter;;
	private OutputStream mCameraTraceOutputFile;
	private BufferedWriter mCameraTraceWriter;;
	private OutputStream mCpuTraceOutputFile;
	private BufferedWriter mCpuTraceWriter;;
	private OutputStream mDeviceDetailsOutputFile;
	private BufferedWriter mDeviceDetailsWriter;;
	private OutputStream mDeviceInfoOutputFile;
	private BufferedWriter mDeviceInfoWriter;;
	private OutputStream mGPSTraceOutputFile;
	private BufferedWriter mGPSTraceWriter;;
	private OutputStream mNetworkDetailsOutputFile;
	private BufferedWriter mNetworkTraceWriter;;
	private OutputStream mRadioTraceOutputFile;
	private BufferedWriter mRadioTraceWriter;;
	private OutputStream mScreenOutputFile;
	private BufferedWriter mScreenRotationTraceWriter;
	private AROScreenRotationReceiver receiverScreenRotation;
	private OutputStream mScreenRotationOutputFile;
	private BufferedWriter mScreenTraceWriter;
	private AROScreenTraceReceiver receiverAROScreenTrace;
	private OutputStream mWifiTraceOutputFile;
	private BufferedWriter mWifiTraceWriter;

	private ARONetworkdDetailsReceiver receiverNetworkDetails;

	private Intent intent;

	private AROWifiEventsReceiver receiverWifiEvents;

	private AROBluetoothReceiver receiverBluetoothEvents;;

	/**
	 * Initialize receivers
	 */
	private void initReceivers() {
		Context context = getApplicationContext();
		Log.i(TAG, "initReceivers()");

		// ScreenTrace
		String outScreenTraceFileName = "screen_events";
		try {
			IntentFilter filterAROScreenTrace = new IntentFilter();
			filterAROScreenTrace.addAction(Intent.ACTION_SCREEN_OFF);
			filterAROScreenTrace.addAction(Intent.ACTION_SCREEN_ON);
			receiverAROScreenTrace = new AROScreenTraceReceiver(context, traceDir, outScreenTraceFileName, mAroUtils);
			registerReceiver(receiverAROScreenTrace, filterAROScreenTrace);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create :" + outScreenTraceFileName);
		}

		// ScreenRotation
		String outScreenRotationFileName = "screen_rotations";
		try {
			receiverScreenRotation = new AROScreenRotationReceiver(context, traceDir, outScreenRotationFileName, mAroUtils);
			registerReceiver(receiverScreenRotation, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create :" + outScreenRotationFileName);
		}

		// Battery
		String outBatteryFileName = "battery_events";
		try {
			receiverBattery = new AROBatteryReceiver(context, traceDir, outBatteryFileName, mAroUtils);
			registerReceiver(receiverBattery, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create :" + outBatteryFileName);
		}
		
		// Network (cellular internet)
		String outNetworkDetailsFileName = "network_details";		
		try {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			receiverNetworkDetails = new ARONetworkdDetailsReceiver(context, traceDir, outNetworkDetailsFileName, mAroUtils);
			registerReceiver(receiverNetworkDetails, intentFilter);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create :" + outNetworkDetailsFileName);
		}
		
		// Wifi
		String outWifiEventsFileName = "wifi_events";		
		try {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
			intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			receiverWifiEvents = new AROWifiEventsReceiver(context, traceDir, outWifiEventsFileName, mAroUtils);
			registerReceiver(receiverWifiEvents, intentFilter);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create :" + outWifiEventsFileName);
		}
		
		// Bluetooth
		String outBluetoothFileName = "bluetooth_events";		
		try {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
			intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
			receiverBluetoothEvents = new AROBluetoothReceiver(context, traceDir, outBluetoothFileName, mAroUtils);
			registerReceiver(receiverBluetoothEvents, intentFilter);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to create :" + outBluetoothFileName);
		}
		

	}

	/**
	 * Release all receivers
	 */
	private void releaseReceivers() {
		Log.i(TAG, "releaseReceivers()");
		if (receiverAROScreenTrace != null ) unregisterReceiver(receiverAROScreenTrace); receiverAROScreenTrace.closeTraceFile();
		if (receiverScreenRotation != null ) unregisterReceiver(receiverScreenRotation); receiverScreenRotation.closeTraceFile();
		if (receiverBattery        != null ) unregisterReceiver(receiverBattery);        receiverBattery.closeTraceFile();
		if (receiverNetworkDetails != null ) unregisterReceiver(receiverNetworkDetails); receiverNetworkDetails.closeTraceFile();
		if (receiverWifiEvents     != null ) unregisterReceiver(receiverWifiEvents);     receiverWifiEvents.closeTraceFile();
		if (receiverBluetoothEvents!= null ) unregisterReceiver(receiverBluetoothEvents);receiverBluetoothEvents.closeTraceFile();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand(...)");
		this.intent = intent;

		if (mAroUtils == null) {
			mAroUtils = new AROCollectorUtils();
			initLists();
			initFiles(intent);
			initReceivers();
			if (receiverNetworkDetails != null) {
				deviceInfo = new DeviceDetails();
				setDeviceDetails();
			}

			initializeFlurryObjects();

			getRunningApplications();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean stopService(Intent name) {
		Log.i(TAG, "stopService()");
		return super.stopService(name);
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		releaseReceivers();
		super.onDestroy();
	}

	private void initLists() {
		missingFiles = new ArrayList<String>();
		localIPAddresses = new ArrayList<InetAddress>();

	}

	/**
	 * Collect all info for the device_details file
	 * the toString() method is formatted for direct output to the device_detail file
	 * @author barrynelson 
	 *
	 */
	class DeviceDetails {

		private String packageName;
		private String deviceModel;
		private String deviceMake;
		private String platform;
		private String osRelease;
		private String collectorVersion;
		private String windowDimensions;
		private String networkType;

		DeviceDetails() {
			packageName = getPackageName();
			deviceModel = Build.MODEL.toString();
			deviceMake = Build.MANUFACTURER.toString();
			platform = "android";
			osRelease = android.os.Build.VERSION.RELEASE;

			try {
				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				collectorVersion = pInfo.versionName.toString();
			} catch (NameNotFoundException e) {
				Log.i(TAG, "PackageInfo Exception:" + e.getMessage());
				e.printStackTrace();
			}

			networkType = Integer.toString(receiverNetworkDetails.getDeviceNetworkType());

			WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			int width = metrics.widthPixels;
			int height = metrics.heightPixels;
			windowDimensions = new String("" + width + "*" + height);

		}

		@Override
		public String toString() {
			return packageName + "\n" 			// 1: com.att.android.arodatacollector
					+ deviceModel + "\n"		// 2: HTC One X
					+ deviceMake + "\n"			// 3: HTC
					+ platform + "\n"			// 4: android
					+ osRelease + "\n"			// 5: 4.0.4
					+ collectorVersion + "\n"	// 6: 3.1.1.6
					+ networkType + "\n"		// 7: 10
					+ windowDimensions + "\n"	// 8: 720*1184
			;
		}
	}

	/**
	 * read Intent for trace directory
	 * @param intent
	 */
	private void initFiles(Intent intent) {
		if (intent != null) {
			Log.i(TAG, "initFiles(Intent " + intent.toString() + ") hasExtras = " + intent.getExtras());
			String traceDirStr = intent.getStringExtra("TRACE_DIR");
			traceDir = new File(traceDirStr);
			traceDir.mkdir();
		} else{
			Log.i(TAG, "intent is null");
		}
	}

	private void getRunningApplications() {
		ActivityManager activity_manager = (ActivityManager) getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
		List<RunningTaskInfo> recentTasksList = activity_manager.getRunningTasks(maxNum);
		List<RunningAppProcessInfo> recentApplicationsList = activity_manager.getRunningAppProcesses();
		List<RunningServiceInfo> runningServiceList = activity_manager.getRunningServices(maxNum);

		Log.i(TAG, "recentTasksList ");
		for (RunningTaskInfo recentTask : recentTasksList) {
			// Log.i(TAG, "Running Task " + recentTass.getClass().getCanonicalName());
			Log.i(TAG, "Running Task " + recentTask.baseActivity);
		}

		// Log.i(TAG, "recentApplicationsList ");
		// for (RunningAppProcessInfo recentApplication : recentApplicationsList) {
		// Log.i(TAG, "Running Task " + recentApplication.processName);
		// }
		//
		// Log.i(TAG, "runningServiceList ");
		// for (RunningServiceInfo runningService : runningServiceList) {
		// //if (runningService.clientPackage != null) Log.i(TAG, "----Running Task " + runningService.clientPackage);
		// //Log.i(TAG, "Running Task " + runningService.clientLabel);
		// Log.i(TAG, "Running Task " + runningService.process);
		// }
	}

	/**
	 * get the device name (manufacturer + model)
	 * 
	 * @return device manufacturer and model in lower case
	 */
	private String getDeviceName() {
		String manufacturer = Build.MANUFACTURER.toLowerCase();
		String model = Build.MODEL.toLowerCase();
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer + " " + model;
		}
	}

	/**
	 * Create deviceinfo file store info
	 */
	private void setDeviceDetails() {
		Log.i(TAG, "setDeviceDetails()");
		File file = new File(traceDir, DEVICEDETAILS_FILE);
		Log.i(TAG, "create file:" + file.getAbsolutePath());
		try {
			file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(deviceInfo.toString());
			bw.close();
		} catch (IOException e) {
			Log.i(TAG, "setDeviceDetails() Exception:" + e.getMessage());
			e.printStackTrace();
			return;
		}

	}

	/**
	 * Reads a device Info from the device file in trace folder.
	 * 
	 * @throws IOException
	 */
	private void readDeviceDetails() throws IOException {

		File file = new File(traceDir, DEVICEDETAILS_FILE);
		if (!file.exists()) {
			this.missingFiles.add(DEVICEDETAILS_FILE);
			return;
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {

			String s;
			while ((s = br.readLine()) != null) {

				// In case of IPv6 scoped address, remove scope ID
				int i = s.indexOf('%');
				localIPAddresses.add(InetAddress.getByName(i >= 0 ? s.substring(0, i) : s));
			}

		} finally {
			br.close();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Initializes Flurry Event objects
	 */
	private void initializeFlurryObjects() {

		// tests need to maintain states
		/*
		networkTypeFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_networkType), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		networkInterfaceFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_networkInterface), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		wifiFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_wifi), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		batteryFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_battery), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		gpsFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_gps), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		cameraFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_camera), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		bluetoothFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_bluetooth), -1, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		// log events at end; do not need states
		backgroundAppsFlurryEvent = new FlurryEvent(this.getString(R.string.flurry_backgroundApps), 0, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		makeModelEvent = new FlurryEvent(this.getString(R.string.flurry_makeModel), 0, new HashMap<String, String>(), AROCollectorUtils.EMPTY_STRING);
		*/
	}

	/** Broadcast receiver for Batter events */
	private BroadcastReceiver mBatteryLevelReceiver;

}
