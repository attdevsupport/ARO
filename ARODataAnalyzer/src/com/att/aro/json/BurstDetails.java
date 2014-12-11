/**
 * 
 */
package com.att.aro.json;

/**
 * @author hy0910
 *
 */
public class BurstDetails {
	
	private String burst;
	private double startTime;
	private double timeElasped;
	private long bytes;
	private int packetCount;
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
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the timeElasped
	 */
	public double getTimeElasped() {
		return timeElasped;
	}
	/**
	 * @param timeElasped the timeElasped to set
	 */
	public void setTimeElasped(double timeElasped) {
		this.timeElasped = timeElasped;
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
	 * @return the packetCount
	 */
	public int getPacketCount() {
		return packetCount;
	}
	/**
	 * @param packetCount the packetCount to set
	 */
	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}
	
	

}
