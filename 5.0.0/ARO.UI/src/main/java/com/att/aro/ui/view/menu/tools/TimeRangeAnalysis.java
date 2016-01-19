package com.att.aro.ui.view.menu.tools;

/*
 *  Copyright 2015 AT&T
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

import java.io.Serializable;
import java.util.List;

import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.ui.commonui.ContextAware;

/**
 * Encapsulates Time Range Analysis information.
 */
public class TimeRangeAnalysis implements Serializable {
	private static final long serialVersionUID = 1L;

	private double startTime;
	private double endTime;
	private long totalBytes;
	private long payloadLen; // bytes
	private double activeTime;
	private double energy;

	/**
	 * Initializes an instance of the TimeRangeAnalysis class, using the specified start and 
	 * end times, total number of bytes transferred, payload length, active state time, and energy value. 
	 * 
	 * @param startTime The start of the time range (in seconds). 
	 * @param endTime The end of the time range (in seconds). 
	 * @param totalBytes The total bytes transferred, including all packet headers. 
	 * @param payloadLen The length of the payload in bytes.
	 * @param activeTime The total amount of high energy radio time. 
	 * @param energy The amount of energy used to deliver the payload.
	 */
	public TimeRangeAnalysis(double startTime, double endTime, long totalBytes,
			long payloadLen, double activeTime, double energy) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.totalBytes = totalBytes;
		this.payloadLen = payloadLen;
		this.activeTime = activeTime;
		this.energy = energy;
	}

	/**
	 * Returns the total number of bytes transferred, including packet headers.
	 * @return The total bytes transferred.
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 *  Returns the length of the payload. 
	 * 
	 * @return The payload length, in bytes.
	 */
	public long getPayloadLen() {
		return payloadLen;
	}

	/**
	 * Returns the total amount of time that the radio was in a high energy active state.
	 * 
	 * @return The active time value, in seconds.
	 */
	public double getActiveTime() {
		return activeTime;
	}

	/**
	 * Returns the amount of energy used to deliver the payload. 
	 * 
	 * @return The energy value, in joules.
	 */
	public double getEnergy() {
		return energy;
	}

	/**
	 * Returns the average throughput for the time range
	 * @return The throughput value, in kilobits per second.
	 */
	public double getKbps() {
		return (totalBytes * 8 / 1000) / (endTime - startTime);
	}
	/**
	 * Performs a TimeRangeAnalysis on the trace data.
	 * TODO:  The calculation should not be in the UI - move elsewhere (Core)
	 * 
	 * @return TimeRangeAnalysis The object containing TimeRangeAnalysis
	 *         data.
	 */
	public static TimeRangeAnalysis performTimeRangeAnalysis(PacketAnalyzerResult analysisData, double analyzeBeginTime, double analyzeEndTime) {
		List<RrcStateRange> rrcCollection = analysisData.getStatemachine().getStaterangelist();
		List<PacketInfo> packets = analysisData.getTraceresult().getAllpackets();
		Profile profile = analysisData.getProfile();
		long payloadLength = 0;
		long totalBytes = 0;
		int packetNum = packets.size();

		for (int i = 0; i < packetNum; i++) {
			PacketInfo packetInfo = packets.get(i);
			if (packetInfo.getTimeStamp() >= analyzeBeginTime && packetInfo.getTimeStamp() <= analyzeEndTime) {
				payloadLength += packetInfo.getPayloadLen();
				totalBytes += packetInfo.getLen();
			}
		}

		double energy = 0.0f;
		double activeTime = 0.0f;
		int collectionSize = rrcCollection.size();

		for (int i = 0; i < collectionSize; i++) {
			double beginTime;
			double endTime;

			RrcStateRange rrc = rrcCollection.get(i);
			if (rrc.getEndTime() < analyzeBeginTime) {
				continue;
			}
			if (rrc.getBeginTime() > analyzeEndTime) {
				continue;
			}

			if (rrc.getBeginTime() >= analyzeBeginTime) {
				beginTime = rrc.getBeginTime();
			} else {
				beginTime = analyzeBeginTime;
			}

			if (rrc.getEndTime() <= analyzeEndTime) {
				endTime = rrc.getEndTime();
			} else {
				endTime = analyzeEndTime;
			}

			RRCState rrcState = rrc.getState();
			IProfileFactory profileFactory = ContextAware.getAROConfigContext().getBean(IProfileFactory.class);

			energy += updateEnergy(analysisData, profile, beginTime,
					endTime, rrcState, profileFactory);
			
			activeTime += updateActiveTime(profile, beginTime, endTime, rrcState);
		}

		return new TimeRangeAnalysis(analyzeBeginTime, analyzeEndTime, totalBytes, payloadLength, activeTime, energy);
	}

	// TODO:  The calculation in this method should not be in the UI - move elsewhere
	private static double updateActiveTime(Profile profile, double beginTime, double endTime, RRCState rrcState) {
		double activeTime = 0.0f;
		if (profile.getProfileType() == ProfileType.T3G && (rrcState == RRCState.STATE_DCH || rrcState == RRCState.TAIL_DCH)
				|| profile.getProfileType() == ProfileType.LTE && (rrcState == RRCState.LTE_CONTINUOUS || rrcState == RRCState.LTE_CR_TAIL)
				|| profile.getProfileType() == ProfileType.WIFI && (rrcState == RRCState.WIFI_ACTIVE || rrcState == RRCState.WIFI_TAIL)) {
			activeTime = endTime - beginTime;
		}
		return activeTime;
	}

	// TODO:  Move this separation of calls by profile type elsewhere - not just the UI
	private static double updateEnergy(PacketAnalyzerResult analysisData,
			Profile profile, double beginTime, double endTime,
			RRCState rrcState, IProfileFactory profileFactory) {
		double energy = 0.0f;

		if (profile.getProfileType().equals(ProfileType.T3G)) {
			energy += profileFactory.energy3G(beginTime, endTime, rrcState, (Profile3G)profile);
		} else if (profile.getProfileType().equals(ProfileType.LTE)) {
			energy += profileFactory.energyLTE(beginTime, endTime, rrcState, (ProfileLTE)profile, analysisData.getTraceresult().getAllpackets());
		} else if (profile.getProfileType().equals(ProfileType.WIFI)) {
			energy += profileFactory.energyWiFi(beginTime, endTime, rrcState, (ProfileWiFi)profile);
		}
		return energy;
	}
}
