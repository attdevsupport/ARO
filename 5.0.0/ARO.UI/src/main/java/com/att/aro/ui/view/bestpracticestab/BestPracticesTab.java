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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.IAROPrintable;
import com.att.aro.ui.commonui.ImagePanel;
import com.att.aro.ui.commonui.RoundedBorder;
import com.att.aro.ui.commonui.TabPanelJScrollPane;
import com.att.aro.ui.commonui.UIComponent;
import com.att.aro.ui.view.AROModelObserver;
import com.att.aro.ui.view.statistics.DateTraceAppDetailPanel;

public class BestPracticesTab extends TabPanelJScrollPane implements IAROPrintable{

	private static final long serialVersionUID = 1L;

	AROModelObserver bpObservable;

	private JPanel container;
	private JPanel mainPanel;
	private IARODiagnosticsOverviewRoute diagnosticsOverviewRoute;


	Insets insets = new Insets(10, 1, 10, 1);

	private BpDetailResultsPanel bpDetailResultsPanel;
	
	/**
	 * Create the panel.
	 */
	public BestPracticesTab(IARODiagnosticsOverviewRoute diagnosticsOverviewRoute) {
		super();
		this.diagnosticsOverviewRoute = diagnosticsOverviewRoute;

		bpObservable = new AROModelObserver();

		container = new JPanel(new BorderLayout());

		// load graphic header
		container.add(UIComponent.getInstance().getLogoHeader("bestPractices.header.result"), BorderLayout.NORTH);

		ImagePanel panel = new ImagePanel(null);
		panel.setLayout(new GridBagLayout());

		panel.add(layoutDataPanel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL
				, new Insets(10, 10, 0, 10)
				, 0, 0));
		container.add(panel, BorderLayout.CENTER);
		setViewportView(container);
		getVerticalScrollBar().setUnitIncrement(10);
		getHorizontalScrollBar().setUnitIncrement(10);
		
	}

	@Override
	public JPanel layoutDataPanel() {
		if (mainPanel == null) {
			
			mainPanel = new JPanel(new GridBagLayout());
		//	mainPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			mainPanel.setOpaque(false);
			
			int section = 1;			

			mainPanel.add(buildTopGroup(), new GridBagConstraints(
					0, section++
					, 1, 1
					, 1.0, 0.0
					, GridBagConstraints.CENTER
					, GridBagConstraints.HORIZONTAL
					, new Insets(0, 0, 0, 0)
					, 0, 0));
			
			// BP Detail -aka- AROBpDetailedResultPanel
			bpDetailResultsPanel = new BpDetailResultsPanel(diagnosticsOverviewRoute);
			mainPanel.add(bpDetailResultsPanel, new GridBagConstraints(
					0, section++
					, 1, 1
					, 1.0, 0.0
					, GridBagConstraints.CENTER
					, GridBagConstraints.HORIZONTAL
					, new Insets(0, 0, 0, 0)
					, 0, 0));
			bpObservable.registerObserver(bpDetailResultsPanel);

		}
		return mainPanel;
	}

	/**
	 * 
	 */
	private JPanel buildTopGroup() {
		
		JPanel topPanel;
		topPanel = new JPanel(new GridBagLayout());

		topPanel.setOpaque(false);
		topPanel.setBorder(new RoundedBorder(new Insets(20, 20, 20, 20), Color.WHITE));

		int section = 0;

		//top summary

		DateTraceAppDetailPanel dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		topPanel.add(dateTraceAppDetailPanel, new GridBagConstraints(0, section++, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		bpObservable.registerObserver(dateTraceAppDetailPanel);

		// BP Overall -aka- AROBpOverallResulsPanel
		BpTestStatisticsPanel testStatisticsPanel = new BpTestStatisticsPanel();
		topPanel.add(testStatisticsPanel, new GridBagConstraints(0, section++, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		bpObservable.registerObserver(testStatisticsPanel);

		// Separator
		topPanel.add(UIComponent.getInstance().getSeparator()
				, new GridBagConstraints(0, section++
										, 1, 1
										, 1.0, 0.0
										, GridBagConstraints.CENTER
										, GridBagConstraints.HORIZONTAL
										, new Insets(0, 0, 10, 0)
										, 0, 0));

		// BP Overall -aka- AROBpOverallResulsPanel
		BpTestsConductedPanel bpTestsConductedPanel = new BpTestsConductedPanel();
		topPanel.add(bpTestsConductedPanel, new GridBagConstraints(
				0, section++ 						//   int gridx, int gridy           
				, 1, 1 								// , int gridwidth,  int gridheight   
				, 0.0, 0.0 							// , double weightx, double weighty 
				, GridBagConstraints.CENTER 		// , int anchor                     
				, GridBagConstraints.HORIZONTAL 	// , int fill                       
				, new Insets(0, 0, 0, 0) 			// , Insets insets                  
				, 0, 0)); 							// , int ipadx, int ipady           
		bpObservable.registerObserver(bpTestsConductedPanel);
		
		return topPanel;
	}

	/**
	 * Refreshes the AROBestPracticesTab using the specified trace analysis
	 * data. This method is typically called when a new trace file is loaded.
	 * 
	 * @param analysisData
	 *            The trace analysis data.
	 */
	@Override
	public void refresh(AROTraceData aModel) {
		bpObservable.refreshModel(aModel);
	}

	@Override
	public synchronized void addComponentListener(ComponentListener listener) {
		super.addComponentListener(listener);
	}

	/**
	 * Triggers and expansion of any tableViews that need expanding before returning the container.
	 * @return a JPanel prepared for printing everything
	 */
	@Override
	public JPanel getPrintablePanel() {
		bpDetailResultsPanel.expand();
		return container;
	}

}
