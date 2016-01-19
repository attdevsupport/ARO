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

public class ScreenRotationResult extends AbstractBestPracticeResult {

	private double screenRotationBurstTime = 0.0;
	private String exportAllScreenRotationDescPass;
	private String exportAllScreenRotationFailed;
	
	public double getScreenRotationBurstTime() {
		return screenRotationBurstTime;
	}

	public void setScreenRotationBurstTime(double screenRotationBurstTime) {
		this.screenRotationBurstTime = screenRotationBurstTime;
	}

	
	public String getExportAllScreenRotationDescPass() {
		return exportAllScreenRotationDescPass;
	}

	public void setExportAllScreenRotationDescPass(
			String exportAllScreenRotationDescPass) {
		this.exportAllScreenRotationDescPass = exportAllScreenRotationDescPass;
	}

	public String getExportAllScreenRotationFailed() {
		return exportAllScreenRotationFailed;
	}

	public void setExportAllScreenRotationFailed(
			String exportAllScreenRotationFailed) {
		this.exportAllScreenRotationFailed = exportAllScreenRotationFailed;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.SCREEN_ROTATION;
	}

}
