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

package com.att.aro.diagnostics;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.NumberFormatRenderer;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.TCPSession;

/**
 * Represents the table model used to display the TCP Flows data.
 */
public class TCPFlowsTableModel extends DataTableModel<TCPSession> {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(TCPFlowsTableModel.class.getName());

	private static final int TIME_COL = 0;
	private static final int APP_COL = 1;
	private static final int DOMAIN_COL = 2;
	private static final int LOCALPORT_COL = 3;
	private static final int REMOTEPORT_COL = 4;
	private static final int PACKETCOUNT_COL = 5;
	private static final int TCP_UDP_COL = 6;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String hostPortSeparator = rb.getString("tcp.hostPortSeparator");
	private static final String stringListSeparator = rb.getString("stringListSeparator");
	private static final String[] columns = { rb.getString("tcp.time"), rb.getString("tcp.app"),
			rb.getString("tcp.domain"), rb.getString("tcp.local"), rb.getString("tcp.remote"),
			rb.getString("tcp.packetcount"),rb.getString("tcp.protocol") };

	private Set<TCPSession> highlighted = new HashSet<TCPSession>();

	private TableColumnModel cols = null;
	private RequestResponseDetailsPanel rrPanel;

	/**
	 * Constructor
	 * @param rrPanel request response details panel
	 */
	public TCPFlowsTableModel() {
		super(columns);
		this.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent arg0) {
				if (arg0.getType() == TableModelEvent.INSERT || arg0.getType() == TableModelEvent.DELETE) {
					highlighted.clear();
				}
			}
		});
	}
	
	/**
	 * Stores a reference to RequestResponseDetailsPanel.
	 * @param rrPanel RequestResponseDetailsPanel
	 */
	void setRrDetailsPanel(RequestResponseDetailsPanel rrPanel) {
		this.rrPanel = rrPanel;
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
	if (cols == null) {
			cols = super.createDefaultTableColumnModel();
			TableColumn col = cols.getColumn(TIME_COL);
			col.setCellRenderer(new NumberFormatRenderer(new DecimalFormat(
					"0.000")));

			TableColumn appCol = cols.getColumn(APP_COL);
			DefaultTableCellRenderer r = new DefaultTableCellRenderer();
			r.setHorizontalAlignment(SwingConstants.RIGHT);
			appCol.setCellRenderer(r);

		}
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
		case TIME_COL:
			return Double.class;
		case PACKETCOUNT_COL:
			return Integer.class;
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
	protected Object getColumnValue(TCPSession item, int columnIndex) {
		switch (columnIndex) {
		case TIME_COL:
			if (!item.isUDP()) {
				return item.getPackets().get(0).getTimeStamp();
			} else {
				return item.getUDPPackets().get(0).getTimeStamp();
			}
		case APP_COL:
			if(!item.isUDP()) {
				TableColumn appCol = cols.getColumn(APP_COL);
				int width = appCol.getWidth();
	
				Iterator<String> it = item.getAppNames().iterator();
				if (!it.hasNext()) {
					return rb.getString("aro.unknownApp");
				}
	
				//not pretty, but intended to dynamically determine how
				// many characters will fit in a cell based on column width in
				// pixels, then substring app name and prefix with ...
				// start with 16% and add another .5% for each 70 pixels
				double basePct = .16;
				int units = width / 70;
				if (units > 0) {
					basePct = basePct + (units * .005);
				}
				
				double shortLength = width * basePct;
				
				String app = it.next();
				if (!it.hasNext()) {
					int appStrLength = app.length();
					
					if (appStrLength > shortLength + 2) {					
						app = "..."
								+ app.substring(appStrLength - (int) shortLength);
					}
					return app;
				} else {
					StringBuffer sb = new StringBuffer(app);
					while (it.hasNext()) {
						sb.append(stringListSeparator);
						sb.append(it.next());
					}
					
					int appStrLength = sb.length();
					
					if (appStrLength > shortLength + 2) {					
						return "..."
								+ sb.substring(appStrLength - (int) shortLength);
					} else {
						return sb.toString();
					}
				}
			} else {
				return item.getUDPPackets().get(0).getAppName();
			}
		case DOMAIN_COL:
			if(!item.isUDP()) {
				return item.getDomainName();
			} else {
				return item.getDomainName();
			}
		case LOCALPORT_COL:
			if(!item.isUDP()) {
				return rb.getString("tcp.localhost") + hostPortSeparator + item.getLocalPort();
			} else {
				return rb.getString("tcp.localhost") + hostPortSeparator + item.getLocalPort();
			}
			
		case REMOTEPORT_COL:
			if(!item.isUDP()) {
				return item.getRemoteIP().getHostAddress() + hostPortSeparator + item.getRemotePort();
			} else {
				return item.getRemoteIP().getHostAddress() + hostPortSeparator + item.getRemotePort();
			}
		
		case PACKETCOUNT_COL:
			if(!item.isUDP()) {
				return item.getPackets().size();
			} else {
				return item.getUDPPackets().size();
			}
		case TCP_UDP_COL:
			if(!item.isUDP()) {
				return rb.getString("tcp.tcp");
			} else {
				return rb.getString("tcp.udp");
			}
						
		default:
			return null;
		}
	}
}
