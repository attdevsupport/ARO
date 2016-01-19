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
import com.att.aro.core.bestpractice.pojo.ConnectionClosingResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for connection closing problem
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 14, 2014
 *
 */
public class ConnectionClosingImpl implements IBestPractice {
	@Value("${connections.connClosing.title}")
	private String overviewTitle;
	
	@Value("${connections.connClosing.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.connClosing.desc}")
	private String aboutText;
	
	@Value("${connections.connClosing.url}")
	private String learnMoreUrl;
	
	@Value("${connections.connClosing.pass}")
	private String textResultPass;
	
	@Value("${connections.connClosing.results}")
	private String textResults;
	
	@Value("${exportall.csvConnClosingDesc}")
	private String exportAllConnClosingDesc;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		double wastedBurstEnergy = 0.0;
		boolean conClosingProbPassed = true;
		double tcpControlEnergy = 0;
		double tcpControlEnergyRatio = 0;
		double largestEnergyTime = 0.0;
		double maxEnergy = 0.0;
		double currentEnergy;
		if(tracedata.getBurstcollectionAnalysisData().getTotalEnergy() > 0){
			for(Burst burst : tracedata.getBurstcollectionAnalysisData().getBurstCollection()){
				if(burst.getBurstCategory() == BurstCategory.TCP_PROTOCOL){
					currentEnergy = burst.getEnergy();
					wastedBurstEnergy += burst.getEnergy();
					if (currentEnergy > maxEnergy) {
						maxEnergy = currentEnergy;
						largestEnergyTime = burst.getBeginTime();
					}
				}
			}
			double percentageWasted = wastedBurstEnergy / tracedata.getBurstcollectionAnalysisData().getTotalEnergy();
			conClosingProbPassed = percentageWasted < 0.05;
			tcpControlEnergy = wastedBurstEnergy;
			tcpControlEnergyRatio = percentageWasted;
		}
		ConnectionClosingResult result = new ConnectionClosingResult();
		
		if(!conClosingProbPassed){ //if failed
			result.setResultType(BPResultType.FAIL);
			NumberFormat nfo = NumberFormat.getInstance();
			nfo.setMaximumFractionDigits(1);
			String text = MessageFormat.format(textResults, nfo.format(tcpControlEnergy), 
					nfo.format(tcpControlEnergyRatio*100));
			result.setResultText(text);
		}else{
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}
		result.setWastedBurstEnergy(wastedBurstEnergy);
		result.setConClosingProb(conClosingProbPassed);
		result.setTcpControlEnergy(tcpControlEnergy);
		result.setTcpControlEnergyRatio(tcpControlEnergyRatio);
		result.setLargestEnergyTime(largestEnergyTime);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllConnClosingDesc(exportAllConnClosingDesc);
		
		return result;
	}

}
