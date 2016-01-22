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
package com.att.aro.core.bestpractice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.Http3xxCodeResult;
import com.att.aro.core.bestpractice.pojo.HttpCode3xxEntry;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * best practice for Http 3xx errors
 */
public class Http3xxCodeImpl implements IBestPractice {
	@Value("${connections.http3xx.title}")
	private String overviewTitle;
	
	@Value("${connections.http3xx.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.http3xx.desc}")
	private String aboutText;
	
	@Value("${connections.http3xx.url}")
	private String learnMoreUrl;
	
	@Value("${connections.http3xx.pass}")
	private String textResultPass;
	
	@Value("${connections.http3xx.results}")
	private String textResults;
	
	@Value("${connections.http3xx.errorSingular}")
	private String errorSingular;
	
	@Value("${connections.http3xx.errorPlural}")
	private String errorPlural;
	
	@Value("${exportall.csvHttpError}")
	private String exportAllHttpError;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		Http3xxCodeResult result = new Http3xxCodeResult();
		SortedMap<Integer, Integer> httpRedirectCounts3XX = new TreeMap<Integer, Integer>();
		Map<Integer, HttpRequestResponseInfo> firstResMap = new HashMap<Integer, HttpRequestResponseInfo>();
		List<HttpCode3xxEntry> httprescodelist = new ArrayList<HttpCode3xxEntry>();
		HttpRequestResponseInfo lastRequestObj = null;
		for(Session session: tracedata.getSessionlist()){
			lastRequestObj = null;
			for(HttpRequestResponseInfo req: session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.REQUEST){
					lastRequestObj = req;
				}
				if(req.getDirection() == HttpDirection.RESPONSE &&
						HttpRequestResponseInfo.HTTP_SCHEME.equals(req.getScheme()) &&
						req.getStatusCode() >= 300 &&
						req.getStatusCode() < 400){
					Integer code = req.getStatusCode();
					Integer count = httpRedirectCounts3XX.get(code);
					if(count != null){
						httpRedirectCounts3XX.put(code, count + 1);
					}else{
						httpRedirectCounts3XX.put(code, 1);
						firstResMap.put(code, req);
					}
					httprescodelist.add(new HttpCode3xxEntry(req, lastRequestObj, session.getDomainName()));
				}
			}
		}
		if(httpRedirectCounts3XX.isEmpty()){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			result.setResultText(Http3xx4xxHelper.createFailResult(httpRedirectCounts3XX, textResults, errorPlural, errorSingular));
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllHttpError(exportAllHttpError);
		result.setHttp3xxResCode(httprescodelist);
		result.setHttpRedirectCounts3XX(httpRedirectCounts3XX);
		result.setFirstResMap(firstResMap);
		return result;
	}
	
}//end class
