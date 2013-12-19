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
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.images.Images;
import com.att.aro.model.BatteryInfo;
import com.att.aro.model.BluetoothInfo;
import com.att.aro.model.BluetoothInfo.BluetoothState;
import com.att.aro.model.Burst;
import com.att.aro.model.BurstCategory;
import com.att.aro.model.CameraInfo;
import com.att.aro.model.CameraInfo.CameraState;
import com.att.aro.model.ExtensionFileFilter;
import com.att.aro.model.GpsInfo;
import com.att.aro.model.GpsInfo.GpsState;
import com.att.aro.model.NetworkBearerTypeInfo;
import com.att.aro.model.NetworkType;
import com.att.aro.model.PacketInfo;
import com.att.aro.model.Profile;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.RRCState;
import com.att.aro.model.RadioInfo;
import com.att.aro.model.RrcStateRange;
import com.att.aro.model.ScreenStateInfo;
import com.att.aro.model.ScreenStateInfo.ScreenState;
import com.att.aro.model.Throughput;
import com.att.aro.model.TraceData;
import com.att.aro.model.UserEvent;
import com.att.aro.model.UserEvent.UserEventType;
import com.att.aro.model.UserPreferences;
import com.att.aro.model.WifiInfo;
import com.att.aro.model.WifiInfo.WifiState;
import com.att.aro.model.cpu.CpuActivity;
import com.att.aro.model.cpu.CpuActivityList;

/**
 * Represents the Graph Panel that contains the graph in the Diagnostics tab.
 */
