/*
 * Copyright 2012 AT&T
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

package com.att.aro.model;

import java.io.Serializable;

/**
 * Encapsulates information about the ScheduledAlarmInfo Info.
 */
public class ScheduledAlarmInfo extends AlarmInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applicationName;
	private int hasFired;

	// Repeat interval in milliseconds
	private int repeatInterval;

	public ScheduledAlarmInfo(String applicationName, double timestampTrace, 
			double timestampEpoch, double timestampElapsed, 
			AlarmType alarmType, int repeatInterval, int hasFired) {
		super(timestampTrace, timestampEpoch, timestampElapsed, alarmType);
		this.applicationName=applicationName;
		this.repeatInterval=repeatInterval;
		this.hasFired = hasFired;
	}

	public String getApplication() {
		return applicationName;
	}

	public int getRepeatInterval() {
		return repeatInterval;
	}

	public int getHasFired() {
		return hasFired;
	}
}