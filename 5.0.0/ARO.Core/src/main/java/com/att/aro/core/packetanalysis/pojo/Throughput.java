package com.att.aro.core.packetanalysis.pojo;

import java.io.Serializable;

public class Throughput implements Serializable {
	private static final long serialVersionUID = 1L;
	private double samplePeriod;
	private double time;
	private long uploadBytes;
	private long downloadBytes;
	
	public Throughput(double startTime, double endTime, long uploadBytes, long downloadBytes) {
		this.samplePeriod = endTime - startTime;
		this.time = endTime;
		this.uploadBytes = uploadBytes;
		this.downloadBytes = downloadBytes;
	}

	/**
	 * Returns the sample time.
	 * @return The sample time (in seconds).
	 */
	public double getTime() {
		return time;
	}

	/**
	 * The throughput, in kilobits per second (Kbps).
	 * @return  The throughput (in Kbps).
	 */
	public double getKbps() {
		return (uploadBytes + downloadBytes) * 8 / 1000.0 / samplePeriod;
	}

	/**
	 * Returns the upload rate, in kilobits per second (Kbps).
	 * @return The upload rate (in kbps).
	 */
	public double getUploadKbps() {
		return uploadBytes * 8 / 1000.0 / samplePeriod;
	}

	/**
	 * Returns the download rate, in kilobits per second (Kbps).
	 * @return The download rate (in Kbps).
	 */
	public double getDownloadKbps() {
		return downloadBytes * 8 / 1000.0 / samplePeriod;
	}

	/**
	 * The throughput, in megabits per second (Mbps).
	 * @return The throughput (in Mbps).
	 */
	public double getMbps() {
		return getKbps() / 1000.0;
	}

	/**
	 * Returns the upload rate, in megabits per second (Mbps).
	 * @return The upload rate (in Mbps).
	 */
	public double getUploadMbps() {
		return getUploadKbps() / 1000.0;
	}

	/**
	 * Returns the download rate, in megabits per second (Mbps).
	 * @return The download rate (in Mbps).
	 */
	public double getDownloadMbps() {
		return getDownloadKbps() / 1000.0;
	}

	/**
	 * Returns the sample period for the throughput calculation.
	 * @return the The sample period (in seconds).
	 */
	public double getSamplePeriod() {
		return samplePeriod;
	}
	
}
