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
package com.att.aro.ui.view.menu.profiles;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.core.ILogger;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.SharedAttributesProcesses;
/**
 * Represents a dialog that allows the user to select a Device Profile to be
 * used in the trace analysis.
 */
public class SelectProfileDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);

	private JPanel jContentPane = null;
	private JPanel jButtonPanel = null;
	private JPanel selectionPanel = null;
	private JPanel jButtonGrid = null;
	private JButton openButton = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JPanel selectFilePanel = null;
	private JTextField filenameTextField = null;
	private JButton browseButton = null;
	private JPanel selectProfilePanel = null;
	private JScrollPane profileListPanel = null;
	private DataTable<Profile> jProfilesTable;
	private Profile selectedProfile;
	private SharedAttributesProcesses parent;
	/**
	 * Initializes a new instance of the SelectProfileDialog class using the
	 * specified instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public SelectProfileDialog(SharedAttributesProcesses parent) {
		super((parent).getFrame());
		this.parent = parent;
		this.setSize(400, 300);
		this.setModal(true);
		this.setTitle(ResourceBundleHelper.getMessageString("profile.title"));
		this.setLocationRelativeTo(getOwner());
		this.setContentPane(getJContentPane());
		selectedProfile = parent.getProfile();
		jProfilesTable.selectItem(selectedProfile);
		new EnableEscKeyCloseDialog(getRootPane(), this);
	}

	/**
	 * Returns the Device Profile that was selected using this dialog, or null
	 * if no selection was made.
	 * 
	 * @return A Profile object, or null if no profile was selected in the
	 *         dialog.
	 */
	public Profile getSelectedProfile() {
		return selectedProfile;
	}

	/**
	 * This method initializes ContentPane for the JDialog
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.setName("");
			jContentPane.add(getSelectionPanel(), BorderLayout.CENTER);
			jContentPane.add(getJButtonPanel(), BorderLayout.SOUTH);
		}
		return jContentPane;
	}

	/**
	 * Initializes jButtonPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = -1;
			gridBagConstraints.gridy = -1;
			jButtonPanel = new JPanel();
			jButtonPanel.setLayout(new BorderLayout());
			jButtonPanel.add(getJButtonGrid(), BorderLayout.EAST);
		}
		return jButtonPanel;
	}

	/**
	 * Initializes profile selection Panel.
	 */
	private JPanel getSelectionPanel() {
		if (selectionPanel == null) {
			selectionPanel = new JPanel();
			selectionPanel.setLayout(new BorderLayout());
			selectionPanel.add(getSelectProfilePanel(), BorderLayout.CENTER);
			selectionPanel.add(getSelectFilePanel(), BorderLayout.SOUTH);
		}
		return selectionPanel;
	}

	/**
	 * Initializes and returns the JPanel that holds the Ok and Cancel button.
	 */
	private JPanel getJButtonGrid() {
		if (jButtonGrid == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(10);
			jButtonGrid = new JPanel();
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			jButtonGrid.setLayout(gridLayout);
			jButtonGrid.add(getOpenButton(), null);
			jButtonGrid.add(getOkButton(), null);
			jButtonGrid.add(getCancelButton(), null);
		}
		return jButtonGrid;
	}

	/**
	 * Initializes and returns the "Open" button on the selection dialog
	 */
	private JButton getOpenButton() {

		if (openButton == null) {
			openButton = new JButton(ResourceBundleHelper.getMessageString("Button.open"));
			openButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					new ConfigurationFrame(parent.getFrame(), parent,
							jProfilesTable.getSelectedItem(), false).setVisible(true);
				}
			});
			openButton.setEnabled(false);
		}
		return openButton;
	}

	/**
	 * Initializes and returns the "OK" button on the selection dialog.
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(ResourceBundleHelper.getMessageString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					String filename = getFilenameTextField().getText();
					Profile profile = jProfilesTable.getSelectedItem();
					if (profile == null) {
						if (filename == null || filename.trim().length() == 0) {
							logger.debug(ResourceBundleHelper.getMessageString("profile.noselection"));
							MessageDialogFactory.getInstance().showErrorDialog(SelectProfileDialog.this,ResourceBundleHelper.getMessageString("profile.noselection"));
						} else {
							try {
								File file = new File(filename);
								selectedProfile = ProfileManager.getInstance().getProfile(file);
								profile = selectedProfile;
								profile.setName(file.getPath());
								parent.updateProfile(profile);
								dispose();
							} catch (FileNotFoundException fnfException) {
								logger.debug(fnfException.getMessage(), fnfException);
								MessageDialogFactory.getInstance().showErrorDialog(SelectProfileDialog.this,ResourceBundleHelper.getMessageString("profile.filenotfound"));
							} catch (IOException ioException) {
								String message = "Unable to load device profile file: "+ filename;
								logger.debug(message, ioException);
								MessageDialogFactory.getInstance().showErrorDialog(SelectProfileDialog.this, ioException.getMessage());
							} catch (ProfileException profileException) {
								logger.debug(profileException.getMessage(), profileException);
								MessageDialogFactory.getInstance().showErrorDialog(SelectProfileDialog.this,
										MessageFormat.format(ResourceBundleHelper.getMessageString("configuration.parseerror"), profileException.getMessage()));
							} catch (IllegalArgumentException illegalArgException) {
								logger.debug(illegalArgException.getMessage(), illegalArgException);
								MessageDialogFactory.getInstance().showErrorDialog(SelectProfileDialog.this, illegalArgException.getMessage());
							}
						}
					} else {
						SelectProfileDialog.this.selectedProfile = profile;
						parent.updateProfile(profile);
						SelectProfileDialog.this.dispose();
					}
				}
			});
			getRootPane().setDefaultButton(okButton);
		}
		return okButton;
	}

	/**
	 * Initializes and returns the "Cancel" button in the selection dialog.
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(ResourceBundleHelper.getMessageString("Button.cancel"));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					SelectProfileDialog.this.dispose();
				}

			});
		}
		return cancelButton;
	}

	/**
	 * Initializes selectFilePanel
	 */
	private JPanel getSelectFilePanel() {
		if (selectFilePanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.CENTER;
			gridBagConstraints1.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints1.weightx = 1.0;
			selectFilePanel = new JPanel();
			selectFilePanel.setLayout(new GridBagLayout());
			selectFilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10,
					10, 10));
			selectFilePanel.add(getFilenameTextField(), gridBagConstraints1);
			selectFilePanel.add(getBrowseButton(), gridBagConstraints2);
		}
		return selectFilePanel;
	}

	/**
	 * Initializes and returns the file name browse Text Field.
	 */
	private JTextField getFilenameTextField() {
		if (filenameTextField == null) {
			filenameTextField = new JTextField();
		}
		return filenameTextField;
	}

	/**
	 * Initializes and returns the browse Button.
	 */
	private JButton getBrowseButton() {
		if (browseButton == null) {
			browseButton = new JButton();
			browseButton.setText(ResourceBundleHelper.getMessageString("Button.browse"));
			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					final JFileChooser fileChooser = new JFileChooser(UserPreferences.getInstance().getLastProfileDirectory());
					if (fileChooser.showOpenDialog(SelectProfileDialog.this) == JFileChooser.APPROVE_OPTION) {
						getProfilesTable().getSelectionModel().clearSelection();
						getOpenButton().setEnabled(false);
						getFilenameTextField().setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
		}
		return browseButton;
	}

	/**
	 * Initializes selectProfilePanel
	 */
	private JPanel getSelectProfilePanel() {
		if (selectProfilePanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.gridy = 0;
			selectProfilePanel = new JPanel(new BorderLayout());
			selectProfilePanel.setBorder(BorderFactory.createTitledBorder(null,
					ResourceBundleHelper.getMessageString("profile.predefined"),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			selectProfilePanel.add(getProfileListPanel(), BorderLayout.CENTER);
		}
		return selectProfilePanel;
	}

	/**
	 * Initializes profile list Scroll Pane.
	 */
	private JScrollPane getProfileListPanel() {
		if (profileListPanel == null) {
			profileListPanel = new JScrollPane(getProfilesTable());
		}
		return profileListPanel;
	}

	/**
	 * Initializes and returns the JTable that displays the the list of
	 * predefined profiles with their types on the SElect Profile dialog.
	 */
	private DataTable<Profile> getProfilesTable() {

		if (jProfilesTable == null) {
			MessageDialogFactory dialog = new MessageDialogFactory();
			try {
				jProfilesTable = new DataTable<Profile>(new ProfileListTableModel(ProfileManager.getInstance().getPredefinedProfilesList()));
				jProfilesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				jProfilesTable.getSelectionModel().addListSelectionListener(
						new ListSelectionListener() {
							@Override
							public void valueChanged(ListSelectionEvent e) {
								if( jProfilesTable.getSelectedRowCount() != -1){
									getOpenButton().setEnabled(true);
								}
								getFilenameTextField().setText(null);
							}
						});

			} catch (IOException e) {
				dialog.showErrorDialog(
						SelectProfileDialog.this,
						MessageFormat.format(
								ResourceBundleHelper.getMessageString("profile.profileListError"),
								e.getMessage()));

			} catch (ProfileException e) {
				dialog.showErrorDialog(
						SelectProfileDialog.this,
						MessageFormat.format(
								ResourceBundleHelper.getMessageString("profile.profileListError"),
								e.getMessage()));

			}
		}
		return jProfilesTable;
	}

} 
