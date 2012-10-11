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

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import com.att.aro.commonui.AROProgressDialog;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.datadump.DataDump;
import com.att.aro.images.Images;
import com.att.aro.model.AnalysisFilter;
import com.att.aro.model.NetworkType;
import com.att.aro.model.Profile;
import com.att.aro.model.Profile3G;
import com.att.aro.model.ProfileException;
import com.att.aro.model.ProfileLTE;
import com.att.aro.model.ProfileType;
import com.att.aro.model.TimeRange;
import com.att.aro.model.TraceData;
import com.att.aro.model.UserPreferences;
import com.att.aro.pcap.PCapAdapter;
import com.att.aro.video.AROVideoPlayer;

/**
 * Represents the main window of the ARO application.
 */
public class ApplicationResourceOptimizer extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ApplicationResourceOptimizer.class
			.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	// Menu bar
	private JMenuBar jJMenuBar = null;

	// File menu
	private JMenu jFileMenu = null;
	private JMenuItem jBrowseTraceMenuItem = null;
	private JMenuItem jBrowsePcapItem = null;
	private JMenuItem jExitMenuItem = null;
	private JMenuItem jPrintMenuItem = null;

	// Profile menu
	private JMenu jProfileMenu;
	private JMenuItem jLoadMenuItem;
	private JMenuItem jCustomizeMenuItem;

	// Tools menu
	private JMenu jToolMenu;
	private JMenuItem wiresharkMenuItem;
	private JMenuItem timeRangeAnalysisMenuItem;
	private JMenuItem dataDumpMenuItem;

	// View menu
	private JMenu jViewMenu;
	private JCheckBoxMenuItem screenShotsMenu;
	private JMenuItem selectAppsMenuItem;
	private JMenuItem excludeTimeRangeAnalysisMenuItem;
	private JMenuItem viewOptionsMenuItem;

	// Help menu
	private JMenu jHelpMenu;
	private JMenuItem aboutMenuItem;
	private JMenuItem helpFAQMenuItem;
	private JMenuItem helpForumMenuItem;
	private JMenuItem helpUserGuideMenuItem;
	private JMenuItem helpLearnMoreMenuItem;
	private JMenuItem helpQuickGuideMenuItem;
	private JMenuItem helpDependenciesMenuItem;
	private JMenuItem recordingIndicatorMenuItem;

	private JToolTip recordingIndicatorToolTip;

	private JTabbedPane jMainTabbedPane;
	private AROSimpleTabb aroSimpleTab = new AROSimpleTabb(this);
	private AROAdvancedTabb aroAdvancedTab = new AROAdvancedTabb();
	private AROAnalysisResultsTab analyisResultsPanel = new AROAnalysisResultsTab(this);
	private AROWaterfallTabb aroWaterfallTab = new AROWaterfallTabb(this);
	private AROVideoPlayer aroVideoPlayer;
	private AROBestPracticesTab aroBestPracticesPanel = new AROBestPracticesTab(this);
	private ChartPlotOptionsDialog chartPlotOptionsDialog;
	private TimeRangeAnalysisDialog timeRangeAnalysisDialog;
	private ExcludeTimeRangeAnalysisDialog excludeTimeRangeDialog;

	private UserPreferences userPreferences = UserPreferences.getInstance();
	private TraceData traceData;
	private TraceData.Analysis analysisData;
	private File traceDirectory;

	// Data Collector Menu
	private JMenu jDataCollector = null;
	private JMenuItem startDataCollectorMenuItem = null;
	private JMenuItem stopDataCollectorMenuItem = null;

	private DatacollectorBridge aroDataCollectorBridge;

	private Profile profile;
	private boolean isCancel = false;

	/**
	 * Initializes a new instance of the ApplicationResourceOptimizer class.
	 */
	public ApplicationResourceOptimizer() {
		super();
		initialize();
	}

	/**
	 * Returns the captured trace data.
	 * 
	 * @return A TraceData object representing the captured trace data.
	 */
	public TraceData getTraceData() {
		return traceData;
	}

	/**
	 * Returns the currently loaded trace analysis data. This method returns
	 * null if no trace data has been loaded in the application.
	 * 
	 * @return TraceData.Analysis The trace analysis data.
	 */
	public TraceData.Analysis getAnalysisData() {
		return analysisData;
	}

	/**
	 * Returns an instance of the Overview tab.
	 * 
	 * @return An AROSimpleTabb object containing the Overview tab instance.
	 */
	public AROSimpleTabb getAroSimpleTab() {
		return aroSimpleTab;
	}

	/**
	 * Displays the Overview tab screen.
	 */
	public void displaySimpleTab() {
		getJTabbedPane().setSelectedComponent(aroSimpleTab);
	}

	/**
	 * Displays the Diagnostic tab screen.
	 */
	public void displayAdvancedTab() {
		getJTabbedPane().setSelectedComponent(aroAdvancedTab);
	}

	/**
	 * Displays the Statistics tab screen.
	 */
	public void displayResultTab() {
		getJTabbedPane().setSelectedComponent(analyisResultsPanel);
	}

	/**
	 * Returns an instance of the Diagnostic tab.
	 * 
	 * @return An AROAdvancedTabb object that containing Diagnostic tab
	 *         instance.
	 */
	public AROAdvancedTabb getAroAdvancedTab() {
		return aroAdvancedTab;
	}

	/**
	 * Returns the Chart Plot Options dialog. This dialog appears when the
	 * Options menu item in the View menu is clicked.
	 * 
	 * @return An ChartPlotOptionsDialog object that creates the Chart Plot
	 *         Options dialog.
	 */
	public ChartPlotOptionsDialog getChartPlotOptionsDialog() {
		return chartPlotOptionsDialog;
	}

	/**
	 * Sets a value that indicates the visibility of the
	 * ApplicationResourceOptimizer window.
	 * 
	 * @param b
	 *            A boolean value that indicates whether the
	 *            ApplicationResourceOptimizer window is visible.
	 * 
	 * @see java.awt.Window#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);

		aroVideoPlayer.setVisible(getScreenShotsMenu().isSelected());
		aroVideoPlayer.addWindowListener(new WindowAdapter() {

			/**
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				getScreenShotsMenu().setSelected(false);
			}
		});
	}

	/**
	 * Opens the specified trace directory.
	 * 
	 * @param dir
	 *            - The trace directory to open.
	 * @throws IOException
	 */
	public synchronized void openTrace(File dir) throws IOException {

		clearTrace();

		this.traceDirectory = dir.getParentFile();
		this.traceData = new TraceData(dir);
		if (traceData.getMissingFiles().size() > 0) {
			StringBuffer missingFiles = new StringBuffer();
			for (String file : traceData.getMissingFiles()) {
				missingFiles.append(file + "\n");
			}

			MessageDialogFactory.showMessageDialog(this,
					MessageFormat.format(rb.getString("file.missingAlert"), missingFiles));
		}

		// Make sure profile type matches network type of trace
		try {

			if (traceData.getNetworkType() == NetworkType.LTE) {
				if (!(profile instanceof ProfileLTE)) {
					this.profile = ProfileManager.getInstance().getLastUserProfile(ProfileType.LTE);
				}
			} else {
				if (!(profile instanceof Profile3G)) {
					this.profile = ProfileManager.getInstance().getLastUserProfile(ProfileType.T3G);
				}
			}
		} catch (ProfileException e) {

			// On exception just log it and use current profile
			logger.log(Level.WARNING, "Error switching profile type", e);
		}

		refresh(this.profile, null, null);

		// Save selected directory for traces
		userPreferences.setLastTraceDirectory(this.traceDirectory);

		// Change window name to reflect trace directory
		this.setTitle(MessageFormat.format(rb.getString("aro.title"), dir.toString()));
	}

	/**
	 * Clears the previously loaded trace before loading a new trace.
	 * 
	 * @throws IOException
	 */
	public synchronized void clearTrace() throws IOException {

		if (this.traceData != null) {

			this.traceData = null;
			clearAnalysis();
		}
	}

	/**
	 * Clears the analysis data trace before loading a new trace.
	 * 
	 * @throws IOException
	 */
	private synchronized void clearAnalysis() throws IOException {

		if (this.analysisData != null) {
			if (getAroVideoPlayer() != null) {
				getAroVideoPlayer().clear();
			}
			this.analysisData.clear();
			displayAnalysis(null, this.profile, null, null);

			// Free memory from previous trace
			System.gc();

		}
	}

	/**
	 * Implements opening of selected trace directory.
	 * 
	 * @param pcap
	 * @throws IOException
	 */
	private synchronized void openPcap(File pcap) throws IOException, UnsatisfiedLinkError {

		clearTrace();

		this.traceDirectory = pcap.getParentFile();
		this.traceData = new TraceData(pcap);

		refresh(this.profile, null, null);

		// Save selected directory for traces
		userPreferences.setLastTraceDirectory(this.traceDirectory);

		// Change window name to reflect trace directory
		this.setTitle(MessageFormat.format(rb.getString("aro.title"), pcap.toString()));
	}

	/**
	 * Use to filter the applications from the loaded traces.
	 */
	private void filterAppAndIps() {
		// Make sure trace is loaded
		if (analysisData == null) {
			MessageDialogFactory.showMessageDialog(ApplicationResourceOptimizer.this,
					rb.getString("Error.notrace"), rb.getString("Error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Open dialog
		FilterApplicationsAndIpDialog dialog = new FilterApplicationsAndIpDialog(
				ApplicationResourceOptimizer.this);
		dialog.setVisible(true);

	}

	/**
	 * A callback method that is invoked when changes are made to the status of
	 * the ARO Data Collector.
	 * 
	 * @param status
	 *            - The current status of the ARO Data Collector.
	 */
	public void dataCollectorStatusCallBack(DatacollectorBridge.Status status) {
		switch (status) {
		case STOPPED:
			setDataCollectorMenuItems(false, false);
			setStoppedRecordingIndicator();
			this.setRecordingIndicatorVisible(false);
			break;
		case STARTING:
			setStartingRecordingIndicator();
			setRecordingIndicatorVisible(true);
			setDataCollectorMenuItems(false, false);
			displayRecordingIndicatorToolTip();
			break;
		case STARTED:
			setDataCollectorMenuItems(false, true);
			this.setRecordingIndicatorVisible(true);
			setActiveRecordingIndicator();
			break;
		case READY:
			this.setRecordingIndicatorVisible(false);
			setDataCollectorMenuItems(true, false);
			break;
		default:
			this.setRecordingIndicatorVisible(false);
			setDataCollectorMenuItems(false, false);
		}
	}

	/**
	 * Initializes and returns the ARO window menu bar.
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJFileMenu());
			jJMenuBar.add(getJProfileMenu());
			jJMenuBar.add(getJToolMenu());
			jJMenuBar.add(getJViewMenu());
			jJMenuBar.add(getJDataCollectorMenu());
			jJMenuBar.add(getJHelpMenu());
			jJMenuBar.add(Box.createHorizontalGlue());
			jJMenuBar.add(getJRecordingIndicatorMenu());
		}
		return jJMenuBar;
	}

	/**
	 * Initializes and returns the Profile menu.
	 */
	private JMenu getJProfileMenu() {

		if (jProfileMenu == null) {
			jProfileMenu = new JMenu(rb.getString("menu.profile"));
			jProfileMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			jProfileMenu.add(getJLoadMenuItem());
			jProfileMenu.add(getJCustomizeMenuItem());
		}
		return jProfileMenu;
	}

	/**
	 * Initializes and returns the Load menu item under Profile Menu.
	 */
	private JMenuItem getJLoadMenuItem() {
		if (jLoadMenuItem == null) {
			jLoadMenuItem = new JMenuItem(rb.getString("menu.profile.load"));
			jLoadMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					SelectProfileDialog dialog = new SelectProfileDialog(
							ApplicationResourceOptimizer.this);
					dialog.setVisible(true);
					Profile result = dialog.getSelectedProfile();
					if (result != null) {
						try {
							setProfile(result);
						} catch (IOException e) {
							logger.log(Level.SEVERE, "Unexpected error loading new device profile",
									e);
							MessageDialogFactory.showUnexpectedExceptionDialog(
									ApplicationResourceOptimizer.this, e);
						}
					}
				}
			});
		}
		return jLoadMenuItem;
	}

	/**
	 * Initializes and returns the Customize menu item under Profile Menu.
	 */
	private JMenuItem getJCustomizeMenuItem() {
		if (jCustomizeMenuItem == null) {
			jCustomizeMenuItem = new JMenuItem(rb.getString("menu.profile.customize"));
			jCustomizeMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new ConfigurationFrame(ApplicationResourceOptimizer.this).setVisible(true);
				}
			});
		}
		return jCustomizeMenuItem;
	}

	/**
	 * Initializes and returns the Help menu.
	 */
	private JMenu getJHelpMenu() {
		if (jHelpMenu == null) {
			jHelpMenu = new JMenu(rb.getString("menu.help"));
			jHelpMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			jHelpMenu.add(getAboutMenuItem());
			jHelpMenu.add(getFAQMenuItem());
			jHelpMenu.add(getForumMenuItem());
			jHelpMenu.add(getUserGuideMenuItem());
			jHelpMenu.add(getLearnMoreMenuItem());
			jHelpMenu.add(getQuickGuideMenuItem());
			jHelpMenu.add(getHelpDependenciesMenuItem());
		}
		return jHelpMenu;
	}

	/**
	 * Initializes and returns the Recording menu that appears when the data
	 * collector starts.
	 */
	private JMenuItem getJRecordingIndicatorMenu() {
		if (recordingIndicatorMenuItem == null) {
			recordingIndicatorMenuItem = new JMenuItem(rb.getString("menu.recordingindicator")) {
				private static final long serialVersionUID = 1L;

				public JToolTip createToolTip() {
					recordingIndicatorToolTip = super.createToolTip();
					Color lightBlue = new Color(138, 224, 230);
					recordingIndicatorToolTip.setBackground(lightBlue);
					recordingIndicatorToolTip.setForeground(Color.BLACK);
					Font textFont = new Font("TextFont", Font.BOLD, 10);
					recordingIndicatorToolTip.setFont(textFont);
					return recordingIndicatorToolTip;
				}

			};
			recordingIndicatorMenuItem.createToolTip();
			recordingIndicatorMenuItem.setIcon(Images.RED_RECORDING_INDICATOR.getIcon());
			recordingIndicatorMenuItem.setToolTipText(rb
					.getString("menu.tooltip.recordingindicator"));
			recordingIndicatorMenuItem.setVisible(false);
		}
		return recordingIndicatorMenuItem;
	}

	/**
	 * Initializes and returns the About menu item under the Help menu.
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem(rb.getString("menu.help.about"));
			aboutMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					new AboutDialog(ApplicationResourceOptimizer.this).setVisible(true);
				}
			});
		}
		return aboutMenuItem;

	}

	/**
	 * Initializes and returns the FAQ menu item under the Help menu.
	 */
	private JMenuItem getFAQMenuItem() {
		if (helpFAQMenuItem == null) {
			helpFAQMenuItem = new JMenuItem(rb.getString("menu.help.faq"));
			helpFAQMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						BrowserGenerator.openBrowser(rb.getString("help.faq.URL"));
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					}
				}
			});
		}
		return helpFAQMenuItem;
	}

	/**
	 * Initializes and returns the Forum menu item under the Help menu.
	 */
	private JMenuItem getForumMenuItem() {
		if (helpForumMenuItem == null) {
			helpForumMenuItem = new JMenuItem(rb.getString("menu.help.forum"));
			helpForumMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						BrowserGenerator.openBrowser(rb.getString("help.forum.URL"));
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					}
				}
			});
		}
		return helpForumMenuItem;
	}

	/**
	 * Initializes and returns the User Guide menu item under the Help menu.
	 */
	private JMenuItem getUserGuideMenuItem() {
		if (helpUserGuideMenuItem == null) {
			helpUserGuideMenuItem = new JMenuItem(rb.getString("menu.help.userguide"));
			helpUserGuideMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						BrowserGenerator.openBrowser(rb.getString("help.userguide.URL"));
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					}
				}
			});
		}
		return helpUserGuideMenuItem;
	}

	/**
	 * Initializes and returns the Learn More menu item under the Help menu.
	 */
	private JMenuItem getLearnMoreMenuItem() {
		if (helpLearnMoreMenuItem == null) {
			helpLearnMoreMenuItem = new JMenuItem(rb.getString("menu.help.learnmore"));
			helpLearnMoreMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						BrowserGenerator.openBrowser(rb.getString("help.learnmore.URL"));
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					}
				}
			});
		}
		return helpLearnMoreMenuItem;
	}

	/**
	 * Initializes and returns the Analysis Guide menu item under the Help menu.
	 */
	private JMenuItem getQuickGuideMenuItem() {
		if (helpQuickGuideMenuItem == null) {
			helpQuickGuideMenuItem = new JMenuItem(rb.getString("menu.help.analysisguide"));
			helpQuickGuideMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						BrowserGenerator.openBrowser(rb.getString("help.analysisguide.URL"));
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					}
				}
			});
		}
		return helpQuickGuideMenuItem;
	}

	/**
	 * Initializes and returns the Dependencies menu item under the Help menu.
	 */
	private JMenuItem getHelpDependenciesMenuItem() {
		if (helpDependenciesMenuItem == null) {
			helpDependenciesMenuItem = new JMenuItem(rb.getString("menu.help.dependencies"));
			helpDependenciesMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						new NoticesDialog(ApplicationResourceOptimizer.this).setVisible(true);
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					}
				}
			});
		}
		return helpDependenciesMenuItem;
	}

	/**
	 * Initializes and returns the View menu.
	 */
	private JMenu getJViewMenu() {
		if (jViewMenu == null) {
			jViewMenu = new JMenu(rb.getString("menu.view"));
			jViewMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			jViewMenu.add(getScreenShotsMenu());
			jViewMenu.addSeparator();
			jViewMenu.add(getSelectAppsMenuItem());
			jViewMenu.add(getExcludeTimeRangeAnalysisMenuItem());
			jViewMenu.add(getViewOptionsMenuItem());
		}
		return jViewMenu;
	}

	/**
	 * Initializes and returns the Data Collector menu.
	 */
	private JMenu getJDataCollectorMenu() {
		if (jDataCollector == null) {
			jDataCollector = new JMenu();
			jDataCollector.setText(rb.getString("menu.datacollector"));
			jDataCollector.setMnemonic(KeyEvent.VK_UNDEFINED);
			jDataCollector.add(getJdataCollectorStart());
			jDataCollector.add(getJdataCollectorStop());
			stopDataCollectorMenuItem.setEnabled(false);
		}
		return jDataCollector;
	}

	/**
	 * Initializes and returns the Start Collector menu item under the Data
	 * Collector menu.
	 */
	private JMenuItem getJdataCollectorStart() {
		if (startDataCollectorMenuItem == null) {
			startDataCollectorMenuItem = new JMenuItem();
			startDataCollectorMenuItem.setText(rb.getString("menu.datacollector.start"));
			startDataCollectorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (aroDataCollectorBridge == null) {
						aroDataCollectorBridge = new DatacollectorBridge(
								ApplicationResourceOptimizer.this);
					}
					aroDataCollectorBridge.startARODataCollector();
				}
			});
		}
		return startDataCollectorMenuItem;
	}

	/**
	 * Initializes and returns the Stop Collector menu item under the Data
	 * Collector menu.
	 */
	private JMenuItem getJdataCollectorStop() {
		if (stopDataCollectorMenuItem == null) {
			stopDataCollectorMenuItem = new JMenuItem();
			stopDataCollectorMenuItem.setText(rb.getString("menu.datacollector.stop"));
			stopDataCollectorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (aroDataCollectorBridge != null) {
						aroDataCollectorBridge.stopARODataCollector();
					}
				}
			});
		}
		return stopDataCollectorMenuItem;
	}

	/**
	 * Initializes and returns the Show Video Viewer menu item under the View
	 * menu.
	 */
	private JCheckBoxMenuItem getScreenShotsMenu() {
		if (screenShotsMenu == null) {
			screenShotsMenu = new JCheckBoxMenuItem(rb.getString("menu.view.video"), true);
			screenShotsMenu.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					aroVideoPlayer.setVisible(screenShotsMenu.isSelected());
				}

			});
		}
		return screenShotsMenu;
	}

	/**
	 * Initializes and returns the Select Applications menu item under the View
	 * menu.
	 */
	private JMenuItem getSelectAppsMenuItem() {
		if (selectAppsMenuItem == null) {
			selectAppsMenuItem = new JMenuItem(rb.getString("menu.view.apps"));
			selectAppsMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					filterAppAndIps();
				}
			});
		}
		return selectAppsMenuItem;
	}

	/**
	 * Initializes and returns the Start Collector menu item under the Data
	 * Collector menu.
	 */
	private JMenuItem getViewOptionsMenuItem() {
		if (viewOptionsMenuItem == null) {
			viewOptionsMenuItem = new JMenuItem(rb.getString("menu.view.options"));
			viewOptionsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					chartPlotOptionsDialog.setVisibleToUser(true);
				}
			});
		}
		return viewOptionsMenuItem;
	}

	/**
	 * Initializes and returns the Tools menu.
	 */
	private JMenu getJToolMenu() {
		if (jToolMenu == null) {
			jToolMenu = new JMenu(rb.getString("menu.tools"));
			jToolMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			if (Desktop.isDesktopSupported()) {
				jToolMenu.add(getWiresharkMenuItem());
			}
			jToolMenu.add(getTimeRangeAnalysisMenuItem());
			jToolMenu.add(getDataDump());
		}
		return jToolMenu;
	}

	/**
	 * Initializes and returns the Pcap File Analysis menu item under the Tools
	 * menu.
	 */
	private JMenuItem getWiresharkMenuItem() {
		if (wiresharkMenuItem == null) {
			wiresharkMenuItem = new JMenuItem(rb.getString("menu.tools.wireshark"));
			wiresharkMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					openPcapAnalysis();
				}
			});
		}
		return wiresharkMenuItem;
	}

	/**
	 * Initializes and returns the Time Range Analysis menu item under the Tools
	 * menu.
	 */
	private JMenuItem getTimeRangeAnalysisMenuItem() {
		if (timeRangeAnalysisMenuItem == null) {
			timeRangeAnalysisMenuItem = new JMenuItem(rb.getString("menu.tools.timerangeanalysis"));
			timeRangeAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					initiateTimeRangeAnalysisDialog();
				}
			});
		}
		return timeRangeAnalysisMenuItem;
	}

	/**
	 * Initializes and returns the Pcap File Analysis menu item under the Tools
	 * menu.
	 */
	private JMenuItem getDataDump() {
		if (dataDumpMenuItem == null) {
			dataDumpMenuItem = new JMenuItem(rb.getString("menu.tools.dataDump"));
			dataDumpMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser fc = new JFileChooser(traceDirectory);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (fc.showOpenDialog(ApplicationResourceOptimizer.this) == JFileChooser.APPROVE_OPTION) {
						try {
							startDataDump(fc.getSelectedFile());
						} catch (IOException e1) {
							MessageDialogFactory.showInvalidTraceDialog(fc.getSelectedFile()
									.getPath(), ApplicationResourceOptimizer.this, e1);
						}
					}
				}
			});
		}
		return dataDumpMenuItem;
	}

	/**
	 * Initializes and returns the Exclude Time Range Analysis menu item under
	 * the Tools menu.
	 */
	private JMenuItem getExcludeTimeRangeAnalysisMenuItem() {
		if (excludeTimeRangeAnalysisMenuItem == null) {
			excludeTimeRangeAnalysisMenuItem = new JMenuItem(
					rb.getString("menu.tools.excludetimerangeanalysis"));
			excludeTimeRangeAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					initiateExcludeTimeRangeAnalysisDialog();
				}
			});
		}
		return excludeTimeRangeAnalysisMenuItem;
	}

	/**
	 * Initiates the Pcap File Analysis for the trace data on selecting the Pcap
	 * File Analysis Menu Item.
	 */
	private void openPcapAnalysis() {
		// Make sure trace is loaded
		if (analysisData == null) {
			MessageDialogFactory.showMessageDialog(this, rb.getString("Error.notrace"),
					rb.getString("Error.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Open PCAP analysis tool
		try {
			Desktop.getDesktop().open(analysisData.getTraceData().getPcapFile());
		} catch (NullPointerException e) {
			MessageDialogFactory.showErrorDialog(this, rb.getString("Error.noPcap"));
		} catch (IllegalArgumentException e) {
			MessageDialogFactory.showErrorDialog(this, rb.getString("Error.noPcap"));
		} catch (IOException e) {
			MessageDialogFactory.showErrorDialog(this, rb.getString("Error.noPcapApp"));
		}
	}

	/**
	 * Initiates the Exclude Time Range Analysis for the trace data on selecting
	 * the Exclude Time Range Analysis Menu Item.
	 */
	private void initiateExcludeTimeRangeAnalysisDialog() {
		// Make sure trace is loaded
		if (analysisData == null) {
			MessageDialogFactory.showMessageDialog(ApplicationResourceOptimizer.this,
					rb.getString("Error.notrace"), rb.getString("Error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Open dialog
		if (excludeTimeRangeDialog == null) {
			excludeTimeRangeDialog = new ExcludeTimeRangeAnalysisDialog(
					ApplicationResourceOptimizer.this, analysisData);
		}
		excludeTimeRangeDialog.setVisible(true);
	}

	/**
	 * Initiates the Time Range Analysis for the trace data on selecting the
	 * Time Range Analysis Menu Item.
	 */
	private void initiateTimeRangeAnalysisDialog() {
		// Make sure trace is loaded
		if (analysisData == null) {
			MessageDialogFactory.showMessageDialog(ApplicationResourceOptimizer.this,
					rb.getString("Error.notrace"), rb.getString("Error.title"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Open dialog
		if (timeRangeAnalysisDialog == null) {
			timeRangeAnalysisDialog = new TimeRangeAnalysisDialog(
					ApplicationResourceOptimizer.this, analysisData);
		}
		timeRangeAnalysisDialog.setVisible(true);
	}

	/**
	 * Returns the File menu.
	 */
	private JMenu getJFileMenu() {
		if (jFileMenu == null) {
			jFileMenu = new JMenu(rb.getString("menu.file"));
			jFileMenu.setMnemonic(KeyEvent.VK_UNDEFINED);
			jFileMenu.add(getJBrowseTraceMenuItem());
			jFileMenu.add(getjBrowsePcapItem());
			jFileMenu.addSeparator();
			jFileMenu.add(getJPrintMenuItem());
			jFileMenu.addSeparator();
			jFileMenu.add(getJExitMenuItem());
		}
		return jFileMenu;
	}

	/**
	 * Initializes and returns the Open Trace menu item under the File menu.
	 */
	private JMenuItem getJBrowseTraceMenuItem() {
		if (jBrowseTraceMenuItem == null) {
			jBrowseTraceMenuItem = new JMenuItem(rb.getString("menu.file.open"));
			jBrowseTraceMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser(traceDirectory);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (fc.showOpenDialog(ApplicationResourceOptimizer.this) == JFileChooser.APPROVE_OPTION) {
						try {
							openTrace(fc.getSelectedFile());
						} catch (IOException e1) {
							MessageDialogFactory.showInvalidTraceDialog(fc.getSelectedFile()
									.getPath(), ApplicationResourceOptimizer.this, e1);
						}
					}
				}
			});
		}
		return jBrowseTraceMenuItem;
	}

	/**
	 * Initializes and returns the Open Pcap File menu item under the File menu.
	 */
	private JMenuItem getjBrowsePcapItem() {
		if (jBrowsePcapItem == null) {
			jBrowsePcapItem = new JMenuItem(rb.getString("menu.file.pcap"));
			jBrowsePcapItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser(traceDirectory);
					fc.addChoosableFileFilter(new FileFilter() {

						@Override
						public boolean accept(File f) {
							String name = f.getName();
							return f.isDirectory()
									|| name.endsWith(rb.getString("fileChooser.contentType.cap"))
									|| name.endsWith(rb.getString("fileChooser.contentType.pcap"));
						}

						@Override
						public String getDescription() {
							return rb.getString("fileChooser.desc.pcap");
						}

					});
					if (fc.showOpenDialog(ApplicationResourceOptimizer.this) == JFileChooser.APPROVE_OPTION) {
						try {
							openPcap(fc.getSelectedFile());
						} catch (IOException e1) {
							MessageDialogFactory.showUnexpectedExceptionDialog(
									ApplicationResourceOptimizer.this, e1);
						} catch (UnsatisfiedLinkError er) {
							MessageDialogFactory.showErrorDialog(ApplicationResourceOptimizer.this,
									rb.getString("Error.noNetmon"));
						}
					}
				}
			});
		}
		return jBrowsePcapItem;
	}

	/**
	 * Initializes and returns the Print menu item under the File menu.
	 */
	private JMenuItem getJPrintMenuItem() {
		if (jPrintMenuItem == null) {
			jPrintMenuItem = new JMenuItem(rb.getString("menu.file.print"));
			final JTabbedPane tabbedPane = getJTabbedPane();
			tabbedPane.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (tabbedPane.getSelectedComponent() == getBestPracticesPanel()
							|| tabbedPane.getSelectedComponent() == getAnalysisResultsPanel()) {
						jPrintMenuItem.setEnabled(true);

					} else {
						jPrintMenuItem.setEnabled(false);
					}
				}
			});
			jPrintMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					final Component c = tabbedPane.getSelectedComponent();
					if (c instanceof Printable) {
						final PrinterJob printJob = PrinterJob.getPrinterJob();
						if (printJob.printDialog()) {

							new Thread(new Runnable() {

								@Override
								public void run() {

									printJob.setPrintable((Printable) c);
									try {
										printJob.print();
									} catch (PrinterException e) {
										MessageDialogFactory.showErrorDialog(
												ApplicationResourceOptimizer.this, MessageFormat
														.format(rb.getString("Error.printer"),
																e.getMessage()));
									}

								}
							}).start();
						}
					} else {
						throw new IllegalStateException("Printable tab not selected");
					}
				}
			});
		}
		return jPrintMenuItem;
	}

	/**
	 * Initializes and returns the Exit menu item under the File menu.
	 */
	private JMenuItem getJExitMenuItem() {
		if (jExitMenuItem == null) {
			jExitMenuItem = new JMenuItem(rb.getString("menu.file.exit"));
			jExitMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.exit(0);
				}
			});
		}
		return jExitMenuItem;
	}

	/**
	 * Initializes the ARO application.
	 */
	private void initialize() {
		this.setIconImage(Images.ICON.getImage());
		this.setSize(849, 775);
		this.setJMenuBar(getJJMenuBar());
		this.setContentPane(getJTabbedPane());
		this.setTitle(MessageFormat.format(rb.getString("aro.title"), ""));
		this.setResizable(true);

		try {
			// Checks that all necessary pcap libraries are installed on the
			// system.
			PCapAdapter.ping();
		} catch (UnsatisfiedLinkError ule) {
			MessageDialogFactory.showErrorDialog(this, rb.getString("aro.winpcap_error"));
			System.exit(-1);
		} catch (Exception e) {
			MessageDialogFactory.showErrorDialog(this, rb.getString("aro.winpcap_error"));
			System.exit(-1);
		}

		// Register aroWindowStateListener with the frame
		this.addWindowStateListener(aroWindowStateListener);
		// Load user preference
		this.traceDirectory = userPreferences.getLastTraceDirectory();

		// Setup video player
		aroVideoPlayer = new AROVideoPlayer(this.aroAdvancedTab);
		aroVideoPlayer.setBounds(850, 0, 350, 600);
		aroAdvancedTab.setVideoPlayer(aroVideoPlayer);

		// Default profile loaded.
		try {
			this.profile = ProfileManager.getInstance().getLastUserProfile(null);
		} catch (ProfileException e) {
			this.profile = ProfileManager.getInstance().getDefaultProfile();
		} catch (IOException e) {
			MessageDialogFactory.showErrorDialog(this, rb.getString("configuration.loaderror"));
			this.profile = ProfileManager.getInstance().getDefaultProfile();
		}

		chartPlotOptionsDialog = new ChartPlotOptionsDialog(ApplicationResourceOptimizer.this,
				aroAdvancedTab);

		// Commented out because I think this is causing an error on JNLP
		// startup
		// this.getContentPane().addHierarchyBoundsListener(
		// new HierarchyBoundsListener() {
		// @Override
		// public void ancestorMoved(HierarchyEvent e) {
		// // do nothing
		// }
		//
		// @Override
		// public void ancestorResized(HierarchyEvent e) {
		// boolean isStatsTabSelected = false;
		// boolean isWaterFallTabSelected = false;
		// if(getJTabbedPane().getSelectedComponent() == analyisResultsPanel){
		// isStatsTabSelected = true;
		// }else if(getJTabbedPane().getSelectedComponent() == aroWaterfallTab){
		// isWaterFallTabSelected = true;
		// }
		// jMainTabbedPane.remove(analyisResultsPanel);
		// jMainTabbedPane.remove(aroWaterfallTab);
		// analyisResultsPanel = new
		// AROAnalysisResultsTab(ApplicationResourceOptimizer.this);
		// jMainTabbedPane.addTab(rb.getString("aro.tab.analysis"), null,
		// analyisResultsPanel, null);
		// jMainTabbedPane.addTab(rb.getString("aro.tab.waterfall"), null,
		// aroWaterfallTab, null);
		// if(isStatsTabSelected){
		// jMainTabbedPane.setSelectedComponent(analyisResultsPanel);
		// }else if(isWaterFallTabSelected){
		// jMainTabbedPane.setSelectedComponent(aroWaterfallTab);
		// }
		// analyisResultsPanel.refresh(analysisData);
		// //aroWaterfallTab.refresh(analysisData);
		//
		// }
		// });
	}

	/**
	 * Enables or disables the Chart Options menu item in the View menu.
	 * 
	 * @param enable
	 *            When this boolean value is true, the Chart Options menu item
	 *            is visible, when it is false, the menu item is invisible.
	 */
	protected void enableChartOptionsMenuItem(boolean enable) {
		this.viewOptionsMenuItem.setEnabled(enable);
	}

	/**
	 * Initializes and returns the Tabbed Pane for the ARO frame.
	 */
	private JTabbedPane getJTabbedPane() {
		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
		UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
		if (jMainTabbedPane == null) {
			jMainTabbedPane = new JTabbedPane();
			jMainTabbedPane.addTab(rb.getString("aro.tab.bestpractices"), null,
					getBestPracticesPanel(), null);
			jMainTabbedPane.addTab(rb.getString("aro.tab.simple"), null, getAroSimpleTab(), null);
			jMainTabbedPane.addTab(rb.getString("aro.tab.advanced"), null, getAroAdvancedTab(),
					null);
			jMainTabbedPane.addTab(rb.getString("aro.tab.analysis"), null,
					getAnalysisResultsPanel(), null);
			jMainTabbedPane.addTab(rb.getString("aro.tab.waterfall"), null, getWaterfallPanel(),
					null);
		}
		return jMainTabbedPane;
	}

	/**
	 * Initializes and returns the Video Player for the ARO application which
	 * shows the device screen shots
	 */
	private AROVideoPlayer getAroVideoPlayer() {
		if (aroVideoPlayer == null) {
			aroVideoPlayer = new AROVideoPlayer(aroAdvancedTab);
		}
		return aroVideoPlayer;
	}

	/**
	 * Returns an instance of the Statistics tab.
	 * 
	 * @return An AROAnalysisResultsTab object containing the Statistics tab
	 *         instance.
	 */
	public AROAnalysisResultsTab getAnalysisResultsPanel() {
		return analyisResultsPanel;
	}

	/**
	 * Returns an instance of the Waterfall tab.
	 * 
	 * @return An AROWaterfallTabb object containing the Waterfall tab instance.
	 */
	public AROWaterfallTabb getWaterfallPanel() {
		return aroWaterfallTab;
	}

	/**
	 * Refreshes the analysis using a new filter
	 * 
	 * @param filter
	 *            - An AnalysisFilter object that defines the filter to be used
	 *            on the trace data.
	 * @throws IOException
	 */
	public void refresh(AnalysisFilter filter) throws IOException {
		refresh(this.profile, filter, null);
	}

	/**
	 * Returns the Best Practices tab screen.
	 */
	public AROBestPracticesTab getBestPracticesPanel() {
		return this.aroBestPracticesPanel;
	}

	/**
	 * Refreshes the view with updated app/ip settings. This method should be
	 * run on the event dispatch thread
	 */
	private synchronized void refresh(final Profile profile, final AnalysisFilter filter,
			final String msg) throws IOException {

		if (traceData != null) {
			final AROProgressDialog dialog = new AROProgressDialog(this,
					rb.getString("progress.loadingTrace"));
			dialog.setVisible(true);
			final SwingWorker<TraceData.Analysis, Object> mySwingWorker = new SwingWorker<TraceData.Analysis, Object>() {

				@Override
				protected TraceData.Analysis doInBackground() throws IOException {
					return traceData.runAnalysis(profile, filter);
				}

				@Override
				protected void done() {
					try {
						if (!this.isCancelled()) {
							displayAnalysis(get(), profile, filter, msg);
						} else {
							return;
						}

					} catch (IOException e) {
						logger.log(Level.SEVERE, "Unexpected IOException analyzing trace", e);
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE, "Unexpected exception analyzing trace", e);
						MessageDialogFactory.showUnexpectedExceptionDialog(
								ApplicationResourceOptimizer.this, e);
					} catch (ExecutionException e) {
						logger.log(Level.SEVERE, "Unexpected execution exception analyzing trace",
								e);
						if (e.getCause() instanceof OutOfMemoryError) {
							MessageDialogFactory.showErrorDialog(null,
									rb.getString("Error.outOfMemory"));
						} else {
							MessageDialogFactory.showUnexpectedExceptionDialog(
									ApplicationResourceOptimizer.this, e);
						}
					} finally {
						if (!this.isCancelled()) {
							dialog.dispose();
						}
					}

				}
			};
			mySwingWorker.execute();
			dialog.addWindowListener(new WindowListener() {

				@Override
				public void windowOpened(WindowEvent arg0) {
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {

				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
				}

				@Override
				public void windowClosing(WindowEvent arg0) {
					dialog.setVisible(false);
					try {
						isCancel = true;
						mySwingWorker.cancel(true);
						clearTrace();
					} catch (CancellationException cE) {
						cE.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void windowClosed(WindowEvent arg0) {
				}

				@Override
				public void windowActivated(WindowEvent arg0) {
				}
			});

		} else {
			displayAnalysis(null, profile, filter, msg);
		}
	}

	/**
	 * Performs the analysis based on the time range selected in the exclude
	 * time range dialog.
	 * 
	 * @param beginTime
	 *            The start of the time range to be used for the analysis.
	 * @param endTime
	 *            The end of the time range to be used for the analysis.
	 * @throws IOException
	 */
	public void performExcludeTimeRangeAnalysis(double beginTime, double endTime)
			throws IOException {

		if (this.analysisData != null) {
			AnalysisFilter filter = analysisData.getFilter();
			filter.setTimeRange(new TimeRange(beginTime, endTime));
			refresh(this.profile, filter, null);
		}
	}

	/**
	 * Refreshes the view based on the Profile and applications selected when a
	 * trace is loaded for analysis.
	 * 
	 * @param analysis
	 *            The trace analysis data.
	 * @param profile
	 *            The selected profile.
	 * @param selections
	 *            The collection of selected application/IPs.
	 * 
	 */
	private synchronized void displayAnalysis(TraceData.Analysis analysis, Profile profile,
			AnalysisFilter filter, String msg) throws IOException {

		this.analysisData = analysis;

		// Force regeneration of TRA dialog
		this.timeRangeAnalysisDialog = null;
		this.excludeTimeRangeDialog = null;

		getAroVideoPlayer().refresh(analysisData);
		getAroAdvancedTab().setAnalysisData(analysisData);
		getAroSimpleTab().refresh(analysisData);
		getBestPracticesPanel().refresh(analysisData);
		getAnalysisResultsPanel().refresh(analysisData);
		getWaterfallPanel().refresh(analysis);

		this.profile = profile;
		UserPreferences.getInstance().setLastProfile(profile);

		if (msg != null) {
			MessageDialogFactory.showMessageDialog(this, msg);
		}
	}

	/**
	 * Sets the 'selected' status of the specified chart plot option, saves that
	 * selection to the UserPreferences, and notifies the chartPlotOptions
	 * dialog of the change.
	 * 
	 * @param option
	 *            The chart plot option to be set.
	 * @param selected
	 *            A boolean value that indicates whether or not the chart plot
	 *            option was selected in the dialog.
	 */
	public void setExternalChartPlotSelection(ChartPlotOptions option, boolean selected) {
		// grab current list of options
		List<ChartPlotOptions> options = new ArrayList<ChartPlotOptions>(
				userPreferences.getChartPlotOptions());

		// select or de-select
		if (options.contains(option) && !selected) {
			options.remove(option);
		} else if (!options.contains(option) && selected) {
			options.add(option);
		}

		// for setting default value to checked
		if (!(option == ChartPlotOptions.DEFAULT_VIEW && selected)) {
			// remove default setting since this isn't a default view anymore
			options.remove(ChartPlotOptions.DEFAULT_VIEW);
		}

		// save the new preferences
		userPreferences.setChartPlotOptions(options);
		// notify dialog of changes
		this.chartPlotOptionsDialog.updateFromUserPreferences();
	}

	/**
	 * Returns the currently selected device profile.
	 * 
	 * @return A Profile object representing the currently selected profile.
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Sets the device profile that is used for analysis.
	 * 
	 * @param profile
	 *            - The device profile to be set.
	 * 
	 * @throws IOException
	 */
	public void setProfile(Profile profile) throws IOException {
		AnalysisFilter filter = this.analysisData != null ? analysisData.getFilter() : null;
		clearAnalysis();
		refresh(profile, filter, rb.getString("configuration.applied"));
	}

	/**
	 * This listener checks to see if the ApplicationResourceOptimizer window
	 * has been maximized. If it has been then it calls 2 methods. One sets the
	 * AROSimpleTabb vertical split pane to equal parts and one sets the
	 * AROAdvancedTabb split pane to equal parts.
	 * 
	 */
	private final WindowStateListener aroWindowStateListener = new WindowAdapter() {
		public void windowStateChanged(WindowEvent evt) {
			int oldState = evt.getOldState();
			int newState = evt.getNewState();

			if ((oldState & Frame.MAXIMIZED_BOTH) == 0 && (newState & Frame.MAXIMIZED_BOTH) != 0) {
				// aroSimpleTab.resetSplitPanesSimpleTabb();
				aroAdvancedTab.resetSplitPanesAdvancedTabb();
			}
		}
	};

	/**
	 * Sets the visibility of the recording indicator menu item.
	 */
	private void setRecordingIndicatorVisible(boolean bValue) {
		if (recordingIndicatorMenuItem != null) {
			recordingIndicatorMenuItem.setVisible(bValue);
		}
	}

	/**
	 * Sets recording indicator menu item text when the data collector starts.
	 */
	private void setStartingRecordingIndicator() {
		if (recordingIndicatorMenuItem != null) {
			recordingIndicatorMenuItem.setText(rb.getString("menu.recordingindicatorstarting"));
			recordingIndicatorMenuItem.setIcon(Images.YELLOW_RECORDING_INDICATOR.getIcon());
			recordingIndicatorMenuItem.setVisible(true);
		}
	}

	/**
	 * Sets recording indicator menu item text when the trace collection is in
	 * progress.
	 */
	private void setActiveRecordingIndicator() {
		if (recordingIndicatorMenuItem != null) {
			recordingIndicatorMenuItem.setText(rb.getString("menu.recordingindicator"));
			recordingIndicatorMenuItem.setIcon(Images.RED_RECORDING_INDICATOR.getIcon());
			recordingIndicatorMenuItem.setVisible(true);
		}
	}

	/**
	 * Sets recording indicator menu item text when the data collector stops.
	 */
	private void setStoppedRecordingIndicator() {
		if (recordingIndicatorMenuItem != null) {
			recordingIndicatorMenuItem.setText(rb.getString("menu.recordingindicatorstopped"));
			recordingIndicatorMenuItem.setIcon(Images.RED_RECORDING_INDICATOR.getIcon());
			recordingIndicatorMenuItem.setVisible(true);
		}
	}

	/**
	 * Sets the tool tip for the recording indicator.
	 */
	private void displayRecordingIndicatorToolTip() {
		if ((recordingIndicatorToolTip != null) && (recordingIndicatorMenuItem != null)) {
			recordingIndicatorToolTip.setVisible(true);
			ToolTipManager.sharedInstance().mouseMoved(
					new MouseEvent(recordingIndicatorMenuItem, 0, 0, 0, 0, 0, // X-Y
																				// of
																				// the
																				// mouse
																				// for
																				// the
																				// tool
																				// tip
							0, false));
			recordingIndicatorToolTip.setVisible(true);
		}
	}

	/**
	 * Enables/Disables the Data Collector menu items based on the boolean
	 * passed.
	 */
	private void setDataCollectorMenuItems(boolean bStartItem, boolean bStopItem) {
		startDataCollectorMenuItem.setEnabled(bStartItem);
		stopDataCollectorMenuItem.setEnabled(bStopItem);
	}

	/**
	 * Performs the operations for dumping various trace data' in one file.
	 * 
	 * @param dir
	 * @throws IOException
	 */
	private void startDataDump(File dir) throws IOException {
		new DataDump(dir, getProfile().getName());
	}

}
