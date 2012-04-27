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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.commonui.DataTable;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;

/**
 * Represents the Panel that displays the Http request/response details table in
 * the Request/Response View section of the Diagnostic tab.
 */
public class RequestResponseDetailsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private JScrollPane scrollPane;
	private RequestResponseTableModel jRequestResponseTableModel = new RequestResponseTableModel();
	private DataTable<HttpRequestResponseInfo> jRequestResponseTable;
	private JPanel buttonsPanel;
	private JButton viewBtn;
	private JButton saveBtn;

	/**
	 * The default constructor that initializes a new instance of the
	 * RequestResponseDetailsPanel class.
	 */
	public RequestResponseDetailsPanel() {
		initialize();
	}

	/**
	 * Marks the specified HttpRequestResponseInfo object in the
	 * request/response list as selected, by highlighting it.
	 * 
	 * @param rr
	 *            - The Http request/response object to be marked as selected.
	 */
	public void setHighlightedRequestResponse(HttpRequestResponseInfo rr) {
		getJRequestResponseTable().selectItem(rr);
	}

	/**
	 * Sets the HttpRequestResponseInfo data to be displayed on this panel.
	 * 
	 * @param data
	 *            – A collection of HttpRequestResponseInfo objects obtained
	 *            from the trace analysis. Each HttpRequestResponseInfo object
	 *            in the collection represents one row of data in the panel.
	 */
	public void setData(Collection<HttpRequestResponseInfo> data) {
		jRequestResponseTableModel.setData(data);
	}

	/**
	 * Convenience method to select the specified row in the table.
	 * 
	 * @param rrInfo
	 *            -– An HttpRequestResponseInfo object that indicates the
	 *            specified row.
	 */
	public void select(HttpRequestResponseInfo rrInfo) {
		jRequestResponseTable.selectItem(rrInfo);
	}

	/**
	 * Initializes the layout for the RequestResponse table.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getScrollPane(), BorderLayout.CENTER);
		this.add(getButtonsPanel(), BorderLayout.EAST);
	}

	/**
	 * Returns the ScrollPane that contains the RequestResponse table.
	 */
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane(getJRequestResponseTable());
		}
		return scrollPane;
	}

	/**
	 * Initializes and returns the the DataTable that contains Http request and
	 * response informations.
	 */
	private DataTable<HttpRequestResponseInfo> getJRequestResponseTable() {
		if (jRequestResponseTable == null) {
			jRequestResponseTable = new DataTable<HttpRequestResponseInfo>(
					jRequestResponseTableModel);
			jRequestResponseTable.setAutoCreateRowSorter(true);
			jRequestResponseTable.setGridColor(Color.LIGHT_GRAY);
			jRequestResponseTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {

							// Enable view and save as buttons appropriately
							HttpRequestResponseInfo httpRRInfo = jRequestResponseTable
									.getSelectedItem();
							boolean enabled = httpRRInfo != null
									&& httpRRInfo.getContentLength() > 0
									&& httpRRInfo.getDirection() == Direction.RESPONSE
									&& httpRRInfo.getStatusCode() != 0;
							getViewBtn().setEnabled(enabled);
							getSaveBtn().setEnabled(enabled);
						}
					});
			jRequestResponseTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2 && getViewBtn().isEnabled()) {
						try {
							ContentViewer.getInstance().viewContent(
									jRequestResponseTable.getSelectedItem());
						} catch (IOException ex) {
							MessageDialogFactory.showUnexpectedExceptionDialog(
									RequestResponseDetailsPanel.this
											.getTopLevelAncestor(), ex);
						}
					}
				}
			});
		}
		return jRequestResponseTable;
	}

	/**
	 * Initializes and returns the JPanel that contains the View and Save As
	 * buttons for the Request-Response Table.
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel(new GridBagLayout());

			JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
			panel.add(getViewBtn());
			panel.add(getSaveBtn());

			buttonsPanel.add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0,
					1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
					new Insets(5, 5, 5, 5), 0, 0));
		}
		return buttonsPanel;
	}

	/**
	 * Initializes and returns the "View" button for the Request-Response Table.
	 */
	private JButton getViewBtn() {
		if (viewBtn == null) {
			viewBtn = new JButton(rb.getString("button.View"));
			viewBtn.setEnabled(false);
			viewBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						ContentViewer.getInstance().viewContent(
								jRequestResponseTable.getSelectedItem());
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								RequestResponseDetailsPanel.this
										.getTopLevelAncestor(), e);
					}
				}
			});
		}
		return viewBtn;
	}

	/**
	 * Initializes and returns the "Save AS" button for the Request-Response
	 * Table.
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton(rb.getString("button.Save"));
			saveBtn.setEnabled(false);
			saveBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					ContentViewer.getInstance().saveContent(
							RequestResponseDetailsPanel.this
									.getTopLevelAncestor(),
							jRequestResponseTable.getSelectedItem());
				}
			});
		}
		return saveBtn;
	}

}
