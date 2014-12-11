package com.att.arodatacollector;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;
import com.att.arocollector.utils.AROLogger;

public class AROScreenTraceReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "AROScreenTraceReceiver";
	
	// Screen state on-off boolean flag
	Boolean mScreenOn = false;

	/** Screen brightness value from 0-255 */
	private float mScreenCurBrightness = 0;

	/** Previous Screen brightness value */
	private float mPrevScreenCurBrightness = 1;

	/** Screen timeout (Device sleep) value in seconds */
	private int mScreenTimeout = 0;

	/** Previous Screen timeout (Device sleep) value in seconds */
	private int mPrevScreenTimeout = 0;

	public AROScreenTraceReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "AROScreenTraceReceiver(...)");
		
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		mScreenOn = pm.isScreenOn();
				
		// record the initial state
		recordTrace();
	}
	
	/**
	 * Gets the screen brightness and timeout value from Settings file
	 * 
	 * @throws SettingNotFoundException
	 */
	private void getScreenBrightnessTimeout() {
		try {
			mScreenCurBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			if (mScreenCurBrightness >= 255)
				mScreenCurBrightness = 240;
			// Brightness Min value 15 and Max 255
			mScreenCurBrightness = Math.round((mScreenCurBrightness / 240) * 100);
			mScreenTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
			mScreenTimeout = mScreenTimeout / 1000; // In Seconds
		} catch (SettingNotFoundException e) {
			AROLogger.e(TAG, "exception in getScreenBrigthnessTimeout", e);
		}

	}
	
	/**
	 * prepare content for tracefile
	 */
	private void recordTrace(){
		
		getScreenBrightnessTimeout();
		
		if (mScreenOn) {
			writeTraceLineToAROTraceFile(AroTraceFileConstants.ON + " " + mScreenTimeout + " " + mScreenCurBrightness, true);
			mPrevScreenCurBrightness = mScreenCurBrightness;
			mPrevScreenTimeout = mScreenTimeout;
		} else {
			writeTraceLineToAROTraceFile(AroTraceFileConstants.OFF, true);
			mPrevScreenCurBrightness = mScreenCurBrightness;
			mPrevScreenTimeout = mScreenTimeout;
		}

		if (AROLogger.logDebug) {
			AROLogger.d(TAG, "Screen brightness: " + mScreenCurBrightness);
			AROLogger.d(TAG, "Screen Timeout: " + mScreenTimeout);
		}
	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action="+action);
		this.context = context;

		if (action.equals(Intent.ACTION_SCREEN_OFF)) {
			mScreenOn = false;
		} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
			mScreenOn = true;
		}
		
		recordTrace();
	}

}
