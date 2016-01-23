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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.commonui.ContentViewer;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.diagnostic.RequestResponseTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;


public class RequestResponseDetailsPanel extends  JPanel {
	private static final long serialVersionUID = 1L;
	
	private JScrollPane scrollPane;
	private DataTable<HttpRequestResponseInfo> jRequestResponseTable;
	private RequestResponseTableModel jRequestResponseTableModel = new RequestResponseTableModel();
	public RequestResponseTableModel getjRequestResponseTableModel() {
		return jRequestResponseTableModel;
	}

	private JPanel buttonsPanel;
	private JButton viewBtn;
	private JButton saveBtn;
	private IHttpRequestResponseHelper httpHelper = ContextAware.getAROConfigContext().getBean(IHttpRequestResponseHelper.class);
	private ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);

	private Session session;
	
	public Session getSession() {
		return session;
	}


	public void setSession(Session session) {
		this.session = session;
	}


	/**
	 * The default constructor that initializes a new instance of the
	 * RequestResponseDetailsPanel class.
	 * @param tab
	 */
	public RequestResponseDetailsPanel() {
		
		setLayout(new BorderLayout());
		add(getScrollPane(), BorderLayout.CENTER);
		add(getButtonsPanel(), BorderLayout.EAST);

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
	public DataTable<HttpRequestResponseInfo> getJRequestResponseTable() {
		if (jRequestResponseTable == null) {
			jRequestResponseTable = new DataTable<HttpRequestResponseInfo>(
					jRequestResponseTableModel);
			jRequestResponseTable.setAutoCreateRowSorter(true);
			jRequestResponseTable.setGridColor(Color.LIGHT_GRAY);
			jRequestResponseTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent event) {
							// Enable view and save as buttons appropriately
							HttpRequestResponseInfo httpRRInfo = jRequestResponseTable
									.getSelectedItem();
							boolean enabled = httpRRInfo != null
									&& httpRRInfo.getContentLength() > 0
									&& httpRRInfo.getDirection() == HttpDirection.RESPONSE
									&& httpRRInfo.getStatusCode() != 0;
							getViewBtn().setEnabled(enabled);
							getSaveBtn().setEnabled(enabled);
						}
					});
			jRequestResponseTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2 && getViewBtn().isEnabled()) {
						try {
							viewContent(jRequestResponseTable.getSelectedItem());
						} catch ( Exception ex) {
							new MessageDialogFactory().showUnexpectedExceptionDialog(
									RequestResponseDetailsPanel.this.getTopLevelAncestor(), ex);
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

			JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
			panel.add(getViewBtn());
			panel.add(getSaveBtn());

			buttonsPanel.add(panel,
					new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
							GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		}
		return buttonsPanel;
	}

	/**
	 * Initializes and returns the "View" button for the Request-Response Table.
	 */
	private JButton getViewBtn() {
		if (viewBtn == null) {
			viewBtn = new JButton(ResourceBundleHelper.getMessageString("button.View"));
			viewBtn.setEnabled(false);
			viewBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						viewContent(jRequestResponseTable.getSelectedItem());
					} catch ( Exception e) {
						new MessageDialogFactory().showUnexpectedExceptionDialog(
								RequestResponseDetailsPanel.this.getTopLevelAncestor(), e);
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
			saveBtn = new JButton(ResourceBundleHelper.getMessageString("button.Save"));
			saveBtn.setEnabled(false);
			saveBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					ContentViewer.getInstance().saveContent(
							RequestResponseDetailsPanel.this.getTopLevelAncestor(),
							getSession(),jRequestResponseTable.getSelectedItem());
				}
			});

		}
		return saveBtn;
	}
 
 

	public void updateTable(Session session){
		setSession(session);
		jRequestResponseTableModel.refresh(session);
	}
 	
	public void setHighlightedRequestResponse(HttpRequestResponseInfo rr) {
		getJRequestResponseTable().selectItem(rr);
	}

	/**
	 * Creates a content viewer to view the response content.
	 * 
	 * @param rrInfo
	 * @throws IOException
	 */
	private void viewContent(HttpRequestResponseInfo rrInfo) throws Exception {
		if ((rrInfo.getContentType()!=null)&& (!rrInfo.getContentType().contains("video"))) {
			if (httpHelper.getActualByteCount(rrInfo,getSession()) < 5242880) {
				ContentViewer.getInstance().viewContent(getSession(),rrInfo);
			} else {
				new MessageDialogFactory().showErrorDialog(new Window(new Frame()),
						ResourceBundleHelper.getMessageString("Error.fileSize"));
			}
		} else {
			new MessageDialogFactory().showErrorDialog(new Window(new Frame()), MessageFormat.format(
					ResourceBundleHelper.getMessageString("Error.videofile"), rrInfo.getContentType()));
		}
	}
	

}
