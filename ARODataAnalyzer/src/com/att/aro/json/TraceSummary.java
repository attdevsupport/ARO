/**
 * 
 */
package com.att.aro.json;

import java.util.Date;

/**
 * @author hy0910
 *
 */
public class TraceSummary {
	
	private Date date;
	private String trace;
	private String dataCollectorVersion;
	private String osPlatformVersion;
	private String deviceMake;
	private String deciceModel;
	private long ipPacketCount;
	private String[] networkTypes;
	private String profile;
	
	private ApplicationDetails[] applications;
	private Mesurment totalHttpsData;
	private Mesurment httpsDataAnalyzed;
	private Mesurment httpsDataNotAnalyzed;
	private Mesurment duration;
	private Mesurment totalDataTransferred;
	private Mesurment energyConsumed;
	private Score traceScore;
	private Mesurment averageRate;
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	/**
	 * @return the trace
	 */
	public String getTrace() {
		return trace;
	}
	/**
	 * @param trace the trace to set
	 */
	public void setTrace(String trace) {
		this.trace = trace;
	}
	/**
	 * @return the dataCollectorVersion
	 */
	public String getDataCollectorVersion() {
		return dataCollectorVersion;
	}
	/**
	 * @param dataCollectorVersion the dataCollectorVersion to set
	 */
	public void setDataCollectorVersion(String dataCollectorVersion) {
		this.dataCollectorVersion = dataCollectorVersion;
	}
	/**
	 * @return the osPlatformVersion
	 */
	public String getOsPlatformVersion() {
		return osPlatformVersion;
	}
	/**
	 * @param osPlatformVersion the osPlatformVersion to set
	 */
	public void setOsPlatformVersion(String osPlatformVersion) {
		this.osPlatformVersion = osPlatformVersion;
	}
	/**
	 * @return the deviceMake
	 */
	public String getDeviceMake() {
		return deviceMake;
	}
	/**
	 * @param deviceMake the deviceMake to set
	 */
	public void setDeviceMake(String deviceMake) {
		this.deviceMake = deviceMake;
	}
	/**
	 * @return the deciceModel
	 */
	public String getDeciceModel() {
		return deciceModel;
	}
	/**
	 * @param deciceModel the deciceModel to set
	 */
	public void setDeciceModel(String deciceModel) {
		this.deciceModel = deciceModel;
	}
	/**
	 * @return the ipPacketCount
	 */
	public long getIpPacketCount() {
		return ipPacketCount;
	}
	/**
	 * @param ipPacketCount the ipPacketCount to set
	 */
	public void setIpPacketCount(long ipPacketCount) {
		this.ipPacketCount = ipPacketCount;
	}
	/**
	 * @return the networkTypes
	 */
	public String[] getNetworkTypes() {
		return networkTypes;
	}
	/**
	 * @param networkTypes the networkTypes to set
	 */
	public void setNetworkTypes(String[] networkTypes) {
		this.networkTypes = networkTypes;
	}
	/**
	 * @return the profile
	 */
	public String getProfile() {
		return profile;
	}
	/**
	 * @param profile the profile to set
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}
	/**
	 * @return the applications
	 */
	public ApplicationDetails[] getApplications() {
		return applications;
	}
	/**
	 * @param applications the applications to set
	 */
	public void setApplications(ApplicationDetails[] applications) {
		this.applications = applications;
	}
	/**
	 * @return the totalHttpsData
	 */
	public Mesurment getTotalHttpsData() {
		return totalHttpsData;
	}
	/**
	 * @param totalHttpsData the totalHttpsData to set
	 */
	public void setTotalHttpsData(Mesurment totalHttpsData) {
		this.totalHttpsData = totalHttpsData;
	}
	/**
	 * @return the httpsDataAnalyzed
	 */
	public Mesurment getHttpsDataAnalyzed() {
		return httpsDataAnalyzed;
	}
	/**
	 * @param httpsDataAnalyzed the httpsDataAnalyzed to set
	 */
	public void setHttpsDataAnalyzed(Mesurment httpsDataAnalyzed) {
		this.httpsDataAnalyzed = httpsDataAnalyzed;
	}
	/**
	 * @return the httpsDataNotAnalyzed
	 */
	public Mesurment getHttpsDataNotAnalyzed() {
		return httpsDataNotAnalyzed;
	}
	/**
	 * @param httpsDataNotAnalyzed the httpsDataNotAnalyzed to set
	 */
	public void setHttpsDataNotAnalyzed(Mesurment httpsDataNotAnalyzed) {
		this.httpsDataNotAnalyzed = httpsDataNotAnalyzed;
	}
	/**
	 * @return the duration
	 */
	public Mesurment getDuration() {
		return duration;
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(Mesurment duration) {
		this.duration = duration;
	}
	/**
	 * @return the totalDataTransferred
	 */
	public Mesurment getTotalDataTransferred() {
		return totalDataTransferred;
	}
	/**
	 * @param totalDataTransferred the totalDataTransferred to set
	 */
	public void setTotalDataTransferred(Mesurment totalDataTransferred) {
		this.totalDataTransferred = totalDataTransferred;
	}
	/**
	 * @return the energyConsumed
	 */
	public Mesurment getEnergyConsumed() {
		return energyConsumed;
	}
	/**
	 * @param energyConsumed the energyConsumed to set
	 */
	public void setEnergyConsumed(Mesurment energyConsumed) {
		this.energyConsumed = energyConsumed;
	}
	/**
	 * @return the traceScore
	 */
	public Score getTraceScore() {
		return traceScore;
	}
	/**
	 * @param traceScore the traceScore to set
	 */
	public void setTraceScore(Score traceScore) {
		this.traceScore = traceScore;
	}
	/**
	 * @return the averageRate
	 */
	public Mesurment getAverageRate() {
		return averageRate;
	}
	/**
	 * @param averageRate the averageRate to set
	 */
	public void setAverageRate(Mesurment averageRate) {
		this.averageRate = averageRate;
	}

	
}
