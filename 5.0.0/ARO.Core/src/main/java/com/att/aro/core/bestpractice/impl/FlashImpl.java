/**
 * Copyright 2016 AT&T
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ILogger;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.FlashResult;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;


public class FlashImpl implements IBestPractice {
	@Value("${flash.title}")
	private String overviewTitle;
	
	@Value("${flash.detailedTitle}")
	private String detailTitle;
	
	@Value("${flash.desc}")
	private String aboutText;
	
	@Value("${flash.url}")
	private String learnMoreUrl;
	
	@Value("${flash.pass}")
	private String textResultPass;
	
	@Value("${flash.results}")
	private String textResults;
	
	@Value("${exportall.csvNumberOfFlashFiles}")
	private String exportAllNumberOfFlashFiles;
	
	private IHttpRequestResponseHelper reqhelper;
	
	@InjectLogger
	private static ILogger log;
	
	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper){
		this.reqhelper = reqhelper;
	}
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		FlashResult result = new FlashResult();
		String contentType = "";
		for(Session session:tracedata.getSessionlist()){
			for(HttpRequestResponseInfo req:session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.RESPONSE &&
						req.getContentType() != null &&
						req.getContentLength() > 0){
					contentType = req.getContentType();
					if(contentType.equalsIgnoreCase("application/x-shockwave-flash") || 
							contentType.equalsIgnoreCase("video/x-flv")){
						result.incrementNumberOfFlash();
						if(result.getFirstFlash() == null){
							result.setFirstFlash(req);
						}
					}else if(contentType.equalsIgnoreCase("text/css") || 
							contentType.equalsIgnoreCase("text/html")){
						result = checkEmbeddedFlashInHTMLOrCSS(req, session, result);
					}
				}
			}
		}
		String text = "";
		if(result.getFirstFlash() == null){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, result.getNumberOfFlash());
			result.setResultText(text);
		}else{
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, result.getNumberOfFlash());
			result.setResultText(text);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllNumberOfFlashFiles(exportAllNumberOfFlashFiles);
		return result;
	}
	/**
	 * Checks embedded Flash in HTML Or CSS content.
	*/
	private FlashResult checkEmbeddedFlashInHTMLOrCSS(HttpRequestResponseInfo reqRessInfo, Session session, 
			FlashResult resdata) {
		FlashResult result = resdata;
		String flashDownloaded = null;
		try {
			flashDownloaded = reqhelper.getContentString(reqRessInfo, session);
		} catch (Exception e) {
			log.error("Failed to get content from HttpRequestResponseInfo", e);
		}
		
		if (flashDownloaded != null) {
			Document doc = Jsoup.parse(flashDownloaded);
			
			//Parsing "embed" in HTML or CSS
			Elements srcsEmbed = doc.select("embed");

			for(Element src : srcsEmbed){
				if(("embed".equalsIgnoreCase(src.tagName()))&&(src.attr("src")).toLowerCase().contains(".swf")){					
						result = checkFlashExistenceInHTTPObject(reqRessInfo, result);					
				}
			}
			
			//Parsing "object" in HTML or CSS
			Elements srcObject = doc.select("object");
			for (Element src : srcObject) {
				if(("object".equalsIgnoreCase(src.tagName()))&&(src.attr("data")).toLowerCase().contains(".swf")){
					result = checkFlashExistenceInHTTPObject(reqRessInfo, result);
				}				
			}
		}
		return result;
	}
	/**
	 * Checks Flash existence in associated HTTP Object.
	*/
	private FlashResult checkFlashExistenceInHTTPObject(HttpRequestResponseInfo reqRessInfo, FlashResult resdata) {
		FlashResult result = resdata;
		HttpRequestResponseInfo assocReqResp = reqRessInfo.getAssocReqResp();
		if (assocReqResp != null) {
			String flashToSearchFor = assocReqResp.getObjName();
			if (flashToSearchFor.toLowerCase().contains(".swf")) {
				result.incrementNumberOfFlash();
				 if (result.getFirstFlash() == null) {
					 result.setFirstFlash(reqRessInfo);
				 }
			}
		}
		return result;
	}

}
