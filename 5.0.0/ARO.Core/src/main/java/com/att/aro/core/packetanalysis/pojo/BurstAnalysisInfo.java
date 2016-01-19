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

/**
 * A bean class containing the Burst information that is analyzed from the packets in the 
 * trace data.
 */
public class BurstAnalysisInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private BurstCategory category;
	private long payload;
	private double payloadPct;
	private double energy;
	private double energyPct;
	private double rrcActiveTime;
	private double rrcActiveTimePct;
	private Double jpkb;
	
	/**
	 * Initializes an instance of the BurstAnalysisInfo class, using the specified parameters.
	 * 
	 * @param category
	 * 
	 * @param payload The burst payload.
	 * 
	 * @param payloadPct The burst payload percentage.
	 * 
	 * @param energy The burst energy.
	 * 
	 * @param energyPct The burst energy percentage.
	 * 
	 * @param rrcActiveTime The RRC active time for the burst.
	 * 
	 * @param rrcActiveTimePct The RRC active percentage for the burst.
	 * 
	 * @param jpkb The amount of energy used by the burst in joules per kilobyte.
	 */
	public BurstAnalysisInfo(BurstCategory category, long payload, double payloadPct,
			double energy, double energyPct, double rrcActiveTime, double rrcActiveTimePct, Double jpkb) {
		this.category = category;
		this.payload = payload;
		this.payloadPct = payloadPct;
		this.energy = energy;
		this.energyPct = energyPct;
		this.rrcActiveTime = rrcActiveTime;
		this.rrcActiveTimePct = rrcActiveTimePct;
		this.jpkb = jpkb;
	}

	/**
	 * Returns the category of the burst.
	 * 
	 * @return A BurstCategory enumeration value that specifies the category of the burst.
	 */
	public BurstCategory getCategory() {
		return category;
	}

	/**
	 * Returns the payload length, which is the data length of packets (not including 
	 * headers) that occurred during the burst. 
	 * 
	 * @return The payload length of the burst.
	 */
	public long getPayload() {
		return payload;
	}

	/**
	 * Returns the payload percentage, which is the percentage of total payload (the data 
	 * length of all burst payloads in the trace) used by this burst. 
	 * 
	 * @return The burst payload percentage.
	 */
	public double getPayloadPct() {
		return payloadPct;
	}

	/**
	 * Returns the burst energy. 
	 * 
	 * @return A double that is energy used by the burst.
	 */
	public double getEnergy() {
		return energy;
	}

	/**
	 * Returns the burst energy percentage which is the percentage of total burst energy 
	 * used by this burst. 
	 * 
	 * @return The burst energy percentage.
	 */
	public double getEnergyPct() {
		return energyPct;
	}

	/**
	 * Returns the amount of RRC active state time for the burst. 
	 * 
	 * @return The RRC active state time for the burst.
	 */
	public double getRRCActiveTime() {
		return rrcActiveTime;
	}

	/**
	 * Returns the RRC active state  percentage which is the percentage of total RRC active Time used by this burst.
	 * 
	 * @return The RRC active percentage for the burst.
	 */
	public double getRRCActivePercentage() {
		return rrcActiveTimePct;
	}
	
	/**
	 * Returns the energy used by the burst in joules per kilobyte. 
	 * 
	 * @return The burst energy in joules per kilobyte.
	 */
	public Double getJpkb() {
		return jpkb;
	}

}

