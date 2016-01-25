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
public class BenchMarkingGenerator {
	private double throughputPct;
	private double jpkbPct;
	private double promotionRatioPct;

	private double kbps;
	private double jpkb;
	private double promo;
	
	private double sessionTermPct;
	private double tightlyCoupledTCPPct;
	private double longBurstPct;
	private double nonPeriodicBurstPct;

	public BenchMarkingGenerator(){
		
	}
	
	public void refresh(TraceData.Analysis analysis){
		
		this.throughputPct = analysis != null ? analysis.calculateThroughputPercentage(analysis) : 0;
		this.jpkbPct = analysis != null ? analysis.calculateJpkbPercentage(analysis) : 0;
		this.promotionRatioPct = analysis != null ? analysis.calculatePromotionRatioPercentage(analysis) : 0;

		this.kbps = analysis != null ? analysis.getAvgKbps() : 0;
		this.jpkb = analysis != null ? analysis.getRrcStateMachine().getJoulesPerKilobyte() : 0;
		this.promo = analysis != null ? analysis.getRrcStateMachine().getPromotionRatio() : 0;
		
		this.sessionTermPct = analysis!= null ? analysis.calculateSessionTermPercentage(analysis) : 0;
		this.tightlyCoupledTCPPct = analysis!= null ? analysis.calculateTightlyCoupledConnection(analysis) : 0;
		this.longBurstPct = analysis!= null ? analysis.calculateLargeBurstConnection(analysis) : 0;
		this.nonPeriodicBurstPct = analysis!= null ? analysis.calculateNonPeriodicConnection(analysis) : 0;
	}
	
	public TraceBenchmarking[] getBenchMarkingDetails(){
		List<TraceBenchmarking> benchMarkList = new ArrayList<TraceBenchmarking>();
		
		TraceBenchmarking avgRate = new TraceBenchmarking();
		avgRate.setName("Avg Rate");
		avgRate.setValue(kbps);
		avgRate.setPercentile(throughputPct);
		avgRate.setType("kbps");
		benchMarkList.add(avgRate);
		
		TraceBenchmarking energyEfficiency = new TraceBenchmarking();
		energyEfficiency.setName("Energy Efficiency");
		energyEfficiency.setValue(jpkb);
		energyEfficiency.setPercentile(jpkbPct);
		energyEfficiency.setType("j/kb");
		benchMarkList.add(energyEfficiency);
		
		TraceBenchmarking signalingOverhead = new TraceBenchmarking();
		signalingOverhead.setName("Signaling Overhead");
		signalingOverhead.setValue(promo);
		signalingOverhead.setPercentile(promotionRatioPct);
		benchMarkList.add(signalingOverhead);
		
		return benchMarkList.toArray(new TraceBenchmarking[benchMarkList.size()]);
		
	}
	
	public ConnectionStatistics[] getConnectionStats(){
		List<ConnectionStatistics> connectionStstList = new ArrayList<ConnectionStatistics>();
		
		ConnectionStatistics sessionTerminator = new ConnectionStatistics();
		sessionTerminator.setName("Session Termination");
		sessionTerminator.setPercentage(sessionTermPct);
		connectionStstList.add(sessionTerminator);
		
		ConnectionStatistics groupedConnection = new ConnectionStatistics();
		groupedConnection.setName("Tightly grouped connection");
		groupedConnection.setPercentage(tightlyCoupledTCPPct);
		connectionStstList.add(groupedConnection);
		
		ConnectionStatistics nonPerBust = new ConnectionStatistics();
		nonPerBust.setName("Non-Periodic Burst connection");
		nonPerBust.setPercentage(nonPeriodicBurstPct);
		connectionStstList.add(nonPerBust);
		
		ConnectionStatistics largeBurst = new ConnectionStatistics();
		largeBurst.setName("Periodic Burst connection");
		largeBurst.setPercentage(longBurstPct);
		connectionStstList.add(largeBurst);
		
		return connectionStstList.toArray(new ConnectionStatistics[connectionStstList.size()]);
		
	}
}
