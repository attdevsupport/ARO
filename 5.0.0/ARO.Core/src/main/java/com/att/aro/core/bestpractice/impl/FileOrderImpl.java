/**
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
import com.att.aro.core.bestpractice.pojo.FileOrderEntry;
import com.att.aro.core.bestpractice.pojo.FileOrderResult;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

/**
 * File order best practices - CSS files should be loaded before JS.
 */
public class FileOrderImpl implements IBestPractice {
	@Value("${html.fileorder.title}")
	private String overviewTitle;
	
	@Value("${html.fileorder.detailedTitle}")
	private String detailTitle;
	
	@Value("${html.fileorder.desc}")
	private String aboutText;
	
	@Value("${html.fileorder.url}")
	private String learnMoreUrl;
	
	@Value("${html.fileorder.pass}")
	private String textResultPass;
	
	@Value("${html.fileorder.pluresults}")
	private String textResults;
	
	@Value("${html.fileorder.singresults}")
	private String textResult;
	
	@Value("${exportall.csvFileOrderCount}")
	private String exportAll;
	
	private IHttpRequestResponseHelper reqhelper;
	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper){
		this.reqhelper = reqhelper;
	}
	@InjectLogger
	private static ILogger log;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		FileOrderResult result = new FileOrderResult();
		List<FileOrderEntry> results = new ArrayList<FileOrderEntry>();
		for(Session session:tracedata.getSessionlist()){
			HttpRequestResponseInfo lastRequestObj = null;
			for(HttpRequestResponseInfo req: session.getRequestResponseInfo()){
				Document doc = null;
				if(req.getDirection() == HttpDirection.RESPONSE){
					doc = parseHtml(req, session);
				}else if(req.getDirection() == HttpDirection.REQUEST){
					lastRequestObj = req;
				}
				
				if (doc != null && checkFileOrderAnalysisResults(doc, result)) {
					results.add(new FileOrderEntry(req, lastRequestObj, session.getDomainName()));
				}
				
			}
		}
		String text = "";
		if(results.isEmpty()){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, results.size());
		}else{
			result.setResultType(BPResultType.FAIL);
			if(results.size() > 1){
				text = MessageFormat.format(textResults, results.size());
			}else{
				text = MessageFormat.format(textResult, results.size());
			}
		}
		result.setResultText(text);
		result.setResults(results);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setTextResult(textResult);
		result.setExportAll(exportAll);
		return result;
	}
	/**
	 * Check element whether they are part of CSS or JS list created from the
	 * document.
	 * 
	 * */
	public boolean checkElementPresentInCSSJSList(Element elem, Elements css, Elements jss) {
		for (Element elm : css) {
			if (elm.equals(elem)) {
				return true;
			}
		}
		for (Element elm : jss) {
			if (elm.equals(elem)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Gets the parsed packet and checks any CSS files are getting downloaded
	 * after JS.
	 */
	public boolean checkFileOrderAnalysisResults(Document doc, FileOrderResult result) {

		String lastContentType = null;
		ArrayList<Element> docElements = new ArrayList<Element>(doc
				.getAllElements().size());

		// Extracting the elements in HEAD
		Elements headLink = doc.select("head");

		// Taking out the CSS and JS elements in HEAD
		Elements css = headLink.select("link[href]");
		Elements jss = headLink.select("[src]");

		// Take out all the elements in HEAD to iterate in the above list
		Elements delement = headLink.select("*");
		for (Element element : delement) {
			// Checking whether the element has link and text/css to make sure
			// it is a CSS element.
			if ((checkElementPresentInCSSJSList(element, css, jss))
					&& (element.tagName().equals("link"))
					&& (element.toString().contains("text/css"))) {
				docElements.add(element);
				continue;
			}
			// Checking whether the element has link and text/css to make sure
			// it is a JS element.
			if ((checkElementPresentInCSSJSList(element, css, jss))
					&& (element.hasAttr("src"))
					&& (element.tagName().equals("script"))) {
				docElements.add(element);
				continue;
			}
		}

		/*
		 * Updating the last content type after every iteration and checking it
		 * to make sure that no CSS files are loaded after a JS
		 */
		for (Element elem : docElements) {
			if ((elem.tagName().equals("link"))
					&& (elem.toString().contains("text/css"))) {
				if (lastContentType == null) {
					lastContentType = "css";
					continue;
				} else if ("javascript".equals(lastContentType)) {
					result.incrementFileOrderCount();
					return true;
				} else {
					lastContentType = "css";
					continue;
				}
			}
			if ((elem.tagName().equals("script")) && (elem.hasAttr("src"))) {
				lastContentType = "javascript";
				continue;
			}
		}
		return false;
	}

	public Document parseHtml(HttpRequestResponseInfo req, Session session){
		Document doc = null;
		String packetContent = null;
		if(req.getContentLength() > 0 && reqhelper.isHtml(req.getContentType())){
			try {
				packetContent = reqhelper.getContentString(req, session);
			} catch (Exception e) {
				log.error("Failed to parse html", e);
			}
		}
		if(packetContent != null){
			doc = Jsoup.parse(packetContent);
		}
		return doc;
	}

}
