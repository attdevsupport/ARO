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
package com.att.aro.core.packetanalysis.pojo;

/**
 * helper class for keeping track of what HttpRequestResponseInfo is part of a Session
 * @author Borey Sao
 * Date: November 19, 2014
 */
public class HttpRequestResponseInfoWithSession implements Comparable<HttpRequestResponseInfoWithSession> {
	HttpRequestResponseInfo info;
	Session session;
	public HttpRequestResponseInfo getInfo() {
		return info;
	}
	public void setInfo(HttpRequestResponseInfo info) {
		this.info = info;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	@Override
	public int compareTo(HttpRequestResponseInfoWithSession target) {
		return info.compareTo(target.getInfo());
	}
	
}
