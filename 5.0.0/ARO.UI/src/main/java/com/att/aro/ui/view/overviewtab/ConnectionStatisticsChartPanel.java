/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.view.overviewtab;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.TextAnchor;

import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.Termination;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.overview.ConnectionStatisticsInfo;
import com.att.aro.ui.utils.CommonHelper;
import com.att.aro.ui.utils.ResourceBundleHelper;


/**
 * @author Harikrishna Yaramachu
 *
 */
public class ConnectionStatisticsChartPanel extends TabPanelJPanel{
	
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 400;
	private static final int HEIGHT = 170;
	private static final int SESSION_TERMINATION = 0;
	private static final int SESSION_TIGHT_CONN = 1;
	private static final int SESSION_BURST = 2;
	private static final int SESSION_LONG_BURST = 3;
	private static final double BAR_WIDTH_PERCENT = .2;

	// chart panel settings for constructor
	private static final boolean USER_BUFFER = false;
	private static final boolean PROPERTIES = false;
	private static final boolean COPY = false;
	private static final boolean SAVE = false;
	private static final boolean PRINT = false;
	private static final boolean ZOOM = false;
	private static final boolean TOOL_TIPS = true;
	
	private static final double SESSION_TERMINATION_THRESHOLD = 1.0;
	
	
	private CategoryPlot plot;
	private ConnectionStatisticsInfo connectionStatInfo; 
	private AROTraceData traceDataModel;
		
	public JPanel layoutDataPanel(){
		setLayout(new BorderLayout());
		
		JFreeChart chart = initializeChart();
		
		ChartPanel chartPanel = new ChartPanel(chart, WIDTH, HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH + 100,
				100, ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, USER_BUFFER, PROPERTIES, COPY, SAVE, PRINT,
				ZOOM, TOOL_TIPS);

		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);
		add(chartPanel, BorderLayout.CENTER);
		
