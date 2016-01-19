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
package com.att.aro.ui.view.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROMenuAdder;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;
import com.att.aro.ui.view.SharedAttributesProcesses;
import com.att.aro.ui.view.menu.tools.ExportReport;
import com.att.aro.ui.view.menu.tools.TimeRangeAnalysisDialog;

/**
 * @author Nathan F Syfrig/Harikrishna Yaramachu
 *
 */
public class AROToolMenu implements ActionListener{

	private final AROMenuAdder menuAdder = new AROMenuAdder(this);

	private JMenu toolMenu;
	SharedAttributesProcesses parent;

	@InjectLogger
	private ILogger log;


	private enum MenuItem {
		menu_tools,
		menu_tools_wireshark,
		menu_tools_timerangeanalysis,
		menu_tools_dataDump,
		menu_tools_htmlExport,
		menu_tools_jsonExport
	}

	public AROToolMenu(SharedAttributesProcesses parent){
		super();
		this.parent = parent;
	}
	
	/**
	 * @return the toolMenu
	 */
	public JMenu getMenu() {
		if(toolMenu == null){
			toolMenu = new JMenu(ResourceBundleHelper.getMessageString(MenuItem.menu_tools));
			toolMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			
			if (Desktop.isDesktopSupported()) {
				toolMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_tools_wireshark));
			}

			toolMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_tools_timerangeanalysis));
//			toolMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_tools_dataDump));

			toolMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_tools_htmlExport));
			toolMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_tools_jsonExport));
		}
		
		return toolMenu;
	}

	@Override
	public void actionPerformed(ActionEvent aEvent) {

		if(menuAdder.isMenuSelected(MenuItem.menu_tools_wireshark, aEvent)){
			openPcapAnalysis();
		} else if(menuAdder.isMenuSelected(MenuItem.menu_tools_timerangeanalysis, aEvent)){
			openTimeRangeAnalysis();
		} else if(menuAdder.isMenuSelected(MenuItem.menu_tools_htmlExport, aEvent)){
			exportHtml();
		} else if(menuAdder.isMenuSelected(MenuItem.menu_tools_jsonExport, aEvent)){
			exportJson();			
		}
	}
	
	
	private void exportHtml(){
		ExportReport exportHtml = new ExportReport(parent, false, ResourceBundleHelper.getMessageString("menu.tools.export.error"));
		exportHtml.execute();
	}
	
	
	private void exportJson(){
		ExportReport exportJson = new ExportReport(parent, true, ResourceBundleHelper.getMessageString("menu.tools.export.error"));
		exportJson.execute();
	}
	
	/**
	 * Initiates the Pcap File Analysis for the trace data on selecting the Pcap
	 * File Analysis Menu Item.
	 */
	private void openPcapAnalysis() {
		// Make sure trace is loaded
		AROTraceData traceData = ((MainFrame)parent).getController().getTheModel();
		if (traceData == null) {
			MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(), ResourceBundleHelper.getMessageString("Error.notrace"), ResourceBundleHelper.getMessageString("error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Open PCAP analysis tool
		IFileManager fileManager = ContextAware.getAROConfigContext().getBean(IFileManager.class);
		if (traceData==null || traceData.getAnalyzerResult()==null || traceData.getAnalyzerResult().getTraceresult()==null || 
				traceData.getAnalyzerResult().getTraceresult().getTraceDirectory()==null){
			MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(),
					ResourceBundleHelper.getMessageString("menu.error.noTraceLoadedMessage"),
					ResourceBundleHelper.getMessageString("menu.error.title"),
					JOptionPane.ERROR_MESSAGE);	
		} else {
			File dir = fileManager.createFile(traceData.getAnalyzerResult().getTraceresult().getTraceDirectory());
			File[] trafficFiles;
			if (fileManager.isFile(dir.getAbsolutePath())){
				trafficFiles = new File[] {new File(dir.getAbsolutePath()) };
			} else {
				trafficFiles = getTrafficTextFiles(dir);//(new File(dir)).listFiles(filter);
				
			}
			if (trafficFiles!=null && trafficFiles.length>0) {
				try {
					Desktop.getDesktop().open(trafficFiles[0]);
				} catch (NullPointerException e) {
					MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(), ResourceBundleHelper.getMessageString("menu.tools.error.noPcap"));
				} catch (IllegalArgumentException e) {
					MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(), ResourceBundleHelper.getMessageString("menu.tools.error.noPcap"));
				} catch (IOException e) {
					MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(), ResourceBundleHelper.getMessageString("menu.tools.error.noPcapApp"));
				} 
			}
		}
	}
	
	/**
	 * Returns a list of cap or pcap files in the specified folder
	 * @param dir
	 * 			the specified folder where to look for pcap/cap files
	 * @return
	 * 			a list of the pcap files found in the specified folder
	 */
	private File[] getTrafficTextFiles(File dir) {
	    return dir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.indexOf("traffic")>-1)
					return true;
				else 
					return false;
		    }
	    });
	}
	
	private void openTimeRangeAnalysis() {
		MainFrame mainFrame = ((MainFrame)parent);
		if (mainFrame.getController().getTheModel()!=null && mainFrame.getController().getTheModel().getAnalyzerResult()!=null){
			PacketAnalyzerResult analysisData = mainFrame.getController().getTheModel().getAnalyzerResult();
			TimeRangeAnalysisDialog timeRangeDialog = new TimeRangeAnalysisDialog(mainFrame.getJFrame(), analysisData);
			timeRangeDialog.setVisible(true);
		} else {
			MessageDialogFactory.showMessageDialog(((MainFrame)parent).getJFrame(),
					ResourceBundleHelper.getMessageString("menu.error.noTraceLoadedMessage"),
					ResourceBundleHelper.getMessageString("menu.error.title"),
					JOptionPane.ERROR_MESSAGE);	
			}
	}
	
}
