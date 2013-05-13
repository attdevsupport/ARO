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

//import org.apache.http.client.ClientProtocolException;

import java.util.Timer;
import java.util.TimerTask;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.AROCollectorService;
import com.att.android.arodatacollector.main.AROCollectorTraceService;
import com.att.android.arodatacollector.main.ARODataCollector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * Represents the Home screen of the ARO Data Collector, which contains butttons
 * that allow the user to hide or stop the application. When the application is
 * hidden, the user can navigate to Home Screen by selecting a command on the
 * context menu of the application, or by clicking the application.
 * 
 * */

public class AROCollectorHomeActivity extends Activity {

	/** Android log TAG string for ARO-Data Collector Home Screen */
	private static final String TAG = "ARO.HomeActivity";

	/**
	 * The boolean value to enable logs based on production build or debug build
	 */
	private static boolean mIsProduction = false;

	/**
	 * A boolean value that indicates whether or not to enable logging for this
	 * class in a debug build of the ARO Data Collector.
	 */
	public static boolean DEBUG = !mIsProduction;

	/** The Home screen button controls to hide and stop data collector */
	private Button hideDataCollector, stopDataCollector;

	/**
	 * The Application context of the ARO-Data Collector to gets and sets the
	 * application data
	 **/
	private ARODataCollector mApp;

	/** Used to access the "Timer" text box in aroCollector_home_screen.xml */
	private TextView timerText;    
	
	/** Keeps track of the current tick that the timer is on*/
	private long countUp; 
	
	private static Timer delayedStopTimer = null;
	
	private Chronometer stopWatch;
	
	/**
	 * Initializes data members with a saved instance of an AROCollectorHomeActivity object. 
	 * Overrides the android.app.Activity#onCreate method. 
	 * @param savedInstanceState  A saved instance of an AROCollectorHomeActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (ARODataCollector) getApplication();
		//mAroUtils = new AROCollectorUtils();
		setContentView(R.layout.arocollector_home_screen);
		initHomeScreenControls();
		initHomeScreenControlListeners();
		      
		//All code until end of the method is used to maintain the on screen timer.
		stopWatch = (Chronometer) findViewById(R.id.chrono);
		stopWatch.setBase(mApp.getElapsedTimeStartTime());						
		
		timerText = (TextView) findViewById(R.id.timer);    
		
		stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){             
			@Override             
			 public void onChronometerTick(Chronometer arg0) {                 
				 countUp = (System.currentTimeMillis() - arg0.getBase()) / 1000;	
				 String asText;
				 String minPlace = "";
				 String secPlace = "";
				 
				 if (((countUp / 60) % 60) < 10){
					 minPlace = "0";
				 }
				 if (countUp % 60 < 10){
					 secPlace = "0";
				 }
				 //Display the time in standard "Hours:Minutes:Seconds" | "00:00:00"
				 asText = "0" + (countUp / 3600) + ":" + minPlace + ((countUp / 60) % 60) + ":" + secPlace + (countUp % 60);
				 timerText.setText(getText(R.string.aro_traceTimer) + " " + asText); 
			}         
		});         
		stopWatch.start(); 
		
		final Bundle apkCommandLineParameters  = getIntent().getExtras();
		if (apkCommandLineParameters != null) {
			String mAROStopRequestFromAnalyzer = apkCommandLineParameters
					.getString("StopCollector");
			if(mAROStopRequestFromAnalyzer!=null){
				stopARODataCollector();
			}
		}
	}

	/**
	 * Hides the Progress dialog window when it loses focus. Overrides the 
	 * android.app.Activity#onPause method. 
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		if (DEBUG) {
			Log.d(TAG, "onPause() called");
		}
		super.onPause();
		if (mApp != null) {
			mApp.hideProgressDialog();
		}
		
		if (DEBUG){
			Log.i(TAG, "calling unregisterUsbBroadcastReceiver inside onPause()");
		}
		unregisterUsbBroadcastReceiver();
		finish();
	}

	private void unregisterUsbBroadcastReceiver() {
		if (DEBUG){
			Log.i(TAG, "inside unregisterUsbBroadcastReceiver");
		}
		try {
			if (USBBroadcastReceiver != null) {
				unregisterReceiver(USBBroadcastReceiver);
				USBBroadcastReceiver = null;
				
				if (DEBUG){
					Log.i(TAG, "successfully unregistered the USBBroadcastReceiver");
				}
			}
		} catch (Exception e){
			Log.i(TAG, "Ignoring exception in unregisterUsbBroadcastReceiver", e);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (DEBUG){
			Log.i(TAG, "calling unregisterUsbBroadcastReceiver inside onDestroy()");
		}
		
		unregisterUsbBroadcastReceiver();
	}


	/**
	 * Initialize the data control home screen controls
	 */
	private void initHomeScreenControls() {
		hideDataCollector = (Button) findViewById(R.id.hidedatacollector);
		stopDataCollector = (Button) findViewById(R.id.stopcollector);
	}

