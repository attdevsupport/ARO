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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IUserEventReader;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;
import com.att.aro.core.util.Util;

/**
 * read user event captured in the file
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 6, 2014
 *
 */
public class UserEventReaderImpl extends PeripheralBase implements IUserEventReader {

	@InjectLogger
	private static ILogger logger;
	
	@Override
	public List<UserEvent> readData(String directory, double eventTime0, double startTime) {
		List<UserEvent> userEvents = new ArrayList<UserEvent>();
		Map<UserEventType, Double> lastEvent = new EnumMap<UserEventType, Double>(
				UserEventType.class);
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.USER_EVENTS_FILE;
		
		if (!filereader.fileExist(filepath)) {
			return userEvents;
		}
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("failed to read user event file: "+filepath);
		}
		if(lines != null && lines.length > 0){
			for (String lineBuf : lines) {
	
				// Ignore empty line
				if (lineBuf.trim().isEmpty()) {
					continue;
				}
	
				// Parse entry
				String strFields[] = lineBuf.split(" ");
				if (strFields.length < 0) {
					logger.warn("Found invalid user event entry: " + lineBuf);
					continue;
				}
	
				// Get timestamp
				double dTimeStamp = Double.parseDouble(strFields[0]);
				if (dTimeStamp > 1.0e9) {
					dTimeStamp = Util.normalizeTime(dTimeStamp, startTime);
				} else {
	
					// Old data collector method (backward compatible)
					dTimeStamp -= eventTime0;
				}
	
				// Get event type
				UserEvent.UserEventType actionType = UserEvent.UserEventType.EVENT_UNKNOWN;
				String processedEvent;
				if (strFields.length == 3 && TraceDataConst.UserEvent.SCREEN.equals(strFields[1])) {
					processedEvent = strFields[2];
					actionType = UserEventType.SCREEN_TOUCH;
				} else if (strFields.length == 4 && TraceDataConst.UserEvent.KEY.equals(strFields[1])) {
					processedEvent = strFields[3];
					if (TraceDataConst.UserEvent.KEY_KEY.equals(strFields[2])) {
						actionType = UserEventType.KEY_KEY;
					} else if (TraceDataConst.UserEvent.KEY_POWER.equals(strFields[2])) {
						actionType = UserEventType.KEY_POWER;
					} else if (TraceDataConst.UserEvent.KEY_VOLUP.equals(strFields[2])) {
						actionType = UserEventType.KEY_VOLUP;
					} else if (TraceDataConst.UserEvent.KEY_VOLDOWN.equals(strFields[2])) {
						actionType = UserEventType.KEY_VOLDOWN;
					} else if (TraceDataConst.UserEvent.KEY_BALL.equals(strFields[2])) {
						actionType = UserEventType.KEY_BALL;
					} else if (TraceDataConst.UserEvent.KEY_HOME.equals(strFields[2])) {
						actionType = UserEventType.KEY_HOME;
					} else if (TraceDataConst.UserEvent.KEY_MENU.equals(strFields[2])) {
						actionType = UserEventType.KEY_MENU;
					} else if (TraceDataConst.UserEvent.KEY_BACK.equals(strFields[2])) {
						actionType = UserEventType.KEY_BACK;
					} else if (TraceDataConst.UserEvent.KEY_SEARCH.equals(strFields[2])) {
						actionType = UserEventType.KEY_SEARCH;
					} else if (TraceDataConst.UserEvent.KEY_GREEN.equals(strFields[2])) {
						actionType = UserEventType.KEY_GREEN;
					} else if (TraceDataConst.UserEvent.KEY_RED.equals(strFields[2])) {
						actionType = UserEventType.KEY_RED;
					}
				} else {
					
					logger.warn("Invalid user event type in trace: " + lineBuf);
					continue;
				}
	
				// Get press or release
				boolean bPress = false;
				if (TraceDataConst.UserEvent.PRESS.equalsIgnoreCase(processedEvent)) {
					bPress = true;
				} else if (TraceDataConst.UserEvent.RELEASE.equalsIgnoreCase(processedEvent)) {
					bPress = false;
				} else {
					logger.warn("211 - Key event does not have press/release indication: "+ lineBuf);
				
					continue;
				}
	
				if (bPress) {
					lastEvent.put(actionType, dTimeStamp);
				} else {
					Double lastTime = lastEvent.remove(actionType);
					if (lastTime != null) {
						userEvents.add(new UserEvent(actionType, lastTime, dTimeStamp));
					} else {
						logger.warn("Found key release event with no associated press event: "+ lineBuf);
						continue;
					}
				}
			}
		}
		return userEvents;
	}

}
