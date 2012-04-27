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

	/**
	 * The starting point for the ARO Data Analyzer This. method launches the
	 * application with the specified arguments.
	 * 
	 * @param args
	 *            – Argument strings that are passed to the application at
	 *            startup.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				// Initialize application window
				AROUIManager.init();
				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						e.printStackTrace();
						if (e instanceof OutOfMemoryError) {
							logger.log(Level.SEVERE, "Out of memory error", e);
							MessageDialogFactory.showErrorDialog(null,
									rb.getString("Error.outOfMemory"));
						} else {
							logger.log(Level.SEVERE, "Uncaught exception", e);
							MessageDialogFactory.showUnexpectedExceptionDialog(
									null, e);
						}
					}

				});
				ApplicationResourceOptimizer mainClass = new ApplicationResourceOptimizer();
				mainClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainClass.setVisible(true);

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
					}

				}.execute();

			}
		});
	}

}
