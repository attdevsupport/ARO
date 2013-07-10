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

public class FileTypeSummary implements Comparable<FileTypeSummary> {
	private String fileType;
	private long bytes;
	private double pct;

	public FileTypeSummary(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public int compareTo(FileTypeSummary o) {

		// Sort descending
		return -Long.valueOf(bytes).compareTo(o.bytes);
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public double getPct() {
		return pct;
	}

	public void setPct(double pct) {
		this.pct = pct;
	}

}