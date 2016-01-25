/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.view.statistics.statemachine;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.core.packetanalysis.pojo.RrcStateMachineLTE;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;

public class RRCStateMachineSimulationPanelLTE extends RRCStateMachineSimulationPanelBase {
	private static final long serialVersionUID = 1L;
	private JPanel dataPanel;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();

	enum LabelKeys {
		rrc_title,
		rrc_continuousReceptionIdle,
		rrc_continuousReception,
		rrc_continuousReceptionTail,
		rrc_shortDRX,
		rrc_longDRX,
		rrc_idle,
		rrc_crTailRatio,
		rrc_longDRXRatio,
		rrc_shortDRXRatio,
		rrc_promotionRatio,
		rrc_valueAndPctLTE
	}

	public RRCStateMachineSimulationPanelLTE() {
		tabPanelCommon.initTabPanel(this);
		add(layoutDataPanel(), BorderLayout.WEST);
	}

	/**
	 * Creates the JPanel Radio Resource Control State Machine Simulation
	 * 
	 * @return the dataPanel
	 */
	@Override
	public JPanel layoutDataPanel() {
		dataPanel = tabPanelCommon.initDataPanel(
				UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));

		Insets insets = new Insets(2, 2, 2, 2);
		Insets bottomBlankLineInsets = new Insets(2, 2, 8, 2);
		TabPanelCommonAttributes attributes = tabPanelCommon.addLabelLine(
			new TabPanelCommonAttributes.Builder()
				.enumKey(LabelKeys.rrc_title)
				.contentsWidth(1)
				.insets(insets)
				.insetsOverride(bottomBlankLineInsets)
				.header()
	.		build());
		attributes = tabPanelCommon.changeToNextDataPanel(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copy(attributes)
				.gridy(0)
				.enumKey(LabelKeys.rrc_continuousReceptionIdle)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_continuousReception)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_continuousReceptionTail)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_shortDRX)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_longDRX)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_idle)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_crTailRatio)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_longDRXRatio)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_shortDRXRatio)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_promotionRatio)
			.build());
		
		return dataPanel;
	}


	@Override
	public void refresh(AROTraceData model) {
		RrcStateMachineLTE stateMachine = (RrcStateMachineLTE)
				model.getAnalyzerResult().getStatemachine();
		String stringFormat = "%1.2f";
		tabPanelCommon.setText(LabelKeys.rrc_continuousReceptionIdle,
				tabPanelCommon.getText(LabelKeys.rrc_valueAndPctLTE,
					getRatioPercentString(stateMachine.getLteIdleToCRPromotionTime(),
							stateMachine.getLteIdleToCRPromotionTimeRatio()*100)));
		tabPanelCommon.setText(LabelKeys.rrc_continuousReception,
				tabPanelCommon.getText(LabelKeys.rrc_valueAndPctLTE,
					getRatioPercentString(stateMachine.getLteCrTime(),
							stateMachine.getLteCrTimeRatio()*100)));
		tabPanelCommon.setText(LabelKeys.rrc_continuousReceptionTail,
				tabPanelCommon.getText(LabelKeys.rrc_valueAndPctLTE,
					getRatioPercentString(stateMachine.getLteCrTailTime(),
							stateMachine.getLteCrTailTimeRatio()*100)));
		tabPanelCommon.setText(LabelKeys.rrc_shortDRX,
				tabPanelCommon.getText(LabelKeys.rrc_valueAndPctLTE,
					getRatioPercentString(stateMachine.getLteDrxShortTime(),
							stateMachine.getLteDrxShortTimeRatio()*100)));
		tabPanelCommon.setText(LabelKeys.rrc_longDRX,
				tabPanelCommon.getText(LabelKeys.rrc_valueAndPctLTE,
					getRatioPercentString(stateMachine.getLteDrxLongTime(),
							stateMachine.getLteDrxLongTimeRatio()*100)));
		tabPanelCommon.setText(LabelKeys.rrc_idle,
				tabPanelCommon.getText(LabelKeys.rrc_valueAndPctLTE,
					getRatioPercentString(stateMachine.getLteIdleTime(),
							stateMachine.getLteIdleTimeRatio()*100)));
		tabPanelCommon.setText(LabelKeys.rrc_crTailRatio, String.format(stringFormat,
				stateMachine.getCRTailRatio()));
		tabPanelCommon.setText(LabelKeys.rrc_longDRXRatio, String.format(stringFormat,
				stateMachine.getLteDrxLongRatio()));
		tabPanelCommon.setText(LabelKeys.rrc_shortDRXRatio, String.format(stringFormat,
				stateMachine.getLteDrxShortRatio()));
		tabPanelCommon.setText(LabelKeys.rrc_promotionRatio, String.format(stringFormat,
				stateMachine.getCRPromotionRatio()));
	}
}
