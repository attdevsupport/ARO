/**
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
package com.att.aro.core.bestpractice.impl;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.Http10UsageResult;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * check for HTTP 1.0 usage
 */
public class Http10UsageImpl implements IBestPractice {
	@Value("${html.httpUsage.title}")
	private String overviewTitle;
	
	@Value("${html.httpUsage.detailedTitle}")
	private String detailTitle;
	
	@Value("${html.httpUsage.desc}")
	private String aboutText;
	
	@Value("${html.httpUsage.url}")
	private String learnMoreUrl;
	
	@Value("${html.httpUsage.pass}")
	private String textResultPass;
	
	@Value("${html.httpUsage.results}")
	private String textResults;
	
	@Value("${exportall.csvHTTPhdrDesc}")
	private String exportAllHttpHeaderDesc;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		Http10UsageResult result = new Http10UsageResult();
		int http10HeaderCount = 0;
		Session http10Session = null;
		for(Session session: tracedata.getSessionlist()){
			for(HttpRequestResponseInfo httpreq: session.getRequestResponseInfo()){
				if(HttpRequestResponseInfo.HTTP10.equals(httpreq.getVersion())){
					++http10HeaderCount;
					if(http10Session == null){
						http10Session = session;
					}
				}
			}
		}
		result.setHttp10Session(http10Session);
		result.setHttp10HeaderCount(http10HeaderCount);
		if(http10HeaderCount == 0){
			result.setResultType(BPResultType.PASS);
			result.setResultText(MessageFormat.format(textResultPass, http10HeaderCount));
		}else{
			result.setResultType(BPResultType.WARNING);// ref. old analyzer give warning in this best practice
			result.setResultText(MessageFormat.format(textResults, http10HeaderCount));
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllHttpHeaderDesc(exportAllHttpHeaderDesc);
		
		return result;
	}

}
