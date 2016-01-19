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

public class UnnecessaryConnectionResult extends AbstractBestPracticeResult{
	private int tightlyCoupledBurstCount = 0;
	private double tightlyCoupledBurstTime = 0;
	private String exportAllMultiConnDesc;
	private List<UnnecessaryConnectionEntry> tightlyCoupledBurstsDetails;
	
	public int getTightlyCoupledBurstCount() {
		return tightlyCoupledBurstCount;
	}

	public void setTightlyCoupledBurstCount(int tightlyCoupledBurstCount) {
		this.tightlyCoupledBurstCount = tightlyCoupledBurstCount;
	}

	public double getTightlyCoupledBurstTime() {
		return tightlyCoupledBurstTime;
	}

	public void setTightlyCoupledBurstTime(double tightlyCoupledBurstTime) {
		this.tightlyCoupledBurstTime = tightlyCoupledBurstTime;
	}

	public String getExportAllMultiConnDesc() {
		return exportAllMultiConnDesc;
	}

	public void setExportAllMultiConnDesc(String exportAllMultiConnDesc) {
		this.exportAllMultiConnDesc = exportAllMultiConnDesc;
	}

	/**
	 * @return the tightlyCoupledBurstsDetails
	 */
	public List<UnnecessaryConnectionEntry> getTightlyCoupledBurstsDetails() {
		return tightlyCoupledBurstsDetails;
	}

	/**
	 * @param tightlyCoupledBurstsDetails the tightlyCoupledBurstsDetails to set
	 */
	public void setTightlyCoupledBurstsDetails(
			List<UnnecessaryConnectionEntry> tightlyCoupledBurstsDetails) {
		this.tightlyCoupledBurstsDetails = tightlyCoupledBurstsDetails;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.UNNECESSARY_CONNECTIONS;
	}

}
