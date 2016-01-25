package com.att.aro.bp.httprspcd;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

public class HttpCode3XXEntry {
	private static final Logger LOGGER = Logger.getLogger(HttpCode3XXEntry.class.getName());

	private double timeStamp;
	private String httpObjectName;
	private String hostName;
	private int httpCode;
	private HttpRequestResponseInfo httpRequestResponse;

	/**
	 * Creates an instance of the the HttpCode3XXEntry test.
	 * 
	 * @param htmlImage
	 *            HTML image
	 * @param rr
	 *            HTTP object
	 */
	public HttpCode3XXEntry(HttpRequestResponseInfo rr, HttpRequestResponseInfo lastRequestObj) {

		this.timeStamp = rr.getTimeStamp();
		this.httpCode = rr.getStatusCode();
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

		LOGGER.log(Level.FINE, "Host: {0}, Domain: {1}", new Object[] { rr.getHostName(), rr.getSession().getDomainName() });
	}

	/**
	 * Returns time stamp.
	 * 
	 * @return time stamp
	 */
	public Object getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Returns host name.
	 * 
	 * @return host name
	 */
	public Object getHostName() {
		return this.hostName;
	}

	/**
	 * Returns http code.
	 * 
	 * @return  http code
	 */
	public Object getHttpCode() {
		return this.httpCode;
	}

	/**
	 * Returns the requested HTTP object name.
	 * 
	 * @return The HTTP object name
	 */
	public Object getHttpObjectName() {
		return this.httpObjectName;
	}

	/**
	 * Returns HTTP object being represented by this class.
	 * 
	 * @return the httpRequestResponse
	 */
	public HttpRequestResponseInfo getHttpRequestResponse() {
		return this.httpRequestResponse;
	}
}
