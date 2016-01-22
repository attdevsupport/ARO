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
