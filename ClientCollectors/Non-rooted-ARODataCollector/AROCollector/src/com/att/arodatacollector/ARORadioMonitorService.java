package com.att.arodatacollector;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;
import com.att.arocollector.utils.AROLogger;

public class ARORadioMonitorService extends AROMonitorService{

	private static final String TAG = "ARORadioMonitorService";
	public static final String ARO_RADIO_MONITOR_SERVICE = "com.att.arodatacollector.ARORadioMonitorService";
	private PhoneStateListener mPhoneStateListener;
	private TelephonyManager mTelphoneManager;

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
		mTelphoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		setARORadioSignalListener();
		mTelphoneManager.listen(mPhoneStateListener
				, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS 
				| PhoneStateListener.LISTEN_CALL_STATE
				| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		
	}
	
	/**
	 * Stops the trace collection
	 */
	@Override
	protected void stopMonitor() {

		if (mPhoneStateListener != null) {
			mTelphoneManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
			mTelphoneManager = null;
			mPhoneStateListener = null;
		}

	}

	/**
	 * Capture the device radio RSSI(signal strength) during the trace
	 * 
	 */
	private void setARORadioSignalListener() {
		mPhoneStateListener = new PhoneStateListener() {
			private TelephonyManager mTelphoneManager;

			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);

				// GSM Radio signal strength in integer value which will be
				// converted to dDm (This is default considered network type)
				String mRadioSignalStrength = String.valueOf(0);
				mTelphoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (signalStrength.isGsm() || mTelphoneManager.getNetworkType() == 13) {

					int mLteSignalStrength = 0;
					int mLteRsrp = 0;
					int mLteRsrq = 0;
					int mLteRssnr = 0;
					int mLteCqi = 0;
					if (mTelphoneManager.getNetworkType() == 13) {
						try {
							mLteSignalStrength = Integer.parseInt(mAroUtils
									.getSpecifiedFieldValues(SignalStrength.class, signalStrength,
											"mLteSignalStrength"));
						} catch (NumberFormatException nmb) {
							AROLogger.e(TAG, "mLteSignalStrength not found in LTE Signal Strength");
						}

						try {
							mLteRsrp = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteRsrp"));
						} catch (NumberFormatException nmb) {
							AROLogger.e(TAG, "mLteRsrp not found in LTE Signal Strength");
						}

						try {
							mLteRsrq = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteRsrq"));
						} catch (NumberFormatException nmb) {
							AROLogger.e(TAG, "mLteRsrq not found in LTE Signal Strength");
						}
						try {
							mLteRssnr = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteRssnr"));
						} catch (NumberFormatException nmb) {
							AROLogger.e(TAG, "mLteRssnr not found in LTE Signal Strength");
						}
						try {
							mLteCqi = Integer.parseInt(mAroUtils.getSpecifiedFieldValues(
									SignalStrength.class, signalStrength, "mLteCqi"));
						} catch (NumberFormatException nmb) {
							AROLogger.e(TAG, "mLteCqi not found in LTE Signal Strength");
						}

					}

					// Check to see if LTE parameters are set
					if ((mLteSignalStrength == 0 && mLteRsrp == 0 && mLteRsrq == 0 && mLteCqi == 0)
							|| (mLteSignalStrength == -1 && mLteRsrp == -1 && mLteRsrq == -1 && mLteCqi == -1)) {

						// No LTE parameters set. Use GSM signal strength
						final int gsmSignalStrength = signalStrength.getGsmSignalStrength();
						if (signalStrength.isGsm() && gsmSignalStrength != 99) {
							mRadioSignalStrength = String.valueOf(-113 + (gsmSignalStrength * 2));
						}
					} else {

						// If hidden LTE parameters were defined and not set to
						// default values, then used them
						mRadioSignalStrength = mLteSignalStrength + " " + mLteRsrp + " " + mLteRsrq
								+ " " + mLteRssnr + " " + mLteCqi;
					}
				}
				/**
				 * If the network type is CDMA then look for CDMA signal
				 * strength values.
				 */
				else if ((mTelphoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA)) {

					mRadioSignalStrength = String.valueOf(signalStrength.getCdmaDbm());
				}
				/**
				 * If the network type is EVDO O/A then look for EVDO signal
				 * strength values.
				 */
				else if (mTelphoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0
						|| mTelphoneManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A) {

					mRadioSignalStrength = String.valueOf(signalStrength.getEvdoDbm());
				}

				if (AROLogger.logVerbose) {
					AROLogger.v(TAG, "signal strength changed to " + mRadioSignalStrength);
				}
				writeTraceLineToAROTraceFile(mRadioSignalStrength, true);
			}

			//added to listen for 4g-3g-2g transitions
			@Override
			public void onDataConnectionStateChanged (int state, int networkType){
				if (AROLogger.logDebug) {
					AROLogger.d(TAG, "entered onDataConnectionStateChanged ");
					AROLogger.d(TAG, "state=" + state + "; networkType=" + networkType);
				}
				
				final ConnectivityManager mAROConnectivityMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				final NetworkInfo mAROActiveNetworkInfo = mAROConnectivityMgr.getActiveNetworkInfo();
				
				final boolean isNetworkConnected = (state == TelephonyManager.DATA_CONNECTED);
//				if (!isFirstBearerChange) {
//					System.out.println("mAROActiveNetworkInfo: "+mAROActiveNetworkInfo);
//					recordBearerAndNetworkChange(mAROActiveNetworkInfo, isNetworkConnected);
//				}
				
			}
		};
	}

}
