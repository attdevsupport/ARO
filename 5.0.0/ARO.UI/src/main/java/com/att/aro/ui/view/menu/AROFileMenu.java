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
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.text.MessageFormat;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.core.ILogger;
import com.att.aro.core.util.CrashHandler;
import com.att.aro.ui.commonui.AROMenuAdder;
import com.att.aro.ui.commonui.AROPrintablePanel;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.IAROPrintable;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.TabPanelCommon;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.exception.AROUIIllegalStateException;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.SharedAttributesProcesses;
import com.att.aro.ui.view.SharedAttributesProcesses.TabPanels;
import com.att.aro.ui.view.menu.file.ADBPathDialog;
import com.att.aro.ui.view.menu.file.MissingTraceFiles;

/**
 * @author Nathan F Syfrig/Harikrishna Yaramachu
 *
 */
public class AROFileMenu implements ActionListener, MenuListener {
	private ILogger log = ContextAware.getAROConfigContext().getBean(ILogger.class);
	
	private final AROMenuAdder menuAdder = new AROMenuAdder(this);

	private JMenu fileMenu = null;
	private SharedAttributesProcesses parent;

	private TabPanelCommon tabPanelCommon = new TabPanelCommon();
	private JMenuItem printItem;

	private UserPreferences userPreferences = UserPreferences.getInstance();

	private enum MenuItem {
		menu_file,
		menu_file_open,
		menu_file_pcap,
		menu_file_adb,
		menu_file_print,
		menu_file_exit,
		error_printer,
		error_printer_notprintable,
		file_missingAlert,
	}

	public AROFileMenu(SharedAttributesProcesses parent){
		super();
		this.parent = parent;
	}
		
	/**
	 * 
	 * @return
	 */
	public JMenu getMenu() {
		if(fileMenu == null){
			fileMenu = new JMenu(ResourceBundleHelper.getMessageString(MenuItem.menu_file));
			fileMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			fileMenu.addActionListener(this);
			fileMenu.addMenuListener(this);

			fileMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_file_open));
			fileMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_file_pcap));
			fileMenu.addSeparator();

			fileMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_file_adb));
			fileMenu.addSeparator();

			printItem = menuAdder.getMenuItemInstance(MenuItem.menu_file_print);
			TabPanels tabbedPanel = parent.getCurrentTabPanel();
			printItem.setEnabled(tabbedPanel == TabPanels.tab_panel_best_practices ||
					tabbedPanel == TabPanels.tab_panel_statistics);
			fileMenu.add(printItem);
			fileMenu.addSeparator();

			fileMenu.add(menuAdder.getMenuItemInstance(MenuItem.menu_file_exit));
		}
		return fileMenu;
	}

	/**
	 * 
	 */
	@Override
	public void actionPerformed(ActionEvent aEvent) {
		if (menuAdder.isMenuSelected(MenuItem.menu_file_open, aEvent)) {
			File tracePath = null;
			Object event = aEvent.getSource();
			if (event instanceof JMenuItem) {
				tracePath = chooseFileOrFolder(JFileChooser.DIRECTORIES_ONLY,
						ResourceBundleHelper.getMessageString(MenuItem.menu_file_open));
				if (tracePath != null) {
					MissingTraceFiles missingTraceFiles = new MissingTraceFiles(tracePath);
					Set<File> missingFiles = missingTraceFiles.retrieveMissingFiles();
					if (missingFiles.size() > 0) {
//						MessageDialogFactory.showMessageDialog(parent.getFrame(),
//								MessageFormat.format(ResourceBundleHelper.getMessageString(
//										MenuItem.file_missingAlert),
//											missingTraceFiles.formatMissingFiles(missingFiles)));
						log.warn(MessageFormat.format(ResourceBundleHelper.getMessageString(
							MenuItem.file_missingAlert),
								missingTraceFiles.formatMissingFiles(missingFiles)));
					}
					parent.updateTracePath(tracePath);
					userPreferences.setLastTraceDirectory(tracePath.getParentFile());
				}
			}
		} else if (menuAdder.isMenuSelected(MenuItem.menu_file_pcap, aEvent)) {
			File tracePath = null;
			Object event = aEvent.getSource();
			if (event instanceof JMenuItem) {
				tracePath = chooseFileOrFolder(JFileChooser.FILES_ONLY,
						ResourceBundleHelper.getMessageString(MenuItem.menu_file_pcap));
				if (tracePath != null) {
					parent.updateTracePath(tracePath);
					userPreferences.setLastTraceDirectory(tracePath.getParentFile().getParentFile());
				}
			}
		} else if (menuAdder.isMenuSelected(MenuItem.menu_file_adb, aEvent)) {
			new ADBPathDialog(parent).setVisible(true);
		} else if (menuAdder.isMenuSelected(MenuItem.menu_file_print, aEvent)) {
			handlePrint();
		} else if (menuAdder.isMenuSelected(MenuItem.menu_file_exit, aEvent)) {
			parent.dispose();
			System.exit(0);
		}
	}

	/**
	 * 
	 * @param mode
	 * @param title
	 * @return
	 */
	private File chooseFileOrFolder(int mode, String title) {
		File tracePath = null;
		// open window to select from workspace/file system
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(mode);
		String defaultDir = userPreferences.getLastTraceDirectory() == null ?
			System.getProperty("user.home") :
				userPreferences.getLastTraceDirectory().toString();
		if (parent.getTracePath() != null && parent.getTracePath().lastIndexOf(File.separator)
				>-1) {
			defaultDir = parent.getTracePath().substring(0,
					parent.getTracePath().lastIndexOf(File.separator));
		}
		chooser.setCurrentDirectory(new File(defaultDir));
		
		if (mode==JFileChooser.FILES_ONLY){
			FileNameExtensionFilter pcapfilter = new FileNameExtensionFilter(
					"Pcap files (*.cap, *.pcap)", "cap", "pcap");
//			chooser.addChoosableFileFilter(pcapfilter);
			chooser.setFileFilter(pcapfilter);
		}
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			tracePath = chooser.getSelectedFile();
		} 
		return tracePath;
	}

	private void handlePrint() {
		final JComponent currentTabComponent = (JComponent) parent.getCurrentTabComponent();
		if (currentTabComponent instanceof IAROPrintable) {
			final IAROPrintable aroPrintable = (IAROPrintable) currentTabComponent;

			final PrinterJob printJob = PrinterJob.getPrinterJob();
			if (printJob.printDialog()) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
						printJob.setPrintable(new AROPrintablePanel(
								aroPrintable.getPrintablePanel()));
						try {
							printJob.print();
						} catch (PrinterException e) {
							String[] messageWrapper = new String[1];
							messageWrapper[0] = e.getLocalizedMessage();
							new MessageDialogFactory().showErrorDialog(null,
									tabPanelCommon.getText(MenuItem.error_printer,
											messageWrapper));
						}

					}
				}).start();
			}
		} else {
			throw new AROUIIllegalStateException(tabPanelCommon.getText(
					MenuItem.error_printer_notprintable));
		}
	}


	/**
	 * Need to determine whether the print option is enabled or not when menu is opened.
	 */
	@Override
	public void menuSelected(MenuEvent event) {
		TabPanels currentTabPanel = parent.getCurrentTabPanel();
		printItem.setEnabled(currentTabPanel == TabPanels.tab_panel_best_practices ||
				currentTabPanel == TabPanels.tab_panel_statistics);
	}
	@Override
	public void menuDeselected(MenuEvent event) { // Noop
	}
	@Override
	public void menuCanceled(MenuEvent event) { // Noop
	}
}
