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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.EvaluatorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ILogger;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.MinificationEntry;
import com.att.aro.core.bestpractice.pojo.MinificationResult;
import com.att.aro.core.bestpractice.pojo.YuiCompressorErrorReporter;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;


/**
 * best practice for accessing Bluetooth, Camera and GPS
 * 
 * Note: New Core results (files number) will be different than
 * old analyzer. 1. old Analyzer use YuiCompressor lib 2.4.2 and the new
 * core use 2.4.8
 */
public class MinificationImpl implements IBestPractice {
	
	private HtmlCompressor htmlCompressor;
	private static final int MIN_SAVING_PERCENTAGE = 5;//we only care if saving more than 5%

	@InjectLogger
	private static ILogger logger;

	@Value("${minification.title}")
	private String overviewTitle;

	@Value("${minification.detailedTitle}")
	private String detailTitle;

	@Value("${minification.desc}")
	private String aboutText;

	@Value("${minification.url}")
	private String learnMoreUrl;

	@Value("${minification.pass}")
	private String textResultPass;

	@Value("${minification.results}")
	private String textResults;

	@Value("${exportall.csvNumberOfMinifyFiles}")
	private String exportAllNumberOfMinifyFiles;

	private IHttpRequestResponseHelper reqhelper;

	class Worker implements Callable<MinificationEntry>{
		String contentType;
		HttpRequestResponseInfo req;
		HttpRequestResponseInfo lastRequestObj;
		Session session;
		public Worker(String content, HttpRequestResponseInfo request, HttpRequestResponseInfo lastRequestObject, Session sess){
			contentType = content;
			req = request;
			lastRequestObj = lastRequestObject;
			session = sess;
		}
		@Override
		public MinificationEntry call() throws Exception{
			if (reqhelper.isJavaScript(contentType)) {
				MinificationEntry res = calculateSavingMinifiedJavascript(req, lastRequestObj, session); 
//				logger.error("Req length (JS): " + req.getContentLength() + time + (new Date().getTime()-enter.getTime()));
				return res;
			} else if (reqhelper.isCss(contentType)) {
				MinificationEntry res = calculateSavingMinifiedCss(req, lastRequestObj, session); 
//				logger.error("Req length (CSS): " + req.getContentLength() + time + (new Date().getTime()-enter.getTime()));
				return res;
			} else if (reqhelper.isHtml(contentType)) {
				MinificationEntry res = calculateSavingMinifiedHtml(req, lastRequestObj, session); 
//				logger.error("Req length (HTML): " + req.getContentLength() + time + (new Date().getTime()-enter.getTime()));
				return res;
			} else if (reqhelper.isJSON(contentType)) {
				MinificationEntry res = calculateSavingMinifiedJson(req, lastRequestObj, session); 
//				logger.error("Req length (JSON): " + req.getContentLength() + time + (new Date().getTime()-enter.getTime()));
				return res;
			}
			return null;
		}
	}
	
	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper) {
		this.reqhelper = reqhelper;
	}

	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		htmlCompressor = null;
		MinificationResult result = new MinificationResult();
		initHtmlCompressor();
		List<MinificationEntry> minificationEntryList = new ArrayList<MinificationEntry>();
		String contentType = null;
		int totalSavingInBytes = 0;

		final ExecutorService executorService = Executors.newFixedThreadPool(50);
		Collection<Worker> workers = new ArrayList<Worker>();
		
		for (Session session : tracedata.getSessionlist()) {
			HttpRequestResponseInfo lastRequestObj = null;
			for (HttpRequestResponseInfo req : session.getRequestResponseInfo()) {
				if (req.getDirection() == HttpDirection.REQUEST) {
					lastRequestObj = req;
				}
				contentType = req.getContentType();
				if (req.getDirection() == HttpDirection.RESPONSE && req.getContentLength() > 0 && contentType != null) {
					workers.add(new Worker(contentType, req, lastRequestObj, session));
//					if (reqhelper.isJavaScript(contentType)) {
//						entry = calculateSavingMinifiedJavascript(req, lastRequestObj, session);
//					} else if (reqhelper.isCss(contentType)) {
//						workers.add(new Worker(req, lastRequestObj, session));
////						Future<MinificationEntry> res1 = executorService.submit(new Worker(req, lastRequestObj, session);
////						entry = calculateSavingMinifiedCss(req, lastRequestObj, session);
//					} else if (reqhelper.isHtml(contentType)) {
//						entry = calculateSavingMinifiedHtml(req, lastRequestObj, session);
//					} else if (reqhelper.isJSON(contentType)) {
//						entry = calculateSavingMinifiedJson(req, lastRequestObj, session);
//					}
//					if (entry != null) {
//						totalSavingInBytes += entry.getSavingsSizeInByte();
//						minificationEntryList.add(entry);
//					}
				}
			}
		}

		try {
			List<Future<MinificationEntry>> list = executorService.invokeAll(workers);
			for (Future<MinificationEntry> future: list){
				if (future != null && future.get()!=null) {
					totalSavingInBytes += future.get().getSavingsSizeInByte();
					minificationEntryList.add(future.get());
				}
			}
		} catch(InterruptedException ie){
			logger.error(ie.getMessage());
		} catch(ExecutionException ee){
			logger.error(ee.getMessage());
		}
		executorService.shutdown();
		
		int savingInKb = totalSavingInBytes / 1024;
		result.setTotalSavingsInKb(savingInKb);
		result.setTotalSavingsInByte(totalSavingInBytes);
		result.setMinificationEntryList(minificationEntryList);
		int numberOfFiles = minificationEntryList.size();
		String text = "";
		if (minificationEntryList.isEmpty()) {
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, numberOfFiles, savingInKb);
			result.setResultText(text);
		} else {
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, numberOfFiles, savingInKb, totalSavingInBytes);
			result.setResultText(text);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllNumberOfMinifyFiles(exportAllNumberOfMinifyFiles);
		return result;
	}

	/**
	 * calculate Saving Minified Html. Expand content, then compress and compare for savings.
	 * 
	 * @param req
	 * @param lastRequestObj
	 * @param session
	 * @return null or MinificationEntry
	 */
	public MinificationEntry calculateSavingMinifiedHtml(HttpRequestResponseInfo req, HttpRequestResponseInfo lastRequestObj, Session session) {
		String content = null;
		try {
			content = reqhelper.getContentString(req, session);
		} catch (Exception e) {
			logger.error("Failed to get content from html response: " + e.getMessage());
		}
		if (content != null) {
			String minicontent = null;
			try {
				minicontent = htmlCompressor.compress(content);
			} catch (Exception e) {
				logger.error("Failed to compress html response: " + e.getMessage());
			}
			if (minicontent != null) {
				return minSavingEntry(minicontent.length(), content.length(), req, lastRequestObj, session);
			}
		}
		return null;
	}

	public MinificationEntry calculateSavingMinifiedCss(HttpRequestResponseInfo req, HttpRequestResponseInfo lastRequestObj, Session session) {
		String content = null;
		try {
			content = reqhelper.getContentString(req, session);
		} catch (Exception e) {
			logger.error("Failed to get content from Css response: " + e.getMessage());
		}
		if (content != null) {
			String minicontent = null;
			try {
				minicontent = minifyCss(content);
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			}
			if (minicontent != null) {
				return minSavingEntry(minicontent.length(), content.length(), req, lastRequestObj, session);
			}
		}
		return null;
	}

	public String minifyCss(String content) throws IOException {
		Reader input = new StringReader(content);
		Writer writer = new StringWriter();
		CssCompressor compressor = new CssCompressor(input);
		compressor.compress(writer, -1);
		return writer.toString();
	}

	public MinificationEntry calculateSavingMinifiedJavascript(HttpRequestResponseInfo req, HttpRequestResponseInfo lastRequestObj, Session session) {
		String content = null;
		try {
			content = reqhelper.getContentString(req, session);
		} catch (Exception e) {
			logger.error("Failed to get content from Javascript response: " + e.getMessage());
		}
		if (content != null) {
			String minicontent = null;
			try {
				minicontent = minifyJavascript(content);
			} catch (EvaluatorException ex) {
				logger.error(ex.getMessage());
			} catch (IOException ex2) {
				logger.error(ex2.getMessage());
			} catch (Exception ex) {
				logger.error("Failed to minify javascript, content: " + content);
			}
			if (minicontent != null) {
				return minSavingEntry(minicontent.length(), content.length(), req, lastRequestObj, session);
			}
		}
		return null;
	}

	public String minifyJavascript(String content) throws EvaluatorException, IOException {
		Reader input = new StringReader(content);
		Writer writer = new StringWriter();
		JavaScriptCompressor compressor = new JavaScriptCompressor(input, new YuiCompressorErrorReporter());
		compressor.compress(writer, -1, false, false, false, false);
		return writer.toString();
	}

	public MinificationEntry calculateSavingMinifiedJson(HttpRequestResponseInfo req, HttpRequestResponseInfo lastRequestObj, Session session) {
		String content = null;
		try {
			content = reqhelper.getContentString(req, session);
		} catch (Exception e) {
			logger.error("Failed to get content from Json response: " + e.getMessage());
		}
		if (content != null) {
			String minicontent = minifyJson(content);
			return minSavingEntry(minicontent.length(), content.length(), req, lastRequestObj, session);
		}
		return null;
	}

	private MinificationEntry minSavingEntry(int minifiedSize, int originalSize, HttpRequestResponseInfo req, HttpRequestResponseInfo lastRequestObj, Session session) {
		if (minifiedSize >= originalSize) {
			return null;
		}
		int savingBytes = originalSize - minifiedSize;
		float savingPercentage = ((float) savingBytes * 100 / (float) originalSize);
		if (savingPercentage > MIN_SAVING_PERCENTAGE) {
			return new MinificationEntry(req, lastRequestObj, session.getDomainName(), Math.round(savingPercentage), savingBytes);
		}
		return null;
	}

	/**
	 * compress the given json string and return a compressed string
	 * 
	 * @param jsonString
	 * */
	public String minifyJson(String jsonString) {
		String tokenizer = "\"|(/\\*)|(\\*/)|(//)|\\n|\\r";
		String tmp, tmp2, leftString, rightString = "";
		int from = 0;
		StringBuffer sBuffer = new StringBuffer();
		Pattern pattern = Pattern.compile(tokenizer);
		Matcher matcher = pattern.matcher(jsonString);
		if (!matcher.find()) {
			return jsonString;
		} else {
			matcher.reset();
		}
		while (matcher.find()) {
			leftString = jsonString.substring(0, matcher.start());
			rightString = jsonString.substring(matcher.end(), jsonString.length());
			tmp = jsonString.substring(matcher.start(), matcher.end());
			tmp2 = leftString.substring(from).replaceAll("(\\n|\\r|\\s)*", "");
			sBuffer.append(tmp2);
			from = matcher.end();
			if (!tmp.substring(0, 1).matches("\\n|\\r|\\s")) {
				sBuffer.append(tmp);
			}
		}
		sBuffer.append(rightString);
		return sBuffer.toString();

	}

	//Helper functions
	private void initHtmlCompressor() {
		htmlCompressor = new HtmlCompressor();
		htmlCompressor.setRemoveIntertagSpaces(true);                 // removes iter-tag whitespace characters                   
		htmlCompressor.setRemoveQuotes(true);                         // removes unnecessary tag attribute quotes                 
		htmlCompressor.setSimpleDoctype(true);                        // simplify existing doctype                                
		htmlCompressor.setRemoveScriptAttributes(true);               // remove optional attributes from script tags              
		htmlCompressor.setRemoveStyleAttributes(true);                // remove optional attributes from style tags               
		htmlCompressor.setRemoveLinkAttributes(true);                 // remove optional attributes from link tags                
		htmlCompressor.setRemoveFormAttributes(true);                 // remove optional attributes from form tags                
		htmlCompressor.setRemoveInputAttributes(true);                // remove optional attributes from input tags               
		htmlCompressor.setSimpleBooleanAttributes(true);              // remove values from boolean tag attributes                
		htmlCompressor.setRemoveJavaScriptProtocol(true);             // remove "javascript:" from inline event handlers          
		htmlCompressor.setRemoveHttpProtocol(true);                   // replace "http://" with "//" inside tag attributes        
		htmlCompressor.setRemoveHttpsProtocol(true);                  // replace "https://" with "//" inside tag attributes       
		htmlCompressor.setPreserveLineBreaks(false);                  // preserves original line breaks                           
		htmlCompressor.setRemoveSurroundingSpaces("br,p");            // remove spaces around provided tags                       
		htmlCompressor.setCompressCss(true);                          // compress inline css                                      
		htmlCompressor.setCompressJavaScript(true);                   // compress inline javascript                               
		htmlCompressor.setYuiCssLineBreak(-1);                        // --line-break param for Yahoo YUI Compressor              
		htmlCompressor.setYuiJsDisableOptimizations(false);           // --disable-optimizations param for Yahoo YUI Compressor   
		htmlCompressor.setYuiJsLineBreak(-1);                         // --line-break param for Yahoo YUI Compressor              
		htmlCompressor.setYuiJsNoMunge(true);                         // --nomunge param for Yahoo YUI Compressor                 
		htmlCompressor.setYuiJsPreserveAllSemiColons(true);           // --preserve-semi param for Yahoo YUI Compressor           
		
	}

}//end class
