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
 * A case class containing packet summary information for an application.
 */
public class ApplicationPacketSummary extends PacketSummary {
	private static final long serialVersionUID = 1L;

	private String appName;
	
	/**
	 * Initializes an instance of the ApplicationPacketSummary class using the specified 
	 * application name, number of packets, and total bytes contained in those packets.
	 * @param appName - The application name.
	 * @param packetCount - The number of packets for the application.
	 * @param totalBytes - The total number of bytes contained in the packets. 
	 */
	public ApplicationPacketSummary(String appName, int packetCount, long totalBytes) {
		super(packetCount, totalBytes);
		this.appName = appName;
	}

	/**
	 * Returns the application name.
	 * @return The application name
	 */
	public String getAppName() {
		return appName;
	}
	
}
