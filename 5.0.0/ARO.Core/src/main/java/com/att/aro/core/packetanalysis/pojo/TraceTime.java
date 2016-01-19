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
 * A utility class used for storing and returning trace time
 * information.
 */
public class TraceTime {
	private Double startTime;
	private Double eventTime;
	private Double duration;
	private Integer timezoneOffset;
	
	/**
	 * Returns the start time.
	 * 
	 * @return The start time.
	 */
	public Double getStartTime() {
		return startTime;
	}
	public void setStartTime(Double starttime){
		this.startTime = starttime;
	}

	/**
	 * Returns the time of an event.
	 * 
	 * @return The event Time.
	 */
	public Double getEventTime() {
		return eventTime;
	}
	public void setEventTime(Double eventtime){
		this.eventTime = eventtime;
	}

	/**
	 * Returns the duration.
	 * 
	 * @return The duration value (in seconds).
	 */
	public Double getDuration() {
		return duration;
	}
	public void setDuration(Double duration){
		this.duration = duration;
	}

	/**
	 * Returns the timezone.
	 * 
	 * @return The timezone offset value (in minutes).
	 */
	public Integer getTimezoneOffset() {
		return timezoneOffset;
	}
	public void setTimezoneOffset(Integer timezoneoffset){
		this.timezoneOffset = timezoneoffset;
	}
}
