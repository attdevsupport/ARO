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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the results of an RRC state machine analysis.
 * 
 */
public class RRCStateMachine implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(RRCStateMachine.class.getName());

	private double idleTime;
	private double dchTime;
	private double dchTailTime;
	private double fachTime;
	private double fachTailTime;

	private double lteIdleTime;
	private double lteIdleToCRPromotionTime;
	private double lteCrTime;
	private double lteCrTailTime;
	private double lteDrxShortTime;
	private double lteDrxLongTime;

	private int idleToDch;
	private int fachToDch;

	private double idleToDchTime;
	private double fachToDchTime;

	private double idleEnergy;
	private double dchEnergy;
	private double fachEnergy;
	private double idleToDchEnergy;
	private double fachToDchEnergy;
	private double dchTailEnergy;
	private double fachTailEnergy;
	private double totalRRCEnergy;
	private double joulesPerKilobyte;

	private double lteIdleEnergy;
	private double lteIdleToCRPromotionEnergy;
	private double lteCrEnergy;
	private double lteCrTailEnergy;
	private double lteDrxShortEnergy;
	private double lteDrxLongEnergy;

	private double packetsDuration;
	private double traceDuration;

	private double crPower;
	
	private double wifiActiveEnergy;
	private double wifiTailEnergy;
	private double wifiIdleEnergy;
    
	private double wifiActiveTime;
	private double wifiTailTime;
	private double wifiIdleTime;
	
	private List<RrcStateRange> rrc = new ArrayList<RrcStateRange>();

	/**
	 * Initializes an instance of the RrcStateMachine class using the specified analysisData.
	 * 
	 * @param analysisData
	 *            The trace analysis data.
	 */
	public RRCStateMachine(TraceData.Analysis analysisData) {
        
		TimeRange timeRange = analysisData.getFilter().getTimeRange();
		if(timeRange == null){
		this.rrc = RrcStateRange.runTrace(analysisData);
		}else{
			this.rrc = getRRCStatesForTheTimeRange(RrcStateRange.runTrace(analysisData) , timeRange.getBeginTime() , timeRange.getEndTime());
		}
		if (logger.isLoggable(Level.FINER)) {
			for (RrcStateRange r : rrc) {
				logger.finer(r.getState()
						+ ": "
						+ (r.getEndTime() - r.getBeginTime())
						+ " "
						+ analysisData.getProfile().energy(r.getBeginTime(), r.getEndTime(),
								r.getState(), analysisData.getPackets()));
			}
		}

		this.packetsDuration = analysisData.getPacketsDuration();
		this.traceDuration = analysisData.getTraceData().getTraceDuration();

		final Profile profile = analysisData.getProfile();

		if (profile instanceof Profile3G) {
			// Perform analysis on 3G RRC data
			run3GRRcStatistics(analysisData);
		} else if (profile instanceof ProfileLTE) {
			// Perform analysis on LTE RRC data
			runLTERRcStatistics(analysisData);
		}else if (profile instanceof ProfileWiFi) {
			// Perform analysis on LTE RRC data
			runWiFiRRcStatistics(analysisData);
		}

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("===== RRC State Machine Simulation =====");
			logger.fine("CELL_DCH: " + getDchTime() + "(" + (getDchTimeRatio() * 100) + "%)");
			logger.fine("CELL_FACH: " + getFachTime() + "(" + (getFachTimeRatio() * 100) + "%)");
			logger.fine("IDLE: " + getIdleTime() + "(" + (getIdleTimeRatio() * 100) + "%)");
			logger.fine("IDLE->DCH: " + getIdleToDchTime() + "(" + (getIdleToDchTimeRatio() * 100)
					+ "%)");
			logger.fine("FACH->DCH: " + getFachToDchTime() + "(" + (getFachToDchTimeRatio() * 100)
					+ "%)");
			logger.fine("DCH Tail Ratio = : " + getDchTailRatio());
			logger.fine("FACH Tail Ratio = : " + getFachTailRatio());
			logger.fine("Promotion Ratio = : " + getPromotionRatio());
			logger.fine("");
		}
	}
	
	private List<RrcStateRange> getRRCStatesForTheTimeRange(List<RrcStateRange> rrcRanges , double beginTime , double endTime){
		
		List<RrcStateRange> filteredRRCStates = new ArrayList<RrcStateRange>();
		boolean stateAdded = false;

		for (RrcStateRange rrcRange : rrcRanges) {

			if (rrcRange.getBeginTime() >= beginTime
					&& rrcRange.getEndTime() <= endTime) {
				filteredRRCStates.add(rrcRange);
			} else if (rrcRange.getBeginTime() <= beginTime
					&& rrcRange.getEndTime() <= endTime && rrcRange.getEndTime() > beginTime) {
				filteredRRCStates.add(new RrcStateRange(beginTime, rrcRange
						.getEndTime(), rrcRange.getState()));
			} else if (rrcRange.getBeginTime() <= beginTime
					&& rrcRange.getEndTime() >= endTime) {
				filteredRRCStates.add(new RrcStateRange(beginTime, endTime,
						rrcRange.getState()));
			} else if (rrcRange.getBeginTime() >= beginTime && rrcRange.getBeginTime() < endTime
					&& rrcRange.getEndTime() >= endTime && !stateAdded) {
				filteredRRCStates.add(new RrcStateRange(rrcRange
						.getBeginTime(), endTime, rrcRange
						.getState()));
				stateAdded = true;
			}
		}
		return filteredRRCStates;
		
	}

	/**
	 * Returns a list of all RRC state ranges. 
	 * 
	 * @return A List of RRCStateRange objects.
	 */
	public List<RrcStateRange> getRRcStateRanges() {
		return Collections.unmodifiableList(rrc);
	}

	/**
	 * Returns the total amount of time spent in the Direct channel (DCH) state.
	 * 
	 * @return The total DCH time value.
	 */
	public double getDchTime() {
		return dchTime;
	}

	/**
	 * Returns the ratio of total DCH time to the total trace duration.
	 * 
	 * @return The DCH time ratio value.
	 */
	public double getDchTimeRatio() {
		return traceDuration != 0.0 ? dchTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total amount of time spent in the forward access channel (FACH) state.
	 * 
	 * @return The FACH time value.
	 */
	public double getFachTime() {
		return fachTime;
	}

	/**
	 * Returns the ratio of total FACH time to the total trace duration. 
	 * 
	 * @return The FACH time ratio value.
	 */
	public double getFachTimeRatio() {
		return traceDuration != 0.0 ? fachTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total amount of time spent in the IDLE state.
	 * 
	 * @return The IDLE time value.
	 */
	public double getIdleTime() {
		return idleTime;
	}

	/**
	 * Returns the ratio of total IDLE state time to the total trace duration.
	 * 
	 * @return  The IDLE time ratio value.
	 */
	public double getIdleTimeRatio() {
		return traceDuration != 0.0 ? idleTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total amount of time spent while the radio state was promoted from IDLE to DCH.
	 * 
	 * @return The IDLE to DCH time value.
	 */
	public double getIdleToDchTime() {
		return idleToDchTime;
	}

	/**
	 * Returns the ratio of total IDLE to DCH time, to the trace duration.
	 * 
	 * @return The IDLE to DCH time ratio value.
	 */
	public double getIdleToDchTimeRatio() {
		return traceDuration != 0.0 ? idleToDchTime / traceDuration : 0.0;
	}

	/**
	 * Returns a count of the number of times that the radio state was promoted from IDLE to DCH. 
	 * 
	 * @return The IDLE to DCH count.
	 */
	public int getIdleToDchCount() {
		return idleToDch;
	}

	/**
	 * Returns the amount of time spent while the radio state was promoted from FACH to DCH.
	 * 
	 * @return The FACH to DCH time value.
	 */
	public double getFachToDchTime() {
		return fachToDchTime;
	}

	/**
	 * Returns the ratio of total FACH to DCH time, to the trace duration.
	 * 
	 * @return The FACH to DCH time ratio value.
	 */
	public double getFachToDchTimeRatio() {
		return traceDuration != 0.0 ? fachToDchTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total DCH tail time.
	 * 
	 * @return The DCH tail time value.
	 */
	public double getDchTailTime() {
		return dchTailTime;
	}

	/**
	 * Returns the ratio of total DCH tail time to the total trace duration.
	 * 
	 * @return The DCH tail ratio value.
	 */
	public double getDchTailRatio() {
		return dchTime != 0.0 ? dchTailTime / dchTime : 0.0;
	}

	/**
	 * Returns a count of the number of times that the radio state was promoted from FACH to DCH.
	 * 
	 * @return The FACH to DCH count.
	 */
	public int getFachToDchCount() {
		return fachToDch;
	}

	/**
	 * Returns the total amount of time spent in the FACH tail state. 
	 * 
	 * @return The FACH tail time value.
	 */
	public double getFachTailTime() {
		return fachTailTime;
	}

	/**
	 * Returns the ratio of total FACH tail time to the total trace duration. 
	 * 
	 * @return The FACH tail ratio value.
	 */
	public double getFachTailRatio() {
		return fachTime != 0.0 ? fachTailTime / fachTime : 0.0;
	}

	/**
	 * Returns the ratio of the total amount of promotion time to the trace duration. 
	 * 
	 * @return The total promotion ratio value.
	 */
	public double getPromotionRatio() {
		return packetsDuration != 0.0 ? (idleToDchTime + fachToDchTime) / packetsDuration : 0.0;
	}

	/**
	 * Returns the total amount of energy expended sending packet data in the trace. 
	 * 
	 * @return The total RRC energy value.
	 */
	public double getTotalRRCEnergy() {
		return totalRRCEnergy;
	}

	/**
	 * Returns the average amount of Joules per Kilobyte for the trace.
	 * 
	 * @return The average Joules per Kilobyte value.
	 */
	public double getJoulesPerKilobyte() {
		return joulesPerKilobyte;
	}

	/**
	 * Returns the total amount of energy expended in the IDLE state.
	 * 
	 * @return The IDLE energy value.
	 */
	public double getIdleEnergy() {
		return idleEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the DCH state.
	 * 
	 * @return The DCH energy value.
	 */
	public double getDchEnergy() {
		return dchEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the FACH state.
	 * 
	 * @return The FACH energy value.
	 */
	public double getFachEnergy() {
		return fachEnergy;
	}

	/**
	 * Returns the total amount of energy expended when the radio state was promoted from IDLE to DCH.
	 * 
	 * @return The IDLE to DCH energy value.
	 */
	public double getIdleToDchEnergy() {
		return idleToDchEnergy;
	}

	/**
	 * Returns the total amount of energy expended when the radio state was promoted from FACH to DCH.
	 * 
	 * @return The FACH to DCH energy value.
	 */
	public double getFachToDchEnergy() {
		return fachToDchEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the DCH tail state.
	 * 
	 * @return The DCH tail energy value.
	 */
	public double getDchTailEnergy() {
		return dchTailEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the FACH tail state.
	 * 
	 * @return The FACH tail energy value.
	 */
	public double getFachTailEnergy() {
		return fachTailEnergy;
	}

	/**
	 * Returns the total amount of time spent in the LTE IDLE state.
	 * 
	 * @return The LTE IDLE time value.
	 */
	public double getLteIdleTime() {
		return lteIdleTime;
	}

	/**
	 * Returns the ratio of total LTE IDLE state time to the trace duration.
	 * 
	 * @return The LTE IDLE time ratio value.
	 */
	public double getLteIdleTimeRatio() {
		return traceDuration != 0.0 ? lteIdleTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total amount of time spent while the radio state was promoted from LTE IDLE to LTE CR.
	 * 
	 * @return The LTE IDLE to LTE CR promotion time value.
	 */
	public double getLteIdleToCRPromotionTime() {
		return lteIdleToCRPromotionTime;
	}

	/**
	 * Returns the ratio of total LTE IDLE to LTE CR promotion time, to the trace duration.
	 * 
	 * @return The LTE IDLE to LTE CR promotion time ratio value.
	 */
	public double getLteIdleToCRPromotionTimeRatio() {
		return traceDuration != 0.0 ? lteIdleToCRPromotionTime / traceDuration : 0.0;
	}
	
	/**
	 * Returns the CR promotion time ratio corresponding to trace duration.
	 * 
	 * @return the Promotion Ratio/Signaling overhead
	 */
	public double getCRPromotionRatio() {
		return packetsDuration != 0.0 ? lteIdleToCRPromotionTime / packetsDuration : 0.0;
	}

	/**
	 * Returns the total amount of time spent in the LTE CR state.
	 * 
	 * @return The LTE CR time value.
	 */
	public double getLteCrTime() {
		return lteCrTime;
	}

	/**
	 * Returns the ratio of total CR state time to the trace duration.
	 * 
	 * @return The LTE CR time ratio value.
	 */
	public double getLteCrTimeRatio() {
		return traceDuration != 0.0 ? lteCrTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total amount of time spent in the CR tail state.
	 * 
	 * @return The LTE CR tail time value.
	 */
	public double getLteCrTailTime() {
		return lteCrTailTime;
	}

	/**
	 * Returns the ratio of total CR tail state time to the trace duration.
	 * 
	 * @return The LTE CR tail time ratio value.
	 */
	public double getLteCrTailTimeRatio() {
		return traceDuration != 0.0 ? lteCrTailTime / traceDuration : 0.0;
	}
	
	/**
	 * Returns the total CR tail time ratio corresponding to trace duration.
	 * 
	 * @return the DCH Tail Ratio
	 */
	public double getCRTailRatio() {
		return lteCrTime != 0.0 ? lteCrTailTime / lteCrTime : 0.0;
	}

	/**
	 * Returns the amount of time spent in the LTE DRX Short state.
	 * 
	 * @return The LTE DRX Short time value.
	 */
	public double getLteDrxShortTime() {
		return lteDrxShortTime;
	}

	/**
	 * Returns the ratio of total LTE DRX Short state time to the total duration.
	 * 
	 * @return The LTE DRX Short time value.
	 */
	public double getLteDrxShortTimeRatio() {
		return traceDuration != 0.0 ? lteDrxShortTime / traceDuration : 0.0;
	}
	
	/**
	 * Returns the LTE DRX Short period time Ratio.
	 * 
	 * @return lteDrxShortTimeRatio
	 */
	public double getLteDrxShortRatio() {
		return packetsDuration != 0.0 ? lteDrxShortTime / packetsDuration : 0.0;
	}

	/**
	 * Returns the amount of time spent in the LTE DRX Long state.
	 * 
	 * @return The LTE DRX Long time value.
	 */
	public double getLteDrxLongTime() {
		return lteDrxLongTime;
	}

	/**
	 * Returns the ratio of total LTE DRX Long state time to the trace duration.
	 * 
	 * @return The LTE DRX Long time ratio value.
	 */
	public double getLteDrxLongTimeRatio() {
		return traceDuration != 0.0 ? lteDrxLongTime / traceDuration : 0.0;
	}
	
	/**
	 * Returns the LTE DRX long period time Ratio.
	 * 
	 * @return lteDrxLongTimeRatio
	 */
	public double getLteDrxLongRatio() {
		return packetsDuration != 0.0 ? lteDrxLongTime / packetsDuration : 0.0;
	}

	/**
	 * Returns the total amount of energy expended in the LTE IDLE state.
	 * 
	 * @return The LTE IDLE energy value.
	 */
	public double getLteIdleEnergy() {
		return lteIdleEnergy;
	}

	/**
	 * Returns the total amount of energy expended when the radio state was promoted from LTE IDLE to LTE CR. 
	 * 
	 * @return The LTE IDLE to LTE CR promotion energy value.
	 */
	public double getLteIdleToCRPromotionEnergy() {
		return lteIdleToCRPromotionEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the LTE CR state.
	 * 
	 * @return The LTE CR energy value.
	 */
	public double getLteCrEnergy() {
		return lteCrEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the LTE CR tail state. 
	 * 
	 * @return The LTE CR tail energy value.
	 */
	public double getLteCrTailEnergy() {
		return lteCrTailEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the LTE DRX Short state.
	 * 
	 * @return The LTE DRX Short energy value.
	 */
	public double getLteDrxShortEnergy() {
		return lteDrxShortEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the LTE DRX Long state.
	 * 
	 * @return The LTE DRX Long energy value.
	 */
	public double getLteDrxLongEnergy() {
		return lteDrxLongEnergy;
	}

	/**
	 * Returns the Continuous Reception (CR) state energy value.
	 * 
	 * @return The CR power value.
	 */
	public double getCrPower() {
		return crPower;
	}

	/**
	 * Returns the total amount of energy expended in the WiFi Active state.
	 * @return The WiFi Active energy value.
	 */
	public double getWifiActiveEnergy() {
		return wifiActiveEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the WiFi Tail state.
	 * @return The WiFi Tail energy value.
	 */
	public double getWifiTailEnergy() {
		return wifiTailEnergy;
	}

	/**
	 * Returns the total amount of energy expended in the WiFi Idle state.
	 * @return The WiFi Idle energy value.
	 */
	public double getWifiIdleEnergy() {
		return wifiIdleEnergy;
	}
	
	/**
	 * Returns the ratio of total amount of time spent in the WiFi Active state.
	 * @return The WiFi Active time value.
	 */
	public double getWifiActiveTime() {
		return wifiActiveTime;
	}

	/**
	 * Returns the ratio of total amount of time spent in the WiFi Tail state.
	 * @return The WiFi Tail time value.
	 */
	public double getWifiTailTime() {
		return wifiTailTime;
	}

	/**
	 * Returns the ratio of total amount of time spent in the WiFi Idle state.
	 * @return The WiFi Idle time value.
	 */
	public double getWifiIdleTime() {
		return wifiIdleTime;
	}
	
	/**
	 * Returns the ratio of total WiFi Active state time to the total trace duration.
	 * @return The WiFi Active ratio value.
	 */
	public double getWifiActiveRatio() {
		return traceDuration != 0.0 ? wifiActiveTime / traceDuration : 0.0;
	}

	/**
	 * Returns the ratio of total WiFi Tail state time to the total trace duration.
	 * @return The WiFi Tail ratio value.
	 */
	public double getWifiTailRatio() {
		return traceDuration != 0.0 ? wifiTailTime / traceDuration : 0.0;
	}

	/**
	 * Returns the ratio of total WiFi Idle state time to the total trace duration.
	 * @return The WiFi Idle ratio value.
	 */
	public double getWifiIdleRatio() {
		return traceDuration != 0.0 ? wifiIdleTime / traceDuration : 0.0;
	}

	/**
	 * 3G RRC state time modification.
	 */
	private synchronized void run3GRRcStatistics(TraceData.Analysis analysisData) {
		final Profile3G prof3g = (Profile3G) analysisData.getProfile();
		List<PacketInfo> packets = analysisData.getPackets();
		for (RrcStateRange rrc : this.rrc) {
			double energy = prof3g.energy(rrc.getBeginTime(), rrc.getEndTime(), rrc.getState(),
					packets);
			double d = rrc.getEndTime() - rrc.getBeginTime();
			switch (rrc.getState()) {
			case STATE_IDLE:
				idleTime += d;
				this.idleEnergy += energy;
				break;
			case STATE_DCH:
				dchTime += d;
				this.dchEnergy += energy;
				break;
			case TAIL_DCH:
				dchTime += d;
				dchTailTime += d;
				this.dchEnergy += energy;
				this.dchTailEnergy += energy;
				break;
			case STATE_FACH:
				fachTime += d;
				this.fachEnergy += energy;
				break;
			case TAIL_FACH:
				fachTime += d;
				fachTailTime += d;
				this.fachTailEnergy += energy;
				this.fachEnergy += energy;
				break;
			case PROMO_IDLE_DCH:
				idleToDch++;
				idleToDchTime += d;
				this.idleToDchEnergy += energy;
				break;
			case PROMO_FACH_DCH:
				fachToDch++;
				fachToDchTime += d;
				this.fachToDchEnergy += energy;
				break;
			}
		}
		this.totalRRCEnergy = fachEnergy + dchEnergy + fachToDchEnergy + idleToDchEnergy
				+ idleEnergy;
		long bytes = analysisData.getTotalBytes();
		this.joulesPerKilobyte = bytes != 0 ? totalRRCEnergy / (bytes / 1000.0) : 0.0;
	}

	/**
	 * LTE RRC state time modification.
	 */
	private synchronized void runLTERRcStatistics(TraceData.Analysis analysisData) {
		ProfileLTE profile = (ProfileLTE) analysisData.getProfile();
		List<PacketInfo> packets = analysisData.getPackets();
		for (RrcStateRange rrc : this.rrc) {
			double d = rrc.getEndTime() - rrc.getBeginTime();
			double energy = profile.energy(rrc.getBeginTime(), rrc.getEndTime(), rrc.getState(),
					packets);
			this.totalRRCEnergy += energy;
			switch (rrc.getState()) {
			case LTE_IDLE:
				lteIdleTime += d;
				this.lteIdleEnergy += energy;
				break;
			case LTE_PROMOTION:
				lteIdleToCRPromotionTime += d;
				this.lteIdleToCRPromotionEnergy += energy;
				break;
			case LTE_CONTINUOUS:
				lteCrTime += d;
				this.lteCrEnergy += energy;
				break;
			case LTE_CR_TAIL:
				lteCrTime += d;
				lteCrTailTime += d;
				this.lteCrEnergy += energy;
				this.lteCrTailEnergy += energy;
				break;
			case LTE_DRX_SHORT:
				lteDrxShortTime += d;
				this.lteDrxShortEnergy += energy;
				break;
			case LTE_DRX_LONG:
				lteDrxLongTime += d;
				this.lteDrxLongEnergy += energy;
				break;
			}
		}
		long bytes = analysisData.getTotalBytes();
		this.joulesPerKilobyte = bytes != 0 ? totalRRCEnergy / (bytes / 1000.0) : 0.0;
	}
	private synchronized void runWiFiRRcStatistics(TraceData.Analysis analysisData) {
		ProfileWiFi profile = (ProfileWiFi) analysisData.getProfile();
		List<PacketInfo> packets = analysisData.getPackets();
		for (RrcStateRange rrc : this.rrc) {
			double d = rrc.getEndTime() - rrc.getBeginTime();
			double energy = profile.energy(rrc.getBeginTime(), rrc.getEndTime(), rrc.getState() , packets);
			this.totalRRCEnergy += energy;
			switch (rrc.getState()) {
			case WIFI_ACTIVE:
				wifiActiveTime += d;
				this.wifiActiveEnergy += energy;
				break;
			case WIFI_TAIL:
				wifiActiveTime += d;
				this.wifiActiveEnergy += energy;
				wifiTailTime += d;
				this.wifiTailEnergy += energy;
				break;
			case WIFI_IDLE:
				wifiIdleTime += d;
				this.wifiIdleEnergy += energy;
				break;
			}
			}
		long bytes = analysisData.getTotalBytes();
		this.joulesPerKilobyte = bytes != 0 ? totalRRCEnergy / (bytes / 1000.0) : 0.0;
	}
}
