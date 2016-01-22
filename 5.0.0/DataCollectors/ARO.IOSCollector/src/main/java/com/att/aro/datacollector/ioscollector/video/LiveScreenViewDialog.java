/*
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

package com.att.aro.datacollector.ioscollector.video;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;

import com.att.aro.core.util.Util;

public class LiveScreenViewDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private ImagePanel imagePanel;
	

	/**
	 * Create the dialog.
	 */
	public LiveScreenViewDialog() {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
	//	setIconImage(Toolkit.getDefaultToolkit().getImage("/Users/hamzehzawawy/Documents/TestWorkspace/ARO.UI/src/main/resources/images/aro_24.png"));//LiveScreenViewDialog.class.getResource("/Users/hamzehzawawy/Documents/TestWorkspace/ARO.UI/src/main/resources/images/aro_24.png")));
		
		String dir = Util.getCurrentRunningDir();
		File dirfile = new File(dir);
		dir = dirfile.getParent();
		
		String imagesDir = Util.getAroLibrary()
				+ Util.FILE_SEPARATOR + ".drivers" 
				+ Util.FILE_SEPARATOR + "libimobiledevice"
				+ Util.FILE_SEPARATOR + "images"
				+ Util.FILE_SEPARATOR 
				;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(imagesDir+"aro_24.png"));
		
		setModal(true);
		setTitle("Live Video Screen Capture");
		setResizable(false);
		setBounds(100, 100, 367, 722);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
	    Image image = Toolkit.getDefaultToolkit().getImage(imagesDir+"blackscreen.png");
	    imagePanel = new ImagePanel(image);
	    imagePanel.setBounds(0, 0, 360, 640);
	    
		contentPanel.add(imagePanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton stopButton = new JButton("Stop");
				stopButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(imagesDir+"X_active.png")));
				stopButton.setActionCommand("Stop");
				stopButton.setFocusable(false);
				stopButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						onStop();
					}});
				buttonPane.add(stopButton);
				getRootPane().setDefaultButton(stopButton);
			}
		}
	}
	void onStop(){
		this.setVisible(false);
	}
	public void setImage(Image image){
		this.imagePanel.setImage(image);
	}
	public int getViewWidth(){
		return imagePanel.getWidth();
	}
	public int getViewHeight(){
		return imagePanel.getHeight();
	}
}
