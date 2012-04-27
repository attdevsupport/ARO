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

import java.awt.Component;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Represents a combo box renderer component that renders a combo box in a DataTable cell.
 */
public class ComboBoxRenderer extends JComboBox implements TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the ComboBoxRenderer class.
	 */
	public ComboBoxRenderer() {
		super();
		init();
	}

	/**
	 * Initializes a new instance of the ComboBoxRenderer class using the specified combo box model.
	 * 
	 * @param model The javax.swing.ComboBoxModel object associated with the combo box renderer.
	 */
	public ComboBoxRenderer(ComboBoxModel model) {
		super(model);
		init();
	}

	/**
	 * Initializes a new instance of the ComboBoxRenderer class using the specified array of items
	 * 
	 * @param items The array of objects that are the items in the combo box.
	 */
	public ComboBoxRenderer(Object[] items) {
		super(items);
		init();
	}

	/**
	 * Initializes a new instance of the ComboBoxenderer class using the specified vector of items.
	 * 
	 * @param items The vector that contains the items in the combo box.
	 */
	public ComboBoxRenderer(Vector<?> items) {
		super(items);
		init();
	}

	/**
	 * Returns a table cell renderer component for a combo box, using the specified properties.
	 * * @param table
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
	 * 
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {

		// Set combo box value
		this.setSelectedItem(value);

		this.setEnabled(table.isEnabled());

		// Set foreground/background based on whether row is selected
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}

		// Set the different border as per the focus state
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
	}

}
