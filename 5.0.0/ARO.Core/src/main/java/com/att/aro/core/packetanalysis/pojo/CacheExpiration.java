package com.att.aro.core.packetanalysis.pojo;

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
