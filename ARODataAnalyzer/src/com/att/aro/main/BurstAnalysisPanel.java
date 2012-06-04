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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.DataTable;
import com.att.aro.model.Burst;
import com.att.aro.model.BurstAnalysisInfo;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.TraceData;

/**
 * Represents a panel for displayings the burst analysis data in the Statistics
 * tab of the ARO Data Analyzer.
 */
public class BurstAnalysisPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JScrollPane scroll;
	private JScrollPane burstScroll;
	private BurstAnalysisTableModel tableModel = new BurstAnalysisTableModel();
	private BurstCollectionInfoTableModel burstTableModel = new BurstCollectionInfoTableModel();
	private DataTable<BurstAnalysisInfo> table;
	private DataTable<Burst> burstTable;
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD,
			16);
	private static final int HEADER_DATA_SPACING = 10;
	
	/**
	 * Initializes a new instance of the BurstAnalysisPanel class.
	 */
	public BurstAnalysisPanel() {
		super();
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		initialize();
	}

	/**
	 * Refreshes the content of the Burst Analysis panel with the specified
	 * trace data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysis) {
		tableModel.setData(analysis != null ? analysis.getBcAnalysis()
				.getBurstAnalysisInfo() : null);
		burstTableModel.setData(analysis != null ? analysis.getBcAnalysis()
				.getBurstCollection() : null);
		if (analysis == null || analysis.getProfile() == null)
			return;

		if (analysis.getProfile() instanceof ProfileLTE) {
			tableModel.changeLTECol();
		} else if (analysis.getProfile() instanceof Profile3G) {
			tableModel.change3GCol();
		}
	}

	/**
	 * Initializes the BurstAnalysisPanel.
	 */
	private void initialize() {
		this.setLayout(new VerticalLayout());
		JLabel headerLabel = new JLabel(rb.getString("burstAnalysis.title"));
		headerLabel.setFont(HEADER_FONT);
		this.add(headerLabel);
		this.add(getScroll());
		
		JPanel spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(),
				HEADER_DATA_SPACING));
		spacePanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		this.add(spacePanel);
		
		JLabel subHeaderLabel = new JLabel(rb.getString("burstAnalysis.individualBurst"));
		subHeaderLabel.setFont(HEADER_FONT);
		this.add(subHeaderLabel);
		this.add(getBurstScroll());
	}

	/**
	 * Returns the JScrollPane containing the burst analysis table.
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane(getTable());
			scroll.setPreferredSize(new Dimension(300, 200));
		}
		return scroll;
	}
	
	/**
	 * Returns the JScrollPane containing the burst analysis table.
	 */
	private JScrollPane getBurstScroll() {
		if (burstScroll == null) {
			burstScroll = new JScrollPane(getBurstTable());
			burstScroll.setPreferredSize(new Dimension(300, 150));
		}
		return burstScroll;
	}

	/**
	 * Returns a DataTable containing the burst analysis data.
	 * 
	 * @return A DataTable object containing the burst analysis data.
	 */
	public DataTable<BurstAnalysisInfo> getTable() {
		if (table == null) {
			table = new DataTable<BurstAnalysisInfo>(tableModel);
			table.setGridColor(Color.LIGHT_GRAY);
		}
		return table;
	}

	
	/**
	 * Returns a DataTable containing the burst analysis data.
	 * 
	 * @return A DataTable object containing the burst analysis data.
	 */
	public DataTable<Burst> getBurstTable() {
		if (burstTable == null) {
			burstTable = new DataTable<Burst>(burstTableModel);
			burstTable.setGridColor(Color.LIGHT_GRAY);
		}
		return burstTable;
	}

}
