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
 * Represents the results of state machine analysis
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

	private List<RrcStateRange> rrc = new ArrayList<RrcStateRange>();

	/**
	 * Constructor
	 * 
	 * @param analysisData
	 *            Trace analysis
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
	 * Returns the list of RRC state range.
	 * 
	 * @return the RRC states
	 */
	public List<RrcStateRange> getRRcStateRanges() {
		return Collections.unmodifiableList(rrc);
	}

	/**
	 * Returns the Direct channel time.
	 * 
	 * @return the dchTime
	 */
	public double getDchTime() {
		return dchTime;
	}

	/**
	 * Returns the total DCH time ratio corresponding to trace duration.
	 * 
	 * @return the DCH time as a ratio to traceDuration
	 */
	public double getDchTimeRatio() {
		return traceDuration != 0.0 ? dchTime / traceDuration : 0.0;
	}

	/**
	 * Returns the Forward access channel time.
	 * 
	 * @return the fachTime
	 */
	public double getFachTime() {
		return fachTime;
	}

	/**
	 * Returns the total FACH time ratio corresponding to trace duration.
	 * 
	 * @return the FACH time as a ratio to traceDuration
	 */
	public double getFachTimeRatio() {
		return traceDuration != 0.0 ? fachTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total idle time.
	 * 
	 * @return the idleTime
	 */
	public double getIdleTime() {
		return idleTime;
	}

	/**
	 * Returns the total idle time ratio corresponding to trace duration.
	 * 
	 * @return the idle time as a ratio to traceDuration
	 */
	public double getIdleTimeRatio() {
		return traceDuration != 0.0 ? idleTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total Idle to DCH time.
	 * 
	 * @return the idleToDchTime
	 */
	public double getIdleToDchTime() {
		return idleToDchTime;
	}

	/**
	 * Returns the total Idle to DCH time ratio corresponding to trace duration.
	 * 
	 * @return the Idle->DCH time as a ratio to traceDuration
	 */
	public double getIdleToDchTimeRatio() {
		return traceDuration != 0.0 ? idleToDchTime / traceDuration : 0.0;
	}

	/**
	 * Returns the idle to DCH state count in the trace.
	 * 
	 * @return the idleToDch
	 */
	public int getIdleToDchCount() {
		return idleToDch;
	}

	/**
	 * Returns the FACH to DCH time.
	 * 
	 * @return the fachToDchTime
	 */
	public double getFachToDchTime() {
		return fachToDchTime;
	}

	/**
	 * Returns the total FACH to DCH time ratio corresponding to trace duration.
	 * 
	 * @return the Fach->DCH time as a ratio to traceDuration
	 */
	public double getFachToDchTimeRatio() {
		return traceDuration != 0.0 ? fachToDchTime / traceDuration : 0.0;
	}

	/**
	 * Returns the total DCH tail time.
	 * 
	 * @return the dchTailTime
	 */
	public double getDchTailTime() {
		return dchTailTime;
	}

	/**
	 * Returns the total DCH tail time ratio corresponding to trace duration.
	 * 
	 * @return the DCH Tail Ratio
	 */
	public double getDchTailRatio() {
		return dchTime != 0.0 ? dchTailTime / dchTime : 0.0;
	}

	/**
	 * Returns the total FACH to DCH count.
	 * 
	 * @return the fachToDch
	 */
	public int getFachToDchCount() {
		return fachToDch;
	}

	/**
	 * Returns the total FACH tail time.
	 * 
	 * @return the fachTailTime
	 */
	public double getFachTailTime() {
		return fachTailTime;
	}

	/**
	 * Returns the total FACH tail time ratio corresponding to trace duration.
	 * 
	 * @return the FACH Tail Ratio
	 */
	public double getFachTailRatio() {
		return fachTime != 0.0 ? fachTailTime / fachTime : 0.0;
	}

	/**
	 * Returns the promotion time ratio corresponding to trace duration.
	 * 
	 * @return the Promotion Ratio/Signaling overhead
	 */
	public double getPromotionRatio() {
		return packetsDuration != 0.0 ? (idleToDchTime + fachToDchTime) / packetsDuration : 0.0;
	}

	/**
	 * Returns the total energy expended sending packet data.
	 * 
	 * @return totalRRCEnergy
	 */
	public double getTotalRRCEnergy() {
		return totalRRCEnergy;
	}

	/**
	 * Returns the average joules per kilobyte for the trace.
	 * 
	 * @return joulesPerKilobyte
	 */
	public double getJoulesPerKilobyte() {
		return joulesPerKilobyte;
	}

	/**
	 * Returns the total idle time energy expended.
	 * 
	 * @return idleEnergy
	 */
	public double getIdleEnergy() {
		return idleEnergy;
	}

	/**
	 * Returns the total DCH time energy expended.
	 * 
	 * @return dchEnergy
	 */
	public double getDchEnergy() {
		return dchEnergy;
	}

	/**
	 * Returns the total FACH energy expended.
	 * 
	 * @return fachEnergy
	 */
	public double getFachEnergy() {
		return fachEnergy;
	}

	/**
	 * Returns the total Idle->DCH energy expended
	 * 
	 * @return idleToDchEnergy
	 */
	public double getIdleToDchEnergy() {
		return idleToDchEnergy;
	}

	/**
	 * Returns the total FACH->DCH energy expended.
	 * 
	 * @return fachToDchEnergy
	 */
	public double getFachToDchEnergy() {
		return fachToDchEnergy;
	}

	/**
	 * Returns the total DCH Tail energy expended.
	 * 
	 * @return dchTailEnergy
	 */
	public double getDchTailEnergy() {
		return dchTailEnergy;
	}

	/**
	 * Returns the total energy expended sending packet data.
	 * 
	 * @return fachTailEnergy
	 */
	public double getFachTailEnergy() {
		return fachTailEnergy;
	}

	/**
	 * Returns the LTE idle period time.
	 * 
	 * @return lteIdleTime
	 */
	public double getLteIdleTime() {
		return lteIdleTime;
	}

	/**
	 * Returns the LTE idle period time ratio.
	 * 
	 * @return lteIdleTimeratio
	 */
	public double getLteIdleTimeRatio() {
		return traceDuration != 0.0 ? lteIdleTime / traceDuration : 0.0;
	}

	/**
	 * Returns the Idle to CR promotion period time.
	 * 
	 * @return lteIdleToCRPromotionTime
	 */
	public double getLteIdleToCRPromotionTime() {
		return lteIdleToCRPromotionTime;
	}

	/**
	 * Returns the Idle to CR promotion period time Ratio.
	 * 
	 * @return lteIdleToCRPromotionTimeRatio
	 */
	public double getLteIdleToCRPromotionTimeRatio() {
		return traceDuration != 0.0 ? lteIdleToCRPromotionTime / traceDuration : 0.0;
	}

	/**
	 * Returns the CR period time.
	 * 
	 * @return lteCrTime
	 */
	public double getLteCrTime() {
		return lteCrTime;
	}

	/**
	 * Returns the CR period time ratio.
	 * 
	 * @return lteCrTimeRatio.
	 */
	public double getLteCrTimeRatio() {
		return traceDuration != 0.0 ? lteCrTime / traceDuration : 0.0;
	}

	/**
	 * Returns the CR Tail period time.
	 * 
	 * @return lteCrTailTime
	 */
	public double getLteCrTailTime() {
		return lteCrTailTime;
	}

	/**
	 * Returns the CR Tail period time Ratio.
	 * 
	 * @return lteCrTailTimeRatio
	 */
	public double getLteCrTailTimeRatio() {
		return traceDuration != 0.0 ? lteCrTailTime / traceDuration : 0.0;
	}

	/**
	 * Returns the LTE DRX Short period time.
	 * 
	 * @return lteDrxShortTime
	 */
	public double getLteDrxShortTime() {
		return lteDrxShortTime;
	}

	/**
	 * Returns the LTE DRX Short period time Ratio.
	 * 
	 * @return lteDrxShortTimeRatio
	 */
	public double getLteDrxShortTimeRatio() {
		return traceDuration != 0.0 ? lteDrxShortTime / traceDuration : 0.0;
	}

	/**
	 * Returns the LTE DRX long period time.
	 * 
	 * @return lteDrxLongTime
	 */
	public double getLteDrxLongTime() {
		return lteDrxLongTime;
	}

	/**
	 * Returns the LTE DRX long period time Ratio.
	 * 
	 * @return lteDrxLongTimeRatio
	 */
	public double getLteDrxLongTimeRatio() {
		return traceDuration != 0.0 ? lteDrxLongTime / traceDuration : 0.0;
	}

	/**
	 * Returns the LTE Idle energy.
	 * 
	 * @return lteIdleEnergy
	 */
	public double getLteIdleEnergy() {
		return lteIdleEnergy;
	}

	/**
	 * Returns the LTE Idle to CR promotion energy.
	 * 
	 * @return lteIdleToCRPromotionEnergy
	 */
	public double getLteIdleToCRPromotionEnergy() {
		return lteIdleToCRPromotionEnergy;
	}

	/**
	 * Returns the LTE CR energy.
	 * 
	 * @return lteCrEnergy
	 */
	public double getLteCrEnergy() {
		return lteCrEnergy;
	}

	/**
	 * Returns the LTE CR Tail energy.
	 * 
	 * @return lteCrTailEnergy
	 */
	public double getLteCrTailEnergy() {
		return lteCrTailEnergy;
	}

	/**
	 * Returns the LTE DRX Short energy.
	 * 
	 * @return lteDrxShortEnergy
	 */
	public double getLteDrxShortEnergy() {
		return lteDrxShortEnergy;
	}

	/**
	 * Returns the LTE DRX Long energy.
	 * 
	 * @return lteDrxLongenergy
	 */
	public double getLteDrxLongEnergy() {
		return lteDrxLongEnergy;
	}

	/**
	 * Returns the Continues Reception energy.
	 * 
	 * @return crPower
	 */
	public double getCrPower() {
		return crPower;
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
}
