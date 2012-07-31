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


package com.att.aro.main;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.CheckBoxCellEditor;
import com.att.aro.commonui.CheckBoxRenderer;
import com.att.aro.commonui.ColorCellEditor;
import com.att.aro.commonui.ColorCellRenderer;
import com.att.aro.commonui.DataTableModel;
import com.att.aro.main.FilterIpAddressesTableModel.AppIPAddressSelection;
import com.att.aro.model.AnalysisFilter;
import com.att.aro.model.ApplicationSelection;
import com.att.aro.model.IPAddressSelection;

/**
 * Represents the table model for the Filter IP address dialog. 
 */
public class FilterIpAddressesTableModel extends DataTableModel<AppIPAddressSelection> {
	private static final long serialVersionUID = 1L;

	public class AppIPAddressSelection {
		private IPAddressSelection ipSelection;
		private String appName;
		
		private AppIPAddressSelection(IPAddressSelection ipSelection, String appName) {
			this.ipSelection = ipSelection;
			this.appName = appName;
		}

		/**
		 * @return the ipSelection
		 */
		public IPAddressSelection getIpSelection() {
			return ipSelection;
		}

		/**
		 * @return the appName
		 */
		public String getAppName() {
			return appName;
		}
		
	}
	
	/**
	 * An integer that identifies the select column.
	 */
	public static final int SELECT_COL = 0;

	/**
	 * An integer that identifies the application name column.
	 */
	public static final int APP_COL = 1;

	/**
	 * An integer that identifies the IP address column.
	 */
	public static final int IP_COL = 2;

	/**
	 * An integer that identifies the color column.
	 */
	public static final int COLOR_COL = 3;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String[] columns = { rb.getString("filter.select"),
			rb.getString("filter.app"), rb.getString("filter.ip"),
			rb.getString("filter.color") };

	/**
	 * Constructor that initializes model with filter data from specified analysis.
	 * @param filter The analysis filter to be applied.
	 */
	public FilterIpAddressesTableModel(AnalysisFilter filter) {
		super(columns);

		List<AppIPAddressSelection> data = new ArrayList<AppIPAddressSelection>();
		
		for (ApplicationSelection app : filter.getApplicationSelections()) {
			for (IPAddressSelection ip : app.getIPAddressSelections()) {
				data.add(new AppIPAddressSelection(ip, app.getAppName()));
			}
		}
		setData(data);
	}

	@Override
	protected Object getColumnValue(AppIPAddressSelection item, int columnIndex) {
		switch (columnIndex) {
		case SELECT_COL:
			return item.ipSelection.isSelected();
		case APP_COL:
			return item.appName != null ? item.appName : rb.getString("aro.unknownApp");
		case IP_COL:
			return item.ipSelection.getIpAddress() != null ? item.ipSelection.getIpAddress().getHostAddress() : null;
		case COLOR_COL:
			return item.ipSelection.getColor();
		}
		return null;
	}
	
	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            – The index of the specified column.
	 * 
	 * @return A class representing the specified column.
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case IP_COL:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}
	/**
	 * Returns a value that Indicates if the specified data cell is editable.
	 * 
	 * @param row – The row number of the cell.
	 * 
	 * @param col – The column number of the cell.
	 * 
	 * @return A boolean value that is “true” if the cell is editable, and “false” if not.
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
	 * @param aValue – The value to set for the data item.
	 * 
	 * @param rowIndex – The row index of the data item.
	 * 
	 * @param columnIndex The column index of the data item.
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 *      int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		super.setValueAt(aValue, rowIndex, columnIndex);

		AppIPAddressSelection obj = getValueAt(rowIndex);
		switch (columnIndex) {
		case SELECT_COL:
			if (aValue instanceof Boolean) {
				obj.ipSelection.setSelected(((Boolean) aValue).booleanValue());
			}
			break;
		case COLOR_COL:
			if (aValue instanceof Color) {
				obj.ipSelection.setColor(((Color) aValue));
			}
		}
		fireTableCellUpdated(rowIndex, columnIndex);
	}

}
