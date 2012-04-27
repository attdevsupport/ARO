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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import com.att.aro.main.ResourceBundleManager;

/**
 * A bean class that contains the Burst information that appears on the Diagnostics View 
 * Chart and the Burst Analysis panel of the Statistics tab.
 */
public class Burst implements Serializable {

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle(); // Resource
																						// Bundle

	private static final long serialVersionUID = 1L;

	private double beginTime; // <--- updated by what-if
	private double endTime; // <--- updated by what-if
	private List<PacketInfo> packets;
	private PacketInfo beginPacket;
	private PacketInfo endPacket;
	private PacketInfo firstUplinkDataPacket;
	private boolean bLong; // ibt > th2?

	// energy
	private double energy;
	private double dchTime;
	private double crTime;

	// burst analysis
	private Set<BurstInfo> burstInfos = new HashSet<BurstInfo>();

	/**
	 * Initializes an instance of the Burst class, using the specified packet information.
	 * 
	 * @param packets – A collection of PacketInfo objects that MUST be sorted by time.
	 */
	public Burst(Collection<PacketInfo> packets) {
		if (packets == null || packets.size() == 0) {
			throw new IllegalArgumentException("Burst must have packets in it");
		}
		this.packets = new ArrayList<PacketInfo>(packets);
		this.beginPacket = this.packets.get(0);
		this.beginTime = this.beginPacket.getTimeStamp();
		this.endPacket = this.packets.get(this.packets.size() - 1);
		this.endTime = this.endPacket.getTimeStamp();
		this.firstUplinkDataPacket = null;
	}

	/**
	 * Merges the specified burst into this burst. 
	 * 
	 * @param b – The Burst object to be merged.
	 */
	public synchronized void merge(Burst b) {
		this.packets.addAll(b.packets);
		if (this.beginTime > b.beginTime) {
			this.beginTime = b.beginTime;
			this.beginPacket = b.beginPacket;
		}
		if (this.endTime < b.endTime) {
			this.endTime = b.endTime;
			this.endPacket = b.endPacket;
		}
	}

	/**
	 * Returns all of the packets in this burst. 
	 * 
	 * @return A List of PacketInfo objects containing all the packets in this burst.
	 */
	public List<PacketInfo> getPackets() {
		return packets;
	}

	/**
	 * Returns the beginTime of the burst. 
	 * 
	 * @return A double that is the beginTime of the burst.
	 */
	public double getBeginTime() {
		return beginTime;
	}

	/**
	 * Returns the ending time of the burst. 
	 * 
	 * @return A double that is the ending time.
	 */
	public double getEndTime() {
		return endTime;
	}

	/**
	 * Returns the beginning Packet of the burst. 
	 * 
	 * @return A PacketInfo object that is the beginning Packet.
	 */
	public PacketInfo getBeginPacket() {
		return beginPacket;
	}

	/**
	 * Returns the ending Packet of the burst. 
	 * 
	 * @return A PacketInfo object that is the ending Packet.
	 */
	public PacketInfo getEndPacket() {
		return endPacket;
	}

	/**
	 * Returns the first uplink data packet of the burst. 
	 * 
	 * @return A PacketInfo object that is the first uplink data packet.
	 */
	public PacketInfo getFirstUplinkDataPacket() {
		return firstUplinkDataPacket;
	}

	/**
	 * Returns a value that indicates whether the burst is a long burst.
	 * 
	 * @return A booolean value that is true if the burst is a long burst, and is false 
	 * otherwise.
	 */
	public boolean isbLong() {
		return bLong;
	}

	/**
	 * Sets bLong state of burst.
	 * 
	 * @param bLong
	 *            the bLong to set
	 */
	void setbLong(boolean bLong) {
		this.bLong = bLong;
	}

	/**
	 * Returns the amount of energy used by the burst. 
	 * 
	 * @return A double that is the amount of burst energy.
	 */
	public double getEnergy() {
		return energy;
	}

