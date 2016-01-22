/**
 *  Copyright 2016 AT&T
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import com.att.aro.core.packetanalysis.ICacheAnalysis;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.CacheExpiration;
import com.att.aro.core.packetanalysis.pojo.Diagnosis;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfoWithSession;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Range;
import com.att.aro.core.packetanalysis.pojo.Session;


/**
 * Contains functionality that performs a cache analysis of the HTTP requests and 
 * responses in a set of TCP session data, and encapsulates the resulting cache analysis 
 * information.
 */
public class CacheAnalysisImpl implements ICacheAnalysis{
	@Autowired
	IHttpRequestResponseHelper rrhelper;
	//@InjectLogger
	//private static ILogger log;
	
	int itindex = 0;
	public CacheAnalysis analyze(List<Session> sessionlist){
		CacheAnalysis result = new CacheAnalysis();
		
		long totalRequestResponseBytes = 0;
		long totalRequestResponseDupBytes = 0;
		double duplicateContentBytesRatio = 0.0;
		
		Map<String, CacheEntry> cacheEntries = new HashMap<String, CacheEntry>();
		Map<String, SortedSet<Range>> rangeEntries = new HashMap<String, SortedSet<Range>>();
		List<CacheEntry> diagnosisResults = new ArrayList<CacheEntry>();
		List<CacheEntry> duplicateContent = new ArrayList<CacheEntry>();
		List<CacheEntry> duplicateContentWithOriginals = new ArrayList<CacheEntry>();
		
		Map<CacheExpiration, List<CacheEntry>> cacheExpirationResponses = result.getCacheExpirationResponses();
		// Initialize cache expiration lists
		for (CacheExpiration expiration : CacheExpiration.values()) {
			cacheExpirationResponses.put(expiration,
					new ArrayList<CacheEntry>());
		}
		// Build a sorted list of all of the HTTP request/response in the trace
		List<HttpRequestResponseInfoWithSession> rrInfo = new ArrayList<HttpRequestResponseInfoWithSession>();
		for(Session session:sessionlist){
			if(!session.isUDP()){
				//rrInfo.addAll(session.getRequestResponseInfo());
				for(HttpRequestResponseInfo item: session.getRequestResponseInfo()){
					HttpRequestResponseInfoWithSession itemsession = new HttpRequestResponseInfoWithSession();
					itemsession.setInfo(item);
					itemsession.setSession(session);
					rrInfo.add(itemsession);
				}
			} 
		}
		Collections.sort(rrInfo);
		// Iterate through responses looking for duplicates
		for (HttpRequestResponseInfoWithSession httpreqres : rrInfo) {
			
			HttpRequestResponseInfo response = httpreqres.getInfo();
			Session session = httpreqres.getSession();
			PacketInfo firstPacket = session.getPackets().get(0);
			
			if (response.getDirection() == HttpDirection.REQUEST) {

				// We only want to process responses
				continue;
			}
			
			// Check whether response is valid
			int statusCode = response.getStatusCode();
			if (statusCode == 0) {
				diagnosisResults.add(new CacheEntry(null, response,
						Diagnosis.CACHING_DIAG_INVALID_RESPONSE, 0, 
						firstPacket));
				continue;
			}

			if (statusCode != 200 && statusCode != 206 && statusCode != 304) {
				diagnosisResults.add(new CacheEntry(null, response,
						Diagnosis.CACHING_DIAG_INVALID_REQUEST, 0, 
						firstPacket));
				continue;
			}

			// [A] Find corresponding request
			HttpRequestResponseInfo request = response.getAssocReqResp();
			if (request == null) {
				diagnosisResults
						.add(new CacheEntry(
								request,
								response,
								Diagnosis.CACHING_DIAG_REQUEST_NOT_FOUND,
								0, firstPacket));
				continue;
			}

			// Request must by GET, POST, or PUT
			String requestType = request.getRequestType();
			if (!HttpRequestResponseInfo.HTTP_GET.equals(requestType)
					&& !HttpRequestResponseInfo.HTTP_PUT.equals(requestType)
					&& !HttpRequestResponseInfo.HTTP_POST.equals(requestType)) {
				diagnosisResults.add(new CacheEntry(request, response,
						Diagnosis.CACHING_DIAG_INVALID_REQUEST, 0, 
						firstPacket));
				continue;
			}

			// Check for valid object name and host name
			if (request.getHostName() == null || request.getObjName() == null) {
				diagnosisResults.add(new CacheEntry(request, response,
						Diagnosis.CACHING_DIAG_INVALID_OBJ_NAME, 0, 
						firstPacket));
				continue;
			}

			// [B] Object cacheable?
			if (response.isNoStore() || request.isNoStore()
					|| HttpRequestResponseInfo.HTTP_POST.equals(requestType)
					|| HttpRequestResponseInfo.HTTP_PUT.equals(requestType)) {
				cacheEntries.remove(getObjFullName(request, response));
				diagnosisResults.add(new CacheEntry(request, response,
						Diagnosis.CACHING_DIAG_NOT_CACHABLE, 0, 
						firstPacket));
				continue;
			}
			// [C] Does it hit the cache?
			CacheEntry cacheEntry = cacheEntries.get(getObjFullName(request, response));
			CacheEntry newCacheEntry;
			if (cacheEntry == null) {
				newCacheEntry = new CacheEntry(request, response,
						Diagnosis.CACHING_DIAG_CACHE_MISSED, 
						firstPacket);
				newCacheEntry.setSession(session);
				addToCache(newCacheEntry,rangeEntries, cacheEntries,session);
				newCacheEntry.setCacheCount(1);
				diagnosisResults.add(newCacheEntry);
				continue;
			}else{
				
				int oldCount=cacheEntry.getCacheCount();
				cacheEntry.setCacheCount(oldCount+1);
				
			}
			
			CacheExpiration expStatus = cacheExpired(cacheEntry,
					request.getAbsTimeStamp());
			
			SortedSet<Range> ranges = rangeEntries.get(getObjFullName(cacheEntry.getRequest(),cacheEntry.getResponse()));
			
			boolean isfullcachehit = isFullCacheHit(response, ranges);
			
			if (isfullcachehit) {
				// [D] Is it expired?
				switch (expStatus) {
				case CACHE_EXPIRED:
				case CACHE_EXPIRED_HEURISTIC:
					newCacheEntry = handleCacheExpired(session, response, request, firstPacket, cacheEntry);
					diagnosisResults.add(newCacheEntry);
					break;
				case CACHE_NOT_EXPIRED:
				case CACHE_NOT_EXPIRED_HEURISTIC:
					
					newCacheEntry = new CacheEntry(request, response,
							Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP, 
							firstPacket);
					diagnosisResults.add(newCacheEntry);
					break;
				default:

					// Should not occur
					newCacheEntry = null;
				}
			} else {
				long bytesInCache = getBytesInCache(response, ranges);

				// [D] Is it expired?
				switch (expStatus) {
				case CACHE_EXPIRED:
				case CACHE_EXPIRED_HEURISTIC:
					newCacheEntry = handleCacheExpiredWithByteInCache(session, response, request, firstPacket, cacheEntry, bytesInCache);
					diagnosisResults.add(newCacheEntry);
					break;
				case CACHE_NOT_EXPIRED:
				case CACHE_NOT_EXPIRED_HEURISTIC:
					
					newCacheEntry = new CacheEntry(
							request,
							response,
							Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT,
							bytesInCache, firstPacket);
					diagnosisResults.add(newCacheEntry);
					break;
				default:
					// Should not occur
					newCacheEntry = null;
				}

			}

			cacheExpirationResponses.get(expStatus).add(newCacheEntry);
			newCacheEntry.setCacheHit(cacheEntry);
			//addToCache(newCacheEntry);
			
		} // END: Iterate through responses looking for duplicates
		
		// Get cache problems
		Set<CacheEntry> dupsWithOrig = new HashSet<CacheEntry>();
		for (CacheEntry cache : diagnosisResults) {
			//flist.append("\n\n + "+cache.getHttpObjectName());
			long bytes = cache.getResponse().getContentLength();
			totalRequestResponseBytes += bytes;
			switch (cache.getDiagnosis()) {
			case CACHING_DIAG_NOT_EXPIRED_DUP:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER:
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT:
			case CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT:
				duplicateContent.add(cache);
				totalRequestResponseDupBytes += bytes;
				dupsWithOrig.add(cache);
				if (cache.getCacheHit() != null) {
					dupsWithOrig.add(cache.getCacheHit());
				}
				break;
			default:
				break;
			}
		}
		
		duplicateContentWithOriginals.addAll(dupsWithOrig);
		
		Collections.sort(duplicateContentWithOriginals);
		duplicateContentBytesRatio = totalRequestResponseBytes != 0 ? (double) totalRequestResponseDupBytes
				/ totalRequestResponseBytes
				: 0.0;
		result.setCacheExpirationResponses(cacheExpirationResponses);
		result.setDiagnosisResults(diagnosisResults);
		result.setDuplicateContent(duplicateContent);
		result.setDuplicateContentBytesRatio(duplicateContentBytesRatio);
		result.setDuplicateContentWithOriginals(duplicateContentWithOriginals);
		result.setTotalRequestResponseBytes(totalRequestResponseBytes);
		result.setTotalRequestResponseDupBytes(totalRequestResponseDupBytes);
		return result;
	}
	CacheEntry handleCacheExpiredWithByteInCache(Session session, 
			HttpRequestResponseInfo response, HttpRequestResponseInfo request, PacketInfo firstPacket, 
			CacheEntry cacheEntry, long bytesInCache){
		CacheEntry newCacheEntry = handleCacheExpiredCommon(session, response, request, firstPacket, cacheEntry);
		if(newCacheEntry == null){
			if (request.isIfModifiedSince()
					|| request.isIfNoneMatch()) {
				newCacheEntry = new CacheEntry(
						request,
						response,
						Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER,
						bytesInCache,
						firstPacket);
			} else {
				newCacheEntry = new CacheEntry(
						request,
						response,
						Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT,
						bytesInCache,
						firstPacket);
			}
		}
		return newCacheEntry;
	}
	CacheEntry handleCacheExpired(Session session, 
			HttpRequestResponseInfo response, HttpRequestResponseInfo request, PacketInfo firstPacket, 
			CacheEntry cacheEntry){
		CacheEntry newCacheEntry = handleCacheExpiredCommon(session, response, request, firstPacket, cacheEntry);
		if(newCacheEntry == null){
			if (request.isIfModifiedSince()
					|| request.isIfNoneMatch()) {
				newCacheEntry = new CacheEntry(
						request,
						response,
						Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER, 
						firstPacket);
			} else {
				newCacheEntry = new CacheEntry(
						request,
						response,
						Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT, 
						firstPacket);
			}
		}
		return newCacheEntry;
	}
	CacheEntry handleCacheExpiredCommon(Session session, 
			HttpRequestResponseInfo response, HttpRequestResponseInfo request, PacketInfo firstPacket, 
			CacheEntry cacheEntry){
		// Check to see if object changed
		HttpRequestResponseInfo cachedResponse = cacheEntry.getResponse();
		boolean isTheSame = rrhelper.isSameContent(response, cachedResponse, session, cacheEntry.getSession());
		CacheEntry newCacheEntry;
		if ((response.getLastModified() != null
				&& cachedResponse.getLastModified() != null && !response
				.getLastModified().equals(
						cachedResponse.getLastModified()))
				|| !isTheSame) {

			newCacheEntry = new CacheEntry(request, response,
					Diagnosis.CACHING_DIAG_OBJ_CHANGED, 
					firstPacket);
		} else if (response.getStatusCode() == 304) {
			newCacheEntry = new CacheEntry(
					request,
					response,
					Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304, 
					firstPacket);
		} else{
			newCacheEntry = null;
		}
		return newCacheEntry;
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
	private boolean isFullCacheHit(
			HttpRequestResponseInfo response, SortedSet<Range> ranges) {
		
		if (ranges != null) {
			for (Range range : ranges) {
				// Here we are looking at the numbers IN THE HEADER instead
				// of ON THE WIRE
				// We assume "Content-Range" in the RESPONSE header match
				// "Range" in the REQUEST
				if (response.getRangeFirst() >= range.getFirstByte()
						&& response.getRangeLast() < range.getLastByte()) {
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
	private long getBytesInCache(
			HttpRequestResponseInfo response, SortedSet<Range> ranges) {

		long xferFirst = response.isRangeResponse() ? response.getRangeFirst()
				: 0;
		long xferLast = xferFirst + response.getRawSize();

		long cachedBytes = 0L;
		for (Range range : ranges) {
			cachedBytes += Math.max(
					0,
					Math.min(xferLast, range.getLastByte())
							- Math.max(xferFirst, range.getFirstByte()));
		}
		return cachedBytes;
	}

	private String getObjFullName(HttpRequestResponseInfo request,
			HttpRequestResponseInfo response) {
		return request.getHostName() + "|" + request.getObjName() + "|"
				+ response.getEtag();
	}

 
	/**
	 * Adds the request and response in cacheEntries after the analysis.
	 * 
	 * @param cacheEntry
	 */
	private void addToCache(CacheEntry cacheEntry, Map<String, SortedSet<Range>> rangeEntries,
			Map<String, CacheEntry> cacheEntries, Session session) {
		HttpRequestResponseInfo response = cacheEntry.getResponse();

		Range range = null;
		long xferSize = calculatePartialTransfer(response, session);
		if (response.isRangeResponse()) {
			long first = response.getRangeFirst();
			long last = xferSize > 0 ? first + xferSize - 1 : response
					.getRangeLast();
			range = new Range(first, last + 1);
		} else if (xferSize > 0) {
			range = new Range(0, xferSize);
		}

		String objFullName = getObjFullName(cacheEntry.getRequest(), cacheEntry.getResponse());
		if (range != null) {
			SortedSet<Range> ranges = rangeEntries.get(objFullName);
			if (ranges != null) {
				ranges.add(range);
				Iterator<Range> iter = ranges.iterator();
				Range last = iter.next();
				while (iter.hasNext()) {
					Range curr = iter.next();
					if (curr.getFirstByte() >= last.getFirstByte()
							&& curr.getFirstByte() <= last.getLastByte()) {
						last.setLastByte( Math.max(last.getLastByte(), curr.getLastByte()));
						iter.remove();
					}
				}
			} else {
				ranges = new TreeSet<Range>();
				ranges.add(range);
				rangeEntries.put(objFullName, ranges);
			}
		} else {
			rangeEntries.remove(objFullName);
		}

		cacheEntries.put(objFullName, cacheEntry);
	}

	/**
	 * by cross checking the content length and the actual byte count partial
	 * transfer is calculated.
	 * 
	 * @param request
	 * @param response
	 * @return -1 for partial transfers else actual response bytes.
	 */
	private long calculatePartialTransfer(
			HttpRequestResponseInfo response, Session session) {
		if (response.isChunked()) {
			return response.isChunkModeFinished() ? 0 : response
					.getContentLength();
		}

		// compute expectedBytes
		int expectedBytes;
		if (response.isRangeResponse()) {
			expectedBytes = response.getRangeLast() - response.getRangeFirst()
					+ 1;
			if (expectedBytes <= 0) {
				expectedBytes = response.getContentLength();
			}
		} else {
			expectedBytes = response.getContentLength();
		}
		if (expectedBytes <= 0) {
			return -1;
		}

		// compute actualTransferred
		long actualBytes = rrhelper.getActualByteCount(response, session);
		if (actualBytes <= 0) {
			return -1;
		}

		if (actualBytes < (int) (expectedBytes * 0.9f)) {
			return actualBytes;
		} else {
			return -1;
		}
	}

}//end class