		return this;
	}

	public JFreeChart initializeChart(){
		JFreeChart chart = ChartFactory.createBarChart(
				ResourceBundleHelper.getMessageString("overview.sessionoverview.title"), null, null, createDataset(),
				PlotOrientation.HORIZONTAL, false, false, false);
		chart.setBackgroundPaint(this.getBackground());
		chart.getTitle().setFont(AROUIManager.HEADER_FONT);
		

		this.plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setMaximumCategoryLabelWidthRatio(1.0f);
		domainAxis.setMaximumCategoryLabelLines(2);
		domainAxis.setLabelFont(AROUIManager.LABEL_FONT);
		domainAxis.setTickLabelFont(AROUIManager.LABEL_FONT);
																							
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabel(ResourceBundleHelper.getMessageString("analysisresults.percentage"));
		rangeAxis.setRange(0.0, 100.0);
		rangeAxis.setTickUnit(new NumberTickUnit(10));
		rangeAxis.setLabelFont(AROUIManager.LABEL_FONT);
		rangeAxis.setTickLabelFont(AROUIManager.LABEL_FONT);
				

		BarRenderer renderer = new StackedBarRenderer();
		renderer.setBasePaint(AROUIManager.CHART_BAR_COLOR);
		renderer.setAutoPopulateSeriesPaint(false);
		renderer.setBaseItemLabelGenerator(new PercentLabelGenerator());
		renderer.setBaseItemLabelsVisible(true);
		renderer.setBaseItemLabelPaint(Color.black);

		// Make second bar in stack invisible
		renderer.setSeriesItemLabelsVisible(1, false);
		renderer.setSeriesPaint(1, new Color(0, 0, 0, 0));

		ItemLabelPosition insideItemlabelposition = new ItemLabelPosition(ItemLabelAnchor.INSIDE3,
				TextAnchor.CENTER_RIGHT);
		renderer.setBasePositiveItemLabelPosition(insideItemlabelposition);

		ItemLabelPosition outsideItemlabelposition = new ItemLabelPosition(
				ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT);
		renderer.setPositiveItemLabelPositionFallback(outsideItemlabelposition);
		
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setShadowVisible(false);
		renderer.setMaximumBarWidth(BAR_WIDTH_PERCENT);
		
		renderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
			@Override
			public String generateToolTip(CategoryDataset arg0, int arg1, int arg2) {
				String sessionInfo = "";
				switch (arg2) {
				case SESSION_TERMINATION:
					sessionInfo = ResourceBundleHelper.getMessageString("tooltip.sessionTermination");
					break;
				case SESSION_TIGHT_CONN:
					sessionInfo = ResourceBundleHelper.getMessageString("tooltip.sessionTightConn");
					break;
				case SESSION_BURST:
					sessionInfo = ResourceBundleHelper.getMessageString("tooltip.sessionBurst");
					break;
				case SESSION_LONG_BURST:
					sessionInfo = ResourceBundleHelper.getMessageString("tooltip.sessionLongBurst");
					break;
				default:
					break;
				}

				return sessionInfo;
			}
		});

		plot.setRenderer(renderer);
		plot.getDomainAxis().setMaximumCategoryLabelLines(2);
		
		return chart;
	}
	
	/**
	 * Creates the plot data set for the Connection statistics chart from the
	 * current analysis.
	 * 
	 * @param analysis
	 *            The analysis data for the trace.
	 * @return CategoryDataset The plot data set.
	 */
	private CategoryDataset createDataset() {
		double sessionTermPct = 0;
		double tightlyCoupledTCPPct = 0;
		double longBurstPct = 0;
		double nonPeriodicBurstPct = 0;
		
		if(CommonHelper.isNotNull(connectionStatInfo)){
		
			sessionTermPct = connectionStatInfo.getSessionTermPct();
			tightlyCoupledTCPPct = connectionStatInfo.getTightlyCoupledTCPPct();
			longBurstPct = connectionStatInfo.getLongBurstPct();
			nonPeriodicBurstPct = connectionStatInfo.getNonPeriodicBurstPct();
		}
		
		

		double[][] data = new double[2][4];
		data[0][0] = sessionTermPct;
		data[0][1] = tightlyCoupledTCPPct;
		data[0][2] = nonPeriodicBurstPct;
		data[0][3] = longBurstPct;
		data[1][0] = 100.0 - sessionTermPct;
		data[1][1] = 100.0 - tightlyCoupledTCPPct;
		data[1][2] = 100.0 - nonPeriodicBurstPct;
		data[1][3] = 100.0 - longBurstPct;
		return DatasetUtilities.createCategoryDataset(
				new Integer[] { 1, 2 },
				new String[] { ResourceBundleHelper.getMessageString("overview.sessionoverview.sessionTerm"),
						ResourceBundleHelper.getMessageString("overview.sessionoverview.tightlyGroupedBurstTerm"),
						ResourceBundleHelper.getMessageString("overview.sessionoverview.nonPeriodicBurstTerm"),
						ResourceBundleHelper.getMessageString("overview.sessionoverview.longBurstTerm") }, data);
	}

	/**
	 * @return the connectionStatInfo
	 */
	public ConnectionStatisticsInfo getConnectionStatInfo() {
		return connectionStatInfo;
	}

	/**
	 * @param connectionStatInfo the connectionStatInfo to set
	 */
	public void setConnectionStatInfo(ConnectionStatisticsInfo connectionStatInfo) {
		this.connectionStatInfo = connectionStatInfo;
		plot.setDataset(createDataset());
	}
	
	private ConnectionStatisticsInfo generateDataForChart(){
		ConnectionStatisticsInfo connectionStatisticsPojo = new ConnectionStatisticsInfo();
		
		double sessionTermPct = 0.0;
		int longBurstCount = 0;
		
		if(traceDataModel != null && traceDataModel.getAnalyzerResult()!= null){
			int termSessions = 0;
			int properTermSessions = 0;
			
			for(Session tcpSession : traceDataModel.getAnalyzerResult().getSessionlist()){
				if(!tcpSession.isUDP()){
					Termination termination = tcpSession.getSessionTermination();
					if(termination != null){
						++termSessions;
						if(termination.getSessionTerminationDelay() <= SESSION_TERMINATION_THRESHOLD){
							++properTermSessions;
						}
					}
				}
			}
			if(termSessions > 0){
			
				sessionTermPct = 100.0 * properTermSessions / termSessions;
			}	
			
			longBurstCount = traceDataModel.getAnalyzerResult().getBurstcollectionAnalysisData().getLongBurstCount();
		}
		connectionStatisticsPojo.setSessionTermPct(sessionTermPct);
		
		double tightlyCoupledTCPPct = 0.0;
		if(traceDataModel != null && traceDataModel.getAnalyzerResult()!= null){
			int burstSize = traceDataModel.getAnalyzerResult().getBurstcollectionAnalysisData().getBurstCollection().size();		
			int periodicBurstCount = 0;
			for (Burst burstInfo : traceDataModel.getAnalyzerResult().getBurstcollectionAnalysisData().getBurstCollection()){
				BurstCategory bCategory = burstInfo.getBurstCategory();
				if(bCategory == BurstCategory.PERIODICAL){
					periodicBurstCount += 1;
				}
			}
			
			double nonPeriodicBurstPct = 100 - 100.0 * periodicBurstCount /burstSize;
			connectionStatisticsPojo.setNonPeriodicBurstPct(nonPeriodicBurstPct);
		
			int tightlyCoupledBurstCount = 0;
			for (AbstractBestPracticeResult  abstractResult : traceDataModel.getBestPracticeResults()){
				if(abstractResult.getBestPracticeType().equals(BestPracticeType.UNNECESSARY_CONNECTIONS)){
					UnnecessaryConnectionResult unnecessaryConnt = (UnnecessaryConnectionResult)abstractResult;
					tightlyCoupledBurstCount = 	unnecessaryConnt.getTightlyCoupledBurstCount();
					
				}
			}
			tightlyCoupledTCPPct = 100.0 * tightlyCoupledBurstCount / burstSize;
			connectionStatisticsPojo.setTightlyCoupledTCPPct(tightlyCoupledTCPPct);
			double longBurstPct = 0.0;
			
			longBurstPct = 100.0 * longBurstCount / burstSize;
			connectionStatisticsPojo.setLongBurstPct(longBurstPct);
			
		}
		
		
		return connectionStatisticsPojo;
	}
	
	public void refresh(AROTraceData aModel){
		
		traceDataModel = aModel;
		setConnectionStatInfo(generateDataForChart());
		
	}
}
