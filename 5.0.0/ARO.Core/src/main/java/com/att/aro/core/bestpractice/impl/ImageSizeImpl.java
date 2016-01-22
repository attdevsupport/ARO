/**
 *  Copyright 2016 AT&T
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

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
import com.att.aro.core.bestpractice.pojo.HtmlImage;
import com.att.aro.core.bestpractice.pojo.ImageSizeEntry;
import com.att.aro.core.bestpractice.pojo.ImageSizeResult;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;

/**
 * best practice for image size
 */
public class ImageSizeImpl implements IBestPractice {
	@Value("${imageSize.title}")
	private String overviewTitle;
	
	@Value("${imageSize.detailedTitle}")
	private String detailTitle;
	
	@Value("${imageSize.desc}")
	private String aboutText;
	
	@Value("${imageSize.url}")
	private String learnMoreUrl;
	
	@Value("${imageSize.pass}")
	private String textResultPass;
	
	@Value("${imageSize.results}")
	private String textResults;
	
	@Value("${exportall.csvNumberOfLargeImages}")
	private String exportNumberOfLargeImages;
	
	@InjectLogger
	private static ILogger log;
	
	private IHttpRequestResponseHelper reqhelper;
	
	@Autowired
	public void setReqhelper(IHttpRequestResponseHelper helper){
		this.reqhelper = helper;
	}
	//helper variable from legacy code
	private boolean mImageFoundInHtmlOrCss = false;
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		mImageFoundInHtmlOrCss = false;
		ImageSizeResult result = new ImageSizeResult();
		List<ImageSizeEntry> entrylist = new ArrayList<ImageSizeEntry>();
		int deviceScreenSizeX = 528; //480 * 1.1;
		int deviceScreenSizeY = 880; //800 * 1.1;
		//screen size is available when reading trace directory
		if(tracedata.getTraceresult().getTraceResultType() == TraceResultType.TRACE_DIRECTORY){
			TraceDirectoryResult dirdata = (TraceDirectoryResult)tracedata.getTraceresult();
			deviceScreenSizeX = (dirdata.getDeviceScreenSizeX() * 110)/100;
			deviceScreenSizeY = (dirdata.getDeviceScreenSizeY() * 110)/100;
		}
		for(Session session: tracedata.getSessionlist()){
			HttpRequestResponseInfo lastReq = null;
			for(HttpRequestResponseInfo req: session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.REQUEST){
					lastReq = req;
				}
				if(req.getDirection() == HttpDirection.RESPONSE && 
						req.getContentType() != null && 
						req.getContentType().contains("image/")){
					boolean isBigSize = false;
					List<HtmlImage> htmlImageList = checkThisImageInAllHTMLOrCSS(session, req);
					if(mImageFoundInHtmlOrCss){
						mImageFoundInHtmlOrCss = false;
						if(!htmlImageList.isEmpty()){
							for(int index=0; index<htmlImageList.size(); index++) {
								HtmlImage htmlImage = htmlImageList.get(index);
								isBigSize = compareDownloadedImgSizeWithStdImageSize(req, htmlImage, deviceScreenSizeX, 
										deviceScreenSizeY, session);									
								if (isBigSize) {
									break;
								}
							}
						}else{
							isBigSize = compareDownloadedImgSizeWithStdImageSize(req, null, deviceScreenSizeX, 
									deviceScreenSizeY, session); 
						}
						if(isBigSize){
							entrylist.add(new ImageSizeEntry(req, lastReq, session.getDomainName()));
						}
					}
				}
			}
		}
		result.setDeviceScreenSizeRangeX(deviceScreenSizeX);
		result.setDeviceScreenSizeRangeY(deviceScreenSizeY);
		result.setResults(entrylist);
		String text = "";
		if(entrylist.isEmpty()){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, entrylist.size());
			result.setResultText(text);
		}else{
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, entrylist.size());
			result.setResultText(text);
		}
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportNumberOfLargeImages(exportNumberOfLargeImages);
		return result;
	}
	/**
	 * This method checks the existence of specific image in all HTML Or CSS files.
	 * 
	 * @return List of HtmlImage
	 */
	private List<HtmlImage> checkThisImageInAllHTMLOrCSS(Session tcpSession, HttpRequestResponseInfo reqRessInfo) {
		ArrayList<HtmlImage> htmlImageLst = new ArrayList<HtmlImage>();
		int noOfRRRecords =0;
		for (HttpRequestResponseInfo hrri : tcpSession.getRequestResponseInfo()) {
			++noOfRRRecords;
			if (hrri.getDirection() == HttpDirection.RESPONSE && hrri.getContentType() != null) {
				String contentType = hrri.getContentType();
				if (contentType.equalsIgnoreCase("text/css") || contentType.equalsIgnoreCase("text/html")) {
					HttpRequestResponseInfo assocReqResp = reqRessInfo.getAssocReqResp();
					if (assocReqResp != null) {
						String imageToSearchFor = assocReqResp.getObjName();
						String imageDownloaded = null;
						try {
							imageDownloaded = reqhelper.getContentString(hrri, tcpSession);
						} catch (IOException e) {
							log.warn("IOException, something is wrong.",e);
						} catch (Exception e) {
							log.error("Failed to get content from HttpRequestResponseInfo", e);
						}
					
						if ((imageToSearchFor != null && imageDownloaded != null)&&
								(imageDownloaded.toLowerCase().contains(imageToSearchFor.toLowerCase()))) {
							Document doc = Jsoup.parse(imageDownloaded);
							Elements images = doc.select("[src]");
							for (Element src : images) {
								 if ((src.tagName().equals("img"))&&
										 ((src.attr("abs:src")).contains(imageToSearchFor))) {									
									 mImageFoundInHtmlOrCss = true;
									 /* Get width and height from HTML or CSS*/
									String width = extractNumericValue(src.attr("width"));
									String height = extractNumericValue(src.attr("height"));
									if (!width.isEmpty() && !height.isEmpty()) {
										 double iWidth = Double.parseDouble(width);
										 double iHeight = Double.parseDouble(height);
										 htmlImageLst.add(new HtmlImage((int)iWidth, (int)iHeight));
										 break;
									}
								 }								 
							}
						}													
					}
				}else if(!mImageFoundInHtmlOrCss && (noOfRRRecords == tcpSession.getRequestResponseInfo().size())){
					/* searched all the RR records in this session. calculate the size of this image w.r.t to screen
					 * size*/
					mImageFoundInHtmlOrCss = true;
				}
			}	
		}
		return htmlImageLst;
	}
	
	/**
	 * This method extracts only numeric value from string.
	 * 
	 * @return String with numeric value 
	 */
	String extractNumericValue(String str) {
		StringBuilder sBuilder = new StringBuilder("");
	    for (int i = 0; i<str.length(); i++) {
	        Character character = str.charAt(i);
	        if (Character.isDigit(character) || character == '.') {
	        	sBuilder.append(character);
	        }
	    }
		return sBuilder.toString();
	}
	
	/**
	 * This method compares the downloaded image size with standard image size (present in HTML/CSS or device screen size)
	 * 
	 * @return true if the height or width of downloaded image >= 110% of Standard Image Size else false
	 */
	private boolean compareDownloadedImgSizeWithStdImageSize(HttpRequestResponseInfo reqRessInfo, HtmlImage htmlImage, 
			int deviceScreenSizeRangeX,int deviceScreenSizeRangeY, Session session) {
		byte[] content = null;
		try {
			content = reqhelper.getContent(reqRessInfo, session);
		} catch (Exception e) {
			log.error("Failed to get content from HttpRequestResponseInfo: "+ e.getMessage());
		}
		if (content != null) {
			int widthRange = deviceScreenSizeRangeX;
			int heightRange = deviceScreenSizeRangeY;
			if (htmlImage != null) {
				widthRange = (htmlImage.getWidth() * 110) / 100;
				heightRange = (htmlImage.getHeight() * 110) / 100;			
			}
			
			ImageIcon downloadedImg = new ImageIcon(content);
			if ((downloadedImg != null)&&
					(downloadedImg.getIconWidth() >= widthRange || downloadedImg.getIconHeight() >= heightRange)){
				return true;					
			}
		}
		return false;
	}
	

}//end class
