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

import com.att.aro.core.packetanalysis.pojo.CacheEntry;

public class DuplicateContentResult extends AbstractBestPracticeResult{
	private double duplicateContentBytesRatio = 0.0;
	private int duplicateContentSizeOfUniqueItems = 0;
	private long duplicateContentBytes = 0;
	private long totalContentBytes = 0;
	private int duplicateContentsize = 0;
	private String exportAllPct;
	private String exportAllFiles;
	private String staticsUnitsMbytes;
	private List<CacheEntry> duplicateContentList;
	
	
	public List<CacheEntry> getDuplicateContentList() {
		return duplicateContentList;
	}
	public void setDuplicateContentList(List<CacheEntry> duplicateContentList) {
		this.duplicateContentList = duplicateContentList;
	}
	public String getExportAllPct() {
		return exportAllPct;
	}
	public void setExportAllPct(String exportAllPct) {
		this.exportAllPct = exportAllPct;
	}
	public String getExportAllFiles() {
		return exportAllFiles;
	}
	public void setExportAllFiles(String exportAllFiles) {
		this.exportAllFiles = exportAllFiles;
	}
	public String getStaticsUnitsMbytes() {
		return staticsUnitsMbytes;
	}
	public void setStaticsUnitsMbytes(String staticsUnitsMbytes) {
		this.staticsUnitsMbytes = staticsUnitsMbytes;
	}
	public int getDuplicateContentsize() {
		return duplicateContentsize;
	}
	public void setDuplicateContentsize(int duplicateContentsize) {
		this.duplicateContentsize = duplicateContentsize;
	}
	public double getDuplicateContentBytesRatio() {
		return duplicateContentBytesRatio;
	}
	public void setDuplicateContentBytesRatio(double duplicateContentBytesRatio) {
		this.duplicateContentBytesRatio = duplicateContentBytesRatio;
	}
	public int getDuplicateContentSizeOfUniqueItems() {
		return duplicateContentSizeOfUniqueItems;
	}
	public void setDuplicateContentSizeOfUniqueItems(
			int duplicateContentSizeOfUniqueItems) {
		this.duplicateContentSizeOfUniqueItems = duplicateContentSizeOfUniqueItems;
	}
	public long getDuplicateContentBytes() {
		return duplicateContentBytes;
	}
	public void setDuplicateContentBytes(long duplicateContentBytes) {
		this.duplicateContentBytes = duplicateContentBytes;
	}
	public long getTotalContentBytes() {
		return totalContentBytes;
	}
	public void setTotalContentBytes(long totalContentBytes) {
		this.totalContentBytes = totalContentBytes;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.DUPLICATE_CONTENT;
	}


}
