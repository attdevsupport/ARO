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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import com.att.aro.images.Images;

/**
 * Creates a panel with image background.
 */
class BackgroundPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	Image image;

	/**
	 * Constructor.
	 */
	public BackgroundPanel() {
		try {
			image = Images.BLUE_HEADER.getIcon().getImage();
		} catch (Exception e) {
		}
	}

	/**
	 * The Constructor with required color as argument
	 * 
	 * @param color
	 *            The required color for the background.
	 */
	public BackgroundPanel(Color color) {
		try {
			if (color == Color.GREEN) {
				image = Images.TEST_PASS_HEADER.getIcon().getImage();
			} else if (color == Color.RED) {
				image = Images.TEST_FAIL_HEADER.getIcon().getImage();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Changes the background image as per the given color.
	 * 
	 * @param color
	 *            The background color required.
	 */
	public void changeImage(Color color) {
		if (color == Color.GREEN) {
			this.image = Images.TEST_PASS_HEADER.getIcon().getImage();
		} else if (color == Color.RED) {
			this.image = Images.TEST_FAIL_HEADER.getIcon().getImage();
		}
		this.paintComponent(this.getGraphics());
	}

	/**
	 * @see JPanel#paintComponents(Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null)
			g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
	}
}