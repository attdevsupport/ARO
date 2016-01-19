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
package com.att.aro.ui.view.menu.profiles;

/**
 * Encapsulates Configuration information.
 */
public class ConfigurationData {

	private String profileDesc; // Display name for Configuration GUI
	private String profileAttr; // Name in Profile
	private String profileData; // data

	/**
	 * Initializes an instance of the ConfigurationData class using the specified profile 
	 * attribute, and the descriptions and data associated with the attribute.
	 * 
	 * @param desc The description of the device profile attribute.
	 * 
	 * @param attribute A Profile.Attribute enumeration value that specifies the device profile attribute.
	 * 
	 * @param data The data associated with the attribute.
	 */
	public ConfigurationData(String desc, String attribute, String data) {

		this.profileDesc = desc;
		this.profileAttr = attribute;
		this.profileData = data;
	}

	/**
	 * Returns the description of the device profile attribute.
	 * 
	 * @return A string containing the description of the device profile attribute.
	 */
	public String getProfileDesc() {
		return profileDesc;
	}

	/**
	 * Returns the device profile attribute. 
	 * 
	 * @return A Profile.Attribute enumeration value that specifies the device profile attribute.
	 */
	public String getProfileAttr() {
		return profileAttr;
	}

	/**
	 * Returns the data associated with the device profile attribute. 
	 * 
	 * @return A string that contains the device profile attribute data.
	 */
	public String getProfileData() {
		return profileData;
	}

	/**
	 * Sets the data for the device profile attribute to the specified data.
	 * 
	 * @param profileData - The device profile attribute data  to set.
	 */
	public void setProfileData(String profileData) {
		this.profileData = profileData;
	}

}
