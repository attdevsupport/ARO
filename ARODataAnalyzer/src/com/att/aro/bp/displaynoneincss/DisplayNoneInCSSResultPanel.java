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
package com.att.aro.bp.displaynoneincss;

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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.att.aro.bp.BestPracticeButtonPanel;
import com.att.aro.bp.DataTableResultPanel;
import com.att.aro.bp.displaynoneincss.DisplayNoneInCSSEntry;
import com.att.aro.bp.displaynoneincss.DisplayNoneInCSSTableModel;
import com.att.aro.commonui.DataTable;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.util.Util;

/**
 * Represents the panel that has the information about the html/css files which
 * contains display:none CSS rule.
 */
public class DisplayNoneInCSSResultPanel extends JPanel implements DataTableResultPanel {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger
			.getLogger(DisplayNoneInCSSResultPanel.class.getName());

	private static final int ROW_HEIGHT = 20;
	private static final int NO_OF_ROWS = 6;
	private static final int SCROLL_PANE_HEIGHT = DisplayNoneInCSSResultPanel.NO_OF_ROWS
			* DisplayNoneInCSSResultPanel.ROW_HEIGHT;
	private static final int SCROLL_PANE_LENGHT = 300;
	private int noOfRecords = 0;

	private JLabel title;
	private JPanel contentPanel;
	private JScrollPane scrollPane;
	private BestPracticeButtonPanel bpButtonPanel;
	private DisplayNoneInCSSTableModel tableModel;
	private DataTable<DisplayNoneInCSSEntry> contentTable;

	/**
	 * Initializes a new instance of the DisplayNoneInCSSResultPanel class using
	 * the specified instance of the ApplicationResourceOptimizer as the parent
	 * window.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public DisplayNoneInCSSResultPanel() {
		LOGGER.fine("init. DisplayNoneInCSSResultPanel");
		initialize();
	}

	/**
	 * Sets the Display:none result data.
	 * 
	 * @param data
	 *            - The data to be displayed in the result content table.
	 */
	public void setData(Collection<DisplayNoneInCSSEntry> data) {
		LOGGER.log(Level.FINE, "setData, size: {0}",
				new Object[] { data.size() });
		this.tableModel.setData(data);
		noOfRecords = data.size();
		if (bpButtonPanel != null) {
			bpButtonPanel.setNoOfRecords(noOfRecords);
		}
	}

	/**
	 * Initializes the DuplicateResultPanel.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getContentPanel(), BorderLayout.CENTER);
		JPanel contentPanelWidth = new JPanel(new GridLayout(2, 1, 5, 5));
		JPanel contentPanelWidthAdjust = new JPanel(new GridBagLayout());
		contentPanelWidthAdjust.add(contentPanelWidth, new GridBagConstraints(
				0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 30), 0, 0));
		contentPanelWidthAdjust.setBackground(Color.WHITE);
		this.add(contentPanelWidthAdjust, BorderLayout.EAST);
	}

	/**
	 * Returns the title label.
	 */
	private JLabel getTitle() {
		if (title == null) {
			title = new JLabel(
					Util.RB.getString("html.displaynoneincss.table.header"),
					JLabel.CENTER);
		}
		return title;
	}

	/**
	 * Initializes and returns the content panel.
	 */
	private JPanel getContentPanel() {
		if (this.contentPanel == null) {
			this.contentPanel = new JPanel(new BorderLayout());
			this.contentPanel.add(getScrollPane(), BorderLayout.CENTER);
			this.contentPanel.add(getButtonsPanel(), BorderLayout.EAST);
		}
		return this.contentPanel;
	}

	/**
	 * Initializes and returns the JPanel that contains the button.
	 */
	private JPanel getButtonsPanel() {
		if (this.bpButtonPanel == null) {

			bpButtonPanel = new BestPracticeButtonPanel();
			bpButtonPanel.setScrollPane(getScrollPane());
			bpButtonPanel.setNoOfRecords(noOfRecords);

		}
		return this.bpButtonPanel;
	}

	/**
	 * Restores the table.
	 */
	public void restoreTable() {
		JButton viewBtn = ((BestPracticeButtonPanel) getButtonsPanel())
				.getViewBtn();
		if (this.noOfRecords > 5) {

			((BestPracticeButtonPanel) getButtonsPanel()).setExpanded(false);
			viewBtn.setEnabled(true);
			viewBtn.setText("+");
		} else {
			viewBtn.setEnabled(false);
		}
		this.scrollPane.setPreferredSize(new Dimension(
				DisplayNoneInCSSResultPanel.SCROLL_PANE_LENGHT,
				DisplayNoneInCSSResultPanel.SCROLL_PANE_HEIGHT));
	}

	/**
	 * Initializes and returns the Scroll Pane.
	 */
	private JScrollPane getScrollPane() {
		if (this.scrollPane == null) {
			this.scrollPane = new JScrollPane(getContentTable());
		}
		return this.scrollPane;
	}

	/**
	 * Initializes and returns the content table.
	 */
	private DataTable<DisplayNoneInCSSEntry> getContentTable() {
		if (contentTable == null) {
			tableModel = new DisplayNoneInCSSTableModel();
			contentTable = new DataTable<DisplayNoneInCSSEntry>(tableModel);
			contentTable.setAutoCreateRowSorter(true);
			contentTable.setGridColor(Color.LIGHT_GRAY);
			contentTable.setRowHeight(ROW_HEIGHT);
			contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			contentTable
					.addMouseListener(getDisplayNoneInCSSTableMouseListener());
		}
		return contentTable;
	}

	private MouseListener getDisplayNoneInCSSTableMouseListener() {

		MouseListener ml;
		ml = new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				DisplayNoneInCSSEntry entry = contentTable.getSelectedItem();
				if (e.getClickCount() == 2 && entry != null) {
					ApplicationResourceOptimizer aro = ApplicationResourceOptimizer
							.getAroFrame();
					aro.displayDiagnosticTab();
					aro.getAroAdvancedTab().setHighlightedRequestResponse(
							entry.getHttpRequestResponse());
				}
			}
		};

		return ml;
	}

	/**
	 * Sets number of records in table.
	 */
	public void setNoOfRecords(int noOfRecords) {
		this.noOfRecords = noOfRecords;
		restoreTable();
	}

	/**
	 * Updates the table.
	 */
	public void updateTableForPrint() {
		// Check if already expanded, do nothing.
		BestPracticeButtonPanel bpanel=	((BestPracticeButtonPanel) getButtonsPanel());
		JButton viewBtn = bpanel.getViewBtn();
		if (!bpanel.isExpanded() && this.noOfRecords > 5) {
			
			viewBtn.doClick();
			this.scrollPane.revalidate();
		}
	}
	
	/**
	 * Clears data from the result table.
	 */
	public void clearTable() {
		LOGGER.log(Level.FINE, "clear none in css data");
		this.tableModel.removeAllRows();
	}
	

}
