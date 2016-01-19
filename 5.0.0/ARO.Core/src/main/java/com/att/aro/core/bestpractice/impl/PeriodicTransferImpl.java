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
package com.att.aro.core.bestpractice.impl;

import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.IatInfo;
import com.att.aro.core.bestpractice.pojo.PeriodicTransferResult;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TcpInfo;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.packetreader.pojo.PacketDirection;

/**
 * best practice for periodic transfer
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 6, 2014
 */
public class PeriodicTransferImpl implements IBestPractice {
	/*
	 *  Maximum number of requests the algorithm can process in reasonable time.
	 *  Testings have shown that the number should be smaller than 400 or even smaller for large traces.
	 */
	private static final int MAX_NUM_OF_REQUESTS = 200;
	private static final int SMALL_PERIODICITY = 3;
	
//	@InjectLogger
//	private static ILogger logger;
	
	int periodicCount = 0;
	int diffPeriodicCount = 0;
	double minimumPeriodicRepeatTime = 0.0;
	
	@Value("${connections.periodic.title}")
	private String overviewTitle;
	
	@Value("${connections.periodic.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.periodic.desc}")
	private String aboutText;
	
	@Value("${connections.periodic.url}")
	private String learnMoreUrl;
	
	@Value("${connections.periodic.pass}")
	private String textResultPass;
	
	@Value("${connections.periodic.results}")
	private String textResults;
	
	@Value("${connections.periodic.result}")
	private String textResult;
	
	@Value("${exportall.csvIneffConnDesc}")
	private String exportAllIneffConnDesc;
	
	@Value("${exportall.csvIneffConnRptDesc}")
	private String exportAllIneffConnRptDesc;
	
	@Value("${exportall.csvIneffConnTimeDesc}")
	private String exportAllIneffConnTimeDesc;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		//logger.info("PeriodicTransferImpl start");
		periodicCount = 0;
		diffPeriodicCount = 0;
		minimumPeriodicRepeatTime = 0.0;
		
		PeriodicTransferResult result = new PeriodicTransferResult();
		//logger.info("run diagnosisPeriodicRequest");
		diagnosisPeriodicRequest(tracedata.getSessionlist(), tracedata.getBurstcollectionAnalysisData().getBurstCollection(),
				tracedata.getProfile());
		//logger.info("run calculatePeriodicRequestTime");
		calculatePeriodicRepeatTime(tracedata.getBurstcollectionAnalysisData().getBurstCollection());
		
		if(this.minimumPeriodicRepeatTime == 0.0){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			String text = MessageFormat.format( diffPeriodicCount > 1 ? this.textResults : this.textResult, 
					this.diffPeriodicCount, this.periodicCount, this.minimumPeriodicRepeatTime);
			result.setResultText(text);
		}
		result.setDiffPeriodicCount(diffPeriodicCount);
		result.setPeriodicCount(periodicCount);
		result.setMinimumPeriodicRepeatTime(minimumPeriodicRepeatTime);
		
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setSelfTest(false);
		result.setExportAllIneffConnDesc(exportAllIneffConnDesc);
		result.setExportAllIneffConnRptDesc(exportAllIneffConnRptDesc);
		result.setExportAllIneffConnTimeDesc(exportAllIneffConnTimeDesc);
		
