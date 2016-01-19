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
package com.att.aro.core.packetanalysis.pojo;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A model of burst collection analysis results.<br>
 * A Burst is a subset of packets from a trace. Determination is defined by a
 * burst threshold.<br>
 * (see. BurstCollectionAnalysisImpl)
 * 
 * <pre>
 *   burstCollection            // A List of Burst objects
 *   totalEnergy                // total Energy
 *   longBurstCount             // long Burst Count
 *   shortestPeriodPacketInfo   // shortest Period PacketInfo
 *   burstAnalysisInfo          // BurstAnalysisInfo List
 * </pre>
 * 
 */
public class BurstCollectionAnalysisData implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * A List of Burst objects
	 */
	private List<Burst> burstCollection;

	/**
	 * total Energy in joules
	 */
	private double totalEnergy;

	/**
	 * A count of long(large) bursts
	 */
	private int longBurstCount = 0;

	/**
	 * The first Packet of a Burst with the shortest periodicity. This
	 * identifies a potential periodic transfer problem.
	 */
	private PacketInfo shortestPeriodPacketInfo = null;

	/**
	 * A List of BurstAnalysisInfo
	 */
	private List<BurstAnalysisInfo> burstAnalysisInfo = null;

	@JsonIgnore
	public List<Burst> getBurstCollection() {
		return burstCollection;
	}

	/**
	 * Sets A List of Burst objects
	 * @param burstCollection - A List of Burst objects
	 */
	public void setBurstCollection(List<Burst> burstCollection) {
		this.burstCollection = burstCollection;
	}

	/**
	 * Returns total Energy in joules
	 * @return total Energy in joules
	 */
	public double getTotalEnergy() {
		return totalEnergy;
	}

	/**
	 * Sets total Energy in joules
	 * @param totalEnergy - total Energy in joules
	 */
	public void setTotalEnergy(double totalEnergy) {
		this.totalEnergy = totalEnergy;
	}

	/**
	 * Returns A count of long(large) bursts
	 * @return a count of long(large) bursts
	 */
	public int getLongBurstCount() {
		return longBurstCount;
	}

	/**
	 * A count of long(large) bursts
	 * @param longBurstCount a count of long(large) bursts
	 */
	public void setLongBurstCount(int longBurstCount) {
		this.longBurstCount = longBurstCount;
	}

	/**
	 * Returns the first Packet of a Burst with the shortest periodicity. 
	 * @return The first Packet of a Burst with the shortest periodicity. 
	 */
	public PacketInfo getShortestPeriodPacketInfo() {
		return shortestPeriodPacketInfo;
	}

	/**
	 * Sets The first Packet of a Burst with the shortest periodicity. 
	 * @param shortestPeriodPacketInfo - The first Packet of a Burst with the shortest periodicity. 
	 */
	public void setShortestPeriodPacketInfo(PacketInfo shortestPeriodPacketInfo) {
		this.shortestPeriodPacketInfo = shortestPeriodPacketInfo;
	}

	/**
	 * Returns  List of BurstAnalysisInfo
	 * @return a List of BurstAnalysisInfo
	 */
	public List<BurstAnalysisInfo> getBurstAnalysisInfo() {
		return burstAnalysisInfo;
	}

	/**
	 * Sets a List of BurstAnalysisInfo
	 * @param burstAnalysisInfo a List of BurstAnalysisInfo
	 */
	public void setBurstAnalysisInfo(List<BurstAnalysisInfo> burstAnalysisInfo) {
		this.burstAnalysisInfo = burstAnalysisInfo;
	}

}
