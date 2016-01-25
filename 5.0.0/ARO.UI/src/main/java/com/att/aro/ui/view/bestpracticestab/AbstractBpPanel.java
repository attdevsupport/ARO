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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;
import com.att.aro.ui.commonui.TabPanelSupport;
import com.att.aro.ui.exception.AROUIPanelException;
import com.att.aro.ui.utils.ResourceBundleHelper;

public abstract class AbstractBpPanel extends JPanel implements Observer, IUITabPanelLayoutUpdate, Printable {

	private static final long serialVersionUID = 1L;
	
	private final TabPanelSupport tabPanelSupport;

	JPanel dataPanel;

	static final Font TEXT_FONT = new Font("TextFont", Font.PLAIN, 12);
	static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD, 14);
	static final Font SUMMARY_FONT = new Font("HeaderFont", Font.BOLD, 18);
	static final Font TEXT_FONT_BOLD = new Font("BoldTextFont", Font.BOLD, 12);
	static final Font LABEL_FONT = new Font("TEXT_FONT", Font.BOLD, 12);

	public AbstractBpPanel() {

		final int borderGap = 10;

		this.setLayout(new BorderLayout(borderGap, borderGap));
		this.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, borderGap, 0));

		tabPanelSupport = new TabPanelSupport(this);
	}

	/**
	 * 
	 * @param infoLabel
	 * @param labelText
	 * @param gridy
	 * @param width
	 * @param weightx
	 * @param insets
	 * @param font
	 */
	protected void addLabelLine(JLabel infoLabel, String labelText, int gridy, int width, double weightx, Insets insets, Font font) {

		JLabel label = null;
		if (labelText.split(" ").length == 0) {
			label = new JLabel(labelText);
		} else {
			label = new JLabel(ResourceBundleHelper.getMessageString(labelText));
		}
		label.setFont(font);
		dataPanel.add(label, new GridBagConstraints(0, gridy, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
		dataPanel.add(infoLabel, new GridBagConstraints(1, gridy, width, 1, weightx, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));

	}

	/**
	 * @return the aroModel
	 */
	public AROTraceData getAroModel() {
		return tabPanelSupport.getAroModel();
	}

	@Override
	public void update(Observable observable, Object model){
		if (!(model instanceof AROTraceData)) {
			throw new AROUIPanelException("Bad data model type passed");
		}
		tabPanelSupport.update(observable, (AROTraceData) model, isVisible());
	}


	@Override
	public String toString() {
		return "TabPanelJPanel [tabPanelSupport=" + tabPanelSupport + "]";
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public JPanel layoutDataPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(AROTraceData analyzerResult) {
		// TODO Auto-generated method stub
		
	}


}
