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

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.att.aro.core.configuration.pojo.Profile;

/**
 * Data returned from analyzing trace file or directory
 * 
 * @author Borey Sao Date: November 7, 2014
 */
public class PacketAnalyzerResult {
	private AbstractTraceResult traceresult;
	@JsonIgnore
	private List<Session> sessionlist;

	private Statistic statistic;
	private AbstractRrcStateMachine statemachine;
	private EnergyModel energyModel;

	private BurstCollectionAnalysisData burstCollectionAnalysisData;
	
	//For changing the packets based on selection
	private boolean ipv4Packets = true;
	private boolean ipv6Packets = true;
	private boolean udpPackets = true;

	
	/**
	 * The profile used in creating the EnergyModel
	 */
	private Profile profile;
	
	/**
	 * AnalysisFilter is used to limit the analysis results.<p>
	 * A null value is no filtering
	 */
	private AnalysisFilter filter;
	
	/**
	 * Results of a cache analysis of the HTTP requests and responses.
	 */
	private CacheAnalysis cacheAnalysis = null;

	/**
	 * Returns trace results
	 *  
	 * @return Returns trace results
	 */
	public AbstractTraceResult getTraceresult() {
		return traceresult;
	}

	/**
	 * Sets trace results
	 * 
	 * @param traceresult an AbstractTraceResult object
	 */
	public void setTraceresult(AbstractTraceResult traceresult) {
		this.traceresult = traceresult;
	}

	/**
	 * Returns sessionlist a List of all sessions contained in trace
	 * 
	 * @return sessionlist a List of sessions
	 */
	public List<Session> getSessionlist() {
		return sessionlist;
	}

	/**
	 * Stores the List of all sessions contained in trace
	 * 
	 * @param sessionlist a List of sessions
	 */
	public void setSessionlist(List<Session> sessionlist) {
		this.sessionlist = sessionlist;
	}

	/**
	 * Returns the Statistic object containing info about the trace.
	 * 
	 * @return Returns the Statistic object containing info about the trace
	 */
	public Statistic getStatistic() {
		return statistic;
	}

	/**
	 * Sets Statistic info about the trace
	 * 
	 * @param statistic info about the trace
	 */
	public void setStatistic(Statistic statistic) {
		this.statistic = statistic;
	}

	/**
	 * Returns resource state machine (AbstractRrcStateMachine) energy and duration
	 * 
	 * @return Returns resource state machine (AbstractRrcStateMachine) energy and duration
	 */
	public AbstractRrcStateMachine getStatemachine() {
		return statemachine;
	}

	/**
	 * Sets resource state machine (AbstractRrcStateMachine) energy and duration
	 * 
	 * @param statemachine a AbstractRrcStateMachine object
	 */
	public void setStatemachine(AbstractRrcStateMachine statemachine) {
		this.statemachine = statemachine;
	}

	/**
	 * Returns an energy model (EnergyModel) of peripherals
	 * 
	 * @return an energy model (EnergyModel) of peripherals
	 */
	public EnergyModel getEnergyModel() {
		return energyModel;
	}

	/**
	 * Sets an energy model (EnergyModel) of peripherals
	 * 
	 * @param energyModel an EnergyModel object
	 */
	public void setEnergyModel(EnergyModel energyModel) {
		this.energyModel = energyModel;
	}

	/**
	 * Returns burst collection analysis data
	 * 
	 * @return a BurstCollectionAnalysisData object
	 */
	public BurstCollectionAnalysisData getBurstcollectionAnalysisData() {
		return burstCollectionAnalysisData;
	}

	/**
	 * Sets burst collection analysis data
	 * 
	 * @param burstcollectionAnalysisData a BurstCollectionAnalysisData object
	 */
	public void setBurstcollectionAnalysisData(BurstCollectionAnalysisData burstcollectionAnalysisData) {
		this.burstCollectionAnalysisData = burstcollectionAnalysisData;
	}

	/**
	 * Returns profile data, profiling the device used in trace
	 * 
	 * @return profile data of device
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Sets profile data, profiling the device used in trace
	 * 
	 * @param profile data of device
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	/**
	 * Returns filter used when analyzing trace
	 * 
	 * @return filter used when analyzing trace
	 */
	public AnalysisFilter getFilter() {
		return filter;
	}

	/**
	 * Sets filter used when analyzing trace
	 * 
	 * @param filter used when analyzing trace
	 */
	public void setFilter(AnalysisFilter filter) {
		this.filter = filter;
	}

	/**
	 * Returns cache analysis of trace
	 * 
	 * @return cacheAnalysis a CacheAnalysis object
	 */
	public CacheAnalysis getCacheAnalysis() {
		return cacheAnalysis;
	}
	
	/**
	 * Sets cache analysis of trace
	 * 
	 * @param cacheAnalysis - results from a cache analysis of the HTTP requests and responses
	 */
	public void setCacheAnalysis(CacheAnalysis cacheAnalysis) {
		this.cacheAnalysis = cacheAnalysis;
	}

	/**
	 * @return the ipv4Packets
	 */
	public boolean isIpv4Packets() {
		return ipv4Packets;
	}

	/**
	 * @param ipv4Packets the ipv4Packets to set
	 */
	public void setIpv4Packets(boolean ipv4Packets) {
		this.ipv4Packets = ipv4Packets;
	}

	/**
	 * @return the ipv6Packets
	 */
	public boolean isIpv6Packets() {
		return ipv6Packets;
	}

	/**
	 * @param ipv6Packets the ipv6Packets to set
	 */
	public void setIpv6Packets(boolean ipv6Packets) {
		this.ipv6Packets = ipv6Packets;
	}

	/**
	 * @return the udpPackets
	 */
	public boolean isUdpPackets() {
		return udpPackets;
	}

	/**
	 * @param udpPackets the udpPackets to set
	 */
	public void setUdpPackets(boolean udpPackets) {
		this.udpPackets = udpPackets;
	}

}
