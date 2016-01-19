/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.packetanalysis.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IByteArrayLineReader;
import com.att.aro.core.packetanalysis.IParseHeaderLine;
import com.att.aro.core.packetanalysis.IRequestResponseBuilder;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpPattern;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RequestResponseTimeline;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.packetreader.pojo.TCPPacket;

/**
 * Helper class to build HttpRequestResponseInfo from packet and group it in a session
 * @author EDS Team
 * Refactored by Borey Sao
 * Date: February 5, 2015
 */
public class RequestResponseBuilderImpl implements IRequestResponseBuilder {
	@InjectLogger
	private static ILogger logger;
	
	@Autowired
	private IParseHeaderLine parseHeaderLine;

	private Session session;

	private IByteArrayLineReader storageReader;
	
	@Autowired
	public void setByteArrayLineReader(IByteArrayLineReader reader){
		this.storageReader = reader;
	}
	
	private ArrayList<HttpRequestResponseInfo> result = null;

	Map<String, Integer> wellKnownParts = new HashMap<String, Integer>(5);

	public RequestResponseBuilderImpl(){
		wellKnownParts.put("HTTP", 80);
		wellKnownParts.put("HTTPS", 443);
		wellKnownParts.put("RTSP", 554);
	}

	public List<HttpRequestResponseInfo> createRequestResponseInfo(Session session) throws IOException {
	
		result = new ArrayList<HttpRequestResponseInfo>();
		this.session = session;
		extractHttpRequestResponseInfo(PacketDirection.UPLINK);
		extractHttpRequestResponseInfo(PacketDirection.DOWNLINK);
		Collections.sort(result);
		result.trimToSize();
		if(!session.isUDP() && !result.isEmpty()){/* By pass for UDP packets*/
			
			// Get DNS info for waterfall
			Double dns = null;
			if (session.getDnsRequestPacket() != null && session.getDnsResponsePacket() != null) {
				dns = session.getDnsRequestPacket().getTimeStamp();
			}

			// Find syn and ack packets for session
			Double synTime = null;
			for (PacketInfo pinfo : session.getPackets()) {
				if (pinfo.getPacket() instanceof TCPPacket) {
					TCPPacket tcp = (TCPPacket) pinfo.getPacket();
					if (tcp.isSYN()) {
						synTime = pinfo.getTimeStamp();
						break;
					}
				}
			}
			
			Double sslNegTime = null;
			PacketInfo handshake = session.getLastSslHandshakePacket();
			if (handshake != null) {
				sslNegTime = handshake.getTimeStamp();
			}

			// Associate requests/responses
			List<HttpRequestResponseInfo> reqs = new ArrayList<HttpRequestResponseInfo>(result.size());
			for (HttpRequestResponseInfo rrinfo : result) {
				if (rrinfo.getDirection() == HttpDirection.REQUEST) {
					reqs.add(rrinfo);
				} else if (rrinfo.getDirection() == HttpDirection.RESPONSE && !reqs.isEmpty()) {

					rrinfo.setAssocReqResp(reqs.remove(0));
					rrinfo.getAssocReqResp().setAssocReqResp(rrinfo);

				}
			}

			// Build waterfall for each request/response pair
			for (HttpRequestResponseInfo rrinfo : result) {
				if (rrinfo.getDirection() != HttpDirection.REQUEST 
				 || rrinfo.getAssocReqResp() == null) {
					// Only process non-HTTPS request/response pairs
					continue;
				}
				
				double startTime = -1;
				//check firstDataPacket and lastDataPacket
				if (rrinfo.getFirstDataPacket() != null 
				 && rrinfo.getLastDataPacket() != null) {
					double firstReqPacket = rrinfo.getFirstDataPacket().getTimeStamp();
					double lastReqPacket = rrinfo.getLastDataPacket().getTimeStamp();
					
					HttpRequestResponseInfo resp = rrinfo.getAssocReqResp();
					
					// check getAssocReqResp firstDataPacket and lastDataPacket packet
					if(resp != null && resp.getFirstDataPacket() !=null && resp.getLastDataPacket() != null) {
						double firstRespPacket = resp.getFirstDataPacket().getTimeStamp();
						double lastRespPacket = resp.getLastDataPacket().getTimeStamp();
	
						// Add DNS and initial connect to fist req/resp pair only
						Double dnsDuration = null;
						if (dns != null) {
							startTime = dns.doubleValue();
							if (synTime != null) {
								dnsDuration = synTime.doubleValue() - dns.doubleValue();
							} else {
								dnsDuration = firstReqPacket - dns.doubleValue();
							}
							
							// Prevent from being added again
							dns = null;
						}
						
						Double initConnDuration = null;
						if (synTime != null) {
							initConnDuration = firstReqPacket - synTime;
							if (startTime < 0.0) {
								startTime = synTime.doubleValue();
							}
							
							// Prevent from being added again
							synTime = null;
						}
						
						// Calculate request time
						if (startTime < 0.0) {
							startTime = firstReqPacket;
						}
						
						// Store waterfall in request/response
						if (sslNegTime != null) {
							rrinfo.setWaterfallInfos(new RequestResponseTimeline(startTime, dnsDuration, initConnDuration, sslNegTime - firstReqPacket, 0, 0, lastRespPacket - sslNegTime));
						} else {
							if (firstRespPacket >= lastReqPacket) {
								rrinfo.setWaterfallInfos(new RequestResponseTimeline(startTime, dnsDuration, initConnDuration, null, lastReqPacket - firstReqPacket, firstRespPacket - lastReqPacket, lastRespPacket - firstRespPacket));
							} else {
								rrinfo.setWaterfallInfos( new RequestResponseTimeline(startTime, dnsDuration, initConnDuration, null, 0, 0, lastRespPacket - firstReqPacket));
							}
						}
					}// end  (resp != null && resp.getFirstDataPacket() !=null && resp.getLastDataPacket() != null)
				}// check firstDataPacket and lastDataPacket rr check end
			}
		} /* by pass for UDP sessions.*/
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns the HTTP request/response result list.
	 * 
	 * @return The result The list object containing the requests and
	 *         responses.
	 */
	public List<HttpRequestResponseInfo> getResult() {
		return Collections.unmodifiableList(result);
	}

	/**
	 * Returns a list of HTTP requests and responses from the specified TCP
	 * session.
	 * 
	 * 
	 * @param direction
	 *            The direction i.e. uplink/downlink.
	 * @throws IOException
	 * 
	 * @return A List of HttpRequestResponseInfo objects that contain the
	 *         request/response data from a TCP session.
	 */
	public void extractHttpRequestResponseInfo(PacketDirection direction) throws IOException {

		SortedMap<Integer, PacketInfo> packetOffsets;
		switch (direction) {
		case DOWNLINK:
			storageReader.init(session.getStorageDl());
			packetOffsets = session.getPacketOffsetsDl();
			break;
		case UPLINK:
			storageReader.init(session.getStorageUl());
			packetOffsets = session.getPacketOffsetsUl();
			break;
		default:
			throw new IllegalArgumentException("Direction argument invalid");
		}

		HttpRequestResponseInfo rrInfo = findNextRequestResponse(direction,	packetOffsets);
		String line;
		while ((line = storageReader.readLine()) != null && rrInfo != null) {
			if (line.length() == 0) {
				if (rrInfo.getContentLength() > 0) {
					rrInfo.setContentOffsetLength(new TreeMap<Integer, Integer>());
					rrInfo.getContentOffsetLength().put(storageReader.getIndex(), rrInfo.getContentLength());

					// Skip content
					storageReader.skipContent(rrInfo.getContentLength());
				} else if (rrInfo.isChunked()) {
					rrInfo.setContentOffsetLength(new TreeMap<Integer, Integer>());
					while (true) {

						// Read each chunk
						line = storageReader.readLine();
						if (line != null) {
							String[] str = line.split(";");
							int size = Integer.parseInt(str[0].trim(), 16);
							if (size > 0) {

								// Save content offsets
								rrInfo.getContentOffsetLength().put(storageReader.getIndex(), size);
								rrInfo.setContentLength(rrInfo.getContentLength() + size);
								storageReader.skipForward(size);
								

								// CRLF at end of each chunk
								line = storageReader.readLine();
								if (line != null && line.length() > 0) {
									logger.warn("Unexpected end of chunk: " + line);
								}
							} else {
								rrInfo.setChunkModeFinished(true);

								// End of chunks
								line = storageReader.readLine();
								if (line != null && line.length() > 0) {
									logger.warn("Unexpected end of chunked data: " + line);
								}
								break;
							}
						} else {
							break;
						}
					}
				}

				mapPackets(packetOffsets, rrInfo.getRrStart(), storageReader.getIndex() - 1, direction, rrInfo);
				rrInfo.setRawSize(storageReader.getIndex() - rrInfo.getRrStart());
				
				// Build an absolute URI if possible
				if (rrInfo.getObjUri() != null && !rrInfo.getObjUri().isAbsolute()) {
					try {
						int port = Integer.valueOf(rrInfo.getPort()).equals(wellKnownParts.get(rrInfo.getScheme())) ? -1 : rrInfo.getPort();
						rrInfo.setObjUri( new URI(rrInfo.getScheme().toLowerCase(), null, rrInfo.getHostName(), port, rrInfo.getObjUri().getPath(), rrInfo.getObjUri().getQuery(), rrInfo.getObjUri().getFragment()));
					} catch (URISyntaxException e) {
						// Just log fine message
						logger.info("Unexpected exception creating URI for request: " + e.getMessage()+
								". Scheme=" + rrInfo.getScheme().toLowerCase() +",Host name="+ rrInfo.getHostName()
								+",Path=" + rrInfo.getObjUri().getPath() + ",Fragment="+ rrInfo.getObjUri().getFragment());
						
					}
				}
				
				result.add(rrInfo);
				if (rrInfo.getDirection() == null) {
					logger.warn("Request/response object has unknown direction");
				}
				rrInfo = findNextRequestResponse(direction, packetOffsets);
			} else {
				parseHeaderLine.parseHeaderLine(line, rrInfo);
			}
		} // end: while
	}

	/**
	 * Process of map the packets with its direction.
	 * 
	 * @param packetOffsets
	 *            The collection of packets with ids.
	 * @param start
	 *            The begin id.
	 * @param end
	 *            The end id.
	 * @param direction
	 *            The packet direction i.e. uplink/downlink.
	 * @param rrInfo
	 *            The request/response info associated with the packet.
	 */
	private void mapPackets(SortedMap<Integer, PacketInfo> packetOffsets,
			int start, int end, PacketDirection direction,
			HttpRequestResponseInfo rrInfo) {

		// Determine the packets that make up the request/response
		rrInfo.setFirstDataPacket(determineDataPacketAtIndex(packetOffsets, start, direction));

		rrInfo.setLastDataPacket(determineDataPacketAtIndex(packetOffsets, end, direction));

	}

	/**
	 * Determine the packets that make up the request/response
	 * 
	 * @param packetOffsets
	 * @param index
	 * @return Success case PacketInfo which creates a request/response;
	 *         else null.
	 */
	private PacketInfo determineDataPacketAtIndex( SortedMap<Integer
													, PacketInfo> packetOffsets
													, int indexAt
													, PacketDirection direction) {
		int index = indexAt;
		// Determine the packets that make up the request/response
		
		for (SortedMap.Entry<Integer, PacketInfo> entry : packetOffsets.entrySet()) {
			int packetOffset = entry.getKey().intValue();
			
			if (index >= packetOffset && index < packetOffset + entry.getValue().getPayloadLen()) {
				return entry.getValue();
			}
		}

		if (direction == PacketDirection.UPLINK && this.session.getStorageUlEx() != null) {
			index = this.session.getStorageUlEx().length - 1;
		} else if (direction == PacketDirection.DOWNLINK && this.session.getStorageDlEx() != null) {
			index = this.session.getStorageDlEx().length - 1;
		}

		for (SortedMap.Entry<Integer, PacketInfo> entry : packetOffsets.entrySet()) {
			int packetOffset = entry.getKey().intValue();
			if (index >= packetOffset && index < packetOffset + entry.getValue().getPayloadLen()) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * process to detect the next HttpRequestResponseInfo.
	 * 
	 * @param direction
	 *            indicates the HTTP transaction direction with server.
	 *            REQUEST/RESPONSE.
	 * @param packetOffsets
	 *            range of packet info.
	 * @return next HttpRequestResponseInfo object.
	 * @throws IOException
	 */
	private HttpRequestResponseInfo findNextRequestResponse(
			PacketDirection direction
			, SortedMap<Integer, PacketInfo> packetOffsets) throws IOException {
		
		int index = storageReader.getIndex();
		String line = storageReader.readLine();
		while (line != null && line.length() == 0) {
			index = storageReader.getIndex();
			line = storageReader.readLine();
		}

		HttpRequestResponseInfo rrInfo = null;
		if (line != null && direction != null) {
			Matcher matcher;
			rrInfo = new HttpRequestResponseInfo(session.getRemoteHostName(), direction);
			rrInfo.setRrStart(index);

			// Check for request type
			matcher = HttpPattern.strReRequestType.matcher(line);
			if (matcher.lookingAt()) {
				rrInfo.setStatusLine( line);
				rrInfo.setRequestType(matcher.group(1));
				rrInfo.setDirection(HttpDirection.REQUEST);
				rrInfo.setObjName(matcher.group(2));
				try {
					rrInfo.setObjUri(new URI(rrInfo.getObjName()));
					if (rrInfo.getObjUri().getHost() != null) {
						rrInfo.setHostName(rrInfo.getObjUri().getHost());
					}
				} catch (URISyntaxException e) {
					// Ignore since value does not have to be a URI
					logger.error(e.getMessage());
				}
				rrInfo.setVersion(matcher.group(3));
				rrInfo.setScheme(rrInfo.getVersion().split("/")[0]);
				
				if(direction == PacketDirection.UPLINK){
					rrInfo.setPort(session.getRemotePort());
				}else if(direction == PacketDirection.DOWNLINK){
					rrInfo.setPort(session.getLocalPort());
				}

			}

			// Get response
			matcher = HttpPattern.strReResponseResults.matcher(line);
			if (matcher.lookingAt()) {
				rrInfo.setStatusLine(line);
				rrInfo.setDirection(HttpDirection.RESPONSE);
				rrInfo.setVersion(matcher.group(1));
				rrInfo.setScheme(rrInfo.getVersion().split("/")[0]);
				rrInfo.setStatusCode(Integer.parseInt(matcher.group(2)));
				rrInfo.setResponseResult(matcher.group(3));
			}

			if (rrInfo.getDirection() == null) {

				// Check for HTTPS
				if (session.isSsl()) {
					rrInfo.setSsl(true);
				}
				//storageArray.readLine() will increment global counter to the end
				while ((line = storageReader.readLine()) != null) {
					if(line.length() <= 0){
						break;
					}
				}
				rrInfo.setRawSize(storageReader.getIndex() - index);

				if(direction == PacketDirection.UPLINK){
					rrInfo.setDirection(HttpDirection.REQUEST);
				}else if(direction == PacketDirection.DOWNLINK){
					rrInfo.setDirection(HttpDirection.RESPONSE);

					// Actual content length is unknown so headers are included
					rrInfo.setContentOffsetLength(new TreeMap<Integer, Integer>());
					rrInfo.getContentOffsetLength().put(index, rrInfo.getRawSize());
				}
				
				mapPackets(packetOffsets, index, storageReader.getIndex() - 1, direction, rrInfo);
				result.add(rrInfo);
			}
		}

		return rrInfo;
	}
}
