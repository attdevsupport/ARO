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
package com.att.aro.mvc;

import java.awt.event.ActionEvent;

import com.att.aro.core.datacollector.IDataCollector;

public class AROCollectorActionEvent extends ActionEvent {

	private static final long serialVersionUID = 1L;
	
	IDataCollector collector;

	private int deviceId;
	private String deviceSerialNumber;

	private String trace;
	private boolean videoCapture;

	public AROCollectorActionEvent(Object source, int eventId, String command, int deviceId, String trace, boolean videoCapture) {
		super(source, eventId, command);
		this.collector = null;
		this.deviceId = deviceId;
		this.trace = trace;
		this.videoCapture = videoCapture;
	}

	/**
	 * 
	 * @param source
	 * @param eventId
	 * @param command
	 * @param collector collector to be used
	 * @param deviceSerialNumber serial number of device
	 * @param trace directory
	 * @param videoCapture true for video capture, false no video
	 */
	public AROCollectorActionEvent(Object source, int eventId, String command, IDataCollector collector, String deviceSerialNumber, String trace, boolean videoCapture) {
		super(source, eventId, command);
		this.collector = collector;
		this.deviceSerialNumber = deviceSerialNumber;
		this.trace = trace;
		this.videoCapture = videoCapture;
	}

	public String getTrace() {
		return trace;
	}

	public boolean isVideoCapture() {
		return videoCapture;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public String getDeviceSerialNumber() {
		return deviceSerialNumber;
	}
	
	public IDataCollector getCollector() {
		return collector;
	}
}
