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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.CheckBoxRenderer;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;


public class TCPUDPFlowsTableModel extends DataTableModel<Session> {
	private static final long serialVersionUID = 3691872179254714817L;
	
	private static final int TIME_COL = 0;
	private static final int CHECKBOX_COL = 1;
	private static final int APP_COL = 2;
	private static final int DOMAIN_COL = 3;
	private static final int LOCALPORT_COL = 4;
	private static final int REMOTEIP_COL = 5;
	private static final int REMOTEPORT_COL = 6;
	private static final int BYTE_COUNT_COL = 7;
	private static final int PACKETCOUNT_COL = 8;
	private static final int TCP_UDP_COL = 9;
	private static final String HOSTPORTSEPERATOR = ResourceBundleHelper.getMessageString("tcp.hostPortSeparator");
	private static final String STRINGLISTSEPARATOR = ResourceBundleHelper.getMessageString("stringListSeparator");

	private  static final String[] COLUMNNAMES = { 
		ResourceBundleHelper.getMessageString("tcp.time"),"", 
		ResourceBundleHelper.getMessageString("tcp.app"),
		ResourceBundleHelper.getMessageString("tcp.domain"), 
		ResourceBundleHelper.getMessageString("tcp.local"), 
		ResourceBundleHelper.getMessageString("tcp.remote"), 
		ResourceBundleHelper.getMessageString("tcp.remoteport"),
		ResourceBundleHelper.getMessageString("tcp.bytecount"),
		ResourceBundleHelper.getMessageString("tcp.packetcount"),
		ResourceBundleHelper.getMessageString("tcp.protocol") 
	};
	private Set<Session> highlighted = new HashSet<Session>();
	private TableColumnModel cols;	
	private Map<Session,Boolean> checkboxMap;// side load and track...
	public Map<Session, Boolean> getCheckboxMap() {
		return checkboxMap;
	}

	
	/**
	 * Initializes a new instance of the BurstAnalysisTableModel class.
	 */
	public TCPUDPFlowsTableModel() {
		super(COLUMNNAMES);
		checkboxMap  = new HashMap<Session,Boolean>();
		this.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent arg0) {
				if (arg0.getType() == TableModelEvent.INSERT || arg0.getType() == TableModelEvent.DELETE) {
					highlighted.clear();
				}
			}
		});

	}
	
	public void refresh(AROTraceData aroTraceData) {
		setData(aroTraceData.getAnalyzerResult().getSessionlist());
		getCheckboxMap().clear();
		for(int index = 0 ;index < aroTraceData.getAnalyzerResult().getSessionlist().size();index++){
			getCheckboxMap().put(aroTraceData.getAnalyzerResult().getSessionlist().get(index),new Boolean(true));
		}
		
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
		case CHECKBOX_COL:
			return Boolean.class;
		case REMOTEPORT_COL:
			return Integer.class;
		case BYTE_COUNT_COL:
			return Integer.class;
		default:
			return super.getColumnClass(columnIndex);
		}
	}
		
	
	public boolean isCellEditable(int row, int col) {
		
        return col == CHECKBOX_COL;
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
			col.setMaxWidth(60);

			TableColumn appCol = cols.getColumn(APP_COL);
			DefaultTableCellRenderer defaultTableCR = new DefaultTableCellRenderer();
			appCol.setCellRenderer(defaultTableCR);
			
			//Re-sizing the column
			appCol = cols.getColumn(BYTE_COUNT_COL);
			appCol.setMaxWidth(75); 
			
			appCol = cols.getColumn(PACKETCOUNT_COL);
			appCol.setMaxWidth(75); 
			
			appCol = cols.getColumn(TCP_UDP_COL);
			appCol.setMaxWidth(75); 
			
			//Adding checkbox to column greg story
			appCol = cols.getColumn(CHECKBOX_COL);
			appCol.setCellRenderer(new CheckBoxRenderer());
			appCol.setMaxWidth(35);
		}
		return cols;
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
	protected Object getColumnValue(Session item, int columnIndex) {
		
		switch (columnIndex) {
		case TIME_COL:
			if(item.isUDP()){
				return item.getUDPPackets().get(0).getTimeStamp();
			}else{
				return item.getPackets().get(0).getTimeStamp();
			}
		case CHECKBOX_COL:
			return getCheckboxMap().get(item);
		case APP_COL:
			if(item.isUDP()){
				return item.getUDPPackets().get(0).getAppName();
			}else{
				TableColumn appCol = cols.getColumn(APP_COL);
				int width = appCol.getWidth();
	
				Iterator<String> iterator = item.getAppNames().iterator();
				if (!iterator.hasNext()) {
					return ResourceBundleHelper.getMessageString("aro.unknownApp");
				}
	
				//not pretty, but intended to dynamically determine how
				// many characters will fit in a cell based on column width in
				// pixels, then substring app name and prefix with ...
				// start with 16% and add another .5% for each 70 pixels
				double basePct = .16;
				int units = width / 70;
				if (units > 0) {
					basePct = basePct + units * .005;
				}
				
				double shortLength = width * basePct;				
				String app = iterator.next();
				if(iterator.hasNext()){
					StringBuffer sbuffer = new StringBuffer(app);
					while (iterator.hasNext()) {
						sbuffer.append(STRINGLISTSEPARATOR);
						sbuffer.append(iterator.next());
					}
					
					int appStrLength = sbuffer.length();
					
					if (appStrLength > shortLength + 2) {					
						return "..."
								+ sbuffer.substring(appStrLength - (int) shortLength);
					} else {
						return sbuffer.toString();
					}

				}else{
					
					int appStrLength = app.length();					
					if (appStrLength > shortLength + 2) {					
						app = "..."
								+ app.substring(appStrLength - (int) shortLength);
					}
					return app;
				}

			}
		case DOMAIN_COL:

			return item.getDomainName();
			
		case LOCALPORT_COL:
			
			return ResourceBundleHelper.getMessageString("tcp.localhost") + HOSTPORTSEPERATOR + item.getLocalPort();

		case REMOTEIP_COL:
			
			return item.getRemoteIP().getHostAddress();
			
		case REMOTEPORT_COL:
			return item.getRemotePort();
			
		case BYTE_COUNT_COL:
			return item.getBytesTransferred();
			
		case PACKETCOUNT_COL:
			if(item.isUDP()){
				return item.getUDPPackets().size();
			}else{
				return item.getPackets().size();
			}
		case TCP_UDP_COL:
			if(item.isUDP()){
				return ResourceBundleHelper.getMessageString("tcp.udp");
			}else{
				return ResourceBundleHelper.getMessageString("tcp.tcp");
			}
						
		default:
			return null;
		}
	}
	/**
	 * updata the datamodel with data 
	 */
	public void setValueAt(Object value, int row, int col) {
		if(col == CHECKBOX_COL){
			Session item = getValueAt(row);
			if(value instanceof Boolean){
				boolean checkBoxValue = (Boolean) value;
				if(checkboxMap.containsKey(item)){
					checkboxMap.put(item, checkBoxValue);
				}
			}
			
			fireTableCellUpdated(row, col);
		}	
       
    }

}
