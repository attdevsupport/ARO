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
