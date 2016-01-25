package com.att.arodatacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;

public abstract class AROMonitorService extends Service{

	private static final String TAG = "xxxxx";

	/** ARO Data Collector utilities class object */
	protected AROCollectorUtils mAroUtils;
	
	protected File traceDir;
	protected String outputTraceFile;
	protected FileOutputStream outputTraceFileStream;
	protected BufferedWriter bufferedWriter;
	protected String traceFileName;

	private List<RunningAppProcessInfo> mActiveProcessprocess;

	/**
	 * Setup and start monitoring
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand(...)");

		if (mAroUtils == null) {
			mAroUtils = new AROCollectorUtils();
			initFiles(intent);
			
	//		startTraceMonitor();

		}
		return super.onStartCommand(intent, flags, startId);
		
	}

	/**
	 * read Intent for trace directory
	 * @param intent
	 */
	protected void initFiles(Intent intent) {
		
		if (intent != null) {
			
			Log.i(TAG, "initFiles(Intent " + intent.toString() + ") hasExtras = " + intent.getExtras());
			String traceDirStr = intent.getStringExtra("TRACE_DIR");
			traceFileName = intent.getStringExtra("TRACE_FILE_NAME");

			traceDir = new File(traceDirStr);
			traceDir.mkdir();

			try {
				outputTraceFile = traceDir + "/" + traceFileName;
				outputTraceFileStream = new FileOutputStream(outputTraceFile, true);
				bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputTraceFileStream));
			} catch (FileNotFoundException e) {
				outputTraceFileStream = null;
			}

		} else {
			Log.i(TAG, "intent is null");
		}
	}
	
//	/**
//	 * Create file store info
//	 */
//	protected void createTraceFile(String traceFileName) {
//		Log.i(TAG, "setDeviceInfo()");
//		
//		try {
//			outputTraceFile = traceDir + "/" + traceFileName;
//			outputTraceFileStream = new FileOutputStream(outputTraceFile, true);
//			bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputTraceFileStream));
//		} catch (FileNotFoundException e) {
//			outputTraceFileStream = null;
//		}
//	}


	/**
	 * stop monitoring and close trace file
	 */
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		stopMonitor();
		closeTraceFile();
		super.onDestroy();
	}

	/**
	 * Close the trace file
	 */
	public void closeTraceFile(){
		Log.i(TAG, "Close TraceFile :"+outputTraceFile);
		try {
			bufferedWriter.close();
			outputTraceFileStream.close();
		} catch (IOException e) {
			Log.e(TAG, "closeTraceFile() <"+outputTraceFile+"> IOException :"+e.getMessage());
		}
	}
	
	/**
	 * Stops the monitor
	 */
	abstract protected void stopMonitor();


	/**
	 * 
	 * @param content
	 * @param timestamp
	 */
	protected void writeTraceLineToAROTraceFile(String content, boolean timestamp) {
		try {
			if (bufferedWriter != null) {

				final String eol = System.getProperty("line.separator");
				if (timestamp) {
					bufferedWriter.write(mAroUtils.getDataCollectorEventTimeStamp() + " " + content + eol);
					bufferedWriter.flush();
				} else {
					bufferedWriter.write(content + eol);
					bufferedWriter.flush();
				}
			}
		} catch (IOException e) {
			// TODO: Need to display the exception error instead of Mid Trace
			// mounted error
			// mApp.setMediaMountedMidAROTrace(mAroUtils.checkSDCardMounted());
			Log.e(TAG, "exception in writeTraceLineToAROTraceFile", e);
		}
	}
	
	/**
	 * Checks the state of process is background
	 * 
	 * @param process
	 * 
	 *            name
	 * @return boolean value to represent the if package state is background
	 */
	protected boolean checkCurrentProcessState(String processname) {
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActiveProcessprocess = mActivityManager.getRunningAppProcesses();
		for (final Iterator<RunningAppProcessInfo> iterator = mActiveProcessprocess.iterator(); iterator.hasNext();) {
			final RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) iterator.next();
			final String pSname = runningAppProcessInfo.processName.toLowerCase();
			final int pImportance = runningAppProcessInfo.importance;
			if (pSname.contains(processname.toLowerCase()) && !pSname.contains(":")) {
				switch (pImportance) {
				case RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks the state of process is foreground
	 * 
	 * @param process
	 *            name
	 * 
	 * @return boolean value to represent the if package state is foreground
	 */
	protected boolean checkCurrentProcessStateForGround(String processname) {
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActiveProcessprocess = mActivityManager.getRunningAppProcesses();
		for (final Iterator<RunningAppProcessInfo> iterator = mActiveProcessprocess.iterator(); iterator.hasNext();) {
			final RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) iterator.next();
			final String pSname = runningAppProcessInfo.processName.toLowerCase();
			final int pImportance = runningAppProcessInfo.importance;
			if (pSname.contains(processname.toLowerCase())) {
				switch (pImportance) {
				case RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Starts the active process trace by logging all running process in the
	 * trace file
	 */
	protected void startAROActiveProcessTrace() {
		// mActiveProcessStates //
		String[] mActiveProcessStates;
		final ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		mActiveProcessprocess = mActivityManager.getRunningAppProcesses();
		mActiveProcessStates = new String[mActiveProcessprocess.size()];
		for (final Iterator<RunningAppProcessInfo> iterator = mActiveProcessprocess.iterator(); iterator
				.hasNext();) {
			final RunningAppProcessInfo runningAppProcessInfo = (RunningAppProcessInfo) iterator
					.next();
			final int pImportance = runningAppProcessInfo.importance;
			int Index = 0;
			switch (pImportance) {

			case RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
				mActiveProcessStates[Index] = "Name:" + runningAppProcessInfo.processName + " State:" + AroTraceFileConstants.IMPORTANCE_BACKGROUND;
			//	writeTraceLineToAROTraceFile(mActiveProcessTracewriter, mActiveProcessStates[Index], true);

			//	//Flurry only allows max of 10 parameters to an event; if exceed, event is not logged.
			//	if (backgroundAppsFlurryEvent.getCounter() < 10) {
			//	mApp.writeToFlurry(backgroundAppsFlurryEvent.getMapToWrite(), runningAppProcessInfo.processName, 
			//			AROCollectorUtils.EMPTY_STRING + backgroundAppsFlurryEvent.incrementCounter(), 
			//			backgroundAppsFlurryEvent.getEventName(), AROCollectorUtils.NOT_APPLICABLE, AROCollectorUtils.EMPTY_STRING);
			//	}

				Index++;
				break;

			case RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
				mActiveProcessStates[Index] = "Name:" + runningAppProcessInfo.processName
						+ " State:" + AroTraceFileConstants.IMPORTANCE_FOREGROUND;
			//	writeTraceLineToAROTraceFile(mActiveProcessTracewriter, mActiveProcessStates[Index], true);
				Index++;
				break;
			}
		}
	}

//	@Override
//	public boolean stopService(Intent name) {
//		Log.i(TAG, "stopService()");
//		return super.stopService(name);
//	}
	

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "onBind(...)");
		// TODO Auto-generated method stub
		return null;
	}

	


}
