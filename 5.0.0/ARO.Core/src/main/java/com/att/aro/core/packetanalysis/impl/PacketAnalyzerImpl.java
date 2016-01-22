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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.IBurstCollectionAnalysis;
import com.att.aro.core.packetanalysis.IEnergyModelFactory;
import com.att.aro.core.packetanalysis.IPacketAnalyzer;
import com.att.aro.core.packetanalysis.IPktAnazlyzerTimeRangeUtil;
import com.att.aro.core.packetanalysis.IRrcStateMachineFactory;
import com.att.aro.core.packetanalysis.ISessionManager;
import com.att.aro.core.packetanalysis.ITraceDataReader;
import com.att.aro.core.packetanalysis.pojo.AbstractRrcStateMachine;
import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.ApplicationPacketSummary;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.packetanalysis.pojo.IPPacketSummary;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketCounter;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceFileResult;
import com.att.aro.core.packetreader.pojo.IPPacket;
import com.att.aro.core.packetreader.pojo.TCPPacket;
import com.att.aro.core.packetreader.pojo.UDPPacket;

/**
 * analyze trace file or trace directory and return data that can be used by practice engines.
 */

public class PacketAnalyzerImpl implements IPacketAnalyzer {
	
	private ITraceDataReader tracereader;
	
	private ISessionManager sessionmanager;
	
	private IRrcStateMachineFactory statemachinefactory;
	
	private IProfileFactory profilefactory;
	
	private IEnergyModelFactory energymodelfactory;
	
	private IBurstCollectionAnalysis burstcollectionanalyzer;
	
	private IPktAnazlyzerTimeRangeUtil pktTimeUtil;
	
//	@InjectLogger
//	private static ILogger logger;

	public PacketAnalyzerImpl(){
	}
	
	@Autowired
	public void setTraceReader(ITraceDataReader tracereader){
		this.tracereader = tracereader;
	}
	@Autowired
	public void setSessionManager(ISessionManager sessionmanager){
		this.sessionmanager = sessionmanager;
	}
	@Autowired
	public void setRrcStateMachineFactory(IRrcStateMachineFactory rrcstatemachinefactory){
		this.statemachinefactory = rrcstatemachinefactory;
	}
	@Autowired
	public void setProfileFactory(IProfileFactory profilefactory){
		this.profilefactory = profilefactory;
	}
	@Autowired
	public void setEnergyModelFactory(IEnergyModelFactory energymodelfactory){
		this.energymodelfactory = energymodelfactory;
	}
	@Autowired
	public void setBurstCollectionAnalayzer(IBurstCollectionAnalysis burstcollectionanalysis){
		this.burstcollectionanalyzer = burstcollectionanalysis;
	}
	@Autowired
	public void setPktTimeRangeUtil(IPktAnazlyzerTimeRangeUtil pktUtil){
		this.pktTimeUtil = pktUtil;
	}


