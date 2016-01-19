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
package com.att.aro.core.packetanalysis.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.core.packetanalysis.IBurstCollectionAnalysis;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstAnalysisInfo;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetreader.pojo.PacketDirection;
import com.att.aro.core.peripheral.pojo.CpuActivity;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;



public class BurstCollectionAnalysisImpl implements IBurstCollectionAnalysis {

	private static final double EPS = 1 * Math.pow(10, -6);
	private static final double USER_EVENT_TOLERATE = 4.0f;
	private static final double AVG_CPU_USAGE_THRESHOLD = 70.0f;
	
	@Autowired
	IProfileFactory profilefactory;
	
	@Override
	public BurstCollectionAnalysisData analyze(List<PacketInfo> packets,
			Profile profile, Map<Integer, Integer> packetSizeToCountMap,
			List<RrcStateRange> rrcstaterangelist,
			List<UserEvent> usereventlist, List<CpuActivity> cpuactivitylist,
			List<Session> sessionlist) {
		BurstCollectionAnalysisData data = new BurstCollectionAnalysisData();
		
		Set<Integer> mss = calculateMssLargerPacketSizeSet(packetSizeToCountMap);
		List<Burst> burstCollection = groupIntoBursts(packets, profile, mss, rrcstaterangelist);
		data.setBurstCollection(burstCollection);
		
		if(!burstCollection.isEmpty()){
			int longBurstCount = analyzeBursts(burstCollection, usereventlist, cpuactivitylist, profile);
			data.setLongBurstCount(longBurstCount);
			
			double totalEnergy = computeBurstEnergyRadioResource(rrcstaterangelist, burstCollection, profile, packets);
			data.setTotalEnergy(totalEnergy);
			
			List<BurstAnalysisInfo> burstAnalysisInfo = analyzeBurstStat(burstCollection);
			data.setBurstAnalysisInfo(burstAnalysisInfo);
			
			PacketInfo shortestPacket = findShortestPeriodPacketInfo(burstCollection);
			data.setShortestPeriodPacketInfo(shortestPacket);
		}
		return data;
	}
	/**
	 * Method to find the different periodic connection and periodic duration.
	 */
	private PacketInfo findShortestPeriodPacketInfo(List<Burst> burstCollection) {
		int burstSize = burstCollection.size();
		Burst lastPeriodicalBurst = null;
		int periodicPacketCounter = 0;
		double minRepeatTime = Double.MAX_VALUE;
		PacketInfo pktId = null;
		double time = 0;
		for (int i = 0; i < burstSize; i++) {
			Burst burst = burstCollection.get(i);
			if (burst.getBurstCategory() == BurstCategory.PERIODICAL) {
				if (periodicPacketCounter != 0) {
					time = burst.getBeginTime() - lastPeriodicalBurst.getBeginTime();
					if (time < minRepeatTime) {
						minRepeatTime = time;
						pktId = burst.getFirstUplinkDataPacket();
						if (null == pktId) {
							pktId = burst.getBeginPacket();
						}
					}
				}
				periodicPacketCounter++;
				lastPeriodicalBurst = burst;
				
			}
		}

		return pktId;
	}
	/**
	 * Method to assign the states for all the bursts.
	 * 
	 * @param analyzeBeginTime
	 * @param analyseEndTime
	 * @return 
	 */
	private List<BurstAnalysisInfo> analyzeBurstStat(List<Burst> burstCollection) {
		Map<BurstCategory, Double> burstCategoryToEnergy = new EnumMap<BurstCategory, Double>(
				BurstCategory.class);
		Map<BurstCategory, Long> burstCategoryToPayload = new EnumMap<BurstCategory, Long>(
				BurstCategory.class);
		Map<BurstCategory, Double> burstCategoryToActive = new EnumMap<BurstCategory, Double>(
				BurstCategory.class);
		List<BurstAnalysisInfo> burstAnalysisInfo = new ArrayList<BurstAnalysisInfo>();
		long totalPayload = 0;
		double totalAct = 0.0;
		double totalEnergy = 0.0;

		for (Burst aBurst : burstCollection) {
			BurstCategory category = aBurst.getBurstCategory();
			double energy = aBurst.getEnergy();
			totalEnergy += energy;
			Double catEnergy = burstCategoryToEnergy.get(category);
			double catEnergygValue = catEnergy != null ? catEnergy.doubleValue() : 0.0;
			catEnergygValue += energy;
			burstCategoryToEnergy.put(category, catEnergygValue);

			int bPayLoadLength = getPayloadLength(aBurst, false);
			totalPayload += bPayLoadLength;
			Long payload = burstCategoryToPayload.get(category);
			long payLoadValue = payload != null ? payload.longValue() : 0L;
			payLoadValue += bPayLoadLength;
			burstCategoryToPayload.put(category, payLoadValue);

			double activeTime = aBurst.getActiveTime();
			totalAct += activeTime;
			Double catAct = burstCategoryToActive.get(category);
			catEnergygValue = catAct != null ? catAct.doubleValue() : 0.0;
			catEnergygValue += activeTime;
			burstCategoryToActive.put(category, catEnergygValue);

		}
		for (Map.Entry<BurstCategory, Double> entry : burstCategoryToEnergy.entrySet()) {
			BurstCategory categ = entry.getKey();
			long catPayload = burstCategoryToPayload.get(categ);
			double catEnergy = burstCategoryToEnergy.get(categ);
			double catActive = burstCategoryToActive.get(categ);

			Double jpkb = catPayload > 0 ? catEnergy / (catPayload * 8 / 1000.0f) : null;
			burstAnalysisInfo.add(new BurstAnalysisInfo(categ, catPayload,
					totalPayload > 0 ? (((double) catPayload / totalPayload) * 100.0) : 0, catEnergy,
					totalEnergy > 0.0 ? ((catEnergy / totalEnergy) * 100.0) : 0.0, catActive, 
					totalAct > 0.0 ? ((catActive / totalAct) * 100.0) : 0.0,
					jpkb));

		}
		return burstAnalysisInfo;
	}/**
	 * Getter for getting the payload length for the provided burst.
	 * 
	 * @param burst
	 * @param bIncludeBkgApp
	 * @return
	 */
	private int getPayloadLength(Burst burst, boolean bIncludeBkgApp) {
		int bPayLenTotal = 0;
		for (PacketInfo pInfo : burst.getPackets()) {
			if (bIncludeBkgApp || pInfo.getAppName() != null) {
				bPayLenTotal += pInfo.getPayloadLen();
			}
		}
		return bPayLenTotal;
	}
	
