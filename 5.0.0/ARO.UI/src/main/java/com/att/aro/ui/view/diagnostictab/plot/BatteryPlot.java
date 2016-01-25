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

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.peripheral.pojo.BatteryInfo;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class BatteryPlot implements IPlot{
	private ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);
	private List<BatteryInfo> batteryInfos;
	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		XYSeries series = new XYSeries(0);
		if (analysis == null ) {
			logger.info("analysis data is null");
		}else{
			TraceResultType resultType = analysis.getAnalyzerResult().getTraceresult().getTraceResultType();
			if(resultType.equals(TraceResultType.TRACE_FILE)){
				logger.info("didn't get analysis trace data!");

			}else{
 			TraceDirectoryResult traceresult = (TraceDirectoryResult)analysis.getAnalyzerResult().getTraceresult();
			AnalysisFilter filter = analysis.getAnalyzerResult().getFilter(); 
 			 batteryInfos = traceresult.getBatteryInfos();

			if (batteryInfos.size() > 0 && filter.getTimeRange() != null) {
				BatteryInfo first = batteryInfos.get(0);
				series.add(filter.getTimeRange().getBeginTime(),
						first.getBatteryLevel());
			}
			for (BatteryInfo bi : batteryInfos) {
				series.add(bi.getBatteryTimeStamp(), bi.getBatteryLevel());
			}
			if (batteryInfos.size() > 0) {

				BatteryInfo last = batteryInfos.get(batteryInfos.size() - 1);
				if (filter.getTimeRange() != null) {
					series.add(filter.getTimeRange().getEndTime(),
							last.getBatteryLevel());
				} else {
					series.add(traceresult.getTraceDuration(), last.getBatteryLevel());

				}
			}

			XYItemRenderer renderer = plot.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {

					BatteryInfo bi = batteryInfos.get(Math.min(item, batteryInfos.size() - 1));
					StringBuffer displayInfo = new StringBuffer(
							ResourceBundleHelper.getMessageString("battery.tooltip.prefix"));
					displayInfo.append(MessageFormat.format(
							ResourceBundleHelper.getMessageString("battery.tooltip.content"),
							bi.getBatteryLevel(),
							bi.getBatteryTemp(),
							bi.isBatteryState() ? 
									ResourceBundleHelper.getMessageString("battery.tooltip.connected") : 
										ResourceBundleHelper.getMessageString("battery.tooltip.disconnected")));
					displayInfo.append(ResourceBundleHelper.getMessageString("battery.tooltip.suffix"));

					return displayInfo.toString();
				}

			});
		}

		plot.setDataset(new XYSeriesCollection(series));		
		}

	}

}
