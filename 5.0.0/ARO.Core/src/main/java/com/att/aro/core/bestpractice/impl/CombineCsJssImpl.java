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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.CombineCsJssResult;
import com.att.aro.core.bestpractice.pojo.CsJssFilesDetails;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * Best practice for combining CSS and JSS
 * @author EDS team
 * Refactored by Borey Sao
 * Date: November 14, 2014
 */
public class CombineCsJssImpl implements IBestPractice {
	@Value("${combinejscss.title}")
	private String overviewTitle;
	
	@Value("${combinejscss.detailedTitle}")
	private String detailTitle;
	
	@Value("${combinejscss.desc}")
	private String aboutText;
	
	@Value("${combinejscss.url}")
	private String learnMoreUrl;
	
	@Value("${combinejscss.pass}")
	private String textResultPass;
	
	@Value("${combinejscss.results}")
	private String textResults;
	
	@Value("${exportall.csvInefficientCssRequests}")
	private String exportAllInefficientCssRequest;
	
	@Value("${exportall.csvInefficientJsRequests}")
	private String exportAllInefficientJsRequest;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		CombineCsJssResult result = new CombineCsJssResult();
		List<CsJssFilesDetails>  fileDetails = new ArrayList<CsJssFilesDetails>(); //Changes for US432336
		int inefficientCssRequests = 0;
		PacketInfo consecutiveCssJsFirstPacket = null;
		int inefficientJsRequests = 0;
		double cssLastTimeStamp = 0.0;
		double jsLastTimeStamp = 0.0;
		String contentType = "";
		for(Session session: tracedata.getSessionlist()){
			HttpRequestResponseInfo lastRequestObj = null;
			for(HttpRequestResponseInfo httpreq: session.getRequestResponseInfo()){
				if(httpreq.getDirection() == HttpDirection.REQUEST){
					lastRequestObj = httpreq;
				}
				if(httpreq.getDirection() == HttpDirection.RESPONSE && httpreq.getContentType() != null){
					PacketInfo pktInfo = httpreq.getFirstDataPacket();
					if(pktInfo != null){
						contentType = httpreq.getContentType().toLowerCase().trim();
						if(contentType.equalsIgnoreCase("text/css")){
							if (cssLastTimeStamp == 0.0) {
								cssLastTimeStamp = pktInfo.getTimeStamp();
								continue;
							} else {
								if ((pktInfo.getTimeStamp() - cssLastTimeStamp) <= 2.0) {
									inefficientCssRequests++;
									CsJssFilesDetails cssFileDetails = new CsJssFilesDetails(); //Changes for US432336
									cssFileDetails.setTimeStamp(pktInfo.getTimeStamp());
									if(httpreq.getObjName() != null){
										cssFileDetails.setFileName(httpreq.getObjName());
									} else {
										if(lastRequestObj != null){
											cssFileDetails.setFileName(lastRequestObj.getObjName());
										}
									}
									cssFileDetails.setSize(httpreq.getContentLength());
									fileDetails.add(cssFileDetails);
									
									if (consecutiveCssJsFirstPacket == null) {
										consecutiveCssJsFirstPacket = pktInfo;
									}
								}
								cssLastTimeStamp = pktInfo.getTimeStamp();
							}
						}else if(contentType.equalsIgnoreCase("text/javascript") ||
								contentType.equalsIgnoreCase("application/x-javascript") ||
								contentType.equalsIgnoreCase("application/javascript")){
							if (jsLastTimeStamp == 0.0) {
								jsLastTimeStamp = pktInfo.getTimeStamp();
								continue;
							} else {
								if ((pktInfo.getTimeStamp() - jsLastTimeStamp) < 2.0) {
									inefficientJsRequests++;
									
									CsJssFilesDetails jsFileDetails = new CsJssFilesDetails(); //Changes for US432336
									jsFileDetails.setTimeStamp(pktInfo.getTimeStamp());
									if(httpreq.getObjName() != null){
										jsFileDetails.setFileName(httpreq.getObjName());
									} else{
										if(lastRequestObj != null){
											jsFileDetails.setFileName(lastRequestObj.getObjName());
										}
									}
									
									jsFileDetails.setSize(httpreq.getContentLength());
									
									fileDetails.add(jsFileDetails);
									
									if (consecutiveCssJsFirstPacket == null) {
										consecutiveCssJsFirstPacket = pktInfo;
									}
								}
								jsLastTimeStamp = pktInfo.getTimeStamp();
							}
						}
					}
				}
			}
		}
		result.setConsecutiveCssJsFirstPacket(consecutiveCssJsFirstPacket);
		result.setInefficientCssRequests(inefficientCssRequests);
		result.setInefficientJsRequests(inefficientJsRequests);
		if(inefficientCssRequests < 1 && inefficientJsRequests < 1){
			result.setResultType(BPResultType.PASS);
			result.setResultText(textResultPass);
		}else{
			result.setResultType(BPResultType.FAIL);
			result.setResultText(textResults);
			result.setFilesDetails(fileDetails);  //Changes for US432336
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllInefficientCssRequest(exportAllInefficientCssRequest);
		result.setExportAllInefficientJsRequest(exportAllInefficientJsRequest);
		return result;
	}

}
