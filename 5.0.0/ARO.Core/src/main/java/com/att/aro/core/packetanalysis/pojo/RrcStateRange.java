/**
 *  Copyright 2016 AT&T
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
 * Provides a runTrace method that performs Radio Resource Control (RRC)
 * analysis by analyzing the time range between RRC states. This class also acts
 * as a bean class that encapsulates RRC range information.
 */

public class RrcStateRange implements Comparable<RrcStateRange>, Serializable{
	private static final long serialVersionUID = 1L;
	private double beginTime;
	private double endTime;
	private RRCState state;

	/**
	 * Initializes an instance of the RrcStateRange class with one RRC state
	 * range, using the specified begin time, end time, and RRC state.
	 * 
	 * @param beginTime
	 *            The time when the RRC state begins.
	 * 
	 * @param endTime
	 *            The time when the RRC state ends.
	 * 
	 * @param state
	 *            The RRC state. One of the values of the RRCState
	 *            enumeration.
	 */
	public RrcStateRange(double beginTime, double endTime, RRCState state) {
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.state = state;
	}

	/**
	 * Compares the specified RrcStateRange object to this one. This method is
	 * used to sort RRCStateRanges from the earliest time to the latest time.
	 * 
	 * @return An int value that is the result of the comparison.
	 */
	public int compareTo(RrcStateRange rsr) {
		return Double.valueOf(beginTime).compareTo(rsr.beginTime);
	}

	/**
	 * Returns The time when the RRC state begins.
	 * 
	 * @return The RRC begin time value.
	 */
	public double getBeginTime() {
		return beginTime;
	}

	/**
	 * Returns the time when the RRC state ends.
	 * 
	 * @return The RRC end time value.
	 */
	public double getEndTime() {
		return endTime;
	}

	/**
	 * Returns the RRC state.
	 * 
	 * @return The RRC state. One of the values of the RRCState enumeration.
	 */
	public RRCState getState() {
		return state;
	}

	public void setBeginTime(double beginTime) {
		this.beginTime = beginTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public void setState(RRCState state) {
		this.state = state;
	}
	
}
