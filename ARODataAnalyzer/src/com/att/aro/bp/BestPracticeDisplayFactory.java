/*
 * Copyright 2012 AT&T
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
package com.att.aro.bp;

import java.net.URI;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import com.att.aro.bp.asynccheck.AsyncCheckResultPanel;
import com.att.aro.bp.asynccheck.BPAsyncCheckInScript;
import com.att.aro.bp.displaynoneincss.DisplayNoneInCSSBestPractice;
import com.att.aro.bp.displaynoneincss.DisplayNoneInCSSResultPanel;
import com.att.aro.bp.duplicate.DuplicateBestPractice;
import com.att.aro.bp.duplicate.DuplicateResultPanel;
import com.att.aro.bp.emptyurl.EmptyUrlBestPractice;
import com.att.aro.bp.fileorder.FileOrderBestPractice;
import com.att.aro.bp.fileorder.FileOrderResultPanel;
import com.att.aro.bp.flash.FlashBestPractice;
import com.att.aro.bp.httprspcd.Http3xxBestPractice;
import com.att.aro.bp.http4xx5xxrespcodes.Http4xx5xxBestPractice;
import com.att.aro.bp.http4xx5xxrespcodes.Http4xx5xxStatusResponseCodesResultPanel;
import com.att.aro.bp.httprspcd.HttpCode3XXResultPanel;
import com.att.aro.bp.imageSize.ImageSizeBestPractice;
import com.att.aro.bp.imageSize.ImageSizeResultPanel;
import com.att.aro.bp.minification.MinificationBestPractice;
import com.att.aro.bp.minification.MinificationResultPanel;
import com.att.aro.bp.scripts.Scripts;
//import com.att.aro.bp.smallrequest.SmallRequestBestPractice;
//import com.att.aro.bp.smallrequest.SmallRequestResultPanel;
import com.att.aro.bp.spriteimage.SpriteImageBestPractice;
import com.att.aro.bp.spriteimage.SpriteImageResultPanel;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.ChartPlotOptions;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.main.TextFileCompressionResultPanel;
import com.att.aro.model.BestPractices;
import com.att.aro.model.BurstCollectionAnalysis;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;

/**
 * Singleton factory class that creates the default set of best practices to be
 * displayed in the ARO analyzer.  This class may be modified or extended to 
 * allow for the addition of custom best practices and best practice groups.
 */
public class BestPracticeDisplayFactory {

	/**
	 * Singleton instance
	 */
	private static final BestPracticeDisplayFactory instance = new BestPracticeDisplayFactory();

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private BestPracticeDisplayGroup fileDownloadSection;
	private BestPracticeDisplayGroup connectionsSection;
	private BestPracticeDisplayGroup otherSection;
	private BestPracticeDisplayGroup htmlSection;
	private Collection<BestPracticeDisplayGroup> bestPracticeDisplay;
	
	/**
	 * Returns singleton instance
	 * @return the instance
	 */
	public static BestPracticeDisplayFactory getInstance() {
		return instance;
	}

	/**
	 * Protected constructor allows for extensions
	 */
	protected BestPracticeDisplayFactory() {
		
	}

	/**
	 * Factory method that returns the best practice configuration
	 * @return Collection of best practice groups containing the best practices
	 * to be displayed in the ARO analyzer.
	 */
	public Collection<BestPracticeDisplayGroup> getBestPracticeDisplay() {
		if (bestPracticeDisplay == null) {
			this.bestPracticeDisplay = new ArrayList<BestPracticeDisplayGroup>(5);
			bestPracticeDisplay.add(getFileDownloadSection());
			bestPracticeDisplay.add(getConnectionsSection());
			bestPracticeDisplay.add(getHtmlSection());
			bestPracticeDisplay.add(getOtherSection());
		}
		return this.bestPracticeDisplay;
	}

	/**
	 * Returns the pre-defined file download best practice section
	 * @return The best practice display group for file download
	 */
	protected BestPracticeDisplayGroup getFileDownloadSection() {
		if (fileDownloadSection == null) {
			fileDownloadSection = new BestPracticeDisplayGroupImpl(
					rb.getString("bestPractices.header.fileDownload"),
					rb.getString("bestPractices.header.fileDownloadDescription"),
					rb.getString("bestPractice.referSection.fileDownload"),
					Arrays.asList(FILE_COMPRESSION, DUPLICATE_CONTENT,USING_CACHE, CACHE_CONTROL,
							PREFETCHING,COMBINE_CS_JSS,IMAGE_SIZE, MINIFICATION, SPRITEIMAGE/*, SMALLREQUEST*/));
		}
		return fileDownloadSection;
	}
	
