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

public class PeriodicTransferResult extends AbstractBestPracticeResult {
	private int periodicCount = 0;
	private int diffPeriodicCount = 0;
	private double minimumPeriodicRepeatTime = 0.0;
	private String exportAllIneffConnDesc;
	private String exportAllIneffConnRptDesc;
	private String exportAllIneffConnTimeDesc;
	
	public PeriodicTransferResult(){
		super();
		this.resultType = BPResultType.FAIL;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.PERIODIC_TRANSFER;
	}

	public int getPeriodicCount() {
		return periodicCount;
	}

	public void setPeriodicCount(int periodicCount) {
		this.periodicCount = periodicCount;
	}

	public int getDiffPeriodicCount() {
		return diffPeriodicCount;
	}

	public void setDiffPeriodicCount(int diffPeriodicCount) {
		this.diffPeriodicCount = diffPeriodicCount;
	}

	public double getMinimumPeriodicRepeatTime() {
		return minimumPeriodicRepeatTime;
	}

	public void setMinimumPeriodicRepeatTime(double minimumPeriodicRepeatTime) {
		this.minimumPeriodicRepeatTime = minimumPeriodicRepeatTime;
	}

	public String getExportAllIneffConnDesc() {
		return exportAllIneffConnDesc;
	}

	public void setExportAllIneffConnDesc(String exportAllIneffConnDesc) {
		this.exportAllIneffConnDesc = exportAllIneffConnDesc;
	}

	public String getExportAllIneffConnRptDesc() {
		return exportAllIneffConnRptDesc;
	}

	public void setExportAllIneffConnRptDesc(String exportAllIneffConnRptDesc) {
		this.exportAllIneffConnRptDesc = exportAllIneffConnRptDesc;
	}

	public String getExportAllIneffConnTimeDesc() {
		return exportAllIneffConnTimeDesc;
	}

	public void setExportAllIneffConnTimeDesc(String exportAllIneffConnTimeDesc) {
		this.exportAllIneffConnTimeDesc = exportAllIneffConnTimeDesc;
	}
	
	
	
}
