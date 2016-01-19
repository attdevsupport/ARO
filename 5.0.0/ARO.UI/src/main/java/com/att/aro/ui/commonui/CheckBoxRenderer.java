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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * Represents a check box renderer component that renders a check box in a table
 * cell based upon the boolean value in that cell. The checkbox displayed in the
 * table will be checked based upon the boolean value for the cell in the table
 * model.
 */
public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * The default constructor.Initializes a new instance of the
	 * CheckBoxRenderer class.
	 * 
	 */
	public CheckBoxRenderer() {
		super();
		init();
	}

	/**
	 * Initializes a new instance of the CheckBoxRenderer class using the
	 * specified icon.
	 * 
	 * @param icon
	 *            The Icon object to associate with the check box.
	 */
	public CheckBoxRenderer(Icon icon) {
		super(icon);
		init();
	}

	/**
	 * Initializes a new instance of the CheckBoxRenderer class using the
	 * specified string and icon.
	 * 
	 * @param text
	 *            The String object to associate with the check box.
	 * @param icon
	 *            The Icon object to associate with the check box.
	 */
	public CheckBoxRenderer(String text, Icon icon) {
		super(text, icon);
		init();
	}

	/**
	 * Initializes a new instance of the CheckBoxRenderer class using the
	 * specified string.
	 * 
	 * @param text
	 *            The String object to associate with the check box.
	 */
	public CheckBoxRenderer(String text) {
		super(text);
		init();
	}

	/**
	 *Returns a table cell renderer component using the specified properties.
	 * 
	 * @param table
	 *            The JTable object to associate with the table cell renderer. 
	 * @param value
	 *            An object that is the table cell value. 
	 * @param hasFocus
	 *            A boolean value that sets the cell selection status. 
	 * @param row
	 *            An int value that indicates the selected row. 
	 * @param column
	 *            An int value that indicates the selected column.
	 * @return A java.awt.Component object that is the table cell renderer with the specified properties.
	 * 
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// Set checkbox state based on value
		if (value instanceof Boolean) {
			setSelected(((Boolean) value).booleanValue());
			setEnabled(table.isEnabled() && table.isCellEditable(row, column));
		} else {
			return null;
		}
		// Set foreground/background based on whether row is selected
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		if (hasFocus) {
			setBorder(TableCellRendererDefaults.getFocusBorder());
		} else {
			setBorder(TableCellRendererDefaults.getNoFocusBorder());
		}
		return this;
	}

	/**
	 * This method is used to give the checkboxes common default settings. Of
	 * course, these settings can be changed.
	 */
	private void init() {
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorderPainted(true);
	}
}
