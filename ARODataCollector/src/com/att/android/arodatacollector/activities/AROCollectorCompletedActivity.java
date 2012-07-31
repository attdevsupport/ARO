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

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.ARODataCollector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Represents the Trace Completed screen of the ARO Data Collector, which
 * displays a summary of trace collection details. The summary includes the
 * duration of the trace and the location of the trace files.
 */

public class AROCollectorCompletedActivity extends Activity {

	/** Android log TAG string for ARO-Data Collector trace summary Screen */
	private static final String TAG = "ARO.CompletedActivity";

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean mIsProduction = true;

	/**
	 * A boolean value that indicates whether or not to enable logging for this
	 * class in a debug build of the ARO Data Collector.
	 */
	public static boolean DEBUG = !mIsProduction;

	/** The final trace summary OK button control **/
	private Button traceSummaryOKButton;

	/**
	 * The Application context of the ARo-Data Collector to gets and sets the
	 * application data
	 **/
	private ARODataCollector mApp;

	/**
	 * Overriding onCreate initialize data members
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (ARODataCollector) getApplication();
		setContentView(R.layout.arocollector_tracecompleted_screen);
		initTraceSummaryControls();
		initTraceSummaryControlListeners();
	}

	/**
	 * Overrides the android.app.Activity#onKeyDown method to handle key presses
	 * to the OK button.
	 * 
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
			Log.e(TAG, "exception in readPcapStartEndTime. Could not read trace start time", e);
		}
		traceSummaryOKButton = (Button) findViewById(R.id.datasummaryok);
		tracePath.setText(ARODataCollector.ARO_TRACE_ROOTDIR + mApp.getDumpTraceFolderName());
		if ((mApp.getCollectVideoOption() && !mApp.isVideoFileExisting())
				|| mApp.getVideoCaptureFailed()) {
			videotrace.setText(getResources().getText(R.string.aro_failedvideo));
		} else if (mApp.getCollectVideoOption()) {
			videotrace.setText(getResources().getText(R.string.aro_yestext));
		} else {
			videotrace.setText(getResources().getText(R.string.aro_notext));
		}

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
				startAROMainActivity();
				// Will close current activity
				finish();
			}
		});

	}

	/**
	 * Overriding onPause
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG) {
			Log.d(TAG, "onPause() called");
		}
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
		if (DEBUG) {
			Log.i(TAG, "DataCollector up time=" + (appUpHours < 10 ? "0" : "") + appUpHours + ":"
					+ (appUpMinutes < 10 ? "0" : "") + appUpMinutes + ":"
					+ (appUpSeconds < 10 ? "0" : "") + appUpSeconds);
		}

	}

}
