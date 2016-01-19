/*
 *  Copyright 2014 AT&T
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

import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

public class HttpEntry {
	private double timeStamp;
	private String httpObjectName = "";
	private String hostName = "";
	private int httpCode;
	private HttpRequestResponseInfo httpRequestResponse;
	
	public HttpEntry(HttpRequestResponseInfo hrri, HttpRequestResponseInfo lastRequestObj, String domainName) {
		this.timeStamp = hrri.getTimeStamp();
		this.httpCode = hrri.getStatusCode();
		this.httpRequestResponse = hrri;

		HttpRequestResponseInfo rsp = hrri.getAssocReqResp();
		if (rsp != null) {
			this.httpObjectName = rsp.getObjName();
			if(rsp.getHostName() == null || rsp.getHostName().isEmpty()) {
				if(hrri.getHostName() == null || hrri.getHostName().isEmpty()) {
					this.hostName = domainName;
				} else {
					this.hostName = hrri.getHostName();
				}
			} else {
				this.hostName = rsp.getHostName();
			}
		} else {
			if(hrri.getObjName() != null) {
				this.httpObjectName = hrri.getObjName();
			} else {
				/*We should consider ObjectName of lastReqResp in case getAssocReqResp() is null because 
				they are in sequence and belong to same TCP session.*/
				if(lastRequestObj != null && lastRequestObj.getDirection() == HttpDirection.REQUEST) {
					this.httpObjectName = lastRequestObj.getObjName();
				} else {
					this.httpObjectName = "";
				}
			}
			
			if(hrri.getHostName() == null || hrri.getHostName().isEmpty()) {
				this.hostName = domainName;
			} else {
				this.hostName = hrri.getHostName();
			}
		}
		
		if ((this.httpObjectName==null) || (this.httpObjectName.isEmpty())) {
			this.httpObjectName = "/";
		}
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
}//end class
