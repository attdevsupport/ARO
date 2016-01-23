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

import com.att.aro.core.packetanalysis.pojo.RrcStateMachineWiFi;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.TabPanelCommonAttributes;

public class RRCStateMachineSimulationPanelWiFi extends JPanel implements IUITabPanelLayoutUpdate {
	private static final long serialVersionUID = 1L;
	private JPanel dataPanel;
	private final TabPanelCommon tabPanelCommon = new TabPanelCommon();

	enum LabelKeys {
		rrc_title,
		rrc_dch,
		rrc_fach,
		rrc_idle,
		rrc_idle2dch,
		rrc_fach2dch,
		rrc_dchTailRatio,
		rrc_fachTailRatio,
		rrc_promotionRatio,
		rrc_valueAndPct
	}

	public RRCStateMachineSimulationPanelWiFi() {
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
				.enumKey(LabelKeys.rrc_dch)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_fach)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_idle)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_idle2dch)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_fach2dch)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_dchTailRatio)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_fachTailRatio)
			.build());
		attributes = tabPanelCommon.addLabelLine(new TabPanelCommonAttributes.Builder()
				.copyNextLine(attributes)
				.enumKey(LabelKeys.rrc_promotionRatio)
			.build());

		return dataPanel;
	}

	@Override
	public void refresh(AROTraceData model) {
		RrcStateMachineWiFi stateMachine = (RrcStateMachineWiFi)
				model.getAnalyzerResult().getStatemachine();
	}
}
