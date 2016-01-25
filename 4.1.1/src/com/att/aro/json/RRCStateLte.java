package com.att.aro.json;

public class RRCStateLte {	
	
	private RRCStateDetails[] lteIdle;
	private RRCStateDetails[] lteIdleToCRPromotion;
	private RRCStateDetails[] lteCr;
	private RRCStateDetails[] lteCrTail;
	private RRCStateDetails[] lteDrxShort;
	private RRCStateDetails[] lteDrxLong;
	
	public RRCStateDetails[] getLteIdle() {
		return lteIdle;
	}
	public void setLteIdle(RRCStateDetails[] lteIdle) {
		this.lteIdle = lteIdle;
	}
	public RRCStateDetails[] getLteIdleToCRPromotion() {
		return lteIdleToCRPromotion;
	}
	public void setLteIdleToCRPromotion(RRCStateDetails[] lteIdleToCRPromotion) {
		this.lteIdleToCRPromotion = lteIdleToCRPromotion;
	}
	public RRCStateDetails[] getLteCr() {
		return lteCr;
	}
	public void setLteCr(RRCStateDetails[] lteCr) {
		this.lteCr = lteCr;
	}
	public RRCStateDetails[] getLteCrTail() {
		return lteCrTail;
	}
	public void setLteCrTail(RRCStateDetails[] lteCrTail) {
		this.lteCrTail = lteCrTail;
	}
	public RRCStateDetails[] getLteDrxShort() {
		return lteDrxShort;
	}
	public void setLteDrxShort(RRCStateDetails[] lteDrxShort) {
		this.lteDrxShort = lteDrxShort;
	}
	public RRCStateDetails[] getLteDrxLong() {
		return lteDrxLong;
	}
	public void setLteDrxLong(RRCStateDetails[] lteDrxLong) {
		this.lteDrxLong = lteDrxLong;
	}



}
