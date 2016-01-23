/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.view.statistics.model;

import java.util.List;

import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.CacheControlResult;
import com.att.aro.core.bestpractice.pojo.ConnectionClosingResult;
import com.att.aro.core.bestpractice.pojo.DuplicateContentResult;
import com.att.aro.core.bestpractice.pojo.PeriodicTransferResult;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionResult;
import com.att.aro.core.bestpractice.pojo.UsingCacheResult;
import com.att.aro.core.packetanalysis.pojo.AbstractRrcStateMachine;
import com.att.aro.core.packetanalysis.pojo.BurstAnalysisInfo;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachine3G;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.exception.AROUIPanelException;
import com.att.aro.ui.view.menu.ApplicationSampling;


/**
 * This is hopefully temporary.  It exists because discovering the attributes in the new data
 * model was unsuccessful and some sort of data bucket was needed for rendering.  In other
 * words, the attributes defined here probably belong in the main data model as  derived data.
 */
public class ApplicationScoreDerived {
	private int connectionClosingScore = 0;
	private int cacheHeaderControlScore = 0;
	private int tightlyGroupedConnectionScore = 0;
	private int periodicTransferScore = 0;
	private int connectionExpirationScore = 0;
	private int duplicateContentScore = 0;
	private int signalingOverheadScore = 0;
	private int averageRateScore = 0;
	private int energyEfficiencyScore = 0;


	/**
	 * Score detection
	 */
	public enum Severity {
		SEVERITY_1, SEVERITY_2, SEVERITY_3, SEVERITY_MAXIMUM;
	}

	public ApplicationScoreDerived(AROTraceData model,
			ApplicationSampling applicationSampling) {
		processDerivedAttributes(model, applicationSampling);
	}


	public int getConnectionClosingScore() {
		return connectionClosingScore;
	}
	public int getCacheHeaderControlScore() {
		return cacheHeaderControlScore;
	}
	public int getTightlyGroupedConnectionScore() {
		return tightlyGroupedConnectionScore;
	}
	public int getPeriodicTransferScore() {
		return periodicTransferScore;
	}
	public int getConnectionExpirationScore() {
		return connectionExpirationScore;
	}
	public int getCausesScore() {
		return cacheHeaderControlScore + connectionClosingScore
			+ tightlyGroupedConnectionScore + connectionExpirationScore + periodicTransferScore;
	}
	public int getDuplicateContentScore() {
		return duplicateContentScore;
	}
	public int getSignalingOverheadScore() {
		return signalingOverheadScore;
	}
	public int getAverageRateScore() {
		return averageRateScore;
	}
	public int getEffectScore() {
		return duplicateContentScore + signalingOverheadScore + averageRateScore
				+ energyEfficiencyScore;
	}
	public int getEnergyEfficiencyScore() {
		return energyEfficiencyScore;
	}
	public int getTotalApplicationScore() {
		return getCausesScore() + getEffectScore();
	}

