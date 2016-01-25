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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.att.aro.commonui.DataTable;
import com.att.aro.commonui.DataTableModel;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.Profile;
import com.att.aro.model.ProfileException;
import com.att.aro.model.UserPreferences;

/**
 * Represents a dialog that allows the user to select a Device Profile to be
 * used in the trace analysis.
 */
public class SelectProfileDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SelectProfileDialog.class
			.getName());
	private static ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

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
	private ApplicationResourceOptimizer aro;
	private Profile selectedProfile;

	/**
	 * Initializes a new instance of the SelectProfileDialog class using the
	 * specified instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public SelectProfileDialog(Window owner) {
		super(owner);
		aro = (ApplicationResourceOptimizer) owner;
		initialize();
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
	 * Initializes the Profile Selection Dialog.
	 */
	private void initialize() {
		this.setSize(400, 300);
		this.setModal(true);
		this.setTitle(rb.getString("profile.title"));
		this.setLocationRelativeTo(getOwner());
		this.setContentPane(getJContentPane());
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
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
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
			openButton = new JButton(rb.getString("Button.open"));
			openButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new ConfigurationFrame(SelectProfileDialog.this, aro ,
							jProfilesTable.getSelectedItem()).setVisible(true);
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
			okButton.setText(rb.getString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					String filename = getFilenameTextField().getText();
					Profile profile = jProfilesTable.getSelectedItem();
					if (profile != null) {
						SelectProfileDialog.this.selectedProfile = profile;
						SelectProfileDialog.this.dispose();
					} else if (filename != null && filename.trim().length() > 0) {
						try {
							File file = new File(filename);
							SelectProfileDialog.this.selectedProfile = ProfileManager
									.getInstance().getProfile(file);
							SelectProfileDialog.this.dispose();
						} catch (FileNotFoundException e) {
							MessageDialogFactory.showErrorDialog(
									SelectProfileDialog.this,
									rb.getString("profile.filenotfound"));
						} catch (IOException e) {
							String message = "Unable to load device profile file: "
									+ filename;
							logger.log(Level.SEVERE, message, e);
							MessageDialogFactory.showUnexpectedExceptionDialog(
									SelectProfileDialog.this, e);
						} catch (ProfileException e) {
							MessageDialogFactory.showErrorDialog(
									SelectProfileDialog.this,
									MessageFormat.format(
											rb.getString("configuration.parseerror"),
											e.getMessage()));
						} catch (IllegalArgumentException e) {
							MessageDialogFactory.showErrorDialog(
									SelectProfileDialog.this,
									rb.getString("profile.invalidprofile"));
						}
					} else {
						MessageDialogFactory.showErrorDialog(
								SelectProfileDialog.this,
								rb.getString("profile.noselection"));
					}
				}

			});
		}
		return okButton;
	}

	/**
	 * Initializes and returns the "Cancel" button in the selection dialog.
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(rb.getString("Button.cancel"));
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
			browseButton.setText(rb.getString("Button.browse"));
			browseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					final JFileChooser fc = new JFileChooser(UserPreferences
							.getInstance().getLastProfileDirectory());
					if (fc.showOpenDialog(SelectProfileDialog.this) == JFileChooser.APPROVE_OPTION) {
						getProfilesTable().getSelectionModel().clearSelection();
						getOpenButton().setEnabled(false);
						getFilenameTextField().setText(
								fc.getSelectedFile().getAbsolutePath());
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
					rb.getString("profile.predefined"),
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

			try {
				jProfilesTable = new DataTable<Profile>(
						new ProfileListTableModel(ProfileManager.getInstance()
								.getPredefinedProfilesList()));
				jProfilesTable
						.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
				MessageDialogFactory.showErrorDialog(
						SelectProfileDialog.this,
						MessageFormat.format(
								rb.getString("profile.profileListError"),
								e.getMessage()));

			} catch (ProfileException e) {
				MessageDialogFactory.showErrorDialog(
						SelectProfileDialog.this,
						MessageFormat.format(
								rb.getString("profile.profileListError"),
								e.getMessage()));

			}
		}
		return jProfilesTable;
	}

	/**
	 * Implements the Data Model for the Profiles Table on the Profile Selection
	 * Dialog.
	 */
	private class ProfileListTableModel extends DataTableModel<Profile> {

		private static final int PROFILE_NAME = 0;
		private static final int PROFILE_TYPE = 1;

		public ProfileListTableModel(Collection<Profile> profileNames) {
			super(new String[] { "", "" }, profileNames);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected Object getColumnValue(Profile item, int columnIndex) {

			switch (columnIndex) {

			case PROFILE_NAME:
				return item.getName();
			case PROFILE_TYPE:
				return ResourceBundleManager.getEnumString(item
						.getProfileType());

			}
			return null;
		}

	}

} // @jve:decl-index=0:visual-constraint="10,10"
