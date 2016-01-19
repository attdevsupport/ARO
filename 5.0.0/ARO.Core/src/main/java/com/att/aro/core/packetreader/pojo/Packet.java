/*
 Copyright 2014 AT&T
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.att.aro.core.packetreader.pojo;

import java.io.Serializable;

/**
 * generic packet data
 */
public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private byte[] data;
	private long seconds;
	private long microSeconds;
	private int len;
	private int dataOffset;

	/**
	 *  Initializes  a new instance of the Packet class, using the specified parameters.
	 *  @param datalinkHdrLen The datalink for the packet.
	 *  @param seconds The number of seconds for the packet.
	 *  @param microSeconds The number of microseconds for the packet.
	 *  @param len The length of the packet (in bytes) including both the header and the data.
	 *  @param data An array of bytes that is the data portion of the packet.
	 */
	public Packet(long seconds, long microSeconds, int len, int datalinkHdrLen, byte[] data) {
		this.dataOffset = datalinkHdrLen;
		this.seconds = seconds;
		this.microSeconds = microSeconds;
		this.len = len;
		this.data = data;
	}

	/**
	 * Gets the data portion of the packet.
	 * 
	 * @return The packet data.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Gets the number of seconds.
	 * 
	 * @return A long that is tThe number of seconds.
	 */
	public long getSeconds() {
		return this.seconds;
	}

	/**
	 * Gets the number of microseconds.
	 * 
	 * @return A long that is the number of microseconds.
	 */
	public long getMicroSeconds() {
		return this.microSeconds;
	}

	/**
	 * Calculates and returns the timestamp value.
	 * 
	 * @return A double that is the timestamp value.
	 */
	public double getTimeStamp() {
		return ((double) seconds) + (((double) microSeconds) / 1000000.0);
	}

	/**
	 * Gets the length of the packet (in bytes) including both the header and
	 * the data.
	 * 
	 * @return An int that is the length of the packet (in bytes).
	 */
	public int getLen() {
		return len;
	}

	/**
	 * Returns the length of the data portion of the packet. Subclasses should
	 * override this method to identify their specific payload.
	 * 
	 * @return The payload length.
	 */
	public int getPayloadLen() {

		// Use method here rather than member for data offset in case
		// overridden by subclass
		return len - getDataOffset();
	}

	/**
	 * Returns the offset into the data array where the payload of
	 * the packet starts. Subclasses should override this to give proper data
	 * offset excluding enclosed headers.
	 * 
	 * @return The offset within the data array of the packet data, excluding the header information.
	 */
	public int getDataOffset() {
		return dataOffset;
	}

	/**
	 * Returns the size of the datalink header on the packet.
	 * 
	 * @return The size of the datalink header for the packet.
	 */
	public final int getDatalinkHeaderSize() {
		return dataOffset;
	}
}
