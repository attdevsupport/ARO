package com.att.aro.ui.view.diagnostictab.plot;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;
import com.att.aro.ui.utils.ResourceBundleHelper;
/**
 * User Input
 */
public class UserEventPlot implements IPlot{
	XYIntervalSeriesCollection userInputData = new XYIntervalSeriesCollection();
	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		if (analysis != null) {
			userInputData.removeAllSeries();
			// create the dataset...
			Map<UserEvent.UserEventType, XYIntervalSeries> seriesMap = new EnumMap<UserEvent.UserEventType, XYIntervalSeries>(
					UserEvent.UserEventType.class);
			for (UserEvent.UserEventType eventType : UserEvent.UserEventType.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				userInputData.addSeries(series);
			}
			// Populate the data set
			//need to add something here
			for (UserEvent event : analysis.getAnalyzerResult().getTraceresult().getUserEvents()) {
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
					return ResourceBundleHelper.getEnumString(eventType);
				}
			});

		}

		plot.setDataset(userInputData);
//		return plot;
	}

}
