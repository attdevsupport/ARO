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
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.ui.tabbedui.VerticalLayout;

/**
 * Represents a progress dialog that is displayed when the ARO 
 * Data Collector is starting, stopping, or pulling a trace file.
 */
public class AROEmulatorTraceSummary extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
	private static final int HEADER_DATA_SPACING = 10;

	private JLabel summaryLabel;
	private JLabel pathLabel;
	private JLabel pathValueLabel;
	private JLabel dataLabel;
	private JLabel dataValueLabel;
	private JLabel videoLabel;
	private JLabel videoValueLabel;
	private JLabel durationLabel;
	private JLabel durationValueLabel;

	/**
	 * Initializes a new instance of the AROEmulatorTraceSummary class, using 
	 * the specified trace directory path, video status string, and the trace duration.
	 * 
	 * @param path
	 *            The path to the trace directory. 
	 * @param videoStatus
	 *            A string indicating the video status.
	 * @param traceDuration
	 *            The duration of the trace.
	 */
	public AROEmulatorTraceSummary(String path, String videoStatus, String traceDuration) {
		super(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(createSummaryPanel(path, videoStatus, traceDuration), BorderLayout.WEST);
	}

	private JPanel createSummaryPanel(String path, String videoStatus, String traceDuration) {
		JPanel summaryAlligmentPanel = new JPanel(new BorderLayout());

		JPanel emulatorSummaryDataPanel = new JPanel();
		emulatorSummaryDataPanel.setLayout(new VerticalLayout());

		pathLabel = new JLabel(rb.getString("Emulator.path"));
		pathLabel.setFont(TEXT_FONT);
		pathValueLabel = new JLabel(path);
		pathValueLabel.setFont(TEXT_FONT);
		dataLabel = new JLabel(rb.getString("Emulator.data"));
		dataLabel.setFont(TEXT_FONT);
		dataValueLabel = new JLabel(rb.getString("Emulator.dataValue"));
		dataValueLabel.setFont(TEXT_FONT);
		videoLabel = new JLabel(rb.getString("Emulator.video"));
		videoLabel.setFont(TEXT_FONT);
		videoValueLabel = new JLabel(videoStatus);
		videoValueLabel.setFont(TEXT_FONT);
		durationLabel = new JLabel(rb.getString("Emulator.duration"));
		durationLabel.setFont(TEXT_FONT);
		durationValueLabel = new JLabel(traceDuration);
		durationValueLabel.setFont(TEXT_FONT);

		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(), HEADER_DATA_SPACING));

		JPanel summaryDataPanel = new JPanel(new GridLayout(4, 2, 0, 5));
		summaryDataPanel.add(pathLabel);
		summaryDataPanel.add(pathValueLabel);
		summaryDataPanel.add(dataLabel);
		summaryDataPanel.add(dataValueLabel);
		summaryDataPanel.add(videoLabel);
		summaryDataPanel.add(videoValueLabel);
		summaryDataPanel.add(durationLabel);
		summaryDataPanel.add(durationValueLabel);

		JPanel summaryTitlePanel = new JPanel(new BorderLayout());
		summaryLabel = new JLabel(rb.getString("Emulator.summary"));
		summaryLabel.setFont(TEXT_FONT);
		summaryTitlePanel.add(summaryLabel, BorderLayout.CENTER);

		emulatorSummaryDataPanel.add(summaryTitlePanel);
		emulatorSummaryDataPanel.add(spacePanel);
		emulatorSummaryDataPanel.add(summaryDataPanel);

		summaryAlligmentPanel.add(emulatorSummaryDataPanel, BorderLayout.SOUTH);

		return summaryAlligmentPanel;
	}

}
