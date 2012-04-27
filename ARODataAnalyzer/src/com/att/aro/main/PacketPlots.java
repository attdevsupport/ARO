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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalDataItem;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleEdge;

import com.att.aro.model.ApplicationSelection;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.PacketInfo;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData;

/**
 * Represents the packet upload and download plots on the Trace chart in the
 * Diagnostics tab.
 */
public class PacketPlots {

	/**
	 * Data item that stores the packet so it is available for tooltips
	 */
	private static class PacketDataItem extends YIntervalDataItem {
		private static final long serialVersionUID = 1L;

		private static final ResourceBundle rb = ResourceBundleManager
				.getDefaultBundle();
		private static final String TOOLTIP_PREFIX = rb
				.getString("packet.tooltip.prefix");
		private static final String PACKET_TOOLTIP = rb
				.getString("packet.tooltip.packet");
		private static final String SESSION_TOOLTIP = rb
				.getString("packet.tooltip.session");
		private static final String RR_TOOLTIP = rb
				.getString("packet.tooltip.reqresp");
		private static final String TOOLTIP_SUFFIX = rb
				.getString("packet.tooltip.suffix");

		private String tooltip;

		/**
		 * Initializes a new instance of the PacketPlots class.
		 * 
		 * @param packet
		 *            The PacketInfo instance containing the the various
		 *            informations about the packet to be plotted.
		 */
		public PacketDataItem(PacketInfo packet) {
			super(packet.getTimeStamp(), 0, 0, 1);

			// Build tooltip message
			StringBuffer displayInfo = new StringBuffer(TOOLTIP_PREFIX);

			// Packet info for tooltip
			displayInfo.append(MessageFormat.format(PACKET_TOOLTIP,
					packet.getId(), packet.getTimeStamp(),
					checkNull(packet.getAppName())));

			// Session info for tooltip
			TCPSession session = packet.getSession();
			if (session != null) {
				List<PacketInfo> packets = session.getPackets();
				double beginTime = packets.get(0).getTimeStamp();
				double endTime = packets.get(packets.size() - 1).getTimeStamp();

				displayInfo.append(MessageFormat.format(SESSION_TOOLTIP,
						beginTime, endTime, session.getRemoteIP()
								.getHostAddress(),
						new Integer(session.getRemotePort()).toString(),
						new Integer(session.getLocalPort()).toString()));
			}

			// Request/response info for tooltip
			HttpRequestResponseInfo httpRequestResponse = packet
					.getRequestResponseInfo();
			if (httpRequestResponse != null) {
				HttpRequestResponseInfo req;
				HttpRequestResponseInfo resp;
				if (httpRequestResponse.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
					req = httpRequestResponse;
					resp = httpRequestResponse.getAssocReqResp();
				} else {
					req = httpRequestResponse.getAssocReqResp();
					resp = httpRequestResponse;
				}

				String objName = req != null ? req.getObjNameWithoutParams()
						: null;
				String length = resp != null ? NumberFormat
						.getIntegerInstance().format(resp.getContentLength())
						: null;
				String type = resp != null ? resp.getContentType() : null;

				displayInfo
						.append(MessageFormat.format(RR_TOOLTIP,
								checkNull(objName), checkNull(length),
								checkNull(type)));
			}

			displayInfo.append(TOOLTIP_SUFFIX);
			this.tooltip = displayInfo.toString();
		}

		/**
		 * Returns the tooltip text for the plot.
		 * 
		 * @return The tooltip text for the plot.
		 */
		public String getTooltip() {
			return tooltip;
		}

	}

	/**
	 * Used to represent a series of packets which is all packets related to a
	 * single application.
	 */
	private class PacketSeries extends YIntervalSeries {
		private static final long serialVersionUID = 1L;

		public PacketSeries(String appName) {
			super(appName != null ? appName : "", false, true);
		}

		public void add(PacketDataItem item) {
			super.add(item, true);
		}

		/**
		 * @see org.jfree.data.xy.YIntervalSeries#getDataItem(int)
		 */
		@Override
		public PacketDataItem getDataItem(int index) {
			return (PacketDataItem) super.getDataItem(index);
		}

	}

	/**
	 * Tooltip generator for a hovered packet
	 */
	private class PacketToolTipGenerator implements XYToolTipGenerator {

		@Override
		public String generateToolTip(XYDataset dataset, int series, int item) {

			PacketSeries pSeries = (PacketSeries) ((YIntervalSeriesCollection) dataset)
					.getSeries(series);
			return pSeries.getDataItem(item).getTooltip();
		}

	}

	private XYPlot ulPlot;
	private XYPlot dlPlot;

	/**
	 * Constructor
	 */
	public PacketPlots() {

		this.ulPlot = createPlot();
		this.dlPlot = createPlot();

	}

