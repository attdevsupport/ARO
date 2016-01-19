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
