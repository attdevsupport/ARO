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

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.MessageDialogFactory;

/**
 * Provides the main entry point for the ARO Data Analyzer application.
 */
public class Launch {

	private static final Logger logger = Logger.getLogger(Launch.class
			.getName());
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final int indexOfTraceDirectoryName = 0;
	private static final int indexOfTraceDurationInMins = 1;
	
	/**
	 * The starting point for the ARO Data Analyzer. This method launches the
	 * application with the specified arguments.
	 * 
	 * @param args
	 *            Argument strings that are passed to the application at
	 *            startup.
	 */
	public static void main(String[] args) {
		
		//Handle command line parameters
		if (handleCommandLineParameters(args) == false) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				// Initialize application window
				AROUIManager.init();
				final ApplicationResourceOptimizer mainClass = new ApplicationResourceOptimizer();
				mainClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainClass.setVisible(true);
				CommandLineHandler.getInstance().setParent(mainClass);
				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						e.printStackTrace();
						if (e instanceof OutOfMemoryError) {
							logger.log(Level.SEVERE, "Out of memory error", e);
							MessageDialogFactory.showErrorDialog(mainClass,
									rb.getString("Error.outOfMemory"));
						} else {
							logger.log(Level.SEVERE, "Uncaught exception", e);
							MessageDialogFactory.showUnexpectedExceptionDialog(
									mainClass, e);
						}
					}

				});

				// Code to display About window from Mac menu
				if (System.getProperty("os.name").contains("Mac")) {
				    try {
				        Object app = Class.forName("com.apple.eawt.Application").getMethod("getApplication",
				         (Class[]) null).invoke(null, (Object[]) null);

				        Object al = Proxy.newProxyInstance(Class.forName("com.apple.eawt.AboutHandler")
				                .getClassLoader(), new Class[] { Class.forName("com.apple.eawt.AboutHandler") },
				                    new AboutListener());
				        app.getClass().getMethod("setAboutHandler", new Class[] {
				            Class.forName("com.apple.eawt.AboutHandler") }).invoke(app, new Object[] { al });
				    } catch (Exception e) {
				        //fail quietly
				    }
				}
				
				// Display splash
				final SplashScreen splash = new SplashScreen(mainClass);
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

					/**
					 * @see javax.swing.SwingWorker#done()
					 */
					@Override
					protected void done() {
						splash.dispose();

						if(CommandLineHandler.getInstance().IsCommandLineEvent() == true) {
							ApplicationResourceOptimizer parent = CommandLineHandler.getInstance().getParent();
							if (parent != null) {
		        				DatacollectorBridge aroDataCollectorBridge = new DatacollectorBridge(parent);
		        				aroDataCollectorBridge.startARODataCollector();
							}
	        			}
					}

				}.execute();

			}
		});		
	}

	private static class AboutListener implements InvocationHandler {

	    public Object invoke(Object proxy, Method method, Object[] args) {
	    	new AboutDialog(null).setVisible(true);
	        return null;
	    }
	}
	
	/**
	 * Handles command line parameters.
	 */	
	private static boolean handleCommandLineParameters(String[] args) {
		try {
			if(args != null) {
				int argsCount = args.length;
		        if (argsCount > 0) {
		        	
		        	//Update trace directory name
		        	CommandLineHandler.getInstance().InitializeTraceInfoFile();
		        	CommandLineHandler.getInstance().setTraceDirectoryName(args[indexOfTraceDirectoryName]);
		        			        	  
		        	//Update trace duration
        			try {
        				double traceDurationInMins = Double.parseDouble(args[indexOfTraceDurationInMins]);
        				CommandLineHandler.getInstance().setTraceDuration(traceDurationInMins);
        				CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.traceDurationInPropFile"), Double.toString(traceDurationInMins * 60));
        			} catch (NumberFormatException nfEx) {
        				CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.invalidDuration"));
        				CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
        				return false;
        			} catch (Exception ex) {
        				CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.missingDuration"));
        				CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
        				return false;
        			}
	        		
	        		CommandLineHandler.getInstance().SetCommandLineEvent(true);
		        }		
		    }
		} catch (Exception Ex) {
			CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), rb.getString("cmdline.ErrorCmdArgs"));
			CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
			return false;
		}
		
		return true;
	}
}
