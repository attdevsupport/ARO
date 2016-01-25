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

package com.att.aro.ui.view.menu.tools;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.util.CrashHandler;
import com.att.aro.mvc.AROController;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.commonui.UserPreferences;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.menu.profiles.ProfileException;

/**
 * Manages export functionality of multiple traces.
 */
public class DataDump {

	private ILogger log = new LoggerImpl(DataDump.class.getCanonicalName());

	private final AROController controller;
	private final File traceDir;
	private File fileToSave;
	private final boolean singleTrace;
	private boolean subfolderAccess = true;
	private boolean userConfirmation = false;

	private static final Window MSG_WINDOW = new Window(new Frame());

	private static final int FILE_NAME_MAX_LENGTH = 50;

	private static final String EXTENSION_FILTER = "." + ResourceBundleHelper.getMessageString(
			MessageItem.fileChooser_contentType_json);

	private static enum MessageItem {
		datadump_invalidfolder,
		Error_dataDump_valideFolder,
		datadump_autofilename,
		datadump_exists,
		table_export_success,
		Error_unableToOpen,
		Error_outOfMemory,
		fileChooser_contentType_csv,
		fileChooser_desc_csv,
		fileChooser_contentType_json,
		fileChooser_desc_json,
		fileChooser_Title,
		fileChooser_Save,
		exportall_errorLongFileName,
		fileChooser_fileExists,
		datadump_trafficFile,
		datadump_subfolder,
	}

	/**
	 * Initializes a new instance of {@link DataDump}.
	 * 
	 * If <code>singleTraceDir</code> is set to true, the datadump file will
	 * automatically be named and saved into the provided trace directory. No
	 * error or file chooser dialogs will be displayed and errors/exceptions
	 * will be thrown rather than shown in an error dialog. The
	 * <code>overwrite</code> flag is only valid when
	 * <code>singleTraceDir</code> is set to true.
	 * 
	 * @param traceDir
	 *            File
	 * @param prof
	 *            Profile
	 * @param singleTraceDir
	 *            boolean
	 * @param overwrite
	 *            boolean
	 * @throws IOException
	 */
	public DataDump(File traceDir, AROController controller, boolean singleTraceDir,
			final boolean overwrite) throws IOException {
		this.controller = controller;
		this.traceDir = traceDir;
		this.singleTrace = singleTraceDir;

		// Initialize application window
		AROUIManager.init();
		
		
		if(!this.traceDir.exists()) {
			if (singleTrace) {
				throw new IOException(ResourceBundleHelper.getMessageString(
						MessageItem.datadump_invalidfolder));
			} else {
				new MessageDialogFactory().showErrorDialog(MSG_WINDOW,
						ResourceBundleHelper.getMessageString(
								MessageItem.datadump_invalidfolder));
					return;
			}
		}
		
		//create a list of one for single trace folder, list of contained trace folders if not
		final List<File> traceFolders = singleTrace ? Arrays
				.asList(traceDir) : Arrays.asList(traceDir
				.listFiles(new java.io.FileFilter() {
					@Override
					public boolean accept(File arg0) {
						return arg0.isDirectory();
					}
				}));

		if (traceFolders.size() == 0) {
			new MessageDialogFactory().showErrorDialog(MSG_WINDOW,
					ResourceBundleHelper.getMessageString(
							MessageItem.Error_dataDump_valideFolder));
			return;
		}
		
		if (singleTrace) {
			fileToSave = new File(traceDir,
					(File.separatorChar + ResourceBundleHelper.
							getMessageString(MessageItem.datadump_autofilename)));
			if (!overwrite && fileToSave.exists()) {
				log.info(ResourceBundleHelper.getMessageString(
						MessageItem.datadump_exists));
				return;
			}
		} else {
			if (!showSaveChooser()) {
				return;
			}
		}

		// create and start SwingWorker thread
		startBackgroundWorker(traceFolders);
	}
	
	/**
	 * Initialize and start background worker thread to create datadump file
	 * @param traceFolders
	 */
	private void startBackgroundWorker(final List<File> traceFolders) {
		SwingWorker<File, Object> datadumpWorker = new SwingWorker<File, Object>() {
			@Override
			protected File doInBackground() throws IOException, ProfileException {
				Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
				startDataDump(traceFolders);
				return fileToSave;
			}

			@Override
			protected void done() {
				try {
					if (get().getName().contains(EXTENSION_FILTER)) {
						if (singleTrace) {
							log.info(ResourceBundleHelper.getMessageString(
									MessageItem.table_export_success));
						} else {
							if (new MessageDialogFactory()
									.showExportConfirmDialog(MSG_WINDOW) ==
											JOptionPane.YES_OPTION) {
								Desktop desktop = Desktop.getDesktop();
								desktop.open(get());
							}
						}
					}
					this.cancel(true);
				} catch (IOException e) {
					log.error("Unexpected IOException analyzing trace", e);
					new MessageDialogFactory().showUnexpectedExceptionDialog(MSG_WINDOW, e);
				} catch (UnsupportedOperationException unsupportedException) {
					MessageDialogFactory.showMessageDialog(MSG_WINDOW,
							ResourceBundleHelper.getMessageString(
									MessageItem.Error_unableToOpen));
				} catch (InterruptedException e) {
					log.error("Unexpected exception analyzing trace", e);
					new MessageDialogFactory().showUnexpectedExceptionDialog(MSG_WINDOW, e);
				} catch (ExecutionException e) {
					log.error("Unexpected execution exception analyzing trace", e);
					if (e.getCause() instanceof OutOfMemoryError) {
						new MessageDialogFactory().showErrorDialog(null,
								ResourceBundleHelper.getMessageString(
										MessageItem.Error_outOfMemory));
					} else {
						new MessageDialogFactory().showUnexpectedExceptionDialog(MSG_WINDOW, e);
					}
				}
			}
		};
		
		datadumpWorker.execute();
	}
	
