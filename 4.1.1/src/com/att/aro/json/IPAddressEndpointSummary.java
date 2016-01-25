/**
 * 
 */
package com.att.aro.json;

/**
 * @author hy0910
 *
 */
public class IPAddressEndpointSummary {

	private String ipAddress;
	private int packetCount;
	private long totalBytes;
	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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
