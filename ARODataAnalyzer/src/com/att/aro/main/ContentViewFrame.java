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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.att.aro.model.ContentException;
import com.att.aro.model.HttpRequestResponseInfo;

/**
 * Represents a frame for the ContentViewer.
 */
public class ContentViewFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private HttpRequestResponseInfo rrInfo;
	private JScrollPane contentComponent;
	private JPanel buttonPanel;
	private JButton closeButton;

	/**
	 * Initializes a new instance of the ContentViewFrame class using the
	 * specified content.
	 * 
	 * @param rrInfo
	 *            - The HttpRequestResponseInfo object containing the content.
	 */
	public ContentViewFrame(HttpRequestResponseInfo rrInfo)
			throws ContentException, IOException {
		this.rrInfo = rrInfo;
		initialize();
	}

	/**
	 * Returns the request/response object whose content is being viewed.
	 * 
	 * @return HttpRequestResponseInfo The request/response object associated
	 *         with this ContentViewer.
	 */
	public HttpRequestResponseInfo getRrInfo() {
		return rrInfo;
	}

	/**
	 * This method initializes the frame.
	 * 
	 * @throws ContentException
	 * @throws IOException
	 */
	private void initialize() throws ContentException, IOException {
		this.setMinimumSize(new Dimension(400, 400));
		this.setTitle(rb.getString("fileChooser.rrContentViewerTitle"));
		this.setLocationByPlatform(true);

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(getContentComponent(), BorderLayout.CENTER);
		panel.add(getButtonPanel(), BorderLayout.SOUTH);
		this.setContentPane(panel);
	}

	private JScrollPane getContentComponent() throws ContentException,
			IOException {
		if (contentComponent == null) {
			JComponent result = null;
			if (rrInfo.getContentType() != null
					&& rrInfo.getContentType().contains(
							rb.getString("fileChooser.contentType.image"))) {
				ImageIcon icon = new ImageIcon(rrInfo.getContent());
				JLabel imagelJLabel = new JLabel(icon);
				imagelJLabel.setVerticalAlignment(JLabel.CENTER);
				result = imagelJLabel;
			} else {
				JTextArea contentTxtArea = new JTextArea(
						rrInfo.getContentString());
				contentTxtArea.setLineWrap(true);
				contentTxtArea.setEditable(false);
				result = contentTxtArea;
			}
			contentComponent = new JScrollPane(result);
		}
		return contentComponent;
	}

	/**
	 * @return the buttonPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel(new BorderLayout());
			JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
			panel.add(getCloseButton());
			buttonPanel.add(panel, BorderLayout.EAST);
		}
		return buttonPanel;
	}

	/**
	 * Initializes and returns the Close button for this ContentViewer.
	 * 
	 * @return JButton The Close button.
	 */
	public JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(rb.getString("Button.close"));
			closeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					ContentViewFrame.this.dispose();
				}

			});
		}
		return closeButton;
	}

}
