package com.att.arodatacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.util.Log;

import com.att.arocollector.utils.AROCollectorUtils;
import com.att.arocollector.utils.AROLogger;

public abstract class AROBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "AROBroadcastReceiver";
	protected AROCollectorUtils mAroUtils;
	protected Context context;
	private FileOutputStream fileOutputStream;
	private BufferedWriter bufferedWriter;
	private String outputFile;

	/**
	 * Create logfile and opens a BufferedWriter OutputStream
	 * 
	 * 
	 * @param context
	 * @param traceDir
	 * @param outFileName
	 * @param mAroUtils
	 * @throws FileNotFoundException
	 */
	public AROBroadcastReceiver(Context context, File traceDir, String outFileName, AROCollectorUtils mAroUtils) throws FileNotFoundException{
		Log.i(TAG, "AROBroadcastReceiver(...)");
		
		if (outFileName != null) {
			outputFile = traceDir + "/" + outFileName;
			this.context = context;
			fileOutputStream = new FileOutputStream(outputFile, true);
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
			this.mAroUtils = mAroUtils;
		}
	}

	/**
	 * Close the trace file
	 */
	public void closeTraceFile(){
		Log.i(TAG, "Close TraceFile :"+outputFile);
		try {
			bufferedWriter.close();
			fileOutputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "closeTraceFile() <"+outputFile+"> IOException :"+e.getMessage());
		}
	}

	/**
	 * Method write given String message to trace file passed as an argument
	 * outputfilewriter : Name of Trace File writer to which trace has to be
	 * written content : Trace message to be written
	 */
	protected void writeTraceLineToAROTraceFile(String content, boolean timestamp) {
		try {
			if (fileOutputStream != null) {

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
			// TODO: Need to display the exception error instead of Mid Trace mounted error
			// TODO: mApp in secure collector is an Application
			// mApp.setMediaMountedMidAROTrace(mAroUtils.checkSDCardMounted());
			AROLogger.e(TAG, "exception in writeTraceLineToAROTraceFile", e);
		}
	}
}
