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

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;

import com.att.aro.core.packetanalysis.pojo.RrcStateMachine3G;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Displays the portion of the Energy Consumption Simulation section on the Statistics Tab 
 * information page that apply to a 3G Energy Model. 
 */
public class EnergyModelStatistics3GPanel extends EnergyModelStatisticsPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleHelper.getDefaultBundle();
	private static final String units = rb.getString("energy.units");

	private JLabel dchLabel;
	private JLabel dchValueLabel;
	private JLabel fachLabel;
	private JLabel fachValueLabel;
	private JLabel idleLabel;
	private JLabel idleValueLabel;
	private JLabel idle2dchLabel;
	private JLabel idle2dchValueLabel;
	private JLabel fach2dchLabel;
	private JLabel fach2dchValueLabel;
	private JLabel dchTailLabel;
	private JLabel dchTailValueLabel;
	private JLabel fachTailLabel;
	private JLabel fachTailValueLabel;
	private JLabel rrcTotalLabel;
	private JLabel rrcTotalValueLabel;
	private JLabel jpkbLabel;
	private JLabel jpkbValueLabel;

	/**
	 * Initializes a new instance of the EnergyModelStatistics3GPanel class.
	 */
	public EnergyModelStatistics3GPanel() {
		super();
	}
	
	/**
	 * Creates the JPanel that contains the Energy Consumption statistics data.
	 */
	protected void createRRCStatsPanel() {
		dchLabel = new JLabel(rb.getString("energy.dch"));
		dchLabel.setFont(TEXT_FONT);
		dchValueLabel = new JLabel();
		dchValueLabel.setFont(TEXT_FONT);
		fachLabel = new JLabel(rb.getString("energy.fach"));
		fachLabel.setFont(TEXT_FONT);
		fachValueLabel = new JLabel();
		fachValueLabel.setFont(TEXT_FONT);
		idleLabel = new JLabel(rb.getString("energy.idle"));
		idleLabel.setFont(TEXT_FONT);
		idleValueLabel = new JLabel();
		idleValueLabel.setFont(TEXT_FONT);
		idle2dchLabel = new JLabel(rb.getString("energy.idle2dch"));
		idle2dchLabel.setFont(TEXT_FONT);
		idle2dchValueLabel = new JLabel();
		idle2dchValueLabel.setFont(TEXT_FONT);
		fach2dchLabel = new JLabel(rb.getString("energy.fach2dch"));
		fach2dchLabel.setFont(TEXT_FONT);
		fach2dchValueLabel = new JLabel();
		fach2dchValueLabel.setFont(TEXT_FONT);
		dchTailLabel = new JLabel(rb.getString("energy.dchTail"));
		dchTailLabel.setFont(TEXT_FONT);
		dchTailValueLabel = new JLabel();
		dchTailValueLabel.setFont(TEXT_FONT);
		fachTailLabel = new JLabel(rb.getString("energy.fachTail"));
		fachTailLabel.setFont(TEXT_FONT);
		fachTailValueLabel = new JLabel();
		fachTailValueLabel.setFont(TEXT_FONT);
		rrcTotalLabel = new JLabel(rb.getString("energy.rrcTotal"));
		rrcTotalLabel.setFont(TEXT_FONT);
		rrcTotalValueLabel = new JLabel();
		rrcTotalValueLabel.setFont(TEXT_FONT);
		jpkbLabel = new JLabel(rb.getString("energy.jpkb"));
		jpkbLabel.setFont(TEXT_FONT);
		jpkbValueLabel = new JLabel();
		jpkbValueLabel.setFont(TEXT_FONT);

		energyConsumptionStatsPanel.add(dchLabel);
		energyConsumptionStatsPanel.add(dchValueLabel);
		energyConsumptionStatsPanel.add(fachLabel);
		energyConsumptionStatsPanel.add(fachValueLabel);
		energyConsumptionStatsPanel.add(idleLabel);
		energyConsumptionStatsPanel.add(idleValueLabel);
		energyConsumptionStatsPanel.add(idle2dchLabel);
		energyConsumptionStatsPanel.add(idle2dchValueLabel);
		energyConsumptionStatsPanel.add(fach2dchLabel);
		energyConsumptionStatsPanel.add(fach2dchValueLabel);
		energyConsumptionStatsPanel.add(dchTailLabel);
		energyConsumptionStatsPanel.add(dchTailValueLabel);
		energyConsumptionStatsPanel.add(fachTailLabel);
		energyConsumptionStatsPanel.add(fachTailValueLabel);
		energyConsumptionStatsPanel.add(rrcTotalLabel);
		energyConsumptionStatsPanel.add(rrcTotalValueLabel);
		energyConsumptionStatsPanel.add(jpkbLabel);
		energyConsumptionStatsPanel.add(jpkbValueLabel);

	}

	/**
	 * Refreshes various label values in the EnergyModelStatistics3GPanel when a
	 * trace is loaded.
	 * 
	 * @param analysis
	 *          - The Analysis object containing the trace data.
	 * @param nf 
	 * 			- The number format used to display the label values.
	 */
	public void refreshRRCStatistic(AROTraceData analysis, NumberFormat nf) {
		if (analysis != null) {

			RrcStateMachine3G rrc = (RrcStateMachine3G)analysis.getAnalyzerResult().getStatemachine();

			dchValueLabel.setText(MessageFormat.format(units, nf.format(rrc.getDchEnergy())));
			fachValueLabel.setText(MessageFormat.format(units, nf.format(rrc.getFachEnergy())));
			idleValueLabel.setText(MessageFormat.format(units, nf.format(rrc.getIdleEnergy())));
			idle2dchValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getIdleToDchEnergy())));
			fach2dchValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getFachToDchEnergy())));
			dchTailValueLabel
					.setText(MessageFormat.format(units, nf.format(rrc.getDchTailEnergy())));
			fachTailValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getFachTailEnergy())));
			rrcTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getTotalRRCEnergy())));
			jpkbValueLabel.setText(nf.format(rrc.getJoulesPerKilobyte()));

			energyContent.put(rb.getString("energy.dch"), dchValueLabel.getText());
			energyContent.put(rb.getString("energy.fach"), fachValueLabel.getText());
			energyContent.put(rb.getString("energy.idle"), idleValueLabel.getText());
			energyContent.put(rb.getString("energy.idle2dch"), idle2dchValueLabel.getText());
			energyContent.put(rb.getString("energy.fach2dch"), fach2dchValueLabel.getText());
			energyContent.put(rb.getString("energy.dchTail"), dchTailValueLabel.getText());
			energyContent.put(rb.getString("energy.fachTail"), fachTailValueLabel.getText());
			energyContent.put(rb.getString("energy.rrcTotal"), rrcTotalValueLabel.getText());
			energyContent.put(rb.getString("energy.jpkb"), jpkbValueLabel.getText());

		} else {
			dchValueLabel.setText(null);
			fachValueLabel.setText(null);
			idleValueLabel.setText(null);
			idle2dchValueLabel.setText(null);
			fach2dchValueLabel.setText(null);
			dchTailValueLabel.setText(null);
			fachTailValueLabel.setText(null);
			rrcTotalValueLabel.setText(null);
			jpkbValueLabel.setText(null);

		}
	}

	/**
	 * Returns dchLabel JLabel object.
	 * 
	 * @return dchLabel label.
	 */
	JLabel getDCHLabel() {
		return dchLabel;
	}

	/**
	 * Returns fachLabel JLabel object.
	 * 
	 * @return fachLabel label.
	 */
	JLabel getFACHLabel() {
		return fachLabel;
	}

	/**
	 * Returns idleLabel JLabel object.
	 * 
	 * @return idleLabel label.
	 */
	JLabel getIdleLabel() {
		return idleLabel;
	}

	/**
	 * Returns idle2dchLabel JLabel object.
	 * 
	 * @return idle2dchLabel label.
	 */
	JLabel getIdle2dchLabel() {
		return idle2dchLabel;
	}

	/**
	 * Returns fach2dchLabel JLabel object.
	 * 
	 * @return fach2dchLabel label.
	 */
	JLabel getFach2dchLabel() {
		return fach2dchLabel;
	}

	/**
	 * Returns dchTailLabel JLabel object.
	 * 
	 * @return dchTailLabel label.
	 */
	JLabel getDchTailLabel() {
		return dchTailLabel;
	}

	/**
	 * Returns fachTailLabel JLabel object.
	 * 
	 * @return fachTailLabel label.
	 */
	JLabel getFachTailLabel() {
		return fachTailLabel;
	}

	/**
	 * Returns rrcTotalLabel JLabel object.
	 * 
	 * @return rrcTotalLabel label.
	 */
	JLabel getRRCTotalLabel() {
		return rrcTotalLabel;
	}

	/**
	 * Returns jpkbLabel JLabel object.
	 * 
	 * @return jpkbLabel label.
	 */
	JLabel getJPKBLabel() {
		return jpkbLabel;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
