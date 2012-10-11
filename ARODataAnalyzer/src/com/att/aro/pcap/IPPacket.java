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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * A bean class that provides access to IP Packet data.
 */
public class IPPacket extends Packet implements Serializable {
	private static final long serialVersionUID = 1L;

	private byte ipVersion;
	private byte priority;
	private int packetLength;
	private boolean rsvFrag;
	private boolean dontFrag;
	private boolean moreFrag;
	private short fragmentOffset;
	private short timeToLive;
	private short protocol;
	private InetAddress sourceIPAddress;
	private InetAddress destinationIPAddress;
	private int dataOffset;
	private int payloadLen;

	/**
	 * Creates a new instance of the IPPacket class.
	 */
	protected IPPacket(long seconds, long microSeconds, int len, int datalinkHdrLen,
			byte[] data) {
		super(seconds, microSeconds, len, datalinkHdrLen, data);

		// Parse data
		ByteBuffer bytes = ByteBuffer.wrap(data);
		int headerOffset = super.getDataOffset();

		ipVersion = (byte) ((bytes.get(super.getDataOffset()) & 0xf0) >> 4);
		int hlen = ((bytes.get(headerOffset) & 0x0f) << 2);
		dataOffset = headerOffset + hlen;
		packetLength = bytes.getShort(headerOffset + 2);
		payloadLen = packetLength - hlen;

		short i = bytes.getShort(headerOffset + 6);
		rsvFrag = (i & 0x8000) != 0;
		dontFrag = (i & 0x4000) != 0;
		moreFrag = (i & 0x2000) != 0;
		fragmentOffset = (short) (i & 0x1fff);

		i = bytes.getShort(headerOffset + 8);
		timeToLive = (short) ((i & 0xff00) >> 8);
		protocol = (short) (i & 0xff);

		byte[] b = new byte[4];
		try {
			bytes.position(headerOffset + 12);
			bytes.get(b, 0, 4);
			sourceIPAddress = InetAddress.getByAddress(b);
			bytes.get(b, 0, 4);
			destinationIPAddress = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see com.att.aro.pcap.Packet#getDataOffset()
	 * @return The offset within the data array of the packet data, excluding the header information.
	 */
	@Override
	public int getDataOffset() {
		return dataOffset;
	}

	/**
	 * @see com.att.aro.pcap.Packet#getPayloadLen()
	 */
	@Override
	public int getPayloadLen() {
		return payloadLen;
	}

	/**
	 * Gets the IP version for IPv4 and IPv6.
	 * 
	 * @return A byte value that is the IP version. A value of 4 indicates IPv4.
	 */
	public byte getIPVersion() {
		return ipVersion;
	}

	/**
	 * Gets the priority value for the IPPacket. In Pv4 this is TOS, and in Pv6
	 * it is Traffic Class.
	 * 
	 * @return A byte value that is the priority value.
	 */
	public byte getPriority() {
		return priority;
	}

	/**
	 * Gets the length of the packet including the header.
	 * 
	 * @return An int value that is the length of the packet in bytes.
	 */
	public int getPacketLength() {
		return packetLength;
	}

	/**
	 * Gets the value of the Fragmentation Reservation flag. (IPv4 only)
	 * 
	 * @return A boolean value that is the Fragmentation Reservation flag.
	 */
	public boolean isRsvFrag() {
		return rsvFrag;
	}

	/**
	 * Gets the value of the Don't Fragment flag. (IPv4 only)
	 * 
	 * @return A boolean value that is the Don't Fragment flag.
	 */
	public boolean isDontFrag() {
		return dontFrag;
	}

	/**
	 * Gets the value of the More Fragment flag. (IPv4 only)
	 * 
	 * @return A boolean value that is the More Fragment flag.
	 */
	public boolean isMoreFrag() {
		return moreFrag;
	}

	/**
	 * gets the fragment offset.(IPv4 only)
	 * 
	 * @return A short value that is the fragment offset.
	 */
	public short getFragmentOffset() {
		return fragmentOffset;
	}

	/**
	 * Gets theTime to Live (TTL) value. (IPv4 only)
	 * 
	 * @return A short that is the TTL value.
	 */
	public short getTimeToLive() {
		return timeToLive;
	}

	/**
	 * Gets the protocol for the IPPacket.
	 * 
	 * @return A short value that is the protocol.
	 */
	public short getProtocol() {
		return protocol;
	}

	/**
	 * Gets the source IP Address.
	 * 
	 * @return The source IP Address in the InetAddress format.
	 */
	public InetAddress getSourceIPAddress() {
		return sourceIPAddress;
	}

	/**
	 * Returns Gets the destination IP address.
	 * 
	 * @return The destination IP address in the InetAddress format.
	 */
	public InetAddress getDestinationIPAddress() {
		return destinationIPAddress;
	}

}
