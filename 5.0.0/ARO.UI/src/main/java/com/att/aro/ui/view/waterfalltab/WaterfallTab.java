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
package com.att.aro.ui.view.waterfalltab;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.commonui.UIComponent;
import com.att.aro.ui.view.AROModelObserver;

public class WaterfallTab extends TabPanelJPanel{
	
	AROModelObserver waterfallObservable;
	private IARODiagnosticsOverviewRoute route;

	public WaterfallTab(IARODiagnosticsOverviewRoute route){
		super();
		this.route = route;
	}
	public JPanel layoutDataPanel(){
		
		this.setLayout(new BorderLayout());
		//Get the blue header panel with ATT logo.
		this.add(UIComponent.getInstance().getLogoHeader("Waterfall.title"), BorderLayout.NORTH);
		WaterfallPanel wfPanel = new WaterfallPanel(this);
		this.add(wfPanel.layoutDataPanel(), BorderLayout.CENTER);
		waterfallObservable = new AROModelObserver();
		waterfallObservable.registerObserver(wfPanel);
		
		return this;
		
	}
	
	public void refresh(AROTraceData aModel){
		waterfallObservable.refreshModel(aModel);
	}
	public void updateMainFrame(Object object){
		
		route.updateDiagnosticsTab(object);
		
	}

}
