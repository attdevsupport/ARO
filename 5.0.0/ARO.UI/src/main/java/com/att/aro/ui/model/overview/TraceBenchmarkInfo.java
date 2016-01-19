/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.model.overview;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class TraceBenchmarkInfo {
	
	//analysis.calculateThroughputPercentage()
	private double throughputPct;
	//analysis.calculateJpkbPercentage
	private double jpkbPct;
	//analysis.calculatePromotionRatioPercentage
	private double promotionRatioPct;

	//analysis.getAvgKbps()
	private double kbps;
	//analysis.getRrcStateMachine().getJoulesPerKilobyte() 
	private double jpkb;
	//analysis.getRrcStateMachine().getPromotionRatio()
	private double promoRatioPercentail;
	
	/**
	 * @return the throughputPct
	 */
	public double getThroughputPct() {
		return throughputPct;
	}
	/**
	 * @param throughputPct the throughputPct to set
	 */
	public void setThroughputPct(double throughputPct) {
		this.throughputPct = throughputPct;
	}
	/**
	 * @return the jpkbPct
	 */
	public double getJpkbPct() {
		return jpkbPct;
	}
	/**
	 * @param jpkbPct the jpkbPct to set
	 */
	public void setJpkbPct(double jpkbPct) {
		this.jpkbPct = jpkbPct;
	}
	/**
	 * @return the promotionRatioPct
	 */
	public double getPromotionRatioPct() {
		return promotionRatioPct;
	}
	/**
	 * @param promotionRatioPct the promotionRatioPct to set
	 */
	public void setPromotionRatioPct(double promotionRatioPct) {
		this.promotionRatioPct = promotionRatioPct;
	}
	/**
	 * @return the kbps
	 */
	public double getKbps() {
		return kbps;
	}
	/**
	 * @param kbps the kbps to set
	 */
	public void setKbps(double kbps) {
		this.kbps = kbps;
	}
	/**
	 * @return the jpkb
	 */
	public double getJpkb() {
		return jpkb;
	}
	/**
	 * @param jpkb the jpkb to set
	 */
	public void setJpkb(double jpkb) {
		this.jpkb = jpkb;
	}
	/**
	 * @return the promo
	 */
	public double getPromoRatioPercentail() {
		return promoRatioPercentail;
	}
	/**
	 * @param promo the promo to set
	 */
	public void setPromoRatioPercentail(double promo) {
		this.promoRatioPercentail = promo;
	}


}
