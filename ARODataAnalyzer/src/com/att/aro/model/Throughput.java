/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates methods for calculating data throughput.
 */
public class Throughput implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a list of throughput calculations for the specified time range,  
	 * sampling window, and list of packets. 
	 * @param startTime The starting time in the trace to begin throughput calculations.
	 * @param maxTS The sampling window for each throughput point.
	 * @param thStep The ending time in the trace for throughput calculations.
	 * @param packets A List of packets to calculate throughput on. This method assumes that 
	 * these packets are sorted by timestamp. The results of this method are undefined for an unsorted packet list.
	 *  
	 * @return A List of Throughput objects containing the results of the calculations.
	 */
	public static List<Throughput> calculateThroughput(double startTime,
			double maxTS, double thStep, List<PacketInfo> packets) {

		List<Throughput> result = new ArrayList<Throughput>();

		// Amount of time used in sample for throughput calc
		final double thBin = thStep;

		// Build data set
		if (packets.size() > 0) {

			Iterator<PacketInfo> headIter = packets.iterator();
			Iterator<PacketInfo> tailIter = packets.iterator();

			int nSteps = (int) ((maxTS - startTime) / thStep);
			long headUpAccum = 0;
			long tailUpAccum = 0;
			long headDownAccum = 0;
			long tailDownAccum = 0;
			if (headIter.hasNext() && tailIter.hasNext()) {

				PacketInfo head = headIter.next();
				PacketInfo tail = tailIter.next();
				double beginTS;
				double endTS = startTime;
				for (int i = 1; i <= nSteps; i++) {
					// Set up time slot
					endTS += thStep;
					beginTS = endTS - thBin;
					if (beginTS < startTime) {
						continue;
					}

					// Determine the number of bytes downloaded in the
					// current
					// slot
					while (head != null && head.getTimeStamp() < beginTS) {
						if (head.getDir() != null) {
							switch (head.getDir()) {
							case UPLINK :
								headUpAccum += head.getLen();
								break;
							case DOWNLINK :
								headDownAccum += head.getLen();
								break;
							}
						}
						head = headIter.hasNext() ? headIter.next() : null;
					}
					while (tail != null && tail.getTimeStamp() < endTS) {
						if (tail.getDir() != null) {
							switch (tail.getDir()) {
							case UPLINK :
								tailUpAccum += tail.getLen();
								break;
							case DOWNLINK :
								tailDownAccum += tail.getLen();
								break;
							}
						}
						tail = tailIter.hasNext() ? tailIter.next() : null;
					}

					// Add slot to data set
					result.add(new Throughput(beginTS, endTS, tailUpAccum
							- headUpAccum, tailDownAccum - headDownAccum));
				}
				
				// Add an entry for leftover bin
				if (maxTS > endTS) {
					beginTS = (maxTS - thBin) + (endTS + thStep - maxTS);
					
					while (head != null && head.getTimeStamp() < beginTS) {
						if (head.getDir() != null) {
							switch (head.getDir()) {
							case UPLINK :
								headUpAccum += head.getLen();
								break;
							case DOWNLINK :
								headDownAccum += head.getLen();
								break;
							}
						}
						head = headIter.hasNext() ? headIter.next() : null;
					}
					while (tail != null && tail.getTimeStamp() < maxTS) {
						if (tail.getDir() != null) {
							switch (tail.getDir()) {
							case UPLINK :
								tailUpAccum += tail.getLen();
								break;
							case DOWNLINK :
								tailDownAccum += tail.getLen();
								break;
							}
						}
						tail = tailIter.hasNext() ? tailIter.next() : null;
					}
					
					// Add slot to data set
					result.add(new Throughput(beginTS, maxTS, tailUpAccum
							- headUpAccum, tailDownAccum - headDownAccum));
				}
			}
		}
		return result;
	}

	private double samplePeriod;
	private double time;
	private long uploadBytes;
	private long downloadBytes;
	
	private Throughput(double startTime, double endTime, long uploadBytes, long downloadBytes) {
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
