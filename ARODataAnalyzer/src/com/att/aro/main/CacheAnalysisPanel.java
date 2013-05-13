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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.commonui.DataTable;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.model.CacheEntry;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;

/**
 * Represents the panel that displays the Duplicate Content table on the
 * Overview tab.
 */
public class CacheAnalysisPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();

	private ApplicationResourceOptimizer parent;
	private JLabel jDuplicateTitleLabel;
	private JPanel jDuplicateContentPanel;
	private JScrollPane scrollPane;
	private CacheAnalysisTableModel jDuplicateContentTableModel = new CacheAnalysisTableModel();
	private DataTable<CacheEntry> jDuplicateContentTable;
	private JPanel buttonsPanel;
	private JButton viewBtn;
	private JButton saveBtn;

	/**
	 * Initializes a new instance of the CacheAnalysisPanel class using the
	 * specified instance of the ApplicationResourceOptimizer as the parent
	 * window.
	 * 
	 * @param parent
	 *            - The ApplicationResourceOptimizer instance.
	 */
	public CacheAnalysisPanel(ApplicationResourceOptimizer parent) {
		this.parent = parent;
		initialize();
	}

	/**
	 * Sets the data for the Duplicate Content table.
	 * 
	 * @param data
	 *            - The data to be displayed in the Duplicate Content table.
	 */
	public void setData(Collection<CacheEntry> data) {
		jDuplicateContentTableModel.setData(data);
	}

	/**
	 * Initializes the CacheAnalysisPanel.
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.add(getjDuplicateTitleLabel(), BorderLayout.NORTH);
		this.add(getjDuplicateContentPanel(), BorderLayout.CENTER);

	}

	/**
	 * Returns the duplicate content header label.
	 */
	private JLabel getjDuplicateTitleLabel() {
		if (jDuplicateTitleLabel == null) {
			jDuplicateTitleLabel = new JLabel(rb.getString("duplicate.title"), JLabel.CENTER);
		}
		return jDuplicateTitleLabel;
	}

	/**
	 * Initializes and returns the DuplicateContentPanel.
	 */
	private JPanel getjDuplicateContentPanel() {
		if (jDuplicateContentPanel == null) {
			jDuplicateContentPanel = new JPanel(new BorderLayout());
			jDuplicateContentPanel.add(getScrollPane(), BorderLayout.CENTER);
			jDuplicateContentPanel.add(getButtonsPanel(), BorderLayout.EAST);
		}
		return jDuplicateContentPanel;
	}

	/**
	 * Returns the Scroll Pane for the DuplicateContentTable.
	 */
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane(getJDuplicateContentTable());
		}
		return scrollPane;
	}

	/**
	 * Initializes and returns the RequestResponseTable.
	 */
	private DataTable<CacheEntry> getJDuplicateContentTable() {
		if (jDuplicateContentTable == null) {
			jDuplicateContentTable = new DataTable<CacheEntry>(jDuplicateContentTableModel);
			jDuplicateContentTable.setAutoCreateRowSorter(true);
			jDuplicateContentTable.setGridColor(Color.LIGHT_GRAY);
			jDuplicateContentTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {

							// Enable view and save as buttons appropriately
							CacheEntry entry = jDuplicateContentTable.getSelectedItem();
							HttpRequestResponseInfo rr = entry != null ? entry.getResponse() : null;
							boolean enabled = rr != null && rr.getContentLength() > 0
									&& rr.getDirection() == Direction.RESPONSE;
							getViewBtn().setEnabled(enabled);
							getSaveBtn().setEnabled(enabled);
						}
					});
			jDuplicateContentTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					CacheEntry entry = jDuplicateContentTable.getSelectedItem();
					if (e.getClickCount() == 2 && entry != null) {
						parent.displayDiagnosticTab();
						parent.getAroAdvancedTab().setHighlightedRequestResponse(
								entry.getResponse());
					}
				}
			});
		}
		return jDuplicateContentTable;
	}

	/**
	 * Returns the panel that contains the view and save button.
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel(new GridBagLayout());

			JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
			panel.add(getViewBtn());
			panel.add(getSaveBtn());

			buttonsPanel.add(panel,
					new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
							GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		}
		return buttonsPanel;
	}

	/**
	 * Returns the view button.
	 */
	private JButton getViewBtn() {
		if (viewBtn == null) {
			viewBtn = new JButton(rb.getString("button.View"));
			viewBtn.setEnabled(false);
			viewBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						CacheEntry entry = jDuplicateContentTable.getSelectedItem();
						if (entry != null) {
							if (!entry.getResponse().getContentType().contains("video")) {
								if (entry.getResponse().getActualByteCount() < 5242880) {
									ContentViewer.getInstance().viewContent(entry.getResponse());
								} else {
									MessageDialogFactory.showErrorDialog(new Window(new Frame()),
											rb.getString("Error.fileSize"));
								}
							} else {
								MessageDialogFactory.showErrorDialog(new Window(new Frame()),
										MessageFormat.format(rb.getString("Error.videofile"), entry.getResponse().getContentType()));
							}
						}
					} catch (IOException e) {
						MessageDialogFactory.showUnexpectedExceptionDialog(
								CacheAnalysisPanel.this.getTopLevelAncestor(), e);
					}
				}
			});
		}
		return viewBtn;
	}

	/**
	 * Returns the save button.
	 */
	private JButton getSaveBtn() {
		if (saveBtn == null) {
			saveBtn = new JButton(rb.getString("button.Save"));
			saveBtn.setEnabled(false);
			saveBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					CacheEntry entry = jDuplicateContentTable.getSelectedItem();
					if (entry != null) {
						ContentViewer.getInstance().saveContent(
								CacheAnalysisPanel.this.getTopLevelAncestor(), entry.getResponse());
					}
				}
			});
		}
		return saveBtn;
	}

}
