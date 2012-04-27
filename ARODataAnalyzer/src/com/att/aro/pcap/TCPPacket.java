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
 * A bean class that provides access to TCP packet data.
 */
public class TCPPacket extends IPPacket implements Serializable {
	private static final long serialVersionUID = 1L;

	private int sourcePort;
	private int destinationPort;
	private long sequenceNumber;
	private long ackNumber;
	private boolean URG;
	private boolean ACK;
	private boolean PSH;
	private boolean RST;
	private boolean SYN;
	private boolean FIN;
	private int window;
	private short urgentPointer;
	private int dataOffset;
	private int payloadLen;

	/**
	 * Creates a new instance of the TCPPacket class.
	 */
	protected TCPPacket(int datalink, long seconds, long microSeconds, int len,
			byte[] data) {
		super(datalink, seconds, microSeconds, len, data);

		int headerOffset = super.getDataOffset();

		ByteBuffer bytes = ByteBuffer.wrap(data);
		sourcePort = bytes.getShort(headerOffset) & 0xFFFF;
		destinationPort = bytes.getShort(headerOffset + 2) & 0xFFFF;
		sequenceNumber = bytes.getInt(headerOffset + 4) & 0xFFFFFFFFL;
		ackNumber = bytes.getInt(headerOffset + 8) & 0xFFFFFFFFL;
		int hlen = ((bytes.get(headerOffset + 12) & 0xF0) >> 2);
		dataOffset = headerOffset + hlen;
		payloadLen = super.getPayloadLen() - hlen;
		short i = bytes.getShort(headerOffset + 12);
		URG = (i & 0x0020) != 0;
		ACK = (i & 0x0010) != 0;
		PSH = (i & 0x0008) != 0;
		RST = (i & 0x0004) != 0;
		SYN = (i & 0x0002) != 0;
		FIN = (i & 0x0001) != 0;
		window = bytes.getShort(headerOffset + 14) & 0xFFFF;
		urgentPointer = bytes.getShort(headerOffset + 18);
	}

	/**
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
	 * Gets the sequence number.
	 * 
	 * @return A long value that is the sequence number.
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Gets the acknowledgment number.
	 * 
	 * @return A long value that is the acknowledgement number.
	 */
	public long getAckNumber() {
		return ackNumber;
	}

	/**
	 * Gets the Urgent (URG) flag that prioritizes certain data in a packet
	 * segment.
	 * 
	 * @return true if prioritize are set with in the packet else it is false.
	 */
	public boolean isURG() {
		return URG;
	}

	/**
	 * Gets the Acknowledge Flag.
	 * 
	 * @return A boolean value that is “true if data is prioritized within the
	 *         packet, and is “false” otherwise.
	 */
	public boolean isACK() {
		return ACK;
	}

	/**
	 * Gets the PSH flag. The PSH flag in the TCP header, informs the receiving
	 * host that the data should be pushed up to the receiving application
	 * immediately.
	 * 
	 * @return A boolean value that is “true” if the data should be pushed to
	 *         the receiving application immediately, and is “false” if a push
	 *         is not required.
	 */
	public boolean isPSH() {
		return PSH;
	}

	/**
	 * Gets the RST flag. The RST flag indicates whether a connection should be
	 * aborted in response to an error.
	 * 
	 * @return A boolean value that is “true” if the connection should be closed
	 *         in response to an error, and is false if it should not.
	 */
	public boolean isRST() {
		return RST;
	}

	/**
	 * Gets a flag that indicates whether a connection should be initiated.
	 * 
	 * @return A boolean value that is true if a connection should be initiated,
	 *         and is “false if a connection won’t be initiated.
	 */
	public boolean isSYN() {
		return SYN;
	}

	/**
	 * Gets the FIN Flag.
	 * 
	 * @return A boolean value that is “true” if the connection should be
	 *         closed, and is “false” if the connection should remain the same.
	 */
	public boolean isFIN() {
		return FIN;
	}

	/**
	 * Gets the window.
	 * 
	 * @return An int value that is the window.
	 */
	public int getWindow() {
		return window;
	}

	/**
	 * Gets the urgent pointer that indicates priority data.
	 * 
	 * @return A short value that is the urgent pointer.
	 */
	public short getUrgentPointer() {
		return urgentPointer;
	}

}
