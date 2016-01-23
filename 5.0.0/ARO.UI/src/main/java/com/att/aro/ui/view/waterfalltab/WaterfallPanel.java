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
package com.att.aro.ui.view.waterfalltab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.RequestResponseTimeline;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.waterfall.WaterfallCategory;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.view.images.Images;

/**
 * Panel that displays a waterfall view of the analysis data
 */
public class WaterfallPanel extends TabPanelJPanel{

 
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_TIMELINE = 100;
	private static final int CATEGORY_MAX_COUNT = 25;
	private static final double ZOOM_FACTOR = 2;

	private Color noneColor = new Color(0, 0, 0, 0);
	private Color dnsLoolupColor = new Color(0, 128, 128);
	private Color initiaConnColor = new Color(255, 140, 0);
	private Color sslNegColor = new Color(199, 21, 133);
	private Color requestTimeColor = new Color(255, 255, 0);
	private Color firstByteTimeColor = new Color(0, 255, 0);
	private Color contentDownloadColor = new Color(70, 130, 180);
	private Color threexColor = Color.BLUE;
	private Color fourxColor = Color.RED;

	private SlidingCategoryDataset dataset;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private ChartPanel chartPanel;
	private JScrollBar verticalScroll;
	private JScrollBar horizontalScroll;
	private NumberAxis timeAxis;
	private CategoryAxis categoryAxis;
	private double traceDuration = DEFAULT_TIMELINE;
	
	private WaterfallPopup popup;
	private StackedBarRenderer renderer;

	private static final NumberFormat format = new DecimalFormat();
	private static final TickUnits units = new TickUnits();

