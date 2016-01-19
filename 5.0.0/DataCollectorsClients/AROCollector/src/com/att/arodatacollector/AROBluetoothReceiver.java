package com.att.arodatacollector;

import java.io.File;
import java.io.FileNotFoundException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.att.arocollector.R;
import com.att.arocollector.utils.AROCollectorUtils;

public class AROBluetoothReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "AROBluetoothReceiver";
		
	
	public AROBluetoothReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "AROBluetoothReceiver(...)");

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action=" + action);
		this.context = context;

		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

			switch (BluetoothAdapter.getDefaultAdapter().getState()) {
			case BluetoothAdapter.STATE_ON:
				writeTraceLineToAROTraceFile(AroTraceFileConstants.DISCONNCTED, true);
			//	writeToFlurryAndMaintainStateAndLogEvent(getString(R.string.flurry_param_status), AroTraceFileConstants.DISCONNCTED, true);
				break;

			case BluetoothAdapter.STATE_OFF:
				writeTraceLineToAROTraceFile(AroTraceFileConstants.OFF, true);
			//	writeToFlurryAndMaintainStateAndLogEvent(getString(R.string.flurry_param_status), AroTraceFileConstants.OFF, true);
				break;
			}
		}
		if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action) || BluetoothDevice.ACTION_FOUND.equals(action)) {

			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				writeTraceLineToAROTraceFile(AroTraceFileConstants.DISCONNCTED, true);
			//	writeToFlurryAndMaintainStateAndLogEvent(getString(R.string.flurry_param_status), AroTraceFileConstants.DISCONNCTED, true);
			} else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
				writeTraceLineToAROTraceFile(AroTraceFileConstants.CONNECTED, true);
			//	writeToFlurryAndMaintainStateAndLogEvent(getString(R.string.flurry_param_status), AroTraceFileConstants.CONNECTED, true);
			}
		}

	}
	
}
