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
package com.att.aro.ui.model.bestpractice;

import java.text.DecimalFormat;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * @author Harikrishna Yaramachu, Barry Nelson
 *
 */
public class FileDuplicateContentTableModel extends DataTableModel<CacheEntry>{
	
	private static final long serialVersionUID = 1L;

	private static final int FILESIZE_COL = 0;    // File Size
	private static final int COUNT_COL    = 1;    // Time
	private static final int FILENAME_COL = 2;    // File Name
	
	private static final int COUNT_COL_MIN = 70;
	private static final int COUNT_COL_MAX = 100;
	private static final int COUNT_COL_PREF = 70;

	private static final int FILESIZE_COL_MIN = 70;
	private static final int FILESIZE_COL_MAX = 100;
	private static final int FILESIZE_COL_PREF = 70;

	private static final int FILENAME_COL_MIN = 500;
	private static final int FILENAME_COL_PREF = 500;

	private static final String[] columns = { 
		  ResourceBundleHelper.getMessageString("duplicate.table.col0") // File Size
		, ResourceBundleHelper.getMessageString("duplicate.table.col1") // Count
		, ResourceBundleHelper.getMessageString("duplicate.table.col2") // File Name
		};

	/**
	 * Initializes a new instance of the CacheAnalysisTableModel class.
	 */
	public FileDuplicateContentTableModel() {
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

		col = cols.getColumn(COUNT_COL);
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));
		col.setMinWidth(COUNT_COL_MIN);
		col.setPreferredWidth(COUNT_COL_PREF);
		col.setMaxWidth(COUNT_COL_MAX);

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
		case COUNT_COL:
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
	protected Object getColumnValue(CacheEntry item, int columnIndex) {
		switch (columnIndex) {
		case FILESIZE_COL:
			return item.getContentLength();
		case COUNT_COL:
			return item.getCacheHitCount();
		case FILENAME_COL:
			return item.getHttpObjectName();
		default:
			return null;
		}
	}

}
