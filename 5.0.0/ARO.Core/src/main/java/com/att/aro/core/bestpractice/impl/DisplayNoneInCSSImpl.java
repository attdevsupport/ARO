/*
 *  Copyright 2015 AT&T
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ILogger;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.DisplayNoneInCSSEntry;
import com.att.aro.core.bestpractice.pojo.DisplayNoneInCSSResult;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * 
 * Display:none in CSS Analysis.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: January 22, 2015
 */
public class DisplayNoneInCSSImpl implements IBestPractice {
	
	@InjectLogger
	private static ILogger log;
	
	@Value("${html.displaynoneincss.title}")
	private String overviewTitle;
	
	@Value("${html.displaynoneincss.detailedTitle}")
	private String detailTitle;
	
	@Value("${html.displaynoneincss.desc}")
	private String aboutText;
	
	@Value("${html.displaynoneincss.url}")
	private String learnMoreUrl;
	
	@Value("${html.displaynoneincss.pass}")
	private String textResultPass;
	
	@Value("${html.displaynoneincss.results}")
	private String textResults;
	
	@Value("${exportall.csvNumberOfFilesWithDisplayNone}")
	private String exportAll;
	
	private IHttpRequestResponseHelper reqhelper;
	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper){
		this.reqhelper = reqhelper;
	}
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		DisplayNoneInCSSResult result = new DisplayNoneInCSSResult();
		
		List<DisplayNoneInCSSEntry> results = new ArrayList<DisplayNoneInCSSEntry>();
		for(Session session: tracedata.getSessionlist()){
			for(HttpRequestResponseInfo reqinfo: session.getRequestResponseInfo()){
				Document doc = null;
				String css;
				if ((reqinfo.getDirection() == HttpDirection.RESPONSE) && (reqinfo.getContentType() != null)
						&& (reqinfo.getContentType().equalsIgnoreCase("text/html"))) {
				
					doc = getParsedHtml(reqinfo, session);
					
					if (doc != null && checkIfDisplayNoneIsPresentInCSSembeddedInHTML(doc)) {
						results.add(new DisplayNoneInCSSEntry(reqinfo));
					}
					
				}
				else if(reqinfo.getDirection() == HttpDirection.RESPONSE && reqinfo.getContentType() != null
						&& reqinfo.getContentType().equalsIgnoreCase("text/css")){
					css = getCSS(reqinfo, session);
					
					if (css != null && checkIfDisplayNoneIsPresentInCSS(css)) {
						results.add(new DisplayNoneInCSSEntry(reqinfo));
					}
					
				}
			}
		}
		int numOfCSSFiles = results.size();
		String text = "";
		if(results.isEmpty()){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, numOfCSSFiles);
		}else{
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, numOfCSSFiles);
		}
		result.setResultText(text);
		result.setResults(results);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAll(exportAll);
		return result;
	}
	/**
	 * Parse the HTML file using JSOUP
	 * @return Document, the parsed HTML file
	 */
	private Document getParsedHtml(HttpRequestResponseInfo reqres, Session session){
		Document doc = null;
		String htmlContent = null;
		try {
			htmlContent = reqhelper.getContentString(reqres, session);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		if (htmlContent != null){
			doc = Jsoup.parse(htmlContent);
		}
		if (doc != null) {
			return doc;
		} else { 
			return null;
		}
	}
	/**
	 * This method return the CSS file from the RR objects
	 * @return CSS file 
	 */
	private String getCSS(HttpRequestResponseInfo reqres, Session session){
		
		String cssContent = null;
		try {
			cssContent = reqhelper.getContentString(reqres, session);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return cssContent;
	}

	/**
	 * This function checks for display:none in CSS embedded in HTML
	 * 
	 * @return true if found
	 */
	private boolean checkIfDisplayNoneIsPresentInCSSembeddedInHTML(Document doc) {
		/* Check HEAD for CSS rules*/
		Elements headLink = doc.select("head");
		/*Iterate the HEAD elements for CSS rules.*/
		Elements elements = headLink.select("*");
		for (Element element : elements) {
			/*Check if the CSS is embedded in the html */
			if ((element.tagName().equalsIgnoreCase("style"))
					&& (element.toString().contains("text/css"))
					&& element.text().contains("display:none")) {
					return true;
			}
		}
		/* get all the html elements which contains inline CSS rules.*/
		Elements cssElements  = doc.select("[style*=display:none]");
		if(!cssElements.isEmpty()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * This function checks for display:none in css files.
	 * 
	 * @return true if found
	 */
	private boolean checkIfDisplayNoneIsPresentInCSS(String css){
		
		if(css.contains("display:none")){
			return true;
		}
		return false;
		
	}
	
}
