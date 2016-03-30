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

package com.att.aro.ui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.android.ddmlib.IDevice;
import com.att.aro.core.ILogger;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.pojo.CollectorStatus;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.pojo.VersionInfo;
import com.att.aro.core.util.CrashHandler;
import com.att.aro.core.util.GoogleAnalyticsUtil;
import com.att.aro.core.util.Util;
import com.att.aro.mvc.AROController;
import com.att.aro.ui.collection.AROCollectorSwingWorker;
import com.att.aro.ui.commonui.ARODiagnosticsOverviewRouteImpl;
import com.att.aro.ui.commonui.AROSwingWorker;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.bestpracticestab.BestPracticesTab;
import com.att.aro.ui.view.diagnostictab.ChartPlotOptions;
import com.att.aro.ui.view.diagnostictab.DiagnosticsTab;
import com.att.aro.ui.view.menu.AROMainFrameMenu;
import com.att.aro.ui.view.menu.help.SplashScreen;
import com.att.aro.ui.view.menu.tools.DataDump;
import com.att.aro.ui.view.overviewtab.OverviewTab;
import com.att.aro.ui.view.statistics.StatisticsTab;
import com.att.aro.ui.view.video.AROVideoPlayer;
import com.att.aro.ui.view.video.LiveScreenViewDialog;
import com.att.aro.ui.view.waterfalltab.WaterfallTab;
import com.att.aro.view.images.Images;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class MainFrame implements SharedAttributesProcesses {

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages"); //$NON-NLS-1$

	private JFrame frmApplicationResourceOptimizer;
	private JTabbedPane jMainTabbedPane;
	private BestPracticesTab bestPracticesTab;
	private StatisticsTab statisticsTab;
	private DiagnosticsTab diagnosticsTab;
	private OverviewTab overviewTab;
	private WaterfallTab waterfallTab;
	private AROMainFrameMenu mainMenu;
	private AROController controller;
	private String tracePath;
	private String reportPath;
	private Profile profile;
	private final List<PropertyChangeListener> propertyChangeListeners =
			new ArrayList<PropertyChangeListener>();
	private final List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private static MainFrame window;
	private AROModelObserver modelObserver; //for updating all the observers

	private ILogger log = ContextAware.getAROConfigContext().getBean(ILogger.class);
	private VersionInfo versionInfo = ContextAware.getAROConfigContext().getBean(VersionInfo.class);
	private TabPanels tabPanel = TabPanels.tab_panel_best_practices;

	private boolean videoPlayerSelected = true;
	private AROVideoPlayer aroVideoPlayer;

	private CollectorStatus collectorStatus;

	private IARODiagnosticsOverviewRoute route;

	private LiveScreenViewDialog liveView;
	
	private AnalysisFilter filter;
	
	public static MainFrame getWindow() {
		return window;
	}
	
	public JFrame getJFrame(){
		return frmApplicationResourceOptimizer;
	}

	public AROController getController() {
		return controller;
	}

	public static class CommandLineParams {
		@Parameter(names = {"--help", "-h", "-?"}, description = "show help", help = true)
		private boolean help = false;

		@Parameter(names = "--input", converter = FileConverter.class,
				validateWith = ValidateInput.class, description = "open report located in directory")
		private File input;

		@Parameter(names = "--splash", description = "display splashscreen upon startup", arity = 1, hidden = true)
		private boolean isSplashRequired = true;

		public File getInputDirectory() {
			return input;
		}

		public boolean isSplashRequired() {
			return isSplashRequired;
		}

		public static class ValidateInput implements IParameterValidator {
			public void validate(String name, String value)
					throws ParameterException {
				File input = new File(value);
				if (!input.exists()) {
					throw new ParameterException("File not found: " + value);
				}
			}
		}

		public static class FileConverter implements IStringConverter<File> {
			public File convert(String value) {
				return new File(value);
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		CommandLineParams cmds = new CommandLineParams();
		JCommander commandLineParser = new JCommander(cmds);
		try {
			commandLineParser.parse(args);
			if (cmds.help) {
				commandLineParser.usage();
				System.exit(0);
			} else {
				startUI(cmds);
			}
		} catch (ParameterException ex) {
			System.err.println("Error parsing command: " + ex.getMessage());
			commandLineParser.usage();
			System.exit(1);
		}
	}

	private static void startUI(final CommandLineParams params) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new MainFrame();
					window.frmApplicationResourceOptimizer.setVisible(true);
					File input = params.getInputDirectory();
					if (input != null) {
						window.updateTracePath(input);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Code to display About window from Mac menu
				if (System.getProperty("os.name").contains("Mac")) {
				    try {
				        Object app = Class.forName("com.apple.eawt.Application").getMethod("getApplication",
				         (Class[]) null).invoke(null, (Object[]) null);

//				        Object al = Proxy.newProxyInstance(Class.forName("com.apple.eawt.AboutHandler")
//				                .getClassLoader(), new Class[] { Class.forName("com.apple.eawt.AboutHandler") },
//				                    new AboutListener());
//				        app.getClass().getMethod("setAboutHandler", new Class[] {
//				            Class.forName("com.apple.eawt.AboutHandler") }).invoke(app, new Object[] { al });
				    } catch (Exception e) {
				        //fail quietly
				    }
				}
				if (params.isSplashRequired()) {
					final SplashScreen splash = new SplashScreen();
					splash.setVisible(true);
					splash.setAlwaysOnTop(true);
					new SwingWorker<Object, Object>() {

						@Override
						protected Object doInBackground() {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
							}
							return null;
						}
						@Override
						protected void done() {
							splash.dispose();
						}

					}.execute();
				}
				System.out.println("---------------------DONE---------------------");
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainFrame() {
		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		AROUIManager.init();
		frmApplicationResourceOptimizer = new JFrame();
		frmApplicationResourceOptimizer.setTitle("AT&T Application Resource Optimizer (ARO)");
		frmApplicationResourceOptimizer.setIconImage(Images.ICON.getImage());
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frmApplicationResourceOptimizer.setBounds(0,0,screenSize.width, screenSize.height);
		frmApplicationResourceOptimizer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainMenu = new AROMainFrameMenu(this);//.getInstance();
		modelObserver = new AROModelObserver();
		frmApplicationResourceOptimizer.setJMenuBar(mainMenu.getAROMainFileMenu());

		controller = new AROController(this);
		frmApplicationResourceOptimizer.setContentPane(getJTabbedPane());
		
		aroVideoPlayer = new AROVideoPlayer();
		aroVideoPlayer.setBounds(1041, 0, 350, 600);
		aroVideoPlayer.setVisible(true);
		modelObserver.registerObserver(aroVideoPlayer);
		
		aroVideoPlayer.setAroAdvancedTab(diagnosticsTab);
		diagnosticsTab.setVideoPlayer(aroVideoPlayer);

		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().applicationInfo(GoogleAnalyticsUtil.getAnalyticsEvents().getTrackerID(), BUNDLE.getString("aro.title.short").trim(), versionInfo.getVersion());
		sendInstallationInfoTOGA();
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsStartSessionEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getAnalyzerEvent(), GoogleAnalyticsUtil.getAnalyticsEvents().getStartApp());
		frmApplicationResourceOptimizer.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent wEvent){
				dispose();
			}
			
		});
		
		log.info("ARO UI started");
	}
	
	private JTabbedPane getJTabbedPane() {
		if (jMainTabbedPane == null) {
			UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
			UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);
			
			jMainTabbedPane = new JTabbedPane();
			route = new ARODiagnosticsOverviewRouteImpl(jMainTabbedPane);
			
			bestPracticesTab = new BestPracticesTab(route);
			jMainTabbedPane.add(BUNDLE.getString("aro.tab.bestpractices"), bestPracticesTab);
			modelObserver.registerObserver(bestPracticesTab);
			
			overviewTab = new OverviewTab(route);
			jMainTabbedPane.add(BUNDLE.getString("aro.tab.overview"), overviewTab.layoutDataPanel());
			modelObserver.registerObserver(overviewTab);

			diagnosticsTab = new DiagnosticsTab(this);
			jMainTabbedPane.add(BUNDLE.getString("aro.tab.advanced"),diagnosticsTab);
			modelObserver.registerObserver(diagnosticsTab);

			statisticsTab = new StatisticsTab(this);
			jMainTabbedPane.add(BUNDLE.getString("aro.tab.statistics"), statisticsTab);
			modelObserver.registerObserver(statisticsTab);

			waterfallTab = new WaterfallTab(route);
			jMainTabbedPane.add(BUNDLE.getString("aro.tab.waterfall"), waterfallTab.layoutDataPanel());
			modelObserver.registerObserver(waterfallTab);
			
			jMainTabbedPane.addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(ChangeEvent event) {
					onTabChanged(event);
				}});
		}
		return jMainTabbedPane;
	}
	
	private void onTabChanged(ChangeEvent event){
		if(getCurrentTabComponent() == bestPracticesTab){
			tabPanel = TabPanels.tab_panel_best_practices;
		}else if(getCurrentTabComponent() == statisticsTab){
			tabPanel = TabPanels.tab_panel_statistics;
		}else if(getCurrentTabComponent() == diagnosticsTab){
			tabPanel = TabPanels.tab_panel_other;
		}else{
			tabPanel = TabPanels.tab_panel_other;
		}
	}

	/**
	 * Takes care of displaying or removing the Video Player based on argument.
	 * 
	 * @param displayPlayer
	 */
	private void displayRemoveVideoPlayer(boolean displayPlayer) {
		this.aroVideoPlayer.setVisible(displayPlayer);
	}

	@Override
	public Component getCurrentTabComponent() {
		return jMainTabbedPane.getSelectedComponent();
	}
	
	@Override
	public void addAROPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
		propertyChangeListeners.add(propertyChangeListener);
	}
	
	@Override
	public void addAROActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	@Override
	public void updateTracePath(File path) {
		if (path!=null){
			notifyPropertyChangeListeners("tracePath", tracePath, path.getAbsolutePath());
			tracePath = path.getAbsolutePath();
			
			if(path.isDirectory()){
				GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getAnalyzerEvent(), GoogleAnalyticsUtil.getAnalyticsEvents().getLoadTrace()); //GA Request
				
			} else {
				GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getAnalyzerEvent(), GoogleAnalyticsUtil.getAnalyticsEvents().getLoadPcap()); //GA Request
			}

		}
	}

	@Override
	public void updateReportPath(File path) {
		if (path!=null){
			notifyPropertyChangeListeners("reportPath", reportPath, path.getAbsolutePath());
			reportPath = path.getAbsolutePath();
		}
	}

	private void showErrorMessage(String message) {
		MessageDialogFactory.showMessageDialog(this.getJFrame(),
				ResourceBundleHelper.getMessageString(message),
				ResourceBundleHelper.getMessageString("menu.error.title"),
				JOptionPane.ERROR_MESSAGE);	
	}

