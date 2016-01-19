package com.att.arodatacollector;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;
import com.att.arocollector.utils.AROLogger;

public class ARONetworkdDetailsReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "AROBearerReceiver";
	
	private ConnectivityManager connectivityManager;
	
	/**indicates whether WIFI, MOBILE, or UNKNOWN **/
	private String prevNetwork = AroTraceFileConstants.NOT_ASSIGNED_NETWORK;
	
	private int prevNetworkType;
	
	private String applicationVersion = null;

	private boolean isProduction = false;

	private boolean isFirstBearerChange = true;

	private int currentNetworkType;
	
	
	public ARONetworkdDetailsReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "AROBearerReceiver(...)");

		connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);

		final ConnectivityManager mAROConnectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mAROActiveNetworkInfo = mAROConnectivityMgr.getActiveNetworkInfo();
		currentNetworkType = getDeviceNetworkType(mAROActiveNetworkInfo);
		recordBearerAndNetworkChange(mAROActiveNetworkInfo, true);
	}
	
	public int getDeviceNetworkType() {
			return currentNetworkType;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action="+action);
		this.context = context;
		
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			
			final boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY , false);
			final boolean isNetworkConnected = !noConnectivity;
			
			final ConnectivityManager mAROConnectivityMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo mAROActiveNetworkInfo = mAROConnectivityMgr.getActiveNetworkInfo();
			if (!isFirstBearerChange ) {
				recordBearerAndNetworkChange(mAROActiveNetworkInfo, isNetworkConnected);
			}
		}
			
	}
	
	/**
	 * called by the mAROBearerChangeReceiver and mPhoneStateListener to record:
	 * 		1. bearer change between Wifi-Mobile
	 * 		2. network change between 4G-3G-2G
	 * @param mAROActiveNetworkInfo
	 * @param isNetworkConnected
	 */
	private void recordBearerAndNetworkChange(final NetworkInfo mAROActiveNetworkInfo, final boolean isNetworkConnected){
		
		AROLogger.d(TAG, "enter recordBearerAndNetworkChange()");
		if (    mAROActiveNetworkInfo != null 
				&& isNetworkConnected 
				&& getDeviceNetworkType(mAROActiveNetworkInfo) != TelephonyManager.NETWORK_TYPE_UNKNOWN){
			
			String currentBearer = getCurrentBearer();
			currentNetworkType = getDeviceNetworkType(mAROActiveNetworkInfo);
			if (AROLogger.logDebug){
				AROLogger.d(TAG, "mAROActiveNetworkInfo.state=" + mAROActiveNetworkInfo.getState());
				AROLogger.d(TAG, "mAROPrevBearer=" + prevNetwork + "; currentBearer=" + currentBearer);
				AROLogger.d(TAG, "mAROPrevNetworkType=" + prevNetworkType + "; currentNetworkType=" + currentNetworkType);
			}
			if(!prevNetwork.equals(currentBearer)) {
				//bearer change, signaling a failover
				prevNetwork = currentBearer;
				writeTraceLineToAROTraceFile(Integer.toString(currentNetworkType), true);
				
				if (AROLogger.logDebug){
					AROLogger.d(TAG, "failover, wrote networkType=" + currentNetworkType + " to networkdetails completed at timestamp: " + mAroUtils.getDataCollectorEventTimeStamp());
				}
				prevNetworkType = currentNetworkType;
				//Flurry logs
				final String tempNetworkTypeFlurryState = (getifCurrentBearerWifi() ? AROCollectorUtils.NOT_APPLICABLE : mAROActiveNetworkInfo.getSubtypeName());
				final String tempNetworkInterfaceFlurryState = getifCurrentBearerWifi() ? "WIFI" : "MOBILE";
				
			//	writeToFlurryAndMaintainStateAndLogEvent(networkTypeFlurryEvent, getString(R.string.flurry_param_status), tempNetworkTypeFlurryState, true);
			//	writeToFlurryAndMaintainStateAndLogEvent(networkInterfaceFlurryEvent, getString(R.string.flurry_param_status), tempNetworkInterfaceFlurryState, true);
			}
			//We need to handle case when we switch between 4G-3G-2G ( This is not as handover)
			//-1 - Wifi (We don't want to check for wifi network for 4G-3G-2G transition)
			else if( currentNetworkType != -1 && prevNetworkType != currentNetworkType){
				writeTraceLineToAROTraceFile(Integer.toString(currentNetworkType), true);
				if (AROLogger.logDebug){
					AROLogger.d(TAG, "4g-3g-2g switch, wrote networkType=" + currentNetworkType + " to networkdetails completed at timestamp: " + mAroUtils.getDataCollectorEventTimeStamp());
				}
				//log the 4G-3G-2G network switch
				final String tempNetworkFlurryState = mAROActiveNetworkInfo.getSubtypeName();
			//	writeToFlurryAndMaintainStateAndLogEvent(networkTypeFlurryEvent, getString(R.string.flurry_param_status), tempNetworkFlurryState, true);
				
				prevNetworkType = currentNetworkType;
			}
			// device_details trace file
			if (isFirstBearerChange) {
				isFirstBearerChange = false;
			}

		}
		else {
			AROLogger.d(TAG, "mAROActiveNetworkInfo is null, network is not CONNECTED, or networkType is unknown...exiting recordBearerAndNetworkChange()");
		}
	}

	/**
	 * Gets the current connected bearer
	 * 
	 * @return boolean value to validate if current bearer is wifi
	 */
	private Boolean getifCurrentBearerWifi() {
		int type = 0;
		if (connectivityManager == null)
			return false;
		if (connectivityManager.getActiveNetworkInfo() != null) {
			type = connectivityManager.getActiveNetworkInfo().getType();
		}
		if (type == ConnectivityManager.TYPE_MOBILE) {
			AROLogger.d(TAG, " Connection Type :  Mobile");
			return false;
		} else {
			AROLogger.d(TAG, " Connection Type :  Wifi");
			return true;
		}
	}
	
	/**
	 * returns the value of the current bearer, either WIFI or MOBILE
	 */
	private String getCurrentBearer(){
		
		return getifCurrentBearerWifi() ? "WIFI" : "MOBILE";
	}

	/**
	 * Gets the current connected data network type of device i.e 3G/LTE/Wifi
	 * @param mCurrentNetworkType network info class object to get current network type 
	 * @return mCellNetworkType Current network type
	 */
	private int getDeviceNetworkType(NetworkInfo mCurrentNetworkType) {
		if (AROLogger.logDebug) {
			AROLogger.d(TAG, "getting device network type" + mCurrentNetworkType);
		}
		final TelephonyManager mAROtelManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mAROtelManager.getNetworkType();
				
		// Check if the current network is WiFi *//
		if (mCurrentNetworkType.getType() == 1) {
			networkType = -1;
		}
		return networkType;
			
	}

	/**
	 * Gets the ARO Data Collector application version from the manifest file.
	 * 
	 * @return A string that is the application version.
	 */
	public String getVersion() {
		
		if (applicationVersion  == null) {
			try {
				final String packageName = this.context.getPackageName();
				if (packageName != null) {
					final PackageManager pm = this.context.getPackageManager();
					if (pm != null) {
						final PackageInfo info = pm.getPackageInfo(packageName, 0);
						if (info != null) {
							applicationVersion = info.versionName;
						}
					}
				}
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(TAG, "exception in getVersion", e);
			}
			if (applicationVersion == null) {
				applicationVersion = "";
			} else if (isProduction  ) {
				// if running in production, truncate the build number
				final String[] parts = applicationVersion.split("\\.");
				if (parts.length > 2) {
					final StringBuilder builder = new StringBuilder();
					builder.append(parts[0]);
					builder.append('.');
					builder.append(parts[1]);
					// skip the minor version as well if it's just a 0
					// (e.g. 1.0.0.x = 1.0, 1.0.1.x = 1.0.1)
					if (!parts[2].equals("0")) {
						builder.append('.');
						builder.append(parts[2]);
					}
					applicationVersion = builder.toString();
				}
			}
		}
		return applicationVersion;
	}

}
