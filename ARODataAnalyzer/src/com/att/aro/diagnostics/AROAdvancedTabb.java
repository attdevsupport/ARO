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


package com.att.aro.diagnostics;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.commonui.DataTable;
import com.att.aro.main.ApplicationResourceOptimizer;
import com.att.aro.main.ChartPlotOptions;
import com.att.aro.main.DeviceNetworkProfilePanel;
import com.att.aro.main.GraphPanelListener;
import com.att.aro.main.ResourceBundleManager;
import com.att.aro.model.HttpRequestResponseInfo;
import com.att.aro.model.PacketInfo;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData;
import com.att.aro.video.AROVideoPlayer;

/**
 * Represents the Diagnostic tab screen.
 */
public class AROAdvancedTabb extends SearchablePanel {

	private static final long serialVersionUID = 1L;
	
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	private static final int MAX_ZOOM = 4;
	private static final Double matchingSecondsRange = 0.5;

	// Split pane for TCP flow data
	private JSplitPane internalPanel;

	// TCP Flows header
	private JPanel tcpFlowsHeadingPanel;
	private JLabel tcpFlowsLabel;

	private RequestResponseDetailsPanel jHttpReqResPanel;
	
	// Components for TCP Flows scroll table
	private JPanel jTCPFlowsPanel;
	private JScrollPane jTCPFlowsScrollPane;
	private TCPFlowsTableModel jTCPFlowsTableModel = new TCPFlowsTableModel();
	private DataTable<TCPSession> jTCPFlowsTable;

	// TCP flow detail tabbed pane
	private JTabbedPane jTCPFlowsContentTabbedPane;

	// Packet view
	private JScrollPane jPacketViewTapScrollPane;

	private PacketInfoTableModel jPacketViewTableModel = new PacketInfoTableModel();
	private DataTable<PacketInfo> jPacketViewTable;

	// Content view
	private JScrollPane jContentViewScrollPane; // Content View
	private JPanel jContentViewPanel; // Content View
	private JPanel buttonsPanel; // Content View
	private JTextArea jContentTextArea; // Context text

	// network profile panel
	private DeviceNetworkProfilePanel deviceNetworkProfilePanel;

	// Chart panel
	private GraphPanel graphPanel;

	private AROVideoPlayer aroVideoPlayer;

	// Trace data currently displayed
	private TraceData.Analysis analysisData;
	private boolean graphPanelClicked = false;
	private boolean bTCPPacketFound;


	/**
	 * Initializes a new instance of the AROAdvancedTabb class.
	 * @param aro ApplicationResourceOptimizer
	 */
	public AROAdvancedTabb(ApplicationResourceOptimizer aro) {
		this.aro = aro;
		this.setLayout(new BorderLayout());
		// Add profile panel
		this.add(getDeviceNetworkProfilePanel(), BorderLayout.NORTH);
		
		JPanel chartAndTablePanel = new JPanel();
		chartAndTablePanel.setLayout(new BorderLayout());
		// Add chart
		chartAndTablePanel.add(getGraphPanel(), BorderLayout.NORTH);
		// Add TCP flows split pane
		chartAndTablePanel.add(getOrientationPanel(), BorderLayout.CENTER);
		this.add(chartAndTablePanel, BorderLayout.CENTER);
	}

	/**
	 * Sets the trace data to be displayed on the Diagnostic tab screen.
	 * 
	 * @param analysisData
	 *            The trace analysis data to be displayed.
	 */
	public synchronized void setAnalysisData(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;
		if (analysisData != null) {
			jTCPFlowsTableModel.setData(analysisData.getTcpSessions());
		} else {
			jTCPFlowsTableModel.setData(null);
		}
		getGraphPanel().resetChart(analysisData);
		deviceNetworkProfilePanel.refresh(analysisData);
	}

	/**
	 * Sets the video player to be used with this AROAdvancedTabb. The video
	 * player displays a video of screen captures that were recorded while trace
	 * data was being captured. The current time position in the video playback
	 * is updated to match the selected time position in the AROAdvancedTabb
	 * chart.
	 * 
	 * @param videoPlayer
	 *            An AROVideoPlayer object representing the video player to be
	 *            used with this AROAdvancedTabb.
	 */
	public void setVideoPlayer(AROVideoPlayer videoPlayer) {
		aroVideoPlayer = videoPlayer;
	}

