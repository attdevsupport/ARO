/**
 * Copyright 2014 AT&T
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.ILogger;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.FileCompressionResult;
import com.att.aro.core.bestpractice.pojo.TextFileCompressionEntry;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetreader.pojo.PacketDirection;

/**
 * best practice for text file compression
 * Note: In order to verify the content encoding, instead of checking "gzip" or "compressed", using "identity"
 * The "identity" content coding is always acceptable.The old analyzer counting probably omitted other types
 * content encoding. It caused the new Core results is more than the old analyzer.
 */
public class FileCompressionImpl implements IBestPractice {
	public static final int FILE_SIZE_THRESHOLD_BYTES = 850;
	public static final int UNCOMPRESSED_SIZE_FAILED_THRESHOLD = 100*1024; //100 Kb
	
	@InjectLogger
	ILogger logger;
	
	@Value("${textFileCompression.title}")
	private String overviewTitle;
	
	@Value("${textFileCompression.detailedTitle}")
	private String detailTitle;
	
	@Value("${textFileCompression.desc}")
	private String aboutText;
	
	@Value("${textFileCompression.url}")
	private String learnMoreUrl;
	
	@Value("${textFileCompression.pass}")
	private String textResultPass;
	
	@Value("${textFileCompression.results}")
	private String textResults;
	
	@Value("${exportall.csvTextFileCompression}")
	private String exportAll;
	
	@Value("${exportall.csvTextFileCompressionKb}")
	private String exportAllKb;
	
	private IHttpRequestResponseHelper reqhelper;
	@Autowired
	public void setHttpRequestResponseHelper(IHttpRequestResponseHelper reqhelper){
		this.reqhelper = reqhelper;
	}

	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		FileCompressionResult result = new FileCompressionResult();
		List<TextFileCompressionEntry> resultlist = new ArrayList<TextFileCompressionEntry>();
		int uncompressedCounter = 0;
		int compressedCounter = 0;
		int totalUncompressBytes = 0;
		for(Session session: tracedata.getSessionlist()){
			HttpRequestResponseInfo lastRequestObj = null;
			for(HttpRequestResponseInfo req:session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.REQUEST){
					lastRequestObj = req;
				}
				if(req.getPacketDirection() == PacketDirection.DOWNLINK &&
						req.getContentLength() > 0 && req.getContentType() != null &&
						isTextContent(req.getContentType())){
					//no compression?
					if(req.getContentEncoding() == null || req.getContentEncoding().contains("identity")){
						//don't count tiny file
						if(req.getContentLength() > FILE_SIZE_THRESHOLD_BYTES){
							uncompressedCounter++;
						}else{
							compressedCounter++;
						}
						totalUncompressBytes += req.getContentLength();
						TextFileCompressionEntry tfcEntry = new TextFileCompressionEntry(req,lastRequestObj,session.getDomainName());
						tfcEntry.setSavingsTextPercentage(calculateSavingForTextBasedOnGzip(req, session));
						resultlist.add(tfcEntry);
						
					}else{
						compressedCounter++;
					}
				}
			}
		}
		result.setNoOfCompressedFiles(compressedCounter);
		result.setNoOfUncompressedFiles(uncompressedCounter);
		result.setTotalUncompressedSize(totalUncompressBytes);
		result.setResults(resultlist);
		String text = "";
		if(uncompressedCounter == 0){
			result.setResultType(BPResultType.PASS);
		}else if(totalUncompressBytes >= UNCOMPRESSED_SIZE_FAILED_THRESHOLD){
			result.setResultType(BPResultType.FAIL);
		}else{
			result.setResultType(BPResultType.WARNING);
		}
		if(result.getResultType() == BPResultType.PASS){
			text = MessageFormat.format(textResultPass, FILE_SIZE_THRESHOLD_BYTES, FILE_SIZE_THRESHOLD_BYTES);
		}else{
			text = MessageFormat.format(textResults, totalUncompressBytes / 1024, FILE_SIZE_THRESHOLD_BYTES);
		}
		result.setResultText(text);
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAll(exportAll);
		result.setExportAllKb(exportAllKb);
		return result;
	}
	/**
	 *  Indicates whether the content type is text or not.
	 *  
	 *  The following content types are considered as text:
	 * - any type starting with 'text/'
	 * - any type starting with 'application/' and followed by 'xml', for example: 'application/atom+xml'
 	 * - application/ecmascript
     * - application/json
     * - application/javascript
     * - message/http
     * 
	 * @return Returns true if the content type is text otherwise return false;
	 */
	public boolean isTextContent(String contentType) {
		Pattern textContentTypeText = Pattern.compile("^text/.*");
		Pattern textContentTypeXml = Pattern.compile("^application/.*xml");
			
		if("application/ecmascript".equals(contentType)||
			"application/json".equals(contentType)||
			"application/javascript".equals(contentType)||
			"text/javascript".equals(contentType)||
			"message/http".equals(contentType)){
			return true;		
		} else {

			Matcher match = textContentTypeText.matcher(contentType);
			if (match.matches()) {
				return true;
			}
			match = textContentTypeXml.matcher(contentType);
			if (match.matches()) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Calculating the percentage of savings when we use gzip. US432352 Changes
	 * @param req
	 * @param session
	 * @return
	 */
	public int calculateSavingForTextBasedOnGzip(HttpRequestResponseInfo req, Session session){
		byte[] content = null;
		byte[] compressedBytes = null;
		try {
			content = reqhelper.getContent(req, session);
		} catch (Exception exp) {
			logger.error("Failed to get text content from response: "+ exp.getMessage());
		}
		if(content == null){
			return 0;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
		GZIPOutputStream gzip;
		try {
			gzip = new GZIPOutputStream(out);

			gzip.write(content);
			gzip.close();
	
			compressedBytes = out.toByteArray(); 
			out.close();
		} catch (IOException IOexp) {
			logger.error("Failed to get text content on Gzip savings calculation : "+ IOexp.getMessage());
		}
		
		int originalSize = req.getContentLength();
		int savingBytes = originalSize - compressedBytes.length;
		
		if(savingBytes < 0){
			return 0;
		}
		
		float savingPercentage = ((float)savingBytes * 100/(float)originalSize);
/*		if(savingPercentage > MIN_SAVING_PERCENTAGE){
			MinificationEntry entry = new MinificationEntry(req, lastRequestObj, session.getDomainName(), 
					Math.round(savingPercentage), savingBytes);
*/		
		return Math.round(savingPercentage);
		
	}
	
	
}//end class
