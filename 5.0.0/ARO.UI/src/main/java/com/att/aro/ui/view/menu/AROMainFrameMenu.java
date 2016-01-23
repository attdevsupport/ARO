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
package com.att.aro.ui.view.menu;

import javax.swing.JMenuBar;

import com.att.aro.ui.view.SharedAttributesProcesses;

public class AROMainFrameMenu {

	SharedAttributesProcesses parent;
	public AROMainFrameMenu(SharedAttributesProcesses parent){
		super();
		this.parent = parent;
	}
	
	public JMenuBar getAROMainFileMenu(){

		JMenuBar aroJMenuBar = new JMenuBar();
		AROFileMenu aFileMenu = new AROFileMenu(parent);
		aroJMenuBar.add(aFileMenu.getMenu());
		
		AROProfileMenu aProfileMenu = new AROProfileMenu(parent);
		aroJMenuBar.add(aProfileMenu.getMenu());
		
		AROToolMenu aToolMenu = new AROToolMenu(parent);
		aroJMenuBar.add(aToolMenu.getMenu());
	
		AROViewMenu aViewMenu = new AROViewMenu(parent);
		aroJMenuBar.add(aViewMenu.getMenu());
		
		ARODataCollectorMenu aDataCollectorMenu = new ARODataCollectorMenu(parent);
		aroJMenuBar.add(aDataCollectorMenu.getMenu());
		
		AROHelpMenu aHelpMenu = AROHelpMenu.getInstance();
		aroJMenuBar.add(aHelpMenu.getMenu());

		return aroJMenuBar;
	}
}
