/**
 * 
 */
package com.att.aro.ui.view.statistics.statemachine;

import javax.swing.JPanel;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;


/**
 * @author Nathan F Syfrig
 *
 */
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
