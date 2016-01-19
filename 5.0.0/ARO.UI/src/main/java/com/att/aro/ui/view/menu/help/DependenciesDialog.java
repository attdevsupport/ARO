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
package com.att.aro.ui.view.menu.help;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;

import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * @author Harikrishna Yaramachu
 *
* Represents the Notices dialog that displays the content of the notices.txt
 * file.
 */
public class DependenciesDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final String NOTICES = "NOTICES.txt";

	private JPanel mainPanel;
	private JTextComponent text;
	private String content;
	private boolean htmlContent;
	private JScrollPane scroll;
	private JPanel buttonPanel;
	private JButton okButton;

	private final boolean noticesRead;

	/**
	 * Initializes a new instance of the DependenciesDialog class using the specified
	 * instance of the ApplicationResourceOptimizer as the parent instance.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public DependenciesDialog(Window parent) {
		super(parent);

		content = readContent(NOTICES);
		noticesRead = content != null;

		setModal(true);
		setContentPane(getMainPanel(content));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(ResourceBundleHelper.getMessageString("notices.title"));
		setResizable(false);
		pack();
		setLocationRelativeTo(parent);

		getRootPane().setFocusable(true);
		getRootPane().setDefaultButton(okButton);
		new EnableEscKeyCloseDialog(getRootPane(), this);
	}

	private String readContent(String textFile) {
		StringBuffer txtBuf = null;
		htmlContent = false;
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(textFile);
		if (inputStream != null) {
			Reader reader = new BufferedReader(new InputStreamReader(inputStream));
			txtBuf = new StringBuffer();
			try {
				char[] buf = new char[2048];
				int count;
				while ((count = reader.read(buf, 0, buf.length)) >= 0) {
					txtBuf.append(buf, 0, count);
				}
				if (txtBuf.toString().trim().toLowerCase().startsWith("<html>")) {
					htmlContent = true;
				}
			} catch(IOException e) {
				new MessageDialogFactory().showUnexpectedExceptionDialog(
						this, e);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
		return txtBuf != null ? txtBuf.toString() : null;
	}

	/**
	 * Returns the main Panel for the dialog.
	 */
	private JPanel getMainPanel(String content) {
		if (mainPanel == null) {
			mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(getScroll(content), BorderLayout.CENTER);
			mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return mainPanel;
	}

	/**
	 * Returns the TextArea in the dialog.
	 */
	private JTextArea getJTextArea() {
		JTextArea text = new JTextArea();
		text.setColumns(60);
		text.setRows(30);
		text.setEditable(false);
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		text.setFont(AROUIManager.LABEL_FONT);
		return text;
	}
	private JTextPane getJTextPane() {
		JTextPane text = new JTextPane();
		text.setEditable(false);
		text.setContentType("text/html");
		text.setFont(AROUIManager.LABEL_FONT);
		Dimension dialogSize = new Dimension(540, 450);
		text.setPreferredSize(dialogSize);
		text.setSize(dialogSize);
		return text;
	}

	/**
	 * Returns the Scroll Pane for the TextArea in the dialog.
	 */
	private JScrollPane getScroll(String content) {
		if (scroll == null) {
			text = htmlContent ? getJTextPane() : getJTextArea();
			text.setText(content);
			scroll = new JScrollPane(text);
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
			okButton.setText(ResourceBundleHelper.getMessageString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					DependenciesDialog.this.dispose();
				}

			});
		}
		return okButton;
	}

	public boolean isNoticesRead() {
		return noticesRead;
	}
}
