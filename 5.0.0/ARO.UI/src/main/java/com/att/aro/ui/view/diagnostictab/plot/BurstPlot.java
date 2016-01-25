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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class BurstPlot implements IPlot{
	XYIntervalSeriesCollection burstDataCollection = new XYIntervalSeriesCollection();
	public void populate(XYPlot plot, AROTraceData analysis){

		if (analysis != null) {
			burstDataCollection.removeAllSeries();
			Map<BurstCategory, XYIntervalSeries> seriesMap = new EnumMap<BurstCategory, XYIntervalSeries>(
					BurstCategory.class);
			final Map<BurstCategory, List<Burst>> burstMap = new HashMap<BurstCategory, List<Burst>>();
			for (BurstCategory eventType : BurstCategory.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				burstDataCollection.addSeries(series);
				burstMap.put(eventType, new ArrayList<Burst>());
			}
			final List<Burst> burstStates = analysis.getAnalyzerResult().getBurstcollectionAnalysisData().getBurstCollection();
			Iterator<Burst> iter = burstStates.iterator();
			while (iter.hasNext()) {
				Burst currEvent = iter.next();
				if (currEvent != null) {
					BurstCategory burstState = currEvent.getBurstCategory(); 
					if (burstState != null) {
						seriesMap.get(burstState).add(currEvent.getBeginTime(),
								currEvent.getBeginTime(), currEvent.getEndTime(), 0.5, 0, 1);
						burstMap.get(burstState).add(currEvent);
					}
				}
			}

			Color myGreen = new Color(34, 177, 76);
			Color lightGreen = new Color(134, 232, 162);

			XYItemRenderer renderer = plot.getRenderer();
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.TCP_PROTOCOL), Color.blue);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.TCP_LOSS_OR_DUP), Color.black);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.USER_INPUT), myGreen);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.SCREEN_ROTATION), lightGreen);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.CLIENT_APP), Color.red);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.SERVER_NET_DELAY), Color.yellow);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.LONG), Color.gray);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.PERIODICAL), Color.magenta);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.CPU), Color.cyan);
			renderer.setSeriesPaint(burstDataCollection.indexOf(BurstCategory.UNKNOWN), Color.darkGray);

			// Assign ToolTip to renderer
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					BurstCategory eventType = (BurstCategory) burstDataCollection.getSeries(series)
							.getKey();
					Burst b = burstMap.get(eventType).get(item);
					final String PREFIX = "BurstCategory.";
					return MessageFormat.format(ResourceBundleHelper.getMessageString(PREFIX + eventType.ordinal()),
							b.getPackets().size(), b.getBurstBytes(), b.getBurstThroughPut());
				}
			});

		}

		plot.setDataset(burstDataCollection);
				
	}
}
