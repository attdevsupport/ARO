/*
 *  Copyright 2012 AT&T
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.HttpRequestResponseInfo.Direction;
import com.att.aro.model.RequestResponseTimeline;

/**
 * Popup window that appears on the waterfall diagram when a user selects a 
 * request/response in order to view details.
 */
public class WaterfallPopup extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	private static final String NA = rb.getString("waterfall.na");
	private static String S = rb.getString("waterfall.seconds");
	private static String MS = rb.getString("waterfall.ms");
	private static String KB = rb.getString("waterfall.kb");

	private JTabbedPane tabbedPane;
	private JPanel detailsPanel;
	private JLabel urlValueLabel = new JLabel();
	private JLabel ipValueLabel = new JLabel();
	private JLabel hostValueLabel = new JLabel();
	private JLabel statusValueLabel = new JLabel();
	private JLabel startValueLabel = new JLabel();
	private JLabel dnsValueLabel = new JLabel();
	private JLabel initConnValueLabel = new JLabel();
	private JLabel sslValueLabel = new JLabel();
	private JLabel reqTimeValueLabel = new JLabel();
	private JLabel firstByteValueLabel = new JLabel();
	private JLabel contentValueLabel = new JLabel();
	private JLabel bytesInValueLabel = new JLabel();
	private JLabel bytesOutValueLabel = new JLabel();
	private RequestResponsePanel requestPanel;
	private RequestResponsePanel responsePanel;
	private HttpRequestResponseInfo reqResp;
	
	/**
	 * Constructor
	 * @param parent parent window/frame
	 */
	public WaterfallPopup(Window parent) {
		super(parent);
		this.setLayout(new BorderLayout());
		this.add(getTabbedPane(), BorderLayout.CENTER);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setSize(new Dimension(600, 400));
		this.setMinimumSize(getDetailsPanel().getPreferredSize());
		this.setTitle(rb.getString("waterfall.popupTitle"));
	}

	/**
	 * Refreshes the popup window with data from the specified request/response
	 * @param req The request/response pair.  Direction must be REQUEST and
	 * response will be read from request.
	 */
	public synchronized void refresh(HttpRequestResponseInfo req) {
		
		if (req != null) {
			
			// Check to see if request/response is already displayed
			if (req == reqResp) {
				return;
			}
			
			// Check for valid argument - only HTTP requests
			if (req.getDirection() != Direction.REQUEST || req.getWaterfallInfos() == null) {
				throw new IllegalArgumentException("Invalid request object");
			}
			
			RequestResponseTimeline wf = req.getWaterfallInfos();
			HttpRequestResponseInfo resp = req.getAssocReqResp();

			NumberFormat secFmt = NumberFormat.getNumberInstance();
			secFmt.setMaximumFractionDigits(3);

			NumberFormat kbFmt = NumberFormat.getNumberInstance();
			kbFmt.setMaximumFractionDigits(1);
			
			urlValueLabel.setText(req.getObjUri() != null ? req.getObjUri().toString() : req.getObjName());
			hostValueLabel.setText(req.getSession().getRemoteHostName());
			ipValueLabel.setText(req.getSession().getRemoteIP().getHostAddress());
			statusValueLabel.setText(String.valueOf(resp.getStatusCode()));
			startValueLabel.setText(MessageFormat.format(S, secFmt.format(req.getTimeStamp())));
			refreshWaterfallLabel(dnsValueLabel, wf.getDnsLookupDuration());
			refreshWaterfallLabel(initConnValueLabel, wf.getInitialConnDuration());
			refreshWaterfallLabel(sslValueLabel, wf.getSslNegotiationDuration());
			refreshWaterfallLabel(reqTimeValueLabel, wf.getRequestDuration());
			refreshWaterfallLabel(firstByteValueLabel, wf.getTimeToFirstByte());
			refreshWaterfallLabel(contentValueLabel, wf.getContentDownloadDuration());
			bytesInValueLabel.setText(MessageFormat.format(KB, kbFmt.format(resp.getRawSizeInKB())));
			bytesOutValueLabel.setText(MessageFormat.format(KB, kbFmt.format(req.getRawSizeInKB())));
			getRequestPanel().refresh(req);
			getResponsePanel().refresh(resp);
		} else {
			setVisible(false);
		}
		this.reqResp = req;
	}
	
	private void refreshWaterfallLabel(JLabel l, Double value) {
		if (value != null) {
			NumberFormat msFmt = NumberFormat.getNumberInstance();
			msFmt.setMaximumFractionDigits(0);

			l.setText(MessageFormat.format(MS, msFmt.format(value * 1000)));
		} else {
			l.setText(NA);
		}
	}
	
	/**
	 * @return the tabbedPane
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab(rb.getString("waterfall.details"), getDetailsPanel());
			tabbedPane.addTab(rb.getString("waterfall.request"), getRequestPanel());
			tabbedPane.addTab(rb.getString("waterfall.response"), getResponsePanel());
		}
		return tabbedPane;
	}

	/**
	 * @return the detailsPanel
	 */
	private JPanel getDetailsPanel() {
		if (detailsPanel == null) {
			
			JPanel panel = new JPanel(new GridLayout(13, 1, 5, 5));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.url")), urlValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.host")), hostValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.ip")), ipValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.status")), statusValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.start")), startValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.dns")), dnsValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.initConn")), initConnValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.ssl")), sslValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.reqTime")), reqTimeValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.firstByte")), firstByteValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.content")), contentValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.bytesIn")), bytesInValueLabel));
			panel.add(createDetailRowPanel(new JLabel(rb.getString("waterfall.bytesOut")), bytesOutValueLabel));
			
			detailsPanel = new JPanel(new GridBagLayout());
			detailsPanel.add(panel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		}
		return detailsPanel;
	}
	
	/**
	 * @return the requestPanel
	 */
	private RequestResponsePanel getRequestPanel() {
		if (requestPanel == null) {
			requestPanel = new RequestResponsePanel();
		}
		return requestPanel;
	}

	/**
	 * @return the responsePanel
	 */
	private RequestResponsePanel getResponsePanel() {
		if (responsePanel == null) {
			responsePanel = new RequestResponsePanel();
		}
		return responsePanel;
	}
	
	private JPanel createDetailRowPanel(JLabel label, JLabel value) {
		JPanel result = new JPanel(new BorderLayout(5, 5));
		result.add(label, BorderLayout.WEST);
		result.add(value, BorderLayout.CENTER);
		return result;
	}

	/**
	 * Common panel for displaying the request and response tabs
	 */
	private class RequestResponsePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private JTextArea textArea;

		/**
		 * Constructor
		 */
		public RequestResponsePanel() {
			super(new BorderLayout());
			this.add(new JScrollPane(getTextArea()), BorderLayout.CENTER);
		}

		/**
		 * @return the textArea
		 */
		public JTextArea getTextArea() {
			if (textArea == null) {
				textArea = new JTextArea();
				textArea.setEditable(false);
			}
			return textArea;
		}

		/**
		 * Refreshes this panel with specified request/response info
		 * @param rr
		 */
		public void refresh(HttpRequestResponseInfo rr) {
			textArea.setText(rr != null ? rr.getRequestResponseText() : null);
			textArea.setCaretPosition(0);
		}

	}
}
