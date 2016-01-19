package com.att.aro.ui.view.diagnostictab.plot;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.att.aro.core.packetanalysis.IThroughputCalculator;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Throughput;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class ThroughputPlot implements IPlot{
	private IThroughputCalculator throughputHelper = ContextAware
			.getAROConfigContext().getBean(IThroughputCalculator.class);
	private static final String THROUGHPUT_TOOLTIP = ResourceBundleHelper
			.getMessageString("throughput.tooltip");

	public void populate(XYPlot plot, AROTraceData analysis) {
		XYSeries series = new XYSeries(0);
		if (analysis != null) {

			// Get packet iterators
			List<PacketInfo> packets = analysis.getAnalyzerResult()
					.getTraceresult().getAllpackets();
			final double maxTS = analysis.getAnalyzerResult().getTraceresult()
					.getTraceDuration();
		
			final List<String> tooltipList = new ArrayList<String>(1000);

			Double zeroTime = null;
			double lastTime = 0.0;
			double startTime = analysis.getAnalyzerResult().getFilter().getTimeRange().getBeginTime();
			for (Throughput t : throughputHelper.calculateThroughput(startTime,
					maxTS, analysis.getAnalyzerResult().getProfile()
							.getThroughputWindow(), packets)) {

				double time = t.getTime();
				double kbps = t.getKbps();
				if (kbps != 0.0) {
					if (zeroTime != null && zeroTime.doubleValue() != lastTime) {
						series.add(lastTime, 0.0);
						tooltipList.add(MessageFormat.format(
								THROUGHPUT_TOOLTIP, 0.0));
					}
					// Add slot to data set
					series.add(time, kbps);

					tooltipList.add(MessageFormat.format(THROUGHPUT_TOOLTIP,
							kbps));
					zeroTime = null;
				} else {
					if (zeroTime == null) {
						// Add slot to data set
						series.add(time, kbps);

						tooltipList.add(MessageFormat.format(
								THROUGHPUT_TOOLTIP, kbps));
						zeroTime = Double.valueOf(time);
					}
				}

				lastTime = time;
			}
			plot.getRenderer().setBaseToolTipGenerator(
					new XYToolTipGenerator() {

						@Override
						public String generateToolTip(XYDataset dataset,
								int series, int item) {

							// Tooltip displays throughput value
							return tooltipList.get(item);
						}

					});
		}

		plot.setDataset(new XYSeriesCollection(series));
//		return plot;
	}
}
