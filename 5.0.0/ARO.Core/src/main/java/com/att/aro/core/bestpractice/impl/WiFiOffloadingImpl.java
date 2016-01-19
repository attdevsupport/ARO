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

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.WiFiOffloadingResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for wifi off-loading
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 14, 2014
 *
 */
public class WiFiOffloadingImpl implements IBestPractice {
	@Value("${connections.offloadingToWifi.title}")
	private String overviewTitle;
	
	@Value("${connections.offloadingToWifi.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.offloadingToWifi.desc}")
	private String aboutText;
	
	@Value("${connections.offloadingToWifi.url}")
	private String learnMoreUrl;
	
	@Value("${connections.offloadingToWifi.pass}")
	private String textResultPass;
	
	@Value("${connections.offloadingToWifi.results}")
	private String textResults;
	
	@Value("${exportall.csvOffWiFiDesc}")
	private String exportAllOffWifiDesc;
	 
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		WiFiOffloadingResult result = new WiFiOffloadingResult();
		
		result.setSelfTest(false);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllOffWifiDesc(exportAllOffWifiDesc);
		 
		int longBurstCount = tracedata.getBurstcollectionAnalysisData().getLongBurstCount();
		boolean offloadingToWiFi = longBurstCount <= 3;
		double time;
		double largestBurstDuration = 0.0;
		double largestBurstBeginTime = 0.0;
		
		for(Burst burst:tracedata.getBurstcollectionAnalysisData().getBurstCollection()){
			time = burst.getEndTime() - burst.getBeginTime();
			if (time > largestBurstDuration) {
				largestBurstDuration = time;
				largestBurstBeginTime = burst.getBeginTime();
			}
		}
		if(offloadingToWiFi){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			String text = MessageFormat.format(textResults,longBurstCount);
			result.setResultText(text);
		}
		result.setLongBurstCount(longBurstCount);
		result.setLargestBurstBeginTime(largestBurstBeginTime);
		result.setLargestBurstDuration(largestBurstDuration);
		return result;
	}

}
