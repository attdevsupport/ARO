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

import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.att.android.arodatacollector.utils.AROLogger;

/**
 * Represents the Trace Completed screen of the ARO Data Collector, which
 * displays a summary of trace collection details. The summary includes the
 * duration of the trace and the location of the trace files.
 */

public class AROCollectorCompletedActivity extends Activity {

	/** Android log TAG string for ARO-Data Collector trace summary Screen */
	private static final String TAG = "ARO.CompletedActivity";

	/** The final trace summary OK button control **/
	private Button traceSummaryOKButton;

	/**
	 * The Application context of the ARo-Data Collector to gets and sets the
	 * application data
	 **/
	private ARODataCollector mApp;

	/**
	 * Initializes data members with a saved instance of an AROCollectorCompletedActivity 
	 * object. Overrides the android.app.Activity#onCreate method.
	 * @param savedInstanceState A saved instance of an AROCollectorCompletedActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (new AROCollectorUtils().isTcpDumpRunning()) {
			//this is the case when the summary screen from a previous
			//collector instance was destroyed by the system, so it
			//was not cleaned up when the analyzer launches a new collector instance
			exitSummaryScreen();
			return;
		}
		
		mApp = (ARODataCollector) getApplication();
		setContentView(R.layout.arocollector_tracecompleted_screen);
		initTraceSummaryControls();
		initTraceSummaryControlListeners();
		registerAnalyzerLaunchReceiver();
	}
	
	/**
	 * Closes the current activity
	 */
	private void exitSummaryScreen() {
		// Close the current summary screen
		AROLogger.d(TAG, "another instance of collector already running, will exit this summary screen");
		
		finish();
	}

	/**
	 * Overrides the android.app.Activity#onKeyDown method to handle key presses
	 * to the OK button.
	 * @param keyCode A code that represents the key that was pressed down.
	 * @param event An event object for the key action.
	 * @return A boolean value that is true if the key was pressed, and false otherwise.
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			startAROMainActivity();
			finish(); // Will close current activity
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Initializes the control for trace summary UI screen
	 */
	private void initTraceSummaryControls() {
		final TextView tracePath = (TextView) findViewById(R.id.tracepath);
		final TextView videotrace = (TextView) findViewById(R.id.tracevideo);

		try {
			mApp.readPcapStartEndTime();
			setApplicationUpTime();
		} catch (IOException e) {
			// TODO: Setting default value for application up time and notify
			// user
			AROLogger.e(TAG, "exception in readPcapStartEndTime. Could not read trace start time", e);
		}
		traceSummaryOKButton = (Button) findViewById(R.id.datasummaryok);
		tracePath.setText(ARODataCollector.ARO_TRACE_ROOTDIR + mApp.getDumpTraceFolderName());
		if ((mApp.getCollectVideoOption() && !mApp.isVideoFileExisting() && !mApp.isUSBVideoCaptureON())
				|| mApp.getVideoCaptureFailed()) {
			videotrace.setText(getResources().getText(R.string.aro_failedvideo));
		} else if (mApp.getCollectVideoOption() || mApp.isUSBVideoCaptureON() ) {
			videotrace.setText(getResources().getText(R.string.aro_yestext));
		} else {
			videotrace.setText(getResources().getText(R.string.aro_notext));
		}
		mApp.setTcpDumpTraceFolderName(null);
	}

	/**
	 * Starts the Data Collector Main Activity page
	 */
	private void startAROMainActivity() {
		final Intent splashScreenIntent = new Intent(getBaseContext(), AROCollectorMainActivity.class);
		// Generic Error ID number 100 passed as an argument to navigate to Main
		// Screen without any dialog
		splashScreenIntent.putExtra(ARODataCollector.ERRODIALOGID, 100);
		splashScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(splashScreenIntent);
	}

	/**
	 * Initializes the button control and event mapping
	 */
	private void initTraceSummaryControlListeners() {
		traceSummaryOKButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mApp.isRQMCollectorLaunchfromAnalyzer() || mApp.isCollectorLaunchfromAnalyzer()) {
					finish();
				} else {
					startAROMainActivity();
					// Will close current activity
					finish();
				}
			}
		});

	}

	/**
	 * Overrides the android.app.Activity#onPause method.
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		AROLogger.d(TAG, "onPause() called");
	}

	/**
	 * Sets the Data Collector application trace duration
	 */
	private void setApplicationUpTime() {
		long appTimeR, appUpHours, appUpMinutes, appUpSeconds;
		final long appUpTime = mApp.getAppUpTimeinSeconds();
		appTimeR = appUpTime % 3600;
		appUpHours = appUpTime / 3600;
		appUpMinutes = (appTimeR / 60);
		appUpSeconds = appTimeR % 60;
		final TextView traceduration = (TextView) findViewById(R.id.traceduration);
		traceduration.setText((appUpHours < 10 ? "0" : "") + appUpHours + ":"
				+ (appUpMinutes < 10 ? "0" : "") + appUpMinutes + ":"
				+ (appUpSeconds < 10 ? "0" : "") + appUpSeconds);
		
		if (AROLogger.logDebug){
			AROLogger.d(TAG, "DataCollector up time=" + (appUpHours < 10 ? "0" : "") + appUpHours + ":"
					+ (appUpMinutes < 10 ? "0" : "") + appUpMinutes + ":"
					+ (appUpSeconds < 10 ? "0" : "") + appUpSeconds);
		}
	}

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
	 * method to register the receiver that listens to analyzer launch intent
	 */
	private void registerAnalyzerLaunchReceiver() {
		AROLogger.d(TAG, "registering analyzerTimeOutReceiver");
		registerReceiver(analyzerLaunchReceiver, new IntentFilter(AROCollectorUtils.ANALYZER_LAUNCH_CLEANUP_INTENT));
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
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterLaunchReceiver();
		
		AROLogger.d(TAG, "inside onDestroy, unregistered analyzerLaunchReceiver");
	}
}
