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

import java.io.File;
import java.util.List;
import java.util.Properties;
/**
 * Represents a device profile for modeling WiFi energy states when analyzing trace data.
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
	 * Default constructor
	 */
	public ProfileWiFi() {
		super();
	}

	/**
	 * Initializes a new instance of the WiFi Profile class with the specified location for the 
	 * profile file and profile properties. 
	 * 
	 * @param file
	 *            The file where profile properties can be saved. This parameter can be null.
	 * @param properties
	 *            The profile properties that are to be set to the profile.
	 * @throws ProfileException
	 */
	public ProfileWiFi(File file, Properties properties) throws ProfileException {
		super(file, properties);
	}

	/**
	 * Initializes a new instance of the WiFi Profile class with the specified profile name, and properties. 
	 * 
	 * @param name
	 *            The name of the profile.
	 * @param properties
	 *            The properties that are to be set to the profile.
	 * @throws ProfileException
	 */
	public ProfileWiFi(String name, Properties properties)
			throws ProfileException {
		super(name, properties);
	}

	/**
	* A utility method for calculating RRC energy for WiFi. Implements the energy method in the 
	* Profile base class. 
	* 
	* @param time1 A beginning time value. 
	* @param time2 An ending time value. 
	* @param state An RRCState enumeration value that indicates the RRC energy state. 
	* @param packets A List of packets that were passed during the specified time period. 
	* The packet information may be used in determining the amount of energy used.
	*/
	@Override
	public double energy(double time1, double time2, RRCState state , List<PacketInfo> packets) {
		
		double deltaTime = time2 - time1;
		
		 switch(state){
		 case WIFI_ACTIVE:
		 case WIFI_TAIL:
			 return deltaTime * wifiActivePower;
		 case WIFI_IDLE:
			 return deltaTime * wifiIdlePower;
			 
		 }
		return 0;
	}

	/**
	* Returns the type of profile. Subclasses of this class must identify the profile type.
	* @return The profile type. One of the values of the ProfileType enumeration.
	*/
	@Override
	public ProfileType getProfileType() { 
		
		return ProfileType.WIFI;
	}

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

	/**
	 * Initializes Profile class members using values from the specified Properties object. 
	 * 
	 * @param properties - The profile values to be set.
	 */
	@Override
	protected void setProperties(Properties properties) {
		
		wifiTailTime = readDouble(properties, WIFI_TAIL_TIME, 0.25);
		wifiActivePower = readDouble(properties, POWER_WIFI_ACTIVE, 0.403);
		wifiIdlePower = readDouble(properties, POWER_WIFI_STANDBY, 0.02);
		
	}

	/**
	* Saves the current profile values in the specified Properties object. This method 
	* is used by sub-classes to save member values to a properties object for persistence.
	* 
	* @param properties - The properties object in which to store the values.
	*/
	@Override
	protected void saveProperties(Properties properties) {
		
		properties.setProperty(WIFI_TAIL_TIME, String.valueOf(wifiTailTime));
		properties.setProperty(POWER_WIFI_ACTIVE, String.valueOf(wifiActivePower));
		properties.setProperty(POWER_WIFI_STANDBY, String.valueOf(wifiIdlePower));
	}
}
