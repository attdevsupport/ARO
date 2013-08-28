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
package com.att.aro.bp.minification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;
import com.att.aro.util.Util;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * Represents minification analysis.
 * 
 */
public class MinificationAnalysis {

	private static final Logger LOGGER = Logger
			.getLogger(MinificationAnalysis.class.getName());

	private static final String ORIG_FILE_NAME = "minifyOrig.aro";
	private static final String MINI_FILE_NAME = "minify.aro";
	private static final int MIN_FILE_COMPRESSION = 5;
	private static final float HUNDRED_PERCENT = 100F;
	private static final int NO_LINE_BREAK = -1;

	private static final boolean IS_JAVA_SCRIPT = true;
	private static final boolean IS_CSS = false;

	private File tmpOriginalFile;
	private File tmpMinifiedFile;
	private HtmlCompressor htmlCompressor;
	private List<MinificationEntry> analysisResults = new ArrayList<MinificationEntry>();
	private long totalSavingsInKb = 0L;

	/**
	 * Performs minification analysis.
	 * 
	 * @param TCP
	 *            session
	 * 
	 */
	public MinificationAnalysis(List<TCPSession> tcpSessions) {
		if (null != tcpSessions) {
			createTmpFileObjects();
			initHtmlCompressor();
			analyzeTcpSessions(tcpSessions);
			deleteTempFiles();
		}
	}

	public long getTotalSavingsInKb() {
		return totalSavingsInKb;
	}

	private void createTmpFileObjects() {
		tmpMinifiedFile = new File(Util.TEMP_DIR, MINI_FILE_NAME);
		tmpOriginalFile = new File(Util.TEMP_DIR, ORIG_FILE_NAME);
	}

	private void initHtmlCompressor() {
		htmlCompressor = new HtmlCompressor();
		htmlCompressor.setRemoveIntertagSpaces(true); // removes iter-tag
														// whitespace characters
		htmlCompressor.setRemoveQuotes(true); // removes unnecessary tag
												// attribute quotes
		htmlCompressor.setSimpleDoctype(true); // simplify existing doctype
		htmlCompressor.setRemoveScriptAttributes(true); // remove optional
														// attributes from
														// script tags
		htmlCompressor.setRemoveStyleAttributes(true); // remove optional
														// attributes from style
														// tags
		htmlCompressor.setRemoveLinkAttributes(true); // remove optional
														// attributes from link
														// tags
		htmlCompressor.setRemoveFormAttributes(true); // remove optional
														// attributes from form
														// tags
		htmlCompressor.setRemoveInputAttributes(true); // remove optional
														// attributes from input
														// tags
		htmlCompressor.setSimpleBooleanAttributes(true); // remove values from
															// boolean tag
															// attributes
		htmlCompressor.setRemoveJavaScriptProtocol(true); // remove
															// "javascript:"
															// from inline event
															// handlers
		htmlCompressor.setRemoveHttpProtocol(true); // replace "http://" with
													// "//" inside tag
													// attributes
		htmlCompressor.setRemoveHttpsProtocol(true); // replace "https://" with
														// "//" inside tag
														// attributes
		htmlCompressor.setPreserveLineBreaks(false); // preserves original line
														// breaks
		htmlCompressor.setRemoveSurroundingSpaces("br,p"); // remove spaces
															// around provided
															// tags
		htmlCompressor.setCompressCss(true); // compress inline css
		htmlCompressor.setCompressJavaScript(true); // compress inline
													// javascript
		htmlCompressor.setYuiCssLineBreak(NO_LINE_BREAK); // --line-break param
															// for Yahoo YUI
															// Compressor
		htmlCompressor.setYuiJsDisableOptimizations(false); // --disable-optimizations
															// param for Yahoo
															// YUI Compressor
		htmlCompressor.setYuiJsLineBreak(NO_LINE_BREAK); // --line-break param
															// for Yahoo YUI
															// Compressor
		htmlCompressor.setYuiJsNoMunge(true); // --nomunge param for Yahoo YUI
												// Compressor
		htmlCompressor.setYuiJsPreserveAllSemiColons(true); // --preserve-semi
															// param for Yahoo
															// YUI Compressor
	}

