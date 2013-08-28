/*
 *  Copyright 2013 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.att.aro.model;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains one entry representing the result of the Text File Compression test.
 */
public class TextFileCompressionEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(TextFileCompressionEntry.class.getName());

	private double timeStamp;
	private int contentLength;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;

	/**
	 * Creates an instance of the the Text File Compression test.
	 * 
	 * @param rr
	 *            - HTTP object
	 */
	public TextFileCompressionEntry(HttpRequestResponseInfo rr) {

		this.timeStamp = rr.getTimeStamp();
		this.contentLength = rr.getContentLength();
		this.httpRequestResponse = rr;

		HttpRequestResponseInfo rsp = rr.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			this.hostName = rsp.getHostName();
		} else {
			if(rr.getFileName() != null) {
				this.httpObjectName = rr.getFileName();
			} else {
				this.httpObjectName = "";
			}
			
			if(rr.getHostName() != null) {
				this.hostName = rr.getHostName();
			} else {
				this.hostName = "";
			}
		}

		LOGGER.log(Level.FINE, "Host: {0}, Domain: {1}", new Object[] { rr.getHostName(), rr.getSession().getDomainName() });
	}

	/**
	 * Returns time stamp.
	 * @return time stamp
	 */
	public Object getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Returns host name.
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
