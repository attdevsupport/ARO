/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import com.att.aro.model.TraceData.Severity;

/**
 * Contains the information and calculations about application score.
 */
public class ApplicationScore {

	private TraceData.Analysis analysisData;

	// Causes Scores
	private int cacheHeaderControlScore;
	private int connectionClosingScore;
	private int tightlyGroupConnectionScore;
	private int signlaingOverheadScore;
	private int periodicTransferScore;
	private int causesScore;

	// Effects Scores
	private int duplicateContentScore;
	private int connectionExpirationScore;
	private int averageRateScore;
	private int energyEfficiencyScore;
	private int effectScore;

	private int totalApplicationScore;

	/**
	 * Initializes an instance of the ApplicationScore class, using the
	 * specified set of trace analysis data.
	 * 
	 * @param analysisData
	 *            An Analysis object containing the set of trace analysis
	 *            data.
	 */
	public ApplicationScore(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;
		calculateAppScore();
	}

	/**
	 * Calculates the Application score.
	 * 
	 * @param analysisData
	 * @return Application score.
	 */
	private void calculateAppScore() {
		final int TOPSCORE = 100;
		BestPractices bp = analysisData.getBestPractice();

		// Causes Scores
		// Cache control score calculation
		final double cacheValue = bp.getCacheHeaderRatio();
		cacheHeaderControlScore = (int) ((bp.isUsingCache() ? TOPSCORE : getIndividualScore(cacheValue, 10,
				25, 65)) * .75);

		// Connection closing score calculation
		final double tcpControlEnergyRatio = bp.getTcpControlEnergyRatio() * 100;
		connectionClosingScore = (int) ((bp.getConnectionClosingProblem() ? TOPSCORE : getIndividualScore(
				tcpControlEnergyRatio, 5, 20, 50)) * .75);

		// Tightly grouped connection added
		int size = analysisData.getBurstInfos().size();
		int tightlyGroupedBursts = (int) (size > 0 ? 100.0
				* analysisData.getBcAnalysis().getTightlyCoupledBurstCount() / size : 0.0);
		tightlyGroupConnectionScore = (int) (tightlyGroupedBursts * 1.50);

		// Periodic connection calculation
		periodicTransferScore = (int) ((bp.getPeriodicTransfer() ? TOPSCORE
				: getPeriodicTransferScoreCalculation()) * 1.50);
		
		// Content expiration score calculation
		connectionExpirationScore = (int) ((bp.isCacheControl() ? TOPSCORE
				: getConnectionExpirationScoreCalculation()) * .50);

		// Causes Score
		causesScore = cacheHeaderControlScore + connectionClosingScore
				+ tightlyGroupConnectionScore + connectionExpirationScore + periodicTransferScore;

		// Effects Scores
		// Duplicate content calculation
		duplicateContentScore = (int) ((bp.getDuplicateContent() ? TOPSCORE
				: getDuplicateContentScoreCalculation()) * 1.25);
		
		// Signaling overhead added
		signlaingOverheadScore = (int) (ApplicationSampling.getInstance().getPromoRatioPercentile(
				analysisData.getRrcStateMachine().getPromotionRatio()) * 1.25) ;

		// Average rate added
		averageRateScore = (int) (ApplicationSampling.getInstance().getThroughputPercentile(
				analysisData.getAvgKbps()) * .625);

		// Energy consumption added
		energyEfficiencyScore = (int) (ApplicationSampling.getInstance().getJpkbPercentile(
				analysisData.getRrcStateMachine().getJoulesPerKilobyte()) * 1.875);

		// Effects score
		effectScore = duplicateContentScore + signlaingOverheadScore + averageRateScore
				+ energyEfficiencyScore;

		// Total Score
		totalApplicationScore = causesScore + effectScore;

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
		case VAMPIRE:
			return 0;
		}
		return 100;
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
	private int getPeriodicTransferScoreCalculation() {
		final int differentPeriodicCount = analysisData.getBcAnalysis().getDiffPeriodicCount();
		final int periodicCount = analysisData.getBcAnalysis().getPeriodicCount();
		final double periodicEnergy = analysisData.getBcAnalysis().getPeriodicEnergy();
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
	 * Content expiration score.
	 * 
	 * @return expiration score.
	 */
	private int getConnectionExpirationScoreCalculation() {
		final int notExpiredcount = analysisData.getBestPractice().getHitNotExpiredDupCount();
		final int header304Count = analysisData.getBestPractice().getHitExpired304Count();
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
	private int getDuplicateContentScoreCalculation() {
		final int DUPLICATE_CONTENT_DENOMINATOR = 1048576;
		final double duplicateContentRatio = analysisData.getBestPractice()
				.getDuplicateContentBytesRatio() * 100.0;
		final double duplicateContentSize = ((double) analysisData.getBestPractice()
				.getDuplicateContentBytes()) / DUPLICATE_CONTENT_DENOMINATOR;
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
	 * Returns Cache control score.
	 * 
	 * @return Cache control score.
	 */
	public int getCacheHeaderControlScore() {
		return cacheHeaderControlScore;
	}

	/**
	 * Returns Connection Closing Score.
	 * 
	 * @return Connection Closing Score.
	 */
	public int getConnectionClosingScore() {
		return connectionClosingScore;
	}

	/**
	 * Returns Tightly Group Connection Score.
	 * 
	 * @return Tightly Group Connection Score.
	 */
	public int getTightlyGroupConnectionScore() {
		return tightlyGroupConnectionScore;
	}

	/**
	 * Returns Signaling Overhead Score.
	 * 
	 * @return Signaling Overhead Score.
	 */
	public int getSignalingOverheadScore() {
		return signlaingOverheadScore;
	}

	/**
	 * Returns Connection Expiration Score.
	 * 
	 * @return Connection Expiration Score.
	 */
	public int getConnectionExpirationScore() {
		return connectionExpirationScore;
	}

	/**
	 * Returns Average Rate Score.
	 * 
	 * @return Average Rate Score.
	 */
	public int getAverageRateScore() {
		return averageRateScore;
	}

	/**
	 * Returns Energy Efficiency Score.
	 * 
	 * @return Energy Efficiency Score.
	 */
	public int getEnergyEfficiencyScore() {
		return energyEfficiencyScore;
	}

	/**
	 * Returns Periodic TransferScore.
	 * 
	 * @return Periodic TransferScore.
	 */
	public int getPeriodicTransferScore() {
		return periodicTransferScore;
	}

	/**
	 * Returns Duplicate Content Score.
	 * 
	 * @return Duplicate Content Score.
	 */
	public int getDuplicateContentScore() {
		return duplicateContentScore;
	}

	/**
	 * Returns Causes Score.
	 * 
	 * @return Causes Score.
	 */
	public int getCausesScore() {
		return causesScore;
	}

	/**
	 * Returns Effect Score.
	 * 
	 * @return Effect Score.
	 */
	public int getEffectScore() {
		return effectScore;
	}

	/**
	 * Returns Total Application Score.
	 * 
	 * @return Total Application Score.
	 */
	public int getTotalApplicationScore() {
		return totalApplicationScore;
	}

}
