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

public class WiFiOffloadingResult extends AbstractBestPracticeResult {
	int longBurstCount = 0;
	private String exportAllOffWifiDesc;
	double largestBurstBeginTime = 0.0;
	double largestBurstDuration = 0.0;
	public String getExportAllOffWifiDesc() {
		return exportAllOffWifiDesc;
	}

	public void setExportAllOffWifiDesc(String exportAllOffWifiDesc) {
		this.exportAllOffWifiDesc = exportAllOffWifiDesc;
	}

	public int getLongBurstCount() {
		return longBurstCount;
	}

	public void setLongBurstCount(int longBurstCount) {
		this.longBurstCount = longBurstCount;
	}
	
	
	public double getLargestBurstBeginTime() {
		return largestBurstBeginTime;
	}

	public void setLargestBurstBeginTime(double largestBurstBeginTime) {
		this.largestBurstBeginTime = largestBurstBeginTime;
	}
	

	public double getLargestBurstDuration() {
		return largestBurstDuration;
	}

	public void setLargestBurstDuration(double largestBurstDuration) {
		this.largestBurstDuration = largestBurstDuration;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return null;//BestPracticeType.WIFI_OFFLOADING;
	}

}
