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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.packetanalysis.pojo.RRCState;
import com.att.aro.core.packetanalysis.pojo.RrcStateRange;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class RrcPlot implements IPlot{
	XYIntervalSeriesCollection rrcDataCollection = new XYIntervalSeriesCollection();
	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		if (analysis != null) {
			rrcDataCollection.removeAllSeries();
			Map<RRCState, XYIntervalSeries> seriesMap = new EnumMap<RRCState, XYIntervalSeries>(
					RRCState.class);
			for (RRCState eventType : RRCState.values()) {
				XYIntervalSeries series = new XYIntervalSeries(eventType);
				seriesMap.put(eventType, series);
				rrcDataCollection.addSeries(series);
			}
			List<RrcStateRange> rrcStates = analysis.getAnalyzerResult().getStatemachine().getStaterangelist();
			
			Iterator<RrcStateRange> iter = rrcStates.iterator();
			while (iter.hasNext()) {
				RrcStateRange currEvent = iter.next();
				RRCState state = currEvent.getState();
				if (state == RRCState.STATE_FACH || state == RRCState.TAIL_FACH) {
					seriesMap.get(state).add(currEvent.getBeginTime(), currEvent.getBeginTime(),
							currEvent.getEndTime(), 0.25, 0, 0.5);
				} else {
					seriesMap.get(state).add(currEvent.getBeginTime(), currEvent.getBeginTime(),
							currEvent.getEndTime(), 0.5, 0, 1);
				}

			}
			XYItemRenderer renderer = plot.getRenderer();
			Color dchGreen = new Color(34, 177, 76);
			Color fachOrange = new Color(255, 201, 14);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.STATE_IDLE), Color.white);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_IDLE), Color.white);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.PROMO_IDLE_DCH), Color.red);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_PROMOTION), Color.red);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.STATE_DCH), fachOrange);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_CONTINUOUS), fachOrange);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.TAIL_DCH),
					getTailPaint(fachOrange));
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_CR_TAIL),
					getTailPaint(fachOrange));
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_DRX_SHORT),
					getTailPaint(fachOrange));
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.LTE_DRX_LONG),
					getTailPaint(fachOrange));

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.STATE_FACH), dchGreen);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.TAIL_FACH),
					getTailPaint(dchGreen));

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.PROMO_FACH_DCH), Color.red);

			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.WIFI_IDLE), Color.white);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.WIFI_ACTIVE), fachOrange);
			renderer.setSeriesPaint(rrcDataCollection.indexOf(RRCState.WIFI_TAIL),
					getTailPaint(fachOrange));

			// Assign ToolTip to renderer
			
			final Profile profile = analysis.getAnalyzerResult().getProfile();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					RRCState eventType = (RRCState) rrcDataCollection.getSeries(series).getKey();
					final String PREFIX = "RRCTooltip.";
					if (eventType == RRCState.LTE_IDLE&&profile instanceof ProfileLTE) {
						return MessageFormat.format(ResourceBundleHelper.getMessageString(PREFIX + eventType),
								((ProfileLTE) profile).getIdlePingPeriod());
					}
					return ResourceBundleHelper.getMessageString(PREFIX + eventType);
				}
			});

		}

		plot.setDataset(rrcDataCollection);
	}
	/**
	 * Creating DchTail and FachTail Cross Hatch
	 * 
	 * @return Paint The Tail state paint object
	 */
	private static Paint getTailPaint(Color color) {

		BufferedImage bufferedImage = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, 5, 5);
		g2.setColor(color);
		g2.drawLine(0, 0, 5, 5);
		g2.drawLine(5, 5, 0, 0);
		g2.drawLine(0, 5, 5, 0);
		Rectangle2D rect = new Rectangle2D.Double(0, 0, 5, 5);
		return new TexturePaint(bufferedImage, rect);
	}

}
