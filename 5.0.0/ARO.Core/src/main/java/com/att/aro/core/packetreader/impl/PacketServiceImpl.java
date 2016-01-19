package com.att.aro.core.packetreader.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetreader.IDomainNameParser;
import com.att.aro.core.packetreader.IPacketService;
import com.att.aro.core.packetreader.IPcapngHelper;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.packetreader.pojo.TCPPacket;
import com.att.aro.core.packetreader.pojo.UDPPacket;

public class PacketServiceImpl implements IPacketService {

	@InjectLogger
	private static ILogger logger;

	private static final short IPV4 = 0x0800;
	private static final short IPV6 = (short) 0x86DD;

	private static final int DLT_EN10MB = 1;
	private static final int DLT_RAW = 12;
	private static final int DLT_LINUX_SLL = 113;

	private static final int NETMON_ETHERNET = 1;
	private static final int NETMON_WIFI = 6;
	private static final int NETMON_WIRELESSWAN = 8;
	private static final int NETMON_RAW = 9;

	@Autowired
	private IPcapngHelper pcapngHelper;

	@Autowired
	private IDomainNameParser domainparser;

	/**
	 * Returns a new instance of the Packet class, using a datalink to a Pcap
	 * file and the specified parameters to initialize the class members.
	 * 
	 * @param datalink
	 *            The datalink to a Pcap file.
	 * @param seconds
	 *            The number of seconds for the packet.
	 * @param microSeconds
	 *            The number of microseconds for the packet.
	 * @param len
	 *            The length of the packet (in bytes) including both the header
	 *            and the data.
	 * @param data
	 *            An array of bytes that is the data portion of the packet.
	 * 
	 * @return The newly created packet.
	 */
	@Override
	public Packet createPacketFromPcap(int datalink, long seconds, long microSeconds, int len, byte[] data, String pcapfile) {
		// Determine network protocol
		short network = 0;
		int hdrLen = 0;
		ByteBuffer bytes = ByteBuffer.wrap(data);
		try {
			switch (datalink) {
			case DLT_RAW: // Raw IP
				network = IPV4;
				break;
			case DLT_EN10MB: // Ethernet (WiFi)
				network = bytes.getShort(12);
				hdrLen = 14;
				break;
			case DLT_LINUX_SLL: // Linux cooked capture (Android)
				network = bytes.getShort(14);
				hdrLen = 16;
				break;
			default:
				if (pcapfile != null) {
					try {
						if (pcapngHelper.isApplePcapng(pcapfile)) {
							network = IPV4;
							hdrLen = 4;
						}
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}
				break;
			}
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage());
		}

		return createPacket(network, seconds, microSeconds, len, hdrLen, data);
	}

	/**
	 * Returns a new instance of the Packet class, using a datalink to the
	 * Microsoft Network Monitor and the specified parameters to initialize the
	 * class members.
	 * 
	 * @param datalink
	 *            The datalink to the Microsoft Network Monitor.
	 * @param seconds
	 *            The number of seconds for the packet.
	 * @param microSeconds
	 *            The number of microseconds for the packet.
	 * @param len
	 *            The length of the packet (in bytes) including both the header
	 *            and the data.
	 * @param data
	 *            An array of bytes that is the data portion of the packet.
	 * 
	 * @return The newly created packet.
	 */
	@Override
	public Packet createPacketFromNetmon(int datalink, long seconds, long microSeconds, int len, byte[] data) {
		// Check for PCAP datalink
		if (datalink >= 0xe000 && datalink <= 0xefff) {
			return createPacketFromPcap(datalink - 0xe000, seconds, microSeconds, len, data, null);
		}

		// Determine network protocol
		short network = 0;
		int hdrLen = 0;
		ByteBuffer bytes = ByteBuffer.wrap(data);
		try {
			switch (datalink) {
			case NETMON_RAW: // Raw IP
				network = IPV4;
				break;
			case NETMON_WIRELESSWAN:
				network = IPV4;
				break;
			case NETMON_ETHERNET: // Ethernet (WiFi)
				network = bytes.getShort(12);
				hdrLen = 14;
				break;
			case NETMON_WIFI:

				// Get the NetMon 802.11 capture header length
				hdrLen = bytes.get(1);

				// Read the IEEE 802.11 frame control flags
				short control = bytes.get(hdrLen);

				// Check for data frame type
				if ((control & 0x000c) == 0x0008) {

					// Check data frame sub-type
					if ((control & 0x0080) == 0) {
						// Data
						hdrLen += 32;
					} else {
						// Data - QoS sub-type
						hdrLen += 34;
					}
					network = bytes.getShort(hdrLen - 2);
				}
				break;
			default:
				break;
			}
		} catch (IndexOutOfBoundsException e) {
			logger.error(e.getMessage());
		}

		return createPacket(network, seconds, microSeconds, len, hdrLen, data);
	}

	/**
	 * Returns a new instance of the Packet class, using the specified
	 * parameters to initialize the class members.
	 * 
	 * @param network
	 *            The datalink to the network.
	 * @param seconds
	 *            The number of seconds for the packet.
	 * @param microSeconds
	 *            The number of microseconds for the packet.
	 * @param len
	 *            The length of the data portion of the packet (in bytes).
	 * @param datalinkHdrLen
	 *            The length of the header portion of the packet (in bytes).
	 * @param data
	 *            An array of bytes that is the data portion of the packet.
	 * 
	 * @return The newly created packet.
	 */
	@Override
	public Packet createPacket(short network, long seconds, long microSeconds, int len, int datalinkHdrLen, byte[] data) {
		Packet packet = null;
		// Minimum IP header length is 20 bytes
		ByteBuffer bytes = ByteBuffer.wrap(data);
		if (network == IPV6 && data.length >= datalinkHdrLen + 40) {
			// Determine IPV6 protocol
			byte protocol = bytes.get(datalinkHdrLen + 6);
			switch (protocol) {
			case 6: // TCP
				packet = new TCPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
				break;
			case 17: // UDP
				packet = createUDPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
				break;
			default:
				packet = new IPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
				break;
			}
		} else if (network == IPV4 && data.length >= datalinkHdrLen + 20) {

			byte iphlen = (byte) ((bytes.get(datalinkHdrLen) & 0x0f) << 2);
			if (data.length < datalinkHdrLen + iphlen) {
				// Truncated packet
				packet = new Packet(seconds, microSeconds, len, datalinkHdrLen, data);
			} else {
				// Determine IP protocol
				byte protocol = bytes.get(datalinkHdrLen + 9);
				switch (protocol) {
				case 6: // TCP
					if (data.length >= datalinkHdrLen + iphlen + 20) {
						packet = new TCPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
					} else {
						packet = new Packet(seconds, microSeconds, len, datalinkHdrLen, data);
					}
					break;
				case 17: // UDP
					if (data.length >= datalinkHdrLen + iphlen + 6) {
						packet = createUDPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
					} else {
						packet = new Packet(seconds, microSeconds, len, datalinkHdrLen, data);
					}
					break;
				default:
					packet = new IPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
				}
			}
		} else {
			packet = new Packet(seconds, microSeconds, len, datalinkHdrLen, data);
		}
		return packet;
	}

	private Packet createUDPPacket(long seconds, long microSeconds, int len, int datalinkHdrLen, byte[] data) {
		UDPPacket packet = new UDPPacket(seconds, microSeconds, len, datalinkHdrLen, data);
		if (packet.isDNSPacket()) {
			DomainNameSystem dns = domainparser.parseDomainName(packet);
			packet.setDns(dns);
		}
		return packet;
	}

}