	/**
	 * Creates the plot for the uplink and downlink packets using the specified
	 * trace analysis data.
	 * 
	 * @param analysis
	 *            - The trace analysis data.
	 */
	public void populatePacketPlots(TraceData.Analysis analysis) {

		LinkedHashMap<String, PacketSeries> ulDatasets = new LinkedHashMap<String, PacketSeries>();
		LinkedHashMap<String, PacketSeries> dlDatasets = new LinkedHashMap<String, PacketSeries>();

		Map<String, ApplicationSelection> appSel = Collections.emptyMap();
		if (analysis != null) {
			appSel = analysis.getApplicationSelections();

			LinkedHashMap<String, PacketSeries> datasets;
			for (PacketInfo packet : analysis.getPackets()) {
				if (packet.getDir() == null) {
					continue;
				}
				switch (packet.getDir()) {
				case UPLINK:
					datasets = ulDatasets;
					break;
				case DOWNLINK:
					datasets = dlDatasets;
					break;
				default:
					continue;
				}

				// Add the packet to the proper series based on app name
				String appName = packet.getAppName();
				PacketSeries series = datasets.get(appName);
				if (series == null) {
					series = new PacketSeries(appName);
					datasets.put(appName, series);
				}
				series.add(new PacketDataItem(packet));

			}
		}

		populatePacketPlot(dlPlot, appSel, dlDatasets);
		populatePacketPlot(ulPlot, appSel, ulDatasets);
	}

	/**
	 * Returns the plot for the uplink packets (Packets UL) in the Trace chart.
	 * 
	 * @return An XYPlot object that contains the coordinates of the plot for
	 *         the uplink packets.
	 */
	public XYPlot getUlPlot() {
		return ulPlot;
	}

	/**
	 * Returns the plot for the downlink packets (Packets DL) in the Trace
	 * chart.
	 * 
	 * @return An XYPlot object that contains the coordinates of the plot for
	 *         the downlink packets.
	 */
	public XYPlot getDlPlot() {
		return dlPlot;
	}

	/**
	 * Creates the XYIntervalSeries for the uplink and downlink packets plot.
	 * 
	 * @param plot
	 *            The XYPlot for the uplink/downlink plots.
	 * @param appSel
	 *            The selected apps.
	 * @param dataset
	 *            The uplink/downlink datasets.
	 */
	private void populatePacketPlot(XYPlot plot,
			Map<String, ApplicationSelection> appSel,
			LinkedHashMap<String, PacketSeries> dataset) {

		// Create the XY data set
		YIntervalSeriesCollection coll = new YIntervalSeriesCollection();
		XYItemRenderer renderer = plot.getRenderer();
		for (PacketSeries series : dataset.values()) {
			coll.addSeries(series);

			Comparable<?> key = series.getKey();
			ApplicationSelection sel = appSel.get(key);
			if (sel != null) {
				renderer.setSeriesPaint(coll.indexOf(key), sel.getColor());
			}
		}

		// Create tooltip generator
		renderer.setBaseToolTipGenerator(new PacketToolTipGenerator());

		plot.setDataset(coll);
	}

	/**
	 * The utility method that creates the packet plots from a packet series map
	 */
	private XYPlot createPlot() {

		// Create the plot renderer
		YIntervalRenderer renderer = new YIntervalRenderer() {
			public void drawItem(Graphics2D g2, XYItemRendererState state,
					Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
					ValueAxis domainAxis, ValueAxis rangeAxis,
					XYDataset dataset, int series, int item,
					CrosshairState crosshairState, int pass) {

				// setup for collecting optional entity info...
				Shape entityArea = null;
				EntityCollection entities = null;
				if (info != null) {
					entities = info.getOwner().getEntityCollection();
				}

				IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

				double x = intervalDataset.getXValue(series, item);
				double yLow = intervalDataset.getStartYValue(series, item);
				double yHigh = intervalDataset.getEndYValue(series, item);

				RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
				RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

				double xx = domainAxis
						.valueToJava2D(x, dataArea, xAxisLocation);
				double yyLow = rangeAxis.valueToJava2D(yLow, dataArea,
						yAxisLocation);
				double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea,
						yAxisLocation);

				Paint p = getItemPaint(series, item);
				Stroke s = getItemStroke(series, item);

				Line2D line = null;
				PlotOrientation orientation = plot.getOrientation();
				if (orientation == PlotOrientation.HORIZONTAL) {
					line = new Line2D.Double(yyLow, xx, yyHigh, xx);
				} else if (orientation == PlotOrientation.VERTICAL) {
					line = new Line2D.Double(xx, yyLow, xx, yyHigh);
				}
				g2.setPaint(p);
				g2.setStroke(s);
				g2.draw(line);

				// add an entity for the item...
				if (entities != null) {
					if (entityArea == null) {
						entityArea = line.getBounds();
					}
					String tip = null;
					XYToolTipGenerator generator = getToolTipGenerator(series,
							item);
					if (generator != null) {
						tip = generator.generateToolTip(dataset, series, item);
					}
					XYItemEntity entity = new XYItemEntity(entityArea, dataset,
							series, item, tip, null);
					entities.add(entity);
				}

			}

		};
		renderer.setAdditionalItemLabelGenerator(null);
		renderer.setBaseShape(new Rectangle());
		renderer.setAutoPopulateSeriesShape(false);
		renderer.setAutoPopulateSeriesPaint(false);
		renderer.setBasePaint(Color.GRAY);

		// Create the plot
		XYPlot plot = new XYPlot(null, null, new NumberAxis(), renderer);
		plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		plot.getRangeAxis().setVisible(false);

		return plot;
	}

	private static String checkNull(String s) {
		return s != null ? s : "";
	}
}
