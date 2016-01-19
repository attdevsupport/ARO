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

package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.att.aro.ui.commonui.ImagePanel;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.view.images.Images;

/**
 * Represents a panel that displays the header information in a Best Practices
 * detailed Results section.
 */
public class BpHeaderPanel extends ImagePanel {
	private static final long serialVersionUID = 1L;

	//private static final ResourceBundle rb = ResourceBundleHelper.getDefaultBundle();
	private static final Font PAGE_FONT = new Font("TITLEFont", Font.BOLD, 14);
	private static final Font TITLE_FONT = new Font("TITLEFont", Font.BOLD, 18);
	private static final Image NOT_RUN_HEADER = Images.TEST_NOT_RUN_HEADER.getImage();
	private static final Image PASS_HEADER = Images.TEST_PASS_HEADER.getImage();
	private static final Image FAIL_HEADER = Images.TEST_FAIL_HEADER.getImage();
	private static final Image WARNING_HEADER = Images.TEST_WARNING_HEADER.getImage();
	private static final String PASS = ResourceBundleHelper.getMessageString("bestPractices.pass");
	private static final String FAIL = ResourceBundleHelper.getMessageString("bestPractices.fail");
	private static final String WARNING = ResourceBundleHelper.getMessageString("bestPractices.warning");

	private JLabel rightLabel;

	/**
	 * Initializes a new instance of the BpHeaderPanel class using the header
	 * text.
	 * 
	 * @param title
	 *            - The header text for the Detailed Results section.
	 */
	public BpHeaderPanel(String title) {
		super(NOT_RUN_HEADER, true);
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
	public void setPass(Boolean pass, boolean warning) {
		if (pass == null) {
			rightLabel.setText(null);
			setImage(NOT_RUN_HEADER);
		} else if (!pass.booleanValue()) {
			rightLabel.setText(FAIL);
			setImage(FAIL_HEADER);
		} else if (warning) {
			rightLabel.setText(WARNING);
			setImage(WARNING_HEADER);
		} else {
			rightLabel.setText(PASS);
			setImage(PASS_HEADER);
		} 
	}

}