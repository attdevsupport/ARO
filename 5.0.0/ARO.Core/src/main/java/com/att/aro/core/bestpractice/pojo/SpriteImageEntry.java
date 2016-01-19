package com.att.aro.core.bestpractice.pojo;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

public class SpriteImageEntry {
	private double timeStamp;
	private int fileSize;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;
	
	public SpriteImageEntry(HttpRequestResponseInfo hrri) {
		this.timeStamp = hrri.getTimeStamp();
		this.fileSize = hrri.getContentLength();
		this.httpRequestResponse = hrri;

		HttpRequestResponseInfo rsp = hrri.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			this.hostName = rsp.getHostName();
		} else {
			this.httpObjectName = "";
			this.hostName = "";
		}
	}
	public int getFileSize() {
		return fileSize;
	}
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	public double getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(double timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHttpObjectName() {
		return httpObjectName;
	}
	public void setHttpObjectName(String httpObjectName) {
		this.httpObjectName = httpObjectName;
	}
	public HttpRequestResponseInfo getHttpRequestResponse() {
		return httpRequestResponse;
	}
	public void setHttpRequestResponse(HttpRequestResponseInfo httpRequestResponse) {
		this.httpRequestResponse = httpRequestResponse;
	}
	
}