//	@Override
	public void refresh() {
		
		if(controller != null){

			AROTraceData traceData = controller.getTheModel();
			if (traceData.isSuccess()) {
				modelObserver.refreshModel(traceData);
				this.profile = traceData.getAnalyzerResult().getProfile();
				if (traceData.getAnalyzerResult().getTraceresult()
						.getTraceResultType() == TraceResultType.TRACE_DIRECTORY) {
					TraceDirectoryResult traceResults = (TraceDirectoryResult) traceData
							.getAnalyzerResult().getTraceresult();
					GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(
									traceResults.getDeviceDetail().getDeviceModel(),
									traceResults.getDeviceDetail().getOsType(),
									traceResults.getDeviceDetail().getOsVersion()); // GA Request
				}
			} else if (traceData.getError() != null) {
				MessageDialogFactory.getInstance().showErrorDialog(window.getJFrame(),
						traceData.getError().getDescription());

			} else {
				showErrorMessage("menu.error.unknownfileformat");
			}

		}
	}
	
	@Override
	public String getTracePath() {
		return tracePath;
	}

	@Override
	public boolean isModelPresent() {
		return controller != null && controller.getTheModel() != null &&
				controller.getTheModel().isSuccess();
	}

	@Override
	public String getReportPath() {
		return reportPath;
	}

	@Override
	public TabPanels getCurrentTabPanel() {
		return tabPanel;
	}

	/**
	 * Sets the device profile that is used for analysis.
	 * 
	 * @param profile
	 *            - The device profile to be set.
	 * 
	 * @throws IOException
	 */
	@Override
	public void updateProfile(Profile profile) {
		notifyPropertyChangeListeners("profile", this.profile, profile);
		this.profile = profile;
	}

	@Override
	public boolean isVideoPlayerSelected() {
		return videoPlayerSelected;
	}

	@Override
	public void updateVideoPlayerSelected(boolean videoPlayerSelected) {
		this.videoPlayerSelected = videoPlayerSelected;
		displayRemoveVideoPlayer(videoPlayerSelected);
	}

	@Override
	public Profile getProfile() {
		return profile;
	}

	public void notifyPropertyChangeListeners(String property, Object oldValue, Object newValue) {
		if (property.equals("profile"))	{
			new AROSwingWorker<Void, Void>(frmApplicationResourceOptimizer, propertyChangeListeners, property, oldValue, newValue, 
					ResourceBundleHelper.getMessageString("configuration.applied")).execute();
		}else if (property.equals("filter")){
			new AROSwingWorker<Void, Void>(frmApplicationResourceOptimizer, propertyChangeListeners, property, oldValue, newValue, 
					null).execute();
		} else {
			new AROSwingWorker<Void, Void>(frmApplicationResourceOptimizer, propertyChangeListeners, property, oldValue, newValue, null).execute();	
		}	
	}

