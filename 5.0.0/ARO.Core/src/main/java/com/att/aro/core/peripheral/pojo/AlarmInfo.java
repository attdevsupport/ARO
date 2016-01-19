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
package com.att.aro.core.peripheral.pojo;

/**
 * Encapsulates information about the Alarm Info.
 */
public class AlarmInfo implements Comparable<AlarmInfo> {

	/**
	 * The AlarmInfo.AlarmType Enumeration specifies constant values that
	 * describe the AlarmManager alarm type.
	 */
	// Alarm Triggered timestamp in milliseconds
	private double timestampSinceTrace;
	private double timestampEpoch;
	
	// Elapsed timestamp in milliseconds
	private double timestampElapsed;

	// current wakelock state
	private AlarmType alarmType;
	
	public enum AlarmType {
		RTC_WAKEUP,
		RTC,
		ELAPSED_REALTIME_WAKEUP,
		ELAPSED_REALTIME,
		UNKNOWN
	}

	/**
	 * Initializes an instance of the AlarmInfo class using the specified
	 * timestamp, alarm type.
	 * 
	 * @param timestamp
	 *            The timestamp for alarm triggered.
	 * 
	 * @param alarmType
	 *            Alarm type.
	 */
	public AlarmInfo(double timestampSinceTrace, double timestampEpoch,
			 double timestampElapsed,AlarmType alarmType) {
		this.timestampSinceTrace = timestampSinceTrace;
		this.timestampEpoch = timestampEpoch;
		this.timestampElapsed = timestampElapsed;
		this.alarmType = alarmType;
	}

	/**
	 * Compares the specified AlarmInfo object to this one and returns an int
	 * that indicates the result.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AlarmInfo aInfo) {
		return Double.valueOf(timestampSinceTrace).compareTo(aInfo.timestampSinceTrace);
	}

	/**
	 * Returns the triggered timestamp for an alarm.
	 * 
	 * @return The triggered timestamp since trace in milliseconds
	 */
	public double getTimeStamp() {
		return timestampSinceTrace;
	}

	/**
	 * Returns the triggered timestamp for an alarm.
	 * 
	 * @return The triggered timestamp since epoch in milliseconds
	 */
	public double getTimestampEpoch() {
		return timestampEpoch;
	}

	/**
	 * Returns the triggered timestamp for an alarm.
	 * 
	 * @return The triggered timestamp since elapsed in milliseconds.
	 */
	public double getTimestampElapsed() {
		return timestampElapsed;
	}

	/**
	 * Returns the AlarmType.
	 * 
	 * @return One of the values of the AlarmType enumeration that indicates the
	 *         type of the alarm triggered.
	 */
	public AlarmType getAlarmType() {
		return alarmType;
	}
}
