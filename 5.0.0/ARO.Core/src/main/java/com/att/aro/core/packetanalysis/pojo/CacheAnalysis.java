/*
 Copyright 2014 AT&T
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.att.aro.core.packetanalysis.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


/**
 * CacheAnalysis models the result of a Cashe Analysis (see. CacheAnalysisImpl)
 * <pre>
 *  totalRequestResponseBytes           // total Request Response Bytes
 *  totalRequestResponseDupBytes        // total Request Response Duplicate Bytes
 *  duplicateContentBytesRatio          // duplicate Content Bytes Ratio
 *  cacheExpirationResponses            // cache Expiration Responses
 *  diagnosisResults                    // diagnosis Results
 *  duplicateContent                    // duplicate Content
 *  duplicateContentWithOriginals       // duplicate Content With Originals
 * </pre>
 * 
 */
public class CacheAnalysis implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The total number of bytes in the TCP Requests and Responses.
	 */
	private long totalRequestResponseBytes;
	
	/**
	 * The total number of bytes that are a result of duplicate content.
	 */
	private long totalRequestResponseDupBytes;
	
	/**
	 * The ratio of the number of bytes to the total bytes, that were sent for duplicate content
	 * .
	 */
	private double duplicateContentBytesRatio;
	
	/**
	 * The cache expiration response data that consists of a mapping of cache 
	 * entries with cache expiration values.
	 */
	private Map<CacheExpiration, List<CacheEntry>> cacheExpirationResponses = new EnumMap<CacheExpiration, List<CacheEntry>>(
			CacheExpiration.class);
	
	/**
	 * The diagnosis results. 
	 */
	private List<CacheEntry> diagnosisResults = new ArrayList<CacheEntry>();
	
	/**
	 * The list of duplicate content cache entries. 
	 */
	private List<CacheEntry> duplicateContent = new ArrayList<CacheEntry>();
	
	/**
	 * The duplicate content cache entries along with the original cache entries. 
	 */
	private List<CacheEntry> duplicateContentWithOriginals = new ArrayList<CacheEntry>();
	
	/**
	 * Returns the diagnosis results. 
	 * 
	 * @return A List of CacheEntry objects that contain the diagnosis results.
	 */
	public List<CacheEntry> getDiagnosisResults() {
		return diagnosisResults;
	}

	/**
	 * Returns the list of duplicate content cache entries. 
	 * 
	 * @return A List of CacheEntry object that contain the duplicate content.
	 */
	public List<CacheEntry> getDuplicateContent() {
		return duplicateContent;
	}

	/**
	 * Returns the duplicate content cache entries along with the original cache entries. 
	 * 
	 * @return duplicateContentWithOriginals.
	 */
	public List<CacheEntry> getDuplicateContentWithOriginals() {
		return duplicateContentWithOriginals;
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
		return cacheExpirationResponses;
	}

	/**
	 * Sets the total number of bytes in the TCP Requests and Responses.
	 * @param totalRequestResponseBytes - The total number of bytes in the TCP Requests and Responses.
	 */
	public void setTotalRequestResponseBytes(long totalRequestResponseBytes) {
		this.totalRequestResponseBytes = totalRequestResponseBytes;
	}

	/**
	 * Sets the total number of bytes that are a result of duplicate content.
	 * @param totalRequestResponseDupBytes - The total number of bytes that are a result of duplicate content.
	 */
	public void setTotalRequestResponseDupBytes(long totalRequestResponseDupBytes) {
		this.totalRequestResponseDupBytes = totalRequestResponseDupBytes;
	}

	/**
	 * Sets the ratio of the number of bytes to the total bytes, that were sent for duplicate content .
	 * @param duplicateContentBytesRatio - The ratio of the number of bytes to the total bytes, that were sent for duplicate content .
	 */
	public void setDuplicateContentBytesRatio(double duplicateContentBytesRatio) {
		this.duplicateContentBytesRatio = duplicateContentBytesRatio;
	}

	/**
	 * Sets the cache expiration response data that consists of a mapping of cache entries with cache expiration values.
	 * @param cacheExpirationResponses - The cache expiration response data that consists of a mapping of cache entries with cache expiration values.
	 */
	public void setCacheExpirationResponses(
			Map<CacheExpiration, List<CacheEntry>> cacheExpirationResponses) {
		this.cacheExpirationResponses = cacheExpirationResponses;
	}

	/**
	 * Sets the diagnosis results.
	 * @param diagnosisResults - The diagnosis results.
	 */
	public void setDiagnosisResults(List<CacheEntry> diagnosisResults) {
		this.diagnosisResults = diagnosisResults;
	}

	/**
	 * Sets the list of duplicate content cache entries.
	 * @param duplicateContent - The list of duplicate content cache entries.
	 */
	public void setDuplicateContent(List<CacheEntry> duplicateContent) {
		this.duplicateContent = duplicateContent;
	}

	/**
	 * Sets the duplicate content cache entries along with the original cache entries.
	 * @param duplicateContentWithOriginals - List of duplicate plus originals
	 */
	public void setDuplicateContentWithOriginals(
			List<CacheEntry> duplicateContentWithOriginals) {
		this.duplicateContentWithOriginals = duplicateContentWithOriginals;
	}
	
}
