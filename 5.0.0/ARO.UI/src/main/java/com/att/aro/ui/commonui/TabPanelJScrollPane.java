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
package com.att.aro.ui.commonui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollPane;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.exception.AROUIPanelException;

/**
 * @author Harikrishna Yaramachu
 *
 */
public abstract class TabPanelJScrollPane extends JScrollPane implements Observer,
		IUITabPanelLayoutUpdate {
	private static final long serialVersionUID = 1L;

	private final TabPanelSupport tabPanelSupport;

	public TabPanelJScrollPane(){
		tabPanelSupport = new TabPanelSupport(this);
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
}
