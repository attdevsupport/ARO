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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.ProfileManager;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.ApplicationPacketSummary;
import com.att.aro.model.BestPractices;
import com.att.aro.model.BurstAnalysisInfo;
import com.att.aro.model.BurstCategory;
import com.att.aro.model.BurstCollectionAnalysis;
import com.att.aro.model.CacheInfoParser;
import com.att.aro.model.EnergyModel;
import com.att.aro.model.FileTypeSummary;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileException;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.ProfileType;
import com.att.aro.model.RRCStateMachine;
import com.att.aro.model.TraceData;
import com.att.aro.model.TraceData.Analysis;
import com.att.aro.model.UserPreferences;

/**
 * Manages export functionality of multiple traces.
 */
public class DataDump {

	private static final Logger logger = Logger.getLogger(ApplicationResourceOptimizer.class
			.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private File traceDir;
	private File fileToSave;

	private static final Window msgWindow = new Window(new Frame());

	private static final String lineSep = System.getProperty(rb
			.getString("statics.csvLine.seperator"));
	private static final String commaSep = rb.getString("statics.csvCell.seperator");
	private static final String quoteSep = "\"";

	private static final NumberFormat nf = new DecimalFormat("0.00");

	/**
	 * Initializes a new instance of {@link DataDump}.
	 * 
	 * @param dir
	 * @throws IOException
	 */
	public DataDump(File dir, String profileVal) throws IOException {
		this.traceDir = dir;
		final String profile = profileVal;
		// Initialize application window
		AROUIManager.init();
		
		if(!this.traceDir.exists()) {
			MessageDialogFactory.showErrorDialog(msgWindow,
					rb.getString("datadump.invalidfolder"));
			return;
		}

		final List<File> traceFolders = Arrays.asList(traceDir.listFiles(new java.io.FileFilter() {
			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory();
			}
		}));

		if (traceFolders.size() == 0) {
			MessageDialogFactory.showErrorDialog(msgWindow,
					rb.getString("Error.dataDump.valideFolder"));
			return;
		} else {

			if (!showSaveChooser()) {
				return;
			}

			new SwingWorker<File, Object>() {
				@Override
				protected File doInBackground() throws IOException {
					startDataDump(traceFolders, profile);
					return fileToSave;
				}

				@Override
				protected void done() {
					try {
						if (get().getName().contains(".csv")) {
							if (MessageDialogFactory.showExportConfirmDialog(msgWindow) == JOptionPane.YES_OPTION) {
								Desktop desktop = Desktop.getDesktop();
								desktop.open(get());
							}
						}
						this.cancel(true);
					} catch (IOException e) {
						logger.log(Level.SEVERE, "Unexpected IOException analyzing trace", e);
						MessageDialogFactory.showUnexpectedExceptionDialog(msgWindow, e);
					} catch (UnsupportedOperationException unsupportedException) {
						MessageDialogFactory.showMessageDialog(msgWindow,
								rb.getString("Error.unableToOpen"));
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE, "Unexpected exception analyzing trace", e);
						MessageDialogFactory.showUnexpectedExceptionDialog(msgWindow, e);
					} catch (ExecutionException e) {
						logger.log(Level.SEVERE, "Unexpected execution exception analyzing trace",
								e);
						if (e.getCause() instanceof OutOfMemoryError) {
							MessageDialogFactory.showErrorDialog(null,
									rb.getString("Error.outOfMemory"));
						} else {
							MessageDialogFactory.showUnexpectedExceptionDialog(msgWindow, e);
						}
					}
				}
			}.execute();
		}
	}

	/**
	 * Initiates data dump from command line.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO need to verify the folder names with spaces
		if (args.length == 0) {
			MessageDialogFactory.showErrorDialog(msgWindow,
					rb.getString("datadump.Error.argumentfaliure"));
		}
		final File dataDumpDir = new File(args[0]);
		try {
			new DataDump(dataDumpDir, args[1]);
		} catch (IOException e) {
			logger.info("" + e);
		}
	}

	/**
	 * Initiates the save chooser options and creates a file.
	 */
	private boolean showSaveChooser() {
		JFileChooser chooser = new JFileChooser(UserPreferences.getInstance()
				.getLastExportDirectory());
		chooser.addChoosableFileFilter(new FileNameExtensionFilter(rb
				.getString("fileChooser.desc.csv"), rb.getString("fileChooser.contentType.csv")));
		chooser.setDialogTitle(rb.getString("fileChooser.Title"));
		chooser.setApproveButtonText(rb.getString("fileChooser.Save"));
		chooser.setMultiSelectionEnabled(false);

		if (chooser.showSaveDialog(msgWindow) != JFileChooser.APPROVE_OPTION) {
			return false;
		}

		fileToSave = chooser.getSelectedFile();
		if (fileToSave.getName().length() >= 50) {
			MessageDialogFactory.showErrorDialog(msgWindow,
					rb.getString("exportall.errorLongFileName"));
			return false;
		}
		if (!chooser.getFileFilter().accept(fileToSave)) {
			fileToSave = new File(fileToSave.getAbsolutePath() + "."
					+ rb.getString("fileChooser.contentType.csv"));
		}
		if (fileToSave.exists()) {
			if (MessageDialogFactory.showConfirmDialog(
					msgWindow,
					MessageFormat.format(rb.getString("fileChooser.fileExists"),
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
	private FileWriter startDataDump(List<File> traceFolders, String profile) throws IOException {

		Profile currentProfile = ProfileManager.getInstance().getDefaultProfile();
		FileWriter writer = new FileWriter(fileToSave);
		try {
			if (profile.contains("3G")) {
				currentProfile = ProfileManager.getInstance().getLastUserProfile(ProfileType.T3G);
			} else if (profile.contains("LTE")) {
				currentProfile = ProfileManager.getInstance().getLastUserProfile(ProfileType.LTE);
			} 

			writer.append(rb.getString("menu.profile") + ":" + currentProfile.getName());
			writer.append(lineSep);

			if (currentProfile instanceof Profile3G) {
				writer = add3GHeader(writer);
			} else if (currentProfile instanceof ProfileLTE) {
				writer = addLTEHeader(writer);
			}

			writer.append(lineSep);

			for (File traceDir : traceFolders) {
				final List<File> traceFiles = Arrays.asList(traceDir.listFiles());
				if (!checkTraceDir(traceFiles)) {
					continue;
				}

				final TraceData traceData = new TraceData(traceDir);
				TraceData.Analysis analysis = traceData.runAnalysis(currentProfile, null);

				writer = addContent(writer, analysis);

			}
		} catch (ProfileException proEx) {
			// TODO Display profile exception
		} finally {
			writer.close();
		}
		return writer;
	}

	/**
	 * Validates individual trace folder.
	 * 
	 * @param traceFiles
	 *            - List of trace folder names.
	 * @return true if valid trace else false.
	 */
	private boolean checkTraceDir(List<File> traceFiles) {
		if (!(traceFiles.size() > 0)) {
			return false;
		}
		boolean isTraffic = false;
		for (File traceFile : traceFiles) {
			if (traceFile.getName().equalsIgnoreCase(rb.getString("datadump.trafficFile"))) {
				isTraffic = true;
			}
		}
		return isTraffic;
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
	private FileWriter addContent(FileWriter writer, TraceData.Analysis analysis)
			throws IOException {

		writer = addCommonContents(writer, analysis);

		// Best practices data
		writer = addBestPractices(writer, analysis);

		// Basic Statistics data
		writer = addBasicStatistics(writer, analysis);

		// RRC Energy and RRC State simulation
		if (analysis.getProfile() instanceof Profile3G) {
			writer = add3GRRCStateAndEnergySimulation(writer, analysis);
		} else if (analysis.getProfile() instanceof ProfileLTE) {
			writer = addLTERRCStateAndEnergySimulation(writer, analysis);
		}

		// Cache Content score
		writer = addPeripheralEnergy(writer, analysis);

		// Burst analysis
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_USER);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_CLIENT);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_PROTOCOL);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_LOSS);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_SERVER);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_LONG);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_PERIODICAL);
		writer = addBurstAnalysis(writer, analysis, BurstCategory.BURSTCAT_BKG);

		// Burst analysis
		writer = addFileTypes(writer, analysis);

		// Trace Benchmarking
		writer = addTraceBenchmarking(writer, analysis);

		// Trace Benchmarking
		writer = addConnectionStatics(writer, analysis);

		// Application score
		writer = addApplicationScore(writer, analysis);

		// Application score
		writer = addApplicationEndPointSummary(writer, analysis);

		// Cache Content score
		writer = addCacheContent(writer, analysis);

		writer.append(lineSep);
		return writer;
	}

	private FileWriter addPeripheralEnergy(FileWriter writer, Analysis analysis) throws IOException {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		EnergyModel model = analysis.getEnergyModel();

		writer.append(quoteSep + nf.format(model.getGpsActiveEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getGpsStandbyEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getTotalGpsEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getTotalCameraEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getBluetoothActiveEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getBluetoothStandbyEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getTotalBluetoothEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(model.getTotalScreenEnergy()) + quoteSep);
		writer.append(commaSep);
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
		writer.append(quoteSep + format.format(pct(cIPaser.getCacheable(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getCacheableBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getNotCacheable(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getNotCacheableBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

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
		writer.append(quoteSep + format.format(pct(cIPaser.getCacheMiss(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getCacheMissBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getNotCacheable(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getNotCacheableBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getHitExpired304(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getHitExpired304Bytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getHitResponseChanged(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ format.format(pct(cIPaser.getHitResponseChangedBytes(), totalBytes)) + quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getHitNotExpiredDup(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getHitNotExpiredDupBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getHitExpiredDupClient(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ format.format(pct(cIPaser.getHitExpiredDupClientBytes(), totalBytes)) + quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getHitExpiredDupServer(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ format.format(pct(cIPaser.getHitExpiredDupServerBytes(), totalBytes)) + quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getPartialHitNotExpiredDup(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ format.format(pct(cIPaser.getPartialHitNotExpiredDupBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getPartialHitExpiredDupClient(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ format.format(pct(cIPaser.getPartialHitExpiredDupClientBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getPartialHitExpiredDupServer(), total))
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ format.format(pct(cIPaser.getPartialHitExpiredDupServerBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		total = cIPaser.getExpired() + cIPaser.getExpiredHeur() + cIPaser.getNotExpired()
				+ cIPaser.getNotExpiredHeur();
		totalBytes = cIPaser.getExpiredBytes() + cIPaser.getExpiredHeurBytes()
				+ cIPaser.getNotExpiredBytes() + cIPaser.getNotExpiredHeurBytes();

		writer.append(quoteSep + format.format(pct(cIPaser.getNotExpired(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getNotExpiredBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getNotExpiredHeur(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getNotExpiredHeurBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getExpired(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getExpiredBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

		writer.append(quoteSep + format.format(pct(cIPaser.getExpiredHeur(), total)) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + format.format(pct(cIPaser.getExpiredHeurBytes(), totalBytes))
				+ quoteSep);
		writer.append(commaSep);

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
		for (int i = 0; i < 5; i++) {
			if (i < analysis.getApplicationPacketSummary().size()) {
				ApplicationPacketSummary appSummary = appList.get(i);
				if (appSummary != null) {
					writer.append(appSummary.getAppName() != null ? appSummary.getAppName() : rb
							.getString("aro.unknownApp"));
					writer.append(commaSep);
					writer.append(quoteSep + appSummary.getPacketCount() + quoteSep);
					writer.append(commaSep);
					writer.append(quoteSep + appSummary.getTotalBytes() + quoteSep);
					writer.append(commaSep);
				} else {
					writer.append(commaSep);
					writer.append(commaSep);
					writer.append(commaSep);
				}
			} else {
				writer.append(commaSep);
				writer.append(commaSep);
				writer.append(commaSep);
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
		writer.append(quoteSep + analysis.getApplicationScore().getCausesScore() + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + analysis.getApplicationScore().getEffectScore() + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + analysis.getApplicationScore().getTotalApplicationScore()
				+ quoteSep);
		writer.append(commaSep);
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
		writer.append(quoteSep
				+ (analysis != null ? analysis.calculateSessionTermPercentage(analysis) : 0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ (analysis != null ? analysis.calculateLargeBurstConnection(analysis) : 0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ (analysis != null ? analysis.calculateTightlyCoupledConnection(analysis) : 0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ (analysis != null ? 100 - analysis.calculateNonPeriodicConnection(analysis) : 0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ (analysis != null ? analysis.calculateNonPeriodicConnection(analysis) : 0)
				+ quoteSep);
		writer.append(commaSep);
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
	private FileWriter addCommonContents(FileWriter writer, Analysis analysis) throws IOException {
		writer.append(quoteSep + analysis.getTraceData().getTraceDir().getName() + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + analysis.getTraceData().getCollectorVersion() + quoteSep);
		writer.append(commaSep);
		StringBuffer apps = new StringBuffer();
		for (String app : analysis.getAppNames()) {
			String appVersion = analysis.getTraceData().getAppVersionMap().get(app);
			apps.append((app != null ? app : rb.getString("aro.unknownApp"))
					+ (appVersion != null ? " : " + appVersion : "") + ", ");
		}
		if (apps.length() >= 2) {
			apps.deleteCharAt(apps.length() - 2);
		}
		writer.append(quoteSep + apps.toString() + quoteSep);
		writer.append(commaSep);
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
		writer.append(commaSep);
		writer.append(""
				+ (analysis != null ? analysis.calculateThroughputPercentage(analysis) : 0));
		writer.append(commaSep);

		writer.append(""
				+ (analysis != null ? analysis.getRrcStateMachine().getJoulesPerKilobyte() : 0));
		writer.append(commaSep);
		writer.append("" + (analysis != null ? analysis.calculateJpkbPercentage(analysis) : 0));
		writer.append(commaSep);

		writer.append(""
				+ (analysis != null ? analysis.getRrcStateMachine().getPromotionRatio() : 0));
		writer.append(commaSep);
		writer.append(""
				+ (analysis != null ? analysis.calculatePromotionRatioPercentage(analysis) : 0));
		writer.append(commaSep);

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
					writer.append(commaSep);
					writer.append("" + summary.getPct());
					writer.append(commaSep);
					writer.append("" + summary.getBytes());
					writer.append(commaSep);
				} else {
					writer.append(commaSep);
					writer.append(commaSep);
					writer.append(commaSep);
				}
			} else {
				writer.append(commaSep);
				writer.append(commaSep);
				writer.append(commaSep);
			}
		}
		return writer;
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
		final BestPractices bestPractices = analysis.getBestPractice();
		BurstCollectionAnalysis burst = analysis.getBcAnalysis();
		final String commaSepWithSpace = commaSep + " ";
		final int DUPLICATE_CONTENT_DENOMINATOR = 1048576;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		NumberFormat nf2 = NumberFormat.getInstance();
		nf2.setMaximumFractionDigits(3);

		writer.append(bestPractices.getDuplicateContent() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(quoteSep
				+ String.valueOf(nf.format(bestPractices.getDuplicateContentBytesRatio() * 100.0)
						+ " " + rb.getString("exportall.csvPct"))
				+ commaSepWithSpace
				+ String.valueOf(bestPractices.getDuplicateContentsize())
				+ " "
				+ rb.getString("exportall.csvFiles")
				+ commaSepWithSpace
				+ String.valueOf(nf2.format(((double) bestPractices.getDuplicateContentBytes())
						/ DUPLICATE_CONTENT_DENOMINATOR)
						+ " " + rb.getString("statics.csvUnits.mbytes")) + quoteSep);
		writer.append(commaSep);

		writer.append(bestPractices.isUsingCache() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(NumberFormat.getIntegerInstance().format(
				bestPractices.getCacheHeaderRatio())
				+ rb.getString("exportall.csvCacheConPct")));
		writer.append(commaSep);

		writer.append(bestPractices.isCacheControl() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(quoteSep + String.valueOf(bestPractices.getHitNotExpiredDupCount()) + " "
				+ rb.getString("datadump.csvCacheConNExpDesc") + commaSepWithSpace
				+ String.valueOf(bestPractices.getHitExpired304Count()) + " "
				+ rb.getString("exportall.csvCacheCon304Desc") + quoteSep);
		writer.append(commaSep);

		writer.append(bestPractices.getPrefetching() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(bestPractices.getUserInputBurstCount()) + " "
				+ rb.getString("exportall.csvPrefetchDesc"));
		writer.append(commaSep);

		writer.append(rb.getString("bestPractices.selfTest"));
		writer.append(commaSep);
		writer.append(commaSep);

		writer.append(bestPractices.getMultipleTcpCon() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(burst.getTightlyCoupledBurstCount()) + " "
				+ rb.getString("exportall.csvMultiConnDesc"));
		writer.append(commaSep);

		writer.append(bestPractices.getPeriodicTransfer() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(quoteSep + String.valueOf(burst.getDiffPeriodicCount()) + " "
				+ rb.getString("exportall.csvIneffConnDesc") + commaSepWithSpace
				+ String.valueOf(burst.getPeriodicCount()) + " "
				+ rb.getString("exportall.csvIneffConnRptDesc") + commaSepWithSpace
				+ String.valueOf(burst.getMinimumPeriodicRepeatTime()) + " "
				+ rb.getString("exportall.csvIneffConnTimeDesc") + quoteSep);
		writer.append(commaSep);

		writer.append(bestPractices.getScreenRotationProblem() ? rb.getString("bestPractices.pass")
				: rb.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(analysis.getTraceData().getScreenRotationCounter())
				+ " "
				+ (bestPractices.getScreenRotationProblem() ? rb
						.getString("exportall.csvSrcnRtnDescPass") : rb
						.getString("exportall.csvSrcnRtnDesc")));
		writer.append(commaSep);

		writer.append(bestPractices.getConnectionClosingProblem() ? rb
				.getString("bestPractices.pass") : rb.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(nf.format(analysis.getBestPractice().getTcpControlEnergy())
				+ " " + rb.getString("exportall.csvConnClosingDesc")));
		writer.append(commaSep);

		writer.append(bestPractices.getOffloadingToWiFi() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(burst.getLongBurstCount()) + " "
				+ rb.getString("exportall.csvOffWiFiDesc"));
		writer.append(commaSep);

		writer.append(bestPractices.getAccessingPeripherals() ? rb.getString("bestPractices.pass")
				: rb.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(quoteSep
				+ String.valueOf(nf.format(bestPractices.getGPSActiveStateRatio()) + " "
						+ rb.getString("exportall.csvAccGPSDesc"))
				+ commaSepWithSpace
				+ String.valueOf(nf.format(bestPractices.getBluetoothActiveStateRatio()) + " "
						+ rb.getString("exportall.csvAccBTDesc"))
				+ commaSepWithSpace
				+ String.valueOf(nf.format(bestPractices.getCameraActiveStateRatio()) + " "
						+ rb.getString("exportall.csvAccCamDesc")) + quoteSep);
		writer.append(commaSep);

		writer.append(bestPractices.getHttp10Usage() ? rb.getString("bestPractices.pass") : rb
				.getString("bestPractices.fail"));
		writer.append(commaSep);
		writer.append(String.valueOf(bestPractices.getHttp1_0HeaderCount()) + " "
				+ rb.getString("exportall.csvHTTPhdrDesc"));
		writer.append(commaSep);

		return writer;
	}

	/**
	 * Adds burst analysis data for provided burst category.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @param analysis
	 *            - A TraceData.Analysis object.
	 * @param bCategory
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addBurstAnalysis(FileWriter writer, Analysis analysis,
			BurstCategory bCategory) throws IOException {
		BurstAnalysisInfo burst = null;
		for (BurstAnalysisInfo expectedBurst : analysis.getBcAnalysis().getBurstAnalysisInfo()) {
			if (expectedBurst.getCategory() == bCategory) {
				burst = expectedBurst;
			}
		}

		if (burst != null) {
			writer.append(quoteSep + burst.getPayload() + quoteSep);
			writer.append(commaSep);
			writer.append(quoteSep + burst.getPayloadPct() + quoteSep);
			writer.append(commaSep);
			writer.append(quoteSep + burst.getEnergy() + quoteSep);
			writer.append(commaSep);
			writer.append(quoteSep + burst.getEnergyPct() + quoteSep);
			writer.append(commaSep);
			writer.append(quoteSep + burst.getRRCActiveTime() + quoteSep);
			writer.append(commaSep);
			writer.append(quoteSep + burst.getRRCActivePercentage() + quoteSep);
			writer.append(commaSep);
			writer.append(quoteSep + (burst.getJpkb() != null ? burst.getJpkb() : 0.000) + quoteSep);
			writer.append(commaSep);
		} else {
			writer.append(commaSep);
			writer.append(commaSep);
			writer.append(commaSep);
			writer.append(commaSep);
			writer.append(commaSep);
			writer.append(commaSep);
			writer.append(commaSep);
		}

		return writer;
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
		writer.append(quoteSep + nf.format(rrc.getIdleTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getIdleTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getDchTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getDchTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getIdleToDchTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getIdleToDchTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + rrc.getIdleToDchCount() + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachToDchTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachToDchTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + rrc.getFachToDchCount() + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getDchTailRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachTailRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getPromotionRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getTotalRRCEnergy()) + quoteSep);
		writer.append(commaSep);
		// Energy Simulation
		writer.append(quoteSep + nf.format(rrc.getIdleEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format((rrc.getIdleEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getDchEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format((rrc.getDchEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format((rrc.getFachEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getIdleToDchEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getIdleToDchEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachToDchEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getFachToDchEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getDchTailEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getDchTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getFachTailEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getFachTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getJoulesPerKilobyte()) + quoteSep);
		writer.append(commaSep);
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
		writer.append(quoteSep + nf.format(rrc.getLteIdleTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteIdleTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteCrTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteCrTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteCrTailTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteCrTailTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxShortTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxShortTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxLongTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxLongTimeRatio() * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteIdleToCRPromotionTime()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteIdleToCRPromotionTimeRatio() * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getCRTailRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxLongRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxShortRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getCRPromotionRatio()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getTotalRRCEnergy()) + quoteSep);
		writer.append(commaSep);
		// Energy Simulation
		writer.append(quoteSep + nf.format(rrc.getLteIdleEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getLteIdleEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteIdleToCRPromotionEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getLteIdleToCRPromotionEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteCrEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getLteCrEnergy() / rrc.getTotalRRCEnergy()) * 100.0) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteCrTailEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getLteCrTailEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxShortEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getLteDrxShortEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getLteDrxLongEnergy()) + quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep
				+ nf.format((rrc.getLteDrxLongEnergy() / rrc.getTotalRRCEnergy()) * 100.0)
				+ quoteSep);
		writer.append(commaSep);
		writer.append(quoteSep + nf.format(rrc.getJoulesPerKilobyte()) + quoteSep);
		writer.append(commaSep);
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
		writer.append(quoteSep + analysis.getTotalBytes() + quoteSep);
		writer.append(commaSep);
		// Duration
		writer.append(quoteSep + analysis.getPacketsDuration() + quoteSep);
		writer.append(commaSep);
		// packets
		writer.append(quoteSep + analysis.getPackets().size() + quoteSep);
		writer.append(commaSep);
		// average rate
		writer.append(quoteSep + analysis.getAvgKbps() + quoteSep);
		writer.append(commaSep);
		return writer;
	}

	/**
	 * Adds 3G headers in to CSV file.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter add3GHeader(FileWriter writer) throws IOException {

		final String basicStat = rb.getString("datadump.basicstat");
		final String rrcStatMachSim = rb.getString("datadump.rrcstatmcsimulation");
		final String rrcIdle = rb.getString("datadump.idle");
		final String rrcCellDCH = rb.getString("datadump.celldch");
		final String rrcCellFACH = rb.getString("datadump.cellfach");
		final String rrcIdleDCH = rb.getString("datadump.idledch");
		final String rrcFACHDCH = rb.getString("datadump.fachdch");
		final String rrcSec = rb.getString("datadump.seconds");
		final String rrcPCT = rb.getString("datadump.pct");
		final String energySimulation = rb.getString("datadump.energysimulation");
		final String energyJ = rb.getString("statics.csvUnits.j");
		final String burstAnalysis = rb.getString("burstAnalysis.title");
		final String userInput = rb.getString("chart.userInput");
		final String app = rb.getString("burst.type.App");
		final String tcpControl = rb.getString("datadump.tcpcontrol");
		final String tcpLossRec = rb.getString("datadump.tcplossrec");
		final String srvrNetDelay = rb.getString("burst.type.SvrNetDelay");
		final String largeBurst = rb.getString("datadump.largeburst");
		final String periodical = rb.getString("burst.type.Periodical");
		final String nonTarget = rb.getString("burst.type.NonTarget");
		final String bestPractices = rb.getString("datadump.bpractices");
		final String bpPassFail = rb.getString("datadump.bppassfail");
		final String bpAssoData = rb.getString("datadump.bpassodata");
		final String filetype = rb.getString("datadump.filetype");
		final String value = rb.getString("overview.traceoverview.value");
		final String percentile = rb.getString("overview.traceoverview.percentile");
		final String conStats = rb.getString("overview.sessionoverview.title");
		final String appScore = rb.getString("appscore.title");
		final String endpntSumm = rb.getString("endpointsummary.title");
		final String burstbytes = rb.getString("burstAnalysis.bytes");
		final String burstbytpct = rb.getString("burstAnalysis.bytesPct");
		final String burstEner = rb.getString("burstAnalysis.energy");
		final String burstEngpct = rb.getString("burstAnalysis.energyPct");
		final String dch = rb.getString("datadump.dch");
		final String pctdch = rb.getString("datadump.pctdch");
		final String burstjpkb = rb.getString("burstAnalysis.jpkb");
		final String packettype = rb.getString("packet.type");
		final String pct = rb.getString("simple.percent");
		final String endpntapp = rb.getString("endpointsummary.appname");
		final String endpntpkt = rb.getString("endpointsummary.packets");
		final String endpntbyte = rb.getString("endpointsummary.bytes");

		for (int i = 0; i <= 1; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, bestPractices, 23);

		for (int i = 0; i <= 3; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, rrcStatMachSim, 14);
		writer = addContinuousHeader(writer, energySimulation, 15);

		for (int i = 0; i <= 7; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, burstAnalysis, 55);

		for (int i = 0; i <= 14; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, rb.getString("overview.traceoverview.title"), 5);

		for (int i = 0; i <= 22; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, rb.getString("datadump.cachenoncache"), 3);
		writer = addContinuousHeader(writer, rb.getString("datadump.cacheCacheSim"), 19);
		writer = addContinuousHeader(writer, rb.getString("datadump.dupFile"), 7);

		writer.append(lineSep);

		for (int i = 0; i <= 1; i++) {
			writer.append(commaSep);
		}

		writer = addContinuousHeader(writer,
				rb.getString("caching.duplicateContent.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("caching.usingCache.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("caching.cacheControl.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("caching.prefetching.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.connectionOpening.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.unnecssaryConn.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("connections.periodic.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.screenRotation.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("connections.connClosing.detailedTitle"),
				1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.offloadingToWifi.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("other.accessingPeripherals.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("other.httpUsage.detailedTitle"), 1);

		writer = addContinuousHeader(writer, basicStat, 3);

		writer.append(commaSep);
		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcCellDCH);
		writer.append(commaSep);
		writer.append(rrcCellDCH);
		writer.append(commaSep);
		writer.append(rrcCellFACH);
		writer.append(commaSep);
		writer.append(rrcCellFACH);
		writer.append(commaSep);
		writer.append(rrcIdleDCH);
		writer.append(commaSep);
		writer.append(rrcIdleDCH);
		writer.append(commaSep);
		writer.append(rrcIdleDCH);
		writer.append(commaSep);
		writer.append(rrcFACHDCH);
		writer.append(commaSep);
		writer.append(rrcFACHDCH);
		writer.append(commaSep);
		writer.append(rrcFACHDCH);

		for (int i = 0; i <= 4; i++) {
			writer.append(commaSep);
		}

		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rb.getString("datadump.dch"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.dch"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.fach"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.fach"));
		writer.append(commaSep);
		writer.append(rrcIdleDCH);
		writer.append(commaSep);
		writer.append(rrcIdleDCH);
		writer.append(commaSep);
		writer.append(rrcFACHDCH);
		writer.append(commaSep);
		writer.append(rrcFACHDCH);
		writer.append(commaSep);
		writer.append(rb.getString("RRCState.TAIL_DCH"));
		writer.append(commaSep);
		writer.append(rb.getString("RRCState.TAIL_DCH"));
		writer.append(commaSep);
		writer.append(rb.getString("RRCState.TAIL_FACH"));
		writer.append(commaSep);
		writer.append(rb.getString("RRCState.TAIL_FACH"));
		writer.append(commaSep);

		writer = addContinuousHeader(writer, rb.getString("datadump.energytitle"), 7);
		writer = addContinuousHeader(writer, userInput, 6);
		writer = addContinuousHeader(writer, app, 6);
		writer = addContinuousHeader(writer, tcpControl, 6);
		writer = addContinuousHeader(writer, tcpLossRec, 6);
		writer = addContinuousHeader(writer, srvrNetDelay, 6);
		writer = addContinuousHeader(writer, largeBurst, 6);
		writer = addContinuousHeader(writer, periodical, 6);
		writer = addContinuousHeader(writer, nonTarget, 6);

		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 3; j++) {
				writer.append(commaSep);
				writer.append(filetype + i);
			}
		}

		writer = addContinuousHeader(writer, rb.getString("Export.traceoverview.throughput"), 1);
		writer = addContinuousHeader(writer, rb.getString("Export.traceoverview.jpkb"), 1);
		writer = addContinuousHeader(writer, rb.getString("Export.traceoverview.promoratio"), 1);

		for (int j = 0; j <= 4; j++) {
			writer.append(commaSep);
			writer.append(conStats);
		}

		for (int j = 0; j <= 2; j++) {
			writer.append(commaSep);
			writer.append(appScore);
		}

		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 3; j++) {
				writer.append(commaSep);
				writer.append(endpntSumm + " " + i);
			}
		}

		writer = addContinuousHeader(writer, rb.getString("cache.cacheable"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.nonCachable"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheMiss"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.notCacheable"), 1);
		writer = addContinuousHeader(
				writer,
				rb.getString("cache.cacheHitExpiredDup304").replace(
						rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(
				writer,
				rb.getString("cache.cacheHitRespChanged").replace(
						rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheHitNotExpiredDup"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheHitExpiredClientDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheHitExpiredServerDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.partialHitNotExpiredDup"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.partialHitExpiredClientDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.partialHitExpiredServerDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.notExpired"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.notExpiredHeur"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.expired"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.expiredHeur"), 1);

		writer.append(lineSep);

		writer.append(rb.getString("datadump.tracename"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.appVersion"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.appnamever"));

		for (int i = 0; i <= 11; i++) {
			writer.append(commaSep);
			writer.append(bpPassFail);
			writer.append(commaSep);
			writer.append(bpAssoData);
		}

		writer.append(commaSep);
		writer.append(rb.getString("datadump.sizeinbyte"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.durationinsec"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.packets"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.avgrate"));

		for (int i = 0; i <= 3; i++) {
			writer.append(commaSep);
			writer.append(rrcSec);
			writer.append(commaSep);
			writer.append(rrcPCT);
		}

		writer.append(commaSep);
		writer.append(rb.getString("datadump.hash"));
		writer.append(commaSep);
		writer.append(rrcSec);
		writer.append(commaSep);
		writer.append(rrcPCT);
		writer.append(commaSep);
		writer.append(rb.getString("datadump.hash"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.dchtail"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.fachtail"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.promoratio"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.totalE"));

		for (int i = 0; i <= 6; i++) {
			writer.append(commaSep);
			writer.append(energyJ);
			writer.append(commaSep);
			writer.append(rrcPCT);
		}

		writer.append(commaSep);
		writer.append(rb.getString("burstAnalysis.jpkb"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.gpsActive"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.gpsStandby"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.gpsTotal"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.cameraTotal"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.bluetoothActive"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.bluetoothStandby"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.bluetoothTotal"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.screenTotal"));

		for (int i = 0; i <= 7; i++) {
			writer.append(commaSep);
			writer.append(burstbytes);
			writer.append(commaSep);
			writer.append(burstbytpct);
			writer.append(commaSep);
			writer.append(burstEner);
			writer.append(commaSep);
			writer.append(burstEngpct);
			writer.append(commaSep);
			writer.append(dch);
			writer.append(commaSep);
			writer.append(pctdch);
			writer.append(commaSep);
			writer.append(burstjpkb);
		}

		for (int i = 0; i <= 4; i++) {
			writer.append(commaSep);
			writer.append(packettype);
			writer.append(commaSep);
			writer.append(pct);
			writer.append(commaSep);
			writer.append(burstbytes);
		}

		for (int i = 0; i <= 2; i++) {
			writer.append(commaSep);
			writer.append(value);
			writer.append(commaSep);
			writer.append(percentile);
		}

		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.sessionTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.longBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.tightlyGroupedBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.periodicBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.nonPeriodicBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("appscore.subtitle.causes"));
		writer.append(commaSep);
		writer.append(rb.getString("appscore.subtitle.effects"));
		writer.append(commaSep);
		writer.append(rb.getString("appscore.subtitle.total"));

		for (int i = 0; i <= 4; i++) {
			writer.append(commaSep);
			writer.append(endpntapp);
			writer.append(commaSep);
			writer.append(endpntpkt);
			writer.append(commaSep);
			writer.append(endpntbyte);
		}

		for (int j = 0; j <= 15; j++) {
			writer.append(commaSep);
			writer.append(rb.getString("statics.csvFormat.response"));
			writer.append(commaSep);
			writer.append(rb.getString("statics.csvFormat.bytes"));
		}

		return writer;
	}

	/**
	 * Adds LTE headers in to CSV file.
	 * 
	 * @param writer
	 *            - A FileWriter object.
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addLTEHeader(FileWriter writer) throws IOException {

		final String basicStat = rb.getString("datadump.basicstat");
		final String rrcStatMachSim = rb.getString("datadump.rrcstatmcsimulation");
		final String rrcContRecep = rb.getString("rrc.continuousReception");
		final String rrcContReceptail = rb.getString("rrc.continuousReceptionTail");
		final String rrcSDRX = rb.getString("rrc.shortDRX");
		final String rrcLDRX = rb.getString("rrc.longDRX");
		final String rrcIdleToCont = rb.getString("rrc.continuousReceptionIdle");
		final String rrcIdle = rb.getString("datadump.idle");
		final String rrcSec = rb.getString("datadump.seconds");
		final String rrcPCT = rb.getString("datadump.pct");
		final String energySimulation = rb.getString("datadump.energysimulation");
		final String energyJ = rb.getString("statics.csvUnits.j");
		final String burstAnalysis = rb.getString("burstAnalysis.title");
		final String userInput = rb.getString("chart.userInput");
		final String app = rb.getString("burst.type.App");
		final String tcpControl = rb.getString("datadump.tcpcontrol");
		final String tcpLossRec = rb.getString("datadump.tcplossrec");
		final String srvrNetDelay = rb.getString("burst.type.SvrNetDelay");
		final String largeBurst = rb.getString("datadump.largeburst");
		final String periodical = rb.getString("burst.type.Periodical");
		final String nonTarget = rb.getString("burst.type.NonTarget");
		final String bestPractices = rb.getString("datadump.bpractices");
		final String bpPassFail = rb.getString("datadump.bppassfail");
		final String bpAssoData = rb.getString("datadump.bpassodata");
		final String filetype = rb.getString("datadump.filetype");
		final String value = rb.getString("overview.traceoverview.value");
		final String percentile = rb.getString("overview.traceoverview.percentile");
		final String conStats = rb.getString("overview.sessionoverview.title");
		final String appScore = rb.getString("appscore.title");
		final String endpntSumm = rb.getString("endpointsummary.title");
		final String burstbytes = rb.getString("burstAnalysis.bytes");
		final String burstbytpct = rb.getString("burstAnalysis.bytesPct");
		final String burstEner = rb.getString("burstAnalysis.energy");
		final String burstEngpct = rb.getString("burstAnalysis.energyPct");
		final String burstjpkb = rb.getString("burstAnalysis.jpkb");
		final String packettype = rb.getString("packet.type");
		final String pct = rb.getString("simple.percent");
		final String endpntapp = rb.getString("endpointsummary.appname");
		final String endpntpkt = rb.getString("endpointsummary.packets");
		final String endpntbyte = rb.getString("endpointsummary.bytes");
		final String ltecr = rb.getString("burstAnalysis.lteCr");
		final String ltecrpct = rb.getString("burstAnalysis.lteCrPct");

		for (int i = 0; i <= 1; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, bestPractices, 23);

		for (int i = 0; i <= 3; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, rrcStatMachSim, 15);
		writer = addContinuousHeader(writer, energySimulation, 13);

		for (int i = 0; i <= 7; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, burstAnalysis, 55);

		for (int i = 0; i <= 14; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, rb.getString("overview.traceoverview.title"), 5);

		for (int i = 0; i <= 22; i++) {
			writer.append(commaSep);
		}
		writer = addContinuousHeader(writer, rb.getString("datadump.cachenoncache"), 3);
		writer = addContinuousHeader(writer, rb.getString("datadump.cacheCacheSim"), 19);
		writer = addContinuousHeader(writer, rb.getString("datadump.dupFile"), 7);

		writer.append(lineSep);

		for (int i = 0; i <= 1; i++) {
			writer.append(commaSep);
		}

		writer = addContinuousHeader(writer,
				rb.getString("caching.duplicateContent.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("caching.usingCache.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("caching.cacheControl.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("caching.prefetching.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.connectionOpening.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.unnecssaryConn.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("connections.periodic.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.screenRotation.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("connections.connClosing.detailedTitle"),
				1);
		writer = addContinuousHeader(writer,
				rb.getString("connections.offloadingToWifi.detailedTitle"), 1);
		writer = addContinuousHeader(writer,
				rb.getString("other.accessingPeripherals.detailedTitle"), 1);
		writer = addContinuousHeader(writer, rb.getString("other.httpUsage.detailedTitle"), 1);

		writer = addContinuousHeader(writer, basicStat, 3);

		writer.append(commaSep);
		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcContRecep);
		writer.append(commaSep);
		writer.append(rrcContRecep);
		writer.append(commaSep);
		writer.append(rrcContReceptail);
		writer.append(commaSep);
		writer.append(rrcContReceptail);
		writer.append(commaSep);
		writer.append(rrcSDRX);
		writer.append(commaSep);
		writer.append(rrcSDRX);
		writer.append(commaSep);
		writer.append(rrcLDRX);
		writer.append(commaSep);
		writer.append(rrcLDRX);
		writer.append(commaSep);
		writer.append(rrcIdleToCont);
		writer.append(commaSep);
		writer.append(rrcIdleToCont);

		for (int i = 0; i <= 5; i++) {
			writer.append(commaSep);
		}

		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcIdle);
		writer.append(commaSep);
		writer.append(rrcIdleToCont);
		writer.append(commaSep);
		writer.append(rrcIdleToCont);
		writer.append(commaSep);
		writer.append(rrcContRecep);
		writer.append(commaSep);
		writer.append(rrcContRecep);
		writer.append(commaSep);
		writer.append(rrcContReceptail);
		writer.append(commaSep);
		writer.append(rrcContReceptail);
		writer.append(commaSep);
		writer.append(rrcSDRX);
		writer.append(commaSep);
		writer.append(rrcSDRX);
		writer.append(commaSep);
		writer.append(rrcLDRX);
		writer.append(commaSep);
		writer.append(rrcLDRX);
		writer.append(commaSep);

		writer = addContinuousHeader(writer, rb.getString("datadump.energytitle"), 7);
		writer = addContinuousHeader(writer, userInput, 6);
		writer = addContinuousHeader(writer, app, 6);
		writer = addContinuousHeader(writer, tcpControl, 6);
		writer = addContinuousHeader(writer, tcpLossRec, 6);
		writer = addContinuousHeader(writer, srvrNetDelay, 6);
		writer = addContinuousHeader(writer, largeBurst, 6);
		writer = addContinuousHeader(writer, periodical, 6);
		writer = addContinuousHeader(writer, nonTarget, 6);

		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 3; j++) {
				writer.append(commaSep);
				writer.append(filetype + i);
			}
		}

		writer = addContinuousHeader(writer, rb.getString("Export.traceoverview.throughput"), 1);
		writer = addContinuousHeader(writer, rb.getString("Export.traceoverview.jpkb"), 1);
		writer = addContinuousHeader(writer, rb.getString("Export.traceoverview.promoratio"), 1);

		for (int j = 0; j <= 4; j++) {
			writer.append(commaSep);
			writer.append(conStats);
		}

		for (int j = 0; j <= 2; j++) {
			writer.append(commaSep);
			writer.append(appScore);
		}

		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 3; j++) {
				writer.append(commaSep);
				writer.append(endpntSumm + " " + i);
			}
		}

		writer = addContinuousHeader(writer, rb.getString("cache.cacheable"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.nonCachable"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheMiss"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.notCacheable"), 1);
		writer = addContinuousHeader(
				writer,
				rb.getString("cache.cacheHitExpiredDup304").replace(
						rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(
				writer,
				rb.getString("cache.cacheHitRespChanged").replace(
						rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheHitNotExpiredDup"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheHitExpiredClientDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.cacheHitExpiredServerDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.partialHitNotExpiredDup"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.partialHitExpiredClientDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.partialHitExpiredServerDup")
				.replace(rb.getString("statics.csvCell.seperator"), ""), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.notExpired"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.notExpiredHeur"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.expired"), 1);
		writer = addContinuousHeader(writer, rb.getString("cache.expiredHeur"), 1);

		writer.append(lineSep);

		writer.append(rb.getString("datadump.tracename"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.appVersion"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.appnamever"));

		for (int i = 0; i <= 11; i++) {
			writer.append(commaSep);
			writer.append(bpPassFail);
			writer.append(commaSep);
			writer.append(bpAssoData);
		}

		writer.append(commaSep);
		writer.append(rb.getString("datadump.sizeinbyte"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.durationinsec"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.packets"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.avgrate"));

		for (int i = 0; i <= 5; i++) {
			writer.append(commaSep);
			writer.append(rrcSec);
			writer.append(commaSep);
			writer.append(rrcPCT);
		}

		writer.append(commaSep);
		writer.append(rb.getString("rrc.crTailRatio"));
		writer.append(commaSep);
		writer.append(rb.getString("rrc.longDRXRatio"));
		writer.append(commaSep);
		writer.append(rb.getString("rrc.shortDRXRatio"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.promoratio"));
		writer.append(commaSep);
		writer.append(rb.getString("datadump.totalE"));

		for (int i = 0; i <= 5; i++) {
			writer.append(commaSep);
			writer.append(energyJ);
			writer.append(commaSep);
			writer.append(rrcPCT);
		}

		writer.append(commaSep);
		writer.append(rb.getString("burstAnalysis.jpkb"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.gpsActive"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.gpsStandby"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.gpsTotal"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.cameraTotal"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.bluetoothActive"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.bluetoothStandby"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.bluetoothTotal"));
		writer.append(commaSep);
		writer.append(rb.getString("energy.screenTotal"));

		for (int i = 0; i <= 7; i++) {
			writer.append(commaSep);
			writer.append(burstbytes);
			writer.append(commaSep);
			writer.append(burstbytpct);
			writer.append(commaSep);
			writer.append(burstEner);
			writer.append(commaSep);
			writer.append(burstEngpct);
			writer.append(commaSep);
			writer.append(ltecr);
			writer.append(commaSep);
			writer.append(ltecrpct);
			writer.append(commaSep);
			writer.append(burstjpkb);
		}

		for (int i = 0; i <= 4; i++) {
			writer.append(commaSep);
			writer.append(packettype);
			writer.append(commaSep);
			writer.append(pct);
			writer.append(commaSep);
			writer.append(burstbytes);
		}

		for (int i = 0; i <= 2; i++) {
			writer.append(commaSep);
			writer.append(value);
			writer.append(commaSep);
			writer.append(percentile);
		}

		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.sessionTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.longBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.tightlyGroupedBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.periodicBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("Export.sessionoverview.nonPeriodicBurstTerm"));
		writer.append(commaSep);
		writer.append(rb.getString("appscore.subtitle.causes"));
		writer.append(commaSep);
		writer.append(rb.getString("appscore.subtitle.effects"));
		writer.append(commaSep);
		writer.append(rb.getString("appscore.subtitle.total"));

		for (int i = 0; i <= 4; i++) {
			writer.append(commaSep);
			writer.append(endpntapp);
			writer.append(commaSep);
			writer.append(endpntpkt);
			writer.append(commaSep);
			writer.append(endpntbyte);
		}

		for (int j = 0; j <= 15; j++) {
			writer.append(commaSep);
			writer.append(rb.getString("statics.csvFormat.response"));
			writer.append(commaSep);
			writer.append(rb.getString("statics.csvFormat.bytes"));
		}

		return writer;
	}

	/**
	 * Adds Continuous headers in CSV.
	 * 
	 * @param writer
	 * @param inputStr
	 * @param maxLength
	 * @return A FileWriter object.
	 * @throws IOException
	 */
	private FileWriter addContinuousHeader(FileWriter writer, String inputStr, int maxLength)
			throws IOException {
		for (int i = 0; i <= maxLength; i++) {
			writer.append(commaSep);
			writer.append(inputStr);
		}
		return writer;
	}
}
