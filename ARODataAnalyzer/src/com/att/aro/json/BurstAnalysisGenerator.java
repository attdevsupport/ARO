/**
 * 
 */
package com.att.aro.json;

import java.util.ArrayList;
import java.util.List;

import com.att.aro.model.Burst;
import com.att.aro.model.BurstAnalysisInfo;
import com.att.aro.model.TraceData;

/**
 * @author hy0910
 *
 */
public class BurstAnalysisGenerator {
	
	List<BurstAnalysis> burstAnalysisList = new ArrayList<BurstAnalysis>();
	List<BurstDetails> burstDetailsList = new ArrayList<BurstDetails>();
	
	public BurstAnalysisGenerator(){
		
	}
	
	public void refresh(TraceData.Analysis analysis){
		List<BurstAnalysisInfo> burstAnalysisInfoList = analysis.getBcAnalysis().getBurstAnalysisInfo();
		List<Burst> individualBurstList = analysis.getBcAnalysis().getBurstCollection();
		if(burstAnalysisInfoList != null ){
			for (BurstAnalysisInfo burstAnalysisInfo: burstAnalysisInfoList) {
				BurstAnalysis burstAnalysis = new BurstAnalysis();
				
				burstAnalysis.setBurst(burstAnalysisInfo.getCategory().getBurstTypeDescription());
				burstAnalysis.setBytes(burstAnalysisInfo.getPayload());
				burstAnalysis.setBytePercentage(burstAnalysisInfo.getPayloadPct());
				burstAnalysis.setEnergy(burstAnalysisInfo.getEnergy());
				burstAnalysis.setEnergyPercentage(burstAnalysisInfo.getEnergyPct());
				burstAnalysis.setRrcActiveTime(burstAnalysisInfo.getRRCActiveTime());
				burstAnalysis.setRrcActiveTimePercentage(burstAnalysisInfo.getRRCActivePercentage());
				burstAnalysisList.add(burstAnalysis);
				
			}
		}
		
		if(individualBurstList != null){
			for (Burst burst : individualBurstList) {
				BurstDetails burstDetails = new BurstDetails();
				burstDetails.setBurst(burst.getBurstCategory().getBurstTypeDescription());
				burstDetails.setStartTime(burst.getBeginTime());
				burstDetails.setTimeElasped(burst.getElapsedTime());
				burstDetails.setBytes(burst.getBurstBytes());
				burstDetails.setPacketCount(burst.getPackets().size());
				burstDetailsList.add(burstDetails);
			}
		}
		
	}
	
	public BurstAnalysis[] getBurstAnalysis(){
		return this.burstAnalysisList.toArray(new BurstAnalysis[this.burstAnalysisList.size()]);
	}

	public BurstDetails[] getAllIndividualBursts(){
		return this.burstDetailsList.toArray(new BurstDetails[this.burstDetailsList.size()]);
	}
	
	
}