	/**
	 * Sets the amount energy for this burst to the specified value. 
	 * 
	 * @param energy – A double that is the amount of energy for this burst.
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
	}

	/**
	 * Returns the amount of DCH active time for this burst. 
	 * 
	 * @return A double that is the DCH active time.
	 */
	public double getDchTime() {
		return dchTime;
	}

	/**
	 * Sets the amount of DCH active time for this burst. 
	 * 
	 * @param dchTime - A double that is the DCH active time.
	 */
	public void setDchTime(double dchTime) {
		this.dchTime = dchTime;
	}
	
	/**
	 * Returns burst DCH time.
	 * 
	 * @return dchTime.
	 */
	public double getCrTime() {
		return crTime;
	}

	/**
	 * Sets burst DCH time.
	 * 
	 * @param dchTime
	 *            .
	 */
	public void setCrTime(double crTime) {
		this.crTime = crTime;
	}

	
	/**
	 * Returns the Set of burst information contained in this Burst object. 
	 * 
	 * @return A Set of BurstInfo objects containing the burst information.
	 */
	public Set<BurstInfo> getBurstInfos() {
		return Collections.unmodifiableSet(burstInfos);
	}

	/**
	 * Adds a burst information object (BurstInfo) into a list. 
	 * 
	 * @param burstInfo – The burst information to add.
	 */
	public void addBurstInfo(BurstInfo burstInfo) {
		this.burstInfos.add(burstInfo);
	}

	/**
	 * Sets the burst information for this burst, using the specified BurstInfo object. 
	 * 
	 * @param burstInfo - A BurstInfo object containing the burst information to be set.
	 */
	public void setBurstInfo(BurstInfo burstInfo) {
		this.burstInfos = new HashSet<BurstInfo>(Arrays.asList(burstInfo));
	}

	/**
	 * Sets the first uplink data packet for the burst to the specified packet. 
	 * 
	 * @param p - A PacketInfo object containing the first uplink data packet to be set.
	 */
	public void setFirstUplinkDataPacket(PacketInfo p) {
		firstUplinkDataPacket = p;
	}

	/**
	 * Returns the category of the burst. 
	 * 
	 * @return A BurstCategory enumeration value that specifies the category of the burst.
	 */
	public BurstCategory getBurstCategory() {
		boolean multipleCategories = (burstInfos.size() > 1);

		if (burstInfos.contains(BurstInfo.BURST_LOSS_RECOVER)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_LOSS;
		}
		if (burstInfos.contains(BurstInfo.BURST_LOSS_DUP)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_LOSS;
		}
		if (burstInfos.contains(BurstInfo.BURST_USER_INPUT)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_USER;
		}
		if (burstInfos.contains(BurstInfo.BURST_SERVER_DELAY)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_SERVER;
		}
		if (burstInfos.contains(BurstInfo.BURST_CLIENT_DELAY)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_CLIENT;
		}
		if (burstInfos.contains(BurstInfo.BURST_PERIODICAL)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_PERIODICAL;
		}
		if (burstInfos.contains(BurstInfo.BURST_UNKNOWN)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_UNKNOWN;
		}
		if (burstInfos.contains(BurstInfo.BURST_BKG)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_BKG;
		}
		if (burstInfos.contains(BurstInfo.BURST_LONG)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_LONG;
		}

		if (burstInfos.contains(BurstInfo.BURST_USERDEF1)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_USERDEF1;
		}
		if (burstInfos.contains(BurstInfo.BURST_USERDEF2)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_USERDEF2;
		}
		if (burstInfos.contains(BurstInfo.BURST_USERDEF3)) {
			assert multipleCategories : rb.getString("burst.categoryWarning");
			return BurstCategory.BURSTCAT_USERDEF3;
		}
		return BurstCategory.BURSTCAT_PROTOCOL;
	}

}
