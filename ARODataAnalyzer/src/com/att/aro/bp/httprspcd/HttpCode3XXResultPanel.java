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
package com.att.aro.bp.httprspcd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.att.aro.bp.BestPracticeButtonPanel;
import com.att.aro.commonui.DataTable;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.util.Util;

public class HttpCode3XXResultPanel extends JPanel{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(HttpCode3XXResultPanel.class.getName());

	private static final int ROW_HEIGHT = 20;
	private static final int NO_OF_ROWS = 6;
	private static final int SCROLL_PANE_HEIGHT = HttpCode3XXResultPanel.NO_OF_ROWS * HttpCode3XXResultPanel.ROW_HEIGHT;
	private static final int SCROLL_PANE_LENGTH = 300;
	private boolean isExpanded = false;
	private int noOfRecords = 0;
	
	private JLabel title;
	private JPanel contentPanel;
	private JScrollPane scrollPane;
//	private JButton viewBtn;
//	private JPanel buttonPanel;
	private HttpCode3XXTableModel tableModel;
	private DataTable<HttpCode3XXEntry> contentTable;
	private BestPracticeButtonPanel bpButtonPanel;

	/**
	 * Initializes a new instance of the HttpCode3XXResultPanel class.
	 * 
	 */
	public HttpCode3XXResultPanel() {
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
	 * 
	 * @return JLabel title 
	 */
	private JLabel getTitle() {
		if (this.title == null) {
			this.title = new JLabel(Util.RB.getString("connections.http3xx.table.header"), SwingConstants.CENTER);
		}
		return this.title;
	}

	/**
	 * Initializes and returns the content panel.
	 * 
	 * @return JPanel content panel
	 */
	private JPanel getContentPanel() {
		if (this.contentPanel == null) {
			this.contentPanel = new JPanel(new BorderLayout());
			this.contentPanel.add(getTitle(), BorderLayout.NORTH);
			this.contentPanel.add(getScrollPane(), BorderLayout.CENTER);
			this.contentPanel.add(getButtonsPanel(), BorderLayout.EAST);
		}
		return this.contentPanel;
	}
	
	/**
	 * Initializes and returns the JPanel that contains the 
	 * button.
	 */
	private JPanel getButtonsPanel() {
			if (this.bpButtonPanel == null) {
			
			bpButtonPanel=new BestPracticeButtonPanel();
			bpButtonPanel.setScrollPane(getScrollPane());
			bpButtonPanel.setNoOfRecords(noOfRecords);
		
	    	}
		return this.bpButtonPanel;
	}
	
	/**
	 * Initializes and returns the button.
	 */
	/*private JButton getViewBtn() {
		if (this.viewBtn == null) {
			this.viewBtn = new JButton("+");
			this.viewBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					updateTable();
				}
			});
		}
		return this.viewBtn;
	}
	
	/**
	 * Updates the table.
	 */
/*	private void updateTable() {
		if(this.isExpanded) {
			this.isExpanded = false;
			this.viewBtn.setText("+");
			this.scrollPane.setPreferredSize(new Dimension(DuplicateResultPanel.SCROLL_PANE_LENGHT, DuplicateResultPanel.SCROLL_PANE_HEIGHT));
		} else {
			this.isExpanded = true;
			this.viewBtn.setText("-");
			this.scrollPane.setPreferredSize(new Dimension(DuplicateResultPanel.SCROLL_PANE_LENGHT, DuplicateResultPanel.ROW_HEIGHT * (noOfRecords + 1)));
		}
		this.scrollPane.revalidate();
	}
	*/
	/**
	 * Restores the table.
	 */
	public void restoreTable() {
		 JButton viewBtn=((BestPracticeButtonPanel)getButtonsPanel()).getViewBtn();
		if(this.noOfRecords > 5) {
			
			((BestPracticeButtonPanel)getButtonsPanel()).setExpanded(false);
			//this.isExpanded = false;
			viewBtn.setEnabled(true);
			viewBtn.setText("+");			
		} else {
			viewBtn.setEnabled(false);
		}
		this.scrollPane.setPreferredSize(new Dimension(HttpCode3XXResultPanel.SCROLL_PANE_LENGTH, HttpCode3XXResultPanel.SCROLL_PANE_HEIGHT));
	}
	
	/**
	 * Initializes and returns the Scroll Pane.
	 * 
	 * @return JScrollPane scroll pane
	 */
	private JScrollPane getScrollPane() {
		if (this.scrollPane == null) {
			this.scrollPane = new JScrollPane(getContentTable());
		}
		return this.scrollPane;
	}

	/**
	 * Initializes and returns the content table.
	 * 
	 * @return DataTable content table
	 */
	private DataTable<HttpCode3XXEntry> getContentTable() {
		if (this.contentTable == null) {
			this.tableModel = new HttpCode3XXTableModel();
			this.contentTable = new DataTable<HttpCode3XXEntry>(this.tableModel);
			this.contentTable.setAutoCreateRowSorter(true);
			this.contentTable.setGridColor(Color.LIGHT_GRAY);
			this.contentTable.setRowHeight(HttpCode3XXResultPanel.ROW_HEIGHT);
			this.contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			this.contentTable.addMouseListener(getHttpRspCdTableMouseListener());
		}
		return this.contentTable;
	}

	/**
	 * Sets the http response code best practice result data.
	 * 
	 * @param data
	 *            the data to be displayed in the result content table
	 */
	public void setData(Collection<HttpCode3XXEntry> data) {
		this.tableModel.setData(data);
	//	noOfRecords=data.size();
		if(bpButtonPanel!=null)
			bpButtonPanel.setNoOfRecords(noOfRecords);
	}

	/**
	 * Returns mouse listener.
	 * 
	 * @return mouse listener
	 */
	private MouseListener getHttpRspCdTableMouseListener() {

		MouseListener ml = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				HttpCode3XXEntry entry = HttpCode3XXResultPanel.this.contentTable.getSelectedItem();
				if ((e.getClickCount() == 2) && (entry != null)) {
					ApplicationResourceOptimizer aro = ApplicationResourceOptimizer.getAroFrame();
					aro.displayDiagnosticTab();
					aro.getAroAdvancedTab().setHighlightedRequestResponse(entry.getHttpRequestResponse());
				}
			}
		};

		return ml;
	}

	/**
	 * Sets number of records in table.
	 */
	public void setNumberOfRecords(int noOfRecords) {
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
}
