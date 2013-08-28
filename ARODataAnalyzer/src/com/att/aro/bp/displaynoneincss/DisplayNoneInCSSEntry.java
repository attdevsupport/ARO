package com.att.aro.bp.displaynoneincss;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

/* A list of this class represents files containing css command display:none*/
public class DisplayNoneInCSSEntry {
	
	private static final Logger logger = Logger.getLogger(DisplayNoneInCSSEntry.class.getName());
	private double timeStamp;
	private int contentLength;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;
	
	public DisplayNoneInCSSEntry(HttpRequestResponseInfo rr) {
		
		this.timeStamp = rr.getTimeStamp();
		this.contentLength = rr.getContentLength();
		this.httpRequestResponse = rr;

		HttpRequestResponseInfo rsp = rr.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			this.hostName = rsp.getHostName();
		} else {
			this.httpObjectName = "";
			this.hostName = "";
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
	 * Returns host name.
	 * 
	 * @return host name
	 */
	public Object getHostName() {
		return hostName;
	}

	/**
	 * Returns size of the file.
	 * 
	 * @return file size
	 */
	public Object getSize() {
		return contentLength;
	}

	/**
	 * Returns the requested HTTP object name.
	 * 
	 * @return The HTTP object name
	 */
	public Object getHttpObjectName() {
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