	private void processDerivedAttributes(AROTraceData model,
			ApplicationSampling applicationSampling) {
		int topScore = 100;

		PacketAnalyzerResult analyzerResults = model.getAnalyzerResult();
		List<AbstractBestPracticeResult> bpResults = model.getBestPracticeResults();
		List<BurstAnalysisInfo> burstInfo =
				analyzerResults.getBurstcollectionAnalysisData().getBurstAnalysisInfo();
		AbstractRrcStateMachine rrcStateMachine = analyzerResults.getStatemachine();
		Statistic statistics = analyzerResults.getStatistic();
		int burstInfoSize = burstInfo.size();

		// TODO:  CONNECTION_CLOSING and USING_CACHE enum types are reversed in model.  FIX!
		for (AbstractBestPracticeResult result : bpResults) {
			if (result instanceof UsingCacheResult) {
				UsingCacheResult bpuscaResult = (UsingCacheResult) result;
				bpuscaResult.getCacheHeaderRatio();
				cacheHeaderControlScore = (int) ((isUsingCache(bpuscaResult) ? topScore :
					getIndividualScore(bpuscaResult.getCacheHeaderRatio(), 10, 25, 65)) *
						.75);
			}
			else if (result instanceof ConnectionClosingResult) {
				ConnectionClosingResult bpcoclResult = (ConnectionClosingResult) result;
				connectionClosingScore = (int) ((bpcoclResult.isConClosingProb() ? topScore :
					getIndividualScore(bpcoclResult.getTcpControlEnergyRatio() * 100, 5, 20,
						50)) * .75);
			}
			else if (result instanceof UnnecessaryConnectionResult) {
				UnnecessaryConnectionResult bpunco = (UnnecessaryConnectionResult) result;
				bpunco.getTightlyCoupledBurstCount();
				int tightlyGroupedBursts = (int) (burstInfoSize > 0 ? 100.0
						* bpunco.getTightlyCoupledBurstCount() / burstInfoSize : 0.0);
				tightlyGroupedConnectionScore = (int) (tightlyGroupedBursts * 1.50);
			}
			else if (result instanceof PeriodicTransferResult) {
				PeriodicTransferResult periodicTransferResult = (PeriodicTransferResult) result;
				periodicTransferScore =
					(int) ((isPeriodicCount(periodicTransferResult) ? topScore :
						getPeriodicTransferScoreCalculation(periodicTransferResult, burstInfo))
							* 1.50);
			}
			else if (result instanceof CacheControlResult) {
				CacheControlResult cacheControlResultResult = (CacheControlResult) result;
				connectionExpirationScore = (int) ((isCacheControl(cacheControlResultResult) ?
					topScore : getConnectionExpirationScoreCalculation(
						cacheControlResultResult)) * .50);
			}
			else if (result instanceof DuplicateContentResult) {
				DuplicateContentResult duplicateContentResult = (DuplicateContentResult) result;
				duplicateContentScore = (int) ((isDuplicateContent(duplicateContentResult) ?
					topScore : getDuplicateContentScoreCalculation(duplicateContentResult)) *
						1.25);
			}
		}

		double promotionRatio;
		double joulesPerKilobyte;
		switch(rrcStateMachine.getType()) {
			case LTE:
				promotionRatio = ((RrcStateMachineLTE) rrcStateMachine).getCRPromotionRatio();
				joulesPerKilobyte =
						((RrcStateMachineLTE) rrcStateMachine).getJoulesPerKilobyte();
				break;
			case Type3G:
				promotionRatio = ((RrcStateMachine3G) rrcStateMachine).getPromotionRatio();
				joulesPerKilobyte =
						((RrcStateMachine3G) rrcStateMachine).getJoulesPerKilobyte();
				break;
			case WiFi:
				promotionRatio = 0.0;
				joulesPerKilobyte = 0.0;
				break;
			default:
				throw new AROUIPanelException("Undhandled state machine type " +
						rrcStateMachine.getType().name());
		}
		signalingOverheadScore = (int) (applicationSampling.getPromoRatioPercentile(
				promotionRatio) * 1.25) ;
		averageRateScore = (int) (applicationSampling.getThroughputPercentile(
				statistics.getAverageKbps()) * .625);
		energyEfficiencyScore = (int) (ApplicationSampling.getInstance().getJpkbPercentile(
				joulesPerKilobyte) * 1.875);
	}

	private boolean isUsingCache(UsingCacheResult result) {
		return result.getCacheHeaderRatio() <= 10.0;
	}
	private boolean isPeriodicCount(PeriodicTransferResult result) {
		return (result.getMinimumPeriodicRepeatTime() == 0.0);
	}
	private boolean isCacheControl(CacheControlResult result) {
		return result.getHitNotExpiredDupCount() > result.getHitExpired304Count() ?
				false : true;
	}
	private boolean isDuplicateContent(DuplicateContentResult result) {
		return result.getDuplicateContentsize() <= 3;
	}

	/**
	 * Returns individual best practice score.
	 * 
	 * @param presentRange
	 * @param Range1
	 * @param Range2
	 * @param Range3
	 * @return individual score.
	 */
	private int getIndividualScore(double presentRange, int Range1, int Range2, int Range3) {
		int individualScore = 100;
		if (presentRange > Range3) {
			individualScore = detectScore(Severity.SEVERITY_1);
		} else if (presentRange > Range2) {
			individualScore = detectScore(Severity.SEVERITY_2);
		} else if (presentRange > Range1) {
			individualScore = detectScore(Severity.SEVERITY_3);
		}
		return individualScore;
	}

