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
package com.att.aro.ui.view.waterfalltab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.text.NumberFormat;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.RequestResponseTimeline;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Popup window that appears on the waterfall diagram when a user selects a 
 * request/response in order to view details.
 *
 */
public class WaterfallPopup extends JDialog{
	

	private static final long serialVersionUID = 1L;
	private static final String W_NA = ResourceBundleHelper.getMessageString("waterfall.na");
	private static final String W_SECONDS = ResourceBundleHelper.getMessageString("waterfall.seconds");
	private static final String WM_SECONDS = ResourceBundleHelper.getMessageString("waterfall.ms");
	private static final String W_KB = ResourceBundleHelper.getMessageString("waterfall.kb");
	private static final int MAX_CHARS_TO_DISPLAY = 1000;
	
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
	
	public WaterfallPopup(){
		this.setLayout(new BorderLayout());
		this.add(getTabbedPane(), BorderLayout.CENTER);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		this.setSize(new Dimension(600, 400));
		this.setMinimumSize(getDetailsPanel().getPreferredSize());
		
		//TODO need set the parent window
		this.setLocationRelativeTo(this); 
	}
	
	
	/**
	 * 
	 * @return the tabbedPane
	 */
	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab(ResourceBundleHelper.getMessageString("waterfall.details"), getDetailsPanel());
			tabbedPane.addTab(ResourceBundleHelper.getMessageString("waterfall.request"), getRequestPanel());
			tabbedPane.addTab(ResourceBundleHelper.getMessageString("waterfall.response"), getResponsePanel());
		}
		return tabbedPane;
	}
	
	
	/**
	 * @return the detailsPanel
	 */
	private JPanel getDetailsPanel() {
		if (detailsPanel == null) {
			
			JPanel panel = new JPanel(new GridLayout(13, 1, 5, 5));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.url")), urlValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.host")), hostValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.ip")), ipValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.status")), statusValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.start")), startValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.dns")), dnsValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.initConn")), initConnValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.ssl")), sslValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.requestTime")), reqTimeValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.firstByte")), firstByteValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.content")), contentValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.bytesIn")), bytesInValueLabel));
			panel.add(createDetailRowPanel(new JLabel(ResourceBundleHelper.getMessageString("waterfall.bytesOut")), bytesOutValueLabel));
			
			detailsPanel = new JPanel(new GridBagLayout());
			detailsPanel.add(panel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		}
		return detailsPanel;
	}
	
	/**
	 * 
	 * @param label
	 * @param value
	 * @return
	 */
	private JPanel createDetailRowPanel(JLabel label, JLabel value) {
		JPanel result = new JPanel(new BorderLayout(5, 5));
		result.add(label, BorderLayout.WEST);
		result.add(value, BorderLayout.CENTER);
		return result;
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

	//TODO find how to do refresh...once we get the data.
	
	/**
	 * Refreshes the popup window with data from the specified request/response
	 * @param req The request/response pair.  Direction must be REQUEST and
	 * response will be read from request.
	 */
	public void refresh(HttpRequestResponseInfo req, int index) {
				
		if (req == null) {
			this.setVisible(false);
		} else {
			
			this.setTitle(MessageFormat.format(ResourceBundleHelper.getMessageString("waterfall.popupTitle"), index));
			
			// Check to see if request/response is already displayed
			if (req.equals(reqResp)) {
//				logger.exiting("WaterfallPopup", "refresh");
				return;
			}

			// Check for valid argument - only HTTP requests
			if (req.getDirection() != HttpDirection.REQUEST || req.getWaterfallInfos() == null) {
				throw new IllegalArgumentException("Invalid request object");
			}
			
			RequestResponseTimeline rrTimeLine = req.getWaterfallInfos();
			HttpRequestResponseInfo resp = req.getAssocReqResp();

			NumberFormat secFmt = NumberFormat.getNumberInstance();
			secFmt.setMaximumFractionDigits(3);

			NumberFormat kbFmt = NumberFormat.getNumberInstance();
			kbFmt.setMaximumFractionDigits(1);
			
			if(req.getObjUri() == null){
				urlValueLabel.setText(req.getObjName());
			} else {
				urlValueLabel.setText(req.getObjUri().toString());
			}
			//urlValueLabel.setText(req.getObjUri() != null ? req.getObjUri().toString() : req.getObjName());
			hostValueLabel.setText(req.getHostName());
			//TODO
			//ipValueLabel.setText(req.getSession().getRemoteIP().getHostAddress());
			ipValueLabel.setText(req.getFirstDataPacket().getRemoteIPAddress().getHostAddress());
			if(resp.getStatusLine() == null){
				statusValueLabel.setText(ResourceBundleHelper.getMessageString("waterfall.unknownCode"));
			} else {
				statusValueLabel.setText(resp.getStatusLine());
			}
			
			//statusValueLabel.setText(resp.getStatusLine() != null ? resp.getStatusLine() : ResourceBundleHelper.getMessageString("waterfall.unknownCode"));
			startValueLabel.setText(MessageFormat.format(W_SECONDS, secFmt.format(rrTimeLine.getStartTime())));
			refreshWaterfallLabel(dnsValueLabel, rrTimeLine.getDnsLookupDuration());
			refreshWaterfallLabel(initConnValueLabel, rrTimeLine.getInitialConnDuration());
			refreshWaterfallLabel(sslValueLabel, rrTimeLine.getSslNegotiationDuration());
			refreshWaterfallLabel(reqTimeValueLabel, rrTimeLine.getRequestDuration());
			refreshWaterfallLabel(firstByteValueLabel, rrTimeLine.getTimeToFirstByte());
			refreshWaterfallLabel(contentValueLabel, rrTimeLine.getContentDownloadDuration());
			bytesInValueLabel.setText(MessageFormat.format(W_KB, kbFmt.format(resp.getRawSizeInKB())));
			bytesOutValueLabel.setText(MessageFormat.format(W_KB, kbFmt.format(req.getRawSizeInKB())));
			getRequestPanel().refresh(req);
			getResponsePanel().refresh(resp);
		} 
		this.reqResp = req;
		//logger.exiting("WaterfallPopup", "refresh");
	}
	

	/**
	 * 
	 * @param l
	 * @param value
	 */
	private void refreshWaterfallLabel(JLabel l, Double value) {
		if (value == null) {
			l.setText(W_NA);
		} else {
			NumberFormat msFmt = NumberFormat.getNumberInstance();
			msFmt.setMaximumFractionDigits(0);

			l.setText(MessageFormat.format(WM_SECONDS, msFmt.format(value * 1000)));
		} 
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
		public void refresh(HttpRequestResponseInfo reqResInfo) {
	
			
		if (reqResInfo == null) {
			textArea.setText(null);
		} else {
				/*if (reqResInfo.getContentEncoding() != null && reqResInfo.getContentEncoding().length() > MAX_CHARS_TO_DISPLAY) {
					textArea.setText(reqResInfo.getContentEncoding().substring(0,
							MAX_CHARS_TO_DISPLAY - 1));
				} else {
					textArea.setText(reqResInfo.getContentEncoding());
				}*/
			//For getting the request and response Data. Added this logic in new code. Getting the data from packet.
			StringBuffer reqResString = new StringBuffer();
			if(reqResInfo.getFirstDataPacket() != null && reqResInfo.getFirstDataPacket().getPacket() != null){
				String firstPacketData = new String(reqResInfo.getFirstDataPacket().getPacket().getData());
				reqResString.append(firstPacketData);
				
			}
			if(reqResInfo.getLastDataPacket() != null && reqResInfo.getLastDataPacket().getPacket() != null){
				String lastPacketData = new String(reqResInfo.getLastDataPacket().getPacket().getData());
				reqResString.append(lastPacketData);
			}
		
			if(reqResString.length() > MAX_CHARS_TO_DISPLAY){
				textArea.setText(reqResString.substring(0, MAX_CHARS_TO_DISPLAY - 1));
			} else {
				textArea.setText(reqResString.toString());
			}
			
		} 
			textArea.setCaretPosition(0);
		}

	}


	/**
	 * @return the popupDialog
	 */
	public JDialog getPopupDialog() {
		return this;
	}
	
}
