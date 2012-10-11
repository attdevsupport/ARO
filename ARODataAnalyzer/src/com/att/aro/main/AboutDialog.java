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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Represents an About dialog box that is used to display information about ARO,
 * such as the application name and version.
 */
public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JPanel jContentPane;
	private AboutPanel aboutPanel;
	private JPanel buttonPanel;
	private JPanel jButtonGrid;
	private JButton okButton;

	/**
	 * Initializes a new instance of the AboutDialog class, using the specified
	 * instance of the main window of the ARO application to define the parent
	 * frame for the dialog.
	 * 
	 * @param owner
	 *            An instance of an ApplicationResourceOptimizer object that
	 *            defines the main window of the application.
	 */
	public AboutDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * Initializes the dialog.
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle(rb.getString("About.title"));
		this.setModal(true);
		this.setResizable(false);
		this.setContentPane(getJContentPane());
		this.getRootPane().setDefaultButton(getOkButton());
		this.pack();
		this.setLocationRelativeTo(getOwner());
	}

	/**
	 * Initializes and returns jContentPane.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout(5, 5));
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getAboutPanel(), BorderLayout.CENTER);
			jContentPane.setBackground(Color.WHITE);
		}
		return jContentPane;
	}

	/**
	 * Initializes and returns buttonPanel.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.setBackground(Color.WHITE);
			buttonPanel.add(getJButtonGrid(), BorderLayout.EAST);
		}
		return buttonPanel;
	}

	/**
	 * Initializes and returns jButtonGrid.
	 * 
	 * @return javax.swing.JPanel
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
			jButtonGrid.setBackground(Color.WHITE);
			jButtonGrid.add(getOkButton(), null);
		}
		return jButtonGrid;
	}

	/**
	 * Initializes and returns Ok Button.
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(rb.getString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					AboutDialog.this.dispose();
				}

			});
		}
		return okButton;
	}

	/**
	 * Initializes and returns aboutPanel.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAboutPanel() {
		if (aboutPanel == null) {
			aboutPanel = new AboutPanel(this);
		}
		return aboutPanel;
	}

}
