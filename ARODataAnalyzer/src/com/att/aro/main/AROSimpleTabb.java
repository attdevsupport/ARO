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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.commonui.DataTable;
import com.att.aro.model.CacheEntry;
import com.att.aro.model.DomainTCPSessions;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData;

/**
 * Represents the Overview tab screen.
 */
public class AROSimpleTabb extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private ApplicationResourceOptimizer parent;

	private JScrollPane jAccessedDomainsPanel;
	private JScrollPane jAccessedDomainsExpandedPanel;
	private DeviceNetworkProfilePanel deviceNetworkProfilePanel;
	private FileTypesChartPanel fileTypesChart;
	private JLabel labelAccessedDomains;
	private CacheAnalysisPanel jDuplicatesPanel;
	private JLabel labelAccessedExpandedDomains;
	private JSplitPane jBottomSplitPane;
	private TraceOverviewPanel traceOverviewPanel;
	private ProperSessionTerminationPanel sessionTermPanel;

	// Components for Accessed Simple Domain List scroll table
	private JPanel simpleDomainsPanel;
	private SimpleDomainTableModel jSimpleDomainTableModel = new SimpleDomainTableModel();
	private DataTable<DomainTCPSessions> jSimpleDomainTable;

	// Components for Accessed Expanded Domain List scroll table
	private JPanel expandedDomainsPanel;
	private ExpandedDomainTableModel jExtendedDomainTableModel = new ExpandedDomainTableModel();
	private DataTable<TCPSession> jExtendedDomainTable;
	private JSplitPane jTablesSplitPane;

	/**
	 * Initializes a new instance of the AROSimpleTabb class using the specified
	 * instance of the ApplicationResourceOptimizer as the parent window.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public AROSimpleTabb(ApplicationResourceOptimizer parent) {
		super(new BorderLayout());

		this.parent = parent;
		JPanel traceAnalysisAndSessionTermPanel = new JPanel();
		traceAnalysisAndSessionTermPanel.setLayout(new BorderLayout());
		traceAnalysisAndSessionTermPanel.add(getTraceOverviewPanel(), BorderLayout.NORTH);
		traceAnalysisAndSessionTermPanel.add(getProperSessionTermChartPanel(), BorderLayout.SOUTH);

		JPanel chartsPanel = new JPanel();
		chartsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chartsPanel.setLayout(new BorderLayout());
		chartsPanel.add(traceAnalysisAndSessionTermPanel, BorderLayout.EAST);
		chartsPanel.add(getFileTypesChartPanel(), BorderLayout.WEST);

		this.add(getDeviceNetworkProfilePanel(), BorderLayout.NORTH);
		JPanel chartAndTablePanel = new JPanel();
		chartAndTablePanel.setLayout(new BorderLayout());
		chartAndTablePanel.add(chartsPanel, BorderLayout.NORTH);
		chartAndTablePanel.add(getJTablesSplitPane());
		this.add(chartAndTablePanel, BorderLayout.CENTER);

	}

	/**
	 * Initializes and returns the Split Pane that contains the Duplicate
	 * Contents and the Domains table so the the Duplicate contents table can be
	 * resized on mouse drag.
	 * 
	 */
	private JSplitPane getJTablesSplitPane() {
		if (jTablesSplitPane == null) {
			jTablesSplitPane = new JSplitPane();
			jTablesSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jTablesSplitPane.setResizeWeight(0.5);
			jTablesSplitPane.setTopComponent(getJDuplicatesPanel());
			jTablesSplitPane.setBottomComponent(createTablesPanel());

		}
		return jTablesSplitPane;
	}

	/**
	 * Initializes and returns the Split Pane that contains the tables at the
	 * bottom.
	 */
	private JSplitPane createTablesPanel() {
		if (jBottomSplitPane == null) {
			jBottomSplitPane = new JSplitPane();
			jBottomSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			jBottomSplitPane.setResizeWeight(0.5);
			jBottomSplitPane.setLeftComponent(getSimpleDomainsPanel());
			jBottomSplitPane.setRightComponent(getExpandedDomainsPanel());
			jBottomSplitPane.setPreferredSize(new Dimension(100, 180));
		}
		return jBottomSplitPane;
	}

	/**
	 * Initializes Domain TCP Sessions Panel.
	 */
	private JPanel getSimpleDomainsPanel() {
		if (simpleDomainsPanel == null) {
			simpleDomainsPanel = new JPanel(new BorderLayout());
			simpleDomainsPanel.add(getJLabelAccessedDomains(), BorderLayout.NORTH);
			simpleDomainsPanel.add(getjAccessedDomainsPanel(), BorderLayout.CENTER);
		}
		return simpleDomainsPanel;
	}

	/**
	 * Initializes the Accessed Domains panel.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getExpandedDomainsPanel() {
		if (expandedDomainsPanel == null) {
			expandedDomainsPanel = new JPanel();
			expandedDomainsPanel.setLayout(new BorderLayout());
			expandedDomainsPanel.add(getJLabelAccessedExpandedDomains(), BorderLayout.NORTH);
			expandedDomainsPanel.add(getjAccessedDomainsExpandedPanel(), BorderLayout.CENTER);
		}
		return expandedDomainsPanel;
	}

	/**
	 * Initializes getJLabelAccessedDomains
	 * 
	 * @return javax.swing.JLabel
	 */
	private JLabel getJLabelAccessedDomains() {
		if (labelAccessedDomains == null) {
			labelAccessedDomains = new JLabel(rb.getString("simple.domain.title"), JLabel.CENTER);
		}
		return labelAccessedDomains;
	}

	/**
	 * Initializes returns the Accessed Domains Label.
	 */
	private JLabel getJLabelAccessedExpandedDomains() {
		if (labelAccessedExpandedDomains == null) {
			labelAccessedExpandedDomains = new JLabel(rb.getString("expanded.title"), JLabel.CENTER);
		}
		return labelAccessedExpandedDomains;
	}

	/**
	 * Initializes and returns the Device network profile panel.
	 */
	private DeviceNetworkProfilePanel getDeviceNetworkProfilePanel() {
		if (deviceNetworkProfilePanel == null) {
			deviceNetworkProfilePanel = new DeviceNetworkProfilePanel();
		}
		return deviceNetworkProfilePanel;
	}

	/**
	 * Initializes and returns the Panel that contains the File Types Chart.
	 */
	public FileTypesChartPanel getFileTypesChartPanel() {
		if (fileTypesChart == null) {
			fileTypesChart = new FileTypesChartPanel();
		}
		return fileTypesChart;
	}

	/**
	 * Initializes and returns the Panel that contains the Connection Statistics
	 * Chart.
	 */
	public ProperSessionTerminationPanel getProperSessionTermChartPanel() {
		if (sessionTermPanel == null) {
			sessionTermPanel = new ProperSessionTerminationPanel();
		}
		return sessionTermPanel;
	}

	/**
	 * Initializes and returns the Duplicate Contents Panel.
	 * 
	 * @return CacheAnalysisPanel The Duplicate Contents Panel.
	 */
	private CacheAnalysisPanel getJDuplicatesPanel() {
		if (jDuplicatesPanel == null) {
			jDuplicatesPanel = new CacheAnalysisPanel(parent);
		}
		return jDuplicatesPanel;
	}

	/**
	 * Initializes and returns the Domain TCP Sessions Panel.
	 */
	private JScrollPane getjAccessedDomainsPanel() {
		if (jAccessedDomainsPanel == null) {
			jAccessedDomainsPanel = new JScrollPane(getSimpleDomainTable());
		}
		return jAccessedDomainsPanel;
	}

	/**
	 * Initializes and returns the Accessed Domains panel.
	 */
	private JScrollPane getjAccessedDomainsExpandedPanel() {
		if (jAccessedDomainsExpandedPanel == null) {
			jAccessedDomainsExpandedPanel = new JScrollPane(getExtendedDomainTable());
		}
		return jAccessedDomainsExpandedPanel;
	}

	/**
	 * Initializes and returns the Trace Overview panel.
	 */
	public TraceOverviewPanel getTraceOverviewPanel() {
		if (traceOverviewPanel == null) {
			traceOverviewPanel = new TraceOverviewPanel();
		}
		return traceOverviewPanel;
	}

	/**
	 * Refreshes the content of the Overview tab with the specified trace
	 * data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		List<TCPSession> tcpSession;
		List<CacheEntry> dupContent;
		if (analysisData != null) {
			tcpSession = analysisData.getTcpSessions();
			dupContent = analysisData.getCacheAnalysis().getDuplicateContentWithOriginals();
		} else {
			tcpSession = Collections.emptyList();
			dupContent = Collections.emptyList();
		}

		jSimpleDomainTableModel.setData(DomainTCPSessions.extractDomainTCPSessions(tcpSession));
		getJDuplicatesPanel().setData(dupContent);
		deviceNetworkProfilePanel.refresh(analysisData);
		getFileTypesChartPanel().setAnalysisData(analysisData);
		getTraceOverviewPanel().setAnalysisData(analysisData);
		getProperSessionTermChartPanel().setAnalysisData(analysisData);
	}

	/**
	 * Initializes and returns the Domain TCP Sessions Table .
	 */
	private JTable getSimpleDomainTable() {
		if (jSimpleDomainTable == null) {
			jSimpleDomainTable = new DataTable<DomainTCPSessions>(jSimpleDomainTableModel);
			jSimpleDomainTable.setAutoCreateRowSorter(true);
			jSimpleDomainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jSimpleDomainTable.setGridColor(Color.LIGHT_GRAY);
			jSimpleDomainTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent arg0) {
							DomainTCPSessions tcp = jSimpleDomainTable.getSelectedItem();
							if (tcp != null) {
								jExtendedDomainTableModel.setData(tcp.getSessions());
							} else {
								jExtendedDomainTableModel.removeAllRows();
							}
						}
					});
		}
		return jSimpleDomainTable;
	}

	/**
	 * Initializes and returns the Accessed Domain Table.
	 */
	private JTable getExtendedDomainTable() {
		if (jExtendedDomainTable == null) {
			jExtendedDomainTable = new DataTable<TCPSession>(jExtendedDomainTableModel);
			jExtendedDomainTable.setAutoCreateRowSorter(true);
			jExtendedDomainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jExtendedDomainTable.setGridColor(Color.LIGHT_GRAY);
			jExtendedDomainTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					final TCPSession tcp = jExtendedDomainTable.getSelectedItem();
					if (e.getClickCount() == 2 && tcp != null) {
						parent.displayAdvancedTab();

						// Make sure the tab is fully displayed before changing
						// selection
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								parent.getAroAdvancedTab().setHighlightedTCP(tcp);
							}

						});
					}
				}
			});
		}
		return jExtendedDomainTable;
	}

}
