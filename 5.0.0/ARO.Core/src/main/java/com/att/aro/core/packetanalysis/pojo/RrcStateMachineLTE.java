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

public class RrcStateMachineLTE extends AbstractRrcStateMachine {
	private double lteIdleTime;
	private double lteIdleToCRPromotionTime;
	private double lteCrTime;
	private double lteCrTailTime;
	private double lteDrxShortTime;
	private double lteDrxLongTime;
	
	private double lteIdleEnergy;
	private double lteIdleToCRPromotionEnergy;
	private double lteCrEnergy;
	private double lteCrTailEnergy;
	private double lteDrxShortEnergy;
	private double lteDrxLongEnergy;
	public double getLteIdleTime() {
		return lteIdleTime;
	}
	public void setLteIdleTime(double lteIdleTime) {
		this.lteIdleTime = lteIdleTime;
	}
	public double getLteIdleToCRPromotionTime() {
		return lteIdleToCRPromotionTime;
	}
	public void setLteIdleToCRPromotionTime(double lteIdleToCRPromotionTime) {
		this.lteIdleToCRPromotionTime = lteIdleToCRPromotionTime;
	}
	public double getLteCrTime() {
		return lteCrTime;
	}
	public void setLteCrTime(double lteCrTime) {
		this.lteCrTime = lteCrTime;
	}
	public double getLteCrTailTime() {
		return lteCrTailTime;
	}
	public void setLteCrTailTime(double lteCrTailTime) {
		this.lteCrTailTime = lteCrTailTime;
	}
	public double getLteDrxShortTime() {
		return lteDrxShortTime;
	}
	public void setLteDrxShortTime(double lteDrxShortTime) {
		this.lteDrxShortTime = lteDrxShortTime;
	}
	public double getLteDrxLongTime() {
		return lteDrxLongTime;
	}
	public void setLteDrxLongTime(double lteDrxLongTime) {
		this.lteDrxLongTime = lteDrxLongTime;
	}
	public double getLteIdleEnergy() {
		return lteIdleEnergy;
	}
	public void setLteIdleEnergy(double lteIdleEnergy) {
		this.lteIdleEnergy = lteIdleEnergy;
	}
	public double getLteIdleToCRPromotionEnergy() {
		return lteIdleToCRPromotionEnergy;
	}
	public void setLteIdleToCRPromotionEnergy(double lteIdleToCRPromotionEnergy) {
		this.lteIdleToCRPromotionEnergy = lteIdleToCRPromotionEnergy;
	}
	public double getLteCrEnergy() {
		return lteCrEnergy;
	}
	public void setLteCrEnergy(double lteCrEnergy) {
		this.lteCrEnergy = lteCrEnergy;
	}
	public double getLteCrTailEnergy() {
		return lteCrTailEnergy;
	}
	public void setLteCrTailEnergy(double lteCrTailEnergy) {
		this.lteCrTailEnergy = lteCrTailEnergy;
	}
	public double getLteDrxShortEnergy() {
		return lteDrxShortEnergy;
	}
	public void setLteDrxShortEnergy(double lteDrxShortEnergy) {
		this.lteDrxShortEnergy = lteDrxShortEnergy;
	}
	public double getLteDrxLongEnergy() {
		return lteDrxLongEnergy;
	}
	public void setLteDrxLongEnergy(double lteDrxLongEnergy) {
		this.lteDrxLongEnergy = lteDrxLongEnergy;
	}
	
	/**
	 * Returns the ratio of total LTE IDLE state time to the trace duration.
	 * 
	 * @return The LTE IDLE time ratio value.
	 */
	public double getLteIdleTimeRatio() {
		return getTraceDuration() != 0.0 ? lteIdleTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total LTE IDLE to LTE CR promotion time, to the trace duration.
	 * 
	 * @return The LTE IDLE to LTE CR promotion time ratio value.
	 */
	public double getLteIdleToCRPromotionTimeRatio() {
		return getTraceDuration() != 0.0 ? lteIdleToCRPromotionTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the CR promotion time ratio corresponding to trace duration.
	 * 
	 * @return the Promotion Ratio/Signaling overhead
	 */
	public double getCRPromotionRatio() {
		return getPacketsDuration() != 0.0 ? lteIdleToCRPromotionTime / getPacketsDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total CR state time to the trace duration.
	 * 
	 * @return The LTE CR time ratio value.
	 */
	public double getLteCrTimeRatio() {
		return getTraceDuration() != 0.0 ? lteCrTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total CR tail state time to the trace duration.
	 * 
	 * @return The LTE CR tail time ratio value.
	 */
	public double getLteCrTailTimeRatio() {
		return getTraceDuration() != 0.0 ? lteCrTailTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the total CR tail time ratio corresponding to trace duration.
	 * 
	 * @return the DCH Tail Ratio
	 */
	public double getCRTailRatio() {
		return lteCrTime != 0.0 ? lteCrTailTime / lteCrTime : 0.0;
	}
	/**
	 * Returns the ratio of total LTE DRX Short state time to the total duration.
	 * 
	 * @return The LTE DRX Short time value.
	 */
	public double getLteDrxShortTimeRatio() {
		return getTraceDuration() != 0.0 ? lteDrxShortTime / getTraceDuration() : 0.0;
	}
	
	/**
	 * Returns the LTE DRX Short period time Ratio.
	 * 
	 * @return lteDrxShortTimeRatio
	 */
	public double getLteDrxShortRatio() {
		return getPacketsDuration() != 0.0 ? lteDrxShortTime / getPacketsDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total LTE DRX Long state time to the trace duration.
	 * 
	 * @return The LTE DRX Long time ratio value.
	 */
	public double getLteDrxLongTimeRatio() {
		return getTraceDuration() != 0.0 ? lteDrxLongTime / getTraceDuration() : 0.0;
	}
	
	/**
	 * Returns the LTE DRX long period time Ratio.
	 * 
	 * @return lteDrxLongTimeRatio
	 */
	public double getLteDrxLongRatio() {
		return getPacketsDuration() != 0.0 ? lteDrxLongTime / getPacketsDuration() : 0.0;
	}
	@Override
	public RrcStateMachineType getType() {
		return RrcStateMachineType.LTE;
	}
}
