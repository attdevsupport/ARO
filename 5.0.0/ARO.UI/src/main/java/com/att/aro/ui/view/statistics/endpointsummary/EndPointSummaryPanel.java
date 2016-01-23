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
package com.att.aro.ui.view.statistics.endpointsummary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.ApplicationPacketSummary;
import com.att.aro.core.packetanalysis.pojo.IPPacketSummary;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents a panel for displaying the EndPoint Summary Per Application and the End Point Summary 
 * Per IP Address tables in the Statistics tab of the ARO Data Analyzer. 
 */
public class EndPointSummaryPanel extends JSplitPane {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleHelper.getDefaultBundle();

	private JScrollPane scroll;
	private JScrollPane ipScroll;
	private EndPointSummaryTableModel tableModel = new EndPointSummaryTableModel();
	private IPEndPointSummaryTableModel ipTableModel = new IPEndPointSummaryTableModel();
	private DataTable<ApplicationPacketSummary> table;
	private DataTable<IPPacketSummary> ipTable;
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD, 16);

	private JPanel appSummaryPanel;
	private JPanel ipSummaryPanel;

	/**
	 * Initializes a new instance of the EndPointSummaryPanel class.
	 */
	public EndPointSummaryPanel() {
		super();
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setPreferredSize(new Dimension(700, 180));
		setResizeWeight(0.5);
		setLeftComponent(getApplicationSummaryPanel());
		setRightComponent(getIPSummaryPanel());
	}

	/**
	 * Refreshes the content of the EndPointSummaryPanel with the specified
	 * trace data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(AROTraceData model) {
		tableModel.refresh(model);
		ipTableModel.refresh(model);
	}

	/**
	 * Initializes Application summary panel.
	 */
	private JPanel getApplicationSummaryPanel() {
		if (appSummaryPanel == null) {
			JLabel headerLabel = new JLabel(rb.getString("endpointsummary.title"));
			headerLabel.setFont(HEADER_FONT);
			appSummaryPanel = new JPanel(new BorderLayout());
			appSummaryPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			appSummaryPanel.add(headerLabel, BorderLayout.NORTH);
			appSummaryPanel.add(getScroll(), BorderLayout.CENTER);
		}
		return appSummaryPanel;
	}

	/**
	 * Initializes Domain TCP Sessions Panel.
	 */
	private JPanel getIPSummaryPanel() {
		if (ipSummaryPanel == null) {
			JLabel ipHeaderLabel = new JLabel(rb.getString("endpointsummary.ip.title"));
			ipHeaderLabel.setFont(HEADER_FONT);
			ipSummaryPanel = new JPanel(new BorderLayout());
			ipSummaryPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			ipSummaryPanel.add(ipHeaderLabel, BorderLayout.NORTH);
			ipSummaryPanel.add(getIPScroll(), BorderLayout.CENTER);
		}
		return ipSummaryPanel;
	}

	/**
	 * Returns the JScrollPane containing the application end point summary
	 * table.
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane(getTable());
		}
		return scroll;
	}

	/**
	 * Returns the JScrollPane containing the IP end point summary table.
	 */
	private JScrollPane getIPScroll() {
		if (ipScroll == null) {
			ipScroll = new JScrollPane(getIPTable());
		}
		return ipScroll;
	}

	/**
	 * Returns a DataTable containing the end point summary per application data.
	 * 
	 * @return A DataTable object containing the end point summary per application data.
	 */
	public DataTable<ApplicationPacketSummary> getTable() {
		if (table == null) {
			table = new DataTable<ApplicationPacketSummary>(tableModel);
		}
		return table;
	}

	/**
	 * Returns a DataTable containing the end point summary per IP address data.
	 * 
	 * @return A DataTable object containing the end point summary per IP address data.
	 */
	public DataTable<IPPacketSummary> getIPTable() {
		if (ipTable == null) {
			ipTable = new DataTable<IPPacketSummary>(ipTableModel);
		}
		return ipTable;
	}

	/**
	 * @return the tableModel
	 */
	public EndPointSummaryTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * @return the ipTableModel
	 */
	public IPEndPointSummaryTableModel getIpTableModel() {
		return ipTableModel;
	}
}
