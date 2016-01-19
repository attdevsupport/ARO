package com.att.aro.core.packetanalysis.pojo;

/**
 * The CacheEntry.Diagnosis Enumeration specifies constant values that describe the 
 * diagnosis of a cache entry that was not cached correctly. This enumeration is part 
 * of the CacheEntry class.
 */
public enum Diagnosis {
	/**
	 * The cache entry missed the cache. 
	 */
	CACHING_DIAG_CACHE_MISSED,
	/**
	 * The cache entry contains data that is not cacheable. 
	 */
	CACHING_DIAG_NOT_CACHABLE,
	/**
	 * The cache entry contains data that is not expired. 
	 */
	CACHING_DIAG_NOT_EXPIRED_DUP,
	/**
	 * The cache entry contains an object that is changed. 
	 */
	CACHING_DIAG_OBJ_CHANGED,
	/**
	 * The cache entry contains an object that is not changed. 
	 */
	CACHING_DIAG_OBJ_NOT_CHANGED_304,
	/**
	 * The cache entry contains a Server object that is not changed or duplicate. 
	 */
	CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER,
	/**
	 * The cache entry contains a Client object that is not changed or duplicate. 
	 */
	CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT,
	/**
	 * The cache entry contains a Server object that is not changed, duplicate, or a 
	 * partial hit. 
	 */
	CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER,
	/**
	 * The cache entry contains a Client object that is not changed, duplicate, or a 
	 * partial hit. 
	 */
	CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT,
	/**
	 * The cache entry is not expired, duplicate, or a partial hit. 
	 */
	CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT,
	/**
	 * The cache entry contains an invalid request. 
	 */
	CACHING_DIAG_INVALID_REQUEST,
	/**
	 * The cache entry contains an invalid object name. 
	 */
	CACHING_DIAG_INVALID_OBJ_NAME,
	/**
	 * The cache entry contains an invalid response. 
	 */
	CACHING_DIAG_INVALID_RESPONSE,
	/**
	 * The cache entry contains a Request that was not found.
	 */
	CACHING_DIAG_REQUEST_NOT_FOUND
}
