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
 * Encapsulates WIFI info
 */
public class WifiInfo implements Comparable<WifiInfo> {

	/**
	 * ENUM to maintain the wifi states.
	 */
	public enum WifiState {
		/**
		 * Unknown state.
		 */
		WIFI_UNKNOWN,
		/**
		 * WIFI disabled state.
		 */
		WIFI_DISABLED,
		/**
		 * Device is connecting to a Wifi HotSpot.
		 */
		WIFI_CONNECTING,
		/**
		 * Device is connected to a Wifi HotSpot.
		 */
		WIFI_CONNECTED,
		/**
		 * Device is disconnecting from a Wifi HotSpot.
		 */
		WIFI_DISCONNECTING,
		/**
		 * Device is disconnected from a Wifi HotSpot.
		 */
		WIFI_DISCONNECTED,
		/**
		 * Wifi suspended on the device.
		 */
		WIFI_SUSPENDED
	}

	// wifi time stamp
	private double wifiTimeStamp;
	// current wifi state
	private WifiState wifiState;
	// wifi mac address
	private String wifiMacAddress;
	// wifi RSSI id
	private String wifiRSSI;
	// wifi SSID id
	private String wifiSSID;

	/**
	 * Constructor
	 * 
	 * @param dTimestamp
	 * @param wifiState
	 */
	public WifiInfo(double dTimestamp, WifiState wifiState, String macAddress, String rssi,
			String ssid) {
		this.wifiTimeStamp = dTimestamp;
		this.wifiState = wifiState;
		this.wifiMacAddress = macAddress;
		this.wifiRSSI = rssi;
		this.wifiSSID = ssid;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WifiInfo o) {
		return Double.valueOf(wifiTimeStamp).compareTo(o.wifiTimeStamp);
	}

	/**
	 * Returns WiFi timestamp.
	 * 
	 * @return wifiTimeStamp.
	 */
	public double getWifiTimeStamp() {
		return wifiTimeStamp;
	}

	/**
	 * Returns WiFi state.
	 * 
	 * @return wifiState.
	 */
	public WifiState getWifiState() {
		return wifiState;
	}

	/**
	 * Returns WiFi MacAddress.
	 * 
	 * @return wifiMacAddress.
	 */
	public String getWifiMacAddress() {
		return wifiMacAddress;
	}

	/**
	 * Returns WiFi RSSI value.
	 * 
	 * @return wifiRSSI.
	 */
	public String getWifiRSSI() {
		return wifiRSSI;
	}

	/**
	 * Returns WiFi SSID value.
	 * 
	 * @return wifiSSID.
	 */
	public String getWifiSSID() {
		return wifiSSID;
	}

}
