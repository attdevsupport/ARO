package com.att.aro.ui.view.statistics.energy;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;
import com.att.aro.ui.exception.AROUIPanelException;

public class EnergyEfficiencySimulationLTE extends JPanel implements IUITabPanelLayoutUpdate {
	enum LabelKeys {
		energy_title,
		rrc_continuousReceptionIdle,
		energy_promotionRatioTime,
		energy_continuousReception,
		energy_shortDRX,
		energy_lteDrxLongTime,
		energy_idle,
		energy_rrcTotal,
		energy_jpkb,
		energy_promotionRatio,
		energy_continuousReceptionEnergy,
		energy_continuousReceptionTail,
		energy_shortDRXEnergy,
		energy_lteDrxLongEnergy,
		energy_units
	}
	private static final long serialVersionUID = 1L;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();

	public EnergyEfficiencySimulationLTE() {
		tabPanelCommon.initTabPanel(this);
		add(layoutDataPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the Energy Efficiency Simulation sub-panel within the Statistics tab
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {
		Insets bottomBlankLineInsets = new Insets(2, 2, 8, 2);
		TabPanelCommonAttributes attributes = tabPanelCommon.addLabelLine(
				new TabPanelCommonAttributes.Builder()
					.enumKey(LabelKeys.energy_title)
					.insetsOverride(bottomBlankLineInsets)
					.header()
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_continuousReceptionIdle)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_shortDRX)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_continuousReception)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_idle)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_rrcTotal)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_jpkb)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_promotionRatio)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_continuousReceptionEnergy)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_continuousReceptionTail)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_promotionRatioTime)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_shortDRXEnergy)
			.build());

		return tabPanelCommon.getTabPanel();
	}

	private String[] getRrcStateMachineValue(PacketAnalyzerResult analyzerResult,
			LabelKeys labelKey, String[] valueString) {
		RrcStateMachineLTE rrcStateMachine =
				(RrcStateMachineLTE) analyzerResult.getStatemachine();
		Double originalValue = null;
		valueString[0] = "";
		switch(analyzerResult.getStatemachine().getType()) {
			case LTE:
				switch(labelKey) {
					case rrc_continuousReceptionIdle:
						originalValue = rrcStateMachine.getLteIdleToCRPromotionEnergy();
						break;
					case energy_shortDRX:
						originalValue = rrcStateMachine.getLteDrxShortTime();
						break;
					case energy_continuousReception:
						originalValue = rrcStateMachine.getLteCrTime();
						break;
					case energy_idle:
						originalValue = rrcStateMachine.getLteIdleEnergy();
						break;
					case energy_promotionRatio:
						originalValue = rrcStateMachine.getLteIdleToCRPromotionEnergy();
						break;
					case energy_continuousReceptionEnergy:
						originalValue = rrcStateMachine.getLteCrEnergy();
						break;
					case energy_continuousReceptionTail:
						originalValue = rrcStateMachine.getLteCrTailEnergy();
						break;
					case energy_promotionRatioTime:
						originalValue = rrcStateMachine.getLteIdleToCRPromotionTime();
						break;
					case energy_shortDRXEnergy:
						originalValue = rrcStateMachine.getLteDrxShortEnergy();
						break;
					case energy_rrcTotal:
						originalValue = rrcStateMachine.getTotalRRCEnergy();
						break;
					case energy_jpkb:
						originalValue = rrcStateMachine.getJoulesPerKilobyte();
						break;
					default:
						throw new AROUIPanelException("Key " +
								analyzerResult.getStatemachine().getType().name() +
										" not handled yet");
				}
				break;
			default:
				throw new AROUIPanelException("Bad rrc state machine machine type " +
						analyzerResult.getStatemachine().getType() +
								" for expected type of LTE");
		}
		if (originalValue != null) {
			valueString[0] = String.format("%1.2f", originalValue);
		}
		return valueString;
	}

	private void renderValue(PacketAnalyzerResult analyzerResult, LabelKeys labelKey,
			String[] valueString) {
		tabPanelCommon.setText(labelKey, tabPanelCommon.getText(LabelKeys.energy_units,
				getRrcStateMachineValue(analyzerResult, labelKey, valueString)));
	}

	@Override
	public void refresh(AROTraceData model) {
		PacketAnalyzerResult analyzerResult = model.getAnalyzerResult();
		String[] valueString = new String[1];
		renderValue(analyzerResult, LabelKeys.rrc_continuousReceptionIdle, valueString);
		renderValue(analyzerResult, LabelKeys.energy_shortDRX, valueString);
		renderValue(analyzerResult, LabelKeys.energy_continuousReception, valueString);
		renderValue(analyzerResult, LabelKeys.energy_idle, valueString);
		renderValue(analyzerResult, LabelKeys.energy_promotionRatio, valueString);
		renderValue(analyzerResult, LabelKeys.energy_continuousReceptionEnergy, valueString);
		renderValue(analyzerResult, LabelKeys.energy_continuousReceptionTail, valueString);
		renderValue(analyzerResult, LabelKeys.energy_promotionRatioTime, valueString);
		renderValue(analyzerResult, LabelKeys.energy_shortDRXEnergy, valueString);
		renderValue(analyzerResult, LabelKeys.energy_rrcTotal, valueString);
		renderValue(analyzerResult, LabelKeys.energy_jpkb, valueString);
	}
}
