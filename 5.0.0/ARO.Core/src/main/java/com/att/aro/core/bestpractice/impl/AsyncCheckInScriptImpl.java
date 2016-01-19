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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ILogger;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.AsyncCheckEntry;
import com.att.aro.core.bestpractice.pojo.AsyncCheckInScriptResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * best practice for Async scripts
 * @author EDS team
 * Refactored by Borey Sao
 * Date: December 12, 2014
 *
 */
public class AsyncCheckInScriptImpl implements IBestPractice {
	@Value("${html.asyncload.title}")
	private String overviewTitle;
	
	@Value("${html.asyncload.detailedTitle}")
	private String detailTitle;
	
	@Value("${html.asyncload.desc}")
	private String aboutText;
	
	@Value("${html.asyncload.url}")
	private String learnMoreUrl;
	
	@Value("${html.asyncload.pass}")
	private String textResultPass;
	
	@Value("${html.asyncload.results}")
	private String textResults;
	
	@Value("${exportall.csvSyncPacketCount}")
	private String exportAllSyncPacketCount;
	
	private IHttpRequestResponseHelper reqhelper;
	
	@InjectLogger
	private static ILogger logger;
	
	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper){
		this.reqhelper = reqhelper;
	}
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		AsyncCheckInScriptResult result = new AsyncCheckInScriptResult();
		List<AsyncCheckEntry> entrylist = new ArrayList<AsyncCheckEntry>();
		for(Session session:tracedata.getSessionlist()){
			HttpRequestResponseInfo lastRequestObj = null;
			for(HttpRequestResponseInfo req:session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.REQUEST){
					lastRequestObj = req;
				}
				if(req.getDirection() == HttpDirection.RESPONSE && 
						req.getContentType() != null && 
						req.getContentLength() > 0 &&
						reqhelper.isHtml(req.getContentType())){
					result = parseHtmlToFindSyncLoadingScripts(req, session, result);
					if (result.getSyncLoadedScripts() > 0) {
						result.incrementSyncPacketCount();
						result.setSyncLoadedScripts(0);
						entrylist.add(new AsyncCheckEntry(req, lastRequestObj, session.getDomainName()));
						//modified according to the logic in 4.1
					} else if (result.getAsyncLoadedScripts() > 0) {
						result.incrementAsyncPacketCount();
						result.setAsyncLoadedScripts(0);
					}
				}
			}
		}
		result.setResults(entrylist);
		String text = "";
		if(result.getSyncPacketCount() == 0){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, entrylist.size());//result.getSyncPacketCount());
			result.setResultText(text);
		}else{
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, entrylist.size());//result.getSyncPacketCount());
			result.setResultText(text);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllSyncPacketCount(exportAllSyncPacketCount);
		return result;
	}
	/**
	 * Performs parsing of the html file to find the scripts are loaded
	 * asynchronously or not
	 * 
	 * @param HtppRequestResponseInfo
	 *            info - Http packet to be parsed.
	 */
	public AsyncCheckInScriptResult parseHtmlToFindSyncLoadingScripts(
			HttpRequestResponseInfo info, Session session, AsyncCheckInScriptResult resdata) {
		AsyncCheckInScriptResult result = resdata;
		org.jsoup.nodes.Document doc;
		String packetContent = null;
		
		try {
			packetContent = reqhelper.getContentString(info, session);
		} catch (Exception e1) {
			logger.error("Failed to get content from HttpRequestResponseInfo", e1);
		}
		
		if (packetContent != null) {
			doc = Jsoup.parse(packetContent);
			Elements headLink = null;
			Elements scriptLink = null;

			try {
				headLink = doc.select("head");
				scriptLink = headLink.select("script");
				for (Element element : scriptLink) {
					if (element.hasAttr("src")) {
						if ((element.hasAttr("async"))
								|| element.hasAttr("defer")) {
							result.incrementAsyncLoadedScripts();
							
						} else {
							result.incrementSyncLoadedScripts();
						}
					}
				}
			} catch (Exception e) {
				logger.error("Failed to get content from html content", e);
			}
		}

		return result;
	}
}
