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
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.att.aro.commonui.DataTable;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.AnalysisFilter;
import com.att.aro.model.ApplicationSelection;
import com.att.aro.model.TraceData;

/**
 * Represents the Filter Applications Dialog that appears when the user chooses
 * the Select Applications/IPs menu item under the View menu. This dialog prompts
 * the user to select applications and IP addresses that are found in the
 * trace data.
 */
public class FilterApplicationsAndIpDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JPanel jButtonPanel = null;
	private JPanel jButtonGrid = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JPanel mainPanel = null;
	private DataTable<ApplicationSelection> jApplicationsTable = null;
	private DataTable<FilterIpAddressesTableModel.AppIPAddressSelection> jIpAddressTable = null;
	private FilterApplicationsTableModel jApplicationsTableModel;
	private FilterIpAddressesTableModel jIpAddressesTableModel;
	private AnalysisFilter filter;

	/**
	 * Initializes a new instance of the FilterApplicationsAndIpDialog class using
	 * the specified instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            The ApplicationResourceOptimizer instance.
	 */
	public FilterApplicationsAndIpDialog(ApplicationResourceOptimizer owner) {
		super(owner);
		
		TraceData.Analysis analysisData = owner.getAnalysisData();
		this.filter = analysisData.getFilter();
		this.jIpAddressesTableModel = new FilterIpAddressesTableModel(this.filter);
		this.jApplicationsTableModel = new FilterApplicationsTableModel(this.filter);
		this.jApplicationsTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getColumn() == FilterApplicationsTableModel.SELECT_COL || e.getColumn() == FilterApplicationsTableModel.COLOR_COL) {
					for (int row = e.getFirstRow(); row <= e.getLastRow(); ++row) {
						if (row >= 0 && row < jApplicationsTableModel.getRowCount()) {
							ApplicationSelection as = jApplicationsTableModel.getValueAt(row);
							String appName = as.getAppName();
							for (FilterIpAddressesTableModel.AppIPAddressSelection is : jIpAddressesTableModel.getData()) {
								if (appName == is.getAppName() || (appName != null && appName.equals(is.getAppName()))) {
									if ((as.isSelected() || !is.getIpSelection().isSelected()) && e.getColumn() == FilterApplicationsTableModel.COLOR_COL) {
										is.getIpSelection().setColor(as.getColor());
									}
									if (e.getColumn() == FilterApplicationsTableModel.SELECT_COL) {
										is.getIpSelection().setSelected(as.isSelected());
									}
								}
							}
						}
					}
					jIpAddressesTableModel.fireTableDataChanged();
				}
			}
			
		});
		this.jIpAddressesTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getColumn() == FilterIpAddressesTableModel.SELECT_COL) {
					for (int row = e.getFirstRow(); row <= e.getLastRow(); ++row) {
						if (row >= 0 && row < jIpAddressesTableModel.getRowCount()) {
							FilterIpAddressesTableModel.AppIPAddressSelection ipSel = jIpAddressesTableModel.getValueAt(row);
							String appName = ipSel.getAppName();
							boolean b = ipSel.getIpSelection().isSelected();
							if (b) {
								for (FilterIpAddressesTableModel.AppIPAddressSelection is : jIpAddressesTableModel.getData()) {
									if (appName == is.getAppName() || (appName != null && appName.equals(is.getAppName()))) {
										b &= is.getIpSelection().isSelected();
									}
								}
							}
							
							for (ApplicationSelection as : jApplicationsTableModel.getData()) {
								if (appName == as.getAppName() || (appName != null && appName.equals(as.getAppName()))) {
									as.setSelected(b);
									break;
								}
							}
							jApplicationsTableModel.fireTableDataChanged();
						}
					}
				}
			}
			
		});
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
	 * Initializes the dialog.
	 */
	private void initialize() {
		this.setSize(600, 420);
		this.setModal(true);
		this.setTitle(rb.getString("filter.title"));
		this.setLayout(new BorderLayout());
		this.add(getMainPanel(), BorderLayout.CENTER);
		this.setLocationRelativeTo(getOwner());
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
					try {
						getOwner().refresh(filter);
						FilterApplicationsAndIpDialog.this.dispose();
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(FilterApplicationsAndIpDialog.this, e);
					}
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
					FilterApplicationsAndIpDialog.this.dispose();
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
			mainPanel.add(getSelectionPanel(), BorderLayout.CENTER);
		}
		return mainPanel;
	}

	/**
	 * Initializes and returns the Panel that contains the applications and ip
	 * address selection tables.
	 */
	private JPanel getSelectionPanel() {

		JPanel selectionPanel = new JPanel(new GridLayout(2, 1));

		selectionPanel.add(getApplicationSelectionsPanel());
		selectionPanel.add(getIpAddressSelectionsPanel());

		return selectionPanel;
	}

	/**
	 * Initializes panel that contains the the list of applications.
	 */
	private JPanel getApplicationSelectionsPanel() {

		JPanel applicationSelectionPanel = new JPanel(new BorderLayout());

		JScrollPane applicationTableScrollPane = new JScrollPane(
				getJApplicationsTable());
		applicationSelectionPanel.add(applicationTableScrollPane,
				BorderLayout.CENTER);
		applicationSelectionPanel.setBorder(BorderFactory.createEmptyBorder(5,
				5, 5, 5));

		return applicationSelectionPanel;

	}

	/**
	 * Initializes panel that contains the the list of IP addresses.
	 */
	private JPanel getIpAddressSelectionsPanel() {

		JPanel ipAddressSelectionPanel = new JPanel(new BorderLayout());

		JScrollPane ipAddressTableScrollPane = new JScrollPane(
				getJIpAddressesTable());
		ipAddressSelectionPanel.add(ipAddressTableScrollPane,
				BorderLayout.CENTER);
		ipAddressSelectionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
				5, 5));

		return ipAddressSelectionPanel;

	}

	/**
	 * Initializes and returns the table that contains the list of applications
	 * found in the trace data.
	 */
	private JTable getJApplicationsTable() {
		if (jApplicationsTable == null) {

			// Make sure to make a copy of the current data before modifying
			jApplicationsTable = new DataTable<ApplicationSelection>(
					jApplicationsTableModel);
			jApplicationsTable.setAutoCreateRowSorter(true);
		}
		return jApplicationsTable;
	}

	/**
	 * Initializes and returns the table that contains the list of IP addresses
	 * found in the trace data.
	 */
	private JTable getJIpAddressesTable() {
		if (jIpAddressTable == null) {

			// Make sure to make a copy of the current data before modifying
			jIpAddressTable = new DataTable<FilterIpAddressesTableModel.AppIPAddressSelection>(
					jIpAddressesTableModel);
			jIpAddressTable.setAutoCreateRowSorter(true);
		}
		return jIpAddressTable;
	}

} // @jve:decl-index=0:visual-constraint="70,10"
