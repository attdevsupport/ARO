/*
 * Copyright 2012 AT&T
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

package com.att.aro.main;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellEditor;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.Profile;
import com.att.aro.model.ProfileException;
import com.att.aro.model.UserPreferences;

/**
 * Represents the configuration dialog that is displayed when the Customize menu
 * item in the Profile menu is selected.
 */
public class ConfigurationFrame extends JDialog implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final Logger logger = Logger
			.getLogger(ApplicationResourceOptimizer.class.getName());

	private JTable deviceAttributesTable;
	private JTable networkAttributesTable;
	
	private JMenuBar menuBar = null;
	private JMenu fileMenu = null;
	private JMenuItem applyAction = null;
	private JMenuItem saveAction = null;
	private JMenuItem saveAsAction = null;
	private JMenuItem exitAction = null;

	private ApplicationResourceOptimizer aroMain;
	
	ConfigurationTableModel tableModel = new ConfigurationTableModel();

	/**
	 * Initializes a new instance of the ConfigurationFrame class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window.
	 * 
	 * @param aroMain
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public ConfigurationFrame(ApplicationResourceOptimizer aroMain) {
		super(aroMain);
		this.aroMain = aroMain;
		initialize(aroMain.getProfile());
	}

	/**
	 * Initializes a new instance of ConfigurationFrame using the specified
	 * parent window, ARO instance, and profile.
	 * 
	 * @param parent
	 *            The parent window that invokes the Configuration dialog.
	 * @param aroMain
	 *            The ApplicationResourceOptimizer instance.
	 * @param profile
	 *            The profile for which the details are to be displayed in the
	 *            customization table.
	 */
	public ConfigurationFrame(Window parent,
			ApplicationResourceOptimizer aroMain, Profile profile) {
		super(parent);
		this.aroMain = aroMain;
		initialize(profile);
	}

	/**
	 * Initializes and returns the menu bar for the configuration dialog that
	 * appears when we select the customize menu item under profile menu.
	 */
	private JMenuBar getConfigMenuBar() {
		if (menuBar == null) {
			menuBar = new JMenuBar();
			menuBar.add(getFileMenu());
		}
		return menuBar;
	}

	/**
	 * Initializes and returns File menu in the dialog.
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu(rb.getString("configuration.menu.file"));
			fileMenu.add(getApplyAction());
			fileMenu.add(getSaveAction());
			fileMenu.add(getSaveAsAction());
			fileMenu.addSeparator();
			fileMenu.add(getExitAction());
		}
		return fileMenu;
	}

	/**
	 * Initializes and returns the Apply menu item under the file menu.
	 */
	private JMenuItem getApplyAction() {
		if (applyAction == null) {
			applyAction = new JMenuItem(
					rb.getString("configuration.menu.file.apply"));
			applyAction.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						// Make sure current editor is closed
						TableCellEditor networkAttrEditor = networkAttributesTable
								.getCellEditor();
						TableCellEditor deviceAttrEditor = deviceAttributesTable
								.getCellEditor();
						if (networkAttrEditor != null) {
							networkAttrEditor.stopCellEditing();
						}
						if (deviceAttrEditor != null) {
							deviceAttrEditor.stopCellEditing();
						}

						Profile profile = tableModel.getProfile();
						aroMain.setProfile(profile);
					} catch (IOException e) {
						logger.log(Level.SEVERE,
								"IOException applying profile", e);
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ConfigurationFrame.this, e);
					} catch (ProfileException e) {
						handleProfileException(e);
					}
				}
			});
		}
		return applyAction;
	}

	/**
	 * Initializes and returns the Save menu item under the file menu.
	 */
	private JMenuItem getSaveAction() {
		if (saveAction == null) {
			saveAction = new JMenuItem(
					rb.getString("configuration.menu.file.save"));
			saveAction.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (saveConfigurationData()) {
						MessageDialogFactory.showMessageDialog(
								ConfigurationFrame.this,
								rb.getString("configuration.saved"));
					}
				}
			});
		}
		return saveAction;
	}

	/**
	 * Initializes and returns the Save As menu item under the file menu.
	 */
	private JMenuItem getSaveAsAction() {
		if (saveAsAction == null) {
			saveAsAction = new JMenuItem(
					rb.getString("configuration.menu.file.saveas"));
			saveAsAction.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (saveAsConfigurationData()) {
						MessageDialogFactory.showMessageDialog(
								ConfigurationFrame.this,
								rb.getString("configuration.saved"));
					}
				}
			});
		}
		return saveAsAction;
	}

	/**
	 * Initializes and returns the Exit menu item under the file menu.
	 */
	private JMenuItem getExitAction() {
		if (exitAction == null) {
			exitAction = new JMenuItem(
					rb.getString("configuration.menu.file.exit"));
			exitAction.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					ConfigurationFrame.this.dispose();
				}

			});
		}
		return exitAction;
	}

	/**
	 * Initializes the table which shows the profile details on the dialog.
	 */
	private void initialize(Profile profile) {
		setModal(true);
		setPreferredSize(new Dimension(475, 600));
		setLocationRelativeTo(getOwner());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// Create and set up the content pane.
		networkAttributesTable = new JTable(tableModel.getNetworkAttributesTableModel());
		networkAttributesTable.getTableHeader().setReorderingAllowed(false);
		networkAttributesTable.setCellSelectionEnabled(true);
		networkAttributesTable.setColumnSelectionAllowed(true);
		networkAttributesTable.setRowSelectionAllowed(true);
		networkAttributesTable.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

		// Set up column sizes.
		networkAttributesTable
				.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Create and set up the content pane.
		deviceAttributesTable = new JTable(tableModel.getDeviceAttributesTableModel());
		deviceAttributesTable.getTableHeader().setReorderingAllowed(false);
		deviceAttributesTable.setCellSelectionEnabled(true);
		deviceAttributesTable.setColumnSelectionAllowed(true);
		deviceAttributesTable.setRowSelectionAllowed(true);
		deviceAttributesTable.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

		// Set up column sizes.
		deviceAttributesTable
				.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		setJMenuBar(getConfigMenuBar());

		// Create the scroll pane and add the table to it.
		JScrollPane networkAttributesScrollPane = new JScrollPane(networkAttributesTable);
		JScrollPane deviceAttributesScrollPane = new JScrollPane(deviceAttributesTable);

		// Add the scroll pane to this panel.
		setLayout(new GridLayout(2,1));
		add(networkAttributesScrollPane );
		add(deviceAttributesScrollPane);

		// Display the window.
		pack();
		// this.setLocationRelativeTo((ApplicationResourceOptimizer)
		// super.getOwner());
		this.setLocationRelativeTo(super.getOwner());
		setProfile(profile);
	}

	/**
	 * Sets the profile selected from the list of device profiles that appears
	 * on the dialog which gets displayed when we click the open menu item. The
	 * table data in the configuration dialog gets updated when we change the
	 * Profile.
	 * 
	 * @param profile
	 *            The profile to be set to the dialog.
	 */
	private synchronized void setProfile(Profile profile) {
		String name = profile.getName();
		setTitle(MessageFormat.format(rb.getString("configuration.title"),
				name != null ? name : ""));
		this.saveAction.setEnabled(profile.getFile() != null);
		this.tableModel.setProfile(profile);
	}

	/**
	 * Implements the functionality of Save menu item.
	 */
	private synchronized boolean saveConfigurationData() {
		try {
			Profile profile = tableModel.getProfile();
			if (profile.getFile() != null) {

				// Make sure current editor is closed
				TableCellEditor networkAttrEditor = networkAttributesTable
						.getCellEditor();
				TableCellEditor deviceAttrEditor = deviceAttributesTable
						.getCellEditor();
				if (networkAttrEditor != null) {
					networkAttrEditor.stopCellEditing();
				}
				if (deviceAttrEditor != null) {
					deviceAttrEditor.stopCellEditing();
				}

				profile.saveToFile(profile.getFile());
				return true;
			}
		} catch (ProfileException e) {
			handleProfileException(e);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException saving profile", e);
			MessageDialogFactory.showUnexpectedExceptionDialog(
					ConfigurationFrame.this, e);
		}
		return false;
	}// end saveConfigurationData()

	/**
	 * Implements the functionality of Save As menu item.
	 */
	private synchronized boolean saveAsConfigurationData() {
		JFileChooser fc = new JFileChooser(UserPreferences.getInstance()
				.getLastProfileDirectory());
		fc.setDialogTitle(rb.getString("configuration.savefile"));
		fc.setMultiSelectionEnabled(false);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (MessageDialogFactory.showConfirmDialog(this,
							rb.getString("configuration.fileExists"),
							rb.getString("configuration.confirm"),
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						return false;
					}
				}

				// Make sure current editor is closed
				TableCellEditor networkAttrEditor = networkAttributesTable
						.getCellEditor();
				TableCellEditor deviceAttrEditor = deviceAttributesTable
						.getCellEditor();
				if (networkAttrEditor != null) {
					networkAttrEditor.stopCellEditing();
				}
				if (deviceAttrEditor != null) {
					deviceAttrEditor.stopCellEditing();
				}

				Profile profile = tableModel.getProfile();
				profile.saveToFile(file);
				setProfile(profile);
				return true;
			} catch (IOException e) {
				MessageDialogFactory.showUnexpectedExceptionDialog(
						ConfigurationFrame.this, e);
			} catch (ProfileException e) {
				handleProfileException(e);
			}
		}
		return false;
	}// end saveAsConfigurationData()

	private void handleProfileException(ProfileException e) {
		MessageDialogFactory.showErrorDialog(this, MessageFormat.format(
				rb.getString("configuration.parseerror"), e.getMessage()));
	}

}// end ConfigurationFrame Class
