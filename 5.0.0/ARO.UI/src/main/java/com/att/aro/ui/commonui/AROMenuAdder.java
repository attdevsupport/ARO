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
package com.att.aro.ui.commonui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * This allows easy menu item additions with attached listener.  Also includes an 'is menu selected'
 * checker.
 */

public class AROMenuAdder {
	private final ActionListener listener;

	public AROMenuAdder(ActionListener listener) {
		this.listener = listener;
	}


	public JMenuItem getMenuItemInstance(Enum<?> menuItemKey){
		JMenuItem helpMenuItem = new JMenuItem(ResourceBundleHelper.getMessageString(menuItemKey));
		helpMenuItem.addActionListener(listener);
		return helpMenuItem;
	}

	public JCheckBoxMenuItem getCheckboxMenuItemInstance(Enum<?> menuItemKey){
		JCheckBoxMenuItem helpMenuItem = new JCheckBoxMenuItem(
				ResourceBundleHelper.getMessageString(menuItemKey));
		helpMenuItem.addActionListener(listener);
		return helpMenuItem;
	}

	public boolean isMenuSelected(Enum<?> key, ActionEvent event) {
		return ResourceBundleHelper.getMessageString(key).equals(
				event.getActionCommand());
	}


	@Override
	public String toString() {
		return "AROMenuAdder [listener=" + listener + "]";
	}
}
