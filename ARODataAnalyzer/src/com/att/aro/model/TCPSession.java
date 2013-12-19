/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.PacketInfo.TcpInfo;
import com.att.aro.pcap.DomainNameSystem;
import com.att.aro.pcap.TCPPacket;
import com.att.aro.pcap.UDPPacket;

/**
 * Represents a TCP session, acting as a bean class for session information. Provides methods for 
 * extracting a list of sessions from a collection of packets, and analyzing a session to retrieve 
 * the TCP information. 
 */
public class TCPSession implements Serializable, Comparable<TCPSession> {
	private static final long serialVersionUID = 1L;

	/**
	 * Contains the TCP session termination information for a packet. 
	 */
	public static class Termination implements Serializable {
		private static final long serialVersionUID = 1L;

		private PacketInfo packet;
		private double sessionTerminationDelay;

		/**
		 * Constructor
		 * 
		 * @param packet
		 * @param sessionTerminationDelay
		 */
		private Termination(PacketInfo packet, double sessionTerminationDelay) {
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

	private static final Logger logger = Logger.getLogger(TCPSession.class
			.getName());
	private InetAddress remoteIP;
	private String remoteHostName;

	/**
	 * Domain name is the initial host name requested that initiated a TCP
	 * session. This value is either the host name specified by the first HTTP
	 * request in the session or the referrer domain that caused this session to
	 * be opened
	 */
	private PacketInfo dnsRequestPacket;
	private PacketInfo dnsResponsePacket;
	private PacketInfo lastSslHandshakePacket;
	private String domainName;
	private int fileDownloadCount;
	private long bytesTransferred;
	private int remotePort;
	private int localPort;
	private boolean ssl;
	private boolean udpOnly = false;
	private List<PacketInfo> packets = new ArrayList<PacketInfo>();
	private List<PacketInfo> udpPackets = new ArrayList<PacketInfo>();
	private Set<String> appNames = new HashSet<String>(1);
	private Termination sessionTermination;
	private List<HttpRequestResponseInfo> requestResponseInfo = new ArrayList<HttpRequestResponseInfo>();

	private byte[] storageUl;
	private SortedMap<Integer, PacketInfo> packetOffsetsUl;
	private byte[] storageDl;
	private SortedMap<Integer, PacketInfo> packetOffsetsDl;

	/**
	 * Tracks information about a reassembled session
	 */
	private static class Reassembler {
		Long baseSeq;
		long seq = -1;
		List<PacketInfo> ooid = new ArrayList<PacketInfo>();
		ByteArrayOutputStream storage = new ByteArrayOutputStream();
		SortedMap<Integer, PacketInfo> packetOffsets = new TreeMap<Integer, PacketInfo>();

		void clear() {
			baseSeq = null;
			seq = -1;
			ooid.clear();
			storage.reset();
			packetOffsets = new TreeMap<Integer, PacketInfo>();
		}

		/**
		 * @see java.lang.Object#finalize()
		 */
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			storage.close();
		}

	}

	/**
	 * Initializes an instance of the TCPSession class, using the specified remote IP, remote port, 
	 * and local port.
	 * 
	 * @param remoteIP The remove IP address.
	 * 
	 * @param remotePort The remote port.
	 * 
	 * @param localPort The local port.
	 */
	public TCPSession(InetAddress remoteIP, int remotePort, int localPort) {
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}

