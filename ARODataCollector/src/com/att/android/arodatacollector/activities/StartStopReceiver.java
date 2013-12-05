package com.att.android.arodatacollector.activities;

import java.io.File;

import com.att.android.arodatacollector.main.AROCollectorService;
import com.att.android.arodatacollector.main.AROCollectorTraceService;
import com.att.android.arodatacollector.main.ARODataCollector;
import com.att.android.arodatacollector.utils.AROLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartStopReceiver extends BroadcastReceiver
{
	private static String TAG = "ARO.StartStopReceiver";
	
	public static final String ACTION_START_ARO	="com.att.android.arodatacollector.STARTARO";
	public static final String ACTION_STOP_ARO	="com.att.android.arodatacollector.STOPARO";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		AROLogger.d(TAG, "Received \"" + intent.getAction() + "\"");
		if (intent.getAction().equals(ACTION_START_ARO))
		{
			ARODataCollector.setARODataCollectorStopFlag(context, false);		
			ARODataCollector.setDataCollectorInProgressFlag(context,true);
			ARODataCollector.setRequestDataCollectorStop(false);
			ARODataCollector.setVideoCaptureFailed(context, false);
			
			createAROTraceDirectory(context);
			
			//Takes a snap shoot of the time the system booted to be used for the timer on the home page.
			ARODataCollector.setElapsedTimeStartTime(context, System.currentTimeMillis());
			
			// Starting the ARO Data collector service before tcpdump to record
			// >=t(0)
			context.startService(new Intent(context, AROCollectorTraceService.class));
			// Starting the tcpdump service and starts the video capture
			context.startService(new Intent(context, AROCollectorService.class));
			
		}
		else if (intent.getAction().equals(ACTION_STOP_ARO))
		{
			context.stopService(new Intent(context, AROCollectorTraceService.class));
			context.stopService(new Intent(context, AROCollectorService.class));
			
			ARODataCollector.setTcpDumpTraceFolderName(context,null);
		}
		else
		{
			AROLogger.e(TAG, intent.getAction() + " is an unexpected action, doing nothing");
		}			
	}
	
	/**
	 * Creates the given trace directory on the device SD card under root
	 * directory of ARO (\SDCARD\ARO)
	 * 
	 */
	private void createAROTraceDirectory(Context ctx) 
	{			
		String mAroTraceDatapath = ARODataCollector.getTcpDumpTraceFolderName(ctx);
		File traceFolder = new File(mAroTraceDatapath);
		File traceRootFolder = new File(ARODataCollector.ARO_TRACE_ROOTDIR);

		AROLogger.d(TAG, "mAroTraceDatapath=" + mAroTraceDatapath);

		// Creates the trace root directory
		if (!traceRootFolder.exists()) {
			traceRootFolder.mkdir();
		}
		// Creates the trace directory inside /SDCARD/ARO
		if (!traceFolder.exists()) {
			traceFolder.mkdir();
		}
	}

}
