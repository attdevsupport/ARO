package com.att.aro.core.securedpacketreader.pojo;

import com.att.aro.core.packetreader.pojo.PacketDirection;

public class BidirDataChunk {
	private PacketDirection direction;
	private int nBytes;
	private int nPrevBytes;
	public BidirDataChunk(){
		this.direction = PacketDirection.UNKNOWN;
		this.nBytes = 0;
		this.nPrevBytes = 0;
	}
	public PacketDirection getDirection() {
		return direction;
	}
	public void setDirection(PacketDirection direction) {
		this.direction = direction;
	}
	public int getnBytes() {
		return nBytes;
	}
	public void setnBytes(int nBytes) {
		this.nBytes = nBytes;
	}
	public int getnPrevBytes() {
		return nPrevBytes;
	}
	public void setnPrevBytes(int nPrevBytes) {
		this.nPrevBytes = nPrevBytes;
	}
	
}
