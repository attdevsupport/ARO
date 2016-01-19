package com.att.aro.ui.model.overview;

public class TraceInfo {
	private String dateValue ="";
	private String traceValue="";
	private Integer byteCountTotal = new Integer(0);
	private String networkType = "";
	private String profileValue = "";
	public String getDateValue() {
		return dateValue;
	}
	public void setDateValue(String dateValue) {
		this.dateValue = dateValue;
	}
	public String getTraceValue() {
		return traceValue;
	}
	public void setTraceValue(String traceValue) {
		this.traceValue = traceValue;
	}
	public Integer getByteCountTotal() {
		return byteCountTotal;
	}
	public void setByteCountTotal(Integer byteCountTotal) {
		this.byteCountTotal = byteCountTotal;
	}
	public String getNetworkType() {
		return networkType;
	}
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	public String getProfileValue() {
		return profileValue;
	}
	public void setProfileValue(String profileValue) {
		this.profileValue = profileValue;
	}

}
