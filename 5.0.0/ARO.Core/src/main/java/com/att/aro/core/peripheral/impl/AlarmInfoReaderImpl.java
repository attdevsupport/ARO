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
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IAlarmInfoReader;
import com.att.aro.core.peripheral.pojo.AlarmInfo;
import com.att.aro.core.peripheral.pojo.AlarmInfo.AlarmType;
import com.att.aro.core.util.Util;

/**
 * Method to read the alarm event from the trace file and store it in the
 * alarmInfos list.
 * 
 * @author EDS team Refactored by Borey Sao Date: October 6, 2014
 */
public class AlarmInfoReaderImpl extends PeripheralBase implements IAlarmInfoReader {
	
	@InjectLogger
	private static ILogger logger;

	@Override
	public List<AlarmInfo> readData(String directory, double dumpsysEpochTimestamp, double dumpsysElapsedTimestamp, Date traceDateTime) {
		List<AlarmInfo> alarmInfos = new ArrayList<AlarmInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.KERNEL_LOG_FILE;
		double timestamp;
		double timestampElapsed;
		double timestampEpoch;
		double timeDeltaEpochElapsed = dumpsysEpochTimestamp - dumpsysElapsedTimestamp;
		AlarmType alarmType;
		String[] lines = null;

		if (!filereader.fileExist(filepath)) {
			return alarmInfos;
		}

		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed to read kernal log file for Alarm Info data: " + filepath);
			return alarmInfos;
		}

		for (String strLineBuf : lines) {
			if (strLineBuf.indexOf("alarm_timer_triggered") > 0) {
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length > 1) {
					try {
						timestamp = 0;
						switch (Integer.parseInt(strFields[strFields.length - 3])) {
						case 0:
							alarmType = AlarmType.RTC_WAKEUP;
							break;
						case 1:
							alarmType = AlarmType.RTC;
							break;
						case 2:
							alarmType = AlarmType.ELAPSED_REALTIME_WAKEUP;
							timestamp = timeDeltaEpochElapsed;
							break;
						case 3:
							alarmType = AlarmType.ELAPSED_REALTIME;
							timestamp = timeDeltaEpochElapsed;
							break;
						default:

							// should not arrive here
							logger.warn("cannot resolve alarm type: " + timestamp 
									+ " type " + Double.parseDouble(strFields[strFields.length - 3]));
							alarmType = AlarmType.UNKNOWN;
							break;
						}

						// convert ns to milliseconds
						timestamp += Double.parseDouble(strFields[strFields.length - 1]) / 1000000;
						timestampEpoch = timestamp;
						timestamp = timestamp - traceDateTime.getTime();
						timestampElapsed = timestampEpoch 
								- dumpsysEpochTimestamp 
								+ dumpsysElapsedTimestamp;
						alarmInfos.add(new AlarmInfo(timestamp, 
								timestampEpoch, 
								timestampElapsed, 
								alarmType));
//						logger.info("Time: " + timestamp 
//								+ "Alarm type: " + alarmType 
//								+ "\nEpoch: " + timestampEpoch
//								+ "\nElapsed: " + timestampElapsed);
					} catch (Exception e) {
						logger.warn("Unexpected error parsing alarm event: " + strLineBuf, e);
					}
				}
			}
		}
		return alarmInfos;
	}

}
