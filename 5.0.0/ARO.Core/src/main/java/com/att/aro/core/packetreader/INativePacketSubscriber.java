package com.att.aro.core.packetreader;

/**
 * receive packet from the native lib
 * 
 * @author Borey Sao Date: April 4, 2014
 */
public interface INativePacketSubscriber {

	/**
	 * Receive raw packet data as read from traffic file.
	 * 
	 * @param datalink
	 * @param seconds
	 * @param microSeconds
	 * @param len
	 * @param data
	 */
	void receive(int datalink, long seconds, long microSeconds, int len, byte[] data);
}