	/** 
	 * Clears all BP results.
	 * 
	 */
	static public void clearsBPResultsTables() {
		
		getMinification().clearTable();
		getDupicate().clearTable();
		getDisplayNoneInCSSResultPanel().clearTable();
		getFileOrderResultPanel().clearTable();
		getHttp4xx5xxResults().clearTable();
		getHttpRspCdResults().clearTable();
		getImageSize().clearTable();
		getSpriteImageResults().clearTable();
		getAsyncCheckResults().clearTable();
		getTextFileCompression().clearTable();
	}
	
	/**
	 * Returns a results panel for duplicate content.
	 * 
	 * @return result panel
	 */	
	public static DuplicateResultPanel getDupicate() {
		return (DuplicateResultPanel) DUPLICATE_CONTENT.getTestResults();
	}
	
	/**
	 * Returns a result panel for text file compression test.
	 * 
	 * @return text file compression test result panel
	 */
	public static TextFileCompressionResultPanel getTextFileCompression() {
		return (TextFileCompressionResultPanel) FILE_COMPRESSION.getTestResults();
	}
	
	/**
	 * Returns a result panel for image size test.
	 * 
	 * @return result panel
	 */
	public static ImageSizeResultPanel getImageSize() {
		return (ImageSizeResultPanel) IMAGE_SIZE.getTestResults();
	}

	/**
	 * Returns a result panel for minification test.
	 * 
	 * @return result panel
	 */
	 public static MinificationResultPanel getMinification() {
		return (MinificationResultPanel) MINIFICATION.getTestResults();
	}
	
	/**
	 * Returns a result panel for Sprite Image test.
	 * 
	 * @return result panel
	 */
	public static SpriteImageResultPanel getSpriteImageResults() {
		return (SpriteImageResultPanel) SPRITEIMAGE.getTestResults();
	}
	
	//	/**
//	 * Returns a result panel for Small Request test.
//	 * 
//	 * @return result panel
//	 */
//	public static SmallRequestResultPanel getSmallRequestResults() {
//		return (SmallRequestResultPanel) SMALLREQUEST.getTestResults();
//	}
	
	/**
	 * Returns a result panel for HTTP response code test.
	 * 
	 * @return result panel
	 */
	public static HttpCode3XXResultPanel getHttpRspCdResults() {
		 return (HttpCode3XXResultPanel) HTTP_3XX_CODE.getTestResults();
	}
	
	/**
	 * Returns a result panel for HTTP 4xx/5xx response code test.
	 * 
	 * @return result panel
	 */
	public static Http4xx5xxStatusResponseCodesResultPanel getHttp4xx5xxResults() {
		 return (Http4xx5xxStatusResponseCodesResultPanel) HTTP_4XX_5XX.getTestResults();
	}
	/**
	 * Returns a result panel for asynchronous script downloading check test.
	 * 
	 * @return result panel
	 */
	public static AsyncCheckResultPanel getAsyncCheckResults(){
		return (AsyncCheckResultPanel)ASYNC_CHECK.getTestResults();
	}
	
	/**
	 * Returns a result panel for File order test
	 * 
	 * @return result panel
	 */
	public static FileOrderResultPanel getFileOrderResultPanel(){
		return (FileOrderResultPanel)FILE_ORDER.getTestResults();
	}
	
	/**
	 * Returns a result panel for Display None in CSS test
	 * 
	 * @return result panel
	 */
	public static DisplayNoneInCSSResultPanel getDisplayNoneInCSSResultPanel(){
		return (DisplayNoneInCSSResultPanel)DISPLAY_NONE_IN_CSS.getTestResults();
	}
	
	
	/**
	 * Returns the pre-defined connections best practice section
	 * @return The best practice display group for connections
	 */
	protected BestPracticeDisplayGroup getConnectionsSection() {
		if (connectionsSection == null) {
			connectionsSection = new BestPracticeDisplayGroupImpl(
					rb.getString("bestPractices.header.connections"),
					rb.getString("bestPractices.header.connectionsDescription"),
					rb.getString("bestPractice.referSection.connections"),
					Arrays.asList(CONNECTION_OPENING, UNNECESSARY_CONNECTIONS,
							PERIODIC_TRANSFER, SCREEN_ROTATION,
							CONNECTION_CLOSING, WIFI_OFFLOADING, HTTP_4XX_5XX, HTTP_3XX_CODE,
							SCRIPTS_URL));
		}
		return connectionsSection;
	}

