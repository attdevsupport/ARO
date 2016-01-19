package com.att.aro.core.packetanalysis.pojo;

public class PacketCounter {
	private int packetCount=0;
	private long totalBytes=0;

	public void add(PacketInfo pInfo) {
		totalBytes += pInfo.getLen();
		++packetCount;
	}

	public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
	
}
