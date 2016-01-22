/**
 *  Copyright 2016 AT&T
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

public class IatInfo {

	private double iat;
	private double beginTime;
	private int beginEvent;
	private int endEvent;

	public double getIat() {
		return iat;
	}

	public void setIat(double iat) {
		this.iat = iat;
	}

	public double getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(double beginTime) {
		this.beginTime = beginTime;
	}

	public int getBeginEvent() {
		return beginEvent;
	}

	public void setBeginEvent(int beginEvent) {
		this.beginEvent = beginEvent;
	}

	public int getEndEvent() {
		return endEvent;
	}

	public void setEndEvent(int endEvent) {
		this.endEvent = endEvent;
	}
	
	public IatInfo(double iat, double beginTime, int beginEvent, int endEvent) {
		super();
		this.iat = iat;
		this.beginTime = beginTime;
		this.beginEvent = beginEvent;
		this.endEvent = endEvent;
	}

	public IatInfo() {
	}
}
