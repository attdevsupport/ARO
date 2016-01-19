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

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observable;

import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.exception.AROUIPanelException;

/**
 * This class encapsulates the base observable and refresh implementations applicable to
 * all UI tabs.  It is here because some tabs are JPanels and at least one is a JScrollPane
 * (StatisticsTab).
 * 
 * @author Nathan F Syfrig
 *
 */
public class TabPanelSupport {
	private final Component component;

	private boolean lazyLoadFlag;
	private final boolean alwaysLoad;
	private AROTraceData aroModel;

	public TabPanelSupport(Component component, boolean alwaysLoad) {
		if (component.getName() == null) {
			component.setName(component.getClass().getName());
		}
		validateComponent(component);
		this.component = component;
		this.alwaysLoad = alwaysLoad;
		addListener();
	}
	public TabPanelSupport(Component component) {
		this(component, false);
	}

	private void validateComponent(Component component) {
		if (!(component instanceof IUITabPanelLayoutUpdate)) {
			throw new AROUIPanelException(
					"Component argument must implement IUITabPanelLayoutUpdate");
		}
	}
	
	public void addListener(){
		component.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				if (lazyLoadFlag){
					((IUITabPanelLayoutUpdate) component).refresh(aroModel);
					lazyLoadFlag = false;
				}
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	/**
	 * @return the aroModel
	 */
	public AROTraceData getAroModel() {
		return aroModel;
	}

	public void update(Observable observable, AROTraceData aroModel, boolean visible){
		this.aroModel = aroModel;
		if(visible || alwaysLoad){
			((IUITabPanelLayoutUpdate) component).refresh(aroModel);
			this.lazyLoadFlag = false;
		} else {
			this.lazyLoadFlag = true;
		}
	}


	@Override
	public String toString() {
		return "TabPanelSupport [component=" + component + ", lazyLoadFlag="
				+ lazyLoadFlag + ", aroModel=" + aroModel + "]";
	}
}
