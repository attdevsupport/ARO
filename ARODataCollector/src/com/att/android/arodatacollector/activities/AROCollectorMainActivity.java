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

package com.att.android.arodatacollector.activities;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog.Dialog_CallBack_Error;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog.Dialog_Type;
import com.att.android.arodatacollector.main.AROCollectorService;
import com.att.android.arodatacollector.main.AROCollectorTraceService;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.att.android.arodatacollector.utils.AROLogger;

/**
 * Represents the Landing screen, which is the main UI screen of the ARO Data
 * Collector. The Landing screen contains controls for starting an ARO Data
 * Collector trace, and opening the Task Killer.
 */

public class AROCollectorMainActivity extends Activity {

	/** Log TAG string for ARO-Data Collector main landing Screen */
	private static final String TAG = "ARO.MainActivity";

	/**
	 * The tcpdump start time value which is set to 15 seconds. The timer will
	 * wait for 15 seconds to check if tcpdump is kicked off in device native
	 * shell
	 */
	private static final int ARO_START_WATCH_TIME = 15000;

	/** The tcpdump start timer tick time every second */
	private static final int ARO_START_TICK_TIME = 1000;

	/**
	 * The value to check SD Card minimum space before start of ARO-Data
	 * Collector 5 MB Minimum Space required to start the ARO-Data Collector
	 * Trace
	 */
	private static final int AROSDCARD_MIN_SPACEBYTES = 5120;

	/** Integer identifier to set handler case to navigate to home screen **/
	private static final int NAVIGATE_HOME_SCREEN = 0;

	/**
	 * The Application context of the ARO-Data Collector to access the
	 * application data
	 **/
	private ARODataCollector mApp;

	/** Android log TAG string for ARO-Data Collector Splash Screen */
	private Button startDataCollector;

	/**
	 * GUI checkbox component to enable video on/off before start of trace cycle
	 */
	private CheckBox collectScreenVideo;

	/** ARO Data Collector utilities class object */
	private AROCollectorUtils mAroUtils;

	/**
	 * ARO Data Collector GUI Dialog type object to enable error and information
	 * dialog to user
	 */
	private Dialog_Type m_dialog;
	
	private String mAROTraceFolderNamefromAnalyzer;

	/**
	 * Initializes data members with a saved instance of an AROCollectorMainActivity 
	 * object. Overrides the android.app.Activity#onCreate method. 
	 * @param savedInstanceState A saved instance of an AROCollectorMainActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (AROCollectorUtils.isTcpDumpRunning()) {
			//this is the case when the main screen from a previous
			//collector instance was destroyed by the system, so it
			//was not cleaned up when the analyzer launches a new collector instance
			AROLogger.i(TAG, "tcpdump is already running");
			exitMainActivity();
			return;
		}
		
		setContentView(R.layout.arocollector_main_screen);
		validateAROAnalyzerConnectedLaunch();
		initializeMainScreenControls();
		registerAnalyzerTimeoutReceiver();
		registerAnalyzerLaunchReceiver();
	}
	
	/**
	 * Closes the current activity and display Home Screen of Data Collector,
	 */
	private void exitMainActivity() {
		// Close the current summary screen
		AROLogger.d(TAG, "another instance of collector already running, will exit MainActivity");
		finish();
	}

	/**
	 * method to register the receiver that listens to analyzer timeout
	 */
	private void registerAnalyzerTimeoutReceiver() {
		AROLogger.d(TAG, "registering analyzerTimeOutReceiver");
		registerReceiver(analyzerTimeoutReceiver, new IntentFilter(AROCollectorUtils.ANALYZER_TIMEOUT_SHUTDOWN_INTENT));
	}
	
	/**
	 * method to register the receiver that listens to analyzer launch intent
	 */
	private void registerAnalyzerLaunchReceiver() {
		AROLogger.d(TAG, "registering analyzerTimeOutReceiver");
		registerReceiver(analyzerLaunchReceiver, new IntentFilter(AROCollectorUtils.ANALYZER_LAUNCH_CLEANUP_INTENT));
	}

