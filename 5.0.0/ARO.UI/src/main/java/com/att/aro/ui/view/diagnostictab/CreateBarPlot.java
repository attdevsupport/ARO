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
package com.att.aro.ui.view.diagnostictab;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

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
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.YIntervalRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;


public class CreateBarPlot{
	
	/**
	 * Returns a XYPlot 
	 * 
	 * @return XYPlot.
	 */
	//createWakelockStatePlot color is yellow, other is gray, createAlarmPlot need to set numberAxis, create usereventplot
	//createBurstPlot(),createRrcPlot() 
	public XYPlot drawXYBarPlot(Color color,boolean setAxis) {
		// Create renderer
		XYBarRenderer barRenderer = new XYBarRenderer();
		barRenderer.setDrawBarOutline(false);
		barRenderer.setUseYInterval(true);
		barRenderer.setBasePaint(color);
		barRenderer.setAutoPopulateSeriesPaint(false);
		barRenderer.setShadowVisible(false);
		barRenderer.setGradientPaintTransformer(null);
		barRenderer.setBarPainter(new StandardXYBarPainter());
		NumberAxis axis = new NumberAxis();
		if(setAxis){
			axis.setVisible(false);
			axis.setAutoRange(false);
			axis.setRange(0, 1);
		}		
		// Create result plot
		XYPlot barPlot = new XYPlot(null, null, axis, barRenderer);
		barPlot.getRangeAxis().setVisible(false);
		return barPlot;
	}
	
	//createBatteryPlot(), createRadioPlot(), createCpuPlot()
	public XYPlot drawStandardXYPlot(Shape shape,Color color, int minSignal, int maxSignal){
		// Set up renderer
		StandardXYItemRenderer batteryRenderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.SHAPES_AND_LINES);
		batteryRenderer.setAutoPopulateSeriesShape(false);
		batteryRenderer.setBaseShape(shape);
		batteryRenderer.setSeriesPaint(0, color);

		// Normalize the throughput axis so that it represents max value
		NumberAxis axis = new NumberAxis();
		axis.setVisible(false);
		axis.setAutoRange(false);
		axis.setRange(minSignal, maxSignal);

		// Create plot
		XYPlot barPlot = new XYPlot(null, null, axis, batteryRenderer);
		barPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		barPlot.getRangeAxis().setVisible(false);

		return barPlot;		
	}
	
	//createThroughputPlot()
	public XYPlot drawXYItemPlot(){

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
	
	public XYPlot drawYIntervalPlot(){
		// Create the plot renderer
		YIntervalRenderer renderer = new YIntervalRenderer() {
			private static final long serialVersionUID = 1L;

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

}
