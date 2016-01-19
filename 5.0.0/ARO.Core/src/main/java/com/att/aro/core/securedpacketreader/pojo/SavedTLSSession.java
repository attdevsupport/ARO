package com.att.aro.core.securedpacketreader.pojo;

public class SavedTLSSession {
	byte[] pSessionIDorTicket;
	byte[] master = new byte[48];
	
	public byte[] getpSessionIDorTicket() {
		return pSessionIDorTicket;
	}
	
	public void setpSessionIDorTicket(byte[] pSessionIDorTicket) {
		this.pSessionIDorTicket = pSessionIDorTicket;
	}
	
	public byte[] getMaster() {
		return master;
	}
	
	public void setMaster(byte[] master) {
		this.master = master;
	}	
}
