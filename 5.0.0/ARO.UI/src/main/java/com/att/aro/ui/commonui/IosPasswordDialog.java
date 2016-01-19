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

package com.att.aro.ui.commonui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.att.aro.core.ILogger;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents the dialog that is used to start the ARO Data Collector. The
 * dialog prompts the user to enter a trace folder name, and starts the ARO Data
 * Collector on the device emulator when the Start button is clicked.
 */
public class IosPasswordDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private ILogger log = ContextAware.getAROConfigContext().getBean(ILogger.class);
	
	private static final int TRACE_FOLDER_ALLOWED_LENGTH = 50;
	
	private JPanel jContentPane;
	private JPanel buttonPanel;
	private JPanel jButtonGrid;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel optionsPanel;
	private JPanel jAdvancedOptionsPanel;

	private JLabel jTitleLabel;
	private JTextField jPasswordField;

	private String password;

	public void setPassword(String password) {
		this.password = password;
	}

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
	 * @wbp.parser.constructor
	 */
	public IosPasswordDialog(Frame owner) {
		this(owner, null);
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
	 * @param hint
	 *            The name of the folder in which the ARO Data Collector trace
	 *            files should be stored.
	 * 
	 * @param recordVideo
	 *            A boolean value that indicates whether to record video for
	 *            this trace or not.
	 */
	public IosPasswordDialog(Frame owner, String hint) {
		super(owner);
		initialize(hint);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(String hint) {
		this.setModal(true);
		this.setTitle(ResourceBundleHelper.getMessageString("aro.title.short"));
		this.setContentPane(getJContentPane());
		this.pack();
		this.setLocationRelativeTo(getOwner());
		this.getRootPane().setDefaultButton(getOkButton());
		getJPasswordTextField().selectAll();
		jPasswordField.setText("");
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
			okButton.setText(ResourceBundleHelper.getMessageString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					log.debug("Button.ok");
					if(jPasswordField.getText().isEmpty()){						
						log.info("Without sudo password, ARO cannot do important task such as packet capture, setting up Remote Virutal Interface and so on.");
						JOptionPane.showMessageDialog(getOwner()
						, ResourceBundleHelper.getMessageString("Error.sudopasswordissue")
						, MessageFormat.format(ResourceBundleHelper.getMessageString("aro.title.short"), "")
						, JOptionPane.ERROR_MESSAGE);
						
					}else{
						log.debug("get pw for connecting Apple device");
						setPassword(jPasswordField.getText());
						IosPasswordDialog.this.dispose();
						
					}
				}});

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
			cancelButton.setText(ResourceBundleHelper.getMessageString("Button.cancel"));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					password = null;
					IosPasswordDialog.this.dispose();
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
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridy = 0;
			jAdvancedOptionsPanel = new JPanel();
			jAdvancedOptionsPanel.setLayout(new GridBagLayout());
			jAdvancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(null
																			, ""
																			, TitledBorder.DEFAULT_JUSTIFICATION
																			, TitledBorder.DEFAULT_POSITION
																			, new Font("Dialog", Font.BOLD, 12)
																			, new Color(51, 51, 51)));
			jAdvancedOptionsPanel.add(getJTitleLabel(), gridBagConstraints1);
			jAdvancedOptionsPanel.add(getJPasswordTextField(), gridBagConstraints2);
		}
		return jAdvancedOptionsPanel;
	}

	/**
	 * This method initializes TitleLabel
	 */
	private JLabel getJTitleLabel() {
		if (jTitleLabel == null) {
			jTitleLabel = new JLabel(ResourceBundleHelper.getMessageString("Message.entersudopassword"), SwingConstants.CENTER);
		}

		return jTitleLabel;
	}

	private JTextField getJPasswordTextField() {
		if (jPasswordField == null) {
			jPasswordField = new JPasswordField(25);
		}
		return jPasswordField;
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

	/**
	 * @return Password - sudo password
	 */
	public String getPassword() {
		setVisible(true);
		return this.password;
	}
}
