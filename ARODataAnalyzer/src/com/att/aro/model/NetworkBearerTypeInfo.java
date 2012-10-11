package com.att.aro.model;

public class NetworkBearerTypeInfo {

	private NetworkType networkType;
	private double beginTimestamp;
	private double endTimestamp;

	public NetworkBearerTypeInfo(double beginTimestamp, double endTimestamp, NetworkType networkType) {
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
		this.networkType = networkType;
	}

	/**
	 * Returns the network bearer type.
	 * 
	 * @return The bearer type.
	 */
	public NetworkType getNetworkType() {
		return networkType;
	}

	/**
	 * Sets the network bearer type.
	 * 
	 * @param networkType
	 *            The bearer type.
	 */
	public void setNetworkType(NetworkType networkType) {
		this.networkType = networkType;
	}

	/**
	 * Returns the begin time stamp for the network bearer type.
	 * 
	 * @return The begin time stamp.
	 */
	public double getBeginTimestamp() {
		return beginTimestamp;
	}

	/**
	 * Sets the begin time stamp for the network bearer type
	 * 
	 * @param beginTimestamp
	 *            The begin time stamp.
	 */
	public void setBeginTimestamp(double beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}

	/**
	 * Returns the end time stamp for the network bearer type.
	 * 
	 * @return The end time stamp.
	 */

	public double getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * Sets the end time stamp for the network bearer type.
	 * 
	 * @param endTimestamp
	 *            The end time stamp.
	 */
	public void setEndTimestamp(double endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

}
