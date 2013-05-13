package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.DataTable;
import com.att.aro.model.ApplicationPacketSummary;
import com.att.aro.model.IPPacketSummary;
import com.att.aro.model.TraceData;

/**
 * Represents a panel for displaying the EndPoint Summary Per Application and the End Point Summary 
 * Per IP Address tables in the Statistics tab of the ARO Data Analyzer. 
 */
public class EndPointSummaryPanel extends JSplitPane {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private JScrollPane scroll;
	private JScrollPane ipScroll;
	private ApplicationEndPointSummaryTableModel tableModel = new ApplicationEndPointSummaryTableModel();
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
	public void refresh(TraceData.Analysis analysis) {
		if (analysis != null) {
			tableModel.setData(analysis.getApplicationPacketSummary());
			ipTableModel.setData(analysis.getIpPacketSummary());
		} else {
			tableModel.setData(null);
			ipTableModel.setData(null);
		}

		if (analysis == null || analysis.getProfile() == null)
			return;
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
	public ApplicationEndPointSummaryTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * @return the ipTableModel
	 */
	public IPEndPointSummaryTableModel getIpTableModel() {
		return ipTableModel;
	}
}