	/**
	 * Creates a List of TCP sessions using the specified Collection of packets.
	 * 
	 * @param packets A collection of PacketInfo objects holding the packet information.
	 * 
	 * @return The collection of tcp sessions that are found from the packets
	 *         data.
	 * @throws IOException
	 */
	public static List<TCPSession> extractTCPSessions(
			Collection<PacketInfo> packets) throws IOException {
		logger.entering("com.att.aro.model.TCPSession", "extractTCPSessions(Collection<PacketInfo>)");
		Map<String, TCPSession> allSessions = new LinkedHashMap<String, TCPSession>();
		List<PacketInfo> dnsPackets = new ArrayList<PacketInfo>();
		List<PacketInfo> udpPackets = new ArrayList<PacketInfo>();
		Map<InetAddress, String> hostMap = new HashMap<InetAddress, String>();
		
		logger.finest("Starting loop through packets");
		for (PacketInfo packet : packets) {

			/**
			 * Save DNS packets
			 */
			if (!(packet.getPacket() instanceof TCPPacket)) {
				
				// Check for DNS packets
				if (packet.getPacket() instanceof UDPPacket) {
					UDPPacket udp = (UDPPacket) packet.getPacket();
					udpPackets.add(packet);
					if (udp.isDNSPacket()) {
						dnsPackets.add(packet);
						DomainNameSystem dns = udp.getDns();
						if (dns.isResponse()) {
							for (InetAddress inet : dns.getIpAddresses()) {
								hostMap.put(inet, dns.getDomainName());
							}
						}
					}
				}
				continue;
			}

			/**
			 * Set localPort, remoteIP, remotePort
			 * using TCP packet data information
			 */
			TCPPacket tcp = (TCPPacket) packet.getPacket();
			int localPort;
			int remotePort;
			InetAddress remoteIP;
			switch (packet.getDir()) {
			case UPLINK:
				localPort = tcp.getSourcePort();
				remoteIP = tcp.getDestinationIPAddress();
				remotePort = tcp.getDestinationPort();
				break;

			case DOWNLINK:
				localPort = tcp.getDestinationPort();
				remoteIP = tcp.getSourceIPAddress();
				remotePort = tcp.getSourcePort();
				break;

			default:
				logger.warning("29 - Unable to determine packet direction");
				continue;
			}

			// Clear TCP Info
			packet.setTcpInfo(null);

			/**
			 * Creates a new TCP Session using remoteIP, remotePort, localPort.
			 * Stores the session in allSessions Collection
			 * and adds the current packet to the session.
			 */
			String key = localPort + " " + remotePort + " " + remoteIP.getHostAddress();
			TCPSession session = allSessions.get(key);
			if (session == null) {
				session = new TCPSession(remoteIP, remotePort, localPort);
				
				/**
				 * Search for DNS request/response from the last to the first element
				 * and saves dnsResponsePacket in the current session if the DNS response remote IP
				 * matches the session remote IP. 
				 * Finds DNS response packet.
				 */
				ListIterator<PacketInfo> iter = dnsPackets.listIterator(dnsPackets.size());
				DomainNameSystem dns = null;
				while (iter.hasPrevious()) {
					PacketInfo dnsPacket = iter.previous();
					UDPPacket udp = ((UDPPacket) dnsPacket.getPacket());
					dns = udp.getDns();
					if (dns.isResponse() && dns.getIpAddresses().contains(remoteIP)) {
						session.dnsResponsePacket = dnsPacket;
						break;
					}
					dns = null;
				}
				
				// If DNS response packet was found
				if (dns != null) {
					/**
					 *  Loop through all DNS packets to find DNS packet
					 *  matching the domain name of the response DNS packet.
					 *  Then store remoteHostName and the DNS request packet
					 *  in the current session.
					 */
					
					iter = dnsPackets.listIterator();
					String domainName = dns.getDomainName();
					while (iter.hasNext()) {
						PacketInfo p = iter.next();
						UDPPacket udp = ((UDPPacket) p.getPacket());
						dns = udp.getDns();
						if (domainName.equals(dns.getDomainName())) {
							if (session.dnsRequestPacket == null && !dns.isResponse()) {
								session.remoteHostName = domainName;
								session.dnsRequestPacket = p;
							}
							
							// Remove from DNS packets so that it is not used again
							iter.remove();
							
							// Stop processing once response is reached
							if (p == session.dnsResponsePacket) {
								break;
							}
						}
					}
				} else {
					session.remoteHostName = hostMap.get(remoteIP);
				}
				// stores the created session
				allSessions.put(key, session);
			} // END: Create new session
			session.packets.add(packet);
		} // END: Iterating through all packets
		logger.finest("End of loop through packets");

		logger.finest("Starting creating sessions");
		// Reassemble sessions
		List<TCPSession> sessions = new ArrayList<TCPSession>(allSessions.values());
		Reassembler ul = new Reassembler();
		Reassembler dl = new Reassembler();
		for (int sessionIndex = 0; sessionIndex < sessions.size(); ++sessionIndex) {
			
			logger.log(Level.FINEST, "Working with [{0}] session", sessionIndex);

			// Iterator is not used because items may be added to list during
			// iterations
			TCPSession session = sessions.get(sessionIndex);
			
			logger.log(Level.FINEST, "Session has {0} packets", session.getPackets().size());

			// Reset variables
			boolean bTerminated = false;
			ul.clear();
			dl.clear();

			PacketInfo lastPacket = null;
			for (PacketInfo packetInfo: session.packets) {

				TCPPacket packet = (TCPPacket) packetInfo.getPacket();
				logger.log(Level.FINEST, "Processing packet [{0}] with seq.# [{0}]",
						new Object[] {Integer.toString(packetInfo.getId()), Long.toString(packet.getSequenceNumber())});
				if (packet.isSsl()) {
					session.ssl = true;
				}

				Reassembler reassembledSession;
				switch (packetInfo.getDir()) {
				case UPLINK:
					reassembledSession = ul;
					break;

				case DOWNLINK:
					reassembledSession = dl;
					break;

				default:
					logger.warning("91 - No direction for packet");
					continue;
				}

				// If this is the initial sequence number
				if (packet.isSYN()) {
					logger.finest("It is TCP_ESTABLISH packet");
					packetInfo.setTcpInfo(TcpInfo.TCP_ESTABLISH);
					if (reassembledSession.baseSeq == null
							|| reassembledSession.baseSeq.equals(packet.getSequenceNumber())) {
						logger.finest("It is existing TCP session");
						// Finds establish
						reassembledSession.baseSeq = packet.getSequenceNumber();
						if (packet.getPayloadLen() != 0) {
							logger.warning("92 - Payload in establish packet");
						}
					} else {
						logger.finest("It is new TCP session");

						// New TCP session
						List<PacketInfo> currentList = session.packets;
						int index = currentList.indexOf(packetInfo);
						if (!bTerminated) {
							logger.fine("28 - Session termination not found");
						}

						// Correct packet list in original session
						session.packets = new ArrayList<PacketInfo>(
								currentList.subList(0, index));

						// Create new session for remaining packets
						TCPSession newSession = new TCPSession(
								session.remoteIP, session.remotePort,
								session.localPort);
						newSession.packets.addAll(currentList.subList(index, currentList.size()));
						sessions.add(newSession);

						// Break out of packet loop
						break;
					}

				} else {
					logger.finest("It is NOT TCP_ESTABLISH packet");
					//FIN: No more data from sender
					//RST: Reset the connection
					if (packet.isFIN() || packet.isRST()) {

						logger.finest("Packet has FIN or RST flag");

						// Calculate session termination info
						if (!bTerminated && lastPacket != null) {
							double delay = packetInfo.getTimeStamp()
									- lastPacket.getTimeStamp();
							session.sessionTermination = new Termination(packetInfo,
									delay);
						}

						// Mark session terminated
						bTerminated = true;
						if (packet.isFIN()) {
							packetInfo.setTcpInfo(TcpInfo.TCP_CLOSE);
						} else if (packet.isRST()) {
							packetInfo.setTcpInfo(TcpInfo.TCP_RESET);
						}

					}

					// I believe this handles case where we have joined in the
					// middle of a TCP session
					if (reassembledSession.baseSeq == null) {
						logger.finest("We have joined in the middle of a TCP session");
						switch (packetInfo.getDir()) {
						case UPLINK:
							ul.baseSeq = packet.getSequenceNumber();
							dl.baseSeq = packet.getAckNumber();
							break;
						case DOWNLINK:
							dl.baseSeq = packet.getSequenceNumber();
							ul.baseSeq = packet.getAckNumber();
							break;
						}
					}
				}

				// Get appName (there really should be only one per TCP session
				String appName = packetInfo.getAppName();
				if (appName != null) {
					logger.log(Level.FINEST, "Adding {0} app name to the session", appName);
					logger.log(Level.FINEST, "Packet dest. port : {0}", packet.getDestinationPort());
					logger.log(Level.FINEST, "       dest. IP   : {0}", packet.getDestinationIPAddress());
					logger.log(Level.FINEST, "       source port: {0}", packet.getSourcePort());
					session.appNames.add(appName);
					assert (session.appNames.size() <=1)  : "" + session.appNames.size() + " app names per TCP session: " + session.getAppNames();
				}

				// Link packet to session
				packetInfo.setSession(session);

				long seq = packet.getSequenceNumber() - reassembledSession.baseSeq;
				if (seq < 0) {
					seq += 0xFFFFFFFF;
				}

				if (reassembledSession.seq == -1) {
					reassembledSession.seq = seq;
				}

				if (seq == reassembledSession.seq) {
					if (packet.getPayloadLen() > 0) {
						packetInfo.setTcpInfo(TcpInfo.TCP_DATA);
						byte[] data = packet.getData();
						int l = packet.getPayloadLen();
						int dataOffset = packet.getDataOffset();
						if (data.length >= dataOffset + l) {
							reassembledSession.packetOffsets.put(reassembledSession.storage.size(), packetInfo);
							reassembledSession.storage.write(data, dataOffset, l);
							reassembledSession.seq += l;
						}
						if (packet.isSslHandshake()) {
							session.lastSslHandshakePacket = packetInfo;
						}
					}
					if (packet.isSYN() || packet.isFIN()) {
						++reassembledSession.seq;
					}

					while (true) {
						boolean bOODone = true;
						List<PacketInfo> fixed = new ArrayList<PacketInfo>(
								reassembledSession.ooid.size());
						for (PacketInfo pi1 : reassembledSession.ooid) {
							TCPPacket p1 = (TCPPacket) pi1.getPacket();

							seq = p1.getSequenceNumber() - reassembledSession.baseSeq;
							if (seq < 0) {
								seq += 0xFFFFFFFF;
							}

							if (seq == reassembledSession.seq) {
								if (p1.getPayloadLen() > 0) {
									pi1.setTcpInfo(TcpInfo.TCP_DATA);
									byte[] data = p1.getData();
									int l = p1.getPayloadLen();
									int dataOffset = p1.getDataOffset();
									if (data.length >= dataOffset + l) {
										reassembledSession.packetOffsets.put(reassembledSession.storage.size(),
												pi1);
										reassembledSession.storage.write(data, dataOffset, l);
										reassembledSession.seq += l;
									}
									if (p1.isSslHandshake()) {
										session.lastSslHandshakePacket = pi1;
									}
								}
								if (p1.isSYN() || p1.isFIN()) {
									++reassembledSession.seq;
								}
								fixed.add(pi1);
								bOODone = false;
							} else if (p1.getPayloadLen() == 0
									&& seq == reassembledSession.seq - 1 && p1.isACK()
									&& !p1.isSYN() && !p1.isFIN()
									&& !p1.isRST()) {
								logger.warning("31 - ???");
							}
						}
						reassembledSession.ooid.removeAll(fixed);
						if (bOODone) {
							break;
						}
					}

				} else { // out of order packet, i.e., seq != *XLseq
					if (packet.getPayloadLen() == 0 && seq == reassembledSession.seq - 1
							&& packet.isACK() && !packet.isSYN() && !packet.isFIN()
							&& !packet.isRST()) {
						if (packetInfo.getTcpInfo() != null) {
							logger.warning("94 - ???");
						}
						packetInfo.setTcpInfo(TcpInfo.TCP_KEEP_ALIVE);
					} else {
						reassembledSession.ooid.add(packetInfo);
					}
				}

				lastPacket = packetInfo;
			} // packet loop
			session.storageDl = dl.storage.toByteArray();
			session.packetOffsetsDl = dl.packetOffsets;
			session.storageUl = ul.storage.toByteArray();
			session.packetOffsetsUl = ul.packetOffsets;

			for (PacketInfo p : dl.ooid) {
				if (p.getPacket().getPayloadLen() > 0) {
					p.setTcpInfo(TcpInfo.TCP_DATA_DUP);
				}
			}

			for (PacketInfo p : ul.ooid) {
				if (p.getPacket().getPayloadLen() > 0) {
					p.setTcpInfo(TcpInfo.TCP_DATA_DUP);
				}
			}
		} // END: Reassemble sessions
		logger.finest("All sessions where created");

		logger.finest("Starting looping through all sessions");
		// More session parsing
		for (TCPSession s : sessions) {
			for (PacketInfo p : s.packets) {
				s.bytesTransferred += p.getPacket().getLen();
			}
			s.analyzeACK();
			s.analyzeZeroWindow();
			s.analyzeRecoverPkts();

			// TODO Validate TCP info is set on all
			// CheckTCPInfo(s);

			// // TODO Check this?
			// s.pStorageUL->CheckPacketsRange();
			// s.pStorageDL->CheckPacketsRange();
			//

			// Parse HTTP request response info
			s.requestResponseInfo = HttpRequestResponseInfo
					.extractHttpRequestResponseInfo(s);
			for (HttpRequestResponseInfo rr : s.requestResponseInfo) {
				if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {

					// Assume first host found is same for entire session
					if (s.domainName == null) {
						String host = rr.getHostName();
						if (host != null) {
							URI referrer = rr.getReferrer();
							s.remoteHostName = host;
							s.domainName = referrer != null ? referrer
									.getHost() : host;
						}
					}
				} else if (rr.getDirection() == HttpRequestResponseInfo.Direction.RESPONSE) {
					if (rr.getContentLength() > 0) {
						++s.fileDownloadCount;
					}
				}
			}
			if (s.domainName == null) {
				s.domainName = s.remoteHostName != null ? s.remoteHostName : s.remoteIP.getHostAddress();
			}
		}
		ul.clear();
		dl.clear();
		logger.finest("Ended looping through all sessions");
		
		logger.finest("Sorting sessions");
		Collections.sort(sessions);
		logger.finest("End of sorting sessions");

		logger.exiting("com.att.aro.model.TCPSession", "extractTCPSessions(Collection<PacketInfo>)");
		
		/*Get UDP sessions.*/
		if(!udpPackets.isEmpty()){
			List<TCPSession> udpSessions = getUDPSessions(udpPackets,sessions);
			sessions.addAll(udpSessions);
		}

		return sessions;
	}
/**
 * Get the UDP sessions from different UDP packets.
 * @return Collection of TCPSession objects containing only UDP packets
 * */
	
