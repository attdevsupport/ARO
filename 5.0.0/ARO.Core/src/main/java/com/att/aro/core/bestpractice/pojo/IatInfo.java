package com.att.aro.core.bestpractice.pojo;

public class IatInfo {

	private double iat;
	private double beginTime;
	private int beginEvent;
	private int endEvent;

	public double getIat() {
		return iat;
	}

	public void setIat(double iat) {
		this.iat = iat;
	}

	public double getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(double beginTime) {
		this.beginTime = beginTime;
	}

	public int getBeginEvent() {
		return beginEvent;
	}

	public void setBeginEvent(int beginEvent) {
		this.beginEvent = beginEvent;
	}

	public int getEndEvent() {
		return endEvent;
	}

	public void setEndEvent(int endEvent) {
		this.endEvent = endEvent;
	}
	
	public IatInfo(double iat, double beginTime, int beginEvent, int endEvent) {
		super();
		this.iat = iat;
		this.beginTime = beginTime;
		this.beginEvent = beginEvent;
		this.endEvent = endEvent;
	}

	public IatInfo() {
	}
}
