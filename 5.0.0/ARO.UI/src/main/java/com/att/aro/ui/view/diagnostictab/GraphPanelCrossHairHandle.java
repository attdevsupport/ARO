/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.view.diagnostictab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Represents the cross hair pointer that moves across the graph.
 */
public class GraphPanelCrossHairHandle extends JPanel {

	private static final long serialVersionUID = 1L;
	/**
	 * An integer value that is the offset of the cross hair handle from 0 along
	 * the X axis (the timeline) of the graph.
	 */
	public static final int ZERO_OFFSET_X = -5;

	private static final int DEFAULT_ZERO_OFFSET_X = 3;
	private CrossHairHandle handle;

	private static final int Y_OFFSET = 1;
	private Color currentColor;

	/**
	 * Initializes a new instance of the GraphPanelCrossHairHandle class using
	 * the specified color for the cross hair handle.
	 * 
	 * @param handleColor
	 *            - The color of the cross hair handle for the graph.
	 */
	public GraphPanelCrossHairHandle(Color handleColor) {
		this.setLayout(null);
		this.setPreferredSize(new Dimension(100, 10));

		currentColor = handleColor;

		handle = new CrossHairHandle(currentColor);
		handle.setBounds(DEFAULT_ZERO_OFFSET_X, Y_OFFSET, 10, 10);
		this.add(handle);
	}

	/**
	 * Sets the cross hair handle to the specified position on the timeline of
	 * the graph.
	 * 
	 * @param pos
	 *            An int that is the new position of the cross-hair handle on
	 *            the timeline of the graph.
	 */
	public void setHandlePosition(int pos) {
		handle.setBounds((pos == 0 ? DEFAULT_ZERO_OFFSET_X : ZERO_OFFSET_X)
				+ pos, Y_OFFSET, 10, 10);
	}

	/**
	 * Implements the cross hair panel.
	 */
	private class CrossHairHandle extends JPanel {

		private static final long serialVersionUID = 1L;
		private Color myColor = Color.blue;

		public CrossHairHandle(Color color) {
			super();
			myColor = color == null ? myColor : color;
		}

		public void paint(Graphics g) {
			int[] XArray = { 0, 10, 5 };
			int[] YArray = { 2, 2, 8 };
			g.setColor(myColor);
			g.drawPolygon(XArray, YArray, 3);
			g.fillPolygon(XArray, YArray, 3);
		}
	}
}
