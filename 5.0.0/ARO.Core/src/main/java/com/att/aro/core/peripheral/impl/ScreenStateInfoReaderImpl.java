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
import com.att.aro.core.peripheral.IScreenStateInfoReader;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo.ScreenState;
import com.att.aro.core.util.Util;

/**
 * Method to read the Screen State data from the trace file and store it in
 * the ScreenStateInfos list.
 */
public class ScreenStateInfoReaderImpl extends PeripheralBase implements IScreenStateInfoReader {

	@InjectLogger
	private static ILogger logger;
	
	@Override
	public List<ScreenStateInfo> readData(String directory, double startTime,
			double traceDuration) {
		
		List<ScreenStateInfo> screenStateInfos = new ArrayList<ScreenStateInfo>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.SCREEN_STATE_FILE;
		
		if (!filereader.fileExist(filepath)) {
			return  screenStateInfos;
		}
		
		double beginTime = 0.0;
		double endTime = 0.0;
		ScreenState prevScreenState = null;
		ScreenState screenState;
		String prevBrigtness = null;
		String brightness = null;
		int prevTimeOut = 0;
		int timeout = 0;
		String firstLine;
		String strLineBuf;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed to open Screen State info file: "+filepath);
		}
		
		if (lines != null && lines.length > 0) {
			firstLine = lines[0];
			String strFieldsFirstLine[] = firstLine.split(" ");
			if (strFieldsFirstLine.length >= 2) {
				try {
					beginTime = Util.normalizeTime(Double.parseDouble(strFieldsFirstLine[0]),startTime);

					if (TraceDataConst.SCREEN_ON.equals(strFieldsFirstLine[1])) {
						prevScreenState = ScreenState.SCREEN_ON;
						if (strFieldsFirstLine.length >= 4) {
							prevTimeOut = Integer.parseInt(strFieldsFirstLine[2]);
							prevBrigtness = strFieldsFirstLine[3];
						}
					} else if (TraceDataConst.SCREEN_OFF.equals(strFieldsFirstLine[1])) {
						prevScreenState = ScreenState.SCREEN_OFF;
					} else {
						logger.warn("Unknown screen state: " + firstLine);
						prevScreenState = ScreenState.SCREEN_UNKNOWN;
					}

				} catch (Exception e) {
					logger.warn("Unexpected error in screen events: " + firstLine, e);
				}
			} else {
				logger.warn("Unrecognized screen state event: " + firstLine);
			}
			for (int i=1;i<lines.length;i++) {
				strLineBuf = lines[i];
				String strFields[] = strLineBuf.split(" ");
				if (strFields.length >= 2) {
					try {
						endTime = Util.normalizeTime(Double.parseDouble(strFields[0]),startTime);
						brightness = null;
						timeout = 0;
						if (TraceDataConst.SCREEN_ON.equals(strFields[1])) {
							screenState = ScreenState.SCREEN_ON;
							if (strFields.length >= 4) {
								timeout = Integer.parseInt(strFields[2]);
								brightness = strFields[3];
							}
						} else if (TraceDataConst.SCREEN_OFF.equals(strFields[1])) {
							screenState = ScreenState.SCREEN_OFF;
						} else {
							logger.warn("Unknown screen state: " + strLineBuf);
							screenState = ScreenState.SCREEN_UNKNOWN;
						}

						ScreenStateInfo screenInfo = new ScreenStateInfo(beginTime, endTime,
								prevScreenState, prevBrigtness, prevTimeOut);
						screenStateInfos.add(screenInfo);
						prevScreenState = screenState;
						prevBrigtness = brightness;
						prevTimeOut = timeout;
						beginTime = endTime;
					} catch (Exception e) {
						logger.warn("Unexpected error in screen events: "+ strLineBuf, e);
					}
				} else {
					logger.warn("Unrecognized screen state event: " + strLineBuf);
				}
			}
			screenStateInfos.add(new ScreenStateInfo(beginTime, traceDuration,
					prevScreenState, prevBrigtness, prevTimeOut));

		}
		return screenStateInfos;
	}


}
