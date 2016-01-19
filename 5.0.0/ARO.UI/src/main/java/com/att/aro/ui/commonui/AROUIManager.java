/*
 *  Copyright 2012 AT&T
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
package com.att.aro.ui.commonui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * An extension of the UIManager class that contains Fields which define the
 * look and feel of the ARO Data Analyzer application.
 */
public class AROUIManager extends UIManager {

	private static final long serialVersionUID = 1L;

	/**
	 * The Key that is used to map the title font style in the underlying
	 * UIManager class.
	 */
	public static final String TITLE_FONT_KEY = "TITLEFont";

	/**
	 * The Key that is used to map the label font style in the underlying
	 * UIManager class.
	 */
	public static final String LABEL_FONT_KEY = "Label.font";

	/**
	 * The Key that is used to map the background Color in the underlying
	 * UIManager class.
	 */
	public static final String PAGE_BACKGROUND_KEY = "page.background";

	/** The Font style used for headers. */
	public static final Font HEADER_FONT = new Font("Arial Narrow", Font.BOLD, 20);

	/** The Font style used for ARO Data Analyzer labels. */
	public static Font LABEL_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

	/** The color used in the ARO Data Analyzer chart fields. */
	public static final Color CHART_BAR_COLOR = new Color(64, 164, 223);

	static {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0) {
			LABEL_FONT = new Font("Arial Narrow", Font.BOLD, 12);
		}
		try {
			// sets the look and feel of the ARO executing environment
			setLookAndFeel(getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the AROUIManager with the styles that form the default the
	 * look and feel of the ARO Data Analyzer.
	 */
	public static void init() {
		put(TITLE_FONT_KEY, new Font(TITLE_FONT_KEY, Font.BOLD, 18));
		put(PAGE_BACKGROUND_KEY, Color.WHITE);
		put(LABEL_FONT_KEY, LABEL_FONT);

		/* Tool tip visibility delay configured. */
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setDismissDelay(8000);
	}
}
