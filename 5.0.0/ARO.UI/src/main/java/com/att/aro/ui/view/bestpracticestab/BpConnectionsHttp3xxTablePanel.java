/**
 * 
 */
package com.att.aro.ui.view.bestpracticestab;

import java.awt.Color;
import java.util.Collection;

import javax.swing.JTable;

import com.att.aro.core.bestpractice.pojo.HttpCode3xxEntry;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.bestpractice.HttpCode3xxEntryTableModel;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class BpConnectionsHttp3xxTablePanel extends AbstractBpDetailTablePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int noOfRecords;
	
	public BpConnectionsHttp3xxTablePanel(){
		super();
	}

	@Override
	void initTableModel() {
		tableModel = new HttpCode3xxEntryTableModel();
	}
	
	/**
	 * Sets the data for the Duplicate Content table.
	 * 
	 * @param data
	 *            - The data to be displayed in the Duplicate Content table.
	 */
	public void setData(Collection<HttpCode3xxEntry> data) {
		if (!data.isEmpty()) {
			// System.out.println("got data, show table");
			setVisible(true);
		} else {
			// System.out.println("got NO data, hide table");
			setVisible(false);
		}

		setScrollSize(MINIMUM_ROWS);
		((HttpCode3xxEntryTableModel)tableModel).setData(data);
		autoSetZoomBtn();
	}
	
	/**
	 * Initializes and returns the RequestResponseTable.
	 */
	@SuppressWarnings("unchecked")
	public DataTable<HttpCode3xxEntry> getContentTable() {
		if (contentTable == null) {
			contentTable = new DataTable<HttpCode3xxEntry>(tableModel);
			contentTable.setAutoCreateRowSorter(true);
			contentTable.setGridColor(Color.LIGHT_GRAY);
			contentTable.setRowHeight(ROW_HEIGHT);
			contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			//TODO Add listener
		}

		return contentTable;
	}
	
	@Override
	public void refresh(AROTraceData analyzerResult) {
		// TODO Auto-generated method stub
		
	}
}
