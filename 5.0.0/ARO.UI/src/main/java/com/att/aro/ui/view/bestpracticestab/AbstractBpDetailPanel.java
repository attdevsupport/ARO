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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.att.aro.ui.commonui.BrowserGenerator;
import com.att.aro.ui.commonui.ImagePanel;
import com.att.aro.ui.commonui.TabPanelJScrollPane;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.statistics.DateTraceAppDetailPanel;
import com.att.aro.view.images.Images;

public abstract class AbstractBpDetailPanel extends TabPanelJScrollPane{

	private static final long serialVersionUID = 1L;
	
	private static final Font TEXTFONT = new Font("TextFont", Font.PLAIN, 12);
	private static final int TEXT_WIDTH = 550;

	DateTraceAppDetailPanel dateTraceAppDetailPanel;

	JPanel fullPanel;
	JPanel detailPane;
	
	Insets imageInsets = new Insets(25, 10, 10, 10);
	Insets startInsets = new Insets(25, 5, 2, 5);
	Insets insets = new Insets(2, 5, 2, 5);
	
	BpHeaderPanel header;
	
	public AbstractBpDetailPanel(String title) {
		super();
		initialize(title);
	//	add(fullPanel, BorderLayout.CENTER);
	}
		
	public void initialize(String title) {
		
		fullPanel = new JPanel(new BorderLayout());
		fullPanel.setOpaque(false);
	//	setBorder(new RoundedBorder(new Insets(0, 0, 0, 0), Color.WHITE)); // bcn
		
		// Create the header bar
		header = new BpHeaderPanel(ResourceBundleHelper.getMessageString(title));
		String desc = ResourceBundleHelper.getMessageString(title+"Description");
		fullPanel.add(header, BorderLayout.NORTH);
		
		// Create the data panel
		JPanel dataPanel = new JPanel(new BorderLayout());
		dataPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		dataPanel.setOpaque(false);
		dateTraceAppDetailPanel = new DateTraceAppDetailPanel();
		dataPanel.add(dateTraceAppDetailPanel, BorderLayout.NORTH);
		
		// Create the group overview panel
		JPanel textPanel = new JPanel(new BorderLayout(10, 10));
		textPanel.setOpaque(false);
		JScrollPane scroll = new JScrollPane(createJTextArea(desc, null));
	//	scroll.setBackground(Color.RED);	// bcn
		scroll.setBorder(BorderFactory.createEmptyBorder());
		removeMouseWheelListeners(scroll);
		textPanel.add(scroll, BorderLayout.CENTER);
		JPanel separator = new ImagePanel(Images.DIVIDER.getImage(), true, Color.WHITE);
		textPanel.add(separator, BorderLayout.SOUTH);

		// Create the best practices detail panel
		JPanel detailPanel = new JPanel(new GridBagLayout());
		detailPanel.setOpaque(true);

		detailPanel = new JPanel();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.add(textPanel, BorderLayout.NORTH);
		panel.add(detailPanel, BorderLayout.CENTER);

		dataPanel.add(panel);
		fullPanel.add(dataPanel);
	}
	private JTextPane createJTextArea(String textToDisplay, final URI url) {
		HTMLDocument doc = new HTMLDocument();
		StyleSheet style = doc.getStyleSheet();
		style.addRule("body { font-family: " + TEXTFONT.getFamily() + "; "
				+ "font-size: " + TEXTFONT.getSize() + "pt; }");
		style.addRule("a { text-decoration: underline; font-weight:bold; }");
		JTextPane jTextArea = new JTextPane(doc);//, 0, 0);
		jTextArea.setEditable(false);
		jTextArea.setEditorKit(new HTMLEditorKit());
		jTextArea.setStyledDocument(doc);
		jTextArea.setMargin(new Insets(0, 0, 0, 0));

		if (url != null) {
			jTextArea.setText(textToDisplay + "&nbsp;" + " <a href=\"#\">"
					+ ResourceBundleHelper.getMessageString("bestPractices.learnMore") + "</a>");
			jTextArea.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {

					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						try {
							BrowserGenerator.openBrowser(url);
						} catch (IOException e1) {
//							MessageDialogFactory.showUnexpectedExceptionDialog(, e1);
						}
					}
				}

			});
		} else {
			jTextArea.setText(textToDisplay);
		}
		
		// Calculate preferred size
		jTextArea.setSize(TEXT_WIDTH, 9999);
		Dimension d = jTextArea.getPreferredSize();
		d.width = TEXT_WIDTH;
		jTextArea.setPreferredSize(d);
		jTextArea.setMinimumSize(d);

		
		
		return jTextArea;
	}
	
	private static void removeMouseWheelListeners(JScrollPane scrollPane) {
		for (MouseWheelListener mwl : scrollPane.getMouseWheelListeners()) {
			scrollPane.removeMouseWheelListener(mwl);
		}
	}


}
