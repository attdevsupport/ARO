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
package com.att.aro.core.bestpractice.impl;

import java.text.MessageFormat;
import java.text.NumberFormat;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.UsingCacheResult;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.Diagnosis;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;

/**
 * best practice for using cache
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 21, 2014
 */
public class UsingCacheImpl implements IBestPractice {
	@Value("${caching.usingCache.title}")
	private String overviewTitle;
	
	@Value("${caching.usingCache.detailedTitle}")
	private String detailTitle;
	
	@Value("${caching.usingCache.desc}")
	private String aboutText;
	
	@Value("${caching.usingCache.url}")
	private String learnMoreUrl;
	
	@Value("${caching.usingCache.pass}")
	private String textResultPass;
	
	@Value("${caching.usingCache.results}")
	private String textResults;
	
	@Value("${exportall.csvCacheConPct}")
	private String exportAllCacheConPct;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		UsingCacheResult result = new UsingCacheResult();
		if(tracedata.getCacheAnalysis() == null){
			return null;
		}
		double cacheHeaderRatio = 0.0;
		boolean usingCache = false;
		int validCount = 0;
		int noCacheHeadersCount = 0;
		PacketInfo noCacheHeaderFirstPacket = null;
		
		for(CacheEntry entry: tracedata.getCacheAnalysis().getDiagnosisResults()){
			if(entry.getDiagnosis() != Diagnosis.CACHING_DIAG_REQUEST_NOT_FOUND &&
					entry.getDiagnosis() != Diagnosis.CACHING_DIAG_INVALID_OBJ_NAME &&
					entry.getDiagnosis() != Diagnosis.CACHING_DIAG_INVALID_REQUEST &&
					entry.getDiagnosis() != Diagnosis.CACHING_DIAG_INVALID_RESPONSE
					){
				++validCount;
				if (!entry.hasCacheHeaders()) {
					if (noCacheHeadersCount == 0) {
						noCacheHeaderFirstPacket = entry.getSessionFirstPacket();
					}
					++noCacheHeadersCount;
				}
			}
		}
		cacheHeaderRatio = validCount > 0 ? (100.0 * noCacheHeadersCount) / validCount : 0.0;
		usingCache = cacheHeaderRatio <= 10.0;
		if(usingCache){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.WARNING);// ref. old analyzer give warning in this best practice
			String text = MessageFormat.format(textResults, NumberFormat.getIntegerInstance().format(cacheHeaderRatio));
			result.setResultText(text);
		}
		result.setCacheHeaderRatio(cacheHeaderRatio);
		result.setNoCacheHeaderFirstPacket(noCacheHeaderFirstPacket);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllCacheConPct(exportAllCacheConPct);
		
		return result;
	}

}
