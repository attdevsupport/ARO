package com.att.aro.core.packetreader;

import java.io.IOException;

public interface IPacketReader {
	void readPacket(String packetfile, IPacketListener listener) throws IOException;
}
