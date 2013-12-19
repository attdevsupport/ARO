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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.AROCollectorService;
import com.att.android.arodatacollector.main.AROCollectorTraceService;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.att.android.arodatacollector.utils.AROLogger;

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
	
	/** Used for calculating the trace duration*/
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
		
		AROLogger.d(TAG, "inside onCreate");
		
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
		
		registerAnalyzerCloseCmdReceiver();
	}

	/**
	 * Hides the Progress dialog window when it loses focus. Overrides the 
	 * android.app.Activity#onPause method. 
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		AROLogger.d(TAG, "onPause() called");
		super.onPause();
		if (mApp != null) {
			mApp.hideProgressDialog();
		}
		
		AROLogger.d(TAG, "calling unregisterUsbBroadcastReceiver inside onPause()");
		unregisterUsbBroadcastReceiver();
		finish();
	}

	/**
	 * method to unregister the receiver that listens to usb connect/disconnect events
	 */
	private void unregisterUsbBroadcastReceiver() {
		AROLogger.d(TAG, "inside unregisterUsbBroadcastReceiver");
		try {
			if (USBBroadcastReceiver != null) {
				unregisterReceiver(USBBroadcastReceiver);
				USBBroadcastReceiver = null;
				
				AROLogger.d(TAG, "successfully unregistered the USBBroadcastReceiver");
			}
		} catch (Exception e){
			AROLogger.i(TAG, "Ignoring exception in unregisterUsbBroadcastReceiver", e);
		}
	}
	
	/**
	 * override the parent's method to unregister the usb broadcast receiver
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		AROLogger.d(TAG, "calling unregisterUsbBroadcastReceiver inside onDestroy()");
		
		unregisterUsbBroadcastReceiver();
		unregisterAnalyzerCloseCmdReceiver();
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
		if(mApp.isCollectorLaunchfromAnalyzer() || mApp.isRQMCollectorLaunchfromAnalyzer()){
			final boolean dataCollectorStopEnable = mApp.getDataCollectorStopEnable();
			AROLogger.d(TAG, "dataCollectorStopEnable: " + dataCollectorStopEnable);
			stopDataCollector.setEnabled(dataCollectorStopEnable);
		}
	}

	
	/**
	 * Stops the data collector trace by stopping Video Trace and tcpdump from
	 * shell
	 */
	private void stopARODataCollector() {
		AROLogger.d(TAG, "Inside stopARODataCollector....");
		stopDataCollector.setEnabled(false);
		hideDataCollector.setEnabled(false);
		mApp.setARODataCollectorStopFlag(true);
		if (mApp != null) {
			AROLogger.d(TAG, "calling unregisterUsbBroadcastReceiver inside stopARODataCollector");
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
			AROLogger.e(TAG, "inside AROCollectorHomeActivity.stopARODataCollector, but AROCollectorService/AROCollectorTraceService is null. Timestamp: " + System.currentTimeMillis());
			//this typically happens when the service had been killed by Android and has not been restarted.
			//This should no longer happen since we implemented these services as foreground service
		}

	}
	
	/**
	 * USBBroadcastReceiver to listen to usb connect/disconnect event. This is used
	 * to enable/disable the stop button when the trace is started from the analyzer
	 */
	private BroadcastReceiver USBBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	boolean stopEnabled = intent.getBooleanExtra(AROCollectorTraceService.USB_ACTION_EXTRA_KEY, false);
        	AROLogger.d(TAG, "received usbBroadcast, collectorStopEnable: " + stopEnabled);
			stopDataCollector.setEnabled(stopEnabled);
        }
    };
	
	/**
	 * override the parent method to register the usb broadcast receiver
	 */
    @Override
	public void onResume() {
		super.onResume();		
		
		try {
			
			registerReceiver(USBBroadcastReceiver, new IntentFilter(AROCollectorTraceService.USB_BROADCAST_ACTION));
			AROLogger.d(TAG, "registered USBBroadcastReceiver in onResume()");
		} catch (Exception e){
			AROLogger.i(TAG, "Exception caught in onResume.registerReceiver(). Will ignore", e);
		}
	}
    
    /**
	 * method to register the receiver that listens to analyzer timeout
	 */
	private void registerAnalyzerCloseCmdReceiver() {
		AROLogger.d(TAG, "registering analyzerTimeOutReceiver");
		registerReceiver(analyzerCloseCmdReceiver, new IntentFilter(AROCollectorUtils.ANALYZER_CLOSE_CMD_INTENT));
	}
	
	private void unregisterAnalyzerCloseCmdReceiver() {
		AROLogger.d(TAG, "inside unregisterTimeoutReceiver");
		try {
			if (analyzerCloseCmdReceiver != null) {
				unregisterReceiver(analyzerCloseCmdReceiver);
				analyzerCloseCmdReceiver = null;

				AROLogger.d(TAG, "successfully unregistered analyzerCloseCmdReceiver");
			}
		} catch (Exception e) {
			AROLogger.d(TAG, "Ignoring exception in analyzerCloseCmdReceiver", e);
		}
	}
	
	private BroadcastReceiver analyzerCloseCmdReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context ctx, Intent intent) {
	    	AROLogger.d(TAG, "received analyzer close cmd intent at " + System.currentTimeMillis());
	        finish();
	    }
	};
 
}
