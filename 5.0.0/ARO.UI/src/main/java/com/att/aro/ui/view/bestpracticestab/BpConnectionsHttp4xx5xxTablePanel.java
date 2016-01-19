/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.Color;
import java.util.Collection;

import javax.swing.JTable;

import com.att.aro.core.bestpractice.pojo.Http4xx5xxStatusResponseCodesEntry;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.bestpractice.Http4xx5xxStatusResponseCodesTableModel;

/**
 * @author Barry Nelson
 *
 */
public class BpConnectionsHttp4xx5xxTablePanel extends AbstractBpDetailTablePanel {

	private static final long serialVersionUID = 1L;

	int noOfRecords;
	
	public BpConnectionsHttp4xx5xxTablePanel() {
		super();
		
	}
	
	@Override
	void initTableModel() {
		tableModel = new Http4xx5xxStatusResponseCodesTableModel();
	}
	
	/**
	 * Sets the data for the Duplicate Content table.
	 * 
	 * @param data
	 *            - The data to be displayed in the Duplicate Content table.
	 */
	public void setData(Collection<Http4xx5xxStatusResponseCodesEntry> data) {
		if (!data.isEmpty()) {
			// System.out.println("got data, show table");
			setVisible(true);
		} else {
			// System.out.println("got NO data, hide table");
			setVisible(false);
		}

		setScrollSize(MINIMUM_ROWS);
		((Http4xx5xxStatusResponseCodesTableModel)tableModel).setData(data);
		autoSetZoomBtn();
	}

	/**
	 * Initializes and returns the RequestResponseTable.
	 */
	@SuppressWarnings("unchecked")
	public DataTable<Http4xx5xxStatusResponseCodesEntry> getContentTable() {
		if (contentTable == null) {
			contentTable = new DataTable<Http4xx5xxStatusResponseCodesEntry>(tableModel);
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
