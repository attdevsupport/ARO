/**
 * 
 */
package com.att.aro.ui.commonui;

import javax.swing.JPanel;

import com.att.aro.core.pojo.AROTraceData;

/**
 * This defines the overall method structure how to implement an ARO UI tab, Swing view version.
 * 
 * @author Nathan F Syfrig
 *
 */
public interface IUITabPanelLayoutUpdate {
	public JPanel layoutDataPanel();
	public void refresh(AROTraceData analyzerResult);
}
