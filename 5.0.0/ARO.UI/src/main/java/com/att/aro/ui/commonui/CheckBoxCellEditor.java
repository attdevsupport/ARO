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

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

/**
 * Represents a table cell editor that is used for adding modifiable check
 * boxes to a table.To use this class, initialize a new instance of the
 * CheckBoxCellEditor, and add an instance of this class as the cell editor
 * for a column on the a table. Modify the table model so that the column
 * associated with the cell editor, is editable.
 */
public class CheckBoxCellEditor extends DefaultCellEditor implements
		TableCellEditor, TreeCellEditor {

	private static final long serialVersionUID = 1L;

	/**
	 * The default constructor.Initializes a new instance of the
	 * CheckBoxCellEditor class, and registers the new check box renderer object
	 * in the parent class.
	 * 
	 */
	public CheckBoxCellEditor() {
		super(new CheckBoxRenderer());
	}

	/**
	 * Initializes a new instance of the CheckBoxCellEditor class, and registers
	 * the new check box renderer object in the parent class with using the
	 * specified icon object.
	 * 
	 * @param icon
	 *            The Icon object to associate with the check box.
	 */
	public CheckBoxCellEditor(Icon icon) {
		super(new CheckBoxRenderer(icon));
	}

	/**
	 * Initializes a new instance of the CheckBoxCellEditor class, and registers
	 * the new check box renderer object in the parent class using the specified
	 * string and icon objects.
	 * 
	 * @param text
	 *            The String object to associate with the check box.
	 * @param icon
	 *            The Icon object to associate with the check box.
	 */
	public CheckBoxCellEditor(String text, Icon icon) {
		super(new CheckBoxRenderer(text, icon));
	}

	/**
	 * Initializes a new instance of the CheckBoxCellEditor class, and registers
	 * the new check box renderer object in the parent class using the specified
	 * string object.
	 * 
	 * @param text
	 *            The String object to associate with the check box.
	 */
	public CheckBoxCellEditor(String text) {
		super(new CheckBoxRenderer(text));
	}

	/**
	 * Returns the CheckBoxCellEditor object and type casts it to a JCheckBox
	 * object. This method overrides the
	 * javax.swing.DefaultCellEditor.getComponent method.
	 * 
	 * @see javax.swing.DefaultCellEditor#getComponent()
	 * @return The CheckBoxCellEditor as a JCheckBox object.
	 */
	@Override
	public JCheckBox getComponent() {
		return (JCheckBox) super.getComponent();
	}

}
