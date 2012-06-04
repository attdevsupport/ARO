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
 * The BurstCategory Enumeration specifies constant values that describe the different 
 * categories of bursts that occur when data is transferred. 
 */
public enum BurstCategory {
	/**
	 * A Protocol Burst. This is the default burst category. A Protocol burst consists of 
	 * unwanted packets that are being transferred.
	 */
	BURSTCAT_PROTOCOL("protocol"),
	/**
	 * A Loss burst. This burst consists of the packets that are lost while being 
	 * transferred. It also includes the Recovered and Duplicate Burst types. 
	 */
	BURSTCAT_LOSS("loss"),
	/**
	 * A User initiated Burst. 
	 */
	BURSTCAT_USER("user"),
	/**
	 * User initiated Burst.
	 */
	BURSTCAT_SCREEN_ROTATION("screen"),
	/**
	 * A Client initiated burst. 
	 */
	BURSTCAT_CLIENT("client"),
	/**
	 * A Server initiated Burst. 
	 */
	BURSTCAT_SERVER("server"),
	/**
	 * A background burst. 
	 */
	BURSTCAT_BKG("bkg"),
	/**
	 * A Long burst. A Long burst has a duration of more than 5 seconds, and typically 
	 * transfers large amounts of data. 
	 */
	BURSTCAT_LONG("long"),
	/**
	 * A Periodical Burst. If the Internet Addresses, host names, or object names are the same for the 
	 * packets in a set burst over a period of time, then those bursts are considered 
	 * Periodical bursts. 
	 */
	BURSTCAT_PERIODICAL("periodic"),
	/**
	 * User defined burst category 1.
	 */
	BURSTCAT_USERDEF1("user"),
	/**
	 * User defined burst category 2.
	 */
	BURSTCAT_USERDEF2("user"),
	/**
	 * User defined burst category 3.
	 */
	BURSTCAT_USERDEF3("user"),
	/**
	 * A burst of an unknown category. 
	 */
	BURSTCAT_UNKNOWN("unknown");

	private final String resourceKey; // resource bundle key

	/**
	 * Private constructor for enum.
	 * 
	 * @param resourceKey
	 */
	private BurstCategory(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	/**
	 * Returns the resource key.
	 * 
	 * @return Resource key.
	 */
	public String getResourceKey() {
		return this.resourceKey;
	}
}
