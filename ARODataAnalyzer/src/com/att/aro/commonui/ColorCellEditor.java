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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.att.aro.main.ResourceBundleManager;

/**
 * Represents a table cell editor that allows color selection.
 */
public class ColorCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private Color currentColor;
	private JButton button;
	private JColorChooser colorChooser;
	private JDialog dialog;

	/**
	 * Initializes a new instance of the ColorCellEditor class.
	 */
	public ColorCellEditor() {
		button = new JButton();
		button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		button.setBorderPainted(false);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				dialog.setVisible(true);
				// Make the renderer reappear.
				fireEditingStopped();
			}
		});
		// Set up the dialog that the button brings up.
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button,
				rb.getString("selectColor.title"), true, colorChooser,
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						currentColor = colorChooser.getColor();
					}
				}, // OK button handler
				null); // no CANCEL button handler
	}

	/**
	 * Returns the Color object from the table cell editor.
	 * 
	 * @return The color object.
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return currentColor;
	}

	/**
	 * Returns a table cell editor ( a button component) with the specified
	 * properties.
	 * 
	 * @param table
	 *            The JTable object to associate with the table cell editor.
	 * @param value
	 *            An object that is the table cell value.
	 * @param isSelected
	 *            A boolean value that sets the cell selection status.
	 * @param row
	 *            An int value that indicates the selected row.
	 * @param column
	 *            An int value that indicates the selected column.
	 * @return A java.awt.Component object that is the table cell editor with
	 *         the specified properties.
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		currentColor = (Color) value;
		return button;
	}

}
