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
package com.att.aro.ui.model.overview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class DomainsTCPSessions implements Comparable<DomainsTCPSessions>,Serializable{


	private static final long serialVersionUID = 1L;

	private String domainName;
	private double avgSessionLength;
	private double numOfFilesDownLoaded;
	private int numSessions;

	private Collection<Session> tcpSessions;

	/**
	 * 
	 * @param sessions
	 * @return
	 */
	public static Collection<DomainsTCPSessions> extractDomainTcpSessions(Collection<Session> sessions){
		
		if (sessions == null || sessions.size() <=0){
			return Collections.emptyList();
		}
		Map<String, ArrayList<Session>> distinctMap = new HashMap<String, ArrayList<Session>>();
		for(Session tcpSession : sessions){
			if(tcpSession != null){
				if(!tcpSession.isUDP()){
					String domainName = tcpSession.getDomainName();
					
					ArrayList<Session> tempList = distinctMap.get(domainName);
					if(tempList == null){
						tempList = new ArrayList<Session>();
						distinctMap.put(domainName, tempList);
					}
					tempList.add(tcpSession);
				}
			}
		}
		
		List<DomainsTCPSessions> result = new ArrayList<DomainsTCPSessions>();
		for(Map.Entry<String, ArrayList<Session>> entry : distinctMap.entrySet()){
			ArrayList<Session> tempList = entry.getValue();
			tempList.trimToSize();
			result.add(new DomainsTCPSessions(entry.getKey(), tempList));
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param domainName
	 * @param tcpSessions
	 */
	private DomainsTCPSessions(String domainName, Collection<Session> tcpSessions){

		this.domainName = domainName;
		this.tcpSessions = Collections.unmodifiableCollection(tcpSessions);
		this.numSessions = tcpSessions.size();

		double sessionLength = 0.0;
		for (Session aSession : tcpSessions){
			if(aSession != null){
			this.numOfFilesDownLoaded += aSession.getFileDownloadCount();
			sessionLength += aSession.getSessionEndTime() - aSession.getSessionStartTime();

			
			}
		}

		this.avgSessionLength = (double) sessionLength / tcpSessions.size();
		
	}
	
	/**
	 * @return the domainName
	 */
	public String getDomainName() {
		return domainName;
	}
	/**
	 * @param domainName the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	/**
	 * @return the avgSessionLength
	 */
	public double getAvgSessionLength() {
		return avgSessionLength;
	}
	/**
	 * @param avgSessionLength the avgSessionLength to set
	 */
	public void setAvgSessionLength(double avgSessionLength) {
		this.avgSessionLength = avgSessionLength;
	}
	/**
	 * @return the numOfFilesDownLoaded
	 */
	public double getNumOfFilesDownLoaded() {
		return numOfFilesDownLoaded;
	}
	/**
	 * @param numOfFilesDownLoaded the numOfFilesDownLoaded to set
	 */
	public void setNumOfFilesDownLoaded(double numOfFilesDownLoaded) {
		this.numOfFilesDownLoaded = numOfFilesDownLoaded;
	}
	/**
	 * @return the numSessions
	 */
	public int getNumSessions() {
		return numSessions;
	}
	/**
	 * @param numSessions the numSessions to set
	 */
	public void setNumSessions(int numSessions) {
		this.numSessions = numSessions;
	}
	
	
	/**
	 * @return the tcpSessions
	 */
	public Collection<Session> getTcpSessions() {
		return tcpSessions;
	}

	/**
	 * @param tcpSessions the tcpSessions to set
	 */
	public void setTcpSessions(Collection<Session> tcpSessions) {
		this.tcpSessions = tcpSessions;
	}

	@Override
	public int compareTo(DomainsTCPSessions obj) {
		return Double.valueOf(getAvgSessionLength()).compareTo(Double.valueOf(obj.getAvgSessionLength()));
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof DomainsTCPSessions){
			DomainsTCPSessions dTcpSessions = (DomainsTCPSessions) obj;
			return Double.valueOf(getAvgSessionLength()) == dTcpSessions.getAvgSessionLength();
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return (int)Double.doubleToLongBits(getAvgSessionLength());
	}
	
}
