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

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.AROCollectorService;
import com.att.android.arodatacollector.main.ARODataCollector;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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

	/**
	 * Initializes data members with a saved instance of an AROCollectorHomeActivity object. 
	 * Overrides the android.app.Activity#onCreate method. 
	 * @param savedInstanceState – A saved instance of an AROCollectorHomeActivity object.
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
		// TODO : Better way to do this
		if (mApp != null) {
			mApp.hideProgressDialog();
		}
		finish();
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
			mApp.showProgressDialog(this);
		}
		if (AROCollectorService.getServiceObj() != null) {
			// Sends the STOP Command to tcpdump socket and Stop the Video
			// capture on device
			AROCollectorService.getServiceObj().requestDataCollectorStop();
			mApp.cancleAROAlertNotification();
		}

	}

}
