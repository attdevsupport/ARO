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

import java.io.Serializable;

/**
 * Base class for a packet summary
 */
public class PacketSummary implements Serializable {
	private static final long serialVersionUID = 1L;

	private int packetCount;
	private long totalBytes;

	/**
	 * Initializing constructor
	 * @param packetCount
	 * @param totalBytes
	 */
	public PacketSummary(int packetCount, long totalBytes) {
		this.packetCount = packetCount;
		this.totalBytes = totalBytes;
	}
	
	/**
	 * @return the packetCount
	 */
	public int getPacketCount() {
		return packetCount;
	}

	/**
	 * @return the totalBytes
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

}
