/*
 * Copyright 2012 AT&T
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

package com.att.aro.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * The ChartPlotOptions enumeration defines constant values that specify the
 * items of diagnostic information that can be plotted on the Diagnostics Chart.
 * The user selects which of these items to plot on the Diagnostics Chart by
 * marking individual check boxes in the View Options dialog box.
 */
public enum ChartPlotOptions {
	
	/**
	 *  The CPU usage plot option.
	 */
	CPU,
	/**
	 * The Burst Colors chart plot option.
	 */
	BURST_COLORS,
	/**
	 * The Battery States chart plot option.
	 */
	BATTERY,
	/**
	 * The Screen State chart plot option.
	 */
	SCREEN,
	/**
	 * The Radio State chart plot option.
	 */
	RADIO,
	/**
	 * The Bluetooth State chart plot option.
	 */
	BLUETOOTH,
	/**
	 * The Camera State chart plot option.
	 */
	CAMERA,
	/**
	 * The GPS State chart plot option.
	 */
	GPS,
	/**
	 * The WiFi State chart plot option.
	 */
	WIFI,
	/**
	 * The Uplink Packets chart plot option.
	 */
	UL_PACKETS,
	/**
	 * The Downlink Packets chart plot option.
	 */
	DL_PACKETS,
	/**
	 * The Bursts chart plot option.
	 */
	BURSTS,
	/**
	 * The User Inputs chart plot option.
	 */
	USER_INPUT,
	/**
	 * The RRC States chart plot option.
	 */
	RRC,
	/**
	 * The TCP Throughput chart plot option.
	 */
	THROUGHPUT,
	/**
	 * 
	 */
	NETWORK_TYPE,
	/**
	 * The Default View checkbox option in the View Options dialog box.
	 */
	DEFAULT_VIEW;

	private static final String DELIM = ";";
	
	private static final Logger logger = Logger.getLogger(ChartPlotOptions.class.getName());

	/**
	 * Returns a String containing the chart plot options selected by the user
	 * in the View Options dialog box.
	 * 
	 * @param list
	 *            - A List of ChartPlotOptions enumeration values that specify
	 *            the user selected chart plot options.
	 * 
	 * @return A string containing the user selected chart plot options.
	 * 
	 * @see ChartPlotOptions
	 */
	public static String toUserPrefsListString(List<ChartPlotOptions> list) {
		StringBuilder sb = new StringBuilder();
		for (ChartPlotOptions cpo : list) {
			sb.append(cpo.name()).append(DELIM);
		}
		return sb.toString();
	}

	/**
	 * Returns the list of chart plot options selected by the user in the View
	 * Options dialog box. These options specify which items are plotted on the
	 * Diagnostics Chart.
	 * 
	 * @param delimitedPrefsString
	 *            - The delimeter string that is used to separate the returned
	 *            list of chart plot options.
	 * 
	 * @return A List of ChartPlotOptions enumeration values that specify the
	 *         user selected chart plot options.
	 */
	public static List<ChartPlotOptions> toUserPrefsList(
			String delimitedPrefsString) {
		List<ChartPlotOptions> list = new ArrayList<ChartPlotOptions>();

		if (delimitedPrefsString == null) {
			return null;
		}

		String[] tokens = delimitedPrefsString.split(DELIM);
		for (String s : tokens) {
			if (s != null && !"".equals(s.trim())) {
				try {
					ChartPlotOptions cpo = ChartPlotOptions.valueOf(s);
					if (cpo != null) {
						list.add(cpo);
					}
				} catch (IllegalArgumentException e) {
					logger.warning("Unrecognized chart plot option in preferences: " + s);
				}
			}
		}
		return list;
	}

	/**
	 * Returns the default list of chart plot options. These are the options
	 * that are selected when the user marks the Default View checkbox in the
	 * View Options dialog box.
	 * 
	 * @return A List of ChartPlotOptions enumeration values that specify the
	 *         default chart plot options.
	 */
	public static List<ChartPlotOptions> getDefaultList() {
		List<ChartPlotOptions> defaultChartPlotList = new ArrayList<ChartPlotOptions>();
		defaultChartPlotList.add(BURST_COLORS);
		defaultChartPlotList.add(UL_PACKETS);
		defaultChartPlotList.add(DL_PACKETS);
		defaultChartPlotList.add(BURSTS);
		defaultChartPlotList.add(USER_INPUT);
		defaultChartPlotList.add(RRC);
		defaultChartPlotList.add(THROUGHPUT);
		return Collections.unmodifiableList(defaultChartPlotList);
	}
}
