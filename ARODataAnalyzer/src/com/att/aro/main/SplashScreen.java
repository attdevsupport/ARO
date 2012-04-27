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

import java.awt.BorderLayout;
import java.awt.SystemColor;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JWindow;

/**
 * Represents the splash screen that is displayed when the application begins.
 */
public class SplashScreen extends JWindow {
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	/**
	 * Initializes a new instance of the SplashScreen class using the specified
	 * instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public SplashScreen(Window owner) {
		super(owner);
		initialize();
	}

	/**
	 * Initializes the Splash Screen Window.
	 */
	private void initialize() {

		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(getOwner());
		this.setVisible(true);
		this.pack();
	}

	/**
	 * Initializes jContentPane
	 * 
	 * @return javax.swing.JPanel The Panel that holds the splash screen
	 *         content.
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel(new BorderLayout());
			jContentPane.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory
							.createLineBorder(SystemColor.controlShadow, 2),
					null));
			jContentPane.add(new AboutPanel(this), BorderLayout.CENTER);
		}
		return jContentPane;
	}

}
