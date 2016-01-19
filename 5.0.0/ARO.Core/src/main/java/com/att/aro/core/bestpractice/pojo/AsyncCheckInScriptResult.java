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

public class AsyncCheckInScriptResult extends AbstractBestPracticeResult {
	private int syncPacketCount = 0;
	private int asyncPacketCount = 0;
	private int syncLoadedScripts = 0;
	private int asyncLoadedScripts = 0;
	private List<AsyncCheckEntry> results;
	private String exportAllSyncPacketCount;
	
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.ASYNC_CHECK;
	}
	public int getSyncPacketCount() {
		return syncPacketCount;
	}
	public void setSyncPacketCount(int syncPacketCount) {
		this.syncPacketCount = syncPacketCount;
	}
	public int getAsyncPacketCount() {
		return asyncPacketCount;
	}
	public void setAsyncPacketCount(int asyncPacketCount) {
		this.asyncPacketCount = asyncPacketCount;
	}
	public int getSyncLoadedScripts() {
		return syncLoadedScripts;
	}
	public void setSyncLoadedScripts(int syncLoadedScripts) {
		this.syncLoadedScripts = syncLoadedScripts;
	}
	/**
	 * Returns a list of async loaded files.
	 * 
	 * @return the results
	 */
	public List<AsyncCheckEntry> getResults() {
		return results;
	}
	public void setResults(List<AsyncCheckEntry> results) {
		this.results = results;
	}
	public int getTotalLoadedScripts() {
		return syncLoadedScripts + asyncLoadedScripts;
	}
	/**
	 * Increments the Async loaded scripts
	 * 
	 */
	public void incrementAsyncLoadedScripts() {
		this.asyncLoadedScripts++;
	}

	/**
	 * Increments the sync loaded scripts
	 * 
	 */
	public void incrementSyncLoadedScripts() {
		this.syncLoadedScripts++;
	}
	public int getAsyncLoadedScripts() {
		return asyncLoadedScripts;
	}
	public void setAsyncLoadedScripts(int asyncLoadedScripts) {
		this.asyncLoadedScripts = asyncLoadedScripts;
	}
	/**
	 * Increments the Sync packet count
	 * 
	 */
	public void incrementSyncPacketCount() {
		this.syncPacketCount++;
	}
	public void incrementAsyncPacketCount() {
		this.asyncPacketCount++;
	}
	public String getExportAllSyncPacketCount() {
		return exportAllSyncPacketCount;
	}
	public void setExportAllSyncPacketCount(String exportAllSyncPacketCount) {
		this.exportAllSyncPacketCount = exportAllSyncPacketCount;
	}
	
}
