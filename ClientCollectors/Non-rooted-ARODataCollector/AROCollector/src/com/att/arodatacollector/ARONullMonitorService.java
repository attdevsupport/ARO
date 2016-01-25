package com.att.arodatacollector;

import android.content.Intent;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;

public class ARONullMonitorService extends AROMonitorService{

	private static final String TAG = "ARONullMonitorService";
	public static final String ARO_NULL_MONITOR_SERVICE = "com.att.arodatacollector.ARONullMonitorService";

	/**
	 * Setup and start monitoring
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand(...)");

		if (mAroUtils == null) {
			mAroUtils = new AROCollectorUtils();
			initFiles(intent);
			
			startARO_TraceMonitor();

		}
		return super.onStartCommand(intent, flags, startId);
		
	}

	/**
	 * Starts the GPS peripherals trace collection
	 */
	private void startARO_TraceMonitor() {
		Log.i(TAG, "startAROCameraTraceMonitor()");
		
	}
	
	/**
	 * Stops the trace collection
	 */
	@Override
	protected void stopMonitor(){
		
	}

}
