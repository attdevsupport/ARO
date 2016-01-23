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
import java.util.Iterator;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.peripheral.pojo.BluetoothInfo;
import com.att.aro.core.peripheral.pojo.BluetoothInfo.BluetoothState;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class BluetoothPlot implements IPlot{
	private XYIntervalSeriesCollection bluetoothData = new XYIntervalSeriesCollection();

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
			if (analysis != null) {
				bluetoothData.removeAllSeries();
				XYIntervalSeries bluetoothConnected = new XYIntervalSeries(
						BluetoothState.BLUETOOTH_CONNECTED);
				XYIntervalSeries bluetoothDisconnected = new XYIntervalSeries(
						BluetoothState.BLUETOOTH_DISCONNECTED);
				XYIntervalSeries bluetoothOff = new XYIntervalSeries(
						BluetoothState.BLUETOOTH_TURNED_OFF);

				bluetoothData.addSeries(bluetoothConnected);
				bluetoothData.addSeries(bluetoothDisconnected);
				// bluetoothStateCollection.addSeries(bluetoothOff);

				// Populate the data set
				Iterator<BluetoothInfo> iter = analysis.getAnalyzerResult().getTraceresult().getBluetoothInfos().iterator();
				XYIntervalSeries series;
				if (iter.hasNext()) {
					while (iter.hasNext()) {
						BluetoothInfo btEvent = iter.next();
						switch (btEvent.getBluetoothState()) {
						case BLUETOOTH_CONNECTED:
							series = bluetoothConnected;
							break;
						case BLUETOOTH_DISCONNECTED:
							series = bluetoothDisconnected;
							break;
						default:
							series = bluetoothOff;
							break;
						}
						series.add(btEvent.getBeginTimeStamp(), btEvent.getBeginTimeStamp(),
								btEvent.getEndTimeStamp(), 0.5, 0, 1);
					}

				}

				XYItemRenderer renderer = plot.getRenderer();
				renderer.setSeriesPaint(bluetoothData.indexOf(BluetoothState.BLUETOOTH_CONNECTED),
						new Color(34, 177, 76));
				renderer.setSeriesPaint(bluetoothData.indexOf(BluetoothState.BLUETOOTH_DISCONNECTED),
						Color.YELLOW);

				// Assign ToolTip to renderer
				renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset dataset, int series, int item) {
						BluetoothState eventType = (BluetoothState) bluetoothData.getSeries(series)
								.getKey();
						return MessageFormat.format(ResourceBundleHelper.getMessageString("bluetooth.tooltip"),
								dataset.getX(series, item),
								ResourceBundleHelper.getEnumString(eventType));
					}
				});

			}
			plot.setDataset(bluetoothData);		
	}

}
