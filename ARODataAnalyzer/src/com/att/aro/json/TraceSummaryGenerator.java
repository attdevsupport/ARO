/**
 * 
 */
package com.att.aro.json;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.NetworkType;
import com.att.aro.model.TraceData;
import com.att.aro.util.Util;

/**
 * @author hy0910
 *
 */
public class TraceSummaryGenerator {
	
	private static final ResourceBundle RB = ResourceBundleManager.getDefaultBundle();

	private TraceSummary summary;
	
	public TraceSummaryGenerator(){
		
	}
	
	public void refresh(TraceData.Analysis analysisData){
		if (analysisData != null) {
			summary = new TraceSummary();
		
			TraceData traceData = analysisData.getTraceData();
			
			this.summary.setDate(traceData.getTraceDateTime()); //Date
			this.summary.setTrace(traceData.getTraceDir().getName()); // Trace Dir

			
			List<ApplicationDetails> applicationsList = new ArrayList<ApplicationDetails>(); //Applications
			for (String app : analysisData.getAppNames()) {
				
				ApplicationDetails appData = new ApplicationDetails();
				String appVersion = analysisData.getTraceData().getAppVersionMap().get(app);
				String appName = Util.getDefaultAppName(app);
				appData.setName(appName);
				appData.setVersion((appVersion != null ? " : " + appVersion : ""));
				
				applicationsList.add(appData);
			}
			this.summary.setApplications(applicationsList.toArray(new ApplicationDetails[applicationsList.size()])); //add applications
			
			
			if (traceData.getDeviceMake() != null || traceData.getDeviceModel() != null) {
				
				this.summary.setDeviceMake(traceData.getDeviceMake()); //Device Make
				this.summary.setDeciceModel(traceData.getDeviceModel()); //Device Version
			} 
			 this.summary.setOsPlatformVersion(traceData.getOsVersion()); //Os Version

			
			 List<String> networkTypesList = new ArrayList<String>();  //Network List
			if (analysisData.getNetworTypeInfos().size() > 1) {

				for (NetworkType networkType : traceData.getNetworkTypes()) {

					networkTypesList.add(ResourceBundleManager.getEnumString(networkType));
				}

			} else {
				networkTypesList.add(ResourceBundleManager.getEnumString(traceData.getNetworkType()));
			}
			this.summary.setNetworkTypes(networkTypesList.toArray(new String[networkTypesList.size()]));
			
			this.summary.setProfile(analysisData.getProfile().getName()); //Profile
			
			if(TraceData.getCryptAdapter() != null) {
				double totalhttpsDataKB = (double)analysisData.getTotalHTTPSBytes()/1024;
				
				//Https Data
				Mesurment totalHttpsData = new Mesurment();
				totalHttpsData.setPercentage((double)analysisData.getTotalHTTPSBytes()/analysisData.getTotalBytes());
				totalHttpsData.setValue(totalhttpsDataKB);
				totalHttpsData.setUnits("KB");
				this.summary.setTotalHttpsData(totalHttpsData);
			
				double httpsDataAnalyzedKB = (double)analysisData.getTotalHTTPSAnalyzedBytes()/1024;
							
				Mesurment httpsDataAnalyzed = new Mesurment();
				httpsDataAnalyzed.setPercentage((double)analysisData.getTotalHTTPSAnalyzedBytes()/analysisData.getTotalBytes());
				httpsDataAnalyzed.setValue(httpsDataAnalyzedKB);
				httpsDataAnalyzed.setUnits("KB");
				this.summary.setHttpsDataAnalyzed(httpsDataAnalyzed);
			}
			
			long httpsDataNotAnalyzed = analysisData.getTotalHTTPSBytes() - analysisData.getTotalHTTPSAnalyzedBytes();
			double httpsDataNotAnalyzedKB = (double)httpsDataNotAnalyzed/1024;
			double httpsDataNotAnalyzedPct = (double)httpsDataNotAnalyzed/analysisData.getTotalBytes();
		
			Mesurment httpsDataNotAnalyzedObj = new Mesurment();
			httpsDataNotAnalyzedObj.setPercentage(httpsDataNotAnalyzedPct);
			httpsDataNotAnalyzedObj.setValue(httpsDataNotAnalyzedKB);
			httpsDataNotAnalyzedObj.setUnits("KB");
			this.summary.setHttpsDataNotAnalyzed(httpsDataNotAnalyzedObj);
			
			Mesurment duration = new Mesurment();
			duration.setValue(analysisData.getTraceData().getTraceDuration() / 60);
			duration.setUnits(RB.getString("statics.csvUnits.minutes"));
			this.summary.setDuration(duration);
			
			Mesurment totalDataTransferred = new Mesurment();
			totalDataTransferred.setValue(analysisData.getTotalBytes());
			totalDataTransferred.setUnits(RB.getString("statics.csvUnits.bytes"));
			this.summary.setTotalDataTransferred(totalDataTransferred);
			
			Mesurment energyConsumed = new Mesurment();
			energyConsumed.setType("Energy");
			energyConsumed.setValue(analysisData.getEnergyModel().getTotalEnergyConsumed());
			energyConsumed.setUnits(RB.getString("statics.csvUnits.j"));
			this.summary.setEnergyConsumed(energyConsumed);
			
			Score traceScore = new Score();
			traceScore.setCauses(analysisData.getApplicationScore().getCausesScore());
			traceScore.setEffects(analysisData.getApplicationScore().getEffectScore());
			traceScore.setTotal(analysisData.getApplicationScore().getTotalApplicationScore());
			this.summary.setTraceScore(traceScore);
			
			this.summary.setIpPacketCount(analysisData.getPackets().size());
			
			Mesurment averageRate = new Mesurment();
			averageRate.setValue(analysisData.getAvgKbps());
			averageRate.setUnits("kbps");
			this.summary.setAverageRate(averageRate);
						
		} else {
			this.summary = null;
			
		}

	}
	
	public TraceSummary getTraceSummaryObject(){
		
		return this.summary;
		
	}

}
