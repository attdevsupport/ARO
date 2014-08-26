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
package com.att.aro.bp.scripts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
 * Represents 3rd party scripts analysis.
 * 
 */
public class ScriptsAnalysis {

	private static final Logger LOGGER = Logger.getLogger(ScriptsAnalysis.class.getName());

	private static final int MIN_NUM_OF_SCRIPTS_IN_HTML_DOC = 2;

	private HttpRequestResponseInfo firstFailedHtml;
	private int numberOfFailedFiles;

	/**
	 * Performs 3rd party scripts analysis.
	 * 
	 * @param tcpSessions
	 *            TCP session
	 * 
	 */
	public ScriptsAnalysis(List<TCPSession> tcpSessions) {
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
				if ((rr.getDirection() == Direction.RESPONSE) && (rr.getContentLength() != 0) && (contentType != null)
						&& (HttpRequestResponseInfo.isHtml(contentType))) {

					analyzeHtml(rr);
				}
			}
		}
	}

	private void analyzeHtml(HttpRequestResponseInfo rr) {

		Document htmlDoc;
		try {
			htmlDoc = Jsoup.parse(rr.getContentString());

			Elements allSrcElements = new Elements(htmlDoc.select("script"));

			if (allSrcElements.size() >= ScriptsAnalysis.MIN_NUM_OF_SCRIPTS_IN_HTML_DOC) {
				is3rdPartyScript(rr, allSrcElements);
			}

		} catch (ContentException e) {
			ScriptsAnalysis.LOGGER.log(Level.FINE, "Empty URL Analysis - Unexpected Exception {0}", e.getMessage());
		} catch (IOException e) {
			ScriptsAnalysis.LOGGER.log(Level.WARNING, "Empty URL Analysis - Unexpected Exception {0}", e.getMessage());
		}
	}

	private boolean is3rdPartyScript(HttpRequestResponseInfo rr, Elements elements) {

		String originDomain = getOriginDomain(rr);
		ScriptsAnalysis.LOGGER.log(Level.FINE, "Orig. domain: {0}", originDomain);

		List<String> domains = new ArrayList<String>();
		getScriptDomains(elements, domains);

		if (isMultiple3rdPartyDomains(originDomain, domains)) {
			// store the 1st occurrence
			if (firstFailedHtml == null) {
				firstFailedHtml = rr;
				ScriptsAnalysis.LOGGER.log(Level.FINE, "found 1st HTML: {0}", rr.getRequestResponseText());
			}
			numberOfFailedFiles++;
			return true;
		}

		return false;

	}

	private String getOriginDomain(HttpRequestResponseInfo rr) {

		HttpRequestResponseInfo rsp = rr.getAssocReqResp();
		return (rsp != null) ? rsp.getHostName() : "";
	}

	private void getScriptDomains(Elements elements, List<String> domains) {
		String scriptDomain;
		for (Element element : elements) {
			if (element.hasAttr("src") && !element.attr("src").isEmpty()) {
				scriptDomain = getDomain(element);
				if (!scriptDomain.isEmpty()) {
					domains.add(scriptDomain);
				}
			}
		}
	}

	private String getDomain(Element element) {

		String url = element.attr("abs:src");
		if (!url.isEmpty()) {
			try {
				url = new URL(url).getHost();
			} catch (MalformedURLException e) {
				ScriptsAnalysis.LOGGER.log(Level.WARNING, "getDomain(), MalformedURLException, URL: {0}", url);
			}
		}
		ScriptsAnalysis.LOGGER.log(Level.FINE, "JS script domain: >{0}< / {1}", new Object[] { url, element.toString() });

		return url;
	}

	private boolean isMultiple3rdPartyDomains(String originDomain, List<String> domains) {

		int counter = 0;
		for (String domain : domains) {
			if (!domain.equals(originDomain) && (++counter >= ScriptsAnalysis.MIN_NUM_OF_SCRIPTS_IN_HTML_DOC)) {
				LOGGER.log(Level.FINE, "******** found it *****************");
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
		return (firstFailedHtml == null);
	}

	/**
	 * Returns the number of files failing the test.
	 * 
	 * @return the number of files failing the test
	 */
	public int getNumberOfFiles() {
		return numberOfFailedFiles;
	}

	/**
	 * Returns the first failed HTML response.
	 * 
	 * @return the first failed HTML response
	 */
	public HttpRequestResponseInfo getFirstFailedHtml() {
		return firstFailedHtml;
	}

}
