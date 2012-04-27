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
 * Class used to manage data throughput calculations
 */
public class Throughput implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a list of throughput calculations for the time range and 
	 * sampling window
	 * @param startTime Start time to begin throughput calculations
	 * @param time End time for throughput calculations
	 * @param maxTS The sampling window for each throughput point
	 * @param packets List of packets to calculate throughput on.  This method
	 * assumes these packets are sorted by timestamp.  Results are undefined
	 * for unsorted packet list.
	 * @return
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

			int nSteps = (int) (maxTS / thStep);
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
					endTS = thStep * i;
					beginTS = endTS - thBin;
					if (beginTS < 0)
						continue;

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
	 * @return the sample time
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @return the kbps
	 */
	public double getKbps() {
		return (uploadBytes + downloadBytes) * 8 / 1000.0 / samplePeriod;
	}

	/**
	 * @return the upload kbps
	 */
	public double getUploadKbps() {
		return uploadBytes * 8 / 1000.0 / samplePeriod;
	}

	/**
	 * @return the download kbps
	 */
	public double getDownloadKbps() {
		return downloadBytes * 8 / 1000.0 / samplePeriod;
	}

	/**
	 * @return the mbps
	 */
	public double getMbps() {
		return getKbps() / 1000.0;
	}

	/**
	 * @return the upload mbps
	 */
	public double getUploadMbps() {
		return getUploadKbps() / 1000.0;
	}

	/**
	 * @return the download mbps
	 */
	public double getDownloadMbps() {
		return getDownloadKbps() / 1000.0;
	}

	/**
	 * @return the samplePeriod
	 */
	public double getSamplePeriod() {
		return samplePeriod;
	}

}
