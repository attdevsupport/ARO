/*
 *  Copyright 2015 AT&T
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.SharedAttributesProcesses;
import com.att.aro.ui.view.diagnostictab.ChartPlotOptions;

/**
 * Represents the chart plot options dialog.
 * 
 * @author Nathan F Syfrig
 */
public class ChartPlotOptionsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private UserPreferences userPreferences = UserPreferences.getInstance();

	private JPanel jContentPane;
	private JPanel buttonPanel;
	private JPanel jButtonGrid;
	private JButton okButton;
	private JButton cancelButton;
	private JPanel optionsPanel;
	private JPanel jAdvancedOptionsPanel;
	private JCheckBox jCPUStateCheckBox;
	private JCheckBox jGPSStateCheckBox;
	private JCheckBox jRadioStateCheckBox;
	private JCheckBox jBluetoothCheckBox;
	private JCheckBox jCameraStateCheckBox;
	private JCheckBox jScreenStateCheckBox;
	private JCheckBox jBatteryStateCheckBox;
	private JCheckBox jWakelockStateCheckBox;
	private JCheckBox jWifiStateCheckBox;
	private JCheckBox jNetworkTypeCheckBox;
	private JCheckBox jThroughputCheckBox;
	private JCheckBox jUplinkCheckBox;
	private JCheckBox jDownlinkCheckBox;
	private JCheckBox jBurstsCheckBox;
	private JCheckBox jUserInputCheckBox;
	private JCheckBox jRRCStateCheckBox;
	private JCheckBox jAlarmTriggeredCheckBox;
	private JCheckBox jDefaultsCheckBox;

	private List<ChartPlotOptions> currentCheckedOptionList;
	private List<ChartPlotOptions> selectedOptions;
	private List<ChartPlotOptions> defaultOptions;
	private String defaultViewCheckBoxText;
	private Map<JCheckBox, ChartPlotOptions> checkBoxPlots;
	private EnableEscKeyCloseDialog enableEscKeyCloseDialog;
	private final SharedAttributesProcesses parent;
	private final JMenuItem callerMenuItem;

	private enum DialogItem {
		chart_options_dialog_defaults,
		chart_options_dialog_title,
		chart_options_dialog_button_ok,
		chart_options_dialog_button_cancel,
		chart_options_dialog_legend,
		chart_options_dialog_wakelock,
		chart_options_dialog_alarm,
		chart_options_dialog_cpu,
		chart_options_dialog_gps,
		chart_options_dialog_wifi,
		chart_options_dialog_network,
		chart_options_dialog_ulpackets,
		chart_options_dialog_dlpackets,
		chart_options_dialog_bursts,
		chart_options_dialog_userinput,
		chart_options_dialog_rrc,
		chart_options_dialog_radio,
		chart_options_dialog_bluetooth,
		chart_options_dialog_camera,
		chart_options_dialog_battery,
		chart_options_dialog_screen,
		chart_options_dialog_throughput
	}

	/**
	 * Initializes a new instance of the ChartPlotOptionsDialog class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window, and an instance of the AROAdvancedTabb.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 * 
	 * @param actionableClass
	 *            - The AROAdvancedTabb instance.
	 */
	public ChartPlotOptionsDialog(SharedAttributesProcesses parent, JMenuItem callerMenuItem) {
		super(parent.getFrame());
		this.parent = parent;
		this.callerMenuItem = callerMenuItem;
		// grab the selected options from the user pref's file
		this.defaultOptions = ChartPlotOptions.getDefaultList();
		this.defaultViewCheckBoxText = ResourceBundleHelper.
				getMessageString(DialogItem.chart_options_dialog_defaults);
		this.selectedOptions = userPreferences.getChartPlotOptions();
		// create a check box map to iterate through later
		this.checkBoxPlots = new HashMap<JCheckBox, ChartPlotOptions>();
		// call initialize
		initialize();
	}

	private void enableOptions(boolean enabled) {
		jCPUStateCheckBox.setEnabled(enabled);
		jGPSStateCheckBox.setEnabled(enabled);
		jRadioStateCheckBox.setEnabled(enabled);
		jBluetoothCheckBox.setEnabled(enabled);
		jCameraStateCheckBox.setEnabled(enabled);
		jScreenStateCheckBox.setEnabled(enabled);
		jBatteryStateCheckBox.setEnabled(enabled);
		jWakelockStateCheckBox.setEnabled(enabled);
		jWifiStateCheckBox.setEnabled(enabled);
		jNetworkTypeCheckBox.setEnabled(enabled);
		jThroughputCheckBox.setEnabled(enabled);
		jUplinkCheckBox.setEnabled(enabled);
		jDownlinkCheckBox.setEnabled(enabled);
		jBurstsCheckBox.setEnabled(enabled);
		jUserInputCheckBox.setEnabled(enabled);
		jRRCStateCheckBox.setEnabled(enabled);
		jAlarmTriggeredCheckBox.setEnabled(enabled);
	}

	/**
	 * Initializes the dialog.
	 */
	private void initialize() {
		this.setTitle(ResourceBundleHelper.getMessageString(
				DialogItem.chart_options_dialog_title));
		this.setContentPane(getJContentPane());
		if (isUserPrefsSelected(ChartPlotOptions.DEFAULT_VIEW)) {
			enableOptions(false);
		}

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		enableEscKeyCloseDialog = new EnableEscKeyCloseDialog(getRootPane(), this, false);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent event) {
				if (enableEscKeyCloseDialog.consumeEscPressed()) {
					executeCancelButton();
				}
			}
		});
		pack();
		setLocationRelativeTo(parent.getFrame());
		getRootPane().setDefaultButton(okButton);
	}

	/**
	 * /** Initializes jContentPane
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
		this.currentCheckedOptionList = getCheckedOptions();
		return jContentPane;
	}

	/**
	 * Initializes buttonPanel
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
			gridLayout.setHgap(10);
			jButtonGrid = new JPanel();
			jButtonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			jButtonGrid.setLayout(gridLayout);
			jButtonGrid.add(getJDefaultsCheckBox());
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
					DialogItem.chart_options_dialog_button_ok));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					currentCheckedOptionList = getCheckedOptions();
					userPreferences
							.setChartPlotOptions(currentCheckedOptionList);
					parent.updateChartSelection(currentCheckedOptionList);
					setVisible(false);
					callerMenuItem.setEnabled(true);
				}
			});
		}
		return okButton;
	}

	private void executeCancelButton() {
		userPreferences.setChartPlotOptions(currentCheckedOptionList);
		updateFromUserPreferences();
		setVisible(false);
		callerMenuItem.setEnabled(true);
	}

	/**
	 * Initializes and returns the cancel Button
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(ResourceBundleHelper
					.getMessageString(DialogItem.chart_options_dialog_button_cancel));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					executeCancelButton();
				}
			});
		}
		return cancelButton;
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

	private GridBagConstraints getGridBagConstraints(int gridy) {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = gridy;
		return gridBagConstraints;
	}

	/**
	 * Initializes the panel the contains the list of plot options check boxes.
	 */
	private JPanel getJAdvancedOptionsPanel() {
		if (jAdvancedOptionsPanel == null) {

			jAdvancedOptionsPanel = new JPanel();
			jAdvancedOptionsPanel.setLayout(new GridBagLayout());
			jAdvancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(
					null, ResourceBundleHelper.getMessageString(
							DialogItem.chart_options_dialog_legend),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			
			
			jAdvancedOptionsPanel.add(jGPSStateCheckBox = getJCheckBox(jGPSStateCheckBox,
					DialogItem.chart_options_dialog_gps, ChartPlotOptions.GPS),
						getGridBagConstraints(0));
			jAdvancedOptionsPanel.add(jRadioStateCheckBox = getJCheckBox(jRadioStateCheckBox,
					DialogItem.chart_options_dialog_radio, ChartPlotOptions.RADIO),
						getGridBagConstraints(1));
			jAdvancedOptionsPanel.add(jBluetoothCheckBox = getJCheckBox(jBluetoothCheckBox,
					DialogItem.chart_options_dialog_bluetooth, ChartPlotOptions.BLUETOOTH),
						getGridBagConstraints(2));
			jAdvancedOptionsPanel.add(jCameraStateCheckBox = getJCheckBox(jCameraStateCheckBox,
					DialogItem.chart_options_dialog_camera, ChartPlotOptions.CAMERA),
						getGridBagConstraints(3));
			jAdvancedOptionsPanel.add(jScreenStateCheckBox = getJCheckBox(jScreenStateCheckBox,
					DialogItem.chart_options_dialog_screen, ChartPlotOptions.SCREEN),
						getGridBagConstraints(4));
			jAdvancedOptionsPanel.add(jBatteryStateCheckBox = getJCheckBox(jBatteryStateCheckBox,
					DialogItem.chart_options_dialog_battery, ChartPlotOptions.BATTERY),
						getGridBagConstraints(5));
			jAdvancedOptionsPanel.add(jWifiStateCheckBox = getJCheckBox(jWifiStateCheckBox,
					DialogItem.chart_options_dialog_wifi, ChartPlotOptions.WIFI),
						getGridBagConstraints(6));
			jAdvancedOptionsPanel.add(jThroughputCheckBox = getJCheckBox(jThroughputCheckBox,
					DialogItem.chart_options_dialog_throughput, ChartPlotOptions.THROUGHPUT),
						getGridBagConstraints(7));
			jAdvancedOptionsPanel.add(jUplinkCheckBox = getJCheckBox(jUplinkCheckBox,
					DialogItem.chart_options_dialog_ulpackets, ChartPlotOptions.UL_PACKETS),
						getGridBagConstraints(8));
			jAdvancedOptionsPanel.add(jDownlinkCheckBox = getJCheckBox(jDownlinkCheckBox,
					DialogItem.chart_options_dialog_dlpackets, ChartPlotOptions.DL_PACKETS),
						getGridBagConstraints(9));
			jAdvancedOptionsPanel.add(jBurstsCheckBox = getJCheckBox(jBurstsCheckBox,
					DialogItem.chart_options_dialog_bursts, ChartPlotOptions.BURSTS),
						getGridBagConstraints(10));
			jAdvancedOptionsPanel.add(jUserInputCheckBox = getJCheckBox(jUserInputCheckBox,
					DialogItem.chart_options_dialog_userinput, ChartPlotOptions.USER_INPUT),
						getGridBagConstraints(11));
			jAdvancedOptionsPanel.add(jRRCStateCheckBox = getJCheckBox(jRRCStateCheckBox,
					DialogItem.chart_options_dialog_rrc, ChartPlotOptions.RRC),
						getGridBagConstraints(12));
			jAdvancedOptionsPanel.add(jNetworkTypeCheckBox = getJCheckBox(jNetworkTypeCheckBox,
					DialogItem.chart_options_dialog_network, ChartPlotOptions.NETWORK_TYPE),
						getGridBagConstraints(13));
			jAdvancedOptionsPanel.add(jWakelockStateCheckBox = getJCheckBox(
					jWakelockStateCheckBox, DialogItem.chart_options_dialog_wakelock,
						ChartPlotOptions.WAKELOCK), getGridBagConstraints(14));
			jAdvancedOptionsPanel.add(jCPUStateCheckBox = getJCheckBox(jCPUStateCheckBox,
					DialogItem.chart_options_dialog_cpu, ChartPlotOptions.CPU),
						getGridBagConstraints(15));
			jAdvancedOptionsPanel.add(jAlarmTriggeredCheckBox = getJCheckBox(
				jAlarmTriggeredCheckBox, DialogItem.chart_options_dialog_alarm,
					ChartPlotOptions.ALARM), getGridBagConstraints(16));

		}
		return jAdvancedOptionsPanel;
	}

	/**
	 * Returns the list of plot options selected from the list.
	 */
	private List<ChartPlotOptions> getCheckedOptions() {
		List<ChartPlotOptions> list = new ArrayList<ChartPlotOptions>();
		for (JCheckBox cb : checkBoxPlots.keySet()) {
			if (cb.isSelected()) {
				list.add(checkBoxPlots.get(cb));
			}
		}
		return list;
	}

	private boolean isUserPrefsSelected(ChartPlotOptions option) {
		return selectedOptions.contains(option);
	}

	private JCheckBox getJCheckBox(JCheckBox jCheckboxParm, DialogItem dialogItem,
			ChartPlotOptions chartPlotOption) {
		boolean thisOnesNew = jCheckboxParm == null;
		JCheckBox jCheckbox = thisOnesNew ? new JCheckBox() : jCheckboxParm;
		if (thisOnesNew) {
			jCheckbox.setText(ResourceBundleHelper.getMessageString(dialogItem));
			jCheckbox.setSelected(isUserPrefsSelected(chartPlotOption));
			checkBoxPlots.put(jCheckbox, chartPlotOption);
		}
		return jCheckbox;
	}

	/**
	 * Initializes jCPUStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCPUStateCheckBox() {
		if (jCPUStateCheckBox == null) {
			jCPUStateCheckBox = new JCheckBox();
			jCPUStateCheckBox.setText(ResourceBundleHelper.getMessageString(
					DialogItem.chart_options_dialog_cpu));
			jCPUStateCheckBox.setSelected(isUserPrefsSelected(ChartPlotOptions.CPU));
			checkBoxPlots.put(jCPUStateCheckBox, ChartPlotOptions.CPU);
		}
		return jCPUStateCheckBox;
	}
	
	/** 
	 * Return status of the CPU check box from the View Options dialog
	 * 
	 * @return Returns true is selected, false if not selected.
	 */
	public boolean isCpuCheckBoxSelected(){
		return getJCPUStateCheckBox().isSelected();
	}

	/**
	 * Initializes Default View check box
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJDefaultsCheckBox() {
		if (jDefaultsCheckBox == null) {
			jDefaultsCheckBox = new JCheckBox();
			jDefaultsCheckBox.setText(ResourceBundleHelper
					.getMessageString(DialogItem.chart_options_dialog_defaults));
			jDefaultsCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.DEFAULT_VIEW));

			ItemListener itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					boolean enableItems = !(itemEvent.getStateChange() == ItemEvent.SELECTED);
					if (!enableItems) {
						updateDefaultCheckBoxes();
					}
					enableOptions(enableItems);
				}
			};
			jDefaultsCheckBox.addItemListener(itemListener);

			checkBoxPlots.put(jDefaultsCheckBox, ChartPlotOptions.DEFAULT_VIEW);
		}
		return jDefaultsCheckBox;
	}

	/**
	 * Updates the default check boxes for display. It is called when default
	 * check box is displayed and that is why we skip this check box.
	 */
	private void updateDefaultCheckBoxes() {
		for (JCheckBox checkBox : checkBoxPlots.keySet()) {
			String strText = checkBox.getText();
			if (!strText.equalsIgnoreCase(defaultViewCheckBoxText)) {
				boolean selected = defaultOptions.contains(checkBoxPlots
						.get(checkBox));
				checkBox.setSelected(selected);
			}
		}
	}

	/**
	 * Updates the state of the check boxes on the dialog based on current user
	 * preferences.
	 */
	public void updateFromUserPreferences() {
		// grab the selected options from the user pref's file
		this.selectedOptions = userPreferences.getChartPlotOptions();
		// loop on all check boxes and set selected status based on current/new
		// user pref's
		for (JCheckBox checkBox : checkBoxPlots.keySet()) {
			boolean selected = selectedOptions.contains(checkBoxPlots
					.get(checkBox));
			checkBox.setSelected(selected);
		}
		// enable all check boxes
		this.enableOptions(!selectedOptions
				.contains(ChartPlotOptions.DEFAULT_VIEW));
	}
} // @jve:decl-index=0:visual-constraint="132,38"
