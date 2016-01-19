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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.overview.FileTypeSummary;
import com.att.aro.ui.utils.ResourceBundleHelper;


/**
 * @author Harikrishna Yaramachu
 * Represents a Panel for displayings a chart of File Types.
 *
 */
public class FileTypesChartPanel extends TabPanelJPanel{ 
	
	private static final long serialVersionUID = 1L;
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

	private static final int MAX_LIMIT_FILETYPES = 8;
	private AROTraceData traceDataModel; 

	
	private List<FileTypeSummary> fileTypeContent;
	private CategoryPlot cPlot;
	//JPanel fileTypePanel;
	
	public FileTypesChartPanel(){
		
	}
	/**
	 * 
	 * @return
	 */
	public JPanel layoutDataPanel(){
		
		setLayout(new BorderLayout());

		JFreeChart chart = initializeChart();
		
		ChartPanel chartPanel = new ChartPanel(chart, WIDTH, HEIGHT, 400,
				ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT, ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT, USER_BUFFER, PROPERTIES, COPY, SAVE, PRINT,
				ZOOM, TOOL_TIPS);
				
		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);
		add(chartPanel, BorderLayout.CENTER);
		
		return this;
	}
	
	
	private JFreeChart initializeChart(){
		JFreeChart chart = ChartFactory.createBarChart(ResourceBundleHelper.getMessageString("chart.filetype.title"), null,
				ResourceBundleHelper.getMessageString("simple.percent"), null, PlotOrientation.HORIZONTAL, false, false,
				false);
		
		//chart.setBackgroundPaint(fileTypePanel.getBackground());
		chart.setBackgroundPaint(this.getBackground());
		chart.getTitle().setFont(AROUIManager.HEADER_FONT);

		this.cPlot = chart.getCategoryPlot();
		cPlot.setBackgroundPaint(Color.white);
		cPlot.setDomainGridlinePaint(Color.gray);
		cPlot.setRangeGridlinePaint(Color.gray);
		cPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

		CategoryAxis domainAxis = cPlot.getDomainAxis();
		domainAxis.setMaximumCategoryLabelWidthRatio(.5f);
		domainAxis.setLabelFont(AROUIManager.LABEL_FONT);
		domainAxis.setTickLabelFont(AROUIManager.LABEL_FONT);

		NumberAxis rangeAxis = (NumberAxis) cPlot.getRangeAxis();
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

				FileTypeSummary summary = fileTypeContent.get(column);

				return MessageFormat.format(ResourceBundleHelper.getMessageString("chart.filetype.tooltip"), NumberFormat
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

		cPlot.setRenderer(renderer);
		cPlot.getDomainAxis().setMaximumCategoryLabelLines(2);
		
		return chart;
	}
	
	/**
	 * Sets the trace analysis data used for calculating the plot data for the
	 * chart.
	 * 
	 */
	public void setAnalysisDataToPlot(){
		if(fileTypeContent == null){
			cPlot.setDataset(null);
		} else {
			double[][] data = new double[2][fileTypeContent.size()];
			String[] titles = new String[fileTypeContent.size()];
			for (int i = 0; i < fileTypeContent.size(); ++i) {
				FileTypeSummary summary = fileTypeContent.get(i);
				String key = summary.getFileType();
				titles[i] = key;
				data[0][i] = summary.getPct();
				data[1][i] = 100.0 - summary.getPct();
			}
			
			cPlot.setDataset(DatasetUtilities.createCategoryDataset(new Integer[] { 1, 2 }, titles,
					data));
		}
	}

	/**
	 * @return the fileTypeContent
	 */
	public List<FileTypeSummary> getFileTypeContent() {
		return fileTypeContent;
	}

	/**
	 * @param fileTypeContent the fileTypeContent to set
	 */
	public void setFileTypeContent(List<FileTypeSummary> fileTypeContent) {
		this.fileTypeContent = fileTypeContent;
		
		if(fileTypeContent != null){
			setAnalysisDataToPlot();
		}
	}

	/**
	 * Creates the FilTypes list to be plotted on the chart.
	 * @return
	 */
	private void generateDataForChart(){
		Map<String, FileTypeSummary> content = new HashMap<String, FileTypeSummary>();
		int totalContentLength = 0;
		if (traceDataModel != null && traceDataModel.getAnalyzerResult() != null) {
			for (Session tcp : traceDataModel.getAnalyzerResult().getSessionlist()) {
					if(!tcp.isUDP()){
						for (HttpRequestResponseInfo info : tcp.getRequestResponseInfo()) {
							if (HttpDirection.RESPONSE
									.equals(info.getDirection())) {
								long contentLength = info.getRawSize();//info.getContentLength();
								if (contentLength > 0) {
									String contentType = info.getContentType();
									if (contentType == null || contentType.isEmpty()) {
										contentType = ResourceBundleHelper.getMessageString("chart.filetype.unknown");
									}
									FileTypeSummary summary = content.get(contentType);
									if (summary == null) {
										summary = new FileTypeSummary(contentType);
										content.put(contentType, summary);
									}
									// summary.bytes += contentLength;
									summary.setBytes(summary.getBytes() + contentLength);
									totalContentLength += contentLength;
								}
							}
						}
					}

			}
		}

		List<FileTypeSummary> result = new ArrayList<FileTypeSummary>(content.values());
		Collections.sort(result);

		if (result.size() > MAX_LIMIT_FILETYPES) {
			long otherValuesTotal = 0;

			Iterator<FileTypeSummary> iterator = result.iterator();
			for (int index = 0; index < (MAX_LIMIT_FILETYPES - 1); index++) {
				iterator.next();
			}
			while (iterator.hasNext()) {
				otherValuesTotal += iterator.next().getBytes();
				iterator.remove();
			}

			FileTypeSummary other = new FileTypeSummary(ResourceBundleHelper.getMessageString("chart.filetype.others"));
			other.setBytes(otherValuesTotal);
			result.add(other);

			// Sort again
			Collections.sort(result);
		}

		for (FileTypeSummary summary : result) {
			summary.setPct((double) summary.getBytes() / totalContentLength * 100.0);
		}

		this.setFileTypeContent(result);
		
	}

	/**
	 * @return the traceDataModel
	 */
	public AROTraceData getTraceDataModel() {
		return traceDataModel;
	}

	/**
	 * Refresh the chart and set the new model
	 */
	public void refresh(AROTraceData aroModel){
		this.traceDataModel = aroModel;
		this.generateDataForChart();
	}
}
