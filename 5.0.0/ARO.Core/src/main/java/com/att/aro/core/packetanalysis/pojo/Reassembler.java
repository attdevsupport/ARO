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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * helper class for TCPSession. Tracks information about a reassembled session
 */
public class Reassembler {
	private Long baseSeq;
	private long seq = -1;
	private List<PacketInfo> ooid = new ArrayList<PacketInfo>();
	private ByteArrayOutputStream storage = new ByteArrayOutputStream();
	private SortedMap<Integer, PacketInfo> packetOffsets = new TreeMap<Integer, PacketInfo>();
	private List<PacketRangeInStorage> pktRanges =  new ArrayList<PacketRangeInStorage>();
	
	public void clear() {
		baseSeq = null;
		seq = -1;
		ooid.clear();
		storage.reset();
		packetOffsets = new TreeMap<Integer, PacketInfo>();
		pktRanges = new ArrayList<PacketRangeInStorage>();
	}

	
	/**
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		storage.close();
		super.finalize();
	}

	public Long getBaseSeq() {
		return baseSeq;
	}

	public void setBaseSeq(Long baseSeq) {
		this.baseSeq = baseSeq;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public List<PacketInfo> getOoid() {
		return ooid;
	}

	public void setOoid(List<PacketInfo> ooid) {
		this.ooid = ooid;
	}

	public ByteArrayOutputStream getStorage() {
		return storage;
	}

	public void setStorage(ByteArrayOutputStream storage) {
		this.storage = storage;
	}

	public SortedMap<Integer, PacketInfo> getPacketOffsets() {
		return packetOffsets;
	}

	public void setPacketOffsets(SortedMap<Integer, PacketInfo> packetOffsets) {
		this.packetOffsets = packetOffsets;
	}

	public List<PacketRangeInStorage> getPktRanges() {
		return pktRanges;
	}

	public void setPktRanges(List<PacketRangeInStorage> pktRanges) {
		this.pktRanges = pktRanges;
	}
	
}
