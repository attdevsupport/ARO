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

import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.NumberFormatRenderer;
import com.att.aro.model.CacheEntry;

/**
 * Represents the data table model for duplicate content. This class implements
 * the aro.commonui.DataTableModel class using CacheEntry objects.
 */
public class CacheAnalysisTableModel extends DataTableModel<CacheEntry> {
	private static final long serialVersionUID = 1L;

	private static final int TYPE_COL = 0;
	private static final int TIME_COL = 1;
	private static final int FILENAME_COL = 2;
	private static final int FILESIZE_COL = 3;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String[] columns = { rb.getString("duplicate.type"),
			rb.getString("duplicate.time"), rb.getString("duplicate.filename"),
			rb.getString("duplicate.filesize") };

	/**
	 * Initializes a new instance of the CacheAnalysisTableModel class.
	 */
	public CacheAnalysisTableModel() {
		super(columns);
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
	 */
	@Override
	public TableColumnModel createDefaultTableColumnModel() {
		TableColumnModel cols = super.createDefaultTableColumnModel();
		TableColumn col;

		col = cols.getColumn(TIME_COL);
		col.setCellRenderer(new NumberFormatRenderer());

		col = cols.getColumn(FILESIZE_COL);
		col.setCellRenderer(new NumberFormatRenderer(NumberFormat
				.getIntegerInstance()));

		return cols;
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
		case TIME_COL:
		case FILESIZE_COL:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

	@Override
	protected Object getColumnValue(CacheEntry item, int columnIndex) {
		switch (columnIndex) {
		case TYPE_COL:
			return ResourceBundleManager.getEnumString(item.getDiagnosis());
		case TIME_COL:
			return item.getResponse().getTimeStamp();
		case FILENAME_COL:
			return item.getRequest().getObjName();
		case FILESIZE_COL:
			return item.getResponse().getContentLength();
		default:
			return null;
		}
	}

}
