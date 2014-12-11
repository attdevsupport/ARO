package com.att.aro.json;

public class RRCStateWifi {

	private RRCStateDetails[]  wifiActive;
	private RRCStateDetails[]  wifiTail;
	private RRCStateDetails[]  wifiIdle;
	
	public RRCStateDetails[] getWifiActive() {
		return wifiActive;
	}
	public void setWifiActive(RRCStateDetails[] wifiActive) {
		this.wifiActive = wifiActive;
	}
	public RRCStateDetails[] getWifiTail() {
		return wifiTail;
	}
	public void setWifiTail(RRCStateDetails[] wifiTail) {
		this.wifiTail = wifiTail;
	}
	public RRCStateDetails[] getWifiIdle() {
		return wifiIdle;
	}
	public void setWifiIdle(RRCStateDetails[] wifiIdle) {
		this.wifiIdle = wifiIdle;
	}


}
