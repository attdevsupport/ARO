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
 * Contains battery information for a device, and provides methods for retrieving the level, 
 * temperature, and state of the battery.
 */
public class BatteryInfo {

	// Battery Time stamp when the battery level or temperature occur.
	private double batteryTimeStamp;
	// Battery State true stands battery is on; else battery off.
	private boolean batteryState;
	// Battery Level
	private int batteryLevel;
	// Battery temperature
	private int batteryTemp;

	/**
	 * Initializes an instance of the BatteryInfo class, using the specified time stamp, 
	 * battery state, level and temperature.
	 * 
	 * @param bTimestamp The time at which the battery information was modified.
	 * 
	 * @param bState The on/off state of the battery.
	 * 
	 * @param bLevel The battery level.
	 * 
	 * @param bTemp The battery temperature.
	 */
	public BatteryInfo(double bTimestamp, boolean bState, int bLevel, int bTemp) {
		this.batteryTimeStamp = bTimestamp;
		this.batteryState = bState;
		this.batteryLevel = bLevel;
		this.batteryTemp = bTemp;
	}

	/**
	 * Return the time stamp of when the battery information was last modified. 
	 * 
	 * @return A double that is the time stamp of when the battery information was last modified.
	 */
	public double getBatteryTimeStamp() {
		return batteryTimeStamp;
	}

	/**
	 * Returns the on/off state of the battery. 
	 * 
	 * @return A boolean value that is true if the battery is on, and false if the battery 
	 * is off.
	 */
	public boolean isBatteryState() {
		return batteryState;
	}

	/**
	 * Returns the battery level. 
	 * 
	 * @return The battery level.
	 */
	public int getBatteryLevel() {
		return batteryLevel;
	}

	/**
	 * Returns the temperature of the battery. 
	 * 
	 * @return An int that is the battery temperature.
	 */
	public int getBatteryTemp() {
		return batteryTemp;
	}

}
