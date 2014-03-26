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
package com.att.aro.bp.minification;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.HttpRequestResponseInfo;

/**
 * Represents one entry of the Minification test result.
 */
public class MinificationEntry {

	private static final Logger LOGGER = Logger.getLogger(MinificationEntry.class.getName());

	private double timeStamp;
	private int fileSize;
	private String hostName;
	private String httpObjectName;
	private HttpRequestResponseInfo httpRequestResponse;
	private int savingsSize;

	/**
	 * Creates an instance of the the Minification test result.
	 * 
	 * @param rr
	 *            HTTP object
	 * @param saving 
	 */
	public MinificationEntry(HttpRequestResponseInfo rr, HttpRequestResponseInfo lastRequestObj, int saving,int savingsSize) {

		this.timeStamp = rr.getTimeStamp();
		this.fileSize = saving;
		this.httpRequestResponse = rr;
        this.savingsSize=savingsSize;
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
	 * Returns saving size.
	 * 
	 * @return saving size
	 */
	 
	public int getSavingsSize() {
		return savingsSize;
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
	 * Returns file size.
	 * 
	 * @return file size
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
