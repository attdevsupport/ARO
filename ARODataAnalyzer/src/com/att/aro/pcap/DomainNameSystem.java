/**
 * 
 */
package com.att.aro.pcap;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Used to interpret DNS information from a packet containing DNS data
 */
public class DomainNameSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(DomainNameSystem.class.getName());
	
	private IPPacket packet;
	private boolean response;
	private String domainName;
	private String cname;
	private Set<InetAddress> ipAddresses;
	
	/**
	 * Constructor
	 * @param packet The packet containing DNS info
	 */
	public DomainNameSystem(UDPPacket packet) {
		this.packet = packet;
		new Parser();
	}

	/**
	 * @return the packet
	 */
	public IPPacket getPacket() {
		return packet;
	}

	/**
	 * @return the response
	 */
	public boolean isResponse() {
		return response;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * @return the ipAddresses
	 */
	public Set<InetAddress> getIpAddresses() {
		if (ipAddresses != null) {
			return Collections.unmodifiableSet(ipAddresses);
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Inner class that parses the DNS data stream
	 */
	private class Parser {
		private byte[] data;
		private int start;
		private ByteBuffer bytes;
		
		public Parser() {
			start = packet.getDataOffset();
			data = packet.getData();
			
			// Craete a byte buffer with curr position at start of UDP data
			bytes = ByteBuffer.wrap(data);
			bytes.position(start);

			// Read the transaction ID
			bytes.getShort();
			
			// Read the flags
			short flags = bytes.getShort();
			response = (flags & 0x80) != 0;

			// Read the query count
			short queries = bytes.getShort();

			// Make sure that there is one question
			if (queries != 1) {
				logger.warning("DNS packet with more than one query");
				return;
			}			

			// Read the answer count
			short answers = bytes.getShort();
			
			// Read Authority RRs count
			bytes.getShort();
			
			// Read Additional RRs count
			bytes.getShort();

			// Read question
			domainName = readDomainName();
			short qtype = bytes.getShort();
			short qclass = bytes.getShort();
			if (qtype != 1 || qclass != 1) {
				logger.warning("Unrecognized DNS query:  qtype=" + qtype + ", qclass=" + qclass);
				return;
			}

			// Check to see if this is a DNS response
			if (response) {
				
				// Initialize IP addresses set
				ipAddresses = new HashSet<InetAddress>();
				
				// Default canonical name to domain name
				cname = domainName;
				
				// Iterate through answers
				for (int i = 0; i < answers; ++i) {
					
					// Read answer
					String dn = readDomainName();
					qtype = bytes.getShort();
					qclass = bytes.getShort();
					bytes.getInt(); // TTL
					short len = bytes.getShort();
					if (!dn.equals(domainName) && !dn.equals(cname)) {
						logger.warning("Unexpected answer domain: " + dn);
						bytes.position(bytes.position() + len);
						continue;
					}
					if (qclass != 1) {
						logger.warning("Unrecognized DNS answer class:" + qclass);
						bytes.position(bytes.position() + len);
						continue;
					}
					switch (qtype) {
					case 1 :
					case 28 :
						// IPv4 (A) or IPv6 (AAAA)
						byte[] b = new byte[len];
						bytes.get(b, 0, len);
						try {
							ipAddresses.add(InetAddress.getByAddress(b));
						} catch (UnknownHostException e) {
							logger.warning("Unexpected exception reading IP address from DNS response");
						}
						break;
					case 5 :
						// CNAME (canonical domain name)
						cname = readDomainName();
						break;
					default :
						logger.warning("Unhandled DNS answer type:" + qtype);
						bytes.position(bytes.position() + len);
					}
				}
			}

		}

		/**
		 * Utility that reads a domain name (compressed or uncompressed) from
		 * the current position in the ByteWrapper
		 * @return The domain name read from the data
		 */
		private String readDomainName() {
			
			// Create string buffer for result
			StringBuffer sb = new StringBuffer();
			
			// Read domain name and update current byte wrapper position
			bytes.position(readDomainSegment(sb, bytes.position()));
			return sb.toString();
		}

		/**
		 * Utility that reads a domain name (compressed or uncompressed) from
		 * the specified position in the data array
		 * @param sb buffer where results are appended
		 * @param i Index in the data array from which to read
		 * @return The index of the data array where the domain name read
		 * completed
		 */
		private int readDomainSegment(StringBuffer sb, int i) {
			while (data[i] != 0) {
				boolean compressed = (data[i] & 0xc0) == 0xc0;
				if (compressed) {
					readDomainSegment(sb, start + (bytes.getShort(i) & 0x3fff));
					++i;
					break;
				} else {
					if (sb.length() > 0) {
						sb.append('.');
					}
					sb.append(new String(data, i + 1, data[i]));
					i += (data[i] + 1);
				}
			}

			return ++i;
		}

	}
}
