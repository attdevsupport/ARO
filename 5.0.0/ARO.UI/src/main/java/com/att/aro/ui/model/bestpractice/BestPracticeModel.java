package com.att.aro.ui.model.bestpractice;



/* BestPracticeDisplayGroup
 * Header 
 *   AROBestPracticesPanel.getHeaderPanel()
 * DateTraceAppDetailPanel.getDataPanel()
 * AROBpOverallResulsPanel.createTestStatisticsPanel()
 * TESTS CONDUCTED
 * BestPracticeDisplayGroup.getConnectionsSection()
 * BestPracticeDisplayGroup.getFileDownloadSection()
 * BestPracticeDisplayGroup.getHtmlSection()
 * BestPracticeDisplayGroup.getOtherSection()
 */

/*
 * -header-
 * SUMMARY
 *  TEST STATISTICS
 *  TESTS CONDUCTED
 * AROBpDetailedResultPanel[]
 *  FILE DOWNLOAD
 *  CONNECTIONS
 *  HTML
 *  OTHERS
 */

public class BestPracticeModel {
	
	public Summary summary;
	public BpTestStatistics bpTestStatistics;
	public BpTestsConductedModel bpTestsConductedModel;
	
	
	public Summary getSummary() {
		return summary;
	}

	public void setSummary(Summary summary) {
		this.summary = summary;
	}

	public BpTestStatistics getBpTestStatistics() {
		return bpTestStatistics;
	}

	public void setBpTestStatistics(BpTestStatistics bpTestStatistics) {
		this.bpTestStatistics = bpTestStatistics;
	}

	public BpTestsConductedModel getBpTestsConductedModel() {
		return bpTestsConductedModel;
	}

	public void setBpTestsConductedModel(BpTestsConductedModel bpTestsConductedModel) {
		this.bpTestsConductedModel = bpTestsConductedModel;
	}
	
	
	
}
