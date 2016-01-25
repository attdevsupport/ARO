/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class BpTestStatisticsPanel extends AbstractBpPanel {

	private static final long serialVersionUID = 1L;
	
	private JLabel summaryHeaderLabel;
	private JLabel statisticsHeaderLabel;
	private JLabel httpsDataNotAnalyzedLabel;
	private JLabel durationLabel;
	private JLabel totalDataLabel;
	private JLabel energyConsumedLabel;
	private JLabel summaryFillerHeaderLabel;
	private JLabel testFillerHeaderLabel;

	private NumberFormat pctFmt = null;
	private NumberFormat intFormat = null;
	private NumberFormat numFormat;
	private DecimalFormat decFormat;

	public BpTestStatisticsPanel() {
		
		final int borderGap = 10;

		this.setLayout(new BorderLayout(borderGap, borderGap));
	//	this.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, borderGap, 0));

		pctFmt  = NumberFormat.getPercentInstance();
		pctFmt.setMaximumFractionDigits(2);
		intFormat = NumberFormat.getIntegerInstance();
		numFormat = NumberFormat.getNumberInstance();
		decFormat = new DecimalFormat("#.##");
		
	    summaryHeaderLabel             = new JLabel();
        statisticsHeaderLabel          = new JLabel();

        durationLabel                  = new JLabel();
        energyConsumedLabel            = new JLabel();
        httpsDataNotAnalyzedLabel      = new JLabel();
        summaryFillerHeaderLabel       = new JLabel();
        testFillerHeaderLabel          = new JLabel();
        totalDataLabel                 = new JLabel();

		JLabel appScoreLabel           = new JLabel();
		JLabel causesScoreLabel        = new JLabel();
		JLabel effectsScoreLabel       = new JLabel();
		JLabel httpsDataAnalyzedLabel  = new JLabel();
		JLabel totalAppScoreLabel      = new JLabel();
		JLabel totalhttpsDataLabel     = new JLabel();
        
        summaryHeaderLabel        .setFont(SUMMARY_FONT);
        statisticsHeaderLabel     .setFont(SUMMARY_FONT);
                                                         
        appScoreLabel             .setFont(TEXT_FONT);   
        causesScoreLabel          .setFont(TEXT_FONT);   
        durationLabel             .setFont(TEXT_FONT);   
        effectsScoreLabel         .setFont(TEXT_FONT);   
        energyConsumedLabel       .setFont(TEXT_FONT);   
        httpsDataAnalyzedLabel    .setFont(TEXT_FONT);   
        httpsDataNotAnalyzedLabel .setFont(TEXT_FONT);   
        summaryFillerHeaderLabel  .setFont(TEXT_FONT);   
        testFillerHeaderLabel     .setFont(TEXT_FONT);   
        totalAppScoreLabel        .setFont(TEXT_FONT);   
        totalDataLabel            .setFont(TEXT_FONT);   
        totalhttpsDataLabel       .setFont(TEXT_FONT);   
        
        add(layoutDataPanel(), BorderLayout.CENTER);

	}

	/**
	 * Creates the JPanel containing the Date , Trace and Application details
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {

		final double weightX = 0.5;
		
		if (dataPanel == null) {
			dataPanel = new JPanel(new GridBagLayout());
			dataPanel.setBackground(Color.WHITE);//UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

			Insets insets = new Insets(2, 2, 2, 2);
			int idx = 0;

			addLabelLine(summaryHeaderLabel        , "bestPractices.header.summary"         ,   idx ,2, weightX, insets, SUMMARY_FONT);  // 
			addLabelLine(testFillerHeaderLabel     , " "                                    , ++idx ,2, weightX, insets, TEXT_FONT);     // 		
			addLabelLine(statisticsHeaderLabel     , "bestPractices.header.statistics"      , ++idx ,2, weightX, insets, HEADER_FONT);   // 
			                                                                                                                             // 
			addLabelLine(httpsDataNotAnalyzedLabel , "bestPractices.HTTPSDataNotAnalyzed"   , ++idx ,2, weightX, insets, TEXT_FONT);     // HTTPS data not analyzed\:
			/*
			 * addLabelLine(totalhttpsDataLabel , "bestPractices.TotalHTTPSData"
			 * , ++idx ,2, weightX, insets, TEXT_FONT); // Total HTTPS data\:
			 * addLabelLine(httpsDataAnalyzedLabel ,
			 * "bestPractices.HTTPSDataAnalyzed" , ++idx ,2, weightX, insets,
			 * TEXT_FONT); // HTTPS data analyzed\:
			 */			
			addLabelLine(durationLabel             , "bestPractices.duration"               , ++idx ,2, weightX, insets, TEXT_FONT);     // 
			addLabelLine(totalDataLabel            , "bestPractices.totalDataTransfered"    , ++idx ,2, weightX, insets, TEXT_FONT);     // 
			addLabelLine(energyConsumedLabel       , "bestPractices.energyConsumed"         , ++idx ,2, weightX, insets, TEXT_FONT);     // 
			addLabelLine(summaryFillerHeaderLabel  , " "                                    , ++idx ,2, weightX, insets, TEXT_FONT);     // 
			
			
			/*
			 * // deprecated addLabelLine(appScoreLabel, "appscore.title",
			 * ++idx, 2, weightX, insets, HEADER_FONT); //
			 * addLabelLine(causesScoreLabel, "bestPractices.causesScore",
			 * ++idx, 2, weightX, insets, TEXT_FONT); //
			 * addLabelLine(effectsScoreLabel, "bestPractices.effectsScore",
			 * ++idx, 2, weightX, insets, TEXT_FONT); //
			 * addLabelLine(totalAppScoreLabel, "bestPractices.totalAppScore",
			 * ++idx, 2, weightX, insets, TEXT_FONT); //
			 */
			
		}
		return dataPanel;
	}

	@Override
	public int print(Graphics arg0, PageFormat arg1, int arg2) throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void refresh(AROTraceData model) {
				
		PacketAnalyzerResult analyzerResults = model.getAnalyzerResult();
		
		int httpsDataNotAnalyzed = analyzerResults.getStatistic().getTotalHTTPSByte();
		double httpsDataNotAnalyzedKB = (double)httpsDataNotAnalyzed/1024;
		double httpsDataNotAnalyzedPct = (double)httpsDataNotAnalyzed/analyzerResults.getStatistic().getTotalByte();

		
		httpsDataNotAnalyzedLabel.setText(MessageFormat.format(
				ResourceBundleHelper.getMessageString("bestPractices.HTTPSDataNotAnalyzedValue"),
				pctFmt.format(httpsDataNotAnalyzedPct), 
				decFormat.format(httpsDataNotAnalyzedKB)));
		
		// Total Data Transferred\:
		totalDataLabel.setText(MessageFormat.format(
				ResourceBundleHelper.getMessageString("bestPractices.totalDataTransferedValue"),
				intFormat.format(analyzerResults.getStatistic().getTotalByte())));
		
		// Duration:
		String duration = decFormat.format(analyzerResults.getTraceresult().getTraceDuration() / 60);
		durationLabel.setText(MessageFormat.format(
				ResourceBundleHelper.getMessageString("bestPractices.durationValue"),
				duration));

		// Energy Consumed:
		energyConsumedLabel.setText(MessageFormat.format(
				ResourceBundleHelper.getMessageString("bestPractices.energyConsumedValue"),
				numFormat.format(analyzerResults.getEnergyModel().getTotalEnergyConsumed())));
		
		//deprecated
//		// Causes:
//		causesScoreLabel.setText(MessageFormat.format(
//				ResourceBundleHelper.getMessageString("bestPractices.scoreref"),
//				analyzerResults.getApplicationScore().getCausesScore())
//				+ " " + ResourceBundleHelper.getMessageString("bestPractices.outofscore1"));
//		
//		// Effects:
//		effectsScoreLabel.setText(MessageFormat.format(
//				ResourceBundleHelper.getMessageString("bestPractices.scoreref"),
//				analyzerResults.getApplicationScore().getEffectScore())
//				+ " " + ResourceBundleHelper.getMessageString("bestPractices.outofscore1"));
//
//		// Total:
//		totalAppScoreLabel.setText(MessageFormat.format(
//				ResourceBundleHelper.getMessageString("bestPractices.scoreref"), 
//				analyzerResults.getApplicationScore().getTotalApplicationScore())
//				+ " " + ResourceBundleHelper.getMessageString("bestPractices.outofscore2"));

		
	}



}
