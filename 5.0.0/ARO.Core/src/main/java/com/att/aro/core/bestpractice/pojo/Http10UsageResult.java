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
package com.att.aro.core.bestpractice.pojo;

import com.att.aro.core.packetanalysis.pojo.Session;

public class Http10UsageResult extends AbstractBestPracticeResult {
	int http10HeaderCount = 0;
	Session http10Session = null;
	private String exportAllHttpHeaderDesc;
	
	public int getHttp10HeaderCount() {
		return http10HeaderCount;
	}

	public void setHttp10HeaderCount(int http10HeaderCount) {
		this.http10HeaderCount = http10HeaderCount;
	}
	
	public Session getHttp10Session() {
		return http10Session;
	}

	public void setHttp10Session(Session http10Session) {
		this.http10Session = http10Session;
	}

	public String getExportAllHttpHeaderDesc() {
		return exportAllHttpHeaderDesc;
	}

	public void setExportAllHttpHeaderDesc(String exportAllHttpHeaderDesc) {
		this.exportAllHttpHeaderDesc = exportAllHttpHeaderDesc;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.HTTP_1_0_USAGE;
	}

}