//	@Override
	public void notifyActionListeners(int id, String command) {
		new AROSwingWorker<Void, Void>(frmApplicationResourceOptimizer, actionListeners, id, command, null).execute();
	}
	
	/*-----------------------
	 * Start-stop Collectors
	 * ----------------------*/
	@Override
	public void startCollector(int deviceId, String tracePath, boolean videoCapture) {
		new AROCollectorSwingWorker<Void, Void>(
									frmApplicationResourceOptimizer
									, actionListeners
									, 1
									, "startCollector"
									, deviceId
									, tracePath
									, videoCapture
									).execute();
	}
	
	@Override
	public void startCollectorIos(IDataCollector iOsCollector, String udid, String tracePath, boolean videoCapture) {
		new AROCollectorSwingWorker<Void, Void>(
									frmApplicationResourceOptimizer
									, actionListeners
									, 2
									, "startCollectorIos"
									, iOsCollector
									, udid
									, tracePath
									, videoCapture
									).execute();
	}
	
	@Override
	public void liveVideoDisplay(IDataCollector collector) {
		liveView = new LiveScreenViewDialog(this, collector);
		liveView.setVisible(true);
		log.info("liveVideoDisplay started");
	}

	@Override
	public void stopCollector() {
		if (liveView != null) {
			liveView.setVisible(false);
			liveView = null;}
		new AROCollectorSwingWorker<Void, Void>(
									frmApplicationResourceOptimizer
									, actionListeners
									, 3
									, "stopCollector"
									, null
									).execute();
	}

	@Override
	public void haltCollector() {
		if (liveView != null) {
			liveView.setVisible(false);
			liveView = null;}
		new AROCollectorSwingWorker<Void, Void>(
									frmApplicationResourceOptimizer
									, actionListeners
									, 3
									, "haltCollectorInDevice"
									, null
									).execute();
	}

	@Override
	public List<IDataCollector> getAvailableCollectors() {
		return controller.getAvailableCollectors();
	}

	@Override
	public IDevice[] getConnectedDevices() {
		return controller.getConnectedDevices();
	}

	@Override
	public void updateCollectorStatus(CollectorStatus collectorStatus, StatusResult statusResult) {
		
		this.collectorStatus = collectorStatus;
		
		if (statusResult == null) {
			return;
		}
		
		log.info("updateCollectorStatus :STATUS :" + statusResult);
		
		// timeout - collection not approved in time
		if (!statusResult.isSuccess()) {
			String traceFolder = controller.getTraceFolderPath();
			log.info("updateCollectorStatus :FAILED STATUS :" + statusResult.getError().getDescription());
			MessageDialogFactory.getInstance().showErrorDialog(window.getJFrame(), statusResult.getError().getDescription());

			
//			String selection = MessageDialogFactory.getInstance().showTimeOutOptions(window.getJFrame()
//																, traceFolder
//																, statusResult.getError().getDescription()
//																, "Off, due to timeout"
//																, "");
//			if (selection.equals("Stop")) {
//				stopCollector();
//			} else if (selection.equals("Quit")) {
//				haltCollector();
//			} else if (selection.equals("Restart")) {
//			}
			//this.collectorStatus = null;
//			haltCollector();
			
			
			return;
		}

		// Collection has been stopped ask to open trace
		if (collectorStatus != null && collectorStatus.equals(CollectorStatus.STOPPED)){
			log.info("stopDialog");
			String traceFolder = controller.getTraceFolderPath();
			int seconds = (int) (controller.getTraceDuration()/1000);
			boolean approveOpenTrace = MessageDialogFactory.getInstance().showTraceSummary(frmApplicationResourceOptimizer, traceFolder, controller.isVideoCapture(), Util.formatHHMMSS(seconds));
			if (approveOpenTrace){
				updateTracePath(new File(controller.getTraceFolderPath()));
			}
			return;
		}
		
	}

	@Override
	public CollectorStatus getCollectorStatus() {
		return collectorStatus;
	}

	/*-----------------------------
	 * end of Start-stop Collectors
	 * ----------------------------*/

	@Override
	public Frame getFrame() {
		return frmApplicationResourceOptimizer;
	}

	@Override
	public void updateChartSelection(List<ChartPlotOptions> optionsSelected) {
		diagnosticsTab.setChartOptions(optionsSelected);		
	}


	@Override
	public void dataDump(File dir) throws IOException {
		new DataDump(dir, controller, false, false);
	}

	@Override
	public void dispose(){
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEndSessionEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getAnalyzerEvent(), GoogleAnalyticsUtil.getAnalyticsEvents().getEndApp());
		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().close();
	}
	
	private void sendInstallationInfoTOGA(){
		
    	String userHome = System.getProperty("user.home") + "/gaInstalledFile.txt";
    	
    	File installationFile = new File(userHome);
    	
    	if(installationFile.exists()){
    		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm");  
    		long installedTime = installationFile.lastModified();
    		String lastModifiedDate = df.format(new Date(installedTime));
    		lastModifiedDate = lastModifiedDate.replace(" ", "-");
    		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getInstaller(), versionInfo.getVersion(), lastModifiedDate);
    		GoogleAnalyticsUtil.getGoogleAnalyticsInstance().sendAnalyticsEvents(GoogleAnalyticsUtil.getAnalyticsEvents().getInstaller(), GoogleAnalyticsUtil.getAnalyticsEvents().getLanguage(), System.getProperty("java.version"));
    		installationFile.delete();
    		
    	} 
	}

	@Override
	public void updateFilter(AnalysisFilter arg0) {
		notifyPropertyChangeListeners("filter", this.filter, arg0);
		this.filter = arg0;
		
	}

}
