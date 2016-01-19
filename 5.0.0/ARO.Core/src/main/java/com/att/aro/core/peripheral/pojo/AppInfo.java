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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppInfo {
	// App Info
	private List<String> appInfos = new ArrayList<String>();

	// App Version Info
	private Map<String, String> appVersionMap = new HashMap<String, String>();

	public List<String> getAppInfos() {
		return appInfos;
	}

	public void setAppInfos(List<String> appInfos) {
		this.appInfos = appInfos;
	}

	public Map<String, String> getAppVersionMap() {
		return appVersionMap;
	}

	public void setAppVersionMap(Map<String, String> appVersionMap) {
		this.appVersionMap = appVersionMap;
	}
	
	
}
