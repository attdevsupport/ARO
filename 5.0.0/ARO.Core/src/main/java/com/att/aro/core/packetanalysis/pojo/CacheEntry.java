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

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A bean class that contains one cache entry, and provides methods for returning the 
 * information from that entry.
 */
public class CacheEntry implements Serializable, Comparable<CacheEntry> {
	private static final long serialVersionUID = 1L;

	private CacheEntry cacheHit;
	private HttpRequestResponseInfo request;
	private HttpRequestResponseInfo response;
	private Diagnosis diagnosis;
	private long rawBytes;
	private long bytesInCache;
	private long bytesNotInCache;
	private PacketInfo sessionFirstPacket;
	private int cacheCount=0;
	private String hostName;

	private long contentLength;
	private Double timeStamp;
	private HttpRequestResponseInfo httpRequestResponse;
	private String httpObjectName;
	
	@JsonIgnore
	private Session session;
	
	@JsonIgnore
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * Initializes an instance of the CacheEntry class using the specified HTTP request 
	 * and response information, and cache diagnosis value.
	 * 
	 * @param request An HttpRequestResponseInfo object containing the request. This parameter 
	 * can be null if the response is not matched to the request.
	 * 
	 * @param response An HttpRequestResponseInfo object containing the response. This 
	 * parameter cannot be null. 
	 * 
	 * @param diagnosis A CacheEntry.Diagnosis enumeration value that identifies the 
	 * diagnosis (or category) of this cache entry.
	 * 
	 * @param sessionFirstPacket The first packet of session.
	 */
	public CacheEntry(HttpRequestResponseInfo request, HttpRequestResponseInfo response,
			Diagnosis diagnosis, PacketInfo sessionFirstPacket) {
		this(request, response, diagnosis, Long.MAX_VALUE, sessionFirstPacket);
	}

	/**
	 * Initializes an instance of the CacheEntry class using the specified HTTP request 
	 * and response information, cache diagnosis value, and number of bytes in the cache.
	 * 
	 * @param request An HttpRequestResponseInfo object containing the request. This parameter 
	 * can be null if the response is not matched to the request.
	 * 
	 * @param response An HttpRequestResponseInfo object containing the response. This 
	 * parameter cannot be null 
	 * 
	 * @param diagnosis A CacheEntry.Diagnosis enumeration value that identifies the 
	 * diagnosis (or category) of this cache entry.
	 * 
	 * @param bytesInCache The number of bytes in the cache. This parameter is used for 
	 * responses with data partially in the cache.
	 * 
	 * @param sessionFirstPacket The first packet of session.
	 */
	public CacheEntry(HttpRequestResponseInfo request, HttpRequestResponseInfo response,
			Diagnosis diagnosis, long bytesInCache, PacketInfo sessionFirstPacket) {
		if (request != null) {
			this.request = request;
			this.rawBytes += request.getRawSize();
		}
		
		if (request != null) {
		    this.timeStamp=0.0;//request.getTimeStamp();
			this.httpObjectName = request.getObjName();
			this.hostName = request.getHostName();
			httpRequestResponse=request.getAssocReqResp();
		} else {
			this.httpObjectName = "";
			this.hostName = "";
		}
		
		
		// Response cannot be null
		this.response = response;
		this.rawBytes += response.getRawSize();
		
	    this.contentLength =	getResponse().getContentLength();
		this.diagnosis = diagnosis;
		this.bytesInCache = Math.min(bytesInCache, rawBytes);
		this.bytesNotInCache = Math.max(0, rawBytes - bytesInCache);
		this.sessionFirstPacket = sessionFirstPacket;
	}

	/**
	 * Compares the specified CacheEntry object to this CacheEntry object are returns a 
	 * value that indicates if they are the same.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CacheEntry entry) {
		return response.compareTo(entry.response);
	}

	/**
	 * Returns the HTTP request information. 
	 * 
	 * @return An HTTPRrequestResponseInformation object containing the request.
	 */
	public HttpRequestResponseInfo getRequest() {
		return request;
	}

	/**
	 * Returns the HTTP response information. 
	 * 
	 * @return An HTTPRrequestResponseInformation object containing the response.
	 */
	public HttpRequestResponseInfo getResponse() {
		return response;
	}

	/**
	 * Returns the cache diagnosis. 
	 * 
	 * @return A CacheEntry.Diagnosis enumeration value that specifies the cache category.
	 */
	public Diagnosis getDiagnosis() {
		return diagnosis;
	}

	/**
	 * Returns the number of raw bytes in the  HTTP request/response. 
	 * 
	 * @return The number of raw bytes.
	 */
	public long getRawBytes() {
		return rawBytes;
	}

	/**
	 * Returns the number of bytes in the cache of the HTTP request/response. 
	 * 
	 * @return The number of bytes in the cache.
	 */
	public long getBytesInCache() {
		return bytesInCache;
	}

	/**
	 * Returns the number of bytes that are not in cache of the HTTP request/response. 
	 * 
	 * @return The number of bytes that are not in the cache.
	 */
	public long getBytesNotInCache() {
		return bytesNotInCache;
	}

	/**
	 * Returns the cache hit of the HTTP request/response. 
	 * 
	 * @return A CacheEntry object containing the cache hit.
	 */
	public CacheEntry getCacheHit() {
		return cacheHit;
	}
	
	/**
	 * Returns the first packet of session. 
	 * 
	 * @return first packet of session.
	 */
	public PacketInfo getSessionFirstPacket() {
		return sessionFirstPacket;
	}

	/**
	 * Sets the HTTP cacheHit to the specified cacheHit.
	 * 
	 * @param cacheHit A CacheEntry object containing the cacheHit to set.
	 */
	public void setCacheHit(CacheEntry cacheHit) {
		this.cacheHit = cacheHit;
	}

	/**
	 * Returns a value that indicates whether the HTTP request/response has cache headers. 
	 * 
	 * @return A boolean value that is true if the HTTP request/response has cache 
	 * headers, and is  false if it does not.
	 */
	public boolean hasCacheHeaders() {
		return response.isHasCacheHeaders() || (request != null && request.isHasCacheHeaders());
	}
	
	/**
	 * Returns a value that indicates whether the HTTP request/response . 
	 * 
	 * @return HttpRequestResponse 
	 *.
	 */
	public HttpRequestResponseInfo getHttpRequestResponse() {
		return httpRequestResponse;
	}

	/**
	 * Returns timeStamp of duplicate content . 
	 * 
	 * @return timeStamp 
	 *.
	 */
	public Double getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Returns content of duplicate content . 
	 * 
	 * @return contentLength 
	 *.
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Returns hostName of duplicate content . 
	 * 
	 * @return hostName 
	 *.
	 */
	
	public String getHostName() {
		return hostName;
	}

	
	public String getHttpObjectName() {
		return httpObjectName;
	}
	@JsonIgnore
	public int getCacheHitCount() {
		return cacheHit.getCacheCount();
	}
	
	public void setCacheHitCount(int cacheCount){
		
		cacheHit.setCacheCount(cacheCount);
	}
	public int getCacheCount() {
		return cacheCount;
	}

	public void setCacheCount(int cacheCount) {
		this.cacheCount = cacheCount;
	}
}