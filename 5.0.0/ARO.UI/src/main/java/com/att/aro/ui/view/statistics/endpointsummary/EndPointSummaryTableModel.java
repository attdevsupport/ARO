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
package com.att.aro.ui.view.statistics.endpointsummary;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.ApplicationPacketSummary;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.util.Util;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;

/**
 * Represents the data table model for end point summary statistics. This class
 * implements the aro.commonui. DataTableModel class using ApplicationSelection.
 */
public class EndPointSummaryTableModel extends DataTableModel<ApplicationPacketSummary> {
	private static final long serialVersionUID = 1L;

	private enum ColumnKeys {
		endpointsummary_appname,
		endpointsummary_packets,
		endpointsummary_bytes,
	}
	private static final ColumnKeys[] columnKeysCollection = ColumnKeys.values();

	private enum ColumnHeaderKeys {
		statics_csvLine_seperator,
		statics_csvCell_seperator
	}

	private static final TabPanelCommon tabPanelCommon = new TabPanelCommon();
	TableColumnModel cols = null;

	/**
	 * Initializes a new instance of the EndPointSummaryTableModel class.
	 */
	public EndPointSummaryTableModel() {
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
			case endpointsummary_packets:
			case endpointsummary_bytes:
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
		NumberFormatRenderer bytesRenderer = new NumberFormatRenderer(
				NumberFormat.getIntegerInstance());

		col = cols.getColumn(ColumnKeys.endpointsummary_bytes.ordinal());
		col.setCellRenderer(bytesRenderer);

		return cols;
	}

	/**
	 * Returns a Cell values.
	 * 
	 * @return Object Cell value.
	 */
	@Override
	protected Object getColumnValue(ApplicationPacketSummary item, int columnIndex) {
		switch(columnKeysCollection[columnIndex]) {
			case endpointsummary_appname:
				return Util.getDefaultAppName(item.getAppName());
			case endpointsummary_packets:
				return item.getPacketCount();
			case endpointsummary_bytes:
				return item.getTotalBytes();
			default:
				return null;
		}
	}

	/**
	 * Returns a Column header name.
	 * 
	 * @return String column header name.
	 */
	@Override
	public String getColumnName(int col) {
		return tabPanelCommon.getText(columnKeysCollection[col]);
	}

	/**
	 * Method to write the end point summary per application into the csv file.
	 * 
	 * @throws IOException
	 */
	public FileWriter addEndPointSummaryPerAppTable(FileWriter writer,
			DataTable<ApplicationPacketSummary> table) throws IOException {
		final String lineSep = System.getProperty(tabPanelCommon.getText(
				ColumnHeaderKeys.statics_csvLine_seperator));

		// Write headers
		for (int i = 0; i < table.getColumnCount(); ++i) {
			if (i > 0) {
				writer.append(tabPanelCommon.getText(
						ColumnHeaderKeys.statics_csvCell_seperator));
			}
			writer.append(createCSVEntry(table.getColumnModel().getColumn(i).getHeaderValue()));
		}
		writer.append(lineSep);
		// Write data
		for (int i = 0; i < table.getRowCount(); ++i) {
			for (int j = 0; j < table.getColumnCount(); ++j) {
				if (j > 0) {
					writer.append(tabPanelCommon.getText(
							ColumnHeaderKeys.statics_csvCell_seperator));
				}
				writer.append(createCSVEntry(table.getValueAt(i, j)));
			}
			writer.append(lineSep);
		}
		return writer;
	}

	/**
	 * Changes the format of the table object.
	 */
	private String createCSVEntry(Object val) {
		StringBuffer writer = new StringBuffer();
		String str = val != null ? val.toString() : "";
		writer.append('"');
		for (char c : str.toCharArray()) {
			switch (c) {
			case '"':
				// Add an extra
				writer.append("\"\"");
				break;
			default:
				writer.append(c);
			}
		}
		writer.append('"');
		return writer.toString();
	}

	public void refresh(AROTraceData model) {
		Statistic statistic = model.getAnalyzerResult().getStatistic();
		if (statistic != null) {
			setData(statistic.getApplicationPacketSummary());
		}
	}
}
