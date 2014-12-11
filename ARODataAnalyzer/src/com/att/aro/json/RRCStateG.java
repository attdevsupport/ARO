package com.att.aro.json;

public class RRCStateG {
	
	private RRCStateDetails[]  idlestate;
	private RRCStateDetails[]  dchstate;
	private RRCStateDetails[]  dchTailstate;
	private RRCStateDetails[]  fachTimestate;
	private RRCStateDetails[]  fachTailstate;
	private RRCStateDetails[]  idleToDchstate;
	private RRCStateDetails[]  fachToDchstate;
	
	public RRCStateDetails[] getIdlestate() {
		return idlestate;
	}
	public void setIdlestate(RRCStateDetails[] idlestate) {
		this.idlestate = idlestate;
	}
	public RRCStateDetails[] getDchstate() {
		return dchstate;
	}
	public void setDchstate(RRCStateDetails[] dchstate) {
		this.dchstate = dchstate;
	}
	public RRCStateDetails[] getDchTailstate() {
		return dchTailstate;
	}
	public void setDchTailstate(RRCStateDetails[] dchTailstate) {
		this.dchTailstate = dchTailstate;
	}
	public RRCStateDetails[] getFachTimestate() {
		return fachTimestate;
	}
	public void setFachTimestate(RRCStateDetails[] fachTimestate) {
		this.fachTimestate = fachTimestate;
	}
	public RRCStateDetails[] getFachTailstate() {
		return fachTailstate;
	}
	public void setFachTailstate(RRCStateDetails[] fachTailstate) {
		this.fachTailstate = fachTailstate;
	}
	public RRCStateDetails[] getIdleToDchstate() {
		return idleToDchstate;
	}
	public void setIdleToDchstate(RRCStateDetails[] idleToDchstate) {
		this.idleToDchstate = idleToDchstate;
	}
	public RRCStateDetails[] getFachToDchstate() {
		return fachToDchstate;
	}
	public void setFachToDchstate(RRCStateDetails[] fachToDchstate) {
		this.fachToDchstate = fachToDchstate;
	}

	

}
