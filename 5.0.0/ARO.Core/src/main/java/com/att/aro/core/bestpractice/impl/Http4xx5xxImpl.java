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
import com.att.aro.core.bestpractice.pojo.Http4xx5xxResult;
import com.att.aro.core.bestpractice.pojo.Http4xx5xxStatusResponseCodesEntry;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
/**
 * Best practice for Http 4xx/5xx errors
 */
public class Http4xx5xxImpl implements IBestPractice {
	@Value("${connections.http4xx5xx.title}")
	private String overviewTitle;
	
	@Value("${connections.http4xx5xx.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.http4xx5xx.desc}")
	private String aboutText;
	
	@Value("${connections.http4xx5xx.url}")
	private String learnMoreUrl;
	
	@Value("${connections.http4xx5xx.pass}")
	private String textResultPass;
	
	@Value("${connections.http4xx5xx.results}")
	private String textResults;
	
	@Value("${connections.http4xx5xx.errorSingular}")
	private String errorSingular;
	
	@Value("${connections.http4xx5xx.errorPlural}")
	private String errorPlural;
	
	@Value("${exportall.csvHttpError}")
	private String exportAllHttpError;

	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		Http4xx5xxResult result = new Http4xx5xxResult();
		Map<Integer, HttpRequestResponseInfo> firstErrorRespMap4XX = new HashMap<Integer, HttpRequestResponseInfo>();
		SortedMap<Integer, Integer> httpErrorCounts4XX = new TreeMap<Integer, Integer>();
		List<Http4xx5xxStatusResponseCodesEntry> httpResCodelist = new ArrayList<Http4xx5xxStatusResponseCodesEntry>();
		
		HttpRequestResponseInfo lastRequestObj = null;
		for(Session session: tracedata.getSessionlist()){
			
			lastRequestObj = null;
			for(HttpRequestResponseInfo req: session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.REQUEST){
					lastRequestObj = req;
				}
				if(req.getDirection() == HttpDirection.RESPONSE &&
						HttpRequestResponseInfo.HTTP_SCHEME.equals(req.getScheme()) &&
						req.getStatusCode() >= 400 &&
						req.getStatusCode() < 600){
					Integer code = req.getStatusCode();
					Integer count = httpErrorCounts4XX.get(code);
					if(count != null){
						httpErrorCounts4XX.put(code, count + 1);
					}else{
						httpErrorCounts4XX.put(code, 1);
						firstErrorRespMap4XX.put(code, req);
					}
					httpResCodelist.add(new Http4xx5xxStatusResponseCodesEntry(req, lastRequestObj, session.getDomainName()));
				}
			}
		}
		if(httpErrorCounts4XX.isEmpty()){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			result.setResultText(Http3xx4xxHelper.createFailResult(httpErrorCounts4XX, textResults, errorPlural, errorSingular));
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllHttpError(exportAllHttpError);
		result.setFirstErrorRespMap4XX(firstErrorRespMap4XX);
		result.setHttpErrorCounts4XX(httpErrorCounts4XX);
		result.setHttpResCodelist(httpResCodelist);
		return result;
	}
	

}
