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
import com.att.aro.model.Burst;

/**
 * Represents the data table model for burst collection information statistics.
 * This class implements the aro.commonui. DataTableModel class using
 * Burst.
 */
public class BurstCollectionInfoTableModel extends DataTableModel<Burst> {
	private static final long serialVersionUID = 1L;

	private static final int START_TIME_COL = 0;
	private static final int END_TIME_COL = 1;
	private static final int BYTES_COL = 2;
	private static final int PACKET_COUNT_COL = 3;
	private static final int TYPE_COL = 4;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String[] columns = { rb.getString("burstAnalysis.startTime"), rb.getString("burstAnalysis.elapsedTime"),
			rb.getString("burstAnalysis.bytes"), rb.getString("burstAnalysis.packetCount"), rb.getString("burstAnalysis.type") };

	TableColumnModel cols = null;

	/**
	 * Initializes a new instance of the BurstCollectionInfoTableModel class.
	 */
	public BurstCollectionInfoTableModel() {
		super(columns);
	}

	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            The index of the specified column.
	 * 
	 * @return A class representing the specified column.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case START_TIME_COL:
		case END_TIME_COL:
		case BYTES_COL:
		case PACKET_COUNT_COL:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}
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
		cols = super.createDefaultTableColumnModel();
		TableColumn col;
		NumberFormatRenderer renderer = new NumberFormatRenderer(new DecimalFormat("0.000"));
		NumberFormatRenderer bytesRenderer = new NumberFormatRenderer(
				NumberFormat.getIntegerInstance());

		col = cols.getColumn(START_TIME_COL);
		col.setCellRenderer(renderer);

		col = cols.getColumn(END_TIME_COL);
		col.setCellRenderer(renderer);

		col = cols.getColumn(BYTES_COL);
		col.setCellRenderer(bytesRenderer);

		col = cols.getColumn(PACKET_COUNT_COL);
		col.setCellRenderer(bytesRenderer);

		return cols;
	}

	/**
	 * Returns the column value for each cells. 
	 * 
	 * @param
	 * 		item A Burst object containing the column information.
	 *		columnIndex The index of the specified column.
	 *
	 *@return The table column value calculated for the object.
	 */
	@Override
	protected Object getColumnValue(Burst item, int columnIndex) {
		switch (columnIndex) {
		case TYPE_COL:
			return BurstAnalysisTableModel.getBurstCategoryString(item.getBurstCategory());
		case START_TIME_COL:
			return item.getBeginTime();
		case END_TIME_COL:
			return item.getElapsedTime();
		case BYTES_COL:
			return item.getBurstBytes();
		case PACKET_COUNT_COL:
			return item.getPackets().size();
		default:
			return null;
		}
	}

	/**
	 * Returns the column header name.
	 */
	@Override
	public String getColumnName(int col) {
		return columns[col];
	}
}
