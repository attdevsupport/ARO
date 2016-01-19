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
package com.att.aro.core.packetanalysis.pojo;

/**
 * The BurstCategory Enumeration specifies constant values that describe the different 
 * categories of bursts that occur when data is transferred. 
 */
public enum BurstCategory {
	CPU("CPU"),
	TCP_PROTOCOL("TcpControl"),
	TCP_LOSS_OR_DUP("TcpLossRecoverOrDup"),
	USER_INPUT("UserInput"),
	SCREEN_ROTATION("ScreenRotation"),
	CLIENT_APP("App"),
	SERVER_NET_DELAY("SvrNetDelay"),
	LONG("LargeBurst"),
	PERIODICAL("Periodical"),
	UNKNOWN("NonTarget");

	private String burstName;

	/**
	 * Private constructor for enum.
	 * 
	 * @param resourceKey
	 */
	private BurstCategory(String burstname) {
		this.burstName = burstname;
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