	static {
		units.add(new NumberTickUnit(500000, format, 5));
		units.add(new NumberTickUnit(250000, format, 5));
		units.add(new NumberTickUnit(100000, format, 10));

		units.add(new NumberTickUnit(50000, format, 5));
		units.add(new NumberTickUnit(25000, format, 5));
		units.add(new NumberTickUnit(10000, format, 10));
		
		units.add(new NumberTickUnit(5000, format, 5));
		units.add(new NumberTickUnit(2500, format, 5));
		
		units.add(new NumberTickUnit(1000, format, 5));
		units.add(new NumberTickUnit(500, format, 5));
		units.add(new NumberTickUnit(250, format, 5));
		units.add(new NumberTickUnit(100, format, 10));
		units.add(new NumberTickUnit(50, format, 10));
		units.add(new NumberTickUnit(25, format, 5));
		units.add(new NumberTickUnit(10, format, 10));
		units.add(new NumberTickUnit(5, format, 5));
		units.add(new NumberTickUnit(2, format, 4));
		units.add(new NumberTickUnit(1, format, 10));
		units.add(new NumberTickUnit(.5, format, 5));
		units.add(new NumberTickUnit(.25, format, 5));
		units.add(new NumberTickUnit(.1, format, 10));
		units.add(new NumberTickUnit(.05, format, 5));
		units.add(new NumberTickUnit(.01, format, 10));
	}
	private WaterfallTab waterfallTab;
	public WaterfallPanel(WaterfallTab waterfallTab){
		super();
		this.waterfallTab = waterfallTab;
	}
	public JPanel layoutDataPanel(){
		this.setLayout(new BorderLayout());
		
		this.dataset = new SlidingCategoryDataset(new DefaultCategoryDataset(), 0,
				CATEGORY_MAX_COUNT);
		
		this.popup = new WaterfallPopup();

		JPanel graphPanel = new JPanel(new BorderLayout());
		graphPanel.add(getChartPanel(), BorderLayout.CENTER);
		graphPanel.add(getVerticalScroll(), BorderLayout.EAST);
		graphPanel.add(getHorizontalScroll(), BorderLayout.SOUTH);
		this.add(graphPanel, BorderLayout.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(getZoomInButton());
		buttonsPanel.add(getZoomOutButton());
		this.add(buttonsPanel, BorderLayout.SOUTH);
		
		
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	private ChartPanel getChartPanel() {
		
		if (chartPanel == null) {

			renderer = new StackedBarRenderer();
			renderer.setMaximumBarWidth(0.05);
			renderer.setShadowVisible(false);

			renderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
				@Override
				public String generateToolTip(CategoryDataset dataset, int row, int column) {

					WaterfallCategory wfCategory = (WaterfallCategory) dataset.getColumnKey(column);
					return wfCategory.getTooltip();
				}
			});
			renderer.setBaseItemLabelsVisible(true);
			renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.INSIDE9,
					TextAnchor.CENTER_LEFT));
			renderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition(ItemLabelAnchor.INSIDE9,
					TextAnchor.CENTER_LEFT));

			// Set up plot
			CategoryPlot plot = new CategoryPlot(new DefaultCategoryDataset(), getCategoryAxis(),
					getTimeAxis(), renderer);
			plot.setOrientation(PlotOrientation.HORIZONTAL);
			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePosition(CategoryAnchor.END);

			JFreeChart chart = new JFreeChart(plot);
			chartPanel = new ChartPanel(chart, 400, 200, 200, 200, 2000, 5000, true, false, false,
					false, false, true);
			chartPanel.setMouseZoomable(false);
			chartPanel.setRangeZoomable(false);
			chartPanel.setDomainZoomable(false);
			chartPanel.addChartMouseListener(new ChartMouseListener() {
				
 				@Override
				public void chartMouseMoved(ChartMouseEvent arg0) {
					// Do Nothing
				}

				@Override
				public void chartMouseClicked(ChartMouseEvent event) {
					
					//TODO Add listner info or separate the listener.
					if (event.getEntity() instanceof CategoryItemEntity) {
						CategoryItemEntity xyitem = (CategoryItemEntity) event.getEntity();
						WaterfallCategory wfCategory = (WaterfallCategory) xyitem.getColumnKey();
						if (wfCategory != null && wfCategory.getReqResp() != null) {
							int count = event.getTrigger().getClickCount();
							if (count > 1) {
								waterfallTab.updateMainFrame(wfCategory.getSession());
							} else {		
								popup.refresh(wfCategory.getReqResp(), wfCategory.getIndex());
								if (!popup.getPopupDialog().isVisible()) {
									popup.getPopupDialog().setVisible(true);
								}
							}
						}
					}

				}
			});
		}
	
		return chartPanel;
	}
	
	/**
	 * @return the categoryAxis
	 */
	private CategoryAxis getCategoryAxis() {
		if (categoryAxis == null) {
			categoryAxis = new CategoryAxis();
			categoryAxis.setMaximumCategoryLabelWidthRatio(0.2f);
			categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(CategoryLabelPositions.STANDARD, new CategoryLabelPosition(
                RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT, 
                CategoryLabelWidthType.RANGE, 1.0f
            )));
		}
		return categoryAxis;
	}
	
	/**
	 * @return the timeAxis
	 */
	private NumberAxis getTimeAxis() {
		if (timeAxis == null) {
			timeAxis = new NumberAxis(ResourceBundleHelper.getMessageString("waterfall.time")) {
				private static final long serialVersionUID = 1L;

				/**
				 * This override prevents the tick units from changing
				 * as the timeline is scrolled to numbers with more digits
				 */
				@Override
				protected double estimateMaximumTickLabelWidth(Graphics2D g2d,
						TickUnit unit) {

					if (isVerticalTickLabels()) {
						return super.estimateMaximumTickLabelWidth(g2d, unit);
					} else {
						RectangleInsets tickLabelInsets = getTickLabelInsets();
						double result = tickLabelInsets.getLeft()
								+ tickLabelInsets.getRight();

						// look at lower and upper bounds...
						FontMetrics fMetrics = g2d.getFontMetrics(getTickLabelFont());
						double upper = traceDuration;
						String upperStr = "";
						NumberFormat formatter = getNumberFormatOverride();
						if (formatter == null) {
							upperStr = unit.valueToString(upper);
						} else {
							upperStr = formatter.format(upper);
						} 
						double width2 = fMetrics.stringWidth(upperStr);
						result += width2;
						return result;
					}


				}
				
			};
			timeAxis.setRange(new Range(0, DEFAULT_TIMELINE));
			timeAxis.setStandardTickUnits(units);
		}
		return timeAxis;
	}
	
	/**
	 * @return the verticalScroll
	 */
	private JScrollBar getVerticalScroll() {
		if (verticalScroll == null) {
			verticalScroll = new JScrollBar(JScrollBar.VERTICAL, 0, 100, 0, 100);
			verticalScroll.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (dataset.getColumnCount() > 0) {
						dataset.setFirstCategoryIndex(verticalScroll.getValue());
					}
				}

			});
		}
		return verticalScroll;
	}
	
	/**
	 * @return the horizontalScroll
	 */
	private JScrollBar getHorizontalScroll() {
		if (horizontalScroll == null) {
			horizontalScroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, DEFAULT_TIMELINE, 0, DEFAULT_TIMELINE);
			horizontalScroll.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					int scrollStart = horizontalScroll.getValue();
					int scrollEnd = scrollStart + horizontalScroll.getVisibleAmount();
					
					//logger.log(Level.FINE, "Change Event: Setting time range to {0} - {1}", new Object[] {scrollStart, scrollEnd});
					// Set time axis range based on scroll bar new position 
					timeAxis.setRange(scrollStart, scrollEnd);
				}
			});
		}
		return horizontalScroll;
	}
	
	/**
	 * Implements the graph zoom out functionality.
	 */
	private JButton getZoomOutButton() {
		if (zoomOutButton == null) {
			ImageIcon zoomOutButtonIcon = Images.DEMAGNIFY.getIcon();
			zoomOutButton = new JButton(zoomOutButtonIcon);
			zoomOutButton.setEnabled(false);
			zoomOutButton.setPreferredSize(new Dimension(60, 30));
			zoomOutButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent aEvent) {
					zoomOut();

				}
			});
			zoomOutButton.setToolTipText(ResourceBundleHelper.getMessageString("chart.tooltip.zoomout"));
		}
		return zoomOutButton;
	}

	/**
	 * Button for zoom in 
	 * @return
	 */
	private JButton getZoomInButton() {
		if (zoomInButton == null) {
			ImageIcon zoomInButtonIcon = Images.MAGNIFY.getIcon();
			zoomInButton = new JButton(zoomInButtonIcon);
			zoomInButton.setEnabled(true);
			zoomInButton.setPreferredSize(new Dimension(60, 30));
			zoomInButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent aEvent) {
					zoomIn();
				}
			});
			zoomInButton.setToolTipText(ResourceBundleHelper.getMessageString("chart.tooltip.zoomin"));
		}
		return zoomInButton;
	}

	/**
	 * This method implements the graph zoom in functionality.
	 */
	private void zoomIn() {
		Range range = timeAxis.getRange();
		double lowl = range.getLowerBound();
		double high = lowl + (range.getUpperBound() - lowl) / ZOOM_FACTOR;
		setTimeRange(lowl, high);
	}

	/**
	 * This method implements the graph zoom out functionality.
	 */
	private void zoomOut() {
		Range r = timeAxis.getRange();
		double low = r.getLowerBound();
		double high = low + (r.getUpperBound() - low) * ZOOM_FACTOR;
		setTimeRange(low, high);
	}

	/**
	 * Setting the time range for the graph.
	 * @param low
	 * @param high
	 */
	private void setTimeRange(double low, double high) {

		double lTime = low;
		double hTime = high;
		boolean zoomInEnabled = true;
		boolean zoomOutEnabled = true;
		JScrollBar scrollBarr = getHorizontalScroll();
		if (hTime > traceDuration) {
			double delta = hTime - traceDuration;
			lTime = lTime - delta;
			hTime = hTime - delta;
			if (lTime < 0) {
				lTime = 0.0;
			}				
		}
		
		if (hTime - lTime <= 1.0) {
			hTime = lTime + 1.0;
			zoomInEnabled = false;
		}

		if((hTime - lTime) < traceDuration){
			zoomOutEnabled = true;
		} else {
			zoomOutEnabled = false;
		}
		
//		logger.log(Level.FINE, "Range set to {0} - {1}", new Object[] {low, high});
		scrollBarr.setValue((int) lTime);
		scrollBarr.setVisibleAmount((int) Math.ceil(hTime - lTime));
		scrollBarr.setBlockIncrement(scrollBarr.getVisibleAmount());

		// Enable zoom buttons appropriately
		zoomOutButton.setEnabled(zoomOutEnabled);
		zoomInButton.setEnabled(zoomInEnabled);
	}
	
	/**
	 * Refreshes the waterfall display with the specified analysis data
	 * @param Analyzed data from aro core.
	 */
	public void refresh(AROTraceData aModel){
	
		this.popup.refresh(null, 0);
		this.popup.setVisible(false);
		
		double range = DEFAULT_TIMELINE;
		
		// Create sorted list of request/response pairs
		List<WaterfallCategory> categoryList = new ArrayList<WaterfallCategory>();
		
		if(aModel != null && aModel.getAnalyzerResult() != null){
			this.traceDuration = aModel.getAnalyzerResult().getTraceresult().getTraceDuration();
			
			// add 20% to make sure labels close to the right edge of the screen are visible
			this.traceDuration *= 1.2; 
			range = Math.min(this.traceDuration, DEFAULT_TIMELINE);
			
			for (Session tcpSession : aModel.getAnalyzerResult().getSessionlist()) {
				Session thisSession = tcpSession;
				if(!tcpSession.isUDP()){
					for (HttpRequestResponseInfo reqResInfo : tcpSession.getRequestResponseInfo()) {
						if(reqResInfo.getDirection() == HttpDirection.REQUEST && reqResInfo.getWaterfallInfos() != null){
							categoryList.add(new WaterfallCategory(reqResInfo,thisSession));							
						} 						
					}
					
				}
				
			}
			
			// Sort and set index
			Collections.sort(categoryList);
			int index = 0;
			for (WaterfallCategory wCategory : categoryList) {
				wCategory.setIndex(++index);
			}
			
			
			
		} 
		
		// Horizontal scroll bar used to scroll through trace duration
		JScrollBar hScrollBar = getHorizontalScroll();
		hScrollBar.setMaximum((int) Math.ceil(this.traceDuration));
		
		// Set the visible time range
		setTimeRange(0, range);
				
		CategoryAxis cAxis = getCategoryAxis();
		cAxis.clearCategoryLabelToolTips();
		
		// Build the dataset
				DefaultCategoryDataset underlying = new DefaultCategoryDataset();
				for (WaterfallCategory wfc : categoryList) {
					RequestResponseTimeline rrTimeLine = wfc.getReqResp().getWaterfallInfos();

					underlying.addValue(rrTimeLine.getStartTime(), Waterfall.BEFORE, wfc);
					underlying.addValue(rrTimeLine.getDnsLookupDuration(), Waterfall.DNS_LOOKUP, wfc);
					underlying.addValue(rrTimeLine.getInitialConnDuration(), Waterfall.INITIAL_CONNECTION, wfc);
					underlying.addValue(rrTimeLine.getSslNegotiationDuration(), Waterfall.SSL_NEGOTIATION, wfc);
					underlying.addValue(rrTimeLine.getRequestDuration(), Waterfall.REQUEST_TIME, wfc);
					underlying.addValue(rrTimeLine.getTimeToFirstByte(), Waterfall.TIME_TO_FIRST_BYTE, wfc);
					underlying.addValue(rrTimeLine.getContentDownloadDuration(), Waterfall.CONTENT_DOWNLOAD, wfc);
					underlying.addValue(null, Waterfall.HTTP_3XX_REDIRECTION, wfc);
					underlying.addValue(null, Waterfall.HTTP_4XX_CLIENTERROR, wfc);

					int code = wfc.getReqResp().getAssocReqResp().getStatusCode();
					double endTime = this.traceDuration - rrTimeLine.getStartTime() - rrTimeLine.getTotalTime();
					if(code >= 300 && code < 400) {
						underlying.addValue(endTime, Waterfall.AFTER_3XX, wfc);
					} else if(code >= 400) {
						underlying.addValue(endTime, Waterfall.AFTER_4XX, wfc);
					} else {
						underlying.addValue(endTime, Waterfall.AFTER, wfc);
					}
					
					cAxis.addCategoryLabelToolTip(wfc, wfc.getTooltip());
				}

				// Vertical scroll bar is used to scroll through data
				JScrollBar vScrollBar = getVerticalScroll();
				int count = underlying.getColumnCount();
				vScrollBar.setValue(0);
				vScrollBar.setMaximum(count);
				vScrollBar.setVisibleAmount(count > 0 ? this.dataset.getMaximumCategoryCount() - 1 / count : 1);
				
				// Add the dataset to the plot
				CategoryPlot plot = getChartPanel().getChart().getCategoryPlot();
				this.dataset = new SlidingCategoryDataset(underlying, 0, CATEGORY_MAX_COUNT);
				plot.setDataset(this.dataset);

				// Place proper colors on renderer for waterfall states
				final CategoryItemRenderer renderer = plot.getRenderer();
				for (Object obj : underlying.getRowKeys()) {
					Waterfall wFall = (Waterfall) obj;
					int index = underlying.getRowIndex(wFall);

					Color paint;
					switch (wFall) {
					case DNS_LOOKUP:
						paint = dnsLoolupColor;
						break;
					case INITIAL_CONNECTION:
						paint = initiaConnColor;
						break;
					case SSL_NEGOTIATION:
						paint = sslNegColor;
						break;
					case REQUEST_TIME:
						paint = requestTimeColor;
						break;
					case TIME_TO_FIRST_BYTE:
						paint = firstByteTimeColor;
						break;
					case CONTENT_DOWNLOAD:
						paint = contentDownloadColor;
						break;
					case AFTER_3XX:
						paint = noneColor;
						renderer.setSeriesItemLabelPaint(index, threexColor);
						renderer.setSeriesVisibleInLegend(index, false);
						break;
					case AFTER_4XX:
						paint = noneColor;
						renderer.setSeriesItemLabelPaint(index, fourxColor);
						renderer.setSeriesVisibleInLegend(index, false);
						break;
					case HTTP_3XX_REDIRECTION:
						paint = threexColor;
						break;
					case HTTP_4XX_CLIENTERROR:
						paint = fourxColor;
						break;
					default:
						renderer.setSeriesItemLabelPaint(index, Color.black);
						renderer.setSeriesVisibleInLegend(index, false);
						paint = noneColor;
					}
					renderer.setSeriesPaint(index, paint);
				}
				
				// Adding the label at the end of bars
				renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {
					private static final long serialVersionUID = 1L;

					@Override
					public String generateLabel(CategoryDataset dataset, int row, int column) {
						if (Waterfall.AFTER == dataset.getRowKey(row)
								|| Waterfall.AFTER_3XX == dataset.getRowKey(row)
								|| Waterfall.AFTER_4XX == dataset.getRowKey(row)) {
							WaterfallCategory waterfallItem = (WaterfallCategory) dataset
									.getColumnKey(column);

							RequestResponseTimeline waterfallInfos = waterfallItem.getReqResp()
									.getWaterfallInfos();
							DecimalFormat formatter = new DecimalFormat("#.##");
							int code = waterfallItem.getReqResp().getAssocReqResp().getStatusCode();
							return MessageFormat.format(
									ResourceBundleHelper.getMessageString("waterfall.totalTime"),
									formatter.format(waterfallInfos.getTotalTime()),
									code > 0 ? waterfallItem.getReqResp().getScheme() + " " + code : ResourceBundleHelper.getMessageString("waterfall.unknownCode"));
						}

						return null;
					}
				});

				
		

	}

}
	
