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

public class AROCpuTraceReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "AROCpuTraceReceiver";
	

	public AROCpuTraceReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "AROCpuTraceReceiver(...)");
		
				
		recordTrace();
	}
	
	/**
	 * prepare content for tracefile
	 */
	private void recordTrace(){
		
	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action="+action);
		this.context = context;

		
		recordTrace();
	}

}
