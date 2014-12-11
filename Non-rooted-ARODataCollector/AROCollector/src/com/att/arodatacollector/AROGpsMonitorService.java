package com.att.arodatacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;

public class AROGpsMonitorService extends Service{

	private static final String TAG = "AROGpsMonitorService";
	public static final String ARO_GPS_MONITOR_SERVICE = "com.att.arocollector.ARO_GPS_MONITOR_SERVICE";


	/** Gps event trace file name */
	private static final String OUTGPSFILENAME = "gps_events";

	/** Location Manager class object */
	private LocationManager mGPSStatesManager;

	/** GPS State listener */
	private GpsStatus.Listener mGPSStatesListner;

	/** Previous GPS enabled state */
	private boolean prevGpsEnabledState = false;

	/** Timer to run every 500 milliseconds to check GPS states */
	private Timer checkLocationService = new Timer();
	
	/** GPS active boolean flag */
	private Boolean mGPSActive = false;

	/**
	 * Camera/GPS/Screen trace timer repeat time value to capture camera events
	 * ( 1/2 seconds)
	 */
	private static int HALF_SECOND_TARCE_TIMER_REPATE_TIME = 1000;

	/** ARO Data Collector utilities class object */
	private AROCollectorUtils mAroUtils;
	
	private File traceDir;
	private String outputTraceFile;
	private FileOutputStream outputTraceFileStream;
	private BufferedWriter bufferedWriter;

	/**
	 * Setup and start monitoring
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand(...)");

		if (mAroUtils == null) {
			mAroUtils = new AROCollectorUtils();
			initFiles(intent);
			createTraceFile(OUTGPSFILENAME);
			
			startAROGpsTraceMonitor();

		}
		return super.onStartCommand(intent, flags, startId);
		
	}

	/**
	 * read Intent for trace directory
	 * @param intent
	 */
	private void initFiles(Intent intent) {
		if (intent != null) {
			Log.i(TAG, "initFiles(Intent " + intent.toString() + ") hasExtras = " + intent.getExtras());
			String traceDirStr = intent.getStringExtra("TRACE_DIR");
			traceDir = new File(traceDirStr);
			traceDir.mkdir();
		} else{
			Log.i(TAG, "intent is null");
		}
	}

	/**
	 * Create deviceinfo file store info
	 */
	private void createTraceFile(String traceFileName) {
		Log.i(TAG, "setDeviceInfo()");
		
		try {
			outputTraceFile = traceDir + "/" + traceFileName;
			outputTraceFileStream = new FileOutputStream(outputTraceFile, true);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputTraceFileStream));
		} catch (FileNotFoundException e) {
			outputTraceFileStream = null;
		}
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

	@Override
	public boolean stopService(Intent name) {
		Log.i(TAG, "stopService()");
		return super.stopService(name);
	}
	
	/**
	 * stop monitoring and close trace file
	 */
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		stopAROGpsTraceMonitor();
		closeTraceFile();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "onBind(...)");
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * Captures the GPS trace data during the trace cycle
	 * 
	 */
	private class GPSStatesListener implements GpsStatus.Listener {

		@Override
		public void onGpsStatusChanged(int event) {

			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				writeTraceLineToAROTraceFile(bufferedWriter, "ACTIVE", true);
			//	writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, getString(R.string.flurry_param_status), "ACTIVE", true);
				mGPSActive = true;
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				writeTraceLineToAROTraceFile(bufferedWriter, AroTraceFileConstants.STANDBY, true);
			//	writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, getString(R.string.flurry_param_status), AroTraceFileConstants.STANDBY, true);
				mGPSActive = false;
				break;
			}
		}
	}

	/**
	 * Starts the GPS peripherals trace collection
	 */
	private void startAROGpsTraceMonitor() {
		Log.i(TAG, "startAROGpsTraceMonitor()");
		mGPSStatesManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mGPSStatesListner = new GPSStatesListener();
		mGPSStatesManager.addGpsStatusListener(mGPSStatesListner);
		
		//write the initial gps state to the trace file
		final boolean initialGpsState = isLocationServiceEnabled();
		writeGpsStateToTraceFile(initialGpsState);
		prevGpsEnabledState = initialGpsState;
		
		checkLocationService.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				// Current GPS enabled state
				final boolean currentGpsEnabledState = isLocationServiceEnabled();
				if (currentGpsEnabledState != prevGpsEnabledState) {
					writeGpsStateToTraceFile(currentGpsEnabledState);
				}
				prevGpsEnabledState = currentGpsEnabledState;
			}
		}, HALF_SECOND_TARCE_TIMER_REPATE_TIME, HALF_SECOND_TARCE_TIMER_REPATE_TIME);
	}
	
	/**
	 * write the gps state to trace file
	 * @param currentGpsEnabledState
	 */
	private void writeGpsStateToTraceFile(final boolean currentGpsEnabledState) {
		if (currentGpsEnabledState) {
			Log.d(TAG, "gps enabled: ");
			if (!mGPSActive) {
				writeTraceLineToAROTraceFile(bufferedWriter, AroTraceFileConstants.STANDBY, true);
				// writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, getString(R.string.flurry_param_status), AroTraceFileConstants.STANDBY, true);
			}
		} else {
			Log.d(TAG, "gps Disabled: ");
			writeTraceLineToAROTraceFile(bufferedWriter, AroTraceFileConstants.OFF, true);
			// writeToFlurryAndMaintainStateAndLogEvent(gpsFlurryEvent, getString(R.string.flurry_param_status), AroTraceFileConstants.OFF, true);
		}
	}

	/**
	 * Stop the GPS peripherals trace collection
	 */
	private void stopAROGpsTraceMonitor() {
		Log.i(TAG, "stopAROGpsTraceMonitor()");
		if (mGPSStatesListner != null) {
			mGPSStatesManager.removeGpsStatusListener(mGPSStatesListner);
			mGPSStatesManager = null;
		}
		checkLocationService.cancel();
	}

	/**
	 * Checks if the GPS radio is turned on and receiving fix
	 * 
	 * @return boolean value to represent if the location service is enabled or
	 *         not
	 */
	private boolean isLocationServiceEnabled() {
		boolean enabled = false;
		// first, make sure at least one provider actually exists
		final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		final boolean gpsExists = (lm.getProvider(LocationManager.GPS_PROVIDER) != null);
		final boolean networkExists = (lm.getProvider(LocationManager.NETWORK_PROVIDER) != null);
		if (gpsExists || networkExists) {
			enabled = ((!gpsExists || lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) && (!networkExists || lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
		}
		return enabled;
	}

	/**
	 * Method write given String message to trace file passed as an argument
	 * outputfilewriter : Name of Trace File writer to which trace has to be
	 * written content : Trace message to be written
	 */
	private void writeTraceLineToAROTraceFile(BufferedWriter outputfilewriter, String content,
			boolean timestamp) {
		try {
			if (outputfilewriter != null){
				
				final String eol = System.getProperty("line.separator");
				if (timestamp) {
					outputfilewriter.write(mAroUtils.getDataCollectorEventTimeStamp() + " " + content + eol);
					outputfilewriter.flush();
				} else {
					outputfilewriter.write(content + eol);
					outputfilewriter.flush();
				}
			}
		} catch (IOException e) {
			// TODO: Need to display the exception error instead of Mid Trace
			// mounted error
		//	mApp.setMediaMountedMidAROTrace(mAroUtils.checkSDCardMounted());
			Log.e(TAG, "exception in writeTraceLineToAROTraceFile", e);
		}
	}


}
