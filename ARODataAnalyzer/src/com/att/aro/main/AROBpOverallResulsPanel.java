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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.commonui.HyperlinkLabel;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.commonui.RoundedBorder;
import com.att.aro.images.Images;
import com.att.aro.model.BestPractices;
import com.att.aro.model.TraceData;

/**
 * Represents the over all results section of the Best Practices tab.
 */
public class AROBpOverallResulsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static Font textFont = new Font("TextFont", Font.PLAIN, 12);
	private static Font headerFont = new Font("HeaderFont", Font.BOLD, 14);
	private static Font summaryFont = new Font("HeaderFont", Font.BOLD, 18);
	private static Font boldTextFont = new Font("BoldTextFont", Font.BOLD, 12);
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private BPResultRowPanel duplicateContentPanel;
	private BPResultRowPanel usingCachePanel;
	private BPResultRowPanel cacheControlPanel;
	private BPResultRowPanel prefetchingPanel;
	private BPResultRowPanel connectionOpeningPanel;
	private BPResultRowPanel unnecessaryConnectionsPanel;
	private BPResultRowPanel periodicTransferPanel;
	private BPResultRowPanel screenRotationPanel;
	private BPResultRowPanel connectionClosingPanel;
	private BPResultRowPanel wifiOffloadingPanel;
	private BPResultRowPanel accessingPeripheralsPanel;
	private BPResultRowPanel http10UsagePanel;

	private DateTraceAppDetailPanel dateTraceAppDetailPanel;

	private JLabel durationValueLabel;
	private JLabel totalDataValueLabel;
	private JLabel energyConsumedValueLabel;

	/**
	 * Initializes a new instance of the AROBpOverallResulsPanel class.
	 */
	public AROBpOverallResulsPanel() {

		setLayout(new GridBagLayout());
		setOpaque(false);
		setBorder(new RoundedBorder(new Insets(20, 20, 20, 20), Color.WHITE));

		Insets insets = new Insets(0, 0, 0, 0);
		dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		add(dateTraceAppDetailPanel, new GridBagConstraints(0, 0, 3, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				insets, 0, 0));
		add(createTestStatisticsPanel(), new GridBagConstraints(0, 1, 3, 1,
				0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		JLabel testConductedHeaderLabel = new JLabel(
				rb.getString("bestPractices.header.testsConducted"));
		testConductedHeaderLabel.setBackground(Color.WHITE);
		testConductedHeaderLabel.setFont(headerFont);
		add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
				new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL,
						new Insets(10, 0, 10, 0), 0, 0));
		add(testConductedHeaderLabel, new GridBagConstraints(0, 3, 3, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets,
				0, 0));

		String cacheRefer = MessageFormat.format(
				rb.getString("bestPractice.referSection"),
				rb.getString("bestPractice.referSection.caching"));
		String connRefer = MessageFormat.format(
				rb.getString("bestPractice.referSection"),
				rb.getString("bestPractice.referSection.connections"));
		String otherRefer = MessageFormat.format(
				rb.getString("bestPractice.referSection"),
				rb.getString("bestPractice.referSection.others"));
		String selfTest = rb.getString("bestPractices.selfTest");
		duplicateContentPanel = new BPResultRowPanel(false,
				rb.getString("caching.duplicateContent.title"), cacheRefer);
		usingCachePanel = new BPResultRowPanel(false,
				rb.getString("caching.usingCache.title"), cacheRefer);
		cacheControlPanel = new BPResultRowPanel(false,
				rb.getString("caching.cacheControl.title"), cacheRefer);
		prefetchingPanel = new BPResultRowPanel(false,
				rb.getString("caching.prefetching.title"), cacheRefer);
		connectionOpeningPanel = new BPResultRowPanel(true,
				rb.getString("connections.connectionOpening.title"), selfTest);
		unnecessaryConnectionsPanel = new BPResultRowPanel(false,
				rb.getString("connections.unnecssaryConn.title"), connRefer);
		periodicTransferPanel = new BPResultRowPanel(false,
				rb.getString("connections.periodic.title"), connRefer);
		screenRotationPanel = new BPResultRowPanel(true,
				rb.getString("connections.screenRotation.title"), selfTest);
		connectionClosingPanel = new BPResultRowPanel(false,
				rb.getString("connections.connClosing.title"), connRefer);
		wifiOffloadingPanel = new BPResultRowPanel(false,
				rb.getString("connections.offloadingToWifi.title"), connRefer);
		accessingPeripheralsPanel = new BPResultRowPanel(false,
				rb.getString("other.accessingPeripherals.title"), otherRefer);
		http10UsagePanel = new BPResultRowPanel(false,
				rb.getString("other.httpUsage.title"), otherRefer);
		insets = new Insets(10, 20, 10, 10);
		// adding the 12 best practices
		add(duplicateContentPanel.getIconLabel(), new GridBagConstraints(0, 4,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(duplicateContentPanel.getTitleLabel(), new GridBagConstraints(1, 4,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(duplicateContentPanel.getReferSectionLabel(),
				new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(usingCachePanel.getIconLabel(), new GridBagConstraints(0, 5, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(usingCachePanel.getTitleLabel(), new GridBagConstraints(1, 5, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(usingCachePanel.getReferSectionLabel(), new GridBagConstraints(2,
				5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(cacheControlPanel.getIconLabel(), new GridBagConstraints(0, 6, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(cacheControlPanel.getTitleLabel(), new GridBagConstraints(1, 6, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(cacheControlPanel.getReferSectionLabel(), new GridBagConstraints(2,
				6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(prefetchingPanel.getIconLabel(), new GridBagConstraints(0, 7, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(prefetchingPanel.getTitleLabel(), new GridBagConstraints(1, 7, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(prefetchingPanel.getReferSectionLabel(), new GridBagConstraints(2,
				7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(connectionOpeningPanel.getIconLabel(), new GridBagConstraints(0, 8,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(connectionOpeningPanel.getTitleLabel(), new GridBagConstraints(1,
				8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(connectionOpeningPanel.getReferSectionLabel(),
				new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(unnecessaryConnectionsPanel.getIconLabel(), new GridBagConstraints(
				0, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(unnecessaryConnectionsPanel.getTitleLabel(),
				new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(unnecessaryConnectionsPanel.getReferSectionLabel(),
				new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(periodicTransferPanel.getIconLabel(), new GridBagConstraints(0, 10,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(periodicTransferPanel.getTitleLabel(), new GridBagConstraints(1,
				10, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(periodicTransferPanel.getReferSectionLabel(),
				new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(screenRotationPanel.getIconLabel(), new GridBagConstraints(0, 11,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(screenRotationPanel.getTitleLabel(), new GridBagConstraints(1, 11,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(screenRotationPanel.getReferSectionLabel(), new GridBagConstraints(
				2, 11, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(connectionClosingPanel.getIconLabel(), new GridBagConstraints(0,
				12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(connectionClosingPanel.getTitleLabel(), new GridBagConstraints(1,
				12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(connectionClosingPanel.getReferSectionLabel(),
				new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(wifiOffloadingPanel.getIconLabel(), new GridBagConstraints(0, 13,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(wifiOffloadingPanel.getTitleLabel(), new GridBagConstraints(1, 13,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(wifiOffloadingPanel.getReferSectionLabel(), new GridBagConstraints(
				2, 13, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(accessingPeripheralsPanel.getIconLabel(), new GridBagConstraints(0,
				14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(accessingPeripheralsPanel.getTitleLabel(), new GridBagConstraints(
				1, 14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(accessingPeripheralsPanel.getReferSectionLabel(),
				new GridBagConstraints(2, 14, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
		add(http10UsagePanel.getIconLabel(), new GridBagConstraints(0, 15, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(http10UsagePanel.getTitleLabel(), new GridBagConstraints(1, 15, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(http10UsagePanel.getReferSectionLabel(), new GridBagConstraints(2,
				15, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
	}

	/**
	 * Returns the results panel for the Duplicate Content Best Practices test.
	 * 
	 * @return BPResultRowPanel The Duplicate Content result panel.
	 */
	public BPResultRowPanel getDuplicateContentPanel() {
		return duplicateContentPanel;
	}

	/**
	 * Returns the results panel for the Using Cache Best Practices test.
	 * 
	 * @return BPResultRowPanel The Using Cache results panel.
	 */
	public BPResultRowPanel getUsingCachePanel() {
		return usingCachePanel;
	}

	/**
	 * Returns the results panel for the Cache Control Best Practices test.
	 * 
	 * @return BPResultRowPanel The Cache Control results panel.
	 */
	public BPResultRowPanel getCacheControlPanel() {
		return cacheControlPanel;
	}

	/**
	 * Returns the results panel for the Prefetching Best Practices test.
	 * 
	 * @return BPResultRowPanel The Prefetching results panel.
	 */
	public BPResultRowPanel getPrefetchingPanel() {
		return prefetchingPanel;
	}

	/**
	 * Returns the results panel for the Connection Opening Best Practices test.
	 * 
	 * @return BPResultRowPanel The Connection Opening result panel.
	 */
	public BPResultRowPanel getConnectionOpeningPanel() {
		return connectionOpeningPanel;
	}

	/**
	 * Returns the results panel for the Unnecessary Connections Best Practices
	 * test.
	 * 
	 * @return BPResultRowPanel The Unnecessary Connections results panel.
	 */
	public BPResultRowPanel getUnnecessaryConnectionsPanel() {
		return unnecessaryConnectionsPanel;
	}

	/**
	 * Returns the results panel for the Periodic Transfer Best Practices test.
	 * 
	 * @return BPResultRowPanel The Periodic Transfer results panel.
	 */
	public BPResultRowPanel getPeriodicTransferPanel() {
		return periodicTransferPanel;
	}

	/**
	 * Returns the results panel for the Screen Rotation Best Practices test.
	 * 
	 * @return BPResultRowPanel The Screen Rotation results panel.
	 */
	public BPResultRowPanel getScreenRotationPanel() {
		return screenRotationPanel;
	}

	/**
	 * Returns the results panel for the Connection Closing Best Practices test.
	 * 
	 * @return BPResultRowPanel The Connection Closing results panel.
	 */
	public BPResultRowPanel getConnectionClosingPanel() {
		return connectionClosingPanel;
	}

	/**
	 * Returns the results panel for the WiFi Offloading Best Practices test.
	 * 
	 * @return BPResultRowPanel The WiFi Offloading results panel.
	 */
	public BPResultRowPanel getWifiOffloadingPanel() {
		return wifiOffloadingPanel;
	}

	/**
	 * Returns the results panel for the Accessing Peripherals Best Practices
	 * test.
	 * 
	 * @return BPResultRowPanel The Accessing Peripherals result Panel.
	 */
	public BPResultRowPanel getAccessingPeripheralsPanel() {
		return accessingPeripheralsPanel;
	}

	/**
	 * Returns the Http1.0 Usage results panel for the Accessing Peripherals
	 * Best Practices test.
	 * 
	 * @return BPResultRowPanel The Accessing Peripherals results panel.
	 */
	public BPResultRowPanel getHttp10UsagePanel() {
		return http10UsagePanel;
	}

	/**
	 * Refreshes the content of the Best Practices over all results panel with
	 * the specified trace data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {

		dateTraceAppDetailPanel.refresh(analysisData);

		if (analysisData != null) {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			nf.setMinimumFractionDigits(1);
			nf.setMinimumIntegerDigits(1);
			durationValueLabel
					.setText(MessageFormat.format(rb
							.getString("bestPractices.durationValue"), nf
							.format(analysisData.getTraceData()
									.getTraceDuration() / 60)));
			energyConsumedValueLabel.setText(MessageFormat.format(rb
					.getString("bestPractices.energyConsumedValue"), nf
					.format(analysisData.getEnergyModel()
							.getTotalEnergyConsumed())));
			NumberFormat intf = NumberFormat.getIntegerInstance();
			totalDataValueLabel.setText(MessageFormat.format(
					rb.getString("bestPractices.totalDataTransferedValue"),
					intf.format(analysisData.getTotalBytes())));

			BestPractices bp = analysisData.getBestPractice();

			duplicateContentPanel.refreshFields(bp.getDuplicateContent());
			usingCachePanel.refreshFields(bp.isUsingCache());
			cacheControlPanel.refreshFields(bp.isCacheControl());
			prefetchingPanel.refreshFields(bp.getPrefetching());
			unnecessaryConnectionsPanel.refreshFields(bp.getMultipleTcpCon());
			periodicTransferPanel.refreshFields(bp.getPeriodicTransfer());
			connectionClosingPanel.refreshFields(bp
					.getConnectionClosingProblem());
			wifiOffloadingPanel.refreshFields(bp.getOffloadingToWiFi());
			accessingPeripheralsPanel.refreshFields(bp
					.getAccessingPeripherals());
			http10UsagePanel.refreshFields(bp.getHttp10Usage());
			connectionOpeningPanel.refreshFields(true);
			screenRotationPanel.refreshFields(true);
		} else {
			durationValueLabel.setText(null);
			energyConsumedValueLabel.setText(null);
			totalDataValueLabel.setText(null);

			duplicateContentPanel.refreshFields(null);
			usingCachePanel.refreshFields(null);
			cacheControlPanel.refreshFields(null);
			prefetchingPanel.refreshFields(null);
			unnecessaryConnectionsPanel.refreshFields(null);
			periodicTransferPanel.refreshFields(null);
			connectionClosingPanel.refreshFields(null);
			wifiOffloadingPanel.refreshFields(null);
			accessingPeripheralsPanel.refreshFields(null);
			http10UsagePanel.refreshFields(null);
			connectionOpeningPanel.refreshFields(null);
			screenRotationPanel.refreshFields(null);
		}
	}

	/**
	 * Creates a JPanel containing the Statistics details.
	 */
	private JPanel createTestStatisticsPanel() {

		JPanel statisticsLeftAllignmentPanel = new JPanel(new BorderLayout());
		statisticsLeftAllignmentPanel.setBackground(Color.white);
		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setLayout(new VerticalLayout());
		statisticsPanel.setBackground(Color.WHITE);

		JLabel summaryHeaderLabel = new JLabel(
				rb.getString("bestPractices.header.summary"));
		summaryHeaderLabel.setBackground(Color.WHITE);
		summaryHeaderLabel.setFont(summaryFont);

		JLabel statisticsHeaderLabel = new JLabel(
				rb.getString("bestPractices.header.statistics"));
		statisticsHeaderLabel.setBackground(Color.WHITE);
		statisticsHeaderLabel.setFont(headerFont);

		JPanel durationPanel = new JPanel(new GridLayout(1, 2));
		durationPanel.setBackground(Color.WHITE);
		JLabel durationLabel = new JLabel(
				rb.getString("bestPractices.duration"));
		durationLabel.setFont(textFont);
		durationValueLabel = new JLabel();
		durationValueLabel.setFont(textFont);
		durationPanel.add(durationLabel);
		durationPanel.add(durationValueLabel);

		JPanel totalDataPanel = new JPanel(new GridLayout(1, 2));
		totalDataPanel.setBackground(Color.WHITE);
		JLabel totalDataLabel = new JLabel(
				rb.getString("bestPractices.totalDataTransfered"));
		totalDataLabel.setFont(textFont);
		totalDataValueLabel = new JLabel();
		totalDataValueLabel.setFont(textFont);
		totalDataPanel.add(totalDataLabel);
		totalDataPanel.add(totalDataValueLabel);

		JPanel energyConsumedPanel = new JPanel(new GridLayout(1, 2));
		energyConsumedPanel.setBackground(Color.WHITE);
		JLabel energyConsumedLabel = new JLabel(
				rb.getString("bestPractices.energyConsumed"));
		energyConsumedLabel.setFont(textFont);
		energyConsumedValueLabel = new JLabel();
		energyConsumedValueLabel.setFont(textFont);
		energyConsumedPanel.add(energyConsumedLabel);
		energyConsumedPanel.add(energyConsumedValueLabel);

		JLabel summaryFillerHeaderLabel = new JLabel(" ");
		JLabel testFillerHeaderLabel = new JLabel(" ");

		statisticsPanel.add(summaryHeaderLabel);
		JPanel subContentPanel = new JPanel(new BorderLayout());
		// TODO: Tried the JSeparator with HORIZONTAL orientation. As there is
		// no effect used the empty JLabel
		// subContentPanel.add(AROBestPracticesPanel.createCustomSeparator(Color.WHITE,
		// AROBestPracticesPanel.PADDING_SEPERATOR_WIDTH,
		// AROBestPracticesPanel.PADDING_SEPERATOR_HEIGHT),
		// BorderLayout.WEST);
		subContentPanel.setBackground(Color.WHITE);
		JPanel titlePanel = new JPanel(new VerticalLayout());
		titlePanel.setBackground(Color.WHITE);
		titlePanel.add(summaryFillerHeaderLabel);
		titlePanel.add(statisticsHeaderLabel);
		titlePanel.add(durationPanel);
		titlePanel.add(totalDataPanel);
		titlePanel.add(energyConsumedPanel);
		titlePanel.add(testFillerHeaderLabel);
		subContentPanel.add(titlePanel, BorderLayout.CENTER);
		statisticsPanel.add(subContentPanel);

		statisticsLeftAllignmentPanel.add(statisticsPanel, BorderLayout.WEST);
		return statisticsLeftAllignmentPanel;

	}

	/**
	 * Represents a row of Best Practice information containing 3 icons and the
	 * Best Practice Label.
	 * 
	 */
	public static class BPResultRowPanel {
		private static final long serialVersionUID = 1L;

		private static ImageIcon passIcon = Images.BP_PASS_DARK.getIcon();
		private static ImageIcon failIcon = Images.BP_FAIL_DARK.getIcon();
		private static ImageIcon notRunIcon = Images.BP_SELFTEST_TRIGGERED
				.getIcon();
		private static ImageIcon manualIcon = Images.BP_MANUAL.getIcon();
		private String PASS = rb.getString("bestPractice.tooltip.pass");
		private String FAIL = rb.getString("bestPractice.tooltip.fail");
		private String MANUAL = rb.getString("bestPractice.tooltip.manual");

		private boolean selfTest;
		private JLabel titleLabel;
		private JLabel iconLabel;
		private HyperlinkLabel referSectionLabel;

		/**
		 * Initializes a new instance of the
		 * AROBpOverallResulsPanel.BPResultRowPanel class.
		 * 
		 * @param selfTest
		 *            - A boolean value that indicates whether this Best
		 *            Practice test is a “self test”.
		 * @param bpTitle
		 *            - The title of the Best Practice test.
		 * @param referMsg
		 *            - A message that refers users to more information about
		 *            the Best Practice test.
		 */
		public BPResultRowPanel(boolean selfTest, String bpTitle,
				String referMsg) {
			this.selfTest = selfTest;

			this.iconLabel = new JLabel(notRunIcon);
			this.titleLabel = new JLabel(bpTitle);
			titleLabel.setFont(textFont);
			this.referSectionLabel = new HyperlinkLabel(referMsg);
			referSectionLabel.setFont(boldTextFont);
			referSectionLabel.setVisible(false);
		}

		/**
		 * Refreshes the icons and adds the refer section label when a trace
		 * file is loaded.
		 * 
		 * @param isPass
		 *            - A boolean value that indicates whether the Best Practice
		 *            tests has passed or failed.
		 */
		public void refreshFields(Boolean isPass) {
			if (isPass == null) {
				iconLabel.setIcon(notRunIcon);
				iconLabel.setToolTipText(null);
				referSectionLabel.setVisible(false);
			} else if (selfTest) {
				iconLabel.setIcon(manualIcon);
				iconLabel.setToolTipText(MANUAL);
				referSectionLabel.setVisible(true);
			} else {
				if (isPass.booleanValue()) {
					iconLabel.setIcon(passIcon);
					iconLabel.setToolTipText(PASS);
				} else {
					iconLabel.setIcon(failIcon);
					iconLabel.setToolTipText(FAIL);
				}
				referSectionLabel.setVisible(!isPass.booleanValue());
			}
		}

		/**
		 * Returns the Title label from this row of BestPractices information.
		 * 
		 * @return A JLabel object containing the BestPractice Title label.
		 */
		public JLabel getTitleLabel() {
			return titleLabel;
		}

		/**
		 * Returns the icon in this row of Best Practice information.
		 * 
		 * @return A JLabel object containing the BestPractices icon label.
		 */
		public JLabel getIconLabel() {
			return iconLabel;
		}

		/**
		 * Returns the click-able label from the Refer section of the
		 * BestPractice information.
		 * 
		 * @return A HyperlinkLabel object that is the click-able label from the
		 *         Refer section.
		 */
		public HyperlinkLabel getReferSectionLabel() {
			return referSectionLabel;
		}

	}
}
