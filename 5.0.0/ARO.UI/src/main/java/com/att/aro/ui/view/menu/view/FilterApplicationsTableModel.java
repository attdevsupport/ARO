/*
 * Copyright 2012 AT&T
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

import java.awt.Color;
import java.util.Collection;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.ApplicationSelection;
import com.att.aro.core.util.Util;
import com.att.aro.ui.commonui.CheckBoxCellEditor;
import com.att.aro.ui.commonui.CheckBoxRenderer;
import com.att.aro.ui.commonui.ColorCellEditor;
import com.att.aro.ui.commonui.ColorCellRenderer;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents the table model for the Filter Applications dialog. 
 */
public class FilterApplicationsTableModel extends DataTableModel<ApplicationSelection> {
	
	/**
	 * An integer that identifies the selected column.
	 */
	public static final int SELECT_COL = 0;
	/**
	 * An integer that identifies the application.
	 */
	public static final int APP_COL = 1;
	/**
	 * An integer that identifies the color.
	 */
	public static final int COLOR_COL = 2;

	private static final String[] columns = {
		ResourceBundleHelper.getMessageString(MessageItem.filter_select),
		ResourceBundleHelper.getMessageString(MessageItem.filter_app),
		ResourceBundleHelper.getMessageString(MessageItem.filter_color)
	};

	private enum MessageItem {
		filter_select,
		filter_app,
		filter_color
	}

	/**
	 * Initializes a new instance of the FilterApplicationsTableModel class.
	 */
	public FilterApplicationsTableModel(AnalysisFilter filter) {
		super(columns);
		setData(filter.getApplicationSelections());
	}

	private static final long serialVersionUID = 1L;

	/**
	 * This is the one method that must be implemented by subclasses. This method defines how 
	 * the data object managed by this table model is mapped to its columns when displayed 
	 * in a row of the table. The getValueAt() method uses this method to retrieve table cell data.
	 * 
	 * @param
	 * 		item A object containing the column information.
			columnIndex The index of the specified column.
	 *		
	 * @return An object containing the table column value. 
	 */
	@Override
	protected Object getColumnValue(ApplicationSelection item, int columnIndex) {
		switch (columnIndex) {
		case SELECT_COL:
			return item.isSelected();
		case APP_COL:
			String s = item.getAppName();
			return Util.getDefaultAppName(s);
		case COLOR_COL:
			return item.getColor();
		}
		return null;
	}
	
	/**
	 * Returns a value that Indicates if the specified data cell is editable.
	 * 
	 * @param row The row number of the cell.
	 * 
	 * @param col The column number of the cell.
	 * 
	 * @return A boolean value that is "true" if the cell is editable, and "false" if not.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == SELECT_COL || col == COLOR_COL;
	}

	/**
	 * Returns a TableColumnModel that is based on the default table column model for the DataTableModel 
	 * class. The TableColumnModel returned by this method has the same number of columns in the same 
	 * order and structure as the table column model in the DataTableModel. When a DataTable object is 
	 * created, this method is used to create the TableColumnModel if one is not specified. This method 
	 * may be overridden in order to provide customizations to the default column model, such as providing 
	 * a default column width and/or adding column renderers and editors.
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

		col = result.getColumn(COLOR_COL);
		col.setCellRenderer(new ColorCellRenderer());
		col.setCellEditor(new ColorCellEditor());

		return result;
	}

	/**
	 * Sets the value of the specified data item.
	 * 
	 * @param aValue The value to set for the data item.
	 * 
	 * @param rowIndex The row index of the data item.
	 * 
	 * @param columnIndex The column index of the data item.
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 *      int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);

		ApplicationSelection obj = getValueAt(rowIndex);
		switch (columnIndex) {
		case SELECT_COL:
			if (aValue instanceof Boolean) {
				obj.setSelected(((Boolean) aValue).booleanValue());
			}
			break;
		case COLOR_COL:
			if (aValue instanceof Color) {
				obj.setColor(((Color) aValue));
			}
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

}
