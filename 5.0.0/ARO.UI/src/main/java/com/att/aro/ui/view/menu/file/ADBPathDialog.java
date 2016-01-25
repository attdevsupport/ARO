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
package com.att.aro.ui.view.menu.file;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.att.aro.core.AROConfig;
import com.att.aro.core.settings.IAROSettings;
import com.att.aro.ui.commonui.EnableEscKeyCloseDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.SharedAttributesProcesses;

public class ADBPathDialog extends JDialog {
	private static final long serialVersionUID = 1L;


	private final JPanel contentPanel = new JPanel();
	private JTextField txtFile;
	private JFileChooser file;
	private IAROSettings settings = null;

	private enum MessageKeys {
		menu_file_adb_title,
		menu_file_adb_adbpath,
		menu_file_adb_badadbpathmsg,
		Button_browse,
		Button_ok,
		Button_cancel
	}

	public ADBPathDialog(SharedAttributesProcesses parent) {
		this(parent, new AROConfig().getAROConfigFile());
	}

	/**
	 * Create the dialog.
	 */
	public ADBPathDialog(SharedAttributesProcesses parent, IAROSettings settings) {
		super(parent.getFrame());
		this.settings = settings;
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setResizable(false);
		setTitle(ResourceBundleHelper.getMessageString(MessageKeys.menu_file_adb_badadbpathmsg));
		setBounds(100, 100, 548, 113);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		setLocationRelativeTo(parent.getFrame());

		JLabel lblAdbPath = new JLabel(ResourceBundleHelper.getMessageString(
				MessageKeys.menu_file_adb_adbpath));
		lblAdbPath.setBounds(10, 11, 68, 14);
		contentPanel.add(lblAdbPath);

		txtFile = new JTextField();
		txtFile.setBounds(72, 8, 371, 20);
		txtFile.setColumns(10);
		contentPanel.add(txtFile);

		JButton btnBrowse = new JButton(ResourceBundleHelper.getMessageString(
				MessageKeys.Button_browse));
		btnBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				onBrowse();
			}

		});
		btnBrowse.setBounds(443, 7, 89, 23);
		contentPanel.add(btnBrowse);

		renderButtonPane();

		file = new JFileChooser();
		file.setMultiSelectionEnabled(false);
		String adb = settings.getAttribute("adb");
		if(adb != null){
			txtFile.setText(adb);
		}
		new EnableEscKeyCloseDialog(getRootPane(), this);
	}


	private void renderOkButton(JPanel buttonPane) {
		JButton okButton = new JButton(ResourceBundleHelper.getMessageString(
				MessageKeys.Button_ok));
		okButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onOk();
			}});
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	}

	private void renderCancelButton(JPanel buttonPane) {
		JButton cancelButton = new JButton(ResourceBundleHelper.getMessageString(
				MessageKeys.Button_cancel));
		cancelButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				onCancel();
			}});
		buttonPane.add(cancelButton);
	}

	private void renderButtonPane() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		renderOkButton(buttonPane);
		renderCancelButton(buttonPane);
	}

	private void onBrowse(){
		file.showOpenDialog(ADBPathDialog.this);
		if(file.getSelectedFile() != null){
			String path = file.getSelectedFile().getPath();
			txtFile.setText(path);
		}
	}

	private void onOk(){
		if(txtFile.getText().length() < 3){
			new MessageDialogFactory().showErrorDialog(null,
					"Please provide location of ADB to continue.");
		}else{
			settings.setAndSaveAttribute("adb", txtFile.getText());
			System.setProperty("ANDROID_ADB", txtFile.getText());
			dispose();
		}
	}

	private void onCancel(){
		dispose();
	}
}

