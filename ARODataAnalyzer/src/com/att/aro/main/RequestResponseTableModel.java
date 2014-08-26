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
import com.att.aro.model.HttpRequestResponseInfo;

/**
 * Represents the table model for the Request/Response View table on the
 * Diagnostic tab.
 */
public class RequestResponseTableModel extends DataTableModel<HttpRequestResponseInfo> {
	private static final long serialVersionUID = 1L;
	private static final ResourceBundle RB = ResourceBundleManager.getDefaultBundle();
	// TODO Put in resource bundles
	private static final String[] COLUMNS = { RB.getString("rrview.time"),
			RB.getString("rrview.direction"), RB.getString("rrview.reqtye"),
			RB.getString("rrview.hostname"), RB.getString("rrview.objectname"),
			RB.getString("rrview.contentlen"), RB.getString("rrview.http.compression") };

	private static final int TIME_COL = 0;
	private static final int DIR_COL = 1;
	private static final int REQ_TYPE_STATUS_COL = 2;
	private static final int HOST_NAME_CONTENT_TYPE_COL = 3;
	private static final int OBJ_NAME_CONTENT_LENGTH = 4;
	private static final int ON_WIRE_CONTENT_LENGTH = 5;
	private static final int HTTP_COMPRESSION = 6;

	/**
	 * Initializes a new instance of the RequestResponseDetailsPanel class.
	 */
	public RequestResponseTableModel() {
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
		TableColumn col = cols.getColumn(TIME_COL);
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
		case TIME_COL:
			return Double.class;
		case ON_WIRE_CONTENT_LENGTH:
			return Long.class;
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
	protected Object getColumnValue(HttpRequestResponseInfo item, int columnIndex) {
		if (item.getDirection() != null) {
			switch (columnIndex) {
			case TIME_COL:
				return item.getTimeStamp();
			case DIR_COL:
				return item.getDirection();
			case REQ_TYPE_STATUS_COL:
				if (item.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
					String type = item.getRequestType();
					return type != null ? type : RB.getString("rrview.unknownType");
				} else {
					return item.getStatusCode();
				}
			case HOST_NAME_CONTENT_TYPE_COL:
				if (item.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
					return item.getHostName();
				} else {
					return item.getContentType();
				}
			case OBJ_NAME_CONTENT_LENGTH:
				if (item.getDirection() == HttpRequestResponseInfo.Direction.REQUEST) {
					return item.getObjName();
				} else {
					return item.getContentLength();
				}
			case ON_WIRE_CONTENT_LENGTH:
				return item.getActualByteCount();
			case HTTP_COMPRESSION:
				return item.getHttpCompression();
			}
		}
		return null;
	}
}
