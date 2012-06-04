package com.att.aro.main;

import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.NumberFormatRenderer;
import com.att.aro.model.ApplicationPacketSummary;

/**
 * Represents the data table model for end point summary statistics. This class
 * implements the aro.commonui. DataTableModel class using ApplicationSelection.
 */
public class ApplicationEndPointSummaryTableModel extends DataTableModel<ApplicationPacketSummary> {
	private static final long serialVersionUID = 1L;

	private static final int APPNAME_COL = 0;
	private static final int PACKET_COL = 1;
	private static final int BYTES_COL = 2;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String[] columns = {
			rb.getString("endpointsummary.appname"),
			rb.getString("endpointsummary.packets"),
			rb.getString("endpointsummary.bytes") };

	TableColumnModel cols = null;

	/**
	 * Initializes a new instance of the EndPointSummaryTableModel class.
	 */
	public ApplicationEndPointSummaryTableModel() {
		super(columns);
	}

	/**
	 * Returns a class representing the specified column. This method is
	 * primarily used to sort numeric columns.
	 * 
	 * @param columnIndex
	 *            – The index of the specified column.
	 * 
	 * @return A class representing the specified column.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case PACKET_COL:
		case BYTES_COL:
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
		
		col = cols.getColumn(BYTES_COL);
		col.setCellRenderer(bytesRenderer);
		
		return cols;
	}

	/**
	 * Returns a Cell values.
	 * @return Object Cell value.
	 */
	@Override
	protected Object getColumnValue(ApplicationPacketSummary item, int columnIndex) {
		switch (columnIndex) {
		case APPNAME_COL:
			return item.getAppName() != null ? item.getAppName() : rb.getString("aro.unknownApp");
		case PACKET_COL:
			return item.getPacketCount();
		case BYTES_COL:
			return item.getTotalBytes();
		default:
			return null;
		}
	}

	/**
	 * Returns a Column header name.
	 * @return String column header name.
	 */
	@Override
	public String getColumnName(int col) {
		return columns[col];
	}	
}
