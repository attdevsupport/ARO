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
import com.att.aro.core.bestpractice.pojo.CacheControlResult;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.Diagnosis;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for Cache Control or Content Expiration
 */
public class CacheControlImpl implements IBestPractice {
	@Value("${caching.cacheControl.title}")
	private String overviewTitle;
	
	@Value("${caching.cacheControl.detailedTitle}")
	private String detailTitle;
	
	@Value("${caching.cacheControl.desc}")
	private String aboutText;
	
	@Value("${caching.cacheControl.url}")
	private String learnMoreUrl;
	
	@Value("${caching.cacheControl.pass}")
	private String textResultPass;
	
	@Value("${caching.cacheControl.results}")
	private String textResults;
	
	@Value("${exportall.csvCacheConNExpDesc}")
	private String exportAllCacheConNExpDesc;
	
	@Value("${exportall.csvCacheCon304Desc}")
	private String exportAllCacheCon304Desc;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		CacheControlResult result = new CacheControlResult();
		if(tracedata.getCacheAnalysis() == null){
			return null;
		}
		int hitNotExpiredDup = 0;
		int hitExpired304 = 0;
		for(CacheEntry entry: tracedata.getCacheAnalysis().getDiagnosisResults()){
			if(entry.getDiagnosis() == Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP ||
					entry.getDiagnosis() == Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT){
				hitNotExpiredDup++;
			}else if(entry.getDiagnosis() == Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304){
				hitExpired304++;
			}
		}
		boolean cacheControl = (hitNotExpiredDup > hitExpired304 ? false : true);
		if(cacheControl){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.WARNING);// ref. old analyzer give warning in this best practice
			String text = MessageFormat.format(textResults, hitNotExpiredDup,hitExpired304);
			result.setResultText(text);
		}
		result.setHitExpired304Count(hitExpired304);
		result.setHitNotExpiredDupCount(hitNotExpiredDup);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllCacheCon304Desc(exportAllCacheCon304Desc);
		result.setExportAllCacheConNExpDesc(exportAllCacheConNExpDesc);
		return result;
	}

}
