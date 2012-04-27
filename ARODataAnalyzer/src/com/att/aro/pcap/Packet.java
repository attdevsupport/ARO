/*
 Copyright [2012] [AT&T]
 
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
package com.att.aro.pcap;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * An adapter class for packet data.
 */
public class Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final short IP = 0x0800;

	private static final int DLT_EN10MB = 1;
	private static final int DLT_RAW = 12;
	private static final int DLT_LINUX_SLL = 113;

	/**
	 * Creates a new instance of the Packet class.
	 */
	public static Packet createPacket(int datalink, long seconds,
			long microSeconds, int len, byte[] data) {

		ByteBuffer bytes = ByteBuffer.wrap(data);

		// Determine network protocol
		short network = 0;
		try {
			switch (datalink) {
			case DLT_RAW: // Raw IP
				network = IP;
				break;
			case DLT_EN10MB: // Ethernet (WiFi)
				network = bytes.getShort(12);
				break;
			case DLT_LINUX_SLL: // Linux cooked capture (Android)
				network = bytes.getShort(14);
				break;
			}
		} catch (IndexOutOfBoundsException e) {
			// Truncated packet
		}

		int dataOffset = headerLength(datalink);

		// Minimum IP header length is 20 bytes
		if (network == IP && data.length >= dataOffset + 20) {

			byte iphlen = (byte) ((bytes.get(dataOffset) & 0x0f) << 2);
			if (data.length < dataOffset + iphlen) {

				// Truncated packet
				return new Packet(datalink, seconds, microSeconds, len, data);
			}

			// Determine IP protocol
			byte protocol = bytes.get(dataOffset + 9);
			switch (protocol) {
			case 6: // TCP
				if (data.length >= dataOffset + iphlen + 20) {
					return new TCPPacket(datalink, seconds, microSeconds, len,
							data);
				} else {
					return new Packet(datalink, seconds, microSeconds, len,
							data);
				}
			case 17: // UDP
				if (data.length >= dataOffset + iphlen + 6) {
					return new UDPPacket(datalink, seconds, microSeconds, len,
							data);
				} else {
					return new Packet(datalink, seconds, microSeconds, len,
							data);
				}
			default:
				return new IPPacket(datalink, seconds, microSeconds, len, data);
			}
		} else {
			return new Packet(datalink, seconds, microSeconds, len, data);
		}

	}

	/**
	 * Returns the header length for packets with the specified datalink type.
	 * 
	 * @param datalink
	 *            The datalink type
	 * @return The header length in bytes..
	 */
	private static int headerLength(int datalink) {
		switch (datalink) {
		case DLT_EN10MB: // Ethernet (WiFi)
			return 14;
		case DLT_LINUX_SLL: // Linux cooked capture (Android)
			return 16;
		default:
			return 0;
		}
	}

	private byte[] data;
	private long seconds;
	private long microSeconds;
	private int len;
	private int dataOffset;

	/**
	 * Constructor
	 */
	protected Packet(int datalink, long seconds, long microSeconds, int len,
			byte[] data) {
		this.dataOffset = headerLength(datalink);
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
	 * This method returns the offset into the data array where the payload of
	 * the packet starts. Subclasses should override this to give proper data
	 * offset excluding enclosed headers.
	 * 
	 * @return The offset within the data array of the packet data excluding the
	 *         header info
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
