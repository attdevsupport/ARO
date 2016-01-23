/*
 *  Copyright 2016 AT&T
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
package com.att.aro.ui.model;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Represents the default table cell renderer that makes default cells appear as
 * disabled, when the table is disabled.
 */
public class DataTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the DataTableCellRenderer class.
	 */
	public DataTableCellRenderer(){
		
	}
	/**
	 * Returns a default table cell renderer component with the specified
	 * properties.
	 * 
	 * @param table
	 *            The JTable object to associate with the table cell renderer.
	 * @param value
	 *            An object that is the table cell value.
	 * @param isSelected
	 *            A boolean value that sets the cell selection status.
	 * @param hasFocus
	 *            A boolean value that sets the background and foreground colors
	 *            to indicate focus.
	 * @param row
	 *            An int value that indicates the selected row.
	 * @param column
	 *            An int value that indicates the selected column.
	 * 
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Component crComp = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		crComp.setEnabled(table.isEnabled());

		// Display tooltip on cells
		String sValue = value != null ? value.toString() : null;
		if (sValue != null && sValue.trim().length() > 0) {
			setToolTipText(sValue);
		} else {
			setToolTipText(null);
		}
		return crComp;
	}
}