	private void analyzeTcpSessions(List<TCPSession> tcpSessions) {
		String contentType;
		// loop through TCP session
		for (TCPSession tcpSession : tcpSessions) {
			// loop through HTTP requests and responses
			for (HttpRequestResponseInfo rr : tcpSession
					.getRequestResponseInfo()) {

				contentType = rr.getContentType();
				if ((rr.getDirection() == Direction.RESPONSE)
						&& (rr.getContentLength() != 0)
						&& (contentType != null)) {

					try {
						analyzeContent(rr, contentType);
					} catch (Exception e) {
						LOGGER.log(
								Level.WARNING,
								"MinificationAnalysis - Unexpected Exception {0}",
								e.getMessage());
					}
				}
			}
		}
	}

	private void analyzeContent(HttpRequestResponseInfo rr, String contentType)
			throws Exception {

		if (HttpRequestResponseInfo.isJavaScript(contentType)) {
			analyzeJavaScript(rr);

		} else if (HttpRequestResponseInfo.isCss(contentType)) {
			analyzeCss(rr);

		} else if (HttpRequestResponseInfo.isHtml(contentType)) {
			analyzeHtml(rr);
		}
	}

	private void analyzeJavaScript(HttpRequestResponseInfo rr) throws Exception {
		savePayloadToTmpFile(rr);
		runJavaScriptMinify(IS_JAVA_SCRIPT);
		evaluateMinificationSavings(rr, getMinificationFileSizeSaving(),tmpOriginalFile.length()-tmpMinifiedFile.length());
	}

	private void analyzeCss(HttpRequestResponseInfo rr) throws Exception {
		savePayloadToTmpFile(rr);
		runJavaScriptMinify(IS_CSS);
		evaluateMinificationSavings(rr, getMinificationFileSizeSaving(),tmpOriginalFile.length()-tmpMinifiedFile.length());
	}

	private void analyzeHtml(HttpRequestResponseInfo rr) throws IOException, ContentException {
		htmlMinificationResult result = runHtmlMinify(rr); 
		evaluateMinificationSavings(rr, getMinificationFileSizeSaving(result.originalSize, result.minifiedSize),new Long(result.originalSize-result.minifiedSize));
	}

	private void savePayloadToTmpFile(HttpRequestResponseInfo rr)
			throws IOException {
		rr.saveContentToFile(tmpOriginalFile);
	}