	/**
	 * Returns the pre-defined other best practice section
	 * @return The best practice display group for html
	 */
	protected BestPracticeDisplayGroup getHtmlSection() {
		if (htmlSection == null) {
			htmlSection = new BestPracticeDisplayGroupImpl(
					rb.getString("bestPractices.header.html"),
					rb.getString("bestPractices.header.htmlDescription"),
					rb.getString("bestPractice.referSection.html"),
					Arrays.asList(ASYNC_CHECK, HTTP_1_0_USAGE, FILE_ORDER, EMPTY_URL, FLASH, DISPLAY_NONE_IN_CSS));
		}
		return htmlSection;
	}
	
	/**
	 * Returns the pre-defined other best practice section
	 * @return The best practice display group for other
	 */
	protected BestPracticeDisplayGroup getOtherSection() {
		if (otherSection == null) {
			otherSection = new BestPracticeDisplayGroupImpl(
					rb.getString("bestPractices.header.others"),
					rb.getString("bestPractices.header.othersDescription"),
					rb.getString("bestPractice.referSection.others"),
					Arrays.asList(ACCESSING_PERIPHERIALS));
		}
		return otherSection;
	}

	/**
	 * Pre-defined duplicate content best practice
	 */
	protected static BestPracticeDisplay DUPLICATE_CONTENT = new DuplicateBestPractice() ;
	
