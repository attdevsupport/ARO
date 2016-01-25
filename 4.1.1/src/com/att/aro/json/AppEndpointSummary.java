/**
 * 
 */
package com.att.aro.json;

/**
 * @author hy0910
 *
 */
public class AppEndpointSummary {
	
	private String applicationName;
	private int packetCount;
	private long totalBytes;
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @param applicationName the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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
	/**
	 * @return the totalBytes
	 */
	public long getTotalBytes() {
		return totalBytes;
	}
	/**
	 * @param totalBytes the totalBytes to set
	 */
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	
	

}
