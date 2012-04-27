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
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.att.aro.commonui.AROUIManager;

/**
 * Represents the Notices dialog that displays the content of the notices.txt
 * file.
 */
public class NoticesDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String NOTICES = "NOTICES.txt";

	private JPanel mainPanel;
	private JTextArea text;
	private JScrollPane scroll;
	private JPanel buttonPanel;
	private JButton okButton;

	/**
	 * Initializes a new instance of the NoticesDialog class using the specified
	 * instance of the ApplicationResourceOptimizer as the parent instance.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public NoticesDialog(Window parent) throws IOException {
		super(parent);

		this.setModal(true);
		this.setContentPane(getMainPanel());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle(rb.getString("Notices.title"));
		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(parent);

		Reader reader = new BufferedReader(new InputStreamReader(getClass()
				.getClassLoader().getResourceAsStream(NOTICES)));
		try {
			char[] buf = new char[2048];
			StringBuffer txtBuf = new StringBuffer();
			int count;
			while ((count = reader.read(buf, 0, buf.length)) >= 0) {
				txtBuf.append(buf, 0, count);
			}
			getText().setText(txtBuf.toString());
			getText().setCaretPosition(0);
		} finally {
			reader.close();
		}
	}

	/**
	 * Returns the main Panel for the dialog.
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(getScroll(), BorderLayout.CENTER);
			mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return mainPanel;
	}

	/**
	 * Returns the TextArea in the dialog.
	 */
	private JTextArea getText() {
		if (text == null) {
			text = new JTextArea();
			text.setColumns(60);
			text.setRows(30);
			text.setEditable(false);
			text.setWrapStyleWord(true);
			text.setLineWrap(true);
			text.setFont(AROUIManager.LABEL_FONT);
		}
		return text;
	}

	/**
	 * Returns the Scroll Pane for the TextArea in the dialog.
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane(getText());
		}
		return scroll;
	}

	/**
	 * Initializes and returns the panel the contains the Ok button in the
	 * dialog.
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(10);
			JPanel panel = new JPanel(gridLayout);
			panel.add(getOkButton());
			buttonPanel.add(panel, BorderLayout.EAST);
		}
		return buttonPanel;
	}

	/**
	 * Initializes and returns the Ok button in the dialog.
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(rb.getString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					NoticesDialog.this.dispose();
				}

			});
		}
		return okButton;
	}

}
