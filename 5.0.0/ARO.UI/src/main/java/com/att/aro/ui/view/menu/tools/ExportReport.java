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
package com.att.aro.ui.view.menu.tools;

import java.awt.Desktop;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.mvc.IAROView;
import com.att.aro.ui.commonui.AROUIWorker;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;

public class ExportReport extends AROUIWorker<Void, Void>{

	JFileChooser chooser;
	IAROView parent;
	boolean json;
	File exportPath;
	
	public ExportReport(IAROView parent, boolean json, String message) {
		super(((MainFrame)parent).getJFrame(), message);
		this.parent = parent;
		this.json = json;
	}

	@Override
	public void before() {
		chooser = new JFileChooser();
		chooser = new JFileChooser(UserPreferences.getInstance().getLastExportDirectory());
		chooser.setDialogTitle(ResourceBundleHelper.getMessageString("fileChooser.Title"));
		chooser.setApproveButtonText(ResourceBundleHelper.getMessageString("fileChooser.Save"));
		chooser.setMultiSelectionEnabled(false);	
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (json){
			FileNameExtensionFilter htmlFilter = new FileNameExtensionFilter(ResourceBundleHelper.getMessageString("fileChooser.desc.json"), 
					ResourceBundleHelper.getMessageString("fileChooser.contentType.json"));
			chooser.addChoosableFileFilter(htmlFilter);
			chooser.setFileFilter(htmlFilter);
		} else {
			FileNameExtensionFilter htmlFilter = new FileNameExtensionFilter(ResourceBundleHelper.getMessageString("fileChooser.desc.html"), 
					ResourceBundleHelper.getMessageString("fileChooser.contentType.html"));
			chooser.addChoosableFileFilter(htmlFilter);
			chooser.setFileFilter(htmlFilter);
		}
	}

	@Override
	public void doing() throws Exception{
		saveFile(chooser);
	}

	@Override
	public void after() {
		if (exportPath!=null) {
			UserPreferences.getInstance().setLastExportDirectory(exportPath);
		}
	}

	/**
	 * Method to export the table content in to the JSON or HTML file format.
	 * 
	 * @param chooser
	 *            {@link JFileChooser} object to validate the save option.
	 */
	private void saveFile(JFileChooser chooser) throws Exception {
		if (chooser.showSaveDialog(((MainFrame)parent).getJFrame()) == JFileChooser.APPROVE_OPTION) {
			exportPath = chooser.getSelectedFile();
			if (!chooser.getFileFilter().accept(exportPath)) {
				if (json) {
					exportPath = new File(exportPath.getAbsolutePath() + "." + ResourceBundleHelper.getMessageString("fileChooser.contentType.json"));
				} else {
					exportPath = new File(exportPath.getAbsolutePath() + "." + ResourceBundleHelper.getMessageString("fileChooser.contentType.html"));
				}
			}
			
			if (exportPath.exists()){
				// file already exists
				int res = MessageDialogFactory.showConfirmDialog(((MainFrame)parent).getJFrame(), ResourceBundleHelper.getMessageString("menu.tools.export.warning"));
				if (res != JOptionPane.YES_OPTION) {
					return;
				}
			} else {
				if (json) {
					((MainFrame)parent).getController().printReport(true, exportPath.getAbsolutePath());
				} else {
					((MainFrame)parent).getController().printReport(false, exportPath.getAbsolutePath());
				}
			}
			
			if (exportPath.getName().contains(".html") || exportPath.getName().contains(".json")) {
				MessageDialogFactory dialogFactory = new MessageDialogFactory();
				int res = dialogFactory.showExportConfirmDialog(((MainFrame)parent).getJFrame());
				if (res == JOptionPane.YES_OPTION) {
					try {
						Desktop desktop = Desktop.getDesktop();
						desktop.open(exportPath);
					} catch (UnsupportedOperationException unsupportedException) {
						MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(),
								ResourceBundleHelper.getMessageString("Error.unableToOpen"));
					}
				}
			} 
		}
	}
}
