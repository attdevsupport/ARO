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
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.TimeRange;
import com.att.aro.model.TraceData;

/**
 * Represents the Select Time Range Dialog Dialog (accessed through the View menu) 
 * that allows the user to set a time range that delineates a section of the 
 * trace data to be analyzed. 
 */
public class ExcludeTimeRangeAnalysisDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	private static ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private JPanel timeRangeSelectionPanel;
	private JPanel jDialogPanel;
	private JButton startButton;
	private JButton cancelButton;
	private JButton resetButton;
	private JTextField startTimeTextField;
	private JTextField endTimeTextField;
	private Double traceEndTime;
	private double timeRangeStartTime;
	private double timeRangeEndTime;

	private ApplicationResourceOptimizer aro;

	private TraceData.Analysis analysisData;

	private JPanel jButtonPanel;

	private JPanel jButtonGrid;

	/**
	 * Initializes a new instance of the ExcludeTimeRangeAnalysisDialog class
	 * using the specified instance of the ApplicationResourceOptimizer as the
	 * owner.
	 * 
	 * @param owner
	 *            The ApplicationResourceOptimizer instance.
	 * 
	 * @param analysisData
	 *            An Analysis object containing the trace data.
	 */
	public ExcludeTimeRangeAnalysisDialog(ApplicationResourceOptimizer owner,
			TraceData.Analysis analysisData) {
		super(owner);
		this.aro = owner;
		this.traceEndTime = analysisData.getTraceData().getTraceDuration();
		TimeRange timeRange = analysisData.getFilter().getTimeRange();
		if(timeRange != null){
			this.timeRangeStartTime = timeRange.getBeginTime();
			this.timeRangeEndTime = timeRange.getEndTime();
		}else{
	    this.timeRangeStartTime = 0.0;
		this.timeRangeEndTime = traceEndTime;
		}
		this.analysisData = analysisData;
		initialize();
	}

	/**
	 * Initializes the dialog.
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(400, 150);
		this.setModal(true);
		this.setTitle(rb.getString("excludetimerangeanalysis.title"));
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
			jDialogPanel.add(getTimeRangeSelectionPanel(), BorderLayout.CENTER);
			jDialogPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jDialogPanel;
	}

	/**
	 * Initializes and returns the JPanel that holds the Ok and Cancel button.
	 */
	private JPanel getJButtonGrid() {
		if (jButtonGrid == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(10);
			jButtonGrid = new JPanel();
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			jButtonGrid.setLayout(gridLayout);
			jButtonGrid.add(getOKButton(), null);
			jButtonGrid.add(getCancelButton(), null);
		}
		return jButtonGrid;
	}

	/**
	 * Initializes jButtonPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = -1;
			gridBagConstraints.gridy = -1;
			jButtonPanel = new JPanel();
			jButtonPanel.setLayout(new BorderLayout());
			jButtonPanel.add(getJButtonGrid(), BorderLayout.EAST);
		}
		return jButtonPanel;
	}

	/**
	 * Initializes and returns the timeRangeSelectionPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getTimeRangeSelectionPanel() {
		if (timeRangeSelectionPanel == null) {
			timeRangeSelectionPanel = new JPanel();

			// timeRangeSelectionPanel.setPreferredSize(new Dimension(75, 75));

			JLabel startTimeLabel = new JLabel(
					rb.getString("timerangeanalysis.start"));
			JLabel endTimeLabel = new JLabel(
					rb.getString("timerangeanalysis.end"));
			timeRangeSelectionPanel.add(startTimeLabel);
			timeRangeSelectionPanel.add(getStartTimeTextField());
			timeRangeSelectionPanel.add(endTimeLabel);
			timeRangeSelectionPanel.add(getEndTimeTextField());
			timeRangeSelectionPanel.add(getResetButton());
		}
		return timeRangeSelectionPanel;
	}

	/**
	 * Initializes and returns the start button
	 */
	private JButton getOKButton() {
		if (startButton == null) {
			startButton = new JButton();
			startButton.setText(rb.getString("Button.ok"));
			startButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (analysisData != null) {

						double startTime;
						double endTime;
						try {
							startTime = getTimeValue(startTimeTextField);
							endTime = getTimeValue(endTimeTextField);
							analysisData.getFilter().setTimeRange(new TimeRange(startTime, endTime));
						} catch (NumberFormatException e) {
							MessageDialogFactory.showErrorDialog(
									ExcludeTimeRangeAnalysisDialog.this,
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

								try {
									aro.performExcludeTimeRangeAnalysis(
											startTime, endTime);
									ExcludeTimeRangeAnalysisDialog.this
											.dispose();
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {
								String strErrorMessage = MessageFormat.format(
										rb.getString("timerangeanalysis.rangeError"),
										0.00, decimalFormat
												.format(traceEndTimeRounded));
								MessageDialogFactory.showMessageDialog(
										ExcludeTimeRangeAnalysisDialog.this,
										strErrorMessage,
										rb.getString("Error.title"),
										JOptionPane.ERROR_MESSAGE);
							}
						} else {
							String strErrorMessage = rb
									.getString("timerangeanalysis.startTimeError");
							MessageDialogFactory.showMessageDialog(
									ExcludeTimeRangeAnalysisDialog.this,
									strErrorMessage,
									rb.getString("Error.title"),
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						MessageDialogFactory.showMessageDialog(
								ExcludeTimeRangeAnalysisDialog.this,
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
	private JButton getResetButton() {
		if (resetButton == null) {
			resetButton = new JButton();
			resetButton.setText(rb.getString("Button.reset"));
			resetButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
                 startTimeTextField.setText(decimalFormat.format(0.0));
                 endTimeTextField.setText(decimalFormat.format(traceEndTime));
				}

			});
		}
		return resetButton;
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
					ExcludeTimeRangeAnalysisDialog.this.dispose();
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