	private static List<TCPSession> getUDPSessions(
			List<PacketInfo> udpPackets,List<TCPSession> sessions)throws IOException{
		Map<String, TCPSession> allUDPSessions = new LinkedHashMap<String, TCPSession>();	
		ListIterator<PacketInfo> iter = null;// = udpPackets.listIterator();//(udpPackets.size());
		DomainNameSystem dns = null;
		Reassembler ul = new Reassembler();
		Reassembler dl = new Reassembler();

		/*Remove all the dns packets part of TCP connections*/
		for (TCPSession sess : sessions) {
			iter = udpPackets.listIterator();
			while (iter.hasNext()) {
				PacketInfo p = iter.next();
				UDPPacket udp = ((UDPPacket) p.getPacket());
				if(udp.isDNSPacket()) {
					dns = udp.getDns();
					if(!dns.isResponse()) {
						if(sess.dnsRequestPacket != null) {
							String domainName = ((UDPPacket)sess.dnsRequestPacket.getPacket()).getDns().getDomainName();
							if(domainName.equals(dns.getDomainName())) {
									iter.remove();
							}									
						}
					} else {
						if(sess.dnsResponsePacket != null) {
							String domainName = ((UDPPacket)sess.dnsResponsePacket.getPacket()).getDns().getDomainName();
							if(domainName.equals(dns.getDomainName())
								&& (dns.getIpAddresses().contains(sess.remoteIP))) {
								iter.remove();
							}
						}
					}
				}
			}
		}
	/*Create a UDP session for those UDP packets which are not associated with TCP connection*/	
		for (PacketInfo packet : udpPackets) {
			UDPPacket udp = (UDPPacket) packet.getPacket();
			int localPort;
			int remotePort;
			InetAddress remoteIP;
			switch (packet.getDir()) {
				case UPLINK:
					localPort = udp.getSourcePort();
					remoteIP = udp.getDestinationIPAddress();
					remotePort = udp.getDestinationPort();
					break;
				case DOWNLINK:
					localPort = udp.getDestinationPort();
					remoteIP = udp.getSourceIPAddress();
					remotePort = udp.getSourcePort();
					break;
				default:
					logger.warning("29 - Unable to determine packet direction");
					continue;
			}
			String key = localPort + " " + remotePort + " " + remoteIP.getHostAddress();
			TCPSession session = allUDPSessions.get(key);
			if (session == null) {
				session = new TCPSession(remoteIP, remotePort, localPort);
				if (udp.isDNSPacket()){
					dns = udp.getDns();
					if(dns != null){
						//session.domainName = dns.getDomainName();
						session.remoteHostName = dns.getDomainName();
						
					}
				}
				if (session.remoteHostName == null){
					session.remoteHostName = session.remoteIP.getHostAddress();
				}
				session.udpOnly = true;
				/* stores the created session*/
				allUDPSessions.put(key, session);
			} // END: Create new session
			session.udpPackets.add(packet);	
		}
		List<TCPSession> udpSessions = new ArrayList<TCPSession>(allUDPSessions.values());
		for (int sessionIndex = 0; sessionIndex < udpSessions.size(); ++sessionIndex) {
			
			TCPSession session = udpSessions.get(sessionIndex);
			ul.clear();
			dl.clear();
			for (PacketInfo packetInfo: session.udpPackets) {
				UDPPacket packet = (UDPPacket) packetInfo.getPacket();
	
				Reassembler reassembledSession;
				switch (packetInfo.getDir()) {
				case UPLINK:
					reassembledSession = ul;
					break;

				case DOWNLINK:
					reassembledSession = dl;
					break;

				default:
					logger.warning("91 - No direction for packet");
					continue;
				}
				if (packet.getPayloadLen() > 0) {
					
					byte[] data = packet.getData();
					int l = packet.getPayloadLen();
					int dataOffset = packet.getDataOffset();
					if (data.length >= dataOffset + l) {
						reassembledSession.packetOffsets.put(reassembledSession.storage.size(), packetInfo);
						reassembledSession.storage.write(data, dataOffset, l);
					}
				}
					
			}
			session.storageDl = dl.storage.toByteArray();
			session.packetOffsetsDl = dl.packetOffsets;
			session.storageUl = ul.storage.toByteArray();
			session.packetOffsetsUl = ul.packetOffsets;

		}
		for (TCPSession sess : udpSessions) {
			sess.requestResponseInfo = HttpRequestResponseInfo.extractHttpRequestResponseInfo(sess);
			
			for (HttpRequestResponseInfo rr : sess.requestResponseInfo) {
				if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {

					// Assume first host found is same for entire session
					if (sess.domainName == null) {
						String host = rr.getHostName();
						if (host != null) {
							URI referrer = rr.getReferrer();
							sess.remoteHostName = host;
							sess.domainName = referrer != null ? referrer
									.getHost() : host;
						}
					}
				} 
			}
			if (sess.domainName == null) {
				sess.domainName = sess.remoteHostName != null ? sess.remoteHostName : sess.remoteIP.getHostAddress();
			}
			
		}
		ul.clear();
		dl.clear();
		return new ArrayList<TCPSession>(allUDPSessions.values());
	}
	
	
	
