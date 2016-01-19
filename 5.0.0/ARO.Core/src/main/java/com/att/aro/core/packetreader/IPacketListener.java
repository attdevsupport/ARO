package com.att.aro.core.packetreader;

import com.att.aro.core.packetreader.pojo.Packet;

/**
 * Defines a listener that is used to listen for incoming packets.
 */
public interface IPacketListener {
	/**
	 * A method that is invoked for each received packet.
	 * 
	 * @param appName The name of the application that generated the packet or
	 * null if not known.
	 * @param packet
	 *            - The packet that is received.
	 */
	void packetArrived(String appName, Packet packet);
}
