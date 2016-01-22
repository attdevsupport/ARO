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

/**
 * helper class for CacheAnalysis and implementation
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
