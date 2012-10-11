package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
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
import org.jfree.ui.TextAnchor;

import com.att.aro.images.Images;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.RequestResponseTimeline;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData.Analysis;


public class WaterFallPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private enum WaterFall {
		NONE,
		DNS_LOOKUP,
		INITIAL_CONNECTION,
		SSL_NEGOTIATION,
		REQUEST_TIME,
		TIME_TO_FIRST_BYTE,
		CONTENT_DOWNLOAD,
	}

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

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
	// private static final Color threexColor = new Color(255, 255, 0);
	//private static final Color fourxColor = new Color(255, 0, 0);

	private SlidingCategoryDataset dataset;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private ChartPanel chartPanel;
	private JScrollBar verticalScroll;
	private JScrollBar horizontalScroll;
	private NumberAxis timeAxis;
	private double traceDuration;
	
	private WaterfallPopup popup;
	private StackedBarRenderer renderer;

	/**
	 * Constructor
	 */
	public WaterFallPanel(Window parent) {

		super(new BorderLayout());
		this.dataset = new SlidingCategoryDataset(new DefaultCategoryDataset(), 0, CATEGORY_MAX_COUNT);
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

	public synchronized void refresh(Analysis analysis) {
		
		// Clear and hide the popup
		popup.refresh(null);
		popup.setVisible(false);

		List<WaterfallCategory> categoryList = new ArrayList<WaterfallCategory>();

		this.traceDuration = DEFAULT_TIMELINE;
		double range = DEFAULT_TIMELINE;
		if (analysis != null) {
			this.traceDuration = analysis.getTraceData().getTraceDuration();
			range = Math.min(this.traceDuration, DEFAULT_TIMELINE);
			for (TCPSession tcpSession : analysis.getTcpSessions()) {

				for (HttpRequestResponseInfo reqRes : tcpSession
						.getRequestResponseInfo()) {
					if (reqRes.getDirection() == Direction.REQUEST && reqRes.getWaterfallInfos() != null) {
						categoryList.add(new WaterfallCategory(reqRes));
					}

				}
			}
		}
		
		JScrollBar h = getHorizontalScroll();
		h.setValue(0);
		h.setMaximum((int) Math.ceil(this.traceDuration));
		h.setVisibleAmount((int) Math.round(range));
		timeAxis.setRange(new Range(0, range));

		DefaultCategoryDataset underlying = new DefaultCategoryDataset();
		for (WaterfallCategory wfc : categoryList) {
			RequestResponseTimeline tl = wfc.reqResp.getWaterfallInfos();

			underlying.addValue(tl.getStartTime(), WaterFall.NONE, wfc);
			underlying.addValue(tl.getDnsLookupDuration(), WaterFall.DNS_LOOKUP, wfc);
			underlying.addValue(tl.getInitialConnDuration(), WaterFall.INITIAL_CONNECTION, wfc);
			underlying.addValue(tl.getSslNegotiationDuration(), WaterFall.SSL_NEGOTIATION, wfc);
			underlying.addValue(tl.getRequestDuration(), WaterFall.REQUEST_TIME, wfc);
			underlying.addValue(tl.getTimeToFirstByte(), WaterFall.TIME_TO_FIRST_BYTE, wfc);
			underlying.addValue(tl.getContentDownloadDuration(), WaterFall.CONTENT_DOWNLOAD, wfc);
		}
		
		JScrollBar v = getVerticalScroll();
		int count = underlying.getColumnCount();
		v.setValue(0);
		v.setMaximum(count);
		v.setVisibleAmount(count > 0 ? this.dataset.getMaximumCategoryCount() - 1 / count : 1);

		CategoryPlot plot = getChartPanel().getChart().getCategoryPlot();
		this.dataset = new SlidingCategoryDataset(underlying, 0, CATEGORY_MAX_COUNT);
		plot.setDataset(this.dataset);

		CategoryItemRenderer renderer = plot.getRenderer();
		for (Object o : underlying.getRowKeys()) {
			WaterFall wf = (WaterFall) o;
			int index = underlying.getRowIndex(wf);
			
			Color paint;
			switch (wf) {
			case DNS_LOOKUP :
				paint = dnsLoolupColor;
				break;
			case INITIAL_CONNECTION :
				paint = initiaConnColor;
				break;
			case SSL_NEGOTIATION :
				paint = sslNegColor;
				break;
			case REQUEST_TIME :
				paint = requestTimeColor;
				break;
			case TIME_TO_FIRST_BYTE :
				paint = firstByteTimeColor;
				break;
			case CONTENT_DOWNLOAD :
				paint = contentDownloadColor;
				break;
			case NONE :
				renderer.setSeriesVisibleInLegend(index, false);
			default :
				paint = noneColor;
			}
			renderer.setSeriesPaint(index, paint);
		}
		
		// Adding the label at the end of bars

				Map<WaterfallCategory, WaterFall> plotLabelData = new HashMap<WaterfallCategory, WaterFall>();

				int lastRow = 0;

				for (int i = 0; i < underlying.getColumnCount(); i++) {

					for (int j = 0; j < underlying.getRowCount(); j++) {

						if (underlying.getValue(j, i) != null
								&& underlying.getValue(j, i).doubleValue() > 0) {

							lastRow = j;
						}
					}

					plotLabelData.put((WaterfallCategory) underlying.getColumnKey(i),
							(WaterFall) underlying.getRowKey(lastRow));
				}
				final Map<WaterfallCategory, WaterFall> data = plotLabelData;

				renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator() {

					@Override
					public String generateLabel(CategoryDataset dataset, int row, int column) {

						if (data.get(dataset.getColumnKey(column)) == dataset.getRowKey(row)) {
							WaterfallCategory waterfallItem = (WaterfallCategory) dataset
									.getColumnKey(column);

							RequestResponseTimeline waterfallInfos = waterfallItem.reqResp
									.getWaterfallInfos();
							double time = waterfallInfos.getDnsLookupDuration()
									+ waterfallInfos.getInitialConnDuration()
									+ waterfallInfos.getSslNegotiationDuration()
									+ waterfallInfos.getRequestDuration()
									+ waterfallInfos.getTimeToFirstByte()
									+ waterfallInfos.getContentDownloadDuration();
							DecimalFormat formatter = new DecimalFormat("#.##");
							if (time > 0) {
								return formatter.format(time) + "s";
							}
						}

						return "";
					}
				});

				renderer.setBaseItemLabelsVisible(true);
				renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3,
						TextAnchor.CENTER_LEFT));


	}

	
	private ChartPanel getChartPanel() {

		if (chartPanel == null) {

		    renderer = new StackedBarRenderer();
		    renderer.setShadowVisible(false);
			renderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
				@Override
				public String generateToolTip(CategoryDataset dataset, int row, int column) {

					WaterfallCategory c = (WaterfallCategory) dataset.getColumnKey(column);

					return c.reqResp != null && c.reqResp.getObjUri() != null ? c.reqResp.getObjUri().toString() : "";
				}
			});

			CategoryPlot plot = new CategoryPlot(new DefaultCategoryDataset(), new CategoryAxis(), getTimeAxis(), renderer);
			plot.setOrientation(PlotOrientation.HORIZONTAL);

			JFreeChart chart = new JFreeChart(plot);
			//chart.setBackgroundPaint(Color.white);

			chartPanel = new ChartPanel(chart, 400, 200, 200, 200, 2000, 5000, true, false, false, false, false, true);
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
						CategoryItemEntity xyitem = (CategoryItemEntity) event
								.getEntity(); // get clicked entity
						WaterfallCategory wc = (WaterfallCategory) xyitem.getColumnKey();
						if (wc != null && wc.reqResp != null) {
							popup.refresh(wc.reqResp);
							if (!popup.isVisible()) {
								popup.setVisible(true);
								Point p = event.getTrigger().getPoint();
								Point pp = chartPanel.getLocationOnScreen();
								popup.setLocation(pp.x + p.x + 10, pp.y + p.y + 10);
							}
						}
					}

				}
			});
		}
		return chartPanel;
	}

	/**
	 * @return the timeAxis
	 */
	private NumberAxis getTimeAxis() {
		if (timeAxis == null) {
		    timeAxis = new NumberAxis(rb.getString("waterfall.time"));
		    timeAxis.setRange(new Range(0 , DEFAULT_TIMELINE));
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
			horizontalScroll = new JScrollBar(JScrollBar.HORIZONTAL, 0, 100, 0, 100);
			horizontalScroll.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					timeAxis.setRange(horizontalScroll.getValue(), horizontalScroll.getValue() + horizontalScroll.getVisibleAmount());
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
		r = new Range(l, l + (r.getUpperBound() - l) / ZOOM_FACTOR);
		getHorizontalScroll().setVisibleAmount((int) Math.ceil(r.getUpperBound() - r.getLowerBound()));
		timeAxis.setRange(r);
		zoomOutButton.setEnabled(true);
	}

	/**
	 * This method implements the graph zoom out functionality.
	 */
	private void zoomOut() {
		Range r = timeAxis.getRange();
		
		double l = r.getLowerBound();
		double u = l + (r.getUpperBound() - l) * ZOOM_FACTOR;

		JScrollBar h = getHorizontalScroll();
		boolean enabled = true;
		if (u > traceDuration) {
			double delta = u - traceDuration;
			l -= delta;
			u -= delta;
			if (l < 0) {
				l = 0.0;
				enabled = false;
			}
		}
		zoomOutButton.setEnabled(enabled);
		h.setVisibleAmount((int) Math.ceil(u - l));
		timeAxis.setRange(new Range(l,u));
	}

	private class WaterfallCategory implements Comparable<WaterfallCategory> {
		private HttpRequestResponseInfo reqResp;

		public WaterfallCategory(HttpRequestResponseInfo reqResp) {
			this.reqResp = reqResp;
		}
		
		@Override
		public int compareTo(WaterfallCategory arg0) {
			return Double.valueOf(reqResp.getTimeStamp()).compareTo(arg0.reqResp.getTimeStamp());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return reqResp != null && reqResp.getObjName() != null ? reqResp.getObjName() : "";
		}
		
	}

}
