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
import com.att.aro.core.bestpractice.pojo.ConnectionOpeningResult;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

/**
 * best practice for connection opening
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 14, 2014
 *
 */
public class ConnectionOpeningImpl implements IBestPractice {

	@Value("${connections.connectionOpening.title}")
	private String overviewTitle;
	
	@Value("${connections.connectionOpening.detailedTitle}")
	private String detailTitle;
	
	@Value("${connections.connectionOpening.desc}")
	private String aboutText;
	
	@Value("${connections.connectionOpening.url}")
	private String learnMoreUrl;
	
	@Value("${connections.connectionOpening.selfEvaluation}")
	private String textResult;
	
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		ConnectionOpeningResult result = new ConnectionOpeningResult();
		result.setSelfTest(true);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setResultText(textResult);
		result.setResultType(BPResultType.SELF_TEST);
		
		return result;
	}

}
