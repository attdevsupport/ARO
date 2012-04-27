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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.att.aro.commonui.DataTable;
import com.att.aro.model.ApplicationSelection;

/**
 * Represents the Filter Applications Dialog that appears when the user chooses the Select Applications 
 * menu item under the View menu. This dialog prompts the user to select applications from the list of 
 * applications found in the trace data. 
 */
public class FilterApplicationsDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JPanel jContentPane = null; // @jve:decl-index=0:visual-constraint="50,32"
	private JPanel jButtonPanel = null;
	private JPanel jButtonGrid = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JPanel mainPanel = null;
	private JPanel applicationsPanel = null;
	private JScrollPane jScrollPane = null;
	private DataTable<ApplicationSelection> jApplicationsTable = null;
	private FilterApplicationsTableModel jApplicationsTableModel = new FilterApplicationsTableModel();
	private Collection<ApplicationSelection> result;

	/**
	 * Initializes a new instance of the FilterApplicationsDialog( class using the specified instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            The ApplicationResourceOptimizer instance.
	 */
	public FilterApplicationsDialog(ApplicationResourceOptimizer owner) {
		super(owner);
		initialize();
	}

	/**
	 * Returns the ApplicationResourceOptimizer instance that is the owner.
	 * 
	 * @return The ApplicationResourceOptimizer instance.
	 * 
	 * @see java.awt.Window#getOwner()
	 */
	@Override
	public ApplicationResourceOptimizer getOwner() {
		return (ApplicationResourceOptimizer) super.getOwner();
	}

	/**
	 * Returns the collection of selected applications. 
	 * 
	 * @return CA collection of selected applications with their properties set.
	 */
	public Collection<ApplicationSelection> getResult() {
		return result;
	}

	/**
	 * Initializes the dialog.
	 */
	private void initialize() {
		this.setSize(473, 302);
		this.setModal(true);
		this.setTitle(rb.getString("filter.title"));
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(getOwner());
	}

	/**
	 * Initializes and returns ContentPane for the dialog.
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getMainPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * Initializes ButtonPanel
	 */
	private JPanel getJButtonPanel() {
		if (jButtonPanel == null) {
			jButtonPanel = new JPanel(new BorderLayout());
			jButtonPanel.add(getJButtonGrid(), BorderLayout.EAST);
		}
		return jButtonPanel;
	}

	/**
	 * Initializes and returns the gird containing the ok and cancel buttons.
	 */
	private JPanel getJButtonGrid() {
		if (jButtonGrid == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(10);
			jButtonGrid = new JPanel(gridLayout);
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			jButtonGrid.add(getOkButton(), null);
			jButtonGrid.add(getCancelButton(), null);
		}
		return jButtonGrid;
	}

	/**
	 * Initializes and returns the ok Button.
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(rb.getString("Button.ok"));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					// Store selected result
					result = jApplicationsTableModel.getData();
					FilterApplicationsDialog.this.dispose();
				}

			});
		}
		return okButton;
	}

	/**
	 * Initializes and returns the cancel Button.
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(rb.getString("Button.cancel"));
			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					FilterApplicationsDialog.this.dispose();
				}

			});
		}
		return cancelButton;
	}

	/**
	 * Initializes mainPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			mainPanel.add(getJButtonPanel(), BorderLayout.SOUTH);
			mainPanel.add(getApplicationsPanel(), BorderLayout.CENTER);
		}
		return mainPanel;
	}

	/**
	 * Initializes panel that contains the the list of applications.
	 */
	private JPanel getApplicationsPanel() {
		if (applicationsPanel == null) {
			applicationsPanel = new JPanel();
			applicationsPanel.setLayout(new BorderLayout());
			applicationsPanel.setBorder(BorderFactory.createTitledBorder(null,
					rb.getString("filter.border")));
			applicationsPanel.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return applicationsPanel;
	}

	/**
	 * Initializes Scroll Pane that contains the the list of applications.
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJApplicationsTable());
		}
		return jScrollPane;
	}

	/**
	 * Initializes and returns the table that contains the list of applications
	 * found in the trace data.
	 */
	private JTable getJApplicationsTable() {
		if (jApplicationsTable == null) {

			// Make sure to make a copy of the current data before modifying
			jApplicationsTableModel.setData(getOwner().getAnalysisData()
					.getApplicationSelections().values());
			jApplicationsTable = new DataTable<ApplicationSelection>(
					jApplicationsTableModel);
		}
		return jApplicationsTable;
	}

} // @jve:decl-index=0:visual-constraint="70,10"
