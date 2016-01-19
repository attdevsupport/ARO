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
package com.att.aro.ui.view.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.SharedAttributesProcesses;
import com.att.aro.ui.view.menu.profiles.ConfigurationFrame;
import com.att.aro.ui.view.menu.profiles.SelectProfileDialog;
/**
 * @author Harikrishna Yaramachu
 *
 */
public class AROProfileMenu implements ActionListener{
	
	private JMenu profileMenu;
	SharedAttributesProcesses parent;

	public AROProfileMenu(SharedAttributesProcesses parent){
		super();
		this.parent = parent;
	}
	
	public JMenu getMenu() {
		if(profileMenu == null){
			profileMenu = new JMenu(ResourceBundleHelper.getMessageString("menu.profile"));
			profileMenu.setMnemonic(KeyEvent.VK_UNDEFINED);

			profileMenu.add(getJLoadMenuItem());
			profileMenu.add(getJCustomizeMenuItem());
			
		}
		
		return profileMenu;
	}
	
	private JMenuItem getJLoadMenuItem(){
		JMenuItem jLoadJMenuItem = getMenuItemInstance();
		jLoadJMenuItem.setText(ResourceBundleHelper.getMessageString("menu.profile.load"));
		jLoadJMenuItem.addActionListener(this);
		return jLoadJMenuItem;
	}
	
	private JMenuItem getJCustomizeMenuItem(){
		JMenuItem jCustomizeMenuItem = getMenuItemInstance();
		jCustomizeMenuItem.setText(ResourceBundleHelper.getMessageString("menu.profile.customize"));
		jCustomizeMenuItem.addActionListener(this);
		return jCustomizeMenuItem;
	}

	private JMenuItem getMenuItemInstance(){
		return new JMenuItem();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent) {
		if(aEvent.getActionCommand().equalsIgnoreCase(ResourceBundleHelper.getMessageString("menu.profile.load"))){
			Object eventSource = aEvent.getSource();
			if (eventSource instanceof JMenuItem){
				SelectProfileDialog selectProfileDialog = new SelectProfileDialog(parent);
				selectProfileDialog.setVisible(true);
			}			
		} else if(aEvent.getActionCommand().equalsIgnoreCase(ResourceBundleHelper.getMessageString("menu.profile.customize"))){
			new ConfigurationFrame(parent.getFrame(), parent,
					parent.getProfile()).setVisible(true);
		}
	}

}
