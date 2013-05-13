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

package com.att.aro.datadump;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.bp.BestPracticeDisplayFactory;
import com.att.aro.bp.BestPracticeDisplayGroup;
import com.att.aro.bp.BestPracticeExport;
import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.main.ProfileManager;
import com.att.aro.model.ApplicationPacketSummary;
import com.att.aro.model.BurstAnalysisInfo;
import com.att.aro.model.BurstCategory;
import com.att.aro.model.CacheInfoParser;
import com.att.aro.model.EnergyModel;
import com.att.aro.model.FileTypeSummary;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileException;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.ProfileType;
import com.att.aro.model.ProfileWiFi;
import com.att.aro.model.RRCStateMachine;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.model.UserPreferences;
import com.att.aro.util.Util;

/**
 * Manages export functionality of multiple traces.
 */
public class DataDump {

	private static final Logger LOGGER = Logger.getLogger(DataDump.class.getName());

	private File traceDir;
	private File fileToSave;
	private Profile profile;
	private boolean singleTrace;
	private boolean subfolderAccess = true;
	private boolean userConfirmation = false;
	private Collection<BestPracticeDisplayGroup> bestPracticeGroups = BestPracticeDisplayFactory.getInstance().getBestPracticeDisplay();
	private Collection<BestPracticeDisplay> bestPractices = new ArrayList<BestPracticeDisplay>();
	{
		for (BestPracticeDisplayGroup group : bestPracticeGroups) {
			for (BestPracticeDisplay bp : group.getBestPractices()) {
				bestPractices.add(bp);
			}
		}
	}

	private static final Window MSG_WINDOW = new Window(new Frame());

	private static final String LINE_SEP = System.getProperty(Util.RB.getString("statics.csvLine.seperator"));
	private static final String COMMA_SEP = Util.RB.getString("statics.csvCell.seperator");
	private static final String QUOTE_SEP = "\"";

