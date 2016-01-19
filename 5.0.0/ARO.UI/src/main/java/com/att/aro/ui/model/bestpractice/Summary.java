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
