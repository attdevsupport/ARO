package com.att.arodatacollector;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;

public class ARONullReceiver extends AROBroadcastReceiver{
	
	private static final String TAG = "ARONullReceiver";
		
	
	public ARONullReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException {
		super(context, traceDir, outFileName, mAroUtils);
		
		Log.i(TAG, "ARONullReceiver(...)");

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.i(TAG, "onReceive(...) action="+action);
		this.context = context;
		
			
	}
	
}
