package com.att.aro.ui.view.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.mvc.IAROView;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.IAROPrintable;
import com.att.aro.ui.commonui.ImagePanel;
import com.att.aro.ui.commonui.RoundedBorder;
import com.att.aro.ui.commonui.TabPanelJScrollPane;
import com.att.aro.ui.commonui.UIComponent;
import com.att.aro.ui.exception.AROUIPanelException;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.menu.tools.ExportReport;
import com.att.aro.ui.view.statistics.burstanalysis.BurstAnalysisPanel;
import com.att.aro.ui.view.statistics.endpointsummary.EndPointSummaryPanel;
import com.att.aro.ui.view.statistics.energy.EnergyModelStatistics3GPanel;
import com.att.aro.ui.view.statistics.energy.EnergyModelStatisticsLTEPanel;
import com.att.aro.ui.view.statistics.energy.EnergyModelStatisticsWiFiPanel;
import com.att.aro.ui.view.statistics.statemachine.RRCStateMachineSimulationPanel3G;
import com.att.aro.ui.view.statistics.statemachine.RRCStateMachineSimulationPanelLTE;
import com.att.aro.ui.view.statistics.statemachine.RRCStateMachineSimulationPanelWiFi;
import com.att.aro.view.images.Images;

public class StatisticsTab extends TabPanelJScrollPane implements IAROPrintable {
	private static final long serialVersionUID = 1L;
	private JPanel container;
	private JPanel mainPanel;
	private DateTraceAppDetailPanel dateTraceAppDetailPanel;
	private TCPSessionStatisticsPanel tcpSessionStatistics;
	private EndPointSummaryPanel endPointSummaryPanel;
	private RRCStateMachineSimulationPanel3G rrcStateMachineSimulationPanel3G;
	private RRCStateMachineSimulationPanelLTE rrcStateMachineSimulationPanelLTE;
	private RRCStateMachineSimulationPanelWiFi rrcStateMachineSimulationPanelWiFi;
	private BurstAnalysisPanel burstAnalysisPanel;
	private HTTPCacheStatistics httpCacheStatistics;
	private EnergyModelStatistics3GPanel energyModelStatistics3GPanel;
	private EnergyModelStatisticsLTEPanel energyModelStatisticsLTEPanel;
	private EnergyModelStatisticsWiFiPanel energyModelStatisticsWiFiPanel;
	private RrcStateMachineType rrcStateMachineType = null;

	private JLabel exportBtn;
	private AROTraceData model = null;
	private final IAROView parent;

	private enum DialogItem {
		table_export,
		chart_tooltip_export,
		fileChooser_desc_html,
		fileChooser_contentType_html,
		fileChooser_Title,
		fileChooser_Save,
		exportall_errorFileOpen,
		menu_tools_export_error,
	}

	/**
	 * Create the panel.
	 */
	public StatisticsTab(IAROView parent) {
		this.parent = parent;

		container = new JPanel(new BorderLayout());
		container.setBackground(Color.white);
		
		ImagePanel panel = new ImagePanel(null);
		panel.setLayout(new GridBagLayout());
		Insets insets = new Insets(10, 10, 10, 10);

		panel.add(layoutDataPanel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		container.add(panel, BorderLayout.CENTER);

		this.setViewportView(container);
		this.getVerticalScrollBar().setUnitIncrement(10);
	}

	/**
	 * Adds the various Panels for the Statistics tab.
	 * 
	 * @return the mainPanel The JPanel containing the entire screen.
	 */
	public JPanel layoutDataPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new GridBagLayout());
			mainPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			Insets insets = new Insets(10, 10, 10, 10);
			mainPanel.setOpaque(false);
			mainPanel.setBorder(new RoundedBorder(new Insets(10, 10, 10, 10), Color.WHITE));

			// load graphic header and Export button
			ImagePanel logoHeader = UIComponent.getInstance().getLogoHeader("statistics.title");
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.setOpaque(false);
			buttonPanel.add(getExportBtn(), BorderLayout.EAST);
//			logoHeader.add(getJsonExportBtn(), BorderLayout.WEST);
			logoHeader.add(buttonPanel, BorderLayout.EAST);
			container.add(logoHeader, BorderLayout.NORTH);

			int gridy = -1;

