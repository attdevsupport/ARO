/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Util {

	public static final String OS_NAME = System.getProperty("os.name");
	public static final String OS_ARCHYTECTURE = System.getProperty("os.arch");
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private static final double TIME_CORRECTION = 1.0E9;
	
	public static boolean isMacOS(){
		return Util.OS_NAME.contains("Mac OS");
	}
	
	public static boolean isWindowsOS(){
        return Util.OS_NAME.contains("Windows");
    }
	
	/**
	 * Returns the path to the Java Application
	 * 
	 * @return path to the Application
	 */
	public static String getAppPath() {
		
		return System.getProperty("user.dir");
		
//		File filepath = new File(Util.class.getProtectionDomain().getCodeSource().getLocation().getPath());
//		return filepath.getParentFile().getParent();
	}

	/**
	 * Returns package.Class::methodName of method enclosing call to this method
	 * @return
	 */
	public static String getMethod() {
		StackTraceElement traceElement = Thread.currentThread().getStackTrace()[2];
		String name = null; // traceElement.getClassName() + "::" + traceElement.getMethodName();
		name = ((traceElement.getFileName()).split("\\."))[0] + "::" + traceElement.getMethodName()+"(...)";
		return name;
	}

	/**
	 * location to save trace data such as pcap, video etc. used by non-rooted IOS
	 * @return
	 */
	public static String getAROTraceDirIOS(){
		return System.getProperty("user.home") + FILE_SEPARATOR + "AROTraceIOS";
	}
	
	/**
	 * location of AroLibrary
	 * @return
	 */
	public static String getAroLibrary(){
		return System.getProperty("user.home") + FILE_SEPARATOR + "AroLibrary";
	}
	
	/**
	 * location to save trace data such as pcap, video etc. used by non-rooted Android
	 * @return
	 */
	public static String getAROTraceDirAndroid(){
		return System.getProperty("user.home") + FILE_SEPARATOR + "AROTraceAndroid";
	}
	
	/**
	 * will return the full path of dir where ARO.jar is running from.
	 * @return full path of directory
	 */
	public static String getCurrentRunningDir(){
		String dir = "";
		File filepath = new File(Util.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		dir = filepath.getParent();
		return dir;
	}
	
	/**
	 * Escape regular expression char so that it won't execute
	 * @param str String to escape special chars
	 * @return
	 */
	public static String escapeRegularExpressionChar(String str){
		String token = str.replace("$", "\\$");
		token = token.replace("^", "\\^");
		token = token.replace("*", "\\*");
		token = token.replace(".", "\\.");
		token = token.replace("?", "\\?");
		return token;
	}
	
	/**
	 * Returns a string representing Unknown App if appName is empty, blank, or null.
	 * Otherwise returns appName.
	 */
	public static String getDefaultAppName(String appName){
		return getDefaultString(appName, "unknown");
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
	
	public static String formatHHMMSS(int seconds){
		String theTime = "";
		int sec = seconds % 60;
		seconds /= 60;
		int minute = seconds % 60;
		seconds /= 60;
		int hour = seconds % 60;
		try {
			theTime = String.format("%02d:%02d:%02d", hour, minute, sec);
		} catch (Exception exception) {
			theTime = exception.getMessage();
		}
		return theTime;
	}

	/** 
	 * Convert remaining time (-0h00m00s000ms) to milliseconds
	 *
	 * @return result in milliseconds
	 */
	public static double convertTime(String time) {
    	double result = 0;
		int start = 0;
    	int end = 0;

    	// Change to positive number
    	if (time.indexOf("-") == 0 || time.indexOf("+") == 0) {
			time = time.substring(1);
    	}
    	if (time.indexOf('T')==0){
			time = time.substring(1);    		
    	}
		end = time.indexOf("d");
    	if (end > 0) {
			result += Integer.parseInt(time.substring(0, end)) * 24 * 60 * 60 * 1000;
			start = end+1;
    	}
    	end = time.indexOf("h");
		if (end > start) {
			result += Integer.parseInt(time.substring(start, end)) * 60 * 60 * 1000;
    		start = end+1;
		}
    	end = time.indexOf("m");
		if (end > start && end != time.indexOf("ms")) {
			result += Integer.parseInt(time.substring(start, end)) * 60 * 1000;
    		start = end+1;
		}
    	end = time.indexOf("s");
		if (end > start && end != (time.indexOf("ms") + 1)) {
			result += Integer.parseInt(time.substring(start, end)) * 1000;
    		start = end+1;
		}
    	end = time.indexOf("ms");
		if (end > start) {
			result += Integer.parseInt(time.substring(start, end));
    	}
		return result;
	}
	
	/**
	 * Date format pattern used to parse HTTP date headers in RFC 1123
	 * format.
	 */
	private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

	/**
	 * Date format pattern used to parse HTTP date headers in RFC 1036
	 * format.
	 */
	private static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

	/**
	 * Date format pattern used to parse HTTP date headers in ANSI C
	 * <code>asctime()</code> format.
	 */
	private static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
	private static final String PATTERN_ASCTIME2 = "EEE MMM d HH:mm:ss zzz yyyy";

	private static DateFormat rfc1123 = new SimpleDateFormat(PATTERN_RFC1123);
	private static DateFormat rfc1036 = new SimpleDateFormat(PATTERN_RFC1036);
	private static DateFormat asctime = new SimpleDateFormat(PATTERN_ASCTIME);
	private static DateFormat asctime2 = new SimpleDateFormat(PATTERN_ASCTIME2);
	private static DateFormat[] dateFormats = { rfc1123, rfc1036, asctime, asctime2 };

	private static final Date BEGINNING_OF_TIME = new Date(0);

	/**
	 * Parses HTTP date formats. Synchronized because DateFormat objects are
	 * not thread-safe. If defaultForExpired is true and value is an invalid
	 * dateFormat (such as -1 or 0 meaning already expired), the returned
	 * Date will be "beginning of time" Jan 1 1970.
	 * 
	 * @param value
	 * @param defaultForExpired
	 *            boolean - true/false provide default "beginning of time"
	 *            Jan 1 1970 GMT Date
	 * @return formated Date value else null.
	 */
	public static  Date readHttpDate(String value, boolean defaultForExpired) {
		if (value != null) {
			for (DateFormat dateFormat : dateFormats) {
				try {
					return dateFormat.parse(value.trim());
				} catch (ParseException e) {
					//logger.error(e.getMessage());
				}
			}
		}
		
		if (defaultForExpired) {
			return BEGINNING_OF_TIME;
		}
		
		//logger.warn("Unable to parse HTTP date: " + value);
		return null;
	}

	/**
	 * Pull the given file name from Jar and write to the local drive for use on
	 * emulator
	 * 
	 * @param filename
	 */
	public static String makeLibFilesFromJar(String filename) {
		String homePath = System.getProperty("user.home");
		String targetLibFolder = homePath + File.separator + "AroLibrary";
		ClassLoader aroClassloader = Util.class.getClassLoader();
		try {
			InputStream is = aroClassloader.getResourceAsStream(filename);
			if (is!=null){
				File libfolder = new File(targetLibFolder);
//				if (!libfolder.exists() || !libfolder.isDirectory() || new File(libfolder+File.separator+filename).exists()) {
					targetLibFolder = makeLibFolder(filename, libfolder);
					if (targetLibFolder!=null)
						makeLibFile(filename, targetLibFolder, is);
					else 
						return null;
//				}
			} 
			return targetLibFolder;
		} catch(Exception e){
			return null;
		}
	}

	/**
	 * makes a folder in the targetLibFolder location and if it fails it makes the folder in the local folder where
	 * the code is being from
	 * @param filename
	 * @param currentRelativePath
	 * @param targetLibFolder
	 * @return
	 */
	public static String makeLibFolder(String filename, File libFolder) {
		String targetLibFolder = libFolder.toPath().toString(); 
		Path currentRelativePath = Paths.get("");
		try {
			Files.createDirectories(libFolder.toPath());
		} catch(IOException ioe1) {
			// if no write access rights to the path folder then extract the lib to a default local folder 
			targetLibFolder = currentRelativePath.toAbsolutePath().toString()+File.separator+"AROLibrary";						
			try {
				Files.createDirectories(libFolder.toPath());
			} catch(IOException ioe2) {
				return null;
			}
		}
		return targetLibFolder;
	}

	/**
	 * makes a file inside the targetLibFolder
	 * @param filename
	 * @param targetLibFolder
	 * @param is
	 */
	public static boolean makeLibFile(String filename, String targetLibFolder, InputStream is) {
		try {
			File result = new File(targetLibFolder, filename);// make the target file in the new lib folder
			OutputStream os = null;
			if (result.createNewFile()) {
				os = new FileOutputStream(result);
				byte[] buffer = new byte[4096];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				try {
					is.close();
					os.close();
				} catch(IOException ioe2) {
					// todo
				}
			}
			return true;
		} catch(Exception ioe){
			return false;
		}
	}

	/**
	 * Load the JNI library directly by using the file name
	 * @param filename
	 * @param targetLibFolder
	 */
	public static boolean loadLibrary(String filename, String targetLibFolder) {
		try {
			System.load(targetLibFolder + File.separator + filename);
			return true;
		} catch (Exception e) {
			return false;
		}
	}	
}
