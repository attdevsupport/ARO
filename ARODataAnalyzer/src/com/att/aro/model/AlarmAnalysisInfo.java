/*
 * Copyright 2012 AT&T
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
	private int wakeup;
	private int running;
	private int totalFired;
	
	public AlarmAnalysisInfo(String application, int running,
		       		int wakeup, int totalFired, 
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

	public int getFired() {
		if(totalFired < 0) {
			return 0;
		}
		return totalFired;
	}

	public int getRunning() {
		return running;
	}
	
	public int getWakeup() {
		return wakeup;
	}

	public List<String> getIntent() {
		return intent;
	}

	protected void setWakeup(int wakeup) {
		this.wakeup = wakeup;
	}
}