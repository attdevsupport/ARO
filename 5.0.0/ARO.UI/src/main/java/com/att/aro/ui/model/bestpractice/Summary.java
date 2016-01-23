/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.model.bestpractice;

public class Summary {
	int httpDataAnalyzed = 0;
	int duration = 0;
	int totalDataTransfer = 0;

	public int getHttpDataAnalyzed() {
		return httpDataAnalyzed;
	}

	public void setHttpDataAnalyzed(int httpDataAnalyzed) {
		this.httpDataAnalyzed = httpDataAnalyzed;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getTotalDataTransfer() {
		return totalDataTransfer;
	}

	public void setTotalDataTransfer(int totalDataTransfer) {
		this.totalDataTransfer = totalDataTransfer;
	}

}
