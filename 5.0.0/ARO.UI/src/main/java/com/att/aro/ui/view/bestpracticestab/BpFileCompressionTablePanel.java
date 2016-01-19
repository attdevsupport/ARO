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

import com.att.aro.core.bestpractice.pojo.TextFileCompressionEntry;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.bestpractice.FileCompressionTableModel;

/**
 * @author Barry Nelson
 *
 */
public class BpFileCompressionTablePanel extends AbstractBpDetailTablePanel {

	private static final long serialVersionUID = 1L;

	int noOfRecords;
	
	public BpFileCompressionTablePanel() {
		super();
		
	}
	
	@Override
	void initTableModel() {
		tableModel = new FileCompressionTableModel();
	}
	
	/**
	 * Sets the data for the Duplicate Content table.
	 * 
	 * @param data
	 *            - The data to be displayed in the Duplicate Content table.
	 */
	public void setData(Collection<TextFileCompressionEntry> data) {
		
		setVisible(!data.isEmpty());

		setScrollSize(MINIMUM_ROWS);
		((FileCompressionTableModel)tableModel).setData(data);
		autoSetZoomBtn();
	}

	/**
	 * Initializes and returns the RequestResponseTable.
	 */
	@SuppressWarnings("unchecked")
	public DataTable<TextFileCompressionEntry> getContentTable() {
		if (contentTable == null) {
			contentTable = new DataTable<TextFileCompressionEntry>(tableModel);
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
