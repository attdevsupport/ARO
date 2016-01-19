/**
 * 
 */
package com.att.aro.core.bestpractice.pojo;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class UnnecessaryConnectionEntry {

	private double lowTime;
	private double highTime;
	private int burstCount;
	private double totalKB;
	
	public UnnecessaryConnectionEntry(double lTime, double hTime, int bCount, double tKB){
		this.lowTime = lTime;
		this.highTime = hTime;
		this.burstCount = bCount;
		this.totalKB = tKB;
		
	}
	
	/**
	 * @return the lowTime
	 */
	public double getLowTime() {
		return lowTime;
	}
	/**
	 * @return the highTime
	 */
	public double getHighTime() {
		return highTime;
	}
	/**
	 * @return the burstCount
	 */
	public int getBurstCount() {
		return burstCount;
	}
	/**
	 * @return the totalKB
	 */
	public double getTotalKB() {
		return totalKB;
	}

	
	
}
