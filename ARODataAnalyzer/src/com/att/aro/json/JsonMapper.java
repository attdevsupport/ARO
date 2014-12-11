/**
 * 
 */
package com.att.aro.json;

import com.att.aro.model.ApplicationPacketSummary;
import com.att.aro.model.CacheInfoParser;
import com.att.aro.model.EnergyModel;
import com.att.aro.model.FileTypeSummary;
import com.att.aro.model.IPPacketSummary;

/**
 * @author hy0910
 *
 */
public class JsonMapper {
	
	private TraceSummary traceSummary;
	private BestPractices[] bestPractices;
	private FileTypeSummary[] fileTypeSummary;
	private TraceBenchmarking[] traceBenchmarking;
	private ConnectionStatistics[] connectionStatistics;
	private TraceScore traceScore;
	private ApplicationPacketSummary[] applicationEndpointSummary;
	private IPPacketSummary[] ipAddressEndpointSummary;
	private RRCStateSimulator rrcMachineStates;
	
	private BurstAnalysis[] burstAnalysis;
	private BurstDetails[] individualBurstAnalysis;
	
	private CacheInfoParser cacheSimulationDetails;
	private EnergyModel energyEffeciencyDetails;
	
	
	/**
	 * @return the traceSummary
	 */
	public TraceSummary getTraceSummary() {
		return traceSummary;
	}
	/**
	 * @param traceSummary the traceSummary to set
	 */
	public void setTraceSummary(TraceSummary traceSummary) {
		this.traceSummary = traceSummary;
	}
	/**
	 * @return the bestPractices
	 */
	public BestPractices[] getBestPractices() {
		return bestPractices;
	}
	/**
	 * @param bestPractices the bestPractices to set
	 */
	public void setBestPractices(BestPractices[] bestPractices) {
		this.bestPractices = bestPractices;
	}

	/**
	 * @return the fileTypeSummary
	 */
	public FileTypeSummary[] getFileTypeSummary() {
		return fileTypeSummary;
	}
	/**
	 * @param fileTypeSummary the fileTypeSummary to set
	 */
	public void setFileTypeSummary(FileTypeSummary[] fileTypeSummary) {
		this.fileTypeSummary = fileTypeSummary;
	}
	/**
	 * @return the traceBenchmarking
	 */
	public TraceBenchmarking[] getTraceBenchmarking() {
		return traceBenchmarking;
	}
	/**
	 * @param traceBenchmarking the traceBenchmarking to set
	 */
	public void setTraceBenchmarking(TraceBenchmarking[] traceBenchmarking) {
		this.traceBenchmarking = traceBenchmarking;
	}
	/**
	 * @return the connectionStatistics
	 */
	public ConnectionStatistics[] getConnectionStatistics() {
		return connectionStatistics;
	}
	/**
	 * @param connectionStatistics the connectionStatistics to set
	 */
	public void setConnectionStatistics(ConnectionStatistics[] connectionStatistics) {
		this.connectionStatistics = connectionStatistics;
	}
	/**
	 * @return the traceScore
	 */
	public TraceScore getTraceScore() {
		return traceScore;
	}
	/**
	 * @param traceScore the traceScore to set
	 */
	public void setTraceScore(TraceScore traceScore) {
		this.traceScore = traceScore;
	}
	/**
	 * @return the applicationEndpointSummary
	 */
	public ApplicationPacketSummary[] getApplicationEndpointSummary() {
		return applicationEndpointSummary;
	}
	/**
	 * @param applicationEndpointSummary the applicationEndpointSummary to set
	 */
	public void setApplicationEndpointSummary(
			ApplicationPacketSummary[] applicationEndpointSummary) {
		this.applicationEndpointSummary = applicationEndpointSummary;
	}
	/**
	 * @return the ipAddressEndpointSummary
	 */
	public IPPacketSummary[] getIpAddressEndpointSummary() {
		return ipAddressEndpointSummary;
	}
	/**
	 * @param ipAddressEndpointSummary the ipAddressEndpointSummary to set
	 */
	public void setIpAddressEndpointSummary(
			IPPacketSummary[] ipAddressEndpointSummary) {
		this.ipAddressEndpointSummary = ipAddressEndpointSummary;
	}
	/**
	 * @return the rrcMachineStates
	 */
	public RRCStateSimulator getRrcMachineStates() {
		return rrcMachineStates;
	}
	/**
	 * @param rrcMachineStates the rrcMachineStates to set
	 */
	public void setRrcMachineStates(RRCStateSimulator rrcMachineStates) {
		this.rrcMachineStates = rrcMachineStates;
	}
	/**
	 * @return the burstAnalysis
	 */
	public BurstAnalysis[] getBurstAnalysis() {
		return burstAnalysis;
	}
	/**
	 * @param burstAnalysis the burstAnalysis to set
	 */
	public void setBurstAnalysis(BurstAnalysis[] burstAnalysis) {
		this.burstAnalysis = burstAnalysis;
	}
	/**
	 * @return the individualBurstAnalysis
	 */
	public BurstDetails[] getIndividualBurstAnalysis() {
		return individualBurstAnalysis;
	}
	/**
	 * @param individualBurstAnalysis the individualBurstAnalysis to set
	 */
	public void setIndividualBurstAnalysis(BurstDetails[] individualBurstAnalysis) {
		this.individualBurstAnalysis = individualBurstAnalysis;
	}
	/**
	 * @return the cacheSimulationDetails
	 */
	public CacheInfoParser getCacheSimulationDetails() {
		return cacheSimulationDetails;
	}
	/**
	 * @param cacheSimulationDetails the cacheSimulationDetails to set
	 */
	public void setCacheSimulationDetails(CacheInfoParser cacheSimulationDetails) {
		this.cacheSimulationDetails = cacheSimulationDetails;
	}
	/**
	 * @return the energyEffeciencyDetails
	 */
	public EnergyModel getEnergyEffeciencyDetails() {
		return energyEffeciencyDetails;
	}
	/**
	 * @param energyEffeciencyDetails the energyEffeciencyDetails to set
	 */
	public void setEnergyEffeciencyDetails(EnergyModel energyEffeciencyDetails) {
		this.energyEffeciencyDetails = energyEffeciencyDetails;
	}
	
	
	
}
