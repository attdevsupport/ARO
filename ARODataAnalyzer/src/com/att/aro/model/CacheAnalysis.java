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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Contains functionality that performs a cache analysis of the HTTP requests and 
 * responses in a set of TCP session data, and encapsulates the resulting cache analysis 
 * information.
 */
public class CacheAnalysis implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CacheAnalysis.class
			.getName());

	/**
	 * The CacheAnalysis.CacheExpiration Enumeration specifies constant values that 
	 * describe the cache expiration state. This enumeration is part of the CacheAnalysis 
	 * class.
	 */
	public enum CacheExpiration {
		/**
		 * The cache has Expired . 
		 */
		CACHE_EXPIRED,
		/**
		 * The cache has not expired. 
		 */
		CACHE_NOT_EXPIRED,
		/**
		 * The expired cache has been discovered. 
		 */
		CACHE_EXPIRED_HEURISTIC,
		/**
		 * The not expired cache has been discovered.
		 */
		CACHE_NOT_EXPIRED_HEURISTIC
	}

	private class Range implements Comparable<Range> {
		private long firstByte;
		private long lastByte;

		public Range(long firstByte, long lastByte) {
			this.firstByte = firstByte;
			this.lastByte = lastByte;
		}

		@Override
		public int compareTo(Range o) {
			return Long.valueOf(firstByte).compareTo(o.firstByte);
		}

		@Override
		public int hashCode() {
			return Long.valueOf(firstByte).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Range) {
				Range r = (Range) obj;
				return firstByte == r.firstByte && lastByte == r.lastByte;
			}
			return false;
		}

	}

	private long totalRequestResponseBytes;
	private long totalRequestResponseDupBytes;
	private double duplicateContentBytesRatio;
	private List<HttpRequestResponseInfo> validRequests = new ArrayList<HttpRequestResponseInfo>();
	private List<HttpRequestResponseInfo> validResponses = new ArrayList<HttpRequestResponseInfo>();
	private List<HttpRequestResponseInfo> invalidRequests = new ArrayList<HttpRequestResponseInfo>();
	private List<HttpRequestResponseInfo> invalidResponses = new ArrayList<HttpRequestResponseInfo>();
	private Map<String, CacheEntry> cacheEntries = new HashMap<String, CacheEntry>();
	private Map<String, SortedSet<Range>> rangeEntries = new HashMap<String, SortedSet<Range>>();
	private Map<CacheExpiration, List<CacheEntry>> cacheExpirationResponses = new EnumMap<CacheExpiration, List<CacheEntry>>(
			CacheExpiration.class);
	private List<CacheEntry> diagnosisResults = new ArrayList<CacheEntry>();
	private List<CacheEntry> duplicateContent = new ArrayList<CacheEntry>();
	private List<CacheEntry> duplicateContentWithOriginals = new ArrayList<CacheEntry>();

	/**
	 * Initializes an instance of the CacheAnalysis class using the specified collection of TCP session data.
	 * 
	 * Analyzes the session data using the following algorithm:
	 * 
	 * For each response [A] Find its corresponding request [B]
	 * Is it cachable? Yes: goto [C] No: report a non-cachable object ->
	 * TERMINATE [C] Does it hit the cache? Yes: goto [D] No: report a
	 * cache-miss -> put it into cache (if it is cachable) -> TERMINATE [D] Is
	 * it expired? Yes: If (object changed) report... otherwise report... NO:
	 * report a duplicate transfer before expiration [E] Update the cache entry
	 * 
	 * @param sessions A Collection of TCPSession objects.
	 * 
	 * @throws java.io.IOException
	 */
	public CacheAnalysis(Collection<TCPSession> sessions) throws IOException {

		// Initialize cache expiration lists
		for (CacheExpiration expiration : CacheExpiration.values()) {
			cacheExpirationResponses.put(expiration,
					new ArrayList<CacheEntry>());
		}

		// Build a sorted list of all of the HTTP request/response in the trace
		List<HttpRequestResponseInfo> rrInfo = new ArrayList<HttpRequestResponseInfo>();
		for (TCPSession session : sessions) {
			rrInfo.addAll(session.getRequestResponseInfo());
		}
		Collections.sort(rrInfo);

		// Iterate through responses looking for duplicates
		for (HttpRequestResponseInfo response : rrInfo) {
			if (response.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {

				// Check whether request is valid
				if (response.getRequestType() != null) {
					validRequests.add(response);
				} else {
					invalidRequests.add(response);
				}

				// We only want to process responses
				continue;
			}

			// Check whether response is valid
			int statusCode = response.getStatusCode();
			if (statusCode == 0) {
				invalidResponses.add(response);
				diagnosisResults.add(new CacheEntry(null, response,
						CacheEntry.Diagnosis.CACHING_DIAG_INVALID_RESPONSE, 0, 
						response.getSession().getPackets().get(0)));
				continue;
			} else {
				validResponses.add(response);
			}

			if (statusCode != 200 && statusCode != 206 && statusCode != 304) {
				diagnosisResults.add(new CacheEntry(null, response,
						CacheEntry.Diagnosis.CACHING_DIAG_INVALID_REQUEST, 0, 
						response.getSession().getPackets().get(0)));
				continue;
			}

			// [A] Find corresponding request
			HttpRequestResponseInfo request = response.getAssocReqResp();
			if (request == null) {
				diagnosisResults
						.add(new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_REQUEST_NOT_FOUND,
								0, response.getSession().getPackets().get(0)));
				continue;
			}

			// Request must by GET, POST, or PUT
			String requestType = request.getRequestType();
			if (!HttpRequestResponseInfo.HTTP_GET.equals(requestType)
					&& !HttpRequestResponseInfo.HTTP_PUT.equals(requestType)
					&& !HttpRequestResponseInfo.HTTP_POST.equals(requestType)) {
				diagnosisResults.add(new CacheEntry(request, response,
						CacheEntry.Diagnosis.CACHING_DIAG_INVALID_REQUEST, 0, 
						response.getSession().getPackets().get(0)));
				continue;
			}

			// Check for valid object name and host name
			if (request.getHostName() == null || request.getObjName() == null) {
				diagnosisResults.add(new CacheEntry(request, response,
						CacheEntry.Diagnosis.CACHING_DIAG_INVALID_OBJ_NAME, 0, 
						response.getSession().getPackets().get(0)));
				continue;
			}

			// [B] Object cacheable?
			if (response.isNoStore() || request.isNoStore()
					|| HttpRequestResponseInfo.HTTP_POST.equals(requestType)
					|| HttpRequestResponseInfo.HTTP_PUT.equals(requestType)) {
				cacheEntries.remove(getObjFullName(request, response));
				diagnosisResults.add(new CacheEntry(request, response,
						CacheEntry.Diagnosis.CACHING_DIAG_NOT_CACHABLE, 0, 
						response.getSession().getPackets().get(0)));
				continue;
			}

			// [C] Does it hit the cache?
			CacheEntry cacheEntry = searchCache(request, response);
			CacheEntry newCacheEntry;
			if (cacheEntry == null) {
				newCacheEntry = new CacheEntry(request, response,
						CacheEntry.Diagnosis.CACHING_DIAG_CACHE_MISSED, 
						response.getSession().getPackets().get(0));
				addToCache(newCacheEntry);
				diagnosisResults.add(newCacheEntry);
				continue;
			}

			CacheExpiration expStatus = cacheExpired(cacheEntry,
					request.getAbsTimeStamp());
			SortedSet<Range> ranges = getPartialRanges(cacheEntry);
			if (isFullCacheHit(request, response, ranges)) {

				// [D] Is it expired?
				switch (expStatus) {
				case CACHE_EXPIRED:
				case CACHE_EXPIRED_HEURISTIC:

					// Check to see if object changed
					HttpRequestResponseInfo cachedResponse = cacheEntry
							.getResponse();
					if ((response.getLastModified() != null
							&& cachedResponse.getLastModified() != null && !response
							.getLastModified().equals(
									cachedResponse.getLastModified()))
							|| !response.isSameContent(cachedResponse)) {

						newCacheEntry = new CacheEntry(request, response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_CHANGED, 
								response.getSession().getPackets().get(0));
					} else if (response.getStatusCode() == 304) {
						newCacheEntry = new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304, 
								response.getSession().getPackets().get(0));
					} else if (request.isIfModifiedSince()
							|| request.isIfNoneMatch()) {
						newCacheEntry = new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER, 
								response.getSession().getPackets().get(0));
					} else {
						newCacheEntry = new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT, 
								response.getSession().getPackets().get(0));
					}
					diagnosisResults.add(newCacheEntry);
					break;
				case CACHE_NOT_EXPIRED:
				case CACHE_NOT_EXPIRED_HEURISTIC:
					if (response.getStatusCode() == 304) {
						logger.warning("679 - Unexpected 304 HTTP status encountered");
					}
					newCacheEntry = new CacheEntry(request, response,
							CacheEntry.Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP, 
							response.getSession().getPackets().get(0));
					diagnosisResults.add(newCacheEntry);
					break;
				default:

					// Should not occur
					newCacheEntry = null;
				}
			} else {
				long bytesInCache = getBytesInCache(request, response, ranges);

				// [D] Is it expired?
				switch (expStatus) {
				case CACHE_EXPIRED:
				case CACHE_EXPIRED_HEURISTIC:

					// Check to see if object changed
					HttpRequestResponseInfo cachedResponse = cacheEntry
							.getResponse();
					if ((response.getLastModified() != null
							&& cachedResponse.getLastModified() != null && !response
							.getLastModified().equals(
									cachedResponse.getLastModified()))
							|| !response.isSameContent(cachedResponse)) {

						newCacheEntry = new CacheEntry(request, response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_CHANGED, 
								response.getSession().getPackets().get(0));
					} else if (response.getStatusCode() == 304) {
						newCacheEntry = new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304, 
								response.getSession().getPackets().get(0));
					} else if (request.isIfModifiedSince()
							|| request.isIfNoneMatch()) {
						newCacheEntry = new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER,
								bytesInCache, response.getSession().getPackets().get(0));
					} else {
						newCacheEntry = new CacheEntry(
								request,
								response,
								CacheEntry.Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT,
								bytesInCache, response.getSession().getPackets().get(0));
					}
					diagnosisResults.add(newCacheEntry);
					break;
				case CACHE_NOT_EXPIRED:
				case CACHE_NOT_EXPIRED_HEURISTIC:
					if (response.getStatusCode() == 304) {
						logger.warning("679 - Unexpected 304 HTTP status encountered");
					}
					newCacheEntry = new CacheEntry(
							request,
							response,
							CacheEntry.Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT,
							bytesInCache, response.getSession().getPackets().get(0));
					diagnosisResults.add(newCacheEntry);
					break;
				default:

					// Should not occur
					newCacheEntry = null;
				}

			}

			cacheExpirationResponses.get(expStatus).add(newCacheEntry);
			newCacheEntry.setCacheHit(cacheEntry);
			addToCache(newCacheEntry);

		}

		// Get cache problems
		Set<CacheEntry> dupsWithOrig = new HashSet<CacheEntry>();
		for (CacheEntry c : diagnosisResults) {
			long bytes = c.getResponse().getActualByteCount();
			totalRequestResponseBytes += bytes;
			switch (c.getDiagnosis()) {
			case CACHING_DIAG_NOT_EXPIRED_DUP:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT:
			case CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT:
				duplicateContent.add(c);
				totalRequestResponseDupBytes += bytes;
				dupsWithOrig.add(c);
				if (c.getCacheHit() != null) {
					dupsWithOrig.add(c.getCacheHit());
				}
				break;
			}
		}
		this.duplicateContentWithOriginals.addAll(dupsWithOrig);
		Collections.sort(duplicateContentWithOriginals);
		this.duplicateContentBytesRatio = totalRequestResponseBytes != 0 ? (double) totalRequestResponseDupBytes
				/ totalRequestResponseBytes
				: 0.0;

	}

	/**
	 * Returns the diagnosis results. 
	 * 
	 * @return A List of CacheEntry objects that contain the diagnosis results.
	 */
	public List<CacheEntry> getDiagnosisResults() {
		return Collections.unmodifiableList(diagnosisResults);
	}

	/**
	 * Returns the list of duplicate content cache entries. 
	 * 
	 * @return A List of CacheEntry object that contain the duplicate content.
	 */
	public List<CacheEntry> getDuplicateContent() {
		return Collections.unmodifiableList(duplicateContent);
	}

	/**
	 * Returns the duplicate content cache entries along with the original cache entries. 
	 * 
	 * @return duplicateContentWithOriginals.
	 */
	public List<CacheEntry> getDuplicateContentWithOriginals() {
		return Collections.unmodifiableList(duplicateContentWithOriginals);
	}

	/**
	 * Returns the ratio of the number of bytes that were sent for duplicate content, 
	 * compared to the total bytes of downloaded content. To get a percentage of duplicate 
	 * content, multiply the value returned by this method by 100. 
	 * 
	 * @return A List of CacheEntry objects containing the duplicate content with the 
	 * original content for each duplicate.
	 */
	public double getDuplicateContentBytesRatio() {
		return duplicateContentBytesRatio;
	}

	/**
	 * Returns the total number of bytes that are a result of duplicate content. 
	 * 
	 * @return The number of bytes that are a result of duplicate content.
	 */
	public long getDuplicateContentBytes() {
		return totalRequestResponseDupBytes;
	}

	/**
	 * Returns the total number of bytes in the TCP Requests and Responses. 
	 * 
	 * @return The total number of downloaded bytes.
	 */
	public long getTotalBytesDownloaded() {
		return totalRequestResponseBytes;
	}

	/**
	 * Returns the cache expiration response data that consists of a mapping of cache 
	 * entries with cache expiration values.
	 * 
	 * @return A Map of CacheExpiration enumeration values and CacheEntry objects.
	 */
	public Map<CacheExpiration, List<CacheEntry>> getCacheExpirationResponses() {
		return Collections.unmodifiableMap(cacheExpirationResponses);
	}

	/**
	 * Cache Expired status analysis.
	 * 
	 * @param cacheEntry
	 * @param timestamp
	 * @return
	 */
	private CacheExpiration cacheExpired(CacheEntry cacheEntry, Date timestamp) {
		HttpRequestResponseInfo request = cacheEntry.getRequest();
		HttpRequestResponseInfo response = cacheEntry.getResponse();

		/*
		 * Cases when an object expires (t=time, s=server, c=client) 
		 * (1) "no-cache" header in request/response 
		 * (2) t >= s.expire (3) t >= c.date + c.max_age (4) t >= s.date + s.max_age
		 * (overrides 1) (5) s.age >= s.expire - s.date (6) s.age >= s.max_age
		 * (overrides 4) (7) s.age >= c.max_age
		 */
		if(request.isNoCache() || request.isPragmaNoCache() || 
			response.isNoCache() || response.isPragmaNoCache()) {
			return CacheExpiration.CACHE_EXPIRED;
		} 
		else if (response.getDate() != null
				&& response.getMaxAge() != null
				&& timestamp.getTime() > response.getAbsTimeStamp().getTime()
						+ (response.getMaxAge().longValue() * 1000)) {
			return CacheExpiration.CACHE_EXPIRED;
		} else if (response.getExpires() != null
				&& !timestamp.before(response.getExpires())) {
			return CacheExpiration.CACHE_EXPIRED;
		} else if (request.getDate() != null
				&& request.getMaxAge() != null
				&& timestamp.getTime() > request.getAbsTimeStamp().getTime()
						+ (request.getMaxAge().longValue() * 1000)) {
			return CacheExpiration.CACHE_EXPIRED;
		} else if (response.getAge() != null
				&& response.getMaxAge() != null
				&& response.getAge().longValue() > response.getMaxAge()
						.longValue()) {
			return CacheExpiration.CACHE_EXPIRED;
		} else if (response.getAge() != null
				&& response.getExpires() != null
				&& response.getDate() != null
				&& (response.getAge().longValue() * 1000) >= response
						.getExpires().getTime() - response.getDate().getTime()) {
			return CacheExpiration.CACHE_EXPIRED;
		} else if (response.getAge() != null
				&& request.getMaxAge() != null
				&& response.getAge().longValue() >= request.getMaxAge()
						.longValue()) {
			return CacheExpiration.CACHE_EXPIRED;
		}
		// we don't consider s-maxage since the cache on the phone is a private
		// cache

		/*
		 * Cases when an object is not expired (1) t < s.expire (2) t < s.date +
		 * s.maxage
		 */
		if (response.getExpires() != null
				&& timestamp.before(response.getExpires())) {
			return CacheExpiration.CACHE_NOT_EXPIRED;
		} else if (response.getDate() != null
				&& response.getMaxAge() != null
				&& timestamp.getTime() < response.getDate().getTime()
						+ (response.getMaxAge().longValue() * 1000)) {
			return CacheExpiration.CACHE_NOT_EXPIRED;
		}

		long oneDay = 86400000L;
		if (response.getDate() != null) {
			if (timestamp.getTime() < response.getDate().getTime() + oneDay) {
				return CacheExpiration.CACHE_NOT_EXPIRED_HEURISTIC;
			} else {
				return CacheExpiration.CACHE_EXPIRED_HEURISTIC;
			}
		}

		return CacheExpiration.CACHE_NOT_EXPIRED_HEURISTIC;
	}

	/**
	 * This method checks a response's byte range to see if it falls fully in
	 * the specified ranges to determine if the response was a full cache hit
	 * 
	 * @param request
	 * @param response
	 * @param ranges
	 * @return true full cache found else false.
	 */
	private boolean isFullCacheHit(HttpRequestResponseInfo request,
			HttpRequestResponseInfo response, SortedSet<Range> ranges) {

		if (ranges != null) {
			for (Range r : ranges) {

				// Here we are looking at the numbers IN THE HEADER instead
				// of ON THE WIRE
				// We assume "Content-Range" in the RESPONSE header match
				// "Range" in the REQUEST
				if (response.getRangeFirst() >= r.firstByte
						&& response.getRangeLast() < r.lastByte) {
					return true;
				}
			}

			// Partial cache hit
			return false;
		}

		// the cache entry contains the entire object
		return true;

	}

	/**
	 * Cache contents calculated in bytes.
	 * 
	 * @param request
	 * @param response
	 * @param ranges
	 * @return cache vaules in bytes
	 */
	private long getBytesInCache(HttpRequestResponseInfo request,
			HttpRequestResponseInfo response, SortedSet<Range> ranges) {

		long xferFirst = response.isRangeResponse() ? response.getRangeFirst()
				: 0;
		long xferLast = xferFirst + response.getRawSize();

		long cachedBytes = 0L;
		for (Range r : ranges) {
			cachedBytes += Math.max(
					0,
					Math.min(xferLast, r.lastByte)
							- Math.max(xferFirst, r.firstByte));
		}
		return cachedBytes;
	}

	private String getObjFullName(HttpRequestResponseInfo request,
			HttpRequestResponseInfo response) {
		return request.getHostName() + "|" + request.getObjName() + "|"
				+ response.getEtag();
	}

	private CacheEntry searchCache(HttpRequestResponseInfo request,
			HttpRequestResponseInfo response) {
		return cacheEntries.get(getObjFullName(request, response));
	}

	private SortedSet<Range> getPartialRanges(CacheEntry ce) {
		return rangeEntries.get(getObjFullName(ce.getRequest(),
				ce.getResponse()));
	}

	/**
	 * Adds the request and response in cacheEntries after the analysis.
	 * 
	 * @param ce
	 */
	private void addToCache(CacheEntry ce) {
		HttpRequestResponseInfo request = ce.getRequest();
		HttpRequestResponseInfo response = ce.getResponse();

		Range r = null;
		long xferSize = calculatePartialTransfer(request, response);
		if (response.isRangeResponse()) {
			long first = response.getRangeFirst();
			long last = xferSize > 0 ? first + xferSize - 1 : response
					.getRangeLast();
			r = new Range(first, last + 1);
		} else if (xferSize > 0) {
			r = new Range(0, xferSize);
		}

		String objFullName = getObjFullName(ce.getRequest(), ce.getResponse());
		if (r != null) {
			SortedSet<Range> ranges = rangeEntries.get(objFullName);
			if (ranges != null) {
				ranges.add(r);
				Iterator<Range> iter = ranges.iterator();
				Range last = iter.next();
				while (iter.hasNext()) {
					Range curr = iter.next();
					if (curr.firstByte >= last.firstByte
							&& curr.firstByte <= last.lastByte) {
						last.lastByte = Math.max(last.lastByte, curr.lastByte);
						iter.remove();
					}
				}
			} else {
				ranges = new TreeSet<Range>();
				ranges.add(r);
				rangeEntries.put(objFullName, ranges);
			}
		} else {
			rangeEntries.remove(objFullName);
		}

		cacheEntries.put(objFullName, ce);
	}

	/**
	 * by cross checking the content length and the actual byte count partial
	 * transfer is calculated.
	 * 
	 * @param request
	 * @param response
	 * @return -1 for partial transfers else actual response bytes.
	 */
	private long calculatePartialTransfer(HttpRequestResponseInfo request,
			HttpRequestResponseInfo response) {
		if (response.isChunked()) {
			return response.isChunkModeFinished() ? 0 : response
					.getContentLength();
		}

		// compute expectedBytes
		int expectedBytes;
		if (response.isRangeResponse()) {
			expectedBytes = response.getRangeLast() - response.getRangeFirst()
					+ 1;
			if (expectedBytes <= 0)
				expectedBytes = response.getContentLength();
		} else {
			expectedBytes = response.getContentLength();
		}
		if (expectedBytes <= 0)
			return -1;

		// compute actualTransferred
		long actualBytes = response.getActualByteCount();
		if (actualBytes <= 0)
			return -1;

		if (actualBytes < (int) (expectedBytes * 0.9f)) {
			return actualBytes;
		} else {
			return -1;
		}
	}

}
