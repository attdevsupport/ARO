package com.att.android.arodatacollector.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamClearer implements Runnable {
	private static String TAG = "StreamClearer";
	InputStream streamToClear = null;
	boolean logStream = false;
	String name = null;

	public StreamClearer(InputStream is, String name, boolean logStream) {
		streamToClear = is;
		this.logStream = logStream;
		this.name = name;
	}

	@Override
	public void run() {

		final BufferedReader reader = new BufferedReader(new InputStreamReader(streamToClear));
		String buf = null;

		if (AROLogger.logDebug || logStream) {
			AROLogger.d(TAG, "StreamClearer start processing logging content from shell's " + name);
		}

		try {
			while ((buf = reader.readLine()) != null) {
				buf = buf.trim();
				if (logStream && buf.length() > 0) {
					AROLogger.e(TAG, name + ">" + buf + "\n");
				}
			}
		} catch (IOException e) {
			AROLogger.e(TAG, "StreamClearer IOException in StreamClearer", e);
		}

		if (AROLogger.logDebug || logStream) {
			AROLogger.d(TAG, "StreamClearer done processing logging content from shell's " + name);
		}
	}
}