	/**
	 * Pre-defined cache control best practice
	 */
	protected static final BestPracticeDisplay USING_CACHE = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("caching.usingCache.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("caching.usingCache.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("caching.usingCache.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("caching.usingCache.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().isUsingCache();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("caching.usingCache.pass");
			} else {
				BestPractices bp = analysisData.getBestPractice();
				return MessageFormat.format(rb.getString("caching.usingCache.results"),
						NumberFormat.getIntegerInstance().format(bp.getCacheHeaderRatio()));
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			parent.getAroAdvancedTab().setHighlightedPacketView(
					parent.getAnalysisData().getBestPractice().getNoCacheHeaderStartTime());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
			result.add(new BestPracticeExport(NumberFormat
					.getIntegerInstance().format(bp.getCacheHeaderRatio()), rb.getString("exportall.csvCacheConPct")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined content expiration best practice
	 */
	protected static final BestPracticeDisplay CACHE_CONTROL = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("caching.cacheControl.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("caching.cacheControl.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("caching.cacheControl.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("caching.cacheControl.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().isCacheControl();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("caching.cacheControl.pass");
			} else {
				BestPractices bp = analysisData.getBestPractice();

				return MessageFormat.format(rb.getString("caching.cacheControl.results"),
						bp.getHitNotExpiredDupCount(), bp.getHitExpired304Count());
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			parent.getAroAdvancedTab().setHighlightedPacketView(
					parent.getAnalysisData().getBestPractice().getCacheControlStartTime());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(2);
			result.add(new BestPracticeExport(String.valueOf(bp.getHitNotExpiredDupCount()), rb.getString("exportall.csvCacheConNExpDesc")));
			result.add(new BestPracticeExport(String.valueOf(bp.getHitExpired304Count()), rb.getString("exportall.csvCacheCon304Desc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined content pre-fetching best practice
	 */
	protected static final BestPracticeDisplay PREFETCHING = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("caching.prefetching.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("caching.prefetching.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("caching.prefetching.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("caching.prefetching.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getPrefetching();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("caching.prefetching.pass");
			} else {
				BestPractices bp = analysisData.getBestPractice();
				return MessageFormat.format(rb.getString("caching.prefetching.results"),
						bp.getUserInputBurstCount());
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			if (isPass(analysisData)) {
				return null;
			} else {
				BestPractices bp = analysisData.getBestPractice();
				List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
				result.add(new BestPracticeExport(String.valueOf(bp
						.getUserInputBurstCount()), rb
						.getString("exportall.csvPrefetchDesc")));
				return result;
			}
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined content combine Css and Js best practice
	 */
	protected static final BestPracticeDisplay COMBINE_CS_JSS = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("combinejscss.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("combinejscss.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("combinejscss.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("combinejscss.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			BestPractices bp = analysis.getBestPractice();
			return (bp.getInefficientCssRequests() > 0 || bp.getInefficientJsRequests() > 0) ? false : true; 
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("combinejscss.pass");
			} else {
				return rb.getString("combinejscss.results");
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			parent.displayDiagnosticTab();
			parent.getAroAdvancedTab().setHighlightedPacketView(
					parent.getAnalysisData().getBestPractice().getConsecutiveCssJsFirstPacket());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(2);
			result.add(new BestPracticeExport(String.valueOf(bp.getInefficientCssRequests()), rb.getString("exportall.csvInefficientCssRequests")));
			result.add(new BestPracticeExport(String.valueOf(bp.getInefficientJsRequests()), rb.getString("exportall.csvInefficientJsRequests")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined connection opening best practice
	 */
	protected static final BestPracticeDisplay CONNECTION_OPENING = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.connectionOpening.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.connectionOpening.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return true;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.connectionOpening.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.connectionOpening.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return true;
		}

		@Override
		public String resultText(Analysis analysisData) {
			return rb.getString("connections.connectionOpening.selfEvaluation");
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			return null;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined multiple simultaneous connections best practice
	 */
	protected static final BestPracticeDisplay UNNECESSARY_CONNECTIONS = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.unnecssaryConn.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.unnecssaryConn.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.unnecssaryConn.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.unnecssaryConn.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getMultipleTcpCon();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("connections.unnecssaryConn.pass");
			} else {
				BurstCollectionAnalysis bursts = analysisData.getBcAnalysis();
				int burstSet = bursts.getTightlyCoupledBurstCount();
				return MessageFormat.format(rb.getString("connections.unnecssaryConn.results"),
								burstSet);
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			parent.getAroAdvancedTab()
					.getDisplayedGraphPanel()
					.setGraphView(
							parent.getAnalysisData().getBcAnalysis()
									.getTightlyCoupledBurstTime());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BurstCollectionAnalysis bursts = analysisData.getBcAnalysis();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
			result.add(new BestPracticeExport(String.valueOf(bursts
					.getTightlyCoupledBurstCount()), rb
					.getString("exportall.csvMultiConnDesc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined periodic transfer best practice
	 */
	protected static final BestPracticeDisplay PERIODIC_TRANSFER = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.periodic.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.periodic.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.periodic.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.periodic.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getPeriodicTransfer();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("connections.periodic.pass");
			} else {
				BurstCollectionAnalysis bursts = analysisData.getBcAnalysis();
				return MessageFormat.format(((bursts.getDiffPeriodicCount() > 1) ? rb.getString("connections.periodic.results") : rb.getString("connections.periodic.result")),
						bursts.getDiffPeriodicCount(), bursts.getPeriodicCount(),
						bursts.getMinimumPeriodicRepeatTime());
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			parent.getAroAdvancedTab().setHighlightedPacketView(
					parent.getAnalysisData().getBcAnalysis().getShortestPeriodPacketInfo());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BurstCollectionAnalysis bursts = analysisData.getBcAnalysis();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(3);
			result.add(new BestPracticeExport(String.valueOf(bursts.getDiffPeriodicCount()), rb.getString("exportall.csvIneffConnDesc")));
			result.add(new BestPracticeExport(String.valueOf(bursts.getPeriodicCount()), rb.getString("exportall.csvIneffConnRptDesc")));
			result.add(new BestPracticeExport(String.valueOf(bursts.getMinimumPeriodicRepeatTime()), rb.getString("exportall.csvIneffConnTimeDesc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined screen rotation best practice
	 */
	protected static final BestPracticeDisplay SCREEN_ROTATION = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.screenRotation.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.screenRotation.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.screenRotation.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.screenRotation.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getScreenRotationProblem();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("connections.screenRotation.pass");
			} else {
				return rb.getString("connections.screenRotation.results");
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			double screenRotationBurstTime = parent.getAnalysisData().getBestPractice()
					.getScreenRotationBurstTime();
			parent.getAroAdvancedTab().setTimeLineLinkedComponents(screenRotationBurstTime);
			parent.getAroAdvancedTab().getVideoPlayer()
					.setMediaDisplayTime(screenRotationBurstTime);
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(
					1);
			result.add(new BestPracticeExport(String.valueOf(analysisData
					.getTraceData().getScreenRotationCounter()),
					isPass(analysisData) ? rb
							.getString("exportall.csvSrcnRtnDescPass") : rb
							.getString("exportall.csvSrcnRtnDesc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined connection closing problems best practice
	 */
	protected static final BestPracticeDisplay CONNECTION_CLOSING = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.connClosing.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.connClosing.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.connClosing.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.connClosing.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getConnectionClosingProblem();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("connections.connClosing.pass");
			} else {
				BestPractices bp = analysisData.getBestPractice();
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(1);
				return MessageFormat.format(rb.getString("connections.connClosing.results"),
						nf.format(bp.getTcpControlEnergy()),
						nf.format(bp.getTcpControlEnergyRatio() * 100));
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			parent.getAroAdvancedTab()
					.getDisplayedGraphPanel()
					.setGraphView(
							parent.getAnalysisData().getBestPractice().getLargestEnergyTime());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			if (isPass(analysisData)) {
				return null;
			} else {
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(1);	
				List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
				result.add(new BestPracticeExport(nf.format(analysisData.getBestPractice().getTcpControlEnergy()),
						rb.getString("exportall.csvConnClosingDesc")));
				return result;
			}
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined HTTP 4xx/5xx errors best practice
	 */
	protected static final Http4xx5xxBestPractice HTTP_4XX_5XX = new Http4xx5xxBestPractice();
	
	/**
	 * Pre-defined wifi off-loading best practice
	 */
	protected static final BestPracticeDisplay WIFI_OFFLOADING = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.offloadingToWifi.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.offloadingToWifi.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.offloadingToWifi.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.offloadingToWifi.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getOffloadingToWiFi();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("connections.offloadingToWifi.pass");
			} else {
				BurstCollectionAnalysis bursts = analysisData.getBcAnalysis();
				return MessageFormat.format(
						rb.getString("connections.offloadingToWifi.results"),
						bursts.getLongBurstCount());
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			refreshAndDisplayBurst(parent);
			parent.getAroAdvancedTab()
					.getDisplayedGraphPanel()
					.setGraphView(
							parent.getAnalysisData().getBestPractice().getLargeBurstTime());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
			BurstCollectionAnalysis bursts = analysisData.getBcAnalysis();
			result.add(new BestPracticeExport(String.valueOf(bursts.getLongBurstCount()),
					rb.getString("exportall.csvOffWiFiDesc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined accessing peripherals best practice
	 */
	protected static final BestPracticeDisplay ACCESSING_PERIPHERIALS = new BestPracticeDisplay() {

		private static final int PERIPHERAL_ACTIVE_LIMIT = 5;

		@Override
		public String getOverviewTitle() {
			return rb.getString("other.accessingPeripherals.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("other.accessingPeripherals.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("other.accessingPeripherals.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("other.accessingPeripherals.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getAccessingPeripherals();
		}

		@Override
		public String resultText(Analysis analysisData) {
			String cameraPer;
			BestPractices bp = analysisData.getBestPractice();
			NumberFormat nf = NumberFormat.getIntegerInstance();
			NumberFormat numFor = NumberFormat.getPercentInstance();
			
			if(bp.getCameraActiveStateRatio() < 1.0){
				 cameraPer = numFor.format(bp.getCameraActiveStateRatio());
				 int per = cameraPer.lastIndexOf("%");
				 cameraPer = cameraPer.substring(0,per);
			}else{
				 cameraPer = nf.format(bp.getCameraActiveStateRatio());
			}

			String key = isPass(analysisData) ? "other.accessingPeripherals.pass"
					: "other.accessingPeripherals.results";
			return MessageFormat.format(rb.getString(key),
					nf.format(bp.getGPSActiveStateRatio()),
					nf.format(bp.getBluetoothActiveStateRatio()),
					cameraPer);
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			BestPractices bp = parent.getAnalysisData().getBestPractice();
			if (bp.getGPSActiveStateRatio() > PERIPHERAL_ACTIVE_LIMIT) {
				parent.setExternalChartPlotSelection(ChartPlotOptions.GPS, true);
			}
			if (bp.getBluetoothActiveStateRatio() > PERIPHERAL_ACTIVE_LIMIT) {
				parent.setExternalChartPlotSelection(ChartPlotOptions.BLUETOOTH, true);
			}
			if (bp.getCameraActiveStateRatio() > PERIPHERAL_ACTIVE_LIMIT) {
				parent.setExternalChartPlotSelection(ChartPlotOptions.CAMERA, true);
			}
			parent.displayDiagnosticTab();
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			NumberFormat nf = NumberFormat.getIntegerInstance();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(3);
			result.add(new BestPracticeExport(nf.format(bp.getGPSActiveStateRatio()), rb.getString("exportall.csvAccGPSDesc")));
			result.add(new BestPracticeExport(nf.format(bp.getBluetoothActiveStateRatio()), rb.getString("exportall.csvAccBTDesc")));
			result.add(new BestPracticeExport(nf.format(bp.getCameraActiveStateRatio()), rb.getString("exportall.csvAccCamDesc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Pre-defined HTTP 1.0 usage best practice
	 */
	protected static final BestPracticeDisplay HTTP_1_0_USAGE = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("html.httpUsage.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("html.httpUsage.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("html.httpUsage.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("html.httpUsage.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getHttp10Usage();
		}

		@Override
		public String resultText(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			String key = isPass(analysisData) ? "html.httpUsage.pass" : "html.httpUsage.results";
			return MessageFormat.format(rb.getString(key), bp.getHttp1_0HeaderCount());
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			parent.displayDiagnosticTab();
			parent.getAroAdvancedTab().setHighlightedTCP(
					parent.getAnalysisData().getBestPractice().getHttp1_0Session());
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(1);
			BestPractices bp = analysisData.getBestPractice();
			result.add(new BestPracticeExport(String.valueOf(bp.getHttp1_0HeaderCount()),
					rb.getString("exportall.csvHTTPhdrDesc")));
			return result;
		}

		@Override
		public JPanel getTestResults() {
			return null;
		}
		
	};
	
	/**
	 * Text File Compression best practice
	 */
	protected static final BestPracticeDisplay FILE_COMPRESSION = new BPTextFileCompression();

	/**
	 * Image Size best practice
	 */
	protected static final BestPracticeDisplay IMAGE_SIZE = new ImageSizeBestPractice();

	/**
	 * Minification best practice
	 */
	protected static final BestPracticeDisplay MINIFICATION = new MinificationBestPractice();

	/**
	 * Empty URL tags best practice
	 */
	protected static final BestPracticeDisplay EMPTY_URL = new EmptyUrlBestPractice();

	/**
	 * Flash best practice
	 */
	protected static final BestPracticeDisplay FLASH = new FlashBestPractice();
	
	/**
	 * 3d party scripts best practice
	 */
	protected static final BestPracticeDisplay SCRIPTS_URL = new Scripts();

	/**
	 * Sprite Image best practice
	 */
	protected static final BestPracticeDisplay SPRITEIMAGE = new SpriteImageBestPractice();
	
	/**
	 * Small Request best practice
	 */
	//protected static final BestPracticeDisplay SMALLREQUEST = new SmallRequestBestPractice();
	
	/**
	* Pre-defined HTTP 301/302 errors best practice
	*/
	protected static final BestPracticeDisplay HTTP_3XX_CODE = new Http3xxBestPractice();

	/**
	 * Asynchronous loading of JavaScript in HEAD best practice
	 */
	protected static final BestPracticeDisplay ASYNC_CHECK = new BPAsyncCheckInScript();
	
	/**
	 * File order checking best practice. In the HEAD, CSS files should be loaded before JS files 
	 * */
	protected static final BestPracticeDisplay FILE_ORDER = new FileOrderBestPractice();
	
	/**
	 * DIsplay None in CSS Best Practice.
	 * */
	protected static final BestPracticeDisplay DISPLAY_NONE_IN_CSS = new DisplayNoneInCSSBestPractice();
	
	/**
	 * Refreshes and displays the burst graph in diagnostic view.
	 */
	private static void refreshAndDisplayBurst(ApplicationResourceOptimizer parent) {
		parent.setExternalChartPlotSelection(ChartPlotOptions.BURSTS, true);
		parent.displayDiagnosticTab();
	}
	
}