	/**
	 * Computes the total burst energy.
	 * @return 
	 */
	private double computeBurstEnergyRadioResource(List<RrcStateRange> rrcstaterangelist, List<Burst> burstCollection,
			Profile profile, List<PacketInfo> packets) {
		List<RrcStateRange> rrcCollection = rrcstaterangelist;
		int rrcCount = rrcCollection.size();
		if (rrcCount == 0) {
			return 0;
		}
		int pCount = 0;
		double time2 = -1;
		double totalEnergy = 0.0f;
		Iterator<Burst> iter = burstCollection.iterator();
		Burst currentBurst = iter.next();
		double time1 = rrcCollection.get(0).getBeginTime();
		while (true) {
			Burst nextBurst = iter.hasNext() ? iter.next() : null;
			time2 = nextBurst != null ? nextBurst.getBeginTime() : rrcCollection.get(rrcCount - 1)
					.getEndTime();
			double energy = 0.0f;
			double activeTime = 0.0f;
			while (pCount < rrcCount) {
				RrcStateRange rrCntrl = rrcCollection.get(pCount);
				if (rrCntrl.getEndTime() < time1) {
					pCount++;
				} else {
					if (time2 > rrCntrl.getEndTime()) {
						if(profile.getProfileType() == ProfileType.T3G){
							energy += profilefactory.energy3G(time1, rrCntrl.getEndTime(), rrCntrl.getState(), (Profile3G)profile);
						}else if(profile.getProfileType() == ProfileType.LTE){
							energy += profilefactory.energyLTE(time1, rrCntrl.getEndTime(), rrCntrl.getState(), (ProfileLTE)profile, packets);
						}else if(profile.getProfileType() == ProfileType.WIFI){
							energy += profilefactory.energyWiFi(time1, rrCntrl.getEndTime(), rrCntrl.getState(), (ProfileWiFi)profile);
						}
						if ((rrCntrl.getState() == RRCState.STATE_DCH || rrCntrl.getState() == RRCState.TAIL_DCH)
								|| (rrCntrl.getState() == RRCState.LTE_CONTINUOUS || rrCntrl
										.getState() == RRCState.LTE_CR_TAIL)
								|| (rrCntrl.getState() == RRCState.WIFI_ACTIVE || rrCntrl
										.getState() == RRCState.WIFI_TAIL)) {
							activeTime += rrCntrl.getEndTime() - time1;
						}
						pCount++;
					}
					break;
				}
			}
			while (pCount < rrcCount) {
				RrcStateRange rrCntrl = rrcCollection.get(pCount);
				if (rrCntrl.getEndTime() < time2) {
					if(profile.getProfileType() == ProfileType.T3G){
						energy += profilefactory.energy3G(Math.max(rrCntrl.getBeginTime(), time1), rrCntrl.getEndTime(), rrCntrl.getState(), (Profile3G)profile);
					}else if(profile.getProfileType() == ProfileType.LTE){
						energy += profilefactory.energyLTE(Math.max(rrCntrl.getBeginTime(), time1), rrCntrl.getEndTime(), rrCntrl.getState(), (ProfileLTE)profile, packets);
					}else if(profile.getProfileType() == ProfileType.WIFI){
						energy += profilefactory.energyWiFi(Math.max(rrCntrl.getBeginTime(), time1), rrCntrl.getEndTime(), rrCntrl.getState(), (ProfileWiFi)profile);
					}
					if ((rrCntrl.getState() == RRCState.STATE_DCH || rrCntrl.getState() == RRCState.TAIL_DCH)
							|| (rrCntrl.getState() == RRCState.LTE_CONTINUOUS || rrCntrl.getState() == RRCState.LTE_CR_TAIL)
							|| (rrCntrl.getState() == RRCState.WIFI_ACTIVE || rrCntrl.getState() == RRCState.WIFI_TAIL)) {
						activeTime += rrCntrl.getEndTime()
								- Math.max(rrCntrl.getBeginTime(), time1);
					}
					pCount++;
				} else {
					if(profile.getProfileType() == ProfileType.T3G){
						energy += profilefactory.energy3G(Math.max(rrCntrl.getBeginTime(), time1), time2, rrCntrl.getState(), (Profile3G)profile);
					}else if(profile.getProfileType() == ProfileType.LTE){
						energy += profilefactory.energyLTE(Math.max(rrCntrl.getBeginTime(), time1), time2, rrCntrl.getState(), (ProfileLTE)profile, packets);
					}else if(profile.getProfileType() == ProfileType.WIFI){
						energy += profilefactory.energyWiFi(Math.max(rrCntrl.getBeginTime(), time1), time2, rrCntrl.getState(), (ProfileWiFi)profile);
					}
					if ((rrCntrl.getState() == RRCState.STATE_DCH || rrCntrl.getState() == RRCState.TAIL_DCH)
							|| (rrCntrl.getState() == RRCState.LTE_CONTINUOUS || rrCntrl.getState() == RRCState.LTE_CR_TAIL)
							|| (rrCntrl.getState() == RRCState.WIFI_ACTIVE || rrCntrl.getState() == RRCState.WIFI_TAIL)) {
						activeTime += time2 - Math.max(rrCntrl.getBeginTime(), time1);
					}
					break;
				}
			}
			currentBurst.setEnergy(energy);
			totalEnergy += energy;
			currentBurst.setActiveTime(activeTime);

			time1 = time2;
			if (nextBurst != null) {
				currentBurst = nextBurst;
			} else {
				break;
			}
		}
		return totalEnergy;
	}
	/**
	 * Assigns burst category to each burst in a collection of bursts.
	 * The collection of bursts have been populated prior to this API call.
	 */
	private int analyzeBursts(List<Burst> burstCollection, List<UserEvent> userEvents, List<CpuActivity> cpuEvents,
			Profile profile) {
		
		int userEventsSize = userEvents.size();
		int cpuEventsSize = cpuEvents.size();
		int userEventPointer = 0;
		int cpuPointer = 0;
		int longBurstCount = 0;
		// Analyze each burst
		Burst burst = null;
		Burst lastBurst;
		for (Iterator<Burst> iterator = burstCollection.iterator(); iterator.hasNext();) {
			
			lastBurst = burst;
			burst = iterator.next();
			List<PacketInfo> burstPacketCollection = new ArrayList<PacketInfo>(burst.getPackets().size());
			int burstPayloadLen = 0;
			Set<TcpInfo> burstPacketTcpInfo = new HashSet<TcpInfo>();
			for (PacketInfo pInfo : burst.getPackets()) {
				burstPayloadLen += pInfo.getPayloadLen();
				burstPacketCollection.add(pInfo);
				TcpInfo tcp = pInfo.getTcpInfo();
				if (tcp != null) {
					burstPacketTcpInfo.add(tcp);
				}
			}
			
			PacketInfo pkt0 = null;
			TcpInfo info0 = null;
			double time0 = 0;
			if (!burstPacketCollection.isEmpty()) {
				pkt0 = burstPacketCollection.get(0);
				info0 = pkt0.getTcpInfo();
				time0 = pkt0.getTimeStamp();
			}
			


			/*
			 * Mark the burst as Long Burst based on the
			 * burst duration and size of the payload. 
			 */
			if (burst.getEndTime() - burst.getBeginTime() > profile.getLargeBurstDuration()
					&& burstPayloadLen > profile.getLargeBurstSize()) {
				burst.setBurstInfo(BurstCategory.LONG);
				++longBurstCount;
				continue;
			}

			/*
			 * For bursts with no payload assign burst type based on
			 * the the type of TCP packets. 
			 */
			if (burstPayloadLen == 0) {
				if (burstPacketTcpInfo.contains(TcpInfo.TCP_CLOSE) ||
				    burstPacketTcpInfo.contains(TcpInfo.TCP_ESTABLISH) ||
				    burstPacketTcpInfo.contains(TcpInfo.TCP_RESET) ||
				    burstPacketTcpInfo.contains(TcpInfo.TCP_KEEP_ALIVE) ||
					burstPacketTcpInfo.contains(TcpInfo.TCP_KEEP_ALIVE_ACK) ||
				    burstPacketTcpInfo.contains(TcpInfo.TCP_ZERO_WINDOW) ||
				    burstPacketTcpInfo.contains(TcpInfo.TCP_WINDOW_UPDATE)) {
					
					burst.setBurstInfo(BurstCategory.TCP_PROTOCOL);
					continue;
				}
				
				if (info0 == TcpInfo.TCP_ACK_RECOVER || info0 == TcpInfo.TCP_ACK_DUP) {
					burst.setBurstInfo(BurstCategory.TCP_LOSS_OR_DUP);
					continue;
				}
			}

			// Step 4: Server delay
			if (pkt0.getDir() == PacketDirection.DOWNLINK
					&& (info0 == TcpInfo.TCP_DATA || info0 == TcpInfo.TCP_ACK)) {
				burst.setBurstInfo(BurstCategory.SERVER_NET_DELAY);
				continue;
			}

			// Step 5: Loss recover
			if (info0 == TcpInfo.TCP_ACK_DUP || info0 == TcpInfo.TCP_DATA_DUP) {
				burst.setBurstInfo(BurstCategory.TCP_LOSS_OR_DUP);
				continue;
			}

			if (info0 == TcpInfo.TCP_DATA_RECOVER || info0 == TcpInfo.TCP_ACK_RECOVER) {
				burst.setBurstInfo(BurstCategory.TCP_LOSS_OR_DUP);
				continue;
			}

			// Step 6: User triggered
			final double USER_EVENT_SMALL_TOLERATE = profile.getUserInputTh();
			if (burstPayloadLen > 0) {
				UserEvent uevent = null;
				while ((userEventPointer < userEventsSize)
						&& ((uevent = userEvents.get(userEventPointer)).getReleaseTime() < (time0 - USER_EVENT_TOLERATE))) {
					++userEventPointer;
				}
				BurstCategory userInputBurst = null;
				if(uevent != null){
					if(uevent.getEventType() == UserEventType.SCREEN_LANDSCAPE || 
							uevent.getEventType() == UserEventType.SCREEN_PORTRAIT){
						userInputBurst = BurstCategory.SCREEN_ROTATION;
					}else{
						userInputBurst = BurstCategory.USER_INPUT;
					}
				}
				int userEventCount = userEventPointer;
				double minGap = Double.MAX_VALUE;
				while (userEventCount < userEventsSize) {
					UserEvent uEvent = userEvents.get(userEventCount);
					if (withinTolerate(uEvent.getPressTime(), time0)) {
						double gap = time0 - uEvent.getPressTime();
						if (gap < minGap) {
							minGap = gap;
						}
					}
					if (withinTolerate(uEvent.getReleaseTime(), time0)) {
						double gap = time0 - uEvent.getReleaseTime();
						if (gap < minGap) {
							minGap = gap;
						}
					}
					if (uEvent.getPressTime() > time0) {
						break;
					}
					userEventCount++;
				}
				if (minGap < USER_EVENT_SMALL_TOLERATE) {
					burst.setBurstInfo(userInputBurst);
					continue;
				} else if (minGap < USER_EVENT_TOLERATE
						&& (lastBurst == null || lastBurst.getEndTime() < burst.getBeginTime() - minGap)) {
					double cpuBegin = time0 - minGap;
					double cpuEnd = time0;
					// Check CPU usage
					while (cpuPointer < cpuEventsSize) {
						double eventTimeStamp = cpuEvents.get(cpuPointer).getTimeStamp();
						if (eventTimeStamp < burst.getBeginTime() - USER_EVENT_TOLERATE) {
							++cpuPointer;
						} else {
							break;
						}
					}
					int cpuActivityKey = cpuPointer;
					double totalCpuUsage = 0.0f;
					int cEventsCount = 0;
					while (cpuActivityKey < cpuEventsSize) {
						CpuActivity cpuAct = cpuEvents.get(cpuActivityKey);
						double caTimeStamp = cpuAct.getTimeStamp();
						if (caTimeStamp > cpuBegin && caTimeStamp < cpuEnd) {
							totalCpuUsage += cpuAct.getTotalCpuUsage();
							cEventsCount++;
						}
						if (caTimeStamp >= cpuEnd) {
							break;
						}
						cpuActivityKey++;
					}
					if (cEventsCount > 0 && (totalCpuUsage / cEventsCount) > AVG_CPU_USAGE_THRESHOLD) {
						burst.setBurstInfo(BurstCategory.CPU);
						continue;
					} else {
						burst.setBurstInfo(userInputBurst);
						continue;
					}
				}
			}

			// Step 7: Client delay
			if (burstPayloadLen == 0) {
				burst.setBurstInfo(BurstCategory.UNKNOWN);
				continue;
			} else {
				burst.setBurstInfo(BurstCategory.CLIENT_APP);
				continue;
			}
		}
		return longBurstCount;
	}
	/**
	 * Utility method
	 * 
	 * @param ueTime
	 * @param pTime
	 * @return
	 */
	private boolean withinTolerate(double ueTime, double pTime) {
		return ((ueTime < pTime) && (ueTime > (pTime - USER_EVENT_TOLERATE)));
	}
	/**
	 * Groups packets into Burst Collections
	 * @return 
	 */
	private List<Burst> groupIntoBursts(List<PacketInfo> packets, Profile profile, Set<Integer> mss, 
			List<RrcStateRange> rrcstaterangelist) {
		List<Burst> burstCollection;
		// Validate that there are packets
		if (packets.size() <= 0) {
			burstCollection = Collections.emptyList();
			return burstCollection;
		}
		ArrayList<Burst> result = new ArrayList<Burst>();
		double burstThresh = profile.getBurstTh();
		double longBurstThresh = profile.getLongBurstTh();
		List<PacketInfo> burstPackets = new ArrayList<PacketInfo>();
		// Step 1: Build bursts using burst time threshold
		PacketInfo lastPacket = null;
		for (PacketInfo packet : packets) {
			if ((lastPacket == null
					|| (packet.getTimeStamp() - lastPacket.getTimeStamp() > burstThresh && 
							!mss.contains(lastPacket.getPayloadLen()))) && (!burstPackets.isEmpty())) {

				result.add(new Burst(burstPackets));
				burstPackets.clear();
			}
			burstPackets.add(packet);
			lastPacket = packet;
		}
		result.add(new Burst(burstPackets));

		// Step 2: Remove promotion delays and merge bursts if possible
		Map<PacketInfo, Double> timestampList = normalizeCore(packets, rrcstaterangelist);
		List<Burst> newBurstColl = new ArrayList<Burst>(result.size());
		int size = result.size();
		Burst newBurst = result.get(0);
		for (int i = 0; i < size - 1; i++) {
			Burst bnext = result.get(i + 1);
			double time1 = timestampList.get(newBurst.getEndPacket());
			double time2 = timestampList.get(bnext.getBeginPacket());
			if ((time2 - time1) < burstThresh) {
				newBurst.merge(bnext);
			} else {
				newBurstColl.add(newBurst);
				newBurst = bnext;
			}
		}
		newBurstColl.add(newBurst);
		burstCollection = newBurstColl;


		// determine short/long IBTs
		size = burstCollection.size();
		for (int i = 0; i < size; i++) {
			Burst aBurst = burstCollection.get(i);
//			assert (aBurst.getEndTime() >= aBurst.getBeginTime());
			if (i < size - 1) {
				double ibt = burstCollection.get(i + 1).getBeginTime() - aBurst.getEndTime();
//				assert (ibt >= burstThresh);
				aBurst.setbLong((ibt > longBurstThresh));
			} else {
				aBurst.setbLong(true);
			}
		}
		return burstCollection;
	}
	/**
	 * Method orginally found in whatif.cpp
	 * 
	 * @param packets
	 *            returns timestampList - List of doubles
	 */
	private Map<PacketInfo, Double> normalizeCore(List<PacketInfo> packets, List<RrcStateRange> rrcstaterangelist) {

		// Step 1: Identify Promotions
		List<RrcStateRange> promoDelays = new ArrayList<RrcStateRange>();
		for (RrcStateRange rrc : rrcstaterangelist) {
			RRCState state = rrc.getState();
			if ((state == RRCState.PROMO_FACH_DCH) || (state == RRCState.PROMO_IDLE_DCH)) {
				promoDelays.add(rrc);
			}
		}
		Collections.sort(promoDelays);
		PacketTimestamp[] timeStampList = new PacketTimestamp[packets.size()];
		for (int i = 0; i < packets.size(); i++) {
			timeStampList[i] = new PacketTimestamp(packets.get(i));
		}

		// Step 2: Remove all promo delays
		int pdSize = promoDelays.size();
		double timeStampShift = 0.0f;
		int pdKey = 0;
		int pdMiddlePosKey = -1; // "in-the-middle" position
		double middlePos = 0; // How to initialize??
		for (int i = 0; i < timeStampList.length; i++) {
			double timeStamp = timeStampList[i].timestamp;
			while (pdKey < pdSize && timeStamp >= promoDelays.get(pdKey).getEndTime() - EPS) {
				if (pdMiddlePosKey != -1) {
//					assert (pdMiddlePosKey == pdKey && i > 0 && promoDelays.get(pdKey).getEndTime() >= middlePos);
					timeStampShift += promoDelays.get(pdKey).getEndTime() - middlePos;
					pdMiddlePosKey = -1;
				} else {
					timeStampShift += promoDelays.get(pdKey).getEndTime()
							- promoDelays.get(pdKey).getBeginTime();
				}
				pdKey++;
			}
			if (pdKey < pdSize && (promoDelays.get(pdKey).getBeginTime() - EPS) < timeStamp
					&& timeStamp < (promoDelays.get(pdKey).getEndTime() + EPS)) {
				if (pdMiddlePosKey == -1) {
					timeStampShift += timeStamp - promoDelays.get(pdKey).getBeginTime();
					middlePos = timeStamp;
					pdMiddlePosKey = pdKey;
				} else {
//					assert (pdMiddlePosKey == pdKey && i > 0);
//					assert (timeStamp >= middlePos);
					timeStampShift += timeStamp - middlePos;
					middlePos = timeStamp;
				}
			}
			timeStampList[i].timestamp = timeStampList[i].timestamp - timeStampShift;
//			assert (i == 0 || timeStampList[i].timestamp >= timeStampList[i - 1].timestamp);
		}
		Map<PacketInfo, Double> result = new LinkedHashMap<PacketInfo, Double>(timeStampList.length);
		for (int i = 0; i < timeStampList.length; ++i) {
			result.put(timeStampList[i].packet, timeStampList[i].timestamp);
		}
		return result;
	}

