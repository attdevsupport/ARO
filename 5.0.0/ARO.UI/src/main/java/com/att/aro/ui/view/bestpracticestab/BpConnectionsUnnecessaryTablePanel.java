/**
 * 
 */
package com.att.aro.ui.view.bestpracticestab;

import java.awt.Color;
import java.util.Collection;

import javax.swing.JTable;

import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionEntry;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.bestpractice.UnnecessaryConnectionTableModel;

/**
 * @author Harikrishna Yaramachu
 *
 */
public class BpConnectionsUnnecessaryTablePanel extends AbstractBpDetailTablePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BpConnectionsUnnecessaryTablePanel(){
		super();
	}
	
	@Override
	void initTableModel() {
		tableModel = new UnnecessaryConnectionTableModel();
	}
	
	/**
	 * Sets the data for the Duplicate Content table.
	 * 
	 * @param data
	 *            - The data to be displayed in the Duplicate Content table.
	 */
	public void setData(Collection<UnnecessaryConnectionEntry> data) {
		if (!data.isEmpty()) {
			// System.out.println("got data, show table");
			setVisible(true);
		} else {
			// System.out.println("got NO data, hide table");
			setVisible(false);
		}

		setScrollSize(MINIMUM_ROWS);
		((UnnecessaryConnectionTableModel)tableModel).setData(data);
		autoSetZoomBtn();
	}
	
	/**
	 * Initializes and returns the RequestResponseTable.
	 */
	@SuppressWarnings("unchecked")
	public DataTable<UnnecessaryConnectionEntry> getContentTable() {
		if (contentTable == null) {
			contentTable = new DataTable<UnnecessaryConnectionEntry>(tableModel);
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
