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

/**
 * Encapsulates the GPS information.
 */
public class GpsInfo implements Comparable<GpsInfo> {

	/**
	 * The GpsInfo.GpsState Enumeration specifies constant values that describe
	 * the operational state of the GPS peripheral on a device. This enumeration
	 * is part of the GpsInfo class.
	 */
	public enum GpsState {
		/**
		 * The GPS is in an unknown state.
		 */
		GPS_UNKNOWN,
		/**
		 * The GPS is in the disabled state.
		 */
		GPS_DISABLED,
		/**
		 * The GPS is in an active state.
		 */
		GPS_ACTIVE,
		/**
		 * The GPS is in the standby state.
		 */
		GPS_STANDBY
	}

	// GPS Time stamp
	private double beginTimeStamp;
	private double endTimeStamp;

	// Current GPS State
	private GpsState gpsState;

	/**
	 * Initializes an instance of the GpsInfo class using the specified
	 * timestamps, and GPS state.
	 * 
	 * @param beginTimeStamp
	 *            The starting timestamp for the GPS event.
	 * @param endTimeStamp
	 *            The ending timestamp for the GPS event.
	 * 
	 * @param gpsState
	 *            A GpsInfo.GpsState enumeration value that specifies the GPS
	 *            state.
	 */
	public GpsInfo(double beginTimeStamp, double endTimeStamp, GpsState gpsState) {
		this.beginTimeStamp = beginTimeStamp;
		this.endTimeStamp = endTimeStamp;
		this.gpsState = gpsState;
	}

	/**
	 * Compares the specified GpsInfo object to this object.
	 */
	@Override
	public int compareTo(GpsInfo o) {
		return Double.valueOf(beginTimeStamp).compareTo(o.beginTimeStamp);
	}

	/**
	 * Returns the starting timestamp for the GPS state.
	 * 
	 * @return The starting timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}

	/**
	 * Returns the ending timestamp for the GPS state.
	 * 
	 * @return The ending timestamp.
	 */
	public double getEndTimeStamp() {
		return endTimeStamp;
	}

	/**
	 * Returns the GPS state.
	 * 
	 * @return A GpsInfo.GpsState enumeration value that indicates the GPS
	 *         state.
	 */
	public GpsState getGpsState() {
		return gpsState;
	}

}
