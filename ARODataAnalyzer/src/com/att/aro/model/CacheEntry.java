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

import java.io.Serializable;

/**
 * A bean class that contains one cache entry, and provides methods for returning the 
 * information from that entry.
 */
public class CacheEntry implements Serializable, Comparable<CacheEntry> {
	private static final long serialVersionUID = 1L;

	/**
	 * ENUM to maintain the Cache Categories.
	 */
	public enum Diagnosis {
		/**
		 * Missed cache.
		 */
		CACHING_DIAG_CACHE_MISSED,
		/**
		 * Not cacheable.
		 */
		CACHING_DIAG_NOT_CACHABLE,
		/**
		 * Not expired.
		 */
		CACHING_DIAG_NOT_EXPIRED_DUP,
		/**
		 * Object changed.
		 */
		CACHING_DIAG_OBJ_CHANGED,
		/**
		 * Object not changed.
		 */
		CACHING_DIAG_OBJ_NOT_CHANGED_304,
		/**
		 * Server object not changed/duplicate.
		 */
		CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER,
		/**
		 * Client object not changed/duplicate.
		 */
		CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT,
		/**
		 * Server object not changed/duplicate/partial hit.
		 */
		CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER,
		/**
		 * Client object not changed/duplicate/partial hit.
		 */
		CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT,
		/**
		 * Not expired/duplicate/partial hit.
		 */
		CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT,
		/**
		 * Invalid request.
		 */
		CACHING_DIAG_INVALID_REQUEST,
		/**
		 * Invalid object name.
		 */
		CACHING_DIAG_INVALID_OBJ_NAME,
		/**
		 * Invalid response.
		 */
		CACHING_DIAG_INVALID_RESPONSE,
		/**
		 * Request not found.
		 */
		CACHING_DIAG_REQUEST_NOT_FOUND
	}

	private CacheEntry cacheHit;
	private HttpRequestResponseInfo request;
	private HttpRequestResponseInfo response;
	private Diagnosis diagnosis;
	private long rawBytes;
	private long bytesInCache;
	private long bytesNotInCache;

	/**
	 * Initializes an instance of the CacheEntry class using the specified HTTP request 
	 * and response information, and cache diagnosis value.
	 * 
	 * @param request – An HttpRequestResponseInfo object containing the request. This parameter 
	 * can be null if the response is not matched to the request.
	 * 
	 * @param response - An HttpRequestResponseInfo object containing the response. This 
	 * parameter cannot be null. 
	 * 
	 * @param diagnosis – A CacheEntry.Diagnosis enumeration value that identifies the 
	 * diagnosis (or category) of this cache entry.
	 */
	public CacheEntry(HttpRequestResponseInfo request, HttpRequestResponseInfo response,
			Diagnosis diagnosis) {
		this(request, response, diagnosis, Long.MAX_VALUE);
	}

	/**
	 * Initializes an instance of the CacheEntry class using the specified HTTP request 
	 * and response information, cache diagnosis value, and number of bytes in the cache.
	 * 
	 * @param request - An HttpRequestResponseInfo object containing the request. This parameter 
	 * can be null if the response is not matched to the request.
	 * 
	 * @param response - An HttpRequestResponseInfo object containing the response. This 
	 * parameter cannot be null 
	 * 
	 * @param diagnosis - A CacheEntry.Diagnosis enumeration value that identifies the 
	 * diagnosis (or category) of this cache entry.
	 * 
	 * @param bytesInCache – The number of bytes in the cache. This parameter is used for 
	 * responses with data partially in the cache.
	 */
	public CacheEntry(HttpRequestResponseInfo request, HttpRequestResponseInfo response,
			Diagnosis diagnosis, long bytesInCache) {
		if (request != null) {
			this.request = request;
			this.rawBytes += request.getRawSize();
		}

		// Response cannot be null
		this.response = response;
		this.rawBytes += response.getRawSize();

		this.diagnosis = diagnosis;
		this.bytesInCache = Math.min(bytesInCache, rawBytes);
		this.bytesNotInCache = Math.max(0, rawBytes - bytesInCache);
	}

	/**
	 * Compares the specified CacheEntry object to this CacheEntry object are returns a 
	 * value that indicates if they are the same.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CacheEntry o) {
		return response.compareTo(o.response);
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
	 * Sets the HTTP cacheHit to the specified cacheHit.
	 * 
	 * @param cacheHit – A CacheEntry object containing the cacheHit to set.
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
}
