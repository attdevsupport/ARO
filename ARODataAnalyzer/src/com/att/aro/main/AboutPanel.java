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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.att.aro.commonui.HyperlinkLabel;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.images.Images;

/**
 * Represents a panel for displaying information in an About dialog. This panel
 * is displayed twice in the application.
 */
public class AboutPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String MAJOR_VERSION_KEY = "build.majorversion";
	private static final String BUILD_VERSION_KEY = "build.timestamp";

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JPanel aboutPanel = null;

	/**
	 * Initializes a new instance of the AboutPanel class using the specified
	 * instance of the AboutDialog as the parent window for the panel.
	 * 
	 * @param parent
	 *            - An instance of an AboutDialog object that is the parent
	 *            window for the AboutPanel.
	 */
	public AboutPanel(Window parent) {
		super();
		initialize();
	}

	/**
	 * Initializes the AboutPanel.
	 * 
	 * @return void
	 */
	private void initialize() {

		JLabel aroLabel = new JLabel();
		aroLabel.setIcon(Images.AROLOGO.getIcon());
		this.setLayout(new BorderLayout());
		this.add(aroLabel, BorderLayout.NORTH);
		this.add(getAboutPanel(), BorderLayout.CENTER);
	}

	/**
	 * Initializes and returns the main Panel for the dialog .
	 * 
	 * @return javax.swing.JPanel The AboutPanel object.
	 */
	private JPanel getAboutPanel() {
		if (aboutPanel == null) {

			ResourceBundle buildBundle = ResourceBundleManager.getBuildBundle();
			JLabel buildLabel = new JLabel(MessageFormat.format(
					rb.getString("About.build"),
					buildBundle.getString(MAJOR_VERSION_KEY) + "."
							+ buildBundle.getString(BUILD_VERSION_KEY)),
					SwingConstants.CENTER);
			JLabel titleLabel = new JLabel(rb.getString("About.name"),
					SwingConstants.CENTER);
			JLabel brandLabel = new JLabel(Images.BRAND.getIcon());
			JLabel swLabel = new JLabel(rb.getString("About.built"),
					SwingConstants.CENTER);
			JPanel vPanel = new JPanel(new GridLayout(4, 1));
			vPanel.setBackground(Color.WHITE);
			vPanel.add(titleLabel);
			vPanel.add(buildLabel);
			vPanel.add(swLabel);
			vPanel.add(getJReleaseNotePanel());

			aboutPanel = new JPanel();
			Insets insets = new Insets(10, 0, 10, 0);
			Insets aboutInsets = new Insets(10, 3, 0, 5);
			aboutPanel.setLayout(new GridBagLayout());
			aboutPanel.setBackground(Color.WHITE);
			aboutPanel.add(brandLabel, new GridBagConstraints(0, 0, 5, 5, 0.0,
					0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					insets, 0, 0));
			aboutPanel.add(vPanel, new GridBagConstraints(5, 0, 0, 0, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.NONE,
					aboutInsets, 0, 0));
		}
		return aboutPanel;
	}

	/**
	 * Creates the release note click-able label on the about dialog
	 */
	private JLabel getJReleaseNotePanel() {
		HyperlinkLabel releaseNoteLabel = new HyperlinkLabel(
				rb.getString("About.releaseNotesUrl"), SwingConstants.CENTER);
		Font font = releaseNoteLabel.getFont();
		Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		font = font.deriveFont(map);
		releaseNoteLabel.setFont(font);
		releaseNoteLabel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					BrowserGenerator.openBrowser(rb
							.getString("About.releaseNotesUrl"));
				} catch (IOException ex) {
					MessageDialogFactory.showUnexpectedExceptionDialog(
							AboutPanel.this, ex);
				}
			}
		});
		return releaseNoteLabel;
	}

}
