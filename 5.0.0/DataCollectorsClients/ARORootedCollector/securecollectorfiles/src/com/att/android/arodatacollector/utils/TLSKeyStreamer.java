package com.att.android.arodatacollector.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.att.android.arodatacollector.utils.AROLogger;

/**
 * This is a util class that will use a background thread to stream the logcat
 * for TLS keys. When stopped, the background thread will create the .ssl file before exiting 
 */
public class TLSKeyStreamer {
	private static final String TAG = "LogcatStreamer";
	private static String keyOutFile = null;
	private static Thread captureThread = null;
	
	public static void startKeyCapture(String outputFile){
		//only start once
		if (captureThread == null){
			keyOutFile = outputFile;
			captureThread = new Thread(new LogcatRunnable());
			captureThread.start();
			AROLogger.d(TAG, "captureThread started");
		}
	}
	
	public static void stopKeyCapture(){
		AROLogger.d(TAG, "inside stopKeyCapture");
		try {
			if (captureThread != null) {
				captureThread.interrupt();
				AROLogger.d(TAG, "interrupt sent");
				captureThread.join();
				AROLogger.d(TAG, "captureThread stopped");
				captureThread = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static class LogcatRunnable implements Runnable {
		private static final String TLSKEY_FILTER = "_TLSKEY_";
		private static final int ONE_SEC_IN_MILLIS = 1000;

		@Override
		public void run() {

			BufferedReader reader = null;
			Process sh = null;
			DataOutputStream os = null;
			DataOutputStream dataOutStream = null;
			try {
				final String clearLogCmd = "logcat -c\n";
				final String viewLogCmd = "logcat -s " + TLSKEY_FILTER + "\n";
				sh = Runtime.getRuntime().exec("su"); AROLogger.e(TAG, "su pid = "+sh);
				os = new DataOutputStream(sh.getOutputStream());
				os.writeBytes(clearLogCmd);
				os.writeBytes(viewLogCmd);

				StreamClearer stderrClearer = new StreamClearer(sh.getErrorStream(), "stderr", true);
				new Thread(stderrClearer).start();

				reader = new BufferedReader(new InputStreamReader(sh.getInputStream()));

				String buf = null;
				AROLogger.d(TAG, "waiting to logcat output");

				final String sslFilePath = keyOutFile;
				dataOutStream = new DataOutputStream(new FileOutputStream(sslFilePath));
				
				//loop while the thread is not interrupted
				while (!Thread.currentThread().isInterrupted()){
					
					//make sure there's data available because the readline will block if there's nothing to read
					while (sh.getInputStream().available() > 0 && (buf = reader.readLine()) != null) {
						buf = buf.trim();
						
						if (buf.length() > 0) {
							AROLogger.d(TAG, ">" + buf);
							if (buf.contains(TLSKEY_FILTER)) {
								TLSKey key = new TLSKey(buf);
								dataOutStream.write(key.getTimestamp());
								//dataOutStream.writeInt(key.getPreMasterLen());
								writeLittleEndianInteger(key.getPreMasterLen(), dataOutStream);
								dataOutStream.write(key.getPreMaster());
								dataOutStream.write(key.getMaster());
							}
						}
					}
					
					Thread.sleep(ONE_SEC_IN_MILLIS);
				}
				AROLogger.d(TAG, "exit while loop");
			} catch (InterruptedException ie){
				//ignore since interruptedException will be thrown if the thread is
				//interrupted while sleeping.
			} catch (Exception e) {
				AROLogger.e(TAG, "Exception caught in LogcatRunnable.run().", e);
			} finally {
				try {
					if (os != null){
						os.writeBytes("exit\n");
						os.close();
					}
					if (reader != null){
						reader.close();
					}
					if (dataOutStream != null) {
						AROLogger.d(TAG, "closed ssl file");
						dataOutStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Write method
	public static void writeLittleEndianInteger(int i, OutputStream ops) throws IOException {
		byte[] buffer = new byte[4];
		buffer[0] = (byte) i;
		buffer[1] = (byte) (i >> 8);
		buffer[2] = (byte) (i >> 16);
		buffer[3] = (byte) (i >> 24);
		ops.write(buffer);
	}
}
