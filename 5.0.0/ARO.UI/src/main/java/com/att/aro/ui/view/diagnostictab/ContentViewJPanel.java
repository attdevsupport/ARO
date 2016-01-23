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
package com.att.aro.ui.view.diagnostictab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.model.diagnostic.SearchHelper;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class ContentViewJPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);

	// Content view
	private JScrollPane jContentViewScrollPane; // Content View
	private JPanel buttonsPanel; // Content View
	private JTextArea jContentTextArea; // session content  text

	private JTextField besearchedText;//small test area
	private JTextArea searchableTextArea;

	private JButton searchNext;
	private static final int TXT_FIELD_SIZE = 10;
 	
	
	private Session session;
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public ContentViewJPanel(){
		setLayout(new BorderLayout());
		add(getJContentViewScrollPane(), BorderLayout.CENTER);
		add(getButtonsPanel(), BorderLayout.EAST);
	}
	
	/**
	 * Initializes and returns the Scroll Pane for the Content View tab at the
	 * bottom.
	 */
	public JScrollPane getJContentViewScrollPane() {
		if (jContentViewScrollPane == null) {
 			jContentViewScrollPane = new JScrollPane(getJContentTextArea());
 			jContentViewScrollPane.setPreferredSize(new Dimension(100, 160));			
			setSearchableTextArea(jContentTextArea);
		}
		return jContentViewScrollPane;
	}
	
	public JTextArea getJContentTextArea(){
		if (jContentTextArea == null) {
			jContentTextArea = new JTextArea(10, 20);
			jContentTextArea.setLineWrap(true);
			jContentTextArea.setCaretPosition(0);
		}
		return jContentTextArea;
	}
	
	/**
	 * Initializes and returns the JPanel that contains the Search.
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel(new GridBagLayout());
			JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
			panel.add(getSearch());
			panel.add(getSearchNextButton());

			buttonsPanel.add(panel,
					new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
							GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
//			enableSearch();
		}
		return buttonsPanel;
	}
	
	/**
	 * Sets specific searchable text area, overriding default HttpRequestResponse search.
	 */
	void setSearchableTextArea(JTextArea textArea) {
		searchableTextArea = textArea;
	}
	
	JTextArea getSearchableTextArea(){
		return searchableTextArea;
	}
	/**
	 * Initializes and returns the search element for the Request-Response Table.
	 */
	public JTextField getSearch() {
		if (besearchedText == null) {
			besearchedText = new SearchTextField(this, getSearchableTextArea());
			besearchedText.setEnabled(false);
 		}
		return besearchedText;
	}

	/**
	 * Initializes and returns the search next button for the Request-Response Table.
	 */
	JButton getSearchNextButton() {
		if (searchNext == null) {
			searchNext = new JButton(ResourceBundleHelper.getMessageString("element.search.next"));
			searchNext.setEnabled(false);
			searchNext.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg) {
					((SearchTextField) besearchedText).doNextSearch();
				}
			});
 		}
		return searchNext;
	}

	/** 
	 * Enables search feature.
	 */
	public void enableSearch() {
		besearchedText.setEnabled(true);
	}

	public void updateContext(Session session){
		setSession(session);
 		getJContentTextArea().setText(session.getDataText());
 		enableSearch();
	}
 
}
