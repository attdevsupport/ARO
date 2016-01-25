/**
 * 
 */
package com.att.aro.json;

/**
 * @author hy0910
 *
 */
public class BurstAnalysis {
	
	private String burst;
	private long bytes;
	private double bytePercentage;
	private double energy;
	private double energyPercentage;
	private double rrcActiveTime;
	private double rrcActiveTimePercentage;
	/**
	 * @return the burst
	 */
	public String getBurst() {
		return burst;
	}
	/**
	 * @param burst the burst to set
	 */
	public void setBurst(String burst) {
		this.burst = burst;
	}
	/**
	 * @return the bytes
	 */
	public long getBytes() {
		return bytes;
	}
	/**
	 * @param bytes the bytes to set
	 */
	public void setBytes(long bytes) {
		this.bytes = bytes;
	}
	/**
	 * @return the bytePercentage
	 */
	public double getBytePercentage() {
		return bytePercentage;
	}
	/**
	 * @param bytePercentage the bytePercentage to set
	 */
	public void setBytePercentage(double bytePercentage) {
		this.bytePercentage = bytePercentage;
	}
	/**
	 * @return the energy
	 */
	public double getEnergy() {
		return energy;
	}
	/**
	 * @param energy the energy to set
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	/**
	 * @return the energyPercentage
	 */
	public double getEnergyPercentage() {
		return energyPercentage;
	}
	/**
	 * @param energyPercentage the energyPercentage to set
	 */
	public void setEnergyPercentage(double energyPercentage) {
		this.energyPercentage = energyPercentage;
	}
	/**
	 * @return the rrcActiveTime
	 */
	public double getRrcActiveTime() {
		return rrcActiveTime;
	}
	/**
	 * @param rrcActiveTime the rrcActiveTime to set
	 */
	public void setRrcActiveTime(double rrcActiveTime) {
		this.rrcActiveTime = rrcActiveTime;
	}
	/**
	 * @return the rrcActiveTimePercentage
	 */
	public double getRrcActiveTimePercentage() {
		return rrcActiveTimePercentage;
	}
	/**
	 * @param rrcActiveTimePercentage the rrcActiveTimePercentage to set
	 */
	public void setRrcActiveTimePercentage(double rrcActiveTimePercentage) {
		this.rrcActiveTimePercentage = rrcActiveTimePercentage;
	}
	
	

}
