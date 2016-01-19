package com.att.aro.core.packetanalysis.pojo;

/**
 * helper class for CacheAnalysis and implementation
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 18, 2014
 *
 */
public class Range implements Comparable<Range> {
	private long firstByte;
	private long lastByte;

	public Range(long firstByte, long lastByte) {
		this.firstByte = firstByte;
		this.lastByte = lastByte;
	}

	@Override
	public int compareTo(Range original) {
		return Long.valueOf(firstByte).compareTo(original.firstByte);
	}

	@Override
	public int hashCode() {
		return Long.valueOf(firstByte).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Range) {
			Range range = (Range) obj;
			return firstByte == range.firstByte && lastByte == range.lastByte;
		}
		return false;
	}

	public long getFirstByte() {
		return firstByte;
	}

	public void setFirstByte(long firstByte) {
		this.firstByte = firstByte;
	}

	public long getLastByte() {
		return lastByte;
	}

	public void setLastByte(long lastByte) {
		this.lastByte = lastByte;
	}
	
}
