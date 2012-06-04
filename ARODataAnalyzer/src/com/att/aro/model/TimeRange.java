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
 * Used to select a specific time range for analysis
 */
public class TimeRange implements Serializable {
	private static final long serialVersionUID = 1L;

	private double beginTime;
	private double endTime;

	/**
	 * Constructs time range object
	 * @param beginTime beginning of time range
	 * @param endTime end of time range
	 */
	public TimeRange(double beginTime, double endTime) {
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
	
	/**
	 * @return the beginTime
	 */
	public double getBeginTime() {
		return beginTime;
	}
	/**
	 * @return the endTime
	 */
	public double getEndTime() {
		return endTime;
	}

}
