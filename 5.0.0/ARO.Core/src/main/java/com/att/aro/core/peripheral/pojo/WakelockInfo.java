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
package com.att.aro.core.peripheral.pojo;


/**
 * Encapsulates information about the Battery Info.
 */
public class WakelockInfo implements Comparable<WakelockInfo> {

	// wakelock time stamp
	private double wakelockTimeStamp;
	// current wakelock state
	private WakelockState wakelockState;
	
	/**
	 * The WakelockInfo.WakelockState Enumeration specifies constant values that
	 * describe the operational state of the Wakelock on a device. This
	 * enumeration is part of the WakelockInfo class.
	 */
	// Wakelock Time stamp in milliseconds
	private double beginTimeStamp;

	public enum WakelockState {
		/**
		 * WiFi is in an unknown state.
		 */
		WAKELOCK_UNKNOWN,
		/**
		 * WIFI is in the disabled state.
		 */
		WAKELOCK_ACQUIRED,
		/**
		 * The device is connecting to a Wifi HotSpot.
		 */
		WAKELOCK_RELEASED,
	}

	/**
	 * Initializes an instance of the WakelockInfo class using the specified
	 * timestamp, WiFi state, Mac address, rssi, and ssid.
	 * 
	 * @param beginTimeStamp
	 *            The start timestamp for the wakelock event.
	 * 
	 * @param wakelockState
	 *            One of the values of the WakelockState enumeration that
	 *            indicates the state of the WakelockState connection.
	 */
	public WakelockInfo(double beginTimeStamp, 
			WakelockState wakelockState) {
		this.beginTimeStamp = beginTimeStamp;
		this.wakelockState = wakelockState;
	}

	/**
	 * Compares the specified WakelockInfo object to this one and returns an int
	 * that indicates the result.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WakelockInfo wLockInfo) {
		return Double.valueOf(wakelockTimeStamp).compareTo(wLockInfo.wakelockTimeStamp);
	}

	/**
	 * Returns the start timestamp for the wakelock state.
	 * 
	 * @return The start timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}

	/**
	 * Returns the WakelockState state.
	 * 
	 * @return One of the values of the WakelockState enumeration that indicates the
	 *         state of the Wakelock connection.
	 */
	public WakelockState getWakelockState() {
		return wakelockState;
	}
}
