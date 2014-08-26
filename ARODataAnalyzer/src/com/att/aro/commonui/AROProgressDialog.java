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


package com.att.aro.commonui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.att.aro.main.ResourceBundleManager;

/**
 * Represents a progress dialog that indicates a background process such as loading, is in progress.
 */
public class AROProgressDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JProgressBar progressBar;
	private JLabel label;

	/**
	 * Initializes a new instance of the AROProgressDialog class using
	 * the specified parent window, and status message.
	 * 
	 * @param parent
	 *            The parent window.
	 * @param message
	 *            The status message to be displayed in the progress dialog.
	 */
	public AROProgressDialog(Window parent, String message) {
		super(parent, rb.getString("aro.title.short"));
		setResizable(false);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

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
