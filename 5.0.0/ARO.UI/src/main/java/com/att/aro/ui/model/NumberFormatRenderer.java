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
package com.att.aro.ui.model;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;

/**
 * The NumberFormatRenderer class is used to render boolean values in a table as
 * a number that is formatted like a time value to the 10th decimal place.
 */
public class NumberFormatRenderer extends DataTableCellRenderer {
	private static final long serialVersionUID = 1L;

	private NumberFormat formatter = new DecimalFormat("0.0");

	/**
	 * Initializes a new instance of the NumberFormatRenderer class using the
	 * default number formatter.
	 * 
	 */
	public NumberFormatRenderer() {
		super();
	}

	/**
	 * Initializes a new instance of the NumberFormatRenderer class using the
	 * specified number formatter.
	 * 
	 * @param formatter
	 *            The number formatter.
	 */
	public NumberFormatRenderer(NumberFormat formatter) {
		super();
		this.formatter = formatter != null ? formatter : this.formatter;
	}

	/**
	 * Returns a table cell renderer component that is created using the
	 * specified properties.
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
	 * @return The java.awt.Component object that is the new table cell
	 *         renderer.
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		return super.getTableCellRendererComponent(table,
				value != null ? formatter.format(value) : null, isSelected,
				hasFocus, row, column);
	}
}
