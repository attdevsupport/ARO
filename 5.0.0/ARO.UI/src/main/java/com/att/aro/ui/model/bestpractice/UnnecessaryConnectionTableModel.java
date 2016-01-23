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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionEntry;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class UnnecessaryConnectionTableModel extends DataTableModel<UnnecessaryConnectionEntry>{
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger
	.getLogger(UnnecessaryConnectionTableModel.class.getName());

	private static final int COL1_MIN = 120;
	private static final int COL1_MAX = 150;
	private static final int COL1_PREF = 150;
	
	private static final int COL2_MIN = 120;
	private static final int COL2_MAX = 150;
	private static final int COL2_PREF = 150;
	
	private static final int COL3_MIN = 120;
	private static final int COL3_MAX = 150;
	private static final int COL3_PREF = 150;
	
	private static final int COL4_MIN = 150;
//	private static final int COL4_MAX = 300;
	private static final int COL4_PREF = 150;
	
	private static final int COL_1 = 0;
	private static final int COL_2 = 1;
	private static final int COL_3 = 2;
	private static final int COL_4 = 3;
	private static final String[] COLUMNS = {
		ResourceBundleHelper.getMessageString("connections.unnecessary.table.col1"),
		ResourceBundleHelper.getMessageString("connections.unnecessary.table.col2"),
		ResourceBundleHelper.getMessageString("connections.unnecessary.table.col3"),
		ResourceBundleHelper.getMessageString("connections.unnecessary.table.col4")};
	
	/**
	 * Initializes a new instance of the UnnecessaryTableModel.
	 */
	public UnnecessaryConnectionTableModel(){
		super(COLUMNS);
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
		
		col = cols.getColumn(COL_1);
		col.setMinWidth(COL1_MIN);
		col.setPreferredWidth(COL1_PREF);
		col.setMaxWidth(COL1_MAX);
		
		col = cols.getColumn(COL_2);
		col.setMinWidth(COL2_MIN);
		col.setPreferredWidth(COL2_PREF);
		col.setMaxWidth(COL2_MAX);
		
		col = cols.getColumn(COL_3);
		col.setMinWidth(COL3_MIN);
		col.setPreferredWidth(COL3_PREF);
		col.setMaxWidth(COL3_MAX);
	
		col = cols.getColumn(COL_4);
		col.setMinWidth(COL4_MIN);
		col.setPreferredWidth(COL4_PREF);
//		col.setMaxWidth(COL4_MAX);

	
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
		LOGGER.log(Level.FINE, "getColumnClass, idx: {0}", columnIndex);
		switch (columnIndex) {
		case COL_1:
			return Double.class;
		case COL_2:
			return Double.class;
		case COL_3:
			return Integer.class;
		case COL_4:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}
	
	/**
	* Defines how the data object managed by this table model is mapped to its
	* columns when displayed in a row of the table.
	* 
	* @param item
	*            An object containing the column information.
	* @param columnIndex
	*            The index of the specified column.
	* 
	* @return The table column value calculated for the object.
	*/
	@Override
	protected Object getColumnValue(UnnecessaryConnectionEntry item, int columnIndex) {
		LOGGER.log(Level.FINEST, "getColumnValue, idx:{0}", columnIndex);
		switch (columnIndex) {
		case COL_1:
			return Math.round(item.getLowTime()*1000)/1000.00;
		case COL_2:
			return Math.round(item.getHighTime()*1000)/1000.00;
		case COL_3:
			return item.getBurstCount();
		case COL_4:
			return Math.round(item.getTotalKB()*1000)/1000.00;
		
		default:
			return null;
		}
	}

}
