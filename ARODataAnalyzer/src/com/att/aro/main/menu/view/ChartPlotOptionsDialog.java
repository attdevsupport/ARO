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


package com.att.aro.main.menu.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
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
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.att.aro.main.AROAdvancedTabb;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.ChartPlotOptions;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.UserPreferences;

/**
 * Represents the chart plot options dialog.
 */
public class ChartPlotOptionsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final ResourceBundle RB = ResourceBundleManager
			.getDefaultBundle();
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
	private JCheckBox jWifiStateCheckBox;
	private JCheckBox jNetworkTypeCheckBox;
	private JCheckBox jThroughputCheckBox;
	private JCheckBox jUplinkCheckBox;
	private JCheckBox jDownlinkCheckBox;
	private JCheckBox jBurstsCheckBox;
	private JCheckBox jUserInputCheckBox;
	private JCheckBox jRRCStateCheckBox;
	private JCheckBox jDefaultsCheckBox;

	private List<ChartPlotOptions> currentCheckedOptionList;
	private List<ChartPlotOptions> selectedOptions;
	private List<ChartPlotOptions> defaultOptions;
	private String defaultViewCheckBoxText;
	private Map<JCheckBox, ChartPlotOptions> checkBoxPlots;
	private AROAdvancedTabb actionableClass;
	private Frame owner;

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
	public ChartPlotOptionsDialog(Frame owner, AROAdvancedTabb actionableClass) {
		super(owner);
		this.owner = owner;
		// grab the class instance to be updated when this dialog is submitted
		this.actionableClass = actionableClass;
		// grab the selected options from the user pref's file
		this.defaultOptions = ChartPlotOptions.getDefaultList();
		this.defaultViewCheckBoxText = RB
				.getString("chart.options.dialog.defaults");
		this.selectedOptions = userPreferences.getChartPlotOptions();
		// create a check box map to iterate through later
		this.checkBoxPlots = new HashMap<JCheckBox, ChartPlotOptions>();
		// call initialize
		initialize();
	}

	/**
	 * Sets the visibility of the chart plot options dialog.
	 * 
	 * @param visible
	 *            A boolean value that indicates whether the dialog should be
	 *            visible or not.
	 */
	public void setVisibleToUser(boolean visible) {
		this.setVisible(true);
		((ApplicationResourceOptimizer) owner)
				.enableChartOptionsMenuItem(false);
	}

	private void enableOptions(boolean enabled) {
		jCPUStateCheckBox.setEnabled(enabled);
		jGPSStateCheckBox.setEnabled(enabled);
		jRadioStateCheckBox.setEnabled(enabled);
		jBluetoothCheckBox.setEnabled(enabled);
		jCameraStateCheckBox.setEnabled(enabled);
		jScreenStateCheckBox.setEnabled(enabled);
		jBatteryStateCheckBox.setEnabled(enabled);
		jWifiStateCheckBox.setEnabled(enabled);
		jNetworkTypeCheckBox.setEnabled(enabled);
		jThroughputCheckBox.setEnabled(enabled);
		jUplinkCheckBox.setEnabled(enabled);
		jDownlinkCheckBox.setEnabled(enabled);
		jBurstsCheckBox.setEnabled(enabled);
		jUserInputCheckBox.setEnabled(enabled);
		jRRCStateCheckBox.setEnabled(enabled);
	}

	/**
	 * Initializes the dialog.
	 */
	private void initialize() {
		this.setTitle(RB.getString("chart.options.dialog.title"));
		this.setContentPane(getJContentPane());
		if (isUserPrefsSelected(ChartPlotOptions.DEFAULT_VIEW)) {
			enableOptions(false);
		}

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				ChartPlotOptionsDialog.this.setVisible(false);
				((ApplicationResourceOptimizer) owner)
						.enableChartOptionsMenuItem(true);
			}
		});
		this.pack();
		this.setLocationRelativeTo((ApplicationResourceOptimizer) super
				.getOwner());
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
			okButton.setText(RB.getString("chart.options.dialog.button.ok"));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					currentCheckedOptionList = getCheckedOptions();
					userPreferences
							.setChartPlotOptions(currentCheckedOptionList);
					actionableClass.setChartOptions(currentCheckedOptionList);
					ChartPlotOptionsDialog.this.setVisible(false);
					((ApplicationResourceOptimizer) owner)
							.enableChartOptionsMenuItem(true);
				}
			});
		}
		return okButton;
	}

	/**
	 * Initializes and returns the cancel Button
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText(RB
					.getString("chart.options.dialog.button.cancel"));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					userPreferences
							.setChartPlotOptions(currentCheckedOptionList);
					updateFromUserPreferences();
					ChartPlotOptionsDialog.this.setVisible(false);
					((ApplicationResourceOptimizer) owner)
							.enableChartOptionsMenuItem(true);
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

	/**
	 * Initializes the panel the contains the list of plot options check boxes.
	 */
	private JPanel getJAdvancedOptionsPanel() {
		if (jAdvancedOptionsPanel == null) {

			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.gridy = 14;

			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridy = 13;

			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 12;

			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridy = 11;

			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 10;

			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.gridy = 9;

			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.gridy = 8;

			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.gridy = 7;

			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridy = 6;

			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridy = 5;

			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.gridy = 4;

			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.gridy = 3;

			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.gridy = 2;

			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;

			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridy = 0;

			jAdvancedOptionsPanel = new JPanel();
			jAdvancedOptionsPanel.setLayout(new GridBagLayout());
			jAdvancedOptionsPanel.setBorder(BorderFactory.createTitledBorder(
					null, RB.getString("chart.options.dialog.legend"),
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			
			
			jAdvancedOptionsPanel.add(getJGPSStateCheckBox(), gridBagConstraints1);
			jAdvancedOptionsPanel.add(getJRadioStateCheckBox(), gridBagConstraints2);
			jAdvancedOptionsPanel.add(getJBluetoothCheckBox(), gridBagConstraints3);
			jAdvancedOptionsPanel.add(getJCameraCheckBox(), gridBagConstraints4);
			jAdvancedOptionsPanel.add(getJScreenCheckBox(), gridBagConstraints5);
			jAdvancedOptionsPanel.add(getJBatteryCheckBox(), gridBagConstraints6);
			jAdvancedOptionsPanel.add(getJWifiStateCheckBox(), gridBagConstraints7);
			jAdvancedOptionsPanel.add(getJThroughputCheckBox(),gridBagConstraints8);
			jAdvancedOptionsPanel.add(getJUplinkCheckBox(), gridBagConstraints9);
			jAdvancedOptionsPanel.add(getJDownlinkCheckBox(), gridBagConstraints10);
			jAdvancedOptionsPanel.add(getJBurstsCheckBox(), gridBagConstraints11);
			jAdvancedOptionsPanel.add(getJUserInputCheckBox(), gridBagConstraints12);
			jAdvancedOptionsPanel.add(getJRRCStateCheckBox(), gridBagConstraints13);
			jAdvancedOptionsPanel.add(getJNetworkTypeCheckBox(), gridBagConstraints14);
			jAdvancedOptionsPanel.add(getJCPUStateCheckBox(), gridBagConstraints15);

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
		return this.selectedOptions.contains(option);
	}

	/**
	 * Initializes jCPUStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCPUStateCheckBox() {
		if (jCPUStateCheckBox == null) {
			jCPUStateCheckBox = new JCheckBox();
			jCPUStateCheckBox.setText(RB.getString("chart.options.dialog.cpu"));
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
	 * Initializes jGPSStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJGPSStateCheckBox() {
		if (jGPSStateCheckBox == null) {
			jGPSStateCheckBox = new JCheckBox();
			jGPSStateCheckBox.setText(RB.getString("chart.options.dialog.gps"));
			jGPSStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.GPS));
			checkBoxPlots.put(jGPSStateCheckBox, ChartPlotOptions.GPS);
		}
		return jGPSStateCheckBox;
	}

	
	/**
	 * Initializes jWifiStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJWifiStateCheckBox() {
		if (jWifiStateCheckBox == null) {
			jWifiStateCheckBox = new JCheckBox();
			jWifiStateCheckBox.setText(RB
					.getString("chart.options.dialog.wifi"));
			jWifiStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.WIFI));
			checkBoxPlots.put(jWifiStateCheckBox, ChartPlotOptions.WIFI);
		}
		return jWifiStateCheckBox;
	}
	
	/**
	 * Initializes jNetworkTypeCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJNetworkTypeCheckBox() {
		if (jNetworkTypeCheckBox == null) {
			jNetworkTypeCheckBox = new JCheckBox();
			jNetworkTypeCheckBox.setText(RB
					.getString("chart.options.dialog.network"));
			jNetworkTypeCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.NETWORK_TYPE));
			checkBoxPlots.put(jNetworkTypeCheckBox, ChartPlotOptions.NETWORK_TYPE);
		}
		return jNetworkTypeCheckBox;
	}

	/**
	 * Initializes jUplinkCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJUplinkCheckBox() {
		if (jUplinkCheckBox == null) {
			jUplinkCheckBox = new JCheckBox();
			jUplinkCheckBox.setText(RB
					.getString("chart.options.dialog.ulpackets"));
			jUplinkCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.UL_PACKETS));
			checkBoxPlots.put(jUplinkCheckBox, ChartPlotOptions.UL_PACKETS);
		}
		return jUplinkCheckBox;
	}

	/**
	 * This method initializes jDownlinkCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJDownlinkCheckBox() {
		if (jDownlinkCheckBox == null) {
			jDownlinkCheckBox = new JCheckBox();
			jDownlinkCheckBox.setText(RB
					.getString("chart.options.dialog.dlpackets"));
			jDownlinkCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.DL_PACKETS));
			checkBoxPlots.put(jDownlinkCheckBox, ChartPlotOptions.DL_PACKETS);
		}
		return jDownlinkCheckBox;
	}

	/**
	 * Initializes jBurstsCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJBurstsCheckBox() {
		if (jBurstsCheckBox == null) {
			jBurstsCheckBox = new JCheckBox();
			jBurstsCheckBox
					.setText(RB.getString("chart.options.dialog.bursts"));
			jBurstsCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.BURSTS));
			checkBoxPlots.put(jBurstsCheckBox, ChartPlotOptions.BURSTS);
		}
		return jBurstsCheckBox;
	}

	/**
	 * Initializes jUserInputCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJUserInputCheckBox() {
		if (jUserInputCheckBox == null) {
			jUserInputCheckBox = new JCheckBox();
			jUserInputCheckBox.setText(RB
					.getString("chart.options.dialog.userinput"));
			jUserInputCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.USER_INPUT));
			checkBoxPlots.put(jUserInputCheckBox, ChartPlotOptions.USER_INPUT);
		}
		return jUserInputCheckBox;
	}

	/**
	 * Initializes jRRCStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRRCStateCheckBox() {
		if (jRRCStateCheckBox == null) {
			jRRCStateCheckBox = new JCheckBox();
			jRRCStateCheckBox.setText(RB.getString("chart.options.dialog.rrc"));
			jRRCStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.RRC));
			checkBoxPlots.put(jRRCStateCheckBox, ChartPlotOptions.RRC);
		}
		return jRRCStateCheckBox;
	}

	/**
	 * Initializes jRadioStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJRadioStateCheckBox() {
		if (jRadioStateCheckBox == null) {
			jRadioStateCheckBox = new JCheckBox();
			jRadioStateCheckBox.setText(RB
					.getString("chart.options.dialog.radio"));
			jRadioStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.RADIO));
			checkBoxPlots.put(jRadioStateCheckBox, ChartPlotOptions.RADIO);
		}
		return jRadioStateCheckBox;
	}

	/**
	 * Initializes jBluetoothCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJBluetoothCheckBox() {
		if (jBluetoothCheckBox == null) {
			jBluetoothCheckBox = new JCheckBox();
			jBluetoothCheckBox.setText(RB
					.getString("chart.options.dialog.bluetooth"));
			jBluetoothCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.BLUETOOTH));
			checkBoxPlots.put(jBluetoothCheckBox, ChartPlotOptions.BLUETOOTH);
		}
		return jBluetoothCheckBox;
	}

	/**
	 * Initializes jCameraStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCameraCheckBox() {
		if (jCameraStateCheckBox == null) {
			jCameraStateCheckBox = new JCheckBox();
			jCameraStateCheckBox.setText(RB
					.getString("chart.options.dialog.camera"));
			jCameraStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.CAMERA));
			checkBoxPlots.put(jCameraStateCheckBox, ChartPlotOptions.CAMERA);
		}
		return jCameraStateCheckBox;
	}

	/**
	 * Initializes jBatteryStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJBatteryCheckBox() {
		if (jBatteryStateCheckBox == null) {
			jBatteryStateCheckBox = new JCheckBox();
			jBatteryStateCheckBox.setText(RB
					.getString("chart.options.dialog.battery"));
			jBatteryStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.BATTERY));
			checkBoxPlots.put(jBatteryStateCheckBox, ChartPlotOptions.BATTERY);
		}
		return jBatteryStateCheckBox;
	}

	/**
	 * Initializes jScreenStateCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJScreenCheckBox() {
		if (jScreenStateCheckBox == null) {
			jScreenStateCheckBox = new JCheckBox();
			jScreenStateCheckBox.setText(RB
					.getString("chart.options.dialog.screen"));
			jScreenStateCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.SCREEN));
			checkBoxPlots.put(jScreenStateCheckBox, ChartPlotOptions.SCREEN);
		}
		return jScreenStateCheckBox;
	}

	/**
	 * Initializes jThroughputCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJThroughputCheckBox() {
		if (jThroughputCheckBox == null) {
			jThroughputCheckBox = new JCheckBox();
			jThroughputCheckBox.setText(RB
					.getString("chart.options.dialog.throughput"));
			jThroughputCheckBox
					.setSelected(isUserPrefsSelected(ChartPlotOptions.THROUGHPUT));
			checkBoxPlots.put(jThroughputCheckBox, ChartPlotOptions.THROUGHPUT);
		}
		return jThroughputCheckBox;
	}

	/**
	 * Initializes Default View check box
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJDefaultsCheckBox() {
		if (jDefaultsCheckBox == null) {
			jDefaultsCheckBox = new JCheckBox();
			jDefaultsCheckBox.setText(RB
					.getString("chart.options.dialog.defaults"));
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
