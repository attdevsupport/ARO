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
public class ConnectionStatisticsInfo {
	
	//analysis.calculateSessionTermPercentage()
	private double sessionTermPct;
	//analysis.calculateTightlyCoupledConnection()
	private double tightlyCoupledTCPPct;
	//analysis.calculateLargeBurstConnection()
	private double longBurstPct;
	//analysis.calculateNonPeriodicConnection()
	private double nonPeriodicBurstPct;
	/**
	 * @return the sessionTermPct
	 */
	public double getSessionTermPct() {
		return sessionTermPct;
	}
	/**
	 * @param sessionTermPct the sessionTermPct to set
	 */
	public void setSessionTermPct(double sessionTermPct) {
		this.sessionTermPct = sessionTermPct;
	}
	/**
	 * @return the tightlyCoupledTCPPct
	 */
	public double getTightlyCoupledTCPPct() {
		return tightlyCoupledTCPPct;
	}
	/**
	 * @param tightlyCoupledTCPPct the tightlyCoupledTCPPct to set
	 */
	public void setTightlyCoupledTCPPct(double tightlyCoupledTCPPct) {
		this.tightlyCoupledTCPPct = tightlyCoupledTCPPct;
	}
	/**
	 * @return the longBurstPct
	 */
	public double getLongBurstPct() {
		return longBurstPct;
	}
	/**
	 * @param longBurstPct the longBurstPct to set
	 */
	public void setLongBurstPct(double longBurstPct) {
		this.longBurstPct = longBurstPct;
	}
	/**
	 * @return the nonPeriodicBurstPct
	 */
	public double getNonPeriodicBurstPct() {
		return nonPeriodicBurstPct;
	}
	/**
	 * @param nonPeriodicBurstPct the nonPeriodicBurstPct to set
	 */
	public void setNonPeriodicBurstPct(double nonPeriodicBurstPct) {
		this.nonPeriodicBurstPct = nonPeriodicBurstPct;
	}
	
	

}
