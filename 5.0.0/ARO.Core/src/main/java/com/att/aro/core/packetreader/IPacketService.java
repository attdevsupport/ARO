package com.att.aro.core.packetreader;

import com.att.aro.core.packetreader.pojo.Packet;

public interface IPacketService {
	Packet createPacketFromPcap(int datalink, long seconds, long microSeconds, int len,
			byte[] data, String pcapfile);
	/**
	 * Returns a new instance of the Packet class, using a datalink to the Microsoft Network Monitor 
	 * and the specified parameters to initialize the class members.
	 * @param datalink The datalink to the Microsoft Network Monitor.
	 * @param seconds The number of seconds for the packet.
	 * @param microSeconds The number of microseconds for the packet.
	 * @param len The length of the packet (in bytes) including both the header and the data.
	 * @param data An array of bytes that is the data portion of the packet.
	 * 
	 * @return The newly created packet.
	 */
	Packet createPacketFromNetmon(int datalink, long seconds, long microSeconds, int len,
			byte[] data) ;
	/**
	 * Returns a new instance of the Packet class, using the specified parameters to initialize the class members.
	 * @param network The datalink to the network.
	 * @param seconds The number of seconds for the packet.
	 * @param microSeconds The number of microseconds for the packet.
	 * @param len The length of the data portion of the packet (in bytes).
	 * @param datalinkHdrLen The length of the header portion of the packet (in bytes).
	 * @param data An array of bytes that is the data portion of the packet.
	 * 
	 * @return The newly created packet.
	 */
	Packet createPacket(short network, long seconds, long microSeconds, int len, int datalinkHdrLen,
			byte[] data);
}
