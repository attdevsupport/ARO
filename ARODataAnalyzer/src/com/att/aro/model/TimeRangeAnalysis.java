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
	 * Initializes an instance of the TimeRangeAnalysis class, using the specified payload 
	 * length, DCH time, and energy value.
	 * 
	 * @param startTime start of time range in seconds
	 * @param endTime end of time range in seconds
	 * @param totalBytes total bytes transferred including all packet headers
	 * @param payloadLen – The length of the payload in bytes.
	 * @param activeTime time of high energy radio time
	 * @param energy – The amount of energy used to deliver the payload.
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
	 * Returns the total bytes transferred including packet headers
	 * @return the totalBytes
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 *  Returns the length of the payload, in bytes. 
	 * 
	 * @return The payload length.
	 */
	public long getPayloadLen() {
		return payloadLen;
	}

	/**
	 * Returns the time of high energy radio active time.
	 * 
	 * @return activeTime.
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
	 * @return throughput in kilobits per second
	 */
	public double getKbps() {
		return (totalBytes * 8 / 1000) / (endTime - startTime);
	}
}