			//top summary
			dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
			mainPanel.add(dateTraceAppDetailPanel, new GridBagConstraints(0, ++gridy,
					1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
			layoutDivider(++gridy, insets);

			//TCP(Session) Statistics
			tcpSessionStatistics = new TCPSessionStatisticsPanel();
			mainPanel.add(tcpSessionStatistics, new GridBagConstraints(0, ++gridy,
					1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
			layoutDivider(++gridy, insets);

//			//Trace Score
//			traceScorePanel = new TraceScorePanel();
//			mainPanel.add(traceScorePanel, new GridBagConstraints(0, ++gridy,
//					1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//						insets, 0, 0));
//			layoutDivider(++gridy, insets);
			++gridy; ++gridy;
			
			//End Point Summary
			endPointSummaryPanel = new EndPointSummaryPanel();
			mainPanel.add(endPointSummaryPanel, new GridBagConstraints(0, ++gridy,
					1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						insets, 0, 0));
			layoutDivider(++gridy, insets);
//gridy 5
			//Radio Resource Control State Machine Simulation (default to LTE)
			rrcStateMachineSimulationPanelLTE = new RRCStateMachineSimulationPanelLTE();
			mainPanel.add(rrcStateMachineSimulationPanelLTE, new GridBagConstraints(0, ++gridy, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			layoutDivider(++gridy, insets);
//gridy 7
			//Burst Analysis
			burstAnalysisPanel = new BurstAnalysisPanel();
			mainPanel.add(burstAnalysisPanel, new GridBagConstraints(0, ++gridy, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			layoutDivider(++gridy, insets);
//gridy 9
			//HTTP Cache Statistics
			httpCacheStatistics = new HTTPCacheStatistics();
			mainPanel.add(httpCacheStatistics, new GridBagConstraints(0, ++gridy, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			layoutDivider(++gridy, insets);
// gridy 11
			//Energy Efficiency Simulation (default to LTE)
			energyModelStatisticsLTEPanel = new EnergyModelStatisticsLTEPanel();
			mainPanel.add(energyModelStatisticsLTEPanel, new GridBagConstraints(0, ++gridy, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
			rrcStateMachineType = RrcStateMachineType.LTE;
			
//			mainPanel.add(energyModelStatistics3GPanel, new GridBagConstraints(
//					0, 7, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
//						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));

		}
		return mainPanel;
	}

	private void layoutDivider(int gridy, Insets insets) {
		mainPanel.add(new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE),
				new GridBagConstraints(0, gridy, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));
	}


	/**
	 * 3G, LTE, and WiFi render different attributes for the RRC sub-panel on the statics tab.
	 * This takes care of ensuring the right ones are used.
	 * 
	 * @param rrcStateMachineType
	 */
	private void layoutRrcStateMachineSimulator(RrcStateMachineType rrcStateMachineType) {
		switch(rrcStateMachineType) {
			case Type3G:
				rrcStateMachineSimulationPanel3G = new RRCStateMachineSimulationPanel3G();
				if (rrcStateMachineSimulationPanelLTE != null) {
					mainPanel.remove(rrcStateMachineSimulationPanelLTE);
				}
				if (rrcStateMachineSimulationPanelWiFi != null) {
					mainPanel.remove(rrcStateMachineSimulationPanelWiFi);
				}
				rrcStateMachineSimulationPanelLTE = null;
				rrcStateMachineSimulationPanelWiFi = null;
				mainPanel.add(rrcStateMachineSimulationPanel3G, new GridBagConstraints(
					0, 8, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
				break;
			case LTE:
				rrcStateMachineSimulationPanelLTE = new RRCStateMachineSimulationPanelLTE();
				if (rrcStateMachineSimulationPanel3G != null) {
					mainPanel.remove(rrcStateMachineSimulationPanel3G);
				}
				if (rrcStateMachineSimulationPanelWiFi != null) {
					mainPanel.remove(rrcStateMachineSimulationPanelWiFi);
				}
				rrcStateMachineSimulationPanel3G = null;
				rrcStateMachineSimulationPanelWiFi = null;
				mainPanel.add(rrcStateMachineSimulationPanelLTE, new GridBagConstraints(
						0, 8, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
				break;
			case WiFi:
				rrcStateMachineSimulationPanelWiFi = new RRCStateMachineSimulationPanelWiFi();
				if (rrcStateMachineSimulationPanel3G != null) {
					mainPanel.remove(rrcStateMachineSimulationPanel3G);
				}
				if (rrcStateMachineSimulationPanelLTE != null) {
					mainPanel.remove(rrcStateMachineSimulationPanelLTE);
				}
				rrcStateMachineSimulationPanel3G = null;
				rrcStateMachineSimulationPanelLTE = null;
				mainPanel.add(rrcStateMachineSimulationPanelWiFi, new GridBagConstraints(
						0, 8, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
				break;
			default:
				throw new AROUIPanelException("Energy render implementation not available.");
		}
	}

	/**
	 * Creates a export button and handles the export functionality.
	 */
	private JLabel getExportBtn() {
		if (exportBtn == null) {
			// exportBtn = new JLabel(Images.EXPORT_BTN.getIcon());
			exportBtn = new JLabel(ResourceBundleHelper.getMessageString(
					DialogItem.table_export), Images.EXPORT_BTN.getIcon(), JLabel.CENTER);
			exportBtn.setVerticalTextPosition(JLabel.BOTTOM);
			exportBtn.setHorizontalTextPosition(JLabel.CENTER);
			exportBtn.setForeground(Color.white);
			exportBtn.setEnabled(false);
			exportBtn.setToolTipText(ResourceBundleHelper.getMessageString(
					DialogItem.chart_tooltip_export));
			exportBtn.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (!exportBtn.isEnabled()) {
						return;
					}
					if (model != null) {
						ExportReport exportHtml = new ExportReport(parent, false, ResourceBundleHelper.getMessageString("menu.tools.export.error"));
						exportHtml.execute();
					}
				}
				
				@Override
	            public void mouseEntered(MouseEvent e) {
					exportBtn.setForeground(Color.GRAY );
					
				}
				@Override
	            public void mouseExited(MouseEvent e) {
					exportBtn.setForeground(Color.WHITE);
	            }    
			});
		}
		return exportBtn;
	}


	/**
	 * 3G, LTE, and WiFi render different attributes for the energy sub-panel on the statics tab.
	 * This takes care of ensuring the right ones are used.
	 * 
	 * @param rrcStateMachineType
	 */
	private void layoutEnergyEffecencySimulation(RrcStateMachineType rrcStateMachineType) {
		switch(rrcStateMachineType) {
			case Type3G:
				energyModelStatistics3GPanel = new EnergyModelStatistics3GPanel();
				if (energyModelStatisticsLTEPanel != null) {
					mainPanel.remove(energyModelStatisticsLTEPanel);
				}				
				if (energyModelStatisticsWiFiPanel != null) {
					mainPanel.remove(energyModelStatisticsWiFiPanel);
				}
				energyModelStatisticsLTEPanel = null;
				energyModelStatisticsWiFiPanel = null;
				mainPanel.add(energyModelStatistics3GPanel, new GridBagConstraints(
					0, 14, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
				break;
			case LTE:
				energyModelStatisticsLTEPanel = new EnergyModelStatisticsLTEPanel();
				if (energyModelStatistics3GPanel != null) {
					mainPanel.remove(energyModelStatistics3GPanel);
				}
				if (energyModelStatisticsWiFiPanel != null) {
					mainPanel.remove(energyModelStatisticsWiFiPanel);
				}
				energyModelStatistics3GPanel = null;
				energyModelStatisticsWiFiPanel = null;
				mainPanel.add(energyModelStatisticsLTEPanel, new GridBagConstraints(
						0, 14, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
				break;
			case WiFi:
				energyModelStatisticsWiFiPanel = new EnergyModelStatisticsWiFiPanel();
				if (energyModelStatistics3GPanel != null) {
					mainPanel.remove(energyModelStatistics3GPanel);
				}
				if (energyModelStatisticsLTEPanel != null) {
					mainPanel.remove(energyModelStatisticsLTEPanel);
				}	
				energyModelStatistics3GPanel = null;
				energyModelStatisticsLTEPanel = null;
				mainPanel.add(energyModelStatisticsWiFiPanel, new GridBagConstraints(
						0, 14, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
				break;
			default:
				throw new AROUIPanelException("Energy render implementation not available.");
//			case Type3G:
//				energyEfficiencySimulation3G = new EnergyEfficiencySimulation3G();
//				if (energyEfficiencySimulationLTE != null) {
//					mainPanel.remove(energyEfficiencySimulationLTE);
//				}
//				energyEfficiencySimulationLTE = null;
//				mainPanel.add(energyEfficiencySimulation3G, new GridBagConstraints(
//					0, 7, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
//						GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
//				break;
//			case LTE:
//				energyEfficiencySimulationLTE = new EnergyEfficiencySimulationLTE();
//				if (energyEfficiencySimulation3G != null) {
//					mainPanel.remove(energyEfficiencySimulation3G);
//				}
//				energyEfficiencySimulation3G = null;
//				mainPanel.add(energyEfficiencySimulationLTE, new GridBagConstraints(
//						0, 7, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
//							GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
//				break;
//			case WiFi:
//				// do nothing
//			default:
//				throw new AROUIPanelException("Energy render implementation not available.");
				
		}
	}

	public void refresh(AROTraceData model) {
		//Energy Efficiency Simulation & Rrc State Machine Simulation
		PacketAnalyzerResult analyzerResult = model.getAnalyzerResult();
		if (rrcStateMachineType != analyzerResult.getStatemachine().getType()) {
			rrcStateMachineType = analyzerResult.getStatemachine().getType();
			layoutRrcStateMachineSimulator(rrcStateMachineType);
			layoutEnergyEffecencySimulation(rrcStateMachineType);
		}

		dateTraceAppDetailPanel.refresh(model);
		tcpSessionStatistics.refresh(model);
//		traceScorePanel.refresh(model);
		endPointSummaryPanel.refresh(model);
		burstAnalysisPanel.refresh(model);
		httpCacheStatistics.refresh(model);
		switch(rrcStateMachineType) {
			case Type3G:
				rrcStateMachineSimulationPanel3G.refresh(model);
				energyModelStatistics3GPanel.refresh(model);
				break;
			case LTE:
				rrcStateMachineSimulationPanelLTE.refresh(model);
				energyModelStatisticsLTEPanel.refresh(model);
				break;
			case WiFi:
				rrcStateMachineSimulationPanelWiFi.refresh(model);
				energyModelStatisticsWiFiPanel.refresh(model);
				break;
			default:
				throw new AROUIPanelException("Unhandled handling for rrc type " +
						rrcStateMachineType.name());
		}

		this.model = model;
		exportBtn.setEnabled(model != null);
	}

	@Override
	public JPanel getPrintablePanel() {
		return container;
	}

}//end class
