/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.peripheral.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.ScheduledAlarmInfo;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IAlarmAnalysisInfoParser;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisInfo;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisResult;
import com.att.aro.core.peripheral.pojo.AlarmInfo.AlarmType;
import com.att.aro.core.util.Util;

/**
 * Parsing alarm dumpsys file to collect all the triggered alarms in the
 * summary.
 * 
 * @author EDS team Refactored by Borey Sao Date: October 2, 2014
 *
 */
public class AlarmAnalysisInfoParserImpl extends PeripheralBase implements IAlarmAnalysisInfoParser {
	
	@InjectLogger
	private static ILogger logger;
	
	@Override
	public AlarmAnalysisResult parse(String directory, String fileName, String osVersion, double dumpsysEpochTimestamp, double dumpsysElapsedTimestamp, Date traceDateTime) {
		AlarmAnalysisResult result = new AlarmAnalysisResult();
		String filepath = directory + Util.FILE_SEPARATOR + fileName;
		Map<String, List<ScheduledAlarmInfo>> scheduledAlarms = new HashMap<String, List<ScheduledAlarmInfo>>();
		List<AlarmAnalysisInfo> statistics = new ArrayList<AlarmAnalysisInfo>();
		result.setStatistics(statistics);
		result.setScheduledAlarms(scheduledAlarms);

		boolean alarmStats = false;
		Pattern patternAlarms = Pattern.compile("\\s+(\\d+)\\salarms:.+");
		Pattern patternRunning = Pattern.compile("\\s+(\\d+)ms.+,\\s(\\d+)\\swakeups");
		Pattern patternAlarmRepeat = Pattern.compile("\\s+type=(\\d+)\\swhen=\\+?(\\w+)\\srepeatInterval=(\\d+)\\scount=(\\d+)\\s*");
		String applicationName = "";
		int totalScheduledAlarms = 0;
		String strLineBuf;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("failed to read Alarm info file: " + filepath);
		}
		if (lines != null) {
			for (int lineCursor = 0; lineCursor < lines.length; lineCursor++) {
				strLineBuf = lines[lineCursor];
				// Parsing Scheduled Alarm
				if (fileName.equals(TraceDataConst.FileName.ALARM_END_FILE) && !alarmStats) {
					String scheduledAlarmLine[] = strLineBuf.trim().split(" ");
					AlarmType alarmType = null;
					if (scheduledAlarmLine[0].equals("RTC_WAKEUP")) {
						alarmType = AlarmType.RTC_WAKEUP;
					} else if (scheduledAlarmLine[0].equals("RTC")) {
						alarmType = AlarmType.RTC;
					} else if (scheduledAlarmLine[0].equals("ELAPSED_WAKEUP")) {
						alarmType = AlarmType.ELAPSED_REALTIME_WAKEUP;
					} else if (scheduledAlarmLine[0].equals("ELAPSED")) {
						alarmType = AlarmType.ELAPSED_REALTIME;
					}
					if (alarmType != null) {
						totalScheduledAlarms++;
						String temp = scheduledAlarmLine[scheduledAlarmLine.length - 1];
						String appName = temp.substring(0, temp.length() - 1);
						String nextLine = lines[++lineCursor];
						Matcher alarmMatcher = patternAlarmRepeat.matcher(nextLine);
						if (alarmMatcher.matches()) {

							/* timestamp in milliseconds */
							double whenNextEpoch = 0;
							double whenNextTrace = 0;
							double whenNextElapsed = 0;

							/**
							 * pre-gingerbread devices use epoch timestamp /
							 * elapsed timestamp instead of a readable format.
							 */
							if (osVersion != null && osVersion.compareTo("2.3") < 0) {
								if (alarmType == AlarmType.RTC_WAKEUP || alarmType == AlarmType.RTC) {
									whenNextEpoch = Double.parseDouble(alarmMatcher.group(2));
									whenNextElapsed = whenNextEpoch - dumpsysEpochTimestamp + dumpsysElapsedTimestamp;
								} else {
									whenNextElapsed = Double.parseDouble(alarmMatcher.group(2));
									whenNextEpoch = whenNextElapsed - dumpsysElapsedTimestamp + dumpsysEpochTimestamp;
								}
							} else {
								double remainingTimeForNextAlarm = Util.convertTime(alarmMatcher.group(2));
								whenNextEpoch = remainingTimeForNextAlarm + dumpsysEpochTimestamp;
								whenNextElapsed = remainingTimeForNextAlarm + dumpsysElapsedTimestamp;
							}
							whenNextTrace = whenNextEpoch - traceDateTime.getTime();

							// round to 3 decimal places.
							whenNextElapsed = (double) Math.round(whenNextElapsed * 1000) / 1000;
							double repeatInterval = Double.parseDouble(alarmMatcher.group(3));
							double count = Double.parseDouble(alarmMatcher.group(4));
							List<ScheduledAlarmInfo> adding;
							if (scheduledAlarms.containsKey(appName)) {
								adding = scheduledAlarms.get(appName);
							} else {
								adding = new ArrayList<ScheduledAlarmInfo>();
							}
							adding.add(new ScheduledAlarmInfo(appName, whenNextTrace, whenNextEpoch, whenNextElapsed, alarmType, repeatInterval, count));
							scheduledAlarms.put(appName, adding);
							if (logger != null) {
								logger.debug("alarmAnalysisInfoParser \n" + appName 
										+ "\nElapsed: " + whenNextElapsed 
										+ "\nEpoch: " + whenNextEpoch 
										+ "\nFrom trace start: " + whenNextTrace);
							}
						} else {
							logger.debug("Application Name Not Found: " + appName);
						}
						++lineCursor;
					}
				}

				// Locate expired alarms
				if (alarmStats) {
					String running = "";
					if (applicationName != null && !applicationName.trim().equals("")) {
						running = strLineBuf;
					} else {
						applicationName = strLineBuf;
						running = lines[++lineCursor];
					}
					Matcher run = patternRunning.matcher(running);
					double runningTime = 0;
					double wakeups = 0;
					if (run.matches()) {
						logger.info("RUNNING: " + run.group(1) + " wakeups: " + run.group(2));
						runningTime = Double.parseDouble(run.group(1));
						wakeups = Double.parseDouble(run.group(2));
					}
					logger.info("APPLICATION: " + applicationName + " running " + running + "ms");

					// Gathering alarm intents of an application
					List<String> intents = new ArrayList<String>();
					double totalAlarmFiredofThatApplication = 0;

					// Alarm may not have any fired intent
					++lineCursor;
					String alarms = null;
					if (lineCursor < lines.length) {
						alarms = lines[lineCursor];
						if (alarms != null) {
							Matcher alarmsFired = patternAlarms.matcher(alarms);
							while (alarms != null && ++lineCursor < lines.length && alarmsFired.matches()) {
								totalAlarmFiredofThatApplication += Double.parseDouble(alarmsFired.group(1));
								intents.add(alarms.trim());
								alarms = lines[lineCursor];
								if (alarms != null) {
									alarmsFired = patternAlarms.matcher(alarms);
								}
							}
						}
					}
					// Adding AlarmAnalysisInfo to the return list
					statistics.add(new AlarmAnalysisInfo(applicationName, runningTime, wakeups, totalAlarmFiredofThatApplication, intents));

					// Cache the name of next application which has been read
					if (alarms != null) {
						applicationName = alarms;
					}
				}
				if (!alarmStats && strLineBuf.indexOf("Alarm Stats:") > 0) {
					logger.info("Alarm Stats Found" + strLineBuf);
					alarmStats = true;
				}
			}
			logger.info("Number of scheduled alarm = " + totalScheduledAlarms 
					+ "\n Number of apps has scheduled alarms: " + scheduledAlarms.size());
		}
		return result;
	}

	/**
	 * Comparing Alarm Stats before/after capture
	 *
	 * @param end
	 *            List<AlarmAnalysisInfo> of alarms triggered summary at end of
	 *            capture.
	 * @param start
	 *            List<AlarmAnalysisInfo> of alarms triggered summary at start
	 *            of capture.
	 *
	 *            Return the difference
	 */
	@Override
	public List<AlarmAnalysisInfo> compareAlarmAnalysis(List<AlarmAnalysisInfo> end, List<AlarmAnalysisInfo> start) {

		Iterator<AlarmAnalysisInfo> itrAlarmAnalysisInfoS;
		ListIterator<AlarmAnalysisInfo> itrAlarmAnalysisInfoE;
		Pattern patternAlarms = Pattern.compile("(\\d+)\\salarms:\\s(.+)");
		itrAlarmAnalysisInfoE = end.listIterator();

		/**
		 * Go through alarms history in END_FILE and loop through START_FILE to
		 * find the matching application
		 */
		while (itrAlarmAnalysisInfoE.hasNext()) {
			AlarmAnalysisInfo alarmInfoE = (AlarmAnalysisInfo) itrAlarmAnalysisInfoE.next();

			/**
			 * Start iterator from beginning for every new applications as the
			 * lists are not in any order
			 */
			double totalFired = 0;
			itrAlarmAnalysisInfoS = start.iterator();
			AlarmAnalysisInfo alarmInfoS = (AlarmAnalysisInfo) itrAlarmAnalysisInfoS.next();

			// Application look up in START_FILE
			while (!alarmInfoS.getApplication().equals(alarmInfoE.getApplication()) && itrAlarmAnalysisInfoS.hasNext()) {
				alarmInfoS = (AlarmAnalysisInfo) itrAlarmAnalysisInfoS.next();
			}

			// If no matching application, skip to next
			if (!alarmInfoS.getApplication().equals(alarmInfoE.getApplication())) {
				logger.info("no matching application - " + alarmInfoE.getApplication());
				continue;
			}

			/**
			 * Found matching application Note down the difference in Alarm
			 * Stats
			 */
			double wakeup = alarmInfoE.getWakeup() - alarmInfoS.getWakeup();
			double running = alarmInfoE.getRunning() - alarmInfoS.getRunning();
			logger.debug("running: " + running);
			/**
			 * Discard those applications didn't fire any alarms using running
			 * time as reference.
			 */
			if (running > 0) {
				List<String> intentS = alarmInfoS.getIntent();
				List<String> intentE = alarmInfoE.getIntent();
				Iterator<String> itStringStart;
				ListIterator<String> itStringEnd = intentE.listIterator();

				String stringStart;
				String stringEnd;

				/**
				 * Iterate through all alarms under the found application. to
				 * compare the applications' intents
				 */
				while (itStringEnd.hasNext()) {
					itStringStart = intentS.iterator();
					if (itStringStart.hasNext()) {
						stringStart = itStringStart.next();
						stringEnd = itStringEnd.next();
						Matcher startingMatcher = patternAlarms.matcher(stringStart);
						Matcher endingMatcher = patternAlarms.matcher(stringEnd);

						// Search for matching alarms intent from matching application
						while (startingMatcher.matches() 
								&& endingMatcher.matches() 
								&& !startingMatcher.group(2).equals(endingMatcher.group(2))
								) {
							if (itStringStart.hasNext()) {
								stringStart = (String) itStringStart.next();
								startingMatcher = patternAlarms.matcher(stringStart);
							} else {
								break;
							}
						}

						// Found matching fired intent, update the statistics here
						if (startingMatcher.group(2).equals(endingMatcher.group(2))) {
							double alarms = Double.parseDouble(endingMatcher.group(1)) - Double.parseDouble(startingMatcher.group(1));
							if (alarms > 0) {
								totalFired += alarms;
								itStringEnd.set(alarms + " alarms: " + endingMatcher.group(2));
							} else {
								itStringEnd.remove();
								logger.debug("compareAlarmAnalysis: alarm intent discarded - " + endingMatcher.group(2));
							}
						} else {
							totalFired += Double.parseDouble(endingMatcher.group(1));
							logger.debug("No matching alarm intent found \n Total fired = " + endingMatcher.group(1) + " " + stringEnd);
						}
					} else {
						break;
					}
				}

				// Replacing updated AlarmAnalysisInfo
				AlarmAnalysisInfo replacing = new AlarmAnalysisInfo(alarmInfoE.getApplication(), running, wakeup, totalFired, alarmInfoE.getIntent());
				itrAlarmAnalysisInfoE.set(replacing);
			} else {
				itrAlarmAnalysisInfoE.remove();
				logger.debug("compareAlarmAnalysis: history discarded - " + alarmInfoE.getApplication());
			}
		}
		return end;
	}
}
