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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.peripheral.pojo.WifiInfo;
import com.att.aro.core.peripheral.pojo.WifiInfo.WifiState;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class WifiPlot implements IPlot{
	private ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);	
	private XYIntervalSeriesCollection wifiData;
	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		 wifiData = new XYIntervalSeriesCollection();

		if (analysis == null ) {
			logger.info("didn't get analysis trace data!  ");
		}else{
			TraceResultType resultType = analysis.getAnalyzerResult().getTraceresult().getTraceResultType();
			if(resultType.equals(TraceResultType.TRACE_FILE)){
				logger.info("it is not contain the file ");
			}else{
	 		TraceDirectoryResult traceresult = (TraceDirectoryResult)analysis.getAnalyzerResult().getTraceresult();

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
			List<WifiInfo> wifiInfos = traceresult.getWifiInfos();
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

					StringBuffer message = new StringBuffer(ResourceBundleHelper.getMessageString("wifi.tooltip.prefix"));
					message.append(MessageFormat.format(ResourceBundleHelper.getMessageString("wifi.tooltip"),
							dataset.getX(series, item),
							ResourceBundleHelper.getEnumString(eventType)));
					switch (eventType) {
					case WIFI_CONNECTED:
						WifiInfo info = eventMap.get(dataset.getX(series, item));
						if (info != null && info.getWifiState() == WifiState.WIFI_CONNECTED) {
							message.append(MessageFormat.format(ResourceBundleHelper.getMessageString("wifi.connTooltip"),
									info.getWifiMacAddress(), info.getWifiRSSI(),
									info.getWifiSSID()));
						}
						break;
					default:
						break;
					}
					message.append(ResourceBundleHelper.getMessageString("wifi.tooltip.suffix"));
					return message.toString();
				}
			});
		}
		}

		plot.setDataset(wifiData);
	}

}
