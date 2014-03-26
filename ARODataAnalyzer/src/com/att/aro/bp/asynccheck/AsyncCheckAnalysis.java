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
package com.att.aro.bp.asynccheck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;

/**
 * Represents Asynchronous script load Analysis
 * 
 */
public class AsyncCheckAnalysis {

	private static final Logger LOGGER = Logger
			.getLogger(AsyncCheckAnalysis.class.getName());

	private List<AsyncCheckEntry> results = new ArrayList<AsyncCheckEntry>();
	private int asyncLoadedScripts = 0;
	private int syncLoadedScripts = 0;
	private int syncPacketCount = 0;
	private int asyncPacketCount = 0;

	/**
	 * Performs Async Check Analysis
	 * 
	 * @param tcpSessions
	 *            - TCP sessions to be analyzed.
	 */
	public AsyncCheckAnalysis(List<TCPSession> tcpSessions) {
		for (TCPSession tcpSession : tcpSessions) {
			for (HttpRequestResponseInfo rr : tcpSession
					.getRequestResponseInfo()) {
				if (rr.getDirection() == Direction.RESPONSE) {
					if (!rr.checkAsyncAttributeInHead(this)) {
						results.add(new AsyncCheckEntry(rr));
					}
				}
			}
		}
	}

	/**
	 * Performs parsing of the html file to find the scripts are loaded
	 * asynchronously or not
	 * 
	 * @param HtppRequestResponseInfo
	 *            info - Http packet to be parsed.
	 */
	public boolean parseHtmlToFindSyncLoadingScripts(
			HttpRequestResponseInfo info) {

		org.jsoup.nodes.Document doc;
		String packetContent = null;
		try {
			packetContent = info.getContentString();
		} catch (ContentException e1) {
			LOGGER.log(Level.FINE,
					"Content Exception while getting the content from the packet");
		} catch (IOException e1) {
			LOGGER.log(Level.FINE,
					"IOException while getting the content from the packet");
		}

		if (packetContent != null) {
			doc = Jsoup.parse(packetContent);
			Elements headLink = null;
			Elements scriptLink = null;

			try {
				headLink = doc.select("head");
				scriptLink = headLink.select("script");
				for (Element element : scriptLink) {
					if (element.hasAttr("src")) {
						if ((element.hasAttr("async"))
								|| element.hasAttr("defer")) {
							incrementAsyncLoadedScripts();
						} else {
							incrementSyncLoadedScripts();
						}
					} else {
						LOGGER.log(Level.FINE,
								"The <SCRIPT> element doesn't download any javascript files");
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.FINE, "Exception while parsing the html file");
			}
		}

		if (syncLoadedScripts > 0) {
			incrementSyncPacketCount();
			syncLoadedScripts = 0;
			return false;
		} else if (asyncLoadedScripts > 0) {
			incrementAsyncPacketCount();
			asyncLoadedScripts = 0;
			return true;
		} else {
			return true;
		}
	}

	/**
	 * Increments the Sync packet count
	 * 
	 */
	public void incrementSyncPacketCount() {
		this.syncPacketCount++;
	}

	/**
	 * Returns the Sync packet count
	 * 
	 */
	public int getSyncPacketCount() {
		return syncPacketCount;
	}

	/**
	 * Increments the async packet count
	 * 
	 */
	public void incrementAsyncPacketCount() {
		this.asyncPacketCount++;
	}

	/**
	 * Returns the async packet count
	 * 
	 */
	public int getAsyncPacketCount() {
		return asyncPacketCount;
	}

	/**
	 * Increments the Async loaded scripts
	 * 
	 */
	public void incrementAsyncLoadedScripts() {
		this.asyncLoadedScripts++;
	}

	/**
	 * Increments the sync loaded scripts
	 * 
	 */
	public void incrementSyncLoadedScripts() {
		this.syncLoadedScripts++;
	}

	/**
	 * Returns the Sync loaded scripts
	 * 
	 */
	public int getSyncLoadedScripts() {
		return syncLoadedScripts;
	}

	/**
	 * Returns the Async loaded scripts
	 * 
	 */
	public int getAsyncLoadedScripts() {
		return asyncLoadedScripts;
	}

	/**
	 * Returns the total loaded scripts
	 * 
	 */
	public int getTotalLoadedScripts() {
		return syncLoadedScripts + asyncLoadedScripts;
	}

	/**
	 * Returns an indicator whether the async script loading test has failed or
	 * not.
	 * 
	 * @return failed/success test indicator
	 */
	public boolean isTestFailed() {
		return (getSyncPacketCount() > 0);
	}

	/**
	 * Returns a list of async loaded files.
	 * 
	 * @return the results
	 */
	public List<AsyncCheckEntry> getResults() {
		return results;
	}

}
