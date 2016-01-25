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

package com.att.aro.ui.view.menu.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.mvc.IAROView;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;

/**
 * Represents the Select Time Range Dialog Dialog (accessed through the View menu) 
 * that allows the user to set a time range that delineates a section of the 
 * trace data to be analyzed.
 */
public class ExcludeTimeRangeAnalysisDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	private static ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);
	
	private JPanel timeRangeSelectionPanel;
	private JPanel jDialogPanel;
	private JButton startButton;
	private JButton cancelButton;
	private JButton resetButton;
	private JTextField startTimeTextField;
	private JTextField endTimeTextField;
	private IAROView parent;
	
	private double traceEndTime;
	private double timeRangeStartTime;
	private double timeRangeEndTime;
	private double endTimeResetValue = 0.0;
	
	private JPanel jButtonPanel;

	private JPanel jButtonGrid;

	enum DialogItem {
		excludetimerangeanalysis_title,
		timerangeanalysis_start,
		timerangeanalysis_end,
		Button_ok,
		timerangeanalysis_numberError,
		timerangeanalysis_rangeError,
		menu_error_title,
		timerangeanalysis_startTimeError,
		Button_reset,
		Button_cancel
	}

	/**
	 * Initializes a new instance of the ExcludeTimeRangeAnalysisDialog class
	 * using the specified instance of the ApplicationResourceOptimizer as the
	 * owner.
	 * 
	 * @param parent
	 *            The menus parent instance (MainFrame).
	 * 
	 * @param ownerFrame
	 *            Parent graphical instance (MainFrame cast to Frame).
	 */
	public ExcludeTimeRangeAnalysisDialog(IAROView parent) {
		super();
		this.parent = parent;
		initialize();
	}

	/**
	 * Initializes the dialog.
	 * 
	 * @return void
	 */
	private void initialize() {
		PacketAnalyzerResult traceresult = ((MainFrame)parent).getController().getTheModel().getAnalyzerResult();
		if (traceresult==null){
			logger.error("Trace result error! " );
			MessageDialogFactory.getInstance().showErrorDialog(ExcludeTimeRangeAnalysisDialog.this,"wrong.."); 
		}else{
			if(endTimeResetValue == 0.0){
				endTimeResetValue = traceresult.getTraceresult().getTraceDuration();
			}
			traceEndTime = endTimeResetValue;
			TimeRange timeRange = traceresult.getFilter().getTimeRange();
			if(timeRange != null){
				timeRangeStartTime = timeRange.getBeginTime();
				timeRangeEndTime = timeRange.getEndTime();
			}else{
				timeRangeStartTime = 0.0;
				timeRangeEndTime = traceEndTime;
			}
		}

		this.setSize(400, 150);
		this.setModal(true);
		this.setTitle(ResourceBundleHelper.getMessageString(
				DialogItem.excludetimerangeanalysis_title));
		this.setLocationRelativeTo(getOwner());
		this.setContentPane(getJDialogPanel());
		getRootPane().setDefaultButton(startButton);
		new EnableEscKeyCloseDialog(getRootPane(), this);
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
					ResourceBundleHelper.getMessageString(DialogItem.timerangeanalysis_start));
			JLabel endTimeLabel = new JLabel(
					ResourceBundleHelper.getMessageString(DialogItem.timerangeanalysis_end));
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
			startButton.setText(ResourceBundleHelper.getMessageString(DialogItem.Button_ok));
			startButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					double startTime;
					double endTime;
					try {
						startTime = getTimeValue(startTimeTextField);
						endTime = getTimeValue(endTimeTextField);
					} catch (NumberFormatException e) {
						MessageDialogFactory.getInstance().showErrorDialog(
							ExcludeTimeRangeAnalysisDialog.this,
							ResourceBundleHelper.getMessageString(
									DialogItem.timerangeanalysis_numberError));
						return;
					}

					double timeRangeEndTime = Double.valueOf(DECIMAL_FORMAT.format( traceEndTime));
					if (startTime < endTime) {
						if ((startTime >= 0.0) && (startTime <= endTime) &&
								endTime <= timeRangeEndTime) {
							AnalysisFilter filter = ((MainFrame)parent).getController().getTheModel().getAnalyzerResult().getFilter();
							filter.setTimeRange(new TimeRange(startTime, endTime));
							((MainFrame)parent).updateFilter(filter);
							dispose();
						} else {
							String strErrorMessage = MessageFormat.format(
								ResourceBundleHelper.getMessageString(
								DialogItem.timerangeanalysis_rangeError), 0.00, DECIMAL_FORMAT
								.format(timeRangeEndTime));
							MessageDialogFactory.showMessageDialog(
									ExcludeTimeRangeAnalysisDialog.this,
									strErrorMessage,
									ResourceBundleHelper.getMessageString(DialogItem.menu_error_title),
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						String strErrorMessage = ResourceBundleHelper
								.getMessageString(DialogItem.timerangeanalysis_startTimeError);
						MessageDialogFactory.showMessageDialog(
								ExcludeTimeRangeAnalysisDialog.this,
								strErrorMessage,
								ResourceBundleHelper.getMessageString(DialogItem.menu_error_title),
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
			resetButton.setText(ResourceBundleHelper.getMessageString(DialogItem.Button_reset));
			resetButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					traceEndTime = endTimeResetValue;
					setText(startTimeTextField, 0.0);
					setText(endTimeTextField, endTimeResetValue);
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
			cancelButton.setText(ResourceBundleHelper.getMessageString(DialogItem.Button_cancel));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
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
			setText(startTimeTextField, timeRangeStartTime);
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
			setText(endTimeTextField, endTimeResetValue);
		}
		return endTimeTextField;
	}

	private void setText(JTextField field, double value) {
		field.setText(DECIMAL_FORMAT.format(value));
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
			setText(startTimeTextField, timeRangeStartTime);
			setText(endTimeTextField, timeRangeEndTime);
		}
		super.setVisible(visible);
	}

}
