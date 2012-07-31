package com.att.aro.main;

import java.awt.GridLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.RRCStateMachine;
import com.att.aro.model.TraceData.Analysis;

public class EnergyModelStatisticsWiFiPanel extends EnergyModelStatisticsPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String units = rb.getString("energy.units");

	private JLabel wifiActiveValueLabel;
	private JLabel wifiTailValueLabel;
	private JLabel wifiIdleValueLabel;
	private JLabel totalRrcEnergyValueLabel;

	protected void createRRCStatsPanel() {

		energyConsumptionStatsPanel = new JPanel(new GridLayout(12, 2, 5, 5));
		energyConsumptionStatsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		JLabel wifiActiveLabel = new JLabel(rb.getString("rrc.wifiActive"));
		wifiActiveLabel.setFont(TEXT_FONT);
		wifiActiveValueLabel = new JLabel();
		wifiActiveValueLabel.setFont(TEXT_FONT);
		JLabel wifiTailLabel = new JLabel(rb.getString("rrc.WifiTail"));
		wifiTailLabel.setFont(TEXT_FONT);
		wifiTailValueLabel = new JLabel();
		wifiTailValueLabel.setFont(TEXT_FONT);
		JLabel wifiIdleLabel = new JLabel(rb.getString("rrc.WiFiIdle"));
		wifiIdleLabel.setFont(TEXT_FONT);
		wifiIdleValueLabel = new JLabel();
		wifiIdleValueLabel.setFont(TEXT_FONT);
		JLabel totalRrcEnergyLabel = new JLabel(rb.getString("energy.rrcTotal"));
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

	@Override
	public void refreshRRCStatistic(Analysis analysis, NumberFormat nf) {
		if (analysis != null) {

			RRCStateMachine rrc = analysis.getRrcStateMachine();

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

		} else {
			wifiActiveValueLabel.setText(null);
			wifiTailValueLabel.setText(null);
			wifiIdleValueLabel.setText(null);
			totalRrcEnergyValueLabel.setText(null);
		}

	}

}
