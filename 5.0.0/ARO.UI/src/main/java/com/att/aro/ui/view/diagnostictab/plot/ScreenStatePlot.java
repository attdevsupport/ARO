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
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo.ScreenState;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.diagnostictab.GraphPanel;

public class ScreenStatePlot implements IPlot {
	private ILogger logger = ContextAware.getAROConfigContext().getBean(
			ILogger.class);
	private XYIntervalSeriesCollection screenData = new XYIntervalSeriesCollection();

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		if (analysis == null) {
			logger.info("analysis data is null");
		} else {
			screenData.removeAllSeries();
			TraceResultType resultType = analysis.getAnalyzerResult()
					.getTraceresult().getTraceResultType();
			if (resultType.equals(TraceResultType.TRACE_FILE)) {
				logger.info("didn't get analysis trace data!");

			} else {
	 			TraceDirectoryResult traceresult = (TraceDirectoryResult)analysis.getAnalyzerResult().getTraceresult();

				XYIntervalSeries series = new XYIntervalSeries(
						ScreenState.SCREEN_ON);
				screenData.addSeries(series);

				// Populate the data set
				final Map<Double, ScreenStateInfo> dataMap = new HashMap<Double, ScreenStateInfo>();
				Iterator<ScreenStateInfo> iter = traceresult
						.getScreenStateInfos().iterator();
				if (iter.hasNext()) {
					while (iter.hasNext()) {
						ScreenStateInfo screenEvent = iter.next();
						if (screenEvent.getScreenState() == ScreenState.SCREEN_ON) {
							series.add(screenEvent.getBeginTimeStamp(),
									screenEvent.getBeginTimeStamp(),
									screenEvent.getEndTimeStamp(), 0.5, 0, 1);
							dataMap.put(screenEvent.getBeginTimeStamp(),
									screenEvent);
						}
					}
				}

				// Assign ToolTip to renderer
				XYItemRenderer renderer = plot.getRenderer();
				renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {

					@Override
					public String generateToolTip(XYDataset dataset,
							int series, int item) {

						ScreenStateInfo si = dataMap.get(dataset.getXValue(
								series, item));
						if (si != null) {

							StringBuffer displayInfo = new StringBuffer(
									ResourceBundleHelper
											.getMessageString("screenstate.tooltip.prefix"));
							int timeout = si.getScreenTimeout();
							displayInfo.append(MessageFormat.format(
									ResourceBundleHelper
											.getMessageString("screenstate.tooltip.content"),
									ResourceBundleHelper.getEnumString(si
											.getScreenState()),
									si.getScreenBrightness(),
									timeout > 0 ? timeout
											: ResourceBundleHelper
													.getMessageString("screenstate.noTimeout")));
							displayInfo.append(ResourceBundleHelper
									.getMessageString("screenstate.tooltip.suffix"));
							return displayInfo.toString();
						}
						return null;
					}
				});
			}
		}
		plot.setDataset(screenData);
	}

}
