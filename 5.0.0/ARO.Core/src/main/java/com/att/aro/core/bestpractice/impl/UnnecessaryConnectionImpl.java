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
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionEntry;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for multiple simultaneous Connection
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 14, 2014
 *
 */
public class UnnecessaryConnectionImpl implements IBestPractice {

	int tightlyCoupledBurstCount = 0;
	double tightlyCoupledBurstTime = 0;
	private List<UnnecessaryConnectionEntry> ucEntryList;
	
	@Value("${connections.unnecssaryConn.title}")
	private String overviewTitle;
	
	@Value("${connections.unnecssaryConn.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.unnecssaryConn.desc}")
	private String aboutText;
	
	@Value("${connections.unnecssaryConn.url}")
	private String learnMoreUrl;
	
	@Value("${connections.unnecssaryConn.pass}")
	private String textResultPass;
	
	@Value("${connections.unnecssaryConn.results}")
	private String textResults;
	
	@Value("${exportall.csvMultiConnDesc}")
	private String exportAllMultiConnDesc;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		UnnecessaryConnectionResult result = new UnnecessaryConnectionResult();
		ucEntryList = new ArrayList<UnnecessaryConnectionEntry>();
		validateUnnecessaryConnections(tracedata.getBurstcollectionAnalysisData().getBurstCollection());
		if(tightlyCoupledBurstCount < 4){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			String text = MessageFormat.format(this.textResults, this.tightlyCoupledBurstCount);
			result.setResultText(text);
		}
		result.setTightlyCoupledBurstCount(tightlyCoupledBurstCount);
		result.setTightlyCoupledBurstTime(tightlyCoupledBurstTime);
		result.setTightlyCoupledBurstsDetails(ucEntryList);
		
		result.setSelfTest(false);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllMultiConnDesc(exportAllMultiConnDesc);
		
		return result;
	}
	/**
	 * To Validate the simultaneous TCP connections
	 */
	private void validateUnnecessaryConnections(List<Burst> burstCollection) {
		int setCount = 0;
		int maxCount = 0;
		Burst maxBurst = null;
		for (int i = 0; i < burstCollection.size(); ++i) {
			Burst burstInfo = burstCollection.get(i);
			if (burstInfo.getBurstCategory() == BurstCategory.USER_INPUT
					|| burstInfo.getBurstCategory() == BurstCategory.SCREEN_ROTATION) {
				continue;
			}
			double startTime = burstInfo.getBeginTime();
			double endTime = startTime + 60.0;
			int count = 1;
			double totalSize=0;
			for (int j = i + 1; j < burstCollection.size()
					&& burstCollection.get(j).getEndTime() <= endTime; ++j) {
				if (burstCollection.get(j).getBurstCategory() != BurstCategory.USER_INPUT
						|| burstInfo.getBurstCategory() == BurstCategory.SCREEN_ROTATION) {
					++count;
					totalSize += burstCollection.get(j).getBurstBytes();
					
				}
			}
			//Checking for 4 burts within 60 sec 
			if (count >= 4) {
				ucEntryList.add(new UnnecessaryConnectionEntry(startTime, endTime, count, totalSize/1024));
				++setCount;
				if (count > maxCount) {
					maxCount = count;
					maxBurst = burstInfo;
				}
				i = i + count;
			} 
		}

		tightlyCoupledBurstCount = setCount;
		
		if (maxBurst != null) {
			tightlyCoupledBurstTime = maxBurst.getBeginTime();
		}
	}

}//end class
