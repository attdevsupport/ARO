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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.model.EnergyModel;
import com.att.aro.model.TraceData;

/**
 * Represents a panel that displays the Energy Model statistics about the trace.
 */
public abstract class EnergyModelStatisticsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JLabel gpsActiveLabel;
	private JLabel gpsActiveValueLabel;
	private JLabel gpsStandbyLabel;
	private JLabel gpsStandbyValueLabel;
	private JLabel gpsTotalLabel;
	private JLabel gpsTotalValueLabel;
	private JLabel cameraTotalLabel;
	private JLabel cameraTotalValueLabel;
	private JLabel wifiActiveLabel;
	private JLabel wifiActiveValueLabel;
	private JLabel wifiStandbyLabel;
	private JLabel wifiStandbyValueLabel;
	private JLabel wifiTotalLabel;
	private JLabel wifiTotalValueLabel;
	private JLabel bluetoothActiveLabel;
	private JLabel bluetoothActiveValueLabel;
	private JLabel bluetoothStandbyLabel;
	private JLabel bluetoothStandbyValueLabel;
	private JLabel bluetoothTotalLabel;
	private JLabel bluetoothTotalValueLabel;
	private JLabel screenTotalLabel;
	private JLabel screenTotalValueLabel;

	protected static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	protected static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD,
			16);
	protected static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN,
			12);
	protected static final int HEADER_DATA_SPACING = 10;

	protected JPanel energyStatisticsLeftAlligmentPanel = null;
	protected JPanel energyStatisticsPanel = null;
	protected JLabel energyStatisticsHeaderLabel = null;
	protected JPanel spacePanel = null;
	protected JPanel energyConsumptionStatsPanel = null;

	protected Map<String, String> energyContent = new LinkedHashMap<String, String>();

	/**
	 * Initializes a new instance of the EnergyModelStatisticsPanel class.
	 */
	public EnergyModelStatisticsPanel() {
		super(new BorderLayout(10, 10));
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
	}

	/**
	 * Refreshes the label values in the EnergyModelStatisticsPanel using the
	 * specified trace data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public abstract void refresh(TraceData.Analysis analysis);

	/**
	 * Creates the JPanel that contains the peripheral statistics data.
	 */
	protected void createPeripheralStatisticsPanel() {
		gpsActiveLabel = new JLabel(rb.getString("energy.gpsActive"));
		gpsActiveLabel.setFont(TEXT_FONT);
		gpsActiveValueLabel = new JLabel();
		gpsActiveValueLabel.setFont(TEXT_FONT);
		gpsStandbyLabel = new JLabel(rb.getString("energy.gpsStandby"));
		gpsStandbyLabel.setFont(TEXT_FONT);
		gpsStandbyValueLabel = new JLabel();
		gpsStandbyValueLabel.setFont(TEXT_FONT);
		gpsTotalLabel = new JLabel(rb.getString("energy.gpsTotal"));
		gpsTotalLabel.setFont(TEXT_FONT);
		gpsTotalValueLabel = new JLabel();
		gpsTotalValueLabel.setFont(TEXT_FONT);
		cameraTotalLabel = new JLabel(rb.getString("energy.cameraTotal"));
		cameraTotalLabel.setFont(TEXT_FONT);
		cameraTotalValueLabel = new JLabel();
		cameraTotalValueLabel.setFont(TEXT_FONT);
		wifiActiveLabel = new JLabel(rb.getString("energy.wifiActive"));
		wifiActiveLabel.setFont(TEXT_FONT);
		wifiActiveValueLabel = new JLabel();
		wifiActiveValueLabel.setFont(TEXT_FONT);
		wifiStandbyLabel = new JLabel(rb.getString("energy.wifiStandby"));
		wifiStandbyLabel.setFont(TEXT_FONT);
		wifiStandbyValueLabel = new JLabel();
		wifiStandbyValueLabel.setFont(TEXT_FONT);
		wifiTotalLabel = new JLabel(rb.getString("energy.wifiTotal"));
		wifiTotalLabel.setFont(TEXT_FONT);
		wifiTotalValueLabel = new JLabel();
		wifiTotalValueLabel.setFont(TEXT_FONT);
		bluetoothActiveLabel = new JLabel(
				rb.getString("energy.bluetoothActive"));
		bluetoothActiveLabel.setFont(TEXT_FONT);
		bluetoothActiveValueLabel = new JLabel();
		bluetoothActiveValueLabel.setFont(TEXT_FONT);
		bluetoothStandbyLabel = new JLabel(
				rb.getString("energy.bluetoothStandby"));
		bluetoothStandbyLabel.setFont(TEXT_FONT);
		bluetoothStandbyValueLabel = new JLabel();
		bluetoothStandbyValueLabel.setFont(TEXT_FONT);
		bluetoothTotalLabel = new JLabel(rb.getString("energy.bluetoothTotal"));
		bluetoothTotalLabel.setFont(TEXT_FONT);
		bluetoothTotalValueLabel = new JLabel();
		bluetoothTotalValueLabel.setFont(TEXT_FONT);
		screenTotalLabel = new JLabel(rb.getString("energy.screenTotal"));
		screenTotalLabel.setFont(TEXT_FONT);
		screenTotalValueLabel = new JLabel();
		screenTotalValueLabel.setFont(TEXT_FONT);

		energyStatisticsPanel = new JPanel();
		energyStatisticsPanel.setLayout(new VerticalLayout());
		energyStatisticsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		energyStatisticsHeaderLabel = new JLabel(rb.getString("energy.title"));
		energyStatisticsHeaderLabel.setFont(HEADER_FONT);
		energyStatisticsPanel.add(energyStatisticsHeaderLabel);

		spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(),
				HEADER_DATA_SPACING));
		spacePanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		energyStatisticsPanel.add(spacePanel);

		energyConsumptionStatsPanel = new JPanel(new GridLayout(20, 2, 5, 5));
		energyConsumptionStatsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
	}

	/**
	 * Adds the JPanel that contains the peripheral statistics data.
	 */
	protected void addPeripheralStatisticsPanel() {
		energyConsumptionStatsPanel.add(gpsActiveLabel);
		energyConsumptionStatsPanel.add(gpsActiveValueLabel);
		energyConsumptionStatsPanel.add(gpsStandbyLabel);
		energyConsumptionStatsPanel.add(gpsStandbyValueLabel);
		energyConsumptionStatsPanel.add(gpsTotalLabel);
		energyConsumptionStatsPanel.add(gpsTotalValueLabel);
		energyConsumptionStatsPanel.add(cameraTotalLabel);
		energyConsumptionStatsPanel.add(cameraTotalValueLabel);
		energyConsumptionStatsPanel.add(wifiActiveLabel);
		energyConsumptionStatsPanel.add(wifiActiveValueLabel);
		energyConsumptionStatsPanel.add(wifiStandbyLabel);
		energyConsumptionStatsPanel.add(wifiStandbyValueLabel);
		energyConsumptionStatsPanel.add(wifiTotalLabel);
		energyConsumptionStatsPanel.add(wifiTotalValueLabel);
		energyConsumptionStatsPanel.add(bluetoothActiveLabel);
		energyConsumptionStatsPanel.add(bluetoothActiveValueLabel);
		energyConsumptionStatsPanel.add(bluetoothStandbyLabel);
		energyConsumptionStatsPanel.add(bluetoothStandbyValueLabel);
		energyConsumptionStatsPanel.add(bluetoothTotalLabel);
		energyConsumptionStatsPanel.add(bluetoothTotalValueLabel);
		energyConsumptionStatsPanel.add(screenTotalLabel);
		energyConsumptionStatsPanel.add(screenTotalValueLabel);

		energyStatisticsPanel.add(energyConsumptionStatsPanel);

		energyStatisticsLeftAlligmentPanel = new JPanel(new BorderLayout());
		energyStatisticsLeftAlligmentPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		energyStatisticsLeftAlligmentPanel.add(energyStatisticsPanel,
				BorderLayout.WEST);
	}

	/**
	 * Updates the JPanel that contains the Energy Consumption statistics data.
	 * 
	 * @param NumberFormat
	 *            The display format
	 * @param String
	 *            The units for energy
	 * @param EnergyModel
	 *            The energy model
	 */
	protected void updatePeripheralStatistics(TraceData.Analysis analysis,
			NumberFormat nf, String units, EnergyModel model) {
		if (analysis != null) {
			gpsActiveValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getGpsActiveEnergy())));
			gpsStandbyValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getGpsStandbyEnergy())));
			gpsTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalGpsEnergy())));
			cameraTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalCameraEnergy())));
			wifiActiveValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getWifiActiveEnergy())));
			wifiStandbyValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getWifiStandbyEnergy())));
			wifiTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalWifiEnergy())));
			bluetoothActiveValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getBluetoothActiveEnergy())));
			bluetoothStandbyValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getBluetoothStandbyEnergy())));
			bluetoothTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalBluetoothEnergy())));
			screenTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalScreenEnergy())));
		} else {
			gpsActiveValueLabel.setText(null);
			gpsStandbyValueLabel.setText(null);
			gpsTotalValueLabel.setText(null);
			cameraTotalValueLabel.setText(null);
			wifiActiveValueLabel.setText(null);
			wifiStandbyValueLabel.setText(null);
			wifiTotalValueLabel.setText(null);
			bluetoothActiveValueLabel.setText(null);
			bluetoothStandbyValueLabel.setText(null);
			bluetoothTotalValueLabel.setText(null);
			screenTotalValueLabel.setText(null);

			energyContent.clear();
		}
	}

	/**
	 * Returns a Map containing key-value pairs of the energy consumption
	 * statistics data.
	 * 
	 * @return A Map object containing the energy consumption statistics data.
	 */
	public Map<String, String> getEnergyContent() {
		return energyContent;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
