package com.att.arodatacollector;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.att.arocollector.R;
import com.att.arocollector.utils.AROCollectorUtils;
import com.att.arocollector.utils.AROLogger;

public class AROBatteryReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "AROBatteryReceiver";
	
	public AROBatteryReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "AROBatteryReceiver(...)");

	}
	

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action="+action);
		this.context = context;
		
		getPowerSourceStateAndBatteryLevel(intent);
		
	}
	
	/**
	 * Method will get Current power source state and Battery level for the
	 * device s * @param intent
	 */
	private void getPowerSourceStateAndBatteryLevel(Intent intent) {
		// AC Power Source boolean flag
		Boolean mPowerSource = false;
		/** Battery level */
		int mBatteryLevel = 0;
		// Battery temperature //
		int mBatteryTemp;
		int status = -1;
		final String action = intent.getAction();
		mBatteryTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
			final Bundle extras = intent.getExtras();
			if (extras != null) {
				status = extras.getInt(BatteryManager.EXTRA_PLUGGED, -1);
				final int rawlevel = intent.getIntExtra("level", -1);
				final int scale = intent.getIntExtra("scale", -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				mBatteryLevel = level;
			}
			if (status != -1) {
				switch (status) {
				
				case 0: //USB Unplugged
					writeTraceLineToAROTraceFile("USB cable detached", true);
//					Intent signalProblem = new Intent("arovpndatacollector.service.close");
//					context.startActivity(signalProblem);
					// VPN collection requires USB connection for video & retrieval of data
//					if(mApp.isCollectorLaunchfromAnalyzer()){
//						AROLogger.d(TAG, "usb disconnected, set dataCollectorStopEnable to true");
//	//TODO					mApp.setDataCollectorStopEnable(true);
//	//TODO					broadcastUsbAction(true);
//					}
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					mPowerSource = true;
					break;
				case BatteryManager.BATTERY_PLUGGED_AC:
					mPowerSource = true;
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					mPowerSource = false;
				default:
					mPowerSource = false;
					break;
				}
			}
		}
		if (AROLogger.logDebug) {
			AROLogger.d(TAG, "received battery level: " + mBatteryLevel);
			AROLogger.d(TAG, "received battery temp: " + mBatteryTemp / 10 + "C");
			AROLogger.d(TAG, "received power source " + mPowerSource);
		}
		writeTraceLineToAROTraceFile(mBatteryLevel + " " + mBatteryTemp / 10
				+ " " + mPowerSource, true);
		
		//write to Flurry only if the values change
		final String tempBatteryString = "level: " + mBatteryLevel + "%" + " " + 
				   "temp: " + mBatteryTemp / 10 + "C" + " " + 
				   "power source: " + mPowerSource;
		writeToFlurryAndMaintainStateAndLogEvent(context.getString(R.string.flurry_param_status), tempBatteryString, true);
	}


	private void writeToFlurryAndMaintainStateAndLogEvent(String string, String tempBatteryString, boolean b) {
		// TODO Auto-generated method stub
		
	}
}
