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

import javax.swing.JPanel;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;


public abstract class RRCStateMachineSimulationPanelBase extends JPanel
		implements IUITabPanelLayoutUpdate {
	private static final long serialVersionUID = 1L;

	@Override
	public abstract JPanel layoutDataPanel();

	@Override
	public abstract void refresh(AROTraceData analyzerResult);

	protected String[] getRatioPercentString(double item, double ratio) {
		String[] ratioPercentStrings = new String[2];
		ratioPercentStrings[0] = String.format("%,1.2f", item);
		ratioPercentStrings[1] = String.format("%,1.2f", ratio);
		return ratioPercentStrings;
	}
	protected String[] getRatioPercentString(int item, double ratio) {
		String[] ratioPercentStrings = new String[2];
		ratioPercentStrings[0] = String.format("%,1d", item);
		ratioPercentStrings[1] = String.format("%,1.2f", ratio);
		return ratioPercentStrings;
	}
}
