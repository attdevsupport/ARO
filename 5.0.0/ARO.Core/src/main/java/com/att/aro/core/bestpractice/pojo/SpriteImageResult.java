package com.att.aro.core.bestpractice.pojo;

import java.util.List;

public class SpriteImageResult extends AbstractBestPracticeResult {
	private List<SpriteImageEntry> analysisResults = null;
	private String exportAllNumberOfSpriteFiles;
	
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.SPRITEIMAGE;
	}
	public List<SpriteImageEntry> getAnalysisResults() {
		return analysisResults;
	}
	public void setAnalysisResults(List<SpriteImageEntry> analysisResults) {
		this.analysisResults = analysisResults;
	}
	public String getExportAllNumberOfSpriteFiles() {
		return exportAllNumberOfSpriteFiles;
	}
	public void setExportAllNumberOfSpriteFiles(String exportAllNumberOfSpriteFiles) {
		this.exportAllNumberOfSpriteFiles = exportAllNumberOfSpriteFiles;
	}
	
}
