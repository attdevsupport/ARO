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
import com.att.aro.model.BurstAnalysisInfo;
import com.att.aro.model.BurstCategory;

/**
 * Represents the data table model for burst analysis statistics. This class
 * implements the aro.commonui. DataTableModel class using BurstAnalysisInfo.
 */
public class BurstAnalysisTableModel extends DataTableModel<BurstAnalysisInfo> {
	private static final long serialVersionUID = 1L;

	private static final int TYPE_COL = 0;
	private static final int BYTES_COL = 1;
	private static final int BYTES_PCT_COL = 2;
	private static final int ENERGY_COL = 3;
	private static final int ENERGY_PCT_COL = 4;
	private static final int RRC_ACT_COL = 5;
	private static final int RRC_ACT_PCT_COL = 6;
	private static final int JPKB_COL = 7;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String[] columns = {
			rb.getString("burstAnalysis.type"),
			rb.getString("burstAnalysis.bytes"),
			rb.getString("burstAnalysis.bytesPct"),
			rb.getString("burstAnalysis.energy"),
			rb.getString("burstAnalysis.energyPct"),
			rb.getString("burstAnalysis.dch"),
			rb.getString("burstAnalysis.dchPct"),
			rb.getString("burstAnalysis.jpkb") };

	TableColumnModel cols = null;

	/**
	 * Initializes a new instance of the BurstAnalysisTableModel class.
	 */
	public BurstAnalysisTableModel() {
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
		case BYTES_COL:
		case BYTES_PCT_COL:
		case ENERGY_COL:
		case ENERGY_PCT_COL:
		case RRC_ACT_COL:
		case RRC_ACT_PCT_COL:
		case JPKB_COL:
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

		col = cols.getColumn(BYTES_COL);
		col.setCellRenderer(bytesRenderer);

		col = cols.getColumn(BYTES_PCT_COL);
		col.setCellRenderer(pctRenderer);

		col = cols.getColumn(ENERGY_COL);
		col.setCellRenderer(renderer);

		col = cols.getColumn(ENERGY_PCT_COL);
		col.setCellRenderer(pctRenderer);

		col = cols.getColumn(RRC_ACT_COL);
		col.setCellRenderer(jpkbRenderer);

		col = cols.getColumn(RRC_ACT_PCT_COL);
		col.setCellRenderer(pctRenderer);

		col = cols.getColumn(JPKB_COL);
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
		switch (columnIndex) {
		case TYPE_COL:
			return getBurstCategoryString(item.getCategory());
		case BYTES_COL:
			return item.getPayload();
		case BYTES_PCT_COL:
			return item.getPayloadPct();
		case ENERGY_COL:
			return item.getEnergy();
		case ENERGY_PCT_COL:
			return item.getEnergyPct();
		case RRC_ACT_COL:
			return item.getRRCActiveTime();
		case RRC_ACT_PCT_COL:
			return item.getRRCActivePercentage();
		case JPKB_COL:
			return item.getJpkb() != null? item.getJpkb() : 0.000;
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int col) {
		return columns[col];
	}

	/**
	 * Returns a string describing the burst type that correspond to the
	 * specified burst category.
	 * 
	 * @param category
	 *            - The burst category.
	 * 
	 * @return A string describing the burst type
	 * 
	 * @see BurstCategory
	 */
	public static String getBurstCategoryString(BurstCategory category) {
		switch (category) {
		case BURSTCAT_PROTOCOL:
			return rb.getString("burst.type.TcpControl");
		case BURSTCAT_LOSS:
			return rb.getString("burst.type.TcpLossRecover");
		case BURSTCAT_USER:
			return rb.getString("burst.type.UserInput");
		case BURSTCAT_SCREEN_ROTATION:
			return rb.getString("burst.type.ScreenRotation");
		case BURSTCAT_CLIENT:
			return rb.getString("burst.type.App");
		case BURSTCAT_SERVER:
			return rb.getString("burst.type.SvrNetDelay");
		case BURSTCAT_BKG:
			return rb.getString("burst.type.NonTarget");
		case BURSTCAT_LONG:
			return rb.getString("burst.type.LargeBurst");
		case BURSTCAT_PERIODICAL:
			return rb.getString("burst.type.Periodical");
		case BURSTCAT_UNKNOWN:
			return rb.getString("burst.type.Unknown");
		case BURSTCAT_USERDEF1:
			return rb.getString("burst.type.Userdef.1");
		case BURSTCAT_USERDEF2:
			return rb.getString("burst.type.Userdef.2");
		case BURSTCAT_USERDEF3:
			return rb.getString("burst.type.Userdef.3");
		default:
			return rb.getString("burst.type.error");
		}
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
		changeColHeader(RRC_ACT_COL, rb.getString("burstAnalysis.lteCr"));
		changeColHeader(RRC_ACT_PCT_COL, rb.getString("burstAnalysis.lteCrPct"));
	}

	/**
	 * Changes the columns and headers in the table to match a 3G device profile.
	 */
	public void change3GCol() {
		changeColHeader(RRC_ACT_COL, rb.getString("burstAnalysis.dch"));
		changeColHeader(RRC_ACT_PCT_COL, rb.getString("burstAnalysis.dchPct"));
	}
	/**
	 * Changes the columns and headers in the table to match a WiFi device profile.
	 */
	public void changeWiFiCol() {
		changeColHeader(RRC_ACT_COL, rb.getString("burstAnalysis.wifiActive"));
		changeColHeader(RRC_ACT_PCT_COL, rb.getString("burstAnalysis.wifiActivePct"));
	}
}

