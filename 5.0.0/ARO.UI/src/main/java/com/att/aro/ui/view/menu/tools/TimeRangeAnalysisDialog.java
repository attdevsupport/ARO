/*
 *  Copyright 2015 AT&T
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


package com.att.aro.ui.view.menu.tools;

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

import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents the Time Range Analysis Dialog that allows the user to set a time
 * range that delineates the section of the trace data to be analyzed.
 */
public class TimeRangeAnalysisDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	private static ResourceBundle resourceBundle = ResourceBundleHelper.getDefaultBundle();

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

	private PacketAnalyzerResult analysisData;

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
	public TimeRangeAnalysisDialog(Window owner, PacketAnalyzerResult analysisData) {
		super(owner);
		this.traceEndTime = analysisData.getTraceresult().getTraceDuration();
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
		this.setTitle(resourceBundle.getString("timerangeanalysis.title"));
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
					resourceBundle.getString("timerangeanalysis.start"));
			JLabel endTimeLabel = new JLabel(
					resourceBundle.getString("timerangeanalysis.end"));
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
					resourceBundle.getString("timerangeanalysis.results"));
			if (timeRangeAnalysisResultsTextArea == null) {
				String strTextArea = resourceBundle
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
			startButton.setText(resourceBundle.getString("Button.start"));
			startButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (analysisData == null) {
							MessageDialogFactory.showMessageDialog(
									TimeRangeAnalysisDialog.this,
									resourceBundle.getString("menu.error.noTraceLoadedMessage"),
									resourceBundle.getString("error.title"),
									JOptionPane.ERROR_MESSAGE);
					} else {

						double startTime;
						double endTime;
						try {
							startTime = getTimeValue(startTimeTextField);
							endTime = getTimeValue(endTimeTextField);
						} catch (NumberFormatException e) {
							MessageDialogFactory.showMessageDialog(
									TimeRangeAnalysisDialog.this,
									resourceBundle.getString("timerangeanalysis.numberError"));
							return;
						}

						// Rounding traceEndTime as getEndTimeTextField() to
						// handle time comparison
						Double traceEndTimeRounded = Double.valueOf(DECIMAL_FORMAT.format(traceEndTime));
						if (startTime < endTime) {
							if (startTime >= 0.0 && startTime <= traceEndTimeRounded && endTime >= 0.0 && endTime <= traceEndTimeRounded) {

								TimeRangeAnalysis timeRangeAnalysis = TimeRangeAnalysis.performTimeRangeAnalysis(analysisData, startTime, endTime);
								String msg = null;
								ProfileType profileType = analysisData.getProfile().getProfileType();
								if(profileType == ProfileType.T3G){
									msg = resourceBundle.getString("timerangeanalysis.3g");
								}else if(profileType == ProfileType.LTE){
									msg = resourceBundle.getString("timerangeanalysis.lte");
								}else if(profileType == ProfileType.WIFI){
									msg = resourceBundle.getString("timerangeanalysis.wifi");
								}
								timeRangeAnalysisResultsTextArea.setText(MessageFormat.format(
										msg, DECIMAL_FORMAT.format(startTime),
										DECIMAL_FORMAT.format(endTime),
										timeRangeAnalysis.getPayloadLen(),
										timeRangeAnalysis.getTotalBytes(),
										DECIMAL_FORMAT.format(timeRangeAnalysis
												.getEnergy()), DECIMAL_FORMAT
												.format(timeRangeAnalysis
														.getActiveTime()),
										DECIMAL_FORMAT.format(timeRangeAnalysis
												.getKbps())));

								timeRangeStartTime = startTime;
								timeRangeEndTime = endTime;
							} else {
								String strErrorMessage = MessageFormat.format(
										resourceBundle.getString("timerangeanalysis.rangeError"),
										0.00, DECIMAL_FORMAT
												.format(traceEndTimeRounded));
								MessageDialogFactory.showMessageDialog(
										TimeRangeAnalysisDialog.this,
										strErrorMessage,
										resourceBundle.getString("error.title"),
										JOptionPane.ERROR_MESSAGE);
							}
						} else {
							String strErrorMessage = resourceBundle
									.getString("timerangeanalysis.startTimeError");
							MessageDialogFactory.showMessageDialog(
									TimeRangeAnalysisDialog.this,
									strErrorMessage,
									resourceBundle.getString("error.title"),
									JOptionPane.ERROR_MESSAGE);
						}
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
			cancelButton.setText(resourceBundle.getString("Button.cancel"));
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
			String strStartTime = DECIMAL_FORMAT.format(timeRangeStartTime);
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
			String strEndTime = DECIMAL_FORMAT.format(timeRangeEndTime);
			endTimeTextField.setText(strEndTime);
		}
		return endTimeTextField;
	}

	/**
	 * Sets a value that indicates the visibility of the TimeRangeAnalysis
	 * dialog box.
	 * 
	 * @param visible
	 *            A boolean value that indicates whether the TimeRangeAnalysis
	 *            dialog box is visible.
	 * 
	 * @see java.awt.Dialog#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (!isVisible() || !visible) {
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			startTimeTextField
					.setText(decimalFormat.format(timeRangeStartTime));
			endTimeTextField.setText(decimalFormat.format(timeRangeEndTime));
		}
		super.setVisible(visible);
	}

}
