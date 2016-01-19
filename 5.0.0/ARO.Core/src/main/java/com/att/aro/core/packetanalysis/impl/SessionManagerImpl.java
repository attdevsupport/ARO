package com.att.aro.core.packetanalysis.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IRequestResponseBuilder;
import com.att.aro.core.packetanalysis.ISessionManager;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.PacketRangeInStorage;
import com.att.aro.core.packetanalysis.pojo.Reassembler;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetanalysis.pojo.Termination;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.packetreader.pojo.TCPPacket;
import com.att.aro.core.packetreader.pojo.UDPPacket;

/**
 * group packet into session
 * @author EDS team
 * Refactored by Borey Sao
 * Date: February 5, 2015
 *
 */
public class SessionManagerImpl implements ISessionManager {
	
	@InjectLogger
	private static ILogger logger;
	
	@Autowired
	IRequestResponseBuilder requestResponseBuilder;
	
	public List<Session> assembleSession(List<PacketInfo> packets){
		Map<String, Session> allSessions = new LinkedHashMap<String, Session>();
		List<PacketInfo> dnsPackets = new ArrayList<PacketInfo>();
		List<PacketInfo> udpPackets = new ArrayList<PacketInfo>();
		Map<InetAddress, String> hostMap = new HashMap<InetAddress, String>();
		int packetIndex = 0;
		logger.debug("looping thru packets info list, total pakets: "+packets.size());
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
						if (dns != null && dns.isResponse()) {
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
				logger.warn("29 - Unable to determine packet direction");
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
			Session session = allSessions.get(key);
			if (session == null) {
				session = new Session(remoteIP, remotePort, localPort);
				
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
					if (dns != null && dns.isResponse() && dns.getIpAddresses().contains(remoteIP)) {
						session.setDnsResponsePacket(dnsPacket);
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
						PacketInfo pac = iter.next();
						UDPPacket udp = ((UDPPacket) pac.getPacket());
						dns = udp.getDns();
						if (dns != null && domainName.equals(dns.getDomainName())) {
							if (session.getDnsRequestPacket() == null && !dns.isResponse()) {
								session.setRemoteHostName(domainName);
								session.setDnsRequestPacket(pac);
							}
							
							// Remove from DNS packets so that it is not used again
							iter.remove();
							
							// Stop processing once response is reached
							if (pac == session.getDnsResponsePacket()) {
								break;
							}
						}
					}
				} else {
					session.setRemoteHostName(hostMap.get(remoteIP));
				}
				// stores the created session
				allSessions.put(key, session);
			} // END: Create new session
			
			session.getPackets().add(packet);
			session.getPktIndex().add(packetIndex++);
		} // END: Iterating through all packets
		logger.debug("end of first looping, now got session: "+allSessions.size());
		logger.debug("dns packet: "+dnsPackets.size());
		// Reassemble sessions
		List<Session> sessions = new ArrayList<Session>(allSessions.values());
		Reassembler upl = new Reassembler();
		Reassembler dol = new Reassembler();
		for (int sessionIndex = 0; sessionIndex < sessions.size(); ++sessionIndex) {
			
			// Iterator is not used because items may be added to list during.
			// iterations
			Session pSes = sessions.get(sessionIndex);
			// Reset variables
			boolean bTerminated = false;
			upl.clear();
			dol.clear();
			//logger.debug("Session index: "+sessionIndex+" has packets: "+pSes.getPackets().size());
			PacketInfo lastPacket = null;
			for (PacketInfo packetInfo: pSes.getPackets()) {
				
				TCPPacket pac = (TCPPacket) packetInfo.getPacket();
				
				pSes.setSsl(pac.isSsl());
				

				Reassembler reassembledSession;
				switch (packetInfo.getDir()) {
				case UPLINK:
					reassembledSession = upl;
					break;

				case DOWNLINK:
					reassembledSession = dol;
					break;

				default:
					logger.warn("91 - No direction for packet");
					continue;
				}

				// If this is the initial sequence number
				if (pac.isSYN()) {
					packetInfo.setTcpInfo(TcpInfo.TCP_ESTABLISH);
					if (reassembledSession.getBaseSeq() == null
							|| reassembledSession.getBaseSeq().equals(pac.getSequenceNumber())) {
						// Finds establish
						reassembledSession.setBaseSeq(pac.getSequenceNumber());
						if (pac.getPayloadLen() != 0) {
							logger.warn("92 - Payload in establish packet");
						}
					} else {

						// New TCP session
						List<PacketInfo> currentList = pSes.getPackets();
						int index = currentList.indexOf(packetInfo);
						if (!bTerminated) {
							logger.debug("28 - Session termination not found");
						}

						// Correct packet list in original session
						pSes.setPackets( new ArrayList<PacketInfo>(
								currentList.subList(0, index)));

						// Create new session for remaining packets
						Session newSession = new Session(
								pSes.getRemoteIP(), pSes.getRemotePort(),
								pSes.getLocalPort());
						newSession.getPackets().addAll(currentList.subList(index, currentList.size()));
						sessions.add(newSession);

						// Break out of packet loop
						break;
					}

				} else {
					//FIN: No more data from sender
					//RST: Reset the connection
					if (pac.isFIN() || pac.isRST()) {
						// Calculate session termination info
						if (!bTerminated && lastPacket != null) {
							double delay = packetInfo.getTimeStamp()
									- lastPacket.getTimeStamp();
							pSes.setSessionTermination(new Termination(packetInfo,
									delay));
						}

						// Mark session terminated
						bTerminated = true;
						if (pac.isFIN()) {
							packetInfo.setTcpInfo(TcpInfo.TCP_CLOSE);
						} else if (pac.isRST()) {
							packetInfo.setTcpInfo(TcpInfo.TCP_RESET);
						}

					}

					// I believe this handles case where we have joined in the
					// middle of a TCP session
					if (reassembledSession.getBaseSeq() == null) {
						switch (packetInfo.getDir()) {
						case UPLINK:
							upl.setBaseSeq(pac.getSequenceNumber());
							dol.setBaseSeq(pac.getAckNumber());
							break;
						case DOWNLINK:
							dol.setBaseSeq(pac.getSequenceNumber());
							upl.setBaseSeq(pac.getAckNumber());
							break;
						default:
							logger.error("Invalid packet direction");
						}
					}
				}

				// Get appName (there really should be only one per TCP session
				String appName = packetInfo.getAppName();
				if (appName != null) {
					pSes.getAppNames().add(appName);
					assert (pSes.getAppNames().size() <=1)  : pSes.getAppNames().size() + " app names per TCP session: " + pSes.getAppNames();
				}

				long seqn = pac.getSequenceNumber() - reassembledSession.getBaseSeq();
				if (seqn < 0) {
					seqn += 0xFFFFFFFF;
					seqn++; 
				}
				long seq = seqn;

				if (reassembledSession.getSeq() == -1) {
					reassembledSession.setSeq(seqn);
				}

				if (seqn == reassembledSession.getSeq()) {
					
					if (seq == reassembledSession.getSeq() || (seq < reassembledSession.getSeq() && seq + pac.getPayloadLen() > reassembledSession.getSeq())) {
						reassembledSession = doReassembleSession(pac, packetInfo, reassembledSession, pSes);
					} 

					while (true) {
						boolean bOODone = true;
						List<PacketInfo> fixed = new ArrayList<PacketInfo>(
								reassembledSession.getOoid().size());
						for (PacketInfo pin1 : reassembledSession.getOoid()) {
							TCPPacket tPacket1 = (TCPPacket) pin1.getPacket();

							seqn = tPacket1.getSequenceNumber() - reassembledSession.getBaseSeq();
							if (seqn < 0) {
								seqn += 0xFFFFFFFF;
								seqn++; 
							}
							
							long seq2 = seqn; 

							if (seqn == reassembledSession.getSeq()) {
								
								if (seq2 == reassembledSession.getSeq() || (seq2 < reassembledSession.getSeq() && seq2 + pac.getPayloadLen() > reassembledSession.getSeq())) {
									reassembledSession = doReassembleSession(tPacket1, pin1, reassembledSession, pSes);
								}
								
								fixed.add(pin1);
								bOODone = false;
							} else if (tPacket1.getPayloadLen() == 0
									&& seqn == reassembledSession.getSeq() - 1 && tPacket1.isACK()
									&& !tPacket1.isSYN() && !tPacket1.isFIN()
									&& !tPacket1.isRST()) {
								logger.warn("31 - ???");
							}
						}
						reassembledSession.getOoid().removeAll(fixed);
						if (bOODone) {
							break;
						}
					}//end while true

				} else { // out of order packet, i.e., seq != *XLseq
					if (pac.getPayloadLen() == 0 && seqn == reassembledSession.getSeq() - 1
							&& pac.isACK() && !pac.isSYN() && !pac.isFIN()
							&& !pac.isRST()) {
						
						packetInfo.setTcpInfo(TcpInfo.TCP_KEEP_ALIVE);
					} else {
						reassembledSession.getOoid().add(packetInfo);
					}
				}

				lastPacket = packetInfo;
			} // packet loop
			
			pSes.setStorageDl(dol.getStorage().toByteArray());
			pSes.setPacketOffsetsDl(dol.getPacketOffsets());
			pSes.setPktRangesDl(dol.getPktRanges());
			pSes.setStorageUl(upl.getStorage().toByteArray());
			pSes.setPacketOffsetsUl(upl.getPacketOffsets());
			pSes.setPktRangesUl(upl.getPktRanges());
			
			for (PacketInfo pinfo : dol.getOoid()) {
				if (pinfo.getPacket().getPayloadLen() > 0) {
					pinfo.setTcpInfo(TcpInfo.TCP_DATA_DUP);
				}
			}

			for (PacketInfo pinfo : upl.getOoid()) {
				if (pinfo.getPacket().getPayloadLen() > 0) {
					pinfo.setTcpInfo(TcpInfo.TCP_DATA_DUP);
				}
			}
		} // END: Reassemble sessions
		logger.debug("creating HttpReqResInfo for sessions: "+sessions.size());
		for (Session sess : sessions) {
			for (PacketInfo sPacket : sess.getPackets()) {
				sess.setBytesTransferred(sess.getBytesTransferred() + sPacket.getLen());
			}
			this.analyzeACK(sess);
			this.analyzeZeroWindow(sess);
			this.analyzeRecoverPkts(sess);

			// Parse HTTP request response info
			try {
				sess.setRequestResponseInfo(requestResponseBuilder.createRequestResponseInfo(sess));
			} catch (IOException exe) {
				logger.error("Error create RequestResponseInfo", exe);
			}
			for (HttpRequestResponseInfo rrinfo : sess.getRequestResponseInfo()) {
				if (rrinfo.getDirection() == HttpDirection.REQUEST) {

					// Assume first host found is same for entire session
					if (sess.getDomainName() == null) {
						String host = rrinfo.getHostName();
						if (host != null) {
							URI referrer = rrinfo.getReferrer();
							sess.setRemoteHostName(host);
							sess.setDomainName(referrer != null ? referrer.getHost() : host);
						}
					}
				} else if (rrinfo.getDirection() == HttpDirection.RESPONSE && rrinfo.getContentLength() > 0) {
					sess.setFileDownloadCount(sess.getFileDownloadCount() + 1);
					
				}
			}
			if (sess.getDomainName() == null) {
				sess.setDomainName( sess.getRemoteHostName() != null ? sess.getRemoteHostName() : sess.getRemoteIP().getHostAddress());
			}
		}		
		upl.clear();
		dol.clear();
		Collections.sort(sessions);
		/*Get UDP sessions.*/
		if(!udpPackets.isEmpty()){
			List<Session> udpSessions;
			try {
				udpSessions = getUDPSessions(udpPackets,sessions);
				sessions.addAll(udpSessions);
			} catch (IOException e) {
				logger.error("Error", e);
			}
			
		}

