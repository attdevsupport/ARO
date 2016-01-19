/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.packetanalysis.pojo;

import java.net.URI;
import java.util.Date;
import java.util.SortedMap;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.att.aro.core.packetreader.pojo.PacketDirection;

/**
 * Encapsulates information about an HTTP request or response. This class was
 * converted from struct HTTP_REQUEST_RESPONSE
 * @author EDS Team
 * 
 * Refactored by: Borey Sao
 * Date: April 24, 2014
 */
public class HttpRequestResponseInfo implements Comparable<HttpRequestResponseInfo>{
	/**
	 * Returns HTTP version 1.0.
	 */
	public static final String HTTP10 = "HTTP/1.0";

	/**
	 * Returns HTTP version 1.1.
	 */
	public static final String HTTP11 = "HTTP/1.1";
	
	/**
     * Returns UTF-8.
     */
    public static final String UTF8 = "UTF-8";

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
	public static final String CONTENT_ENCODING_GZIP = "gzip";
	public static final String CONTENT_ENCODING_COMPRESS = "compress";
	public static final String CONTENT_ENCODING_DEFLATE = "deflate";
	public static final String CONTENT_ENCODING_NONE = "none";
	public static final String CONTENT_ENCODING_NA = "";
	public static final Date BEGINNING_OF_TIME = new Date(0);
	private static final CharSequence IMAGE = "image";
	
	private PacketDirection packetDirection;
	private HttpDirection direction; // REQUEST or RESPONSE
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
	private String contentEncoding = null;
	private int rrStart;
	private int rawSize; // Includes headers
	private boolean ssl;
	
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
	@JsonIgnore
	private HttpRequestResponseInfo assocReqResp;
	private RequestResponseTimeline waterfallInfos;
	private String allHeaders;
	
	public HttpRequestResponseInfo(){
	}

	public HttpRequestResponseInfo(String remoteHostName,
			PacketDirection direction) {
		if (direction == null) {
			throw new IllegalArgumentException(
					"Http Direction may be null");
		}
		this.packetDirection = direction;
		// Initialize session remote host
		this.hostName = remoteHostName;
		
	}
	public PacketDirection getPacketDirection() {
		return packetDirection;
	}

	public void setPacketDirection(PacketDirection packetDirection) {
		this.packetDirection = packetDirection;
	}

	/**
	 * Returns the direction (request or response).
	 * 
	 * @return An HttpRequestResponseInfo.Direction enumeration value that
	 *         indicates the direction.
	 */
	public HttpDirection getDirection() {
		return direction;
	}

	public void setDirection(HttpDirection direction) {
		this.direction = direction;
	}

	/**
	 * Returns the protocol used (e.g http)
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Returns the HTTP request/response version.
	 * 
	 * @return A string that is the HTTP request/response version.
	 */
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatusLine() {
		return statusLine;
	}

	public void setStatusLine(String statusLine) {
		this.statusLine = statusLine;
	}

	/**
	 * The HTTP requestType.
	 * 
	 * @return A string containing the HTTP requestType.
	 */
	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
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

	public void setObjNameWithoutParams(String objNameWithoutParams) {
		this.objNameWithoutParams = objNameWithoutParams;
	}

