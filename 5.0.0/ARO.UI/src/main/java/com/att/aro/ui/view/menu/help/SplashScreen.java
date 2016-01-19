package com.att.aro.ui.view.menu.help;

import java.awt.BorderLayout;
import java.awt.SystemColor;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class SplashScreen extends JWindow{
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;

	/**
	 * Initializes a new instance of the SplashScreen class using the specified
	 * instance of the ApplicationResourceOptimizer as the owner.
	 * 
	 * @param owner
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public SplashScreen() {
		super();
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
			jContentPane.add(new AboutPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
}
