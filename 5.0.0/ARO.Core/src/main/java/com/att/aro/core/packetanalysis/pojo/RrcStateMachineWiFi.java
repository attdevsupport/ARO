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

public class RrcStateMachineWiFi extends AbstractRrcStateMachine {
	private double wifiActiveEnergy;
	private double wifiTailEnergy;
	private double wifiIdleEnergy;
    
	private double wifiActiveTime;
	private double wifiTailTime;
	private double wifiIdleTime;
	public double getWifiActiveEnergy() {
		return wifiActiveEnergy;
	}
	public void setWifiActiveEnergy(double wifiActiveEnergy) {
		this.wifiActiveEnergy = wifiActiveEnergy;
	}
	public double getWifiTailEnergy() {
		return wifiTailEnergy;
	}
	public void setWifiTailEnergy(double wifiTailEnergy) {
		this.wifiTailEnergy = wifiTailEnergy;
	}
	public double getWifiIdleEnergy() {
		return wifiIdleEnergy;
	}
	public void setWifiIdleEnergy(double wifiIdleEnergy) {
		this.wifiIdleEnergy = wifiIdleEnergy;
	}
	public double getWifiActiveTime() {
		return wifiActiveTime;
	}
	public void setWifiActiveTime(double wifiActiveTime) {
		this.wifiActiveTime = wifiActiveTime;
	}
	public double getWifiTailTime() {
		return wifiTailTime;
	}
	public void setWifiTailTime(double wifiTailTime) {
		this.wifiTailTime = wifiTailTime;
	}
	public double getWifiIdleTime() {
		return wifiIdleTime;
	}
	public void setWifiIdleTime(double wifiIdleTime) {
		this.wifiIdleTime = wifiIdleTime;
	}
	
	/**
	 * Returns the ratio of total WiFi Active state time to the total trace duration.
	 * @return The WiFi Active ratio value.
	 */
	public double getWifiActiveRatio() {
		return getTraceDuration() != 0.0 ? wifiActiveTime / getTraceDuration() : 0.0;
	}

	/**
	 * Returns the ratio of total WiFi Tail state time to the total trace duration.
	 * @return The WiFi Tail ratio value.
	 */
	public double getWifiTailRatio() {
		return getTraceDuration() != 0.0 ? wifiTailTime / getTraceDuration() : 0.0;
	}

	/**
	 * Returns the ratio of total WiFi Idle state time to the total trace duration.
	 * @return The WiFi Idle ratio value.
	 */
	public double getWifiIdleRatio() {
		return getTraceDuration() != 0.0 ? wifiIdleTime / getTraceDuration() : 0.0;
	}
	@Override
	public RrcStateMachineType getType() {
		return RrcStateMachineType.WiFi;
	}
}
