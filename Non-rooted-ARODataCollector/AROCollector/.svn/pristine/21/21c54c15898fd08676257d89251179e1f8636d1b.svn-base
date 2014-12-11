package com.att.arodatacollector;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;

public class AROScreenRotationReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "AROScreenRotationReceiver";

	/**
	 * LandScape Screen orientation
	 */
	private static final String LANDSCAPE_MODE = "landscape";
	
	/**
	 * Portrait Screen orientation
	 */
	private static final String PORTRAIT_MODE = "portrait";
	
	public AROScreenRotationReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "AROScreenRotationReceiver(...)");
		
		//recordScreenRotation();
	}
	
	/**
	 * method to record the screen rotation.
	 */
	private void recordScreenRotation() {
		final Configuration newConfig = context.getResources().getConfiguration();
		
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			writeTraceLineToAROTraceFile(LANDSCAPE_MODE, true);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			writeTraceLineToAROTraceFile(PORTRAIT_MODE, true);
		}
	}
	

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action="+action);
		this.context = context;

		recordScreenRotation();
		
	}
	
}
