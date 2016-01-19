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

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

public class Http4xx5xxResult extends AbstractBestPracticeResult {
	private Map<Integer, HttpRequestResponseInfo> firstErrorRespMap4XX;
	private SortedMap<Integer, Integer> httpErrorCounts4XX;
	private String exportAllHttpError;
	private List<Http4xx5xxStatusResponseCodesEntry> httpResCodelist;
	
	public String getExportAllHttpError() {
		return exportAllHttpError;
	}
	public void setExportAllHttpError(String exportAllHttpError) {
		this.exportAllHttpError = exportAllHttpError;
	}
	public Map<Integer, HttpRequestResponseInfo> getFirstErrorRespMap4XX() {
		return firstErrorRespMap4XX;
	}
	public void setFirstErrorRespMap4XX(
			Map<Integer, HttpRequestResponseInfo> firstErrorRespMap4XX) {
		this.firstErrorRespMap4XX = firstErrorRespMap4XX;
	}
	public SortedMap<Integer, Integer> getHttpErrorCounts4XX() {
		return httpErrorCounts4XX;
	}
	public void setHttpErrorCounts4XX(
			SortedMap<Integer, Integer> httpErrorCounts4XX) {
		this.httpErrorCounts4XX = httpErrorCounts4XX;
	}
	public List<Http4xx5xxStatusResponseCodesEntry> getHttpResCodelist() {
		return httpResCodelist;
	}
	public void setHttpResCodelist(
			List<Http4xx5xxStatusResponseCodesEntry> httpResCodelist) {
		this.httpResCodelist = httpResCodelist;
	}
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.HTTP_4XX_5XX;
	}

}
