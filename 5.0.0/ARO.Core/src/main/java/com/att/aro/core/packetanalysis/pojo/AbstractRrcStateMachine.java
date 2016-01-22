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
package com.att.aro.core.packetanalysis.pojo;

import java.util.List;

public abstract class AbstractRrcStateMachine {
	private double totalRRCEnergy;
	private double joulesPerKilobyte;
	private double traceDuration;
	private double packetsDuration;
	private List<RrcStateRange> staterangelist;
	
	
	public List<RrcStateRange> getStaterangelist() {
		return staterangelist;
	}

	public void setStaterangelist(List<RrcStateRange> staterangelist) {
		this.staterangelist = staterangelist;
	}

	public double getTotalRRCEnergy() {
		return totalRRCEnergy;
	}

	public void setTotalRRCEnergy(double totalRRCEnergy) {
		this.totalRRCEnergy = totalRRCEnergy;
	}

	public double getJoulesPerKilobyte() {
		return joulesPerKilobyte;
	}

	public void setJoulesPerKilobyte(double joulesPerKilobyte) {
		this.joulesPerKilobyte = joulesPerKilobyte;
	}

	public double getTraceDuration() {
		return traceDuration;
	}

	public void setTraceDuration(double traceDuration) {
		this.traceDuration = traceDuration;
	}

	public double getPacketsDuration() {
		return packetsDuration;
	}

	public void setPacketsDuration(double packetsDuration) {
		this.packetsDuration = packetsDuration;
	}
	
	public abstract RrcStateMachineType getType();
}
