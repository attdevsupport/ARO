/*
 *  Copyright 2012 AT&T
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * Represents an ImagePanel that is an extension of a JPanel which allows a
 * background image.
 */
public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Image image;
	private boolean scale;

	/**
	 * Initializes a new instance of the ImagePanel class using the specified
	 * Image
	 * 
	 * @param image
	 *            The Image object to be displayed in the ImagePanel.
	 */
	public ImagePanel(Image image) {
		this(image, false);
	}

	/**
	 * Initializes a new instance of the ImagePanel class using the specified
	 * Image, a flag that indicates whether the image should be scaled, and a
	 * background color.
	 * 
	 * @param image
	 *            Image object to be displayed in the ImagePanel.
	 * @param backgroundColor
	 *            A Color object that is the background color to be displayed in
	 *            the ImagePanel.
	 */
	public ImagePanel(Image image, Color backgroundColor) {
		this(image, false, backgroundColor);
	}

	/**
	 * Initializes a new instance of the ImagePanel class using the specified
	 * Image, and a flag that indicates whether the image should be scaled to
	 * the width and height of the ImagePanel.
	 * 
	 * @param image
	 *            Image object to be displayed in the ImagePanel.
	 * @param scale
	 *            A boolean value. When this value is true, the Image will be
	 *            scaled to the width and height of the ImagePanel. When this
	 *            value is false, the image is not scaled.
	 */
	public ImagePanel(Image image, boolean scale) {
		this(image, scale, null);
	}

	/**
	 * Initializes a new instance of the ImagePanel class using the specified
	 * Image, a flag that indicates whether the image should be scaled, and a
	 * background color.
	 * 
	 * @param image
	 *            Image object to be displayed in the ImagePanel.
	 * @param scale
	 *            A boolean value. When this value is false, the image is not
	 *            scaled.
	 * @param backgroundColor
	 *            A Color object that is the background color to be displayed in
	 *            the ImagePanel.
	 */
	public ImagePanel(Image image, boolean scale, Color backgroundColor) {
		this.image = image;
		this.scale = scale;
		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}
	}

	/**
	 * Sets the image in the ImagePanel at runtime.
	 * 
	 * @param image
	 *            The Image object to display in the ImagePanel.
	 */
	public void setImage(Image image) {
		this.image = image;
		repaint();
	}

	/**
	 * Paints the image on the ImagePanel using the specified Graphics object. 
	 * Overrides the paintComponent method in JComponent.
	 */
	@Override
	protected void paintComponent(Graphics gObject) {
		super.paintComponent(gObject);
		// draw the image as per the scale state.
		if (scale) {
			gObject.drawImage(image, 0, 0, this.getWidth() + 2, this.getHeight(),
					this);
		} else {
			gObject.drawImage(image, 0, 0, this);
		}
	}

}
