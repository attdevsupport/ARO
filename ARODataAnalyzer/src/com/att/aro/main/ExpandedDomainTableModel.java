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
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.NumberFormatRenderer;
import com.att.aro.model.PacketInfo;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TCPSession.Termination;

/**
 * Represents the table model for displaying the expanded domain TCP session
 * data. This class implements the aro.commonui.DataTableModel class using
 * TCPSession objects.
 */
public class ExpandedDomainTableModel extends DataTableModel<TCPSession> {
	private static final long serialVersionUID = 1L;

	private static final int SESSION_TIME = 0;
	private static final int EXTERNAL_IP = 1;
	private static final int LOCAL_PORT = 2;
	private static final int CONN_LEN = 3;
	private static final int BYTES = 4;
	private static final int TIME_GAP = 5;
	private static final int CLOSED_CONN = 6;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String[] columns = { rb.getString("expanded.session.time"),
			rb.getString("expanded.external"), rb.getString("expanded.local"),
			rb.getString("expanded.connection.length"), rb.getString("expanded.bytes"),
			rb.getString("expanded.time.gap"), rb.getString("expanded.domain.name") };

	/**
	 * Initializes a new instance of the ExpandedDomainTableModel class.
	 */
	public ExpandedDomainTableModel() {
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

		NumberFormatRenderer nFFormat = new NumberFormatRenderer(new DecimalFormat("0.000"));

		col = cols.getColumn(SESSION_TIME);
		col.setCellRenderer(nFFormat);

		col = cols.getColumn(CONN_LEN);
		col.setCellRenderer(new NumberFormatRenderer());

		col = cols.getColumn(BYTES);
		col.setCellRenderer(new NumberFormatRenderer(NumberFormat.getIntegerInstance()));

		col = cols.getColumn(TIME_GAP);
		col.setCellRenderer(nFFormat);

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
		case SESSION_TIME:
		case CONN_LEN:
		case BYTES:
		case TIME_GAP:
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
	 * 		item A TCPSession object containing the column information.
			columnIndex The index of the specified column.
	 *		
	 * @return An object containing the table column value. 
	 */
	@Override
	protected Object getColumnValue(TCPSession item, int columnIndex) {
		switch (columnIndex) {
		case SESSION_TIME:
			return item.getSessionStartTime();
		case EXTERNAL_IP:
			return item.getRemoteIP().getHostAddress();
		case LOCAL_PORT:
			return item.getLocalPort();
		case CONN_LEN:
			return item.getSessionEndTime() - item.getSessionStartTime();
		case BYTES:
			return item.getBytesTransferred();
		case TIME_GAP:
			Termination status = item.getSessionTermination();
			return status != null ? status.getSessionTerminationDelay() : null;
		case CLOSED_CONN:
			Termination packetStatus = item.getSessionTermination();
			if (packetStatus != null) {
				PacketInfo packet = packetStatus.getPacket();
				switch (packet.getDir()) {
				case UPLINK:
					return rb.getString("expanded.domain.name.client");
				case DOWNLINK:
					return rb.getString("expanded.domain.name.server");
				default:
					return null;
				}
			} else {
				return null;
			}
		default:
			return null;
		}
	}
}
