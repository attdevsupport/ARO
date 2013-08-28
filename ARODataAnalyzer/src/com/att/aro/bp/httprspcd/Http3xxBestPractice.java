/*
 *  Copyright 2013 AT&T
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
package com.att.aro.bp.httprspcd;

import java.util.Map;

import com.att.aro.model.BestPractices;
import com.att.aro.model.HttpRequestResponseInfo;

/**
 * Implementation of HTTP 3xx error codes best practice
 * 
 * @author ns5254
 *
 */
public class Http3xxBestPractice extends HttpRspCdBestPractice {
	private final static String RB_KEY = "http3xx";
	
	/**
	 * Create new HTTP 3xx best practice
	 */
	public Http3xxBestPractice() {
		super(RB_KEY);
	}
	
	@Override
	protected Map<Integer, Integer> getHttpRspCdCountMap(BestPractices bp) {
		if (bp != null) {
			return bp.getHttpRedirectCounts();
		} else {
			return null;
		}
	}

	@Override
	protected Map<Integer, HttpRequestResponseInfo> getFirstRespMap(BestPractices bp) {
		if (bp != null) {
			return bp.getFirstRedirectRespMap();
		} else {
			return null;
		}
	}
}
