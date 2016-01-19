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
package com.att.aro.ui.view.overviewtab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.view.AROModelObserver;

/**
 *
 */
public class OverviewTab extends TabPanelJPanel {
		
	private static final long serialVersionUID = 1L;

	OverviewTabTableSplitPane overviewSplitPanel;

	AROModelObserver overViewObservable;
	private IARODiagnosticsOverviewRoute parent;
	public OverviewTab(IARODiagnosticsOverviewRoute parent){
		super(true);
		this.parent = parent;
	}
	
	public JPanel layoutDataPanel(){
	
		setLayout(new BorderLayout());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds(0,0,screenSize.width, screenSize.height);
		
		overViewObservable = new AROModelObserver();
		
		DeviceNetworkProfilePanel deviceNetworkProfile = new DeviceNetworkProfilePanel();
		add(deviceNetworkProfile.layoutDataPanel(), BorderLayout.NORTH);
		overViewObservable.registerObserver(deviceNetworkProfile);
		
		JPanel chartsPanel = new JPanel();
		chartsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chartsPanel.setLayout(new GridLayout(1,2));
		
		
		
		FileTypesChartPanel fChartPanel = new FileTypesChartPanel();
		chartsPanel.add(fChartPanel.layoutDataPanel());
		overViewObservable.registerObserver(fChartPanel);
		
		JPanel benchmarkAndConnectionStatPanel = new JPanel();
		benchmarkAndConnectionStatPanel.setLayout(new GridLayout(2,1));
		
		TraceBenchmarkChartPanel benchmarkPanel = new TraceBenchmarkChartPanel();
		benchmarkAndConnectionStatPanel.add(benchmarkPanel.layoutDataPanel());
		overViewObservable.registerObserver(benchmarkPanel);
		
		ConnectionStatisticsChartPanel connectionStatPanel = new ConnectionStatisticsChartPanel();
		benchmarkAndConnectionStatPanel.add(connectionStatPanel.layoutDataPanel());
		overViewObservable.registerObserver(connectionStatPanel);
		
		chartsPanel.add(benchmarkAndConnectionStatPanel);
	
		JPanel chartAndTablePanel = new JPanel();
		chartAndTablePanel.setLayout(new BorderLayout());
		chartAndTablePanel.add(chartsPanel, BorderLayout.NORTH);
//		chartAndTablePanel.add(getJTablesSplitPane());
		overviewSplitPanel = new OverviewTabTableSplitPane(this);
		chartAndTablePanel.add(overviewSplitPanel.getOverviewSplitPanel()); 
		overViewObservable.registerObserver(overviewSplitPanel);
		add(chartAndTablePanel, BorderLayout.CENTER);
		
		return this; 
	}


	public void refresh(AROTraceData aModel){
		overViewObservable.refreshModel(aModel);
	}

	public void updateDiagnosticsTab(Object object){
	
		parent.updateDiagnosticsTab(object);
		
	}

	public void setHighlightedDuplicate(CacheEntry selectedDuplicate) {
		if (overviewSplitPanel != null) {
			overviewSplitPanel.setHighlightedDuplicate(selectedDuplicate);
		}
	}
}
