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

package com.att.aro.ui.view.menu.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import com.att.aro.core.packetanalysis.IBurstCollectionAnalysis;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.FilteredProcessSelection;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.ProcessSelection;
import com.att.aro.core.peripheral.pojo.CpuActivityList;
import com.att.aro.mvc.IAROView;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;

/**
 * Represents the Select Processes options dialog.
 */
public class FilterProcessesDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final int BORDER_SIZE = 10;
	private static final int GAP_SIZE = 10;
	private static final int FONT_SIZE = 12;
	private static final int COLOR_51 = 51;
	private static final int BORDER_APP_SEL_PNL = 5;

	private JPanel jContentPane;
	private JPanel buttonPanel;
	private JPanel jButtonGrid;
	private JPanel optionsPanel;
	private JPanel jAdvancedOptionsPanel;
	private JButton okButton;
	private JButton cancelButton;

	private EnableEscKeyCloseDialog enableEscKeyCloseDialog;
	private final JMenuItem callerMenuItem;

	private DataTable<ProcessSelection> jProcessesTable = null;
	private FilterProcessesTableModel jProcessesTableModel;
	
	private static FilteredProcessSelection filteredProcessSelection;
	// A copy of the original filter used to recover the old values when Cancel
	// button is clicked.
	private FilteredProcessSelection filteredProcessSelectionCopy;

	private IBurstCollectionAnalysis burstAnalysis = ContextAware.getAROConfigContext().getBean(IBurstCollectionAnalysis.class);
	private IAROView parent;

	private enum DialogItem {
		processes_dialog_title,
		processes_dialog_button_ok,
		processes_dialog_button_cancel,
		processes_dialog_legend
	}

	/**
	 * Initializes a new instance of the SelectProcessesDialog class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window, and an instance of the AROAdvancedTabb.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 * 
	 */
	public FilterProcessesDialog(IAROView parent, JMenuItem callerMenuItem) {
		super();
		this.callerMenuItem = callerMenuItem;
		this.parent = parent;
		PacketAnalyzerResult analysisData = ((MainFrame)parent).getController().getTheModel().getAnalyzerResult();
		CpuActivityList cpuAList = analysisData.getTraceresult().getCpuActivityList();
		if (analysisData != null) {
			if (FilterProcessesDialog.filteredProcessSelection == null) {
				FilterProcessesDialog.filteredProcessSelection = new FilteredProcessSelection(cpuAList);
			}
			this.jProcessesTableModel = new FilterProcessesTableModel(FilterProcessesDialog.filteredProcessSelection);

			// create copy of the original filter
			this.filteredProcessSelectionCopy = new FilteredProcessSelection(FilterProcessesDialog.filteredProcessSelection);

			initialize();
		}
	}

	/**
	 * Initializes the dialog.
	 */
	private void initialize() {
		this.setTitle(ResourceBundleHelper.getMessageString(DialogItem.processes_dialog_title));
		this.setContentPane(getJContentPane());
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(super.getOwner());
		getRootPane().setDefaultButton(okButton);
		enableEscKeyCloseDialog = new EnableEscKeyCloseDialog(getRootPane(), this);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent event) {
				if (enableEscKeyCloseDialog.consumeEscPressed()) {
					cancelButtonAction();
				}
			}
		});
	}

	/**
	 * /** Initializes JPanel serving as a content pane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getButtonPanel(), BorderLayout.SOUTH);
			jContentPane.add(getOptionsPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * Initializes button panel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BorderLayout());
			buttonPanel.add(getJButtonGrid(), BorderLayout.EAST);
		}
		return buttonPanel;
	}

	/**
	 * Initializes jButtonGrid
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJButtonGrid() {
		if (jButtonGrid == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setHgap(GAP_SIZE);
			jButtonGrid = new JPanel();
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
			jButtonGrid.setLayout(gridLayout);
			jButtonGrid.add(getOkButton(), null);
			jButtonGrid.add(getCancelButton(), null);
		}
		return jButtonGrid;
	}

	/**
	 * Initializes and returns the OK Button
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText(ResourceBundleHelper.getMessageString(
					DialogItem.processes_dialog_button_ok));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					oKButtonAction();
				}
			});
		}
		return okButton;
	}

	/**
	 * Actions performed when OK button is clicked.
	 */
	private void oKButtonAction() {
		CpuActivityList cpuAList = ((MainFrame)parent).getController().getTheModel().getAnalyzerResult().getTraceresult().getCpuActivityList();
		cpuAList.setProcessSelection(filteredProcessSelection);
		cpuAList.recalculateTotalCpu();
		PacketAnalyzerResult tempresult = ((MainFrame)parent).getController().getTheModel().getAnalyzerResult() ;
		
		BurstCollectionAnalysisData bd = burstAnalysis.analyze(tempresult.getTraceresult().getAllpackets(), 
				tempresult.getProfile(), tempresult.getStatistic().getPacketSizeToCountMap(), tempresult.getStatemachine().getStaterangelist(), 
				tempresult.getTraceresult().getUserEvents(), cpuAList.getCpuActivities(), tempresult.getSessionlist());
		
		((MainFrame)parent).getController().getTheModel().getAnalyzerResult().setBurstcollectionAnalysisData(bd);
		((MainFrame)parent).refresh();
		callerMenuItem.setEnabled(true);
		dispose();
	}

	/**
	 * Initializes and returns the cancel Button
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(ResourceBundleHelper.getMessageString(
					DialogItem.processes_dialog_button_cancel));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					cancelButtonAction();
				}
			});
		}
		return cancelButton;
	}

	/**
	 * Actions performed when Cancel button is clicked.
	 */
	private void cancelButtonAction() {
		FilterProcessesDialog.filteredProcessSelection = this.filteredProcessSelectionCopy;
		callerMenuItem.setEnabled(true);		
		dispose();

	}

	/**
	 * Initializes optionsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getOptionsPanel() {
		if (optionsPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 1.0D;
			gridBagConstraints.gridy = 0;
			optionsPanel = new JPanel();
			optionsPanel.setLayout(new GridBagLayout());
			optionsPanel.add(getJAdvancedOptionsPanel(), gridBagConstraints);
		}
		return optionsPanel;
	}

	/**
	 * Initializes the panel the contains the list of plot options check boxes.
	 */
	private JPanel getJAdvancedOptionsPanel() {
		if (jAdvancedOptionsPanel == null) {

			jAdvancedOptionsPanel = new JPanel();
			jAdvancedOptionsPanel.setLayout(new GridBagLayout());
			jAdvancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(null,
				ResourceBundleHelper.getMessageString(DialogItem.processes_dialog_legend),
					TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
						new Font("Dialog", Font.BOLD, FONT_SIZE),
							new Color(COLOR_51, COLOR_51, COLOR_51)));
			jAdvancedOptionsPanel.add(getApplicationSelectionsPanel());
		}
		return jAdvancedOptionsPanel;
	}

	/**
	 * Initializes panel that contains the the list of applications.
	 */
	private JPanel getApplicationSelectionsPanel() {

		JPanel applicationSelectionPanel = new JPanel(new BorderLayout());
		JScrollPane applicationTableScrollPane = new JScrollPane(getJApplicationsTable());
		applicationSelectionPanel.add(applicationTableScrollPane, BorderLayout.CENTER);
		applicationSelectionPanel.setBorder(BorderFactory.createEmptyBorder(
				BORDER_APP_SEL_PNL, BORDER_APP_SEL_PNL, BORDER_APP_SEL_PNL, BORDER_APP_SEL_PNL));
		return applicationSelectionPanel;
	}

	/**
	 * Initializes and returns the table that contains the list of applications
	 * found in the trace data.
	 */
	private JTable getJApplicationsTable() {
		if (jProcessesTable == null) {
			jProcessesTable = new DataTable<ProcessSelection>(jProcessesTableModel);
			jProcessesTable.setAutoCreateRowSorter(true);
		}
		return jProcessesTable;
	}
}