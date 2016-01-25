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

import java.util.List;
import java.util.Map;

/**
 * Parse cache information from the provided {@link CacheAnalysis} Object and
 * retains in its member variables.
 */
public class CacheInfoParser {

	private int cacheable = 0;
	private long cacheableBytes = 0;
	private int notCacheable = 0;
	private long notCacheableBytes = 0;
	private int expired = 0;
	private long expiredBytes = 0;
	private int notExpired = 0;
	private long notExpiredBytes = 0;
	private int expiredHeur = 0;
	private long expiredHeurBytes = 0;
	private int notExpiredHeur = 0;
	private long notExpiredHeurBytes = 0;
	private int cacheMiss = 0;
	private long cacheMissBytes = 0;
	private int hitNotExpiredDup = 0;
	private long hitNotExpiredDupBytes = 0;
	private int hitResponseChanged = 0;
	private long hitResponseChangedBytes = 0;
	private int hitExpiredDupClient = 0;
	private long hitExpiredDupClientBytes = 0;
	private int hitExpiredDupServer = 0;
	private long hitExpiredDupServerBytes = 0;
	private int hitExpired304 = 0;
	private long hitExpired304Bytes = 0;
	private int partialHitExpiredDupClient = 0;
	private long partialHitExpiredDupClientBytes = 0;
	private int partialHitExpiredDupServer = 0;
	private long partialHitExpiredDupServerBytes = 0;
	private int partialHitNotExpiredDup = 0;
	private long partialHitNotExpiredDupBytes = 0;

	/**
	 * Initializes the instance of class CacheInfoParser and starts parse
	 * operation.
	 * 
	 * @param cacheAnalysis
	 */
	public CacheInfoParser(CacheAnalysis cacheAnalysis) {
		parseCacheContent(cacheAnalysis);
	}

	/**
	 * Parse the cache content.
	 * 
	 * @param cacheAnalysis
	 */
	public void parseCacheContent(CacheAnalysis cacheAnalysis) {
		for (CacheEntry entry : cacheAnalysis.getDiagnosisResults()) {
			long bytes = entry.getBytesInCache();
			switch (entry.getDiagnosis()) {
			case CACHING_DIAG_REQUEST_NOT_FOUND:
			case CACHING_DIAG_INVALID_OBJ_NAME:
			case CACHING_DIAG_INVALID_REQUEST:
			case CACHING_DIAG_INVALID_RESPONSE:
				break;
			case CACHING_DIAG_CACHE_MISSED:
				++cacheMiss;
				cacheMissBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_NOT_CACHABLE:
				++notCacheable;
				notCacheableBytes += entry.getBytesNotInCache();
				break;
			case CACHING_DIAG_NOT_EXPIRED_DUP:
				++hitNotExpiredDup;
				hitNotExpiredDupBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT:
				++hitExpiredDupClient;
				hitExpiredDupClientBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER:
				++hitExpiredDupServer;
				hitExpiredDupServerBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_OBJ_CHANGED:
				++hitResponseChanged;
				hitResponseChangedBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_OBJ_NOT_CHANGED_304:
				++hitExpired304;
				hitExpired304Bytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT:
				++partialHitExpiredDupClient;
				partialHitExpiredDupClientBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER:
				++partialHitExpiredDupServer;
				partialHitExpiredDupServerBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			case CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT:
				++partialHitNotExpiredDup;
				partialHitNotExpiredDupBytes += bytes;
				++cacheable;
				cacheableBytes += bytes;
				break;
			}
		}

		// Categorize expiration types on responses
		Map<CacheAnalysis.CacheExpiration, List<CacheEntry>> expirations = cacheAnalysis
				.getCacheExpirationResponses();
		for (Map.Entry<CacheAnalysis.CacheExpiration, List<CacheEntry>> entry : expirations
				.entrySet()) {
			int count = 0;
			int bytes = 0;
			for (CacheEntry cacheEntry : entry.getValue()) {
				++count;
				bytes += cacheEntry.getBytesInCache();
			}
			switch (entry.getKey()) {
			case CACHE_EXPIRED:
				expired = count;
				expiredBytes = bytes;
				break;
			case CACHE_EXPIRED_HEURISTIC:
				expiredHeur = count;
				expiredHeurBytes = bytes;
				break;
			case CACHE_NOT_EXPIRED:
				notExpired = count;
				notExpiredBytes = bytes;
				break;
			case CACHE_NOT_EXPIRED_HEURISTIC:
				notExpiredHeur = count;
				notExpiredHeurBytes = bytes;
				break;
			}
		}
	}

	/**
	 * Returns cacheable value.
	 * 
	 * @return int cacheable.
	 */
	public int getCacheable() {
		return cacheable;
	}

	/**
	 * Returns cacheableBytes value.
	 * 
	 * @return long cacheableBytes.
	 */
	public long getCacheableBytes() {
		return cacheableBytes;
	}

	/**
	 * Returns notCacheable value.
	 * 
	 * @return int notCacheable.
	 */
	public int getNotCacheable() {
		return notCacheable;
	}

	/**
	 * Returns notCacheableBytes value.
	 * 
	 * @return long notCacheableBytes.
	 */
	public long getNotCacheableBytes() {
		return notCacheableBytes;
	}

	/**
	 * Returns expired value.
	 * 
	 * @return int expired.
	 */
	public int getExpired() {
		return expired;
	}

