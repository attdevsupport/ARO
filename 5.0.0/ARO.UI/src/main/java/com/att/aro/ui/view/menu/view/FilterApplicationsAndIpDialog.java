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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.ApplicationSelection;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.mvc.IAROView;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;

/**
 * Represents the Filter Applications Dialog that appears when the user chooses
 * the Select Applications/IPs menu item under the View menu. This dialog prompts
 * the user to select applications and IP addresses that are found in the
 * trace data.
 */
public class FilterApplicationsAndIpDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);

	private JPanel jButtonPanel = null;
	private JPanel jButtonGrid = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JPanel mainPanel = null;
	private DataTable<ApplicationSelection> jApplicationsTable = null;
	private DataTable<FilterIpAddressesTableModel.AppIPAddressSelection> jIpAddressTable = null;
	private FilterApplicationsTableModel jApplicationsTableModel;
	private FilterIpAddressesTableModel jIpAddressesTableModel;
 	private IAROView parent;
 	
 	private PacketAnalyzerResult traceresult ;
 	private boolean ipv4Selection;
 	private boolean ipv6Selection;
 	private boolean udpSelection;
 	
	public PacketAnalyzerResult getPktAnalyzerResult() {
		return traceresult;
	}

	public void setPktAnalyzerResult(PacketAnalyzerResult traceresult) {
		this.traceresult = traceresult;
	}

	private enum DialogItem {
		filter_title,
		Button_ok,
		Button_cancel
	}

	/**
	 * Initializes a new instance of the FilterApplicationsAndIpDialog class using
	 * the specified instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param parent
	 *            The top level instance (that implements SharedAttributesProcesses).
	 */
	public FilterApplicationsAndIpDialog(IAROView parent) {
		this.parent = parent;
		initialize();
	}

	/**
	 * Initializes the dialog.
	 */
	private void initialize() {
		PacketAnalyzerResult traceresultTemp = ((MainFrame)parent).getController().getTheModel().getAnalyzerResult();
		
		if (traceresultTemp==null){
			logger.error("Trace result error! " );
			MessageDialogFactory.getInstance().showErrorDialog(FilterApplicationsAndIpDialog.this,"wrong.."); 
		}else{
			setPktAnalyzerResult(traceresultTemp);
			
			this.jIpAddressesTableModel = new FilterIpAddressesTableModel(traceresultTemp.getFilter());
			this.jApplicationsTableModel = new FilterApplicationsTableModel(traceresultTemp.getFilter());
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
		}

		this.setSize(600, 420);
		this.setModal(true);
		this.setTitle(ResourceBundleHelper.getMessageString(DialogItem.filter_title));
		this.setLayout(new BorderLayout());
		this.add(getMainPanel(), BorderLayout.CENTER);
		this.setLocationRelativeTo(getOwner());
		new EnableEscKeyCloseDialog(getRootPane(), this);
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
			okButton.setText(ResourceBundleHelper.getMessageString(DialogItem.Button_ok));
			okButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(getPktAnalyzerResult().getFilter()!=null){
						AnalysisFilter filter = getPktAnalyzerResult().getFilter();
						Map<InetAddress, String> domainNames= filter.getDomainNames();
						Map<String, ApplicationSelection> appSelections = new HashMap<String, ApplicationSelection>(filter.getAppSelections().size());
						for (ApplicationSelection sel : filter.getAppSelections().values()) {
							if(domainNames != null){ //Greg Story Add domain names map to Application Selection
								sel.setDomainNames(domainNames);
							}
							appSelections.put(sel.getAppName(), new ApplicationSelection(sel));
						}
						filter.setIpv4Sel(ipv4Selection);
						filter.setIpv6Sel(ipv6Selection);
						filter.setUdpSel(udpSelection);
						((MainFrame)parent).updateFilter(filter);
						dispose();
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
			cancelButton.setText(ResourceBundleHelper.getMessageString(DialogItem.Button_cancel));
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
		
		//selectionPanel.add(getPacketSelectionPanel());
		selectionPanel.add(getApplicationSelectionsPanel());
		selectionPanel.add(getIpAddressSelectionsPanel());

		return selectionPanel;
	}

	private JPanel getPacketSelectionPanel(){
		JPanel checkBoxSelPanel = new JPanel();
		final JCheckBox chkIpv4 = new JCheckBox("IPV4");
	    final JCheckBox chkIpv6 = new JCheckBox("IPV6");
	    final JCheckBox chkUdp = new JCheckBox("UDP");

	    chkIpv4.setSelected(ipv4Selection = traceresult.getFilter().isIpv4Sel());
		chkIpv6.setSelected(ipv6Selection = traceresult.getFilter().isIpv6Sel());
		chkUdp.setSelected(udpSelection = traceresult.getFilter().isUdpSel());
				
		chkIpv4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent aEvent) {
				JCheckBox cb = (JCheckBox) aEvent.getSource();
				if(cb.isSelected()){
					ipv4Selection = true;
				} else {
					ipv4Selection = false;
				}
				
			}
		});

		chkIpv6.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent aEvent) {
				JCheckBox cb = (JCheckBox) aEvent.getSource();
				
				if(cb.isSelected()){
					ipv6Selection = true;
				} else {
					ipv6Selection = false;
				}
			}
		});
		
		chkUdp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent aEvent) {
				JCheckBox cb = (JCheckBox) aEvent.getSource();
				if(cb.isSelected()){
					udpSelection = true;
				} else {
					udpSelection = false;
				}
			}
		});
	    checkBoxSelPanel.add(chkIpv4);
	    checkBoxSelPanel.add(chkIpv6);
	    checkBoxSelPanel.add(chkUdp);
	    checkBoxSelPanel.setSize(50, 20);
		return checkBoxSelPanel;
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
		applicationSelectionPanel.add(getPacketSelectionPanel(), BorderLayout.SOUTH);

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

//		ipAddressSelectionPanel.add(getPacketSelectionPanel(), BorderLayout.SOUTH);

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
