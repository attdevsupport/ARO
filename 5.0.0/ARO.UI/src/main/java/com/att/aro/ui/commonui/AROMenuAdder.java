/**
 * 
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
 * 
 * @author Nathan F Syfrig
 *
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
