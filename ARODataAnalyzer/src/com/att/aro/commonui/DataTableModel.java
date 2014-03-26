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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Represents a base implementation for a table model that is used to display
 * data in JTables. The DataTableModel class manages its table data as a collection of generic
 * objects whose type is defined when the class is used.
 */
public abstract class DataTableModel<T> extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private List<T> data = new ArrayList<T>();
	private String[] columns;

	/**
	 * Initializes a new instance of an empty DataTableModel class.
	 * 
	 * @param columns
	 *            An array of java.lang.String objects that are the columns in
	 *            the data table.
	 */
	public DataTableModel(String[] columns) {
		this.columns = columns;
	}

	/**
	 * Initializes a new instance of a populated DataTableModel class
	 * 
	 * @param columns
	 *            An array of java.lang.String objects that are the columns in
	 *            the data table.
	 * @param data
	 *            A java.util.Collection of data objects.
	 */
	public DataTableModel(String[] columns, Collection<T> data) {
		this.columns = columns;
		setData(data);
	}

	/**
	 * Returns the data object at the specified row.
	 * 
	 * @param rowIndex
	 *            The index of the row.
	 * @return The data object.
	 */
	public T getValueAt(int rowIndex) {
		return this.data.get(rowIndex);
	}

	/**
	 * Gets the number of rows in the data table model.
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 * @return The number of rows.
	 */
	@Override
	public int getRowCount() {
		return this.data.size();
	}

	/**
	 * Gets the name of the column at the specified index.
	 * 
	 * @param column
	 *            The index of the column.
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 * @return The column name.
	 */
	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	/**
	 * Gets the number of columns in the data table model.
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 * @return The number of columns.
	 */
	@Override
	public int getColumnCount() {
		return columns.length;
	}

	/**
	 * Returns the data object at the specified row and column in the data
	 * table.
	 * 
	 * @param rowIndex
	 *            The index of the row.
	 * @param columnIndex
	 *            The index of the column.
	 * @return The data object.
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getColumnValue(getValueAt(rowIndex), columnIndex);
	}

	/**
	 * Returns the data contained in the table as a list that cannot be
	 * modified. The list returned by this method is made un-modifiable to
	 * prevent inadvertent changes to the data in the table.
	 * 
	 * @return The data contained in the table as a Returns the data
	 *         java.util.List that cannot be modified.
	 */
	public List<T> getData() {
		return Collections.unmodifiableList(data);
	}

	/**
	 * Sets the data to be displayed in the data table.
	 * 
	 * @param data
	 *            The data to be displayed in the table. If this parameter is
	 *            set to null, it will clear the existing table data.
	 */
	public synchronized void setData(Collection<T> data) {
		this.data.clear();
		if (data != null) {
			this.data.addAll(data);
		}
		fireTableDataChanged();
	}

	/**
	 * Adds a single row to the end of the data set.
	 * 
	 * @param row
	 *            The new row to be added.
	 */
	public synchronized void addRow(T row) {
		this.data.add(row);
		fireTableDataChanged();
	}

	/**
	 * Adds a single row at the specified location in the data set.
	 * 
	 * @param location
	 *            An int that is the specified location.
	 * @param row
	 *            The new row to be added.
	 */
	public synchronized void addRow(int location, T row) {
		this.data.add(location, row);
		fireTableDataChanged();
	}

	/**
	 * Adds multiple rows to the end of the data set.
	 * 
	 * @param rows
	 *            A java.util.Collection of rows to be added to the end of the
	 *            data set.
	 */
	public synchronized void addRows(Collection<T> rows) {
		if (rows != null && rows.size() > 0) {
			this.data.addAll(rows);
			fireTableDataChanged();
		}
	}

	/**
	 * Removes the specified row from the data table .
	 * 
	 * @param index
	 *            The index of the specified row.
	 * @return The data object that was removed.
	 */
	public synchronized T removeRow(int index) {
		T removed = this.data.remove(index);
		fireTableDataChanged();
		return removed;
	}

	/**
	 * Removes the specified item from the data table.
	 * 
	 * @param item
	 *            The item to be removed.
	 * @return The removed item, or null if the item is not in the table.
	 */
	public synchronized T removeRow(T item) {
		int index = indexOf(item);
		return index >= 0 ? removeRow(index) : null;
	}

	/**
	 * Removes all data from the data table.
	 */
	public synchronized void removeAllRows() {
		this.data.clear();
		fireTableDataChanged();
	}

	/**
	 * Returns the index of the first occurrence of the specified item in the
	 * data table.
	 * 
	 * @param item
	 *            The item to return the index for.
	 * @return The index of the specified item, or -1 if the item is not found.
	 */
	public int indexOf(T item) {
		if (this.data == null || this.data.isEmpty()){
			return -1;
		}
		
		return this.data.indexOf(item);
	}

	/**
	 * Returns a TableColumnModeltable column model that is based on the default
	 * table column model for the DataTableModel class. The
	 * TableColumnModelcolumn model returned by this method has the same number
	 * of columns in the same order and structure as the table column model in
	 * the DataTableModel. When a DataTable object is created, this method is used
	 * to create the TableColumnModel if one is not specified. This method may
	 * be overridden in order to provide customizations to the default column
	 * model, such as providing a default column width and/or adding column
	 * renderers and/ editors.
	 * 
	 * @return The TableColumnModel object.
	 */
	public TableColumnModel createDefaultTableColumnModel() {

		TableColumnModel result = new DefaultTableColumnModel();
		for (int i = 0; i < getColumnCount(); ++i) {
			TableColumn tc = new TableColumn(i);
			tc.setHeaderValue(getColumnName(i));
			result.addColumn(tc);
		}
		return result;
	}

	/**
	 * Defines how the data object managed by this table model is mapped
	 * to its columns when displayed in a row of the table. The getValueAt()
	 * method uses this method to retrieve table cell data.
	 * 
	 * Note: This method must be implemented by subclasses.
	 * 
	 * @param item The item to retrieve the value for.
	 * @param columnIndex The index of the column.
	 * @return The table column value calculated for the object
	 */
	protected abstract Object getColumnValue(T item, int columnIndex);

}
