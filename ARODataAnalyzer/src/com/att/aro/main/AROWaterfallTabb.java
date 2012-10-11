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
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.ImagePanel;
import com.att.aro.images.Images;
import com.att.aro.model.TraceData;

/**
 *
 */
public class AROWaterfallTabb extends JPanel {
	private static final long serialVersionUID = 1L;

	public static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private ApplicationResourceOptimizer parent;

	private ImagePanel headerPanel;
	private WaterFallPanel waterFallPanel;

	/**
	 * Initializes a new instance of the AROWaterfallTabb class.
	 */
	public AROWaterfallTabb(ApplicationResourceOptimizer parent) {
		this.parent = parent;
		this.setLayout(new BorderLayout());
		this.add(getHeaderPanel(), BorderLayout.NORTH);
		this.add(getWaterFallPanel(), BorderLayout.CENTER);

	}

	protected void refresh(TraceData.Analysis analysisData) {
		getWaterFallPanel().refresh(analysisData);
	}


	private WaterFallPanel getWaterFallPanel() {
		if( waterFallPanel == null){
			waterFallPanel = new WaterFallPanel(parent);
		}
		return waterFallPanel;
	}

	/**
	 * Returns the blue header panel with the ATT logo.
	 */
	private ImagePanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new ImagePanel(Images.BLUE_HEADER.getImage());
			headerPanel.setLayout(new BorderLayout(50, 50));
			headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10,
					10));
			JLabel l = new JLabel(Images.HEADER_ICON.getIcon(),
					SwingConstants.CENTER);
			l.setPreferredSize(new Dimension(80, 80));
			headerPanel.add(l, BorderLayout.WEST);

			JLabel bpHeaderLabel = new JLabel(rb.getString("Waterfall.title"));
			bpHeaderLabel.setFont(UIManager
					.getFont(AROUIManager.TITLE_FONT_KEY));
			bpHeaderLabel.setForeground(Color.WHITE);
			headerPanel.add(bpHeaderLabel, BorderLayout.CENTER);
		}
		return headerPanel;
	}

}

