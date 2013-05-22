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

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.utils.AROCollectorUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

/**
 * Represents the Legal Terms screen of the ARO Data Collector. A user must
 * accept the legal terms in order to continue with the application.
 * 
 */
public class AROCollectorLegalTermsActivity extends Activity {

	/**
	 * The boolean value to enable logs based on production build or debug build
	 */
	private static boolean mIsProduction = false;

	/**
	 * A boolean value that indicates whether or not to enable logging for this
	 * class in a debug build of the ARO Data Collector.
	 */
	public static boolean DEBUG = !mIsProduction;
	
	/** Android log TAG string for ARO-Data Collector Legal Screen */
	private static final String TAG = "ARO.LegalTermsActivity";

	/** Identifies that the ARO Data Collector legal terms have been accepted. */
	public static int TERMS_ACCEPTED = 1;

	/** Identifies that the ARO Data Collector legal terms have been rejected. */
	public static int TERMS_REJECTED = 2;

	/**
	 * Initializes data members with a saved instance of an AROCollectorLegalTermsActivity object. 
	 * Overrides the android.app.Activity#onCreate method. 
	 * @param savedInstanceState  A saved instance of an AROCollectorLegalTermsActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aro_legal_terms);
		initializeLegalPageControls();
		
		registerAnalyzerTimeoutReceiver();
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
			Log.e(TAG, "exception in initializeControls :AROCollectorLegalTermsActivity ", e);
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
				finish();
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
	    	if(DEBUG){
	        	Log.i(TAG, "received analyzerTimeoutIntent at " + System.currentTimeMillis());
	        }
	        finish();
	    }
	};
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterTimeoutReceiver();
	}
	
	private void unregisterTimeoutReceiver() {
		if (DEBUG){
			Log.i(TAG, "inside unregisterTimeoutReceiver");
		}
		try {
			if (analyzerTimeoutReceiver != null) {
				unregisterReceiver(analyzerTimeoutReceiver);
				analyzerTimeoutReceiver = null;
				
				if (DEBUG){
					Log.i(TAG, "successfully unregistered analyzerTimeoutReceiver");
				}
			}
		} catch (Exception e){
			Log.i(TAG, "Ignoring exception in unregisterTimeoutReceiver", e);
		}
	}
	
	private void registerAnalyzerTimeoutReceiver() {
		if (DEBUG){
			Log.i(TAG, "registering analyzerTimeOutReceiver");
		}
		registerReceiver(analyzerTimeoutReceiver, new IntentFilter(AROCollectorUtils.ANALYZER_TIMEOUT_SHUTDOWN_INTENT));
	}
	
}