	/**
	 * Returns the set of appli	 * Compares the specified HttpRequestResponseInfo object to this one.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TCPSession o) {
		return Double.valueOf(getSessionStartTime()).compareTo(
				Double.valueOf(o.getSessionStartTime()));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TCPSession) {
			TCPSession oTcp = (TCPSession)obj;
			return Double.valueOf(getSessionStartTime()) == oTcp.getSessionStartTime();
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)Double.doubleToLongBits(getSessionStartTime());
	}
	
	/**
	 * Returns the set of application names contained in the TCP session. 
	 * 
	 * @return A Set of strings containing the application names.
	 */
	public Set<String> getAppNames() {
		return appNames;
	}

	/**
	 * Returns the remote IP address. 
	 * 
	 * @return The remote IP.
	 */
	public InetAddress getRemoteIP() {
		return remoteIP;
	}

	/**
	 * Returns the remote port. 
	 * 
	 * @return The remote port.
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * Returns the local port. 
	 * 
	 * @return The local port.
	 */
	public int getLocalPort() {
		return localPort;
	}

	/**
	 * Indicates whether SSL packets were detected in this session
	 * @return the ssl
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * Returns the name of the remote host. 
	 * 
	 * @return The remote host name.
	 */
	public String getRemoteHostName() {
		return remoteHostName;
	}