public class GraphPanel extends JPanel implements ActionListener, ChartMouseListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Implements the renderer for the RRC states on the plot. The main purpose
	 * of creating the class is to get the triangular shape for the promotion
	 * states on the plot.
	 */
	private static class RRCChartRenderer extends XYBarRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea,
				PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
				XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {

			RRCState key = (RRCState) dataset.getSeriesKey(series);
			if (key == RRCState.PROMO_FACH_DCH || key == RRCState.PROMO_IDLE_DCH
					|| key == RRCState.LTE_PROMOTION) {

				if (!getItemVisible(series, item)) {
					return;
				}
				IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

				double value0;
				double value1;
				if (this.getUseYInterval()) {
					value0 = intervalDataset.getStartYValue(series, item);
					value1 = intervalDataset.getEndYValue(series, item);
				} else {
					value0 = this.getBase();
					value1 = intervalDataset.getYValue(series, item);
				}
				if (Double.isNaN(value0) || Double.isNaN(value1)) {
					return;
				}
				if (value0 <= value1) {
					if (!rangeAxis.getRange().intersects(value0, value1)) {
						return;
					}
				} else {
					if (!rangeAxis.getRange().intersects(value1, value0)) {
						return;
					}
				}

				double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea,
						plot.getRangeAxisEdge());
				double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea,
						plot.getRangeAxisEdge());
				double bottom = Math.min(translatedValue0, translatedValue1);
				double top = Math.max(translatedValue0, translatedValue1);

				double startX = intervalDataset.getStartXValue(series, item);
				if (Double.isNaN(startX)) {
					return;
				}
				double endX = intervalDataset.getEndXValue(series, item);
				if (Double.isNaN(endX)) {
					return;
				}
				if (startX <= endX) {
					if (!domainAxis.getRange().intersects(startX, endX)) {
						return;
					}
				} else {
					if (!domainAxis.getRange().intersects(endX, startX)) {
						return;
					}
				}

				RectangleEdge location = plot.getDomainAxisEdge();
				double translatedStartX = domainAxis.valueToJava2D(startX, dataArea, location);
				double translatedEndX = domainAxis.valueToJava2D(endX, dataArea, location);

				double translatedWidth = Math.max(1, Math.abs(translatedEndX - translatedStartX));

				double left = Math.min(translatedStartX, translatedEndX);
				if (getMargin() > 0.0) {
					double cut = translatedWidth * getMargin();
					translatedWidth = translatedWidth - cut;
					left = left + cut / 2;
				}

				Polygon bar = new Polygon();
				PlotOrientation orientation = plot.getOrientation();
				if (orientation == PlotOrientation.HORIZONTAL) {
					// clip left and right bounds to data area
					bottom = Math.max(bottom, dataArea.getMinX());
					top = Math.min(top, dataArea.getMaxX());
					bar.addPoint((int) bottom, (int) (left + translatedWidth));
					if (key == RRCState.PROMO_FACH_DCH) {
						bar.addPoint((int) bottom, (int) (left + (translatedWidth / 2)));
					}
					bar.addPoint((int) top, (int) left);
					bar.addPoint((int) top, (int) (left + translatedWidth));
					bar.addPoint((int) bottom, (int) (left + translatedWidth));

					// bar = new Rectangle2D.Double(bottom, left, top - bottom,
					// translatedWidth);
				} else if (orientation == PlotOrientation.VERTICAL) {
					// clip top and bottom bounds to data area
					bottom = Math.max(bottom, dataArea.getMinY());
					top = Math.min(top, dataArea.getMaxY());
					bar.addPoint((int) left, (int) top);
					if (key == RRCState.PROMO_FACH_DCH) {
						bar.addPoint((int) left, (int) (bottom + ((top - bottom) / 2)));
					}
					bar.addPoint((int) (left + translatedWidth), (int) bottom);
					bar.addPoint((int) (left + translatedWidth), (int) top);
					bar.addPoint((int) left, (int) top);
					// bar = new Rectangle2D.Double(left, bottom,
					// translatedWidth, top
					// - bottom);
				}

				Paint itemPaint = getItemPaint(series, item);
				if (getGradientPaintTransformer() != null && itemPaint instanceof GradientPaint) {
					GradientPaint gp = (GradientPaint) itemPaint;
					itemPaint = getGradientPaintTransformer().transform(gp, bar);
				}
				g2.setPaint(itemPaint);
				g2.fill(bar);
				if (isDrawBarOutline() && Math.abs(translatedEndX - translatedStartX) > 3) {
					Stroke stroke = getItemOutlineStroke(series, item);
					Paint paint = getItemOutlinePaint(series, item);
					if (stroke != null && paint != null) {
						g2.setStroke(stroke);
						g2.setPaint(paint);
						g2.draw(bar);
					}
				}

				if (isItemLabelVisible(series, item)) {
					XYItemLabelGenerator generator = getItemLabelGenerator(series, item);
					drawItemLabel(g2, dataset, series, item, plot, generator, bar.getBounds2D(),
							value1 < 0.0);
				}

				// update the crosshair point
				double x1 = (startX + endX) / 2.0;
				double y1 = dataset.getYValue(series, item);
				double transX1 = domainAxis.valueToJava2D(x1, dataArea, location);
				double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());
				int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
				int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
				updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex,
						transX1, transY1, plot.getOrientation());

				EntityCollection entities = state.getEntityCollection();
				if (entities != null) {
					addEntity(entities, bar, dataset, series, item, 0.0, 0.0);
				}
			} else {
				super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset,
						series, item, crosshairState, pass);
			}
		}
	}

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String THROUGHPUT_TOOLTIP = rb.getString("throughput.tooltip");

	private static final Logger logger = Logger.getLogger(GraphPanel.class.getName());

	private static final Shape DEFAULT_POINT_SHAPE = new Ellipse2D.Double(-2, -2, 4, 4);
	private static final Shape CPU_PLOT_POINT_SHAPE = new Ellipse2D.Double(-3, -3, 6, 6);

	private static final String ZOOM_IN_ACTION = "zoomIn";
	private static final String ZOOM_OUT_ACTION = "zoomOut";
	private static final String SAVE_AS_ACTION = "saveGraph";

	private static final int MINOR_TICK_COUNT_10 = 10;
	private static final int MINOR_TICK_COUNT_5 = 5;
	private static final int MINOR_TICK_COUNT_4 = 4;

	private static final int DEFAULT_TIMELINE = 100;

	private static final int MIN_SIGNAL = -121;
	private static final int MAX_SIGNAL = -25;
	
	private static final int MIN_CPU_USAGE = -10;
	private static final int MAX_CPU_USAGE = 110;

	private static final int FIVE      = 5;
	private static final int FIVE0     = 50;
	private static final int FIVE00    = 500;
	private static final int FIVE000   = 5000;
	private static final int FIVE0000  = 50000;
	private static final int FIVE00000 = 500000;
	
	private static final int TVENTYFIVE     = 25;
	private static final int TVENTYFIVE0    = 250;
	private static final int TVENTYFIVE00   = 2500;
	private static final int TVENTYFIVE000  = 25000;
	private static final int TVENTYFIVE0000 = 250000;
	
	private static final int ONE = 1;
	private static final int ONE0 = 10;
	private static final int ONE00 = 100;
	private static final int ONE000 = 1000;
	private static final int ONE0000 = 10000;
	private static final int ONE00000 = 100000;
	
	private static final int TWO = 2;
	
	private static final double POINT5  = 0.5;
	private static final double POINT25 = .25;
	private static final double POINT1  = .1;
	private static final double POINT05 = .05;
	private static final double POINT01 = .01;

	private static final NumberFormat FORMAT = new DecimalFormat();
	private static final TickUnits UNITS = new TickUnits();
	static {
		UNITS.add(new NumberTickUnit(FIVE00000, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(TVENTYFIVE0000, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(ONE00000, FORMAT, MINOR_TICK_COUNT_10));

		UNITS.add(new NumberTickUnit(FIVE0000, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(TVENTYFIVE000, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(ONE0000, FORMAT, MINOR_TICK_COUNT_10));
		
		UNITS.add(new NumberTickUnit(FIVE000, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(TVENTYFIVE00, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(ONE000, FORMAT, MINOR_TICK_COUNT_10));
		
		UNITS.add(new NumberTickUnit(FIVE00, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(TVENTYFIVE0, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(ONE00, FORMAT, MINOR_TICK_COUNT_10));
		
		UNITS.add(new NumberTickUnit(FIVE0, FORMAT, MINOR_TICK_COUNT_10));
		UNITS.add(new NumberTickUnit(TVENTYFIVE, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(ONE0, FORMAT, MINOR_TICK_COUNT_10));
		
		UNITS.add(new NumberTickUnit(FIVE, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(TWO, FORMAT, MINOR_TICK_COUNT_4));
		UNITS.add(new NumberTickUnit(ONE, FORMAT, MINOR_TICK_COUNT_10));
		
		UNITS.add(new NumberTickUnit(POINT5, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(POINT25, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(POINT1, FORMAT, MINOR_TICK_COUNT_10));
		UNITS.add(new NumberTickUnit(POINT05, FORMAT, MINOR_TICK_COUNT_5));
		UNITS.add(new NumberTickUnit(POINT01, FORMAT, MINOR_TICK_COUNT_10));
	}

	private static final List<ChartPlotOptions> plotOrder = new ArrayList<ChartPlotOptions>(
			ChartPlotOptions.values().length);
	static {
		plotOrder.add(ChartPlotOptions.GPS);
		plotOrder.add(ChartPlotOptions.RADIO);
		plotOrder.add(ChartPlotOptions.BLUETOOTH);
		plotOrder.add(ChartPlotOptions.CAMERA);
		plotOrder.add(ChartPlotOptions.SCREEN);
		plotOrder.add(ChartPlotOptions.BATTERY);
		plotOrder.add(ChartPlotOptions.WIFI);
		plotOrder.add(ChartPlotOptions.NETWORK_TYPE);
		plotOrder.add(ChartPlotOptions.THROUGHPUT);
		plotOrder.add(ChartPlotOptions.UL_PACKETS);
		plotOrder.add(ChartPlotOptions.DL_PACKETS);
		plotOrder.add(ChartPlotOptions.BURSTS);
		plotOrder.add(ChartPlotOptions.USER_INPUT);
		plotOrder.add(ChartPlotOptions.RRC);
		plotOrder.add(ChartPlotOptions.CPU);
	}

	private static String graphPanelSaveDirectory;

	private JScrollPane pane;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton saveGraphButton;
	private JPanel zoomSavePanel;
	private JViewport port;
	private JPanel graphLabelsPanel;
	private JPanel chartPanel;
	private GraphPanelCrossHairHandle handlePanel;

	private Map<ChartPlotOptions, GraphPanelPlotLabels> subplotMap = new EnumMap<ChartPlotOptions, GraphPanelPlotLabels>(
			ChartPlotOptions.class);
	private PacketPlots pp;

	private CombinedDomainXYPlot plot;
	private JFreeChart advancedGraph;
	private ChartPanel advancedGraphPanel;
	private NumberAxis axis;
	private JLabel axisLabel;

	private TraceData traceData;

	private int zoomCounter = 0;
	private int maxZoom = 5;
	private double zoomFactor = 2;

	private Set<GraphPanelListener> listeners = new HashSet<GraphPanelListener>();

	/**
	 * Initializes a new instance of the GraphPanel class.
	 */
	public GraphPanel() {

		subplotMap.put(ChartPlotOptions.GPS, new GraphPanelPlotLabels(rb.getString("chart.gps"), createBarPlot(Color.gray), 1));
		subplotMap.put(ChartPlotOptions.RADIO, new GraphPanelPlotLabels(rb.getString("chart.radio"), createRadioPlot(), 2));
		subplotMap.put(ChartPlotOptions.BLUETOOTH, new GraphPanelPlotLabels(rb.getString("chart.bluetooth"), createBarPlot(Color.gray), 1));
		subplotMap.put(ChartPlotOptions.CAMERA, new GraphPanelPlotLabels(rb.getString("chart.camera"), createBarPlot(Color.gray), 1));
		subplotMap.put(ChartPlotOptions.SCREEN, new GraphPanelPlotLabels(rb.getString("chart.screen"), createBarPlot(new Color(34, 177, 76)), 1));
		subplotMap.put(ChartPlotOptions.BATTERY, new GraphPanelPlotLabels(rb.getString("chart.battery"), createBatteryPlot(), 2));
		subplotMap.put(ChartPlotOptions.WIFI, new GraphPanelPlotLabels(rb.getString("chart.wifi"), createBarPlot(Color.gray), 1));
		subplotMap.put(ChartPlotOptions.NETWORK_TYPE, new GraphPanelPlotLabels(rb.getString("chart.networkType"), createBarPlot(Color.gray), 1));
		subplotMap.put(ChartPlotOptions.THROUGHPUT,	new GraphPanelPlotLabels(rb.getString("chart.throughput"), createThroughputPlot(), 2));
		subplotMap.put(ChartPlotOptions.BURSTS,	new GraphPanelPlotLabels(rb.getString("chart.bursts"), createBurstPlot(), 1));
		subplotMap.put(ChartPlotOptions.USER_INPUT, new GraphPanelPlotLabels(rb.getString("chart.userInput"), createUserEventPlot(), 1));
		subplotMap.put(ChartPlotOptions.RRC, new GraphPanelPlotLabels(rb.getString("chart.rrc"), createRrcPlot(), 1));
		subplotMap.put(ChartPlotOptions.CPU, new GraphPanelPlotLabels(rb.getString("chart.cpu"), createCpuPlot(), 1));

		this.pp = new PacketPlots();
		subplotMap.put(ChartPlotOptions.UL_PACKETS,
				new GraphPanelPlotLabels(rb.getString("chart.ul"), pp.getUlPlot(), 1));
		subplotMap.put(ChartPlotOptions.DL_PACKETS,
				new GraphPanelPlotLabels(rb.getString("chart.dl"), pp.getDlPlot(), 1));

		this.axis = new NumberAxis();
		this.axis.setStandardTickUnits(UNITS);
		this.axis.setRange(new Range(0, DEFAULT_TIMELINE));
		this.axis.setLowerBound(0);

		this.axis.setAutoTickUnitSelection(true);
		this.axis.setTickMarkInsideLength(1);
		this.axis.setTickMarkOutsideLength(1);

		this.axis.setMinorTickMarksVisible(true);
		this.axis.setMinorTickMarkInsideLength(2f);
		this.axis.setMinorTickMarkOutsideLength(2f);
		this.axis.setTickMarkInsideLength(4f);
		this.axis.setTickMarkOutsideLength(4f);

		this.axisLabel = new JLabel(rb.getString("chart.timeline"));
		this.axisLabel.setHorizontalAlignment(SwingConstants.CENTER);

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(200, 310));
		this.add(getZoomSavePanel(), BorderLayout.EAST);
		this.add(getPane(), BorderLayout.CENTER);
		this.add(getLabelsPanel(), BorderLayout.WEST);

		setChartOptions(UserPreferences.getInstance().getChartPlotOptions());
	}

	/**
	 * Sets the chart plot options to those selected by clicking the Options
	 * menu item in the View menu.
	 * 
	 * @param optionsSelected
	 *            A List of ChartPlotOptions to be set on the GraphPanel
	 *            chart.
	 */
	public synchronized void setChartOptions(List<ChartPlotOptions> optionsSelected) {

		if (optionsSelected == null || optionsSelected.contains(ChartPlotOptions.DEFAULT_VIEW)) {
			optionsSelected = ChartPlotOptions.getDefaultList();
		}

		// Remove all plots from combined plot
		CombinedDomainXYPlot plot = getPlot();
		for (GraphPanelPlotLabels subplot : subplotMap.values()) {
			if (subplot != null && subplot.getPlot() != null) {

				plot.remove(subplot.getPlot());
				subplot.getLabel().setVisible(false);
			}
		}

		// Add selected plots
		for (ChartPlotOptions option : GraphPanel.plotOrder) {

			// Keep charts in order of enum
			if (optionsSelected.contains(option)) {
				GraphPanelPlotLabels subplot = subplotMap.get(option);
				if (subplot != null && subplot.getPlot() != null) {
					plot.add(subplot.getPlot(), subplot.getWeight());
					subplot.getLabel().setVisible(true);
				}
			}
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				layoutGraphLabels();
			}
		});
	}

	/**
	 * Resets the chart labels, and the chart and axis information of the
	 * GraphPanel when the specified trace data is loaded. The current instance
	 * of the GraphPanel is preserved, along with the current zoom state. The
	 * scroll bar is moved to position 0.
	 * 
	 * @param analysis
	 *            - An Analysis object containing the new trace analysis data.
	 */
	public synchronized void resetChart(TraceData.Analysis analysis) {

		this.traceData = analysis != null ? analysis.getTraceData() : null;
		getSaveGraphButton().setEnabled(analysis != null);
		// Setting the initial value on the time axis to -0.01 as the first
		// packet time stamp in pcap analysis is always zero and hence the tool
		// tip does not get displayed.
		this.axis.setRange(new Range(-0.01, analysis != null ? analysis.getTraceData()
				.getTraceDuration() : DEFAULT_TIMELINE));

		setGraphView(0);
		for (Map.Entry<ChartPlotOptions, GraphPanelPlotLabels> entry : subplotMap.entrySet()) {
			switch (entry.getKey()) {
			case BATTERY:
				populateBatteryPlot(entry.getValue().getPlot(), analysis);
				break;
			case BLUETOOTH:
				populateBluetoothPlot(entry.getValue().getPlot(), analysis);
				break;
			case BURSTS:
				populateBurstPlot(entry.getValue().getPlot(), analysis);
				break;
			case CAMERA:
				populateCameraPlot(entry.getValue().getPlot(), analysis);
				break;
			case GPS:
				populateGpsPlot(entry.getValue().getPlot(), analysis);
				break;
			case NETWORK_TYPE:
				populateNetworkTyesPlot(entry.getValue().getPlot(), analysis);
				break;
			case RADIO:
				populateRadioPlot(entry.getValue().getPlot(), analysis);
				break;
			case RRC:
				populateRrcPlot(entry.getValue().getPlot(), analysis);
				break;
			case SCREEN:
				populateScreenStatePlot(entry.getValue().getPlot(), analysis);
				break;
			case THROUGHPUT:
				populateThroughputPlot(entry.getValue().getPlot(), analysis);
				break;
			case USER_INPUT:
				populateUserEventPlot(entry.getValue().getPlot(), analysis);
				break;
			case WIFI:
				populateWifiPlot(entry.getValue().getPlot(), analysis);
				break;
			case CPU:
				populateCpuPlot(entry.getValue().getPlot(), analysis);
				break;
			default:
				break;
			}
		}
		this.pp.populatePacketPlots(analysis);
		this.getZoomInButton().setEnabled(analysis != null);
		this.getZoomOutButton().setEnabled(analysis != null);
		this.getSaveGraphButton().setEnabled(analysis != null);
	}

	/**
	 * Sets the maximum number of times a user can zoom in or zoom out. Each
	 * zoom increment doubles the precision. The default zoom value is 5.
	 * 
	 * @param zoom
	 *            - The maximum zoom value that can be set.
	 */
	public void setMaxZoom(int zoom) {
		this.maxZoom = zoom;
	}

	/**
	 * Sets the zoom factor. The default value is 2, meaning that the zoom
	 * doubles in precision for each zoom increment.
	 * 
	 * @param zoomFactor
	 *            A double that indicates the zoom factor.
	 */
	public void setZoomFactor(double zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	/**
	 * Sets the GraphPanel cross hair to the specified value, and centers the
	 * graph on the cross hair.
	 * 
	 * @param graphCrosshairSetting
	 *            A double that is the new cross-hair value.
	 */
	public void setGraphView(double graphCrosshairSetting) {
		setGraphView(graphCrosshairSetting, true);
	}

	/**
	 * Sets the GraphPanel cross hair to the specified value, and centers the
	 * graph on the cross hair if the centerChartOnCrosshair parameter is
	 * "true".
	 * 
	 * @param graphCrosshairSetting
	 *            A timestamp that is the new cross hair setting.
	 * 
	 * @param centerChartOnCrosshair
	 *            A boolean value that indicates whether the graph should be
	 *            centered on the new cross hair value.
	 */
	public void setGraphView(double graphCrosshairSetting, boolean centerChartOnCrosshair) {
		setCrossHair(graphCrosshairSetting);
		if (centerChartOnCrosshair) {
			resetScrollPosition();
		}
	}

	/**
	 * Returns the lower bound of the area of the GraphPanel that is visible to
	 * the user (the viewport).
	 * 
	 * @return A double value that is the lower bound of the viewport.
	 */
	public double getViewportLowerBound() {
		return new Float(getScrollPosRatio() * getGraphLength());
	}

	/**
	 * Returns the upper bound of the area of the GraphPanel that is visible to
	 * the user (the viewport).
	 * 
	 * @return A double value that is the highest bound of the viewport.
	 */
	public double getViewportUpperBound() {
		return new Float(getViewportLowerBound() + (getGraphLength() * getViewportOffsetRatio()));
	}

	/**
	 * Returns the total length of the graph based on its axis.
	 * 
	 * @return A float value that is the total length of the graph.
	 */
	public float getGraphLength() {
		return new Float(this.axis.getRange().getLength());
	}

	/**
	 * Adds a listener to the GraphPanel.
	 * 
	 * @param listner
	 *            - The listener to add to the Graph Panel. The listener must be
	 *            an implementation of the com.att.aro.main.GraphPanelListener
	 *            interface.
	 */
	public void addGraphPanelListener(GraphPanelListener listner) {
		listeners.add(listner);
	}

	/**
	 * Removes a listener from the GraphPanel.
	 * 
	 * @param l
	 *            - The listener to be removed from the GraphPanel.
	 */
	public void removeGraphPanelListener(GraphPanelListener l) {
		listeners.remove(l);
	}

	/**
	 * Returns the current cross hair value.
	 * 
	 * @return A double that is the current cross hair value for the GraphPanel.
	 */
	public double getCrosshair() {
		return plot.getDomainCrosshairValue();
	}

	/**
	 * Returns a value that indicates if the GraphPanel cross hair is within the
	 * viewport (the viewable lower and upper bounds).
	 * 
	 * @return A boolean value that is "true" if the cross-hair is within the
	 *         viewport.
	 */
	public boolean isCrossHairInViewport() {
		return (getCrosshair() >= getViewportLowerBound() && getCrosshair() <= getViewportUpperBound());
	}

	/**
	 * This method is invoked when a mouse move event occurs in the GraphPanel.
	 */
	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
	}

	/**
	 * This method is invoked when a mouse click event occurs in the GraphPanel.
	 */
	@Override
	public void chartMouseClicked(ChartMouseEvent chartmouseevent) {
		Point2D point = chartmouseevent.getTrigger().getPoint();
		Rectangle2D plotArea = advancedGraphPanel.getScreenDataArea();

		XYPlot plot = (XYPlot) advancedGraph.getPlot();
		final double lastChartX = new Double(plot.getDomainAxis().java2DToValue(point.getX(),
				plotArea, plot.getDomainAxisEdge()));

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				setCrossHair(lastChartX);
			}

		});

		for (GraphPanelListener l : listeners) {
			l.graphPanelClicked(lastChartX);
		}
	}

	private JPanel getLabelsPanel() {
		if (graphLabelsPanel == null) {
			graphLabelsPanel = new JPanel();
			graphLabelsPanel.setPreferredSize(new Dimension(100, 100));
			graphLabelsPanel.setLayout(null);
			for (GraphPanelPlotLabels label : subplotMap.values()) {
				graphLabelsPanel.add(label.getLabel());
			}
			graphLabelsPanel.add(this.axisLabel);
		}
		return graphLabelsPanel;
	}

	/**
	 * Resets the scroll position on zoom in or zoom out events.
	 */
	private void resetScrollPosition() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				pane.getHorizontalScrollBar().setValue(getCrosshairViewPos());
			}
		});

		// update the handle position
		this.handlePanel.setHandlePosition(getHandleCoordinate());
	}

	private synchronized void layoutGraphLabels() {

		CombinedDomainXYPlot combinedPlot = getPlot();

		// grab the height of the chart panel minus the axis labels and
		// scrollbar
		int height = this.advancedGraphPanel.getHeight() - 20;

		// find weights and use them to determine how may divisions are needed.
		int plotWeightedDivs = 0;
		List<?> plots = combinedPlot.getSubplots();
		for (Object p : plots) {
			if (p instanceof XYPlot) {
				plotWeightedDivs += ((XYPlot) p).getWeight();
			}
		}

		// check for zero
		plotWeightedDivs = plotWeightedDivs == 0 ? 1 : plotWeightedDivs;

		// determine the size of the divisions for each XYPlot
		int division = Math.round(height / plotWeightedDivs);

		// working from top to bottom, set the y-coord. for the first XYPlot
		int currentY = getLabelsPanel().getY() + 4 + this.advancedGraphPanel.getY();

		// loop on the list of Plots
		for (ChartPlotOptions option : plotOrder) {
			GraphPanelPlotLabels subplot = subplotMap.get(option);
			if (subplot != null && subplot.getLabel().isVisible()) {
				int weightDivisionFactor = division * subplot.getWeight();

				// set the current position using weight
				subplot.getLabel().setBounds(3, currentY + 1, 100, weightDivisionFactor + 3);

				// adjust the currentY value for the next label in the loop
				currentY += weightDivisionFactor;
			}
		}

		// add the axis label
		this.axisLabel.setBounds(3, height + 3 + this.advancedGraphPanel.getY(), 100, 15);
	}

	/**
	 * Sets the Cross hair value.
	 */
	private void setCrossHair(double crossHairValue) {
		// set the cross hair values of plot and sub-plots
		Plot mainplot = advancedGraph.getPlot();
		if (mainplot instanceof CombinedDomainXYPlot) {
			CombinedDomainXYPlot combinedPlot = (CombinedDomainXYPlot) mainplot;
			List<?> plots = combinedPlot.getSubplots();
			for (Object p : plots) {
				if (p instanceof XYPlot) {
					XYPlot subPlot = (XYPlot) p;
					subPlot.setDomainCrosshairLockedOnData(false);
					subPlot.setDomainCrosshairValue(crossHairValue);
					subPlot.setDomainCrosshairVisible(true);
				}
			}
			combinedPlot.setDomainCrosshairLockedOnData(false);
			combinedPlot.setDomainCrosshairValue(crossHairValue, true);
			combinedPlot.setDomainCrosshairVisible(true);
		}

		handlePanel.setHandlePosition(getHandleCoordinate());
	}

	/**
	 * This method is invoked when an ActionEvent occurs in the GraphPanel.
	 */
	public void actionPerformed(ActionEvent e) {
		if (SAVE_AS_ACTION.equals(e.getActionCommand())) {
			try {
				saveAs();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, rb.getString("chart.saveError"));
				logger.fine("An error occurred trying to save the chart: " + e1.getMessage());
			}
		} else if (ZOOM_IN_ACTION.equals(e.getActionCommand())) {
			zoomIn();
		} else if (ZOOM_OUT_ACTION.equals(e.getActionCommand())) {
			zoomOut();
		}
	}

	/**
	 * Implements the saving of the graph snapshot.
	 */
	private void saveAs() throws IOException {

		// Determine save directory
		File saveDir = traceData.getTraceDir();
		if (graphPanelSaveDirectory != null) {
			saveDir = new File(graphPanelSaveDirectory);
		}
		JFileChooser fc = new JFileChooser(saveDir);

		// Set up file types
		String[] fileTypesJPG = new String[2];
		String fileDisplayTypeJPG = rb.getString("fileChooser.contentDisplayType.jpeg");
		fileTypesJPG[0] = rb.getString("fileChooser.contentType.jpeg");
		fileTypesJPG[1] = rb.getString("fileChooser.contentType.jpg");
		FileFilter filterJPG = new ExtensionFileFilter(fileDisplayTypeJPG, fileTypesJPG);

		fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
		String[] fileTypesPng = new String[1];
		String fileDisplayTypePng = rb.getString("fileChooser.contentDisplayType.png");
		fileTypesPng[0] = rb.getString("fileChooser.contentType.png");
		FileFilter filterPng = new ExtensionFileFilter(fileDisplayTypePng, fileTypesPng);
		fc.addChoosableFileFilter(filterPng);
		fc.setFileFilter(filterJPG);
		File plotImageFile = null;

		boolean bSavedOrCancelled = false;
		while (!bSavedOrCancelled) {
			if (fc.showSaveDialog(this.getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
				String strFile = fc.getSelectedFile().toString();
				String strFileLowerCase = strFile.toLowerCase();
				String fileDesc = fc.getFileFilter().getDescription();
				String fileType = rb.getString("fileChooser.contentType.jpg");
				if ((fileDesc.equalsIgnoreCase(rb.getString("fileChooser.contentDisplayType.png")) || strFileLowerCase
						.endsWith(rb.getString("fileType.filters.dot")
								+ fileTypesPng[0].toLowerCase()))) {
					fileType = fileTypesPng[0];
				}
				if (strFile.length() > 0) {
					// Save current directory
					graphPanelSaveDirectory = fc.getCurrentDirectory().getPath();

					if ((fileType != null) && (fileType.length() > 0)) {
						String fileTypeLowerCaseWithDot = rb.getString("fileType.filters.dot")
								+ fileType.toLowerCase();
						if (!strFileLowerCase.endsWith(fileTypeLowerCaseWithDot)) {
							strFile += rb.getString("fileType.filters.dot") + fileType;
						}
					}
					plotImageFile = new File(strFile);
					boolean bAttemptToWriteToFile = true;
					if (plotImageFile.exists()) {
						if (MessageDialogFactory.showConfirmDialog(this, MessageFormat.format(
								rb.getString("fileChooser.fileExists"),
								plotImageFile.getAbsolutePath()), rb
								.getString("fileChooser.confirm"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							bAttemptToWriteToFile = false;
						}
					}
					if (bAttemptToWriteToFile) {
						try {
							if (fileType.equalsIgnoreCase(fileTypesPng[0])) {
								BufferedImage bufImage = createImage(pane);
								ImageIO.write(bufImage, "png", plotImageFile);
							} else {
								BufferedImage bufImage = createImage(pane);
								ImageIO.write(bufImage, "jpg", plotImageFile);
							}
							bSavedOrCancelled = true;
						} catch (IOException e) {
							MessageDialogFactory.showMessageDialog(
									this,
									rb.getString("fileChooser.errorWritingToFile"
											+ plotImageFile.toString()));
						}
					}
				}
			} else {
				bSavedOrCancelled = true;
			}
		}
	}

	/**
	 * Returns buffered image of current viewport in provided JScrollPane.
	 * 
	 * @param pane
	 * @return Graph image.
	 */
	private BufferedImage createImage(JScrollPane pane) {
		JViewport jVPort = pane.getViewport();
		jVPort.getWidth();
		int w = jVPort.getWidth();
		int h = jVPort.getHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		jVPort.paint(g);
		return bi;
	}

	/**
	 * Implements the graph zoom in functionality.
	 */
	private void zoomIn() {
		if (zoomCounter < maxZoom) {
			this.getZoomInButton().setEnabled(false);
			advancedGraphPanel.setPreferredSize(new Dimension(
					(int) (advancedGraphPanel.getWidth() * this.zoomFactor), 200));
			zoomCounter++;
			zoomEventUIUpdate();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getZoomInButton().setEnabled(true);
				}
			});
		}
	}

	/**
	 * This method implements the graph zoom out functionality.
	 */
	private void zoomOut() {
		if (zoomCounter > 0) {
			this.getZoomOutButton().setEnabled(false);
			advancedGraphPanel.setPreferredSize(new Dimension(
					(int) (advancedGraphPanel.getWidth() / this.zoomFactor), 200));
			zoomCounter--;
			zoomEventUIUpdate();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getZoomOutButton().setEnabled(true);
				}
			});
		}
	}

	/**
	 * Updates the graph UI after zoom in or zoom out.
	 */
	private void zoomEventUIUpdate() {
		// allow for better scrolling efficiency for new size
		pane.getHorizontalScrollBar().setUnitIncrement(zoomCounter * 10);
		// update the screen panels for repaint
		advancedGraphPanel.updateUI();
		// updates the scroll bar after resize updates.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resetScrollPosition();
			}
		});
	}

	/**
	 * Returns the Cross hair current coordinate.
	 */
	private int getHandleCoordinate() {
		Rectangle2D plotArea = advancedGraphPanel.getScreenDataArea();
		XYPlot plot = (XYPlot) advancedGraph.getPlot();
		int handleCoordinate = new Float(plot.getDomainAxis().valueToJava2D(getCrosshair(),
				plotArea, plot.getDomainAxisEdge())).intValue();
		return handleCoordinate;
	}

	private int getCrosshairViewPos() {
		float pos = getCrosshairPosRatio() - getCrossSectionOffsetRatio();
		float chartPosValue = new Float(getScrollMax() * pos);
		return Math.max(0, Math.round(chartPosValue));
	}

	/**
	 * Returns the current position of the scroll bar.
	 */
	private float getScrollPos() {
		return new Float(this.pane.getHorizontalScrollBar().getValue());
	}

	/**
	 * Returns the maximum position of the scroll bar.
	 */
	private float getScrollMax() {
		return new Float(this.pane.getHorizontalScrollBar().getMaximum());
	}

	/**
	 * Returns the scroll position ratio of the scroll bar.
	 */
	private float getScrollPosRatio() {
		return new Float(getScrollPos() / getScrollMax());
	}

	private float getCrossSectionOffsetRatio() {
		return new Float(new Float(getCrossSection()) / new Float(getScrollMax()));
	}

	private float getViewportOffsetRatio() {
		return new Float(new Float(this.pane.getWidth()) / new Float(getScrollMax()));
	}

	private float getCrosshairPosRatio() {
		return new Float(new Float(getCrosshair()) / new Float(getGraphLength()));
	}

	private float getCrossSection() {
		return new Float(this.pane.getWidth() / 2);
	}

	private JScrollPane getPane() {
		if (pane == null) {
			pane = new JScrollPane();
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			pane.getHorizontalScrollBar().setUnitIncrement(10);
			pane.setViewport(getViewport());
		}
		return pane;
	}

	private GraphPanelCrossHairHandle getHandlePanel() {
		if (handlePanel == null) {
			handlePanel = new GraphPanelCrossHairHandle(Color.blue);
		}
		return handlePanel;
	}

	private JPanel getChartAndHandlePanel() {
		if (chartPanel == null) {
			chartPanel = new JPanel();
			chartPanel.setLayout(new BorderLayout());
			chartPanel.add(getHandlePanel(), BorderLayout.NORTH);
			chartPanel.add(getChartPanel(), BorderLayout.CENTER);
		}
		return chartPanel;
	}

	private JViewport getViewport() {
		if (port == null) {
			port = new JViewport();
			port.setView(getChartAndHandlePanel());
		}
		return port;
	}

	private ChartPanel getChartPanel() {
		if (advancedGraphPanel == null) {
			advancedGraphPanel = new ChartPanel(getAdvancedGraph());
			advancedGraphPanel.setMouseZoomable(false);
			advancedGraphPanel.setDomainZoomable(false);
			advancedGraphPanel.setRangeZoomable(false);
			advancedGraphPanel.setDisplayToolTips(true);
			advancedGraphPanel.addChartMouseListener(this);
			advancedGraphPanel.setAutoscrolls(false);
			advancedGraphPanel.setPopupMenu(null);
			advancedGraphPanel.setPreferredSize(new Dimension(100, 100));
			advancedGraphPanel.setRefreshBuffer(true);
			advancedGraphPanel.setMaximumDrawWidth(100000);

		}
		return advancedGraphPanel;
	}

	private JFreeChart getAdvancedGraph() {
		if (advancedGraph == null) {

			this.advancedGraph = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, getPlot(),
					true);
			advancedGraph.removeLegend();
		}
		return this.advancedGraph;
	}

	private JPanel getZoomSavePanel() {
		if (zoomSavePanel == null) {
			zoomSavePanel = new JPanel();
			zoomSavePanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			zoomSavePanel.add(getZoomInButton(), gbc);
			gbc.gridx = 0;
			gbc.gridy = 1;
			zoomSavePanel.add(getZoomOutButton(), gbc);
			gbc.gridx = 0;
			gbc.gridy = 2;
			zoomSavePanel.add(getSaveGraphButton(), gbc);
		}
		return zoomSavePanel;
	}

	private JButton getZoomOutButton() {
		if (zoomOutButton == null) {
			ImageIcon zoomOutButtonIcon = Images.DEMAGNIFY.getIcon();
			zoomOutButton = new JButton("", zoomOutButtonIcon);
			zoomOutButton.setActionCommand(ZOOM_OUT_ACTION);
			zoomOutButton.setEnabled(false);
			zoomOutButton.setPreferredSize(new Dimension(60, 30));
			zoomOutButton.addActionListener(this);
			zoomOutButton.setToolTipText(rb.getString("chart.tooltip.zoomout"));
		}
		return zoomOutButton;
	}

	private JButton getZoomInButton() {
		if (zoomInButton == null) {
			ImageIcon zoomInButtonIcon = Images.MAGNIFY.getIcon();
			zoomInButton = new JButton("", zoomInButtonIcon);
			zoomInButton.setActionCommand(ZOOM_IN_ACTION);
			zoomInButton.setEnabled(false);
			zoomInButton.setPreferredSize(new Dimension(60, 30));
			zoomInButton.addActionListener(this);
			zoomInButton.setToolTipText(rb.getString("chart.tooltip.zoomin"));
		}
		return zoomInButton;
	}

	private JButton getSaveGraphButton() {
		if (saveGraphButton == null) {
			ImageIcon saveGraphButtonIcon = Images.SAVE.getIcon();
			saveGraphButton = new JButton("", saveGraphButtonIcon);
			saveGraphButton.setActionCommand(SAVE_AS_ACTION);
			saveGraphButton.setEnabled(false);
			saveGraphButton.setPreferredSize(new Dimension(60, 30));
			saveGraphButton.addActionListener(this);
			saveGraphButton.setToolTipText(rb.getString("chart.tooltip.saveas"));
		}
		return saveGraphButton;
	}

	private CombinedDomainXYPlot getPlot() {
		if (this.plot == null) {

			this.plot = new CombinedDomainXYPlot(this.axis);
			plot.setOrientation(PlotOrientation.VERTICAL);
			plot.setGap(0.1);
		}
		return this.plot;
	}

	private static void populateGpsPlot(XYPlot plot, TraceData.Analysis analysis) {
		final XYIntervalSeriesCollection gpsData = new XYIntervalSeriesCollection();
		if (analysis != null) {

			// create the GPS dataset...
			Map<GpsState, XYIntervalSeries> seriesMap = new EnumMap<GpsState, XYIntervalSeries>(
					GpsState.class);
			for (GpsState eventType : GpsState.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				gpsData.addSeries(series);
			}
			Iterator<GpsInfo> iter = analysis.getGpsInfos().iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					GpsInfo gpsEvent = iter.next();
					if (gpsEvent.getGpsState() != GpsState.GPS_DISABLED) {
						seriesMap.get(gpsEvent.getGpsState())
								.add(gpsEvent.getBeginTimeStamp(), gpsEvent.getBeginTimeStamp(),
										gpsEvent.getEndTimeStamp(), 0.5, 0, 1);
					}
				}
			}

			XYItemRenderer renderer = plot.getRenderer();
			renderer.setSeriesPaint(gpsData.indexOf(GpsState.GPS_STANDBY), Color.YELLOW);
			renderer.setSeriesPaint(gpsData.indexOf(GpsState.GPS_ACTIVE), new Color(34, 177, 76));

			// Assign ToolTip to renderer
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					GpsState eventType = (GpsState) gpsData.getSeries(series).getKey();
					return MessageFormat.format(rb.getString("gps.tooltip"),
							dataset.getX(series, item),
							ResourceBundleManager.getEnumString(eventType));
				}
			});

		}
		plot.setDataset(gpsData);
	}

	/**
	 * Returns a XYPlot for GPS info
	 * 
	 * @return XYPlot.
	 */
	private static XYPlot createBarPlot(Color color) {

		// Create renderer
		XYBarRenderer barRenderer = new XYBarRenderer();
		barRenderer.setDrawBarOutline(false);
		barRenderer.setUseYInterval(true);
		barRenderer.setBasePaint(color);
		barRenderer.setAutoPopulateSeriesPaint(false);
		barRenderer.setShadowVisible(false);
		barRenderer.setGradientPaintTransformer(null);

		XYBarPainter painter = new StandardXYBarPainter();
		barRenderer.setBarPainter(painter);

		// Create result plot
		XYPlot barPlot = new XYPlot(null, null, new NumberAxis(), barRenderer);
		barPlot.getRangeAxis().setVisible(false);

		return barPlot;
	}

	private static void populateBluetoothPlot(XYPlot plot, TraceData.Analysis analysis) {

		// create the dataset...
		final XYIntervalSeriesCollection bluetoothData = new XYIntervalSeriesCollection();
		if (analysis != null) {

			XYIntervalSeries bluetoothConnected = new XYIntervalSeries(
					BluetoothState.BLUETOOTH_CONNECTED);
			XYIntervalSeries bluetoothDisconnected = new XYIntervalSeries(
					BluetoothState.BLUETOOTH_DISCONNECTED);
			XYIntervalSeries bluetoothOff = new XYIntervalSeries(
					BluetoothState.BLUETOOTH_TURNED_OFF);

			bluetoothData.addSeries(bluetoothConnected);
			bluetoothData.addSeries(bluetoothDisconnected);
			// bluetoothStateCollection.addSeries(bluetoothOff);

			// Populate the data set
			Iterator<BluetoothInfo> iter = analysis.getBluetoothInfos().iterator();
			XYIntervalSeries series;
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					BluetoothInfo btEvent = iter.next();
					switch (btEvent.getBluetoothState()) {
					case BLUETOOTH_CONNECTED:
						series = bluetoothConnected;
						break;
					case BLUETOOTH_DISCONNECTED:
						series = bluetoothDisconnected;
						break;
					default:
						series = bluetoothOff;
						break;
					}
					series.add(btEvent.getBeginTimeStamp(), btEvent.getBeginTimeStamp(),
							btEvent.getEndTimeStamp(), 0.5, 0, 1);
				}

			}

			XYItemRenderer renderer = plot.getRenderer();
			renderer.setSeriesPaint(bluetoothData.indexOf(BluetoothState.BLUETOOTH_CONNECTED),
					new Color(34, 177, 76));
			renderer.setSeriesPaint(bluetoothData.indexOf(BluetoothState.BLUETOOTH_DISCONNECTED),
					Color.YELLOW);

			// Assign ToolTip to renderer
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					BluetoothState eventType = (BluetoothState) bluetoothData.getSeries(series)
							.getKey();
					return MessageFormat.format(rb.getString("bluetooth.tooltip"),
							dataset.getX(series, item),
							ResourceBundleManager.getEnumString(eventType));
				}
			});

		}
		plot.setDataset(bluetoothData);
	}

	private static void populateWifiPlot(XYPlot plot, TraceData.Analysis analysis) {

		// create the dataset...
		final XYIntervalSeriesCollection wifiData = new XYIntervalSeriesCollection();
		if (analysis != null) {

			Map<WifiState, XYIntervalSeries> seriesMap = new EnumMap<WifiState, XYIntervalSeries>(
					WifiState.class);
			for (WifiState eventType : WifiState.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				switch (eventType) {
				case WIFI_UNKNOWN:
				case WIFI_DISABLED:
					// Don't chart these
					break;
				default:
					wifiData.addSeries(series);
					break;
				}
			}

			// Populate the data set
			List<WifiInfo> wifiInfos = analysis.getWifiInfos();
			final Map<Double, WifiInfo> eventMap = new HashMap<Double, WifiInfo>(wifiInfos.size());
			Iterator<WifiInfo> iter = wifiInfos.iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					WifiInfo wifiEvent = iter.next();
					seriesMap.get(wifiEvent.getWifiState()).add(wifiEvent.getBeginTimeStamp(),
							wifiEvent.getBeginTimeStamp(), wifiEvent.getEndTimeStamp(), 0.5, 0, 1);
					eventMap.put(wifiEvent.getBeginTimeStamp(), wifiEvent);
				}
			}

			XYItemRenderer renderer = plot.getRenderer();
			for (WifiState eventType : WifiState.values()) {
				Color paint;
				switch (eventType) {
				case WIFI_CONNECTED:
				case WIFI_CONNECTING:
				case WIFI_DISCONNECTING:
					paint = new Color(34, 177, 76);
					break;
				case WIFI_DISCONNECTED:
				case WIFI_SUSPENDED:
					paint = Color.YELLOW;
					break;
				default:
					paint = Color.WHITE;
					break;
				}

				int index = wifiData.indexOf(eventType);
				if (index >= 0) {
					renderer.setSeriesPaint(index, paint);
				}
			}

			// Assign ToolTip to renderer
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					WifiState eventType = (WifiState) wifiData.getSeries(series).getKey();

					StringBuffer message = new StringBuffer(rb.getString("wifi.tooltip.prefix"));
					message.append(MessageFormat.format(rb.getString("wifi.tooltip"),
							dataset.getX(series, item),
							ResourceBundleManager.getEnumString(eventType)));
					switch (eventType) {
					case WIFI_CONNECTED:
						WifiInfo info = eventMap.get(dataset.getX(series, item));
						if (info != null && info.getWifiState() == WifiState.WIFI_CONNECTED) {
							message.append(MessageFormat.format(rb.getString("wifi.connTooltip"),
									info.getWifiMacAddress(), info.getWifiRSSI(),
									info.getWifiSSID()));
						}
						break;
					default:
						break;
					}
					message.append(rb.getString("wifi.tooltip.suffix"));
					return message.toString();
				}
			});

		}

		plot.setDataset(wifiData);
	}

	private static void populateCameraPlot(XYPlot plot, TraceData.Analysis analysis) {

		XYIntervalSeriesCollection cameraData = new XYIntervalSeriesCollection();

		if (analysis != null) {

			XYIntervalSeries series = new XYIntervalSeries(CameraState.CAMERA_ON);
			cameraData.addSeries(series);

			// Populate the data set
			Iterator<CameraInfo> iter = analysis.getCameraInfos().iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					CameraInfo cameraEvent = iter.next();
					if (cameraEvent.getCameraState() == CameraState.CAMERA_ON) {
						series.add(cameraEvent.getBeginTimeStamp(),
								cameraEvent.getBeginTimeStamp(), cameraEvent.getEndTimeStamp(),
								0.5, 0, 1);
					}
				}
			}

			// Assign ToolTip to renderer
			XYItemRenderer renderer = plot.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					return MessageFormat.format(rb.getString("camera.tooltip"), dataset.getX(
							series, item), ResourceBundleManager.getEnumString((Enum<?>) dataset
							.getSeriesKey(series)));
				}
			});

		}

		plot.setDataset(cameraData);
	}

	private static void populateScreenStatePlot(XYPlot plot, TraceData.Analysis analysis) {

		final XYIntervalSeriesCollection screenData = new XYIntervalSeriesCollection();
		if (analysis != null) {

			XYIntervalSeries series = new XYIntervalSeries(ScreenState.SCREEN_ON);
			screenData.addSeries(series);

			// Populate the data set
			final Map<Double, ScreenStateInfo> dataMap = new HashMap<Double, ScreenStateInfo>();
			Iterator<ScreenStateInfo> iter = analysis.getScreenStateInfos().iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					ScreenStateInfo screenEvent = iter.next();
					if (screenEvent.getScreenState() == ScreenState.SCREEN_ON) {
						series.add(screenEvent.getBeginTimeStamp(),
								screenEvent.getBeginTimeStamp(), screenEvent.getEndTimeStamp(),
								0.5, 0, 1);
						dataMap.put(screenEvent.getBeginTimeStamp(), screenEvent);
					}
				}
			}

			// Assign ToolTip to renderer
			XYItemRenderer renderer = plot.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {

					ScreenStateInfo si = dataMap.get(dataset.getXValue(series, item));
					if (si != null) {

						StringBuffer displayInfo = new StringBuffer(rb
								.getString("screenstate.tooltip.prefix"));
						int timeout = si.getScreenTimeout();
						displayInfo.append(MessageFormat.format(
								rb.getString("screenstate.tooltip.content"),
								ResourceBundleManager.getEnumString(si.getScreenState()),
								si.getScreenBrightness(),
								timeout > 0 ? timeout : rb.getString("screenstate.noTimeout")));
						displayInfo.append(rb.getString("screenstate.tooltip.suffix"));
						return displayInfo.toString();
					}
					return null;
				}
			});
		}

		plot.setDataset(screenData);
	}

	private static void populateBatteryPlot(XYPlot plot, TraceData.Analysis analysis) {

		XYSeries series = new XYSeries(0);

		if (analysis != null) {

			final List<BatteryInfo> batteryInfos = analysis.getBatteryInfos();

			if (batteryInfos.size() > 0 && analysis.getFilter().getTimeRange() != null) {
				BatteryInfo first = batteryInfos.get(0);
				series.add(analysis.getFilter().getTimeRange().getBeginTime(),
						first.getBatteryLevel());
			}
			for (BatteryInfo bi : batteryInfos) {
				series.add(bi.getBatteryTimeStamp(), bi.getBatteryLevel());
			}
			if (batteryInfos.size() > 0) {

				BatteryInfo last = batteryInfos.get(batteryInfos.size() - 1);
				if (analysis.getFilter().getTimeRange() != null) {
					series.add(analysis.getFilter().getTimeRange().getEndTime(),
							last.getBatteryLevel());
				} else {
					series.add(analysis.getTraceData().getTraceDuration(), last.getBatteryLevel());

				}
			}

			XYItemRenderer renderer = plot.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {

					BatteryInfo bi = batteryInfos.get(Math.min(item, batteryInfos.size() - 1));
					StringBuffer displayInfo = new StringBuffer(rb
							.getString("battery.tooltip.prefix"));
					displayInfo.append(MessageFormat.format(
							rb.getString("battery.tooltip.content"),
							bi.getBatteryLevel(),
							bi.getBatteryTemp(),
							bi.isBatteryState() ? rb.getString("battery.tooltip.connected") : rb
									.getString("battery.tooltip.disconnected")));
					displayInfo.append(rb.getString("battery.tooltip.suffix"));

					return displayInfo.toString();
				}

			});
		}

		plot.setDataset(new XYSeriesCollection(series));
	}

	/**
	 * Returns a XYPlot for Battery Info
	 * 
	 * @return XYPlot.
	 */
	private static XYPlot createBatteryPlot() {

		// Set up renderer
		StandardXYItemRenderer batteryRenderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.SHAPES_AND_LINES);
		batteryRenderer.setAutoPopulateSeriesShape(false);
		batteryRenderer.setBaseShape(DEFAULT_POINT_SHAPE);
		batteryRenderer.setSeriesPaint(0, Color.red);

		// Normalize the throughput axis so that it represents max value
		NumberAxis axis = new NumberAxis();
		axis.setVisible(false);
		axis.setAutoRange(false);
		axis.setRange(0, 110);

		// Create plot
		XYPlot batteryPlot = new XYPlot(null, null, axis, batteryRenderer);
		batteryPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		batteryPlot.getRangeAxis().setVisible(false);

		return batteryPlot;

	}

	private static void populateUserEventPlot(XYPlot plot, TraceData.Analysis analysis) {

		final XYIntervalSeriesCollection userInputData = new XYIntervalSeriesCollection();
		if (analysis != null) {

			// create the dataset...
			Map<UserEvent.UserEventType, XYIntervalSeries> seriesMap = new EnumMap<UserEvent.UserEventType, XYIntervalSeries>(
					UserEvent.UserEventType.class);
			for (UserEvent.UserEventType eventType : UserEvent.UserEventType.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				userInputData.addSeries(series);
			}
			// Populate the data set
			for (UserEvent event : analysis.getUserEvents()) {
				seriesMap.get(event.getEventType()).add(event.getPressTime(), event.getPressTime(),
						event.getReleaseTime(), 0.5, 0, 1);
			}

			// Assign ToolTip to renderer
			XYItemRenderer renderer = plot.getRenderer();
			renderer.setSeriesPaint(userInputData.indexOf(UserEventType.SCREEN_LANDSCAPE),
					Color.BLUE);
			renderer.setSeriesPaint(userInputData.indexOf(UserEventType.SCREEN_PORTRAIT),
					Color.BLUE);
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					UserEvent.UserEventType eventType = (UserEvent.UserEventType) userInputData
							.getSeries(series).getKey();
					return ResourceBundleManager.getEnumString(eventType);
				}
			});

		}

		plot.setDataset(userInputData);
	}

	/**
	 * Returns a XYPlot for User Event info
	 * 
	 * @return XYPlot.
	 */
	private static XYPlot createUserEventPlot() {

		// Create renderer
		XYBarRenderer userInputRenderer = new XYBarRenderer();
		userInputRenderer.setDrawBarOutline(false);
		userInputRenderer.setUseYInterval(true);
		userInputRenderer.setBasePaint(Color.gray);
		userInputRenderer.setAutoPopulateSeriesPaint(false);
		userInputRenderer.setShadowVisible(false);
		userInputRenderer.setGradientPaintTransformer(null);

		XYBarPainter painter = new StandardXYBarPainter();
		userInputRenderer.setBarPainter(painter);

		XYPlot userInputPlot = new XYPlot(null, null, new NumberAxis(), userInputRenderer);
		userInputPlot.getRangeAxis().setVisible(false);
		return userInputPlot;
	}

	private static void populateRadioPlot(XYPlot plot, TraceData.Analysis analysis) {

		XYSeries series = new XYSeries(0);
		if (analysis != null) {

			final List<RadioInfo> radioInfos = analysis.getRadioInfos();

			if (radioInfos.size() > 0 && analysis.getFilter().getTimeRange() != null) {
				RadioInfo first = radioInfos.get(0);
				series.add(analysis.getFilter().getTimeRange().getBeginTime(),
						first.getSignalStrength() < 0 ? first.getSignalStrength() : MIN_SIGNAL);
			}
			for (RadioInfo ri : radioInfos) {
				series.add(ri.getTimeStamp(), ri.getSignalStrength() < 0 ? ri.getSignalStrength()
						: MIN_SIGNAL);
			}
			if (radioInfos.size() > 0) {
				RadioInfo last = radioInfos.get(radioInfos.size() - 1);
				if (analysis.getFilter().getTimeRange() != null) {
					series.add(analysis.getFilter().getTimeRange().getEndTime(),
							last.getSignalStrength() < 0 ? last.getSignalStrength() : MIN_SIGNAL);
				} else {
					series.add(analysis.getTraceData().getTraceDuration(),
							last.getSignalStrength() < 0 ? last.getSignalStrength() : MIN_SIGNAL);
				}
			}

			// Assign ToolTip to renderer
			XYItemRenderer renderer = plot.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {

					RadioInfo ri = radioInfos.get(Math.min(item, radioInfos.size() - 1));
					if (ri.getSignalStrength() < 0) {
						if (ri.isLte()) {
							return MessageFormat.format(rb.getString("radio.tooltip.lte"),
									ri.getLteRsrp(), ri.getLteRsrq());
						} else {
							return MessageFormat.format(rb.getString("radio.tooltip"),
									ri.getSignalStrength());
						}
					} else {
						return rb.getString("radio.noSignal");
					}
				}

			});

		}

		plot.setDataset(new XYSeriesCollection(series));
	}

	/**
	 * Returns a XYPlot for Radio info
	 * 
	 * @return XYPlot.
	 */
	private static XYPlot createRadioPlot() {

		// Set up renderer
		StandardXYItemRenderer radioRenderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.SHAPES_AND_LINES);
		radioRenderer.setAutoPopulateSeriesShape(false);
		radioRenderer.setBaseShape(DEFAULT_POINT_SHAPE);
		radioRenderer.setSeriesPaint(0, Color.red);

		// Normalize the throughput axis so that it represents max value
		NumberAxis axis = new NumberAxis();
		axis.setVisible(false);
		axis.setAutoRange(false);
		axis.setRange(MIN_SIGNAL, MAX_SIGNAL);

		// Create plot
		XYPlot radioPlot = new XYPlot(null, null, axis, radioRenderer);
		radioPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		radioPlot.getRangeAxis().setVisible(false);

		return radioPlot;

	}
	
	/**
	 * Adds CPU data into plot.
	 * 
	 * @param plot
	 *            CPU data are added to plot
	 * @param analysis
	 *            Contains CPU data
	 */
	private static void populateCpuPlot(XYPlot plot, TraceData.Analysis analysis) {
		logger.fine("Starting populateCpuPlot()");
		if (analysis != null) {

			final CpuActivityList cpuAList = analysis.getCpuActivityList();
			boolean filterByTime = cpuAList.isFilterByTime();
			double beginTime = 0;
			double endTime = 0;
			if (filterByTime) {
				beginTime = cpuAList.getBeginTraceTime();
				endTime = cpuAList.getEndTraceTime();
				logger.log(Level.FINE, "begin: {0} end time: {1}", new Object[] { beginTime, endTime });
			}

			final List<CpuActivity> cpuData = cpuAList.getCpuActivityList();
			XYSeries series = new XYSeries(0);
			logger.log(Level.FINE, "Size of CPU data: " + cpuData.size());

			if (cpuData.size() > 0) {
				for (CpuActivity cpu : cpuData) {
					if (filterByTime) {
						logger.log(Level.FINE, "timestamp: {0}", cpu.getTimeStamp());
						if (cpu.getTimeStamp() >= beginTime && cpu.getTimeStamp() <= endTime) {
							logger.log(Level.FINE, "CPU usage: {0}", cpu.getCpuUsageTotalFiltered());
							series.add(cpu.getTimeStamp(), cpu.getCpuUsageTotalFiltered());
						}

					} else {
						logger.log(Level.FINE, "CPU usage: {0}", cpu.getCpuUsageTotalFiltered());
						series.add(cpu.getTimeStamp(), cpu.getCpuUsageTotalFiltered());
					}
				}
			}

			// Assign ToolTip to renderer
			XYItemRenderer renderer = plot.getRenderer();

			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					return GraphPanel.generateToolTip(cpuAList, cpuData, item);
				}
			});

			plot.setDataset(new XYSeriesCollection(series));
		}
	}

	public static String generateToolTip(CpuActivityList cpuAList, List<CpuActivity> cpuData, int item) {
		CpuActivity cpuA = cpuData.get(item);
		return constructCpuToolTipText(cpuAList, cpuA);
	}

	private static String constructCpuToolTipText(CpuActivityList cpuAList, CpuActivity cpuA) {

		StringBuffer toolTip = new StringBuffer(rb.getString("cpu.tooltip.prefix"));

		// generate initial opening total CPU tooltip
		String totalCpuTxt = MessageFormat.format(rb.getString("cpu.tooltip.total"), cpuA.getCpuUsageTotalFiltered());
		toolTip.append(totalCpuTxt);

		// generate individual process CPU tooltip
		String individualCpuToolTips = generateIndividualCpuToolTips(cpuAList, cpuA);
		if (individualCpuToolTips != null) {
			toolTip.append(individualCpuToolTips);
		}

		// generate other tooltip
		if (cpuA.getCpuUsageOther() > 0) {
			String otherCpu = MessageFormat.format(rb.getString("cpu.tooltip.other"), cpuA.getCpuUsageOther());
			toolTip.append(otherCpu);
		}

		// generate closing tooltip
		toolTip.append(rb.getString("cpu.tooltip.suffix"));

		return toolTip.toString();
	}

	private static String generateIndividualCpuToolTips(CpuActivityList cpuAList, CpuActivity cpuA) {

		List<String> processNames = cpuA.getProcessNames();
		List<Double> indCpuUsages = cpuA.getCpuUsages();
		String processName;
		Double cpuUsage;
		StringBuffer sb = new StringBuffer();
		String toolTip;

		if (processNames != null && indCpuUsages != null) {
			if (processNames.size() == indCpuUsages.size() && processNames.size() > 0) {
				for (int i = 0; i < processNames.size(); i++) {
					processName = processNames.get(i);
					cpuUsage = indCpuUsages.get(i);
					if (cpuAList.isProcessSelected(processName)) {
						toolTip = MessageFormat.format(rb.getString("cpu.tooltip.individual"), processName, cpuUsage);
						sb.append(toolTip);
					}
				}
				return sb.toString();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Sets up the CPU plot
	 * 
	 * @return plot CPU plot
	 */
	private static XYPlot createCpuPlot() {

		// Set up renderer
		StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES_AND_LINES);
		renderer.setAutoPopulateSeriesShape(false);
		renderer.setBaseShape(CPU_PLOT_POINT_SHAPE);
		renderer.setSeriesPaint(0, Color.black);

		// Normalize the throughput axis so that it represents max value
		NumberAxis axis = new NumberAxis();
		axis.setVisible(false);
		axis.setAutoRange(false);
		axis.setRange(MIN_CPU_USAGE, MAX_CPU_USAGE);

		// Create plot
		XYPlot plot = new XYPlot(null, null, axis, renderer);
		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		plot.getRangeAxis().setVisible(false);

		return plot;
	}	
	
	
	private static void populateThroughputPlot(XYPlot plot, TraceData.Analysis analysis) {

		XYSeries series = new XYSeries(0);
		if (analysis != null) {

			// Get packet iterators
			List<PacketInfo> packets = analysis.getPackets();
			final double maxTS = analysis.getTraceData().getTraceDuration();

			final List<String> tooltipList = new ArrayList<String>(1000);

			Double zeroTime = null;
			double lastTime = 0.0;
			for (Throughput t : Throughput.calculateThroughput(0.0, maxTS, analysis.getProfile()
					.getThroughputWindow(), packets)) {

				double time = t.getTime();
				double kbps = t.getKbps();
				if (kbps != 0.0) {
					if (zeroTime != null && zeroTime.doubleValue() != lastTime) {
						series.add(lastTime, 0.0);
						tooltipList.add(MessageFormat.format(THROUGHPUT_TOOLTIP, 0.0));
					}
					// Add slot to data set
					series.add(time, kbps);

					tooltipList.add(MessageFormat.format(THROUGHPUT_TOOLTIP, kbps));
					zeroTime = null;
				} else {
					if (zeroTime == null) {
						// Add slot to data set
						series.add(time, kbps);

						tooltipList.add(MessageFormat.format(THROUGHPUT_TOOLTIP, kbps));
						zeroTime = Double.valueOf(time);
					}
				}

				lastTime = time;
			}
			plot.getRenderer().setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {

					// Tooltip displays throughput value
					return tooltipList.get(item);
				}

			});
		}

		plot.setDataset(new XYSeriesCollection(series));
	}

	/**
	 * Creates a sample dataset.
	 * 
	 * @return Series 1.
	 */
	private static XYPlot createThroughputPlot() {

		// Set up renderer
		XYItemRenderer throughputRenderer = new StandardXYItemRenderer();
		throughputRenderer.setSeriesPaint(0, Color.red);

		// Normalize the throughput axis so that it represents max value
		NumberAxis axis = new NumberAxis();
		axis.setVisible(false);

		// Create plot
		XYPlot throughputPlot = new XYPlot(null, null, axis, throughputRenderer);
		throughputPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		throughputPlot.getRangeAxis().setVisible(false);

		return throughputPlot;
	}

	private static void populateBurstPlot(XYPlot plot, TraceData.Analysis analysis) {

		final XYIntervalSeriesCollection burstDataCollection = new XYIntervalSeriesCollection();
		if (analysis != null) {

			Map<BurstCategory, XYIntervalSeries> seriesMap = new EnumMap<BurstCategory, XYIntervalSeries>(
					BurstCategory.class);
			final Map<BurstCategory, List<Burst>> burstMap = new HashMap<BurstCategory, List<Burst>>();
			for (BurstCategory eventType : BurstCategory.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				burstDataCollection.addSeries(series);
				burstMap.put(eventType, new ArrayList<Burst>());
			}
			final List<Burst> burstStates = analysis.getBurstInfos();
			Iterator<Burst> iter = burstStates.iterator();
			while (iter.hasNext()) {
				Burst currEvent = iter.next();
				if (currEvent != null) {
					BurstCategory burstState = currEvent.getBurstCategory();
					if (burstState != null) {
						seriesMap.get(burstState).add(currEvent.getBeginTime(),
								currEvent.getBeginTime(), currEvent.getEndTime(), 0.5, 0, 1);
						burstMap.get(burstState).add(currEvent);
					}
				}
			}

			Color myGreen = new Color(34, 177, 76);
			Color lightGreen = new Color(134, 232, 162);

			XYItemRenderer renderer = plot.getRenderer();
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.TCP_PROTOCOL), Color.blue);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.TCP_LOSS_OR_DUP), Color.black);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.USER_INPUT), myGreen);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.SCREEN_ROTATION), lightGreen);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.CLIENT_APP), Color.red);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.SERVER_NET_DELAY), Color.yellow);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.LONG), Color.gray);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.PERIODICAL), Color.magenta);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.CPU), Color.cyan);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.UNKNOWN), Color.darkGray);

			// Assign ToolTip to renderer
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					BurstCategory eventType = (BurstCategory) burstDataCollection.getSeries(series)
							.getKey();
					Burst b = burstMap.get(eventType).get(item);
					final String PREFIX = "BurstCategory.";
					return MessageFormat.format(rb.getString(PREFIX + eventType.ordinal()),
							b.getPackets().size(), b.getBurstBytes(), b.getBurstThroughPut());
				}
			});

		}

		plot.setDataset(burstDataCollection);
	}

	private static XYPlot createBurstPlot() {
		// Create renderer
		XYBarRenderer burstStateRenderer = new XYBarRenderer();
		burstStateRenderer.setDrawBarOutline(false);
		burstStateRenderer.setUseYInterval(true);
		burstStateRenderer.setBasePaint(Color.gray);
		burstStateRenderer.setAutoPopulateSeriesPaint(false);
		burstStateRenderer.setShadowVisible(false);
		burstStateRenderer.setGradientPaintTransformer(null);

		XYBarPainter painter = new StandardXYBarPainter();
		burstStateRenderer.setBarPainter(painter);

		// Create result plot
		XYPlot burstPlot = new XYPlot(null, null, new NumberAxis(), burstStateRenderer);
		burstPlot.getRangeAxis().setVisible(false);
		return burstPlot;
	}

	private static void populateRrcPlot(XYPlot plot, TraceData.Analysis analysis) {

		final XYIntervalSeriesCollection rrcDataCollection = new XYIntervalSeriesCollection();
		if (analysis != null) {

			Map<RRCState, XYIntervalSeries> seriesMap = new EnumMap<RRCState, XYIntervalSeries>(
					RRCState.class);
			for (RRCState eventType : RRCState.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				rrcDataCollection.addSeries(series);
			}
			List<RrcStateRange> rrcStates = analysis.getRrcStateMachine().getRRcStateRanges();
			Iterator<RrcStateRange> iter = rrcStates.iterator();
			while (iter.hasNext()) {
				RrcStateRange currEvent = iter.next();
				RRCState state = currEvent.getState();
				if (state == RRCState.STATE_FACH || state == RRCState.TAIL_FACH) {
					seriesMap.get(state).add(currEvent.getBeginTime(), currEvent.getBeginTime(),
							currEvent.getEndTime(), 0.25, 0, 0.5);
				} else {
					seriesMap.get(state).add(currEvent.getBeginTime(), currEvent.getBeginTime(),
							currEvent.getEndTime(), 0.5, 0, 1);
				}

			}
			XYItemRenderer renderer = plot.getRenderer();
			Color dchGreen = new Color(34, 177, 76);
			Color fachOrange = new Color(255, 201, 14);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.STATE_IDLE), Color.white);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_IDLE), Color.white);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.PROMO_IDLE_DCH), Color.red);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_PROMOTION), Color.red);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.STATE_DCH), fachOrange);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_CONTINUOUS), fachOrange);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.TAIL_DCH),
					getTailPaint(fachOrange));
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_CR_TAIL),
					getTailPaint(fachOrange));
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_DRX_SHORT),
					getTailPaint(fachOrange));
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_DRX_LONG),
					getTailPaint(fachOrange));

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.STATE_FACH), dchGreen);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.TAIL_FACH),
					getTailPaint(dchGreen));

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.PROMO_FACH_DCH), Color.red);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.WIFI_IDLE), Color.white);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.WIFI_ACTIVE), fachOrange);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.WIFI_TAIL),
					getTailPaint(fachOrange));

			// Assign ToolTip to renderer
			final Profile profile = analysis.getProfile();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					RRCState eventType = (RRCState) rrcDataCollection.getSeries(series).getKey();
					final String PREFIX = "RRCTooltip.";
					if (eventType == RRCState.LTE_IDLE) {
						return MessageFormat.format(rb.getString(PREFIX + eventType),
								((ProfileLTE) profile).getIdlePingPeriod());
					}
					return rb.getString(PREFIX + eventType);
				}
			});

		}

		plot.setDataset(rrcDataCollection);
	}

	/**
	 * This method creates and returns the XYPlot for the RRC states
	 * 
	 * @return org.jfree.chart.plot.XYPlot The RRC plot.
	 * @see XYPlot.
	 */
	private static XYPlot createRrcPlot() {

		// Create renderer
		RRCChartRenderer rrcStateRenderer = new RRCChartRenderer();
		rrcStateRenderer.setDrawBarOutline(false);
		rrcStateRenderer.setUseYInterval(true);
		rrcStateRenderer.setBasePaint(Color.gray);
		rrcStateRenderer.setAutoPopulateSeriesPaint(false);
		rrcStateRenderer.setShadowVisible(false);
		rrcStateRenderer.setGradientPaintTransformer(null);

		XYBarPainter painter = new StandardXYBarPainter();
		rrcStateRenderer.setBarPainter(painter);

		// Create result plot
		XYPlot rrcStatesPlot = new XYPlot(null, null, new NumberAxis(), rrcStateRenderer);
		rrcStatesPlot.getRangeAxis().setVisible(false);

		return rrcStatesPlot;
	}

	private static void populateNetworkTyesPlot(XYPlot plot, TraceData.Analysis analysis) {
		if (analysis != null) {

			final XYIntervalSeriesCollection networkDataSeries = new XYIntervalSeriesCollection();
			final Map<NetworkType, XYIntervalSeries> seriesMap = new EnumMap<NetworkType, XYIntervalSeries>(NetworkType.class);
			createDataSeriesForAllNetworkTypes(seriesMap, networkDataSeries);
			
			Iterator<NetworkBearerTypeInfo> iter = analysis.getNetworTypeInfos().iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					NetworkBearerTypeInfo networkInfo = iter.next();
					if (networkInfo.getNetworkType() != NetworkType.none) {
						seriesMap.get(networkInfo.getNetworkType()).add(
								networkInfo.getBeginTimestamp(), networkInfo.getBeginTimestamp(),
								networkInfo.getEndTimestamp(), 0.5, 0, 1);
					}
				}
			} else {
				NetworkType nt = analysis.getTraceData().getNetworkType();
				if (nt != null && nt != NetworkType.none) {
					seriesMap.get(nt).add(0, 0,
							analysis.getTraceData().getTraceDuration(), 0.5, 0, 1);
				}
			}

			XYItemRenderer renderer = plot.getRenderer();
			setRenderingColorForDataSeries(renderer, networkDataSeries);

			// Assign ToolTip to renderer
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					NetworkType networkType = (NetworkType) networkDataSeries.getSeries(series).getKey();
					return MessageFormat.format(rb.getString("network.tooltip"),
							dataset.getX(series, item),
							ResourceBundleManager.getEnumString(networkType));
				}
			});
			
			plot.setDataset(networkDataSeries);
			
		} else {
			plot.setDataset(new XYIntervalSeriesCollection());
		}
	}

	/**
	 * Creates data series for all network types
	 * 
	 * @param networkDataSeries Collection of data series
	 */
	static void createDataSeriesForAllNetworkTypes(final Map<NetworkType, XYIntervalSeries> seriesMap,
			                                       final XYIntervalSeriesCollection networkDataSeries) {
		for (NetworkType nt : NetworkType.values()) {
			XYIntervalSeries series = new XYIntervalSeries(nt);
			seriesMap.put(nt, series);
			networkDataSeries.addSeries(series);
		}
	}

	/**
	 * Sets rendering color for all different network type data series. 
	 * @param renderer Renderer for the data series
	 * @param dataSeries Data series
	 */
	static void setRenderingColorForDataSeries(XYItemRenderer renderer, final XYIntervalSeriesCollection dataSeries) {
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.none), Color.WHITE);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.LTE), Color.RED);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.WIFI), Color.BLUE);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.UMTS), Color.PINK);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.ETHERNET), Color.BLACK);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSDPA), Color.YELLOW);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSPA), Color.ORANGE);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSPAP), Color.MAGENTA);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSUPA), Color.CYAN);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.GPRS), Color.GRAY);
	}

	/**
	 * Creating DchTail and FachTail Cross Hatch
	 * 
	 * @return Paint The Tail state paint object
	 */
	private static Paint getTailPaint(Color color) {

		BufferedImage bufferedImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, 5, 5);
		g2.setColor(color);
		g2.drawLine(0, 0, 5, 5);
		g2.drawLine(5, 5, 0, 0);
		g2.drawLine(0, 5, 5, 0);
		Rectangle2D rect = new Rectangle2D.Double(0, 0, 5, 5);
		return new TexturePaint(bufferedImage, rect);
	}
}
