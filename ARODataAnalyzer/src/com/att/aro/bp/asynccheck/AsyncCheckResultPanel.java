/*
 * Copyright 2013 AT&T
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

package com.att.aro.bp.asynccheck;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.att.aro.commonui.DataTable;
import com.att.aro.main.ApplicationResourceOptimizer;
//import com.att.aro.model.TextFileCompressionEntry;
import com.att.aro.util.Util;

/**
 * Represents the panel that has synchronous loading of scripts in HEAD
 */
public class AsyncCheckResultPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(AsyncCheckResultPanel.class.getName());

	private static final int ROW_HEIGHT = 20;
	private static final int NO_OF_ROWS = 6;
	private static final int SCROLL_PANE_HEIGHT = NO_OF_ROWS * ROW_HEIGHT;
	private static final int SCROLL_PANE_LENGHT = 300;

	private JLabel title;
	private JPanel contentPanel;
	private JScrollPane scrollPane;
	private AsyncCheckTableModel tableModel;
	private DataTable<AsyncCheckEntry> contentTable;

	/**
	 * Initializes a new instance of the AsyncCheckResultPanel class
	 * using the specified instance of the ApplicationResourceOptimizer as the
	 * parent window.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public AsyncCheckResultPanel() {
		LOGGER.fine("init. AsyncCheckResultPanel");
		initialize();
	}

	/**
	 * Sets the async loading of scripts result data.
	 * 
	 * @param data
	 *            - The data to be displayed in the result content table.
	 */
	public void setData(Collection<AsyncCheckEntry> data) {
		LOGGER.log(Level.FINE, "setData, size: {0}", new Object[] { data.size() });
		this.tableModel.setData(data);
	}

	/**
	 * Initializes the AsyncCheckResultPanel.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getContentPanel(), BorderLayout.CENTER);
		JPanel contentPanelWidth = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel contentPanelWidthAdjust = new JPanel(new GridBagLayout());
		contentPanelWidthAdjust.add(contentPanelWidth, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(
						5, 5, 5, 30), 0, 0));
		contentPanelWidthAdjust.setBackground(Color.WHITE);
		this.add(contentPanelWidthAdjust, BorderLayout.EAST);
	}

	/**
	 * Returns the title label.
	 */
	private JLabel getTitle() {
		if (title == null) {
			title = new JLabel(Util.RB.getString("html.asyncload.table.header"), JLabel.CENTER);
		}
		return title;
	}

	/**
	 * Initializes and returns the content panel.
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new JPanel(new BorderLayout());
			contentPanel.add(getTitle(), BorderLayout.NORTH);
			contentPanel.add(getScrollPane(), BorderLayout.CENTER);
		}
		return contentPanel;
	}

	/**
	 * Initializes and returns the Scroll Pane.
	 */
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane(getContentTable());
			scrollPane.setPreferredSize(new Dimension(SCROLL_PANE_LENGHT, SCROLL_PANE_HEIGHT));
		}
		return scrollPane;
	}

	/**
	 * Initializes and returns the content table.
	 */
	private DataTable<AsyncCheckEntry> getContentTable() {
		if (contentTable == null) {
			tableModel = new AsyncCheckTableModel();
			contentTable = new DataTable<AsyncCheckEntry>(tableModel);
			contentTable.setAutoCreateRowSorter(true);
			contentTable.setGridColor(Color.LIGHT_GRAY);
			contentTable.setRowHeight(ROW_HEIGHT);
			contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			contentTable.addMouseListener(getAsyncCheckTableMouseListener());
		}
		return contentTable;
	}

	private MouseListener getAsyncCheckTableMouseListener() {

		MouseListener ml;
		ml = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				AsyncCheckEntry entry = contentTable.getSelectedItem();
				if (e.getClickCount() == 2 && entry != null) {
					ApplicationResourceOptimizer aro = ApplicationResourceOptimizer.getAroFrame();
					aro.displayDiagnosticTab();
					aro.getAroAdvancedTab().setHighlightedRequestResponse(entry.getHttpRequestResponse());
				}
			}
		};
		
		return ml;
	}

}
