/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.bestpractice.pojo;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;


/* A list of this class represents files containing css command display:none*/
public class DisplayNoneInCSSEntry {
	private double timeStamp;
	private int contentLength;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;
	public DisplayNoneInCSSEntry(HttpRequestResponseInfo rrinfo) {
		
		this.timeStamp = rrinfo.getTimeStamp();
		this.contentLength = rrinfo.getContentLength();
		this.httpRequestResponse = rrinfo;

		HttpRequestResponseInfo rsp = rrinfo.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			this.hostName = rsp.getHostName();
		} else {
			this.httpObjectName = "";
			this.hostName = "";
		}
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
