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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.TraceData;

/**
 * Represents the Basic Statistics panel which displays basic statistics about
 * the trace.
 */
public class BasicStatisticsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD,
			16);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
	private static final int HEADER_DATA_SPACING = 10;

	private JLabel sizeLabel;
	private JLabel sizeValueLabel;
	private JLabel durationLabel;
	private JLabel durationValueLabel;
	private JLabel packetsLabel;
	private JLabel packetsValueLabel;
	private JLabel throughputLabel;
	private JLabel throughputValueLabel;

	Map<String, String> basicContent = new LinkedHashMap<String, String>();

	/**
	 * Initializes a new instance of the BasicStatisticsPanel class.
	 */
	public BasicStatisticsPanel() {
		super(new BorderLayout(10, 10));
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(createBasicStatisticsPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel that contains the Basic statistics data for the trace.
	 * 
	 * @return tcpStatisticsLeftAlligmentPanel The Basic statistics JPanel.
	 */
	private JPanel createBasicStatisticsPanel() {

		JPanel tcpStatisticsLeftAlligmentPanel = new JPanel(new BorderLayout());
		tcpStatisticsLeftAlligmentPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		JPanel tcpStatisticsPanel = new JPanel();
		tcpStatisticsPanel.setLayout(new VerticalLayout());
		tcpStatisticsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		JLabel sessStatisticsHeaderLabel = new JLabel(
				rb.getString("basic.title"));
		sessStatisticsHeaderLabel.setFont(HEADER_FONT);
		sizeLabel = new JLabel(rb.getString("basic.size"));
		sizeLabel.setFont(TEXT_FONT);
		sizeValueLabel = new JLabel();
		sizeValueLabel.setFont(TEXT_FONT);
		durationLabel = new JLabel(rb.getString("basic.duration"));
		durationLabel.setFont(TEXT_FONT);
		durationValueLabel = new JLabel();
		durationValueLabel.setFont(TEXT_FONT);
		packetsLabel = new JLabel(rb.getString("basic.packets"));
		packetsLabel.setFont(TEXT_FONT);
		packetsValueLabel = new JLabel();
		packetsValueLabel.setFont(TEXT_FONT);
		throughputLabel = new JLabel(rb.getString("basic.throughput"));
		throughputLabel.setFont(TEXT_FONT);
		throughputValueLabel = new JLabel();
		throughputValueLabel.setFont(TEXT_FONT);

		tcpStatisticsPanel.add(sessStatisticsHeaderLabel);

		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(),
				HEADER_DATA_SPACING));
		spacePanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		tcpStatisticsPanel.add(spacePanel);

		JPanel tcpStatisticsDataPanel = new JPanel(new GridLayout(4, 2, 5, 5));
		tcpStatisticsDataPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		tcpStatisticsDataPanel.add(durationLabel);
		tcpStatisticsDataPanel.add(durationValueLabel);
		tcpStatisticsDataPanel.add(sizeLabel);
		tcpStatisticsDataPanel.add(sizeValueLabel);
		tcpStatisticsDataPanel.add(packetsLabel);
		tcpStatisticsDataPanel.add(packetsValueLabel);
		tcpStatisticsDataPanel.add(throughputLabel);
		tcpStatisticsDataPanel.add(throughputValueLabel);

		tcpStatisticsPanel.add(tcpStatisticsDataPanel);

		tcpStatisticsLeftAlligmentPanel.add(tcpStatisticsPanel,
				BorderLayout.WEST);

		return tcpStatisticsLeftAlligmentPanel;
	}

	/**
	 * Refreshes the content of the Basic Statistics Panel with the specified
	 * trace data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysis) {

		if (analysis != null) {
			NumberFormat nf = NumberFormat.getNumberInstance();
			nf.setMaximumFractionDigits(1);
			nf.setMinimumFractionDigits(1);
			NumberFormat intf = NumberFormat.getIntegerInstance();
			sizeValueLabel.setText(intf.format(analysis.getTotalBytes()));
			durationValueLabel
					.setText(nf.format(analysis.getPacketsDuration()));
			packetsValueLabel
					.setText(intf.format(analysis.getPackets().size()));
			throughputValueLabel.setText(nf.format(analysis.getAvgKbps()));
			basicContent.put(rb.getString("basic.size"),
					sizeValueLabel.getText());
			basicContent.put(rb.getString("basic.duration"),
					durationValueLabel.getText());
			basicContent.put(rb.getString("basic.packets"),
					packetsValueLabel.getText());
			basicContent.put(rb.getString("basic.throughput"),
					throughputValueLabel.getText());
		} else {
			sizeValueLabel.setText(null);
			durationValueLabel.setText(null);
			packetsValueLabel.setText(null);
			throughputValueLabel.setText(null);
			basicContent.clear();
		}
	}

	/**
	 * Returns a Map object that contains basic statistics data.
	 * 
	 * @return A Map object containing basic statistics data.
	 */
	public Map<String, String> getBasicContent() {
		return basicContent;
	}

	/**
	 * Returns size JLabel object.
	 * 
	 * @return size label.
	 */
	JLabel getSizeLabel() {
		return sizeLabel;
	}

	/**
	 * Returns size value JLabel object.
	 * 
	 * @return size value label.
	 */
	JLabel getSizeValueLabel() {
		return sizeValueLabel;
	}

	/**
	 * Returns time duration JLabel object.
	 * 
	 * @return time duration label.
	 */
	JLabel getDurationLabel() {
		return durationLabel;
	}

	/**
	 * Returns time duration value JLabel object.
	 * 
	 * @return time duration value label.
	 */
	JLabel getDurationValueLabel() {
		return durationValueLabel;
	}

	/**
	 * Returns packet count JLabel object.
	 * 
	 * @return packet count label.
	 */
	JLabel getPacketsLabel() {
		return packetsLabel;
	}

	/**
	 * Returns packet count value JLabel object.
	 * 
	 * @return packet count value label.
	 */
	JLabel getPacketsValueLabel() {
		return packetsValueLabel;
	}

	/**
	 * Returns throughput JLabel object.
	 * 
	 * @return throughput label.
	 */
	JLabel getThroughputLabel() {
		return throughputLabel;
	}

	/**
	 * Returns throughput value JLabel object.
	 * 
	 * @return throughput value label.
	 */
	JLabel getThroughputValueLabel() {
		return throughputValueLabel;
	}
}
