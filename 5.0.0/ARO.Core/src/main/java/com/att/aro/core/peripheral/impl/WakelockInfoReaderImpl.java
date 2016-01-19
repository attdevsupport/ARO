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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IWakelockInfoReader;
import com.att.aro.core.peripheral.pojo.WakelockInfo;
import com.att.aro.core.peripheral.pojo.WakelockInfo.WakelockState;
import com.att.aro.core.util.Util;

/* 
 * Method to read the Wakelock data from the batteryinfo file and store it in the
 * wakelockInfos list.
 *
 * pre: call readAlarmDumpsysTimestamp(), it requires a timestamp for alignment.
 *
 * */
public class WakelockInfoReaderImpl extends PeripheralBase implements IWakelockInfoReader {

	@InjectLogger
	private static ILogger logger;
	
	@Override
	public List<WakelockInfo> readData(String directory, String osVersion, double dumpsysEpochTimestamp, Date traceDateTime){
		List<WakelockInfo> wakelockInfos = new ArrayList<WakelockInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.BATTERYINFO_FILE;
		String[] lines = null;
		if (!filereader.fileExist(filepath)) {
			return wakelockInfos;
		}
		if (osVersion != null && osVersion.compareTo("2.3") < 0) {
			logger.info("OS 2.2(Froyo) or earlier does not has the wakelock timeline");
		}
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed to read Wakelock Info file: "+filepath);
		}
		if(lines != null && lines.length > 0){
			WakelockState wakelockState;
			for (String strLineBuf : lines) {
	
				// Look for +/-wake_lock
				int index = strLineBuf.indexOf(TraceDataConst.WAKELOCK_ACQUIRED);
				if (index < 0) {
					index = strLineBuf.indexOf(TraceDataConst.WAKELOCK_RELEASED);
				}
	
				// If either +/-wake_lock found
				if (index > 0) {
					String actualWakelockstate = strLineBuf.substring(index, index+10);
					String strFields[] = strLineBuf.trim().split(" ");
					try {
					
						// Get timestamp of wakelock event
						double bTimeStamp = dumpsysEpochTimestamp - Util.convertTime(strFields[0]);
						if (bTimeStamp > traceDateTime.getTime()) {
							bTimeStamp = (bTimeStamp - traceDateTime.getTime())/1000;
							if (TraceDataConst.WAKELOCK_RELEASED.equals(actualWakelockstate)) {
								wakelockState = WakelockState.WAKELOCK_RELEASED;
							} else {
								wakelockState = WakelockState.WAKELOCK_ACQUIRED;
							}
							wakelockInfos.add(new WakelockInfo(bTimeStamp, wakelockState));
							logger.info("Trace Start: " 
									+ traceDateTime.getTime() 
									+ "\nWakelock Time: " + bTimeStamp 
									+ " Wakelock state: " + actualWakelockstate 
									+ " strFields " + Arrays.toString(strFields));
						}
					} catch (Exception e) {
						logger.warn("Unexpected error parsing wakelock event: "
										+ Arrays.toString(strFields) 
										+ " found wakelock in " + index, e);
					}
				}
			}
		}
		return wakelockInfos;
	}
}
