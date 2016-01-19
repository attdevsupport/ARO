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
package com.att.aro.core.peripheral.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * A bean class containing the difference in dumpsys alarm statistics 
 * information before and at end of catpure.
 */
public class AlarmAnalysisInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	// Dumpsys alarm statistics
	private String application;
	private List<String> intent;
	private double wakeup;
	private double running;
	private double totalFired;
	
	public AlarmAnalysisInfo(String application, double running,
			double wakeup, double totalFired, 
				List<String> intent) {
		this.application = application;
		this.wakeup = wakeup;
		this.running = running;
		this.totalFired=totalFired;
		this.intent = intent;
	}

	public String getApplication() {
		return application;
	}

	public double getFired() {
		if(totalFired < 0) {
			return 0;
		}
		return totalFired;
	}

	public double getRunning() {
		return running;
	}
	
	public double getWakeup() {
		return wakeup;
	}

	public List<String> getIntent() {
		return intent;
	}

	protected void setWakeup(double wakeup) {
		this.wakeup = wakeup;
	}
}
