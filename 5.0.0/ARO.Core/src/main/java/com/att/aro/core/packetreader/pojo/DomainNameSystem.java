package com.att.aro.core.packetreader.pojo;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;

/**
 * Used to interpret DNS information from a packet containing DNS data
 * 
 * @author EDS Team
 * 
 * Refactored by: Borey Sao
 * Date: April 4, 2014
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
