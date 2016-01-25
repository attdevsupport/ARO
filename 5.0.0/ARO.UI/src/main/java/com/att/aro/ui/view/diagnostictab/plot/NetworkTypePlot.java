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

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.NetworkBearerTypeInfo;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.peripheral.pojo.NetworkType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class NetworkTypePlot implements IPlot {
	private ILogger logger = ContextAware.getAROConfigContext().getBean(
			ILogger.class);

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		plot.setDataset(new XYIntervalSeriesCollection());

		if (analysis == null) {
			logger.info("no trace data here");
		} else {
			if (analysis.getAnalyzerResult().getTraceresult()
					.getTraceResultType() == TraceResultType.TRACE_FILE) {
				logger.info("no trace folder data here");
			} else {
				TraceDirectoryResult traceresult = (TraceDirectoryResult) analysis
						.getAnalyzerResult().getTraceresult();
				final XYIntervalSeriesCollection networkDataSeries = new XYIntervalSeriesCollection();
				final Map<NetworkType, XYIntervalSeries> seriesMap = new EnumMap<NetworkType, XYIntervalSeries>(
						NetworkType.class);
				createDataSeriesForAllNetworkTypes(seriesMap, networkDataSeries);

				Iterator<NetworkBearerTypeInfo> iter = traceresult
						.getNetworkTypeInfos().iterator();
				if (iter.hasNext()) {
					while (iter.hasNext()) {
						NetworkBearerTypeInfo networkInfo = iter.next();
						if (networkInfo.getNetworkType() != NetworkType.none) {
							seriesMap.get(networkInfo.getNetworkType()).add(
									networkInfo.getBeginTimestamp(),
									networkInfo.getBeginTimestamp(),
									networkInfo.getEndTimestamp(), 0.5, 0, 1);
						}
					}
				} else {
					NetworkType nt = traceresult.getNetworkType();
					if (nt != null && nt != NetworkType.none) {
						seriesMap.get(nt).add(0, 0,
								traceresult.getTraceDuration(), 0.5, 0, 1);
					}
				}

				XYItemRenderer renderer = plot.getRenderer();
				setRenderingColorForDataSeries(renderer, networkDataSeries);

				// Assign ToolTip to renderer
				renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset dataset,
							int series, int item) {
						NetworkType networkType = (NetworkType) networkDataSeries
								.getSeries(series).getKey();
						return MessageFormat.format(ResourceBundleHelper
								.getMessageString("network.tooltip"), dataset
								.getX(series, item), ResourceBundleHelper
								.getEnumString(networkType));
					}
				});

				plot.setDataset(networkDataSeries);
			}

		}
	}

	private void createDataSeriesForAllNetworkTypes(
			final Map<NetworkType, XYIntervalSeries> seriesMap,
			final XYIntervalSeriesCollection networkDataSeries) {
		for (NetworkType nt : NetworkType.values()) {
			XYIntervalSeries series = new XYIntervalSeries(nt);
			seriesMap.put(nt, series);
			networkDataSeries.addSeries(series);
		}
	}

	private void setRenderingColorForDataSeries(XYItemRenderer renderer,
			final XYIntervalSeriesCollection dataSeries) {
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.none),
				Color.WHITE);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.LTE), Color.RED);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.WIFI),
				Color.BLUE);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.UMTS),
				Color.PINK);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.ETHERNET),
				Color.BLACK);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSDPA),
				Color.YELLOW);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSPA),
				Color.ORANGE);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSPAP),
				Color.MAGENTA);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.HSUPA),
				Color.CYAN);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.GPRS),
				Color.GRAY);
		renderer.setSeriesPaint(dataSeries.indexOf(NetworkType.EDGE),
				Color.LIGHT_GRAY);
	}
}
