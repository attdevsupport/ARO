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
