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

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;

import com.att.aro.model.RRCStateMachine;
import com.att.aro.model.TraceData;

/**
 * Displays the LTE Energy Model statistics about the trace
 */
public class EnergyModelStatisticsLTEPanel extends EnergyModelStatisticsPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String units = rb.getString("energy.units");

	private JLabel idleCrPromoLabel;
	private JLabel idleCrPromoValueLabel;
	private JLabel continuousReceptionLabel;
	private JLabel continuousReceptionValueLabel;
	private JLabel continuousReceptionTailLabel;
	private JLabel continuousReceptionTailValueLabel;
	private JLabel shortDRXLabel;
	private JLabel shortDRXValueLabel;
	private JLabel longDRXLabel;
	private JLabel longDRXValueLabel;
	private JLabel idleLabel;
	private JLabel idleValueLabel;
	private JLabel rrcTotalLabel;
	private JLabel rrcTotalValueLabel;
	private JLabel jpkbLabel;
	private JLabel jpkbValueLabel;

	/**
	 * Creates the JPanel that contains the Energy Consumption statistics data.
	 */
	protected void createRRCStatsPanel() {
		idleCrPromoLabel = new JLabel(rb.getString("rrc.continuousReceptionIdle"));
		idleCrPromoLabel.setFont(TEXT_FONT);
		idleCrPromoValueLabel = new JLabel();
		idleCrPromoValueLabel.setFont(TEXT_FONT);
		continuousReceptionLabel = new JLabel(rb.getString("rrc.continuousReception"));
		continuousReceptionLabel.setFont(TEXT_FONT);
		continuousReceptionValueLabel = new JLabel();
		continuousReceptionValueLabel.setFont(TEXT_FONT);
		continuousReceptionTailLabel = new JLabel(rb.getString("rrc.continuousReceptionTail"));
		continuousReceptionTailLabel.setFont(TEXT_FONT);
		continuousReceptionTailValueLabel = new JLabel();
		continuousReceptionTailValueLabel.setFont(TEXT_FONT);
		shortDRXLabel = new JLabel(rb.getString("rrc.shortDRX"));
		shortDRXLabel.setFont(TEXT_FONT);
		shortDRXValueLabel = new JLabel();
		shortDRXValueLabel.setFont(TEXT_FONT);
		longDRXLabel = new JLabel(rb.getString("rrc.longDRX"));
		longDRXLabel.setFont(TEXT_FONT);
		longDRXValueLabel = new JLabel();
		longDRXValueLabel.setFont(TEXT_FONT);
		idleLabel = new JLabel(rb.getString("energy.idle"));
		idleLabel.setFont(TEXT_FONT);
		idleValueLabel = new JLabel();
		idleValueLabel.setFont(TEXT_FONT);
		rrcTotalLabel = new JLabel(rb.getString("energy.rrcTotal"));
		rrcTotalLabel.setFont(TEXT_FONT);
		rrcTotalValueLabel = new JLabel();
		rrcTotalValueLabel.setFont(TEXT_FONT);
		jpkbLabel = new JLabel(rb.getString("energy.jpkb"));
		jpkbLabel.setFont(TEXT_FONT);
		jpkbValueLabel = new JLabel();
		jpkbValueLabel.setFont(TEXT_FONT);
		energyConsumptionStatsPanel.add(idleCrPromoLabel);
		energyConsumptionStatsPanel.add(idleCrPromoValueLabel);
		energyConsumptionStatsPanel.add(continuousReceptionLabel);
		energyConsumptionStatsPanel.add(continuousReceptionValueLabel);
		energyConsumptionStatsPanel.add(continuousReceptionTailLabel);
		energyConsumptionStatsPanel.add(continuousReceptionTailValueLabel);
		energyConsumptionStatsPanel.add(shortDRXLabel);
		energyConsumptionStatsPanel.add(shortDRXValueLabel);
		energyConsumptionStatsPanel.add(longDRXLabel);
		energyConsumptionStatsPanel.add(longDRXValueLabel);
		energyConsumptionStatsPanel.add(idleLabel);
		energyConsumptionStatsPanel.add(idleValueLabel);
		energyConsumptionStatsPanel.add(rrcTotalLabel);
		energyConsumptionStatsPanel.add(rrcTotalValueLabel);
		energyConsumptionStatsPanel.add(jpkbLabel);
		energyConsumptionStatsPanel.add(jpkbValueLabel);

	}

	/**
	 * Refreshes various label values in the EnergyModelStatisticsPanel when a
	 * trace is loaded.
	 * 
	 * @param analysis
	 *            The Analysis object containing the trace data.
	 */
	public void refreshRRCStatistic(TraceData.Analysis analysis, NumberFormat nf) {
		if (analysis != null) {

			RRCStateMachine rrc = analysis.getRrcStateMachine();

			idleCrPromoValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getLteIdleToCRPromotionEnergy())));
			continuousReceptionValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getLteCrEnergy())));
			continuousReceptionTailValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getLteCrTailEnergy())));
			shortDRXValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getLteDrxShortEnergy())));
			longDRXValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getLteDrxLongEnergy())));
			idleValueLabel.setText(MessageFormat.format(units, nf.format(rrc.getLteIdleEnergy())));
			rrcTotalValueLabel.setText(MessageFormat.format(units,
					nf.format(rrc.getTotalRRCEnergy())));
			jpkbValueLabel.setText(nf.format(rrc.getJoulesPerKilobyte()));

			energyContent.put(rb.getString("rrc.continuousReceptionIdle"),
					idleCrPromoValueLabel.getText());
			energyContent.put(rb.getString("rrc.continuousReception"),
					continuousReceptionValueLabel.getText());
			energyContent.put(rb.getString("rrc.continuousReceptionTail"),
					continuousReceptionTailValueLabel.getText());
			energyContent.put(rb.getString("rrc.shortDRX"), shortDRXValueLabel.getText());
			energyContent.put(rb.getString("rrc.longDRX"), longDRXValueLabel.getText());
			energyContent.put(rb.getString("energy.idle"), idleValueLabel.getText());
			energyContent.put(rb.getString("energy.rrcTotal"), rrcTotalValueLabel.getText());
			energyContent.put(rb.getString("energy.jpkb"), jpkbValueLabel.getText());

		} else {
			idleCrPromoValueLabel.setText(null);
			continuousReceptionValueLabel.setText(null);
			continuousReceptionTailValueLabel.setText(null);
			shortDRXValueLabel.setText(null);
			longDRXValueLabel.setText(null);
			idleValueLabel.setText(null);
			rrcTotalValueLabel.setText(null);
		}
	}

	/**
	 * Returns idleCrPromoLabel JLabel object.
	 * 
	 * @return idleCrPromoLabel.
	 */
	JLabel getContinuousReceptionIdleLabel() {
		return idleCrPromoLabel;
	}

	/**
	 * Returns continuousReceptionLabel JLabel object.
	 * 
	 * @return continuousReceptionLabel label.
	 */
	JLabel getContinuousReceptionLabel() {
		return continuousReceptionLabel;
	}

	/**
	 * Returns continuousReceptionTailLabel JLabel object.
	 * 
	 * @return continuousReceptionTailLabel label.
	 */
	JLabel getContinuousReceptionTailLabel() {
		return continuousReceptionTailLabel;
	}

	/**
	 * Returns shortDRXLabel JLabel object.
	 * 
	 * @return shortDRXLabel label.
	 */
	JLabel getShortDRXLabel() {
		return shortDRXLabel;
	}

	/**
	 * Returns longDRXLabel JLabel object.
	 * 
	 * @return longDRXLabel label.
	 */
	JLabel getLongDRXLabel() {
		return longDRXLabel;
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
	 * Returns RRC Total JLabel object.
	 * 
	 * @return RRC Total label.
	 */
	public JLabel getRrcTotalLabel() {
		return rrcTotalLabel;
	}

	/**
	 * Returns JPKB JLabel object.
	 * 
	 * @return JPKB label.
	 */
	public JLabel getJpkbLabel() {
		return jpkbLabel;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
