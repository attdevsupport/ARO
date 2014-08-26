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
 * A bean class that provides access to UDP Packet data.
 */
public class UDPPacket extends IPPacket implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int DNS_PORT = 53;

	private int sourcePort;
	private int destinationPort;
	private int packetLength;
	private int dataOffset;
	private int payloadLen;
	private DomainNameSystem dns;

	/**
	 * Constructor
	 */
	public UDPPacket(long seconds, long microSeconds, int len, int datalinkHdrLen,
			byte[] data) {
		super(seconds, microSeconds, len, datalinkHdrLen, data);

		int headerOffset = super.getDataOffset();
		dataOffset = headerOffset + 8;

		ByteBuffer bytes = ByteBuffer.wrap(data);
		sourcePort = bytes.getShort(headerOffset) & 0xFFFF;
		destinationPort = bytes.getShort(headerOffset + 2) & 0xFFFF;
		packetLength = bytes.getShort(headerOffset + 4) & 0xFFFF;
		payloadLen = packetLength - 8;
		
		if (isDNSPacket()) {
			dns = new DomainNameSystem(this);
		}
	}

	/**
	 * @return The offset within the data array of the packet data excluding the header information.
	 * @see com.att.aro.pcap.IPPacket#getDataOffset()
	 */
	@Override
	public int getDataOffset() {
		return dataOffset;
	}

	/**
	 * @see com.att.aro.pcap.IPPacket#getPayloadLen()
	 */
	@Override
	public int getPayloadLen() {
		return payloadLen;
	}

	/**
	 * Gets the source port number.
	 * 
	 * @return An int value that is the source port number.
	 */
	public int getSourcePort() {
		return sourcePort;
	}

	/**
	 * Gets the destination port number.
	 * 
	 * @return An int value that is the destination port number.
	 */
	public int getDestinationPort() {
		return destinationPort;
	}

	/**
	 * Gets the length of the packet including the header.
	 * 
	 * @return An int value that is the length of the packet (in bytes).
	 */
	public int getPacketLength() {
		return packetLength;
	}

	/**
	 * Indicates whether the data portion of this packet contains a DNS packet
	 * @return
	 */
	public boolean isDNSPacket() {
		return destinationPort == DNS_PORT || sourcePort == DNS_PORT;
	}

	/**
	 * If this packet contains DNS info it may be accessed here
	 * @return the dns or null if this is not a DNS packet
	 */
	public DomainNameSystem getDns() {
		return dns;
	}

}
