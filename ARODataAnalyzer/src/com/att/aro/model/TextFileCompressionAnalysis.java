/*
 *  Copyright 2013 AT&T
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents Text File Compression Analysis
 *
 */
public class TextFileCompressionAnalysis {

	private static final int KILO = 1024;
	
	public static final int FILE_SIZE_THRESHOLD_BYTES = 850;
	
	public static final int UNCOMPRESSED_SIZE_FAILED_THRESHOLD_KB = 100;

	private List<TextFileCompressionEntry> results = new ArrayList<TextFileCompressionEntry>();
	private int noOfCompressedFiles;
	private int noOfUncompressedFiles;
	private int totalUncompressedSize;
	
	public enum TextCompressionAnalysisResult {PASS, WARNING, FAIL};

	/** 
	 * Performs Text File Compression Analysis
	 * 
	 * @param tcpSessions
	 * 				- TCP session to be analyzed.
	 */
	public TextFileCompressionAnalysis(List<TCPSession> tcpSessions) {

		for (TCPSession tcpSession : tcpSessions) {
			for (HttpRequestResponseInfo rr : tcpSession.getRequestResponseInfo()) {
				// if the http payload should be compressed but is not
				if (rr.setHttpCompression(this)) {
					results.add(new TextFileCompressionEntry(rr));
				}
			}
		}
	}


	/**
	 * Returns an indicator whether the text file compression test has failed or not.
	 * Failed if total uncompressed size >= UNCOMPRESSED_SIZE_FAILED_THRESHOLD
	 * 
	 * @return failed/success test indicator
	 */
	private boolean isTestFailed() {
		return getTotalUncompressedSize() >= UNCOMPRESSED_SIZE_FAILED_THRESHOLD_KB;
	}

	/**
	 * Test passes if there is no uncompressed file exceeding the size threshold
	 * @return
	 */
	private boolean isTestPassed(){
		return getNoOfUncompressedFiles() == 0;
	}
	
	public TextCompressionAnalysisResult getTextCompressionAnalysisResult(){
		if (isTestPassed()){
			return TextCompressionAnalysisResult.PASS;
		}
		
		if (isTestFailed()){
			return TextCompressionAnalysisResult.FAIL;
		}
		
		return TextCompressionAnalysisResult.WARNING;
	}
	
	
	/**
	 * Returns a list of uncompressed text files.
	 * 
	 * @return the results
	 */
	public List<TextFileCompressionEntry> getResults() {
		return results;
	}

	/**
	 * Returns the number of compressed text files.
	 * 
	 * @return the the number of compressed text files
	 */
	public int getNoOfCompressedFiles() {
		return noOfCompressedFiles;
	}

	/**
	 * Increment the number of compressed text files.
	 * 
	 */
	public void incrementNoOfCompressedFiles() {
		this.noOfCompressedFiles++;
	}

	/**
	 * Returns the number of uncompressed text files.
	 * 
	 * @return the the number of uncompressed text files
	 */
	public int getNoOfUncompressedFiles() {
		return noOfUncompressedFiles;
	}

	/**
	 * Increment the number of uncompressed text files.
	 */
	public void incrementNoOfUncompressedFiles() {
		this.noOfUncompressedFiles++;
	}

	/**
	 * Get total size in KB of all uncompressed files that are more than 850 bytes.
	 * Note that we just return the total size since we had only incremented this when
	 * the uncompressed file is greater than 850 bytes (in HttpRequestResponseInfo.setHttpCompression)
	 * 
	 * @return the total size
	 */
	public int getTotalUncompressedSize() {
		return totalUncompressedSize / KILO;
	}

	/**
	 * Add to the total size of all uncompressed files in kilobytes.
	 * 
	 * @param the size to be added
	 */
	public void addToTotalUncompressedSize(int size) {
		this.totalUncompressedSize += size;
	}
	
}
