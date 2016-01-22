/**
 *  Copyright 2016 AT&T
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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.IRrcStateMachineFactory;
import com.att.aro.core.packetanalysis.IRrcStateRangeFactory;
import com.att.aro.core.packetanalysis.pojo.AbstractRrcStateMachine;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachine3G;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineWiFi;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetanalysis.pojo.TimeRange;

public class RrcStateMachineFactoryImpl implements IRrcStateMachineFactory {

	@Autowired
	IRrcStateRangeFactory staterange;
	@Autowired
	IProfileFactory profilefactory;
	
	@Override
	public AbstractRrcStateMachine create(List<PacketInfo> packetlist,
			Profile profile, double packetDuration, double traceDuration, double totalBytes,
			TimeRange timerange) {
		List<RrcStateRange> staterangelist = staterange.create(packetlist, profile, traceDuration);
		if(timerange != null){
			staterangelist = this.getRRCStatesForTheTimeRange(staterangelist, timerange.getBeginTime(), timerange.getEndTime());
		}
		AbstractRrcStateMachine data = null;
		if(profile.getProfileType() == ProfileType.T3G){
			data = run3GRRcStatistics(staterangelist, (Profile3G)profile, totalBytes, packetDuration, traceDuration);
		}else if(profile.getProfileType() == ProfileType.LTE){
			data = runLTERRcStatistics(staterangelist, (ProfileLTE)profile, packetlist, totalBytes, packetDuration, traceDuration);
		}else if(profile.getProfileType() == ProfileType.WIFI){
			data = runWiFiRRcStatistics(staterangelist, (ProfileWiFi)profile, totalBytes, packetDuration, traceDuration);
		}
		if(data != null){
			data.setStaterangelist(staterangelist);
		}
		return data;
	}
	private RrcStateMachineWiFi runWiFiRRcStatistics(List<RrcStateRange> staterangelist, ProfileWiFi prof,
			double totalBytes, double packetDuration, double traceDuration) {
		double totalRRCEnergy = 0, wifiActiveTime = 0, wifiActiveEnergy = 0, wifiTailTime = 0;
		double wifiTailEnergy = 0, wifiIdleTime = 0, wifiIdleEnergy = 0;
		for (RrcStateRange rrc : staterangelist) {
			double rrcTimeDiff = rrc.getEndTime() - rrc.getBeginTime();
			double energy = profilefactory.energyWiFi(rrc.getBeginTime(), rrc.getEndTime(), rrc.getState(), prof);
			totalRRCEnergy += energy;
			switch (rrc.getState()) {
			case WIFI_ACTIVE:
				wifiActiveTime += rrcTimeDiff;
				wifiActiveEnergy += energy;
				break;
			case WIFI_TAIL:
				wifiActiveTime += rrcTimeDiff;
				wifiActiveEnergy += energy;
				wifiTailTime += rrcTimeDiff;
				wifiTailEnergy += energy;
				break;
			case WIFI_IDLE:
				wifiIdleTime += rrcTimeDiff;
				wifiIdleEnergy += energy;
				break;
			default:
				break;
			}
			}
		double bytes = totalBytes;
		double joulesPerKilobyte = bytes != 0 ? totalRRCEnergy / (bytes / 1000.0) : 0.0;
		RrcStateMachineWiFi stmachine = new RrcStateMachineWiFi();
		
		stmachine.setJoulesPerKilobyte(joulesPerKilobyte);
		stmachine.setPacketsDuration(packetDuration);
		stmachine.setTotalRRCEnergy(totalRRCEnergy);
		stmachine.setTraceDuration(traceDuration);
		
		stmachine.setWifiActiveEnergy(wifiActiveEnergy);
		stmachine.setWifiActiveTime(wifiActiveTime);
		stmachine.setWifiIdleEnergy(wifiIdleEnergy);
		stmachine.setWifiIdleTime(wifiIdleTime);
		stmachine.setWifiTailTime(wifiTailTime);
		stmachine.setWifiTailEnergy(wifiTailEnergy);
		
		return stmachine;
	}
	/**
	 * LTE RRC state time modification.
	 */
	private RrcStateMachineLTE runLTERRcStatistics(List<RrcStateRange> staterangelist, ProfileLTE profile, List<PacketInfo> packets,
			double totalBytes, double packetsDuration, double traceDuration) {
		double totalRRCEnergy = 0, lteIdleTime = 0, lteIdleEnergy = 0, lteIdleToCRPromotionTime = 0;
		double lteIdleToCRPromotionEnergy = 0, lteCrTime = 0, lteCrEnergy = 0, lteCrTailTime = 0;
		double lteCrTailEnergy = 0, lteDrxShortTime = 0, lteDrxShortEnergy = 0, lteDrxLongTime = 0;
		double lteDrxLongEnergy = 0;
		for (RrcStateRange rrc : staterangelist) {
			double duration = rrc.getEndTime() - rrc.getBeginTime();
			double energy = profilefactory.energyLTE(rrc.getBeginTime(), rrc.getEndTime(), rrc.getState(), profile, packets);
			totalRRCEnergy += energy;
			switch (rrc.getState()) {
			case LTE_IDLE:
				lteIdleTime += duration;
				lteIdleEnergy += energy;
				break;
			case LTE_PROMOTION:
				lteIdleToCRPromotionTime += duration;
				lteIdleToCRPromotionEnergy += energy;
				break;
			case LTE_CONTINUOUS:
				lteCrTime += duration;
				lteCrEnergy += energy;
				break;
			case LTE_CR_TAIL:
				lteCrTime += duration;
				lteCrTailTime += duration;
				lteCrEnergy += energy;
				lteCrTailEnergy += energy;
				break;
			case LTE_DRX_SHORT:
				lteDrxShortTime += duration;
				lteDrxShortEnergy += energy;
				break;
			case LTE_DRX_LONG:
				lteDrxLongTime += duration;
				lteDrxLongEnergy += energy;
				break;
			default:
				break;
			}
		}
		double bytes = totalBytes;
		double joulesPerKilobyte = bytes != 0 ? totalRRCEnergy / (bytes / 1000.0) : 0.0;
		RrcStateMachineLTE stmachine = new RrcStateMachineLTE();
		stmachine.setJoulesPerKilobyte(joulesPerKilobyte);
		
		stmachine.setLteCrEnergy(lteCrEnergy);
		stmachine.setLteCrTailEnergy(lteCrTailEnergy);
		stmachine.setLteCrTailTime(lteCrTailTime);
		stmachine.setLteCrTime(lteCrTime);
		
		stmachine.setLteDrxLongEnergy(lteDrxLongEnergy);
		stmachine.setLteDrxLongTime(lteDrxLongTime);
		stmachine.setLteDrxShortEnergy(lteDrxShortEnergy);
		stmachine.setLteDrxShortTime(lteDrxShortTime);
		
		stmachine.setLteIdleEnergy(lteIdleEnergy);
		stmachine.setLteIdleTime(lteIdleTime);
		stmachine.setLteIdleToCRPromotionEnergy(lteIdleToCRPromotionEnergy);
		stmachine.setLteIdleToCRPromotionTime(lteIdleToCRPromotionTime);
		
		stmachine.setPacketsDuration(packetsDuration);
		stmachine.setTotalRRCEnergy(totalRRCEnergy);
		stmachine.setTraceDuration(traceDuration);
		
		return stmachine;
	}
	/**
	 * 3G RRC state time modification.
	 */
	private RrcStateMachine3G run3GRRcStatistics(List<RrcStateRange> staterangelist,
			Profile3G prof3g, double totalBytes, double packetsDuration, double traceDuration) {
		
		double idleTime = 0, idleEnergy = 0, dchTime = 0, dchEnergy = 0, dchTailTime =0, dchTailEnergy=0;
		double fachTime=0, fachEnergy=0, fachTailTime=0;
		double fachTailEnergy=0, idleToDch=0, idleToDchTime=0, idleToDchEnergy=0, fachToDch=0;
		double fachToDchTime=0, fachToDchEnergy=0;
		RrcStateMachine3G statemachine = new RrcStateMachine3G();
		
		for (RrcStateRange rrc : staterangelist) {
			double energy = profilefactory.energy3G(rrc.getBeginTime(), rrc.getEndTime(), rrc.getState(), prof3g);
			double duration = rrc.getEndTime() - rrc.getBeginTime();
			switch (rrc.getState()) {
			case STATE_IDLE:
				idleTime += duration;
				idleEnergy += energy;
				break;
			case STATE_DCH:
				dchTime += duration;
				dchEnergy += energy;
				break;
			case TAIL_DCH:
				dchTime += duration;
				dchTailTime += duration;
				dchEnergy += energy;
				dchTailEnergy += energy;
				break;
			case STATE_FACH:
				fachTime += duration;
				fachEnergy += energy;
				break;
			case TAIL_FACH:
				fachTime += duration;
				fachTailTime += duration;
				fachTailEnergy += energy;
				fachEnergy += energy;
				break;
			case PROMO_IDLE_DCH:
				idleToDch++;
				idleToDchTime += duration;
				idleToDchEnergy += energy;
				break;
			case PROMO_FACH_DCH:
				fachToDch++;
				fachToDchTime += duration;
				fachToDchEnergy += energy;
				break;
			default:
				break;
			}
		}
		double totalRRCEnergy = fachEnergy + dchEnergy + fachToDchEnergy + idleToDchEnergy
				+ idleEnergy;
		double bytes = totalBytes;
		double joulesPerKilobyte = bytes != 0 ? totalRRCEnergy / (bytes / 1000.0) : 0.0;
		
		statemachine.setDchEnergy(dchEnergy);
		statemachine.setDchTailEnergy(dchTailEnergy);
		statemachine.setDchTailTime(dchTailTime);
		statemachine.setDchTime(dchTime);
		
		statemachine.setFachEnergy(fachEnergy);
		statemachine.setFachTailEnergy(fachTailEnergy);
		statemachine.setFachTailTime(fachTailTime);
		statemachine.setFachTime(fachTime);
		statemachine.setFachToDch(fachToDch);
		statemachine.setFachToDchEnergy(fachToDchEnergy);
		statemachine.setFachToDchTime(fachToDchTime);
		
		statemachine.setIdleEnergy(idleEnergy);
		statemachine.setIdleTime(idleTime);
		statemachine.setIdleToDch(idleToDch);
		statemachine.setIdleToDchEnergy(idleToDchEnergy);
		statemachine.setIdleToDchTime(idleToDchTime);
		
		statemachine.setJoulesPerKilobyte(joulesPerKilobyte);
		statemachine.setPacketsDuration(packetsDuration);
		statemachine.setTotalRRCEnergy(totalRRCEnergy);
		statemachine.setTraceDuration(traceDuration);
		
		return statemachine;
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

}
