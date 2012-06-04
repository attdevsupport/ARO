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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

/**
 * Represents the Legal Terms screen of the ARO Data Collector. A user must accept the legal terms in
 * order to continue with the application.  
 * 
 */
public class AROCollectorLegalTermsActivity extends Activity {

	/** Android log TAG string for ARO-Data Collector Legal Screen */
	private static final String TAG = "ARO.LegalTermsActivity";
	
	/** Identifies that the ARO Data Collector legal terms have been accepted. */
	public static int TERMS_ACCEPTED = 1;
	
	/** Identifies that the ARO Data Collector legal terms have been rejected. */
	public static int TERMS_REJECTED = 2;
	
    /**
     * Overriding onCreate initialize data members 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aro_legal_terms);
		initializeLegalPageControls();
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
			reader = new BufferedReader(new InputStreamReader(input),
					input.available());
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
			Log.e(TAG,"exception in initializeControls :AROCollectorLegalTermsActivity ", e);
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

}
