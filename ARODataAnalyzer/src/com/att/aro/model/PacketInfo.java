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

import com.att.aro.pcap.Packet;
import com.att.aro.pcap.TCPPacket;

/**
 * Bean class to contain packet informations.
 */
public class PacketInfo implements Comparable<PacketInfo>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ENUM to maintain the Packet Direction.
	 */
	public enum Direction {
		/**
		 * Unknown direcetion.
		 */
		UNKNOWN,
		/**
		 * Up link which is Request direction.
		 */
		UPLINK,
		/**
		 * Down link which is Response direction.
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
		 * TCP date recover.
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
	 * Constructor
	 * 
	 * @param packet The packet object.
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
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the current packet.
	 * 
	 * @return the packet
	 */
	public Packet getPacket() {
		return packet;
	}

	/**
	 * Returns the packet id.
	 * 
	 * @return packet id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Setting request/response information regarding packet
	 * 
	 * @param httpRequestResponseInfo
	 *            the httpRequestResponseInfo to set
	 */
	public void setRequestResponseInfo(HttpRequestResponseInfo httpRequestResponseInfo) {
		this.httpRequestResponseInfo = httpRequestResponseInfo;
	}

	/**
	 * Returns packet request/response information.
	 * 
	 * @return packet request/response information.
	 */
	public HttpRequestResponseInfo getRequestResponseInfo() {
		return httpRequestResponseInfo;
	}

	/**
	 * Override method to compare time between two time stamp.
	 */
	@Override
	public int compareTo(PacketInfo o) {
		return Double.valueOf(this.timestamp).compareTo(o.timestamp);
	}

	/**
	 * Sets packet timestamp.
	 * 
	 * @param timestamp
	 * 
	 */
	public void setTimestamp(double timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Returns packet timestamp.
	 * 
	 * @return packet timestamp.
	 */
	public double getTimeStamp() {
		return timestamp;
	}

	/**
	 * Returns packet direction enum state.
	 * 
	 * @return packet direction enum state.
	 */
	public Direction getDir() {
		return dir;
	}

	/**
	 * Sets direction of the packet i.e. uplink/downlink.
	 * 
	 * @param dir
	 *            The packet direction to be set.
	 */
	public void setDir(Direction dir) {
		this.dir = dir;
	}

	/**
	 * Returns the length (in bytes) of the packet including both the header and
	 * the data
	 * 
	 * @return length (in bytes) of the packet
	 */
	public int getLen() {

		// Because the ethernet portion of the header does not go through the 3G
		// RAN, we exclude it from the len
		return packet.getLen() - packet.getDatalinkHeaderSize();
	}

	/**
	 * Returns the payload length (in bytes) and the data
	 * 
	 * @return the payload length
	 */
	public int getPayloadLen() {
		return packet.getPayloadLen();
	}

	/**
	 * Sets packet TCP information.
	 * 
	 * @param tcpInfo
	 *            TCP info to set for the packet.
	 */
	public void setTcpInfo(TcpInfo tcpInfo) {
		this.tcpInfo = tcpInfo;
	}

	/**
	 * Returns packet TCP information.
	 * 
	 * @return TcpInfo of a packet.
	 */
	public TcpInfo getTcpInfo() {
		return tcpInfo;
	}

	/**
	 * Sets packet burst information.
	 * 
	 * @param burst
	 *            Burst info to set for a packet.
	 */
	public void setBurst(Burst burst) {
		this.burst = burst;
	}

	/**
	 * Returns packet burst information.
	 * 
	 * @return Burst info of a packet.
	 */
	public Burst getBurst() {
		return burst;
	}

	/**
	 * Sets packet state machine.
	 * 
	 * @param stateMachine
	 *            RRCState state to set.
	 */
	public void setStateMachine(RRCState stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * Returns packet state machine.
	 * 
	 * @return RRCState instance.
	 */
	public RRCState getStateMachine() {
		return stateMachine;
	}

	/**
	 * Returns application name.
	 * 
	 * @return appNamee.
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Sets application name.
	 * 
	 * @param appName
	 *            the appName to set
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * Returns packet TCP session.
	 * 
	 * @return the session
	 */
	public TCPSession getSession() {
		return session;
	}

	/**
	 * Sets packet TCP session.
	 * 
	 * @param session
	 *            the session to set
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
	 * Returns TCP flag as per TCPPacket type.
	 * 
	 * @return tcp flag of a packet.
	 */
	public String getTcpFlagString() {
		return strTcpFlags;
	}

}