	/**
	 * @return the dnsRequestPacket
	 */
	public PacketInfo getDnsRequestPacket() {
		return dnsRequestPacket;
	}

	/**
	 * @return the dnsResponsePacket
	 */
	public PacketInfo getDnsResponsePacket() {
		return dnsResponsePacket;
	}

	/**
	 * @return the lastSslHandshakePacket
	 */
	public PacketInfo getLastSslHandshakePacket() {
		return lastSslHandshakePacket;
	}

	/**
	 * Returns the name of the TCP domain. 
	 * 
	 * @return The TCP domain name.
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Returns a count of the number of files downloaded during the TCP session. 
	 * 
	 * @return The file download count.
	 */
	public int getFileDownloadCount() {
		return fileDownloadCount;
	}

	/**
	 * Returns the number of bytes transferred during the session. 
	 * 
	 * @return The total number of bytes transferred.
	 */
	public long getBytesTransferred() {
		return bytesTransferred;
	}

	/**
	 * Returns all of the packets in the TCP session. 
	 * 
	 * @return A List of PacketInfo objects containing the packet data.
	 */
	public List<PacketInfo> getPackets() {
		return Collections.unmodifiableList(packets);
	}

	/**
	 * Returns all the UDP packets
	 * 
	 * @return A List of PacketInfo objects containing the packet data.
	 */
	public List<PacketInfo> getUDPPackets() {
		return Collections.unmodifiableList(udpPackets);
	}	
	
