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

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

public class EmptyUrlResult extends AbstractBestPracticeResult {
	private HttpRequestResponseInfo firstFailedHtml = null;
	private int numberOfFailedFiles = 0;
	private String exportAllEmptyUrlFiles;
	
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.EMPTY_URL;
	}
	public HttpRequestResponseInfo getFirstFailedHtml() {
		return firstFailedHtml;
	}
	public void setFirstFailedHtml(HttpRequestResponseInfo firstFailedHtml) {
		this.firstFailedHtml = firstFailedHtml;
	}
	public int getNumberOfFailedFiles() {
		return numberOfFailedFiles;
	}
	public void setNumberOfFailedFiles(int numberOfFailedFiles) {
		this.numberOfFailedFiles = numberOfFailedFiles;
	}
	public String getExportAllEmptyUrlFiles() {
		return exportAllEmptyUrlFiles;
	}
	public void setExportAllEmptyUrlFiles(String exportAllEmptyUrlFiles) {
		this.exportAllEmptyUrlFiles = exportAllEmptyUrlFiles;
	}
	

}
