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
import java.awt.Insets;
import java.awt.event.MouseWheelListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.att.aro.commonui.ImagePanel;
import com.att.aro.commonui.RoundedBorder;
import com.att.aro.images.Images;
import com.att.aro.model.TraceData;

/**
 * Represents a panel of Detailed Best Practices results. The panel displays
 * results for a Best Practices category such as Caching, Connection or Others
 * depending on the panels parameter passed to the constructor.
 */
public class AROBpDetailedResultPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static Font textFont = new Font("TextFont", Font.PLAIN, 12);

	private static void removeMouseWheelListeners(JScrollPane scrollPane) {
		for (MouseWheelListener mwl : scrollPane.getMouseWheelListeners()) {
			scrollPane.removeMouseWheelListener(mwl);
		}
	}
	
	private DateTraceAppDetailPanel dateTraceAppDetailPanel;
	private BpHeaderPanel header;
	private Collection<DetailedResultRowPanel> panels;

	/**
	 * Initializes a new instance of the AROBpDetailedResultPanel class, using
	 * the specified title, description, and panel (which indicates the category
	 * of Best Practice).
	 * 
	 * @param title
	 *            - The header title for the detailed result section.
	 * 
	 * @param desc
	 *            - The description for the detailed result section.
	 * 
	 * @param panels
	 *            - The collection of panels that are displayed for the detailed
	 *            result section.
	 */
	public AROBpDetailedResultPanel(String title, String desc,
			Collection<DetailedResultRowPanel> panels) {
		super(new BorderLayout());
		this.panels = panels;
		setOpaque(false);
		setBorder(new RoundedBorder(new Insets(0, 0, 0, 0), Color.WHITE));

		// Create the header bar
		this.header = new BpHeaderPanel(title);
		add(header, BorderLayout.NORTH);

		// Create the date panel
		JPanel dataPanel = new JPanel(new BorderLayout());
		dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		dataPanel.setOpaque(false);
		this.dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		dataPanel.add(dateTraceAppDetailPanel, BorderLayout.NORTH);

		// Create the group overview panel
		JPanel textPanel = new JPanel(new BorderLayout(10, 10));
		textPanel.setOpaque(false);
		JScrollPane scroll = new JScrollPane(createJTextArea(desc));
		scroll.setBorder(BorderFactory.createEmptyBorder());
		removeMouseWheelListeners(scroll);
		textPanel.add(scroll, BorderLayout.NORTH);
		JPanel separator = new ImagePanel(Images.DIVIDER.getImage(), true,
				Color.WHITE);
		textPanel.add(separator, BorderLayout.SOUTH);

		// Create the best practices detail panel
		JPanel detailPanel = new JPanel(new GridBagLayout());
		detailPanel.setOpaque(false);
		int row = 0;
		Insets imageInsets = new Insets(25, 10, 10, 10);
		Insets startInsets = new Insets(25, 5, 2, 5);
		Insets insets = new Insets(2, 5, 2, 5);

		// Add each best practice to the detail panel
		for (DetailedResultRowPanel panel : panels) {
			detailPanel.add(panel.getIconLabel(), new GridBagConstraints(0,
					row, 1, 4, 0.0, 0.0, GridBagConstraints.NORTH,
					GridBagConstraints.NONE, imageInsets, 0, 0));

			detailPanel.add(panel.getTestLabel(), new GridBagConstraints(1,
					row, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, startInsets, 0, 0));
			detailPanel.add(panel.getTestNameLabel(), new GridBagConstraints(2,
					row, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.HORIZONTAL, startInsets, 0, 0));
			++row;
			detailPanel.add(panel.getAboutLabel(), new GridBagConstraints(1,
					row, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));
			scroll = new JScrollPane(panel.getAboutText());
			scroll.setBorder(BorderFactory.createEmptyBorder());
			removeMouseWheelListeners(scroll);
			detailPanel.add(scroll, new GridBagConstraints(2, row, 1, 1, 1.0,
					1.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.HORIZONTAL, insets, 0, 0));
			++row;
			detailPanel.add(panel.getResultLabel(), new GridBagConstraints(1,
					row, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.NONE, insets, 0, 0));
			scroll = new JScrollPane(panel.getResultDetailsLabel());
			scroll.setBorder(BorderFactory.createEmptyBorder());
			removeMouseWheelListeners(scroll);
			detailPanel.add(scroll, new GridBagConstraints(2, row, 1, 1, 1.0,
					1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
					insets, 0, 0));
			++row;
		}

		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.add(textPanel, BorderLayout.NORTH);
		panel.add(detailPanel, BorderLayout.CENTER);

		dataPanel.add(panel, BorderLayout.CENTER);

		this.add(dataPanel, BorderLayout.CENTER);
	}

	/**
	 * Refreshes the content of the Best Practices detailed results panel with
	 * the specified trace data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {

		dateTraceAppDetailPanel.refresh(analysisData);
		if (analysisData != null) {
			boolean pass = true;
			for (DetailedResultRowPanel panel : panels) {
				Boolean b = panel.refresh(analysisData);
				if (b != null) {
					pass = pass && b.booleanValue();
				}
			}
			header.setPass(pass);
		} else {
			for (DetailedResultRowPanel panel : panels) {
				panel.refresh(analysisData);
			}
			header.setPass(null);
		}
	}

	private JTextArea createJTextArea(String textToDisplay) {
		JTextArea jTextArea = new JTextArea(textToDisplay, 0, 0);
		jTextArea.setEditable(false);
		jTextArea.setFont(textFont);
		jTextArea.setWrapStyleWord(true);
		jTextArea.setLineWrap(true);
		// jTextArea.setPreferredSize(new Dimension(10, 10));
		return jTextArea;
	}
}