	@Override
	public PacketAnalyzerResult analyzeTraceFile(String traceFilePath, Profile profile, 
			AnalysisFilter filter) throws IOException{
		TraceFileResult result = tracereader.readTraceFile(traceFilePath);
		
		return finalResult(result,profile,filter);
	}
	@Override
	public PacketAnalyzerResult analyzeTraceDirectory(String traceDirectory, Profile profile, 
			AnalysisFilter filter) throws FileNotFoundException{
		TraceDirectoryResult result = tracereader.readTraceDirectory(traceDirectory);
		if(filter !=null){
			TimeRange tempTimeRange = filter.getTimeRange();
			if(tempTimeRange != null){
				TraceDirectoryResult tempResult = (TraceDirectoryResult) pktTimeUtil.getTimeRangeResult(result, tempTimeRange);
				result.setAlarmInfos(tempResult.getAlarmInfos());
				result.setBatteryInfos(tempResult.getBatteryInfos());
				result.setBluetoothInfos(tempResult.getBluetoothInfos());
				result.setCameraInfos(tempResult.getCameraInfos());
				result.setGpsInfos(tempResult.getGpsInfos());
				result.setNetworkTypeInfos(tempResult.getNetworkTypeInfos());
				result.setRadioInfos(tempResult.getRadioInfos());
				result.setScreenStateInfos(tempResult.getScreenStateInfos());
				result.setUserEvents(tempResult.getUserEvents());
				result.setWifiInfos(tempResult.getWifiInfos());
				result.getCpuActivityList().updateTimeRange(
						tempTimeRange.getBeginTime(), tempTimeRange.getEndTime());

			}
		}

		return finalResult(result,profile,filter);
	}
	protected PacketAnalyzerResult finalResult(AbstractTraceResult result, Profile profile, AnalysisFilter filter){
		PacketAnalyzerResult data = new PacketAnalyzerResult();
		List<PacketInfo> filteredPackets;  // List of packets included in analysis (application filtered)
		Profile aProfile = profile;
		if(aProfile == null){
			aProfile = profilefactory.createLTEdefault();//if the user doesn't load any profile.....
			aProfile.setName("AT&T LTE");
		} 
		
		// for the situation, filter out all noo-ip pacekts and caused the allpackets is empty, need to refactor
		if (result!=null && result.getAllpackets()!=null && result.getAllpackets().size()==0){
			data.setTraceresult(result);
			return data;
		}
		
		TimeRange timeRange = null;
		
 		if(filter == null && result!=null){
			filteredPackets = result.getAllpackets(); //initial, analyze  trace all packets
		}else{// do the filter 		
			filteredPackets = new ArrayList<PacketInfo>();	// create new packets according to the filter setting
			timeRange = filter.getTimeRange();
			// Filter packets based upon selected app names			
			int packetIdx = 0;
			
			boolean ipv4Flag = false;
			boolean ipv6Flag = false;
			boolean udpFlag = false;
			if(!(filter.isIpv4Sel() && filter.isIpv6Sel() && filter.isUdpSel())){
				
				if(!(filter.isIpv4Sel() && filter.isIpv6Sel())){
				
					if(!filter.isIpv4Sel()){
						ipv4Flag = true;
					}
					if(!filter.isIpv6Sel()){
						ipv6Flag = true;
					}	
				}
				if(!filter.isUdpSel()){
					udpFlag = true;
				}
				
			}

		if (result!=null) {
			for (PacketInfo packet : result.getAllpackets()) {

				if(ipv4Flag && packet.getRemoteIPAddress() instanceof Inet4Address){
					continue;
				}
				if(ipv6Flag && packet.getRemoteIPAddress() instanceof Inet6Address){
					continue;
				}
				if(udpFlag && packet.getPacket() instanceof UDPPacket){
					continue;
	
				}
	
					// Check time range
					double timestamp = packet.getTimeStamp();
					if (timeRange != null
							&& (timeRange.getBeginTime() > timestamp || timeRange.getEndTime() < timestamp)) {
						// Not in time range
						continue;
					}
					// Check to see if application is selected
					if (filter.getPacketColor(packet) == null) {
						// App unknown by filter
						continue;
					}
					packet.setPacketId(++packetIdx);
					filteredPackets.add(packet);
				} 
			}
		}
 		
		List<Session> sessionlist = sessionmanager.assembleSession(filteredPackets);
		List<PacketInfo> filteredPacketsNoDNSUDP = new ArrayList<PacketInfo>();
		for(Session session: sessionlist){
			for(PacketInfo packet:session.getPackets()){
				filteredPacketsNoDNSUDP.add(packet);
			}
		}
		int totaltemp = 0;
		for(Session byteCountSession:sessionlist){
			totaltemp += byteCountSession.getBytesTransferred();
		}
		Statistic stat = this.getStatistic(filteredPacketsNoDNSUDP);
		stat.setTotalByte(totaltemp);//to make sure match the same number with 4.1.1.
		//stat is used to get some info for RrcStateMachine etc
		if (result!=null){
			AbstractRrcStateMachine statemachine = statemachinefactory.create(filteredPackets, aProfile, 
					stat.getPacketDuration(), result.getTraceDuration(), stat.getTotalByte(), timeRange);
			
			EnergyModel energymodel = energymodelfactory.create(aProfile, statemachine.getTotalRRCEnergy(), 
					result.getGpsInfos(), result.getCameraInfos(), result.getBluetoothInfos(), result.getScreenStateInfos());
			
			BurstCollectionAnalysisData burstcollectiondata = burstcollectionanalyzer.analyze(filteredPackets, 
					aProfile, stat.getPacketSizeToCountMap(), statemachine.getStaterangelist(), 
					result.getUserEvents(), result.getCpuActivityList().getCpuActivities(), sessionlist);
			
			
			data.setBurstcollectionAnalysisData(burstcollectiondata);
			data.setEnergyModel(energymodel);
			data.setSessionlist(sessionlist);
			data.setStatemachine(statemachine);
			data.setStatistic(stat);
			data.setTraceresult(result);
			data.setProfile(aProfile);
			data.setFilter(filter);
		}
		return data;
	}
	@Override
	public Statistic getStatistic(List<PacketInfo> packetlist){
		Statistic stat = new Statistic();
		Set<String> appNames = new HashSet<String>();
		List<PacketInfo> packets = packetlist;
		if (!packets.isEmpty()) {
			int totalHTTPSBytes = 0;
			int totalBytes = 0;
			double avgKbps = 0;
			double packetsDuration = 0;
			List<IPPacketSummary> ipPacketSummary = new ArrayList<IPPacketSummary>();
			List<ApplicationPacketSummary> applicationPacketSummary = new ArrayList<ApplicationPacketSummary>();
			Map<Integer, Integer> packetSizeToCountMap = new HashMap<Integer, Integer>();
			
			PacketInfo lastPacket = packets.get(packets.size() - 1);
			Map<String, PacketCounter> appPackets = new HashMap<String, PacketCounter>();
			Map<InetAddress, PacketCounter> ipPackets = new HashMap<InetAddress, PacketCounter>();
			for (PacketInfo packet : packets) {
				//totalBytes += packet.getLen();
				
				if (packet.getPacket() instanceof TCPPacket) {
					TCPPacket tcp = (TCPPacket) packet.getPacket();
					if ((tcp.isSsl()) || (tcp.getDestinationPort() == 443) || (tcp.getSourcePort() == 443)) {
						totalHTTPSBytes += packet.getLen();
					}
					totalBytes += packet.getLen();
					
				} else {
					
					totalBytes += packet.getPayloadLen();
				}

				String appName = packet.getAppName();
				appNames.add(appName);
				PacketCounter pCounter = appPackets.get(appName);
				if (pCounter == null) {
					pCounter = new PacketCounter();
					appPackets.put(appName, pCounter);
				}
				pCounter.add(packet);

				if (packet.getPacket() instanceof IPPacket) {

					// Count packets by packet size
					Integer packetSize = packet.getPayloadLen();

					Integer iValue = packetSizeToCountMap.get(packetSize);
					if (iValue == null) {
						iValue = 1;
					} else {
						iValue++;
					}
					packetSizeToCountMap.put(packetSize, iValue);

					// Get IP address summary
					InetAddress ipAddress = packet.getRemoteIPAddress();
					pCounter = ipPackets.get(ipAddress);
					if (pCounter == null) {
						pCounter = new PacketCounter();
						ipPackets.put(ipAddress, pCounter);
					}
					pCounter.add(packet);
				}
			}
			
			for (Map.Entry<InetAddress, PacketCounter> ipPacketMap : ipPackets.entrySet()) {
				ipPacketSummary.add(new IPPacketSummary(ipPacketMap.getKey(), ipPacketMap.getValue().getPacketCount(), ipPacketMap
						.getValue().getTotalBytes()));
			}
			for (Map.Entry<String, PacketCounter> appPacketMap : appPackets.entrySet()) {
				applicationPacketSummary.add(new ApplicationPacketSummary(appPacketMap.getKey(), appPacketMap
						.getValue().getPacketCount(), appPacketMap.getValue().getTotalBytes()));
			}

			packetsDuration = lastPacket.getTimeStamp() - packets.get(0).getTimeStamp();
			avgKbps = packetsDuration != 0 ? totalBytes * 8.0 / 1000.0 / packetsDuration : 0.0;
			
			stat.setApplicationPacketSummary(applicationPacketSummary);
			stat.setAppName(appNames);
			stat.setAverageKbps(avgKbps);
			stat.setIpPacketSummary(ipPacketSummary);
			stat.setPacketDuration(packetsDuration);
			stat.setTotalByte(totalBytes);
			stat.setTotalHTTPSByte(totalHTTPSBytes);
			stat.setTotalPackets(packets.size());
			stat.setPacketSizeToCountMap(packetSizeToCountMap);
		}
		
		stat.setAppName(appNames);
		return stat;
	}
}//end class
