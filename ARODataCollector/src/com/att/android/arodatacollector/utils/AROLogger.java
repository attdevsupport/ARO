package com.att.android.arodatacollector.utils;

import com.att.android.arodatacollector.BuildConfig;

import android.util.Log;

/**
 * if debug build, print all log statements. If not debug build, only print log
 * statements higher than predefined level.
 */
public class AROLogger {
	//TODO: have a method to detect debug build and set logging level accordingly
	private static final boolean debugBuild = true;
	private static final LogLevel logLevel = LogLevel.DEBUG;
	public static boolean logVerbose = false, logDebug = false, logInfo = false, logWarn = false,
			logError = false;

	private enum LogLevel {
		VERBOSE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5);

		private final int level;

		LogLevel(int level) {
			this.level = level;
		}

		public int value() {
			return level;
		}
	}

	static {
		// just init here since these values don't change
		logVerbose = isLogVerbose();
		logDebug = isLogDebug();
		logInfo = isLogInfo();
		logWarn = isLogWarn();
		logError = isLogError();
	}

	public static void v(String tag, String msg) {
		if (logVerbose) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (logDebug) {
			Log.d(tag, msg);
		}
	}

	public static void d(String tag, String msg, Throwable t) {
		if (logDebug) {
			Log.d(tag, msg, t);
		}
	}

	public static void i(String tag, String msg) {
		if (logInfo) {
			Log.i(tag, msg);
		}
	}

	public static void i(String tag, String msg, Throwable tr) {
		if (logInfo) {
			Log.i(tag, msg, tr);
		}
	}

	public static void w(String tag, String msg) {
		if (logWarn) {
			Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (logWarn) {
			Log.w(tag, msg, tr);
		}
	}

	public static void e(String tag, String msg) {
		if (logError) {
			Log.e(tag, msg);
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (logError) {
			Log.e(tag, msg, tr);
		}
	}

	private static boolean isLogVerbose() {
		//return BuildConfig.DEBUG || LogLevel.VERBOSE.value() >= prodLevel.value();
		return LogLevel.VERBOSE.value() >= logLevel.value();
	}

	private static boolean isLogDebug() {
		//return BuildConfig.DEBUG || LogLevel.DEBUG.value() >= prodLevel.value();
		return LogLevel.DEBUG.value() >= logLevel.value();
	}

	private static boolean isLogInfo() {
		//return BuildConfig.DEBUG || LogLevel.INFO.value() >= prodLevel.value();
		return LogLevel.INFO.value() >= logLevel.value();
	}

	private static boolean isLogWarn() {
		//return BuildConfig.DEBUG || LogLevel.WARN.value() >= prodLevel.value();
		return LogLevel.WARN.value() >= logLevel.value();
	}

	private static boolean isLogError() {
		//return BuildConfig.DEBUG || LogLevel.ERROR.value() >= prodLevel.value();
		return LogLevel.ERROR.value() >= logLevel.value();
	}
}
