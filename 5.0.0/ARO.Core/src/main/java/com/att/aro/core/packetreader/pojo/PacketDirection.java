package com.att.aro.core.packetreader.pojo;

public enum PacketDirection {
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
