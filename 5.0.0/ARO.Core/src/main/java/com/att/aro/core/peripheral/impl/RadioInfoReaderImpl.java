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
package com.att.aro.core.peripheral.impl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IRadioInfoReader;
import com.att.aro.core.peripheral.pojo.RadioInfo;
import com.att.aro.core.util.Util;

/**
 * Reads the Radio data from the file and stores it in the RadioInfo.
 */
public class RadioInfoReaderImpl extends PeripheralBase implements IRadioInfoReader {
	
	@InjectLogger
	private static ILogger logger;
	
	@Override
	public List<RadioInfo> readData(String directory, double startTime) {
		List<RadioInfo> radioInfos = new ArrayList<RadioInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.RADIO_EVENTS_FILE;
		String[] lines = null;
		if (!filereader.fileExist(filepath)) {
			return radioInfos;
		}
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed to read Radio info file: "+filepath);
		}
		
		Double lastDbmValue = null;
		if(lines != null && lines.length > 0){
			for (String strLineBuf : lines) {
	
				String[] strFields = strLineBuf.split(" ");
				try {
					if (strFields.length == 2) {
						double timestampVal = Util.normalizeTime(Double.parseDouble(strFields[0]),startTime);
						double dbmValue = Double.parseDouble(strFields[1]);
	
						// Special handling for lost or regained signal
						if (lastDbmValue != null && timestampVal > 0.0
								&& (dbmValue >= 0.0 || lastDbmValue.doubleValue() >= 0.0)
								&& dbmValue != lastDbmValue.doubleValue()) {
							radioInfos.add(new RadioInfo(timestampVal, lastDbmValue.doubleValue()));
						}
	
						// Add radio event
						radioInfos.add(new RadioInfo(timestampVal, dbmValue));
						lastDbmValue = dbmValue;
					} else if (strFields.length == 6) {
	
						// LTE
						double timestampVal = Util.normalizeTime(Double.parseDouble(strFields[0]),startTime);
						RadioInfo radioInformation = new RadioInfo(timestampVal, Integer.parseInt(strFields[1]),
								Integer.parseInt(strFields[2]), Integer.parseInt(strFields[3]),
								Integer.parseInt(strFields[4]), Integer.parseInt(strFields[5]));
	
						// Special handling for lost or regained signal
						if (lastDbmValue != null
								&& timestampVal > 0.0
								&& (radioInformation.getSignalStrength() >= 0.0 || lastDbmValue.doubleValue() >= 0.0)
								&& radioInformation.getSignalStrength() != lastDbmValue.doubleValue()) {
							radioInfos.add(new RadioInfo(timestampVal, lastDbmValue.doubleValue()));
						}
	
						// Add radio event
						radioInfos.add(radioInformation);
						lastDbmValue = radioInformation.getSignalStrength();
	
					} else {
						logger.warn("Invalid radio_events entry: " + strLineBuf);
					}
				} catch (Exception e) {
					logger.warn("Unexpected error parsing radio event: " + strLineBuf, e);
				}
			}
		}
		return radioInfos;
	}

}
