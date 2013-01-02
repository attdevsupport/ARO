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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.event.HyperlinkEvent;

import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.ChartPlotOptions;
import com.att.aro.main.ResourceBundleManager;
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

	private BestPracticeDisplayGroup cachingSection;
	private BestPracticeDisplayGroup connectionsSection;
	private BestPracticeDisplayGroup otherSection;
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
			bestPracticeDisplay.add(getCachingSection());
			bestPracticeDisplay.add(getConnectionsSection());
			bestPracticeDisplay.add(getOtherSection());
		}
		return this.bestPracticeDisplay;
	}

	/**
	 * Returns the pre-defined caching best practice section
	 * @return The best practice display group for caching
	 */
	protected BestPracticeDisplayGroup getCachingSection() {
		if (cachingSection == null) {
			cachingSection = new BestPracticeDisplayGroupImpl(
					rb.getString("bestPractices.header.cache"),
					rb.getString("bestPractices.header.cacheDescription"),
					rb.getString("bestPractice.referSection.caching"),
					Arrays.asList(duplicateContent, usingCache, cacheControl,
							prefetching));
		}
		return cachingSection;
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
					Arrays.asList(connectionOpening, unnecessaryConnections,
							periodicTransfer, screenRotation,
							connectionClosing, wifiOffloading, http4xx5xx));
		}
		return connectionsSection;
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
					Arrays.asList(accessingPeripherals, http10Usage));
		}
		return otherSection;
	}

	/**
	 * Pre-defined duplicate content best practice
	 */
	protected static final BestPracticeDisplay duplicateContent = new BestPracticeDisplay() {

		private static final int DUPLICATE_CONTENT_DENOMINATOR = 1048576;

		@Override
		public String getOverviewTitle() {
			return rb.getString("caching.duplicateContent.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("caching.duplicateContent.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("caching.duplicateContent.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("caching.duplicateContent.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getDuplicateContent();
		}

		@Override
		public String resultText(Analysis analysisData) {
			if (isPass(analysisData)) {
				return rb.getString("caching.duplicateContent.pass");
			} else {
				BestPractices bp = analysisData.getBestPractice();
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(1);
				NumberFormat nf2 = NumberFormat.getInstance();
				nf2.setMaximumFractionDigits(3);

				return MessageFormat.format(
						rb.getString("caching.duplicateContent.results"),
						nf.format(bp.getDuplicateContentBytesRatio() * 100.0),
						bp.getDuplicateContentsize(),
						nf2.format(((double) bp.getDuplicateContentBytes())
								/ DUPLICATE_CONTENT_DENOMINATOR),
						nf2.format(((double) bp.getTotalContentBytes())
								/ DUPLICATE_CONTENT_DENOMINATOR));
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			parent.displaySimpleTab();
		}

		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(1);
			NumberFormat nf2 = NumberFormat.getInstance();
			nf2.setMaximumFractionDigits(3);

			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(3);
			result.add(new BestPracticeExport(nf.format(bp
					.getDuplicateContentBytesRatio() * 100.0), rb
					.getString("exportall.csvPct")));
			result.add(new BestPracticeExport(String.valueOf(bp
					.getDuplicateContentsize()), rb
					.getString("exportall.csvFiles")));
			result.add(new BestPracticeExport(nf2.format(((double) bp
					.getDuplicateContentBytes())
					/ DUPLICATE_CONTENT_DENOMINATOR), rb
					.getString("statics.csvUnits.mbytes")));
			return result;
		}
		
	};
	
	/**
	 * Pre-defined cache control best practice
	 */
	protected static final BestPracticeDisplay usingCache = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined content expiration best practice
	 */
	protected static final BestPracticeDisplay cacheControl = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined content pre-fetching best practice
	 */
	protected static final BestPracticeDisplay prefetching = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined connection opening best practice
	 */
	protected static final BestPracticeDisplay connectionOpening = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined multiple simultaneous connections best practice
	 */
	protected static final BestPracticeDisplay unnecessaryConnections = new BestPracticeDisplay() {

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
				return burstSet > 1 ? MessageFormat.format(rb.getString("connections.unnecssaryConn.results"),
						burstSet) : MessageFormat.format(rb.getString("connections.unnecssaryConn.result"),
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
		
	};
	
	/**
	 * Pre-defined periodic transfer best practice
	 */
	protected static final BestPracticeDisplay periodicTransfer = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined screen rotation best practice
	 */
	protected static final BestPracticeDisplay screenRotation = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined connection closing problems best practice
	 */
	protected static final BestPracticeDisplay connectionClosing = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined HTTP 4xx/5xx errors best practice
	 */
	protected static final BestPracticeDisplay http4xx5xx = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("connections.http4xx5xx.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("connections.http4xx5xx.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("connections.http4xx5xx.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("connections.http4xx5xx.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getHttpErrorCounts().isEmpty();
		}

		@Override
		public String resultText(Analysis analysisData) {
			Map<Integer, Integer> map = analysisData.getBestPractice().getHttpErrorCounts();
			Iterator<Map.Entry<Integer, Integer>> i = map.entrySet().iterator();
			if (i.hasNext()) {
				Map.Entry<Integer, Integer> entry = i.next();
				String message = formatError(entry);
				if (i.hasNext()) {
					entry = i.next();
					while (i.hasNext()) {
						message = MessageFormat.format(rb
								.getString("connections.http4xx5xx.errorList"),
								message, formatError(entry));
						entry = i.next();
					}
					message = MessageFormat
							.format(rb
									.getString("connections.http4xx5xx.errorListEnd"),
									message, formatError(entry));
				}
				
				return MessageFormat.format(
						rb.getString("connections.http4xx5xx.results"), message);
			} else {
				return rb.getString("connections.http4xx5xx.pass");
			}
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			try {
				
				// Find a response with the selected status code
				int status = Integer.parseInt(h.getDescription());
				parent.displayAdvancedTab();
				parent.getAroAdvancedTab().setHighlightedRequestResponse(
						parent.getAnalysisData().getBestPractice()
								.getFirstErrorRespMap().get(status));
			} catch (NumberFormatException e) {
				// Ignore
			}
		}
		
		@Override
		public List<BestPracticeExport> getExportData(Analysis analysisData) {
			Map<Integer, Integer> map = analysisData.getBestPractice().getHttpErrorCounts();
			List<BestPracticeExport> result = new ArrayList<BestPracticeExport>(map.size());
			for (Map.Entry<Integer, Integer> entry : analysisData.getBestPractice().getHttpErrorCounts().entrySet()) {
				result.add(new BestPracticeExport(String.valueOf(entry.getValue()),
						MessageFormat.format(rb.getString("exportall.csvHttpError"), entry.getKey())));
			}
			return result;
		}

		private String formatError(Map.Entry<Integer, Integer> entry) {
			int count = entry.getValue();
			if (count > 1) {
				return MessageFormat.format(
						rb.getString("connections.http4xx5xx.errorPlural"),
						count, entry.getKey());
			} else {
				return MessageFormat.format(
						rb.getString("connections.http4xx5xx.errorSingular"),
						entry.getKey());
			}
		}
	};
	
	/**
	 * Pre-defined wifi off-loading best practice
	 */
	protected static final BestPracticeDisplay wifiOffloading = new BestPracticeDisplay() {

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
		
	};
	
	/**
	 * Pre-defined accessing peripherals best practice
	 */
	protected static final BestPracticeDisplay accessingPeripherals = new BestPracticeDisplay() {

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
			BestPractices bp = analysisData.getBestPractice();
			NumberFormat nf = NumberFormat.getIntegerInstance();

			String key = isPass(analysisData) ? "other.accessingPeripherals.pass"
					: "other.accessingPeripherals.results";
			return MessageFormat.format(rb.getString(key),
					nf.format(bp.getGPSActiveStateRatio()),
					nf.format(bp.getBluetoothActiveStateRatio()),
					nf.format(bp.getCameraActiveStateRatio()));
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
			parent.displayAdvancedTab();
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
		
	};
	
	/**
	 * Pre-defined HTTP 1.0 usage best practice
	 */
	protected static final BestPracticeDisplay http10Usage = new BestPracticeDisplay() {

		@Override
		public String getOverviewTitle() {
			return rb.getString("other.httpUsage.title");
		}

		@Override
		public String getDetailTitle() {
			return rb.getString("other.httpUsage.detailedTitle");
		}

		@Override
		public boolean isSelfTest() {
			return false;
		}

		@Override
		public String getAboutText() {
			return rb.getString("other.httpUsage.desc");
		}

		@Override
		public URI getLearnMoreURI() {
			return URI.create(rb.getString("other.httpUsage.url"));
		}

		@Override
		public boolean isPass(TraceData.Analysis analysis) {
			return analysis.getBestPractice().getHttp10Usage();
		}

		@Override
		public String resultText(Analysis analysisData) {
			BestPractices bp = analysisData.getBestPractice();
			String key = isPass(analysisData) ? "other.httpUsage.pass" : "other.httpUsage.results";
			return MessageFormat.format(rb.getString(key), bp.getHttp1_0HeaderCount());
		}

		@Override
		public void performAction(HyperlinkEvent h, ApplicationResourceOptimizer parent) {
			parent.displayAdvancedTab();
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
		
	};
	
	/**
	 * Refreshes and displays the burst graph in diagnostic view.
	 */
	private static void refreshAndDisplayBurst(ApplicationResourceOptimizer parent) {
		parent.setExternalChartPlotSelection(ChartPlotOptions.BURSTS, true);
		parent.displayAdvancedTab();
	}

}
