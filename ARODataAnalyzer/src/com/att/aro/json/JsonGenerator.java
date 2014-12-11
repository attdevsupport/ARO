/**
 * 
 */
package com.att.aro.json;

import java.util.List;

import com.att.aro.main.AROBestPracticesTab;
import com.att.aro.model.CacheInfoParser;
import com.att.aro.model.EnergyModel;
import com.att.aro.model.FileTypeSummary;
import com.att.aro.model.TraceData;

/**
 * @author hy0910
 *
 */
public class JsonGenerator {
	
	private TraceSummaryGenerator traceSummaryGenerator;
	private BenchMarkingGenerator benchMarkGenerator;
	private TraceScoreGenerator traceScoreGenerator;
	private EndpointSummaryGenerator endpointSummaryGenerator;
	private RRCStateGenerator rrcStatesGenerator;
	private BurstAnalysisGenerator burstAnalysisGenerator;
	
	private CacheInfoParser cacheParser;
	private EnergyModel energyEfficency;
	
	private List<FileTypeSummary> content;
	TraceData.Analysis analysisData = null;
	
	
	public JsonGenerator(){
		
	}
	
	public void refresh(TraceData.Analysis analysisData){
		if(analysisData != null){
			
			this.analysisData = analysisData;
			getTraceSummaryGanerator().refresh(analysisData);
			this.content = analysisData.constructContent(analysisData);
			getBenchMarkGenerator().refresh(analysisData);
			getTraceScoreGenerator().refresh(analysisData);
			getEndpointSummaryGenerator().refresh(analysisData);
			getRRCStateGenerator().refresh(analysisData);
			getBurstAnalysisGenerator().refresh(analysisData);
			this.cacheParser = analysisData.getCacheInfoParser();
			this.energyEfficency = analysisData.getEnergyModel();
			
		}
		
	}
	
	
	public JsonMapper getJsonMapper(AROBestPracticesTab aroBestPracticesPanel){
		JsonMapper jsonFileMapper = new JsonMapper();
		
		jsonFileMapper.setTraceSummary(getTraceSummaryGanerator().getTraceSummaryObject());
		jsonFileMapper.setBestPractices(aroBestPracticesPanel.getBestPracticesPanel().getBestPracticeContentForJson(analysisData));
		jsonFileMapper.setFileTypeSummary(getFileTypesSummery());
		jsonFileMapper.setTraceBenchmarking(getBenchMarkGenerator().getBenchMarkingDetails());
		jsonFileMapper.setConnectionStatistics(getBenchMarkGenerator().getConnectionStats());
		
		jsonFileMapper.setTraceScore(getTraceScoreGenerator().getTraceScore());
		
		jsonFileMapper.setApplicationEndpointSummary(getEndpointSummaryGenerator().getAppPacketSummary());
		jsonFileMapper.setIpAddressEndpointSummary(getEndpointSummaryGenerator().getIPPacketSummary());
		jsonFileMapper.setRrcMachineStates(getRRCStateGenerator().getRRCStateDetails());
		
		jsonFileMapper.setBurstAnalysis(getBurstAnalysisGenerator().getBurstAnalysis());
		jsonFileMapper.setIndividualBurstAnalysis(getBurstAnalysisGenerator().getAllIndividualBursts());
		jsonFileMapper.setCacheSimulationDetails(this.cacheParser);
		jsonFileMapper.setEnergyEffeciencyDetails(this.energyEfficency);
		
		
		return jsonFileMapper;
	}
		
	private TraceSummaryGenerator getTraceSummaryGanerator(){
		if(traceSummaryGenerator == null){
			traceSummaryGenerator = new TraceSummaryGenerator();
		}
		
		return traceSummaryGenerator;
	}
	
	private FileTypeSummary[] getFileTypesSummery(){
		FileTypeSummary[] fileypes = null;
		if(this.content != null){
			fileypes = this.content.toArray(new FileTypeSummary[this.content.size()]);
		}
		
		return fileypes;
	}
	
	private BenchMarkingGenerator getBenchMarkGenerator(){
		if(this.benchMarkGenerator == null){
			this.benchMarkGenerator = new BenchMarkingGenerator();
		}
		
		return this.benchMarkGenerator;
	}
	
	private TraceScoreGenerator getTraceScoreGenerator(){
		if(this.traceScoreGenerator == null){
			this.traceScoreGenerator = new TraceScoreGenerator();
		}
		return this.traceScoreGenerator;
			
	}
	
	private EndpointSummaryGenerator getEndpointSummaryGenerator(){
		if(this.endpointSummaryGenerator == null){
			this.endpointSummaryGenerator = new EndpointSummaryGenerator();
		}
		return this.endpointSummaryGenerator;
	}

	private RRCStateGenerator getRRCStateGenerator(){
		if(this.rrcStatesGenerator == null){
			this.rrcStatesGenerator = new RRCStateGenerator();
		}
		return this.rrcStatesGenerator;
	}
	
	private BurstAnalysisGenerator getBurstAnalysisGenerator(){
		if(this.burstAnalysisGenerator == null){
			this.burstAnalysisGenerator = new BurstAnalysisGenerator();
		}
		return this.burstAnalysisGenerator;
	}
}
