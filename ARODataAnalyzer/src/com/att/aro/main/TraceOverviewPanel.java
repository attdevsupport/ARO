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
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

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

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.TraceData;

/**
 * Represents a Panel that displays the Trace Overview chart.
 */
public class TraceOverviewPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

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

	private double throughputPct;
	private double jpkbPct;
	private double promotionRatioPct;

	private double kbps;
	private double jpkb;
	private double promo;

	/**
	 * Initializes a new instance of the TraceOverviewPanel class.
	 */
	public TraceOverviewPanel() {
		super(new BorderLayout());
		initialize();
	}

	/**
	 * Sets the trace analysis data that is used to calculate the trace related
	 * information that is plotted on the chart.
	 * 
	 * @param analysis
	 *            - The trace analysis data.
	 */
	public void setAnalysisData(TraceData.Analysis analysis) {
		plot.setDataset(createDataset(analysis));
	}

	/**
	 * Initializes the Main panel and its various components.
	 */
	private void initialize() {
		JFreeChart chart = ChartFactory.createBarChart(
				rb.getString("overview.traceoverview.title"), null, null, createDataset(null),
				PlotOrientation.HORIZONTAL, false, true, false);
		chart.setBackgroundPaint(this.getBackground());
		chart.getTitle().setFont(AROUIManager.HEADER_FONT);

		this.plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

		CategoryAxis valueRangeAxis = plot.getDomainAxis();
		valueRangeAxis.setMaximumCategoryLabelWidthRatio(.5f);
		valueRangeAxis.setLabelFont(AROUIManager.LABEL_FONT);
		valueRangeAxis.setTickLabelFont(AROUIManager.LABEL_FONT);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setLabel(rb.getString("analysisresults.percentile"));
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
					traceInfo = rb.getString("tooltip.traceAnalysis.avg");
					break;
				case TRACE_ENERGY:
					traceInfo = rb.getString("tooltip.traceAnalysis.engy");
					break;
				case TRACE_OVERHEAD:
					traceInfo = rb.getString("tooltip.traceAnalysis.ovrhd");
					break;
				}

				return traceInfo;
			}
		});

		plot.setRenderer(renderer);
		plot.getDomainAxis().setMaximumCategoryLabelLines(2);

		ChartPanel chartPanel = new ChartPanel(chart, WIDTH, HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH, 100, ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, USER_BUFFER, PROPERTIES, COPY, SAVE, PRINT,
				ZOOM, TOOL_TIPS);

		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);

		this.add(chartPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates the plot data set from the current analysis
	 * 
	 * @param analysis
	 *            The analysis data for the trace
	 * @return CategoryDataset The plot data set for promotion ratio ,
	 *         throughput and J/Kb.
	 */
	private CategoryDataset createDataset(TraceData.Analysis analysis) {
		this.throughputPct = analysis != null ? analysis.calculateThroughputPercentage(analysis) : 0;
		this.jpkbPct = analysis != null ? analysis.calculateJpkbPercentage(analysis) : 0;
		this.promotionRatioPct = analysis != null ? analysis.calculatePromotionRatioPercentage(analysis) : 0;

		this.kbps = analysis != null ? analysis.getAvgKbps() : 0;
		this.jpkb = analysis != null ? analysis.getRrcStateMachine().getJoulesPerKilobyte() : 0;
		this.promo = analysis != null ? analysis.getRrcStateMachine().getPromotionRatio() : 0;

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);

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
						MessageFormat.format(rb.getString("overview.traceoverview.throughput"),
								nf.format(kbps)),
						MessageFormat.format(rb.getString("overview.traceoverview.jpkb"),
								nf.format(jpkb)),
						MessageFormat.format(rb.getString("overview.traceoverview.promoratio"),
								nf.format(promo)) }, data);
	}

	/**
	 * Adds the trace overview information in to provided writer.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void addTraceOverview(FileWriter writer) throws IOException {

		addKeyValue(writer, "", rb.getString("overview.traceoverview.value"),
				rb.getString("overview.traceoverview.percentile"));
		addKeyValue(writer, rb.getString("Export.traceoverview.throughput"), "" + kbps, ""
				+ throughputPct);
		addKeyValue(writer, rb.getString("Export.traceoverview.jpkb"), "" + jpkb, "" + jpkbPct);
		addKeyValue(writer, rb.getString("Export.traceoverview.promoratio"), "" + promo, ""
				+ promotionRatioPct);

	}

	/**
	 * method writes a provided key values in to the file writer.
	 * 
	 * @param writer
	 * @param key
	 * @param value
	 * @param value1
	 * @throws IOException
	 */
	private void addKeyValue(FileWriter writer, String key, String value, String value1)
			throws IOException {
		final String lineSep = System.getProperty(rb.getString("statics.csvLine.seperator"));
		writer.append(key);
		writer.append(',');
		writer.append(value);
		writer.append(',');
		writer.append(value1);
		writer.append(lineSep);
	}

}
