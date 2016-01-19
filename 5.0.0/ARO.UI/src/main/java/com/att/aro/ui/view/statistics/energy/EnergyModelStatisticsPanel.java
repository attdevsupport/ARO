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

package com.att.aro.ui.view.statistics.energy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Represents a panel that displays the Energy Consumption Simulation section of 
 * the Statistics Tab information page.
 */
public abstract class EnergyModelStatisticsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	//Returns the font used for the header portion of the panel.
	protected static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD, 16);
	
	//Returns the font used for the data portion of the panel.
	protected static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
	
	//Returns a constant value for header data spacing.
	protected static final int HEADER_DATA_SPACING = 10;

	private JLabel gpsActiveLabel;
	private JLabel gpsActiveValueLabel;
	private JLabel gpsStandbyLabel;
	private JLabel gpsStandbyValueLabel;
	private JLabel gpsTotalLabel;
	private JLabel gpsTotalValueLabel;
	private JLabel cameraTotalLabel;
	private JLabel cameraTotalValueLabel;
	private JLabel bluetoothActiveLabel;
	private JLabel bluetoothActiveValueLabel;
	private JLabel bluetoothStandbyLabel;
	private JLabel bluetoothStandbyValueLabel;
	private JLabel bluetoothTotalLabel;
	private JLabel bluetoothTotalValueLabel;
	private JLabel screenTotalLabel;
	private JLabel screenTotalValueLabel;

	private static final ResourceBundle rb = ResourceBundleHelper.getDefaultBundle();
	private static final String units = rb.getString("energy.units");

	private JPanel energyStatisticsLeftAlligmentPanel = null;
	private JPanel energyStatisticsPanel = null;
	private JLabel energyStatisticsHeaderLabel = null;
	private JPanel spacePanel = null;
	
	//Returns the JPanel object encapsulated by this class.
	protected JPanel energyConsumptionStatsPanel = null;

	//Returns a map of strings that contain the energy statistics column in the panel.
	protected Map<String, String> energyContent = new LinkedHashMap<String, String>();

	/**
	 * Initializes a new instance of the EnergyModelStatisticsPanel class.
	 */
	public EnergyModelStatisticsPanel() {
		super(new BorderLayout(10, 10));
		setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		init();
		add(energyStatisticsLeftAlligmentPanel, BorderLayout.WEST);
	}

	/**
	 * Refreshes the label values in the EnergyModelStatisticsPanel using the
	 * specified trace data.
	 * 
	 * @param analysis
	 *            - The Analysis object containing the trace data.
	 */
	public synchronized void refresh(AROTraceData analysis) {
		energyContent.clear();

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		refreshRRCStatistic(analysis, nf);
		if (analysis != null) {
			updatePeripheralStatistics(analysis, nf, units, analysis.getAnalyzerResult().getEnergyModel());
			updatePeripheralStatisticsValues();
		} else {
			updatePeripheralStatistics(null, null, null, null);
		}
	}

	/**
	 * Returns a Map containing key-value pairs of the energy consumption
	 * statistics data.
	 * 
	 * @return A Map object containing the energy consumption statistics data.
	 */
	public Map<String, String> getEnergyContent() {
		return Collections.unmodifiableMap(energyContent);
	}

	/**
	 * Refreshes the JPanel that contains the RRC portion of the Energy Consumption statistics data.
	 * 
	 * @param analysis
	 *          - The Analysis object containing the trace data.
	 * @param nf 
	 *          - The number format used to display the label values.
	 */
	protected abstract void refreshRRCStatistic(AROTraceData analysis, NumberFormat nf);

	/**
	* Creates the JPanel that contains the RRC statistics data.
	*/
	protected abstract void createRRCStatsPanel();

	private void init() {

		energyConsumptionStatsPanel = new JPanel(new GridLayout(17, 2, 5, 5));
		energyStatisticsLeftAlligmentPanel = new JPanel(new BorderLayout());
		energyConsumptionStatsPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		energyStatisticsLeftAlligmentPanel.setBackground(UIManager
				.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		createRRCStatsPanel();
		createPeripheralStatisticsPanel();

		energyStatisticsPanel = new JPanel();
		energyStatisticsPanel.setLayout(new VerticalLayout());
		energyStatisticsPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		energyStatisticsHeaderLabel = new JLabel(rb.getString("energy.title"));
		energyStatisticsHeaderLabel.setFont(HEADER_FONT);
		energyStatisticsPanel.add(energyStatisticsHeaderLabel);

		spacePanel = new JPanel();
		spacePanel.setPreferredSize(new Dimension(this.getWidth(), HEADER_DATA_SPACING));
		spacePanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		energyStatisticsPanel.add(spacePanel);

		energyStatisticsPanel.add(energyConsumptionStatsPanel);

		energyStatisticsLeftAlligmentPanel.add(energyStatisticsPanel, BorderLayout.WEST);

	}

	/**
	 * Creates the JPanel that contains the peripheral statistics data.
	 */
	private void createPeripheralStatisticsPanel() {

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
		bluetoothActiveLabel = new JLabel(rb.getString("energy.bluetoothActive"));
		bluetoothActiveLabel.setFont(TEXT_FONT);
		bluetoothActiveValueLabel = new JLabel();
		bluetoothActiveValueLabel.setFont(TEXT_FONT);
		bluetoothStandbyLabel = new JLabel(rb.getString("energy.bluetoothStandby"));
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

		energyConsumptionStatsPanel.add(gpsActiveLabel);
		energyConsumptionStatsPanel.add(gpsActiveValueLabel);
		energyConsumptionStatsPanel.add(gpsStandbyLabel);
		energyConsumptionStatsPanel.add(gpsStandbyValueLabel);
		energyConsumptionStatsPanel.add(gpsTotalLabel);
		energyConsumptionStatsPanel.add(gpsTotalValueLabel);
		energyConsumptionStatsPanel.add(cameraTotalLabel);
		energyConsumptionStatsPanel.add(cameraTotalValueLabel);
		energyConsumptionStatsPanel.add(bluetoothActiveLabel);
		energyConsumptionStatsPanel.add(bluetoothActiveValueLabel);
		energyConsumptionStatsPanel.add(bluetoothStandbyLabel);
		energyConsumptionStatsPanel.add(bluetoothStandbyValueLabel);
		energyConsumptionStatsPanel.add(bluetoothTotalLabel);
		energyConsumptionStatsPanel.add(bluetoothTotalValueLabel);
		energyConsumptionStatsPanel.add(screenTotalLabel);
		energyConsumptionStatsPanel.add(screenTotalValueLabel);

	}

	/**
	 * Updates the JPanel that contains the Energy Consumption statistics data.
	 * 
	 * @param TraceData
	 *            .Analysis analysis data
	 * @param NumberFormat
	 *            The display format
	 * @param String
	 *            The units for energy
	 * @param EnergyModel
	 *            The energy model
	 */
	private void updatePeripheralStatistics(AROTraceData analysis, NumberFormat nf,
			String units, EnergyModel model) {
		if (analysis != null) {
			gpsActiveValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getGpsActiveEnergy())));
			gpsStandbyValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getGpsStandbyEnergy())));
			gpsTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalGpsEnergy())));
			cameraTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(model.getTotalCameraEnergy())));
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
			bluetoothActiveValueLabel.setText(null);
			bluetoothStandbyValueLabel.setText(null);
			bluetoothTotalValueLabel.setText(null);
			screenTotalValueLabel.setText(null);
		}
	}

	/**
	 * Updates the JPanel that contains the Energy Consumption statistics data
	 * with values.
	 */
	private void updatePeripheralStatisticsValues() {
		energyContent.put(rb.getString("energy.gpsActive"), gpsActiveValueLabel.getText());
		energyContent.put(rb.getString("energy.gpsStandby"), gpsStandbyValueLabel.getText());
		energyContent.put(rb.getString("energy.gpsTotal"), gpsTotalValueLabel.getText());
		energyContent.put(rb.getString("energy.cameraTotal"), cameraTotalValueLabel.getText());
		energyContent.put(rb.getString("energy.bluetoothActive"),
				bluetoothActiveValueLabel.getText());
		energyContent.put(rb.getString("energy.bluetoothStandby"),
				bluetoothStandbyValueLabel.getText());
		energyContent
				.put(rb.getString("energy.bluetoothTotal"), bluetoothTotalValueLabel.getText());
		energyContent.put(rb.getString("energy.screenTotal"), screenTotalValueLabel.getText());
	}

	/**
	 * Method to add the energy statistics content in the csv file
	 * 
	 * @throws IOException
	 */
	public FileWriter addEnergyContent(FileWriter writer)
			throws IOException {
		final String lineSep = System.getProperty(rb.getString("statics.csvLine.seperator"));
		for (Map.Entry<String, String> iter : energyContent.entrySet()) {
			String individualVal = iter.getValue().replace(
					rb.getString("statics.csvCell.seperator"), "");
			writer.append(iter.getKey());
			writer.append(rb.getString("statics.csvCell.seperator"));
			if (individualVal.contains(rb.getString("statics.csvUnits.j"))) {
				writer.append(individualVal.substring(0,
						individualVal.indexOf(rb.getString("statics.csvUnits.j"))));
				writer.append(rb.getString("statics.csvCell.seperator"));
				writer.append(rb.getString("statics.csvUnits.j"));
			} else {
				writer.append(individualVal);
			}
			writer.append(lineSep);
		}
		return writer;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
