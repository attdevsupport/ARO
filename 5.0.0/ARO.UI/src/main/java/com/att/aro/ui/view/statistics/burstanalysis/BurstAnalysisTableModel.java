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

import com.att.aro.core.packetanalysis.pojo.BurstAnalysisInfo;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.exception.AROUIPanelException;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.model.NumberFormatRenderer;

/**
 * Represents the data table model for burst analysis statistics. This class
 * implements the aro.commonui. DataTableModel class using BurstAnalysisInfo.
 */
public class BurstAnalysisTableModel extends DataTableModel<BurstAnalysisInfo> {
	private static final long serialVersionUID = 1L;

	private enum ColumnKeys {
		burstAnalysis_type,
		burstAnalysis_bytes,
		burstAnalysis_bytesPct,
		burstAnalysis_energy,
		burstAnalysis_energyPct,
		burstAnalysis_dch,
		burstAnalysis_dchPct,
		burstAnalysis_jpkb
	}
	private static final ColumnKeys[] columnKeysCollection = ColumnKeys.values();

	private enum ColumnHeaderKeys {
		burstAnalysis_lteCr,
		burstAnalysis_lteCrPct,
		burstAnalysis_wifiActive,
		burstAnalysis_wifiActivePct
	}

	private static final TabPanelCommon tabPanelCommon = new TabPanelCommon();
	private TableColumnModel cols = null;
	private RrcStateMachineType type;

	/**
	 * Initializes a new instance of the BurstAnalysisTableModel class.
	 */
	public BurstAnalysisTableModel() {
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
			case burstAnalysis_bytes:
			case burstAnalysis_bytesPct:
			case burstAnalysis_energy:
			case burstAnalysis_energyPct:
			case burstAnalysis_dch:
			case burstAnalysis_dchPct:
			case burstAnalysis_jpkb:
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
		NumberFormatRenderer renderer = new NumberFormatRenderer(
				new DecimalFormat("0.00"));
		NumberFormatRenderer pctRenderer = new NumberFormatRenderer();
		NumberFormatRenderer bytesRenderer = new NumberFormatRenderer(
				NumberFormat.getIntegerInstance());
		NumberFormatRenderer jpkbRenderer = new NumberFormatRenderer(
				new DecimalFormat("0.000"));

		col = cols.getColumn(ColumnKeys.burstAnalysis_bytes.ordinal());
		col.setCellRenderer(bytesRenderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_bytesPct.ordinal());
		col.setCellRenderer(pctRenderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_energy.ordinal());
		col.setCellRenderer(renderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_energyPct.ordinal());
		col.setCellRenderer(pctRenderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_dch.ordinal());
		col.setCellRenderer(jpkbRenderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_dchPct.ordinal());
		col.setCellRenderer(pctRenderer);

		col = cols.getColumn(ColumnKeys.burstAnalysis_jpkb.ordinal());
		col.setCellRenderer(jpkbRenderer);

		return cols;
	}

	/**
	 * This is the one method that must be implemented by subclasses. This method defines 
	 * how the data object managed by this table model is mapped to its columns when 
	 * displayed in a row of the table. The getValueAt() method uses this method to retrieve 
	 * table cell data. 
	 * 
	 * @param
	 * 		item A BurstAnalysisInfo object containing the column information.
			columnIndex The index of the specified column.
	 *		
	 * @return An object containing the table column value. 
	 */
	@Override
	protected Object getColumnValue(BurstAnalysisInfo item, int columnIndex) {
		ColumnKeys columnKey = columnKeysCollection[columnIndex];
		switch (columnKey) {
			case burstAnalysis_type:
				return item.getCategory().getBurstTypeDescription();
			case burstAnalysis_bytes:
				return item.getPayload();
			case burstAnalysis_bytesPct:
				return item.getPayloadPct();
			case burstAnalysis_energy:
				return item.getEnergy();
			case burstAnalysis_energyPct:
				return item.getEnergyPct();
			case burstAnalysis_dch:
				return item.getRRCActiveTime();
			case burstAnalysis_dchPct:
				return item.getRRCActivePercentage();
			case burstAnalysis_jpkb:
				return item.getJpkb() != null? item.getJpkb() : 0.000;
			default:
				return null;
		}
	}

	@Override
	public String getColumnName(int col) {
		return tabPanelCommon.getText(columnKeysCollection[col]);
	}

	/**
	 * Changes Column name.
	 * 
	 * @param colId
	 *            - Id of the column name which need to change
	 * @param colValue
	 *            - New name of the column
	 */
	private void changeColHeader(int colId, String colValue) {
		TableColumn col = cols.getColumn(colId);
		col.setHeaderValue(colValue);
	}

	/**
	 * Changes the columns and headers in the table to match an LTE device profile.
	 */
	public void changeLTECol() {
		changeColHeader(ColumnKeys.burstAnalysis_dch.ordinal(),
				tabPanelCommon.getText(ColumnHeaderKeys.burstAnalysis_lteCr));
		changeColHeader(ColumnKeys.burstAnalysis_dchPct.ordinal(),
				tabPanelCommon.getText(ColumnHeaderKeys.burstAnalysis_lteCrPct));
	}

	/**
	 * Changes the columns and headers in the table to match a 3G device profile.
	 */
	public void change3GCol() {
		changeColHeader(ColumnKeys.burstAnalysis_dch.ordinal(),
				tabPanelCommon.getText(ColumnKeys.burstAnalysis_dch));
		changeColHeader(ColumnKeys.burstAnalysis_dchPct.ordinal(),
				tabPanelCommon.getText(ColumnKeys.burstAnalysis_dchPct));
	}
	/**
	 * Changes the columns and headers in the table to match a WiFi device profile.
	 */
	public void changeWiFiCol() {
		changeColHeader(ColumnKeys.burstAnalysis_dch.ordinal(),
				tabPanelCommon.getText(ColumnHeaderKeys.burstAnalysis_wifiActive));
		changeColHeader(ColumnKeys.burstAnalysis_dchPct.ordinal(),
				tabPanelCommon.getText(ColumnHeaderKeys.burstAnalysis_wifiActivePct));
	}

	private void refreshHeader(RrcStateMachineType type) {
		if (type != this.type) {
			this.type = type;
			switch(type) {
				case Type3G:
					change3GCol();
					break;
				case LTE:
					changeLTECol();
					break;
				case WiFi:
					changeWiFiCol();
					break;
				default:
					throw new AROUIPanelException("type " + type.name() + " not handled");
			}
		}
	}

	public void refresh(AROTraceData model) {
		RrcStateMachineType type = model.getAnalyzerResult().getStatemachine().getType();
		refreshHeader(type);

		setData(model.getAnalyzerResult().getBurstcollectionAnalysisData().
				getBurstAnalysisInfo());
	}
}

