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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.TraceData;
import com.att.aro.util.Util;

/**
 * Represents an application detail panel in the Best Practices tab that is used
 * to displays trace data, and trace application information.
 */
public class DateTraceAppDetailPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JPanel dataPanel;
	private JLabel dateValueLabel;
	private JLabel traceValueLabel;
	private JLabel applicationNameLabel;
	private JLabel appVersionValueLabel;
	private JLabel deviceModelValueLabel;
	private JLabel osVersionLabel;
	private JLabel networkTypeValueLabel;
	private JLabel profileValueLabel;
	private static final Font LABEL_FONT = new Font("TEXT_FONT", Font.BOLD, 12);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);

	/**
	 * Initializes a new instance of the DateTraceAppDetailPanel class.
	 */
	public DateTraceAppDetailPanel() {
		
		final int borderGap = 10;
		
		this.setLayout(new BorderLayout(borderGap, borderGap));
		this.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, borderGap, 0));

		dateValueLabel = new JLabel();
		dateValueLabel.setFont(TEXT_FONT);
		
		traceValueLabel = new JLabel();
		traceValueLabel.setFont(TEXT_FONT);
		
		applicationNameLabel = new JLabel();
		applicationNameLabel.setFont(TEXT_FONT);
		
		appVersionValueLabel = new JLabel();
		appVersionValueLabel.setFont(TEXT_FONT);

		deviceModelValueLabel = new JLabel();
		deviceModelValueLabel.setFont(TEXT_FONT);

		osVersionLabel = new JLabel();
		osVersionLabel.setFont(TEXT_FONT);
		
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
	 * @param analysisData
	 *            An Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		if (analysisData != null) {
			TraceData traceData = analysisData.getTraceData();
			DateFormat format = DateFormat.getDateTimeInstance();
			dateValueLabel.setText(format.format(traceData.getTraceDateTime()));
			traceValueLabel.setText(traceData.getTraceDir().getName());
			StringBuffer apps = new StringBuffer("<html><body>");
			String appName;
			for (String app : analysisData.getAppNames()) {
				String appVersion = analysisData.getTraceData().getAppVersionMap().get(app);
				appName = Util.getDefaultAppName(app);
				apps.append(appName);
				apps.append(appVersion != null ? " : " + appVersion : "");
				apps.append("<br/>");
			}
			applicationNameLabel.setText(apps.toString());
			appVersionValueLabel.setText(traceData.getCollectorVersion());
			if (traceData.getDeviceMake() != null || traceData.getDeviceModel() != null) {
				deviceModelValueLabel.setText(MessageFormat.format(
						Util.RB.getString("bestPractices.devicemodelvalue"), traceData.getDeviceMake(),
						traceData.getDeviceModel()));
			} else {
				deviceModelValueLabel.setText(null);
			}

			osVersionLabel.setText(traceData.getOsVersion());
			
			if (analysisData.getNetworTypeInfos().size() > 1) {

				networkTypeValueLabel.setText(traceData.getNetworkTypesList());

			} else {
				networkTypeValueLabel.setText(ResourceBundleManager.getEnumString((traceData.getNetworkType())));
			}
			profileValueLabel.setText(analysisData.getProfile().getName());
		} else {
			dateValueLabel.setText(null);
			traceValueLabel.setText(null);
			applicationNameLabel.setText(null);
			appVersionValueLabel.setText(null);
			deviceModelValueLabel.setText(null);
			osVersionLabel.setText(null);
			networkTypeValueLabel.setText(null);
			profileValueLabel.setText(null);
		}
	}

	/**
	 * Returns the date value label.
	 * 
	 * @return A JLabel object containing the date value label.
	 */
	JLabel getDateValueLabel() {
		return dateValueLabel;
	}

	/**
	 * Returns the trace value label.
	 * 
	 * @return A JLabel object containing the trace value label.
	 */
	JLabel getTraceValueLabel() {
		return traceValueLabel;
	}

	/**
	 * Returns the application name label.
	 * 
	 * @return A JTextArea object containing the application name label.
	 */
	JLabel getApplicationNameLabel() {
		return applicationNameLabel;
	}

	/**
	 * Creates the JPanel containing the Date , Trace and Application details
	 * 
	 * @return the dataPanel
	 */
	private JPanel getDataPanel() {
		
		final double weightX = 0.5;
		
		if (dataPanel == null) {
			dataPanel = new JPanel(new GridBagLayout());
			dataPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

			Insets insets = new Insets(2, 2, 2, 2);
			int idx = 0;
			addLabelLine(dateValueLabel, "bestPractices.date", idx, 1, 0.0, insets);
			addLabelLine(traceValueLabel, "bestPractices.trace", ++idx, 2, 0.0, insets);
			addLabelLine(applicationNameLabel, "bestPractices.application", ++idx, 2, weightX, insets);
			addLabelLine(appVersionValueLabel, "bestPractices.applicationversion", ++idx, 2, 0.0, insets);
			addLabelLine(deviceModelValueLabel, "bestPractices.devicemodel", ++idx, 2, 0.0, insets);
			addLabelLine(osVersionLabel, "bestPractices.os.version", ++idx, 2, 0.0, insets);
			addLabelLine(networkTypeValueLabel, "bestPractices.networktype", ++idx, 2, 0.0, insets);
			addLabelLine(profileValueLabel, "bestPractices.profile", ++idx, 2, 0.0, insets);

			JLabel label;
			label = new JLabel(Util.RB.getString("bestPractices.sideTitle"), JLabel.RIGHT);
			label.setFont(LABEL_FONT);
			dataPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		}
		return dataPanel;
	}
	
	private void addLabelLine(JLabel infoLabel, String labelText, int gridy, int width, double weightx, Insets insets){
		
		JLabel label;
		label = new JLabel(Util.RB.getString(labelText));
		label.setFont(LABEL_FONT);
		dataPanel.add(label, new GridBagConstraints(0, gridy, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		dataPanel.add(infoLabel, new GridBagConstraints(1, gridy, width, 1, weightx, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		
	}

	/**
	 * Method to add the Trace information content in the csv file.
	 * 
	 * @throws IOException
	 */
	public FileWriter addTraceDateContent(FileWriter writer, TraceData.Analysis analysisData)
			throws IOException {
		final String lineSep = System.getProperty(Util.RB.getString("statics.csvLine.seperator"));

		addKeyValue(writer, Util.RB.getString("bestPractices.date"), dateValueLabel.getText()
				.replace(Util.RB.getString("statics.csvCell.seperator"), ""));
		writer.append(lineSep);

		addKeyValue(writer, Util.RB.getString("bestPractices.trace"), traceValueLabel.getText());
		writer.append(lineSep);

		writer.append(Util.RB.getString("bestPractices.application"));
		writer.append(Util.RB.getString("statics.csvCell.seperator"));
		StringBuffer apps = new StringBuffer();
		String appName;
		for (String app : analysisData.getAppNames()) {
			String appVersion = analysisData.getTraceData().getAppVersionMap().get(app);
			appName = Util.getDefaultAppName(app);
			apps.append(appName);
			apps.append(appVersion != null ? " , " + appVersion : "");
			apps.append('\n');
			apps.append(',');
		}
		writer.append(apps);
		writer.append(lineSep);

		addKeyValue(writer, Util.RB.getString("bestPractices.applicationversion"),
				appVersionValueLabel.getText());
		writer.append(lineSep);

		addKeyValue(writer, Util.RB.getString("bestPractices.devicemodel"),
				deviceModelValueLabel.getText());
		writer.append(lineSep);
		
		addKeyValue(writer, Util.RB.getString("bestPractices.os.version"),
				osVersionLabel.getText());
		writer.append(lineSep);
		
		addKeyValue(writer, Util.RB.getString("bestPractices.networktype"),
				networkTypeValueLabel.getText());
		writer.append(lineSep);

		addKeyValue(writer, Util.RB.getString("bestPractices.profile"),
				profileValueLabel.getText());
		writer.append(lineSep);

		return writer;
	}

	/**
	 * Writes a provided key and value in the file writer.
	 * 
	 * @param writer
	 * @param key
	 * @param value
	 * @return
	 * @throws IOException
	 */
	private FileWriter addKeyValue(FileWriter writer, String key, String value) throws IOException {
		writer.append(key);
		writer.append(value);
		return writer;
	}

}
