/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.packetreader.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetreader.IDomainNameParser;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.UDPPacket;

/**
 * class that parses the DNS data stream
 */

public class DomainNameParserImpl implements IDomainNameParser {

	private static final short TYPE_A = 1;
	private static final short TYPE_CNAME = 5;
	private static final short TYPE_AAAA = 28;
	
	private byte[] data;
	private int start;
	private ByteBuffer bytes;
	@InjectLogger
	private static ILogger logger;
	@Override
	public DomainNameSystem parseDomainName(UDPPacket packet) {
		DomainNameSystem domain = new DomainNameSystem();
		domain.setPacket(packet);
		
		start = packet.getDataOffset();
		data = packet.getData();
		
		// Craete a byte buffer with curr position at start of UDP data
		bytes = ByteBuffer.wrap(data);
		bytes.position(start);

		// Read the transaction ID
		bytes.getShort();
		
		// Read the flags
		short flags = bytes.getShort();
		boolean response = (flags & 0x80) != 0;

		domain.setResponse(response);
		
		// Read the query count
		short queries = bytes.getShort();

		// Make sure that there is one question
		if (queries != 1) {
			logger.warn("DNS packet with more than one query");
			return null;
		}			

		// Read the answer count
		short answers = bytes.getShort();
		
		// Read Authority RRs count
		bytes.getShort();
		
		// Read Additional RRs count
		bytes.getShort();

		// Read question
		String domainName = readDomainName();
		short qtype = bytes.getShort();
		short qclass = bytes.getShort();
		if ((qtype != TYPE_A && qtype != TYPE_AAAA) || qclass != 1) {
			//logger.warning("Unrecognized DNS query:  qtype=" + qtype + ", qclass=" + qclass);
			return null;
		}
		domain.setDomainName(domainName);
		
		// Check to see if this is a DNS response
		if (response) {
			
			// Initialize IP addresses set
			Set<InetAddress> ipAddresses = new HashSet<InetAddress>();
			
			// Default canonical name to domain name
			String cname = domainName;
			
			// Iterate through answers
			for (int i = 0; i < answers; ++i) {
				
				// Read answer
				String domainname = readDomainName();
				qtype = bytes.getShort();
				qclass = bytes.getShort();
				bytes.getInt(); // TTL
				short len = bytes.getShort();
				if (!domainname.equals(domainName) && !domainname.equals(cname)) {
					logger.warn("Unexpected answer domain: " + domainname);
					bytes.position(bytes.position() + len);
					continue;
				}
				if (qclass != 1) {
					logger.warn("Unrecognized DNS answer class:" + qclass);
					bytes.position(bytes.position() + len);
					continue;
				}
				switch (qtype) {
				case TYPE_A :
				case TYPE_AAAA :
					// IPv4 (A) or IPv6 (AAAA)
					byte[] bdata = new byte[len];
					bytes.get(bdata, 0, len);
					try {
						ipAddresses.add(InetAddress.getByAddress(bdata));
					} catch (UnknownHostException e) {
						logger.warn("Unexpected exception reading IP address from DNS response");
					}
					break;
				case TYPE_CNAME :
					// CNAME (canonical domain name)
					cname = readDomainName();
					break;
				default :
					logger.warn("Unhandled DNS answer type:" + qtype);
					bytes.position(bytes.position() + len);
				}
			}
			domain.setCname(cname);
			domain.setIpAddresses(ipAddresses);
		}
		return domain;
	}
	/**
	 * Utility that reads a domain name (compressed or uncompressed) from
	 * the current position in the ByteWrapper
	 * @return The domain name read from the data
	 */
	private String readDomainName() {
		
		// Create string buffer for result
		StringBuffer sbuffer = new StringBuffer();
		
		// Read domain name and update current byte wrapper position
		bytes.position(readDomainSegment(sbuffer, bytes.position()));
		return sbuffer.toString();
	}

	/**
	 * Utility that reads a domain name (compressed or uncompressed) from
	 * the specified position in the data array
	 * @param sb buffer where results are appended
	 * @param startindex Index in the data array from which to read
	 * @return The index of the data array where the domain name read
	 * completed
	 */
	private int readDomainSegment(StringBuffer sbuffer, int startindex) {
		int index = startindex;
		while (data[index] != 0) {
			boolean compressed = (data[index] & 0xc0) == 0xc0;
			if (compressed) {
				readDomainSegment(sbuffer, start + (bytes.getShort(index) & 0x3fff));
				++index;
				break;
			} else {
				if (sbuffer.length() > 0) {
					sbuffer.append('.');
				}
				sbuffer.append(new String(data, index + 1, data[index]));
				index += (data[index] + 1);
			}
		}

		return ++index;
	}

}