	public boolean isUDP()
	{
		return udpOnly;
	}
	/**
	 * Gets the start time of the session, in seconds, relative to the start of the trace. 
	 * 
	 * @return The start time of the session.
	 */
	public double getSessionStartTime() {
		return packets.get(0).getTimeStamp();
	}

	/**
	 * Gets the end time of the session, in seconds, relative to the start of the trace. 
	 * 
	 * @return The end time of the session.
	 */
	public double getSessionEndTime() {
		return packets.get(packets.size() - 1).getTimeStamp();
	}
	
	/**
	 * Gets the start time of the UDP session, in seconds, relative to the start of the trace. 
	 * 
	 * @return The start time of the UDP session.
	 */
	public double getUDPSessionStartTime() {
		return udpPackets.get(0).getTimeStamp();
	}

	/**
	 * Gets the end time of the UDP session, in seconds, relative to the start of the trace. 
	 * 
	 * @return The end time of the UDP session.
	 */
	public double getUDPSessionEndTime() {
		return udpPackets.get(udpPackets.size() - 1).getTimeStamp();
	}

	
	
	
	/**
	 * Return the request/response information for all of the packets. 
	 * 
	 * @return A List of HTTPRequestResponseInfo objects containing the information.
	 */
	public List<HttpRequestResponseInfo> getRequestResponseInfo() {
		return requestResponseInfo;
	}

	/**
	 * Returns the consolidated string for uplink and downlink storage. 
	 * 
	 * @return The result string.
	 */
	public String getDataText() {
		StringBuffer buf = new StringBuffer(storageUl.length + storageDl.length);
		buf.append(new String(storageUl));
		buf.append(new String(storageDl));
		String result = buf.toString();
		System.gc();
		return result;
	}

	/**
	 * Return the uplink storage. 
	 * 
	 * @return An array of bytes containing the uplink storage.
	 */
	public byte[] getStorageUl() {
		return storageUl;
	}

