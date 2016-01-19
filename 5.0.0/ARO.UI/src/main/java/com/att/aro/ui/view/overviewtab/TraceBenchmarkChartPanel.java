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
import java.text.MessageFormat;
import java.text.NumberFormat;

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

import com.att.aro.core.packetanalysis.pojo.RrcStateMachine3G;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineWiFi;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.overview.TraceBenchmarkInfo;
import com.att.aro.ui.utils.CommonHelper;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.menu.ApplicationSampling;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class TraceBenchmarkChartPanel extends TabPanelJPanel{

	private static final long serialVersionUID = 1L;
	
	private static final int WIDTH = 430;
	private static final int HEIGHT = 170;
	private static final int TRACE_AVERAGE = 0;
	private static final int TRACE_ENERGY = 1;
	private static final int TRACE_OVERHEAD = 2;
	private static final double BAR_WIDTH_PERCENT = .2;
	// chart panel settings for constructor
	private static final boolean USER_BUFFER = false;
	private static final boolean PROPERTIES = false;
	private static final boolean COPY = false;
	private static final boolean SAVE = false;
	private static final boolean PRINT = false;
	private static final boolean ZOOM = false;
	private static final boolean TOOL_TIPS = true;

	private CategoryPlot plot;
	private TraceBenchmarkInfo traceBenchmarkData;
	private AROTraceData traceModel;
	
	
	/**
	 * 
	 * @return
	 */
	public JPanel layoutDataPanel(){
		setLayout(new BorderLayout());
		
		JFreeChart chart = initializeChart();
		
		ChartPanel chartPanel = new ChartPanel(chart, WIDTH, HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH + 100,100, ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, USER_BUFFER, PROPERTIES, COPY, SAVE, PRINT,
				ZOOM, TOOL_TIPS);
		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);
		add(chartPanel, BorderLayout.CENTER);
		
		return this;
	}

	private JFreeChart initializeChart(){
		
		JFreeChart chart = ChartFactory.createBarChart(
				ResourceBundleHelper.getMessageString("overview.traceoverview.title"), null, null, createDataset(),
				PlotOrientation.HORIZONTAL, false, true, false);
		chart.setBackgroundPaint(this.getBackground());
		chart.getTitle().setFont(AROUIManager.HEADER_FONT);

		this.plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

		CategoryAxis valueRangeAxis = plot.getDomainAxis();
		valueRangeAxis.setMaximumCategoryLabelWidthRatio(1.0f);
		valueRangeAxis.setMaximumCategoryLabelLines(2);
		valueRangeAxis.setLabelFont(AROUIManager.LABEL_FONT);
		valueRangeAxis.setTickLabelFont(AROUIManager.LABEL_FONT);
		 

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabel(ResourceBundleHelper.getMessageString("analysisresults.percentile"));
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
				String traceInfo = "";
				switch (arg2) {
				case TRACE_AVERAGE:
					traceInfo = ResourceBundleHelper.getMessageString("tooltip.traceAnalysis.avg");
					break;
				case TRACE_ENERGY:
					traceInfo = ResourceBundleHelper.getMessageString("tooltip.traceAnalysis.engy");
					break;
				case TRACE_OVERHEAD:
					traceInfo = ResourceBundleHelper.getMessageString("tooltip.traceAnalysis.ovrhd");
					break;
				default:
					break;
				}

				return traceInfo;
			}
		});

		plot.setRenderer(renderer);
		plot.getDomainAxis().setMaximumCategoryLabelLines(2);
		
		return chart;
		
	}

	/**
	 * Creates the plot data set from the current analysis
	 * 
	 * @return CategoryDataset The plot data set for promotion ratio ,
	 *         throughput and J/Kb.
	 */
	private CategoryDataset createDataset() {
		double throughputPct = 0;
		double jpkbPct = 0;
		double promotionRatioPct = 0;

		double kbps = 0;
		double jpkb = 0;
		double promo = 0;
		
		if(CommonHelper.isNotNull(traceBenchmarkData)){
		
			throughputPct = traceBenchmarkData.getThroughputPct();
			jpkbPct = traceBenchmarkData.getJpkbPct();
			promotionRatioPct = traceBenchmarkData.getPromotionRatioPct();

			kbps = traceBenchmarkData.getKbps();
			jpkb = traceBenchmarkData.getJpkb();
			promo = traceBenchmarkData.getPromoRatioPercentail();
		}

		NumberFormat nFormat = NumberFormat.getNumberInstance();
		nFormat.setMaximumFractionDigits(1);
		nFormat.setMinimumFractionDigits(1);

		double[][] data = new double[2][3];
		data[0][0] = throughputPct;
		data[0][1] = jpkbPct;
		data[0][2] = promotionRatioPct;
		data[1][0] = 100.0 - throughputPct;
		data[1][1] = 100.0 - jpkbPct;
		data[1][2] = 100.0 - promotionRatioPct;
		return DatasetUtilities.createCategoryDataset(
				new Integer[] { 1, 2 },
				new String[] {
						MessageFormat.format(ResourceBundleHelper.getMessageString("overview.traceoverview.throughput"),
								nFormat.format(kbps)),
						MessageFormat.format(ResourceBundleHelper.getMessageString("overview.traceoverview.jpkb"),
								nFormat.format(jpkb)),
						MessageFormat.format(ResourceBundleHelper.getMessageString("overview.traceoverview.promoratio"),
								nFormat.format(promo)) }, data);
	}

	
	/**
	 * @return the traceBenchmarkDat
	 */
	public TraceBenchmarkInfo getTraceBenchmarkInfo() {
		return traceBenchmarkData;
	}

	/**
	 * @param traceBenchmarkDat the traceBenchmarkDat to set
	 */
	public void setTraceBenchmarkInfo(TraceBenchmarkInfo traceBenchmarkData) {
		this.traceBenchmarkData = traceBenchmarkData;
		plot.setDataset(createDataset());
	}
	
	public TraceBenchmarkInfo generateDataForChart(){
		
		TraceBenchmarkInfo traceBenchmarkPojo = new TraceBenchmarkInfo();
		
		if(traceModel != null && traceModel.getAnalyzerResult() != null){
			double averageKbps = traceModel.getAnalyzerResult().getStatistic().getAverageKbps();
			traceBenchmarkPojo.setThroughputPct(ApplicationSampling.getInstance().getThroughputPercentile(averageKbps));
			
			double totalKbps = traceModel.getAnalyzerResult().getStatistic().getTotalByte() / 1024;
			traceBenchmarkPojo.setKbps(totalKbps);
			
			double promo = 0.0;
			double joulesPerKbps = 0.0;
			
			switch (traceModel.getAnalyzerResult().getStatemachine().getType()) {
				case Type3G:
					RrcStateMachine3G rrcState3G = (RrcStateMachine3G) traceModel.getAnalyzerResult().getStatemachine();
					joulesPerKbps = rrcState3G.getJoulesPerKilobyte();
					promo = rrcState3G.getPromotionRatio();	
					break;
				
				case LTE:
					RrcStateMachineLTE rrcStateLTE = (RrcStateMachineLTE) traceModel.getAnalyzerResult().getStatemachine();
					joulesPerKbps = rrcStateLTE.getJoulesPerKilobyte();
					promo = rrcStateLTE.getLteDrxLongRatio();
					break;
				case WiFi:
					RrcStateMachineWiFi rrcStateWifi = (RrcStateMachineWiFi) traceModel.getAnalyzerResult().getStatemachine();
					joulesPerKbps = rrcStateWifi.getJoulesPerKilobyte();
					promo = (rrcStateWifi.getWifiIdleTime() + rrcStateWifi.getWifiActiveTime()) / rrcStateWifi.getPacketsDuration();
					break;
				default:
					break;
					
			}
					
			traceBenchmarkPojo.setJpkb(joulesPerKbps);
				
			traceBenchmarkPojo.setJpkbPct(ApplicationSampling.getInstance().getJpkbPercentile(joulesPerKbps));
			
			traceBenchmarkPojo.setPromotionRatioPct(promo);
			traceBenchmarkPojo.setPromoRatioPercentail(ApplicationSampling.getInstance().getPromoRatioPercentile(promo));
			
			
		}

		
		
		
		return traceBenchmarkPojo;
	}
	
	public void refresh(AROTraceData aModel){
	
		traceModel = aModel;
		setTraceBenchmarkInfo(generateDataForChart());
		
	}
}
