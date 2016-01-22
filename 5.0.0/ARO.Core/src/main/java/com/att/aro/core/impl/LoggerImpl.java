/**
 *  Copyright 2016 AT&T
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
package com.att.aro.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.aro.core.ILogger;
import com.att.aro.core.util.CrashHandler;
import com.att.aro.core.util.GoogleAnalyticsUtil;

public class LoggerImpl implements ILogger {

	Logger logger;
	public LoggerImpl(String className){
		logger = LoggerFactory.getLogger(className);
	}
	@Override
	public void debug(String message) {
		StringBuffer source = getSource();
		logger.debug(wrapMessage(message, source));
	}
	@Override
	public void debug(String message, Throwable throwable) {
		StringBuffer source = getSource();
		logger.debug(wrapMessage(message, source), throwable);
	}

	@Override
	public void error(String error) {
		StringBuffer source = getSource();
		logger.error(wrapMessage(error, source));
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendExceptionEvents(error, source.substring(0, source.indexOf(":")), false);	
	}
	@Override
	public void error(String error, Throwable throwable) {
		StringBuffer source = getSource();
		logger.error(wrapMessage(error, source), throwable);
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendExceptionEvents(CrashHandler.convertTracetoString(error, throwable), source.substring(0, source.indexOf(":")), false);	
	}

	@Override
	public void info(String info) {
		StringBuffer source = getSource();
		logger.info(wrapMessage(info, source));
	}
	
	@Override
	public void info(String info, Throwable throwable) {
		StringBuffer source = getSource();
		logger.info(wrapMessage(info, source), throwable);
	}
	@Override
	public void warn(String warn) {
		StringBuffer source = getSource();
		logger.warn(wrapMessage(warn, source));
	}
	@Override
	public void warn(String warn, Throwable throwable) {
		StringBuffer source = getSource();
		logger.warn(wrapMessage(warn, source), throwable);
	}
	
	public StringBuffer getSource(){
		StringBuffer sbMessage = new StringBuffer();
			if (Thread.currentThread().getStackTrace().length>3) {
				StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
				sbMessage.append(traceElement.getMethodName());
				sbMessage.append(" ");
				sbMessage.append( ((traceElement.getFileName()).split("\\."))[0]);
				sbMessage.append(":");
				sbMessage.append(traceElement.getLineNumber());
				sbMessage.append(" ");
			}
		return sbMessage;
	}
	
	public String wrapMessage(String message, StringBuffer source) {
		source.append(message);
		return source.toString();
	}

}
