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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.jsoup.Jsoup;

import com.att.aro.bp.fileorder.FileOrderAnalysis;
import com.att.aro.bp.asynccheck.AsyncCheckAnalysis;
import com.att.aro.pcap.TCPPacket;
import com.att.aro.util.Util;

/**
 * Encapsulates information about an HTTP request or response. This class was
 * converted from struct HTTP_REQUEST_RESPONSE
 */
public class HttpRequestResponseInfo implements
		Comparable<HttpRequestResponseInfo> {

	/**
	 * Returns HTTP version 1.0.
	 */
	public static final String HTTP10 = "HTTP/1.0";

	/**
	 * Returns HTTP version 1.1.
	 */
	public static final String HTTP11 = "HTTP/1.1";

	/**
	 * Returns the GET HTTP type.
	 */
	public static final String HTTP_GET = "GET";

	/**
	 * Returns the PUT HTTP type.pe.
	 */
	public static final String HTTP_PUT = "PUT";

	/**
	 * Returns the POST HTTP type.
	 */
	public static final String HTTP_POST = "POST";
	public static final String HTTP_SCHEME = "HTTP";
	private static final String CONTENT_ENCODING_GZIP = "gzip";
	private static final String CONTENT_ENCODING_COMPRESS = "compress";
	private static final String CONTENT_ENCODING_DEFLATE = "deflate";
	private static final String CONTENT_ENCODING_NONE = Util.RB.getString("rrview.http.compression.no");
	private static final String CONTENT_ENCODING_NA = "";
	private static final Date BEGINNING_OF_TIME = new Date(0);
	private static final Logger logger = Logger.getLogger(HttpRequestResponseInfo.class.getName());
	private static final Map<String, Integer> wellKnownPorts = new HashMap<String, Integer>(5);
	private static final Pattern TEXT_CONTENT_TYPE_TEXT = Pattern.compile("^text/.*");
	private static final Pattern TEXT_CONTENT_TYPE_XML = Pattern.compile("^application/.*xml");
	private static final CharSequence IMAGE = Util.RB.getString("fileChooser.contentType.image");
	
	static {
		wellKnownPorts.put(HTTP_SCHEME, 80);
		wellKnownPorts.put("HTTPS", 443);
		wellKnownPorts.put("RTSP", 554);
	}

	private PacketInfo.Direction packetDirection;
	private TCPSession session;
	private Direction direction; // REQUEST or RESPONSE
	private String scheme;
	private int port;
	private String version;
	private String statusLine;
	private String requestType; // e.g., HTTP_POST, HTTP_GET
	private int statusCode; // e.g., 200
	private String hostName; // e.g., a57.foxnews.com
	private String contentType; // image/jpeg
	private String charset;
	private String objName; // e.g., /static/managed/img/.../JoePerry640.jpg
	private String objNameWithoutParams;
	private String fileName;
	private URI objUri;
	private String responseResult;
	private boolean chunked;
	private boolean chunkModeFinished;
	private boolean rangeResponse;
	private boolean ifModifiedSince;
	private boolean ifNoneMatch;
	private int rangeFirst;
	private int rangeLast;
	private long rangeFull;
	private int contentLength;
	private String contentEncoding;
	private int rrStart;
	private int rawSize; // Includes headers
	private boolean ssl;
	private TextFileCompression textFileCompression;
	
	// Map of the content offset/
	private SortedMap<Integer,Integer> contentOffsetLength;

	// packets
	private PacketInfo firstDataPacket;
	private PacketInfo lastDataPacket;

	// Cache info
	private Date date;
	private boolean hasCacheHeaders;
	private boolean pragmaNoCache;
	private boolean noCache;
	private boolean noStore;
	private boolean publicCache;
	private boolean privateCache;
	private boolean mustRevalidate;
	private boolean proxyRevalidate;
	private boolean onlyIfCached;
	private String etag;
	private Long age;
	private Date expires;
	private URI referrer;
	private Date lastModified;
	private Long maxAge;
	private Long sMaxAge;
	private Long minFresh;
	private Long maxStale;

	private HttpRequestResponseInfo assocReqResp;
	private RequestResponseTimeline waterfallInfos;
	private String allHeaders;

	/**
	 * The HttpRequestResponseInfo.Direction Enumeration specifies constant
	 * values that describe the direction of an HTTP request/response. The
	 * direction indicates whether an HttpRequestResponseInfo object contains a
	 * request (up link) or a response (downlink). This enumeration is part of
	 * the HttpRequestResponseInfo class.
	 */
	public enum Direction {
		/**
		 * A Request traveling in the up link direction.
		 */
		REQUEST,
		/**
		 * A Response traveling in the down link direction.
		 */
		RESPONSE;
	}
	
	public enum TextFileCompression {

		GZIP(CONTENT_ENCODING_GZIP),
		COMPRESS(CONTENT_ENCODING_COMPRESS),
		DEFLATE(CONTENT_ENCODING_DEFLATE),
		NONE(CONTENT_ENCODING_NONE),
		NOT_APPLICABLE(CONTENT_ENCODING_NA);

		private String type;

		TextFileCompression(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	/**
	 * This is a synchronized request/response builder class
	 */
	private static class RequestResponseBuilder {

		/**
		 * Date format pattern used to parse HTTP date headers in RFC 1123
		 * format.
		 */
		private static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

		/**
		 * Date format pattern used to parse HTTP date headers in RFC 1036
		 * format.
		 */
		private static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";

		/**
		 * Date format pattern used to parse HTTP date headers in ANSI C
		 * <code>asctime()</code> format.
		 */
		private static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
		private static final String PATTERN_ASCTIME2 = "EEE MMM d HH:mm:ss zzz yyyy";

		private static final String CHARSET = "charset";
		private static final String CHUNKED = "chunked";
		private static final String NOCACHE = "no-cache";
		private static final String NOSTORE = "no-store";
		private static final String PUBLIC = "public";
		private static final String PRIVATE = "private";
		private static final String MUSTREVALIDATE = "must-revalidate";
		private static final String PROXYREVALIDATE = "proxy-revalidate";
		private static final String ONLYIFCACHED = "only-if-cached";

		private static final Object HEADERS_SEPARATOR = " ";

		private static Pattern strReRequestType = Pattern
				.compile("(\\S*)\\s* \\s*(\\S*)\\s* \\s*(HTTP/1\\.[0|1]|RTSP/1\\.[0|1])");
		private static Pattern strReRequestHost = Pattern.compile("[H|h]ost:");
		private static Pattern strReResponseContentLength = Pattern
				.compile("[C|c]ontent-[L|l]ength:");
		private static Pattern strReTransferEncoding = Pattern
				.compile("[T|t]ransfer-[E|e]ncoding:");
		private static Pattern strReResponseContentType = Pattern
				.compile("[C|c]ontent-[T|t]ype:");
		private static Pattern strReResponseContentEncoding = Pattern
				.compile("[C|c]ontent-[E|e]ncoding:");
		private static Pattern strReResponseResults = Pattern
				.compile("(HTTP/1\\.[0|1]|RTSP/1\\.[0|1])\\s* \\s*(\\d++)\\s* \\s*(.*)");
		private static Pattern strReResponseEtag = Pattern
				.compile("ETag\\s*:\\s*(W/)?\"(.*)\"");
		private static Pattern strReResponseAge = Pattern
				.compile("Age\\s*:\\s*(\\d*)");
		private static Pattern strReResponseExpires = Pattern
				.compile("Expires\\s*:(.*)");
		private static Pattern strReResponseReferer = Pattern
				.compile("Referer\\s*:(.*)");
		private static Pattern strReResponseLastMod = Pattern
				.compile("Last-Modified\\s*:(.*)");
		private static Pattern strReResponseDate = Pattern
				.compile("Date\\s*:(.*)");
		private static Pattern strReResponsePragmaNoCache = Pattern
				.compile("Pragma\\s*:\\s*no-cache");
		private static Pattern strReResponseCacheControl = Pattern
				.compile("Cache-Control\\s*:(.*)");
		private static Pattern strReCacheMaxAge = Pattern
				.compile("max-age\\s*=\\s*(\\d++)");
		private static Pattern strReCacheSMaxAge = Pattern
				.compile("s-maxage\\s*=\\s*(\\d++)");
		private static Pattern strReCacheMinFresh = Pattern
				.compile("min-fresh\\s*=\\s*(\\d++)");
		private static Pattern strReCacheMaxStale = Pattern
				.compile("max-stale\\s*(?:=\\s*(\\d*))?");
		private static Pattern strReContentRange = Pattern
				.compile("Content-Range\\s*:\\s*bytes (\\d*)\\s*-\\s*(\\d*)\\s*/\\s*(\\d*)");
		private static Pattern strReIfModifiedSince = Pattern
				.compile("If-Modified-Since\\s*:");
		private static Pattern strReIfNoneMatch = Pattern
				.compile("If-None-Match\\s*:");

		private TCPSession session;
		private ArrayList<HttpRequestResponseInfo> result = new ArrayList<HttpRequestResponseInfo>();
		private int counter;
		private byte[] input;

		private DateFormat rfc1123 = new SimpleDateFormat(PATTERN_RFC1123);
		private DateFormat rfc1036 = new SimpleDateFormat(PATTERN_RFC1036);
		private DateFormat asctime = new SimpleDateFormat(PATTERN_ASCTIME);
		private DateFormat asctime2 = new SimpleDateFormat(PATTERN_ASCTIME2);
		private DateFormat[] dateFormats = { rfc1123, rfc1036, asctime,
				asctime2 };

		public RequestResponseBuilder(TCPSession session) throws IOException {
			this.session = session;
			extractHttpRequestResponseInfo(PacketInfo.Direction.UPLINK);
			extractHttpRequestResponseInfo(PacketInfo.Direction.DOWNLINK);
			Collections.sort(result);
			result.trimToSize();
			if(!session.isUDP()){/* By pass for UDP packets*/
				if (result.size() > 0) {
					
					// Get DNS info for waterfall
					Double dns = null;
					if (session.getDnsRequestPacket() != null && session.getDnsResponsePacket() != null) {
						dns = session.getDnsRequestPacket().getTimeStamp();
					}
	
					// Find syn and ack packets for session
					Double synTime = null;
					for (PacketInfo p : session.getPackets()) {
						if (p.getPacket() instanceof TCPPacket) {
							TCPPacket tcp = (TCPPacket) p.getPacket();
							if (tcp.isSYN()) {
								synTime = p.getTimeStamp();
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
					List<HttpRequestResponseInfo> reqs = new ArrayList<HttpRequestResponseInfo>(
							result.size());
					for (HttpRequestResponseInfo rr : result) {
						if (rr.direction == Direction.REQUEST) {
							reqs.add(rr);
						} else if (rr.direction == Direction.RESPONSE) {
							if (!reqs.isEmpty()) {
								rr.assocReqResp = reqs.remove(0);
								rr.assocReqResp.assocReqResp = rr;
							}
						}
					}
	
					// Build waterfall for each request/response pair
					for (HttpRequestResponseInfo rr : result) {
						if (rr.getDirection() != Direction.REQUEST || rr.getAssocReqResp() == null) {
							// Only process non-HTTPS request/response pairs
							continue;
						}
						
						double startTime = -1;
						double firstReqPacket = rr.firstDataPacket.getTimeStamp();
						double lastReqPacket = rr.lastDataPacket.getTimeStamp();
						
						HttpRequestResponseInfo resp = rr.getAssocReqResp();
						double firstRespPacket = resp.firstDataPacket.getTimeStamp();
						double lastRespPacket = resp.lastDataPacket.getTimeStamp();
	
						// Add DNS and initial connect to fist req/resp pair only
						Double dnsDuration = null;
						if (dns != null) {
							startTime = dns.doubleValue();
							if (synTime != null) {
								dnsDuration = synTime.doubleValue() - dns.doubleValue();
							} else {
								logger.warning("Found DNS connection with no initial session connection");
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
							rr.waterfallInfos = new RequestResponseTimeline(startTime, dnsDuration, initConnDuration, sslNegTime - firstReqPacket, 0, 0, lastRespPacket - sslNegTime);
						} else {
							if (firstRespPacket >= lastReqPacket) {
								rr.waterfallInfos = new RequestResponseTimeline(startTime, dnsDuration, initConnDuration, null, lastReqPacket - firstReqPacket, firstRespPacket - lastReqPacket, lastRespPacket - firstRespPacket);
							} else {
								rr.waterfallInfos = new RequestResponseTimeline(startTime, dnsDuration, initConnDuration, null, 0, 0, lastRespPacket - firstReqPacket);
							}
						}
					}
				}
			} /* by pass for UDP sessions.*/
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
		 * @param direction
		 *            The direction i.e. uplink/downlink.
		 * @throws IOException
		 * 
		 * @return A List of HttpRequestResponseInfo objects that contain the
		 *         request/response data from a TCP session.
		 */
		public synchronized void extractHttpRequestResponseInfo(
				PacketInfo.Direction direction) throws IOException {

			SortedMap<Integer, PacketInfo> packetOffsets;
			switch (direction) {
			case DOWNLINK:
				this.input = session.getStorageDl();
				packetOffsets = session.getPacketOffsetsDl();
				break;
			case UPLINK:
				this.input = session.getStorageUl();
				packetOffsets = session.getPacketOffsetsUl();
				break;
			default:
				throw new IllegalArgumentException("Direction argument invalid");
			}

			this.counter = 0;

			HttpRequestResponseInfo rrInfo = findNextRequestResponse(direction,	packetOffsets);
			String line;
			while ((line = readLine()) != null && rrInfo != null) {

				if (line.length() == 0) {
					if (rrInfo.contentLength > 0) {
						//rrInfo.contentOffsetLength = new TreeMap<Integer, Integer>();
						rrInfo.contentOffsetLength = new TreeMap<Integer,Integer>();
						rrInfo.contentOffsetLength.put(counter,
								rrInfo.contentLength);

						// Skip content
						counter = Math.min(input.length, counter
								+ rrInfo.contentLength);
						
						if (counter < 0) {
							counter = input.length;
						}
					} else if (rrInfo.chunked) {
						rrInfo.contentOffsetLength = new TreeMap<Integer,Integer>();
						while (true) {

							// Read each chunk
							line = readLine();
							if (line != null) {
								String[] s = line.split(";");
								int size = Integer.parseInt(s[0].trim(), 16);
								if (size > 0) {

									// Save content offsets
									rrInfo.contentOffsetLength.put(counter,
											size);
									rrInfo.contentLength += size;
									for (int i = 0; i < size; ++i) {
										readInput();
									}

									// CRLF at end of each chunk
									line = readLine();
									if (line != null && line.length() > 0) {
										logger.warning("Unexpected end of chunk: "
												+ line);
									}
								} else {
									rrInfo.chunkModeFinished = true;

									// End of chunks
									line = readLine();
									if (line != null && line.length() > 0) {
										logger.warning("Unexpected end of chunked data: "
												+ line);
									}
									break;
								}
							} else {
								break;
							}
						}
					}

					mapPackets(packetOffsets, rrInfo.rrStart, counter - 1,
							direction, rrInfo);
					rrInfo.rawSize = counter - rrInfo.rrStart;
					
					// Build an absolute URI if possible
					if (rrInfo.objUri != null && !rrInfo.objUri.isAbsolute()) {
						try {
							int port = Integer.valueOf(rrInfo.port).equals(wellKnownPorts.get(rrInfo.scheme)) ? -1 : rrInfo.port;
							rrInfo.objUri = new URI(rrInfo.scheme.toLowerCase(), null, rrInfo.hostName, port, rrInfo.objUri.getPath(), rrInfo.objUri.getQuery(), rrInfo.objUri.getFragment());
						} catch (URISyntaxException e) {
							// Just log fine message
							logger.log(Level.FINE, "Unexpected exception creating URI for request: " + e.getMessage()+
									". Scheme=" + rrInfo.scheme.toLowerCase() +",Host name="+ rrInfo.hostName
									+",Path=" + rrInfo.objUri.getPath() + ",Fragment="+ rrInfo.objUri.getFragment());
							
						}
					}
					
					result.add(rrInfo);
					if (rrInfo.getDirection() == null) {
						logger.warning("Request/response object has unknown direction");
					}
					rrInfo = findNextRequestResponse(direction, packetOffsets);
				} else {
					parseHeaderLine(line, rrInfo);
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
				int start, int end, PacketInfo.Direction direction,
				HttpRequestResponseInfo rrInfo) {

			// Determine the packets that make up the request/response
			rrInfo.firstDataPacket = determineDataPacketAtIndex(packetOffsets,
					start);

			rrInfo.lastDataPacket = determineDataPacketAtIndex(
					packetOffsets, counter - 1);
// Removed because this logic is not totally correct and is currently not needed
//			long startSeq = ((TCPPacket) rrInfo.firstDataPacket.getPacket())
//					.getSequenceNumber();
//			long endSeq = ((TCPPacket) lastDataPacket.getPacket())
//					.getSequenceNumber();
//			ArrayList<PacketInfo> rrPackets = new ArrayList<PacketInfo>();
//			for (PacketInfo p : session.getPackets()) {
//				TCPPacket tcp = (TCPPacket) p.getPacket();
//				if (p.getDir() == direction) {
//					if (tcp.getSequenceNumber() >= startSeq
//							&& tcp.getSequenceNumber() <= endSeq
//									+ lastDataPacket.getPayloadLen()) {
//						rrPackets.add(p);
//						p.setRequestResponseInfo(rrInfo);
//					}
//				}
//			}
//			rrPackets.trimToSize();
//			rrInfo.packets = rrPackets;
		}

		/**
		 * Determine the packets that make up the request/response
		 * 
		 * @param packetOffsets
		 * @param index
		 * @return Success case PacketInfo which creates a request/response;
		 *         else null.
		 */
		private PacketInfo determineDataPacketAtIndex(
				SortedMap<Integer, PacketInfo> packetOffsets, int index) {

			// Determine the packets that make up the request/response
			for (SortedMap.Entry<Integer, PacketInfo> entry : packetOffsets
					.entrySet()) {
				int packetOffset = entry.getKey().intValue();
				if (index >= packetOffset
						&& index < packetOffset
								+ entry.getValue().getPayloadLen()) {
					return entry.getValue();
				}
			}
			return null;
		}

		/**
		 * Read a line of text from the HTTP request/response stream
		 * 
		 * @param input
		 *            the request/response stream
		 * @return Next line of text in stream or null if end of stream reached
		 * @throws IOException
		 */
		private synchronized String readLine() throws IOException {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int b;
			try {

				// Look for CRLF
				while ((b = readInput()) != -1) {
					switch (b) {
					case '\r':
						b = readInput();
						if (b == '\n') {

							// Return found line of text
							return new String(output.toByteArray(), "UTF-8");
						} else {
							output.write('\r');
							output.write(b);
						}
						break;
					default:
						output.write(b);
					}
				}

				// End of stream
				return output.size() > 0 ? new String(output.toByteArray(),
						"UTF-8") : null;
			} finally {
				output.close();
			}
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
		private synchronized HttpRequestResponseInfo findNextRequestResponse(
				PacketInfo.Direction direction,
				SortedMap<Integer, PacketInfo> packetOffsets)
				throws IOException {
			int index = counter;
			String line = readLine();
			while (line != null && line.length() == 0) {
				index = counter;
				line = readLine();
			}

			HttpRequestResponseInfo rrInfo = null;
			if (line != null) {
				Matcher matcher;
				rrInfo = new HttpRequestResponseInfo(session, direction);
				rrInfo.rrStart = index;

				// Check for request type
				matcher = strReRequestType.matcher(line);
				if (matcher.lookingAt()) {
					rrInfo.statusLine = line;
					rrInfo.requestType = matcher.group(1);
					rrInfo.direction = Direction.REQUEST;
					rrInfo.objName = matcher.group(2);
					try {
						rrInfo.objUri = new URI(rrInfo.objName);
						if (rrInfo.objUri.getHost() != null) {
							rrInfo.hostName = rrInfo.objUri.getHost();
						}
					} catch (URISyntaxException e) {
						// Ignore since value does not have to be a URI
					}
					rrInfo.version = matcher.group(3);
					rrInfo.scheme = rrInfo.version.split("/")[0];
					
					switch (direction) {
					case UPLINK:
						rrInfo.port = session.getRemotePort();
						break;
					case DOWNLINK:
						rrInfo.port = session.getLocalPort();
						break;
					}

				}

				// Get response
				matcher = strReResponseResults.matcher(line);
				if (matcher.lookingAt()) {
					rrInfo.statusLine = line;
					rrInfo.direction = Direction.RESPONSE;
					rrInfo.version = matcher.group(1);
					rrInfo.scheme = rrInfo.version.split("/")[0];
					rrInfo.statusCode = Integer.parseInt(matcher.group(2));
					rrInfo.responseResult = matcher.group(3);
				}

				if (rrInfo.direction == null) {

					// Check for HTTPS
					if (session.isSsl()) {
						rrInfo.ssl = true;
					}
					while ((line = readLine()) != null && line.length() > 0)
						;
					rrInfo.rawSize = counter - index;
					switch (direction) {
					case UPLINK:
						rrInfo.direction = Direction.REQUEST;
						break;
					case DOWNLINK:
						rrInfo.direction = Direction.RESPONSE;

						// Actual content length is unknown so headers are
						// included
						rrInfo.contentOffsetLength = new TreeMap<Integer, Integer>();
						rrInfo.contentOffsetLength.put(index, rrInfo.rawSize);
						break;
					}
					mapPackets(packetOffsets, index, counter - 1, direction,
							rrInfo);
					result.add(rrInfo);
				}
			}

			return rrInfo;
		}

		/**
		 * Parse data from the line of text
		 * 
		 * @param headerLine
		 * @param rrInfo
		 */
		private synchronized void parseHeaderLine(String headerLine,
												  HttpRequestResponseInfo rrInfo) {
			
			appendHeaderToHttpRequestResponseInfo(headerLine, rrInfo);
			
			Matcher matcher;
			String[] s;

			// Get request host
			matcher = strReRequestHost.matcher(headerLine);
			if (matcher.lookingAt()) {
				String hostName = headerLine.substring(matcher.end()).trim();
				
				// Strip port info if included
				int i = hostName.indexOf(':');
				if (i >= 0) {
					hostName = hostName.substring(0, i);
				}
				rrInfo.hostName = hostName;
				return;
			}

			// Get request content length
			matcher = strReResponseContentLength.matcher(headerLine);
			if (matcher.lookingAt() && rrInfo.contentLength == 0) {
				try{
					rrInfo.contentLength = Integer.parseInt(headerLine.substring(matcher.end()).trim());
				}catch(NumberFormatException e) {
					/* The value exceeds the Interger.MAX_VALUE i.e 2^31-1=2147483647*/
					logger.log(Level.FINE, "Cannot parse the string to int for contentLength,because"
							+ " The value to parse is :" 
							+ (headerLine.substring(matcher.end()).trim()) 
							+ " which is greater than the Integer.MAX_VALUE (2^31-1=2147483647).");
				}
				
				return;
			}

			// Get request transfer encoding
			matcher = strReTransferEncoding.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.chunked = CHUNKED.equals(headerLine.substring(matcher.end())
						.trim());
				return;
			}

			// Get request transfer encoding
			matcher = strReResponseContentEncoding.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.contentEncoding = headerLine.substring(matcher.end()).trim().toLowerCase();
				return;
			}

			// Get content type
			matcher = strReResponseContentType.matcher(headerLine);
			if (matcher.lookingAt()) {
				s = headerLine.substring(matcher.end()).trim().split(";");
				rrInfo.contentType = s[0].trim().toLowerCase();
				for (int i = 1; i < s.length; ++i) {
					int index = s[i].indexOf("=");
					if (index >= 0) {
						String attr = s[i].substring(0, index - 1).trim();
						if (CHARSET.equals(attr)) {
							rrInfo.charset = s[i].substring(index).trim();
						}
					}
				}
				return;
			}

			// Date
			matcher = strReResponseDate.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.date = readHttpDate(matcher.group(1), false);
				return;
			}

			// Pragma: no-cache
			matcher = strReResponsePragmaNoCache.matcher(headerLine);
			if (matcher.lookingAt()) {
				rrInfo.hasCacheHeaders = true;
				rrInfo.pragmaNoCache = true;
				return;
			}

			// Cache-Control
			matcher = strReResponseCacheControl.matcher(headerLine);
			if (matcher.lookingAt()) {
				s = matcher.group(1).split(",");
				if (s.length > 0) {
					rrInfo.hasCacheHeaders = true;
				}
				for (int i = 0; i < s.length; ++i) {
					String directive = s[i].trim();
					if (NOCACHE.equals(directive)) {
						rrInfo.noCache = true;
						continue;
					} else if (NOSTORE.equals(directive)) {
						rrInfo.noStore = true;
						continue;
					}

					// max-age
					matcher = strReCacheMaxAge.matcher(directive);
					if (matcher.lookingAt()) {
						rrInfo.maxAge = Long.valueOf(matcher.group(1));
						continue;
					}

					if (rrInfo.direction == Direction.REQUEST) {
						if (ONLYIFCACHED.equals(directive)) {
							rrInfo.onlyIfCached = true;
							continue;
						}

						// min-fresh
						matcher = strReCacheMinFresh.matcher(directive);
						if (matcher.lookingAt()) {
							rrInfo.minFresh = Long.valueOf(matcher.group(1));
							continue;
						}

						// max-stale
						matcher = strReCacheMaxStale.matcher(directive);
						if (matcher.lookingAt()) {
							rrInfo.maxStale = matcher.group(1) != null ? Long
									.valueOf(matcher.group(1)) : Long.MAX_VALUE;
							continue;
						}

					} else if (rrInfo.direction == Direction.RESPONSE) {
						if (PUBLIC.equals(directive)) {
							rrInfo.publicCache = true;
							continue;
						} else if (PRIVATE.equals(directive)) {
							rrInfo.privateCache = true;
							continue;
						} else if (MUSTREVALIDATE.equals(directive)) {
							rrInfo.mustRevalidate = true;
							continue;
						} else if (PROXYREVALIDATE.equals(directive)) {
							rrInfo.proxyRevalidate = true;
							continue;
						}

						// s-maxage
						matcher = strReCacheSMaxAge.matcher(directive);
						if (matcher.lookingAt()) {
							rrInfo.sMaxAge = Long.valueOf(matcher.group(1));
							continue;
						}

					}
				}
				return;
			}

			if (rrInfo.direction == Direction.RESPONSE) {

				// ETag
				matcher = strReResponseEtag.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.etag = matcher.group(2);
					return;
				}

				// Age
				matcher = strReResponseAge.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.age = Long.valueOf(matcher.group(1));
					return;
				}

				// Expires
				matcher = strReResponseExpires.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.expires = readHttpDate(matcher.group(1), true);
					return;
				}

				// Last modified
				matcher = strReResponseLastMod.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.lastModified = readHttpDate(matcher.group(1), false);
					return;
				}

				// Content-Range
				matcher = strReContentRange.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.rangeResponse = true;
					rrInfo.rangeFirst = Integer.parseInt(matcher.group(1));
					try{
						rrInfo.rangeLast = Integer.parseInt(matcher.group(2));
					}catch(NumberFormatException e) {
					/* The value exceeds the Interger.MAX_VALUE i.e 2^31-1=2147483647.
					 * Continue.*/
					logger.log(Level.FINE, "Cannot parse the string to int for rangeLast,because"
							+ " The value to parse is :" 
							+ matcher.group(2)
							+ " which is greater than the Integer.MAX_VALUE (2^31-1=2147483647).");
					
					}
					rrInfo.rangeFull = Long.parseLong(matcher.group(3));

					if (rrInfo.contentLength == 0) {
						rrInfo.contentLength = rrInfo.rangeLast
								- rrInfo.rangeFirst + 1;
					}
					return;
				}

			} else if (rrInfo.direction == Direction.REQUEST) {

				// Referrer
				matcher = strReResponseReferer.matcher(headerLine);
				if (matcher.lookingAt()) {
					try {
						rrInfo.referrer = new URI(matcher.group(1).trim());
					} catch (URISyntaxException e) {
						logger.fine("Invalid referrer URI: "
								+ matcher.group(1));
					}
					return;
				}

				// If-Modified-Since
				matcher = strReIfModifiedSince.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.ifModifiedSince = true;
					return;
				}

				// If-None-Match
				matcher = strReIfNoneMatch.matcher(headerLine);
				if (matcher.lookingAt()) {
					rrInfo.ifNoneMatch = true;
					return;
				}

			}
			
			logger.log(Level.FINEST, "found a line that was not parsed: {0}", headerLine);
		}

		private void appendHeaderToHttpRequestResponseInfo(String line, HttpRequestResponseInfo rrInfo) {
			if (rrInfo != null) {
				StringBuilder headersBuilder;
				if (rrInfo.allHeaders == null) {
					headersBuilder = new StringBuilder();
				} else {
					headersBuilder = new StringBuilder(rrInfo.allHeaders);
				}
				headersBuilder.append(HEADERS_SEPARATOR).append(line);
				rrInfo.allHeaders = headersBuilder.toString();
			}
		}

		/**
		 * Reads from input stream keeping counter of how much has been read
		 * 
		 * @return
		 * @throws IOException
		 */
		private synchronized int readInput() throws IOException {
			int result;
			if (counter < input.length) {
				result = input[counter];
				++counter;
			} else {
				result = -1;
			}
			return result;
		}

		/**
		 * Parses HTTP date formats. Synchronized because DateFormat objects are
		 * not thread-safe. If defaultForExpired is true and value is an invalid
		 * dateFormat (such as -1 or 0 meaning already expired), the returned
		 * Date will be "beginning of time" Jan 1 1970.
		 * 
		 * @param value
		 * @param defaultForExpired
		 *            boolean - true/false provide default "beginning of time"
		 *            Jan 1 1970 GMT Date
		 * @return formated Date value else null.
		 */
		private synchronized Date readHttpDate(String value, boolean defaultForExpired) {
			if (value != null) {
				for (DateFormat dateFormat : dateFormats) {
					try {
						return dateFormat.parse(value.trim());
					} catch (ParseException e) {
						// Ignore for now
					}
				}
			}
			
			if (defaultForExpired) {
				return BEGINNING_OF_TIME;
			}
			
			logger.fine("Unable to parse HTTP date: " + value);
			return null;
		}

	}

	/**
	 * Builds the request/response list from the specified TCP session
	 * 
	 * @param session
	 *            The tcp session object.
	 * @return The list of requests/responses that were found in the specified
	 *         tcp session.
	 * @throws IOException
	 */
	public static List<HttpRequestResponseInfo> extractHttpRequestResponseInfo(
			TCPSession session) throws IOException {
		return new RequestResponseBuilder(session).getResult();
	}

	/**
	 * Constructor
	 */
	private HttpRequestResponseInfo(TCPSession session,
			PacketInfo.Direction direction) {
		if (session == null || direction == null) {
			throw new IllegalArgumentException(
					"Neither session nor direction may be null");
		}		
		this.session = session;
		this.packetDirection = direction;
		// Initialize session remote host
		this.hostName = session.getRemoteHostName();
		
	}

	/**
	 * Compares the specified HttpRequestResponseInfo object to this one.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(HttpRequestResponseInfo o) {
		return Double.valueOf(getTimeStamp()).compareTo(
				Double.valueOf(o.getTimeStamp()));
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HttpRequestResponseInfo) {
			HttpRequestResponseInfo oHttp = (HttpRequestResponseInfo)obj;
			return Double.valueOf(getTimeStamp()) == oHttp.getTimeStamp();
		} else {
			return false;
		}
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)Double.doubleToLongBits(getTimeStamp());
	}

	/**
	 * Returns the TCP session.
	 * 
	 * @return A TCPSession object containing the TCP session.
	 */
	public TCPSession getSession() {
		return session;
	}

	/**
	 * Returns the associated HTTP request/response. For instance, if this
	 * object contains an HTTP request, then this method will return the HTTP
	 * response associated with that request.
	 * 
	 * @return An HttpRequestResponseInfo object containing the associated
	 *         request/response.
	 */
	public HttpRequestResponseInfo getAssocReqResp() {
		return assocReqResp;
	}

	/**
	 * Returns the waterfall information for this request/response pair.  This
	 * is only set when the direction is "REQUEST" and there is an associated
	 * response.
	 * @return
	 */
	public RequestResponseTimeline getWaterfallInfos() {
		return waterfallInfos;
	}

	/**
	 * Returns the timestamp of the first packet associated with this
	 * HttpRequestResponseInfo . This is the offset of the request/response
	 * within the current trace.
	 * 
	 * @return A double that is the first packet timestamp associated with this
	 *         HttpRequestResponseInfo. If the first packet is null, then this
	 *         method returns 0.
	 */
	public double getTimeStamp() {
		return firstDataPacket != null ? firstDataPacket.getTimeStamp() : 0.0;
	}

	/**
	 * Gets the real Date (the first packet Date) for the request/response.
	 * 
	 * @return The first packet Date associated with the
	 *         HttpRequestResponseInfo. If the first packet is null, then this
	 *         method returns null.
	 */
	public Date getAbsTimeStamp() {
		return firstDataPacket != null ? new Date(Math.round(firstDataPacket
				.getPacket().getTimeStamp() * 1000)) : null;
	}

	/**
	 * Returns the direction (request or response).
	 * 
	 * @return An HttpRequestResponseInfo.Direction enumeration value that
	 *         indicates the direction.
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * @return the statusLine
	 */
	public String getStatusLine() {
		return statusLine;
	}

	/**
	 * The HTTP requestType.
	 * 
	 * @return A string containing the HTTP requestType.
	 */
	public String getRequestType() {
		return requestType;
	}

	/**
	 * Returns the HTTP object name.
	 * 
	 * @return A string containing the object name.
	 */
	public String getObjName() {
		return objName;
	}

	/**
	 * Returns the HTTP object URL.
	 * 
	 * @return The HTTP object URL.
	 */
	public URI getObjUri() {
		return objUri;
	}
	
	/**
	 * Returns the HTTP object name without parameters.
	 * 
	 * @return A string containing the object name without parameters.
	 */
	public String getObjNameWithoutParams() {
		if (objName != null && objNameWithoutParams == null) {
			int index = objName.indexOf('?');
			if (index > 0) {
				objNameWithoutParams = objName.substring(0, index);
			} else {
				objNameWithoutParams = objName;
			}
		}
		return objNameWithoutParams;
	}

	/**
	 * Returns the file name that was accessed by this request
	 * @return
	 */
	public String getFileName() {
		if (objName != null && fileName == null) {
			String s = getObjNameWithoutParams();
			int index = s.lastIndexOf('/');
			if (index == s.length() - 1) {
				--index;
			}
			fileName = index >= 0 ? s.substring(index + 1) : s;
		}
		return fileName;
	}

	/**
	 * Returns the host name.
	 * 
	 * @return A string containing the host name.
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Returns the status code.
	 * 
	 * @return An int that is the status code.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns the content type.
	 * 
	 * @return A string that describes the content type.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the content length.
	 * 
	 * @return The content length in bytes.
	 */
	public int getContentLength() {
		return contentLength;
	}

	/**
	 * Returns the raw size in bytes.
	 * 
	 * @return The raw size in bytes.
	 */
	public int getRawSize() {
		return rawSize;
	}

	/**
	 * Returns the raw size in kilobytes.
	 * 
	 * @return The raw size in kilobytes.
	 */
	public double getRawSizeInKB() {
		return (double) rawSize / 1024;
	}

	/**
	 * @return the encrypted
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * Returns the protocol used (e.g http)
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Returns the HTTP request/response version.
	 * 
	 * @return A string that is the HTTP request/response version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the HTTP response result.
	 * 
	 * @return A string containing the HTTP response result.
	 */
	public String getResponseResult() {
		return responseResult;
	}

	/**
	 * Returns the HTTP request/response ContentEncoding.
	 * 
	 * @return A string containing the ContentEncoding.
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	/**
	 * Returns the first data packet associated with the request/response.
	 * 
	 * @return A PacketInfo object containing the first data packet.
	 */
	public PacketInfo getFirstDataPacket() {
		return firstDataPacket;
	}

	/**
	 * Returns the HTTP request/response date.
	 * 
	 * @return The date.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Returns the HTTP CacheHeaders state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         CacheHeaders, and is false otherwise.
	 */
	public boolean isHasCacheHeaders() {
		return hasCacheHeaders;
	}

	/**
	 * Returns the HTTP PragmaNoCache state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         PragmaNoCache, and is false otherwise.
	 */
	public boolean isPragmaNoCache() {
		return pragmaNoCache;
	}

	/**
	 * Returns the HTTP NoCache state.
	 * 
	 * @return A boolean value that is true if the request/response has NoCache,
	 *         and is false otherwise.
	 */
	public boolean isNoCache() {
		return noCache;
	}

	/**
	 * Returns the HTTP NoStore state.
	 * 
	 * @return A boolean value that is true if the request/response has NoStore,
	 *         and is false otherwise.
	 */
	public boolean isNoStore() {
		return noStore;
	}

	/**
	 * Returns the HTTP PublicCache state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         PublicCache, and is false otherwise.
	 */
	public boolean isPublicCache() {
		return publicCache;
	}

	/**
	 * Returns the HTTP PrivateCache state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         PrivateCache, and is false otherwise.
	 */
	public boolean isPrivateCache() {
		return privateCache;
	}

	/**
	 * Returns the HTTP MustRevalidate state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         MustRevalidate, and is false otherwise.
	 */
	public boolean isMustRevalidate() {
		return mustRevalidate;
	}

	/**
	 * Returns the HTTP ProxyRevalidate state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         ProxyRevalidate, and is false otherwise.
	 */
	public boolean isProxyRevalidate() {
		return proxyRevalidate;
	}

	/**
	 * Returns the HTTP OnlyIfCached state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         OnlyIfCached, and is false otherwise.
	 */
	public boolean isOnlyIfCached() {
		return onlyIfCached;
	}

	/**
	 * Returns the HTTP etag.
	 * 
	 * @return A string containing the HTTP etag.
	 */
	public String getEtag() {
		return etag;
	}

	/**
	 * Returns the HTTP request/response age.
	 * 
	 * @return The HTTP request/response age.
	 */
	public Long getAge() {
		return age;
	}

	/**
	 * Returns the HTTP request/response expire date.
	 * 
	 * @return The HTTP request/response expire date.
	 */
	public Date getExpires() {
		return expires;
	}

	/**
	 * Returns the URI referrer.
	 * 
	 * @return The URI referrer.
	 */
	public URI getReferrer() {
		return referrer;
	}

	/**
	 * Returns the HTTP request/response LastModified date.
	 * 
	 * @return The HTTP request/response LastModified date.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Returns the HTTP request/response MaxAge.
	 * 
	 * @return The HTTP request/response MaxAge.
	 */
	public Long getMaxAge() {
		return maxAge;
	}

	/**
	 * Returns the HTTP request/response sMaxAge.
	 * 
	 * @return The HTTP request/response sMaxAge.
	 */
	public Long getsMaxAge() {
		return sMaxAge;
	}

	/**
	 * Returns the HTTP request/response minFresh.
	 * 
	 * @return The HTTP request/response minFresh.
	 */
	public Long getMinFresh() {
		return minFresh;
	}

	/**
	 * Returns the HTTP request/response maxStale.
	 * 
	 * @return The HTTP request/response maxStale.
	 */
	public Long getMaxStale() {
		return maxStale;
	}
	
	/**
	 * To know the packet direction
	 * 
	 * @return direction
	 */
	public PacketInfo.Direction getPacketDirection() {
		return packetDirection;
	}

	/**
	 * Returns all headers.
	 * 
	 * @return Headers as a long string.
	 */
	public String getAllHeaders() {
		return allHeaders;
	}
	
	/**
	 * Returns the binary content of the request/response body.
	 * 
	 * @return An array of bytes containing the binary content of the
	 *         request/response body, or Null if no content is found.
	 * 
	 * @throws ContentException
	 *             - When part of the content is not available.
	 */
	public byte[] getContent() throws ContentException, IOException {
		if (contentOffsetLength != null) {
			byte[] buffer = getStorageBuffer();
			if (buffer == null) {
				return null;
			}

			ByteArrayOutputStream output = new ByteArrayOutputStream(
					(int) getActualByteCount());
			for (Map.Entry<Integer, Integer> entry : contentOffsetLength
					.entrySet()) {
				int start = entry.getKey();
				int size = entry.getValue();
				if (buffer.length < start + size) {
					throw new ContentException("The content may be corrupted.");
				}
				for (int i = start; i < start + size; ++i) {
					output.write(buffer[i]);
				}
			}
			if (CONTENT_ENCODING_GZIP.equals(contentEncoding)) {

				// Decompress gzipped content
				GZIPInputStream gzip=null;
				try{
					gzip = new GZIPInputStream(
							new ByteArrayInputStream(output.toByteArray()));
					output.reset();
					buffer = new byte[2048];
					int len;
					while ((len = gzip.read(buffer)) >= 0) {
						output.write(buffer, 0, len);
					}
				}catch(IOException ioe){
					if (gzip != null) {
						try{
							gzip.close();
						}catch (IOException ex){
							throw ex;
						}
					}
					if (output != null) {
						try{
							output.close();
						}catch (IOException ex){
							throw ex;
						}
					}
					logger.log(Level.FINE, " IOException !!.The file may be corrupt." + ioe.getMessage());
				}
			}
			return output.toByteArray();
		} else {
			return null;
		}
	}

	/**
	 * Saves the binary content of the request/response body to the specified
	 * file.
	 * 
	 * @throws ContentException
	 *             - When part of content is not available.
	 */
	public void saveContentToFile(File file) throws IOException {

		if (contentOffsetLength != null) {
			FileOutputStream fos = new FileOutputStream(file);
			try {
				fos.write(getContent());
			} catch (ContentException e) {

				// If we get a ContentException, just save the bytes we have
				byte[] buffer = getStorageBuffer();
				if (buffer == null) {
					buffer = new byte[0];
				}

				for (Map.Entry<Integer, Integer> entry : contentOffsetLength
						.entrySet()) {
					int start = entry.getKey();
					int len = Math.min(entry.getValue(), buffer.length - start);
					fos.write(buffer, start, len);
				}
			} finally {
				fos.close();
			}
		}
	}

	/**
	 * Gets the number of bytes in the request/response body. The actual byte
	 * count.
	 * 
	 * @return The total number of bytes in the request/response body. If
	 *         contentOffsetLength is null, then this method returns 0.
	 */
	public long getActualByteCount() {
		if (contentOffsetLength != null) {

			byte[] buffer = getStorageBuffer();
			int bufferSize = buffer != null ? buffer.length : 0;

			long result = 0;
			for (Map.Entry<Integer, Integer> entry : contentOffsetLength
					.entrySet()) {
				int start = entry.getKey();
				int size = entry.getValue();
				if (bufferSize < start + size) {

					// Only include what was actually downloaded.
					size = bufferSize - start;
				}
				result += size;
			}
			return result;
		} else {
			return 0;
		}
	}

	/**
	 * Determines whether the same content is contained in this request/response as in
	 * the specified request/response
	 * @param rr The request to compare to
	 * @return true if the content is the same
	 */
	public boolean isSameContent(HttpRequestResponseInfo rr) {

		// Check specified content length
		if (contentLength > 0 && contentLength != rr.contentLength) {
			return false;
		}

		long count = getActualByteCount();
		if (count == rr.getActualByteCount()) {
			
			// If not data then they are the same
			if (count == 0) {
				return true;
			}
			
			// Otherwise do byte by byte compare
			byte[] b1 = getStorageBuffer();
			byte[] b2 = rr.getStorageBuffer();

			Iterator<Map.Entry<Integer, Integer>> it1 = contentOffsetLength.entrySet().iterator();
			Iterator<Map.Entry<Integer, Integer>> it2 = rr.contentOffsetLength.entrySet().iterator();
			
			if (it1.hasNext() && it2.hasNext()) {
				
				Map.Entry<Integer, Integer> e1 = it1.next();
				Map.Entry<Integer, Integer> e2 = it2.next();
				
				int i1 = e1.getKey();
				int i2 = e2.getKey();
				
				do {
					if (b1[i1] != b2[i2]) {
						return false;
					}

					++i1; ++i2;
					if (i1 >= b1.length || i2 >= b2.length) {
						break;
					}
					if (i1 >= e1.getKey() + e1.getValue()) {
						if (it1.hasNext()) {
							e1 = it1.next();
							i1 = e1.getKey();
						} else {
							break;
						}
					}
					if (i2 >= e2.getKey() + e2.getValue()) {
						if (it2.hasNext()) {
							e2 = it2.next();
							i2 = e2.getKey();
						} else {
							break;
						}
					}
				} while (true);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the HTTP rangeResponse state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         rangeResponse, and is false otherwise.
	 */
	public boolean isRangeResponse() {
		return rangeResponse;
	}

	/**
	 * Returns the HTTP request/response rangeFirst value.
	 * 
	 * @return An int that is the HTTP request/response rangeFirst value.
	 */
	public int getRangeFirst() {
		return rangeFirst;
	}

	/**
	 * Returns the HTTP request/response rangeLast value.
	 * 
	 * @return An int that is the HTTP request/response rangeLast value.
	 */
	public int getRangeLast() {
		return rangeLast;
	}

	/**
	 * Returns the HTTP request/response rangeFull value.
	 * 
	 * @return The HTTP request/response rangeFull.
	 */
	public long getRangeFull() {
		return rangeFull;
	}

	/**
	 * Returns the HTTP request/response IfModifiedSince state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         IfModifiedSince, and is false otherwise
	 */
	public boolean isIfModifiedSince() {
		return ifModifiedSince;
	}

	/**
	 * Returns the HTTP request/response ifNoneMatch state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         ifNoneMatch, and is false otherwise.
	 */
	public boolean isIfNoneMatch() {
		return ifNoneMatch;
	}

	/**
	 * Returns the HTTP request/response chunked state.
	 * 
	 * @return A boolean value that is true if the request/response is chunked,
	 *         and is false otherwise.
	 */
	public boolean isChunked() {
		return chunked;
	}

	/**
	 * Returns the HTTP request/response chunkModeFinished state.
	 * 
	 * @return A boolean value that is true if the request/response has
	 *         chunkModeFinished, and is false otherwise.
	 */
	public boolean isChunkModeFinished() {
		return chunkModeFinished;
	}

	/**
	 * Returns the request/response body as a text string. The returned text may
	 * not be readable.
	 * 
	 * @return The content of the request/response body as a string, or null if
	 *         the method does not execute successfully.
	 * 
	 * @throws ContentException
	 *             - When part of the content is not available.
	 */
	public String getContentString() throws ContentException, IOException {
		byte[] content = getContent();
		return content != null ? new String(content, charset != null ? charset
				: "UTF-8") : null;
	}

	/**
	 * Returns the entire request/response including headers as a UTF-8 string.
	 * No decompression of the content is done.
	 * @return UTF-8 string or null if an error occurred
	 */
	public String getRequestResponseText() {
		byte[] storage = getStorageBuffer();
		if (storage == null) {
			return null;
		}
		try {
			return new String(storage, rrStart, rawSize, "UTF-8");
		} catch (UnsupportedEncodingException e) {

			// This should not happen because UTF-8 is valid encoding
			logger.log(Level.SEVERE, "Unexpected error creating request/response string", e);
			return null;
		}
	}

	/**
	 * Convenience method that gets the storage array in the session where this request/
	 * response is located.
	 * @return
	 */
	private byte[] getStorageBuffer() {
		switch (packetDirection) {
		case DOWNLINK:
			return session.getStorageDl();
		case UPLINK:
			return session.getStorageUl();
		default:

			// This should not happen because value is checked on construction
			logger.severe("Packet direction for request/response is has unexpected value");
			return null;
		}

	}
	
	/**
	 * Indicates whether the HTTP content is image or not.
	 * 
	 * @return Returns true when the content is image otherwise returns false.
	 */
	public boolean isImageContent() {
		if (this.getContentType() != null && this.getContentType().contains(IMAGE)) {
			return true;
		}
		return false;
	}

	/**
	 * Indicates whether the server response containing text payload is
	 * compressed or not.
	 * 
	 * @return Returns "none" string if the server response with a text payload is not
	 *         compressed. Otherwise returns a string representation of the compression type used.
	 */
	public String getHttpCompression() {
		return textFileCompression.toString();
	}

	/**
	 * Indicates whether the server response containing text payload is
	 * compressed or not.
	 * 
	 * @param textFileCompressionAnalysis
	 *            - Text file compression analysis.
	 * 
	 * @return Returns true when the text file payload is not compressed.
	 *         Otherwise returns false.
	 */
	public boolean setHttpCompression(TextFileCompressionAnalysis textFileCompressionAnalysis) {

		boolean rsp = false;

		if (packetDirection == PacketInfo.Direction.DOWNLINK && contentLength != 0 && contentType != null && isTextContent(contentType)) {

			if (CONTENT_ENCODING_GZIP.equals(contentEncoding)) {
				textFileCompression = TextFileCompression.GZIP;
				textFileCompressionAnalysis.incrementNoOfCompressedFiles();

			} else if (CONTENT_ENCODING_COMPRESS.equals(contentEncoding)) {
				textFileCompression = TextFileCompression.COMPRESS;
				textFileCompressionAnalysis.incrementNoOfCompressedFiles();

			} else if (CONTENT_ENCODING_DEFLATE.equals(contentEncoding)) {
				textFileCompression = TextFileCompression.DEFLATE;
				textFileCompressionAnalysis.incrementNoOfCompressedFiles();

			} else {
				// the content should be compressed but is not
				textFileCompression = TextFileCompression.NONE;
				
				if(contentLength > 850) {
					textFileCompressionAnalysis.incrementNoOfUncompressedFiles();
					textFileCompressionAnalysis.addToTotalSize(contentLength);
					rsp = true;
				} else {
					textFileCompressionAnalysis.incrementNoOfCompressedFiles();
				}
				
			}

		} else {
			textFileCompression = TextFileCompression.NOT_APPLICABLE;
		}

		return rsp;
	}
	
	/**
	 * Indicates whether the packet is a response packet and the content type is
	 * text/html.
	 * 
	 * @param AsyncCheckAnalysis
	 *            - Script loading asynchronous analysis.
	 * 
	 * @return Returns true when there are no asynchronously loaded files or the
	 *         packet's content type is not text/html. Otherwise returns false.
	 */
	public boolean checkAsyncAttributeInHead(
			AsyncCheckAnalysis asyncCheckAnalysis) {
		// Checking the content length and content type (only text/html).
		if (contentLength != 0 && contentType != null
				&& isContentTypeTextHtml(contentType)) {
			return asyncCheckAnalysis.parseHtmlToFindSyncLoadingScripts(this);
		} else {
			return true;
		}
	}

	/**
	 *  Indicates whether the content type is text/html or not.
	 *  
	 * @return Returns true if the content type is text/html otherwise return false;
	 */
	boolean isContentTypeTextHtml(String contentType){
		if (contentType.equalsIgnoreCase("text/html")) {
			return true;
		} else {
			return false;
		}
	}
	
	public org.jsoup.nodes.Document parseHtml(FileOrderAnalysis fileOrderAnalysis){
		
		org.jsoup.nodes.Document doc = null;
		String packetContent = null;
		try {
			if (this.getContentString() != null) {
				if ((contentLength != 0) && (contentType != null)
						&& (isContentTypeTextHtml(contentType))) {
					try {
						packetContent = this.getContentString();
					} catch (ContentException e) {
						logger.log(Level.FINE,
								"ContentException in parseHtml()");
					} catch (IOException e) {
						logger.log(Level.FINE, "IOExecption in parseHtml()");
					}
					if (packetContent != null) {
						doc = Jsoup.parse(packetContent);
					}
				}
			}
		} catch (ContentException e) {
			logger.log(Level.FINE,"ContentException in parseHtml()");
		} catch (IOException e) {
			logger.log(Level.FINE,"IOExecption in parseHtml()");
		}
			if (doc != null) {
				return doc;
			} else {
				return null;
			}
	}
	
	/**
	 *  Indicates whether the content type is text or not.
	 *  
	 *  The following content types are considered as text:
	 * - any type starting with 'text/'
	 * - any type starting with 'application/' and followed by 'xml', for example: 'application/atom+xml'
 	 * - application/ecmascript
     * - application/json
     * - application/javascript
     * - message/http
     * 
	 * @return Returns true if the content type is text otherwise return false;
	 */
	public static boolean isTextContent(String contentType) {

		if (contentType.equals("application/ecmascript") ||
			contentType.equals("application/json") ||
			contentType.equals("application/javascript") ||
			contentType.equals("message/http")) {
			return true;
		} else {

			Matcher m = TEXT_CONTENT_TYPE_TEXT.matcher(contentType);
			if (m.matches()) {
				return true;
			}
			m = TEXT_CONTENT_TYPE_XML.matcher(contentType);
			if (m.matches()) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Indicates whether the content type is JavaScript or not.
	 * 
	 * The following content types are considered as JavaScript:
	 * 
	 * - application/ecmascript
	 * - application/javascript
	 * - text/javascript
	 * 
	 * @return returns true if the content type is JavaScript otherwise return false
	 * 
	 */
	public static boolean isJavaScript(String contentType) {

		if (contentType.equals("application/ecmascript") ||
			contentType.equals("application/javascript") ||
			contentType.equals("text/javascript")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates whether the content type is CSS or not.
	 * 
	 * The following content types are considered as CSS:
	 * 
	 * - text/css
	 * 
	 * @return returns true if the content type is CSS otherwise return false
	 * 
	 */
	public static boolean isCss(String contentType) {
		return contentType.equals("text/css") ? true : false;
	}
	
	/**
	 * Indicates whether the content type is HTML or not.
	 * 
	 * The following content types are considered as HTML:
	 * 
	 * - text/html
	 * 
	 * @return returns true if the content type is HTML otherwise return false
	 * 
	 */
	public static boolean isHtml(String contentType) {
		return contentType.equals("text/html") ? true : false;
	}

	public static boolean isJSON(String contentType) {
		return contentType.equals("application/json")  ? true : false;
	}
}
	