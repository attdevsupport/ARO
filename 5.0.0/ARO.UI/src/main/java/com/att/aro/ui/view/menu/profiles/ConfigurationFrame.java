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

package com.att.aro.ui.view.menu.profiles;

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

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.configuration.IProfileFactory;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.configuration.pojo.ProfileLTE;
import com.att.aro.core.configuration.pojo.ProfileWiFi;
import com.att.aro.mvc.AROController;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;
import com.att.aro.ui.view.SharedAttributesProcesses;

/**
 * Represents the configuration dialog that is displayed when the Customize menu
 * item in the Profile menu is selected.
 */
public class ConfigurationFrame extends JDialog implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final ResourceBundle DEFAULTBUNDLE = ResourceBundleHelper.getDefaultBundle();
	private static final Logger LOG = Logger.getLogger(ConfigurationFrame.class.getName());

	private JTable deviceAttributesTable;
	private JTable networkAttributesTable;
	
	private JMenuBar menuBar = null;
	private JMenu fileMenu = null;
	private JMenuItem applyAction = null;
	private JMenuItem saveAction = null;
	private JMenuItem saveAsAction = null;
	private JMenuItem exitAction = null;
	private SharedAttributesProcesses aroMain;

	@Autowired
	IProfileFactory factory;// = new ProfileFactoryImpl(); 

	private ConfigurationTableModel tableModel = new ConfigurationTableModel();
	private boolean saveAllowed = true;

	/**
	 * Initializes a new instance of the ConfigurationFrame class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window.
	 * 
	 * @param aroMain
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public ConfigurationFrame(SharedAttributesProcesses aroMain) {
		super(aroMain.getFrame());
		this.aroMain = aroMain;
		Profile profile = null;
		if (aroMain.isModelPresent() && aroMain.getProfile() == null) {
			AROController controller = ((MainFrame) aroMain).getController();
			if (controller.getTheModel() != null && controller.getTheModel().getAnalyzerResult()
					!= null) {
				profile = controller.getTheModel().getAnalyzerResult().getProfile();
			}
		}
		else {
			profile = aroMain.getProfile();
		}
		initialize(profile);
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
	public ConfigurationFrame(Window parent, SharedAttributesProcesses aroMain, Profile profile) {
		super(parent);
		this.aroMain = aroMain;
		initialize(profile);
	}

	public ConfigurationFrame(Window parent, SharedAttributesProcesses aroMain, Profile profile,
			boolean saveAllowed) {
		super(parent);
		this.aroMain = aroMain;
		this.saveAllowed = saveAllowed;
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
			fileMenu = new JMenu(DEFAULTBUNDLE.getString("configuration.menu.file"));
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
			applyAction = new JMenuItem(DEFAULTBUNDLE.getString("configuration.menu.file.apply"));
			applyAction.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						// Make sure current editor is closed
						TableCellEditor networkAttrEditor = networkAttributesTable.getCellEditor();
						TableCellEditor deviceAttrEditor = deviceAttributesTable.getCellEditor();
						if (networkAttrEditor != null) {
							networkAttrEditor.stopCellEditing();
						}
						if (deviceAttrEditor != null) {
							deviceAttrEditor.stopCellEditing();
						}

						Profile profile = tableModel.getProfile();
						aroMain.updateProfile(profile);
						// todo: udpate user preferences
						
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
			saveAction = new JMenuItem(DEFAULTBUNDLE.getString("configuration.menu.file.save"));
			saveAction.setEnabled(saveAllowed);
			if (saveAllowed) {
				saveAction.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if (saveConfigurationData()) {
							MessageDialogFactory.showMessageDialog(ConfigurationFrame.this,
									DEFAULTBUNDLE.getString("configuration.saved"));
							try {
								aroMain.updateProfile(tableModel.getProfile());
							} catch (ProfileException err) {
								handleProfileException(err);
							}
						}
					}
				});
			}
		}
		return saveAction;
	}

	/**
	 * Initializes and returns the Save As menu item under the file menu.
	 */
	private JMenuItem getSaveAsAction() {
		if (saveAsAction == null) {
			saveAsAction = new JMenuItem(DEFAULTBUNDLE.getString("configuration.menu.file.saveas"));
			saveAsAction.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (saveAsConfigurationData()) {
						MessageDialogFactory.showMessageDialog(
								ConfigurationFrame.this,
								DEFAULTBUNDLE.getString("configuration.saved"));
						try {
							aroMain.updateProfile(tableModel.getProfile());
						} catch (ProfileException err) {
							handleProfileException(err);
						}
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
					DEFAULTBUNDLE.getString("configuration.menu.file.exit"));
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
//		ApplicationContext context = new AnnotationConfigApplicationContext(AROConfig.class);
		factory = ContextAware.getAROConfigContext().getBean(IProfileFactory.class);
		
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
		networkAttributesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Create and set up the content pane.
		deviceAttributesTable = new JTable(tableModel.getDeviceAttributesTableModel());
		deviceAttributesTable.getTableHeader().setReorderingAllowed(false);
		deviceAttributesTable.setCellSelectionEnabled(true);
		deviceAttributesTable.setColumnSelectionAllowed(true);
		deviceAttributesTable.setRowSelectionAllowed(true);
		deviceAttributesTable.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

		// Set up column sizes.
		deviceAttributesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		setProfile(profile);
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
		this.setLocationRelativeTo(super.getOwner());

		new EnableEscKeyCloseDialog(getRootPane(), this);
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
	private void setProfile(Profile chosenProfile) {
		String name;
		Profile profile = chosenProfile; 
		synchronized(factory){
			if (profile==null){
				profile = factory.createLTEdefault();
				name = "lte";
			} else {
				name = profile.getName();
			}
			if (name == null){
				name = "";
			}
			setTitle(MessageFormat.format(DEFAULTBUNDLE.getString("configuration.title"), name));
			tableModel.setProfile(profile);
		}
	}

	/**
	 * Implements the functionality of Save menu item.
	 */
	private boolean saveConfigurationData() {
		synchronized(tableModel){
			try {
				Profile profile = tableModel.getProfile();
				if (profile.getName() != null) {
	
					// Make sure current editor is closed
					TableCellEditor networkAttrEditor = networkAttributesTable.getCellEditor();
					TableCellEditor deviceAttrEditor = deviceAttributesTable.getCellEditor();
					if (networkAttrEditor != null) {
						networkAttrEditor.stopCellEditing();
					}
					if (deviceAttrEditor != null) {
						deviceAttrEditor.stopCellEditing();
					}
	
					saveProfile(null, profile);
					setProfile(profile);
	
					return true;
				}
			} catch (ProfileException e) {
				handleProfileException(e);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "IOException saving profile", e);
				MessageDialogFactory dialog = new MessageDialogFactory();
				dialog.showUnexpectedExceptionDialog(ConfigurationFrame.this, e);
			}
			return false;
		}
	}// end saveConfigurationData()

	private void saveProfile(File saveToFile, Profile profile)
			throws IOException {
		if (profile != null) {
			String filename = profile.getName() != null ? profile.getName() :
				saveToFile.getAbsolutePath();
			int dotIndex = filename.indexOf(".");
			if (dotIndex < 0) {
				filename += ".conf";
			}
			else if (dotIndex == filename.length() - 1) {
				filename += "conf";
			}

			if (profile instanceof ProfileWiFi) {
				factory.saveWiFi(filename, (ProfileWiFi) profile);
			} else if (profile instanceof ProfileLTE) {
				factory.saveLTE(filename, (ProfileLTE) profile);
			} else {
				factory.save3G(filename, (Profile3G) profile);
			}
		}
	}

	/**
	 * Implements the functionality of Save As menu item.
	 */
	private boolean saveAsConfigurationData() {
		synchronized (tableModel) {
			JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getLastProfileDirectory());
			fileChooser.setDialogTitle(DEFAULTBUNDLE.getString("configuration.savefile"));
			fileChooser.setMultiSelectionEnabled(false);
			int returnVal = fileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					File file = fileChooser.getSelectedFile();
					if (file.exists() && MessageDialogFactory.showConfirmDialog(this,
								DEFAULTBUNDLE.getString("configuration.fileExists"),
								DEFAULTBUNDLE.getString("configuration.confirm"),
								JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							return false;
					}
	
					// Make sure current editor is closed
					TableCellEditor networkAttrEditor = networkAttributesTable.getCellEditor();
					TableCellEditor deviceAttrEditor = deviceAttributesTable.getCellEditor();
					if (networkAttrEditor != null) {
						networkAttrEditor.stopCellEditing();
					}
					if (deviceAttrEditor != null) {
						deviceAttrEditor.stopCellEditing();
					}
	
					Profile profile = tableModel.getProfile();
					saveProfile(file, profile);
					setProfile(profile);
					return true;
				} catch (IOException ioException) {
					MessageDialogFactory dialog = new MessageDialogFactory();
					dialog.showUnexpectedExceptionDialog(ConfigurationFrame.this, ioException);
				} catch (ProfileException profileException) {
					handleProfileException(profileException);
				}
			}
			return false;
		}
	}

	private void handleProfileException(ProfileException profileException) {
		MessageDialogFactory dialog = new MessageDialogFactory();
		dialog.showErrorDialog(this, MessageFormat.format(ResourceBundleHelper.getMessageString("configuration.parseerror"), profileException.getMessage()));
	}

}// end ConfigurationFrame Class
