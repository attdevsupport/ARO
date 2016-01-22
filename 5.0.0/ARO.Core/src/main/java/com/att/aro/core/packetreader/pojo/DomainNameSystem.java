
/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.packetreader.pojo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

/**
 * Used to interpret DNS information from a packet containing DNS data
 */

public class DomainNameSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private IPPacket packet;
	private boolean response;
	private String domainName;
	private String cname;
	private Set<InetAddress> ipAddresses;
	
	public DomainNameSystem(){}

	public String getCname(){
		return this.cname;
	}
	public void setCname(String cname){
		this.cname = cname;
	}
	/**
	 * @return the packet
	 */
	public IPPacket getPacket() {
		return packet;
	}
	public void setPacket(IPPacket packet){
		this.packet = packet;
	}

	/**
	 * @return the response
	 */
	public boolean isResponse() {
		return response;
	}
	public void setResponse(boolean value){
		this.response = value;
	}

	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String name){
		this.domainName = name;
	}

	/**
	 * @return the ipAddresses
	 */
	public Set<InetAddress> getIpAddresses() {
		if (ipAddresses != null) {
			return Collections.unmodifiableSet(ipAddresses);
		} else {
			return Collections.emptySet();
		}
	}
	public void setIpAddresses(Set<InetAddress> ipAddresses){
		this.ipAddresses = ipAddresses;
	}


}
