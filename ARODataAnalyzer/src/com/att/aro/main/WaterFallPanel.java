package com.att.aro.main;

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
import java.util.ResourceBundle;
import java.util.logging.Logger;

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

import com.att.aro.images.Images;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.RequestResponseTimeline;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData.Analysis;

/**
 * Panel that displays a waterfall view of the analysis data
 */
public class WaterFallPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(WaterFallPanel.class.getName());

	private enum WaterFall {
		BEFORE, DNS_LOOKUP, INITIAL_CONNECTION, SSL_NEGOTIATION, REQUEST_TIME, TIME_TO_FIRST_BYTE, CONTENT_DOWNLOAD, 
		AFTER, AFTER_3XX, AFTER_4XX, HTTP_3XX_REDIRECTION, HTTP_4XX_CLIENTERROR;

		@Override
		public String toString() {
			switch (this) {
			case DNS_LOOKUP : 
				return rb.getString("waterfall.dnsLookup");
			case INITIAL_CONNECTION : 
				return rb.getString("waterfall.initialConnection");
			case SSL_NEGOTIATION : 
				return rb.getString("waterfall.sslNeg");
			case REQUEST_TIME : 
				return rb.getString("waterfall.reqTime");
			case TIME_TO_FIRST_BYTE : 
				return rb.getString("waterfall.firstByteTime");
			case CONTENT_DOWNLOAD : 
				return rb.getString("waterfall.contentDownload");
			case HTTP_3XX_REDIRECTION : 
				return rb.getString("waterfall.3xxResult");
			case HTTP_4XX_CLIENTERROR : 
				return rb.getString("waterfall.4xxResult");
			default :
				return super.toString();
			}
		}		
	}

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private static final int DEFAULT_TIMELINE = 100;
	private static final int CATEGORY_MAX_COUNT = 25;
	private static final double ZOOM_FACTOR = 2;

	private static final Color noneColor = new Color(0, 0, 0, 0);
	private static final Color dnsLoolupColor = new Color(0, 128, 128);
	private static final Color initiaConnColor = new Color(255, 140, 0);
	private static final Color sslNegColor = new Color(199, 21, 133);
	private static final Color requestTimeColor = new Color(255, 255, 0);
	private static final Color firstByteTimeColor = new Color(0, 255, 0);
	private static final Color contentDownloadColor = new Color(70, 130, 180);
	private static final Color threexColor = Color.BLUE;
	private static final Color fourxColor = Color.RED;

	private static final NumberFormat format = new DecimalFormat();
	private static final TickUnits units = new TickUnits();
	static {
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

	private ApplicationResourceOptimizer parent;
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

	/**
	 * Constructor
	 */
	public WaterFallPanel(ApplicationResourceOptimizer parent) {

		super(new BorderLayout());
		this.parent = parent;
		this.dataset = new SlidingCategoryDataset(new DefaultCategoryDataset(), 0,
				CATEGORY_MAX_COUNT);
		this.popup = new WaterfallPopup(parent);

		JPanel graphPanel = new JPanel(new BorderLayout());
		graphPanel.add(getChartPanel(), BorderLayout.CENTER);
		graphPanel.add(getVerticalScroll(), BorderLayout.EAST);
		graphPanel.add(getHorizontalScroll(), BorderLayout.SOUTH);
		add(graphPanel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(getZoomInButton());
		buttonsPanel.add(getZoomOutButton());
		add(buttonsPanel, BorderLayout.SOUTH);
	}

	/**
	 * Refreshes the waterfall display with the specified analysis data
	 * @param analysis The analysis data to be displayed
	 */
	public synchronized void refresh(Analysis analysis) {
		
		logger.entering("WaterFallPanel", "refresh");

		// Clear and hide the popup
		popup.refresh(null, 0);
		popup.setVisible(false);

		this.traceDuration = DEFAULT_TIMELINE;
		double range = DEFAULT_TIMELINE;

		// Create sorted list of request/response pairs
		List<WaterfallCategory> categoryList = new ArrayList<WaterfallCategory>();
		if (analysis != null) {
			this.traceDuration = analysis.getTraceData().getTraceDuration();
			range = Math.min(this.traceDuration, DEFAULT_TIMELINE);
			for (TCPSession tcpSession : analysis.getTcpSessions()) {
				for (HttpRequestResponseInfo reqRes : tcpSession.getRequestResponseInfo()) {
					if (reqRes.getDirection() == Direction.REQUEST
							&& reqRes.getWaterfallInfos() != null) {
						categoryList.add(new WaterfallCategory(reqRes));
					}
				}
			}
			
			// Sort and set index
			Collections.sort(categoryList);
			int index = 0;
			for (WaterfallCategory wc : categoryList) {
				wc.index = ++index;
			}
		}

		// Horizontal scroll bar used to scroll through trace duration
		JScrollBar h = getHorizontalScroll();
		h.setMaximum((int) Math.ceil(this.traceDuration));

		// Set the visible time range
		setTimeRange(0, range);
		
		CategoryAxis cAxis = getCategoryAxis();
		cAxis.clearCategoryLabelToolTips();

		// Build the dataset
		DefaultCategoryDataset underlying = new DefaultCategoryDataset();
		for (WaterfallCategory wfc : categoryList) {
			RequestResponseTimeline tl = wfc.reqResp.getWaterfallInfos();

			underlying.addValue(tl.getStartTime(), WaterFall.BEFORE, wfc);
			underlying.addValue(tl.getDnsLookupDuration(), WaterFall.DNS_LOOKUP, wfc);
			underlying.addValue(tl.getInitialConnDuration(), WaterFall.INITIAL_CONNECTION, wfc);
			underlying.addValue(tl.getSslNegotiationDuration(), WaterFall.SSL_NEGOTIATION, wfc);
			underlying.addValue(tl.getRequestDuration(), WaterFall.REQUEST_TIME, wfc);
			underlying.addValue(tl.getTimeToFirstByte(), WaterFall.TIME_TO_FIRST_BYTE, wfc);
			underlying.addValue(tl.getContentDownloadDuration(), WaterFall.CONTENT_DOWNLOAD, wfc);
			underlying.addValue(null, WaterFall.HTTP_3XX_REDIRECTION, wfc);
			underlying.addValue(null, WaterFall.HTTP_4XX_CLIENTERROR, wfc);

			int code = wfc.reqResp.getAssocReqResp().getStatusCode();
			double endTime = this.traceDuration - tl.getStartTime() - tl.getTotalTime();
			if(code >= 300 && code < 400) {
				underlying.addValue(endTime, WaterFall.AFTER_3XX, wfc);
			} else if(code >= 400) {
				underlying.addValue(endTime, WaterFall.AFTER_4XX, wfc);
			} else {
				underlying.addValue(endTime, WaterFall.AFTER, wfc);
			}
			
			cAxis.addCategoryLabelToolTip(wfc, wfc.getTooltip());
		}

		// Vertical scroll bar is used to scroll through data
		JScrollBar v = getVerticalScroll();
		int count = underlying.getColumnCount();
		v.setValue(0);
		v.setMaximum(count);
		v.setVisibleAmount(count > 0 ? this.dataset.getMaximumCategoryCount() - 1 / count : 1);

		// Add the dataset to the plot
		CategoryPlot plot = getChartPanel().getChart().getCategoryPlot();
		this.dataset = new SlidingCategoryDataset(underlying, 0, CATEGORY_MAX_COUNT);
		plot.setDataset(this.dataset);

		// Place proper colors on renderer for waterfall states
		final CategoryItemRenderer renderer = plot.getRenderer();
		for (Object o : underlying.getRowKeys()) {
			WaterFall wf = (WaterFall) o;
			int index = underlying.getRowIndex(wf);

			Color paint;
			switch (wf) {
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
				if (WaterFall.AFTER == dataset.getRowKey(row)
						|| WaterFall.AFTER_3XX == dataset.getRowKey(row)
						|| WaterFall.AFTER_4XX == dataset.getRowKey(row)) {
					WaterfallCategory waterfallItem = (WaterfallCategory) dataset
							.getColumnKey(column);

					RequestResponseTimeline waterfallInfos = waterfallItem.reqResp
							.getWaterfallInfos();
					DecimalFormat formatter = new DecimalFormat("#.##");
					int code = waterfallItem.reqResp.getAssocReqResp().getStatusCode();
					return MessageFormat.format(
							rb.getString("waterfall.totalTime"),
							formatter.format(waterfallInfos.getTotalTime()),
							code > 0 ? waterfallItem.reqResp.getScheme() + " " + code : rb.getString("waterfall.unknownCode"));
				}

				return null;
			}
		});

		logger.exiting("WaterFallPanel", "refresh");

	}

	private ChartPanel getChartPanel() {
		
		logger.entering("WaterFallPanel", "getChartPanel");

		if (chartPanel == null) {

			renderer = new StackedBarRenderer();
			renderer.setMaximumBarWidth(0.05);
			renderer.setShadowVisible(false);
			renderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
				@Override
				public String generateToolTip(CategoryDataset dataset, int row, int column) {

					WaterfallCategory c = (WaterfallCategory) dataset.getColumnKey(column);
					return c.getTooltip();
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
					if (event.getEntity() instanceof CategoryItemEntity) {
						CategoryItemEntity xyitem = (CategoryItemEntity) event.getEntity();
						WaterfallCategory wc = (WaterfallCategory) xyitem.getColumnKey();
						if (wc != null && wc.reqResp != null) {
							if (event.getTrigger().getClickCount() > 1) {
								parent.displayAdvancedTab();
								parent.getAroAdvancedTab().setHighlightedRequestResponse(wc.reqResp);
							} else {
								popup.refresh(wc.reqResp, wc.index);
								if (!popup.isVisible()) {
									popup.setVisible(true);
								}
							}
						}
					}

				}
			});
		}
		logger.exiting("WaterFallPanel", "getChartPanel");
		return chartPanel;
	}

	/**
	 * @return the timeAxis
	 */
	private NumberAxis getTimeAxis() {
		if (timeAxis == null) {
			timeAxis = new NumberAxis(rb.getString("waterfall.time")) {
				private static final long serialVersionUID = 1L;

				/**
				 * This override prevents the tick units from changing
				 * as the timeline is scrolled to numbers with more digits
				 */
				@Override
				protected double estimateMaximumTickLabelWidth(Graphics2D g2,
						TickUnit unit) {

					if (isVerticalTickLabels()) {
						return super.estimateMaximumTickLabelWidth(g2, unit);
					} else {
						RectangleInsets tickLabelInsets = getTickLabelInsets();
						double result = tickLabelInsets.getLeft()
								+ tickLabelInsets.getRight();

						// look at lower and upper bounds...
						FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
						double upper = traceDuration;
						String upperStr = "";
						NumberFormat formatter = getNumberFormatOverride();
						if (formatter != null) {
							upperStr = formatter.format(upper);
						} else {
							upperStr = unit.valueToString(upper);
						}
						double w2 = fm.stringWidth(upperStr);
						result += w2;
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
					timeAxis.setRange(horizontalScroll.getValue(), horizontalScroll.getValue()
							+ horizontalScroll.getVisibleAmount());
				}

			});
		}
		return horizontalScroll;
	}

	/**
	 * Implements the graph zoom in functionality.
	 */
	private JButton getZoomOutButton() {
		if (zoomOutButton == null) {
			ImageIcon zoomOutButtonIcon = Images.DEMAGNIFY.getIcon();
			zoomOutButton = new JButton(zoomOutButtonIcon);
			zoomOutButton.setEnabled(true);
			zoomOutButton.setPreferredSize(new Dimension(60, 30));
			zoomOutButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					zoomOut();

				}
			});
			zoomOutButton.setToolTipText(rb.getString("chart.tooltip.zoomout"));
		}
		return zoomOutButton;
	}

	private JButton getZoomInButton() {
		if (zoomInButton == null) {
			ImageIcon zoomInButtonIcon = Images.MAGNIFY.getIcon();
			zoomInButton = new JButton(zoomInButtonIcon);
			zoomInButton.setEnabled(true);
			zoomInButton.setPreferredSize(new Dimension(60, 30));
			zoomInButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					zoomIn();
				}
			});
			zoomInButton.setToolTipText(rb.getString("chart.tooltip.zoomin"));
		}
		return zoomInButton;
	}

	private void zoomIn() {
		Range r = timeAxis.getRange();
		double l = r.getLowerBound();
		double u = l + (r.getUpperBound() - l) / ZOOM_FACTOR;
		setTimeRange(l,u);
	}

	/**
	 * This method implements the graph zoom out functionality.
	 */
	private void zoomOut() {
		Range r = timeAxis.getRange();

		double l = r.getLowerBound();
		double u = l + (r.getUpperBound() - l) * ZOOM_FACTOR;
		setTimeRange(l, u);
	}
	
	private void setTimeRange(double l, double u) {
		boolean zoomInEnabled = true;
		boolean zoomOutEnabled = true;
		
		JScrollBar h = getHorizontalScroll();
		if (u > traceDuration) {
			double delta = u - traceDuration;
			l -= delta;
			u -= delta;
			if (l < 0) {
				l = 0.0;
				zoomOutEnabled = false;
			}
		}
		if (u - l <= 1.0) {
			u = l + 1.0;
			zoomInEnabled = false;
		}

		// Set the time range
		timeAxis.setRange(new Range(l, u));
		h.setValue((int) l);
		h.setVisibleAmount((int) Math.ceil(u - l));
		h.setBlockIncrement(h.getVisibleAmount());

		// Enable zoom buttons appropriately
		zoomOutButton.setEnabled(zoomOutEnabled);
		zoomInButton.setEnabled(zoomInEnabled);
	}
	
	private class WaterfallCategory implements Comparable<WaterfallCategory> {
		private HttpRequestResponseInfo reqResp;
		private int index;

		public WaterfallCategory(HttpRequestResponseInfo reqResp) {
			this.reqResp = reqResp;
		}

		@Override
		public int compareTo(WaterfallCategory arg0) {
			return Double.valueOf(reqResp.getWaterfallInfos().getStartTime())
					.compareTo(arg0.reqResp.getWaterfallInfos().getStartTime());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return MessageFormat.format(
					rb.getString("waterfall.categoryText"),
					index,
					reqResp.getHostName() != null ? reqResp.getHostName() : rb.getString("waterfall.unknownHost"),
					(reqResp != null && reqResp.getObjName() != null ? reqResp
							.getObjName() : ""));
		}

		/**
		 * Tooltip to be displayed for this item
		 * @return
		 */
		public String getTooltip() {
			if (reqResp.isSsl()) {
				return rb.getString("waterfall.https");
			} else {
				return reqResp != null && reqResp.getObjUri() != null ? reqResp.getObjUri().toString() : rb.getString("waterfall.unknownHost");
			}
		}
	}

}
