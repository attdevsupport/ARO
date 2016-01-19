package com.att.aro.core.packetanalysis.pojo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * helper class for TCPSession. Tracks information about a reassembled session
 * @author EDS Team
 * 
 * Refactored by: Borey Sao
 * Date: April 24, 2014
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
