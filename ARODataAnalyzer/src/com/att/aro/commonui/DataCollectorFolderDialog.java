/*
 *  Copyright 2012 AT&T
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
package com.att.aro.commonui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;

public class DataCollectorFolderDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JFileChooser file;
	JCheckBox chckbxCaptureVideo;

    public DataCollectorFolderDialog()
    {
        this(null);
    }

    /**
     * Create the dialog.
     */
    public DataCollectorFolderDialog(Frame parent) {
		setAlwaysOnTop(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(parent);
        setTitle("Folder Name");
		setBounds(100, 100, 500, 132);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblFolder = new JLabel("Enter Folder Name: ");
        lblFolder.setBounds(10, 11, 110, 14);
		contentPanel.add(lblFolder);
		
		textField = new JTextField();
		textField.setEditable(true);
        textField.setBounds(125, 8, 300, 20);
		contentPanel.add(textField);
		textField.setColumns(10);
		
		/*
		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setBounds(404, 7, 77, 23);
		btnBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				onBrowse();
			}
		});
		contentPanel.add(btnBrowse);
		*/
		
		chckbxCaptureVideo = new JCheckBox("Capture Video");
		chckbxCaptureVideo.setToolTipText("Capture video of mobile screen");
		chckbxCaptureVideo.setSelected(true);
		chckbxCaptureVideo.setBounds(6, 32, 150, 23);
		contentPanel.add(chckbxCaptureVideo);
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
		file.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//
	    // disable the "All files" option.
	    //
	    file.setAcceptAllFileFilterUsed(false);
	}
	void onBrowse(){
		file.showSaveDialog(DataCollectorFolderDialog.this);
		if(file.getSelectedFile() != null){
			String path = file.getSelectedFile().getPath();
			textField.setText(path);
		}
	}
	public void onOk(){
		if(textField.getText().length() < 1){
			MessageDialogFactory.showErrorDialog(null, "Please select a folder to store data.");
			return;
		}
		this.setVisible(false);
	}
	public void onCancel(){
		this.setVisible(false);
	}
	/**
	 * get full directory path selected by user. e.g: /User/Documents
	 * @return
	 */
	public String getFullDirectoryPath(){
		return textField.getText();
	}
	/**
	 * get the name of the directory. The last part of full directory after slash. 
	 * e.g: full path /User/Documents will return Documents as the name.
	 * @return 
	 */
	public String getDirectoryName(){
		String name = "";
		String path = textField.getText();
		if(path.length() > 1){
			path = path.replace('\\', '/');
			int index = path.lastIndexOf('/');
			if(index != -1){
				name = path.substring(index + 1);
			}else{
				name = path;
			}
		}
		return name;
	}
	/**
	 * return True if the checkbox is selected for Capture Video option.
	 * @return
	 */
	public boolean isCaptureVideo(){
		return chckbxCaptureVideo.isSelected();
	}
}//end class
