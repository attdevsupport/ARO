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
import java.net.InetAddress;

import com.att.aro.pcap.IPPacket;
import com.att.aro.pcap.Packet;
import com.att.aro.pcap.TCPPacket;

/**
 * A bean class that contains information about a packet in a TCP Session. 
 */
public class PacketInfo implements Comparable<PacketInfo>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ENUM to maintain the Packet Direction.
	 */
	public enum Direction {
		/**
		 * The packet direction is unknown. 
		 */
		UNKNOWN,
		/**
		 * The packet is traveling in the up link (Request) direction.
		 */
		UPLINK,
		/**
		 * The packet is traveling in the down link (Response) direction. 
		 */
		DOWNLINK
	}

	/**
	 * ENUM to maintain the Packets TCP state information.
	 */
	public enum TcpInfo {
		/**
		 * TCP data information.
		 */
		TCP_DATA,
		/**
		 * TCP acknowledge.
		 */
		TCP_ACK,
		/**
		 * TCP establish.
		 */
		TCP_ESTABLISH,
		/**
		 * TCP close packet.
		 */
		TCP_CLOSE,
		/**
		 * TCP reset packet.
		 */
		TCP_RESET,
		/**
		 * TCP duplicate data.
		 */
		TCP_DATA_DUP,
		/**
		 * TCP duplicate acknowledge.
		 */
		TCP_ACK_DUP,
		/**
		 * TCP keep alive.
		 */
		TCP_KEEP_ALIVE,
		/**
		 * TCP keep alive acknowledge.
		 */
		TCP_KEEP_ALIVE_ACK,
		/**
		 * TCP zero window.
		 */
		TCP_ZERO_WINDOW,
		/**
		 * TCP window update.
		 */
		TCP_WINDOW_UPDATE,
		/**
		 * TCP data recover. 
		 */
		TCP_DATA_RECOVER,
		/**
		 * TCP acknowledge recover.
		 */
		TCP_ACK_RECOVER
	}

	private int id; // 1-based
	private double timestamp;
	private Direction dir; // UPLINK / DOWNLINK direction

	private TCPSession session;
	private TcpInfo tcpInfo; // ********was DWORD
	private Burst burst;
	private String appName;

	// state machine inference
	private RRCState stateMachine; // ********was DWORD

	private Packet packet;

	private String strTcpFlags = "";

	private HttpRequestResponseInfo httpRequestResponseInfo = null;

	/**
	 * Initializes an instance of the PacketInfo class, using the specified packet data.
	 * 
	 * @param packet A com.att.aro.pcap.Packet object containing the packet data.
	 */
	public PacketInfo(Packet packet) {
		this.packet = packet;
		this.timestamp = packet.getTimeStamp();

		if (packet instanceof TCPPacket) {
			setTcpFlagString((TCPPacket) packet);
		}
	}

	/**
	 * Sets the packet id. 
	 * 
	 * @param id The packet id.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the current packet.
	 * 
	 * @return A com.att.aro.pcap.Packet object containing the packet data.
	 */
	public Packet getPacket() {
		return packet;
	}

	/**
	 * Returns the packet id. 
	 * 
	 * @return An int that is the id of the packet.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Setting the HTTP request/response information for the packet. 
	 * 
	 * @param httpRequestResponseInfo - The HTTP request/response information to set.
	 */
	public void setRequestResponseInfo(HttpRequestResponseInfo httpRequestResponseInfo) {
		this.httpRequestResponseInfo = httpRequestResponseInfo;
	}

	/**
	 * Returns the packet request/response information. 
	 * 
	 * @return An HTTPRequestResponse object containing the packet request/response information.
	 */
	public HttpRequestResponseInfo getRequestResponseInfo() {
		return httpRequestResponseInfo;
	}

	/**
	 * Compares the specified PacketInfo object to this one.
	 */
	@Override
	public int compareTo(PacketInfo o) {
		return Double.valueOf(this.timestamp).compareTo(o.timestamp);
	}

	/**
	 * Sets the packet timestamp. 
	 * 
	 * @param timestamp The timestamp to set.
	 * 
	 */
	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns the timestamp of the packet. 
	 * 
	 * @return The packet timestamp.
	 */
	public double getTimeStamp() {
		return timestamp;
	}

	/**
	 * Returns the direction of the packet (uplink, downlink, or unknown). 
	 * 
	 * @return The packet direction. One of the values of the PacketInfo.Direction enumeration.
	 */
	public Direction getDir() {
		return dir;
	}

	/**
	 * Returns the remote IP address if this packet represents an IP packet and
	 * a direction for the packet has been identified.
	 * @return The remote IP address, or null if it cannot be determined.
	 */
	public InetAddress getRemoteIPAddress() {
		if (packet instanceof IPPacket && dir != null) {
			IPPacket ip = (IPPacket) packet;
			switch (dir) {
			case UPLINK :
				return ip.getDestinationIPAddress();
			case DOWNLINK :
				return ip.getSourceIPAddress();
			}
		}
		return null;
	}
	
	/**
	 * Sets the packet direction. 
	 * 
	 * @param dir A PacketInfo.Direction enumeration value that indicates the packet direction.
	 */
	public void setDir(Direction dir) {
		this.dir = dir;
	}

	/**
	 * Returns the length (in bytes) of the packet, including both the header and the data. 
	 * 
	 * @return The length (in bytes) of the packet.
	 */
	public int getLen() {

		// Because the ethernet portion of the header does not go through the 3G
		// RAN, we exclude it from the len
		return packet.getLen() - packet.getDatalinkHeaderSize();
	}

	/**
	 * Returns the length of the payload data.
	 * 
	 * @return The payload length, in bytes.
	 */
	public int getPayloadLen() {
		return packet.getPayloadLen();
	}

	/**
	 * Sets the TCP information for the packet. 
	 * 
	 * @param tcpInfo The TCP information to set.
	 */
	public void setTcpInfo(TcpInfo tcpInfo) {
		this.tcpInfo = tcpInfo;
	}

	/**
	 * Returns the TCP information for the packet. 
	 * 
	 * @return A PacketInfo.TcpInfo enumeration value.
	 */
	public TcpInfo getTcpInfo() {
		return tcpInfo;
	}

	/**
	 * Sets the burst information for the packet burst. 
	 * 
	 * @param burst The burst information to set.
	 */
	public void setBurst(Burst burst) {
		this.burst = burst;
	}

	/**
	 * Returns the burst information from the packet. 
	 * 
	 * @return A Burst object containing the burst information.
	 */
	public Burst getBurst() {
		return burst;
	}

	/**
	 * Sets the RRC state machine for the packet. 
	 * 
	 * @param stateMachine The RRC state machine value.
	 */
	public void setStateMachine(RRCState stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * Returns the RRC state machine for this packet. 
	 * 
	 * @return An RRCState enumeration value.
	 */
	public RRCState getStateMachine() {
		return stateMachine;
	}

	/**
	 * Returns the application name. 
	 * 
	 * @return A string containing the application name.
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Sets the application name for the packet. 
	 * 
	 * @param appName - The application name to set.
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * Returns the TCP session that contains this packet. 
	 * 
	 * @return A TCPSession object that containing this packet.
	 */
	public TCPSession getSession() {
		return session;
	}

	/**
	 * Sets the TCP session that is associated with this packet. 
	 * 
	 * @param session - The TCP session to set.
	 */
	public void setSession(TCPSession session) {
		this.session = session;
	}

	/**
	 * Sets a TCP flag as per TCPPacket type.
	 * 
	 * @param tcpPacket
	 */
	private void setTcpFlagString(TCPPacket tcpPacket) {
		StringBuilder strBuf = new StringBuilder();
		if (tcpPacket.isACK())
			strBuf.append("A");
		if (tcpPacket.isPSH())
			strBuf.append("P");
		if (tcpPacket.isRST())
			strBuf.append("R");
		if (tcpPacket.isSYN())
			strBuf.append("S");
		if (tcpPacket.isFIN())
			strBuf.append("F");
		strTcpFlags = strBuf.toString();
	}

	/**
	 * Returns the TCP flag that indicates the TCPPacket type. 
	 * 
	 * @return A string containing the TCP flag for the packet.
	 */
	public String getTcpFlagString() {
		return strTcpFlags;
	}

}