	/**
	 * Returns periodic score.
	 * 
	 * @return periodic score.
	 */
	private int getPeriodicTransferScoreCalculation(PeriodicTransferResult result,
			List<BurstAnalysisInfo> burstInfo) {
		final int differentPeriodicCount = result.getDiffPeriodicCount();
		final int periodicCount = result.getPeriodicCount();
		final double periodicEnergy = getPeriodicEnergy(burstInfo);
		int periodicScore = 100;
		if (periodicCount > 60 || periodicEnergy > 50) {
			periodicScore = detectScore(Severity.SEVERITY_1);
		} else if (periodicCount > 20 || periodicEnergy > 20) {
			periodicScore = detectScore(Severity.SEVERITY_2);
		} else if (differentPeriodicCount >= 3) {
			periodicScore = detectScore(Severity.SEVERITY_3);
		}
		return periodicScore;
	}

	/**
	 * 
	 * @return energy used by periodic bursts
	 */
	private double getPeriodicEnergy(List<BurstAnalysisInfo> burstInfo){
		for(BurstAnalysisInfo currentBurstInfo : burstInfo){
			if(currentBurstInfo.getCategory() == BurstCategory.PERIODICAL){
				return currentBurstInfo.getEnergyPct();
			}
		}
		return 0;
	}

	/**
	 * Content expiration score.
	 * 
	 * @return expiration score.
	 */
	private int getConnectionExpirationScoreCalculation(CacheControlResult result) {
		final int notExpiredcount = result.getHitNotExpiredDupCount();
		final int header304Count = result.getHitExpired304Count();
		int contentExpirationScore = 100;
		if (notExpiredcount > 25 && header304Count == 0) {
			contentExpirationScore = detectScore(Severity.SEVERITY_1);
		} else if (notExpiredcount > 3 && header304Count == 0) {
			contentExpirationScore = detectScore(Severity.SEVERITY_2);
		} else if (notExpiredcount > header304Count) {
			contentExpirationScore = detectScore(Severity.SEVERITY_3);
		}
		return contentExpirationScore;
	}

	/**
	 * Returns Duplicate content score.
	 * 
	 * @return Duplicate content score.
	 */
	private int getDuplicateContentScoreCalculation(DuplicateContentResult result) {
		final int DUPLICATE_CONTENT_DENOMINATOR = 1048576;
		final double duplicateContentRatio = result.getDuplicateContentBytesRatio() * 100.0;
		final double duplicateContentSize = ((double) result.getDuplicateContentBytes()) /
				DUPLICATE_CONTENT_DENOMINATOR;
		int duplicateContentScore = 100;
		if (duplicateContentRatio > 20 || duplicateContentSize > 2) {
			duplicateContentScore = detectScore(Severity.SEVERITY_1);
		} else if (duplicateContentRatio > 5 || duplicateContentSize > .5) {
			duplicateContentScore = detectScore(Severity.SEVERITY_2);
		} else if (duplicateContentRatio > 0 || duplicateContentSize > 0) {
			duplicateContentScore = detectScore(Severity.SEVERITY_3);
		}
		return duplicateContentScore;
	}

	/**
	 * Returns score as per severity.
	 * 
	 * @param severity
	 * @return score
	 */
	private int detectScore(Severity severity) {
		switch (severity) {
		case SEVERITY_1:
			return 50;
		case SEVERITY_2:
			return 70;
		case SEVERITY_3:
			return 90;
		case SEVERITY_MAXIMUM:
			return 0;
		}
		return 100;
	}


	@Override
	public String toString() {
		return "ApplicationScoreDerived [connectionClosingScore="
				+ connectionClosingScore + ", cacheHeaderControlScore="
				+ cacheHeaderControlScore + ", tightlyGroupedConnectionScore="
				+ tightlyGroupedConnectionScore + ", periodicTransferScore="
				+ periodicTransferScore + ", connectionExpirationScore="
				+ connectionExpirationScore + ", duplicateContentScore="
				+ duplicateContentScore + ", signalingOverheadScore="
				+ signalingOverheadScore + "]";
	}
}
