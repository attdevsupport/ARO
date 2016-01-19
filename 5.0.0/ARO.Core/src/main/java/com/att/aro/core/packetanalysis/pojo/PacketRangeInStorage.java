package com.att.aro.core.packetanalysis.pojo;

/**
 * helper class for TCPSession
 * @author EDS Team
 * 
 * Refactored by: Borey Sao
 * Date: April 24, 2014
 */
public class PacketRangeInStorage {
	private int offset;
	private int size;
	private int pktID;
	
	public PacketRangeInStorage(int offset, int size, int pktID) {
		this.offset = offset;
		this.size = size;
		this.pktID = pktID;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getPktID() {
		return pktID;
	}
	public void setPktID(int pktID) {
		this.pktID = pktID;
	}	
}
