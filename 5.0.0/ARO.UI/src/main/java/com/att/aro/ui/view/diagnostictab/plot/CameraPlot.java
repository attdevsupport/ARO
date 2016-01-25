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
import java.util.Iterator;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.peripheral.pojo.CameraInfo;
import com.att.aro.core.peripheral.pojo.CameraInfo.CameraState;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class CameraPlot implements IPlot{

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		XYIntervalSeriesCollection cameraData = new XYIntervalSeriesCollection();
		
		if (analysis != null) {

			XYIntervalSeries series = new XYIntervalSeries(CameraState.CAMERA_ON);
			cameraData.addSeries(series);

			// Populate the data set
			Iterator<CameraInfo> iter = analysis.getAnalyzerResult().getTraceresult().getCameraInfos().iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					CameraInfo cameraEvent = iter.next();
					if (cameraEvent.getCameraState() == CameraState.CAMERA_ON) {
						series.add(cameraEvent.getBeginTimeStamp(),
								cameraEvent.getBeginTimeStamp(), cameraEvent.getEndTimeStamp(),
								0.5, 0, 1);
					}
				}
			}

			// Assign ToolTip to renderer
			XYItemRenderer renderer = plot.getRenderer();
			renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
				@Override
				public String generateToolTip(XYDataset dataset, int series, int item) {
					return MessageFormat.format(
							ResourceBundleHelper.getMessageString("camera.tooltip"), dataset.getX(
							series, item), ResourceBundleHelper.getEnumString((Enum<?>) dataset
							.getSeriesKey(series)));
				}
			});

		}

		plot.setDataset(cameraData);
	}

}
