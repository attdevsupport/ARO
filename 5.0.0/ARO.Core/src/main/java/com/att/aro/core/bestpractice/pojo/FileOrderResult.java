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
