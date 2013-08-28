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
package com.att.aro.bp.emptyurl;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;

/**
 * Represents empty URL analysis.
 * 
 */
public class EmptyUrlAnalysis {

	private static final Logger LOGGER = Logger.getLogger(EmptyUrlAnalysis.class.getName());

	private HttpRequestResponseInfo firstFailedHtml;
	private int numberOfFailedFiles;

	/**
	 * Performs empty URL analysis.
	 * 
	 * @param tcpSessions
	 *            TCP session
	 * 
	 */
	public EmptyUrlAnalysis(List<TCPSession> tcpSessions) {
		if (null != tcpSessions) {
			analyzeTcpSessions(tcpSessions);
		}
	}

	private void analyzeTcpSessions(List<TCPSession> tcpSessions) {
		String contentType;
		// loop through TCP session
		for (TCPSession tcpSession : tcpSessions) {
			// loop through HTTP requests and responses
			for (HttpRequestResponseInfo rr : tcpSession.getRequestResponseInfo()) {

				contentType = rr.getContentType();
				if ((rr.getDirection() == Direction.RESPONSE) && (rr.getContentLength() != 0) && (contentType != null)) {
					analyzeContent(rr, contentType);
				}
			}
		}
	}

	private void analyzeContent(HttpRequestResponseInfo rr, String contentType) {
		if (HttpRequestResponseInfo.isHtml(contentType)) {
			analyzeHtml(rr);
		}
	}

	private void analyzeHtml(HttpRequestResponseInfo rr) {

		Document htmlDoc;
		try {
			htmlDoc = Jsoup.parse(rr.getContentString());

			Elements allHrefElements = new Elements();
			allHrefElements.addAll(htmlDoc.select("a"));
			allHrefElements.addAll(htmlDoc.select("link"));

			Elements allSrcElements = new Elements();
			allSrcElements.addAll(htmlDoc.select("iframe"));
			allSrcElements.addAll(htmlDoc.select("img"));
			allSrcElements.addAll(htmlDoc.select("script"));

			if (isAttributeEmpty(rr, allHrefElements, "href")) {
				return;
			}

			if (isAttributeEmpty(rr, allSrcElements, "src")) {
				return;
			}

		} catch (ContentException e) {
			LOGGER.log(Level.WARNING, "Empty URL Analysis - Unexpected Exception {0}", e.getMessage());
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Empty URL Analysis - Unexpected Exception {0}", e.getMessage());
		}
	}

	private boolean isAttributeEmpty(HttpRequestResponseInfo rr, Elements elements, String attribute) {
		for (Element element : elements) {
			if (element.hasAttr(attribute) && element.attr(attribute).isEmpty()) {
				// store the 1st occurrence
				if (this.firstFailedHtml == null) {
					this.firstFailedHtml = rr;
					LOGGER.log(Level.FINE, "found 1st HTML: {0}", rr.getRequestResponseText());
				}
				this.numberOfFailedFiles++;
				LOGGER.log(Level.FINE, "element: {0}", element.toString());
				return true;
			}
		}
		return false;
	}

	/**
	 * Indicates whether the test has passed or failed.
	 * 
	 * @return true if test passes, otherwise false is returned
	 */
	public boolean isTestPassed() {
		return (this.firstFailedHtml == null);
	}

	/**
	 * Returns the number of files failing the test.
	 * 
	 * @return the number of files failing the test
	 */
	public int getNumberOfEmptyUrlFiles() {
		return this.numberOfFailedFiles;
	}

	/**
	 * Returns the first failed HTML response.
	 * 
	 * @return the first failed HTML response
	 */
	public HttpRequestResponseInfo getFirstFailedHtml() {
		return this.firstFailedHtml;
	}

}
