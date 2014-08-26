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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.interfaces.Settings;
import com.att.aro.model.SettingsImpl;

public class ADBPathDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField txtFile;
	private JFileChooser file;
	Settings settings = null;

	public ADBPathDialog() {
		this(new SettingsImpl());
	}
	/**
	 * Create the dialog.
	 */
	public ADBPathDialog(Settings settings) {
		this.settings = settings;
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setResizable(false);
		setTitle("Android Debug Bridge executable path");
		setBounds(100, 100, 548, 113);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblAdbPath = new JLabel("ADB Path: ");
		lblAdbPath.setBounds(10, 11, 68, 14);
		contentPanel.add(lblAdbPath);
		
		txtFile = new JTextField();
		txtFile.setBounds(62, 8, 371, 20);
		contentPanel.add(txtFile);
		txtFile.setColumns(10);
		
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnBrowseClick();
			}
			
		});
		btnBrowse.setBounds(443, 7, 89, 23);
		contentPanel.add(btnBrowse);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						onOk();
					}});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0) {
						onCancel();
					}});
				buttonPane.add(cancelButton);
			}
		}
		
		file = new JFileChooser();
		file.setMultiSelectionEnabled(false);
		String adb = settings.getProperty("adb");
		if(adb != null){
			txtFile.setText(adb);
		}
	}
	void btnBrowseClick(){
		file.showOpenDialog(ADBPathDialog.this);
		if(file.getSelectedFile() != null){
			String path = file.getSelectedFile().getPath();
			txtFile.setText(path);
		}
	}
	public void onOk(){
		if(txtFile.getText().length() < 3){
			MessageDialogFactory.showErrorDialog(null, "Please provide location of ADB to continue.");
		}else{
			this.setVisible(false);
		}
	}
	public void onCancel(){
		this.setVisible(false);
	}
	/**
	 * get location of ADB. e.g: /User/Documents/adt-bundle-windows-x86-20130729/sdk/tools/adb
	 * @return location of ADB executable file
	 */
	public String getADBPath(){
		return txtFile.getText();
	}
}//end class
