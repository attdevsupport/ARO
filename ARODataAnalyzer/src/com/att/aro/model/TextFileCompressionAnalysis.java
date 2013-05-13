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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents Text File Compression Analysis
 *
 */
public class TextFileCompressionAnalysis {

	private static final Logger LOGGER = Logger.getLogger(TextFileCompressionAnalysis.class.getName());

	private static final double ALLOWED_TO_FAIL = 5.0;

	private static final double PERCENT_100 = 100.0;

	private static final int KILO = 1024;

	private List<TextFileCompressionEntry> results = new ArrayList<TextFileCompressionEntry>();
	private int noOfCompressedFiles;
	private int noOfUncompressedFiles;
	private int totalSize;

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
	 * Returns percentage number of uncompressed text files.
	 * 
	 * @return percentage number of uncompressed text files
	 */
	public int getPercentage() {
		double percentage = 0;
		double total = noOfCompressedFiles + noOfUncompressedFiles;
		if (total > 0) {
			percentage = (noOfUncompressedFiles * PERCENT_100) / total;
		}
		LOGGER.log(Level.FINE, "Uncomp.: {0}, Compr.: {1}, %: {2}", new Object[] { noOfUncompressedFiles, noOfCompressedFiles, percentage });
		return (int)percentage;
	}

	/**
	 * Returns an indicator whether the text file compression test has failed or not.
	 * 
	 * @return failed/success test indicator
	 */
	public boolean isTestFailed() {
		return (getPercentage() >= ALLOWED_TO_FAIL);
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
	 * Get total size of all uncompressed files in kilobytes.
	 * 
	 * @return the total size
	 */
	public int getTotalSize() {
		return totalSize / KILO;
	}

	/**
	 * Add to the total size of all uncompressed files in kilobytes.
	 * 
	 * @param the size to be added
	 */
	public void addToTotalSize(int size) {
		this.totalSize += size;
	}
	
}
