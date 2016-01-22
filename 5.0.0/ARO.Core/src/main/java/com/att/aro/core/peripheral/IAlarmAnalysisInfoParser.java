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
package com.att.aro.core.peripheral;

import java.util.Date;
import java.util.List;

import com.att.aro.core.peripheral.pojo.AlarmAnalysisInfo;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisResult;

/**
 * Parsing alarm dumpsys file to collect all the triggered alarms in the summary.
 */

public interface IAlarmAnalysisInfoParser {
	AlarmAnalysisResult parse(String directory, String fileName, String osVersion, 
			double dumpsysEpochTimestamp, double dumpsysElapsedTimestamp, Date traceDateTime);
	List<AlarmAnalysisInfo> compareAlarmAnalysis(List<AlarmAnalysisInfo> end, 
			List<AlarmAnalysisInfo> start);
}
