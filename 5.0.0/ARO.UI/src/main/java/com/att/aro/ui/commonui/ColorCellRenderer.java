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
package com.att.aro.ui.commonui;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * Represents a color cell renderer that sets the border and color on a table
 * cell renderer component.
 */
public class ColorCellRenderer extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	private Map<Color, Border> borders = new HashMap<Color, Border>();

	/**
	 * Returns a table cell renderer component using the specified properties.
	 * 
	 * @param table
	 *            The JTable object to associate with the table cell renderer.
	 * @param value
	 *            An object that is the table cell value.
	 * @param isSelected
	 *            A boolean value that sets the cell selection status.
	 * @param row
	 *            An int value that indicates the selected row.
	 * @param column
	 *            An int value that indicates the selected column.
	 * @return A java.awt.Component object that is the table cell renderer with the specified properties.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// Create a border that is the color of the table background
		Color borderColor = isSelected ? table.getSelectionBackground() : table
				.getBackground();
		Border border = borders.get(borderColor);
		if (border == null) {
			border = BorderFactory.createMatteBorder(2, 5, 2, 5, borderColor);
			borders.put(borderColor, border);
		}
		this.setBorder(border);

		// Now set the selected color to be rendered
		Color color = (Color) value;
		if (color != null) {
			this.setOpaque(true);
			this.setBackground(color);
		} else {
			this.setOpaque(false);
			this.setBackground(table.getBackground());
		}
		return this;
	}
}
