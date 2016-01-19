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
package com.att.aro.core.packetanalysis.pojo;

import java.io.Serializable;

/**
 * Encapsulates a specific time range of trace data for analysis.
 */
public class TimeRange implements Serializable {
	private static final long serialVersionUID = 1L;

	private double beginTime;
	private double endTime;

	/**
	 * Initializes an instance of the TimeRange class, using the specified beginning and ending times.
	 * @param beginTime - The beginning of the time range.
	 * @param endTime - The ending of the time range.
	 */
	public TimeRange(double beginTime, double endTime) {
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
	
	/**
	 * Returns the beginning time of the time range.
	 * @return The beginning time.
	 */
	public double getBeginTime() {
		return beginTime;
	}
	/**
	 * Returns the ending time of the time range.
	 * @return The ending time.
	 */
	public double getEndTime() {
		return endTime;
	}

}
