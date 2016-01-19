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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.jfree.util.Log;

import com.android.ddmlib.IDevice;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.pojo.CollectorStatus;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.fileio.impl.FileManagerImpl;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.util.Util;
import com.att.aro.datacollector.ioscollector.impl.IOSCollectorImpl;
import com.att.aro.ui.commonui.DataCollectorStartDialog;
import com.att.aro.ui.commonui.IosPasswordDialog;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.MainFrame;
import com.att.aro.ui.view.SharedAttributesProcesses;

/**
 * @author Harikrishna Yaramachu
 * @author Barry Nelson
 */
public class ARODataCollectorMenu implements ActionListener , MenuListener{

	LoggerImpl log = new LoggerImpl(this.getClass().getName());
	
	private IFileManager fileManager = new FileManagerImpl();

	private JMenu dataCollectorMenu;
	private SharedAttributesProcesses parent;
	private JMenuItem dataCollectorStartMenuItem;
	private JMenuItem dataCollectorStopMenuItem;

	private String menuItemDatacollectorStart = ResourceBundleHelper.getMessageString("menu.datacollector.start");
	private String menuItemDatacollectorStop = ResourceBundleHelper.getMessageString("menu.datacollector.stop");

	public ARODataCollectorMenu(SharedAttributesProcesses parent){
		super();
		this.parent = parent;
	}

	/**
	 * @return the dataCollectorMenu
	 */
	public JMenu getMenu() {
		
		if(dataCollectorMenu == null){
			dataCollectorMenu = new JMenu(ResourceBundleHelper.getMessageString("menu.datacollector"));
			dataCollectorMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			
			dataCollectorMenu.addActionListener(this);
			dataCollectorMenu.addMenuListener(this);
			
			dataCollectorMenu.add(getJdataCollectorStart());
			dataCollectorMenu.add(getJdataCollectorStop());
		//	dataCollectorMenu.setEnabled(false);
		}
		setStartMenuItem(true);
		return dataCollectorMenu;
	}

	private JMenuItem getJdataCollectorStart(){
		dataCollectorStartMenuItem = getMenuItemInstance();
		dataCollectorStartMenuItem.setText(menuItemDatacollectorStart);
		dataCollectorStartMenuItem.addActionListener(this);
		return dataCollectorStartMenuItem;
	}
	
	private JMenuItem getJdataCollectorStop(){
		dataCollectorStopMenuItem = getMenuItemInstance();
		dataCollectorStopMenuItem.setText(menuItemDatacollectorStop);
		dataCollectorStopMenuItem.addActionListener(this);
		return dataCollectorStopMenuItem;
	}
	
