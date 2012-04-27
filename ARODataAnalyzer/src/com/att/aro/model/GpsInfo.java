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
	 * ENUM to maintain the gps states.
	 */
	public enum GpsState {
		/**
		 * Unknown GPS state.
		 */
		GPS_UNKNOWN,
		/**
		 * GPS disabled state.
		 */
		GPS_DISABLED,
		/**
		 * GPS active state.
		 */
		GPS_ACTIVE,
		/**
		 * GPS standby state.
		 */
		GPS_STANDBY
	}

	// GPS Time stamp
	private double gpsTimeStamp;

	// Current GPS State
	private GpsState gpsState;

	/**
	 * Initializes an instance of the GpsInfo class using the specified timestamp, and GPS 
	 * state.
	 * 
	 * @param dTimestamp – The timestamp.
	 * 
	 * @param gpsState – A GpsInfo.GpsState enumeration value that specifies the GPS state.
	 */
	public GpsInfo(double dTimestamp, GpsState gpsState) {
		this.gpsTimeStamp = dTimestamp;
		this.gpsState = gpsState;
	}

	/**
	 * Compares the specified GpsInfo object to this object.
	 * 
	 * @see Comparable.compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(GpsInfo o) {
		return Double.valueOf(gpsTimeStamp).compareTo(o.gpsTimeStamp);
	}

	/**
	 * Returns the GPS timestamp. 
	 * 
	 * @return A double that is the GPS timestamp.
	 */
	public double getGpsTimeStamp() {
		return gpsTimeStamp;
	}

	/**
	 * Returns the GPS state. 
	 * 
	 * @return A GpsInfo.GpsState enumeration value that indicates the GPS state.
	 */
	public GpsState getGpsState() {
		return gpsState;
	}

}