	private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.00");

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
	 * @param dir
	 *            File
	 * @param prof
	 *            Profile
	 * @param singleTraceDir
	 *            boolean
	 * @param overwrite
	 *            boolean
	 * @throws IOException
	 */
	public DataDump(File dir, final Profile prof, final boolean singleTraceDir, final boolean overwrite) throws IOException {
		this.traceDir = dir;
		this.profile = prof;
		this.singleTrace = singleTraceDir;

		// Initialize application window
		AROUIManager.init();
		
		
		if(!this.traceDir.exists()) {
			if (singleTrace) {
				throw new IOException(Util.RB.getString("datadump.invalidfolder"));
			} else {
				MessageDialogFactory.showErrorDialog(MSG_WINDOW,
						Util.RB.getString("datadump.invalidfolder"));
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
			MessageDialogFactory.showErrorDialog(MSG_WINDOW,
					Util.RB.getString("Error.dataDump.valideFolder"));
			return;
		}
		
		if (singleTrace) {
			fileToSave = new File(traceDir,
					(File.separatorChar + Util.RB
							.getString("datadump.autofilename")));
			if (!overwrite && fileToSave.exists()) {
				LOGGER.log(Level.FINE, Util.RB.getString("datadump.exists"));
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
				startDataDump(traceFolders);
				return fileToSave;
			}

			@Override
			protected void done() {
				try {
					if (get().getName().contains(".csv")) {
						if (singleTrace) {
							LOGGER.log(Level.INFO, Util.RB.getString("table.export.success"));
						} else {
							if (MessageDialogFactory
									.showExportConfirmDialog(MSG_WINDOW) == JOptionPane.YES_OPTION) {
								Desktop desktop = Desktop.getDesktop();
								desktop.open(get());
							}
						}
					}
					this.cancel(true);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Unexpected IOException analyzing trace", e);
					MessageDialogFactory.showUnexpectedExceptionDialog(MSG_WINDOW, e);
				} catch (UnsupportedOperationException unsupportedException) {
					MessageDialogFactory.showMessageDialog(MSG_WINDOW,
							Util.RB.getString("Error.unableToOpen"));
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, "Unexpected exception analyzing trace", e);
					MessageDialogFactory.showUnexpectedExceptionDialog(MSG_WINDOW, e);
				} catch (ExecutionException e) {
					LOGGER.log(Level.SEVERE, "Unexpected execution exception analyzing trace",
							e);
					if (e.getCause() instanceof OutOfMemoryError) {
						MessageDialogFactory.showErrorDialog(null,
								Util.RB.getString("Error.outOfMemory"));
					} else {
						MessageDialogFactory.showUnexpectedExceptionDialog(MSG_WINDOW, e);
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
		JFileChooser chooser = new JFileChooser(UserPreferences.getInstance()
				.getLastExportDirectory());
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(Util.RB.getString("fileChooser.desc.csv"),
				Util.RB.getString("fileChooser.contentType.csv")));
		chooser.setDialogTitle(Util.RB.getString("fileChooser.Title"));
		chooser.setApproveButtonText(Util.RB.getString("fileChooser.Save"));
		chooser.setMultiSelectionEnabled(false);

		if (chooser.showSaveDialog(MSG_WINDOW) != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		fileToSave = chooser.getSelectedFile();
		if (fileToSave.getName().length() >= 50) {
			MessageDialogFactory.showErrorDialog(MSG_WINDOW,
					Util.RB.getString("exportall.errorLongFileName"));
			return false;
		}
		if (!chooser.getFileFilter().accept(fileToSave)) {
			fileToSave = new File(fileToSave.getAbsolutePath() + "."
					+ Util.RB.getString("fileChooser.contentType.csv"));
		}
		if (fileToSave.exists()) {
			if (MessageDialogFactory.showConfirmDialog(
					MSG_WINDOW,
					MessageFormat.format(Util.RB.getString("fileChooser.fileExists"),
							fileToSave.getAbsolutePath()), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
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
			Profile currentProfile = profile != null ? profile : ProfileManager
					.getInstance().getLastUserProfile(ProfileType.T3G);

			/*
			 * The 1nd, 2rd, and 3th line in CSV file:
			 */
			writer.append(Util.RB.getString("menu.profile") + ": " + currentProfile.getName());
			DataDumpHelper.addCommas(writer, 2);
			if (currentProfile instanceof Profile3G) {
				new DataDump3G().addHeader(writer, this.bestPractices);
			} else if (currentProfile instanceof ProfileLTE) {
				new DataDumpLTE().addHeader(writer, this.bestPractices);
			} else if (currentProfile instanceof ProfileWiFi) {
				new DataDumpWiFi().addHeader(writer, this.bestPractices);
			}

			/*
			 * The 4th line in CSV file:
			 */
			writer.append(LINE_SEP);

			List<File> validFolderList = new ArrayList<File>();
			getValidFolderList(traceFolders, validFolderList);
			for (File traceDirectory : validFolderList) {
				TraceData.Analysis analysis;
				try {
					TraceData traceData = new TraceData(traceDirectory);
					analysis = traceData.runAnalysis(currentProfile, null);
					addAnalysisContent(writer, analysis);
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Unable to run analysis on folder: " + traceDirectory, e);
				}
			}
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
		String trafficFile = Util.RB.getString("datadump.trafficFile");
		for (File traceDirectory : traceFolders) {

			// Check if it is a valid trace folder or not
			if (new File(traceDirectory, trafficFile).exists()) {
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
						&& MessageDialogFactory.showConfirmDialog(MSG_WINDOW,
								Util.RB.getString("datadump.subfolder"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {

					subfolderAccess = false;
					continue;
				}
				userConfirmation = true;
				getValidFolderList(allFolders, validFolderList);
			}
		}
	}

	/**
	 * Adds trace content into CSV file.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addAnalysisContent(FileWriter writer, TraceData.Analysis analysis)
			throws IOException {

		addCommonContents(writer, analysis);

		// Best practices data
		addBestPractices(writer, analysis);

		// Basic Statistics data
		addBasicStatistics(writer, analysis);

		// RRC Energy and RRC State simulation
		if (analysis.getProfile() instanceof Profile3G) {
			add3GRRCStateAndEnergySimulation(writer, analysis);
		} else if (analysis.getProfile() instanceof ProfileLTE) {
			addLTERRCStateAndEnergySimulation(writer, analysis);
		} else if (analysis.getProfile() instanceof ProfileWiFi) {
			addWiFiRRCStateAndEnergySimulation(writer, analysis);
		}

		// Cache Content score
		addPeripheralEnergy(writer, analysis);

		DataDumpHelper.addAnchor(writer);

		// Burst analysis
		for (BurstCategory burstCategory : BurstCategory.values()) {
			// skip unknown burst
			if (!burstCategory.equals(BurstCategory.UNKNOWN)) {
				addBurstAnalysis(writer, analysis, burstCategory);
			}
		}

		DataDumpHelper.addAnchor(writer);

		writer.append(COMMA_SEP);

		// Burst analysis
		addFileTypes(writer, analysis);

		// Trace Benchmarking
		addTraceBenchmarking(writer, analysis);

		// Trace Benchmarking
		addConnectionStatics(writer, analysis);

		// Application score
		addApplicationScore(writer, analysis);

		// Application score
		addApplicationEndPointSummary(writer, analysis);

		// Cache Content score
		addCacheContent(writer, analysis);

		writer.append(LINE_SEP);
		return writer;
	}

	private FileWriter addPeripheralEnergy(FileWriter writer, Analysis analysis) throws IOException {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		EnergyModel model = analysis.getEnergyModel();

		writer.append(QUOTE_SEP + nf.format(model.getGpsActiveEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getGpsStandbyEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getTotalGpsEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getTotalCameraEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getBluetoothActiveEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getBluetoothStandbyEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getTotalBluetoothEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + nf.format(model.getTotalScreenEnergy()) + QUOTE_SEP);
		return writer;
	}

	/**
	 * Adds Cache analysis content.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addCacheContent(FileWriter writer, Analysis analysis) throws IOException {
		CacheInfoParser cIPaser = analysis.getCacheInfoParser();
		NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(1);
		format.setMinimumFractionDigits(1);

		int total = cIPaser.getCacheable() + cIPaser.getNotCacheable();
		long totalBytes = cIPaser.getCacheableBytes() + cIPaser.getNotCacheableBytes();
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getCacheable(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getCacheableBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotCacheable(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotCacheableBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		total = cIPaser.getCacheMiss() + cIPaser.getNotCacheable() + cIPaser.getHitNotExpiredDup()
				+ cIPaser.getHitResponseChanged() + cIPaser.getHitExpiredDupClient()
				+ cIPaser.getHitExpiredDupServer() + cIPaser.getHitExpired304()
				+ cIPaser.getPartialHitExpiredDupClient() + cIPaser.getPartialHitExpiredDupServer()
				+ cIPaser.getPartialHitNotExpiredDup();
		totalBytes = cIPaser.getCacheMissBytes() + cIPaser.getNotCacheableBytes()
				+ cIPaser.getHitNotExpiredDupBytes() + cIPaser.getHitResponseChangedBytes()
				+ cIPaser.getHitExpiredDupClientBytes() + cIPaser.getHitExpiredDupServerBytes()
				+ cIPaser.getHitExpired304Bytes() + cIPaser.getPartialHitExpiredDupClientBytes()
				+ cIPaser.getPartialHitExpiredDupServerBytes()
				+ cIPaser.getPartialHitNotExpiredDupBytes();
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getCacheMiss(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getCacheMissBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotCacheable(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotCacheableBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitExpired304(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitExpired304Bytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitResponseChanged(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ format.format(pct(cIPaser.getHitResponseChangedBytes(), totalBytes)) + QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitNotExpiredDup(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitNotExpiredDupBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitExpiredDupClient(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ format.format(pct(cIPaser.getHitExpiredDupClientBytes(), totalBytes)) + QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getHitExpiredDupServer(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ format.format(pct(cIPaser.getHitExpiredDupServerBytes(), totalBytes)) + QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getPartialHitNotExpiredDup(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ format.format(pct(cIPaser.getPartialHitNotExpiredDupBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getPartialHitExpiredDupClient(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ format.format(pct(cIPaser.getPartialHitExpiredDupClientBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getPartialHitExpiredDupServer(), total))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ format.format(pct(cIPaser.getPartialHitExpiredDupServerBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		total = cIPaser.getExpired() + cIPaser.getExpiredHeur() + cIPaser.getNotExpired()
				+ cIPaser.getNotExpiredHeur();
		totalBytes = cIPaser.getExpiredBytes() + cIPaser.getExpiredHeurBytes()
				+ cIPaser.getNotExpiredBytes() + cIPaser.getNotExpiredHeurBytes();

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotExpired(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotExpiredBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotExpiredHeur(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getNotExpiredHeurBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getExpired(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getExpiredBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getExpiredHeur(), total)) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + format.format(pct(cIPaser.getExpiredHeurBytes(), totalBytes))
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);

		return writer;
	}

	/**
	 * This method calculates the percentage of x in y.
	 */
	private double pct(long x, long y) {
		return y != 0.0 ? (double) x / y * 100.0 : 0.0;
	}

	/**
	 * Adds Application end point summary data.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addApplicationEndPointSummary(FileWriter writer, Analysis analysis)
			throws IOException {
		ArrayList<ApplicationPacketSummary> appList = new ArrayList<ApplicationPacketSummary>(
				analysis.getApplicationPacketSummary());
		Collections.sort(appList, new AppPacketSummaryBytesComparator());
		String appName;
		for (int i = 0; i < 5; i++) {
			if (i < analysis.getApplicationPacketSummary().size()) {
				ApplicationPacketSummary appSummary = appList.get(i);
				if (appSummary != null) {
					appName = appSummary.getAppName();
					appName = com.att.aro.util.Util.getDefaultAppName(appName);
					writer.append(appName);
					writer.append(COMMA_SEP);
					writer.append(QUOTE_SEP + appSummary.getPacketCount() + QUOTE_SEP);
					writer.append(COMMA_SEP);
					writer.append(QUOTE_SEP + appSummary.getTotalBytes() + QUOTE_SEP);
					writer.append(COMMA_SEP);
				} else {
					writer.append(COMMA_SEP);
					writer.append(COMMA_SEP);
					writer.append(COMMA_SEP);
				}
			} else {
				writer.append(COMMA_SEP);
				writer.append(COMMA_SEP);
				writer.append(COMMA_SEP);
			}
		}
		return writer;
	}

	/**
	 * Compares {@link ApplicationPacketSummary} list object based on total
	 * bytes.
	 */
	private class AppPacketSummaryBytesComparator implements Comparator<ApplicationPacketSummary> {
		@Override
		public int compare(ApplicationPacketSummary arg0, ApplicationPacketSummary arg1) {
			return Long.valueOf(arg1.getTotalBytes()).compareTo(arg0.getTotalBytes());
		}
	}

	/**
	 * Adds Application score.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addApplicationScore(FileWriter writer, Analysis analysis) throws IOException {
		writer.append(QUOTE_SEP + analysis.getApplicationScore().getCausesScore() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + analysis.getApplicationScore().getEffectScore() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + analysis.getApplicationScore().getTotalApplicationScore()
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}

	/**
	 * Adds Connection Statics.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addConnectionStatics(FileWriter writer, Analysis analysis)
			throws IOException {
		writer.append(QUOTE_SEP
				+ (analysis != null ? analysis.calculateSessionTermPercentage(analysis) : 0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ (analysis != null ? analysis.calculateLargeBurstConnection(analysis) : 0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ (analysis != null ? analysis.calculateTightlyCoupledConnection(analysis) : 0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ (analysis != null ? 100 - analysis.calculateNonPeriodicConnection(analysis) : 0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ (analysis != null ? analysis.calculateNonPeriodicConnection(analysis) : 0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}

	/**
	 * Adds Common contents.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addCommonContents(FileWriter writer, Analysis analysis)
			throws IOException {

		String traceDirPath = analysis.getTraceData().getTraceDir().toString();
		String traceDirRelativeName = null;
		if (singleTrace) {
			traceDirRelativeName = traceDirPath.replace(
					this.traceDir.getParent(), "");
		} else {
			traceDirRelativeName = traceDirPath.replace(
					this.traceDir.toString(), "");
		}

		writer.append(QUOTE_SEP + traceDirRelativeName + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + analysis.getTraceData().getCollectorVersion()
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		StringBuffer apps = new StringBuffer();
		String appName;
		for (String app : analysis.getAppNames()) {
			String appVersion = analysis.getTraceData().getAppVersionMap()
					.get(app);
			appName = com.att.aro.util.Util.getDefaultAppName(app);
			apps.append(appName);
			apps.append(appVersion != null ? " : " + appVersion : "");
			apps.append(", ");
		}
		if (apps.length() >= 2) {
			apps.deleteCharAt(apps.length() - 2);
		}
		writer.append(QUOTE_SEP + apps.toString() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}

	/**
	 * Adds Trace Benchmarking contents.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addTraceBenchmarking(FileWriter writer, Analysis analysis)
			throws IOException {

		writer.append("" + (analysis != null ? analysis.getAvgKbps() : 0));
		writer.append(COMMA_SEP);
		writer.append(""
				+ (analysis != null ? analysis.calculateThroughputPercentage(analysis) : 0));
		writer.append(COMMA_SEP);

		writer.append(""
				+ (analysis != null ? analysis.getRrcStateMachine().getJoulesPerKilobyte() : 0));
		writer.append(COMMA_SEP);
		writer.append("" + (analysis != null ? analysis.calculateJpkbPercentage(analysis) : 0));
		writer.append(COMMA_SEP);

		writer.append(""
				+ (analysis != null ? analysis.getRrcStateMachine().getPromotionRatio() : 0));
		writer.append(COMMA_SEP);
		writer.append(""
				+ (analysis != null ? analysis.calculatePromotionRatioPercentage(analysis) : 0));
		writer.append(COMMA_SEP);

		return writer;
	}

	/**
	 * Adds file types.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addFileTypes(FileWriter writer, Analysis analysis) throws IOException {
		final List<FileTypeSummary> content = analysis.constructContent(analysis);
		for (int i = 0; i < 5; i++) {
			if (i < content.size()) {
				FileTypeSummary summary = content.get(i);
				if (summary != null) {
					writer.append(summary.getFileType());
					writer.append(COMMA_SEP);
					writer.append("" + summary.getPct());
					writer.append(COMMA_SEP);
					writer.append("" + summary.getBytes());
					writer.append(COMMA_SEP);
				} else {
					writer.append(COMMA_SEP);
					writer.append(COMMA_SEP);
					writer.append(COMMA_SEP);
				}
			} else {
				writer.append(COMMA_SEP);
				writer.append(COMMA_SEP);
				writer.append(COMMA_SEP);
			}
		}
		return writer;
	}

	private String bestPracticeExport(BestPracticeExport bpe) {
		String value = bpe.getValue();
		String units = bpe.getUnitsDescription();
		return ((value != null ? value : "" ) + " " + (units != null ? units : "")).trim();
	}
	
	/**
	 * Adds Best practices data.
	 * 
	 * @param writer
	 * @param analysis
	 * @return
	 * @throws IOException
	 */
	private FileWriter addBestPractices(FileWriter writer, Analysis analysis) throws IOException {
		final String bpPass = Util.RB.getString("bestPractices.pass");
		final String bpFail = Util.RB.getString("bestPractices.fail");
		final String commaSepWithSpace = COMMA_SEP + " ";
		
		for (BestPracticeDisplay bp : this.bestPractices) {
			writer.append(bp.isPass(analysis) ? bpPass : bpFail);
			writer.append(COMMA_SEP);
			
			List<BestPracticeExport> bpes = bp.getExportData(analysis);
			if (bpes != null && bpes.size() > 0) {
				Iterator<BestPracticeExport> i = bpes.iterator();
				BestPracticeExport bpe = i.next();
				StringBuffer s = new StringBuffer(bestPracticeExport(bpe));
				while (i.hasNext()) {
					bpe = i.next();
					s.append(commaSepWithSpace);
					s.append(bestPracticeExport(bpe));
				}
				writer.append(DataDumpHelper.createCSVEntry(s));
			}
			writer.append(COMMA_SEP);
		}

		return writer;
	}

	/**
	 * Adds burst analysis data for provided burst category.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @param category
	 * @throws IOException
	 */
	private void addBurstAnalysis(FileWriter writer, Analysis analysis, BurstCategory category) throws IOException {
		
		BurstAnalysisInfo burst = null;
		List<BurstAnalysisInfo> burstInfo = analysis.getBcAnalysis().getBurstAnalysisInfo();
		for (BurstAnalysisInfo b : burstInfo) {
			if (b.getCategory() == category) {
				LOGGER.log(Level.FINE, "Size of the Burst Analysis Info: {0}", burstInfo.size());
				LOGGER.log(Level.FINE, "Burst Analysis Category: {0}", category);
				burst = b;
				break;
			}
		}

		if (burst != null) {
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + burst.getPayload() + QUOTE_SEP);
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + burst.getPayloadPct() + QUOTE_SEP);
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + burst.getEnergy() + QUOTE_SEP);
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + burst.getEnergyPct() + QUOTE_SEP);
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + burst.getRRCActiveTime() + QUOTE_SEP);
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + burst.getRRCActivePercentage() + QUOTE_SEP);
			writer.append(COMMA_SEP);
			writer.append(QUOTE_SEP + (burst.getJpkb() != null ? burst.getJpkb() : 0.000) + QUOTE_SEP);
		} else {
			DataDumpHelper.addCommas(writer, DataDumpHelper.BURST_INFO_SIZE_ACTUAL);
		}
	}

	/**
	 * Adds 3G RRC state and energy simulation.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter add3GRRCStateAndEnergySimulation(FileWriter writer, Analysis analysis)
			throws IOException {
		RRCStateMachine rrc = analysis.getRrcStateMachine();
		// RRC simulation
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getIdleTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getIdleTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getDchTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getDchTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getIdleToDchTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getIdleToDchTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + rrc.getIdleToDchCount() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachToDchTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachToDchTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + rrc.getFachToDchCount() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getDchTailRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachTailRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getPromotionRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getTotalRRCEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		// Energy Simulation
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getIdleEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format((rrc.getIdleEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getDchEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format((rrc.getDchEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format((rrc.getFachEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getIdleToDchEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getIdleToDchEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachToDchEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getFachToDchEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getDchTailEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getDchTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getFachTailEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getFachTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getJoulesPerKilobyte()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}

	/**
	 * Adds LTE RRC state and energy simulation.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addLTERRCStateAndEnergySimulation(FileWriter writer, Analysis analysis)
			throws IOException {
		RRCStateMachine rrc = analysis.getRrcStateMachine();
		// RRC simulation
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteIdleTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteIdleTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteCrTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteCrTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteCrTailTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteCrTailTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxShortTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxShortTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxLongTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxLongTimeRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteIdleToCRPromotionTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteIdleToCRPromotionTimeRatio() * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getCRTailRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxLongRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxShortRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getCRPromotionRatio()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getTotalRRCEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		// Energy Simulation
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteIdleEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getLteIdleEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteIdleToCRPromotionEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getLteIdleToCRPromotionEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteCrEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getLteCrEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteCrTailEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getLteCrTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxShortEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getLteDrxShortEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getLteDrxLongEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getLteDrxLongEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getJoulesPerKilobyte()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}

	/**
	 * Adds WiFi RRC state and energy simulation.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addWiFiRRCStateAndEnergySimulation(FileWriter writer, Analysis analysis)
			throws IOException {
		RRCStateMachine rrc = analysis.getRrcStateMachine();
		// RRC simulation
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiActiveTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiActiveRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiTailTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiTailRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiIdleTime()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiIdleRatio() * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);

		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getTotalRRCEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		// Energy Simulation
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiActiveEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getWifiActiveEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiTailEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getWifiTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getWifiIdleEnergy()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP
				+ NUMBER_FORMAT.format((rrc.getWifiIdleEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		writer.append(QUOTE_SEP + NUMBER_FORMAT.format(rrc.getJoulesPerKilobyte()) + QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}

	/**
	 * Adds Basic Statistics data in writer.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addBasicStatistics(FileWriter writer, TraceData.Analysis analysis)
			throws IOException {
		// Size
		writer.append(QUOTE_SEP + analysis.getTotalBytes() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		// Duration
		writer.append(QUOTE_SEP + analysis.getPacketsDuration() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		// packets
		writer.append(QUOTE_SEP + analysis.getPackets().size() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		// average rate
		writer.append(QUOTE_SEP + analysis.getAvgKbps() + QUOTE_SEP);
		writer.append(COMMA_SEP);
		return writer;
	}
}
