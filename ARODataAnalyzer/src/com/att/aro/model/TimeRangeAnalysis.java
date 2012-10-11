/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.Serializable;

/**
 * Encapsulates Time Range Analysis information.
 */
public class TimeRangeAnalysis implements Serializable {
	private static final long serialVersionUID = 1L;

	private double startTime;
	private double endTime;
	private long totalBytes;
	private long payloadLen; // bytes
	private double activeTime;
	private double energy;

	/**
	 * Initializes an instance of the TimeRangeAnalysis class, using the specified start and 
	 * end times, total number of bytes transferred, payload length, active state time, and energy value. 
	 * 
	 * @param startTime The start of the time range (in seconds). 
	 * @param endTime The end of the time range (in seconds). 
	 * @param totalBytes The total bytes transferred, including all packet headers. 
	 * @param payloadLen The length of the payload in bytes.
	 * @param activeTime The total amount of high energy radio time. 
	 * @param energy The amount of energy used to deliver the payload.
	 */
	public TimeRangeAnalysis(double startTime, double endTime, long totalBytes,
			long payloadLen, double activeTime, double energy) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.totalBytes = totalBytes;
		this.payloadLen = payloadLen;
		this.activeTime = activeTime;
		this.energy = energy;
	}

	/**
	 * Returns the total number of bytes transferred, including packet headers.
	 * @return The total bytes transferred.
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 *  Returns the length of the payload. 
	 * 
	 * @return The payload length, in bytes.
	 */
	public long getPayloadLen() {
		return payloadLen;
	}

	/**
	 * Returns the total amount of time that the radio was in a high energy active state.
	 * 
	 * @return The active time value, in seconds.
	 */
	public double getActiveTime() {
		return activeTime;
	}

	/**
	 * Returns the amount of energy used to deliver the payload. 
	 * 
	 * @return The energy value, in joules.
	 */
	public double getEnergy() {
		return energy;
	}

	/**
	 * Returns the average throughput for the time range
	 * @return The throughput value, in kilobits per second.
	 */
	public double getKbps() {
		return (totalBytes * 8 / 1000) / (endTime - startTime);
	}
}
