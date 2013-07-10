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
package com.att.aro.bp.imageSize;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

/**
 * Contains one entry representing the result of the Image Size test.
 */
public class ImageSizeEntry {

	private static final Logger LOGGER = Logger.getLogger(ImageSizeEntry.class.getName());

	private double timeStamp;
	private int imageSize;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;

	/**
	 * Creates an instance of the the Text File Compression test.
	 * 
	 * @param htmlImage
	 *            HTML image
	 * @param rr
	 *            HTTP object
	 */
	public ImageSizeEntry(HttpRequestResponseInfo rr) {

		this.timeStamp = rr.getTimeStamp();
		this.imageSize = rr.getContentLength();
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
	public Object getImageSize() {
		return this.imageSize;
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
