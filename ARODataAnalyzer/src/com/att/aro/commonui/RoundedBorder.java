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
package com.att.aro.commonui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * Represents a rounded border for button or panel objects.
 */
public class RoundedBorder implements Border {

	private int radius = 25;
	private Color fillColor;
	private Insets insets;

	/**
	 * Initializes a new instance of the RoundedBorder class using the default
	 * radius for the rounded corners.
	 */
	public RoundedBorder() {
		this(null, null);
	}

	/**
	 * Initializes a new instance of the RoundedBorder class using the specified
	 * radius for the rounded corners, and the specified fill color.
	 * 
	 * @param insets
	 *            The radius for the rounded corners.
	 * @param fillColor
	 *            The fill color for the border.
	 */
	public RoundedBorder(Insets insets, Color fillColor) {
		this.fillColor = fillColor;
		this.insets = insets != null ? insets : new Insets(this.radius + 1,
				this.radius + 1, this.radius + 2, this.radius);
	}

	/**
	 * Returns the insets (the corner radius values) of the specified border
	 * object.
	 * 
	 * @param c
	 *            A Component object, that is the border to return the inset
	 *            values for.
	 * @return An java.awt.Insets object that contains the corner radius values.
	 */
	@Override
	public Insets getBorderInsets(Component c) {
		return insets;
	}

	/**
	 * RReturns a boolean value that indicates whether or not the border is
	 * opaque. If the border is opaque, it is responsible for filling its own
	 * background when painting.
	 * 
	 * @return A boolean value that is true if the border is opaque and false if
	 *         it is not.
	 */
	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	/**
	 * Paints the border of the specified component using the specified position
	 * and size.
	 * 
	 * @param c
	 *            The component for which the border is being painted.
	 * @param g
	 *            The graphics object to use for painting.
	 * @param x
	 *            The x position of the painted border.
	 * @param y
	 *            The y position of the painted border.
	 * @param width
	 *            The width of the painted border.
	 * @param height
	 *            The height of the painted border.
	 * 
	 */
	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		if (fillColor != null) {
			Color color = g.getColor();
			g.setColor(Color.WHITE);
			g.fillRoundRect(x, y, width, height, radius, radius);
			g.setColor(color);
		}
		g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
	}

}
