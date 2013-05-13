package com.att.aro.model;

public class FileTypeSummary implements Comparable<FileTypeSummary> {
	private String fileType;
	private long bytes;
	private double pct;

	public FileTypeSummary(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public int compareTo(FileTypeSummary o) {

		// Sort descending
		return -Long.valueOf(bytes).compareTo(o.bytes);
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public double getPct() {
		return pct;
	}

	public void setPct(double pct) {
		this.pct = pct;
	}

}