	/**
	 * Return the downlink storage. 
	 * 
	 * @return An array of bytes containing the downlink storage.
	 */
	public byte[] getStorageDl() {
		return storageDl;
	}

	/**
	 * Returns a sorted Map of offsets and packet data for each uplink packet in the 
	 * storage array. 
	 * 
	 * @return A Map of offsets and corresponding PacketInfo objects that contain the 
	 * uplink packet data.
	 */
	public SortedMap<Integer, PacketInfo> getPacketOffsetsUl() {
		return Collections.unmodifiableSortedMap(packetOffsetsUl);
	}

	/**
	 * Returns a sorted Map of offsets and packet data for each downlink packet in the 
	 * storage array.
	 * 
	 * @return A Map of offsets and corresponding PacketInfo objects that contain the 
	 * downlink packet data.
	 */
	public SortedMap<Integer, PacketInfo> getPacketOffsetsDl() {
		return Collections.unmodifiableSortedMap(packetOffsetsDl);
	}

	/**
	 * Returns information about the session termination if one exists in the trace. 
	 * 
	 * @return A TCPSession.Termination object containing the information, or null, if 
	 * there was no session termination in the trace.
	 */
	public Termination getSessionTermination() {
		return sessionTermination;
	}

	/**
	 * Analyze the packet to find the TCPInfo. Marked flags: TCP_ACK,
	 * TCP_ACK_DUP, TCP_WINDOW_UPDATE, TCP_KEEP_ALIVE_ACK
	 */
	private void analyzeACK() {

		Map<Long, Integer> ulAckWinSize = new HashMap<Long, Integer>();
		Map<Long, Integer> dlAckWinSize = new HashMap<Long, Integer>();

		Set<Long> ulAliveAck = new HashSet<Long>();
		Set<Long> dlAliveAck = new HashSet<Long>();

		for (PacketInfo pi : packets) {
			TCPPacket p = (TCPPacket) pi.getPacket();

			if (!p.isACK()) {
				continue;
			}

			long ackNum = p.getAckNumber();
			int win = p.getWindow();

			Map<Long, Integer> pAckWinSize;
			Set<Long> pAliveAck;
			Set<Long> pAliveAck2;

			switch (pi.getDir()) {
			case UPLINK:
				pAckWinSize = ulAckWinSize;
				pAliveAck = ulAliveAck;
				pAliveAck2 = dlAliveAck;
				break;

			case DOWNLINK:
				pAckWinSize = dlAckWinSize;
				pAliveAck = dlAliveAck;
				pAliveAck2 = ulAliveAck;
				break;

			default:
				logger.warning("97 - No direction for packet");
				continue;
			}

			if (pi.getTcpInfo() == TcpInfo.TCP_KEEP_ALIVE) {
				pAliveAck.add(p.getSequenceNumber());
				continue;
			}

			int tcpFlag;
			if (p.isFIN()) {
				tcpFlag = 1;
			} else if (p.isSYN()) {
				tcpFlag = 2;
			} else if (p.isRST()) {
				tcpFlag = 4;
			} else {
				tcpFlag = 0;
			}
			long key = ((ackNum << 32) | tcpFlag);

			// TODO Verify change in asserts ie getTCP!=null is ok
			int payloadLen = p.getPayloadLen();
			if (pAliveAck2.contains(ackNum - 1) && payloadLen == 0
					&& !p.isSYN() && !p.isFIN() && !p.isRST()) {
				if (pi.getTcpInfo() != null) {
					logger.warning("34 - Packet already typed");
				}
				pi.setTcpInfo(TcpInfo.TCP_KEEP_ALIVE);
			} else if (!pAckWinSize.containsKey(key)) {
				pAckWinSize.put(key, win);
				if (payloadLen == 0 && !p.isSYN() && !p.isFIN() && !p.isRST()) {
					if (pi.getTcpInfo() != null) {
						logger.warning("98 - Packet already typed");
					}
					pi.setTcpInfo(TcpInfo.TCP_ACK);
				}
			} else {
				int prevWin = pAckWinSize.get(key);
				if (win == prevWin) {
					if (payloadLen == 0 && !p.isRST()
							&& pi.getTcpInfo() != TcpInfo.TCP_KEEP_ALIVE) {
						if (pi.getTcpInfo() != null
								&& pi.getTcpInfo() != TcpInfo.TCP_ESTABLISH
								&& pi.getTcpInfo() != TcpInfo.TCP_CLOSE) {
							logger.warning("33 - Packet already typed");
						}
						pi.setTcpInfo(TcpInfo.TCP_ACK_DUP);
					}
				} else {
					pAckWinSize.put(key, win);
					if (payloadLen == 0 && !p.isRST()
							&& pi.getTcpInfo() != TcpInfo.TCP_KEEP_ALIVE) {
						if (pi.getTcpInfo() != null
								&& pi.getTcpInfo() != TcpInfo.TCP_ESTABLISH
								&& pi.getTcpInfo() != TcpInfo.TCP_CLOSE) {

							logger.warning("32 - Packet already typed");
						}
						pi.setTcpInfo(TcpInfo.TCP_WINDOW_UPDATE);
					}
				}
			}
		}
	}

