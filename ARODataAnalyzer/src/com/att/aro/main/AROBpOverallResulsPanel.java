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

	private static Font textFont = new Font("TextFont", Font.PLAIN, 12);
	private static Font headerFont = new Font("HeaderFont", Font.BOLD, 14);
	private static Font summaryFont = new Font("HeaderFont", Font.BOLD, 18);
	private static Font boldTextFont = new Font("BoldTextFont", Font.BOLD, 12);
	private static final int HEADER_DATA_SPACING = 10;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

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

	/**
	 * Initializes a new instance of the AROBpOverallResulsPanel class.
	 */
	public AROBpOverallResulsPanel(ApplicationResourceOptimizer parent,
			Collection<BestPracticeDisplayGroup> bpGroups) {

		// Main panel settings
		this.parent = parent;
		setLayout(new GridBagLayout());
		setOpaque(false);
		setBorder(new RoundedBorder(new Insets(20, 20, 20, 20), Color.WHITE));

		int y = 0;

		// Add date panel
		Insets insets = new Insets(0, 0, 0, 0);
		dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		add(dateTraceAppDetailPanel, new GridBagConstraints(0, y++, 3, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		add(createTestStatisticsPanel(), new GridBagConstraints(0, y++, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		JLabel testConductedHeaderLabel = new JLabel(
				rb.getString("bestPractices.header.testsConducted"));
		testConductedHeaderLabel.setBackground(Color.WHITE);
		testConductedHeaderLabel.setFont(headerFont);
		add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE), new GridBagConstraints(0,
				y++, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(10, 0, 10, 0), 0, 0));
		add(testConductedHeaderLabel, new GridBagConstraints(0, y++, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));

		// Add the best practice overviews
		insets = new Insets(10, 20, 10, 10);
		for (BestPracticeDisplayGroup bpGroup : bpGroups) {
			String refer = MessageFormat.format(rb.getString("bestPractice.referSection"),
					bpGroup.getReferSectionName());
			Collection<BestPracticeDisplay> bps = bpGroup.getBestPractices();
			List<BPResultRowPanel> list = new ArrayList<BPResultRowPanel>(bps.size());
			for (BestPracticeDisplay bp : bps) {
				BPResultRowPanel bpp = new BPResultRowPanel(bp, refer);
				add(bpp.getIconLabel(), new GridBagConstraints(0, y, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
				add(bpp.getTitleLabel(), new GridBagConstraints(1, y, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
				add(bpp.getReferSectionLabel(), new GridBagConstraints(2, y, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
				panels.add(bpp);
				list.add(bpp);
				++y;
			}
			panelMap.put(bpGroup, Collections.unmodifiableList(list));
		}
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
					rb.getString("bestPractices.durationValue"),
					nf.format(analysisData.getTraceData().getTraceDuration() / 60)));
			energyConsumedValueLabel.setText(MessageFormat.format(
					rb.getString("bestPractices.energyConsumedValue"),
					nf.format(analysisData.getEnergyModel().getTotalEnergyConsumed())));
			causesScoreValueLabel.setText(MessageFormat.format(rb
					.getString("bestPractices.scoreref"), analysisData.getApplicationScore()
					.getCausesScore())
					+ " " + rb.getString("bestPractices.outofscore1"));
			effectsScoreValueLabel.setText(MessageFormat.format(rb
					.getString("bestPractices.scoreref"), analysisData.getApplicationScore()
					.getEffectScore())
					+ " " + rb.getString("bestPractices.outofscore1"));
			totalAppScoreValueLabel.setText(MessageFormat.format(rb
					.getString("bestPractices.scoreref"), analysisData.getApplicationScore()
					.getTotalApplicationScore())
					+ " " + rb.getString("bestPractices.outofscore2"));
			NumberFormat intf = NumberFormat.getIntegerInstance();
			totalDataValueLabel.setText(MessageFormat.format(
					rb.getString("bestPractices.totalDataTransferedValue"),
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

		JLabel summaryHeaderLabel = new JLabel(rb.getString("bestPractices.header.summary"));
		summaryHeaderLabel.setBackground(Color.WHITE);
		summaryHeaderLabel.setFont(summaryFont);

		JLabel statisticsHeaderLabel = new JLabel(rb.getString("bestPractices.header.statistics"));
		statisticsHeaderLabel.setBackground(Color.WHITE);
		statisticsHeaderLabel.setFont(headerFont);

		JPanel durationPanel = new JPanel(new GridLayout(1, 2));
		durationPanel.setBackground(Color.WHITE);
		JLabel durationLabel = new JLabel(rb.getString("bestPractices.duration"));
		durationLabel.setFont(textFont);
		durationValueLabel = new JLabel();
		durationValueLabel.setFont(textFont);
		durationPanel.add(durationLabel);
		durationPanel.add(durationValueLabel);

		JPanel totalDataPanel = new JPanel(new GridLayout(1, 2));
		totalDataPanel.setBackground(Color.WHITE);
		JLabel totalDataLabel = new JLabel(rb.getString("bestPractices.totalDataTransfered"));
		totalDataLabel.setFont(textFont);
		totalDataValueLabel = new JLabel();
		totalDataValueLabel.setFont(textFont);
		totalDataPanel.add(totalDataLabel);
		totalDataPanel.add(totalDataValueLabel);

		JPanel energyConsumedPanel = new JPanel(new GridLayout(1, 2));
		energyConsumedPanel.setBackground(Color.WHITE);
		JLabel energyConsumedLabel = new JLabel(rb.getString("bestPractices.energyConsumed"));
		energyConsumedLabel.setFont(textFont);
		energyConsumedValueLabel = new JLabel();
		energyConsumedValueLabel.setFont(textFont);
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
		JLabel appScoreLabel = new JLabel(rb.getString("appscore.title"));
		appScoreLabel.setFont(headerFont);
		appScoreTitlePanel.add(appScoreLabel);

		JPanel causesScorePanel = new JPanel(new GridLayout(1, 2));
		causesScorePanel.setBackground(Color.WHITE);
		JLabel causesScoreLabel = new JLabel(rb.getString("bestPractices.causesScore"));
		causesScoreLabel.setFont(textFont);
		causesScorePanel.add(causesScoreLabel);
		causesScoreValueLabel = createJTextPane();
		causesScoreValueLabel.setFont(textFont);
		causesScorePanel.add(causesScoreValueLabel);
		causesScoreValueLabel.addMouseListener(appScoreMouseAdapter);

		JPanel effectsScorePanel = new JPanel(new GridLayout(1, 2));
		effectsScorePanel.setBackground(Color.WHITE);
		JLabel effectsScoreLabel = new JLabel(rb.getString("bestPractices.effectsScore"));
		effectsScoreLabel.setFont(textFont);
		effectsScorePanel.add(effectsScoreLabel);
		effectsScoreValueLabel = createJTextPane();
		effectsScoreValueLabel.setFont(textFont);
		effectsScorePanel.add(effectsScoreValueLabel);
		effectsScoreValueLabel.addMouseListener(appScoreMouseAdapter);

		JPanel totalScorePanel = new JPanel(new GridLayout(1, 2));
		totalScorePanel.setBackground(Color.WHITE);
		JLabel totalAppScoreLabel = new JLabel(rb.getString("bestPractices.totalAppScore"));
		totalAppScoreLabel.setFont(textFont);
		totalScorePanel.add(totalAppScoreLabel);
		totalAppScoreValueLabel = createJTextPane();
		totalAppScoreValueLabel.setFont(textFont);
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
		style.addRule("body { font-family: " + textFont.getFamily() + "; " + "font-size: "
				+ textFont.getSize() + "pt; }");
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

		private static ImageIcon passIcon = Images.BP_PASS_DARK.getIcon();
		private static ImageIcon failIcon = Images.BP_FAIL_DARK.getIcon();
		private static ImageIcon notRunIcon = Images.BP_SELFTEST_TRIGGERED.getIcon();
		private static ImageIcon manualIcon = Images.BP_MANUAL.getIcon();
		private static String PASS = rb.getString("bestPractice.tooltip.pass");
		private static String FAIL = rb.getString("bestPractice.tooltip.fail");
		private static String MANUAL = rb.getString("bestPractice.tooltip.manual");
		private static String SELFTEST = rb.getString("bestPractices.selfTest");

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

			this.iconLabel = new JLabel(notRunIcon);
			this.titleLabel = new JLabel(bp.getOverviewTitle());
			titleLabel.setFont(textFont);
			this.referSectionLabel = new HyperlinkLabel(bp.isSelfTest() ? SELFTEST : referMsg);
			referSectionLabel.setFont(boldTextFont);
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
				iconLabel.setIcon(notRunIcon);
				iconLabel.setToolTipText(null);
				referSectionLabel.setVisible(false);
			} else if (bp.isSelfTest()) {
				iconLabel.setIcon(manualIcon);
				iconLabel.setToolTipText(MANUAL);
				referSectionLabel.setVisible(true);
			} else {
				boolean isPass = bp.isPass(analysis);
				if (isPass) {
					iconLabel.setIcon(passIcon);
					iconLabel.setToolTipText(PASS);
				} else {
					iconLabel.setIcon(failIcon);
					iconLabel.setToolTipText(FAIL);
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
		final String lineSep = System.getProperty(rb.getString("statics.csvLine.seperator"));
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		nf.setMinimumIntegerDigits(1);

		writer = addKeyValue(writer, rb.getString("bestPractices.duration"),
				String.valueOf(nf.format(analysisData.getTraceData().getTraceDuration() / 60)));

		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("statics.csvUnits.minutes"));
		writer.append(lineSep);

		writer = addKeyValue(writer, rb.getString("bestPractices.totalDataTransfered"),
				String.valueOf(analysisData.getTotalBytes()));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("statics.csvUnits.bytes"));
		writer.append(lineSep);

		writer = addKeyValue(writer, rb.getString("bestPractices.energyConsumed"),
				String.valueOf(nf.format(analysisData.getEnergyModel().getTotalEnergyConsumed()))
						.replace(rb.getString("statics.csvCell.seperator"), ""));

		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("statics.csvUnits.joules"));
		writer.append(lineSep);

		writer = addKeyValue(writer, rb.getString("appscore.title"), String.valueOf(""));
		writer.append(lineSep);

		writer = addKeyValue(writer, rb.getString("exportall.causesScore"),
				String.valueOf(analysisData.getApplicationScore().getCausesScore()));
		writer.append(lineSep);

		writer = addKeyValue(writer, rb.getString("exportall.effectsScore"),
				String.valueOf(analysisData.getApplicationScore().getEffectScore()));
		writer.append(lineSep);

		writer = addKeyValue(writer, rb.getString("exportall.totalAppScore"),
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
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(value);
		return writer;
	}
}