	/**
	 * Returns the file name that was accessed by this request
	 * @return
	 */
	public String getFileName() {
		if (objName != null && fileName == null) {
			String objName = getObjNameWithoutParams();
			int index = objName.lastIndexOf('/');
			if (index == objName.length() - 1) {
				--index;
			}
			fileName = index >= 0 ? objName.substring(index + 1) : objName;
		}
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public URI getObjUri() {
		return objUri;
	}

	public void setObjUri(URI objUri) {
		this.objUri = objUri;
	}

	/**
	 * Returns the HTTP response result.
	 * 
	 * @return A string containing the HTTP response result.
	 */
	public String getResponseResult() {
		return responseResult;
	}

	public void setResponseResult(String responseResult) {
		this.responseResult = responseResult;
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

	public void setChunked(boolean chunked) {
		this.chunked = chunked;
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

	public void setChunkModeFinished(boolean chunkModeFinished) {
		this.chunkModeFinished = chunkModeFinished;
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

	public void setRangeResponse(boolean rangeResponse) {
		this.rangeResponse = rangeResponse;
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

	public void setIfModifiedSince(boolean ifModifiedSince) {
		this.ifModifiedSince = ifModifiedSince;
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

	public void setIfNoneMatch(boolean ifNoneMatch) {
		this.ifNoneMatch = ifNoneMatch;
	}

	/**
	 * Returns the HTTP request/response rangeFirst value.
	 * 
	 * @return An int that is the HTTP request/response rangeFirst value.
	 */
	public int getRangeFirst() {
		return rangeFirst;
	}

	public void setRangeFirst(int rangeFirst) {
		this.rangeFirst = rangeFirst;
	}

	/**
	 * Returns the HTTP request/response rangeLast value.
	 * 
	 * @return An int that is the HTTP request/response rangeLast value.
	 */
	public int getRangeLast() {
		return rangeLast;
	}

	public void setRangeLast(int rangeLast) {
		this.rangeLast = rangeLast;
	}

	/**
	 * Returns the HTTP request/response rangeFull value.
	 * 
	 * @return The HTTP request/response rangeFull.
	 */
	public long getRangeFull() {
		return rangeFull;
	}

	public void setRangeFull(long rangeFull) {
		this.rangeFull = rangeFull;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * Returns the HTTP request/response ContentEncoding.
	 * 
	 * @return A string containing the ContentEncoding.
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public int getRrStart() {
		return rrStart;
	}

	public void setRrStart(int rrStart) {
		this.rrStart = rrStart;
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
	public void setRawSize(int rawSize) {
		this.rawSize = rawSize;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public SortedMap<Integer, Integer> getContentOffsetLength() {
		return contentOffsetLength;
	}

	public void setContentOffsetLength(
			SortedMap<Integer, Integer> contentOffsetLength) {
		this.contentOffsetLength = contentOffsetLength;
	}

	/**
	 * Returns the first data packet associated with the request/response.
	 * 
	 * @return A PacketInfo object containing the first data packet.
	 */
	public PacketInfo getFirstDataPacket() {
		return firstDataPacket;
	}

	public void setFirstDataPacket(PacketInfo firstDataPacket) {
		this.firstDataPacket = firstDataPacket;
	}

	public PacketInfo getLastDataPacket() {
		return lastDataPacket;
	}

	public void setLastDataPacket(PacketInfo lastDataPacket) {
		this.lastDataPacket = lastDataPacket;
	}

	/**
	 * Returns the HTTP request/response date.
	 * 
	 * @return The date.
	 */
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public void setHasCacheHeaders(boolean hasCacheHeaders) {
		this.hasCacheHeaders = hasCacheHeaders;
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

	public void setPragmaNoCache(boolean pragmaNoCache) {
		this.pragmaNoCache = pragmaNoCache;
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

	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
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

	public void setNoStore(boolean noStore) {
		this.noStore = noStore;
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

	public void setPublicCache(boolean publicCache) {
		this.publicCache = publicCache;
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

	public void setPrivateCache(boolean privateCache) {
		this.privateCache = privateCache;
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

	public void setMustRevalidate(boolean mustRevalidate) {
		this.mustRevalidate = mustRevalidate;
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

	public void setProxyRevalidate(boolean proxyRevalidate) {
		this.proxyRevalidate = proxyRevalidate;
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

	public void setOnlyIfCached(boolean onlyIfCached) {
		this.onlyIfCached = onlyIfCached;
	}

	/**
	 * Returns the HTTP etag.
	 * 
	 * @return A string containing the HTTP etag.
	 */
	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	/**
	 * Returns the HTTP request/response age.
	 * 
	 * @return The HTTP request/response age.
	 */
	public Long getAge() {
		return age;
	}

	public void setAge(Long age) {
		this.age = age;
	}

	/**
	 * Returns the HTTP request/response expire date.
	 * 
	 * @return The HTTP request/response expire date.
	 */
	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	/**
	 * Returns the URI referrer.
	 * 
	 * @return The URI referrer.
	 */
	public URI getReferrer() {
		return referrer;
	}

	public void setReferrer(URI referrer) {
		this.referrer = referrer;
	}

	/**
	 * Returns the HTTP request/response LastModified date.
	 * 
	 * @return The HTTP request/response LastModified date.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Returns the HTTP request/response MaxAge.
	 * 
	 * @return The HTTP request/response MaxAge.
	 */
	public Long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Long maxAge) {
		this.maxAge = maxAge;
	}

	/**
	 * Returns the HTTP request/response sMaxAge.
	 * 
	 * @return The HTTP request/response sMaxAge.
	 */
	public Long getsMaxAge() {
		return sMaxAge;
	}

	public void setsMaxAge(Long sMaxAge) {
		this.sMaxAge = sMaxAge;
	}

	/**
	 * Returns the HTTP request/response minFresh.
	 * 
	 * @return The HTTP request/response minFresh.
	 */
	public Long getMinFresh() {
		return minFresh;
	}

	public void setMinFresh(Long minFresh) {
		this.minFresh = minFresh;
	}

	/**
	 * Returns the HTTP request/response maxStale.
	 * 
	 * @return The HTTP request/response maxStale.
	 */
	public Long getMaxStale() {
		return maxStale;
	}

	public void setMaxStale(Long maxStale) {
		this.maxStale = maxStale;
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

	public void setAssocReqResp(HttpRequestResponseInfo assocReqResp) {
		this.assocReqResp = assocReqResp;
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

	public void setWaterfallInfos(RequestResponseTimeline waterfallInfos) {
		this.waterfallInfos = waterfallInfos;
	}

	public String getAllHeaders() {
		return allHeaders;
	}

	public void setAllHeaders(String allHeaders) {
		this.allHeaders = allHeaders;
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
	 * Compares the specified HttpRequestResponseInfo object to this one.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(HttpRequestResponseInfo resInfo) {
		return Double.valueOf(getTimeStamp()).compareTo(
				Double.valueOf(resInfo.getTimeStamp()));
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
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HttpRequestResponseInfo) {
			HttpRequestResponseInfo oHttp = (HttpRequestResponseInfo)obj;
			return getTimeStamp() == oHttp.getTimeStamp();
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
	
}//end class
