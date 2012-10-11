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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.ProfileType;
import com.att.aro.model.TimeRangeAnalysis;
import com.att.aro.model.TraceData;

/**
 * Represents the Time Range Analysis Dialog that allows the user to set a time
 * range that delineates the section of the trace data to be analyzed.
 */
public class TimeRangeAnalysisDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	private static ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private JPanel timeRangeSelectionPanel;
	private JPanel timeRangeResultsPanel;
	private JPanel jDialogPanel;
	private JButton startButton;
	private JButton cancelButton;
	private JTextArea timeRangeAnalysisResultsTextArea;
	private JTextField startTimeTextField;
	private JTextField endTimeTextField;
	private Double traceEndTime;
	private double timeRangeStartTime;
	private double timeRangeEndTime;

	private TraceData.Analysis analysisData;

	/**
	 * Initializes a new instance of the TimeRangeAnalysisDialog class using the
	 * specified instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            The ApplicationResourceOptimizer instance.
	 * 
	 * @param analysisData
	 *            An Analysis object containing the trace data.
	 */
	public TimeRangeAnalysisDialog(Window owner, TraceData.Analysis analysisData) {
		super(owner);
		this.traceEndTime = analysisData.getTraceData().getTraceDuration();
		this.timeRangeStartTime = 0.0;
		this.timeRangeEndTime = traceEndTime;
		this.analysisData = analysisData;
		initialize();
	}

	/**
	 * Initializes the dialog.
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(400, 300);
		this.setModal(true);
		this.setTitle(rb.getString("timerangeanalysis.title"));
		this.setLocationRelativeTo(getOwner());
		this.setContentPane(getJDialogPanel());
	}

	/**
	 * Initializes jDialogPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJDialogPanel() {
		if (jDialogPanel == null) {
			jDialogPanel = new JPanel();
			jDialogPanel.setLayout(new BorderLayout());
			jDialogPanel.add(getTimeRangeSelectionPanel(), BorderLayout.NORTH);
			jDialogPanel.add(getTimeRangeResultsPanel(), BorderLayout.CENTER);
		}
		return jDialogPanel;
	}

	/**
	 * Initializes and returns the timeRangeSelectionPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getTimeRangeSelectionPanel() {
		if (timeRangeSelectionPanel == null) {
			timeRangeSelectionPanel = new JPanel();

			timeRangeSelectionPanel.setPreferredSize(new Dimension(100, 70));

			JLabel startTimeLabel = new JLabel(
					rb.getString("timerangeanalysis.start"));
			JLabel endTimeLabel = new JLabel(
					rb.getString("timerangeanalysis.end"));
			timeRangeSelectionPanel.add(startTimeLabel);
			timeRangeSelectionPanel.add(getStartTimeTextField());
			timeRangeSelectionPanel.add(endTimeLabel);
			timeRangeSelectionPanel.add(getEndTimeTextField());
			timeRangeSelectionPanel.add(getStartButton());
			timeRangeSelectionPanel.add(getCancelButton());
		}
		return timeRangeSelectionPanel;
	}

	/**
	 * Initializes and returns timeRangeResultsPanel
	 */
	private JPanel getTimeRangeResultsPanel() {
		if (timeRangeResultsPanel == null) {
			timeRangeResultsPanel = new JPanel();
			timeRangeResultsPanel.setLayout(new BorderLayout());
			JLabel resultsLabel = new JLabel(
					rb.getString("timerangeanalysis.results"));
			if (timeRangeAnalysisResultsTextArea == null) {
				String strTextArea = rb
						.getString("timerangeanalysis.actionInfo");
				timeRangeAnalysisResultsTextArea = new JTextArea("\n"
						+ strTextArea);
			}
			timeRangeAnalysisResultsTextArea.setEditable(false);
			timeRangeAnalysisResultsTextArea.setFocusable(false);
			timeRangeAnalysisResultsTextArea.setLineWrap(true);
			timeRangeAnalysisResultsTextArea.setWrapStyleWord(true);
			Border padding = BorderFactory
					.createBevelBorder(BevelBorder.RAISED);
			timeRangeResultsPanel.setBorder(padding);
			timeRangeResultsPanel.add(resultsLabel, BorderLayout.NORTH);
			timeRangeResultsPanel.add(timeRangeAnalysisResultsTextArea,
					BorderLayout.CENTER);
		}
		return timeRangeResultsPanel;
	}

	/**
	 * Initializes and returns the start button
	 */
	private JButton getStartButton() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setText(rb.getString("Button.start"));
			startButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (analysisData != null) {

						double startTime;
						double endTime;
						try {
							startTime = getTimeValue(startTimeTextField);
							endTime = getTimeValue(endTimeTextField);
						} catch (NumberFormatException e) {
							MessageDialogFactory.showErrorDialog(
									TimeRangeAnalysisDialog.this,
									rb.getString("timerangeanalysis.numberError"));
							return;
						}

						// Rounding traceEndTime as getEndTimeTextField() to
						// handle time comparison
						Double traceEndTimeRounded = Double
								.valueOf(decimalFormat.format(traceEndTime));
						if (startTime < endTime) {
							if (((startTime >= 0.0) && (startTime <= traceEndTimeRounded))
									&& ((endTime >= 0.0) && (endTime <= traceEndTimeRounded))) {

								TimeRangeAnalysis timeRangeAnalysis = analysisData
										.performTimeRangeAnalysis(startTime,
												endTime);
								String msg = null;
								ProfileType profileType = analysisData.getProfile().getProfileType();
								if(profileType == ProfileType.T3G){
									msg = rb.getString("timerangeanalysis.3g");
								}else if(profileType == ProfileType.LTE){
									msg = rb.getString("timerangeanalysis.lte");
								}else if(profileType == ProfileType.WIFI){
									msg = rb.getString("timerangeanalysis.wifi");
								}
								timeRangeAnalysisResultsTextArea.setText(MessageFormat.format(
										msg, decimalFormat.format(startTime),
										decimalFormat.format(endTime),
										timeRangeAnalysis.getPayloadLen(),
										timeRangeAnalysis.getTotalBytes(),
										decimalFormat.format(timeRangeAnalysis
												.getEnergy()), decimalFormat
												.format(timeRangeAnalysis
														.getActiveTime()),
										decimalFormat.format(timeRangeAnalysis
												.getKbps())));

								timeRangeStartTime = startTime;
								timeRangeEndTime = endTime;
							} else {
								String strErrorMessage = MessageFormat.format(
										rb.getString("timerangeanalysis.rangeError"),
										0.00, decimalFormat
												.format(traceEndTimeRounded));
								MessageDialogFactory.showMessageDialog(
										TimeRangeAnalysisDialog.this,
										strErrorMessage,
										rb.getString("Error.title"),
										JOptionPane.ERROR_MESSAGE);
							}
						} else {
							String strErrorMessage = rb
									.getString("timerangeanalysis.startTimeError");
							MessageDialogFactory.showMessageDialog(
									TimeRangeAnalysisDialog.this,
									strErrorMessage,
									rb.getString("Error.title"),
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						MessageDialogFactory.showMessageDialog(
								TimeRangeAnalysisDialog.this,
								rb.getString("Error.notrace"),
								rb.getString("Error.title"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return startButton;
	}

	/**
	 * Initializes and returns the cancel button.
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(rb.getString("Button.cancel"));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					TimeRangeAnalysisDialog.this.dispose();
				}

			});
		}
		return cancelButton;
	}

	/**
	 * Initializes and returns the startTimeTextField
	 */
	private JTextField getStartTimeTextField() {
		if (startTimeTextField == null) {
			startTimeTextField = new JTextField(8);
			String strStartTime = decimalFormat.format(timeRangeStartTime);
			startTimeTextField.setText(strStartTime);
		}
		return startTimeTextField;
	}

	/**
	 * Returns the start time from the start time field
	 */
	private Double getTimeValue(JTextField field) throws NumberFormatException {
		return Double.parseDouble(field.getText());
	}

	/**
	 * Initializes and returns the endTimeTextField
	 */
	private JTextField getEndTimeTextField() {
		if (endTimeTextField == null) {
			endTimeTextField = new JTextField(8);
			String strEndTime = decimalFormat.format(timeRangeEndTime);
			endTimeTextField.setText(strEndTime);
		}
		return endTimeTextField;
	}

	/**
	 * Sets a value that indicates the visibility of the TimeRangeAnalysis
	 * dialog box.
	 * 
	 * @param b
	 *            A boolean value that indicates whether the TimeRangeAnalysis
	 *            dialog box is visible.
	 * 
	 * @see java.awt.Dialog#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		if (!isVisible() || !b) {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			startTimeTextField
					.setText(decimalFormat.format(timeRangeStartTime));
			endTimeTextField.setText(decimalFormat.format(timeRangeEndTime));
		}
		super.setVisible(b);
	}

}
