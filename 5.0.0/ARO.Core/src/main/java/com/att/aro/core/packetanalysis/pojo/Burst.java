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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A bean class that contains the Burst information that appears on the
 * Diagnostics View Chart and the Burst Analysis panel of the Statistics tab.
 */
public class Burst implements Serializable {

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
	private double activeTime;

	// burst analysis
	private BurstCategory burstInfo;

	/**
	 * Initializes an instance of the Burst class, using the specified packet
	 * information.
	 * 
	 * @param packets
	 *            A collection of PacketInfo objects that MUST be sorted by
	 *            time.
	 */
	public Burst(Collection<PacketInfo> packets) {
		if (packets == null || packets.isEmpty()) {
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
	 * @param aBurst
	 *            The Burst object to be merged.
	 */
	public void merge(Burst aBurst) {
		this.packets.addAll(aBurst.packets);
		if (this.beginTime > aBurst.beginTime) {
			this.beginTime = aBurst.beginTime;
			this.beginPacket = aBurst.beginPacket;
		}
		if (this.endTime < aBurst.endTime) {
			this.endTime = aBurst.endTime;
			this.endPacket = aBurst.endPacket;
		}
	}

	/**
	 * Returns all of the packets in this burst.
	 * 
	 * @return A List of PacketInfo objects containing all the packets in this
	 *         burst.
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
	 * @return A booolean value that is true if the burst is a long burst, and
	 *         is false otherwise.
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
	public void setbLong(boolean bLong) {
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
	 * @param energy
	 *            A double that is the amount of energy for this burst.
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
	}

	/**
	 * Returns the amount of RRC active state time for this burst.
	 * 
	 * @return A double that is the RRC active state time.
	 */
	public double getActiveTime() {
		return activeTime;
	}

	/**
	 * Sets the amount of RRC active state time for this burst.
	 * 
	 * @param activeTime
	 *            - A double that is the RRC active state time.
	 */
	public void setActiveTime(double activeTime) {
		this.activeTime = activeTime;
	}

	/**
	 * Returns the burst information contained in this Burst object.
	 * 
	 * @return A BurstInfo objects containing the burst information.
	 */
	public BurstCategory getBurstInfos() {
		return burstInfo;
	}

	/**
	 * Sets a burst information object (BurstInfo).
	 * 
	 * @param burstInfo
	 *            The burst information to set.
	 */
	public void setBurstInfo(BurstCategory burstInfo) {
		this.burstInfo = burstInfo;
	}

	/**
	 * Sets the first uplink data packet for the burst to the specified packet.
	 * 
	 * @param pInfo
	 *            - A PacketInfo object containing the first uplink data packet
	 *            to be set.
	 */
	public void setFirstUplinkDataPacket(PacketInfo pInfo) {
		firstUplinkDataPacket = pInfo;
	}

	/**
	 * Returns the category of the burst.
	 * 
	 * @return A BurstCategory enumeration value that specifies the category of
	 *         the burst.
	 */
	public BurstCategory getBurstCategory() {
		return burstInfo;
	}

	/**
	 * Returns the number of bytes transferred in the Burst.
	 * 
	 * @return A long that is the number of bytes transferred.
	 */
	public long getBurstBytes() {
		long bytes = 0;
		for (PacketInfo pInfo : packets) {
			bytes += pInfo.getPayloadLen();
		}
		return bytes;
	}

	/**
	 * Returns the amount of throughput for this Burst.
	 * 
	 * @return A string that is the amount of throughput.
	 */
	public String getBurstThroughPut() {
		double throughtput = 0;
		//Handling time differences more then one millisecond.
		if ((endTime - beginTime) > 0.001) {
			throughtput = getBurstBytes() * 8 / 1000.0 / (endTime - beginTime);
		} else {
			throughtput = getBurstBytes() * 8 / 1000.0;
		}
		return MessageFormat.format("{0} kbps", throughtput);
	}

	/**
	 * Returns the amount of time elapsed during the burst.
	 * 
	 * @return A double that is the elapsed time, in seconds.
	 */
	public double getElapsedTime() {
		return endTime - beginTime;
	}

}