	private JMenuItem getMenuItemInstance(){
		return new JMenuItem();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent) {
		
		if(aEvent.getActionCommand().equalsIgnoreCase(menuItemDatacollectorStart)){
			
			Object event = aEvent.getSource();
			if (event instanceof JMenuItem){

				List<IDataCollector> collectors = parent.getAvailableCollectors();
				log.info("collector count:"+collectors.size());
				
				for (IDataCollector collector:collectors){
					log.info(collector.getName());
				}
				
				if (collectors == null || collectors.isEmpty()){
					MessageDialogFactory.getInstance();
					MessageDialogFactory.showMessageDialog(((MainFrame) parent).getJFrame()
							, ResourceBundleHelper.getMessageString("collector.nocollectors")
							, ResourceBundleHelper.getMessageString("menu.error.title")
							, JOptionPane.ERROR_MESSAGE);
							return;
				}
				
				IDevice[] devices = parent.getConnectedDevices();
				IDataCollector iosCollector = findIOSCollector(collectors);
				String iOsDeviceID = null;
				if (iosCollector != null) {
					iOsDeviceID = getIOSDevice(iosCollector);
					if (iOsDeviceID != null && !iOsDeviceID.isEmpty() && iosCollector.getPassword().isEmpty()) {
						String password = "invalid";
						String hint = "";
						do {
							password = requestPassword(hint);
							if (password == null){
								return;
							}
							if (iosCollector.setPassword(password)){
								break;
							} else {
								hint = "invalid";
							}
						} while (true);
					}
				}
				
				if ((devices == null || devices.length == 0) 
				&& (iOsDeviceID == null || iOsDeviceID.isEmpty())){
					MessageDialogFactory.getInstance();
					MessageDialogFactory.showMessageDialog(((MainFrame) parent).getJFrame()
							, ResourceBundleHelper.getMessageString("collector.nodevices")
							, ResourceBundleHelper.getMessageString("menu.error.title")
							, JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				// have at least one collector and  at least one device
				// default behavior
				// iOSDevice is paramount to Android
				// if iOsDeviceID is empty, then Android device 0 is used
				chooseFolder(menuItemDatacollectorStart, 0, iOsDeviceID);
			}
			
		} else if (aEvent.getActionCommand().equalsIgnoreCase(menuItemDatacollectorStop)) {
			((MainFrame) parent).stopCollector();
			setStartMenuItem(true);
		}
	}

	/**
	 * ask user for a password
	 * @param hint 
	 * @return
	 */
	private String requestPassword(String hint) {
		IosPasswordDialog dialog = new IosPasswordDialog(((MainFrame) parent).getJFrame(), hint);
		return dialog.getPassword();
	}

	private String getIOSDevice(IDataCollector iOSCollector){
		String udid = null;
		if (iOSCollector != null){
			IOSCollectorImpl iOSCollectorImpl = new IOSCollectorImpl();
			StatusResult status = new StatusResult();
			udid = iOSCollectorImpl.getDeviceSerialNumber(status);
			
		}
		return udid; 
	}
	
	private IDataCollector findIOSCollector(List<IDataCollector> collectors) {
		if (Util.isMacOS()) {
			for (IDataCollector collector : collectors) {
				if (collector instanceof IOSCollectorImpl) {
					return collector;
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * <pre>Custom dialog to choose (enter) trace folder name
	 * actions:
	 * 	Enter trace folder name
	 *  Checkbox Capture Video
	 *  Cancel - restores menu items, returns null
	 *  OK - toggles menu items, triggers startCollector
	 *   </pre>
	 * @param title for dialog
	 * @param deviceId
	 * @param deviceSerialNumber
	 * @return traceFolderName or null if cancelled
	 */
	private String chooseFolder(String title, int deviceId, String deviceSerialNumber) {
		
		String traceFolderName = "";
		DataCollectorStartDialog dialog = new DataCollectorStartDialog(((MainFrame) parent).getJFrame(), traceFolderName, true );
		if (dialog.getResponse()){
			
			traceFolderName = dialog.getTraceFolder();
			
			String traceFolderPath = (deviceSerialNumber == null || deviceSerialNumber.isEmpty())
					?Util.getAROTraceDirAndroid() + System.getProperty("file.separator") + traceFolderName
					:Util.getAROTraceDirIOS() + System.getProperty("file.separator") + traceFolderName;
			String currentPath = ((MainFrame) parent).getTracePath();
//			log.info("currentPath directory :"+currentPath+ " idel: "+ traceFolderPath);
			if (fileManager.directoryExistAndNotEmpty(traceFolderPath)) {
				int result = folderExistsDialog();
				if (result == JOptionPane.OK_OPTION) {
					if(traceFolderPath.equals(currentPath)){
						new MessageDialogFactory().showErrorDialog(null,
								ResourceBundleHelper.getMessageString("viewer.contentUnwritable"));

						return null;
					}
					deleteFolderContents(traceFolderPath);
				} else {
					return null;
				}
			}
			
			if (deviceSerialNumber == null || deviceSerialNumber.isEmpty()){
				// Android
				((MainFrame) parent).startCollector(deviceId, traceFolderName, dialog.getRecordVideo());
			} else {
				// iOS
				IDataCollector iOsCollector = findIOSCollector(parent.getAvailableCollectors());
				((MainFrame) parent).startCollectorIos(iOsCollector, deviceSerialNumber, traceFolderName, dialog.getRecordVideo());
			}

		} else {
			traceFolderName = null;
		}
		dialog.dispose();
		return traceFolderName;
	}

	/**
	 * Delete all items from a folder
	 * 
	 * @param folderPath
	 */
	private void deleteFolderContents(String folderPath) {

		String[] files = fileManager.list(folderPath, null);
		for (String file : files) {
			String filepath = folderPath + Util.FILE_SEPARATOR + file;
			if (fileManager.directoryExistAndNotEmpty(filepath)) {
				deleteFolderContents(filepath);
			}
			boolean delResult = fileManager.deleteFile(filepath);
			Log.info("delete :" + file + (delResult ? " deleted" : " failed"));
		}
	}
	
	/**
	 * Check for prior existence of trace folder. Generate dialog to ask about reuse, if it does exist.
	 * @return 
	 */
	private int folderExistsDialog() {
		MessageDialogFactory.getInstance();
		String mssg = ResourceBundleHelper.getMessageString("Error.tracedirexists");
		String title = ResourceBundleHelper.getMessageString("aro.title.short");
		int dialogResults = MessageDialogFactory.showConfirmDialog(((MainFrame) parent).getJFrame(), mssg, title, JOptionPane.YES_NO_OPTION);
		log.info("replace directory :"+dialogResults);
		return dialogResults;
	}

	/**
	 * Controls state of the dataCollectorStartMenuItem and dataCollectorStopMenuItem.
	 * Only one or the other should be active.
	 * 
	 * @param active
	 */
	public void setStartMenuItem(boolean active) {
		log.debug(active?"set start":"set stop");
		dataCollectorStartMenuItem.setEnabled(active);
		dataCollectorStopMenuItem.setEnabled(!active);
	}

	@Override
	public void menuSelected(MenuEvent e) {
		CollectorStatus collectorStatus = parent.getCollectorStatus();
		setStartMenuItem(collectorStatus == null || collectorStatus.equals(CollectorStatus.STOPPED));
	}

	@Override
	public void menuDeselected(MenuEvent e) {

	}

	@Override
	public void menuCanceled(MenuEvent e) {

	}
}