	/**
	 * Analyze the packet to find the TCPInfo. Marked flags: TCP_ZERO_WINDOW
	 */
	private void analyzeZeroWindow() {
		for (PacketInfo pi : packets) {
			TCPPacket p = (TCPPacket) pi.getPacket();
			if (p.getPayloadLen() == 0 && p.getWindow() == 0 && !p.isSYN()
					&& !p.isFIN() && !p.isRST()) {
				pi.setTcpInfo(TcpInfo.TCP_ZERO_WINDOW);
			}
		}
	}

	/**
	 * Analyze the packet to find the TCPInfo. Marked flags: TCP_DATA_RECOVER,
	 * TCP_ACK_RECOVER
	 */
	private void analyzeRecoverPkts() {

		// "Recover data": its seq equals to the duplicated ACK
		// "Recover ack": its ack equals to the duplicated DATA + payload len

		Map<Long, TCPPacket> dupAckUl = new HashMap<Long, TCPPacket>();
		Map<Long, TCPPacket> dupAckDl = new HashMap<Long, TCPPacket>();
		Map<Long, TCPPacket> dupSeqUl = new HashMap<Long, TCPPacket>();
		Map<Long, TCPPacket> dupSeqDl = new HashMap<Long, TCPPacket>();

		for (PacketInfo pi : packets) {
			TCPPacket p = (TCPPacket) pi.getPacket();

			TcpInfo pType = pi.getTcpInfo();
			PacketInfo.Direction dir = pi.getDir();
			if (pType == TcpInfo.TCP_DATA_DUP) {
				if (dir == PacketInfo.Direction.UPLINK) {
					dupSeqUl.put(p.getSequenceNumber() + p.getPayloadLen(), p);
				} else {
					dupSeqDl.put(p.getSequenceNumber() + p.getPayloadLen(), p);
				}
			}

			// Duplicated data means duplicated ack as well
			if (pType == TcpInfo.TCP_ACK_DUP || pType == TcpInfo.TCP_DATA_DUP) {
				if (dir == PacketInfo.Direction.UPLINK) {
					dupAckUl.put(p.getAckNumber(), p);
				} else {
					dupAckDl.put(p.getAckNumber(), p);
				}
			}

			if (pType == TcpInfo.TCP_DATA) {
				if (dir == PacketInfo.Direction.UPLINK
						&& dupAckDl.containsKey(p.getSequenceNumber())) {
					pi.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
				if (dir == PacketInfo.Direction.DOWNLINK
						&& dupAckUl.containsKey(p.getSequenceNumber())) {
					pi.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
			}

			if (pType == TcpInfo.TCP_ACK) {
				if (dir == PacketInfo.Direction.UPLINK
						&& dupSeqDl.containsKey(p.getAckNumber())) {
					pi.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
				if (dir == PacketInfo.Direction.DOWNLINK
						&& dupSeqUl.containsKey(p.getAckNumber())) {
					pi.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
			}

			// A special case:
			// DL: TCP_ACK_DUP with ack = 1
			// DL: TCP_ACK_DUP with ack = 1
			// UL: TCP_ACK with seq = 1
			// UL: TCP_DATA with seq = 1 <==== This is NOT a DATA_RECOVER
			if (pType == TcpInfo.TCP_ACK || pType == TcpInfo.TCP_ACK_DUP
					|| pType == TcpInfo.TCP_ACK_RECOVER) {
				if (dir == PacketInfo.Direction.UPLINK) {
					dupAckDl.remove(p.getSequenceNumber());
				}
				if (dir == PacketInfo.Direction.DOWNLINK) {
					dupAckUl.remove(p.getSequenceNumber());
				}
			}

			// DL: TCP_DATA_DUP with seq = 1, len = 2
			// DL: TCP_DATA_DUP with seq = 1, len = 2
			// UL: TCP_DATA with ack = 3
			// UL: TCP_ACK with ack = 3 <==== This is NOT an ACK_RECOVER

			// Duplicated data means duplicated ack as well
			// But vise versa is not true
			if (pType == TcpInfo.TCP_DATA || pType == TcpInfo.TCP_DATA_RECOVER) {
				if (dir == PacketInfo.Direction.UPLINK) {
					dupAckUl.remove(p.getAckNumber());
				}
				if (dir == PacketInfo.Direction.DOWNLINK) {
					dupAckDl.remove(p.getAckNumber());
				}
			}
		}
	}
}
