package com.att.aro.core.packetreader;

public interface INetmonPacketSubscriber {
	void receiveNetmonPacket(int datalink, long seconds, long microSeconds,
			int len, byte[] data, String appName);
}
