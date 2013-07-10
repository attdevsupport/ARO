/*
 *  Copyright 2013 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.main.ResourceBundleManager;

public final class Util {
	
	private Util() {}
	
	private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
	public static final ResourceBundle RB = ResourceBundleManager.getDefaultBundle();
	private static final double TIME_CORRECTION = 1.0E9;
	public static final String TRAFFIC_FILE = RB.getString("datadump.trafficFile");
	public static final String OS_NAME = System.getProperty("os.name");
	public static final String OS_ARCHYTECTURE = System.getProperty("os.arch");
	public static final String FILE_SEPARATOR = System.getProperty("file.separator"); 
	//platform independent directory to store temporary files
	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	
	/**
	 * Returns a string representing Unknown App if appName is empty, blank, or null.
	 * Otherwise returns appName.
	 */
	public static String getDefaultAppName(String appName){
		return getDefaultString(appName, RB.getString("aro.unknownApp"));
	}
	
	
	/**
	 * Returns defaultStr if str is empty, blank, or null.
	 * Otherwise returns str.
	 */
	public static String getDefaultString(String str, String defaultStr) {
		return isEmptyIsBlank(str) ? defaultStr : str;
	}

	/**
	 * Returns false if sting is empty, blank, or null
	 */
	public static Boolean isEmptyIsBlank(String str) {

		return (str == null || str.trim().isEmpty());
	
	}


	/**
	 * Normalizes the collected time with respect to the trace start time.
	 * 
	 * @param time The time value to be normalized.
	 * @param pcapTime The trace start time.
	 * @return The normalized time in double.
	 */
	public static double normalizeTime(double time, double pcapTime) {
	
		double tmpTime;
		// The comparison check here is for backward compatibility
		tmpTime = time > TIME_CORRECTION ? time - pcapTime : time;
		if (tmpTime < 0) {
			tmpTime = 0.0;
		}
		return tmpTime;
	}
	
	/**
	 * Opens URL to a text file and returns content as String
	 * 
	 * @param url
	 * @return String - content of file at URL
	 * @throws IOException
	 */
	public static String fetchFile(URL url) throws IOException {
		if (LOGGER.isLoggable(Level.FINER)){
			LOGGER.finer("Connecting to - " + url.toExternalForm());
		}
		HttpURLConnection connection = null;
		StringBuilder contentSb = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept", "*/*");

			if (200 == connection.getResponseCode()) {
				InputStream response = connection.getInputStream();
				contentSb = new StringBuilder(
						(int) (response.available() * 1.1));
				BufferedReader reader = null;
				reader = new BufferedReader(new InputStreamReader(response));
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						contentSb.append(line).append('\n');
					}
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
				
				if (LOGGER.isLoggable(Level.FINER)){
					LOGGER.finer("Connection succeeded");
				}
			}

		} finally {
			if (connection != null) {
				if (LOGGER.isLoggable(Level.FINER)){
					LOGGER.finer("Disconnecting");
				}
				connection.disconnect();
			}
		}
		
		if (contentSb != null) {
			return contentSb.toString();
		} else {
			return "";
		}
	}
	
}
