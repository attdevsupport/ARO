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

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.PrefetchingResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for pre-fetching content
 */
public class PrefetchingImpl implements IBestPractice {
	@Value("${caching.prefetching.title}")
	private String overviewTitle;
	
	@Value("${caching.prefetching.detailedTitle}")
	private String detailTitle;
	
	@Value("${caching.prefetching.desc}")
	private String aboutText;
	
	@Value("${caching.prefetching.url}")
	private String learnMoreUrl;
	
	@Value("${caching.prefetching.pass}")
	private String textResultPass;
	
	@Value("${caching.prefetching.results}")
	private String textResults;
	
	@Value("${exportall.csvPrefetchDesc}")
	private String exportAllPrefetchDesc;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		PrefetchingResult result = new PrefetchingResult();
		int userInputBurstCount = 0;
		int burstCategoryCount = 0;
		for(Burst burst: tracedata.getBurstcollectionAnalysisData().getBurstCollection()){
			/*
			 * Counts number of subsequent USER bursts. Stores the highest
			 * number to be used for the best practice tab.
			 */
			if (BurstCategory.USER_INPUT == burst.getBurstCategory()) {
				burstCategoryCount++;
			} else {
				burstCategoryCount = 0;
			}
			userInputBurstCount = Math.max(userInputBurstCount,burstCategoryCount);
		}
		if(userInputBurstCount < 5){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			String text = MessageFormat.format(textResults, userInputBurstCount);
			result.setResultText(text);
		}
		result.setUserInputBurstCount(userInputBurstCount);
		result.setBurstCategoryCount(burstCategoryCount);
		
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllPrefetchDesc(exportAllPrefetchDesc);
		
		return result;
	}

}
