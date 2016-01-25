package com.att.aro.json;

public class RRCStateSimulator {
	private int idleToDch;
	private int fachToDch;
	private RRCStateDetails[] rccstateg;
	private RRCStateDetails[] rccstatelte;
	private RRCStateDetails[] rccstatewifi;	
	private double crPower;
	private double totalRRCEnergy;
	private double joulesPerKilobyte;

	public RRCStateDetails[] getRccstateg() {
		return rccstateg;
	}

	public void setRccstateg(RRCStateDetails[] rccstateg) {
		this.rccstateg = rccstateg;
	}

	public RRCStateDetails[] getRccstatelte() {
		return rccstatelte;
	}

	public void setRccstatelte(RRCStateDetails[] rccstatelte) {
		this.rccstatelte = rccstatelte;
	}

	public RRCStateDetails[] getRccstatewifi() {
		return rccstatewifi;
	}

	public void setRccstatewifi(RRCStateDetails[] rccstatewifi) {
		this.rccstatewifi = rccstatewifi;
	}

	public int getIdleToDch() {
		return idleToDch;
	}

	public void setIdleToDch(int idleToDch) {
		this.idleToDch = idleToDch;
	}

	public int getFachToDch() {
		return fachToDch;
	}

	public void setFachToDch(int fachToDch) {
		this.fachToDch = fachToDch;
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

	public double getCrPower() {
		return crPower;
	}

	public void setCrPower(double crPower) {
		this.crPower = crPower;
	}

}