	/**
	 * Returns expiredBytes value.
	 * 
	 * @return long expiredBytes.
	 */
	public long getExpiredBytes() {
		return expiredBytes;
	}

	/**
	 * Returns notExpired value.
	 * 
	 * @return int notExpired.
	 */
	public int getNotExpired() {
		return notExpired;
	}

	/**
	 * Returns notExpiredBytes value.
	 * 
	 * @return long notExpiredBytes.
	 */
	public long getNotExpiredBytes() {
		return notExpiredBytes;
	}

	/**
	 * Returns expiredHeur value.
	 * 
	 * @return int expiredHeur.
	 */
	public int getExpiredHeur() {
		return expiredHeur;
	}

	/**
	 * Returns expiredHeurBytes value.
	 * 
	 * @return long expiredHeurBytes.
	 */
	public long getExpiredHeurBytes() {
		return expiredHeurBytes;
	}

	/**
	 * Returns notExpiredHeur value.
	 * 
	 * @return int notExpiredHeur.
	 */
	public int getNotExpiredHeur() {
		return notExpiredHeur;
	}

	/**
	 * Returns notExpiredHeurBytes value.
	 * 
	 * @return long notExpiredHeurBytes.
	 */
	public long getNotExpiredHeurBytes() {
		return notExpiredHeurBytes;
	}

	/**
	 * Returns cacheMiss value.
	 * 
	 * @return int cacheMiss.
	 */
	public int getCacheMiss() {
		return cacheMiss;
	}

	/**
	 * Returns cacheMissBytes value.
	 * 
	 * @return long cacheMissBytes.
	 */
	public long getCacheMissBytes() {
		return cacheMissBytes;
	}

	/**
	 * Returns hitNotExpiredDup value.
	 * 
	 * @return int hitNotExpiredDup.
	 */
	public int getHitNotExpiredDup() {
		return hitNotExpiredDup;
	}

	/**
	 * Returns hitNotExpiredDupBytes value.
	 * 
	 * @return long hitNotExpiredDupBytes.
	 */
	public long getHitNotExpiredDupBytes() {
		return hitNotExpiredDupBytes;
	}

	/**
	 * Returns hitResponseChanged value.
	 * 
	 * @return int hitResponseChanged.
	 */
	public int getHitResponseChanged() {
		return hitResponseChanged;
	}

	/**
	 * Returns hitResponseChangedBytes value.
	 * 
	 * @return long hitResponseChangedBytes.
	 */
	public long getHitResponseChangedBytes() {
		return hitResponseChangedBytes;
	}

	/**
	 * Returns hitExpiredDupClient value.
	 * 
	 * @return int hitExpiredDupClient.
	 */
	public int getHitExpiredDupClient() {
		return hitExpiredDupClient;
	}

	/**
	 * Returns hitExpiredDupClientBytes value.
	 * 
	 * @return long hitExpiredDupClientBytes.
	 */
	public long getHitExpiredDupClientBytes() {
		return hitExpiredDupClientBytes;
	}

	/**
	 * Returns hitExpiredDupServer value.
	 * 
	 * @return int hitExpiredDupServer.
	 */
	public int getHitExpiredDupServer() {
		return hitExpiredDupServer;
	}

	/**
	 * Returns hitExpiredDupServerBytes value.
	 * 
	 * @return long hitExpiredDupServerBytes.
	 */
	public long getHitExpiredDupServerBytes() {
		return hitExpiredDupServerBytes;
	}

	/**
	 * Returns hitExpired304 value.
	 * 
	 * @return int hitExpired304.
	 */
	public int getHitExpired304() {
		return hitExpired304;
	}

	/**
	 * Returns hitExpired304Bytes value.
	 * 
	 * @return long hitExpired304Bytes.
	 */
	public long getHitExpired304Bytes() {
		return hitExpired304Bytes;
	}

	/**
	 * Returns partialHitExpiredDupClient value.
	 * 
	 * @return int partialHitExpiredDupClient.
	 */
	public int getPartialHitExpiredDupClient() {
		return partialHitExpiredDupClient;
	}

	/**
	 * Returns partialHitExpiredDupClientBytes value.
	 * 
	 * @return long partialHitExpiredDupClientBytes.
	 */
	public long getPartialHitExpiredDupClientBytes() {
		return partialHitExpiredDupClientBytes;
	}

	/**
	 * Returns partialHitExpiredDupServer value.
	 * 
	 * @return int partialHitExpiredDupServer.
	 */
	public int getPartialHitExpiredDupServer() {
		return partialHitExpiredDupServer;
	}

	/**
	 * Returns partialHitExpiredDupServerBytes value.
	 * 
	 * @return long partialHitExpiredDupServerBytes.
	 */
	public long getPartialHitExpiredDupServerBytes() {
		return partialHitExpiredDupServerBytes;
	}

	/**
	 * Returns partialHitNotExpiredDup value.
	 * 
	 * @return int partialHitNotExpiredDup.
	 */
	public int getPartialHitNotExpiredDup() {
		return partialHitNotExpiredDup;
	}

	/**
	 * Returns partialHitNotExpiredDupBytes value.
	 * 
	 * @return long partialHitNotExpiredDupBytes.
	 */
	public long getPartialHitNotExpiredDupBytes() {
		return partialHitNotExpiredDupBytes;
	}

}
