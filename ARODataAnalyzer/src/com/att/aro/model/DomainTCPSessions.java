/*
 *  Copyright 2012 AT&T
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the information for all of the Domain TCP Sessions. Provides a method for extracting  Domain TCP Sessions information from a collection of TCPSession information. 
 */
public class DomainTCPSessions implements Serializable {
	private static final long serialVersionUID = 1L;

	private String domainName;
	private Collection<TCPSession> sessions;
	private double avgSessionLength;
	private double numFiles;

	/**
	 * Returns a collection of DomainTCPSessions objects from the collection TCPSession objects found in the trace.
	 * 
	 * @param sessions The collection TCPSession objects found in the trace. 
	 * @return The collection of domain tcp sessions.
	 */
	public static Collection<DomainTCPSessions> extractDomainTCPSessions(
			Collection<TCPSession> sessions) {
		if (sessions == null || sessions.size() <= 0) {
			return Collections.emptyList();
		}

		// Aggregate TCP sessions by domain
		Map<String, ArrayList<TCPSession>> distinctMap = new HashMap<String, ArrayList<TCPSession>>();
		for (TCPSession tcpSession : sessions) {
			if (null != tcpSession) {
				String domainName = tcpSession.getDomainName();

				ArrayList<TCPSession> tempList = distinctMap.get(domainName);
				if (tempList == null) {
					tempList = new ArrayList<TCPSession>();
					distinctMap.put(domainName, tempList);
				}
				tempList.add(tcpSession);
			}
		}

		// Formulate result list
		List<DomainTCPSessions> result = new ArrayList<DomainTCPSessions>(
				distinctMap.size());
		for (Map.Entry<String, ArrayList<TCPSession>> entry : distinctMap
				.entrySet()) {
			ArrayList<TCPSession> tempList = entry.getValue();
			tempList.trimToSize();
			result.add(new DomainTCPSessions(entry.getKey(), tempList));
		}
		return result;
	}

	/**
	 * A factory method that returns a Collection of DomainTCPSessions objects extracted 
	 * from a collection of TCPSession objects.
	 * 
	 * @param sessions A Collection of TCPSession objects from which to extract the data.
	 * 
	 * @return A Collection of DomainTCPSessions objects.
	 */
	private DomainTCPSessions(String domainName, Collection<TCPSession> sessions) {
		this.domainName = domainName;
		this.sessions = Collections.unmodifiableCollection(sessions);
		double sessionLength = 0;
		for (TCPSession session : sessions) {
			sessionLength += session.getSessionEndTime()
					- session.getSessionStartTime();
			numFiles += session.getFileDownloadCount();
		}
		int size = sessions.size();
		this.avgSessionLength = (double) sessionLength / size;
	}

	/**
	 * Returns the name of the TCP domain. 
	 * 
	 * @return The TCP domain name.
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Returns the collection of TCP sessions. 
	 * 
	 * @return A Collection of TCPSession objects.
	 */
	public Collection<TCPSession> getSessions() {
		return sessions;
	}

	/**
	 * Returns the average length of a TCP session in the collection. 
	 * 
	 * @return A double that is the average session length.
	 */
	public double getAvgSessionLength() {
		return avgSessionLength;
	}

	/**
	 * Returns the number of files in the collection. 
	 * 
	 * @return The number of files.
	 */
	public double getNumFiles() {
		return numFiles;
	}

}
