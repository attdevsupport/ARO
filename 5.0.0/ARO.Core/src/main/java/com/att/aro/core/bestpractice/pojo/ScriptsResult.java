/**
 *  Copyright 2016 AT&T
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


public class ScriptsResult extends AbstractBestPracticeResult {
	private int numberOfFailedFiles = 0;
	private HttpRequestResponseInfo firstFailedHtml;
	private String exportAllNumberOfScriptsFiles;
	
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.SCRIPTS_URL;
	}
	public int getNumberOfFailedFiles() {
		return numberOfFailedFiles;
	}
	public void setNumberOfFailedFiles(int numberOfFailedFiles) {
		this.numberOfFailedFiles = numberOfFailedFiles;
	}
	public void incrementNumberOfFailedFiles(){
		this.numberOfFailedFiles++;
	}
	public HttpRequestResponseInfo getFirstFailedHtml() {
		return firstFailedHtml;
	}
	public void setFirstFailedHtml(HttpRequestResponseInfo firstFailedHtml) {
		this.firstFailedHtml = firstFailedHtml;
	}
	public String getExportAllNumberOfScriptsFiles() {
		return exportAllNumberOfScriptsFiles;
	}
	public void setExportAllNumberOfScriptsFiles(
			String exportAllNumberOfScriptsFiles) {
		this.exportAllNumberOfScriptsFiles = exportAllNumberOfScriptsFiles;
	}
	
}
