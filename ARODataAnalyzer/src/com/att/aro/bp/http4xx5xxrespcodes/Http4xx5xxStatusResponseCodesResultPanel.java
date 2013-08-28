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
package com.att.aro.bp.http4xx5xxrespcodes;

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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.logging.Level;
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

public class Http4xx5xxStatusResponseCodesResultPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger
	.getLogger(Http4xx5xxStatusResponseCodesResultPanel.class.getName());

	private static final int ROW_HEIGHT = 20;
	private static final int NO_OF_ROWS = 6;
	private static final int SCROLL_PANE_HEIGHT = Http4xx5xxStatusResponseCodesResultPanel.NO_OF_ROWS * Http4xx5xxStatusResponseCodesResultPanel.ROW_HEIGHT;
	private static final int SCROLL_PANE_LENGHT = 300;
	private boolean isExpanded = false;
	private int noOfRecords = 0;
	
	private JLabel title;
	private JPanel contentPanel;
	private JScrollPane scrollPane;
	private BestPracticeButtonPanel bpButtonPanel;

	private Http4xx5xxStatusResponseCodesTableModel tableModel;
	private DataTable<Http4xx5xxStatusResponseCodesEntry> contentTable;
	
	/**
	* Initializes a new instance of the Http4xx5xxStatusResponseCodesResultPanel class 
	*/
	public Http4xx5xxStatusResponseCodesResultPanel() {
		LOGGER.fine("init. http_400_500_status_response_codes_ResultPanel");
		initialize();
	}
	

	
	/**
	* Initializes the http4xx5xxStatusResponseCodesResultPanel.
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
	* Sets the 400,500 HTTP Status Response code data.
	* 
	* @param data
	*            - The data to be displayed in the result content table.
	*/
	public void setData(Collection<Http4xx5xxStatusResponseCodesEntry> data) {
		LOGGER.log(Level.FINE, "setData, size: {0}",
			new Object[] { data.size() });
		this.tableModel.setData(data);
		if(bpButtonPanel!=null)
		bpButtonPanel.setNoOfRecords(noOfRecords);
	}	
	/**
	* Returns the title label.
	*/
	private JLabel getTitle() {
		if (title == null) {
			title = new JLabel(
					Util.RB.getString("connections.http4xx5xx.table.header"),
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
	 * Restores the table.
	 */
	public void restoreTable() {
		 JButton viewBtn=((BestPracticeButtonPanel)getButtonsPanel()).getViewBtn();
		if(this.noOfRecords > 5) {
			
			((BestPracticeButtonPanel)getButtonsPanel()).setExpanded(false);
			viewBtn.setEnabled(true);
			viewBtn.setText("+");			
		} else {
			viewBtn.setEnabled(false);
		}
		this.scrollPane.setPreferredSize(new Dimension(Http4xx5xxStatusResponseCodesResultPanel.SCROLL_PANE_LENGHT, Http4xx5xxStatusResponseCodesResultPanel.SCROLL_PANE_HEIGHT));
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
	private DataTable<Http4xx5xxStatusResponseCodesEntry> getContentTable() {
		if (this.contentTable == null) {
			this.tableModel = new Http4xx5xxStatusResponseCodesTableModel();
			this.contentTable = new DataTable<Http4xx5xxStatusResponseCodesEntry>(tableModel);
			this.contentTable.setAutoCreateRowSorter(true);
			this.contentTable.setGridColor(Color.LIGHT_GRAY);
			this.contentTable.setRowHeight(Http4xx5xxStatusResponseCodesResultPanel.ROW_HEIGHT);
			this.contentTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			this.contentTable.addMouseListener(getHTTP4xx5xxStatusCodesTableMouseListener());
		}
		return this.contentTable;
	}
	
	/**
	 * Returns mouse listener.
	 * 
	 * @return mouse listener
	 */
	private MouseListener getHTTP4xx5xxStatusCodesTableMouseListener() {
	
		MouseListener ml;
		ml = new MouseAdapter() {
		
			public void mouseClicked(MouseEvent e) {
				Http4xx5xxStatusResponseCodesEntry entry = contentTable.getSelectedItem();
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
