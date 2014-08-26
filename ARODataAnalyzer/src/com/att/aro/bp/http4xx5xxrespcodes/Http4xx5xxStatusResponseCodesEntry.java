package com.att.aro.bp.http4xx5xxrespcodes;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

public class Http4xx5xxStatusResponseCodesEntry {
	
	private static final Logger logger = Logger.getLogger(Http4xx5xxStatusResponseCodesEntry.class.getName());
	private double timeStamp;
	private int httpCode;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;
		
	public Http4xx5xxStatusResponseCodesEntry(HttpRequestResponseInfo rr, HttpRequestResponseInfo lastRequestObj){
		
		this.timeStamp = rr.getTimeStamp();
		this.httpCode = rr.getStatusCode();
		this.hostName = rr.getSession().getDomainName();
		this.httpRequestResponse = rr;
	
		HttpRequestResponseInfo rsp = rr.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			if(rsp.getHostName() == null || rsp.getHostName().isEmpty()) {
				if(rr.getHostName() == null || rr.getHostName().isEmpty()) {
					this.hostName = rr.getSession().getDomainName();
				} else {
					this.hostName = rr.getHostName();
				}
			} else {
				this.hostName = rsp.getHostName();
			}
		} else {
			if(rr.getObjName() != null) {
				this.httpObjectName = rr.getObjName();
			} else {
				/*We should consider ObjectName of lastReqResp in case getAssocReqResp() is null because 
				they are in sequence and belong to same TCP session.*/
				if(lastRequestObj != null && lastRequestObj.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
					this.httpObjectName = lastRequestObj.getObjName();
				} else {
					this.httpObjectName = "";
				}
			}
			
			if(rr.getHostName() == null || rr.getHostName().isEmpty()) {
				this.hostName = rr.getSession().getDomainName();
			} else {
				this.hostName = rr.getHostName();
			}
		}
		
		if(this.httpObjectName.isEmpty()) {
			this.httpObjectName = "/";
		}
		
		logger.log(Level.FINE, "Host: {0}, Domain: {1}",
				new Object[] { rr.getHostName(),
						rr.getSession().getDomainName() });
	}
	
	/**
	 * Returns time stamp.
	 * 
	 * @return time stamp
	 */
	public Object getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Returns http code
	 * 
	 * @return http code
	 */
	public Object getHttpCode() {
		return httpCode;
	}

	/**
	 * Returns domain name
	 * 
	 * @return file size
	 */
	public Object getDomainName() {
		return hostName;
	}

	/**
	 * Returns the requested HTTP object name.
	 * 
	 * @return The HTTP object name
	 */
	public Object getFileName() {
		return httpObjectName;
	}

	/**
	 * Returns HTTP object being represented by this class.
	 * 
	 * @return the httpRequestResponse
	 */
	public HttpRequestResponseInfo getHttpRequestResponse() {
		return httpRequestResponse;
	}


}
