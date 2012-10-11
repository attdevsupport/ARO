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

/**
 * Used to store time information for stages of a request/response
 */
public class RequestResponseTimeline implements Serializable {
	private static final long serialVersionUID = 1L;

	private double startTime;
	private Double dnsLookupDuration;
	private Double sslNegotiationDuration;
	private Double initialConnDuration;
	private double requestDuration;
	private double timeToFirstByte;
	private double contentDownloadDuration;

	/**
	 * Initializing constructor
	 * 
	 * @param startTime
	 *            Trace time when request/response started
	 * @param dnsLookupDuration
	 *            duration of DNS lookup or null if no DNS lookup
	 * @param sslNegotiationDuration
	 *            duration of SSL negotiation or null if N/A
	 * @param initialConnDuration
	 *            duration of session initiation or null if already initiated
	 * @param requestDuration
	 *            duration of request upload
	 * @param timeToFirstByte
	 *            wait time for response
	 * @param contentDownloadDuration
	 *            duration of response download
	 */
	public RequestResponseTimeline(double startTime, Double dnsLookupDuration,
			Double sslNegotiationDuration, Double initialConnDuration, double requestDuration,
			double timeToFirstByte, double contentDownloadDuration) {
		this.startTime = startTime;
		if (dnsLookupDuration != null) {
			this.dnsLookupDuration = dnsLookupDuration;
		} else {
			this.dnsLookupDuration = 0.0;
		}
		if (sslNegotiationDuration != null) {
			this.sslNegotiationDuration = sslNegotiationDuration;
		} else {
			this.sslNegotiationDuration = 0.0;
		}
		if (initialConnDuration != null) {
			this.initialConnDuration = initialConnDuration;
		} else {
			this.initialConnDuration = 0.0;
		}
		this.requestDuration = requestDuration;
		this.timeToFirstByte = timeToFirstByte;
		this.contentDownloadDuration = contentDownloadDuration;
	}

	/**
	 * @return the startTime
	 */
	public double getStartTime() {
		return startTime;
	}

	/**
	 * @return the dnsLookupDuration
	 */
	public Double getDnsLookupDuration() {
		return dnsLookupDuration;
	}

	/**
	 * @return the sslNegotiationDuration
	 */
	public Double getSslNegotiationDuration() {
		return sslNegotiationDuration;
	}

	/**
	 * @return the initialConnDuration
	 */
	public Double getInitialConnDuration() {
		return initialConnDuration;
	}

	/**
	 * @return the requestDuration
	 */
	public double getRequestDuration() {
		return requestDuration;
	}

	/**
	 * @return the timeToFirstByte
	 */
	public double getTimeToFirstByte() {
		return timeToFirstByte;
	}

	/**
	 * @return the contentDownloadDuration
	 */
	public double getContentDownloadDuration() {
		return contentDownloadDuration;
	}

}
