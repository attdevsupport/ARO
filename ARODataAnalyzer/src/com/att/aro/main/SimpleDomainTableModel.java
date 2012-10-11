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

import java.text.DecimalFormat;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.NumberFormatRenderer;
import com.att.aro.model.DomainTCPSessions;

/**
 * Represents the table model used to display the data in the Accessed Domains
 * table.
 */
public class SimpleDomainTableModel extends DataTableModel<DomainTCPSessions> {
	private static final long serialVersionUID = 1L;

	private static final int DOMAIN_NAME = 0;
	private static final int NO_TCP = 1;
	private static final int SESSION_LEN = 2;
	private static final int NO_FILES = 3;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String[] columns = {
			rb.getString("simple.domain.name"), rb.getString("simple.no.tcp"),
			rb.getString("simple.session.length"),
			rb.getString("simple.no.files") };

	/**
	 * Initializes a new instance of the SimpleDomainTableModel class.
	 */
	public SimpleDomainTableModel() {
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

		col = cols.getColumn(SESSION_LEN);
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));

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
		case NO_TCP:
		case SESSION_LEN:
		case NO_FILES:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

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
	protected Object getColumnValue(DomainTCPSessions item, int columnIndex) {
		switch (columnIndex) {
		case DOMAIN_NAME:
			return item.getDomainName();
		case NO_TCP:
			return item.getSessions().size();
		case SESSION_LEN:
			return item.getAvgSessionLength();
		case NO_FILES:
			return item.getNumFiles();
		default:
			return null;
		}
	}

}
