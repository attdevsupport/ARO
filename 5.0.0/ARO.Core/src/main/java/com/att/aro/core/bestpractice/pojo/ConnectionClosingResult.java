/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.bestpractice.pojo;

public class ConnectionClosingResult extends AbstractBestPracticeResult {
	private double wastedBurstEnergy = 0.0;
	private boolean conClosingProb = true;
	private double tcpControlEnergy = 0;
	private double tcpControlEnergyRatio = 0;
	private double largestEnergyTime = 0.0;
	private String exportAllConnClosingDesc;
	
	public double getWastedBurstEnergy() {
		return wastedBurstEnergy;
	}

	public void setWastedBurstEnergy(double wastedBurstEnergy) {
		this.wastedBurstEnergy = wastedBurstEnergy;
	}

	public boolean isConClosingProb() {
		return conClosingProb;
	}

	public void setConClosingProb(boolean conClosingProb) {
		this.conClosingProb = conClosingProb;
	}

	public double getTcpControlEnergy() {
		return tcpControlEnergy;
	}

	public void setTcpControlEnergy(double tcpControlEnergy) {
		this.tcpControlEnergy = tcpControlEnergy;
	}

	public double getTcpControlEnergyRatio() {
		return tcpControlEnergyRatio;
	}

	public void setTcpControlEnergyRatio(double tcpControlEnergyRatio) {
		this.tcpControlEnergyRatio = tcpControlEnergyRatio;
	}

	
	public String getExportAllConnClosingDesc() {
		return exportAllConnClosingDesc;
	}

	public void setExportAllConnClosingDesc(String exportAllConnClosingDesc) {
		this.exportAllConnClosingDesc = exportAllConnClosingDesc;
	}

	
	public double getLargestEnergyTime() {
		return largestEnergyTime;
	}

	public void setLargestEnergyTime(double largestEnergyTime) {
		this.largestEnergyTime = largestEnergyTime;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.CONNECTION_CLOSING;
	}

}
