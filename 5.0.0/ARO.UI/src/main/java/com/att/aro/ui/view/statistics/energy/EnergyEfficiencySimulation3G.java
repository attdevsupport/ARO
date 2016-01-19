package com.att.aro.ui.view.statistics.energy;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachine3G;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;
import com.att.aro.ui.exception.AROUIPanelException;

public class EnergyEfficiencySimulation3G extends JPanel implements IUITabPanelLayoutUpdate {
	enum LabelKeys {
		energy_title,
		energy_rrcTotal,
		energy_idle,
		energy_dch,
		energy_fach,
		energy_idle2dch,
		energy_fach2dch,
		energy_dchTail,
		energy_fachTail,
		energy_jpkb,
		energy_gpsTotal,
		energy_gpsActive,
		energy_gpsStandby,
		energy_cameraTotal,
		energy_wifiTotal,
		energy_wifiActive,
		energy_wifiStandby,
		energy_bluetoothTotal,
		energy_bluetoothActive,
		energy_bluetoothStandby,
		energy_screenTotal,
		energy_units
	}
	private static final long serialVersionUID = 1L;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();

	public EnergyEfficiencySimulation3G() {
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
	.		build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_dch)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_fach)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_idle)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_idle2dch)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_fach2dch)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_dchTail)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_fachTail)
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
				.enumKey(LabelKeys.energy_gpsActive)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_gpsStandby)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_gpsTotal)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_cameraTotal)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_bluetoothActive)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_bluetoothStandby)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_bluetoothTotal)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.energy_screenTotal)
			.build());

		return tabPanelCommon.getTabPanel();
	}

	private String[] getRrcStateMachineValue(PacketAnalyzerResult analyzerResult,
			LabelKeys labelKey, String[] valueString) {
		RrcStateMachine3G rrcStateMachine = (RrcStateMachine3G) analyzerResult.getStatemachine();
		Double originalValue = null;
		valueString[0] = "";
		switch(analyzerResult.getStatemachine().getType()) {
			case Type3G:
				switch(labelKey) {
					case energy_dch:
						originalValue = rrcStateMachine.getDchEnergy();
						break;
					case energy_fach:
						originalValue = rrcStateMachine.getFachEnergy();
						break;
					case energy_idle:
						originalValue = rrcStateMachine.getIdleEnergy();
						break;
					case energy_idle2dch:
						originalValue = rrcStateMachine.getIdleToDchEnergy();
						break;
					case energy_fach2dch:
						originalValue = rrcStateMachine.getFachToDchEnergy();
						break;
					case energy_dchTail:
						originalValue = rrcStateMachine.getDchTailEnergy();
						break;
					case energy_fachTail:
						originalValue = rrcStateMachine.getFachTailEnergy();
						break;
					case energy_rrcTotal:
						originalValue = rrcStateMachine.getTotalRRCEnergy();
						break;
					case energy_jpkb:
						originalValue = rrcStateMachine.getJoulesPerKilobyte();
						break;
					default:
						break;
				}
				break;
			default:
				throw new AROUIPanelException("Bad rrc state machine machine type " +
						analyzerResult.getStatemachine().getType() + " for expected type of 3G");
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
		renderValue(analyzerResult, LabelKeys.energy_dch, valueString);
		renderValue(analyzerResult, LabelKeys.energy_fach, valueString);
		renderValue(analyzerResult, LabelKeys.energy_idle, valueString);
		renderValue(analyzerResult, LabelKeys.energy_idle2dch, valueString);
		renderValue(analyzerResult, LabelKeys.energy_fach2dch, valueString);
		renderValue(analyzerResult, LabelKeys.energy_dchTail, valueString);
		renderValue(analyzerResult, LabelKeys.energy_fachTail, valueString);
		renderValue(analyzerResult, LabelKeys.energy_rrcTotal, valueString);
		renderValue(analyzerResult, LabelKeys.energy_jpkb, valueString);
	}
}