	/**
	 * Initialized the set the events for home screen components
	 */
	private void initHomeScreenControlListeners() {
		hideDataCollector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(); // Will close current activity
			}
		});
		stopDataCollector.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopARODataCollector();
			}
		});
		if(mApp.isCollectorLaunchfromAnalyzer()){
			final boolean dataCollectorStopEnable = mApp.getDataCollectorStopEnable();
			Log.i(TAG, "dataCollectorStopEnable: " + dataCollectorStopEnable);
			stopDataCollector.setEnabled(dataCollectorStopEnable);
		}
	}

	
	/**
	 * Stops the data collector trace by stopping Video Trace and tcpdump from
	 * shell
	 */
	private void stopARODataCollector() {
		if (DEBUG) {
			Log.i(TAG, "Inside stopARODataCollector....");
		}
		stopDataCollector.setEnabled(false);
		hideDataCollector.setEnabled(false);
		mApp.setARODataCollectorStopFlag(true);
		if (mApp != null) {
			if (DEBUG){
				Log.i(TAG, "calling unregisterUsbBroadcastReceiver inside stopARODataCollector");
			}
			unregisterUsbBroadcastReceiver();
			mApp.showProgressDialog(this);
		}
		if (AROCollectorService.getServiceObj() != null && AROCollectorTraceService.getServiceObj() != null) {
			// Sends the STOP Command to tcpdump socket and Stop the Video
			// capture on device
			AROCollectorService.getServiceObj().requestDataCollectorStop();
			mApp.cancleAROAlertNotification();
		}
		else {
			Log.w(TAG, "inside AROCollectorHomeActivity.stopARODataCollector, but AROCollectorService/AROCollectorTraceService is null. Timestamp: " + System.currentTimeMillis());
			//this typically happens when the service had been killed by Android and has not been restarted.
			//so here, we will request the service start and schedule a task to check  
			
			if (AROCollectorService.getServiceObj() == null){
				startService(new Intent(getApplicationContext(), AROCollectorService.class));
			}
			
			if (AROCollectorTraceService.getServiceObj() == null){
				startService(new Intent(getApplicationContext(), AROCollectorTraceService.class));
			}
			
			delayedStopTimer = new Timer();
			
			TimerTask stopTask = new TimerTask(){
				//schedule a task to request the data collector stop in 5 sec
				public void run(){
					Log.i(TAG, "timer's up, will try to stop tcpdump. Timestamp: " + System.currentTimeMillis());
					if (AROCollectorService.getServiceObj() != null && AROCollectorTraceService.getServiceObj() != null) {
						Log.i(TAG, "AROCollectorService and AROCollectorTraceService are running, will invoke requestDataCollectorStop(). Timestamp: " + System.currentTimeMillis());
						// Sends the STOP Command to tcpdump socket and Stop the Video
						// capture on device
						AROCollectorService.getServiceObj().requestDataCollectorStop();
						mApp.cancleAROAlertNotification();
						delayedStopTimer.cancel();
					}
					else {
						Log.i(TAG, "AROCollectorService/AROCollectorTraceService is still not restarted yet. Will try again in 3secs. Timestamp: " + System.currentTimeMillis());
					}
				}

			};
			
			Log.i(TAG, "scheduling a timer task to keep checking for when the AROCollectorService/AROCollectorTraceService will be started by Android");
			delayedStopTimer.schedule(stopTask, 5000, 3000);
		}

	}
	
	/**
	 * USBBroadcastReceiver
	 */
	private BroadcastReceiver USBBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	boolean stopEnabled = intent.getBooleanExtra(AROCollectorTraceService.USB_ACTION_EXTRA_KEY, false);
        	if(DEBUG){
        	Log.i(TAG, "received usbBroadcast, collectorStopEnable: " + stopEnabled);
        	}
			stopDataCollector.setEnabled(stopEnabled);
        }
    };
	
	@Override
	public void onResume() {
		super.onResume();		
		
		try {
			
			registerReceiver(USBBroadcastReceiver, new IntentFilter(AROCollectorTraceService.USB_BROADCAST_ACTION));
			if (DEBUG){
				Log.i(TAG, "registered USBBroadcastReceiver in onResume()");
			}
		} catch (Exception e){
			Log.w(TAG, "Exception caught in onResume.registerReceiver(). Will ignore", e);
		}
	}
 
}
