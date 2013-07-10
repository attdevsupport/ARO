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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.bp.BestPracticeDisplayGroup;
import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.HyperlinkLabel;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.commonui.RoundedBorder;
import com.att.aro.images.Images;
import com.att.aro.model.TraceData;

/**
 * Represents the over all results section of the Best Practices tab.
 */
public class AROBpOverallResulsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Font TEXT_FONT = new Font("TextFont", Font.PLAIN, 12);
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD, 14);
	private static final Font SUMMARY_FONT = new Font("HeaderFont", Font.BOLD, 18);
	private static final Font TEXT_FONT_BOLD = new Font("BoldTextFont", Font.BOLD, 12);
	private static final int HEADER_DATA_SPACING = 10;
	private static final ResourceBundle RB = ResourceBundleManager.getDefaultBundle();
	private static final Insets BASIC_INSETS = new Insets(0, 0, 0, 0);
	private static final Insets TESTS_CONDUCTED_INSETS = new Insets(10, 8, 10, 10);
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private static final int ICON_GRIDX_1ST_COLUMN = 0;
	private static final int TITLE_GRIDX_1ST_COLUMN = 1;
	private static final int ICON_GRIDX_2ST_COLUMN = 2;
	private static final int TITLE_GRIDX_2ST_COLUMN = 3;

	private DateTraceAppDetailPanel dateTraceAppDetailPanel;

	private JLabel durationValueLabel;
	private JLabel totalDataValueLabel;
	private JLabel energyConsumedValueLabel;
	private JTextPane causesScoreValueLabel;
	private JTextPane effectsScoreValueLabel;
	private JTextPane totalAppScoreValueLabel;

	private ApplicationResourceOptimizer parent;
	private Map<BestPracticeDisplayGroup, List<BPResultRowPanel>> panelMap = new HashMap<BestPracticeDisplayGroup, List<BPResultRowPanel>>();
	private List<BPResultRowPanel> panels = new ArrayList<BPResultRowPanel>();
	private Collection<BestPracticeDisplayGroup> bpGroups;

	/**
	 * Initializes a new instance of the AROBpOverallResulsPanel class.
	 */
	public AROBpOverallResulsPanel(ApplicationResourceOptimizer parent, Collection<BestPracticeDisplayGroup> bpGroups) {

		this.parent = parent;
		this.bpGroups = bpGroups;
		setResultPanelParameters();

		int gridY = 0;
		gridY = addDateAndTraceInfo(gridY);
		gridY = addTestsConductedHeader(gridY);
		gridY = addTestsConductedSummary(gridY);
	}

	/**
	 * Adds summary for conducted tests.
	 * 
	 * @param gridY
	 *            Grid Y
	 * @return Current grid Y
	 */
	private int addTestsConductedSummary(int gridY) {
		int testCounter = 0;
		final int halfNumberOfTest = getNumberOfTests() / 2 + getNumberOfTests() % 2;
		for (BestPracticeDisplayGroup bpGroup : this.bpGroups) {
			String refer = MessageFormat.format(RB.getString("bestPractice.referSection"), bpGroup.getReferSectionName());
			Collection<BestPracticeDisplay> bps = bpGroup.getBestPractices();
			List<BPResultRowPanel> list = new ArrayList<BPResultRowPanel>(bps.size());
			for (BestPracticeDisplay bp : bps) {
				++testCounter;
				if (testCounter < halfNumberOfTest) {
					gridY = addGridCell(gridY, ICON_GRIDX_1ST_COLUMN, TITLE_GRIDX_1ST_COLUMN, refer, list, bp);
				} else if (testCounter == halfNumberOfTest) {
					gridY = addGridCell(gridY, ICON_GRIDX_1ST_COLUMN, TITLE_GRIDX_1ST_COLUMN, refer, list, bp);
					gridY -= halfNumberOfTest;
				} else {
					gridY = addGridCell(gridY, ICON_GRIDX_2ST_COLUMN, TITLE_GRIDX_2ST_COLUMN, refer, list, bp);
				}
			}
			this.panelMap.put(bpGroup, Collections.unmodifiableList(list));
		}
		return gridY;
	}

	/**
	 * Adds test result icon and test tile into a grid.
	 * 
	 * @param gridY
	 *            Y grid
	 * @param iconGridX
	 *            Icon grid X
	 * @param titleGridX
	 *            Test title grid X
	 * @param refer
	 *            A message that refers users to more information about the Best
	 *            Practice test.
	 * @param list
	 *            Best Practice result row panel.
	 * @param bp
	 *            Best Practice display
	 * @return Y grid
	 */
	private int addGridCell(int gridY, int iconGridX, int titleGridX, String refer, List<BPResultRowPanel> list, BestPracticeDisplay bp) {
		BPResultRowPanel bpp = new BPResultRowPanel(bp, refer);
		this.add(bpp.getIconLabel(), getTestsConductedGridConstraints(iconGridX, gridY));
		this.add(bpp.getTitleLabel(), getTestsConductedGridConstraints(titleGridX, gridY));
		this.panels.add(bpp);
		list.add(bpp);
		return ++gridY;
	}

	/**
	 * Gets number of tests conducted.
	 * 
	 * @return Number of tests conducted
	 */
	private int getNumberOfTests() {
		int counter = 0;
		for (BestPracticeDisplayGroup bpGroup : this.bpGroups) {
			Collection<BestPracticeDisplay> bps = bpGroup.getBestPractices();
			counter += bps.size();
		}
		return counter;
	}	

	/**
	 * Gets grid constraints for tests conducted data.
	 * 
	 * @param gridX
	 *            X grid
	 * @param gridY
	 *            Y grid
	 * @return Tests conducted grid constraints
	 */
	private GridBagConstraints getTestsConductedGridConstraints(int gridX, int gridY) {
		return new GridBagConstraints(gridX, gridY, 1, 1, 1, 1, GridBagConstraints.LINE_START, 
				GridBagConstraints.NONE, TESTS_CONDUCTED_INSETS, 0, 0);
	}

	/**
	 * Adds Test Conducted header.
	 * 
	 * @param gridY
	 *            Grid Y
	 * @return Current grid Y
	 */
	private int addTestsConductedHeader(int gridY) {
		JLabel testConductedHeaderLabel = new JLabel(RB.getString("bestPractices.header.testsConducted"));
		testConductedHeaderLabel.setBackground(Color.WHITE);
		testConductedHeaderLabel.setFont(HEADER_FONT);
		add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
				new GridBagConstraints(0, gridY++, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			    new Insets(10, 0, 10, 0), 0, 0));
		add(testConductedHeaderLabel, new GridBagConstraints(0, gridY++, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, BASIC_INSETS, 0, 0));
		return gridY;
	}

	/**
	 * Adds date and trace information.
	 * 
	 * @param gridY
	 *            Grid Y
	 * @return Current grid Y
	 */
	private int addDateAndTraceInfo(int gridY) {
		// Add date panel
		this.dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		this.add(dateTraceAppDetailPanel, new GridBagConstraints(0, gridY++, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				BASIC_INSETS, 0, 0));
		this.add(createTestStatisticsPanel(), new GridBagConstraints(0, gridY++, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				BASIC_INSETS, 0, 0));
		return gridY;
	}

	/**
	 * Sets basic layout parameters for the Result panel.
	 */
	private void setResultPanelParameters() {
		this.setLayout(new GridBagLayout());
		this.setOpaque(false);
		this.setBorder(new RoundedBorder(new Insets(20, 20, 20, 20), Color.WHITE));
	}

	/**
	 * Returns the date, trace and application detail panel Best Practices test.
	 * 
	 * @return DateTraceAppDetailPanel
	 */
	public DateTraceAppDetailPanel getDateTraceAppDetailPanel() {
		return dateTraceAppDetailPanel;
	}

	/**
	 * Returns the list of result row panels contained in the overall results
	 * for the specified best practice group.
	 * 
	 * @param bpGroup
	 *            The group whose panels are to be returned or null to get all.
	 * @return The resulting list of row panels or null if an invalid bpGroup is
	 *         specified
	 */
	public List<BPResultRowPanel> getResultRowPanels(BestPracticeDisplayGroup bpGroup) {
		return bpGroup != null ? panelMap.get(bpGroup) : panels;
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
			durationValueLabel.setText(MessageFormat.format(
					RB.getString("bestPractices.durationValue"),
					nf.format(analysisData.getTraceData().getTraceDuration() / 60)));
			energyConsumedValueLabel.setText(MessageFormat.format(
					RB.getString("bestPractices.energyConsumedValue"),
					nf.format(analysisData.getEnergyModel().getTotalEnergyConsumed())));
			causesScoreValueLabel.setText(MessageFormat.format(RB
					.getString("bestPractices.scoreref"), analysisData.getApplicationScore()
					.getCausesScore())
					+ " " + RB.getString("bestPractices.outofscore1"));
			effectsScoreValueLabel.setText(MessageFormat.format(RB
					.getString("bestPractices.scoreref"), analysisData.getApplicationScore()
					.getEffectScore())
					+ " " + RB.getString("bestPractices.outofscore1"));
			totalAppScoreValueLabel.setText(MessageFormat.format(RB
					.getString("bestPractices.scoreref"), analysisData.getApplicationScore()
					.getTotalApplicationScore())
					+ " " + RB.getString("bestPractices.outofscore2"));
			NumberFormat intf = NumberFormat.getIntegerInstance();
			totalDataValueLabel.setText(MessageFormat.format(
					RB.getString("bestPractices.totalDataTransferedValue"),
					intf.format(analysisData.getTotalBytes())));

			for (BPResultRowPanel bpp : panels) {
				bpp.refreshFields(analysisData);
			}
		} else {
			durationValueLabel.setText(null);
			energyConsumedValueLabel.setText(null);
			totalDataValueLabel.setText(null);
			causesScoreValueLabel.setText(null);
			effectsScoreValueLabel.setText(null);
			totalAppScoreValueLabel.setText(null);

			for (BPResultRowPanel bpp : panels) {
				bpp.refreshFields(null);
			}
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

		JLabel summaryHeaderLabel = new JLabel(RB.getString("bestPractices.header.summary"));
		summaryHeaderLabel.setBackground(Color.WHITE);
		summaryHeaderLabel.setFont(SUMMARY_FONT);

		JLabel statisticsHeaderLabel = new JLabel(RB.getString("bestPractices.header.statistics"));
		statisticsHeaderLabel.setBackground(Color.WHITE);
		statisticsHeaderLabel.setFont(HEADER_FONT);

		JPanel durationPanel = new JPanel(new GridLayout(1, 2));
		durationPanel.setBackground(Color.WHITE);
		JLabel durationLabel = new JLabel(RB.getString("bestPractices.duration"));
		durationLabel.setFont(TEXT_FONT);
		durationValueLabel = new JLabel();
		durationValueLabel.setFont(TEXT_FONT);
		durationPanel.add(durationLabel);
		durationPanel.add(durationValueLabel);

		JPanel totalDataPanel = new JPanel(new GridLayout(1, 2));
		totalDataPanel.setBackground(Color.WHITE);
		JLabel totalDataLabel = new JLabel(RB.getString("bestPractices.totalDataTransfered"));
		totalDataLabel.setFont(TEXT_FONT);
		totalDataValueLabel = new JLabel();
		totalDataValueLabel.setFont(TEXT_FONT);
		totalDataPanel.add(totalDataLabel);
		totalDataPanel.add(totalDataValueLabel);

		JPanel energyConsumedPanel = new JPanel(new GridLayout(1, 2));
		energyConsumedPanel.setBackground(Color.WHITE);
		JLabel energyConsumedLabel = new JLabel(RB.getString("bestPractices.energyConsumed"));
		energyConsumedLabel.setFont(TEXT_FONT);
		energyConsumedValueLabel = new JLabel();
		energyConsumedValueLabel.setFont(TEXT_FONT);
		energyConsumedPanel.add(energyConsumedLabel);
		energyConsumedPanel.add(energyConsumedValueLabel);

		final MouseAdapter appScoreMouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (parent.getTraceData() != null) {
					parent.displayResultTab();
					parent.getAnalysisResultsPanel().getVerticalScrollBar().setValue(550);
				}
			}
		};

		JPanel appScoreTitlePanel = new JPanel(new GridLayout(1, 2));
		appScoreTitlePanel.setBackground(Color.WHITE);
		JLabel appScoreLabel = new JLabel(RB.getString("appscore.title"));
		appScoreLabel.setFont(HEADER_FONT);
		appScoreTitlePanel.add(appScoreLabel);

		JPanel causesScorePanel = new JPanel(new GridLayout(1, 2));
		causesScorePanel.setBackground(Color.WHITE);
		JLabel causesScoreLabel = new JLabel(RB.getString("bestPractices.causesScore"));
		causesScoreLabel.setFont(TEXT_FONT);
		causesScorePanel.add(causesScoreLabel);
		causesScoreValueLabel = createJTextPane();
		causesScoreValueLabel.setFont(TEXT_FONT);
		causesScorePanel.add(causesScoreValueLabel);
		causesScoreValueLabel.addMouseListener(appScoreMouseAdapter);

		JPanel effectsScorePanel = new JPanel(new GridLayout(1, 2));
		effectsScorePanel.setBackground(Color.WHITE);
		JLabel effectsScoreLabel = new JLabel(RB.getString("bestPractices.effectsScore"));
		effectsScoreLabel.setFont(TEXT_FONT);
		effectsScorePanel.add(effectsScoreLabel);
		effectsScoreValueLabel = createJTextPane();
		effectsScoreValueLabel.setFont(TEXT_FONT);
		effectsScorePanel.add(effectsScoreValueLabel);
		effectsScoreValueLabel.addMouseListener(appScoreMouseAdapter);

		JPanel totalScorePanel = new JPanel(new GridLayout(1, 2));
		totalScorePanel.setBackground(Color.WHITE);
		JLabel totalAppScoreLabel = new JLabel(RB.getString("bestPractices.totalAppScore"));
		totalAppScoreLabel.setFont(TEXT_FONT);
		totalScorePanel.add(totalAppScoreLabel);
		totalAppScoreValueLabel = createJTextPane();
		totalAppScoreValueLabel.setFont(TEXT_FONT);
		totalScorePanel.add(totalAppScoreValueLabel);
		totalAppScoreValueLabel.addMouseListener(appScoreMouseAdapter);

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
		titlePanel.add(getSpacePanel());
		titlePanel.add(appScoreTitlePanel);
		titlePanel.add(causesScorePanel);
		titlePanel.add(effectsScorePanel);
		titlePanel.add(totalScorePanel);
		titlePanel.add(testFillerHeaderLabel);
		subContentPanel.add(titlePanel, BorderLayout.CENTER);
		statisticsPanel.add(subContentPanel);

		statisticsLeftAllignmentPanel.add(statisticsPanel, BorderLayout.WEST);
		return statisticsLeftAllignmentPanel;

	}

	private JTextPane createJTextPane() {
		HTMLDocument doc = new HTMLDocument();
		StyleSheet style = doc.getStyleSheet();
		style.addRule("body { font-family: " + TEXT_FONT.getFamily() + "; " + "font-size: "
				+ TEXT_FONT.getSize() + "pt; }");
		style.addRule("a { text-decoration: underline; font-weight:bold; }");
		JTextPane jTextArea = new JTextPane(doc);
		jTextArea.setEditable(false);
		jTextArea.setEditorKit(new HTMLEditorKit());
		jTextArea.setStyledDocument(doc);
		jTextArea.setMargin(new Insets(0, 0, 0, 0));
		jTextArea.setPreferredSize(new Dimension(50, 16));
		return jTextArea;
	}

	/**
	 * Creates a space panel
	 * 
	 * @return
	 */
	private JPanel getSpacePanel() {
		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(), HEADER_DATA_SPACING));
		spacePanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		return spacePanel;
	}

	/**
	 * Represents a row of Best Practice information containing 3 icons and the
	 * Best Practice Label.
	 * 
	 */
	public static class BPResultRowPanel {

		private static final ImageIcon PASS_ICON = Images.BP_PASS_DARK.getIcon();
		private static final ImageIcon FAIL_ICON = Images.BP_FAIL_DARK.getIcon();
		private static ImageIcon WARNING_ICON = Images.BP_WARNING_DARK.getIcon();
		private static final ImageIcon NOT_RUN_ICON = Images.BP_SELFTEST_TRIGGERED.getIcon();
		private static final ImageIcon MANUAL_ICON = Images.BP_MANUAL.getIcon();
		private static final String PASS = RB.getString("bestPractice.tooltip.pass");
		private static final String FAIL = RB.getString("bestPractice.tooltip.fail");
		private static String WARNING = rb.getString("bestPractice.tooltip.warning");
		private static final String MANUAL = RB.getString("bestPractice.tooltip.manual");
		private static final String SELFTEST = RB.getString("bestPractices.selfTest");

		private BestPracticeDisplay bp;
		private JLabel titleLabel;
		private JLabel iconLabel;
		private HyperlinkLabel referSectionLabel;

		/**
		 * Initializes a new instance of the
		 * AROBpOverallResulsPanel.BPResultRowPanel class.
		 * 
		 * @param bp
		 *            Data for the best practice to be displayed
		 * @param referMsg
		 *            A message that refers users to more information about the
		 *            Best Practice test.
		 */
		public BPResultRowPanel(BestPracticeDisplay bp, String referMsg) {
			this.bp = bp;

			this.iconLabel = new JLabel(NOT_RUN_ICON);
			this.titleLabel = new JLabel(bp.getOverviewTitle());
			titleLabel.setFont(TEXT_FONT);
			this.referSectionLabel = new HyperlinkLabel(bp.isSelfTest() ? SELFTEST : referMsg);
			referSectionLabel.setFont(TEXT_FONT_BOLD);
			referSectionLabel.setVisible(false);
		}

		/**
		 * Refreshes the icons and adds the refer section label when a trace
		 * file is loaded.
		 * 
		 * @param analysisData
		 *            The current analysis data used to refresh the fields in
		 *            the panel
		 */
		public void refreshFields(TraceData.Analysis analysis) {
			if (analysis == null) {
				iconLabel.setIcon(NOT_RUN_ICON);
				iconLabel.setToolTipText(null);
				referSectionLabel.setVisible(false);
			} else if (bp.isSelfTest()) {
				iconLabel.setIcon(MANUAL_ICON);
				iconLabel.setToolTipText(MANUAL);
				referSectionLabel.setVisible(true);
			} else {
				boolean isPass = bp.isPass(analysis);
				if (isPass) {
					iconLabel.setIcon(PASS_ICON);
					iconLabel.setToolTipText(PASS);
				}else{
					if(((bp.getOverviewTitle()).equals(rb.getString("caching.usingCache.title")))
							|| ((bp.getOverviewTitle()).equals(rb.getString("caching.cacheControl.title")))
							|| ((bp.getOverviewTitle()).equals(rb.getString("connections.offloadingToWifi.title")))
							|| ((bp.getOverviewTitle()).equals(rb.getString("html.httpUsage.title")))
							|| ((bp.getOverviewTitle()).equals(rb.getString("other.accessingPeripherals.title")))
							){
						iconLabel.setIcon(WARNING_ICON);
						iconLabel.setToolTipText(WARNING); 
					}else {
						iconLabel.setIcon(FAIL_ICON);
						iconLabel.setToolTipText(FAIL);
					}
				}
				referSectionLabel.setVisible(!isPass);
			}
		}

		/**
		 * Returns the best practice display element used by this row panel
		 * 
		 * @return the bp
		 */
		public BestPracticeDisplay getBp() {
			return bp;
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

	/**
	 * Method to add the Trace information content in the csv file.
	 * 
	 * @throws IOException
	 */
	public FileWriter addBpOverallContent(FileWriter writer, TraceData.Analysis analysisData)
			throws IOException {
		final String lineSep = System.getProperty(RB.getString("statics.csvLine.seperator"));
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		nf.setMinimumIntegerDigits(1);

		writer = addKeyValue(writer, RB.getString("bestPractices.duration"),
				String.valueOf(nf.format(analysisData.getTraceData().getTraceDuration() / 60)));

		writer.append(RB.getString("statics.csvCell.seperator"));
		writer.append(RB.getString("statics.csvUnits.minutes"));
		writer.append(lineSep);

		writer = addKeyValue(writer, RB.getString("bestPractices.totalDataTransfered"),
				String.valueOf(analysisData.getTotalBytes()));
		writer.append(RB.getString("statics.csvCell.seperator"));
		writer.append(RB.getString("statics.csvUnits.bytes"));
		writer.append(lineSep);

		writer = addKeyValue(writer, RB.getString("bestPractices.energyConsumed"),
				String.valueOf(nf.format(analysisData.getEnergyModel().getTotalEnergyConsumed()))
						.replace(RB.getString("statics.csvCell.seperator"), ""));

		writer.append(RB.getString("statics.csvCell.seperator"));
		writer.append(RB.getString("statics.csvUnits.joules"));
		writer.append(lineSep);

		writer = addKeyValue(writer, RB.getString("appscore.title"), String.valueOf(""));
		writer.append(lineSep);

		writer = addKeyValue(writer, RB.getString("exportall.causesScore"),
				String.valueOf(analysisData.getApplicationScore().getCausesScore()));
		writer.append(lineSep);

		writer = addKeyValue(writer, RB.getString("exportall.effectsScore"),
				String.valueOf(analysisData.getApplicationScore().getEffectScore()));
		writer.append(lineSep);

		writer = addKeyValue(writer, RB.getString("exportall.totalAppScore"),
				String.valueOf(analysisData.getApplicationScore().getTotalApplicationScore()));
		writer.append(lineSep);

		return writer;
	}

	/**
	 * Writes a provided key and value in the file writer.
	 * 
	 * @param writer
	 * @param key
	 * @param value
	 * @return
	 * @throws IOException
	 */
	private FileWriter addKeyValue(FileWriter writer, String key, String value) throws IOException {
		writer.append(key);
		writer.append(RB.getString("statics.csvCell.seperator"));
		writer.append(value);
		return writer;
	}
}
