package com.att.aro.bp.flash;

import java.io.IOException;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;

public class FlashAnalysis {
	
	private HttpRequestResponseInfo firstFlash = null;
	private int numberOfFlash = 0;

	/**
	 * Performs Flash Analysis.
	 * 
	 * @param tcpSessions
	 *            TCP session
	 * 
	 */
	public FlashAnalysis(List<TCPSession> tcpSessions) {
		if (null != tcpSessions) {
			analyzeTcpSessions(tcpSessions);
		}
	}

	private void analyzeTcpSessions(List<TCPSession> tcpSessions) {
		// loop through TCP session
		for (TCPSession tcpSession : tcpSessions) {
			// loop through HTTP requests and responses
			for (HttpRequestResponseInfo reqRessInfo : tcpSession.getRequestResponseInfo()) {
				if (reqRessInfo.getDirection() == Direction.RESPONSE 
					&& reqRessInfo.getContentType() != null 
					&& reqRessInfo.getContentLength() > 0) {
						if (reqRessInfo.getContentType().equalsIgnoreCase("application/x-shockwave-flash")
							|| reqRessInfo.getContentType().equalsIgnoreCase("video/x-flv")) {
						numberOfFlash++;
						if (this.firstFlash == null) {
							this.firstFlash = reqRessInfo;
						}
					} else {
						String contentType = reqRessInfo.getContentType();
						if (contentType.equalsIgnoreCase("text/css") || contentType.equalsIgnoreCase("text/html")) {
							checkEmbeddedFlashInHTMLOrCSS(reqRessInfo);
						}
					}
				}
			}
		}
	}

	/**
	 * Checks embedded Flash in HTML Or CSS content.
	*/
	private void checkEmbeddedFlashInHTMLOrCSS(HttpRequestResponseInfo reqRessInfo) {
		String flashDownloaded = null;
		try {
			flashDownloaded = reqRessInfo.getContentString();
		} catch (ContentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (flashDownloaded != null) {
			Document doc = Jsoup.parse(flashDownloaded);
			
			//Parsing "embed" in HTML or CSS
			Elements srcsEmbed = doc.select("embed");
			for (Element src : srcsEmbed) {
				if (src.tagName().toLowerCase().equals("embed")) {
					 if ((src.attr("src")).toLowerCase().contains(".swf")) {
						 checkFlashExistenceInHTTPObject(reqRessInfo);
					 }
				 }
			}
			
			//Parsing "object" in HTML or CSS
			Elements srcObject = doc.select("object");
			for (Element src : srcObject) {
				if (src.tagName().toLowerCase().equals("object")) {
					 if ((src.attr("data")).toLowerCase().contains(".swf")) {
						 checkFlashExistenceInHTTPObject(reqRessInfo);
					 }
				 }
			}
		}
	}
	
	/**
	 * Checks Flash existence in associated HTTP Object.
	*/
	private void checkFlashExistenceInHTTPObject(HttpRequestResponseInfo reqRessInfo) {
		HttpRequestResponseInfo assocReqResp = reqRessInfo.getAssocReqResp();
		if (assocReqResp != null) {
			String flashToSearchFor = assocReqResp.getObjName();
			if (flashToSearchFor.toLowerCase().contains(".swf")) {
				this.numberOfFlash++;
				 if (this.firstFlash == null) {
					 this.firstFlash = reqRessInfo;
				 }
			}
		}
	}
	
	/**
	 * Indicates whether the test has passed or failed.
	 * 
	 * @return true if test passes, otherwise false is returned
	 */
	public boolean isTestPassed() {
		return (this.firstFlash == null);
	}

	/**
	 * Returns the number of files failing the test.
	 * 
	 * @return the number of files failing the test
	 */
	public int getNumberOfFlashlFiles() {
		return this.numberOfFlash;
	}

	/**
	 * Returns the first flash ReqResInfo.
	 * 
	 * @return the first flash ReqResInfo
	 */
	public HttpRequestResponseInfo getFirstFlashReqResInfo() {
		return this.firstFlash;
	}
}