	/**
	 * Initiates the save chooser options and creates a file.
	 */
	private boolean showSaveChooser() {

		final String fileExtention = ResourceBundleHelper.getMessageString(
				MessageItem.fileChooser_contentType_json);
		
		JFileChooser chooser = new JFileChooser(UserPreferences.getInstance().getLastExportDirectory());
		FileNameExtensionFilter ff;
		ff = new FileNameExtensionFilter(ResourceBundleHelper.getMessageString(
				MessageItem.fileChooser_desc_json), fileExtention);
		chooser.addChoosableFileFilter(ff);
		chooser.setFileFilter(ff);
		chooser.setDialogTitle(ResourceBundleHelper.getMessageString(
				MessageItem.fileChooser_Title));
		chooser.setApproveButtonText(ResourceBundleHelper.getMessageString(
				MessageItem.fileChooser_Save));
		chooser.setMultiSelectionEnabled(false);

		if (chooser.showSaveDialog(MSG_WINDOW) != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		fileToSave = chooser.getSelectedFile();
		if (fileToSave.getName().length() >= FILE_NAME_MAX_LENGTH) {
			new MessageDialogFactory().showErrorDialog(MSG_WINDOW,
					ResourceBundleHelper.getMessageString(
							MessageItem.exportall_errorLongFileName));
			return false;
		}
		if (!chooser.getFileFilter().accept(fileToSave)) {
			fileToSave = new File(fileToSave.getAbsolutePath() + "." + fileExtention);
		}
		
		String message = MessageFormat.format(ResourceBundleHelper.getMessageString(
				MessageItem.fileChooser_fileExists), fileToSave.getAbsolutePath());
		if (fileToSave.exists()) {
			int option = new MessageDialogFactory().showConfirmDialog(
					MSG_WINDOW, message, JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Starts dumping data in CSV file for the provided trace files.
	 * 
	 * @param traceFolders
	 *            - List of trace folder names.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter startDataDump(List<File> traceFolders) throws IOException, ProfileException {

		FileWriter writer = new FileWriter(fileToSave);
		try {
			List<File> validFolderList = new ArrayList<File>();
			Set<AROTraceData> analysis = new LinkedHashSet<AROTraceData>();
			
			getValidFolderList(traceFolders, validFolderList);
			for (File traceDirectory : validFolderList) {
				try {
					analysis.add(controller.runAnalyzer(traceDirectory.getAbsolutePath(),
						controller.getTheModel().getAnalyzerResult().getProfile(), null));
				} catch (Exception e) {
					log.warn("Unable to run analysis on folder: " + traceDirectory, e);
				}
			}

			try {
				new ObjectMapper().writeValue(fileToSave, analysis);
			} catch (JsonGenerationException e) {
				log.error(e.getMessage());
				new MessageDialogFactory().showUnexpectedExceptionDialog(MSG_WINDOW, e);
			} catch (JsonMappingException e) {
				log.error(e.getMessage());
				new MessageDialogFactory().showUnexpectedExceptionDialog(MSG_WINDOW, e);
			} catch (IOException e) {
				log.error(e.getMessage());
				new MessageDialogFactory().showUnexpectedExceptionDialog(MSG_WINDOW, e);
			}

			UserPreferences.getInstance().setLastTraceDirectory(traceDir);
		} finally {
			writer.close();
		}
		return writer;
	}

	/**
	 * Collects valid trace folders.
	 * 
	 * @param traceFolders
	 *            - List of available folder names.
	 * @param validFolderList
	 *            - List of valid trace folder names.
	 */
	private void getValidFolderList(List<File> traceFolders, List<File> validFolderList) {
		for (File traceDirectory : traceFolders) {

			// Check if it is a valid trace folder or not
			if (new File(traceDirectory, ResourceBundleHelper.getMessageString(
					MessageItem.datadump_trafficFile)).exists()) {
				validFolderList.add(traceDirectory);
			}

			// Check if it has sub-folders or not
			List<File> allFolders = Arrays.asList(traceDirectory.listFiles(new java.io.FileFilter() {
				@Override
				public boolean accept(File arg0) {
					return arg0.isDirectory();
				}
			}));
			if (subfolderAccess && allFolders.size() > 0) {
				if (!userConfirmation
						&& new MessageDialogFactory().showConfirmDialog(MSG_WINDOW,
							ResourceBundleHelper.getMessageString(
								MessageItem.datadump_subfolder), JOptionPane.YES_NO_OPTION) ==
									JOptionPane.NO_OPTION) {
					subfolderAccess = false;
					continue;
				}
				userConfirmation = true;
				getValidFolderList(allFolders, validFolderList);
			}
		}
	}
}
