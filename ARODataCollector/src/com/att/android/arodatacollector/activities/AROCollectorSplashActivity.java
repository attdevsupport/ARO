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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.att.android.arodatacollector.R;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog.DialogKeyListner;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog.Dialog_CallBack_Error;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog.Dialog_Type;
import com.att.android.arodatacollector.main.AROCollectorCustomDialog.ReadyListener;
import com.att.android.arodatacollector.main.AROCollectorService;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROCollectorUtils;
import com.att.android.arodatacollector.utils.AROLogger;

/**
 * 
 * Represents the Splash screen, which is the first UI screen that appears when
 * the ARO Data Collector is launched and the application data is being
 * initialized. The Splash Screen appears for a specified (configured) duration
 * before the Main screen of the application is displayed.
 * 
 */

public class AROCollectorSplashActivity extends Activity {

	/** Log filter TAG string for ARO-Data Collector Splash Screen */
	private static final String TAG = "ARO.SplashActivity";

	/** Splash Screen UI display timeout in seconds */
	private static final int SPLASH_DISPLAY_TIME = 5000;

	/**
	 * The Screen height pixel value which is used to small screen resolution to
	 * align "Driving text" inside orange circle
	 **/
	private static final int SCREEN_HEIGHT_TOADJUST = 500;

	/** Request code from ARO legal screen page which is used in Splash Screen **/
	private static final int TERMS_ACTIVITY = 1;

	/**
	 * The handler is used to launch the Legal terms page from background thread
	 * once the application initialization is done
	 **/
	private Handler mHandler = new Handler();

	/**
	 * The Application context of the ARO-Data Collector to access the
	 * application data
	 **/
	private ARODataCollector mApp;

	/** The Mutex Object **/
	private Object mMutex = new Object();

	/**
	 * Boolean to keep track if the current splash screen launch has been
	 * aborted
	 **/
	private boolean mAbortSplash = false;

	/**
	 * Boolean variable to check if the application context and application libs
	 * have been initialized after application is launched by the user
	 **/
	private boolean mInitialized = false;

	/** Boolean to check if the activity instance is in current memory **/
	private boolean mActivityFinished = false;
	
	private String mAROTraceFolderNamefromAnalyzer;

	/**
	 * Overrides the onTouchEvent method to handle the ACTION DOWN event.
	 * 
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// go ahead and skip to the next activity by simply touching
		// the screen as long as all initialization is complete
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			synchronized (mMutex) {
				if (!mAbortSplash) {
					AROLogger.d(TAG, "splash screen touched");
					if (mInitialized) {
						AROLogger.d(TAG, "Initialize complete - will abort");
					} else {
						AROLogger.d(TAG, "Initialize still running - will abort when complete");
					}
					mAbortSplash = true;
					mMutex.notify();
				}
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Overrides the onDestroy method to close current activity.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		mApp = null;
		finish();
	}
	
	private void exitSplashActivity() {
		AROLogger.d(TAG, "exiting splash activity");
		finish();
	}
	
	/**
	 * show the error message saying that analyzer launch is already in progress
	 */
	private void showAnalyzerLaunchInProgress() {

		Dialog_Type m_dialog = Dialog_Type.ARO_ANALYZER_LAUNCH_IN_PROGRESS;
		final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
				AROCollectorSplashActivity.this, android.R.style.Theme_Translucent, m_dialog,
				new ReadyListener(){ 
					public void ready(Dialog_CallBack_Error errorcode, boolean success){
						//exit
						finish();
						exitSplashActivity();
					}

				}, new DialogKeyListner(){

					@Override
					public void HandleKeyEvent(String key, Dialog_Type type) {
						if (AROCollectorCustomDialog.BACK_KEY_PRESSED.equalsIgnoreCase(key)){
							//exit
							finish();
							exitSplashActivity();
						}
					}
				});
		