	private void runJavaScriptMinify(boolean isJavaScript) throws IOException {
		Reader in = null;
		Writer out = null;
		JsMinificationOptions o = new JsMinificationOptions();
		try {
			in = new InputStreamReader(new FileInputStream(tmpOriginalFile),
					o.charset);
			out = new OutputStreamWriter(new FileOutputStream(tmpMinifiedFile),
					o.charset);
			if (isJavaScript) {
				JavaScriptCompressor compressor = new JavaScriptCompressor(in,
						new YuiCompressorErrorReporter());
				compressor.compress(out, o.lineBreakPos, o.munge, o.verbose,
						o.preserveAllSemiColons, o.disableOptimizations);

			} else {
				CssCompressor compressor = new CssCompressor(in);
				compressor.compress(out, o.lineBreakPos);
			}
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					"Error from JavaScript minifiaction: {0}", e.getMessage());
			throw (e);
		} catch (EvaluatorException e) {
			LOGGER.log(Level.WARNING,
					"Error from JavaScript minifiaction: {0}", e.getMessage());
			throw (e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING,
						"Error from JavaScript minifiaction: {0}",
						e.getMessage());
				throw (e);
			}
		}
	}

	private htmlMinificationResult runHtmlMinify(HttpRequestResponseInfo rr)
			throws ContentException, IOException {

		htmlMinificationResult result = new htmlMinificationResult(0, 0);
		String htmlIn;
		try {
			htmlIn = rr.getContentString();
			if (htmlIn != null) {
				String compressedHtml = htmlCompressor.compress(htmlIn);
				result.originalSize = htmlIn.length();
				result.minifiedSize = compressedHtml.length();
			}
		} catch (ContentException e) {
			LOGGER.log(Level.WARNING, "Error from runHtmlMinify: {0}",
					e.getMessage());
			throw (e);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Error from runHtmlMinify: {0}",
					e.getMessage());
			throw (e);
		}
		return result;
	}

	private void evaluateMinificationSavings(HttpRequestResponseInfo rr, int saving,Long savingzinKb) {
		if(saving > MIN_FILE_COMPRESSION) {
			this.analysisResults.add(new MinificationEntry(rr, saving,savingzinKb.intValue()));
			LOGGER.log(Level.FINE, "minify the file");
		} else {
			totalSavingsInKb-=savingzinKb;
			LOGGER.log(Level.FINE, "the file was minified");
		}
	}

	private int getMinificationFileSizeSaving() {
		long origSize = tmpOriginalFile.length();
		long miniSize = tmpMinifiedFile.length();
		return getMinificationFileSizeSaving(origSize, miniSize);
	}

	private int getMinificationFileSizeSaving(long origSize, long miniSize) {
		totalSavingsInKb+=(origSize-miniSize);
		float saving = HUNDRED_PERCENT - (miniSize * HUNDRED_PERCENT / origSize);
		LOGGER.log(Level.FINE, "original size: {0}, mini size: {1}, saving: {2}", new Object[] {origSize, miniSize, saving});
		return Math.round(saving);
	}

	private void deleteTempFiles() {

		if (tmpMinifiedFile.exists() && !tmpMinifiedFile.delete()) {
			LOGGER.log(Level.SEVERE, "Cannot delete minified tmp file.");
		}
		if (tmpOriginalFile.exists() && !tmpOriginalFile.delete()) {
			LOGGER.log(Level.SEVERE, "Cannot delete original tmp file.");
		}
	}

	/**
	 * Indicates whether the test has passed or failed.
	 * 
	 * @return true if test passes, otherwise false is returned
	 */
	public boolean isTestPassed() {
		return (analysisResults.size() == 0);
	}

	/**
	 * Returns the number of files failing the test.
	 * 
	 * @return the number of files failing the test
	 */
	public int getNumberOfMinifyFiles() {
		return this.analysisResults.size();
	}

	/**
	 * Returns results of the test.
	 * 
	 * @return results of the test
	 */
	public List<MinificationEntry> getResults() {
		return this.analysisResults;
	}

	private static class htmlMinificationResult {
		public htmlMinificationResult(int originalSize, int minifiedSize) {
			this.originalSize = originalSize;
			this.minifiedSize = minifiedSize;
		}

		int originalSize;
		int minifiedSize;
	}

	private static class JsMinificationOptions {
		String charset = "UTF-8";
		// do not split long lines
		int lineBreakPos = NO_LINE_BREAK;
		boolean munge = false;
		boolean verbose = false;
		boolean preserveAllSemiColons = false;
		boolean disableOptimizations = false;
	}

	private static class YuiCompressorErrorReporter implements ErrorReporter {

		@Override
		public void warning(String message, String sourceName, int line,
				String lineSource, int lineOffset) {
			if (line < 0) {
				LOGGER.log(Level.FINE, message);
			} else {
				LOGGER.log(Level.FINE, line + ':' + lineOffset + ':' + message);
			}
		}

		@Override
		public void error(String message, String sourceName, int line,
				String lineSource, int lineOffset) {
			if (line < 0) {
				LOGGER.log(Level.FINE, message);
			} else {
				LOGGER.log(Level.FINE, line + ':' + lineOffset + ':' + message);
			}
		}

		@Override
		public EvaluatorException runtimeError(String message,
				String sourceName, int line, String lineSource, int lineOffset) {
			error(message, sourceName, line, lineSource, lineOffset);
			return new EvaluatorException(message);
		}

	}

}
