package com.att.aro.bp.spriteimage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

public class SpriteImageEntry {
	private static final Logger LOGGER = Logger.getLogger(SpriteImageEntry.class.getName());

	private double timeStamp;
	private int fileSize;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;

	/**
	 * Creates an instance of the the SpriteImageEntry test.
	 * 
	 * @param htmlImage
	 *            HTML image
	 * @param rr
	 *            HTTP object
	 */
	public SpriteImageEntry(HttpRequestResponseInfo rr) {

		this.timeStamp = rr.getTimeStamp();
		this.fileSize = rr.getContentLength();
		this.httpRequestResponse = rr;

		HttpRequestResponseInfo rsp = rr.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			this.hostName = rsp.getHostName();
		} else {
			this.httpObjectName = "";
			this.hostName = "";
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
	 * Returns size of the image.
	 * 
	 * @return image size
	 */
	public Object getFileSize() {
		return this.fileSize;
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
