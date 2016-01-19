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

import java.io.Serializable;

/**
 * A Base class containing packet summary information.
 */
public class PacketSummary implements Serializable {
	private static final long serialVersionUID = 1L;

	private int packetCount;
	private long totalBytes;

	/**
	 * Initializes an instance of the PacketSummary class, 
	 * using the specified packet count and total number of bytes.
	 * @param packetCount - The number of packets.
	 * @param totalBytes - The total number of bytes in the packets.
	 */
	public PacketSummary(int packetCount, long totalBytes) {
		this.packetCount = packetCount;
		this.totalBytes = totalBytes;
	}
	
	/**
	 * Returns the packet count.
	 * @return The number of packets.
	 */
	public int getPacketCount() {
		return packetCount;
	}

	/**
	 * Returns the total bytes.
	 * @return The total number of bytes in the packets.
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

}
