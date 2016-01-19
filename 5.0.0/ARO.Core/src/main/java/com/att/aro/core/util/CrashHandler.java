package com.att.aro.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.att.aro.core.AROConfig;
import com.att.aro.core.ILogger;
import com.att.aro.core.analytics.IGoogleAnalytics;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		IGoogleAnalytics googleAnalytics = GoogleAnalyticsUtil.getGoogleAnalyticsInstance();
		googleAnalytics.sendCrashEvents(convertTracetoString("", throwable), "");
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(AROConfig.class);
		context.getBean(ILogger.class).error("Uncaught ARO Exception:", throwable);
		context.close();
	}

	public static String convertTracetoString(String message, Throwable throwable){
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		int max = message.length() < 100 ? message.length():100;
		String exceptionAsString = (message+'\n'+sw.toString()).substring(0, max);
		return exceptionAsString;
	}
	
}
