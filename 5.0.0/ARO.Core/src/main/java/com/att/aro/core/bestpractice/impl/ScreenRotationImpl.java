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

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.ScreenRotationResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for screen rotation
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 14, 2014
 *
 */
public class ScreenRotationImpl implements IBestPractice {
	@Value("${connections.screenRotation.title}")
	private String overviewTitle;
	
	@Value("${connections.screenRotation.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.screenRotation.desc}")
	private String aboutText;
	
	@Value("${connections.screenRotation.url}")
	private String learnMoreUrl;
	
	@Value("${connections.screenRotation.pass}")
	private String textResultPass;
	
	@Value("${connections.screenRotation.results}")
	private String textResults;
	
	@Value("${exportall.csvSrcnRtnDescPass}")
	private String exportAllScreenRotationDescPass;
	
	@Value("${exportall.csvSrcnRtnDesc}")
	private String exportAllScreenRotationFailed;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		ScreenRotationResult result = new ScreenRotationResult();
		boolean passScreenRotation = true;//assuming it is passed for now
		double screenRotationBurstTime = 0.0;
		for(Burst burst:tracedata.getBurstcollectionAnalysisData().getBurstCollection()){
			if(burst.getBurstCategory() == BurstCategory.SCREEN_ROTATION){
				passScreenRotation = false;//screen rotation trigger network activity => fail
				screenRotationBurstTime = burst.getBeginTime();
				break; 
			}
		}
		result.setScreenRotationBurstTime(screenRotationBurstTime);
		if(passScreenRotation){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			result.setResultText(textResults);
		}
		result.setScreenRotationBurstTime(screenRotationBurstTime);
		
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllScreenRotationDescPass(exportAllScreenRotationDescPass);
		result.setExportAllScreenRotationFailed(exportAllScreenRotationFailed);
		
		return result;
	}

}
