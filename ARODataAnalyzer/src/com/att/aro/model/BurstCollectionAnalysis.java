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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.att.aro.model.PacketInfo.Direction;
import com.att.aro.model.PacketInfo.TcpInfo;
import com.att.aro.model.UserEvent.UserEventType;
import com.att.aro.pcap.IPPacket;
import com.att.aro.pcap.Packet;

/**
 * Contains methods for analyzing the information from all of the bursts in the
 * trace data.
 * <p>
 * The BurstCollectionAnalysis class contains functionality for collecting the
 * bursts from the trace data and storeing them in a collection of Burst
 * objects, analyzing each burst and categorizing them, and performing analysis
 * on the bursts.
 */
public class BurstCollectionAnalysis implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double EPS = 1 * Math.pow(10, -6);
	private static final double USER_EVENT_TOLERATE = 4.0f;
	private static final double AVG_CPU_USAGE_THRESHOLD = 0.7;

	private TraceData.Analysis analysis;
	private Profile profile;
	private Set<Integer> mss = new HashSet<Integer>();
	private List<Burst> burstCollection;
	private Set<String> hostPeriodicInfoSet = new HashSet<String>();
	private double totalEnergy;
	private int longBurstCount = 0;
	private int tightlyCoupledBurstCount = 0;
	private double tightlyCoupledBurstTime = 0;
	private int periodicCount = 0;
	private int diffPeriodicCount = 0;
	private double minimumPeriodicRepeatTime = 0.0;
	private TCPSession shortestPeriodTCPSession = null;
	private PacketInfo shortestPeriodPacketInfo = null;

	// Contains the burst analysis info
	private List<BurstAnalysisInfo> burstAnalysisInfo = new ArrayList<BurstAnalysisInfo>();

	/**
	 * Initializes an instance of the BurstCollectionAnalysis class, using the
	 * specified trace analysis data.
	 * 
	 * @param analysis
	 *            - An Analysis object containing the trace analysis data.
	 */
	public BurstCollectionAnalysis(TraceData.Analysis analysis) {
		this.analysis = analysis;
		this.profile = analysis.getProfile();
		this.mss = calculateMssLargerPacketSizeSet();
		groupIntoBursts();
		if (this.burstCollection.size() > 0) {
			analyzeBursts();
			computeBurstEnergyRadioResource();
			diagnosisPeriodicRequest();
			analyzeBurstStat(analysis);
			validateUnnecessaryConnections();
			validatePeriodicConnections();
		}
	}

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

	/**
	 * Private utility class to keep the time stamp of tcpinfo's and HTTP
	 * request response info's
	 */
	private class RequestEvent implements Comparable<RequestEvent> {
		private double ts;

		@Override
		public int compareTo(RequestEvent event) {
			return Double.valueOf(this.ts).compareTo(event.ts);
		}
	}

	/**
	 * Bean class to contain the information of Request Event's Inter Arrival
	 * Time.
	 */
	private class IatInfo {
		private double iat;
		private double beginTime;
		private int beginEvent;
		private int endEvent;
	}

	private class IatInfoSortByBasicTime2 implements Comparator<IatInfo> {
		@Override
		public int compare(IatInfo o1, IatInfo o2) {
			return Double.valueOf(o1.beginTime).compareTo(o2.beginTime);
		}
	}

	private class IatInfoSortByBasicTime1 implements Comparator<IatInfo> {

		@Override
		public int compare(IatInfo o1, IatInfo o2) {
			return Double.valueOf(o1.iat).compareTo(o2.iat);
		}
	}

	/**
	 * Returns the collection of bursts.
	 * 
	 * @return A List of Burst objects.
	 */
	public List<Burst> getBurstCollection() {
		return Collections.unmodifiableList(burstCollection);
	}

	/**
	 * Gets the burst analysis information.
	 * 
	 * @return A List of BurstAnalysisInformation objects.
	 */
	public List<BurstAnalysisInfo> getBurstAnalysisInfo() {
		return Collections.unmodifiableList(burstAnalysisInfo);
	}

	/**
	 * Returns the number of long bursts in the collection.
	 * 
	 * @return Returns a count of the long bursts in the collection.
	 */
	public int getLongBurstCount() {
		return longBurstCount;
	}

	/**
	 * Returns the periodic burst count difference.
	 * 
	 * @return A count of periodic burst differences.
	 */
	public int getDiffPeriodicCount() {
		return diffPeriodicCount;
	}

	/**
	 * Returns the number of periodic bursts in the collection.
	 * 
	 * @return Returns a count of the periodic bursts in the collection.
	 */
	public int getPeriodicCount() {
		return periodicCount;
	}

	/**
	 * Returns the shortest repeat time among periodic bursts.
	 * 
	 * @return A double that is the minimum repeat time for periodic bursts.
	 */
	public double getMinimumPeriodicRepeatTime() {
		return minimumPeriodicRepeatTime;
	}

	/**
	 * Returns the count of tightly coupled bursts in the collection.
	 * 
	 * @return The tightly coupled burst count.
	 */
	public int getTightlyCoupledBurstCount() {
		return tightlyCoupledBurstCount;
	}

	/**
	 * Returns the total time of all tightly coupled bursts in the collection.
	 * 
	 * @return The tightly coupled burst time.
	 */
	public double getTightlyCoupledBurstTime() {
		return tightlyCoupledBurstTime;
	}

	/**
	 * Returns the TCP session information for the shortest periodic burst.
	 * 
	 * @return A TCPSession object containing the TCP session information for
	 *         the shortest periodic burst.
	 */
	public TCPSession getShortestPeriodTCPSession() {
		return shortestPeriodTCPSession;
	}

	/**
	 * Returns the packet information for the shortest periodic burst.
	 * 
	 * @return A PacketInfo object containing the packet information for the
	 *         shortest periodic burst
	 */
	public PacketInfo getShortestPeriodPacketInfo() {
		return shortestPeriodPacketInfo;
	}

	/**
	 * Returns the total energy of all bursts in the collection.
	 * 
	 * @return The total burst energy.
	 */
	public double getTotalEnergy() {
		return totalEnergy;
	}

	/**
	 * Method to calculate the maximum segment size of each packets.
	 * 
	 * @return Set of mss values of the packets.
	 */
	private Set<Integer> calculateMssLargerPacketSizeSet() {
		Set<Integer> mssLargerPacketSizeSet = new HashSet<Integer>();
		long totLargePkts = 0;
		Map<Integer, Integer> packetSizeToCountMap = analysis.getPacketSizeToCountMap();
		for (Map.Entry<Integer, Integer> entry : packetSizeToCountMap.entrySet()) {
			Integer keyValuePacketSize = entry.getKey();
			Integer valueCount = entry.getValue();
			if ((keyValuePacketSize > 1000) && (valueCount > 1)) {
				totLargePkts += valueCount;
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

	/**
	 * Groups packets into Burst Collections
	 */
	private void groupIntoBursts() {
		// Validate that there are packets
		List<PacketInfo> packets = this.analysis.getPackets();
		if (packets.size() <= 0) {
			this.burstCollection = Collections.emptyList();
			return;
		}
		ArrayList<Burst> result = new ArrayList<Burst>();
		double burstThresh = profile.getBurstTh();
		double longBurstThresh = profile.getLongBurstTh();
		List<PacketInfo> burstPackets = new ArrayList<PacketInfo>();
		// Step 1: Build bursts using burst time threshold
		PacketInfo lastPacket = null;
		for (PacketInfo packet : packets) {
			if (lastPacket == null
					|| (packet.getTimeStamp() - lastPacket.getTimeStamp() > burstThresh && !mss
							.contains(lastPacket.getPayloadLen()))) {
				if (burstPackets.size() > 0) {
					result.add(new Burst(burstPackets));
					burstPackets.clear();
				}
			}
			burstPackets.add(packet);
			lastPacket = packet;
		}
		result.add(new Burst(burstPackets));

		// Step 2: Remove promotion delays and merge bursts if possible
		Map<PacketInfo, Double> timestampList = normalizeCore(packets);
		List<Burst> newBurstColl = new ArrayList<Burst>(result.size());
		int n = result.size();
		Burst newBurst = result.get(0);
		for (int i = 0; i < n - 1; i++) {
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
		this.burstCollection = newBurstColl;

		// Step 3: compute burstID for each packet
		n = burstCollection.size();
		for (Burst b : burstCollection) {
			for (PacketInfo p : b.getPackets()) {
				p.setBurst(b);
			}
		}

		// Step 4: determine short/long IBTs
		n = burstCollection.size();
		for (int i = 0; i < n; i++) {
			Burst b = burstCollection.get(i);
			assert (b.getEndTime() >= b.getBeginTime());
			if (i < n - 1) {
				double ibt = burstCollection.get(i + 1).getBeginTime() - b.getEndTime();
				assert (ibt >= burstThresh);
				b.setbLong((ibt > longBurstThresh));
			} else {
				b.setbLong(true);
			}
		}
	}

	/**
	 * Method orginally found in whatif.cpp
	 * 
	 * @param packets
	 *            returns timestampList - List of doubles
	 */
	private Map<PacketInfo, Double> normalizeCore(List<PacketInfo> packets) {

		// Step 1: Identify Promotions
		List<RrcStateRange> promoDelays = new ArrayList<RrcStateRange>();
		for (RrcStateRange rrc : analysis.getRrcStateMachine().getRRcStateRanges()) {
			RRCState state = rrc.getState();
			if (state == RRCState.PROMO_FACH_DCH || state == RRCState.PROMO_IDLE_DCH)
				promoDelays.add(rrc);
		}
		Collections.sort(promoDelays);
		PacketTimestamp[] timeStampList = new PacketTimestamp[packets.size()];
		for (int i = 0; i < packets.size(); i++) {
			timeStampList[i] = new PacketTimestamp(packets.get(i));
		}

		// Step 2: Remove all promo delays
		int m = promoDelays.size();
		double timeStampShift = 0.0f;
		int j = 0;
		int j0 = -1; // "in-the-middle" position
		double middlePos = 0; // How to initialize??
		for (int i = 0; i < timeStampList.length; i++) {
			double timeStamp = timeStampList[i].timestamp;
			while (j < m && timeStamp >= promoDelays.get(j).getEndTime() - EPS) {
				if (j0 != -1) {
					assert (j0 == j && i > 0 && promoDelays.get(j).getEndTime() >= middlePos);
					timeStampShift += promoDelays.get(j).getEndTime() - middlePos;
					j0 = -1;
				} else {
					timeStampShift += promoDelays.get(j).getEndTime()
							- promoDelays.get(j).getBeginTime();
				}
				j++;
			}
			if (j < m && (promoDelays.get(j).getBeginTime() - EPS) < timeStamp
					&& timeStamp < (promoDelays.get(j).getEndTime() + EPS)) {
				if (j0 == -1) {
					timeStampShift += timeStamp - promoDelays.get(j).getBeginTime();
					middlePos = timeStamp;
					j0 = j;
				} else {
					assert (j0 == j && i > 0);
					assert (timeStamp >= middlePos);
					timeStampShift += timeStamp - middlePos;
					middlePos = timeStamp;
				}
			}
			timeStampList[i].timestamp = timeStampList[i].timestamp - timeStampShift;
			assert (i == 0 || timeStampList[i].timestamp >= timeStampList[i - 1].timestamp);
		}
		Map<PacketInfo, Double> result = new LinkedHashMap<PacketInfo, Double>(timeStampList.length);
		for (int i = 0; i < timeStampList.length; ++i) {
			result.put(timeStampList[i].packet, timeStampList[i].timestamp);
		}
		return result;
	}

	/**
	 * Computes the total burst energy.
	 */
	private void computeBurstEnergyRadioResource() {
		List<RrcStateRange> rrcCollection = analysis.getRrcStateMachine().getRRcStateRanges();
		int rrcCount = rrcCollection.size();
		if (rrcCount == 0) {
			return;
		}
		int p = 0;
		double time2 = -1;
		double totalEnergy = 0.0f;
		Iterator<Burst> iter = burstCollection.iterator();
		Burst currentBurst = iter.next();
		double time1 = rrcCollection.get(0).getBeginTime();
		while (true) {
			Burst nextBurst = iter.hasNext() ? iter.next() : null;
			time2 = nextBurst != null ? nextBurst.getBeginTime() : rrcCollection.get(rrcCount - 1)
					.getEndTime();
			double e = 0.0f;
			double dchTime = 0.0f;
			double crTime = 0.0f;
			while (p < rrcCount) {
				RrcStateRange rrCntrl = rrcCollection.get(p);
				if (rrCntrl.getEndTime() < time1) {
					p++;
				} else {
					if (time2 > rrCntrl.getEndTime()) {
						e += profile.energy(time1, rrCntrl.getEndTime(), rrCntrl.getState(),
								analysis.getPackets());
						if (rrCntrl.getState() == RRCState.STATE_DCH
								|| rrCntrl.getState() == RRCState.TAIL_DCH) {
							dchTime += rrCntrl.getEndTime() - time1;
						} else if (rrCntrl.getState() == RRCState.LTE_CONTINUOUS
								|| rrCntrl.getState() == RRCState.LTE_CR_TAIL) {
							crTime += rrCntrl.getEndTime() - time1;
						}
						p++;
					}
					break;
				}
			}
			while (p < rrcCount) {
				RrcStateRange rrCntrl = rrcCollection.get(p);
				if (rrCntrl.getEndTime() < time2) {
					e += profile.energy(Math.max(rrCntrl.getBeginTime(), time1),
							rrCntrl.getEndTime(), rrCntrl.getState(), analysis.getPackets());
					if (rrCntrl.getState() == RRCState.STATE_DCH
							|| rrCntrl.getState() == RRCState.TAIL_DCH) {
						dchTime += rrCntrl.getEndTime() - Math.max(rrCntrl.getBeginTime(), time1);
					} else if (rrCntrl.getState() == RRCState.LTE_CONTINUOUS
							|| rrCntrl.getState() == RRCState.LTE_CR_TAIL) {
						crTime += rrCntrl.getEndTime() - Math.max(rrCntrl.getBeginTime(), time1);
					}
					p++;
				} else {
					e += profile.energy(Math.max(rrCntrl.getBeginTime(), time1), time2,
							rrCntrl.getState(), analysis.getPackets());
					if (rrCntrl.getState() == RRCState.STATE_DCH
							|| rrCntrl.getState() == RRCState.TAIL_DCH) {
						dchTime += time2 - Math.max(rrCntrl.getBeginTime(), time1);
					} else if (rrCntrl.getState() == RRCState.LTE_CONTINUOUS
							|| rrCntrl.getState() == RRCState.LTE_CR_TAIL) {
						crTime += time2 - Math.max(rrCntrl.getBeginTime(), time1);
					}
					break;
				}
			}
			currentBurst.setEnergy(e);
			totalEnergy += e;
			currentBurst.setDchTime(dchTime);
			currentBurst.setCrTime(crTime);
			time1 = time2;
			if (nextBurst != null) {
				currentBurst = nextBurst;
			} else {
				break;
			}
		}
		this.totalEnergy = totalEnergy;
	}

	/**
	 * Method to assign the states for all the bursts.
	 * 
	 * @param analyzeBeginTime
	 * @param analyseEndTime
	 */
	private void analyzeBurstStat(TraceData.Analysis analysis) {
		Map<BurstCategory, Double> burstCategoryToEnergy = new EnumMap<BurstCategory, Double>(
				BurstCategory.class);
		Map<BurstCategory, Long> burstCategoryToPayload = new EnumMap<BurstCategory, Long>(
				BurstCategory.class);
		Map<BurstCategory, Double> burstCategoryToDch = new EnumMap<BurstCategory, Double>(
				BurstCategory.class);
		Map<BurstCategory, Double> burstCategoryToCR = new EnumMap<BurstCategory, Double>(
				BurstCategory.class);

		long totalPayload = 0;
		double totalDCH = 0.0;
		double totalCR = 0.0;
		double totalEnergy = 0.0;
		for (Burst b : burstCollection) {
			BurstCategory category = b.getBurstCategory();
			double energy = b.getEnergy();
			totalEnergy += energy;
			Double catEnergy = burstCategoryToEnergy.get(category);
			double d = catEnergy != null ? catEnergy.doubleValue() : 0.0;
			d += energy;
			burstCategoryToEnergy.put(category, d);
			int p1 = getPayloadLength(b, false);
			totalPayload += p1;
			Long payload = burstCategoryToPayload.get(category);
			long l = payload != null ? payload.longValue() : 0L;
			l += p1;
			burstCategoryToPayload.put(category, l);
			double dchTime = b.getDchTime();
			double crTime = b.getCrTime();
			totalDCH += dchTime;
			totalCR += crTime;
			Double catDch = burstCategoryToDch.get(category);
			d = catDch != null ? catDch.doubleValue() : 0.0;
			d += dchTime;
			burstCategoryToDch.put(category, d);
			Double catCr = burstCategoryToCR.get(category);
			d = catCr != null ? catCr.doubleValue() : 0.0;
			d += crTime;
			burstCategoryToCR.put(category, d);

		}
		{
			long p1 = getPayloadLenBkg();
			burstCategoryToPayload.put(BurstCategory.BURSTCAT_BKG, p1);
			totalPayload += p1;
		}
		for (Map.Entry<BurstCategory, Double> entry : burstCategoryToEnergy.entrySet()) {
			BurstCategory categ = entry.getKey();
			long catPayload = burstCategoryToPayload.get(categ);
			double catEnergy = burstCategoryToEnergy.get(categ);
			double catDCH = burstCategoryToDch.get(categ);
			double catCr = burstCategoryToCR.get(categ);
			Double jpkb = catPayload > 0 ? catEnergy / (catPayload * 8 / 1000.0f) : null;
			if (analysis.getProfile() instanceof Profile3G) {
				burstAnalysisInfo.add(new BurstAnalysisInfo(categ, catPayload,
						((double) catPayload / totalPayload) * 100.0, catEnergy,
						(catEnergy / totalEnergy) * 100.0, catDCH, (catDCH / totalDCH) * 100.0,
						jpkb));
			} else if (analysis.getProfile() instanceof ProfileLTE) {
				burstAnalysisInfo.add(new BurstAnalysisInfo(categ, catPayload,
						((double) catPayload / totalPayload) * 100.0, catEnergy,
						(catEnergy / totalEnergy) * 100.0, catCr, (catCr / totalCR) * 100.0, jpkb));
			}
		}
	}

	/**
	 * Method to assign the burst states.
	 */
	private void analyzeBursts() {
		List<UserEvent> userEvents = analysis.getUserEvents();
		List<CpuActivity> cpuEvents = analysis.getCpuActivityList();
		int userEventsSize = userEvents.size();
		int cpuEventsSize = cpuEvents.size();
		int userEventPointer = 0;
		int cpuPointer = 0;
		// Analyze each burst
		Burst b = null;
		Burst lastBurst;
		for (Iterator<Burst> i = burstCollection.iterator(); i.hasNext();) {
			lastBurst = b;
			b = i.next();
			// Step 1: Remove background packets
			List<PacketInfo> pktIdx = new ArrayList<PacketInfo>(b.getPackets().size());
			int payloadLen = 0;
			Set<TcpInfo> tcpInfo = new HashSet<TcpInfo>();
			for (PacketInfo p : b.getPackets()) {
				if (p.getAppName() != null) {
					payloadLen += p.getPayloadLen();
					pktIdx.add(p);
					TcpInfo tcp = p.getTcpInfo();
					if (tcp != null) {
						tcpInfo.add(tcp);
					}
				}
			}
			if (pktIdx.size() == 0) {
				assert (payloadLen == 0);
				b.addBurstInfo(BurstInfo.BURST_BKG);
				continue;
			}
			PacketInfo pkt0 = pktIdx.get(0);
			TcpInfo info0 = pkt0.getTcpInfo();
			double time0 = pkt0.getTimeStamp();

			// Step 2: a long burst?
			if (b.getEndTime() - b.getBeginTime() > profile.getLargeBurstDuration()
					&& payloadLen > profile.getLargeBurstSize()) {
				b.addBurstInfo(BurstInfo.BURST_LONG);
				++longBurstCount;
				continue;
			}

			// Step 3: Contains no payload?
			if (payloadLen == 0) {
				if (tcpInfo.contains(TcpInfo.TCP_CLOSE)) {
					b.addBurstInfo(BurstInfo.BURST_FIN);
				}
				if (tcpInfo.contains(TcpInfo.TCP_ESTABLISH)) {
					b.addBurstInfo(BurstInfo.BURST_SYN);
				}
				if (tcpInfo.contains(TcpInfo.TCP_RESET)) {
					b.addBurstInfo(BurstInfo.BURST_RST);
				}
				if (tcpInfo.contains(TcpInfo.TCP_KEEP_ALIVE)
						|| tcpInfo.contains(TcpInfo.TCP_KEEP_ALIVE_ACK)) {
					b.addBurstInfo(BurstInfo.BURST_KEEPALIVE);
				}
				if (tcpInfo.contains(TcpInfo.TCP_ZERO_WINDOW)) {
					b.addBurstInfo(BurstInfo.BURST_ZEROWIN);
				}
				if (tcpInfo.contains(TcpInfo.TCP_WINDOW_UPDATE)) {
					b.addBurstInfo(BurstInfo.BURST_WINUPDATE);
				}
				if (b.getBurstInfos().size() == 0
						&& (info0 == TcpInfo.TCP_ACK_RECOVER || info0 == TcpInfo.TCP_ACK_DUP)) {
					b.addBurstInfo(BurstInfo.BURST_LOSS_RECOVER);
				}
				if (b.getBurstInfos().size() > 0)
					continue;
			}

			// Step 4: Server delay
			if (pkt0.getDir() == PacketInfo.Direction.DOWNLINK
					&& (info0 == TcpInfo.TCP_DATA || info0 == TcpInfo.TCP_ACK)) {
				b.addBurstInfo(BurstInfo.BURST_SERVER_DELAY);
				continue;
			}

			// Step 5: Loss recover
			if (info0 == TcpInfo.TCP_ACK_DUP || info0 == TcpInfo.TCP_DATA_DUP) {
				b.addBurstInfo(BurstInfo.BURST_LOSS_DUP);
				continue;
			}

			if (info0 == TcpInfo.TCP_DATA_RECOVER || info0 == TcpInfo.TCP_ACK_RECOVER) {
				b.addBurstInfo(BurstInfo.BURST_LOSS_RECOVER);
				continue;
			}

			// Step 6: User triggered
			final double USER_EVENT_SMALL_TOLERATE = profile.getUserInputTh();
			if (payloadLen > 0) {
				UserEvent ue = null;
				while ((userEventPointer < userEventsSize)
						&& ((ue = userEvents.get(userEventPointer)).getReleaseTime() < (time0 - USER_EVENT_TOLERATE)))
					++userEventPointer;
				BurstInfo bi = ue != null ? ((ue.getEventType() == UserEventType.SCREEN_LANDSCAPE || ue
						.getEventType() == UserEventType.SCREEN_PORTRAIT) ? BurstInfo.BURST_SCREEN_ROTATION_INPUT
						: BurstInfo.BURST_USER_INPUT)
						: null;
				int j = userEventPointer;
				double minGap = Double.MAX_VALUE;
				while (j < userEventsSize) {
					UserEvent uEvent = userEvents.get(j);
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
					j++;
				}
				if (minGap < USER_EVENT_SMALL_TOLERATE) {
					b.addBurstInfo(bi);
					continue;
				} else if (minGap < USER_EVENT_TOLERATE
						&& (lastBurst == null || lastBurst.getEndTime() < b.getBeginTime() - minGap)) {
					double cpuBegin = time0 - minGap;
					double cpuEnd = time0;
					// Check CPU usage
					while (cpuPointer < cpuEventsSize) {
						double t = cpuEvents.get(cpuPointer).getBeginTimeStamp();
						if (t < b.getBeginTime() - USER_EVENT_TOLERATE) {
							++cpuPointer;
						} else {
							break;
						}
					}
					int k = cpuPointer;
					double s = 0.0f;
					int ns = 0;
					while (k < cpuEventsSize) {
						CpuActivity cpuAct = cpuEvents.get(k);
						double t = cpuAct.getBeginTimeStamp();
						if (t > cpuBegin && t < cpuEnd) {
							s += cpuAct.getUsage();
							ns++;
						}
						if (t >= cpuEnd) {
							break;
						}
						k++;
					}
					if (ns > 0 && (s / ns) > AVG_CPU_USAGE_THRESHOLD) {
						b.addBurstInfo(bi);
						b.addBurstInfo(BurstInfo.BURST_CPU_BUSY);
						continue;
					}
				}
			}

			// Step 7: Client delay
			if (b.getBurstInfos().size() == 0 && payloadLen == 0) {
				b.addBurstInfo(BurstInfo.BURST_UNKNOWN);
			} else {
				b.addBurstInfo(BurstInfo.BURST_CLIENT_DELAY);
			}
		}
	}

	/**
	 * Utility method
	 * 
	 * @param ut
	 * @param pt
	 * @return
	 */
	private boolean withinTolerate(double ut, double pt) {
		return ((ut < pt) && (ut > (pt - USER_EVENT_TOLERATE)));
	}

	/**
	 * Burst data's analyzed to categorize the periodic bursts.
	 */
	private void diagnosisPeriodicRequest() {
		Map<String, List<RequestEvent>> requestHost2tsList = new HashMap<String, List<RequestEvent>>();
		Map<String, List<RequestEvent>> requestObj2tsList = new HashMap<String, List<RequestEvent>>();
		Map<InetAddress, List<RequestEvent>> connIP2tsList = new HashMap<InetAddress, List<RequestEvent>>();
		clearHostPeriodicInfoSet();
		periodicCount = 0;
		diffPeriodicCount = 0;
		minimumPeriodicRepeatTime = 0.0;

		for (TCPSession b : analysis.getTcpSessions()) {

			PacketInfo p = b.getPackets().get(0);
			if (p.getTcpInfo() == TcpInfo.TCP_ESTABLISH) {
				RequestEvent re = new RequestEvent();
				re.ts = p.getTimeStamp();
				List<RequestEvent> res = connIP2tsList.get(b.getRemoteIP());
				if (res == null) {
					res = new ArrayList<RequestEvent>();
					connIP2tsList.put(b.getRemoteIP(), res);
				}
				res.add(re);
			}

			for (HttpRequestResponseInfo rr : b.getRequestResponseInfo()) {
				PacketInfo pkt = rr.getFirstDataPacket();
				double ts0 = pkt.getTimeStamp();
				if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST
						&& null != rr.getHostName()) {
					RequestEvent re = new RequestEvent();
					re.ts = ts0;
					List<RequestEvent> tempRequestHostEventList = requestHost2tsList.get(rr
							.getHostName());
					if (tempRequestHostEventList == null) {
						tempRequestHostEventList = new ArrayList<RequestEvent>();
						requestHost2tsList.put(rr.getHostName(), tempRequestHostEventList);
					}
					tempRequestHostEventList.add(re);
				}

				if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST
						&& null != rr.getObjName()) {
					RequestEvent re = new RequestEvent();
					re.ts = ts0;
					String objName = rr.getObjNameWithoutParams();
					List<RequestEvent> tempRequestObjEventList = requestObj2tsList.get(objName);

					if (tempRequestObjEventList == null) {
						tempRequestObjEventList = new ArrayList<RequestEvent>();
						requestObj2tsList.put(objName, tempRequestObjEventList);
					}
					tempRequestObjEventList.add(re);
				}
			}
		}

		Map<String, Integer> hostList = new HashMap<String, Integer>();
		for (Map.Entry<String, List<RequestEvent>> iter : requestHost2tsList.entrySet()) {
			List<RequestEvent> reList = new ArrayList<RequestEvent>();
			List<RequestEvent> requestHostList = iter.getValue();
			Collections.sort(requestHostList);
			if (SelfCorr(requestHostList, reList)) {
				hostList.put(iter.getKey(), 1);
			}
		}

		Map<String, Integer> objList = new HashMap<String, Integer>();
		for (Map.Entry<String, List<RequestEvent>> iter : requestObj2tsList.entrySet()) {
			List<RequestEvent> reList = new ArrayList<RequestEvent>();
			List<RequestEvent> requestObjList = iter.getValue();
			Collections.sort(requestObjList);
			if (SelfCorr(requestObjList, reList)) {
				objList.put(iter.getKey(), 1);
			}
		}

		Map<InetAddress, Integer> ipList = new HashMap<InetAddress, Integer>();
		for (Map.Entry<InetAddress, List<RequestEvent>> iter : connIP2tsList.entrySet()) {
			List<RequestEvent> reList = new ArrayList<RequestEvent>();
			List<RequestEvent> requestIpList = iter.getValue();
			Collections.sort(requestIpList);
			if (SelfCorr(requestIpList, reList)) {
				ipList.put(iter.getKey(), 1);
			}
		}

		for (Burst burst : burstCollection) {
			if (!burst.getBurstInfos().contains(BurstInfo.BURST_CLIENT_DELAY)) {
				continue;
			}
			Packet beginPacket = burst.getBeginPacket().getPacket();
			if (beginPacket instanceof IPPacket) {
				IPPacket ip = (IPPacket) beginPacket;
				if (ipList.containsKey(ip.getDestinationIPAddress())
						|| ipList.containsKey(ip.getSourceIPAddress())) {
					periodicCount++;
					burst.setBurstInfo(BurstInfo.BURST_PERIODICAL);
					if (ipList.containsKey(ip.getDestinationIPAddress())) {
						updateHostPeriodicInfoSet(ip.getDestinationIPAddress().toString());
					} else {
						updateHostPeriodicInfoSet(ip.getSourceIPAddress().toString());
					}
					continue;
				}
			}

			PacketInfo firstUplinkPayloadPacket = null;
			for (PacketInfo p : burst.getPackets()) {
				if (p.getDir() == Direction.UPLINK && p.getPayloadLen() > 0) {
					firstUplinkPayloadPacket = p;
					break;
				}
			}

			for (TCPSession session : analysis.getTcpSessions()) {
				for (HttpRequestResponseInfo rr : session.getRequestResponseInfo()) {
					if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST
							&& (hostList.containsKey(rr.getHostName()) || objList.containsKey(rr
									.getObjNameWithoutParams()))) {
						if (rr.getFirstDataPacket() == firstUplinkPayloadPacket) {
							periodicCount++;
							burst.setBurstInfo(BurstInfo.BURST_PERIODICAL);
							burst.setFirstUplinkDataPacket(firstUplinkPayloadPacket);
							if (hostList.containsKey(rr.getHostName())) {
								updateHostPeriodicInfoSet(rr.getHostName());
							} else {
								updateHostPeriodicInfoSet(rr.getObjNameWithoutParams());
							}
							continue;
						}
					}
				}
			}
		}
		diffPeriodicCount = hostPeriodicInfoSet.size();
	}

	/**
	 * Getter for getting the payload length for the provided burst.
	 * 
	 * @param burst
	 * @param bIncludeBkgApp
	 * @return
	 */
	private int getPayloadLength(Burst burst, boolean bIncludeBkgApp) {
		int r = 0;
		for (PacketInfo p : burst.getPackets()) {
			if (bIncludeBkgApp || p.getAppName() != null) {
				r += p.getPayloadLen();
			}
		}
		return r;
	}

	/**
	 * Getter for the payloadlength of overall packets.
	 * 
	 * @return
	 */
	private long getPayloadLenBkg() {
		long r = 0;
		for (PacketInfo p : analysis.getPackets()) {
			if (p.getAppName() == null) {
				r += p.getPayloadLen();
			}
		}
		return r;
	}

	/**
	 * method to update the subV request event list also to return the cycle or
	 * request.
	 * 
	 * @param v
	 * @param subV
	 * @return
	 */
	private boolean SelfCorr(List<RequestEvent> v, List<RequestEvent> subV) {
		int n = v.size();
		subV.clear();
		if (n <= 3) {
			return false;
		}

		List<IatInfo> c = new ArrayList<IatInfo>();
		for (int i = 0; i < n - 1; i++) {
			for (int j = i + 1; j < n; j++) {
				IatInfo ii = new IatInfo();
				ii.beginTime = v.get(i).ts;
				ii.iat = v.get(j).ts - ii.beginTime;
				ii.beginEvent = i;
				ii.endEvent = j;
				c.add(ii);
			}

		}
		Collections.sort(c, new IatInfoSortByBasicTime1());

		double minPeriod = profile.getPeriodMinCycle();
		double clusterDurationTh = profile.getPeriodCycleTol(); // tolerable
																// cluster size
																// (sec)
		int clusterSizeTh = profile.getPeriodMinSamples();/* n/2 */

		int m = c.size();
		List<IatInfo> cluster = new ArrayList<IatInfo>();
		List<IatInfo> bestCluster = new ArrayList<IatInfo>();
		List<IatInfo> subCluster = new ArrayList<IatInfo>();
		int bestNonOverlapSize = 0;
		for (int i = 0; i < m; i++) {
			cluster.clear();
			int j = i;
			while ((j < m) && ((c.get(j).iat - c.get(i).iat) < clusterDurationTh)) {
				cluster.add(c.get(j++));
			}
			int nonOverlapSize = GetNonOverlapSize(cluster, subCluster);
			if (getAverage(cluster) > minPeriod && nonOverlapSize > bestNonOverlapSize) {
				bestCluster = new ArrayList<IatInfo>(cluster);
				bestNonOverlapSize = nonOverlapSize;
			}
		}

		int r = GetNonOverlapSize(bestCluster, subCluster);
		if (r < clusterSizeTh) {
			return false;
		} else {
			int nSize = subCluster.size();
			subV.add(v.get(subCluster.get(0).beginEvent));
			for (int i = 0; i < nSize - 1; i++) {
				subV.add(v.get(subCluster.get(i).endEvent));
			}
			double cycle = getAverage(bestCluster);
			return cycle > 0;
		}
	}

	/**
	 * Method to calculate the over lap events in burst.
	 */
	private int GetNonOverlapSize(List<IatInfo> v, List<IatInfo> subV) {

		Collections.sort(v, new IatInfoSortByBasicTime2());

		// find the longest path
		List<Integer> opt = new ArrayList<Integer>();
		int n = v.size();
		List<Integer> backTrack = new ArrayList<Integer>();

		int best = -1;
		int bestI = -1;

		for (int i = 0; i < n; i++) {
			int o = 1;
			int b = -1;

			for (int j = 0; j <= i - 1; j++) {
				if ((v.get(j).endEvent == v.get(i).beginEvent) && ((opt.get(j) + 1) > o)) {
					o = opt.get(j) + 1;
					b = j;
				}
			}

			if (o > best) {
				best = o;
				bestI = i;
			}

			opt.add(o);
			backTrack.add(b);
		}

		List<Integer> idxList = new ArrayList<Integer>();
		int i = bestI;
		while (i != -1) {
			idxList.add(i);
			i = backTrack.get(i);
		}

		subV.clear();
		int m = idxList.size();
		for (int ki = m - 1; ki >= 0; ki--) {
			subV.add(v.get(idxList.get(ki)));
		}
		return best;
	}

	/**
	 * method to calculate the average.
	 * 
	 * @param v
	 * @return
	 */
	private double getAverage(List<IatInfo> v) {
		double sum = 0;
		for (IatInfo iatInfo : v) {
			sum += iatInfo.iat;
		}
		return sum / v.size();
	}

	/**
	 * To Validate the simultaneous TCP connections
	 */
	private void validateUnnecessaryConnections() {
		int setCount = 0;
		int maxCount = 0;
		Burst maxBurst = null;
		for (int i = 0; i < burstCollection.size(); ++i) {
			Burst burstInfo = burstCollection.get(i);
			if (burstInfo.getBurstCategory() == BurstCategory.BURSTCAT_USER
					|| burstInfo.getBurstCategory() == BurstCategory.BURSTCAT_SCREEN_ROTATION) {
				continue;
			}
			double startTime = burstInfo.getBeginTime();
			double endTime = startTime + 60.0;
			int count = 1;
			for (int j = i + 1; j < burstCollection.size()
					&& burstCollection.get(j).getEndTime() <= endTime; ++j) {
				if (burstCollection.get(j).getBurstCategory() != BurstCategory.BURSTCAT_USER
						|| burstInfo.getBurstCategory() == BurstCategory.BURSTCAT_SCREEN_ROTATION) {
					++count;
				}
			}
			if (count >= 4) {
				++setCount;
				if (count > maxCount) {
					maxCount = count;
					maxBurst = burstInfo;
				}
				i = i + count;
			} else if (count == 3) {
				endTime = startTime + 15.0;
				count = 1;
				for (int j = i + 1; j < burstCollection.size()
						&& burstCollection.get(j).getEndTime() <= endTime; ++j) {
					if (burstCollection.get(j).getBurstCategory() != BurstCategory.BURSTCAT_USER
							|| burstInfo.getBurstCategory() == BurstCategory.BURSTCAT_SCREEN_ROTATION) {
						++count;
					}
				}
				if (count >= 3) {
					++setCount;
					if (count > maxCount) {
						maxCount = count;
						maxBurst = burstInfo;
					}
					i = i + count;
				}
			}
		}

		tightlyCoupledBurstCount = setCount;
		if (maxBurst != null) {
			tightlyCoupledBurstTime = maxBurst.getBeginTime();
		}
	}

	/**
	 * Method to find the different periodic connection and periodic duration.
	 */
	private void validatePeriodicConnections() {
		int burstSize = burstCollection.size();
		Burst lastPeriodicalBurst = null;
		int periodicCount = 0;
		double minimumRepeatTime = Double.MAX_VALUE;
		PacketInfo packetId = null;
		for (int i = 0; i < burstSize; i++) {
			Burst burst = burstCollection.get(i);
			if (burst.getBurstCategory() == BurstCategory.BURSTCAT_PERIODICAL) {
				if (periodicCount != 0) {
					double time = burst.getBeginTime() - lastPeriodicalBurst.getBeginTime();
					if (time < minimumRepeatTime) {
						minimumRepeatTime = time;
						packetId = burst.getFirstUplinkDataPacket();
						if (packetId == null) {
							packetId = burst.getBeginPacket();
						}
					}
				}
				lastPeriodicalBurst = burst;
				periodicCount++;
			}
		}

		if (packetId != null) {
			shortestPeriodPacketInfo = packetId;
			shortestPeriodTCPSession = packetId.getSession();
		}
		if (minimumRepeatTime != Double.MAX_VALUE) {
			minimumPeriodicRepeatTime = minimumRepeatTime;
		}
	}

	private void clearHostPeriodicInfoSet() {
		hostPeriodicInfoSet.clear();
	}

	private void updateHostPeriodicInfoSet(String strKey) {
		if (strKey != null) {
			if (!hostPeriodicInfoSet.contains(strKey)) {
				hostPeriodicInfoSet.add(strKey);
			}
		}
	}
}
