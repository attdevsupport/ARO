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
package com.att.aro.ui.view.statistics;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;
import com.att.aro.ui.commonui.TabPanelJPanel;


public class TCPSessionStatisticsPanel extends TabPanelJPanel {
	private enum LabelKeys {
		tcpstatistics_title,
		tcpstatistics_size,
		tcpstatistics_duration,
		tcpstatistics_packets,
		tcpstatistics_throughput
	}
	private static final long serialVersionUID = 1L;
	private JPanel dataPanel;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();

	/**
	 * Initializes a new instance of the TCPSessionStatisticsPanel class.
	 */
	public TCPSessionStatisticsPanel() {
		tabPanelCommon.initTabPanel(this);
		add(layoutDataPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel containing the Date , Trace and Application details
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {

		dataPanel = tabPanelCommon.initDataPanel(
				UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		Insets insets = new Insets(2, 2, 2, 2);
		Insets bottomBlankLineInsets = new Insets(2, 2, 8, 2);
		TabPanelCommonAttributes attributes = tabPanelCommon.addLabelLine(
			new TabPanelCommonAttributes.Builder()
				.enumKey(LabelKeys.tcpstatistics_title)
				.contentsWidth(1)
				.insets(insets)
				.insetsOverride(bottomBlankLineInsets)
				.header()
	.		build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.tcpstatistics_duration)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.tcpstatistics_size)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.tcpstatistics_packets)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.tcpstatistics_throughput)
			.build());

		return dataPanel;
	}

	@Override
	public void refresh(AROTraceData model) {
		String intFormatString = "%,2d";
		String doubleFormatString = "%,1.1f";
		Statistic statistic = model.getAnalyzerResult().getStatistic();
		if (statistic != null) {
			tabPanelCommon.setText(LabelKeys.tcpstatistics_size, String.format(intFormatString,
					statistic.getTotalByte()));
			tabPanelCommon.setText(LabelKeys.tcpstatistics_duration, String.format(
					doubleFormatString, statistic.getPacketDuration()));
			tabPanelCommon.setText(LabelKeys.tcpstatistics_packets, String.format(
					intFormatString, statistic.getTotalPackets()));
			tabPanelCommon.setText(LabelKeys.tcpstatistics_throughput, String.format(
					doubleFormatString, statistic.getAverageKbps()));
		}
	}
}
