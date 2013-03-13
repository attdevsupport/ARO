package com.att.aro.util;

import java.util.ResourceBundle;
import com.att.aro.main.ResourceBundleManager;

public final class Util {
	
	private Util() {}
	
	public static final ResourceBundle RB = ResourceBundleManager.getDefaultBundle();
	private static final double TIME_CORRECTION = 1.0E9;

	
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
	
}