	/**
	 * Sets the chart options in the AROAdvancedTab to the specified List of
	 * ChartPlotOptions.
	 * 
	 * @param optionsSelected
	 *            A List containing the chart plot options. Typically, these
	 *            are the options selected using the ChartPlotOptions dialog in
	 *            the View menu.
	 */
	public void setChartOptions(List<ChartPlotOptions> optionsSelected) {
		this.getGraphPanel().setChartOptions(optionsSelected);
	}

	private JSplitPane getOrientationPanel() {
		if (internalPanel == null) {
			
			internalPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getJTCPFlowsPanel(), getJTCPFlowsContentTabbedPane());
			internalPanel.setResizeWeight(.5);
			internalPanel.updateUI();
			jTCPFlowsTableModel.setRrDetailsPanel(getJHttpReqResPanel()); 
		}
		return internalPanel;
	}

	/**
	 * Initializes and returns the Device network profile panel.
	 */
	private DeviceNetworkProfilePanel getDeviceNetworkProfilePanel() {
		if (deviceNetworkProfilePanel == null) {
			deviceNetworkProfilePanel = new DeviceNetworkProfilePanel();
		}
		return deviceNetworkProfilePanel;
	}

	/**
	 * Returns the Panel that contains the graph.
	 */
	private GraphPanel getGraphPanel() {
		if (this.graphPanel == null) {
			this.graphPanel = new GraphPanel();
			graphPanel.setZoomFactor(2);
			graphPanel.setMaxZoom(MAX_ZOOM);
			graphPanel.addGraphPanelListener(new GraphPanelListener() {

				@Override
				public void graphPanelClicked(double timeStamp) {
					setTimeLineLinkedComponents(timeStamp, matchingSecondsRange);
					if (aroVideoPlayer != null) {
						graphPanelClicked = true;
						aroVideoPlayer.setMediaDisplayTime(timeStamp);
					}
				}
			});
		}
		return this.graphPanel;
	}
	/**
	 * Returns the graph panel clicked status
	 * 
	 * @return boolean value.
	 */
	public boolean IsGraphPanelClicked(){
		return graphPanelClicked;
	}
	
	/**
	 * Set the graph panel clicked status
	 */
	public void setGraphPanelClicked(boolean val){
		 graphPanelClicked = val;
	}
	
	/**
	 * Returns the video player associated with this instance.
	 * 
	 * @return An AROVideoPlayer object.
	 */
	public AROVideoPlayer getVideoPlayer() {
		return aroVideoPlayer;
	}

	/**
	 * Initializes and returns the Tabbed pane at the bottom.
	 */
	private JTabbedPane getJTCPFlowsContentTabbedPane() {
		if (jTCPFlowsContentTabbedPane == null) {
			jTCPFlowsContentTabbedPane = new JTabbedPane();
			jTCPFlowsContentTabbedPane.addTab(rb.getString("tcp.tab.reqResp"), null, getJHttpReqResPanel(), null);
			jTCPFlowsContentTabbedPane.addTab(rb.getString("tcp.tab.packet"),  null, getJPacketViewTapScrollPane(), null);
			jTCPFlowsContentTabbedPane.addTab(rb.getString("tcp.tab.content"), null, getJContentViewPanel(), null);
			/*jTCPFlowsContentTabbedPane.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (jTCPFlowsContentTabbedPane.getSelectedComponent() == getJContentViewPanel()) {
						TCPSession sess = jTCPFlowsTable.getSelectedItem();
						if (sess != null) {
							if(!sess.isUDP()){
								jContentTextArea.setText(sess.getDataText());
								jContentTextArea.setCaretPosition(0);
							}
						} else {
							jContentTextArea.setText(null);
						}
					}
				}
			});*/
		}
		return jTCPFlowsContentTabbedPane;
	}

	/**
	 * Returns RequestResponseDetailsPanel.
	 * @return the jHttpReqResPanel
	 */
	private RequestResponseDetailsPanel getJHttpReqResPanel() {
		if (jHttpReqResPanel == null) {
			jHttpReqResPanel = new RequestResponseDetailsPanel(this.aro);
		}
		return jHttpReqResPanel;
	}

	/**
	 * Initializes jTCPFlowsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTCPFlowsPanel() {
		if (jTCPFlowsPanel == null) {
			jTCPFlowsPanel = new JPanel();
			jTCPFlowsPanel.setPreferredSize(new Dimension(400, 160));
			jTCPFlowsPanel.setLayout(new BorderLayout());
			jTCPFlowsPanel.add(getTcpFlowsHeadingPanel(), BorderLayout.NORTH);
			jTCPFlowsPanel.add(getJTCPFlowsScrollPane(), BorderLayout.CENTER);
		}
		return jTCPFlowsPanel;
	}

	/**
	 * Initializes and returns the PacketViewTapScrollPane
	 */
	private JScrollPane getJPacketViewTapScrollPane() {
		if (jPacketViewTapScrollPane == null) {
			jPacketViewTapScrollPane = new JScrollPane(getJPacketViewTable());
		}
		return jPacketViewTapScrollPane;
	}

	/**
	 * Returns the DataTable for the packet view.
	 */
	private DataTable<PacketInfo> getJPacketViewTableAsDataTable() {
		return jPacketViewTable;
	}

	/**
	 * Initializes and returns the Packet View Table.
	 */
	private JTable getJPacketViewTable() {
		if (jPacketViewTable == null) {
			jPacketViewTable = new DataTable<PacketInfo>(jPacketViewTableModel);
			jPacketViewTable.setAutoCreateRowSorter(true);
			jPacketViewTable.setGridColor(Color.LIGHT_GRAY);
			jPacketViewTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						PacketInfo packetInfo;

						@Override
						public synchronized void valueChanged(
								ListSelectionEvent arg0) {
							PacketInfo packetInfo = jPacketViewTable
									.getSelectedItem();
							if (packetInfo != null
									&& packetInfo != this.packetInfo) {
								double crossHairValue = packetInfo.getTimeStamp();
								boolean centerGraph = !(crossHairValue <= graphPanel
										.getViewportUpperBound() && crossHairValue >= graphPanel
										.getViewportLowerBound());
								graphPanel.setGraphView(crossHairValue,
										centerGraph);
								getJHttpReqResPanel().select(
										packetInfo.getRequestResponseInfo());
								if (aroVideoPlayer != null) {
									aroVideoPlayer
											.setMediaDisplayTime(graphPanel
													.getCrosshair());
								}
							}
							this.packetInfo = packetInfo;
						}
					});
		}
		return jPacketViewTable;
	}

	/**
	 * Initializes and returns the Panel for the Content View tab at the
	 * bottom.
	 */
	private JPanel getJContentViewPanel() {
		if (jContentViewPanel == null) {
			jContentViewPanel = new JPanel(new BorderLayout());
			jContentViewPanel.add(getJContentViewScrollPane(), BorderLayout.CENTER);
			jContentViewPanel.add(getButtonsPanel(), BorderLayout.EAST);
		}
		return jContentViewPanel;
	}
	
	/**
	 * Initializes and returns the Scroll Pane for the Content View tab at the
	 * bottom.
	 */
	private JScrollPane getJContentViewScrollPane() {
		if (jContentViewScrollPane == null) {
			if (jContentTextArea == null) {
				jContentTextArea = new JTextArea(10, 20);
			}
			jContentViewScrollPane = new JScrollPane(jContentTextArea);
			jContentTextArea.setLineWrap(true);
			jContentTextArea.setCaretPosition(0);
			jContentViewScrollPane.setPreferredSize(new Dimension(100, 200));
			
			setSearchableTextArea(jContentTextArea);
		}
		return jContentViewScrollPane;
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
			
			enableSearch();
		}
		return buttonsPanel;
	}
	
	
	/**
	 * Initializes and returns the TCPFlowsScrollPane.
	 */
	private JScrollPane getJTCPFlowsScrollPane() {
		if (jTCPFlowsScrollPane == null) {
			jTCPFlowsScrollPane = new JScrollPane(getJTCPFlowsTable());
			jTCPFlowsScrollPane.setPreferredSize(new Dimension(100, 200));

		}
		return jTCPFlowsScrollPane;
	}

	/**
	 * Initializes and returns the Scroll Pane for the TCP flows table.
	 */
	private DataTable<TCPSession> getJTCPFlowsTable() {
		if (jTCPFlowsTable == null) {
			jTCPFlowsTable = new DataTable<TCPSession>(jTCPFlowsTableModel);
			jTCPFlowsTable.setAutoCreateRowSorter(true);
			jTCPFlowsTable.setGridColor(Color.LIGHT_GRAY);
			jTCPFlowsTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						private TCPSession tcp;

						@Override
						public synchronized void valueChanged(
								ListSelectionEvent arg0) {
							TCPSession tcp = jTCPFlowsTable.getSelectedItem();
							if (tcp != this.tcp) {
								if (tcp != null) {
									if(!tcp.isUDP()){
										jPacketViewTableModel.setData(tcp
												.getPackets());
										jPacketViewTable
												.setGridColor(Color.LIGHT_GRAY);
										if (!tcp.getPackets().isEmpty()) {
											jPacketViewTable.getSelectionModel()
													.setSelectionInterval(0, 0);
										}
										jContentTextArea.setText(tcp.getDataText());
										jContentTextArea.setCaretPosition(0);
										getJHttpReqResPanel().setData(
												tcp.getRequestResponseInfo());
									}
									else{
										/*Show the UDP packets.*/
										jPacketViewTableModel.setData(tcp
												.getUDPPackets());
										jPacketViewTable
										.setGridColor(Color.LIGHT_GRAY);
										if (!tcp.getUDPPackets().isEmpty()) {
											jPacketViewTable.getSelectionModel()
													.setSelectionInterval(0, 0);
										}
										if (jTCPFlowsContentTabbedPane
												.getSelectedComponent() == getJContentViewScrollPane()) {
											jContentTextArea.setText(tcp.getDataText());
										}
										jContentTextArea.setCaretPosition(0);
										getJHttpReqResPanel().setData(tcp.getRequestResponseInfo());
									}
								} else {
									jPacketViewTableModel.removeAllRows();
									getJHttpReqResPanel().setData(null);
									jContentTextArea.setText(null);
								}
								this.tcp = tcp;
							}
						}
					});
		}
		return jTCPFlowsTable;
	}

	/**
	 * Creates the TCP Flows heading panel.
	 */
	private JPanel getTcpFlowsHeadingPanel() {
		if (tcpFlowsHeadingPanel == null) {
			tcpFlowsHeadingPanel = new JPanel();
			tcpFlowsHeadingPanel.setLayout(new GridBagLayout());
			tcpFlowsHeadingPanel.add(getTcpFlowsLabel());
			tcpFlowsHeadingPanel.setPreferredSize(new Dimension(110, 20));
		}
		return tcpFlowsHeadingPanel;
	}

	/**
	 * Returns the TCP flows label.
	 */
	private JLabel getTcpFlowsLabel() {
		if (tcpFlowsLabel == null) {
			tcpFlowsLabel = new JLabel(rb.getString("tcp.title"));
		}
		return tcpFlowsLabel;
	}

	/**
	 * Sets the chart cross hair to the specified timestamp in the chart.
	 * 
	 * @param timeStamp
	 *            - The timestamp in the chart to which the cross hair should be
	 *            set.
	 */
	public synchronized void setTimeLineLinkedComponents(double timeStamp) {
		if (analysisData != null) {
			if (timeStamp < 0.0) {
				timeStamp = 0.0;
			}
			if (timeStamp > analysisData.getTraceData().getTraceDuration()) {
				timeStamp = analysisData.getTraceData().getTraceDuration();
			}
			getGraphPanel().setGraphView(timeStamp);
		}
	}

	/**
	 * Method to set the time on graph panel.
	 */
	private synchronized void setTimeLineLinkedComponents(double timeStamp,
			double dTimeRangeInterval) {

		if (analysisData != null) {
			boolean bTCPTimeStampFound = false;
			boolean bExactMatch = false;

			// Do exact match of dTimeInterval == 0.0;
			// If dTimeInterval < 0.0, don't try to match up with the TCP_Flow
			// or packets when click comes from graph or video
			if (dTimeRangeInterval == 0.0) {
				bExactMatch = true;
			} else if (dTimeRangeInterval < 0.0) {
				repaint();
				return;
			}

			// Attempt to find corresponding packet for time.
			double packetTimeStamp = 0.0;
			double packetTimeStampDiff = 0.0;
			double previousPacketTimeStampDiff = 9999.0;
			TCPSession bestMatchingTcpSession = null;
			PacketInfo bestMatchingPacketInfo = null;
			for (TCPSession tcpSess : analysisData.getTcpSessions()) {
				PacketInfo packetInfo = getBestMatchingPacketInTcpSession(
						tcpSess, bExactMatch, timeStamp, dTimeRangeInterval);
				if (packetInfo != null) {
					packetTimeStamp = packetInfo.getTimeStamp();
					packetTimeStampDiff = timeStamp - packetTimeStamp;
					if (packetTimeStampDiff < 0.0) {
						packetTimeStampDiff *= -1.0;
					}
					if (packetTimeStampDiff < previousPacketTimeStampDiff) {
						bestMatchingTcpSession = tcpSess;
						bestMatchingPacketInfo = packetInfo;
						bTCPTimeStampFound = true;
					}
				}
			}

			if (bTCPTimeStampFound) {
				getJTCPFlowsTable().selectItem(bestMatchingTcpSession);
				jPacketViewTable.selectItem(bestMatchingPacketInfo);
				jPacketViewTable.setGridColor(Color.LIGHT_GRAY);
				if (bestMatchingPacketInfo != null) {
					jHttpReqResPanel.select(bestMatchingPacketInfo
							.getRequestResponseInfo());
				} else {
					jHttpReqResPanel.select(null);
				}
			} else {
				getJTCPFlowsTable().selectItem(null);
				jPacketViewTable.selectItem(null);
				jHttpReqResPanel.select(null);
				if (aroVideoPlayer != null) {
					bTCPPacketFound = false;
					aroVideoPlayer.setMediaDisplayTime(graphPanel
							.getCrosshair());
				}
			}
		}
	}

	public boolean getTCPPacketFoundStatus(){
		return bTCPPacketFound;
	}
	
	public void reSetTCPPacketFoundStatus(boolean val){
		bTCPPacketFound = val;
	}
	/**
	 * Provides the Best matching packet info from the provided tcp session.
	 */
	private PacketInfo getBestMatchingPacketInTcpSession(TCPSession tcpSession,
			boolean bExactMatch, double timeStamp, double dTimeRangeInterval) {

		// Try to eliminate session before iterating through packets
		if(!tcpSession.isUDP()){
			if (tcpSession.getSessionStartTime() > timeStamp
					|| tcpSession.getSessionEndTime() < timeStamp) {
				return null;
			}
		}else{
			if (tcpSession.getUDPSessionStartTime() > timeStamp
					|| tcpSession.getUDPSessionEndTime() < timeStamp) {
				return null;
			}
		}

		double packetTimeStamp = 0.0;
		PacketInfo matchedPacket = null;
		if(!tcpSession.isUDP()){
			for (PacketInfo p : tcpSession.getPackets()) {
				packetTimeStamp = p.getTimeStamp();
				if ((bExactMatch && (packetTimeStamp == timeStamp))
						|| ((packetTimeStamp >= (timeStamp - dTimeRangeInterval)) && (packetTimeStamp <= (timeStamp + dTimeRangeInterval)))) {
					matchedPacket = p;
				}
			}
		}else{
			for (PacketInfo p : tcpSession.getUDPPackets()) {
				packetTimeStamp = p.getTimeStamp();
				if ((bExactMatch && (packetTimeStamp == timeStamp))
						|| ((packetTimeStamp >= (timeStamp - dTimeRangeInterval)) && (packetTimeStamp <= (timeStamp + dTimeRangeInterval)))) {
					matchedPacket = p;
				}
			}
		}
		return matchedPacket;
	}

	/**
	 * Highlights the specified TCP session in the TCP flows table.
	 * 
	 * @param tcpSession
	 *            - The TCPSession object to be highlighted.
	 */
	public void setHighlightedTCP(TCPSession tcpSession) {
		getJTCPFlowsTable().selectItem(tcpSession);
	}

	/**
	 * Highlights the specified packet in the Packet view table.
	 * 
	 * @param packetInfo
	 *            The PacketInfo object representing the packet to be
	 *            highlighted.
	 **/
	public void setHighlightedPacketView(PacketInfo packetInfo) {
		if (packetInfo.getSession() != null) {
			setHighlightedTCP(packetInfo.getSession());
			getJPacketViewTableAsDataTable().selectItem(packetInfo);
		}
	}

	/**
	 * Selects the specified request/response object in the request/response
	 * list
	 * 
	 * @param rr
	 *            - The HttpRequestResponseInfo object to be selected in the
	 *            list.
	 */
	public void setHighlightedRequestResponse(HttpRequestResponseInfo rr) {
		if (rr != null) {
			setHighlightedTCP(rr.getSession());
			getJTCPFlowsContentTabbedPane().setSelectedComponent(
					getJHttpReqResPanel());
		}
		getJHttpReqResPanel().setHighlightedRequestResponse(rr);
	}

	/**
	 * Deselect any selected request/response object in the request/response list
	 * 
	 */
	public void resetHighlightedRequestResponse() {
		setHighlightedTCP(null);
		getJHttpReqResPanel().setHighlightedRequestResponse(null);
	}

	/**
	 * Resets the size of any panes that have been split so that they are of
	 * equal size.
	 * 
	 */
	public void resetSplitPanesAdvancedTabb() {
		internalPanel.setDividerLocation(0.5);
	}

	/**
	 * Returns the currently displayed graph panel.
	 * 
	 * @return An object containing the currently displayed graph panel.
	 */
	public GraphPanel getDisplayedGraphPanel() {
		return this.graphPanel;
	}

}
