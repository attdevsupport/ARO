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
import java.awt.Image;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.att.aro.commonui.ImagePanel;
import com.att.aro.images.Images;

/**
 * Represents a panel that displays the header information in a Best Practices
 * detailed Results section.
 */
public class BpHeaderPanel extends ImagePanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final Font PAGE_FONT = new Font("TITLEFont", Font.BOLD, 14);
	private static final Font TITLE_FONT = new Font("TITLEFont", Font.BOLD, 18);
	private static final Image notRunHeader = Images.TEST_NOT_RUN_HEADER
			.getImage();
	private static final Image passHeader = Images.TEST_PASS_HEADER.getImage();
	private static final Image failHeader = Images.TEST_FAIL_HEADER.getImage();
	private static final String PASS = rb.getString("bestPractices.pass");
	private static final String FAIL = rb.getString("bestPractices.fail");

	private JLabel rightLabel;

	/**
	 * Initializes a new instance of the BpHeaderPanel class using the header
	 * text.
	 * 
	 * @param title
	 *            - The header text for the Detailed Results section.
	 */
	public BpHeaderPanel(String title) {
		super(notRunHeader, true);
		setOpaque(false);
		setLayout(new BorderLayout());

		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		JLabel leftLabel = new JLabel(title);
		leftLabel.setFont(TITLE_FONT);
		leftLabel.setForeground(Color.WHITE);
		panel.add(leftLabel, BorderLayout.WEST);

		this.rightLabel = new JLabel();
		rightLabel.setFont(PAGE_FONT);
		rightLabel.setForeground(Color.WHITE);
		panel.add(rightLabel, BorderLayout.EAST);

		this.add(panel, BorderLayout.CENTER);
	}

	/**
	 * Sets the Pass/Fail label on the right side of the header in a Detailed
	 * Results section. If any of the tests in a Best Practices category (
	 * Caching, Connections, and Other) fails, the Detailed Results section for
	 * that category is labeled as "Fail".
	 * 
	 * @param pass
	 *            - A boolean value that indicates whether any of the best
	 *            practices tests in this Detailed Results section have failed.
	 */
	public void setPass(Boolean pass) {
		if (pass == null) {
			rightLabel.setText(null);
			setImage(notRunHeader);
		} else if (pass.booleanValue()) {
			rightLabel.setText(PASS);
			setImage(passHeader);
		} else {
			rightLabel.setText(FAIL);
			setImage(failHeader);
		}
	}

}