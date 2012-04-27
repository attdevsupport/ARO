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

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

/**
 * Represents a table cell editor that is used for adding modifiable combo boxes
 * in a table or tree. To use this class, initialize a new instance of the
 * ComboBoxCellEditor, and add it as the cell editor for a column on the a table.
 * Modify the table model so that the column associated with the cell editor, is
 * editable.
 */
public class ComboBoxCellEditor extends DefaultCellEditor implements
		TableCellEditor, TreeCellEditor {
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes a new instance of the ComboBoxCellEditor class.
	 * 
	 */
	public ComboBoxCellEditor() {
		super(new ComboBoxRenderer());
	}

	/**
	 * Initializes a new instance of the ComboBoxCellEditor class using the
	 * specified combo box model.
	 * 
	 * @param model
	 *            The javax.swing.ComboBoxModel object associated with the combo
	 *            box cell editor.
	 */
	public ComboBoxCellEditor(ComboBoxModel model) {
		super(new ComboBoxRenderer(model));
	}

	/**
	 * 
	 * Initializes a new instance of the ComboBoxCellEditor class using the
	 * specified array of items.
	 * 
	 * @param items
	 *            The array of objects that are the items in the combo box.
	 */
	public ComboBoxCellEditor(Object[] items) {
		super(new ComboBoxRenderer(items));
	}

	/**
	 * Initializes a new instance of the ComboBoxCellEditor class using the
	 * specified vector of items.
	 * 
	 * @param items
	 *            The array of objects that are the items in the combo box.
	 */
	public ComboBoxCellEditor(Vector<?> items) {
		super(new ComboBoxRenderer(items));
	}

	/**
	 * Initializes a new instance of the ComboBoxCellEditor class using the
	 * specified combo box component.
	 * 
	 * @param component
	 *            The javax.swing.JComboBox object associated with the combo box
	 *            cell editor.
	 */
	public ComboBoxCellEditor(JComboBox component) {
		super(component);
	}

	/**
	 * Returns a JComboBox object. This method overrides the
	 * DefaultCellEditor.getComponent method.
	 * 
	 * @return The CheckBoxCellEditor as a JComboBox object.
	 * 
	 * @see javax.swing.DefaultCellEditor#getComponent()
	 */
	@Override
	public JComboBox getComponent() {
		return (JComboBox) super.getComponent();
	}

}
