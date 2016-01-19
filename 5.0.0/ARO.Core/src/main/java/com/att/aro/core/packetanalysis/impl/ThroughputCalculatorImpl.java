/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.packetanalysis.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.att.aro.core.packetanalysis.IThroughputCalculator;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Throughput;
import com.att.aro.core.packetreader.pojo.PacketDirection;

public class ThroughputCalculatorImpl implements IThroughputCalculator {

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
	@Override
	public List<Throughput> calculateThroughput(double startTime, double maxTS,
			double thStep, List<PacketInfo> packets) {
		List<Throughput> result = new ArrayList<Throughput>();

		// Amount of time used in sample for throughput calc
		final double thBin = thStep;

		// Build data set
		if (!packets.isEmpty() ) {

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
							
							if(head.getDir() == PacketDirection.UPLINK){
								headUpAccum += head.getLen();
							}else if(head.getDir() == PacketDirection.DOWNLINK){
								headDownAccum += head.getLen();
							}
						}
						head = headIter.hasNext() ? headIter.next() : null;
					}
					while (tail != null && tail.getTimeStamp() < endTS) {
						if (tail.getDir() != null) {
							
							if(tail.getDir() == PacketDirection.UPLINK){
								tailUpAccum += tail.getLen();
							}else if(tail.getDir() == PacketDirection.DOWNLINK){
								tailDownAccum += tail.getLen();
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
							
							if(head.getDir() == PacketDirection.UPLINK){
								headUpAccum += head.getLen();
							}else if(head.getDir() == PacketDirection.DOWNLINK){
								headDownAccum += head.getLen();
							}
						}
						head = headIter.hasNext() ? headIter.next() : null;
					}
					while (tail != null && tail.getTimeStamp() < maxTS) {
						if (tail.getDir() != null) {
							
							if(tail.getDir() == PacketDirection.UPLINK){
								tailUpAccum += tail.getLen();
							}else if(tail.getDir() == PacketDirection.DOWNLINK){
								tailDownAccum += tail.getLen();
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

}
