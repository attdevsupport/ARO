package com.att.aro.core.packetanalysis.pojo;

public class RrcStateMachine3G extends AbstractRrcStateMachine {
	private double idleTime;
	private double idleEnergy;
	private double dchTime;
	private double dchEnergy;
	private double dchTailEnergy;
	private double fachTime;
	private double fachEnergy;
	private double fachTailTime;
	private double fachTailEnergy;
	private double idleToDch;
	private double idleToDchTime;
	private double idleToDchEnergy;
	private double fachToDch;
	private double fachToDchTime;
	private double fachToDchEnergy;
	private double dchTailTime;
	public double getIdleTime() {
		return idleTime;
	}
	public void setIdleTime(double idleTime) {
		this.idleTime = idleTime;
	}
	public double getIdleEnergy() {
		return idleEnergy;
	}
	public void setIdleEnergy(double idleEnergy) {
		this.idleEnergy = idleEnergy;
	}
	public double getDchTime() {
		return dchTime;
	}
	public void setDchTime(double dchTime) {
		this.dchTime = dchTime;
	}
	public double getDchEnergy() {
		return dchEnergy;
	}
	public void setDchEnergy(double dchEnergy) {
		this.dchEnergy = dchEnergy;
	}
	public double getDchTailEnergy() {
		return dchTailEnergy;
	}
	public void setDchTailEnergy(double dchTailEnergy) {
		this.dchTailEnergy = dchTailEnergy;
	}
	public double getFachTime() {
		return fachTime;
	}
	public void setFachTime(double fachTime) {
		this.fachTime = fachTime;
	}
	public double getFachEnergy() {
		return fachEnergy;
	}
	public void setFachEnergy(double fachEnergy) {
		this.fachEnergy = fachEnergy;
	}
	public double getFachTailTime() {
		return fachTailTime;
	}
	public void setFachTailTime(double fachTailTime) {
		this.fachTailTime = fachTailTime;
	}
	public double getFachTailEnergy() {
		return fachTailEnergy;
	}
	public void setFachTailEnergy(double fachTailEnergy) {
		this.fachTailEnergy = fachTailEnergy;
	}
	public double getIdleToDch() {
		return idleToDch;
	}
	public void setIdleToDch(double idleToDch) {
		this.idleToDch = idleToDch;
	}
	public double getIdleToDchTime() {
		return idleToDchTime;
	}
	public void setIdleToDchTime(double idleToDchTime) {
		this.idleToDchTime = idleToDchTime;
	}
	public double getIdleToDchEnergy() {
		return idleToDchEnergy;
	}
	public void setIdleToDchEnergy(double idleToDchEnergy) {
		this.idleToDchEnergy = idleToDchEnergy;
	}
	public double getFachToDch() {
		return fachToDch;
	}
	public void setFachToDch(double fachToDch) {
		this.fachToDch = fachToDch;
	}
	public double getFachToDchTime() {
		return fachToDchTime;
	}
	public void setFachToDchTime(double fachToDchTime) {
		this.fachToDchTime = fachToDchTime;
	}
	public double getFachToDchEnergy() {
		return fachToDchEnergy;
	}
	public void setFachToDchEnergy(double fachToDchEnergy) {
		this.fachToDchEnergy = fachToDchEnergy;
	}
	public double getDchTailTime() {
		return dchTailTime;
	}
	public void setDchTailTime(double dchTailTime) {
		this.dchTailTime = dchTailTime;
	}

	/**
	 * Returns the ratio of total DCH time to the total trace duration.
	 * 
	 * @return The DCH time ratio value.
	 */
	public double getDchTimeRatio() {
		return super.getTraceDuration() != 0.0 ? dchTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total FACH time to the total trace duration. 
	 * 
	 * @return The FACH time ratio value.
	 */
	public double getFachTimeRatio() {
		return getTraceDuration() != 0.0 ? fachTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total IDLE state time to the total trace duration.
	 * 
	 * @return  The IDLE time ratio value.
	 */
	public double getIdleTimeRatio() {
		return getTraceDuration() != 0.0 ? idleTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total IDLE to DCH time, to the trace duration.
	 * 
	 * @return The IDLE to DCH time ratio value.
	 */
	public double getIdleToDchTimeRatio() {
		return getTraceDuration() != 0.0 ? idleToDchTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total FACH to DCH time, to the trace duration.
	 * 
	 * @return The FACH to DCH time ratio value.
	 */
	public double getFachToDchTimeRatio() {
		return getTraceDuration() != 0.0 ? fachToDchTime / getTraceDuration() : 0.0;
	}
	/**
	 * Returns the ratio of total DCH tail time to the total trace duration.
	 * 
	 * @return The DCH tail ratio value.
	 */
	public double getDchTailRatio() {
		return dchTime != 0.0 ? dchTailTime / dchTime : 0.0;
	}
	/**
	 * Returns the ratio of total FACH tail time to the total trace duration. 
	 * 
	 * @return The FACH tail ratio value.
	 */
	public double getFachTailRatio() {
		return fachTime != 0.0 ? fachTailTime / fachTime : 0.0;
	}
	/**
	 * Returns the ratio of the total amount of promotion time to the trace duration. 
	 * 
	 * @return The total promotion ratio value.
	 */
	public double getPromotionRatio() {
		return getPacketsDuration() != 0.0 ? (idleToDchTime + fachToDchTime) / getPacketsDuration() : 0.0;
	}
	@Override
	public RrcStateMachineType getType() {
		return RrcStateMachineType.Type3G;
	}
}
