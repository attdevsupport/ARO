/*
 *  Copyright 2013 AT&T
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
package com.att.aro.bp.imageSize;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import com.att.aro.bp.spriteimage.SpriteImageEntry;
import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.PacketInfo;
import com.att.aro.model.TCPSession;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.util.Util;

/**
 * Represents image size analysis.
 * 
 */
public class ImageSizeAnalysis {

	private static final Logger LOGGER = Logger.getLogger(ImageSizeAnalysis.class.getName());
	private static final ResourceBundle RB = Util.RB;

	// results of the analysis
	private List<ImageSizeEntry> results = new ArrayList<ImageSizeEntry>();
	boolean m_ImageFoundInHtmlOrCss = false;
	private int deviceScreenSizeRangeX = 0;
	private int deviceScreenSizeRangeY = 0;
	
	/**
	 * Performs image size analysis.
	 * 
	 * @param tcpSessions
	 *            - TCP sessions to be analyzed.
	 */
	public ImageSizeAnalysis(List<TCPSession> tcpSessions, int deviceScreenSizeX, int deviceScreenSizeY) {
		deviceScreenSizeRangeX = (deviceScreenSizeX * 150) / 100;
		deviceScreenSizeRangeY = (deviceScreenSizeY * 150) / 100;

		if (null != tcpSessions) {
			// loop through TCP session
			for (TCPSession tcpSession : tcpSessions) {
				
				// loop through HTTP requests and responses
				for (HttpRequestResponseInfo reqRessInfo : tcpSession.getRequestResponseInfo()) {
					if (reqRessInfo.getDirection() == Direction.RESPONSE 
							&& reqRessInfo.getContentType() != null 
							&& reqRessInfo.getContentType().contains("image/")) {
						boolean isBigSize = false;
						List<HtmlImage> htmlImageLst = checkThisImageInAllHTMLOrCSS(tcpSession, reqRessInfo);
						if (m_ImageFoundInHtmlOrCss) {
							m_ImageFoundInHtmlOrCss = false;
							int size = htmlImageLst.size();
							if (size > 0) {
								for(int index=0; index<size; index++) {
									HtmlImage htmlImage = htmlImageLst.get(index);
									isBigSize = compareDownloadedImgSizeWithStdImageSize(reqRessInfo, htmlImage);									
									if (isBigSize) {
										break;
									}
								}
							} else {
								isBigSize = compareDownloadedImgSizeWithStdImageSize(reqRessInfo, null);
							}
							if (isBigSize) {
								this.results.add(new ImageSizeEntry(reqRessInfo));
							}	
						}
					}
				}
			}
		}
	}

	/**
	 * This method checks the existence of specific image in all HTML Or CSS files.
	 * 
	 * @return List of HtmlImage
	 */
	private List<HtmlImage> checkThisImageInAllHTMLOrCSS(TCPSession tcpSession, HttpRequestResponseInfo reqRessInfo) {
		ArrayList<HtmlImage> htmlImageLst = new ArrayList<HtmlImage>();
		for (HttpRequestResponseInfo rr : tcpSession.getRequestResponseInfo()) {
			if (rr.getDirection() == Direction.RESPONSE && rr.getContentType() != null) {
				String contentType = rr.getContentType();
				if (contentType.equalsIgnoreCase("text/css") || contentType.equalsIgnoreCase("text/html")) {
					HttpRequestResponseInfo assocReqResp = reqRessInfo.getAssocReqResp();
					if (assocReqResp != null) {
						String imageToSearchFor = assocReqResp.getObjName();
						String imageDownloaded = null;
						try {
							imageDownloaded = rr.getContentString();
						} catch (ContentException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (imageToSearchFor != null && imageDownloaded != null) {
							if (imageDownloaded.toLowerCase().contains(imageToSearchFor.toLowerCase())) {
								Document doc = Jsoup.parse(imageDownloaded);
								Elements images = doc.select("[src]");
								for (Element src : images) {
									 if (src.tagName().equals("img")) {
										 if ((src.attr("abs:src")).contains(imageToSearchFor)) {
											 m_ImageFoundInHtmlOrCss = true;
											 
											 /* Get width and height from HTML or CSS*/
											 String width = src.attr("width");
											 String height = src.attr("height");
											  if (!width.isEmpty() && !height.isEmpty()) {
												  int iWidth = Integer.parseInt(width);
												  int iHeight = Integer.parseInt(height);
												  htmlImageLst.add(new HtmlImage(iWidth, iHeight));
											  }
										 }
									 }
								}
							}							
						}
					}
				} 
			}			
		}
		return htmlImageLst;
	}
	
	/**
	 * This method compares the downloaded image size with standard image size (present in HTML/CSS or device screen size)
	 * 
	 * @return true if the height or width of downloaded image >= 150% of Standard Image Size else false
	 */
	private boolean compareDownloadedImgSizeWithStdImageSize(HttpRequestResponseInfo reqRessInfo, HtmlImage htmlImage) {
		try {
			if (reqRessInfo.getContent() != null) {
				int widthRange = deviceScreenSizeRangeX;
				int heightRange = deviceScreenSizeRangeY;
				if (htmlImage != null) {
					widthRange = (htmlImage.getWidth() * 150) / 100;
					heightRange = (htmlImage.getHeight() * 150) / 100;			
				}
				
				ImageIcon downloadedImg = new ImageIcon(reqRessInfo.getContent());
				if (downloadedImg != null) {
					if (downloadedImg.getIconWidth() >= widthRange || downloadedImg.getIconHeight() >= heightRange) {
						return true;					
					}
				}
			}
		} catch (ContentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Indicates whether the test passed or failed.
	 * 
	 * @return true if the test passed
	 */
	public boolean isTestPassed() {
		return (this.results.size() == 0);
	}

	/**
	 * Returns a list of best practice results.
	 * 
	 * @return the results
	 */
	public List<ImageSizeEntry> getResults() {
		return this.results;
	}

	/**
	 * Returns number of images failing the test.
	 * 
	 * @return number of images
	 */
	public int getNumberOfImageFiles() {
		return this.results.size();
	}
}
