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
package com.att.aro.core.bestpractice.pojo;

import java.util.List;

public class FileOrderResult extends AbstractBestPracticeResult{
	private String textResult;
	private String exportAll;
	private List<FileOrderEntry> results = null;
	private int fileOrderCount = 0;
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.FILE_ORDER;
	}

	public String getTextResult() {
		return textResult;
	}

	public void setTextResult(String textResult) {
		this.textResult = textResult;
	}

	public String getExportAll() {
		return exportAll;
	}

	public void setExportAll(String exportAll) {
		this.exportAll = exportAll;
	}
	/**
	 * Increments the file order count
	 * 
	 */
	public void incrementFileOrderCount() {
		this.fileOrderCount++;
	}

	/**
	 * Returns the file order count
	 * 
	 */
	public int getFileOrderCount() {
		return fileOrderCount;
	}

	/**
	 * Returns an indicator whether the file order test has failed or not.
	 * 
	 * @return failed/success test indicator
	 */
	public boolean isTestFailed() {
		return (getFileOrderCount() > 0);
	}

	/**
	 * Returns a list of file order BP files
	 * 
	 * @return the results
	 */
	public List<FileOrderEntry> getResults() {
		return results;
	}


	public void setResults(List<FileOrderEntry> results) {
		this.results = results;
	}

	public void setFileOrderCount(int fileOrderCount) {
		this.fileOrderCount = fileOrderCount;
	}
	
}
