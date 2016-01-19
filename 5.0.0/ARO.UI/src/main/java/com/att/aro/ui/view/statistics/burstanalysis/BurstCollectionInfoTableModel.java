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

package com.att.aro.ui.view.statistics.burstanalysis;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;

/**
 * Represents the data table model for burst collection information statistics.
 * This class implements the aro.commonui. DataTableModel class using
 * Burst.
 */
public class BurstCollectionInfoTableModel extends DataTableModel<Burst> {
	private static final long serialVersionUID = 1L;

	private enum ColumnKeys {
		burstAnalysis_startTime,
		burstAnalysis_elapsedTime,
		burstAnalysis_bytes,
		burstAnalysis_packetCount,
		burstAnalysis_type
	}
	private static final ColumnKeys[] columnKeysCollection = ColumnKeys.values();

	private static final TabPanelCommon tabPanelCommon = new TabPanelCommon();
	TableColumnModel cols = null;

	/**
	 * Initializes a new instance of the BurstCollectionInfoTableModel class.
	 */
	public BurstCollectionInfoTableModel() {
		super(tabPanelCommon.getText(columnKeysCollection));
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
		switch(columnKeysCollection[columnIndex]) {
			case burstAnalysis_startTime:
			case burstAnalysis_elapsedTime:
			case burstAnalysis_bytes:
			case burstAnalysis_packetCount:
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

		col = cols.getColumn(ColumnKeys.burstAnalysis_startTime.ordinal());
		col.setCellRenderer(renderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_elapsedTime.ordinal());
		col.setCellRenderer(renderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_bytes.ordinal());
		col.setCellRenderer(bytesRenderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_packetCount.ordinal());
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
		ColumnKeys columnKey = columnKeysCollection[columnIndex];
		switch(columnKey) {
			case burstAnalysis_type:
				return item.getBurstCategory().getBurstTypeDescription();
			case burstAnalysis_startTime:
				return item.getBeginTime();
			case burstAnalysis_elapsedTime:
				return item.getElapsedTime();
			case burstAnalysis_bytes:
				return item.getBurstBytes();
			case burstAnalysis_packetCount:
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
		return tabPanelCommon.getText(columnKeysCollection[col]);
	}

	public void refresh(AROTraceData model) {
		setData(model.getAnalyzerResult().getBurstcollectionAnalysisData().
				getBurstCollection());
	}
}