	/**
	 * Method to calculate the maximum segment size of each packets.
	 * 
	 * @return Set of mss values of the packets.
	 */
	private Set<Integer> calculateMssLargerPacketSizeSet(Map<Integer, Integer> packetSizeToCountMap) {
		Set<Integer> mssLargerPacketSizeSet = new HashSet<Integer>();
		long totLargePkts = 0;
		if(packetSizeToCountMap != null){
			for (Map.Entry<Integer, Integer> entry : packetSizeToCountMap.entrySet()) {
				Integer keyValuePacketSize = entry.getKey();
				Integer valueCount = entry.getValue();
				if ((keyValuePacketSize > 1000) && (valueCount > 1)) {
					totLargePkts += valueCount;
				}
			}
		}
		
		if (totLargePkts > 0) {
			for (Map.Entry<Integer, Integer> entry : packetSizeToCountMap.entrySet()) {
				Integer keyValuePacketSize = entry.getKey();
				Integer valueCount = entry.getValue();
				if ((keyValuePacketSize > 1000) && (valueCount > 1)) {
					double fractionLargePkts = (double) valueCount / (double) totLargePkts;
					if (fractionLargePkts > 0.3f) {
						mssLargerPacketSizeSet.add(keyValuePacketSize);
					}
				}
			}
		} else {
			Integer keyValuePacketSize = 1460;
			mssLargerPacketSizeSet.add(keyValuePacketSize);
		}
		return mssLargerPacketSizeSet;
	}
	//helper classes
	/**
	 * Private utility class
	 */
	private class PacketTimestamp {
		private PacketInfo packet;
		private double timestamp;

		public PacketTimestamp(PacketInfo packet) {
			this.packet = packet;
			this.timestamp = packet.getTimeStamp();
		}
	}

	
}
