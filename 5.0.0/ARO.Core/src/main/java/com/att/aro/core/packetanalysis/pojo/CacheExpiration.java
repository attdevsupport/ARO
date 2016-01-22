/**
 * Copyright 2016 AT&T
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
