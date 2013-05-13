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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.att.aro.model.PacketInfo.Direction;
import com.att.aro.model.PacketInfo.TcpInfo;
import com.att.aro.model.UserEvent.UserEventType;
import com.att.aro.model.cpu.CpuActivity;
import com.att.aro.pcap.IPPacket;
import com.att.aro.pcap.Packet;
import com.att.aro.util.Util;

/**
 * Contains methods for analyzing the information from all of the bursts in the
 * trace data.
 * <p>
 * The BurstCollectionAnalysis class contains functionality for collecting the
 * bursts from the trace data, storing them in a collection of Burst
 * objects, analyzing each burst and categorizing them, and performing analysis
 * on the bursts.
 */
public class BurstCollectionAnalysis implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(BurstCollectionAnalysis.class.getName());

	private static final double EPS = 1 * Math.pow(10, -6);
	private static final double USER_EVENT_TOLERATE = 4.0f;
	private static final double AVG_CPU_USAGE_THRESHOLD = 70.0f;

	private TraceData.Analysis analysis;
	private Profile profile;
	private Set<Integer> mss = new HashSet<Integer>();
	private List<Burst> burstCollection;
	private double totalEnergy;
	private int longBurstCount = 0;
	private int tightlyCoupledBurstCount = 0;
	private double tightlyCoupledBurstTime = 0;
	private int periodicCount = 0;
	private int diffPeriodicCount = 0;
	private double minimumPeriodicRepeatTime = 0.0;
	private TCPSession shortestPeriodTCPSession = null;
	private PacketInfo shortestPeriodPacketInfo = null;
	
	private static final String LOG_MSG1 = "Burst set to: {0}";

	/*
	 *  Maximum number of requests the algorithm can process in reasonable time.
	 *  Testings have shown that the number should be smaller than 400 or even smaller for large traces.
	 */
	private static final int MAX_NUM_OF_REQUESTS = Integer.parseInt(Util.RB.getString("max.number.of.requests"));

	private static final int SMALL_PERIODICITY = Integer.parseInt(Util.RB.getString("small.periodicity"));;

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

	public BurstCollectionAnalysis() {
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
	 * Bean class to contain the information of Request Event's Inter Arrival
	 * Time.
	 */
	class IatInfo {
		private double iat;
		private double beginTime;
		private int beginEvent;
		private int endEvent;
		
		IatInfo(double iat, double beginTime, int beginEvent, int endEvent) {
			super();
			this.iat = iat;
			this.beginTime = beginTime;
			this.beginEvent = beginEvent;
			this.endEvent = endEvent;
		}

		public IatInfo() {
		}
		
		
	}

	private class SortByBeginTime implements Comparator<IatInfo> {
		@Override
		public int compare(IatInfo o1, IatInfo o2) {
			return Double.valueOf(o1.beginTime).compareTo(o2.beginTime);
		}
	}

	private class SortByIAT implements Comparator<IatInfo> {

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
			double activeTime = 0.0f;
			while (p < rrcCount) {
				RrcStateRange rrCntrl = rrcCollection.get(p);
				if (rrCntrl.getEndTime() < time1) {
					p++;
				} else {
					if (time2 > rrCntrl.getEndTime()) {
						e += profile.energy(time1, rrCntrl.getEndTime(), rrCntrl.getState(),
								analysis.getPackets());
						if ((rrCntrl.getState() == RRCState.STATE_DCH || rrCntrl.getState() == RRCState.TAIL_DCH)
								|| (rrCntrl.getState() == RRCState.LTE_CONTINUOUS || rrCntrl
										.getState() == RRCState.LTE_CR_TAIL)
								|| (rrCntrl.getState() == RRCState.WIFI_ACTIVE || rrCntrl
										.getState() == RRCState.WIFI_TAIL)) {
							activeTime += rrCntrl.getEndTime() - time1;
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
					if ((rrCntrl.getState() == RRCState.STATE_DCH || rrCntrl.getState() == RRCState.TAIL_DCH)
							|| (rrCntrl.getState() == RRCState.LTE_CONTINUOUS || rrCntrl.getState() == RRCState.LTE_CR_TAIL)
							|| (rrCntrl.getState() == RRCState.WIFI_ACTIVE || rrCntrl.getState() == RRCState.WIFI_TAIL)) {
						activeTime += rrCntrl.getEndTime()
								- Math.max(rrCntrl.getBeginTime(), time1);
					}
					p++;
				} else {
					e += profile.energy(Math.max(rrCntrl.getBeginTime(), time1), time2,
							rrCntrl.getState(), analysis.getPackets());
					if ((rrCntrl.getState() == RRCState.STATE_DCH || rrCntrl.getState() == RRCState.TAIL_DCH)
							|| (rrCntrl.getState() == RRCState.LTE_CONTINUOUS || rrCntrl.getState() == RRCState.LTE_CR_TAIL)
							|| (rrCntrl.getState() == RRCState.WIFI_ACTIVE || rrCntrl.getState() == RRCState.WIFI_TAIL)) {
						activeTime += time2 - Math.max(rrCntrl.getBeginTime(), time1);
					}
					break;
				}
			}
			currentBurst.setEnergy(e);
			totalEnergy += e;
			currentBurst.setActiveTime(activeTime);

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
		Map<BurstCategory, Double> burstCategoryToActive = new EnumMap<BurstCategory, Double>(
				BurstCategory.class);

		long totalPayload = 0;
		double totalAct = 0.0;
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

			double activeTime = b.getActiveTime();
			totalAct += activeTime;
			Double catAct = burstCategoryToActive.get(category);
			d = catAct != null ? catAct.doubleValue() : 0.0;
			d += activeTime;
			burstCategoryToActive.put(category, d);

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
	}

	/**
	 * Assigns burst category to each burst in a collection of bursts.
	 * The collection of bursts have been populated prior to this API call.
	 */
	private void analyzeBursts() {
		
		LOGGER.fine("Entered analyzeBursts()");
		
		List<UserEvent> userEvents = analysis.getUserEvents();
		List<CpuActivity> cpuEvents = analysis.getCpuActivityList().getCpuActivityList();
		int userEventsSize = userEvents.size();
		int cpuEventsSize = cpuEvents.size();
		int userEventPointer = 0;
		int cpuPointer = 0;
		// Analyze each burst
		Burst b = null;
		Burst lastBurst;
		for (Iterator<Burst> i = burstCollection.iterator(); i.hasNext();) {
			
			LOGGER.fine("-----------------------------------------");
			
			lastBurst = b;
			b = i.next();
			List<PacketInfo> burstPacketCollection = new ArrayList<PacketInfo>(b.getPackets().size());
			int burstPayloadLen = 0;
			Set<TcpInfo> burstPacketTcpInfo = new HashSet<TcpInfo>();
			for (PacketInfo p : b.getPackets()) {
				burstPayloadLen += p.getPayloadLen();
				burstPacketCollection.add(p);
				TcpInfo tcp = p.getTcpInfo();
				if (tcp != null) {
					burstPacketTcpInfo.add(tcp);
				}
			}
			
			PacketInfo pkt0 = null;
			TcpInfo info0 = null;
			double time0 = 0;
			if (0 != burstPacketCollection.size()) {
				pkt0 = burstPacketCollection.get(0);
				info0 = pkt0.getTcpInfo();
				time0 = pkt0.getTimeStamp();
			}
			


			/*
			 * Mark the burst as Long Burst based on the
			 * burst duration and size of the payload. 
			 */
			if (b.getEndTime() - b.getBeginTime() > profile.getLargeBurstDuration()
					&& burstPayloadLen > profile.getLargeBurstSize()) {
				b.setBurstInfo(BurstCategory.LONG);
				++longBurstCount;
				LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
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
					
					b.setBurstInfo(BurstCategory.TCP_PROTOCOL);
					LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
					continue;
				}
				
				if (info0 == TcpInfo.TCP_ACK_RECOVER || info0 == TcpInfo.TCP_ACK_DUP) {
					b.setBurstInfo(BurstCategory.TCP_LOSS_OR_DUP);
					LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
					continue;
				}
			}

			// Step 4: Server delay
			if (pkt0.getDir() == PacketInfo.Direction.DOWNLINK
					&& (info0 == TcpInfo.TCP_DATA || info0 == TcpInfo.TCP_ACK)) {
				b.setBurstInfo(BurstCategory.SERVER_NET_DELAY);
				LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
				continue;
			}

			// Step 5: Loss recover
			if (info0 == TcpInfo.TCP_ACK_DUP || info0 == TcpInfo.TCP_DATA_DUP) {
				b.setBurstInfo(BurstCategory.TCP_LOSS_OR_DUP);
				LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
				continue;
			}

			if (info0 == TcpInfo.TCP_DATA_RECOVER || info0 == TcpInfo.TCP_ACK_RECOVER) {
				b.setBurstInfo(BurstCategory.TCP_LOSS_OR_DUP);
				LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
				continue;
			}

			// Step 6: User triggered
			final double USER_EVENT_SMALL_TOLERATE = profile.getUserInputTh();
			if (burstPayloadLen > 0) {
				UserEvent ue = null;
				while ((userEventPointer < userEventsSize) && ((ue = userEvents.get(userEventPointer)).getReleaseTime() < (time0 - USER_EVENT_TOLERATE)))
					++userEventPointer;
				
				BurstCategory userInputBurst = (ue != null) ? ((ue.getEventType() == UserEventType.SCREEN_LANDSCAPE || ue
						.getEventType() == UserEventType.SCREEN_PORTRAIT) ? BurstCategory.SCREEN_ROTATION
						: BurstCategory.USER_INPUT)
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
					b.setBurstInfo(userInputBurst);
					LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
					continue;
				} else if (minGap < USER_EVENT_TOLERATE
						&& (lastBurst == null || lastBurst.getEndTime() < b.getBeginTime() - minGap)) {
					double cpuBegin = time0 - minGap;
					double cpuEnd = time0;
					// Check CPU usage
					while (cpuPointer < cpuEventsSize) {
						double t = cpuEvents.get(cpuPointer).getTimeStamp();
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
						double t = cpuAct.getTimeStamp();
						if (t > cpuBegin && t < cpuEnd) {
							s += cpuAct.getTotalCpuUsage();
							ns++;
						}
						if (t >= cpuEnd) {
							break;
						}
						k++;
					}
					if (ns > 0 && (s / ns) > AVG_CPU_USAGE_THRESHOLD) {
						b.setBurstInfo(BurstCategory.CPU);
						LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
						continue;
					} else {
						b.setBurstInfo(userInputBurst);
						LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
						continue;
					}
				}
			}

			// Step 7: Client delay
			if (burstPayloadLen == 0) {
				b.setBurstInfo(BurstCategory.UNKNOWN);
				LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
				continue;
			} else {
				b.setBurstInfo(BurstCategory.CLIENT_APP);
				LOGGER.log(Level.FINE, LOG_MSG1, b.getBurstInfos());
				continue;
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

		/* 
		 * Represent lists of hosts, objects, and IPs requested via HTTP and
		 * timestamps when these requests were made.
		 */
		Map<String, List<Double>> requestedHost2tsList = new HashMap<String, List<Double>>();
		Map<String, List<Double>> requestedObj2tsList = new HashMap<String, List<Double>>();
		Map<InetAddress, List<Double>> connectedIP2tsList = new HashMap<InetAddress, List<Double>>();
		
		periodicCount = 0;
		diffPeriodicCount = 0;
		minimumPeriodicRepeatTime = 0.0;

		LOGGER.fine("Number of TCP sessions to be analyzed: " + analysis.getTcpSessions().size());
		for (TCPSession tcpSession : analysis.getTcpSessions()) {

			// Get a list of timestamps of established sessions with each remote IP
			PacketInfo firstPacket = tcpSession.getPackets().get(0);
			if (firstPacket.getTcpInfo() == TcpInfo.TCP_ESTABLISH) {
				List<Double> res = connectedIP2tsList.get(tcpSession.getRemoteIP());
				if (res == null) {
					res = new ArrayList<Double>();
					connectedIP2tsList.put(tcpSession.getRemoteIP(), res);
				}
				res.add(Double.valueOf(firstPacket.getTimeStamp()));
			}

			// Get a list of timestamps of HTTP requests to hosts/object names
			for (HttpRequestResponseInfo rr : tcpSession.getRequestResponseInfo()) {
				PacketInfo pkt = rr.getFirstDataPacket();
				if (rr.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
					Double ts0 = Double.valueOf(pkt.getTimeStamp());
					if (rr.getHostName() != null) {
						List<Double> tempRequestHostEventList = requestedHost2tsList.get(rr.getHostName());
						if (tempRequestHostEventList == null) {
							tempRequestHostEventList = new ArrayList<Double>();
							requestedHost2tsList.put(rr.getHostName(), tempRequestHostEventList);
						}
						tempRequestHostEventList.add(ts0);
					}

					if (rr.getObjName() != null) {
						String objName = rr.getObjNameWithoutParams();
						List<Double> tempRequestObjEventList = requestedObj2tsList.get(objName);

						if (tempRequestObjEventList == null) {
							tempRequestObjEventList = new ArrayList<Double>();
							requestedObj2tsList.put(objName, tempRequestObjEventList);
						}
						tempRequestObjEventList.add(ts0);
					}
				}
			}
		}

		Set<String> hostList = new HashSet<String>();
		Set<String> objList = new HashSet<String>();
		Set<InetAddress> ipList = new HashSet<InetAddress>();
		for (Map.Entry<String, List<Double>> iter : requestedHost2tsList.entrySet()) {
			if (determinePeriodicity(iter.getValue())) {
				hostList.add(iter.getKey());
			}
		}
		for (Map.Entry<String, List<Double>> iter : requestedObj2tsList.entrySet()) {
			if (determinePeriodicity(iter.getValue())) {
				objList.add(iter.getKey());
			}
		}
		for (Map.Entry<InetAddress, List<Double>> iter : connectedIP2tsList.entrySet()) {
			if (determinePeriodicity(iter.getValue())) {
				ipList.add(iter.getKey());
			}
		}

		determinePeriodicity(hostList, objList, ipList);
		
	}

	/**
	 * Determine periodicity 
	 * 
	 * @param hostList
	 * @param objList
	 * @param ipList
	 */
	private void determinePeriodicity(Set<String> hostList, Set<String> objList, Set<InetAddress> ipList) {
		
		Set<String> hostPeriodicInfoSet = new HashSet<String>();

		for (int i = 0; i < burstCollection.size(); i++) {
			
			Burst burst  = burstCollection.get(i);
			
			if (burst.getBurstInfos() != BurstCategory.CLIENT_APP){
				continue;
			}
			if (isCloseSpacedBurst(i, burst, profile.getCloseSpacedBurstThreshold())){
				continue;
			}

			Packet beginPacket = burst.getBeginPacket().getPacket();
			if (beginPacket instanceof IPPacket) {
				IPPacket ip = (IPPacket) beginPacket;
				InetAddress ia = ip.getDestinationIPAddress();
				if(isIpInIpList(ipList, hostPeriodicInfoSet, burst, ia)) {
					continue;
				}
				ia = ip.getSourceIPAddress();
				if(isIpInIpList(ipList, hostPeriodicInfoSet, burst, ia)) {
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

			findPeriodicalBursts(hostPeriodicInfoSet, hostList, objList, burst, firstUplinkPayloadPacket);
		}
		diffPeriodicCount = hostPeriodicInfoSet.size();
	}

	/**
	 * If the bursts is close spaced to a burst next to it it will return true, otherwise it will return false.
	 * 
	 * @param i Index of the burst to be investigated.
	 * @param burst Collection of bursts.
	 * @param threshold Close spaced bursts threshold.
	 * 
	 * @return If the bursts is close spaced to a burst next to it it will return true, otherwise it will return false;.
	 */
	private boolean isCloseSpacedBurst(int i, Burst burst, double threshold) {
		
		Burst prevBurst;
		if (i > 0 && i < burstCollection.size()) {
			prevBurst = burstCollection.get(i - 1);
			if (burst.getBeginTime() - prevBurst.getEndTime() < threshold) {
				return true;
			}
		}

		Burst nextBurst;
		if (i < burstCollection.size() - 1) {
			nextBurst = burstCollection.get(i + 1);
			if (nextBurst.getBeginTime() - burst.getEndTime() < threshold) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * If IP is in the IP list, set the burst to PERIODICAL, increment periodic counter, add the IP into hostPeriodicInfoSet;
	 * 
	 * @param ipList
	 * @param hostPeriodicInfoSet
	 * @param burst
	 * @param ia
	 */
	private boolean isIpInIpList(Set<InetAddress> ipList, Set<String> hostPeriodicInfoSet, Burst burst, InetAddress ia) {
		if (ipList.contains(ia)) {
			periodicCount++;
			burst.setBurstInfo(BurstCategory.PERIODICAL);
			hostPeriodicInfoSet.add(ia.toString());
			LOGGER.log(Level.FINE, LOG_MSG1, burst.getBurstInfos());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Loops through all the HTTP requests in all TCP session. 
	 * If the host/object in the HTTP request is in the provided list of hosts/objects and
	 * the first data packet associated with the HTTP request is the first uplink payload packet from the burst then
	 * increase periodic count by one and mark the burst periodical.
	 * 
	 * 
	 * @param hostPeriodicInfoSet
	 * @param hostList
	 * @param objList
	 * @param burst
	 * @param firstUplinkPayloadPacket
	 */
	private void findPeriodicalBursts(Set<String> hostPeriodicInfoSet, Set<String> hostList, Set<String> objList, Burst burst, PacketInfo firstUplinkPayloadPacket) {

		for (TCPSession session : analysis.getTcpSessions()) {
			for (HttpRequestResponseInfo httpInfo : session.getRequestResponseInfo()) {
				if (httpInfo.getDirection() == HttpRequestResponseInfo.Direction.REQUEST
						&& (hostList.contains(httpInfo.getHostName()) || objList.contains(httpInfo.getObjNameWithoutParams()))) {
					if (httpInfo.getFirstDataPacket() == firstUplinkPayloadPacket) {
						LOGGER.fine("Found packet which is the firstUplinkPayloadPacket");
						periodicCount++;
						burst.setBurstInfo(BurstCategory.PERIODICAL);
						burst.setFirstUplinkDataPacket(firstUplinkPayloadPacket);
						if (hostList.contains(httpInfo.getHostName())) {
							hostPeriodicInfoSet.add(httpInfo.getHostName());
						} else {
							hostPeriodicInfoSet.add(httpInfo.getObjNameWithoutParams());
						}
						LOGGER.log(Level.FINE, LOG_MSG1, burst.getBurstInfos());
						continue;
					}
				}
			}
		}
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
	 * Determines whether the request are periodic.
	 * 
	 * @param timeList List of timestamps when the object was requested
	 * @return true if periodic, false if not
	 */
	private boolean determinePeriodicity(List<Double> timeList) {

		// ignore request occurring only few times
		if (timeList.size() > SMALL_PERIODICITY) {

			// requests occurring too frequently must be split to improve performance
			if (timeList.size() > MAX_NUM_OF_REQUESTS) {

				List<List<Double>> chunks = splitIntoChunks(timeList, MAX_NUM_OF_REQUESTS);
				boolean ret = false;
				for (List<Double> list : chunks) {
					if (isPeriodicalTraffic(list)) {
						ret = true;
					}
				}
				return ret;

			} else {
				// requests occurring less frequently can be processed all at once
				return isPeriodicalTraffic(timeList);
			}
		} else {
			return false;
		}
	}

	/**
	 * Determines whether the request are periodic.
	 * 
	 * @param timeList List of timestamps when the object was requested
	 * @return true if periodic, false if not
	 */
	private boolean isPeriodicalTraffic(List<Double> timeList) {
		
		int numberOfRequests = timeList.size();
		// Collection for storing Inter-Arrival Times
		List<IatInfo> iatInfoList = new ArrayList<IatInfo>(numberOfRequests * (numberOfRequests - 1) / 2);
		calculateInterArrivalTime(timeList, numberOfRequests, iatInfoList);
		sortInterArrivalTime(iatInfoList);
		return calculateClusterParameters(iatInfoList);
	}

	/**
	 * Splits List into smaller Lists/chunks
	 * 
	 * @param timeList List of timestamps when the object was requested
	 * @param maxChunkLength determines the maximum size of chunks
	 * @return List of chunks
	 */
	public static List<List<Double>> splitIntoChunks(List<Double> timeList, final int maxChunkLength) {
		
		List<List<Double>> chunks = new ArrayList<List<Double>>();
		final int size = timeList.size();
		final int numOfChunks = size / maxChunkLength;
		final int reminder = size % maxChunkLength;
		LOGGER.log(Level.FINE, "size: {2}, chunk size: {3}, num. of chunks: {0}, reminder: {1}", new Object[]{numOfChunks, reminder, size, maxChunkLength} );
		
		for(int ii=0; ii < size; ii += maxChunkLength) {
	
			if(ii + maxChunkLength <= size) {
				chunks.add(timeList.subList(ii, ii + maxChunkLength));
			} else if (reminder > 0) {
				chunks.add(timeList.subList(size - reminder, size));
			} else {
				assert(false);
			}
			
		}
		return chunks;
	}

	/**
	 * Calculates Inter-Arrival Time between all events in the collection, time between each events.
	 * 
	 * @param timeList Contains a collection of events represented by the event's occurrence timestamp.
	 * @param numberOfRequests The total number of events.
	 * @param iatInfoList Stores the results; the calculated IAT, begin time, and other information.
	 */
	private void calculateInterArrivalTime(List<Double> timeList, int numberOfRequests, List<IatInfo> iatInfoList) {
		
		double time1;
		double time2;
		IatInfo ii;
		
		for (int i = 0; i < numberOfRequests - 1; i++) {
			for (int j = i + 1; j < numberOfRequests; j++) {
				time1 = timeList.get(i).doubleValue();
				time2 = timeList.get(j).doubleValue();

				ii = new IatInfo();
				if (time1 <= time2) {
					ii.beginTime = time1;
					ii.iat = time2 - time1;
					ii.beginEvent = i;
					ii.endEvent = j;
				} else {
					ii.beginTime = time2;
					ii.iat = time1 - time2;
					ii.beginEvent = j;
					ii.endEvent = i;
				}

				iatInfoList.add(ii);
			}
		}
	}
	
	/**
	 * Sorts Inter-Arrival Time collection.
	 * 
	 * @param iatInfoList Collection to be sorted.
	 */
	private void sortInterArrivalTime(List<IatInfo> iatInfoList) {
		LOGGER.log(Level.FINE, "sortInterArrivalTime, collection size: {0}", iatInfoList.size());
		Collections.sort(iatInfoList, new SortByIAT());
	}
	
	/**
	 * Creates clusters from the list of Inter-Arrival Time list and calculates
	 * cluster related parameters. Compare these parameters with the profile thresholds and
	 * returns boolean as a result of the comparison.
	 * 
	 * @param iatInfoList List of IAT objects
	 * @return true if periodic, false if not
	 */
	private boolean calculateClusterParameters(List<IatInfo> iatInfoList) {
		
		/*
		 * This parameter is used to find clusters of PTs (periodic transfers). 
		 * This parameter/criterion is applied onto all identified clusters of PTs. 
		 * In order for the cluster to be considered periodical its average value of 
		 * IATs must be greater than this parameter. 
		 * The default value of the parameter is 10 seconds.
		 */
		double minVariation = profile.getPeriodMinCycle();
		/* 
		 * This parameter is used to find clusters of PTs. Each PT has so called 
		 * IAT (Inter Arrival Time) which is the length of time this PT took place. 
		 * A cluster is only created from PTs which have their IAT values close 
		 * to each another within the value specified here by this parameters. 
		 * Only resulting clusters are considered to be further analyzed for periodicity. 
		 * The default value of the parameter is 1 second.
		 */
		double maxVariation = profile.getPeriodCycleTol();
		/*
		 * This parameter is used to find clusters of PTs.
		 * A cluster of PTs must contain PTs which are continuously linked. 
		 * The number of linked PTs must be greater than this parameter. 
		 * The default value is 3.
		 */
		int minSamples = profile.getPeriodMinSamples();
		
		int iatInfoSize = iatInfoList.size();
		int bestNonOverlapSize = 0;
		double cycle = 0;
		List<IatInfo> iAtCluster;
		double sumOfClusterIATs;
		int nonOverlapSize;
		double avgIatInCluster;
		for (int idx = 0; idx < iatInfoSize; idx++) {

			iAtCluster = new ArrayList<IatInfo>();
			sumOfClusterIATs = 0;
			sumOfClusterIATs += createIatCluster(iatInfoList, iAtCluster, idx, maxVariation);
			
			avgIatInCluster = sumOfClusterIATs / iAtCluster.size();
			if (avgIatInCluster > minVariation) {
				nonOverlapSize = getNonOverlapSize(iAtCluster);
				if (nonOverlapSize > bestNonOverlapSize) {
					LOGGER.fine("The avg. IAT for the cluster has exceeded the threshold");
					bestNonOverlapSize = nonOverlapSize;
					cycle = avgIatInCluster;
				}
			}
		}

		if (bestNonOverlapSize < minSamples) {
			LOGGER.fine("Exiting SelfCorr(), returning: " + false);
			return false;
		} else {
			LOGGER.fine("Exiting SelfCorr(), returning: " + (cycle > 0));
			return cycle > 0;
		} 
	}

	/**
	 * Finds all IAT objects with IAT.time close to IAT.time of a IAT object identified by index idx. 
	 * 
	 * If IAT.time difference between the objects is less than the threshold (maxVariation), the IAT object is added to IAT Cluster.
	 * Sum of all IAT.time in the cluster is returned.
	 * 
	 * @param iatInfoList List of IAT objects
	 * @param iat IAT object
	 * @param iAtCluster Cluster of IAT objects 
	 * @param idx Starting index
	 * @param maxVariation max. variation
	 * @return Sum of all IAT.time in the cluster is returned.
	 */
	double createIatCluster(List<IatInfo> iatInfoList, List<IatInfo> iAtCluster, int idx, double maxVariation) {

		int i = idx;
		IatInfo iat = iatInfoList.get(i);
		IatInfo iatInfo;
		int iatInfoSize = iatInfoList.size();
		double sumOfIatInCluster = 0;
		while ((i < iatInfoSize) && (iatInfoList.get(i).iat - iat.iat < maxVariation)) {
			iatInfo = iatInfoList.get(i);
			iAtCluster.add(iatInfo);
			sumOfIatInCluster += iatInfo.iat;
			++i;
		}
		LOGGER.log(Level.FINE, "Inter-Arrival Time Cluster of size {0} was created", iAtCluster.size());
		return sumOfIatInCluster;
	}

	/**
	 * Method to calculate the over lap events in burst.
	 * 
	 * It finds the longest series of subsequent events linked together by event end ID and event begin ID.
	 * 
	 */
	int getNonOverlapSize(List<IatInfo> iAtCluster) {

		sortIatClusterByBeginTime(iAtCluster);

		// find the longest path
		int clusterSize = iAtCluster.size();
		int[] opt = new int[clusterSize];
		int best = -1;
		int tmpBest;
		IatInfo iatInfo;

		for (int i = 0; i < clusterSize; i++) {
			iatInfo = iAtCluster.get(i);
			tmpBest = 1;

			for (int j = 0; j <= i - 1; j++) {
				if (opt[j] >= tmpBest && iAtCluster.get(j).endEvent == iatInfo.beginEvent) {
					tmpBest = opt[j] + 1;
				}
			}

			if (tmpBest > best) {
				best = tmpBest;
			}

			opt[i] = tmpBest;
		}

		LOGGER.log(Level.FINE, "Best value: {0}", best);
		return best;
	}

	/**
	 * @param iAtCluster
	 */
	private void sortIatClusterByBeginTime(List<IatInfo> iAtCluster) {
		LOGGER.log(Level.FINE, "sortIatClusterByBeginTime, collection size: {0}", iAtCluster.size());
		Collections.sort(iAtCluster, new SortByBeginTime());
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
			if (burstInfo.getBurstCategory() == BurstCategory.USER_INPUT
					|| burstInfo.getBurstCategory() == BurstCategory.SCREEN_ROTATION) {
				continue;
			}
			double startTime = burstInfo.getBeginTime();
			double endTime = startTime + 60.0;
			int count = 1;
			for (int j = i + 1; j < burstCollection.size()
					&& burstCollection.get(j).getEndTime() <= endTime; ++j) {
				if (burstCollection.get(j).getBurstCategory() != BurstCategory.USER_INPUT
						|| burstInfo.getBurstCategory() == BurstCategory.SCREEN_ROTATION) {
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
					if (burstCollection.get(j).getBurstCategory() != BurstCategory.USER_INPUT
							|| burstInfo.getBurstCategory() == BurstCategory.SCREEN_ROTATION) {
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
			if (burst.getBurstCategory() == BurstCategory.PERIODICAL) {
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
	
	/**
	 * 
	 * @return energy used by periodic bursts
	 */
	public double getPeriodicEnergy(){
		for(BurstAnalysisInfo burstInfo : getBurstAnalysisInfo()){
			if(burstInfo.getCategory() == BurstCategory.PERIODICAL){
				return burstInfo.getEnergyPct();
			}
		}
		return 0;
	}
}
