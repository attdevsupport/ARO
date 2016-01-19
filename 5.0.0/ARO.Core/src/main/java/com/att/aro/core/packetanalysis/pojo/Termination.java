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
 * Contains the TCP session termination information for a packet. 
 */
public class Termination implements Serializable {
	private static final long serialVersionUID = 1L;

	private PacketInfo packet;
	private double sessionTerminationDelay;

	/**
	 * Constructor
	 * 
	 * @param packet
	 * @param sessionTerminationDelay
	 */
	public Termination(PacketInfo packet, double sessionTerminationDelay) {
		this.packet = packet;
		this.sessionTerminationDelay = sessionTerminationDelay;
	}

	/**
	 * Returns the packet information. 
	 * 
	 * @return A PacketInfo object containing the packet.
	 */
	public PacketInfo getPacket() {
		return packet;
	}

	/**
	 * The amount of time, in seconds, between the last data packet and the packet 
	 * that signaled the session termination. 
	 * 
	 * @return The session termination delay.
	 */
	public double getSessionTerminationDelay() {
		return sessionTerminationDelay;
	}

}
