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
import java.util.HashMap;
import java.util.Iterator;
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
import com.att.aro.core.peripheral.pojo.WakelockInfo;
import com.att.aro.core.peripheral.pojo.WakelockInfo.WakelockState;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class WakeLockPlot implements IPlot{
	private ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);	
	private Map<Double, WakelockInfo> dataMap = new HashMap<Double, WakelockInfo>();

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		 XYIntervalSeriesCollection wakelockData = new XYIntervalSeriesCollection();
			if (analysis == null ) {
				logger.info("analysis data is null");
			}else{
				TraceResultType resultType = analysis.getAnalyzerResult().getTraceresult().getTraceResultType();
				if(resultType.equals(TraceResultType.TRACE_FILE)){
					logger.info("didn't get analysis trace data!");

				}else{
		 			TraceDirectoryResult traceresult = (TraceDirectoryResult)analysis.getAnalyzerResult().getTraceresult();

					XYIntervalSeries series = new XYIntervalSeries(
							WakelockState.WAKELOCK_ACQUIRED);
					wakelockData.addSeries(series);

					// Populate the data set
					Iterator<WakelockInfo> iter = traceresult.getWakelockInfos().iterator();
					if (iter.hasNext()) {
						WakelockInfo lastEvent = iter.next();
						logger.debug("Wakelock Plotting");
						// Check whether WAKELOCK was acquired before logging begins.
						if (lastEvent.getWakelockState() == WakelockState.WAKELOCK_RELEASED) {
							series.add(0,0,lastEvent.getBeginTimeStamp(), 0.5, 0, 1);
							dataMap.put(lastEvent.getBeginTimeStamp(), lastEvent);
						}
						while (iter.hasNext()) {
							WakelockInfo currEvent = iter.next();
							if (lastEvent.getWakelockState() == WakelockState.WAKELOCK_ACQUIRED) {
								logger.debug("Wakelock acquired curr " + currEvent.getBeginTimeStamp());
								logger.debug("Wakelock acquired last " + lastEvent.getBeginTimeStamp());
								series.add(lastEvent.getBeginTimeStamp(),
										lastEvent.getBeginTimeStamp(),
										currEvent.getBeginTimeStamp(), 0.5, 0, 1);
								dataMap.put(lastEvent.getBeginTimeStamp(), lastEvent);
							}
							lastEvent = currEvent;
						}
						if (lastEvent.getWakelockState() == WakelockState.WAKELOCK_ACQUIRED) {
							series.add(lastEvent.getBeginTimeStamp(), lastEvent
									.getBeginTimeStamp(), traceresult
									.getTraceDuration(), 0.5, 0, 1);
							dataMap.put(lastEvent.getBeginTimeStamp(), lastEvent);
						}
					}

					// Assign ToolTip to renderer
					XYItemRenderer renderer = plot.getRenderer();
					renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

						@Override
						public String generateToolTip(XYDataset dataset, int series,
								int item) {

							WakelockInfo wi = dataMap.get(dataset.getXValue(series,
									item));
							if (wi != null) {

								StringBuffer displayInfo = new StringBuffer(
										ResourceBundleHelper.getMessageString("wakelock.tooltip.prefix"));
								displayInfo.append(MessageFormat.format(
										ResourceBundleHelper.getMessageString("wakelock.tooltip.content"),
										ResourceBundleHelper.getEnumString(wi
												.getWakelockState()), wi.getBeginTimeStamp()
										));
								displayInfo.append(
										ResourceBundleHelper.getMessageString("wakelock.tooltip.suffix"));
								return displayInfo.toString();
							}
							return null;
						}
					});

				}
			}

		plot.setDataset(wakelockData);		
	}

}