		myDialog.show();
	}

	/**
	 * Initializes data members with a saved instance of an AROCollectorMainActivity object. 
	 * Overrides the android.app.Activity#onCreate method. 
	 * @param savedInstanceState A saved instance of an AROCollectorSplashActivity object.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// The Display object to fetch the current device display properties
		// like height,width
		Display mScreenDisplay;
		super.onCreate(savedInstanceState);
		
		if (AROCollectorService.getServiceObj() != null) {
			AROLogger.d(TAG, "collector already running, going to home screen");
			startAROHomeActivity();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mApp = (ARODataCollector) getApplication();
		
		mScreenDisplay = getWindowManager().getDefaultDisplay();
		mApp.setDeviceScreenHeight(mScreenDisplay.getHeight());
		mApp.setDeviceScreenWidth(mScreenDisplay.getWidth());
		setContentView(R.layout.splash);
		final TextView version = (TextView) findViewById(R.id.version);
		version.setText("Version " + mApp.getVersion());
		if (mScreenDisplay.getHeight() < SCREEN_HEIGHT_TOADJUST) {
			final TextView SplashMessage = (TextView) findViewById(R.id.splash_message);
			final String splashmessagestring = SplashMessage.getText().toString();
			if ((splashmessagestring.startsWith("Do"))) {
				final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.FILL_PARENT);
				layoutParams.setMargins(15, 160, 0, 0);
				SplashMessage.setLayoutParams(layoutParams);
			}
		}
		
		//add check after screen setup to avoid black screen
		if (ARODataCollector.isAnalyzerLaunchInProgress()){
			//already launched via analyzer
			showAnalyzerLaunchInProgress();
			return;
		}
		
		//new launch, clean up old screen
		sendAnalyzerLaunchBroadcast();
		if (isAnalyzerLaunch()){
			AROLogger.d(TAG, "analyzer launch, setAnalyzerLaunchInProgress(true)");
			ARODataCollector.setAnalyzerLaunchInProgress(true);
		}
		
		// we'll need a timer thread to make sure the screen is
		// displayed for at least SPLASH_DISPLAY_TIME seconds
		final Thread timerThread = new Thread(new Runnable() {
			public void run() {
				if (AROLogger.logDebug){
					AROLogger.d(TAG, "timerThread started at timestamp:" + System.currentTimeMillis());
				}
				
				long timeRemaining = SPLASH_DISPLAY_TIME;
				long stopTime = System.currentTimeMillis() + timeRemaining;

				synchronized (mMutex) {
					do {
						try {
							if (timeRemaining == 0) {
								// we already ran out of time so we must be
								// waiting for initialization to complete
								mMutex.wait();
							} else {
								mMutex.wait(timeRemaining);
							}
						} catch (InterruptedException e) {
							AROLogger.e(TAG, "Splash Screen InterruptedException", e);

						}
						if (timeRemaining > 0) {
							// recalculate time remaining
							long currentTime = System.currentTimeMillis();
							if (currentTime < stopTime) {
								timeRemaining = stopTime - currentTime;
							} else {
								timeRemaining = 0;
							}
						}
					} while (!mActivityFinished
							&& (!mInitialized || (!mAbortSplash && timeRemaining > 0)));
				}

				if (!mActivityFinished) {
					mHandler.post(new Runnable() {
						public void run() {
							if (AROLogger.logDebug){
								AROLogger.d(TAG, "checking for root access at timestamp:" + System.currentTimeMillis());
							}
							
							if (mApp != null){
								
								if (mApp.hasRootAccess()){
									acceptLegalTerms();
									// TODO : EULA Method Call
								}
								else {
									showNoRootAccessDialog();
								}
							}
							else {
								AROLogger.e(TAG, "mApp is null");
							}
						}
					});
				}
			}
		});
		// and another thread to do whatever initialization is needed
		final Thread initializeThread = new Thread(new Runnable() {
			public void run() {
				if (AROLogger.logDebug){
					AROLogger.d(TAG, "start initializeThread at timestamp:" + System.currentTimeMillis());
				}
				try {
					if (mApp != null){
						mApp.initARODataCollector();
					}
				} finally {
					if (AROLogger.logDebug){
						AROLogger.d(TAG, "initialization complete at timestamp:" + System.currentTimeMillis());
					}
					synchronized (mMutex) {
						mInitialized = true;
						mMutex.notify();
					}
				}
			}
		});
		timerThread.start();
		initializeThread.start();
	}

	/**
	 * Shows the Legal terms page to user
	 */
	private void acceptLegalTerms() {
		startActivityForResult(new Intent(AROCollectorSplashActivity.this,
				AROCollectorLegalTermsActivity.class), TERMS_ACTIVITY);
	}

	/**
	 * Handles the result of an activity performed on the UI of the Splash screen 
	 * when calling activity from the current instance has finished and released memory. 
	 * Overrides the android.app.Activity# onActivityResult method.
	 * 
	 * @param requestCode
	 *            A code representing a request to the UI of the Splash screen.
	 * @param resultCode
	 *            A code representing the result of an activity performed on the UI of the Splash screen.
	 * @param data
	 *            An Intent object that contains data associated with the result of the activity.
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TERMS_ACTIVITY) {
			AROLogger.d(TAG, "inside onActivityResult, resultCode=" + resultCode);
			if (resultCode == AROCollectorLegalTermsActivity.TERMS_ACCEPTED) {
				AROLogger.d(TAG, "inside onActivityResult, starting main activity");
				startMainActivity();
			} else {
				AROLogger.d(TAG, "inside onActivityResult, term not accepted, closing activity");
				
				/*if (resultCode == AROCollectorLegalTermsActivity.TERMS_REJECTED){
					resetAnalyzerLaunchWaitingIndicator();
				}*/
				finish();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private boolean isAnalyzerLaunch(){
		return getIntent().getExtras() != null;
	}

	/**
	 * Start the data collector main screen after splash screens timesout
	 */
	private void startMainActivity() {
		
		//TODO:Adding code to launch Data Collector from Analyzer
		final Bundle apkCommandLineParameters  = getIntent().getExtras();
		if (apkCommandLineParameters != null) {
			//sendAnalyzerLaunchBroadcast();
			mAROTraceFolderNamefromAnalyzer = apkCommandLineParameters.getString("TraceFolderName");			
			ARODataCollector.setTcpDumpTraceFolderName(getApplicationContext(), mAROTraceFolderNamefromAnalyzer);
		}
//		else
//		{
//			ARODataCollector.setTcpDumpTraceFolderName(getApplicationContext(), null);
//		}
		
		final Intent splashScreenIntent = new Intent(getBaseContext(),
				AROCollectorMainActivity.class);
		// Generic Error ID number 100 passed as an argument to navigate to Main
		// Screen without any dialog
		splashScreenIntent.putExtra(ARODataCollector.ERRODIALOGID, 100);
		splashScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(splashScreenIntent);
		finish();
	}

	/**
	 * Need to send a broadcast so that other screens from the previous
	 * collector instance can know to exit themselves.
	 */
	private void sendAnalyzerLaunchBroadcast() {
		sendBroadcast(new Intent(AROCollectorUtils.ANALYZER_LAUNCH_CLEANUP_INTENT));
	}

	/**
	 * Closes the current activity and display Home Screen of Data Collector,
	 * This screen could be accessed from context menu when trace cycle in on
	 */
	private void startAROHomeActivity() {
		// Close the current splash activity
		startActivity(new Intent(this, AROCollectorHomeActivity.class));
		finish();
	}

	/**
	 * show a custom dialog notifying the user that ARO requires root access to run
	 */
	private void showNoRootAccessDialog() {
		//this showNoRootAccessDialog() is called from a background thread, 
		//need to use the main UI thread to show the dialog
		AROCollectorSplashActivity.this.runOnUiThread(new Runnable(){
			
			public void run(){
				Dialog_Type m_dialog = Dialog_Type.NO_ROOT_ACCESS;
				
				final AROCollectorCustomDialog myDialog = new AROCollectorCustomDialog(
						AROCollectorSplashActivity.this, android.R.style.Theme_Translucent, m_dialog,
						new ReadyListener(){ 
							public void ready(Dialog_CallBack_Error errorcode, boolean success){
								//on callback, just exit the activity
								finish();
							}
						}, new DialogKeyListner(){

							@Override
							public void HandleKeyEvent(String key, Dialog_Type type) {
								if (AROCollectorCustomDialog.BACK_KEY_PRESSED.equalsIgnoreCase(key)){
									//exit aro
									finish();
								}
							}
						});
				
				myDialog.show();
			}
		});
	}

}
