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

import java.net.InetAddress;

/**
 * A base class containing packet summary information for an IP address.
 */
public class IPPacketSummary extends PacketSummary {
	private static final long serialVersionUID = 1L;

	private InetAddress ipAddress;
	
	/**
	 * Initializes an instance of the IPPacketSummary class, using the specified 
	 * IP address, number of packets for the IP address, and total number of bytes for the IPAddress.
	 * @param ipAddress The IP address.
	 * @param packetCount The number of packets.
	 * @param totalBytes The total number of bytes for the IPAddress.
	 */
	public IPPacketSummary(InetAddress ipAddress, int packetCount, long totalBytes) {
		super(packetCount, totalBytes);
		this.ipAddress = ipAddress;
	}

	/**
	 * Returns the IP address.
	 * @return The IP address.
	 */
	public InetAddress getIPAddress() {
		return ipAddress;
	}
	
}
