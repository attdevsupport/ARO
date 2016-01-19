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

package com.att.aro.ui.view.menu.view;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.FilteredProcessSelection;
import com.att.aro.core.packetanalysis.pojo.ProcessSelection;
import com.att.aro.ui.commonui.CheckBoxCellEditor;
import com.att.aro.ui.commonui.CheckBoxRenderer;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents the table model for the Select Processes dialog.
 */
public class FilterProcessesTableModel extends DataTableModel<ProcessSelection> {

	// Represents the table header names
	private static final String[] COLUMNS = {
		ResourceBundleHelper.getMessageString(DialogItem.process_filter_select),
		ResourceBundleHelper.getMessageString(DialogItem.process_filter)
	};
	// An integer that identifies the process column.
	public static final int PROCESS_COL = 1;
	// An integer that identifies the selection column.
	public static final int SELECT_COL = 0;
	// Width of the column containing check boxes. 
	private static final int SELECT_COL_WIDTH = 50;
	private static final long serialVersionUID = 1L;

	private enum DialogItem {
		process_filter_select,
		process_filter
	}

	/**
	 * Initializes a new instance of the FilterProcessesTableModel class.
	 */
	public FilterProcessesTableModel(FilteredProcessSelection selection) {
		super(COLUMNS);
		setData(selection.getProcessSelection());
	}

	/**
	 * Returns a TableColumnModel that is based on the default table column
	 * model for the DataTableModel class. The TableColumnModel returned by this
	 * method has the same number of columns in the same order and structure as
	 * the table column model in the DataTableModel. When a DataTable object is
	 * created, this method is used to create the TableColumnModel if one is not
	 * specified. This method may be overridden in order to provide
	 * customizations to the default column model, such as providing a default
	 * column width and/or adding column renderers and editors.
	 * 
	 * @return A TableColumnModel object.
	 * 
	 * @see com.att.aro.commonui.DataTableModel#createDefaultTableColumnModel()
	 */
	@Override
	public TableColumnModel createDefaultTableColumnModel() {
		TableColumnModel result = super.createDefaultTableColumnModel();
		TableColumn col;

		col = result.getColumn(SELECT_COL);
		col.setCellRenderer(new CheckBoxRenderer());
		col.setCellEditor(new CheckBoxCellEditor());
		col.setMaxWidth(SELECT_COL_WIDTH);

		return result;
	}

	/**
	 * This method defines how the data object managed by this table model is
	 * mapped to its columns when displayed in a row of the table. The
	 * getValueAt() method uses this method to retrieve table cell data.
	 * 
	 * @param item
	 *            A object containing the column information. columnIndex The
	 *            index of the specified column.
	 * 
	 * @return An object containing the table column value.
	 */
	@Override
	protected Object getColumnValue(ProcessSelection item, int columnIndex) {
		switch (columnIndex) {
		case SELECT_COL:
			return item.isSelected();
		case PROCESS_COL:
			return item.getProcessName();
		default:
			return null;
		}
	}

	/**
	 * Returns a value that Indicates if the specified data cell is editable.
	 * 
	 * @param row
	 *            The row number of the cell.
	 * 
	 * @param col
	 *            The column number of the cell.
	 * 
	 * @return A boolean value that is "true" if the cell is editable, and
	 *         "false" if not.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == SELECT_COL;
	}

	/**
	 * Sets the value of the specified data item.
	 * 
	 * @param aValue
	 *            The value to set for the data item.
	 * 
	 * @param rowIndex
	 *            The row index of the data item.
	 * 
	 * @param columnIndex
	 *            The column index of the data item.
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 *      int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);

		ProcessSelection obj = getValueAt(rowIndex);
		if (columnIndex ==  SELECT_COL) {
			if (aValue instanceof Boolean) {
				obj.setSelected(((Boolean) aValue).booleanValue());
			}
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

}
