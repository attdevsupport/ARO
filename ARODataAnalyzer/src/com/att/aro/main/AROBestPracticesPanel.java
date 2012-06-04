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


package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.images.Images;
import com.att.aro.model.BestPractices;
import com.att.aro.model.BurstCollectionAnalysis;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;

/**
 * Represents the panel for the Best Practices tab.
 */
public class AROBestPracticesPanel extends JScrollPane implements Printable {
	private static final long serialVersionUID = 1L;

	private static final int PERIPHERAL_ACTIVE_LIMIT = 5;
	private static final int DUPLICATE_CONTENT_DENOMINATOR = 1048576;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private ApplicationResourceOptimizer parent;

	private ImagePanel headerPanel;
	private JPanel aroBestPracticesMainPanel;
	private AROBpOverallResulsPanel bpOverallResultsPanel;
	private AROBpDetailedResultPanel bpCachingDetailedResultPanel;
	private AROBpDetailedResultPanel bpConnectionsDetailedResultPanel;
	private AROBpDetailedResultPanel bpOthersDetailedResultPanel;

	/**
	 * Initializes a new instance of the AROBestPracticesPanel class, using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window for the panel.
	 * 
	 * @param appParent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public AROBestPracticesPanel(ApplicationResourceOptimizer appParent) {
		this.parent = appParent;

		aroBestPracticesMainPanel = new JPanel(new BorderLayout());
		aroBestPracticesMainPanel.setBackground(Color.white);
		this.setViewportView(aroBestPracticesMainPanel);
		this.getVerticalScrollBar().setUnitIncrement(10);

		aroBestPracticesMainPanel.add(getHeaderPanel(), BorderLayout.NORTH);

		ImagePanel panel = new ImagePanel(Images.BACKGROUND.getImage());
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(10, 10, 10, 10);

		// adding page 1
		bpOverallResultsPanel = new AROBpOverallResulsPanel();
		panel.add(bpOverallResultsPanel, new GridBagConstraints(0, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		// adding page 2
		List<DetailedResultRowPanel> list = new ArrayList<DetailedResultRowPanel>(
				4);
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("caching.duplicateContent.detailedTitle"), rb
				.getString("caching.duplicateContent.desc"), rb
				.getString("caching.duplicateContent.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getDuplicateContent();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				if (isPass(bp)) {
					return rb.getString("caching.duplicateContent.pass");
				} else {
					NumberFormat nf = NumberFormat.getInstance();
					nf.setMaximumFractionDigits(1);
					NumberFormat nf2 = NumberFormat.getInstance();
					nf2.setMaximumFractionDigits(3);
					return MessageFormat.format(
							rb.getString("caching.duplicateContent.results"),
							nf.format(bp.getDuplicateContentBytesRatio() * 100.0),
							bp.getDuplicateContentsize(), nf2
									.format(((double) bp
											.getDuplicateContentBytes())
											/ DUPLICATE_CONTENT_DENOMINATOR),
							nf2.format(((double) bp.getTotalContentBytes())
									/ DUPLICATE_CONTENT_DENOMINATOR));
				}
			}

			@Override
			public void performAction() {
				parent.displaySimpleTab();
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("caching.usingCache.detailedTitle"), rb
				.getString("caching.usingCache.desc"), rb
				.getString("caching.usingCache.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.isUsingCache();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				if (isPass(bp)) {
					return rb.getString("caching.usingCache.pass");
				} else {
					return MessageFormat.format(
							rb.getString("caching.usingCache.results"),
							NumberFormat.getIntegerInstance().format(
									bp.getCacheHeaderRatio()));
				}
			}

			@Override
			public void performAction() {
				parent.displayResultTab();
				parent.getAnalysisResultsPanel().scrollToCacheStatistics();
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("caching.cacheControl.detailedTitle"), rb
				.getString("caching.cacheControl.desc"), rb
				.getString("caching.cacheControl.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.isCacheControl();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				if (isPass(bp)) {
					return rb.getString("caching.cacheControl.pass");
				} else {
					return MessageFormat.format(
							rb.getString("caching.cacheControl.results"),
							bp.getHitNotExpiredDupCount(),
							bp.getHitExpired304Count());
				}
			}

			@Override
			public void performAction() {
				parent.displayResultTab();
				parent.getAnalysisResultsPanel().scrollToCacheStatistics();
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("caching.prefetching.detailedTitle"), rb
				.getString("caching.prefetching.desc"), rb
				.getString("caching.prefetching.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getPrefetching();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				if (isPass(bp)) {
					return rb.getString("caching.prefetching.pass");
				} else {
					return MessageFormat.format(
							rb.getString("caching.prefetching.results"),
							bp.getUserInputBurstCount());
				}
			}

			@Override
			public void performAction() {
				refreshAndDisplayBurst();
			}

		});
		bpCachingDetailedResultPanel = new AROBpDetailedResultPanel(2,
				rb.getString("bestPractices.header.cache"),
				rb.getString("bestPractices.header.cacheDescription"), list);

		// adding page 3
		list = new ArrayList<DetailedResultRowPanel>(6);
		list.add(new DetailedResultRowPanel(appParent, true, rb
				.getString("connections.connectionOpening.detailedTitle"), rb
				.getString("connections.connectionOpening.desc"), rb
				.getString("connections.connectionOpening.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return true;
			}

			@Override
			public String resultText(Analysis analysisData) {
				return rb
						.getString("connections.connectionOpening.selfEvaluation");
			}

			@Override
			public void performAction() {
			}

		});

		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("connections.unnecssaryConn.detailedTitle"), rb
				.getString("connections.unnecssaryConn.desc"), rb
				.getString("connections.unnecssaryConn.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getMultipleTcpCon();
			}

			@Override
			public String resultText(Analysis analysisData) {
				if (isPass(analysisData.getBestPractice())) {
					return rb.getString("connections.unnecssaryConn.pass");
				} else {
					BurstCollectionAnalysis bursts = parent.getAnalysisData()
							.getBcAnalysis();
					return MessageFormat.format(
							rb.getString("connections.unnecssaryConn.results"),
							bursts.getTightlyCoupledBurstCount());
				}
			}

			@Override
			public void performAction() {
				refreshAndDisplayBurst();
				parent.getAroAdvancedTab()
						.getDisplayedGraphPanel()
						.setGraphView(
								parent.getAnalysisData().getBcAnalysis()
										.getTightlyCoupledBurstTime());
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("connections.periodic.detailedTitle"), rb
				.getString("connections.periodic.desc"), rb
				.getString("connections.periodic.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getPeriodicTransfer();
			}

			@Override
			public String resultText(Analysis analysisData) {
				if (isPass(analysisData.getBestPractice())) {
					return rb.getString("connections.periodic.pass");
				} else {
					BurstCollectionAnalysis bursts = parent.getAnalysisData()
							.getBcAnalysis();
					return MessageFormat.format(
							rb.getString("connections.periodic.results"),
							bursts.getDiffPeriodicCount(),
							bursts.getPeriodicCount(),
							bursts.getMinimumPeriodicRepeatTime());
				}
			}

			@Override
			public void performAction() {
				refreshAndDisplayBurst();
				parent.getAroAdvancedTab().setHighlightedPacketView(
						parent.getAnalysisData().getBcAnalysis()
								.getShortestPeriodPacketInfo());
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("connections.screenRotation.detailedTitle"), rb
				.getString("connections.screenRotation.desc"), rb
				.getString("connections.screenRotation.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getScreenRotationProblem();
			}

			@Override
			public String resultText(Analysis analysisData) {
				if (isPass(analysisData.getBestPractice())) {
					return rb.getString("connections.screenRotation.pass");
				} else {
					return rb.getString("connections.screenRotation.results");
				}
			}

			@Override
			public void performAction() {
				refreshAndDisplayBurst();
				double screenRotationBurstTime = parent.getAnalysisData().getBestPractice().getScreenRotationBurstTime();
				parent.getAroAdvancedTab().setTimeLineLinkedComponents(screenRotationBurstTime);
				parent.getAroAdvancedTab().getVideoPlayer().setMediaDisplayTime(screenRotationBurstTime);
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("connections.connClosing.detailedTitle"), rb
				.getString("connections.connClosing.desc"), rb
				.getString("connections.connClosing.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getConnectionClosingProblem();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				if (isPass(bp)) {
					return rb.getString("connections.connClosing.pass");
				} else {
					NumberFormat nf = NumberFormat.getInstance();
					nf.setMaximumFractionDigits(1);
					return MessageFormat.format(
							rb.getString("connections.connClosing.results"),
							nf.format(bp.getTcpControlEnergy()),
							nf.format(bp.getTcpControlEnergyRatio() * 100));
				}
			}

			@Override
			public void performAction() {
				refreshAndDisplayBurst();
				parent.getAroAdvancedTab()
						.getDisplayedGraphPanel()
						.setGraphView(
								parent.getAnalysisData().getBestPractice()
										.getLargestEnergyTime());
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("connections.offloadingToWifi.detailedTitle"), rb
				.getString("connections.offloadingToWifi.desc"), rb
				.getString("connections.offloadingToWifi.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getOffloadingToWiFi();
			}

			@Override
			public String resultText(Analysis analysisData) {
				if (isPass(analysisData.getBestPractice())) {
					return rb.getString("connections.offloadingToWifi.pass");
				} else {
					BurstCollectionAnalysis bursts = parent.getAnalysisData()
							.getBcAnalysis();
					return MessageFormat.format(rb
							.getString("connections.offloadingToWifi.results"),
							bursts.getLongBurstCount());
				}
			}

			@Override
			public void performAction() {
				refreshAndDisplayBurst();
				parent.getAroAdvancedTab()
						.getDisplayedGraphPanel()
						.setGraphView(
								parent.getAnalysisData().getBestPractice()
										.getLargeBurstTime());
			}

		});
		bpConnectionsDetailedResultPanel = new AROBpDetailedResultPanel(3,
				rb.getString("bestPractices.header.connections"),
				rb.getString("bestPractices.header.connectionsDescription"),
				list);

		// adding page4
		list = new ArrayList<DetailedResultRowPanel>(2);
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("other.accessingPeripherals.detailedTitle"), rb
				.getString("other.accessingPeripherals.desc"), rb
				.getString("other.accessingPeripherals.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getAccessingPeripherals();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				NumberFormat nf = NumberFormat.getIntegerInstance();
				String key = isPass(bp) ? "other.accessingPeripherals.pass"
						: "other.accessingPeripherals.results";
				return MessageFormat.format(rb.getString(key),
						nf.format(bp.getGPSActiveStateRatio()),
						nf.format(bp.getBluetoothActiveStateRatio()),
						nf.format(bp.getCameraActiveStateRatio()));
			}

			@Override
			public void performAction() {
				BestPractices bp = parent.getAnalysisData().getBestPractice();
				if (bp.getGPSActiveStateRatio() > PERIPHERAL_ACTIVE_LIMIT) {
					parent.setExternalChartPlotSelection(ChartPlotOptions.GPS,
							true);
				}
				if (bp.getBluetoothActiveStateRatio() > PERIPHERAL_ACTIVE_LIMIT) {
					parent.setExternalChartPlotSelection(
							ChartPlotOptions.BLUETOOTH, true);
				}
				if (bp.getCameraActiveStateRatio() > PERIPHERAL_ACTIVE_LIMIT) {
					parent.setExternalChartPlotSelection(
							ChartPlotOptions.CAMERA, true);
				}
				parent.displayAdvancedTab();
			}

		});
		list.add(new DetailedResultRowPanel(appParent, false, rb
				.getString("other.httpUsage.detailedTitle"), rb
				.getString("other.httpUsage.desc"), rb
				.getString("other.httpUsage.url")) {

			@Override
			public boolean isPass(BestPractices bp) {
				return bp.getHttp10Usage();
			}

			@Override
			public String resultText(Analysis analysisData) {
				BestPractices bp = analysisData.getBestPractice();
				String key = isPass(bp) ? "other.httpUsage.pass"
						: "other.httpUsage.results";
				return MessageFormat.format(rb.getString(key),
						bp.getHttp1_0HeaderCount());
			}

			@Override
			public void performAction() {
				parent.displayAdvancedTab();
				parent.getAroAdvancedTab().setHighlightedTCP(
						parent.getAnalysisData().getBestPractice()
								.getHttp1_0Session());
			}

		});
		bpOthersDetailedResultPanel = new AROBpDetailedResultPanel(4,
				rb.getString("bestPractices.header.others"),
				rb.getString("bestPractices.header.othersDescription"), list);

		panel.add(bpCachingDetailedResultPanel, new GridBagConstraints(0, 1, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(bpConnectionsDetailedResultPanel, new GridBagConstraints(0,
				2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panel.add(bpOthersDetailedResultPanel, new GridBagConstraints(0, 3, 1,
				1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		aroBestPracticesMainPanel.add(panel, BorderLayout.CENTER);

		ActionListener caching = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				aroBestPracticesMainPanel
						.scrollRectToVisible(bpCachingDetailedResultPanel
								.getBounds());
			}

		};

		ActionListener connections = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				aroBestPracticesMainPanel
						.scrollRectToVisible(bpConnectionsDetailedResultPanel
								.getBounds());
			}

		};

		ActionListener other = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				aroBestPracticesMainPanel
						.scrollRectToVisible(bpOthersDetailedResultPanel
								.getBounds());
			}

		};

		bpOverallResultsPanel.getDuplicateContentPanel().getReferSectionLabel()
				.addActionListener(caching);
		bpOverallResultsPanel.getPrefetchingPanel().getReferSectionLabel()
				.addActionListener(caching);
		bpOverallResultsPanel.getUsingCachePanel().getReferSectionLabel()
				.addActionListener(caching);
		bpOverallResultsPanel.getCacheControlPanel().getReferSectionLabel()
				.addActionListener(caching);
		bpOverallResultsPanel.getConnectionOpeningPanel()
				.getReferSectionLabel().addActionListener(connections);
		bpOverallResultsPanel.getUnnecessaryConnectionsPanel()
				.getReferSectionLabel().addActionListener(connections);
		bpOverallResultsPanel.getPeriodicTransferPanel().getReferSectionLabel()
				.addActionListener(connections);
		bpOverallResultsPanel.getScreenRotationPanel().getReferSectionLabel()
				.addActionListener(connections);
		bpOverallResultsPanel.getConnectionClosingPanel()
				.getReferSectionLabel().addActionListener(connections);
		bpOverallResultsPanel.getWifiOffloadingPanel().getReferSectionLabel()
				.addActionListener(connections);
		bpOverallResultsPanel.getAccessingPeripheralsPanel()
				.getReferSectionLabel().addActionListener(other);
		bpOverallResultsPanel.getHttp10UsagePanel().getReferSectionLabel()
				.addActionListener(other);

	}

	/**
	 * Refreshes the content of the Best Practices panel with the specified
	 * trace data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		bpOverallResultsPanel.refresh(analysisData);
		bpCachingDetailedResultPanel.refresh(analysisData);
		bpConnectionsDetailedResultPanel.refresh(analysisData);
		bpOthersDetailedResultPanel.refresh(analysisData);
	}

	/**
	 * Prints the information in the AROBestPracticesPanel using the specified
	 * graphics object, page format, and page index. This method implements the
	 * print command of the java.awt.print.Printable interface.
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		JComponent[] printables = { bpOverallResultsPanel,
				bpCachingDetailedResultPanel, bpConnectionsDetailedResultPanel,
				bpOthersDetailedResultPanel };

		int pages;
		int pageCount = 0;

		for (JComponent c : printables) {
			AROPrintablePanel p = new AROPrintablePanel(c);
			pages = p.getPageCount(pageFormat);

			if (pageIndex < pageCount + pages) {
				return p.print(graphics, pageFormat, pageIndex - pageCount);
			}
			pageCount += pages;
		}

		return NO_SUCH_PAGE;
	}

	/**
	 * Returns the blue header panel with the ATT logo.
	 */
	private ImagePanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new ImagePanel(Images.BLUE_HEADER.getImage());
			headerPanel.setLayout(new BorderLayout(50, 50));
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			JLabel l = new JLabel(Images.HEADER_ICON.getIcon(),
					SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(80, 80));
			headerPanel.add(l, BorderLayout.WEST);

			JLabel bpHeaderLabel = new JLabel(
					rb.getString("bestPractices.header.result"));
			bpHeaderLabel.setFont(UIManager
					.getFont(AROUIManager.TITLE_FONT_KEY));
			bpHeaderLabel.setForeground(Color.WHITE);
			headerPanel.add(bpHeaderLabel, BorderLayout.CENTER);
		}
		return headerPanel;
	}

	/**
	 * Refreshes and displays the burst graph in diagnostic view.
	 */
	private void refreshAndDisplayBurst() {
		parent.setExternalChartPlotSelection(ChartPlotOptions.BURSTS, true);
		parent.displayAdvancedTab();
	}

}