		return result;
	}
	private void calculatePeriodicRepeatTime(List<Burst> burstCollection) {
		int burstSize = burstCollection.size();
		Burst lastPeriodicalBurst = null;
		periodicCount = 0;
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

		if (minimumRepeatTime != Double.MAX_VALUE && this.periodicCount >= 3) {
			minimumPeriodicRepeatTime = minimumRepeatTime;
		}
	}
	/**
	 * Burst data's analyzed to categorize the periodic bursts.
	 */
	private void diagnosisPeriodicRequest(List<Session> sessionlist, List<Burst> burstCollection, Profile profile) {

		/* 
		 * Represent lists of hosts, objects, and IPs requested via HTTP and
		 * timestamps when these requests were made.
		 */
		Map<String, List<Double>> requestedHost2tsList = new HashMap<String, List<Double>>();
		Map<String, List<Double>> requestedObj2tsList = new HashMap<String, List<Double>>();
		Map<InetAddress, List<Double>> connectedIP2tsList = new HashMap<InetAddress, List<Double>>();
		
		//logger.info("inside diagnosisPeriodicRequest -> looping thru session list size: "+sessionlist.size());
//		int count = 0;
		for (Session tcpSession : sessionlist) {
			
			// Get a list of timestamps of established sessions with each remote IP
			if(!tcpSession.isUDP()){
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
				for (HttpRequestResponseInfo hrri : tcpSession.getRequestResponseInfo()) {
					PacketInfo pkt = hrri.getFirstDataPacket();
					if (hrri.getDirection() == HttpDirection.REQUEST) {
						Double ts0 = Double.valueOf(pkt.getTimeStamp());
						if (hrri.getHostName() != null) {
							List<Double> tempRequestHostEventList = requestedHost2tsList.get(hrri.getHostName());
							if (tempRequestHostEventList == null) {
								tempRequestHostEventList = new ArrayList<Double>();
								requestedHost2tsList.put(hrri.getHostName(), tempRequestHostEventList);
							}
							tempRequestHostEventList.add(ts0);
						}
	
						if (hrri.getObjName() != null) {
							String objName = hrri.getObjNameWithoutParams();
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
		}
		//logger.info("done looping session");
		
		Set<String> hostList = new HashSet<String>();
		Set<String> objList = new HashSet<String>();
		Set<InetAddress> ipList = new HashSet<InetAddress>();
		//logger.info("create hostlist");
//		count = 0;
		for (Map.Entry<String, List<Double>> iter : requestedHost2tsList.entrySet()) {
			if (determinePeriodicity(iter.getValue(), profile)) {
				hostList.add(iter.getKey());
			}
			//logger.info("count: "+count++);
		}
		//logger.info("create objList");
//		count = 0;
		for (Map.Entry<String, List<Double>> iter : requestedObj2tsList.entrySet()) {
			if (determinePeriodicity(iter.getValue(), profile)) {
				objList.add(iter.getKey());
			}
			//logger.info("count: "+count++);
		}
		//logger.info("create ipList");
//		count = 0;
		for (Map.Entry<InetAddress, List<Double>> iter : connectedIP2tsList.entrySet()) {
			if (determinePeriodicity(iter.getValue(), profile)) {
				ipList.add(iter.getKey());
			}
			//logger.info("count: "+count++);
		}
		//logger.info("Done looping, now go to determinePeriodicity");
		determinePeriodicity(hostList, objList, ipList, burstCollection, profile, sessionlist);
		
	}
	/**
	 * Determines whether the request are periodic.
	 * 
	 * @param timeList List of timestamps when the object was requested
	 * @return true if periodic, false if not
	 */
	private boolean determinePeriodicity(List<Double> timeList, Profile profile) {
		
		// ignore request occurring only few times
		if (timeList.size() > SMALL_PERIODICITY) {

			// requests occurring too frequently must be split to improve performance
			if (timeList.size() > MAX_NUM_OF_REQUESTS) {

				List<List<Double>> chunks = splitIntoChunks(timeList, MAX_NUM_OF_REQUESTS);
				boolean ret = false;
				for (List<Double> list : chunks) {
					if (isPeriodicalTraffic(list, profile)) {
						ret = true;
					}
				}
				return ret;

			} else {
				// requests occurring less frequently can be processed all at once
				return isPeriodicalTraffic(timeList, profile);
			}
		} else {
			return false;
		}
	}
	/**
	 * Splits List into smaller Lists/chunks
	 * 
	 * @param timeList List of timestamps when the object was requested
	 * @param maxChunkLength determines the maximum size of chunks
	 * @return List of chunks
	 */
	protected List<List<Double>> splitIntoChunks(List<Double> timeList, final int maxChunkLength) {
		
		List<List<Double>> chunks = new ArrayList<List<Double>>();
		int size = timeList.size();
		int reminder = size % maxChunkLength;
		
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
	 * Determines whether the request are periodic.
	 * 
	 * @param timeList List of timestamps when the object was requested
	 * @return true if periodic, false if not
	 */
	private boolean isPeriodicalTraffic(List<Double> timeList, Profile profile) {
		
		int numberOfRequests = timeList.size();
		// Collection for storing Inter-Arrival Times
		List<IatInfo> iatInfoList = new ArrayList<IatInfo>(numberOfRequests * (numberOfRequests - 1) / 2);
		//logger.info("isPeriodicTraffic.calculateInterArrrivalTime");
		calculateInterArrivalTime(timeList, numberOfRequests, iatInfoList);
		//logger.info("isPeriodicTraffic.sortInterArritvalTime");
		sortInterArrivalTime(iatInfoList);
		//logger.info("isPeriodicTraffic.calculateClusterParameters");
		return calculateClusterParameters(iatInfoList, profile);
	}
	/**
	 * Creates clusters from the list of Inter-Arrival Time list and calculates
	 * cluster related parameters. Compare these parameters with the profile thresholds and
	 * returns boolean as a result of the comparison.
	 * 
	 * @param iatInfoList List of IAT objects
	 * @return true if periodic, false if not
	 */
	private boolean calculateClusterParameters(List<IatInfo> iatInfoList, Profile profile) {
		//logger.info("calculateClusterParameters -> profile.getPeriodMinCycle");
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
		//logger.info("calculateClusterParameters -> profile.getPeriodCycleTol");
		double maxVariation = profile.getPeriodCycleTol();
		/*
		 * This parameter is used to find clusters of PTs.
		 * A cluster of PTs must contain PTs which are continuously linked. 
		 * The number of linked PTs must be greater than this parameter. 
		 * The default value is 3.
		 */
		//logger.info("calculateClusterParameters -> profile.getPeriodMinSamples");
		int minSamples = profile.getPeriodMinSamples();
		
		int iatInfoSize = iatInfoList.size();
		int bestNonOverlapSize = 0;
		double cycle = 0;
		List<IatInfo> iAtCluster;
		double sumOfClusterIATs;
		int nonOverlapSize;
		double avgIatInCluster;
		//logger.info("calculateClusterParameters -> for loop, size: "+iatInfoSize);
		for (int idx = 0; idx < iatInfoSize; idx++) {

			iAtCluster = new ArrayList<IatInfo>();
			sumOfClusterIATs = 0;
			//logger.info("calculateClusterParameters.createIatCluster, ids: "+idx+" of size: "+iatInfoSize);
			sumOfClusterIATs += createIatCluster(iatInfoList, iAtCluster, idx, maxVariation);
			
			avgIatInCluster = sumOfClusterIATs / iAtCluster.size();
			if (avgIatInCluster > minVariation) {
				//logger.info("calculateClusterParameters.gerNonOverlapSize");
				nonOverlapSize = getNonOverlapSize(iAtCluster);
				if (nonOverlapSize > bestNonOverlapSize) {
					bestNonOverlapSize = nonOverlapSize;
					cycle = avgIatInCluster;
				}
			}
		}

		if (bestNonOverlapSize < minSamples) {
			return false;
		} else {
			return cycle > 0;
		} 
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
				if (opt[j] >= tmpBest && iAtCluster.get(j).getEndEvent() == iatInfo.getBeginEvent()) {
					tmpBest = opt[j] + 1;
				}
			}

			if (tmpBest > best) {
				best = tmpBest;
			}

			opt[i] = tmpBest;
		}

		return best;
	}
	/**
	 * @param iAtCluster
	 */
	private void sortIatClusterByBeginTime(List<IatInfo> iAtCluster) {
		Collections.sort(iAtCluster, new SortByBeginTime());
	}
	
	/* 
	 * Finds all IAT objects with IAT.time close to IAT.time of a IAT object identified by index idx. 
	 * If IAT.time difference between the objects is less than the threshold (maxVariation), 
	 * the IAT object is added to IAT Cluster.Sum of all IAT.time in the cluster is returned. 
	 */
	
	/**
	 * @param iatInfoList List of IAT objects
	 * @param iat IAT object
	 * @param iAtCluster Cluster of IAT objects 
	 * @param idx Starting index
	 * @param maxVariation max. variation
	 * @return Sum of all IAT.time in the cluster is returned.
	 **/
	double createIatCluster(List<IatInfo> iatInfoList, List<IatInfo> iAtCluster, int idx, double maxVariation) {

		int index = idx;
		IatInfo iat = iatInfoList.get(index);
		IatInfo iatInfo;
		int iatInfoSize = iatInfoList.size();
		double sumOfIatInCluster = 0;
		while ((index < iatInfoSize) && (iatInfoList.get(index).getIat() - iat.getIat() < maxVariation)) {
			iatInfo = iatInfoList.get(index);
			iAtCluster.add(iatInfo);
			sumOfIatInCluster += iatInfo.getIat();
			++index;
		}
		return sumOfIatInCluster;
	}
	/**
	 * Sorts Inter-Arrival Time collection.
	 * 
	 * @param iatInfoList Collection to be sorted.
	 */
	private void sortInterArrivalTime(List<IatInfo> iatInfoList) {
		Collections.sort(iatInfoList, new SortByIAT());
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
		IatInfo iInfo;
		
		for (int i = 0; i < numberOfRequests - 1; i++) {
			for (int j = i + 1; j < numberOfRequests; j++) {
				time1 = timeList.get(i).doubleValue();
				time2 = timeList.get(j).doubleValue();

				iInfo = new IatInfo();
				if (time1 <= time2) {
					iInfo.setBeginTime(time1);
					iInfo.setIat(time2 - time1);
					iInfo.setBeginEvent(i);
					iInfo.setEndEvent(j);
				} else {

					iInfo.setBeginTime(time2);
					iInfo.setIat(time1 - time2);
					iInfo.setBeginEvent(j);
					iInfo.setEndEvent(i);

				}

				iatInfoList.add(iInfo);
			}
		}
	}
	/**
	 * Determine periodicity 
	 * 
	 * @param hostList
	 * @param objList
	 * @param ipList
	 */
	private void determinePeriodicity(Set<String> hostList, Set<String> objList, Set<InetAddress> ipList,
			List<Burst> burstCollection, Profile profile, List<Session> sessionlist) {
		
		Set<String> hostPeriodicInfoSet = new HashSet<String>();
		
		for (int i = 0; i < burstCollection.size(); i++) {
			
			Burst burst  = burstCollection.get(i);
			
			if (burst.getBurstInfos() != BurstCategory.CLIENT_APP){
				continue;
			}
			if (isCloseSpacedBurst(i, burst, profile.getCloseSpacedBurstThreshold(), burstCollection)){
				continue;
			}

			Packet beginPacket = burst.getBeginPacket().getPacket();
			if (beginPacket instanceof IPPacket) {
				IPPacket iPkt = (IPPacket) beginPacket;
				InetAddress iAddr = iPkt.getDestinationIPAddress();
				if(isIpInIpList(ipList, hostPeriodicInfoSet, burst, iAddr)) {
					periodicCount++;
					continue;
				}
				iAddr = iPkt.getSourceIPAddress();
				if(isIpInIpList(ipList, hostPeriodicInfoSet, burst, iAddr)) {
					periodicCount++;
					continue;
				}
			}
			
			PacketInfo firstUplinkPayloadPacket = null;
			for (PacketInfo pInfo : burst.getPackets()) {
				if (pInfo.getDir() == PacketDirection.UPLINK && pInfo.getPayloadLen() > 0) {
					firstUplinkPayloadPacket = pInfo;
					break;
				}
			}

			periodicCount += findPeriodicalBursts(hostPeriodicInfoSet, hostList, objList, burst, firstUplinkPayloadPacket, sessionlist);
		}
		diffPeriodicCount = hostPeriodicInfoSet.size();
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
	private int findPeriodicalBursts(Set<String> hostPeriodicInfoSet, Set<String> hostList, Set<String> objList, 
			Burst burst, PacketInfo firstUplinkPayloadPacket, List<Session> sessionlist) {
		int periodicCount = 0;
		for (Session session : sessionlist) {
			if(!session.isUDP()){
				for (HttpRequestResponseInfo httpInfo : session.getRequestResponseInfo()) {
					if (httpInfo.getDirection() == HttpDirection.REQUEST
							&& (hostList.contains(httpInfo.getHostName()) || objList.contains(httpInfo.getObjNameWithoutParams()))
							&& (httpInfo.getFirstDataPacket() == firstUplinkPayloadPacket)){						
							//remove nested if statement because PMD rule
							periodicCount++;
							burst.setBurstInfo(BurstCategory.PERIODICAL);
							burst.setFirstUplinkDataPacket(firstUplinkPayloadPacket);
							if (hostList.contains(httpInfo.getHostName())) {
								hostPeriodicInfoSet.add(httpInfo.getHostName());
							} else {
								hostPeriodicInfoSet.add(httpInfo.getObjNameWithoutParams());
							}
							continue;						
					}
				}
			}
		}
		return periodicCount;
	}
	/**
	 * If the bursts is close spaced to a burst next to it it will return true, otherwise it will return false.
	 * 
	 * @param index Index of the burst to be investigated.
	 * @param burst Collection of bursts.
	 * @param threshold Close spaced bursts threshold.
	 * 
	 * @return If the bursts is close spaced to a burst next to it it will return true, otherwise it will return false;.
	 */
	private boolean isCloseSpacedBurst(int index, Burst burst, double threshold, List<Burst> burstCollection) {
		
		Burst prevBurst;
		if (index > 0 && index < burstCollection.size()) {
			prevBurst = burstCollection.get(index - 1);
			if (burst.getBeginTime() - prevBurst.getEndTime() < threshold) {
				return true;
			}
		}

		Burst nextBurst;
		if (index < burstCollection.size() - 1) {
			nextBurst = burstCollection.get(index + 1);
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
	 * @param iaddr
	 */
	private boolean isIpInIpList(Set<InetAddress> ipList, Set<String> hostPeriodicInfoSet, Burst burst, InetAddress iaddr) {
		if (ipList.contains(iaddr)) {
			burst.setBurstInfo(BurstCategory.PERIODICAL);
			hostPeriodicInfoSet.add(iaddr.toString());
			return true;
		} else {
			return false;
		}
	}
	
	//Helper inner classes, remove to pojo
	/**
	 * Bean class to contain the information of Request Event's Inter Arrival
	 * Time.
	 */
//	class IatInfo {
//		private double iat;
//		private double beginTime;
//		private int beginEvent;
//		private int endEvent;	
//		IatInfo(double iat, double beginTime, int beginEvent, int endEvent) {
//			super();
//			this.iat = iat;
//			this.beginTime = beginTime;
//			this.beginEvent = beginEvent;
//			this.endEvent = endEvent;
//		}
//		public IatInfo() {
//		}
//	}

	private class SortByBeginTime implements Comparator<IatInfo> {
		@Override
		public int compare(IatInfo info1, IatInfo info2) {
			return Double.valueOf(info1.getBeginTime()).compareTo(info2.getBeginTime());
		}
	}

	private class SortByIAT implements Comparator<IatInfo> {

		@Override
		public int compare(IatInfo info1, IatInfo info2) {
			return Double.valueOf(info1.getIat()).compareTo(info2.getIat());
		}
	}

}//end class
