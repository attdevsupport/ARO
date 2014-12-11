/**
 * 
 */
package com.att.aro.json;

import java.util.ArrayList;
import java.util.List;

import com.att.aro.model.TraceData;

/**
 * @author hy0910
 *
 */
public class TraceScoreGenerator {
	
	private int cathcControlScore;
	private int connectionClosingScore;
	private int tightlyGroupConnScore;
	private int signalingOverheadScore;
	private int periodicTransferScore;
	private int causeSubTotalScore;
	private int duplicateContentScore;
	private int contentExpirationScore;
	private int averageRateScore;
	private int energyConsumptionScore;
	private int effectSubTotalScore;
	private int totalScore;
	
	public TraceScoreGenerator(){
		
	}
	
	public void refresh(TraceData.Analysis analysis){
		cathcControlScore = analysis.getApplicationScore().getCacheHeaderControlScore();
		connectionClosingScore = analysis.getApplicationScore().getConnectionClosingScore();
		tightlyGroupConnScore = analysis.getApplicationScore().getTightlyGroupConnectionScore();
		signalingOverheadScore = analysis.getApplicationScore().getSignalingOverheadScore();
		periodicTransferScore = analysis.getApplicationScore().getPeriodicTransferScore();
		causeSubTotalScore = analysis.getApplicationScore().getCausesScore();
		duplicateContentScore = analysis.getApplicationScore().getDuplicateContentScore();
		contentExpirationScore = analysis.getApplicationScore().getConnectionExpirationScore();
		averageRateScore = analysis.getApplicationScore().getAverageRateScore();
		energyConsumptionScore = analysis.getApplicationScore().getEnergyEfficiencyScore();
		effectSubTotalScore = analysis.getApplicationScore().getEffectScore();
		totalScore = analysis.getApplicationScore().getTotalApplicationScore();

		
	}
	
	public TraceScore getTraceScore(){
		TraceScore traceScore = new TraceScore();
		
		List<TraceScoreDetails> causes = new ArrayList<TraceScoreDetails>();
			TraceScoreDetails cacheControl = new TraceScoreDetails();
			cacheControl.setName("Cache Control");
			cacheControl.setValue(this.cathcControlScore);
			cacheControl.setDetails("out of 75");
			causes.add(cacheControl);
			
			TraceScoreDetails connectionClose = new TraceScoreDetails();
			connectionClose.setName("Connection Closing Problems");
			connectionClose.setValue(this.connectionClosingScore);
			connectionClose.setDetails("out of 75");
			causes.add(connectionClose);
			
			TraceScoreDetails groupedConnection = new TraceScoreDetails();
			groupedConnection.setName("Tightly Grouped Connections");
			groupedConnection.setValue(this.tightlyGroupConnScore);
			groupedConnection.setDetails("out of 150");
			causes.add(groupedConnection);
			
			TraceScoreDetails periodicTransfer = new TraceScoreDetails();
			periodicTransfer.setName("Periodic Transfers");
			periodicTransfer.setValue(this.periodicTransferScore);
			periodicTransfer.setDetails("out of 150");
			causes.add(periodicTransfer);
			
			TraceScoreDetails contentExpiration = new TraceScoreDetails();
			contentExpiration.setName("Content Expiration");
			contentExpiration.setValue(this.contentExpirationScore);
			contentExpiration.setDetails("out of 50");
			causes.add(contentExpiration);
			
			TraceScoreDetails causesSubTotal = new TraceScoreDetails();
			causesSubTotal.setName("Content Expiration");
			causesSubTotal.setValue(this.causeSubTotalScore);
			causesSubTotal.setDetails("out of 500");
			causes.add(causesSubTotal);
		traceScore.setCauses(causes.toArray(new TraceScoreDetails[causes.size()]));
		
			List<TraceScoreDetails> effects = new ArrayList<TraceScoreDetails>();
			TraceScoreDetails duplicateContent = new TraceScoreDetails();
			duplicateContent.setName("Duplicate Content");
			duplicateContent.setValue(this.duplicateContentScore);
			duplicateContent.setDetails("out of 125");
			effects.add(duplicateContent);
			
			TraceScoreDetails signalingOverHead = new TraceScoreDetails();
			signalingOverHead.setName("Signaling Overhead");
			signalingOverHead.setValue(this.signalingOverheadScore);
			signalingOverHead.setDetails("out of 125");
			effects.add(signalingOverHead);
			
			TraceScoreDetails averageRate = new TraceScoreDetails();
			averageRate.setName("Average Rate");
			averageRate.setValue(this.averageRateScore);
			averageRate.setDetails("out of 62.5");
			effects.add(averageRate);
			
			TraceScoreDetails energyEfficiency = new TraceScoreDetails();
			energyEfficiency.setName("Energy Efficiency");
			energyEfficiency.setValue(this.energyConsumptionScore);
			energyEfficiency.setDetails("out of 187.5");
			effects.add(energyEfficiency);
			
			TraceScoreDetails effectsSubTotal = new TraceScoreDetails();
			effectsSubTotal.setName("Effects Sub-total");
			effectsSubTotal.setValue(this.effectSubTotalScore);
			effectsSubTotal.setDetails("out of 500");
			effects.add(effectsSubTotal);
			
			TraceScoreDetails total = new TraceScoreDetails();
			total.setName("Total Score");
			total.setValue(this.totalScore);
			total.setDetails("out of 1000");
			effects.add(total);
			
		traceScore.setEffects(effects.toArray(new TraceScoreDetails[effects.size()]));
			
		return traceScore;	
			
			
	}
}
