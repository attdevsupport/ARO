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
import java.net.InetAddress;

import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.packetreader.pojo.TCPPacket;

/**
 * A bean class that contains information about a packet in a TCP Session. 
 * <pre>
 * packetId     The packet id
 * timestamp    The timestamp of the Packet
 * dir          The packet direction
 * tcpInfo      TcpInfo enumeration
 * appName      The app name corresponding to the packet
 * stateMachine The RRC machine state (state of the machine)
 * packet       The packet
 * strTcpFlags  A String containing flags representing the type of TCPPacket.
 * </pre>
 */
public class PacketInfo implements Comparable<PacketInfo>, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The packet id. The first Packet of a trace would be 1.
	 */
	private int packetId; // 1-based
	
	/**
	 * The timestamp of the Packet
	 */
	private double timestamp;
	
	/**
	 * Indicates packet direction (UNKNOWN / UPLINK / DOWNLINK)
	 */
	private PacketDirection dir; 

	/**
	 * The TcpInfo 
	 */
	private TcpInfo tcpInfo;
	
	/**
	 * The app name corresponding to the packet
	 */
	private String appName;

	/**
	 *  The RRC machine state (state of the machine)
	 */
	private RRCState stateMachine;

	/**
	 * The packet
	 */
	private Packet packet;

	/**
	 * A String containing flags representing the type of TCPPacket.
	 * <pre>
	 *  'A' ACK
	 *  'P' PSH
	 *  'R' RST
	 *  'S' SYN
	 *  'F' FIN
	 * </pre>
	 * 
	 */
	private String strTcpFlags = "";


	/**
	 * Initializes an instance of the PacketInfo class, using the specified packet data.
	 * 
	 * @param packet A com.att.aro.pcap.Packet object containing the packet data.
	 */
	public PacketInfo(Packet packet) {
		this(null, packet);
	}

	/**
	 * Initializes an instance of the PacketInfo class, using the specified packet data.
	 * 
	 * @param appName The name of the application that produced the packet
	 * @param packet A com.att.aro.pcap.Packet object containing the packet data.
	 */
	public PacketInfo(String appName, Packet packet) {
		this.appName = appName;
		this.packet = packet;
		this.timestamp = packet.getTimeStamp();

		if (packet instanceof TCPPacket) {
			setTcpFlagString((TCPPacket) packet);
		}
	}

	public void clearAnalysis() {
		setStateMachine(null);
		setTcpInfo(null);
	}
	
	/**
	 * Sets the packet id. The first Packet of a trace would be 1.
	 * 
	 * @param pId The packet id.
	 */
	public void setPacketId(int pId) {
		this.packetId = pId;
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
	public int getPacketId() {
		return packetId;
	}

	/**
	 * Compares the specified PacketInfo object to this one.
	 */
	@Override
	public int compareTo(PacketInfo pInfo) {
		return Double.valueOf(this.timestamp).compareTo(pInfo.timestamp);
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
	public PacketDirection getDir() {
		return dir;
	}

	/**
	 * Returns the remote IP address if this packet represents an IP packet and
	 * a direction for the packet has been identified.
	 * @return The remote IP address, or null if it cannot be determined.
	 */
	public InetAddress getRemoteIPAddress() {
		if (packet instanceof IPPacket && dir != null) {
			IPPacket ipPkt = (IPPacket) packet;
			if(dir == PacketDirection.UPLINK){
				return ipPkt.getDestinationIPAddress();
			}else if(dir == PacketDirection.DOWNLINK){
				return ipPkt.getSourceIPAddress();
			}
			
		}
		return null;
	}
	
	/**
	 * Sets the packet direction. 
	 * 
	 * @param dir A PacketInfo.Direction enumeration value that indicates the packet direction.
	 */
	public void setDir(PacketDirection dir) {
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
	 * Sets the RRC state machine state for the packet. 
	 * 
	 * @param stateMachine The RRC state machine value.
	 */
	public void setStateMachine(RRCState stateMachine) {
		this.stateMachine = stateMachine;
	}

	/**
	 * Returns the RRC machine state for this packet. 
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
	 * Sets a TCP flag as per TCPPacket type.
	 * 
	 * @param tcpPacket
	 */
	private void setTcpFlagString(TCPPacket tcpPacket) {
		StringBuilder strBuf = new StringBuilder();
		if (tcpPacket.isACK()) {
			strBuf.append('A');
		}
		if (tcpPacket.isPSH()) {
			strBuf.append('P');
		}
		if (tcpPacket.isRST()) {
			strBuf.append('R');
		}
		if (tcpPacket.isSYN()) {
			strBuf.append('S');
		}
		if (tcpPacket.isFIN()) {
			strBuf.append('F');
		}
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
