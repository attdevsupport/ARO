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
import java.util.List;
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
import com.att.aro.model.FileTypeSummary;
import com.att.aro.model.TraceData;

/**
 * Represents a Panel for displayings a chart of File Types.
 */
public class FileTypesChartPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private static final int WIDTH = 390;
	private static final int HEIGHT = 310;

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

	/**
	 * Initializes a new instance of the FileTypesChartPanel class.
	 */
	public FileTypesChartPanel() {
		super(new BorderLayout());
		initialize();
	}

	/**
	 * Sets the trace analysis data used for calculating the plot data for the
	 * chart.
	 * 
	 * @param analysis
	 *            - The trace analysis data.
	 */
	public synchronized void setAnalysisData(TraceData.Analysis analysis) {
		if (analysis != null) {
			this.content = analysis.constructContent(analysis);
			double[][] data = new double[2][content.size()];
			String[] titles = new String[content.size()];
			for (int i = 0; i < content.size(); ++i) {
				FileTypeSummary summary = content.get(i);
				String key = summary.getFileType();
				titles[i] = key;
				data[0][i] = summary.getPct();
				data[1][i] = 100.0 - summary.getPct();
			}

			plot.setDataset(DatasetUtilities.createCategoryDataset(new Integer[] { 1, 2 }, titles,
					data));
		} else {
			plot.setDataset(null);
		}
	}

	/**
	 * Initializes the File Type chart.
	 */
	private void initialize() {
		JFreeChart chart = ChartFactory.createBarChart(rb.getString("chart.filetype.title"), null,
				rb.getString("simple.percent"), null, PlotOrientation.HORIZONTAL, false, false,
				false);

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
			public String generateToolTip(CategoryDataset dataset, int row, int column) {

				FileTypeSummary summary = content.get(column);

				return MessageFormat.format(rb.getString("chart.filetype.tooltip"), NumberFormat
						.getIntegerInstance().format(summary.getBytes()));
			}
		});

		ItemLabelPosition insideItemlabelposition = new ItemLabelPosition(ItemLabelAnchor.INSIDE3,
				TextAnchor.CENTER_RIGHT);
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
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT, ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, USER_BUFFER, PROPERTIES, COPY, SAVE, PRINT,
				ZOOM, TOOL_TIPS);

		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);

		this.add(chartPanel, BorderLayout.CENTER);
	}

	/**
	 * Adds the file information in to provided writer.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void addFiletypes(FileWriter writer) throws IOException {
		StringBuffer apps = new StringBuffer();
		apps.append(rb.getString("statics.csvCell.seperator"));
		apps.append(rb.getString("simple.percent"));
		apps.append(rb.getString("statics.csvCell.seperator"));
		apps.append(rb.getString("statics.csvUnits.bytes"));
		apps.append('\n');
		for (FileTypeSummary fileType : content) {
			apps.append(fileType.getFileType());
			apps.append(rb.getString("statics.csvCell.seperator"));
			apps.append(fileType.getPct() + "%");
			apps.append(rb.getString("statics.csvCell.seperator"));
			apps.append(fileType.getBytes());
			apps.append('\n');
		}

		writer.append(apps);
	}

}
