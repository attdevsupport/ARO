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
package com.att.aro.core.configuration.pojo;

/**
 * Represents a device profile for modeling WiFi energy states when analyzing trace data.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 15, 2014
 */
public class ProfileWiFi extends Profile {
	private static final long serialVersionUID = 1L;

	/**
	 * The amount of time spent in the WiFi Tail state.
	 */
	public static final String WIFI_TAIL_TIME = "WIFI_TAIL_TIME";

	/**
	 * The average amount of power (in watts) that should be used when WiFi is in the Active state. 
	 */
	public static final String POWER_WIFI_ACTIVE = "POWER_WIFI_ACTIVE";

	/**
	 * The average amount of power (in watts) that should be used when WiFi is in the Standby state. 
	 */
	public static final String POWER_WIFI_STANDBY = "POWER_WIFI_STANDBY";

	private double wifiTailTime;

	/**
	 * Energy consumed during the WiFi is in active state.
	 */
	private double wifiActivePower;

	/**
	 * Energy used when WiFi is in standby mode. 
	 */
	private double wifiIdlePower;

	/**
	 * Returns the amount of time that WiFi was in the Tail state. 
	 * @return The amount of WiFi tail time.
	 */
	public double getWifiTailTime() {
		return wifiTailTime;
	}

	/**
	 * Returns the amount of energy consumed when WiFi is in the Active state. 
	 * @return The amount of WiFi Active energy.
	 */
	public double getWifiActivePower() {
		return wifiActivePower;
	}

	/**
	 * Returns the amount of energy consumed when WiFi is in the Idle state. 
	 * @return The amount of WiFi Idle energy.
	 */
	public double getWifiIdlePower() {
		return wifiIdlePower;
	}

	
	
	public void setWifiTailTime(double wifiTailTime) {
		this.wifiTailTime = wifiTailTime;
	}

	public void setWifiActivePower(double wifiActivePower) {
		this.wifiActivePower = wifiActivePower;
	}

	public void setWifiIdlePower(double wifiIdlePower) {
		this.wifiIdlePower = wifiIdlePower;
	}

	@Override
	public ProfileType getProfileType() {
		return ProfileType.WIFI;
	}

}