	/**
	 * Initializes the Data Collector main screen controls and control event
	 * handling. This is the first method to be called during activity create
	 * and it receives all the error dialogs ID for the respective error
	 * messages to be shown during application life cycle.
	 */
	private void initializeMainScreenControls() {

		final Button taskKiller;
		// Controls initialization
		mApp = (ARODataCollector) getApplication();
		startDataCollector = (Button) findViewById(R.id.startcollector);
		taskKiller = (Button) findViewById(R.id.taskkiller);
		collectScreenVideo = (CheckBox) findViewById(R.id.ckbx_screenshot);
		startDataCollector.setEnabled(true);
		mAroUtils = new AROCollectorUtils();
		mApp.setDataCollectorStopEnable(true);
		if (mApp.getDumpTraceFolderName()!=null) {
			mApp.setUSBVideoCaptureON(true);
			startDataCollector.setText(R.string.continuecollector);
		}else {
			mApp.setUSBVideoCaptureON(false);
			startDataCollector.setText(R.string.startcollector);
		}
			
		if (AROCollectorService.getServiceObj() != null) {
			collectScreenVideo.setChecked(mApp.getCollectVideoOption());
		}
		// Data Collector task killer button listener
		taskKiller.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AROCollectorMainActivity.this, AROCollectorTaskManagerActivity.class));
			}
		});
		
		if (mApp.isRQMCollectorLaunchfromAnalyzer() && getIntent().getExtras().getInt(ARODataCollector.ERRODIALOGID) ==100) {
			if (getInitalCollectorvalidationCheck()) {
				ARODataCollector.setAnalyzerLaunchInProgress(false);
				mApp.setDataCollectorStopEnable(false);
				startARODataCollector();
			}
			return;
		}
		// Start Data Collector button listener
		startDataCollector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (getIntent().getExtras() != null){
					//was launched from analyzer, set waiting to false since
					//the collector is now starting
					AROLogger.d(TAG, "collector starting, setAnalyzerLaunchInProgress(false)");
					ARODataCollector.setAnalyzerLaunchInProgress(false);
				}
				if (getInitalCollectorvalidationCheck()) {
					if (mApp.getDumpTraceFolderName() != null) {
						mApp.setCollectorLaunchfromAnalyzer(true);
						mApp.setDataCollectorStopEnable(false);
						startARODataCollector();
						return;
					}
					mApp.setCollectorLaunchfromAnalyzer(false);
					showARODataCollectorErrorDialog(Dialog_Type.TRACE_FOLDERNAME);
				}
			}
		});
		handleARODataCollectorErrors(getIntent().getExtras().getInt(ARODataCollector.ERRODIALOGID));
	}

	private boolean getInitalCollectorvalidationCheck() {
		
		// The Connectivity manager object to get current connect network, this
		// is used to check if we have active network before DC kicks off
		final ConnectivityManager mAROConnectiviyMgr;
		mAROConnectiviyMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final String state = Environment.getExternalStorageState();
		
		final NetworkInfo wifiNetworkInfo = mAROConnectiviyMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final NetworkInfo mobileNetworkInfo = mAROConnectiviyMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		NetworkInfo.State wifiState = NetworkInfo.State.UNKNOWN, mobileState = NetworkInfo.State.UNKNOWN;
		
		if (wifiNetworkInfo != null){
			wifiState = wifiNetworkInfo.getState();
		}
		if (mobileNetworkInfo != null){
			mobileState = mobileNetworkInfo.getState();
		}
		
		AROLogger.d(TAG, "wifiState=" + wifiState + "; mobileState=" + mobileState);
		
		// Check to validate is SD card is available for writing trace
		// data
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			showSDCardMountedError(false);
			return false;
		} else if (mAroUtils.isAirplaneModeOn(getApplicationContext())
				&& (wifiState == NetworkInfo.State.UNKNOWN || wifiState == NetworkInfo.State.DISCONNECTED)) {
			// Check if Airplane mode is on AND Wifi network is disconnected
			showAirplaneModeEnabledError(false);
			return false;
		} else if ((wifiState == NetworkInfo.State.DISCONNECTED || wifiState == NetworkInfo.State.UNKNOWN)
				&& (mobileState == NetworkInfo.State.DISCONNECTED || mobileState == NetworkInfo.State.UNKNOWN)) {
			// do not allow DC to start with both Mobile and Wifi off
			showARODataCollectorErrorDialog(Dialog_Type.WIFI_MOBILE_BOTH_OFF);
			return false;
		} 
		
		if(AROCollectorUtils.isTcpDumpRunning()){
			showARORunningError();
			return false;
		}
		
		// Making sure root permission is set to ARO application before
		// starting trace cycle
		Process mAROrootShell = null;
		try {
			mAROrootShell = Runtime.getRuntime().exec("su");
			mAROrootShell.getOutputStream();
		} catch (IOException e) {
			AROLogger.e(TAG, "exception in getting root permission", e);
		}
		mAROrootShell = null;
		return true;
	}
	/**
	 * Checks if ARO Data Collector has been kicked from Analyzer connected via USB
	 * 
	 */
	private void validateAROAnalyzerConnectedLaunch(){
		final Bundle apkCommandLineParameters  = getIntent().getExtras();
		if (apkCommandLineParameters != null) {
		    mAROTraceFolderNamefromAnalyzer = apkCommandLineParameters.getString("TraceFolderName");
		    
		}
		
	}
	/**
	 * Starts the ARO Data Collector trace in the background by starting the
	 * tcpdump/ffmpeg in native shell along with other peripherals trace like
	 * Wifi,Battery,GPS,Screen State,Bluetooth,Radio states.The
	 * startARODataCollector waits for 15 seconds to make sure all traces have
	 * been started in bacground before showing failed message to user. In case
	 * of failed start all traces files under trace folder name will be deleted
	 * from SD card.
	 * 
	 */
	private void startARODataCollector() {

		// Timer object which start as soon user press the Start Data Collector
		// to checks the tcpdump execution in the shell
		final Timer aroDCStartWatchTimer = new Timer();
		// Timers to get the PS list from the shell every seconds to verify
		// tcpdump execution till SCStartWatchTimer times out*/
		final Timer aroDCStartTimer = new Timer();
		// Task Killer process info class to manage and store all running
		// process
		
		mApp.setARODataCollectorStopFlag(false);
		mApp.setDataCollectorInProgressFlag(true);
		mApp.setRequestDataCollectorStop(false);
		mApp.setVideoCaptureFailed(false);
		startDataCollector.setEnabled(false);
		createAROTraceDirectory();
		
		if (mApp.getDumpTraceFolderName() != null) {
		
			//Takes a snap shoot of the time the system booted to be used for the timer on the home page.
			mApp.setElapsedTimeStartTime(System.currentTimeMillis());
			
			// Starting the ARO Data collector service before tcpdump to record
			// >=t(0)
			startService(new Intent(getApplicationContext(), AROCollectorTraceService.class));
			// Starting the tcpdump service and starts the video capture
			startService(new Intent(getApplicationContext(), AROCollectorService.class));
			collectScreenVideo.setEnabled(false);
			if (collectScreenVideo.isChecked()) {
				mApp.setCollectVideoOption(true);
			} else {
				mApp.setCollectVideoOption(false);
			}
			mApp.showProgressDialog(this);
			// ARO Watch timer for failed start message of data collector after 15
			// sec
			aroDCStartWatchTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!mApp.getTcpDumpStartFlag()) {
						AROLogger.w(TAG, "Failed to start ARODataCollector in 15 sec");
						stopService(new Intent(getApplicationContext(), AROCollectorTraceService.class));
						stopService(new Intent(getApplicationContext(), AROCollectorService.class));
						// As we collect peripherals trace i.e wifi,GPs
						// service before tcpdump trace so we making sure we delete
						// all of the traces if we don't have tcpdump running
						mAroUtils.deleteTraceFolder(new File(mApp.getTcpDumpTraceFolderName()));
						mAROFailStartHandler.sendMessage(Message.obtain(mAROFailStartHandler,
								NAVIGATE_HOME_SCREEN));
					}
					// Cancel the timers
					aroDCStartWatchTimer.cancel();
					aroDCStartTimer.cancel();
				}
			}, ARO_START_WATCH_TIME);
			// Timer to check start data collector kick-off within 15 secs
			aroDCStartTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					mApp.setTcpDumpStartFlag(AROCollectorUtils.isTcpDumpRunning());
					if (mApp.getTcpDumpStartFlag()) {
						mApp.hideProgressDialog();
						mApp.setDataCollectorInProgressFlag(false);
						mApp.triggerAROAlertNotification();
						mAROHomeScreenHandler.sendMessage(Message.obtain(mAROHomeScreenHandler, 0));
						aroDCStartWatchTimer.cancel();
						aroDCStartTimer.cancel();
					}
				}
			}, ARO_START_TICK_TIME, ARO_START_TICK_TIME);
	   }
	}

	/**
	 * Creates the given trace directory on the device SD card under root
	 * directory of ARO (\SDCARD\ARO)
	 * 
	 */
	private void createAROTraceDirectory() {

		final String mAroTraceDatapath = mApp.getTcpDumpTraceFolderName();
		final File traceFolder = new File(mAroTraceDatapath);
		final File traceRootFolder = new File(ARODataCollector.ARO_TRACE_ROOTDIR);

		AROLogger.d(TAG, "mAroTraceDatapath=" + mAroTraceDatapath);

		// Creates the trace root directory
		if (!traceRootFolder.exists()) {
			traceRootFolder.mkdir();
		}
		// Creates the trace directory inside /SDCARD/ARO
		if (!traceFolder.exists()) {
			traceFolder.mkdir();
		}
	}

	/**
	 * Displays the ARO error message dialog during the application lifetime
	 * 
	 * @param errordialogid
	 *            : Error dialog ID no to show error message
	 * 
	 */
	private void showARODataCollectorErrorDialog(Dialog_Type errordialogid) {
		m_dialog = errordialogid;
		final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
				AROCollectorMainActivity.this, android.R.style.Theme_Translucent, m_dialog,
				new OnTraceFolderListener(), null);
		myDialog.show();
	}

	/**
	 * Displays the ARO error message which caused the Data Collector to stop
	 * during the life cycle
	 * 
	 * @param errordialogid
	 *            : Error dialog ID no to show error message
	 * 
	 */
	private void showARODataCollectorStopErrorDialog(Dialog_Type errordialogid) {
		m_dialog = errordialogid;
		final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
				AROCollectorMainActivity.this, android.R.style.Theme_Translucent, m_dialog,
				new OnErrorDialogCallBackListener(), null);
		myDialog.show();
	}

	/**
	 * Display the error messages when SD card is mounted and not available for
	 * device to write on SD card.
	 * 
	 * @param mMidtraceflag
	 *            : Boolean value to check if the error has triggered mid of
	 *            trace cycle or before start
	 */
	private void showSDCardMountedError(boolean midtraceflag) {
		if (midtraceflag) {
			m_dialog = Dialog_Type.SDCARD_MOUNTED_MIDTRACE;
		} else {
			m_dialog = Dialog_Type.SDCARD_MOUNTED;
		}
		final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
				AROCollectorMainActivity.this, android.R.style.Theme_Translucent, m_dialog,
				new OnTraceFolderListener(), null);
		myDialog.show();
	}
	
	/**
	 * Display the error messages when Airplane Mode is enabled and Wifi is off
	 * 
	 * @param mMidtraceflag
	 *            : Boolean value to check if the error has triggered mid of
	 *            trace cycle or before start
	 */
	private void showAirplaneModeEnabledError(boolean midtraceflag) {
		if (midtraceflag) {
			m_dialog = Dialog_Type.AIRPANCE_MODEON_MIDTRACE;
		} else {
			m_dialog = Dialog_Type.AIRPANCE_MODEON;
		}
		final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
				AROCollectorMainActivity.this, android.R.style.Theme_Translucent, m_dialog,
				new OnTraceFolderListener(), null);
		myDialog.show();
	}

	
	/**
	 * show the error message saying that ARO is already running
	 */
	private void showARORunningError() {
		m_dialog = Dialog_Type.ARO_INSTANCE_RUNNING;
		final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
				AROCollectorMainActivity.this, android.R.style.Theme_Translucent, m_dialog,
				new OnTraceFolderListener(), null);
		myDialog.show();
	}
	
	/**
	 * Overrides the android.app.Activity#onPause method. 
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {

		AROLogger.d(TAG, "onPause() called");
		super.onPause();
	}

	/**
	 * Handles the result of an activity performed on the UI of the Landing screen. 
	 * Overrides the android.app.Activity# onActivityResult method.
	 * @param requestCode A code representing a request to the UI of the Landing screen.
	 * @param resultCode A code representing the result of an activity performed on the UI of the Landing screen.
	 * @param data An Intent object that contains data associated with the result of the activity
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Overrides the android.app.Activity#Destroy method to close current
	 * activity.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {

		super.onDestroy();
		unregisterTimeoutReceiver();
		unregisterLaunchReceiver();
		AROLogger.d(TAG, "inside onDestroy, unregistered broadcast receivers");
	}

	/**
	 * Handler for starting the Home Screen on main UI thread after Data
	 * Collector start notification
	 */
	private Handler mAROHomeScreenHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NAVIGATE_HOME_SCREEN:
				mApp.hideProgressDialog();
				startActivity(new Intent(AROCollectorMainActivity.this,
						AROCollectorHomeActivity.class));
				finish();
				break;
			}
		}
	};

	/**
	 * Handler for starting the Error dialog on main UI thread during any error
	 * message of Data Collector
	 */
	private Handler mAROFailStartHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case NAVIGATE_HOME_SCREEN:
				mApp.hideProgressDialog();
				if (AROCollectorTraceService.getServiceObj() != null) {
					stopService(new Intent(getApplicationContext(), AROCollectorTraceService.class));
				}
				if (AROCollectorService.getServiceObj() != null) {
					stopService(new Intent(getApplicationContext(), AROCollectorService.class));
				}
				showARODataCollectorErrorDialog(Dialog_Type.DC_FAILED_START);
				AROLogger.d(TAG, "Setting Data Collector stop flag");
				mApp.setARODataCollectorStopFlag(true);
				collectScreenVideo.setEnabled(true);
				startDataCollector.setEnabled(true);
				AROLogger.d(TAG, "Setting Data Collector stop flag");
				break;
			}
		}
	};

	/**
	 * Handler to handle the valid error message dialog for data collector stop
	 * reasons on application UI thread
	 * 
	 */
	private void handleARODataCollectorErrors(int errordialogid) {
		switch (errordialogid) {

		case ARODataCollector.TCPDUMPSTOPPED:
			m_dialog = Dialog_Type.TRACE_STOPPED;
			showARODataCollectorStopErrorDialog(Dialog_Type.TRACE_STOPPED);
			break;
		case ARODataCollector.SDCARDERROR:
			m_dialog = Dialog_Type.SDCARD_ERROR;
			showARODataCollectorStopErrorDialog(Dialog_Type.SDCARD_ERROR);
			break;
		case ARODataCollector.SDCARDMOUNTED:
			m_dialog = Dialog_Type.SDCARD_MOUNTED;
			showSDCardMountedError(false);
			break;
		case ARODataCollector.SDCARDMOUNTED_MIDTRACE:
			m_dialog = Dialog_Type.SDCARD_MOUNTED_MIDTRACE;
			showSDCardMountedError(true);
			break;
		case ARODataCollector.AIRPLANEMODEENABLED_MIDTRACE:
			m_dialog = Dialog_Type.AIRPANCE_MODEON_MIDTRACE;
			showAirplaneModeEnabledError(true);
			break;
		}
		AROLogger.d(TAG, "handleErrorDialogs errordialogid=" + errordialogid);
	}

	/**
	 * The Class implements the call back events from the dialogs
	 * 
	 */
	private class OnTraceFolderListener implements AROCollectorCustomDialog.ReadyListener {

		@Override
		public void ready(
				com.att.android.arodatacollector.main.AROCollectorCustomDialog.Dialog_CallBack_Error errorcode,
				boolean success) {
			if (success) {
				AROLogger.d(TAG, "Device SD Card Space=" + mAroUtils.checkSDCardMemoryAvailable());
				// Checking if the available space of SD card is less than 5MB
				// before start of the trace
				if (mAroUtils.checkSDCardMemoryAvailable() < AROSDCARD_MIN_SPACEBYTES) {
					m_dialog = Dialog_Type.SDCARD_ERROR;
					showARODataCollectorStopErrorDialog(Dialog_Type.SDCARD_ERROR);
					return;
				} else
					startARODataCollector();
			} else {
				// Handling specific error message from the call backs
				switch (errorcode) {
				case CALLBACK_TRACEFOLDERERROR:
					showARODataCollectorErrorDialog(Dialog_Type.TRACE_FOLDERNAME_ERRORMESSAGE);
					break;
				case CALLBACK_TRACEEXISTSERROR:
					showARODataCollectorErrorDialog(Dialog_Type.TRACE_FOLDERNAME_EXISTS);
					break;
				case CALLBACK_SHOWTRACENAMEERROR:
					showARODataCollectorErrorDialog(Dialog_Type.TRACE_FOLDERNAME);
					break;
				case CALLBACK_SPECIALCHARERROR:
					showARODataCollectorErrorDialog(Dialog_Type.TRACE_SPECIALCHARERROR);
					break;
				case CALLBACK_DEFAULT: // This is default case to close the app
					finish();
					break;
				}
			}
		}
	}

	/**
	 * The Class implements the listener for error dialogs
	 * 
	 */
	private class OnErrorDialogCallBackListener implements AROCollectorCustomDialog.ReadyListener {
		@Override
		public void ready(Dialog_CallBack_Error errorcode, boolean success) {
			switch (errorcode) {
			case CALLBACK_TRACEFOLDERERROR:
				mApp.cancleAROAlertNotification();
				break;
			}

		}
	}
	
	/**
	 * receiver to listen to the timeout broadcast sent from the analyzer
	 */
	private BroadcastReceiver analyzerTimeoutReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context ctx, Intent intent) {
	    	AROLogger.d(TAG, "received analyzerTimeoutIntent at " + System.currentTimeMillis());
	        finish();
	    }
	};
	
	/**
	 * receiver to listen to the analyzer launch cleanup broadcast sent from the splashActivity
	 */
	private BroadcastReceiver analyzerLaunchReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			AROLogger.d(TAG, "received analyzerLaunchCleanupIntent at " + System.currentTimeMillis());
	        finish();
		}
		
	};

	/**
	 * method to unregister the receiver that listens to analyzer timeout
	 */
	private void unregisterTimeoutReceiver() {
		AROLogger.d(TAG, "inside unregisterTimeoutReceiver");
		try {
			if (analyzerTimeoutReceiver != null) {
				unregisterReceiver(analyzerTimeoutReceiver);
				analyzerTimeoutReceiver = null;
				
				AROLogger.d(TAG, "successfully unregistered analyzerTimeoutReceiver");
			}
		} catch (Exception e){
			AROLogger.i(TAG, "Ignoring exception in unregisterTimeoutReceiver", e);
		}
	}
	
	/**
	 * method to unregister the receiver that listens to analyzer launch
	 */
	private void unregisterLaunchReceiver() {
		AROLogger.d(TAG, "inside unregisterLaunchReceiver");
		try {
			if (analyzerLaunchReceiver != null) {
				unregisterReceiver(analyzerLaunchReceiver);
				analyzerLaunchReceiver = null;
				
				AROLogger.d(TAG, "successfully unregistered analyzerLaunchReceiver");
			}
		} catch (Exception e){
			AROLogger.i(TAG, "Ignoring exception in unregisterLaunchReceiver", e);
		}
	}
}
