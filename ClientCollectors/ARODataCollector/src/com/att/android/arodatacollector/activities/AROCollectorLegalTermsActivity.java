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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.att.android.arodatacollector.utils.AROLogger;

import android.provider.Settings;

/**
 * Represents the Legal Terms screen of the ARO Data Collector. A user must
 * accept the legal terms in order to continue with the application.
 * 
 */
public class AROCollectorLegalTermsActivity extends Activity {

	/** Android log TAG string for ARO-Data Collector Legal Screen */
	private static final String TAG = "ARO.LegalTermsActivity";

	/** Identifies that the ARO Data Collector legal terms have been accepted. */
	public static int TERMS_ACCEPTED = 1;

	/** Identifies that the ARO Data Collector legal terms have been rejected. */
	public static int TERMS_REJECTED = 2;

	/** Identifies that the Android keep activities settings is ON or OFF. */
	private int mKeepActivities;

	/**
	 * Initializes data members with a saved instance of an AROCollectorLegalTermsActivity object. 
	 * Overrides the android.app.Activity#onCreate method. 
	 * @param savedInstanceState  A saved instance of an AROCollectorLegalTermsActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		AROLogger.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		if (new AROCollectorUtils().isTcpDumpRunning()) {
			//this is the case when the tc screen from a previous
			//collector instance was destroyed by the system, so it
			//was not cleaned up when the analyzer launches a new collector instance
			exitTCActivity();
			return;
		}
		Display display = getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		int width = display.getWidth();
		display = null;
		if (width < 300){
			AROLogger.d(TAG, "onCreate (wearable)");
			setContentView(R.layout.aro_legal_terms_wear);
		} else {
			AROLogger.d(TAG, "onCreate (normal)");
			setContentView(R.layout.aro_legal_terms);
		}

		initializeLegalPageControls();
		registerAnalyzerTimeoutReceiver();
		registerAnalyzerLaunchReceiver();
		mKeepActivities = Settings.System.getInt(getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
	}
	
	/**
	 * Closes the current activity
	 */
	private void exitTCActivity() {
		// Close the current summary screen
		AROLogger.d(TAG, "another instance of collector already running, will exit this tc screen");

		finish();
	}

	/**
	 * Reads the legal terms text from html file and creates the web view
	 * 
	 * @throws IOException
	 */
	private void readLegalTermsfromfileX() throws IOException {
		final WebView legalView = (WebView) findViewById(R.id.termsWebView);
		legalView.loadUrl("file:///android_asset/arolegal.html");
	}

	/**
	 * Reads the legal terms text from html file and creates the web view
	 * 
	 * @throws IOException
	 */
	private void readLegalTermsfromfile() throws IOException {
		BufferedReader reader = null;
		try {
			final StringBuilder legalTermsBuilder = new StringBuilder();
			final String result;
			final WebView legalView = (WebView) findViewById(R.id.termsWebView);
			final InputStream input = getResources().openRawResource(R.raw.arolegal);
			reader = new BufferedReader(new InputStreamReader(input), input.available());
			String line;
			while ((line = reader.readLine()) != null) {
				legalTermsBuilder.append(line);
			}
			result = legalTermsBuilder.toString();
			legalView.loadData(result, "text/html", "utf-8");
		} finally {
			reader.close();
		}
	}

	/**
	 * Initializes the legal web view and controls
	 */
	private void initializeLegalPageControls() {
		try {
			readLegalTermsfromfile();
			setupButtons();
		} catch (IOException e) {
			AROLogger.i(TAG, "exception in initializeControls :AROCollectorLegalTermsActivity ", e);
		}

	}

	/**
	 * Sets the event lister for button controls
	 */
	private void setupButtons() {
		final Button acceptButton = (Button) findViewById(R.id.acceptButton);
		final Button cancelButton = (Button) findViewById(R.id.cancelButton);
		acceptButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(TERMS_ACCEPTED);
				if (mKeepActivities != 1)
					finish();
				else {
					startMainActivity();
				}
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setResult(TERMS_REJECTED);
				finish();
			}
		});
	}

	
	private BroadcastReceiver analyzerTimeoutReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context ctx, Intent intent) {
	    	AROLogger.d(TAG, "received analyzerTimeoutIntent at " + System.currentTimeMillis());
	        finish();
	    }
	};
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterTimeoutReceiver();
		unregisterLaunchReceiver();
		AROLogger.d(TAG, "inside onDestroyed, unregistered receivers");
	}
	
	/**
	 * method to unregister the receiver that listens for the analyzer timeout
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
			AROLogger.d(TAG, "Ignoring exception in unregisterTimeoutReceiver", e);
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
			AROLogger.d(TAG, "Ignoring exception in unregisterLaunchReceiver", e);
		}
	}
	
	
	/**
	 * Start the data collector main screen after splash screens timesout
	 */
	private void startMainActivity() {
		final Intent splashScreenIntent = new Intent(getBaseContext(),
				AROCollectorMainActivity.class);
		// Generic Error ID number 100 passed as an argument to navigate to Main
		// Screen without any dialog
		splashScreenIntent.putExtra(ARODataCollector.ERRODIALOGID, 100);
		splashScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(splashScreenIntent);
		finish();
	}
	
}
