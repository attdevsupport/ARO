/*
 *  Copyright 2015 AT&T
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.IAROExpandable;
import com.att.aro.ui.commonui.IUITabPanelLayoutUpdate;
import com.att.aro.ui.commonui.TabPanelSupport;
import com.att.aro.ui.exception.AROUIPanelException;
import com.att.aro.ui.view.AROModelObserver;

/**
 * 
 */
public class BpDetailResultsPanel extends JPanel implements Observer, IUITabPanelLayoutUpdate, Printable, IAROExpandable  {

	private static final long serialVersionUID = 1L;

	AROModelObserver overViewObservable;

	private final TabPanelSupport tabPanelSupport;

	private ArrayList<BpDetail> expandableList = new ArrayList<BpDetail>();

	private Insets insets = new Insets(10, 0, 5, 0);

	private JPanel sectionPanel = null;

	public BpDetailResultsPanel(IARODiagnosticsOverviewRoute diagnosticsOverviewRoute) {
		
		tabPanelSupport = new TabPanelSupport(this);

		setLayout(new GridBagLayout());
		
		overViewObservable = new AROModelObserver();
		
		sectionPanel = new JPanel(new GridBagLayout());

		sectionPanel.setBackground(new Color(238,238,238));	//bcn
				
		int row = 0;
		addBpSection(row++, new BpDetailDownloadPanel("bestPractices.header.fileDownload",
				diagnosticsOverviewRoute));
		addBpSection(row++, new BpDetailConnectionsPanel("bestPractices.header.connections",
				diagnosticsOverviewRoute));
		addBpSection(row++, new BpDetailHtmlPanel("bestPractices.header.html",
				diagnosticsOverviewRoute));
		addBpSection(row++, new BpDetailOtherPanel("bestPractices.header.others",
				diagnosticsOverviewRoute));
	
		add(sectionPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
	}

	/**
	 * Add sections to Best Practice JPanel
	 * 
	 * @param row
	 * @param sectionFileDownload
	 * @param insets
	 * @param sectionPanel
	 */
	private void addBpSection(int row, BpDetail sectionFileDownload) {
		expandableList.add(sectionFileDownload);
		sectionPanel.add(sectionFileDownload  ,new GridBagConstraints(0, row, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		overViewObservable.registerObserver(sectionFileDownload);
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

	
	@Override
	public JPanel layoutDataPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(AROTraceData model) {
		// TODO Auto-generated method stub
		// System.out.println("---------BpDetailResultsPanel.refresh(AROTraceData)");
		overViewObservable.refreshModel(model);
	}

	@Override
	public void update(Observable observable, Object model){
		if (!(model instanceof AROTraceData)) {
			throw new AROUIPanelException("Bad data model type passed");
		}
		tabPanelSupport.update(observable, (AROTraceData) model, isVisible());
	}

	/**
	 * forwards expand() command to All BpDetailItem
	 */
	@Override
	public void expand() {
		for (BpDetail bpDetail:expandableList){
			bpDetail.expand();
		}
	}
}
