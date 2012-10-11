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
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * Represents the dialog that is used to start the ARO Data Collector. The
 * dialog prompts the user to enter a trace folder name, and starts the ARO Data
 * Collector on the device emulator when the Start button is clicked.
 */
public class DataCollectorStartDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final int TRACE_FOLDER_ALLOWED_LENGTH = 50;

	private JPanel jContentPane;
	private JPanel buttonPanel;
	private JPanel jButtonGrid;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel optionsPanel;
	private JPanel jAdvancedOptionsPanel;
	private JCheckBox jRecordVideoCheckBox;
	private JLabel TraceFolderLabel;
	private JTextField jTraceFolderName;
	private DatacollectorBridge mARODataCollectorBridge;

	/**
	 * Initializes a new instance of the DataCollectorStartDialog class using
	 * the specified instance of the ApplicationResourceOptimizer, and
	 * DatacollectorBridge.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 * 
	 * @param aroDataCollectorBridge
	 *            - The DataCollectorBridge instance for capturing traces from a
	 *            device emulator.
	 */
	public DataCollectorStartDialog(Frame owner, DatacollectorBridge aroDataCollectorBridge) {
		this(owner, aroDataCollectorBridge, null, true);
	}

	/**
	 * Initializes a new instance of the DataCollectorStartDialog class using
	 * the specified instance of the ApplicationResourceOptimizer,
	 * DatacollectorBridge, trace folder name, and video flag.
	 * 
	 * @param owner
	 *            The ApplicationResourceOptimizer instance.
	 * 
	 * @param aroDataCollectorBridge
	 *            The DataCollectorBridge instance for capturing traces from a
	 *            device emulator.
	 * 
	 * @param traceFolderName
	 *            The name of the folder in which the ARO Data Collector trace
	 *            files should be stored.
	 * 
	 * @param recordVideo
	 *            A boolean value that indicates whether to record video for
	 *            this trace or not.
	 */
	public DataCollectorStartDialog(Frame owner, DatacollectorBridge aroDataCollectorBridge,
			String traceFolderName, boolean recordVideo) {
		super(owner);
		mARODataCollectorBridge = aroDataCollectorBridge;
		initialize(traceFolderName, recordVideo);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(String traceFolderName, boolean recordVideo) {
		this.setModal(true);
		this.setTitle(rb.getString("aro.title.short"));
		this.setContentPane(getJContentPane());
		this.pack();
		this.setLocationRelativeTo(getOwner());
		this.getRootPane().setDefaultButton(getOkButton());

		getJTraceFolderTextField().setText(traceFolderName);
		getJTraceFolderTextField().selectAll();
		getJRecordVideoCheckBox().setSelected(recordVideo);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getOptionsPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes buttonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.add(getJButtonGrid(), BorderLayout.CENTER);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes jButtonGrid
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonGrid() {
		if (jButtonGrid == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(10);
			jButtonGrid = new JPanel();
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			jButtonGrid.setLayout(gridLayout);
			jButtonGrid.add(getOkButton(), null);
			jButtonGrid.add(getCancelButton(), null);
		}
		return jButtonGrid;
	}

	/**
	 * This method initializes okButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(rb.getString("Button.start"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (mARODataCollectorBridge != null && jTraceFolderName.getText() != null) {
						if (isContainsSpecialCharacterorSpace(jTraceFolderName.getText())) {
							JOptionPane.showMessageDialog(getOwner(),
									rb.getString("Error.specialchar"),
									MessageFormat.format(rb.getString("aro.title.short"), ""),
									JOptionPane.ERROR_MESSAGE);
							return;
						} else if (jTraceFolderName.getText().toString().length() > TRACE_FOLDER_ALLOWED_LENGTH) {
							JOptionPane.showMessageDialog(getOwner(),
									rb.getString("Error.tracefolderlength"),
									MessageFormat.format(rb.getString("aro.title.short"), ""),
									JOptionPane.ERROR_MESSAGE);
							return;
						} else {
							DataCollectorStartDialog.this.dispose();
							mARODataCollectorBridge.startARODataCollector(jTraceFolderName
									.getText(), getJRecordVideoCheckBox().isSelected());
						}
					}
				}

			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(rb.getString("Button.cancel"));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					DataCollectorStartDialog.this.dispose();
				}

			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes optionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 1.0D;
			gridBagConstraints.gridy = 0;
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new GridBagLayout());
			optionsPanel.add(getJAdvancedOptionsPanel(), gridBagConstraints);
		}
		return optionsPanel;
	}

	/**
	 * This method initializes jAdvancedOptionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJAdvancedOptionsPanel() {
		if (jAdvancedOptionsPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.gridy = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridy = 0;
			jAdvancedOptionsPanel = new JPanel();
			jAdvancedOptionsPanel.setLayout(new GridBagLayout());
			jAdvancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(null, "",
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(
							"Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jAdvancedOptionsPanel.add(getJTraceFolderLabel(), gridBagConstraints1);
			jAdvancedOptionsPanel.add(getJTraceFolderTextField(), gridBagConstraints2);
			jAdvancedOptionsPanel.add(getJRecordVideoCheckBox(), gridBagConstraints3);
		}
		return jAdvancedOptionsPanel;
	}

	/**
	 * 
	 */
	private JLabel getJTraceFolderLabel() {
		if (TraceFolderLabel == null) {
			TraceFolderLabel = new JLabel(rb.getString("collector.folder"), SwingConstants.CENTER);
		}

		return TraceFolderLabel;
	}

	/**
	 * This method initializes jGPSStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRecordVideoCheckBox() {
		if (jRecordVideoCheckBox == null) {
			jRecordVideoCheckBox = new JCheckBox();
			jRecordVideoCheckBox.setText(rb.getString("collector.record"));
			jRecordVideoCheckBox.setSelected(true);
		}
		return jRecordVideoCheckBox;
	}

	private JTextField getJTraceFolderTextField() {
		if (jTraceFolderName == null) {
			jTraceFolderName = new JTextField(25);
		}
		return jTraceFolderName;
	}

	private boolean isContainsSpecialCharacterorSpace(String tracefolername) {
		boolean isContainsSC = false;
		if (tracefolername != null && !tracefolername.equals("")) {
			// Pattern to include alphanumeric with "-"
			Matcher m = Pattern.compile("[^a-zA-Z0-9-]").matcher(tracefolername);
			if (m.find()) {
				isContainsSC = true;
			} else {
				isContainsSC = false;
			}
		} else {
			isContainsSC = true;
		}

		return isContainsSC;

	}

}
