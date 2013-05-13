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

import com.att.aro.util.Util;

/**
 * The BurstCategory Enumeration specifies constant values that describe the different 
 * categories of bursts that occur when data is transferred. 
 */
public enum BurstCategory {
	
	// NOTE: The order here must match the order in messages.properties (BurstCategory.N)
	CPU("burst.type.Cpu"),
	TCP_PROTOCOL("burst.type.TcpControl"),
	TCP_LOSS_OR_DUP("burst.type.TcpLossRecoverOrDup"),
	USER_INPUT("burst.type.UserInput"),
	SCREEN_ROTATION("burst.type.ScreenRotation"),
	CLIENT_APP("burst.type.App"),
	SERVER_NET_DELAY("burst.type.SvrNetDelay"),
	LONG("burst.type.LargeBurst"),
	PERIODICAL("burst.type.Periodical"),
	UNKNOWN("burst.type.Unknown");

	private final String burstName;

	/**
	 * Private constructor for enum.
	 * 
	 * @param resourceKey
	 */
	private BurstCategory(String resourceKey) {
		this.burstName = Util.RB.getString(resourceKey);
	}

	/**
	 * Returns a string describing the burst type that correspond to the burst category.
	 * 
	 * @return String value of the burst
	 */
	public String getBurstTypeDescription() {
		return this.burstName;
	}
}
