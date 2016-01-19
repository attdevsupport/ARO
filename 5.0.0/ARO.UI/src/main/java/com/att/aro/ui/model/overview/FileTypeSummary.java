/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.model.overview;


/**
 * @author Harikrishna Yaramachu
 *
 */
public class FileTypeSummary implements Comparable<FileTypeSummary>{

	private String fileType;
	private long bytes;
	private double pct;

	public FileTypeSummary(String fileType) {
		this.fileType = fileType;
	}
	
	/**
	 * @return the fileType
	 */
	public String getFileType() {
		return fileType;
	}
	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	/**
	 * @return the matchedBytes
	 */
	public long getBytes() {
		return bytes;
	}
	/**
	 * @param matchedBytes the matchedBytes to set
	 */
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	/**
	 * @return the pct
	 */
	public double getPct() {
		return pct;
	}
	/**
	 * @param pct the pct to set
	 */
	public void setPct(double pct) {
		this.pct = pct;
	}
	
	@Override
	public int compareTo(FileTypeSummary arg0) {
		// Sort descending
		return -Long.valueOf(bytes).compareTo(arg0.bytes);
	}
	
}
