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
 * Encapsulates information about the WiFi peripheral.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 1, 2014
 */
public class WifiInfo implements Comparable<WifiInfo> {

	// GPS Time stamp
	private double beginTimeStamp;
	private double endTimeStamp;

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
	 * The WifiInfo.WifiState Enumeration specifies constant values that
	 * describe the operational state of the WiFi peripheral on a device. This
	 * enumeration is part of the WifiInfo class.
	 */

	public enum WifiState {
		/**
		 * WiFi is in an unknown state.
		 */
		WIFI_UNKNOWN,
		/**
		 * WIFI is in the disabled state.
		 */
		WIFI_DISABLED,
		/**
		 * The device is connecting to a Wifi HotSpot.
		 */
		WIFI_CONNECTING,
		/**
		 * The device is connected to a Wifi HotSpot.
		 */
		WIFI_CONNECTED,
		/**
		 * The Device is disconnecting from a Wifi HotSpot.
		 */
		WIFI_DISCONNECTING,
		/**
		 * The Device is disconnected from a Wifi HotSpot.
		 */
		WIFI_DISCONNECTED,
		/**
		 * Wifi is suspended on the device.
		 */
		WIFI_SUSPENDED
	}


	/**
	 * Initializes an instance of the WifiInfo class using the specified
	 * timestamps, WiFi state, Mac address, rssi, and ssid.
	 * 
	 * @param beginTimeStamp
	 *            The beginning timestamp for the WiFi event.
	 * @param endTimeStamp
	 *            The ending timestamp for the WiFi event.
	 * 
	 * @param wifiState
	 *            One of the values of the WiFiState enumeration that
	 *            indicates the state of the WiFi connection.
	 * 
	 * @param macAddress
	 *            The WiFi Mac address.
	 * 
	 * @param rssi
	 *            The RSSI value.
	 * 
	 * @param ssid
	 *            The SSID value.
	 */
	public WifiInfo(double beginTimeStamp, double endTimeStamp,
			WifiState wifiState, String macAddress, String rssi, String ssid) {
		this.beginTimeStamp = beginTimeStamp;
		this.endTimeStamp = endTimeStamp;
		this.wifiState = wifiState;
		this.wifiMacAddress = macAddress;
		this.wifiRSSI = rssi;
		this.wifiSSID = ssid;
	}

	/**
	 * Compares the specified WifiInfo object to this one and returns an int
	 * that indicates the result.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WifiInfo wifiInformation) {
		return Double.valueOf(wifiTimeStamp).compareTo(wifiInformation.wifiTimeStamp);
	}

	/**
	 * Returns the timestamp at which the WiFi state begins. 
	 * 
	 * @return The beginning timestamp.
	 */
	public double getBeginTimeStamp() {
		return beginTimeStamp;
	}

	/**
	 * Returns the timestamp at which the WiFi state ends.
	 * 
	 * @return The ending timestamp.
	 */
	public double getEndTimeStamp() {
		return endTimeStamp;
	}

	/**
	 * Returns the WiFi state.
	 * 
	 * @return One of the values of the WiFiState enumeration that indicates the
	 *         state of the WiFi connection.
	 */
	public WifiState getWifiState() {
		return wifiState;
	}

	/**
	 * Returns the WiFi MacAddress.
	 * 
	 * @return The WiFi Mac address.
	 */
	public String getWifiMacAddress() {
		return wifiMacAddress;
	}

	/**
	 * Returns the WiFi RSSI value.
	 * 
	 * @return The RSSI value.
	 */
	public String getWifiRSSI() {
		return wifiRSSI;
	}

	/**
	 * Returns the WiFi SSID value.
	 * 
	 * @return The WiFi SSID.
	 */
	public String getWifiSSID() {
		return wifiSSID;
	}

}
