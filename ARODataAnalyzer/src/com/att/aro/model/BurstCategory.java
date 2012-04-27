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
 * ENUM to maintain the Burst Categories.
 */
public enum BurstCategory {
	/**
	 * Default Burst.
	 */
	BURSTCAT_PROTOCOL("protocol"),
	/**
	 * Recovered and Duplicate Bursts state.
	 */
	BURSTCAT_LOSS("loss"),
	/**
	 * User initiated Burst.
	 */
	BURSTCAT_USER("user"),
	/**
	 * Client initiated Burst.
	 */
	BURSTCAT_CLIENT("client"),
	/**
	 * Server initiated Burst.
	 */
	BURSTCAT_SERVER("server"),
	/**
	 * Background Burst.
	 */
	BURSTCAT_BKG("bkg"),
	/**
	 * Long Burst transfers heavy data.
	 */
	BURSTCAT_LONG("long"),
	/**
	 * Periodical Burst which keep on repeats in some delay.
	 */
	BURSTCAT_PERIODICAL("periodic"),
	/**
	 * User input Burst 1.
	 */
	BURSTCAT_USERDEF1("user"),
	/**
	 * User input Burst 2.
	 */
	BURSTCAT_USERDEF2("user"),
	/**
	 * User input Burst 3.
	 */
	BURSTCAT_USERDEF3("user"),
	/**
	 * Unknown Burst state.
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
