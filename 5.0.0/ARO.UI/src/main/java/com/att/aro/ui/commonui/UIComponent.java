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
package com.att.aro.ui.commonui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.view.images.Images;

public class UIComponent extends JPanel {

	private static final long serialVersionUID = 1L;
	private static UIComponent instance;
	
	private Hashtable<String, ImageIcon> imageCache = new Hashtable<String, ImageIcon>();
	
	public UIComponent() {
	}

	public static UIComponent getInstance() {
		if (instance == null) {
			if (instance == null) {
				instance = new UIComponent();
			}
		}
		return instance;
	}

	/**
	 * Returns the blue header panel with the ATT logo.
	 */
	public ImagePanel getLogoHeader(String headerTitle) {
		ImagePanel	headerPanel = new ImagePanel(Images.BLUE_HEADER.getImage());
			headerPanel.setLayout(new BorderLayout(50, 50));
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JLabel label = new JLabel(Images.HEADER_ICON.getIcon(), SwingConstants.CENTER);
			label.setPreferredSize(new Dimension(80, 80));
			headerPanel.add(label, BorderLayout.WEST);

			JLabel bpHeaderLabel = new JLabel(ResourceBundleHelper.getMessageString(headerTitle));
			bpHeaderLabel.setFont(UIManager.getFont(AROUIManager.TITLE_FONT_KEY));
			bpHeaderLabel.setForeground(Color.WHITE);
			headerPanel.add(bpHeaderLabel, BorderLayout.CENTER);
	
		return headerPanel;
	}

	/**
	 * 
	 * @return
	 */
	public JPanel getSeparator() {
		return new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE);
	}
	
	/**
	 * Retrieves an Image from the jar file or from imageCache
	 * 
	 * @param rsrcKey The resource key found in images.properties
	 * @return ImageIcon of image
	 */
	public ImageIcon getIconByKey(String rsrcKey) {
		ImageIcon imgIcon = imageCache.get(rsrcKey);
		if (imgIcon == null) {
			String resourceName = ResourceBundleHelper.getImageString("ImageBasePath") + ResourceBundleHelper.getImageString(rsrcKey);
			imgIcon = new ImageIcon(getClass().getResource(resourceName));
			imageCache.put(rsrcKey, imgIcon);
		}
		return imgIcon;
	}


}