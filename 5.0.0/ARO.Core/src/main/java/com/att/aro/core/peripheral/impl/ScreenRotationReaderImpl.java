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
import java.util.Collections;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IScreenRotationReader;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;
import com.att.aro.core.util.Util;

/**
 * Reads the screen rotations information contained in the
 * "screen_rotations" file found inside the trace directory and adds them to
 * the user events list.
 */
public class ScreenRotationReaderImpl extends PeripheralBase implements IScreenRotationReader {

	@InjectLogger
	private static ILogger logger;

	@Override
	public List<UserEvent> readData(String directory, double startTime) {
		List<UserEvent> userEvents = new ArrayList<UserEvent>();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.SCREEN_ROTATIONS_FILE;
		if(!filereader.fileExist(filepath)){
			return userEvents;
		}
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("failed to read screen rotation file: "+filepath);
		}
		if(lines != null){
			for(String line: lines) {
				String[] strFields = line.split(" ");
				// Get timestamp
				double dTimeStamp = Util.normalizeTime(Double.parseDouble(strFields[0]), startTime);
	
				UserEventType eventType = null;
	
				if (strFields[1].contains(TraceDataConst.UserEvent.KEY_LANDSCAPE)) {
					eventType = UserEventType.SCREEN_LANDSCAPE;
				} else if (strFields[1].contains(TraceDataConst.UserEvent.KEY_PORTRAIT)) {
					eventType = UserEventType.SCREEN_PORTRAIT;
				}
	
				userEvents.add(new UserEvent(eventType, dTimeStamp, dTimeStamp + 0.5));
			}
	
			Collections.sort(userEvents, new UserEventSorting());
		}
		return userEvents;
	}

}
