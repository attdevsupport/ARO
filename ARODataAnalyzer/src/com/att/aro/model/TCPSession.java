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
import java.nio.ByteBuffer;
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

import com.att.aro.model.PacketInfo.Direction;
import com.att.aro.model.PacketInfo.TcpInfo;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.pcap.AROCryptoAdapter;
import com.att.aro.pcap.DomainNameSystem;
import com.att.aro.pcap.TCPPacket;
import com.att.aro.pcap.UDPPacket;
import com.att.aro.ssl.BIDIR_DATA_CHUNK;
import com.att.aro.ssl.MATCHED_RECORD;
import com.att.aro.ssl.SAVED_TLS_SESSION;
import com.att.aro.ssl.STORAGE_RANGE_MAPPING;
import com.att.aro.ssl.SslKey;
import com.att.aro.ssl.TLSHandshake;
import com.att.aro.ssl.TLS_SESSION_INFO;
import com.att.aro.ssl.crypto_openssl;
import com.att.aro.util.Util;

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

	private static final Logger lOGGER = Logger.getLogger(TCPSession.class
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

	private static final int PROT_RECORD_TLS			= 1;
	private static final int PROT_RECORD_TLS_FIRST		= 2;	//first record
	public static final int TLS_RECORD_CHANGE_CIPHER_SPEC 	= 20;
	public static final int TLS_RECORD_ALERT 				= 21;
	public static final int TLS_RECORD_HANDSHAKE 			= 22;
	public static final int TLS_RECORD_APP_DATA				= 23;
	public static final int TLS_HANDSHAKE_CLIENT_HELLO		= 1;
	public static final int TLS_HANDSHAKE_SERVER_HELLO		= 2;
	public static final int TLS_HANDSHAKE_CERTIFICATE 		= 11;
	public static final int TLS_HANDSHAKE_SERVER_HELLO_DONE = 14;
	public static final int TLS_HANDSHAKE_CLIENT_KEY_EXCHANGE = 16;
	public static final int TLS_HANDSHAKE_FINISHED 			= 20;
	public static final int TLS_HANDSHAKE_SERVER_KEY_EXCHANGE = 12;
	public static final int TLS_HANDSHAKE_CERTIFICATE_REQUEST = 13;
	public static final int TLS_HANDSHAKE_CERTIFICATE_VERIFY = 15;
	public static final int TLS_HANDSHAKE_NEW_SESSION_TICKET = 4;
	public static final int TLS_HANDSHAKE_NEXT_PROTOCOL = 67;
	public static final int TLS_MASTER_SECRET_LEN 			= 48;
	public static final int TLS_STATE_NULL					= 0;	
	public static final int TLS_STATE_C_HELLO				= 1;
	public static final int TLS_STATE_S_HELLO				= 2;
	public static final int TLS_STATE_S_CERTIFICATE			= 3;
	public static final int TLS_STATE_S_HELLODONE			= 4;
	public static final int TLS_STATE_C_KEYEXCHANGE			= 5;
	public static final int TLS_STATE_C_CHANGECIPHERSPEC	= 6;
	public static final int TLS_STATE_C_FINISHED			= 7;
	public static final int TLS_STATE_S_CHANGECIPHERSPEC	= 8;
	public static final int TLS_STATE_S_FINISHED			= 9;
	public static final int TLS_STATE_HS_FINISHED			= 10;
	public static final int COMPRESS_DEFLATE 				= 1;
	public static final int COMPRESS_NONE 					= 0;
	public static final int HTTPS_MODE_NONE					= 0;	//plain text, no https
	public static final int HTTPS_MODE_NORMAL				= 1;	//https, no compression
	public static final int HTTPS_MODE_DEFLATE				= 2;	//https, with deflate compression
	public static final int ALERT_LEVEL_WARNING 			= 1;
	public static final int ALERT_LEVEL_FATAL 				= 2;
	public static final int ALERT_CLOSE_NOTIFY 				= 0;
	private List<Integer> pktIndex = new ArrayList<Integer>();
	private List<BIDIR_DATA_CHUNK> bdcRaw = new ArrayList<BIDIR_DATA_CHUNK>();
	private List<MATCHED_RECORD> mrList =  new ArrayList<MATCHED_RECORD>();
	private Reassembler pStorageBothRAW = new Reassembler();
	private List<PacketRangeInStorage> pktRangesUl;
	private List<PacketRangeInStorage> pktRangesDl;
	private double tsTLSHandshakeBegin = -1;
	private double tsTLSHandshakeEnd = -1;
	private int protocol;	//app-layer-protocol
	private int httpsMode = HTTPS_MODE_NONE;
	private byte[] storageUlext = null;
	private byte[] storageDlext = null;
	private ByteArrayOutputStream pStorageULDCPT = new ByteArrayOutputStream(); //May be replaced by storageUl (Already defined above) after testing.
	private ByteArrayOutputStream pStorageDLDCPT = new ByteArrayOutputStream(); //May be replaced by storageDl (Already defined above) after testing.
	private ByteArrayOutputStream pStorageBothDCPT = new ByteArrayOutputStream();
	private List<STORAGE_RANGE_MAPPING> dec2encUL = new ArrayList<STORAGE_RANGE_MAPPING>();
	private List<STORAGE_RANGE_MAPPING> dec2encDL = new ArrayList<STORAGE_RANGE_MAPPING>();
	
	/**
	 * Tracks information about a reassembled session
	 */
	private static class Reassembler {
		private Long baseSeq;
		private long seq = -1;
		private List<PacketInfo> ooid = new ArrayList<PacketInfo>();
		private ByteArrayOutputStream storage = new ByteArrayOutputStream();
		private SortedMap<Integer, PacketInfo> packetOffsets = new TreeMap<Integer, PacketInfo>();
		private List<PacketRangeInStorage> pktRanges =  new ArrayList<PacketRangeInStorage>();
		
		void clear() {
			baseSeq = null;
			seq = -1;
			ooid.clear();
			storage.reset();
			packetOffsets = new TreeMap<Integer, PacketInfo>();
			pktRanges = new ArrayList<PacketRangeInStorage>();
		}

		private void getPacketIDList(int begin, int end, List<Integer> pktIDList) {
			int n = pktRanges.size();
			for (int i=0; i<n; i++) {
				if (end < pktRanges.get(i).offset) {
					break;
				}
				if (begin > (pktRanges.get(i).offset + pktRanges.get(i).size - 1)) {
					continue;
				}
				pktIDList.add(pktRanges.get(i).pktID);
			}
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
	
	private static class PacketRangeInStorage {
		private int offset;
		private int size;
		private int pktID;
		
		PacketRangeInStorage(int offset, int size, int pktID) {
			this.offset = offset;
			this.size = size;
			this.pktID = pktID;
		}
		public int getOffset() {
			return offset;
		}
		public void setOffset(int offset) {
			this.offset = offset;
		}
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}
		public int getPktID() {
			return pktID;
		}
		public void setPktID(int pktID) {
			this.pktID = pktID;
		}		
	};

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
	public static List<TCPSession> extractTCPSessions(Analysis analysis) throws IOException {
		lOGGER.entering("com.att.aro.model.TCPSession", "extractTCPSessions(Collection<PacketInfo>)");
		Map<String, TCPSession> allSessions = new LinkedHashMap<String, TCPSession>();
		List<PacketInfo> dnsPackets = new ArrayList<PacketInfo>();
		List<PacketInfo> udpPackets = new ArrayList<PacketInfo>();
		Map<InetAddress, String> hostMap = new HashMap<InetAddress, String>();
		lOGGER.finest("Starting loop through packets");
		int packetIndex = 0;
		Collection<PacketInfo> packets = (Collection<PacketInfo>)analysis.getPackets();
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
				lOGGER.warning("29 - Unable to determine packet direction");
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
			session.pktIndex.add(packetIndex++); 
		} // END: Iterating through all packets
		lOGGER.finest("End of loop through packets");
		lOGGER.finest("Starting creating sessions");
		// Reassemble sessions
		List<TCPSession> sessions = new ArrayList<TCPSession>(allSessions.values());
		Reassembler ul = new Reassembler();
		Reassembler dl = new Reassembler();
		for (int sessionIndex = 0; sessionIndex < sessions.size(); ++sessionIndex) {
			lOGGER.log(Level.FINEST, "Working with [{0}] session", sessionIndex);
			
			// Iterator is not used because items may be added to list during.
			// iterations
			TCPSession pS = sessions.get(sessionIndex);
			pS.bdcRaw.clear(); 
			
			lOGGER.log(Level.FINEST, "Session has {0} packets", pS.getPackets().size());

			// Reset variables
			boolean bTerminated = false;
			ul.clear();
			dl.clear();

			PacketInfo lastPacket = null;
			for (PacketInfo packetInfo: pS.packets) {
				
				TCPPacket p = (TCPPacket) packetInfo.getPacket();
				
				lOGGER.log(Level.FINEST, "Processing packet [{0}] with seq.# [{0}]",
						new Object[] {Integer.toString(packetInfo.getId()), Long.toString(p.getSequenceNumber())});
				if (p.isSsl()) {
					pS.ssl = true;
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
					lOGGER.warning("91 - No direction for packet");
					continue;
				}

				// If this is the initial sequence number
				if (p.isSYN()) {
					lOGGER.finest("It is TCP_ESTABLISH packet");
					packetInfo.setTcpInfo(TcpInfo.TCP_ESTABLISH);
					if (reassembledSession.baseSeq == null
							|| reassembledSession.baseSeq.equals(p.getSequenceNumber())) {
						lOGGER.finest("It is existing TCP session");
						// Finds establish
						reassembledSession.baseSeq = p.getSequenceNumber();
						if (p.getPayloadLen() != 0) {
							lOGGER.warning("92 - Payload in establish packet");
						}
					} else {
						lOGGER.finest("It is new TCP session");

						// New TCP session
						List<PacketInfo> currentList = pS.packets;
						int index = currentList.indexOf(packetInfo);
						if (!bTerminated) {
							lOGGER.fine("28 - Session termination not found");
						}

						// Correct packet list in original session
						pS.packets = new ArrayList<PacketInfo>(
								currentList.subList(0, index));

						// Create new session for remaining packets
						TCPSession newSession = new TCPSession(
								pS.remoteIP, pS.remotePort,
								pS.localPort);
						newSession.packets.addAll(currentList.subList(index, currentList.size()));
						sessions.add(newSession);

						// Break out of packet loop
						break;
					}

				} else {
					lOGGER.finest("It is NOT TCP_ESTABLISH packet");
					//FIN: No more data from sender
					//RST: Reset the connection
					if (p.isFIN() || p.isRST()) {

						lOGGER.finest("Packet has FIN or RST flag");

						// Calculate session termination info
						if (!bTerminated && lastPacket != null) {
							double delay = packetInfo.getTimeStamp()
									- lastPacket.getTimeStamp();
							pS.sessionTermination = new Termination(packetInfo,
									delay);
						}

						// Mark session terminated
						bTerminated = true;
						if (p.isFIN()) {
							packetInfo.setTcpInfo(TcpInfo.TCP_CLOSE);
						} else if (p.isRST()) {
							packetInfo.setTcpInfo(TcpInfo.TCP_RESET);
						}

					}

					// I believe this handles case where we have joined in the
					// middle of a TCP session
					if (reassembledSession.baseSeq == null) {
						lOGGER.finest("We have joined in the middle of a TCP session");
						switch (packetInfo.getDir()) {
						case UPLINK:
							ul.baseSeq = p.getSequenceNumber();
							dl.baseSeq = p.getAckNumber();
							break;
						case DOWNLINK:
							dl.baseSeq = p.getSequenceNumber();
							ul.baseSeq = p.getAckNumber();
							break;
						default:
							lOGGER.fine(Util.RB.getString("tls.error.invalidPktDir"));
						}
					}
				}

				// Get appName (there really should be only one per TCP session
				String appName = packetInfo.getAppName();
				if (appName != null) {
					lOGGER.log(Level.FINEST, "Adding {0} app name to the session", appName);
					lOGGER.log(Level.FINEST, "Packet dest. port : {0}", p.getDestinationPort());
					lOGGER.log(Level.FINEST, "       dest. IP   : {0}", p.getDestinationIPAddress());
					lOGGER.log(Level.FINEST, "       source port: {0}", p.getSourcePort());
					pS.appNames.add(appName);
					assert (pS.appNames.size() <=1)  : "" + pS.appNames.size() + " app names per TCP session: " + pS.getAppNames();
				}

				// Link packet to session
				packetInfo.setSession(pS);

				long _seq = p.getSequenceNumber() - reassembledSession.baseSeq;
				if (_seq < 0) {
					_seq += 0xFFFFFFFF;
					_seq++; 
				}
				long seq = (long)_seq;

				if (reassembledSession.seq == -1) {
					reassembledSession.seq = _seq;
				}

				if (_seq == reassembledSession.seq) {
					
					if (seq == reassembledSession.seq || (seq < reassembledSession.seq && seq + p.getPayloadLen() > reassembledSession.seq)) {
						if (p.getPayloadLen() > 0) {
							packetInfo.setTcpInfo(TcpInfo.TCP_DATA);
							byte[] data = p.getData();
							int effectivePayloadLen = p.getPayloadLen();
							int dataOffset = p.getDataOffset();
							if (data.length >= dataOffset + effectivePayloadLen) {
								reassembledSession.packetOffsets.put(reassembledSession.storage.size(), packetInfo);
								reassembledSession.storage.write(data, dataOffset, effectivePayloadLen);
								int offset = reassembledSession.storage.size() - effectivePayloadLen; 
								if(reassembledSession.pktRanges.size() == 0) {
									offset = 0;
								}
								reassembledSession.pktRanges.add(new PacketRangeInStorage(offset, effectivePayloadLen, packetInfo.getId()));
								pS.pStorageBothRAW.storage.write(data, dataOffset, effectivePayloadLen); 
								pS.updateBDC(packetInfo.getDir(), effectivePayloadLen); 
								reassembledSession.seq += effectivePayloadLen;
							}
							if (p.isSslHandshake()) {
								pS.lastSslHandshakePacket = packetInfo;
							}
						}
						if (p.isSYN() || p.isFIN()) {
							++reassembledSession.seq;
						}
					} 

					while (true) {
						boolean bOODone = true;
						List<PacketInfo> fixed = new ArrayList<PacketInfo>(
								reassembledSession.ooid.size());
						for (PacketInfo pi1 : reassembledSession.ooid) {
							TCPPacket p1 = (TCPPacket) pi1.getPacket();

							_seq = p1.getSequenceNumber() - reassembledSession.baseSeq;
							if (_seq < 0) {
								_seq += 0xFFFFFFFF;
								_seq++; 
							}
							
							long seq2 = (long)_seq; 

							if (_seq == reassembledSession.seq) {
								
								if (seq2 == reassembledSession.seq || (seq2 < reassembledSession.seq && seq2 + p.getPayloadLen() > reassembledSession.seq)) {
									if (p1.getPayloadLen() > 0) {
										pi1.setTcpInfo(TcpInfo.TCP_DATA);
										byte[] data = p1.getData();
										int effectivePayloadLen = p1.getPayloadLen();
										int dataOffset = p1.getDataOffset();
										if (data.length >= dataOffset + effectivePayloadLen) {
											reassembledSession.packetOffsets.put(reassembledSession.storage.size(),
													pi1);
											reassembledSession.storage.write(data, dataOffset, effectivePayloadLen);
											int offset = reassembledSession.storage.size() - effectivePayloadLen; 
											if(reassembledSession.pktRanges.size() == 0) {
												offset = 0;
											}
											reassembledSession.pktRanges.add(new PacketRangeInStorage(offset, effectivePayloadLen, packetInfo.getId())); 
											pS.pStorageBothRAW.storage.write(data, dataOffset, effectivePayloadLen); 
											pS.updateBDC(packetInfo.getDir(), effectivePayloadLen); 
											reassembledSession.seq += effectivePayloadLen;
										}
										if (p1.isSslHandshake()) {
											pS.lastSslHandshakePacket = pi1;
										}
									}
									if (p1.isSYN() || p1.isFIN()) {
										++reassembledSession.seq;
									}
								}
								
								fixed.add(pi1);
								bOODone = false;
							} else if (p1.getPayloadLen() == 0
									&& _seq == reassembledSession.seq - 1 && p1.isACK()
									&& !p1.isSYN() && !p1.isFIN()
									&& !p1.isRST()) {
								lOGGER.warning("31 - ???");
							}
						}
						reassembledSession.ooid.removeAll(fixed);
						if (bOODone) {
							break;
						}
					}

				} else { // out of order packet, i.e., seq != *XLseq
					if (p.getPayloadLen() == 0 && _seq == reassembledSession.seq - 1
							&& p.isACK() && !p.isSYN() && !p.isFIN()
							&& !p.isRST()) {
						if (packetInfo.getTcpInfo() != null) {
							lOGGER.warning("94 - ???");
						}
						packetInfo.setTcpInfo(TcpInfo.TCP_KEEP_ALIVE);
					} else {
						reassembledSession.ooid.add(packetInfo);
					}
				}

				lastPacket = packetInfo;
			} // packet loop
			
			pS.storageDl = dl.storage.toByteArray();
			pS.packetOffsetsDl = dl.packetOffsets;
			pS.pktRangesDl = dl.pktRanges;
			pS.storageUl = ul.storage.toByteArray();
			pS.packetOffsetsUl = ul.packetOffsets;
			pS.pktRangesUl = ul.pktRanges;
			
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
		
		lOGGER.finest("All sessions where created");
		lOGGER.finest("Starting looping through all sessions");
		
		if(TraceData.getCryptAdapter() == null) {
			for (TCPSession s : sessions) {
				for (PacketInfo p : s.packets) {
					s.bytesTransferred += p.getLen();
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
		} else {
			List<List<BIDIR_DATA_CHUNK>> bdcRawList = new ArrayList<List<BIDIR_DATA_CHUNK>>();
			int passIndex = 0;
			int bNeed2ndPass = 0;
			AROCryptoAdapter.resetSSL_keys();
			for (int nPass=1; nPass<=2; nPass++) {
				passIndex = 0;
				// More session parsing
				for (TCPSession session : sessions) {
					if (nPass == 2 && bdcRawList.get(passIndex).size() == 0) {
						passIndex++;
						continue;
					}
					
					if (nPass == 1) {
						for (PacketInfo packet : session.packets) {
							session.bytesTransferred += packet.getLen();
						}
					} else {
						session.bdcRaw = bdcRawList.get(passIndex);
					}
					
					session.generateRecords(PROT_RECORD_TLS);
					if(session.mrList.size() > 0) {
						int retVal = session.parse(analysis.getPackets(), nPass);
						if(nPass == 1 && retVal == -2) {
							session.pStorageULDCPT.reset();
							session.pStorageDLDCPT.reset();
							session.pStorageBothDCPT.reset();
							session.dec2encDL.clear();
							session.dec2encUL.clear();
							session.tsTLSHandshakeBegin = -1;
							session.tsTLSHandshakeEnd = -1;
							session.protocol = -1;
							session.httpsMode = HTTPS_MODE_NONE;
							
							bdcRawList.add(session.bdcRaw);
							bNeed2ndPass = 1;
							passIndex++;
							continue;
						} else if (retVal == 1){
							for (PacketInfo packet : session.packets) {
								if (packet.getPacket() instanceof TCPPacket) {
									TCPPacket tcp = (TCPPacket) packet.getPacket();
									if ((tcp.isSsl()) || (tcp.getDestinationPort() == 443) || (tcp.getSourcePort() == 443)) {
										analysis.setTotalHTTPSAnalyzedBytes(analysis.getTotalHTTPSAnalyzedBytes() + packet.getLen());
									}
								}
							}
							
							if(session.pStorageULDCPT.size() > session.storageUl.length) {
								session.storageUlext = session.storageUl;
							}
							if(session.pStorageDLDCPT.size() > session.storageDl.length) {
								session.storageDlext = session.storageDl;
							}
							session.storageUl = session.pStorageULDCPT.toByteArray();
							session.storageDl = session.pStorageDLDCPT.toByteArray();
						}
					} 
	
					session.analyzeACK();
					session.analyzeZeroWindow();
					session.analyzeRecoverPkts();
		
					// TODO Validate TCP info is set on all
					// CheckTCPInfo(s);
		
					// // TODO Check this?
					// s.pStorageUL->CheckPacketsRange();
					// s.pStorageDL->CheckPacketsRange();
					//
		
					// Parse HTTP request response info
					session.requestResponseInfo = HttpRequestResponseInfo
							.extractHttpRequestResponseInfo(session);
					for (HttpRequestResponseInfo rr : session.requestResponseInfo) {
						if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
							// Assume first host found is same for entire session
							if (session.domainName == null) {
								String host = rr.getHostName();
								if (host != null) {
									URI referrer = rr.getReferrer();
									session.remoteHostName = host;
									session.domainName = referrer != null ? referrer
											.getHost() : host;
								}
							}
						} else if (rr.getDirection() == HttpRequestResponseInfo.Direction.RESPONSE) {
							if (rr.getContentLength() > 0) {
								++session.fileDownloadCount;
							}
						}
					}
					
					if (session.domainName == null) {
						session.domainName = session.remoteHostName != null ? session.remoteHostName : session.remoteIP.getHostAddress();
					}
					
					if (nPass == 1) {
						List<BIDIR_DATA_CHUNK> dummy = new ArrayList<BIDIR_DATA_CHUNK>();
						bdcRawList.add(dummy);
					}
					passIndex++; 
				}
				
				if (bNeed2ndPass == 0) {
					break;
				}
			}
		}
		ul.clear();
		dl.clear();
		lOGGER.finest("Ended looping through all sessions");
		
		lOGGER.finest("Sorting sessions");
		Collections.sort(sessions);
		lOGGER.finest("End of sorting sessions");

		lOGGER.exiting("com.att.aro.model.TCPSession", "extractTCPSessions(Collection<PacketInfo>)");
		
		/*Get UDP sessions.*/
		if(!udpPackets.isEmpty()){
			List<TCPSession> udpSessions = getUDPSessions(udpPackets,sessions);
			sessions.addAll(udpSessions);
		}

		return sessions;
	}
	
	private int parse(List<PacketInfo> packetList, int nPass) {
		TLSHandshake handshake = new TLSHandshake();
		TLS_SESSION_INFO tsiServer = new TLS_SESSION_INFO(0);
		TLS_SESSION_INFO tsiClient = new TLS_SESSION_INFO(1);
		TLS_SESSION_INFO tsiPending = new TLS_SESSION_INFO(2);
		byte[] clientRandom = null;
		byte[] serverRandom = null;
		byte[] sessionID = null;
		byte[] thisSessionID = null;
		byte[] clientTicketExtension = null;
		byte[] serverTicketExtension = null;
		byte[] masterSecret = null;
		byte[] serverIssuedTicket = null;
		int bServerIssuedTicket = 0;
		int bClientTicketExtension = 0;
		int bClientFinished = 0;
		int bServerFinished = 0;
		int bClientChangeCipher = 0;
		int bServerChangeChiper = 0;
		int state = TLS_STATE_NULL;
		int bClientClosed = 0;
		int bServerClosed = 0;
		long sessionOffsetUL = 0;
		long sessionOffsetDL = 0;
		double serverHelloTS = -1.0f;
		
		int n = this.mrList.size();		
		for (int i=0; i<n; i++) {
			if (bClientClosed !=0 && bServerClosed != 0) {
				break;
			}
			
			PacketInfo.Direction dir = this.mrList.get(i).getDir();
			ByteBuffer pData = ByteBuffer.wrap(getRecord(this.mrList.get(i)));
			if(pData == null) {
				lOGGER.fine(Util.RB.getString("tls.error.ssldata"));
				return -1;
			}
			
			if (dir == PacketInfo.Direction.UPLINK) {
				sessionOffsetUL += this.mrList.get(i).getBytes();
			} else {
				sessionOffsetDL += this.mrList.get(i).getBytes();
			}
			
			byte[] encRecPayload = null; //encrypted
			byte[] recPayload = new byte[65536]; //decrypted
			Integer[] recPayloadLen = new Integer[1];
			{
				byte recType = pData.get(0);
				if (checkTLSVersion(pData.array(), 1) == 0) {
					lOGGER.fine(Util.RB.getString("tls.error.sslversion"));
					return -1;
				}
				int encRecPayloadSize = pData.getShort(3); //Reverse not required as Java follows big endian byte order
				if (encRecPayloadSize > 0) {
					for(int j=0; j<5; j++) {
						pData.get();
					}
					encRecPayload = new byte[encRecPayloadSize];
					pData.get(encRecPayload, 0, encRecPayloadSize);
					pData.position(0);
					
					int returnVal = -1;
					switch (dir) {
					case UPLINK:
						//MyAssert(tsiServer.pCipherClient == NULL, 1159);
						returnVal = tsiServer.Decrypt(encRecPayload, recPayload, recPayloadLen, PacketInfo.Direction.UPLINK, recType);
						break;

					case DOWNLINK:
						//MyAssert(tsiClient.pCipherServer == NULL, 1160);
						returnVal = tsiClient.Decrypt(encRecPayload, recPayload, recPayloadLen, PacketInfo.Direction.DOWNLINK, recType);
						break;
						
					default:
						lOGGER.fine(Util.RB.getString("tls.error.invalidPktDir"));
						return -1;
					}
					
					if (returnVal <= 0) {
						lOGGER.fine("30012 - Error in SSL record decryption.");
						return -1;
					}
				}
				
				ByteBuffer recPayload_buff =  ByteBuffer.wrap(recPayload);
				int recOfs = -1;
				int recLen = 5 + encRecPayloadSize;
				if (dir == PacketInfo.Direction.UPLINK) {
					recOfs = (int)sessionOffsetUL - this.mrList.get(i).getBytes();
				} else {
					recOfs = (int)sessionOffsetDL - this.mrList.get(i).getBytes();
				}
				
				switch (recType) {
					case TLS_RECORD_HANDSHAKE:
					{
						int handshakeOffset = 0;
						Integer[] handshakeSize = new Integer[1];
						handshakeSize[0] = -1;
						
						while (true) {
							if (handshakeOffset == recPayloadLen[0]) {
								break;
							}
						
							for(int j=0; j<handshakeOffset; j++) {
								recPayload_buff.get();
							}
							byte[] recPayloadtemp = new byte[recPayloadLen[0] - handshakeOffset];
							recPayload_buff.get(recPayloadtemp, 0, recPayloadLen[0] - handshakeOffset);
							recPayload_buff.position(0);
							
							int r = handshake.read(recPayloadtemp, recPayloadLen[0] - handshakeOffset, handshakeSize);
							if (r != 1) {
								if(r == 0) {
									lOGGER.fine(Util.RB.getString("tls.error.readhandshake"));
								}
								return -1;
							}
							handshakeOffset += handshakeSize[0];

							switch (handshake.getType()) {
								case TLS_HANDSHAKE_CLIENT_HELLO:
									{	
										clientRandom = new byte[32];
										for(int index=0; index<32; index++) {
											clientRandom[index] = handshake.getClientRandom()[index]; //clientRandom.SetData(handshake.clientRandom, 32);
										}
										
										state = TLS_STATE_C_HELLO;

										{
											List<Integer> l = new ArrayList<Integer>();
											Reassembler rr = new Reassembler();
											rr.pktRanges = this.pktRangesUl;
											rr.getPacketIDList(recOfs, recOfs, l);
											if(l.size() == 1) {
												this.tsTLSHandshakeBegin = packetList.get(l.get(0) - 1).getPacket().getTimeStamp();
											} else {
												lOGGER.fine(Util.RB.getString("tls.error.PacketIDList"));
												return -1;
											}											
										}

										if (handshake.getSessionIDLen() == 0) {
											sessionID = null;
										} else {
											sessionID = new byte[handshake.getSessionIDLen()];
											for(int index=0; index<handshake.getSessionIDLen(); index++) {
												sessionID[index] = handshake.getSessionID()[index]; //sessionID.SetData(handshake.sessionID, handshake.sessionIDLen);
											}											
										}

										clientTicketExtension = null;
										if (handshake.getTicketLen() == -1) {
											bClientTicketExtension = 0;
										} else if (handshake.getTicketLen() == 0) {
											bClientTicketExtension = 1;
										} else {
											bClientTicketExtension = 1;
											clientTicketExtension = new byte[handshake.getTicketLen()];
											for(int index=0; index<handshake.getTicketLen(); index++) {
												clientTicketExtension[index] = handshake.getTicket()[index]; //clientTicketExtension.SetData(handshake.ticket,handshake.ticketLen);
											}											
										}
										break;
									}
									
								case TLS_HANDSHAKE_SERVER_HELLO:
									{
										{
											List<Integer> l = new ArrayList<Integer>();
											Reassembler rr = new Reassembler();
											rr.pktRanges = this.pktRangesUl;
											rr.getPacketIDList(recOfs, recOfs, l);
											if(l.size() == 1) {
												serverHelloTS = packetList.get(l.get(0) - 1).getPacket().getTimeStamp();
											} else {
												lOGGER.fine(Util.RB.getString("tls.error.PacketIDList"));
												return -1;
											}
										}
										
										serverRandom = new byte[32];
										for(int index=0; index<32; index++) {
											serverRandom[index] = handshake.getServerRandom()[index]; //serverRandom.SetData(handshake.serverRandom, 32);
										}
										tsiPending.pSuite = crypto_openssl.tlsGetCipherSuite(handshake.getCipherSuite()) ;//tls_get_cipher_suite(handshake.cipherSuite);
										
										if (tsiPending.pSuite == null) {
											lOGGER.fine(Util.RB.getString("tls.error.keyexchange"));
											return -1;
										}
										//TODO: Diffie-Hellman not supported
										if(tsiPending.pSuite.getKey_exchange() != crypto_openssl.tls_key_exchange.TLS_KEY_X_RSA) {
											lOGGER.fine(Util.RB.getString("tls.error.onlyRSAsupported"));
											return -1;
										}
	
										tsiPending.pCipherData = crypto_openssl.tlsGetCipherData(tsiPending.pSuite.getCipher());
										if(tsiPending.pCipherData == null) {
											lOGGER.fine(Util.RB.getString("tls.error.cipherdata"));
											return -1;
										}
										
										tsiPending.setCompressionMethod(handshake.getCompressionMethod());
										if (handshake.getSessionIDLen() == 0) {
											sessionID = null;
										} else {
											if(sessionID != null) {
												boolean match = true;
												for(int j=0; j<handshake.getSessionIDLen(); j++) {
													if(handshake.getSessionID()[j] != sessionID[j]) {
														match = false;
														break;
													}
												}
												if (handshake.getSessionIDLen() == sessionID.length && match == true) {												
													//sessionID of client and server match
													lOGGER.fine("sessionID of client and server match");
												} else {
													sessionID = null;
												}
											}
											thisSessionID = new byte[handshake.getSessionIDLen()];
											for(int index=0; index<handshake.getSessionIDLen(); index++) {
												thisSessionID[index] = handshake.getSessionID()[index]; //thisSessionID.SetData(handshake.sessionID, handshake.sessionIDLen);
											}
										}
	
										state = TLS_STATE_S_HELLO;
	
										serverTicketExtension = null;
										if (handshake.getTicketLen() == -1) {
											lOGGER.fine("Invalid TLS handshake ticket length");
										} else if (handshake.getTicketLen() == 0) {
											lOGGER.fine("Zero TLS handshake ticket length");
										} else {
											
											serverTicketExtension = new byte[handshake.getTicketLen()];
											for(int index=0; index<handshake.getTicketLen(); index++) {
												serverTicketExtension[index] = handshake.getTicket()[index]; //serverTicketExtension.SetData(handshake.ticket,handshake.ticketLen);
											}											
										}	
										break;
									}
									
									case TLS_HANDSHAKE_CERTIFICATE:
									{
										state = TLS_STATE_S_CERTIFICATE;
										break;
									}
	
									case TLS_HANDSHAKE_SERVER_HELLO_DONE:
									{
										state = TLS_STATE_S_HELLODONE;
										break;
									}
								
									case TLS_HANDSHAKE_CLIENT_KEY_EXCHANGE:
									{
										byte[] master = new byte[4096];
										int retVal = -1;
									
										if (serverHelloTS == -1.0 || serverRandom == null) {
											lOGGER.fine(Util.RB.getString("tls.error.invalidRecType"));
											return -1;
										}
										
										//get the master key by directly read from the SSL log
										retVal = SslKey.getMasterFromSSLLog(serverHelloTS, master,	clientRandom, serverRandom);
										if (retVal == 0) {
											if(nPass == 2) {
												lOGGER.warning(Util.RB.getString("tls.error.masterNotFound"));
											}
											return -2;
										}		

										retVal = SslKey.setupCiphers(master, clientRandom, serverRandom, tsiPending);
										if (retVal == -1) {
											return -1;
										}
										
										/* Interaction between session ID and session ticket (From RFC 5077)
										Rule 1:
										Client: no ticket
										Server: issue a ticket
										Server should put an empty session ID. In any case Session ID is always ignored

										Rule 2:
										Server rejects a ticket, full handshake
										Session ID is valid

										Case 3:
										Server accepts a ticket
										The server must use the same session ID as the client's

										Case 4:
										Client presents a ticket and session ID
										Server must not use the session ID of the cilent for resuming session

										Case 5:
										Client presents a ticket and session ID is empty
										Server's session ID is ignored. 
										*/

										masterSecret = new byte[TLS_MASTER_SECRET_LEN];
										for(int index=0; index<TLS_MASTER_SECRET_LEN; index++) {
											masterSecret[index] = master[index]; //masterSecret.SetData(master, TLS_MASTER_SECRET_LEN);
										}
										retVal = SslKey.saveTLSSessionByID(thisSessionID, master);
										state = TLS_STATE_C_KEYEXCHANGE;
										break;
									}
									
									case TLS_HANDSHAKE_FINISHED:
									{								
										switch (dir) {
											case UPLINK:
												{
													bClientFinished = 1;
													state = TLS_STATE_C_FINISHED;
													break;
												}

											case DOWNLINK:
												{
													bServerFinished = 1;
													state = TLS_STATE_S_FINISHED;
													break;
												}

											default:
												lOGGER.fine(Util.RB.getString("tls.error.invalidPktDir"));
												return -1;
										}

										if (bClientFinished == 1 && bServerFinished ==1) {
											List<Integer> l = new ArrayList<Integer>();
											Reassembler rr = new Reassembler();
											if (dir == PacketInfo.Direction.DOWNLINK) {
												rr.pktRanges = this.pktRangesDl;
											} else {
												rr.pktRanges = this.pktRangesUl;
											}
											rr.getPacketIDList(recOfs, recOfs, l);
											this.tsTLSHandshakeEnd = packetList.get(l.get(0) - 1).getPacket().getTimeStamp();
											
											state = TLS_STATE_HS_FINISHED;
										}
										break;
									}
									
									case TLS_HANDSHAKE_SERVER_KEY_EXCHANGE:
									case TLS_HANDSHAKE_CERTIFICATE_REQUEST:
									case TLS_HANDSHAKE_CERTIFICATE_VERIFY:								
										{
											lOGGER.fine(Util.RB.getString("tls.error.unsupportedTLSHandshake"));
											return -1;
										}
										
									case TLS_HANDSHAKE_NEW_SESSION_TICKET:
									{
										if(bServerIssuedTicket != 0 || masterSecret == null) {
											lOGGER.fine(Util.RB.getString("tls.error.invalidServerIssuedTicket"));
											return -1;
										}
										
										serverIssuedTicket = new byte[handshake.getTicketLen()];
										for(int index=0; index<handshake.getTicketLen(); index++) {
											serverIssuedTicket[index] = handshake.getTicket()[index]; //serverIssuedTicket.SetData(handshake.ticket, handshake.ticketLen);
										}
										bServerIssuedTicket = 1;

										if(masterSecret.length != TLS_MASTER_SECRET_LEN) {
											lOGGER.fine(Util.RB.getString("tls.error.incorrectMasterLen"));
											return -1;
										}
										r = SslKey.saveTLSSessionByTicket(serverIssuedTicket, masterSecret);
										break;
									}
									
									case TLS_HANDSHAKE_NEXT_PROTOCOL:
									{
										this.protocol = handshake.getNextProtocol();
										break;
									}
									
									default:
									lOGGER.fine(Util.RB.getString("tls.error.invalidHSType"));
									return -1;									
							} //End of inner switch
						}//End of while loop
						break;
					} //End of 1st case in main switch
					
					case TLS_RECORD_CHANGE_CIPHER_SPEC:
					{
						//the record payload should only contain 0x01
						switch (dir) {
							case UPLINK:
								{
									switch (state) {
										case TLS_STATE_C_KEYEXCHANGE:
											break;

										case TLS_STATE_S_FINISHED:
											break;

										default:
											lOGGER.fine(Util.RB.getString("tls.error.invalidTLSstate"));
											return -1;
									}

									//client side update
									tsiServer.CopyFrom(tsiPending);
									tsiServer.setpCipherClient(-1);
									TraceData.getCryptAdapter().setcryptociphernull(0, 1);
									tsiPending.setpCipherServer(-1);
									TraceData.getCryptAdapter().setcryptociphernull(2, 0);
									tsiServer.InitDecompression();
									state = TLS_STATE_C_CHANGECIPHERSPEC;
									bClientChangeCipher = 1;
									break;
								}

							case DOWNLINK:
								{
									switch (state) {
										case TLS_STATE_C_FINISHED:
											{
												if(state != TLS_STATE_C_FINISHED) {
													lOGGER.log(Level.FINE, "30029 - Wrong TLS state");
												}
												break;
											}

										case TLS_STATE_S_HELLO:
											{
												if(state != TLS_STATE_S_HELLO) {
													lOGGER.log(Level.FINE, "30030 - Wrong TLS state");
												}

												SAVED_TLS_SESSION[] pSaved = new SAVED_TLS_SESSION[1];
												int r = SslKey.getSavedTLSSessionByID(sessionID, pSaved);												
												if (r == 0) {
													if (bClientTicketExtension == 1) {
														r = SslKey.getSavedTLSSessionByTicket(clientTicketExtension, pSaved);
													} else {
														r = 0;
													}
													
													if (r == 0) {
														return 0;
													}
												}

												masterSecret = new byte[TLS_MASTER_SECRET_LEN];
												for(int index=0; index<TLS_MASTER_SECRET_LEN; index++) {
													masterSecret[index] = pSaved[0].getMaster()[index]; //masterSecret.SetData(pSaved.master, TLS_MASTER_SECRET_LEN);
												}
												r = SslKey.setupCiphers(pSaved[0].getMaster(), clientRandom, serverRandom, tsiPending);
												if (r == -1) {
													return -1;
												}
												break;
											}

										default:
											lOGGER.fine(Util.RB.getString("tls.error.invalidTLSstate"));
											return -1;
									}
																											
									//server side update
									tsiClient.CopyFrom(tsiPending);
									tsiClient.setpCipherServer(-1);
									TraceData.getCryptAdapter().setcryptociphernull(1, 0);
									tsiPending.setpCipherClient(-1);
									TraceData.getCryptAdapter().setcryptociphernull(2, 1);
									tsiClient.InitDecompression();
									state = TLS_STATE_S_CHANGECIPHERSPEC;
									bServerChangeChiper = 1;
									break;
								}

							default: //dir
								lOGGER.fine(Util.RB.getString("tls.error.invalidPktDir"));
								return -1;
						}
						
						if (bClientChangeCipher == 1 && bServerChangeChiper == 1) {
							//At this moment the pending state can be cleaned.
							tsiPending.Clean();

							if (tsiClient.getCompressionMethod() == COMPRESS_DEFLATE) {
								this.httpsMode = HTTPS_MODE_DEFLATE;
							}
						}
						break;
					}
					
					case TLS_RECORD_APP_DATA:
						{
							if ((recPayloadLen == null) || (recPayloadLen[0] == null) || (recPayloadLen[0] == 0)) {
								break;
							}
	
							STORAGE_RANGE_MAPPING srm = new STORAGE_RANGE_MAPPING();
							srm.nx = recOfs;
							srm.ny = recOfs + recLen - 1;
	
							switch (dir) {
								case UPLINK:
									
									if (bClientClosed == 1) {
										break;
									}
									
									srm.x = this.pStorageULDCPT.size();
									srm.y = srm.x + recPayloadLen[0] - 1;
	
									this.pStorageULDCPT.write(recPayload, 0, recPayloadLen[0]); //session.pStorageUL_DCPT->PushData(recPayload);
									this.pStorageBothDCPT.write(recPayload, 0, recPayloadLen[0]); //session.pStorageBoth_DCPT->PushData(recPayload);
									this.dec2encUL.add(srm);									
									break;
	
								case DOWNLINK:
									if (bServerClosed == 1) {
										break;
									}
									srm.x = this.pStorageDLDCPT.size();
									srm.y = srm.x + recPayloadLen[0] - 1;
									
									this.pStorageDLDCPT.write(recPayload, 0, recPayloadLen[0]); //session.pStorageDL_DCPT->PushData(recPayload);
									this.pStorageBothDCPT.write(recPayload, 0, recPayloadLen[0]); //session.pStorageBoth_DCPT->PushData(recPayload);
									this.dec2encDL.add(srm);
									break;
									
								default:
									lOGGER.fine(Util.RB.getString("tls.error.invalidPktDir"));
									return -1;
							}
							break;
						}
					
					case TLS_RECORD_ALERT:
						{
							if(recPayloadLen[0] != 2) {
								lOGGER.fine(Util.RB.getString("tls.error.wrongTLS_AlertSize"));
								return -1;
							}
							
							byte alertLevel = recPayload[0];
							byte alert = recPayload[1];
	
							if(alertLevel != ALERT_LEVEL_WARNING && alertLevel != ALERT_LEVEL_FATAL) {
								lOGGER.fine(Util.RB.getString("tls.error.wrongTLS_AlertLevel"));
								return -1;
							}
							
							switch (alert) {
								case ALERT_CLOSE_NOTIFY:
								{
									if (dir == PacketInfo.Direction.UPLINK) {
										bClientClosed = 1;
									} else {
										bServerClosed = 1;
									}
									break;
								}
	
								default:
									lOGGER.fine(Util.RB.getString("tls.error.invalidTLS_Alert"));
									return -1;
							}
							
							break;
						}
					
					default:
						lOGGER.fine(Util.RB.getString("tls.error.invalidHSType"));
						return -1;
				}//End of main switch
			}//End of custom block
		}//End of FOR loop
		
		if(tsiServer.getDecompresser() != null) {
			tsiServer.getDecompresser().end();
		}
		if(tsiClient.getDecompresser() != null) {
			tsiClient.getDecompresser().end();
		}
		if(tsiPending.getDecompresser() != null) {
			tsiPending.getDecompresser().end();
		}
		return 1;			
	}
	
	private byte[] getRecord(MATCHED_RECORD mr) {
		ByteBuffer pData = null;
		byte[] pOutput = null;
		switch (mr.getDir()) {
			case UPLINK:
				pData = ByteBuffer.wrap(this.getStorageUl());
				pOutput = new byte[this.getStorageUl().length - mr.getUniDirOffset()];
				for(int i=0; i<mr.getUniDirOffset(); i++) {
					pData.get();
				}
				pData.get(pOutput, 0, this.getStorageUl().length - mr.getUniDirOffset());
				break;
				
			case DOWNLINK:
				pData = ByteBuffer.wrap(this.getStorageDl());
				pOutput = new byte[this.getStorageDl().length - mr.getUniDirOffset()];
				for(int i=0; i<mr.getUniDirOffset(); i++) {
					pData.get();
				}
				pData.get(pOutput, 0, this.getStorageDl().length - mr.getUniDirOffset());
				break;
				
			default:
				return null;
		}
		return pOutput;
	}
	
	private void updateBDC(Direction dir, int payloadLen) {
		int n = this.bdcRaw.size();
		BIDIR_DATA_CHUNK back = null;
		
		if(n > 0) {
			back = this.bdcRaw.get(n-1);
		}
		
		if (n == 0 || back.getDir() != dir) {
			BIDIR_DATA_CHUNK c = new BIDIR_DATA_CHUNK();
			c.setBytes(payloadLen);
			c.setDir(dir);
			c.setPrevBytes(this.bdcRaw.size() == 0 ? 0 : (back.getPrevBytes() + back.getBytes())); 
			this.bdcRaw.add(c);
		} else {
			back.setBytes(back.getBytes() + payloadLen);
		}
	}
	
	private void generateRecords(int protocol) {
		this.mrList.clear();
		if (this.bdcRaw.size() == 0) {
			return;
		}
		
		Reassembler pBothStorage = null;
		switch (protocol) {
			case PROT_RECORD_TLS:
				pBothStorage = this.pStorageBothRAW;
				break;

				default:
				lOGGER.log(Level.FINE, "30001 - Unexpected protocol.");
		}
		matchRecords(protocol, pBothStorage, PacketInfo.Direction.UPLINK);
		int k1 = this.mrList.size();
		matchRecords(protocol, pBothStorage, PacketInfo.Direction.DOWNLINK);
		int k2 = this.mrList.size();
		checkRecords();
		checkCompleteness(protocol, 0,  k1-1, PacketInfo.Direction.UPLINK);
		checkCompleteness(protocol, k1, k2-1, PacketInfo.Direction.DOWNLINK);
		Collections.sort(this.mrList);
	}
	
	private void checkCompleteness(int protocol, int nFrom, int nTo, PacketInfo.Direction dir) {
		String[] dirStr = {"", "UPLINK", "DOWNLINK"};
		String[] protStr = {"", "TLS", "", "SPDY_V2", ""};
		int n = this.bdcRaw.size();
		int i = 0;
		while(i<n && this.bdcRaw.get(i).getDir() != dir) {
			i++;
		}
		if (i == n) {
			String msg = null;
			if (dir == PacketInfo.Direction.UPLINK) {
				msg = String.format("30005 - Raw data stream not observed in %s direction (prot: %s, localPort=%d)", 
						dirStr[1], protStr[protocol], this.localPort);
			} else {
				msg = String.format("30005 - Raw data stream not observed in %s direction (prot: %s, localPort=%d)", 
						dirStr[2], protStr[protocol], this.localPort);
			}
			lOGGER.log(Level.FINE, msg);
			return;
		}

		if (nFrom > nTo) {
			/*
			UI::WarningMessage("Record not observed in %s direction (prot: %s, localPort=%d)", 
				dirStr[dir], protStr[prot], s.localPort);
			*/
			return;
		}

		if (this.mrList.get(nFrom).getBeginBDC() != i || this.mrList.get(nFrom).getBeginOfs() != 0) {
			String msg = null;
			if (dir == PacketInfo.Direction.UPLINK) {
				msg = String.format("30006 - Records do not start from the beginning of the %s data stream (prot: %s, localPort=%d)", 
						dirStr[1], protStr[protocol], this.localPort);
			} else {
				msg = String.format("30006 - Records do not start from the beginning of the %s data stream (prot: %s, localPort=%d)", 
						dirStr[2], protStr[protocol], this.localPort);
			}
			lOGGER.log(Level.FINE, msg);
			return;
		}			

		i = n-1;
		while (i>=0 && this.bdcRaw.get(i).getDir() != dir) {
			i--;
		}
		
		if(i < 0) {
			lOGGER.log(Level.FINE, "30007 - Error in checking completeness of SSL records.");
		}

		if (this.mrList.get(nTo).getEndBDC() != i || this.mrList.get(nTo).getEndOfs() != this.bdcRaw.get(i).getBytes() - 1) {
			String msg = null;
			if (dir == PacketInfo.Direction.UPLINK) {
				msg = String.format("30008 - Records do not stop at the end of the %s data stream (prot: %s, localPort=%d)", 
						dirStr[1], protStr[protocol], this.localPort);
			} else {
				msg = String.format("30008 - Records do not stop at the end of the %s data stream (prot: %s, localPort=%d)", 
						dirStr[2], protStr[protocol], this.localPort);
			}
			lOGGER.log(Level.FINE, msg);
			return;
		}

		//test whether the records are consecutive
		for (i=nFrom+1; i<=nTo; i++) {				
			if (this.mrList.get(i-1).getUniDirOffset() + this.mrList.get(i-1).getBytes() != this.mrList.get(i).getUniDirOffset()) {
				lOGGER.log(Level.FINE, "30009 - Error in checking completeness of SSL records.");
			}
			
			int bInOrder = 0;
			if (this.mrList.get(i-1).getEndBDC() < this.mrList.get(i).getBeginBDC()) {
				bInOrder = 1;
			}
			if ((this.mrList.get(i-1).getEndBDC() == this.mrList.get(i).getBeginBDC()) &&
					(this.mrList.get(i-1).getEndOfs() <  this.mrList.get(i).getBeginOfs())) {
				bInOrder = 1;
			}
			
			if (bInOrder ==0) {
				lOGGER.log(Level.FINE, "30010 - Error in checking completeness of SSL records.");
			}
		}
	}
	
	private void checkRecords() {
		int n = this.bdcRaw.size();
		int m = this.mrList.size();
		for (int i=0; i<m; i++) {
			MATCHED_RECORD mr = this.mrList.get(i);
			boolean bPass = 
				mr.getBeginBDC() >= 0 && mr.getBeginBDC() < n && mr.getEndBDC() >= 0 && mr.getEndBDC() < n &&
				mr.getBeginOfs() >= 0 && mr.getBeginOfs() < this.bdcRaw.get(mr.getBeginBDC()).getBytes() &&
				mr.getEndOfs() >= 0 && mr.getEndOfs() < this.bdcRaw.get(mr.getEndBDC()).getBytes() &&
				mr.getDir() == this.bdcRaw.get(mr.getBeginBDC()).getDir() && mr.getDir() == this.bdcRaw.get(mr.getEndBDC()).getDir() &&
				mr.getBytes() > 0 && (mr.getDir() == PacketInfo.Direction.UPLINK || mr.getDir() == PacketInfo.Direction.DOWNLINK) &&
				(mr.getBeginBDC() < mr.getEndBDC() || (mr.getBeginBDC() == mr.getEndBDC() && mr.getBeginOfs() < mr.getEndOfs()));
			if(bPass != true) {
				lOGGER.log(Level.FINE, "30004 - ssl record mismatch.");
			}
		}
	}

	private void matchRecords(int prot, Reassembler pBothStorage, PacketInfo.Direction dir) {
		MATCHED_RECORD mr  = new MATCHED_RECORD();

		//Step 1: find the first matched record
		//We assume the first matched record always at the BEGINNING of each BDC chunk
		boolean bMatchedInitial = false;
		int n = this.bdcRaw.size();
		int unidirOffset = 0;
		for (int i=0; i<n; i++) {
			if (this.bdcRaw.get(i).getDir() != dir) {
				continue;
			}
			mr.SetInput(i, 0, dir, unidirOffset);
			if (matchNextRecord(prot, pBothStorage, null, mr)) {
				bMatchedInitial = true;
				this.mrList.add(mr);
				break;
			}
			unidirOffset += this.bdcRaw.get(i).getBytes();
		}
		if (bMatchedInitial == false) {
			return;
		}

		//Step 2: keep matching
		while (true) {
			MATCHED_RECORD mr2 = new MATCHED_RECORD();
			int size = this.mrList.size();
			MATCHED_RECORD back = null;
			if(size > 0) {
				back = this.mrList.get(size-1);
			}
			if (matchNextRecord(prot, pBothStorage, back, mr2)) {
				this.mrList.add(mr2);
			} else {
				break;
			}
		}
	}
	
	private boolean matchNextRecord(int prot, Reassembler pBothStorage, MATCHED_RECORD pPrevMR, MATCHED_RECORD mr) {
		if (pPrevMR == null) {
			//first record				
			switch (prot) {
				case PROT_RECORD_TLS:
					return matchRecordCore(PROT_RECORD_TLS_FIRST, pBothStorage, mr);
				default:
					lOGGER.log(Level.FINE, "30002 - Unexpected protocol.");
					return false;
			}
		} else {
			Integer[] i = new Integer[1];
			i[0] = pPrevMR.getEndBDC();
			Integer[] j = new Integer[1];
			j[0] = pPrevMR.getEndOfs();

			getBytes(i, j, pBothStorage, this.bdcRaw, 1, null);
			
			mr.SetInput(i[0], j[0], pPrevMR.getDir(), pPrevMR.getUniDirOffset() + pPrevMR.getBytes());
			return matchRecordCore(prot, pBothStorage, mr);
		}			
	}
	
	private boolean matchRecordCore(int prot, Reassembler pBothStorage, MATCHED_RECORD mr) {
		int n = this.bdcRaw.size();
		Integer[] i = new Integer[1];
		i[0] = mr.getBeginBDC();
		Integer[] j = new Integer[1];
		j[0] = mr.getBeginOfs();
		PacketInfo.Direction d = mr.getDir();

		if (i[0]>=n) {
			//already at the end of the entire stream, note (i,j) = (n,0) is a valid position
			return false;
		}

		mr.setBytes(0);
		byte[] header = new byte[10];
		ByteBuffer pData = ByteBuffer.wrap(header);

		switch (prot) {
			case PROT_RECORD_TLS:
			case PROT_RECORD_TLS_FIRST:
				{						
					if (getBytes(i, j, pBothStorage, this.bdcRaw, 5, header) == false) {
						return false;
					}
					mr.setBytes(mr.getBytes() + 5);						
					byte recType = pData.get(0);
					if (recType == TLS_RECORD_CHANGE_CIPHER_SPEC ||
						recType == TLS_RECORD_ALERT ||
						recType == TLS_RECORD_HANDSHAKE ||
						recType == TLS_RECORD_APP_DATA
						) {
							//pData.get(); //Moves by 1
							if (checkTLSVersion(pData.array(), 1) == 0) {
								return false;
							}
							int encRecPayloadSize = pData.getShort(3); //Reverse not required as Java follows big endian byte order
							
							//additional check for the first record
							//we are confident it's a Client/Server Hello by checking the first 6 bytes
							if (prot == PROT_RECORD_TLS_FIRST) {
								if (d == PacketInfo.Direction.UPLINK) { //must be Client Hello
									if (recType != TLS_RECORD_HANDSHAKE) {
										return false;
									}
									if (encRecPayloadSize < 4 + 2 + 32 + 1) {
										return false;								
									}
									if (!getBytes(i, j, pBothStorage, this.bdcRaw, 6, header)) {
										return false;							
									}
									if (pData.get(0) != TLS_HANDSHAKE_CLIENT_HELLO) {
										return false;	//1 byte
									}
								} else { //must be Server hello
									if (recType != TLS_RECORD_HANDSHAKE) {
										return false;
									}
									if (encRecPayloadSize < 4 + 2 + 32 + 3 + 1) {
										return false;										
									}
									if (!getBytes(i, j, pBothStorage, this.bdcRaw, 6, header)) {
										return false;							
									}
									if (pData.get(0) != TLS_HANDSHAKE_SERVER_HELLO) {
										return false;	//1 byte
									}
								}
								int hsPayloadSize = read24bitInteger(pData.array(), 1);		//3 bytes
								if (hsPayloadSize + 4 > encRecPayloadSize) {
									return false;
								}
							//	pData.get(); //Moves by 1
								//pData.get(); //Moves by 1
							//	pData.get(); //Moves by 1
								if (checkTLSVersion(pData.array(), 4) == 0) {
									return false;	//2 bytes
								}
								if (!getBytes(i, j, pBothStorage, this.bdcRaw, encRecPayloadSize - 6, null)) {
									return false;
								}
							} else {
								if (getBytes(i, j, pBothStorage, this.bdcRaw, encRecPayloadSize, null) == false) {
									return false;
								}
							}
							mr.setBytes(mr.getBytes() + encRecPayloadSize);
					} else {
						return false;
					}
					break;
				}

			default:
				lOGGER.log(Level.FINE, "30003 - Unexpected protocol.");
		}

		//go back by one byte
		if (--j[0] == -1) {				
			while (true) {
				i[0]--;	
				if (i[0]<0 || this.bdcRaw.get(i[0]).getDir() == d) {
					break;
				}
			}
			j[0] = this.bdcRaw.get(i[0]).getBytes() - 1;
		}

		mr.setEndBDC(i[0]);
		mr.setEndOfs(j[0]);			
		return true;
	}
	
	private int read24bitInteger(byte pData[], int i) {
		byte[] tmp = new byte[4];
		tmp[3] = pData[i+2];
		tmp[2] = pData[i+1];
		tmp[1] = pData[i];
		tmp[0] = 0;
		int h= ByteBuffer.wrap(tmp).getInt();
		return h;
	}
	
	private int checkTLSVersion(byte[] pData, int i) {
		if ((pData[i] != 0x03) || ((pData[i + 1]) != 0x01)) {
			return 0;
		} else {
			return 1;
		}
	}
	
	private boolean getBytes(Integer[] bdcID, Integer[] offset, Reassembler pBothStorage, List<BIDIR_DATA_CHUNK> bdc, int nBytes, byte pBuffer[]) {
		int n = bdc.size();
		int i = bdcID[0];
		Integer j = offset[0];

		if (i>=n) {
			//already at the end of the entire stream, note (i,j) = (n,0) is a valid position
			return false;
		}

		PacketInfo.Direction dir = bdc.get(i).getDir();
		while (true) {
			int availBytes =  bdc.get(i).getBytes() - j;
			if (availBytes >= nBytes) {					
				if (pBuffer != null) {
					System.arraycopy(pBothStorage.storage.toByteArray(), bdc.get(i).getPrevBytes() + j, pBuffer, 0, nBytes); //http://stackoverflow.com/questions/3329163/is-there-an-equivalent-to-memcpy-in-java
				}
				j += nBytes;
				if (j == bdc.get(i).getBytes()) {
					//end of current chunk, move to the next chunk with the same direction
					j=0;
					while (true) {
						i++;	
						if ((i >= n) || (bdc.get(i).getDir() == dir)) {
							break;
						}
					}
				}
				bdcID[0] = i;
				offset[0] = j;
				return true;
			} else {
				//move to the next chunk with the same direction
				if (pBuffer != null) {
					System.arraycopy(pBothStorage.storage.toByteArray(), bdc.get(i).getPrevBytes() + j, pBuffer, 0, availBytes); //http://stackoverflow.com/questions/3329163/is-there-an-equivalent-to-memcpy-in-java
				}
				nBytes -= availBytes;	
				while (true) {
					i++;	
					if ((i >= n) || (bdc.get(i).getDir() == dir)) {
						break;
					}
				}
				if (i >= n) { 
					return false;
				}
				else {
					j = 0;
				}
			}
		}
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
					lOGGER.warning("29 - Unable to determine packet direction");
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
					lOGGER.warning("91 - No direction for packet");
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
		if(packets != null && packets.size() > 0){
			return packets.get(0).getTimeStamp();
		} 
		
		if(udpPackets != null && udpPackets.size() > 0) {
			return udpPackets.get(0).getTimeStamp();
		}
		
		return 0.0;
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
	byte[] getStorageUl() {
		return storageUl;
	}
	
	/**
	 * Return the extended uplink storage. 
	 * 
	 * @return An array of bytes containing the extended uplink storage.
	 */
	byte[] getStorageUlEx() {
		return storageUlext;
	}

	/**
	 * Return the extended downlink storage. 
	 * 
	 * @return An array of bytes containing the extended downlink storage.
	 */
	byte[] getStorageDlEx() {
		return storageDlext;
	}
	
	/**
	 * Return the downlink storage. 
	 * 
	 * @return An array of bytes containing the downlink storage.
	 */
	byte[] getStorageDl() {
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
				lOGGER.warning("97 - No direction for packet");
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
					lOGGER.warning("34 - Packet already typed");
				}
				pi.setTcpInfo(TcpInfo.TCP_KEEP_ALIVE);
			} else if (!pAckWinSize.containsKey(key)) {
				pAckWinSize.put(key, win);
				if (payloadLen == 0 && !p.isSYN() && !p.isFIN() && !p.isRST()) {
					if (pi.getTcpInfo() != null) {
						lOGGER.warning("98 - Packet already typed");
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
							lOGGER.warning("33 - Packet already typed");
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

							lOGGER.warning("32 - Packet already typed");
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
