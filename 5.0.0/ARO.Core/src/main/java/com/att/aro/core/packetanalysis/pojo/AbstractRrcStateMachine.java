package com.att.aro.core.packetanalysis.pojo;

import java.util.List;

public abstract class AbstractRrcStateMachine {
	private double totalRRCEnergy;
	private double joulesPerKilobyte;
	private double traceDuration;
	private double packetsDuration;
	private List<RrcStateRange> staterangelist;
	
	
	public List<RrcStateRange> getStaterangelist() {
		return staterangelist;
	}

	public void setStaterangelist(List<RrcStateRange> staterangelist) {
		this.staterangelist = staterangelist;
	}

	public double getTotalRRCEnergy() {
		return totalRRCEnergy;
	}

	public void setTotalRRCEnergy(double totalRRCEnergy) {
		this.totalRRCEnergy = totalRRCEnergy;
	}

	public double getJoulesPerKilobyte() {
		return joulesPerKilobyte;
	}

	public void setJoulesPerKilobyte(double joulesPerKilobyte) {
		this.joulesPerKilobyte = joulesPerKilobyte;
	}

	public double getTraceDuration() {
		return traceDuration;
	}

	public void setTraceDuration(double traceDuration) {
		this.traceDuration = traceDuration;
	}

	public double getPacketsDuration() {
		return packetsDuration;
	}

	public void setPacketsDuration(double packetsDuration) {
		this.packetsDuration = packetsDuration;
	}
	
	public abstract RrcStateMachineType getType();
}
