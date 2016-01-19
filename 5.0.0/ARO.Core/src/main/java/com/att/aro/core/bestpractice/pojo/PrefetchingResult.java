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

public class PrefetchingResult extends AbstractBestPracticeResult {
	private int userInputBurstCount = 0;
	private int burstCategoryCount = 0;
	private String exportAllPrefetchDesc;
	
	public int getUserInputBurstCount() {
		return userInputBurstCount;
	}


	public void setUserInputBurstCount(int userInputBurstCount) {
		this.userInputBurstCount = userInputBurstCount;
	}

	
	public int getBurstCategoryCount() {
		return burstCategoryCount;
	}


	public void setBurstCategoryCount(int burstCategoryCount) {
		this.burstCategoryCount = burstCategoryCount;
	}
	

	public String getExportAllPrefetchDesc() {
		return exportAllPrefetchDesc;
	}


	public void setExportAllPrefetchDesc(String exportAllPrefetchDesc) {
		this.exportAllPrefetchDesc = exportAllPrefetchDesc;
	}


	@Override
	public BestPracticeType getBestPracticeType() {
		return null; //BestPracticeType.PREFETCHING;
	}

}
