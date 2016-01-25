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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import com.att.aro.ui.commonui.AROMenuAdder;
import com.att.aro.ui.commonui.BrowserLauncher;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;
import com.att.aro.ui.view.menu.help.AboutDialog;
import com.att.aro.ui.view.menu.help.DependenciesDialog;


public class AROHelpMenu implements ActionListener{
	private static AROHelpMenu instance = null;

	private static final String faqUriString = "http://developer.att.com/ARO/FAQ";
	private static final String userGuideUriString = "http://developer.att.com/ARO/userguide";
	private static final String analysisGuideUriString =
			"http://developer.att.com/ARO/analysisguide";
	private static final String forumUriString = "http://developer.att.com/ARO/forum";
	private static final String supportUriString = "http://developer.att.com/ARO/support";
	private static final String downloadUriString =
			"https://developer.att.com/application-resource-optimizer/get-aro/download";
	private static final String learnmoreUriString = "http://developer.att.com/ARO";

	private final AROMenuAdder menuAdder = new AROMenuAdder(this);

	private enum MenuItem {
		menu_help,
		menu_help_faq,
		menu_help_userguide,
		menu_help_analysisguide,
		menu_help_dependencies,
		menu_help_forum,
		menu_help_support,
		menu_help_downloads,
		menu_help_learnmore,
		menu_help_about
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		BrowserLauncher bg = new BrowserLauncher();
		if (menuAdder.isMenuSelected(MenuItem.menu_help_faq, e)) {
			bg.launchURI(faqUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_userguide, e)) {
			bg.launchURI(userGuideUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_analysisguide, e)) {
			bg.launchURI(analysisGuideUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_dependencies, e)) {
			DependenciesDialog dependenciesDialog =
					new DependenciesDialog(MainFrame.getWindow().getJFrame());
			if (dependenciesDialog.isNoticesRead()) {
				dependenciesDialog.setVisible(true);
			}
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_forum, e)) {
			bg.launchURI(forumUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_support, e)) {
			bg.launchURI(supportUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_downloads, e)) {
			bg.launchURI(downloadUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_learnmore, e)) {
			bg.launchURI(learnmoreUriString);
		}
		else if (menuAdder.isMenuSelected(MenuItem.menu_help_about, e)) {
			new AboutDialog(MainFrame.getWindow().getJFrame()).setVisible(true);
		}
	}

	private JMenu helpMenu;
	
	public synchronized static AROHelpMenu getInstance(){
		AROHelpMenu currentInstance = instance;
		if (currentInstance == null) {
			currentInstance = new AROHelpMenu();
			instance = currentInstance;
		}
		return currentInstance;
	}
	
	public JMenu getMenu(){
		if(helpMenu == null){
			helpMenu = new JMenu(ResourceBundleHelper.getMessageString(MenuItem.menu_help));
			helpMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_faq));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_userguide));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_analysisguide));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_dependencies));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_forum));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_support));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_downloads));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_learnmore));
			helpMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_help_about));
		}
		
		return helpMenu;
	}
}
