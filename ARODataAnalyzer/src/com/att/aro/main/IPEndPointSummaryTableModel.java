package com.att.aro.main;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.DataTable;
import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.NumberFormatRenderer;
import com.att.aro.model.IPPacketSummary;

/**
 * Represents the data table model for the end point summary statistics for IP
 * address. This class implements the aro.commonui. DataTableModel class using
 * ApplicationSelection.
 */
public class IPEndPointSummaryTableModel extends DataTableModel<IPPacketSummary> {
	private static final long serialVersionUID = 1L;

	private static final int APPNAME_COL = 0;
	private static final int PACKET_COL = 1;
	private static final int BYTES_COL = 2;
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String[] columns = { rb.getString("endpointsummary.ipAddress"),
			rb.getString("endpointsummary.packets"), rb.getString("endpointsummary.bytes") };

	TableColumnModel cols = null;

	/**
	 * Initializes a new instance of the IPEndPointSummaryTableModel class.
	 */
	public IPEndPointSummaryTableModel() {
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
	 * Returns the value of the specified item in the specified column.
	 * 
	 * * @param
	 * 		item The specified item.
			columnIndex The index of the specified column.
			
	 * @return An object containing specified cell value
	 */
	@Override
	protected Object getColumnValue(IPPacketSummary item, int columnIndex) {
		switch (columnIndex) {
		case APPNAME_COL:
			return item.getIPAddress().toString().substring(1);
		case PACKET_COL:
			return item.getPacketCount();
		case BYTES_COL:
			return item.getTotalBytes();
		default:
			return null;
		}
	}

	/**
	 * Returns a column header name.
	 * 
	 * @return A string that is the column header name.
	 */
	@Override
	public String getColumnName(int col) {
		return columns[col];
	}

	/**
	 * Method to write the end point summary per IP address into the csv file.
	 * 
	 * @throws IOException
	 */
	public FileWriter addEndPointSummaryPerIPTable(FileWriter writer,
			DataTable<IPPacketSummary> table) throws IOException {
		final String lineSep = System.getProperty(rb.getString("statics.csvLine.seperator"));
		// Write headers
		for (int i = 0; i < table.getColumnCount(); ++i) {
			if (i > 0) {
				writer.append(rb.getString("statics.csvCell.seperator"));
			}
			writer.append(createCSVEntry(table.getColumnModel().getColumn(i).getHeaderValue()));
		}
		writer.append(lineSep);
		// Write data
		for (int i = 0; i < table.getRowCount(); ++i) {
			for (int j = 0; j < table.getColumnCount(); ++j) {
				if (j > 0) {
					writer.append(rb.getString("statics.csvCell.seperator"));
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
}
