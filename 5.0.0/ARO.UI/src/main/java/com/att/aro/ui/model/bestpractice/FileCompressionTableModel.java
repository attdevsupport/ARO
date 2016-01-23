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
package com.att.aro.ui.model.bestpractice;

import java.text.DecimalFormat;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.bestpractice.pojo.TextFileCompressionEntry;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class FileCompressionTableModel extends DataTableModel<TextFileCompressionEntry>{
	
	private static final long serialVersionUID = 1L;

	private static final int TIME_COL = 0;        // Time
	private static final int HOSTNAME_COL = 1;    // Host Name
	private static final int FILESIZE_COL = 2;    // File Size
	private static final int FILENAME_COL = 3;    // File Name
	
	private static final int TIME_COL_MIN = 70;
	private static final int TIME_COL_MAX = 100;
	private static final int TIME_COL_PREF = 70;

	private static final int HOSTNAME_COL_MIN = 150;
	private static final int HOSTNAME_COL_MAX = 200;
	private static final int HOSTNAME_COL_PREF = 200;

	private static final int FILESIZE_COL_MIN = 70;
	private static final int FILESIZE_COL_MAX = 100;
	private static final int FILESIZE_COL_PREF = 70;

	private static final int FILENAME_COL_MIN = 350;
	private static final int FILENAME_COL_PREF = 350;

	private static final String[] columns = { 
		  ResourceBundleHelper.getMessageString("textFileCompression.table.col1") // Time
		, ResourceBundleHelper.getMessageString("textFileCompression.table.col2") // Host Name
		, ResourceBundleHelper.getMessageString("textFileCompression.table.col3") // File Size
		, ResourceBundleHelper.getMessageString("textFileCompression.table.col4") // File Name
		};

	/**
	 * Initializes a new instance of the CacheAnalysisTableModel class.
	 */
	public FileCompressionTableModel() {
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
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));
		col.setMinWidth(TIME_COL_MIN);
		col.setPreferredWidth(TIME_COL_PREF);
		col.setMaxWidth(TIME_COL_MAX);

		col = cols.getColumn(HOSTNAME_COL);
		col.setMinWidth(HOSTNAME_COL_MIN);
		col.setPreferredWidth(HOSTNAME_COL_PREF);
		col.setMaxWidth(HOSTNAME_COL_MAX);

		col = cols.getColumn(FILESIZE_COL);
		col.setMinWidth(FILESIZE_COL_MIN);
		col.setPreferredWidth(FILESIZE_COL_PREF);
		col.setMaxWidth(FILESIZE_COL_MAX);

		col = cols.getColumn(FILENAME_COL);
		col.setMinWidth(FILENAME_COL_MIN);
		col.setPreferredWidth(FILENAME_COL_PREF);
		//col.setMaxWidth(FILENAME_COL_MAX);

		return cols;
	}

	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            The index of the specified column.
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

	/**
	 * This is the one method that must be implemented by subclasses. This method defines 
	 * how the data object managed by this table model is mapped to its columns when 
	 * displayed in a row of the table. The getValueAt() method uses this method to 
	 * retrieve table cell data.
	 * 
	 * @param
	 * 		item An object containing the column information.
	 *		columnIndex The index of the specified column.
	 *		
	 * @return The table column value calculated for the object.
	 */
	@Override
	protected Object getColumnValue(TextFileCompressionEntry item, int columnIndex) {
		switch (columnIndex) {
		case TIME_COL:
			return item.getTimeStamp();
		case HOSTNAME_COL:
			return item.getHostName();
		case FILESIZE_COL:
			return item.getContentLength();
		case FILENAME_COL:
			return item.getHttpObjectName();
		default:
			return null;
		}
	}

}
