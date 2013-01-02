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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.TraceData;

/**
 * Represents the Application Score panel which displays detailed application
 * score on statistics tab.
 */
public class ApplicationScorePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD, 16);
	private static final Font SUB_TITLE_FONT = new Font("HeaderFont", Font.BOLD, 14);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);

	// Causes labels
	private JLabel cacheControlScoreLabel;
	private JLabel cathcControlScoreValueLabel;
	private JLabel connectionClosingScoreLabel;
	private JLabel connectionClosingScoreValueLabel;
	private JLabel tightlyGroupConnLabel;
	private JLabel tightlyGroupConnValueLabel;
	private JLabel periodicTransferLabel;
	private JLabel periodicTransferValueLabel;
	private JLabel causeSubTotalLabel;
	private JLabel causeSubTotalValueLabel;
	private JLabel contentExpirationScoreLabel;
	private JLabel contentExpirationScoreValueLabel;

	// Effects labels
	private JLabel duplicateContentLabel;
	private JLabel duplicateContentValueLabel;
	private JLabel signalingOverheadLabel;
	private JLabel signalingOverheadValueLabel;
	private JLabel averageRateLabel;
	private JLabel averageRateValueLabel;
	private JLabel energyConsumptionLabel;
	private JLabel energyConsumptionValueLabel;
	private JLabel effectSubTotalLabel;
	private JLabel effectSubTotalValueLabel;

	// Total Score
	private JLabel totalScoreLabel;
	private JLabel totalScoreValueLabel;
	private JLabel outOf50Label;
	private JLabel outOf75Label;
	private JLabel outOf75Label2;
	private JLabel outOf125Label;
	private JLabel outOf125Label2;
	private JLabel outOf150Label;
	private JLabel outOf150Label2;
	private JLabel outOf500Label;
	private JLabel outOf500Label2;
	private JLabel outOf625Label;
	private JLabel outOf1000Label;
	private JLabel outOf1825Label;

	Map<String, String> appScoreContent = new LinkedHashMap<String, String>();

	/**
	 * Initializes a new instance of the ApplicationScorePanel class.
	 */
	public ApplicationScorePanel() {
		super(new BorderLayout(10, 10));
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(createAppScorePanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel that contains the Application score data for the
	 * trace.
	 * 
	 * @return applicationScorePanel The application score JPanel.
	 */
	private JPanel createAppScorePanel() {

		JPanel applicationScoreLeftAlligmentPanel = new JPanel(new BorderLayout());
		applicationScoreLeftAlligmentPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		Insets insets = new Insets(2, 2, 2, 5);
		JPanel applicationScorePanel = new JPanel();
		applicationScorePanel.setLayout(new VerticalLayout());
		applicationScorePanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		final JLabel applicationScoreHeaderLabel = new JLabel(rb.getString("appscore.title"));
		applicationScoreHeaderLabel.setFont(HEADER_FONT);
		final JLabel causesSubTitleLabel = new JLabel(rb.getString("appscore.subtitle.causes"));
		causesSubTitleLabel.setFont(SUB_TITLE_FONT);
		final JLabel effectSubTitleLabel = new JLabel(rb.getString("appscore.subtitle.effects"));
		effectSubTitleLabel.setFont(SUB_TITLE_FONT);

		cacheControlScoreLabel = new JLabel(rb.getString("appscore.causes.cacheControl"));
		cacheControlScoreLabel.setFont(TEXT_FONT);
		cathcControlScoreValueLabel = new JLabel();
		cathcControlScoreValueLabel.setFont(TEXT_FONT);
		connectionClosingScoreLabel = new JLabel(rb.getString("appscore.causes.connectionClosing"));
		connectionClosingScoreLabel.setFont(TEXT_FONT);
		connectionClosingScoreValueLabel = new JLabel();
		connectionClosingScoreValueLabel.setFont(TEXT_FONT);
		tightlyGroupConnLabel = new JLabel(rb.getString("appscore.causes.tightlyGroupedConnection"));
		tightlyGroupConnLabel.setFont(TEXT_FONT);
		tightlyGroupConnValueLabel = new JLabel();
		tightlyGroupConnValueLabel.setFont(TEXT_FONT);
		contentExpirationScoreLabel = new JLabel(rb.getString("appscore.effects.contentExpiration"));
		contentExpirationScoreLabel.setFont(TEXT_FONT);
		contentExpirationScoreValueLabel = new JLabel();
		contentExpirationScoreValueLabel.setFont(TEXT_FONT);
		periodicTransferLabel = new JLabel(rb.getString("appscore.causes.periodicTransfers"));
		periodicTransferLabel.setFont(TEXT_FONT);
		periodicTransferValueLabel = new JLabel();
		periodicTransferValueLabel.setFont(TEXT_FONT);
		causeSubTotalLabel = new JLabel(rb.getString("appscore.causes.causeSubTotal"));
		causeSubTotalLabel.setFont(TEXT_FONT);
		causeSubTotalValueLabel = new JLabel();
		causeSubTotalValueLabel.setFont(TEXT_FONT);
		duplicateContentLabel = new JLabel(rb.getString("appscore.effects.duplicateContent"));
		duplicateContentLabel.setFont(TEXT_FONT);
		duplicateContentValueLabel = new JLabel();
		duplicateContentValueLabel.setFont(TEXT_FONT);
		signalingOverheadLabel = new JLabel(rb.getString("appscore.causes.signalingOverhead"));
		signalingOverheadLabel.setFont(TEXT_FONT);
		signalingOverheadValueLabel = new JLabel();
		signalingOverheadValueLabel.setFont(TEXT_FONT);
		averageRateLabel = new JLabel(rb.getString("appscore.effects.averageRate"));
		averageRateLabel.setFont(TEXT_FONT);
		averageRateValueLabel = new JLabel();
		averageRateValueLabel.setFont(TEXT_FONT);
		energyConsumptionLabel = new JLabel(rb.getString("appscore.effects.energyConsumption"));
		energyConsumptionLabel.setFont(TEXT_FONT);
		energyConsumptionValueLabel = new JLabel();
		energyConsumptionValueLabel.setFont(TEXT_FONT);
		effectSubTotalLabel = new JLabel(rb.getString("appscore.effect.effectSubTotal"));
		effectSubTotalLabel.setFont(TEXT_FONT);
		effectSubTotalValueLabel = new JLabel();
		effectSubTotalValueLabel.setFont(TEXT_FONT);
		totalScoreLabel = new JLabel(rb.getString("appscore.subtitle.total"));
		totalScoreLabel.setFont(SUB_TITLE_FONT);
		totalScoreValueLabel = new JLabel();
		totalScoreValueLabel.setFont(TEXT_FONT);
		outOf50Label = new JLabel();
		outOf50Label.setFont(TEXT_FONT);
		outOf75Label = new JLabel();
		outOf75Label.setFont(TEXT_FONT);
		outOf75Label2 = new JLabel();
		outOf75Label2.setFont(TEXT_FONT);
		outOf125Label = new JLabel();
		outOf125Label.setFont(TEXT_FONT);
		outOf125Label2 = new JLabel();
		outOf125Label2.setFont(TEXT_FONT);
		outOf150Label = new JLabel();
		outOf150Label.setFont(TEXT_FONT);
		outOf150Label2 = new JLabel();
		outOf150Label2.setFont(TEXT_FONT);
		outOf500Label = new JLabel();
		outOf500Label.setFont(TEXT_FONT);
		outOf500Label2 = new JLabel();
		outOf500Label2.setFont(TEXT_FONT);
		outOf625Label = new JLabel();
		outOf625Label.setFont(TEXT_FONT);
		outOf1000Label = new JLabel();
		outOf1000Label.setFont(TEXT_FONT);
		outOf1825Label = new JLabel();
		outOf1825Label.setFont(TEXT_FONT);

		JPanel applicationScoreSubPanel = new JPanel();
		applicationScoreSubPanel.setLayout(new GridBagLayout());
		applicationScoreSubPanel
				.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		applicationScoreSubPanel.add(applicationScoreHeaderLabel, new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(getPaddingLabel(), new GridBagConstraints(0, 1, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(causesSubTitleLabel, new GridBagConstraints(0, 2, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(cacheControlScoreLabel, new GridBagConstraints(0, 3, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(cathcControlScoreValueLabel, new GridBagConstraints(1, 3, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf75Label, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(connectionClosingScoreLabel, new GridBagConstraints(0, 4, 1,
				1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(connectionClosingScoreValueLabel, new GridBagConstraints(1, 4,
				1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf75Label2, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(tightlyGroupConnLabel, new GridBagConstraints(0, 5, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(tightlyGroupConnValueLabel, new GridBagConstraints(1, 5, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf150Label, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(periodicTransferLabel, new GridBagConstraints(0, 6, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(periodicTransferValueLabel, new GridBagConstraints(1, 6, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf150Label2, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(contentExpirationScoreLabel, new GridBagConstraints(0, 7, 1,
				1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(contentExpirationScoreValueLabel, new GridBagConstraints(1, 7,
				1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf50Label, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(causeSubTotalLabel, new GridBagConstraints(0, 8, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(causeSubTotalValueLabel, new GridBagConstraints(1, 8, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf500Label, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(getPaddingLabel(), new GridBagConstraints(0, 9, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(effectSubTitleLabel, new GridBagConstraints(0, 10, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(duplicateContentLabel, new GridBagConstraints(0, 11, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(duplicateContentValueLabel, new GridBagConstraints(1, 11, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf125Label, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(signalingOverheadLabel, new GridBagConstraints(0, 12, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(signalingOverheadValueLabel, new GridBagConstraints(1, 12, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf125Label2, new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(averageRateLabel, new GridBagConstraints(0, 13, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(averageRateValueLabel, new GridBagConstraints(1, 13, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf625Label, new GridBagConstraints(2, 13, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(energyConsumptionLabel, new GridBagConstraints(0, 14, 1, 1,
				0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(energyConsumptionValueLabel, new GridBagConstraints(1, 14, 1,
				1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf1825Label, new GridBagConstraints(2, 14, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(effectSubTotalLabel, new GridBagConstraints(0, 15, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(effectSubTotalValueLabel, new GridBagConstraints(1, 15, 1, 1,
				0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf500Label2, new GridBagConstraints(2, 15, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(getPaddingLabel(), new GridBagConstraints(0, 16, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(totalScoreLabel, new GridBagConstraints(0, 17, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(totalScoreValueLabel, new GridBagConstraints(1, 17, 1, 1, 0.0,
				0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		applicationScoreSubPanel.add(outOf1000Label, new GridBagConstraints(2, 17, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));

		applicationScorePanel.add(applicationScoreSubPanel);
		applicationScoreLeftAlligmentPanel.add(applicationScorePanel, BorderLayout.WEST);
		return applicationScoreLeftAlligmentPanel;
	}

	/**
	 * Refreshes the content of the Application score statistics Panel with the
	 * specified trace data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysis) {
		if (analysis != null) {
			cathcControlScoreValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getCacheHeaderControlScore()));
			connectionClosingScoreValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getConnectionClosingScore()));
			tightlyGroupConnValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getTightlyGroupConnectionScore()));
			signalingOverheadValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getSignalingOverheadScore()));
			periodicTransferValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getPeriodicTransferScore()));
			causeSubTotalValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getCausesScore()));
			duplicateContentValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getDuplicateContentScore()));
			contentExpirationScoreValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getConnectionExpirationScore()));
			averageRateValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getAverageRateScore()));
			energyConsumptionValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getEnergyEfficiencyScore()));
			effectSubTotalValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getEffectScore()));
			totalScoreValueLabel.setText(String.valueOf(analysis.getApplicationScore()
					.getTotalApplicationScore()));
			
			outOf50Label.setText(rb.getString("bestPractices.outofscore3"));
			outOf75Label.setText(rb.getString("bestPractices.outofscore4"));
			outOf75Label2.setText(rb.getString("bestPractices.outofscore4"));
			outOf125Label.setText(rb.getString("bestPractices.outofscore6"));
			outOf125Label2.setText(rb.getString("bestPractices.outofscore6"));
			outOf150Label.setText(rb.getString("bestPractices.outofscore5"));
			outOf150Label2.setText(rb.getString("bestPractices.outofscore5"));
			outOf500Label.setText(rb.getString("bestPractices.outofscore1"));
			outOf500Label2.setText(rb.getString("bestPractices.outofscore1"));
			outOf625Label.setText(rb.getString("bestPractices.outofscore7"));
			outOf1000Label.setText(rb.getString("bestPractices.outofscore2"));
			outOf1825Label.setText(rb.getString("bestPractices.outofscore8"));
		} else {
			cathcControlScoreValueLabel.setText(null);
			connectionClosingScoreValueLabel.setText(null);
			tightlyGroupConnValueLabel.setText(null);
			signalingOverheadValueLabel.setText(null);
			periodicTransferValueLabel.setText(null);
			causeSubTotalValueLabel.setText(null);
			duplicateContentValueLabel.setText(null);
			contentExpirationScoreValueLabel.setText(null);
			averageRateValueLabel.setText(null);
			energyConsumptionValueLabel.setText(null);
			effectSubTotalValueLabel.setText(null);
			totalScoreValueLabel.setText(null);
		}
	}

	/**
	 * Creates a padding label
	 * 
	 * @return
	 */
	private JLabel getPaddingLabel() {
		JLabel padding = new JLabel("  ");
		padding.setFont(new Font("TEXT_FONT", Font.PLAIN, 3));
		return padding;
	}

	/**
	 * Returns a Map object that contains basic statistics data.
	 * 
	 * @return A Map object containing basic statistics data.
	 */
	public Map<String, String> getBasicContent() {
		return appScoreContent;
	}

	/**
	 * Method to add the Trace information content in the csv file.
	 * 
	 * @throws IOException
	 */
	public FileWriter addApplicationScoreInfo(FileWriter writer, TraceData.Analysis analysisData)
			throws IOException {
		final String lineSep = System.getProperty(rb.getString("statics.csvLine.seperator"));

		// Causes scores added in to writer
		writer = addKeyValue(writer, rb.getString("appscore.subtitle.causes"), "");
		writer.append(lineSep);
		writer = addKeyValue(writer, cacheControlScoreLabel.getText(), cathcControlScoreValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore4"));
		writer.append(lineSep);
		writer = addKeyValue(
				writer,
				connectionClosingScoreLabel.getText(),
				connectionClosingScoreValueLabel.getText().replace(
						rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore4"));
		writer.append(lineSep);
		writer = addKeyValue(writer, tightlyGroupConnLabel.getText(), tightlyGroupConnValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore5"));
		writer.append(lineSep);
		writer = addKeyValue(writer, periodicTransferLabel.getText(), periodicTransferValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore5"));
		writer.append(lineSep);
		writer = addKeyValue(
				writer,
				contentExpirationScoreLabel.getText(),
				contentExpirationScoreValueLabel.getText().replace(
						rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore3"));
		writer.append(lineSep);
		writer = addKeyValue(writer, causeSubTotalLabel.getText(), causeSubTotalValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore1"));
		writer.append(lineSep);

		// Effects scores added in to writer
		writer = addKeyValue(writer, rb.getString("appscore.subtitle.effects"), "");
		writer.append(lineSep);
		writer = addKeyValue(writer, duplicateContentLabel.getText(), duplicateContentValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore6"));
		writer.append(lineSep);
		writer = addKeyValue(writer, signalingOverheadLabel.getText(), signalingOverheadValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore6"));
		writer.append(lineSep);
		writer = addKeyValue(writer, averageRateLabel.getText(), averageRateValueLabel.getText()
				.replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore7"));
		writer.append(lineSep);
		writer = addKeyValue(writer, energyConsumptionLabel.getText(), energyConsumptionValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore8"));
		writer.append(lineSep);
		writer = addKeyValue(writer, effectSubTotalLabel.getText(), effectSubTotalValueLabel
				.getText().replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore1"));
		writer.append(lineSep);
		writer = addKeyValue(writer, totalScoreLabel.getText(), totalScoreValueLabel.getText()
				.replace(rb.getString("statics.csvCell.seperator"), ""));
		writer.append(rb.getString("statics.csvCell.seperator"));
		writer.append(rb.getString("bestPractices.outofscore2"));
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
