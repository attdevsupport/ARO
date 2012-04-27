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
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.TextAnchor;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData;

/**
 * Represents a Panel for displayings a chart of File Types. 
 */
public class FileTypesChartPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private static final int WIDTH = 390;
	private static final int HEIGHT = 310;
	private static final int MAX_LIMIT_FILETYPES = 8;

	// chart panel settings for constructor
	private static final boolean USER_BUFFER = false;
	private static final boolean PROPERTIES = false;
	private static final boolean COPY = false;
	private static final boolean SAVE = false;
	private static final boolean PRINT = false;
	private static final boolean ZOOM = false;
	private static final boolean TOOL_TIPS = true;

	private List<FileTypeSummary> content;
	private CategoryPlot plot;

	private class FileTypeSummary implements Comparable<FileTypeSummary> {
		private String fileType;
		private long bytes;
		private double pct;

		public FileTypeSummary(String fileType) {
			this.fileType = fileType;
		}

		@Override
		public int compareTo(FileTypeSummary o) {

			// Sort descending
			return -Long.valueOf(bytes).compareTo(o.bytes);
		}

	}

	/**
	 * Initializes a new instance of the FileTypesChartPanel class.
	 */
	public FileTypesChartPanel() {
		super(new BorderLayout());
		initialize();
	}

	/**
	 * Sets the trace analysis data used for calculating the plot data for the chart. 
	 * 
	 * @param analysis - The trace analysis data.
	 */
	public synchronized void setAnalysisData(TraceData.Analysis analysis) {
		this.content = constructContent(analysis);
		double[][] data = new double[2][content.size()];
		String[] titles = new String[content.size()];
		for (int i = 0; i < content.size(); ++i) {
			FileTypeSummary summary = content.get(i);
			String key = summary.fileType;
			titles[i] = key;
			data[0][i] = summary.pct;
			data[1][i] = 100.0 - summary.pct;
		}

		// plot.setDataset(DatasetUtilities.createCategoryDataset("Percentile",
		// values));
		plot.setDataset(DatasetUtilities.createCategoryDataset(new Integer[] {
				1, 2 }, titles, data));
	}

	/**
	 * Initializes the File Type chart.
	 */
	private void initialize() {
		JFreeChart chart = ChartFactory.createBarChart(
				rb.getString("chart.filetype.title"), null,
				rb.getString("simple.percent"), null,
				PlotOrientation.HORIZONTAL, false, false, false);

		chart.setBackgroundPaint(this.getBackground());
		chart.getTitle().setFont(AROUIManager.HEADER_FONT);

		this.plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setMaximumCategoryLabelWidthRatio(.5f);
		domainAxis.setLabelFont(AROUIManager.LABEL_FONT);
		domainAxis.setTickLabelFont(AROUIManager.LABEL_FONT);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
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

		renderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
			@Override
			public String generateToolTip(CategoryDataset dataset, int row,
					int column) {

				FileTypeSummary summary = content.get(column);

				return MessageFormat.format(rb
						.getString("chart.filetype.tooltip"), NumberFormat
						.getIntegerInstance().format(summary.bytes));
			}
		});

		ItemLabelPosition insideItemlabelposition = new ItemLabelPosition(
				ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT);
		renderer.setBasePositiveItemLabelPosition(insideItemlabelposition);

		ItemLabelPosition outsideItemlabelposition = new ItemLabelPosition(
				ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT);
		renderer.setPositiveItemLabelPositionFallback(outsideItemlabelposition);

		BarPainter painter = new StandardBarPainter();
		renderer.setBarPainter(painter);
		renderer.setShadowVisible(false);
		renderer.setMaximumBarWidth(0.1);

		plot.setRenderer(renderer);
		plot.getDomainAxis().setMaximumCategoryLabelLines(2);

		ChartPanel chartPanel = new ChartPanel(chart, WIDTH, HEIGHT, 200,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, USER_BUFFER,
				PROPERTIES, COPY, SAVE, PRINT, ZOOM, TOOL_TIPS);

		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);

		this.add(chartPanel, BorderLayout.CENTER);
	}

	/**
	 * Creates the FilTypes list to be plotted on the chart.
	 */
	private List<FileTypeSummary> constructContent(
			TraceData.Analysis analysisData) {
		Map<String, FileTypeSummary> content = new HashMap<String, FileTypeSummary>();
		int totalContentLength = 0;
		if (analysisData != null) {
			for (TCPSession tcp : analysisData.getTcpSessions()) {
				for (HttpRequestResponseInfo info : tcp
						.getRequestResponseInfo()) {
					if (Direction.RESPONSE.equals(info.getDirection())) {
						long contentLength = info.getActualByteCount();
						if (contentLength > 0) {
							String contentType = info.getContentType();
							if (contentType == null
									|| contentType.trim().length() == 0) {
								contentType = rb
										.getString("chart.filetype.unknown");
							}
							FileTypeSummary summary = content.get(contentType);
							if (summary == null) {
								summary = new FileTypeSummary(contentType);
								content.put(contentType, summary);
							}
							summary.bytes += contentLength;
							totalContentLength += contentLength;
						}
					}
				}
			}
		}

		List<FileTypeSummary> result = new ArrayList<FileTypeSummary>(
				content.values());
		Collections.sort(result);

		if (result.size() > MAX_LIMIT_FILETYPES) {
			long otherValuesTotal = 0;

			Iterator<FileTypeSummary> iterator = result.iterator();
			for (int index = 0; index < (MAX_LIMIT_FILETYPES - 1); index++) {
				iterator.next();
			}
			while (iterator.hasNext()) {
				otherValuesTotal += iterator.next().bytes;
				iterator.remove();
			}

			FileTypeSummary other = new FileTypeSummary(
					rb.getString("chart.filetype.others"));
			other.bytes = otherValuesTotal;
			result.add(other);

			// Sort again
			Collections.sort(result);
		}

		for (FileTypeSummary summary : result) {
			summary.pct = (double) summary.bytes / totalContentLength * 100.0;
		}

		return result;
	}

}
