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
package com.att.aro.core.peripheral;

import java.util.Date;
import java.util.List;

import com.att.aro.core.peripheral.pojo.AlarmInfo;

/**
 * Method to read the alarm event from the trace file and store it in the
 * alarmInfos list.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 6, 2014
 */
public interface IAlarmInfoReader {
	List<AlarmInfo> readData(String directory, double dumpsysEpochTimestamp, double dumpsysElapsedTimestamp,
			Date traceDateTime);
}
