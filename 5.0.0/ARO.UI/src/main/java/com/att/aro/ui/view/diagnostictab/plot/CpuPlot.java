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

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.peripheral.pojo.CpuActivity;
import com.att.aro.core.peripheral.pojo.CpuActivityList;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class CpuPlot implements IPlot {
	private ILogger logger = ContextAware.getAROConfigContext().getBean(
			ILogger.class);
	private CpuActivityList cpuAList;
	private List<CpuActivity> cpuData;

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		XYSeries series = new XYSeries(0);
		if (analysis == null) {
			logger.info("didn't get analysis trace data!");
		} else {
			TraceResultType resultType = analysis.getAnalyzerResult()
					.getTraceresult().getTraceResultType();
			if (resultType.equals(TraceResultType.TRACE_FILE)) {
				logger.info("didn't get analysis trace folder!");

			} else {
				TraceDirectoryResult traceresult = (TraceDirectoryResult) analysis
						.getAnalyzerResult().getTraceresult();

				cpuAList = traceresult.getCpuActivityList();
				boolean filterByTime = cpuAList.isFilterByTime();
				double beginTime = 0;
				double endTime = 0;
				if (filterByTime) {
					beginTime = cpuAList.getBeginTraceTime();
					endTime = cpuAList.getEndTraceTime();
				}

				cpuData = cpuAList.getCpuActivities();
				logger.debug("Size of CPU data: " + cpuData.size());

				if (cpuData.size() > 0) {
					for (CpuActivity cpu : cpuData) {
						if (filterByTime) {
//							logger.debug("timestamp: {0}" + cpu.getTimeStamp());
							if (cpu.getTimeStamp() >= beginTime
									&& cpu.getTimeStamp() <= endTime) {
//								logger.debug("CPU usage: {0}"+ cpu.getCpuUsageTotalFiltered());
								series.add(cpu.getTimeStamp(),
										cpu.getCpuUsageTotalFiltered());
							}

						} else {
//							logger.debug("CPU usage: {0}"+ cpu.getCpuUsageTotalFiltered());
							series.add(cpu.getTimeStamp(),
									cpu.getCpuUsageTotalFiltered());
						}
					}
				}

				// Assign ToolTip to renderer
				XYItemRenderer renderer = plot.getRenderer();

				renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset dataset,
							int series, int item) {
						return constructCpuToolTipText(cpuAList, cpuData, item);
					}
				});

			}
		}			
		plot.setDataset(new XYSeriesCollection(series));
	}

	private String constructCpuToolTipText(CpuActivityList cpuAList,
			List<CpuActivity> cpuData, int item) {
		CpuActivity cpuA = cpuData.get(item);
		StringBuffer toolTip = new StringBuffer(
				ResourceBundleHelper.getMessageString("cpu.tooltip.prefix"));

		// generate initial opening total CPU tooltip
		String totalCpuTxt = MessageFormat.format(
				ResourceBundleHelper.getMessageString("cpu.tooltip.total"),
				cpuA.getCpuUsageTotalFiltered());
		toolTip.append(totalCpuTxt);

		// generate individual process CPU tooltip
		String individualCpuToolTips = generateIndividualCpuToolTips(cpuAList,
				cpuA);
		if (individualCpuToolTips != null) {
			toolTip.append(individualCpuToolTips);
		}

		// generate other tooltip
		if (cpuA.getCpuUsageOther() > 0) {
			String otherCpu = MessageFormat.format(
					ResourceBundleHelper.getMessageString("cpu.tooltip.other"),
					cpuA.getCpuUsageOther());
			toolTip.append(otherCpu);
		}

		// generate closing tooltip
		toolTip.append(ResourceBundleHelper
				.getMessageString("cpu.tooltip.suffix"));

		return toolTip.toString();
	}

	private String generateIndividualCpuToolTips(CpuActivityList cpuAList,
			CpuActivity cpuA) {

		List<String> processNames = cpuA.getProcessNames();
		List<Double> indCpuUsages = cpuA.getCpuUsages();
		String processName;
		Double cpuUsage;
		StringBuffer sb = new StringBuffer();
		String toolTip;

		if (processNames != null && indCpuUsages != null) {
			if (processNames.size() == indCpuUsages.size()
					&& processNames.size() > 0) {
				for (int i = 0; i < processNames.size(); i++) {
					processName = processNames.get(i);
					cpuUsage = indCpuUsages.get(i);
					if (cpuAList.isProcessSelected(processName)) {
						toolTip = MessageFormat.format(ResourceBundleHelper
								.getMessageString("cpu.tooltip.individual"),
								processName, cpuUsage);
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

}
