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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.ApplicationSelection;
import com.att.aro.model.TraceData;

/**
 * Represents an application detail panel in the Best Practices tab that is used
 * to displays trace data, and trace application information.
 */
public class DateTraceAppDetailPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JPanel dataPanel;
	private JLabel dateValueLabel;
	private JLabel traceValueLabel;
	private JTextArea applicationNameLabel;
	private JLabel appVersionValueLabel;
	private JLabel deviceModelValueLabel;
	private JLabel networkTypeValueLabel;
	private JLabel profileValueLabel;
	private static final Font LABEL_FONT = new Font("TEXT_FONT", Font.BOLD, 12);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);

	/**
	 * Initializes a new instance of the DateTraceAppDetailPanel class.
	 */
	public DateTraceAppDetailPanel() {
		this.setLayout(new BorderLayout(10, 10));
		this.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		dateValueLabel = new JLabel();
		dateValueLabel.setFont(TEXT_FONT);
		traceValueLabel = new JLabel();
		traceValueLabel.setFont(TEXT_FONT);
		applicationNameLabel = new JTextArea();
		applicationNameLabel.setEditable(false);
		applicationNameLabel.setFont(traceValueLabel.getFont());
		applicationNameLabel.setLineWrap(true);
		applicationNameLabel.setWrapStyleWord(true);
		applicationNameLabel.setMargin(new Insets(0, 0, 0, 0));
		appVersionValueLabel = new JLabel();
		appVersionValueLabel.setFont(TEXT_FONT);
		deviceModelValueLabel = new JLabel();
		deviceModelValueLabel.setFont(TEXT_FONT);
		networkTypeValueLabel = new JLabel();
		networkTypeValueLabel.setFont(TEXT_FONT);
		profileValueLabel = new JLabel();
		profileValueLabel.setFont(TEXT_FONT);
		add(getDataPanel(), BorderLayout.CENTER);
	}

	/**
	 * Refreshes the content of the DateTraceAppDetailPanel with the specified
	 * trace data.
	 * 
	 * @param analysis
	 *            – An Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		if (analysisData != null) {
			TraceData traceData = analysisData.getTraceData();
			DateFormat format = DateFormat.getDateTimeInstance();
			dateValueLabel.setText(format.format(traceData.getTraceDateTime()));
			traceValueLabel.setText(traceData.getTraceDir().getName());
			StringBuffer apps = new StringBuffer();
			for (ApplicationSelection app : analysisData
					.getApplicationSelections().values()) {
				if (app.isSelected() && app.getAppName() != null) {
					final String appVersion = analysisData.getTraceData()
							.getAppVersionMap().get(app.getAppName());
					apps.append(app.getAppName()
							+ (appVersion != null ? " : " + appVersion : ""));
					apps.append('\n');
				}
			}
			applicationNameLabel.setText(apps.toString());
			appVersionValueLabel.setText(traceData.getCollectorVersion());
			if (traceData.getDeviceMake() != null
					|| traceData.getDeviceModel() != null) {
				deviceModelValueLabel.setText(MessageFormat.format(
						rb.getString("bestPractices.devicemodelvalue"),
						traceData.getDeviceMake(), traceData.getDeviceModel()));
			} else {
				deviceModelValueLabel.setText(null);
			}
			networkTypeValueLabel.setText(ResourceBundleManager
					.getEnumString(traceData.getNetworkType()));
			profileValueLabel.setText(analysisData.getProfile().getName());
		} else {
			dateValueLabel.setText(null);
			traceValueLabel.setText(null);
			applicationNameLabel.setText(null);
			appVersionValueLabel.setText(null);
			deviceModelValueLabel.setText(null);
			networkTypeValueLabel.setText(null);
			profileValueLabel.setText(null);
		}
	}

	/**
	 * Returns the date value label.
	 * 
	 * @return A JLabel object containing the date value label.
	 */
	public JLabel getDateValueLabel() {
		return dateValueLabel;
	}

	/**
	 * Returns the trace value label.
	 * 
	 * @return A JLabel object containing the trace value label.
	 */
	public JLabel getTraceValueLabel() {
		return traceValueLabel;
	}

	/**
	 * Returns the application name label.
	 * 
	 * @return A JTextArea object containing the application name label.
	 */
	public JTextArea getApplicationNameLabel() {
		return applicationNameLabel;
	}

	/**
	 * Creates the JPanel containing the Date , Trace and Application details
	 * 
	 * @return the dataPanel
	 */
	private JPanel getDataPanel() {
		if (dataPanel == null) {
			dataPanel = new JPanel(new GridBagLayout());
			dataPanel.setBackground(UIManager
					.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

			Insets insets = new Insets(2, 2, 2, 2);
			JLabel label;
			label = new JLabel(rb.getString("bestPractices.date"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE, insets,
					0, 0));
			dataPanel.add(dateValueLabel, new GridBagConstraints(1, 0, 1, 1,
					0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));
			label = new JLabel(rb.getString("bestPractices.sideTitle"),
					JLabel.RIGHT);
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.NORTHEAST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));
			label = new JLabel(rb.getString("bestPractices.trace"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
					insets, 0, 0));
			dataPanel.add(traceValueLabel, new GridBagConstraints(1, 1, 1, 1,
					0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));
			label = new JLabel(rb.getString("bestPractices.application"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
					insets, 0, 0));
			JScrollPane scroll = new JScrollPane(applicationNameLabel);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			dataPanel.add(scroll, new GridBagConstraints(1, 2, 1, 1, 0.5, 0.0,
					GridBagConstraints.NORTHWEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));

			label = new JLabel(rb.getString("bestPractices.applicationversion"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE, insets,
					0, 0));
			dataPanel.add(appVersionValueLabel, new GridBagConstraints(1, 3, 1,
					1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));

			label = new JLabel(rb.getString("bestPractices.devicemodel"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE, insets,
					0, 0));
			dataPanel.add(deviceModelValueLabel, new GridBagConstraints(1, 4,
					1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));

			label = new JLabel(rb.getString("bestPractices.networktype"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE, insets,
					0, 0));
			dataPanel.add(networkTypeValueLabel, new GridBagConstraints(1, 5,
					1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));

			label = new JLabel(rb.getString("bestPractices.profile"));
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE, insets,
					0, 0));
			dataPanel.add(profileValueLabel, new GridBagConstraints(1, 6, 1, 1,
					0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));
		}
		return dataPanel;
	}

}
