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
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/**
 * Represents a progress dialog that is displayed when the ARO Data Collector is
 * started, stopped, or when traces are pulled.
 */
public class DataCollectorProgressDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JProgressBar progressBar;
	private JLabel label;

	/**
	 * Initializes a new instance of the DataCollectorProgressDialog class using
	 * the specified parent window, and status message.
	 * 
	 * @param parent
	 *            - The parent window.
	 * @param message
	 *            – The status message to be displayed in the progress dialog.
	 */
	public DataCollectorProgressDialog(Window parent, String message) {
		super(parent, rb.getString("aro.title.short"));
		setResizable(false);
		setLayout(new BorderLayout());

		this.label = new JLabel(message, SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(350, 60));
		add(label, BorderLayout.NORTH);

		this.progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(320, 15));
		progressBar.setIndeterminate(true);
		add(progressBar, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(parent);
	}
}
