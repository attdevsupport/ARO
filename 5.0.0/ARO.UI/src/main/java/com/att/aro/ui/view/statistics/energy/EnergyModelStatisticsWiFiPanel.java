package com.att.aro.ui.view.statistics.energy;

import java.awt.GridLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.RrcStateMachineWiFi;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Displays the portion of the Energy Consumption Simulation section of the Statistics 
 * Tab information page that apply to a WiFi Energy Model.
 */
public class EnergyModelStatisticsWiFiPanel extends EnergyModelStatisticsPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleHelper.getDefaultBundle();
	private static final String units = rb.getString("energy.units");

	private JLabel wifiActiveLabel;
	private JLabel wifiTailLabel;
	private JLabel wifiIdleLabel;
	private JLabel totalRrcEnergyLabel;
	private JLabel wifiActiveValueLabel;
	private JLabel wifiTailValueLabel;
	private JLabel wifiIdleValueLabel;
	private JLabel totalRrcEnergyValueLabel;

	/**
	 * Initializes a new instance of the EnergyModelStatisticsWiFiPanel class.
	 */
	public EnergyModelStatisticsWiFiPanel() {
		
	}
	
	/**
	 * Creates the JPanel that contains the RRC statistics portion of the Energy 
	 * Consumption statistics data.
	 */
	protected void createRRCStatsPanel() {

		energyConsumptionStatsPanel = new JPanel(new GridLayout(12, 2, 5, 5));
		energyConsumptionStatsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		wifiActiveLabel = new JLabel(rb.getString("rrc.wifiActive"));
		wifiActiveLabel.setFont(TEXT_FONT);
		wifiActiveValueLabel = new JLabel();
		wifiActiveValueLabel.setFont(TEXT_FONT);
		wifiTailLabel = new JLabel(rb.getString("rrc.WifiTail"));
		wifiTailLabel.setFont(TEXT_FONT);
		wifiTailValueLabel = new JLabel();
		wifiTailValueLabel.setFont(TEXT_FONT);
		wifiIdleLabel = new JLabel(rb.getString("rrc.WiFiIdle"));
		wifiIdleLabel.setFont(TEXT_FONT);
		wifiIdleValueLabel = new JLabel();
		wifiIdleValueLabel.setFont(TEXT_FONT);
		totalRrcEnergyLabel = new JLabel(rb.getString("energy.wifiTotal"));
		totalRrcEnergyLabel.setFont(TEXT_FONT);
		totalRrcEnergyValueLabel = new JLabel();
		totalRrcEnergyValueLabel.setFont(TEXT_FONT);

		energyConsumptionStatsPanel.add(wifiActiveLabel);
		energyConsumptionStatsPanel.add(wifiActiveValueLabel);
		energyConsumptionStatsPanel.add(wifiTailLabel);
		energyConsumptionStatsPanel.add(wifiTailValueLabel);
		energyConsumptionStatsPanel.add(wifiIdleLabel);
		energyConsumptionStatsPanel.add(wifiIdleValueLabel);
		energyConsumptionStatsPanel.add(totalRrcEnergyLabel);
		energyConsumptionStatsPanel.add(totalRrcEnergyValueLabel);

	}
	
	/**
	 * Refreshes the RRCStatistics portion of the EnergyModelStatisticsWiFiPanel when a trace is loaded.
	 * 
	 * @param analysis
	 *          - The Analysis object containing the trace data.
	 * @param nf 
	 *          - The number format used to display the label values.
	 */
	@Override
	public void refreshRRCStatistic(AROTraceData analysis, NumberFormat nf) {
		if (analysis != null) {

			RrcStateMachineWiFi rrc = (RrcStateMachineWiFi)analysis.getAnalyzerResult().getStatemachine();

			wifiActiveValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getWifiActiveEnergy())));
			wifiTailValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getWifiTailEnergy())));
			wifiIdleValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getWifiIdleEnergy())));
			totalRrcEnergyValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getTotalRRCEnergy())));

			energyContent.put(rb.getString("rrc.wifiActive"), wifiActiveValueLabel.getText());
			energyContent.put(rb.getString("rrc.WifiTail"), wifiTailValueLabel.getText());
			energyContent.put(rb.getString("rrc.WiFiIdle"), wifiIdleValueLabel.getText());
			energyContent.put(rb.getString("energy.wifiTotal"), totalRrcEnergyValueLabel.getText());

		} else {
			wifiActiveValueLabel.setText(null);
			wifiTailValueLabel.setText(null);
			wifiIdleValueLabel.setText(null);
			totalRrcEnergyValueLabel.setText(null);
		}

	}

	/**
	 * @return the wifiActiveLabel
	 */
	public JLabel getWifiActiveLabel() {
		return wifiActiveLabel;
	}

	/**
	 * @return the wifiTailLabel
	 */
	public JLabel getWifiTailLabel() {
		return wifiTailLabel;
	}

	/**
	 * @return the wifiIdleLabel
	 */
	public JLabel getWifiIdleLabel() {
		return wifiIdleLabel;
	}

	/**
	 * @return the totalRrcEnergyLabel
	 */
	public JLabel getTotalRrcEnergyLabel() {
		return totalRrcEnergyLabel;
	}
}