		return sessions;
	}
	public Reassembler doReassembleSession(TCPPacket pac, PacketInfo packetInfo, Reassembler reassembler, Session session){
		Reassembler reassembledSession = reassembler;
		if (pac.getPayloadLen() > 0) {
			Session pSes = session;
			packetInfo.setTcpInfo(TcpInfo.TCP_DATA);
			byte[] data = pac.getData();
			int effectivePayloadLen = pac.getPayloadLen();
			int dataOffset = pac.getDataOffset();
			if (data.length >= dataOffset + effectivePayloadLen) {
				reassembledSession.getPacketOffsets().put(reassembledSession.getStorage().size(), packetInfo);
				reassembledSession.getStorage().write(data, dataOffset, effectivePayloadLen);
				int offset = reassembledSession.getStorage().size() - effectivePayloadLen; 
				if(reassembledSession.getPktRanges().size() == 0) {
					offset = 0;
				}
				reassembledSession.getPktRanges().add(new PacketRangeInStorage(offset, effectivePayloadLen, packetInfo.getPacketId()));
				pSes.getpStorageBothRAW().getStorage().write(data, dataOffset, effectivePayloadLen); 
				 
				reassembledSession.setSeq(reassembledSession.getSeq() + effectivePayloadLen);
			}
			if (pac.isSslHandshake()) {
				pSes.setLastSslHandshakePacket(packetInfo);
			}
		}
		if (pac.isSYN() || pac.isFIN()) {
			reassembledSession.setSeq(reassembledSession.getSeq() + 1);
		}
		return reassembledSession;
	}
	/**
	 * Analyze the packet to find the TCPInfo. Marked flags: TCP_ACK,
	 * TCP_ACK_DUP, TCP_WINDOW_UPDATE, TCP_KEEP_ALIVE_ACK
	 */
	private void analyzeACK(Session sess) {

		Map<Long, Integer> ulAckWinSize = new HashMap<Long, Integer>();
		Map<Long, Integer> dlAckWinSize = new HashMap<Long, Integer>();

		Set<Long> ulAliveAck = new HashSet<Long>();
		Set<Long> dlAliveAck = new HashSet<Long>();

		for (PacketInfo pinfo : sess.getPackets()) {
			TCPPacket pack = (TCPPacket) pinfo.getPacket();

			if (!pack.isACK()) {
				continue;
			}

			long ackNum = pack.getAckNumber();
			int win = pack.getWindow();

			Map<Long, Integer> pAckWinSize;
			Set<Long> pAliveAck;
			Set<Long> pAliveAck2;

			switch (pinfo.getDir()) {
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
				logger.warn("97 - No direction for packet");
				continue;
			}

			if (pinfo.getTcpInfo() == TcpInfo.TCP_KEEP_ALIVE) {
				pAliveAck.add(pack.getSequenceNumber());
				continue;
			}

			int tcpFlag;
			if (pack.isFIN()) {
				tcpFlag = 1;
			} else if (pack.isSYN()) {
				tcpFlag = 2;
			} else if (pack.isRST()) {
				tcpFlag = 4;
			} else {
				tcpFlag = 0;
			}
			long key = ((ackNum << 32) | tcpFlag);

			// TODO Verify change in asserts ie getTCP!=null is ok
			int payloadLen = pack.getPayloadLen();
			if (pAliveAck2.contains(ackNum - 1) && payloadLen == 0
					&& !pack.isSYN() && !pack.isFIN() && !pack.isRST()) {
				if (pinfo.getTcpInfo() != null) {
					logger.warn("34 - Packet already typed");
				}
				pinfo.setTcpInfo(TcpInfo.TCP_KEEP_ALIVE);
			} else if (!pAckWinSize.containsKey(key)) {
				pAckWinSize.put(key, win);
				if (payloadLen == 0 && !pack.isSYN() && !pack.isFIN() && !pack.isRST()) {
					if (pinfo.getTcpInfo() != null) {
						logger.warn("98 - Packet already typed");
					}
					pinfo.setTcpInfo(TcpInfo.TCP_ACK);
				}
			} else {
				int prevWin = pAckWinSize.get(key);
				if (win == prevWin) {
					if (payloadLen == 0 && !pack.isRST()
							&& pinfo.getTcpInfo() != TcpInfo.TCP_KEEP_ALIVE) {
						
						pinfo.setTcpInfo(TcpInfo.TCP_ACK_DUP);
					}
				} else {
					pAckWinSize.put(key, win);
					if (payloadLen == 0 && !pack.isRST()
							&& pinfo.getTcpInfo() != TcpInfo.TCP_KEEP_ALIVE) {
						pinfo.setTcpInfo(TcpInfo.TCP_WINDOW_UPDATE);
					}
				}
			}
		}
	}

	/**
	 * Analyze the packet to find the TCPInfo. Marked flags: TCP_ZERO_WINDOW
	 */
	private void analyzeZeroWindow(Session sess) {
		for (PacketInfo pInfo : sess.getPackets()) {
			TCPPacket tPacket = (TCPPacket) pInfo.getPacket();
			if (tPacket.getPayloadLen() == 0 && tPacket.getWindow() == 0 && !tPacket.isSYN()
					&& !tPacket.isFIN() && !tPacket.isRST()) {
				pInfo.setTcpInfo(TcpInfo.TCP_ZERO_WINDOW);
			}
		}
	}

	/**
	 * Analyze the packet to find the TCPInfo. Marked flags: TCP_DATA_RECOVER,
	 * TCP_ACK_RECOVER
	 */
	private void analyzeRecoverPkts(Session sess) {

		// "Recover data": its seq equals to the duplicated ACK
		// "Recover ack": its ack equals to the duplicated DATA + payload len

		Map<Long, TCPPacket> dupAckUl = new HashMap<Long, TCPPacket>();
		Map<Long, TCPPacket> dupAckDl = new HashMap<Long, TCPPacket>();
		Map<Long, TCPPacket> dupSeqUl = new HashMap<Long, TCPPacket>();
		Map<Long, TCPPacket> dupSeqDl = new HashMap<Long, TCPPacket>();

		for (PacketInfo pInfo : sess.getPackets()) {
			TCPPacket tPacket = (TCPPacket) pInfo.getPacket();

			TcpInfo pType = pInfo.getTcpInfo();
			PacketDirection dir = pInfo.getDir();
			if (pType == TcpInfo.TCP_DATA_DUP) {
				if (dir == PacketDirection.UPLINK) {
					dupSeqUl.put(tPacket.getSequenceNumber() + tPacket.getPayloadLen(), tPacket);
				} else {
					dupSeqDl.put(tPacket.getSequenceNumber() + tPacket.getPayloadLen(), tPacket);
				}
			}

			// Duplicated data means duplicated ack as well
			if (pType == TcpInfo.TCP_ACK_DUP || pType == TcpInfo.TCP_DATA_DUP) {
				if (dir == PacketDirection.UPLINK) {
					dupAckUl.put(tPacket.getAckNumber(), tPacket);
				} else {
					dupAckDl.put(tPacket.getAckNumber(), tPacket);
				}
			}

			if (pType == TcpInfo.TCP_DATA) {
				if (dir == PacketDirection.UPLINK
						&& dupAckDl.containsKey(tPacket.getSequenceNumber())) {
					pInfo.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
				if (dir == PacketDirection.DOWNLINK
						&& dupAckUl.containsKey(tPacket.getSequenceNumber())) {
					pInfo.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
			}

			if (pType == TcpInfo.TCP_ACK) {
				if (dir == PacketDirection.UPLINK
						&& dupSeqDl.containsKey(tPacket.getAckNumber())) {
					pInfo.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
				if (dir == PacketDirection.DOWNLINK
						&& dupSeqUl.containsKey(tPacket.getAckNumber())) {
					pInfo.setTcpInfo(TcpInfo.TCP_DATA_RECOVER);
				}
			}

			// A special case:
			// DL: TCP_ACK_DUP with ack = 1
			// DL: TCP_ACK_DUP with ack = 1
			// UL: TCP_ACK with seq = 1
			// UL: TCP_DATA with seq = 1 <==== This is NOT a DATA_RECOVER
			if (pType == TcpInfo.TCP_ACK || pType == TcpInfo.TCP_ACK_DUP
					|| pType == TcpInfo.TCP_ACK_RECOVER) {
				if (dir == PacketDirection.UPLINK) {
					dupAckDl.remove(tPacket.getSequenceNumber());
				}
				if (dir == PacketDirection.DOWNLINK) {
					dupAckUl.remove(tPacket.getSequenceNumber());
				}
			}

			// DL: TCP_DATA_DUP with seq = 1, len = 2
			// DL: TCP_DATA_DUP with seq = 1, len = 2
			// UL: TCP_DATA with ack = 3
			// UL: TCP_ACK with ack = 3 <==== This is NOT an ACK_RECOVER

			// Duplicated data means duplicated ack as well
			// But vise versa is not true
			if (pType == TcpInfo.TCP_DATA || pType == TcpInfo.TCP_DATA_RECOVER) {
				if (dir == PacketDirection.UPLINK) {
					dupAckUl.remove(tPacket.getAckNumber());
				}
				if (dir == PacketDirection.DOWNLINK) {
					dupAckDl.remove(tPacket.getAckNumber());
				}
			}
		}
	}
	/**
	 * Get the UDP sessions from different UDP packets.
	 * @return Collection of TCPSession objects containing only UDP packets
	 * */
		
		private List<Session> getUDPSessions(
				List<PacketInfo> udpPackets,List<Session> sessions)throws IOException{
			Map<String, Session> allUDPSessions = new LinkedHashMap<String, Session>();	
			ListIterator<PacketInfo> iter = null;// = udpPackets.listIterator();//(udpPackets.size());
			DomainNameSystem dns = null;
			Reassembler rAssembler1 = new Reassembler();
			Reassembler rAssembler2 = new Reassembler();

			/*Remove all the dns packets part of TCP connections*/
			for (Session sess : sessions) {
				iter = udpPackets.listIterator();
				while (iter.hasNext()) {
					PacketInfo pInfo = iter.next();
					UDPPacket udp = ((UDPPacket) pInfo.getPacket());
					if(udp.isDNSPacket()) {
						dns = udp.getDns();
						if(dns != null){
							if(!dns.isResponse()) {
								if(sess.getDnsRequestPacket() != null) {
									String domainName = ((UDPPacket)sess.getDnsRequestPacket().getPacket()).getDns().getDomainName();
									if(domainName.equals(dns.getDomainName())) {
											iter.remove();
									}									
								}
							} else {
								if(sess.getDnsResponsePacket() != null) {
									String domainName = ((UDPPacket)sess.getDnsResponsePacket().getPacket()).getDns().getDomainName();
									if(domainName.equals(dns.getDomainName())
										&& (dns.getIpAddresses().contains(sess.getRemoteIP()))) {
										iter.remove();
									}
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
						logger.warn("29 - Unable to determine packet direction");
						continue;
				}
				String key = localPort + " " + remotePort + " " + remoteIP.getHostAddress();
				Session session = allUDPSessions.get(key);
				if (session == null) {
					session = new Session(remoteIP, remotePort, localPort);
					if (udp.isDNSPacket()){
						dns = udp.getDns();
						if(dns != null){
							//session.domainName = dns.getDomainName();
							session.setRemoteHostName(dns.getDomainName());
							
						}
					}
					if (session.getRemoteHostName() == null){
						session.setRemoteHostName(session.getRemoteIP().getHostAddress());
					}
					session.setUdpOnly(true);
					/* stores the created session*/
					allUDPSessions.put(key, session);
				} // END: Create new session
				session.getUDPPackets().add(packet);	
			}
			List<Session> udpSessions = new ArrayList<Session>(allUDPSessions.values());
			for (int sessionIndex = 0; sessionIndex < udpSessions.size(); ++sessionIndex) {
				
				Session session = udpSessions.get(sessionIndex);
				rAssembler1.clear();
				rAssembler2.clear();
				for (PacketInfo packetInfo: session.getUDPPackets()) {
					UDPPacket packet = (UDPPacket) packetInfo.getPacket();
		
					Reassembler reassembledSession;
					switch (packetInfo.getDir()) {
					case UPLINK:
						reassembledSession = rAssembler1;
						break;

					case DOWNLINK:
						reassembledSession = rAssembler2;
						break;

					default:
						logger.warn("91 - No direction for packet");
						continue;
					}
					if (packet.getPayloadLen() > 0) {
						
						byte[] data = packet.getData();
						int packetLen = packet.getPayloadLen();
						int dataOffset = packet.getDataOffset();
						if (data.length >= dataOffset + packetLen) {
							reassembledSession.getPacketOffsets().put(reassembledSession.getStorage().size(), packetInfo);
							reassembledSession.getStorage().write(data, dataOffset, packetLen);
						}
					}
					
					 //Added to find UDP packet bytes transfered
					session.setBytesTransferred(session.getBytesTransferred() + packet.getPayloadLen());
					
						
				}
				session.setStorageDl( rAssembler2.getStorage().toByteArray());
				session.setPacketOffsetsDl(rAssembler2.getPacketOffsets());
				session.setStorageUl(rAssembler1.getStorage().toByteArray());
				session.setPacketOffsetsUl(rAssembler1.getPacketOffsets());

			}
			for (Session sess : udpSessions) {
				sess.setRequestResponseInfo(this.requestResponseBuilder.createRequestResponseInfo(sess));
				
				for (HttpRequestResponseInfo rrHttp : sess.getRequestResponseInfo()) {
					if ((rrHttp.getDirection() == HttpDirection.REQUEST)
							&& (sess.getDomainName() == null && rrHttp.getHostName() != null)) {

						// Assume first host found is same for entire session
					//	if (sess.getDomainName() == null && rrHttp.getHostName() != null) {
							String host = rrHttp.getHostName();
							URI referrer = rrHttp.getReferrer();
							sess.setRemoteHostName(host);
							sess.setDomainName( referrer != null ? referrer.getHost() : host);
							
					//	}
					} 
				}			
				if (sess.getDomainName() == null) {
					sess.setDomainName(sess.getRemoteHostName() != null ? sess.getRemoteHostName() : sess.getRemoteIP().getHostAddress());
				}
				
			}
			rAssembler1.clear();
			rAssembler2.clear();
			return new ArrayList<Session>(allUDPSessions.values());
		}
}//end class
