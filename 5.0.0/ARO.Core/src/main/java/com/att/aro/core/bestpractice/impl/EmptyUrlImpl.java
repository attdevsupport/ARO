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
import com.att.aro.core.bestpractice.pojo.EmptyUrlResult;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * best practice for Empty URL
 * The original analyzer only count once if the file has both empty image and URL link
 * After refactor, it counts every empty  link and image
 * The pass or fail result won't change but the files number results will be different.
 */
public class EmptyUrlImpl implements IBestPractice {
	@Value("${empty.url.title}")
	private String overviewTitle;
	
	@Value("${empty.url.detailedTitle}")
	private String detailTitle;
	
	@Value("${empty.url.desc}")
	private String aboutText;
	
	@Value("${empty.url.url}")
	private String learnMoreUrl;
	
	@Value("${empty.url.pass}")
	private String textResultPass;
	
	@Value("${empty.url.results}")
	private String textResults;
	
	@Value("${exportall.csvNumberOfEmptyUrlFiles}")
	private String exportAllEmptyUrlFiles;
	
	private IHttpRequestResponseHelper reqhelper;
	
	@InjectLogger
	private static ILogger log;
	
	@Autowired
	public void setHttpReqResHelper(IHttpRequestResponseHelper reqhelper){
		this.reqhelper = reqhelper;
	}
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		EmptyUrlResult result = new EmptyUrlResult();
		for(Session session:tracedata.getSessionlist()){
			for(HttpRequestResponseInfo req: session.getRequestResponseInfo()){
				if(req.getContentType() != null && reqhelper.isHtml(req.getContentType()) && 
						req.getContentLength() > 0 && req.getDirection() == HttpDirection.RESPONSE){
					result = analyzeHtml(req,session,result);
				}
			}
		}
		String text = "";
		if(result.getFirstFailedHtml() == null){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(this.textResultPass, result.getNumberOfFailedFiles());
			result.setResultText(text);
		}else{
			text = MessageFormat.format(textResults, result.getNumberOfFailedFiles());
			result.setResultText(text);
			result.setResultType(BPResultType.FAIL);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllEmptyUrlFiles(exportAllEmptyUrlFiles);
		return result;
	}
	private EmptyUrlResult analyzeHtml(HttpRequestResponseInfo request, Session session, EmptyUrlResult result) {
		EmptyUrlResult res = result;
		Document htmlDoc = null;
		String contentstr = null;
		try {
			contentstr = reqhelper.getContentString(request, session);
		} catch (Exception e) {
			log.error("Failed to get content from HttpRequestResponseInfo", e);
			return res;
		}
		htmlDoc = Jsoup.parse(contentstr);

		Elements allSrcElements = new Elements();
		allSrcElements.addAll(htmlDoc.select("iframe"));
		allSrcElements.addAll(htmlDoc.select("img"));
		allSrcElements.addAll(htmlDoc.select("script"));
		allSrcElements.addAll(htmlDoc.select("a"));
		allSrcElements.addAll(htmlDoc.select("link"));

		res = checkAttributeEmpty(request, allSrcElements, res);
		

		return res;
		
	}

	private EmptyUrlResult checkAttributeEmpty(HttpRequestResponseInfo rreq, Elements elements, EmptyUrlResult input) {
		EmptyUrlResult result = input;
		int numberOfFailedFiles = result.getNumberOfFailedFiles();
		for (Element element : elements) {
			if ((element.hasAttr("href") && element.attr("href").isEmpty()) || 
					(element.hasAttr("src") && element.attr("src").isEmpty())) {
				// store the 1st occurrence
				if (result.getFirstFailedHtml() == null) {
					result.setFirstFailedHtml(rreq);
				}
				numberOfFailedFiles++;
//				log.info("numberOfFailedFiles:"+ numberOfFailedFiles);
				result.setNumberOfFailedFiles(numberOfFailedFiles);
			}
		}
		return result;
	}

}
