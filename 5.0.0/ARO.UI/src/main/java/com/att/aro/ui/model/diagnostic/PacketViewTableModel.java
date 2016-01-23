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
package com.att.aro.ui.model.diagnostic;

import java.text.DecimalFormat;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class PacketViewTableModel extends DataTableModel<PacketInfo>  {
	
	private static final long serialVersionUID = 6258570416342367879L;

	public PacketViewTableModel() {
		super(columnNames);
		// TODO Auto-generated constructor stub
	}
	
	private static String[] columnNames = { 
			ResourceBundleHelper.getMessageString("packet.id"),
			ResourceBundleHelper.getMessageString("packet.time"), 
			ResourceBundleHelper.getMessageString("packet.direction"),
			ResourceBundleHelper.getMessageString("packet.type"), 
			ResourceBundleHelper.getMessageString("packet.length"),
			ResourceBundleHelper.getMessageString("packet.flags") };
 

	private static final int ID_COL = 0;
	private static final int TIME_COL = 1;
	private static final int DIR_COL = 2;
	private static final int TYPE_COL = 3;
	private static final int PAYLOAD_COL = 4;
	private static final int FLAGS_COL = 5;
	
	
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
		col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat("0.000000")));
		return cols;
	}
		

	/**
	 * Returns a class representing the specified column. This method is primarily used to
	 * sort numeric columns.
	 * 
	 * @param columnIndex The index of the specified column.
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 * 
	 * @return A class representing the specified column.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case ID_COL:
		case TIME_COL:
		case PAYLOAD_COL:
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
	protected Object getColumnValue(PacketInfo item, int columnIndex) {
		switch (columnIndex) {
		case ID_COL:
			return item.getPacketId();
		case TIME_COL:
			return item.getTimeStamp();
		case DIR_COL:
			return item.getDir();
		case TYPE_COL:
			return ResourceBundleHelper.getEnumString(item.getTcpInfo());
		case PAYLOAD_COL:
			return item.getPacket().getPayloadLen();
		case FLAGS_COL:
			return item.getTcpFlagString();
		}
		return null;
	}
}
