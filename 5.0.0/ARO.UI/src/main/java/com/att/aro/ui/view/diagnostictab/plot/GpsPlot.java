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
package com.att.aro.ui.view.diagnostictab.plot;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.peripheral.pojo.GpsInfo;
import com.att.aro.core.peripheral.pojo.GpsInfo.GpsState;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class GpsPlot implements IPlot{
	
	XYIntervalSeriesCollection gpsData = new XYIntervalSeriesCollection();

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		if (analysis != null) {
			gpsData.removeAllSeries();
			// create the GPS dataset...
			Map<GpsState, XYIntervalSeries> seriesMap = new EnumMap<GpsState, XYIntervalSeries>(
					GpsState.class);
			for (GpsState eventType : GpsState.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				gpsData.addSeries(series);
			}
			
			Iterator<GpsInfo> iter = analysis.getAnalyzerResult().getTraceresult().getGpsInfos().iterator();
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
					return MessageFormat.format(ResourceBundleHelper.getMessageString("gps.tooltip"),
							dataset.getX(series, item),
							ResourceBundleHelper.getEnumString(eventType));
				}
			});

		}
		plot.setDataset(gpsData);
	}

}
