/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.bestpractice.pojo;

import java.util.List;


public class FileCompressionResult extends AbstractBestPracticeResult{
	private List<TextFileCompressionEntry> results;
	private int noOfCompressedFiles;
	private int noOfUncompressedFiles;
	private int totalUncompressedSize;
	private String exportAll;
	private String exportAllKb;
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.FILE_COMPRESSION;
	}
	public List<TextFileCompressionEntry> getResults() {
		return results;
	}
	public void setResults(List<TextFileCompressionEntry> results) {
		this.results = results;
	}
	public int getNoOfCompressedFiles() {
		return noOfCompressedFiles;
	}
	public void setNoOfCompressedFiles(int noOfCompressedFiles) {
		this.noOfCompressedFiles = noOfCompressedFiles;
	}
	public int getNoOfUncompressedFiles() {
		return noOfUncompressedFiles;
	}
	public void setNoOfUncompressedFiles(int noOfUncompressedFiles) {
		this.noOfUncompressedFiles = noOfUncompressedFiles;
	}
	public int getTotalUncompressedSize() {
		return totalUncompressedSize;
	}
	public void setTotalUncompressedSize(int totalUncompressedSize) {
		this.totalUncompressedSize = totalUncompressedSize;
	}
	public String getExportAll() {
		return exportAll;
	}
	public void setExportAll(String exportAll) {
		this.exportAll = exportAll;
	}
	public String getExportAllKb() {
		return exportAllKb;
	}
	public void setExportAllKb(String exportAllKb) {
		this.exportAllKb = exportAllKb;
	}
	
}
