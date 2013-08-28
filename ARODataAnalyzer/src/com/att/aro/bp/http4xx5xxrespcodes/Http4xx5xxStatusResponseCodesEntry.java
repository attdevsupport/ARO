package com.att.aro.bp.http4xx5xxrespcodes;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

public class Http4xx5xxStatusResponseCodesEntry {
	
	private static final Logger logger = Logger.getLogger(Http4xx5xxStatusResponseCodesEntry.class.getName());
	private double timeStamp;
	private int httpCode;
	private String domainName;
	private String fileName;
	private HttpRequestResponseInfo httpRequestResponse;
		
	public Http4xx5xxStatusResponseCodesEntry(HttpRequestResponseInfo rr){
		
		this.timeStamp = rr.getTimeStamp();
		this.httpCode = rr.getStatusCode();
		this.domainName = rr.getSession().getDomainName();
		this.httpRequestResponse = rr;
	
		HttpRequestResponseInfo rsp = rr.getAssocReqResp();
		if (rsp != null) {
			this.fileName = rsp.getObjName();
		} else {
			this.fileName = "";
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
		return domainName;
	}

	/**
	 * Returns the requested HTTP object name.
	 * 
	 * @return The HTTP object name
	 */
	public Object getFileName() {
		return fileName;
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
