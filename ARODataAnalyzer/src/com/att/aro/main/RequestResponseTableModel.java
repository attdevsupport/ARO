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
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	// TODO Put in resource bundles
	private static final String[] columns = { rb.getString("rrview.time"),
			rb.getString("rrview.direction"), rb.getString("rrview.reqtye"),
			rb.getString("rrview.hostname"), rb.getString("rrview.objectname"),
			rb.getString("rrview.contentlen") };

	private static final int TIME_COL = 0;
	private static final int DIR_COL = 1;
	private static final int REQ_TYPE_STATUS_COL = 2;
	private static final int HOST_NAME_CONTENT_TYPE_COL = 3;
	private static final int OBJ_NAME_CONTENT_LENGTH = 4;
	private static final int ON_WIRE_CONTENT_LENGTH = 5;

	/**
	 * Initializes a new instance of the RequestResponseDetailsPanel class.
	 */
	public RequestResponseTableModel() {
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
		TableColumn col = cols.getColumn(TIME_COL);
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000")));
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
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}

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
					return type != null ? type : rb.getString("rrview.unknownType");
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
			}
		}
		return null;
	}
}
