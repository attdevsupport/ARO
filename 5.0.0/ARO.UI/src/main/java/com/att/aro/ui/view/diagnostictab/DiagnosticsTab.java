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
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfoWithSession;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.mvc.IAROView;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.TabPanelJPanel;
import com.att.aro.ui.model.DataTable;
import com.att.aro.ui.model.diagnostic.PacketViewTableModel;
import com.att.aro.ui.model.diagnostic.TCPUDPFlowsTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.overviewtab.DeviceNetworkProfilePanel;
import com.att.aro.ui.view.video.AROVideoPlayer;

public class DiagnosticsTab extends TabPanelJPanel implements ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private ILogger logger = ContextAware.getAROConfigContext().getBean(ILogger.class);

	private static final int MAX_ZOOM = 4;
	private static final Double MATCH_SECONDS_RANGE = 0.5;
	// network profile panel
	private DeviceNetworkProfilePanel deviceNetworkProfilePanel;
	// Chart panel
	private GraphPanel graphPanel;
	// Split pane for TCP flow data
	private JSplitPane internalPanel;

	// TCP Flows header
	private JPanel tcpFlowsHeadingPanel;
	private JLabel tcpFlowsLabel;

	private DataTable<PacketInfo> jPacketViewTable;
	
	private RequestResponseDetailsPanel jHttpReqResPanel;
	private TCPFlowsDataTable<Session> tcpflowsTable;
	
	//Model
	private TCPUDPFlowsTableModel jTcpUdpFlowsModel = new TCPUDPFlowsTableModel();
	private PacketViewTableModel jPacketViewTableModel = new PacketViewTableModel();
	private List<HttpRequestResponseInfoWithSession> requestResponseWithSession =
			new ArrayList<HttpRequestResponseInfoWithSession>();
	private AROTraceData analyzerResult;


	public List<HttpRequestResponseInfoWithSession> getRequestResponseWithSession() {
		return requestResponseWithSession;
	}

	public void setRequestResponseWithSession(
			List<HttpRequestResponseInfoWithSession> requestResponseWithSession) {
		this.requestResponseWithSession = requestResponseWithSession;
	}

	// Components for TCP Flows scroll table
	private JPanel jTCPFlowsPanel;
	private JScrollPane jTCPFlowsScrollPane;

	// TCP flow detail tabbed pane
	private JTabbedPane jTCPFlowsContentTabbedPane;

	// Packet view
	private JScrollPane jPacketViewTapScrollPane;
	
	// Content view
	private ContentViewJPanel jContentViewPanel; // Content View

	private DiagnosticTabHelper diagHelper = new DiagnosticTabHelper();
	private AROVideoPlayer aroVideoPlayer;
	private List<Session> sessionsSortedByTimestamp = new ArrayList<Session>();

	public AROVideoPlayer getVideoPlayer() {
		return aroVideoPlayer;
	}
	
	public void setVideoPlayer(AROVideoPlayer videoPlayer) {
		aroVideoPlayer = videoPlayer;
	}

	private AROTraceData aroTraceData;
	
	public AROTraceData getAroTraceData() {
		return aroTraceData;
	}

	public void setAroTraceData(AROTraceData aroTraceData) {
		this.aroTraceData = aroTraceData;
	}
	
	private boolean graphPanelClicked = false;
	private boolean bTCPPacketFound = false;

	private IAROView aroview;
	
	public AROTraceData getAnalyzerResult() {
		return analyzerResult;
	}

	public void setAnalyzerResult(AROTraceData analyzerResult) {
		this.analyzerResult = analyzerResult;
	}
	public DiagnosticsTab(IAROView aroview) {
		super(true);
		this.aroview = aroview;
		setLayout(new BorderLayout());
		add(getDeviceNetworkProfilePanel().layoutDataPanel(), BorderLayout.NORTH);
		JPanel chartAndTablePanel = new JPanel();
		chartAndTablePanel.setLayout(new BorderLayout());
		// Add chart
		chartAndTablePanel.add(getGraphPanel(), BorderLayout.NORTH);
		// Add TCP flows split pane
		chartAndTablePanel.add(getOrientationPanel(), BorderLayout.CENTER);
		add(chartAndTablePanel, BorderLayout.CENTER);				

	}

	/**
	 * Returns the Panel that contains the graph.
	 */
	private GraphPanel getGraphPanel() {
		if ( graphPanel == null) {
			 graphPanel = new GraphPanel(aroview,this);
				graphPanel.setZoomFactor(2);
				graphPanel.setMaxZoom(MAX_ZOOM);
				graphPanel.addGraphPanelListener(new GraphPanelListener() {
					@Override
					public void graphPanelClicked(double timeStamp) {
//						logger.info("graphclicked: " + timeStamp);
							setTimeLineToTable(timeStamp);
						if (getVideoPlayer() != null) {
							graphPanelClicked = true;
//							logger.info("enter getGraphPanel() ");
							getVideoPlayer().setMediaDisplayTime(timeStamp);
//							logger.info("leave getGraphPanel() ");

						}
				}
				});
		}
		return  graphPanel;
	}
	
	private JSplitPane getOrientationPanel() {
		if (internalPanel == null) {
			
			internalPanel = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, getJTCPFlowsPanel(), getJTCPFlowsContentTabbedPane());
			internalPanel.setOneTouchExpandable(true);
			internalPanel.setContinuousLayout(true);
			internalPanel.setResizeWeight(0.5);
			internalPanel.setDividerLocation(0.5);
			
		}
		return internalPanel;
	}

	/**
	 * Initializes and returns the Tabbed pane at the bottom.
	 */
	private JTabbedPane getJTCPFlowsContentTabbedPane() {
		if (jTCPFlowsContentTabbedPane == null) {
			jTCPFlowsContentTabbedPane = new JTabbedPane();
			jTCPFlowsContentTabbedPane.addTab(ResourceBundleHelper.getMessageString("tcp.tab.reqResp"), null, getJHttpReqResPanel(), null);
			jTCPFlowsContentTabbedPane.addTab(ResourceBundleHelper.getMessageString("tcp.tab.packet"),  null, getJPacketViewTapScrollPane(), null);
			jTCPFlowsContentTabbedPane.addTab(ResourceBundleHelper.getMessageString("tcp.tab.content"), null, getJContentViewPanel(), null);
//			jTCPFlowsPanel.setPreferredSize(new Dimension(400, 110));
			jTCPFlowsPanel.setMinimumSize(new Dimension(400, 110));
		}
		return jTCPFlowsContentTabbedPane;
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
	 * Initializes and returns the Packet View Table.
	 */
	private DataTable<PacketInfo> getJPacketViewTable() {
		if (jPacketViewTable == null) {
			jPacketViewTable = new DataTable<PacketInfo>(jPacketViewTableModel);
			jPacketViewTable.setAutoCreateRowSorter(true);
			jPacketViewTable.setGridColor(Color.LIGHT_GRAY);
			jPacketViewTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						PacketInfo packetInfo;
						@Override
						public void valueChanged(
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
//								getJHttpReqResPanel().select(
//										packetInfo.getRequestResponseInfo());
								
								if (aroVideoPlayer != null) {
//									logger.info("enter getJPacketViewTable()");	
									aroVideoPlayer.setMediaDisplayTime(graphPanel
													.getCrosshair());
//									logger.info("leave  getJPacketViewTable()");	

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
	private ContentViewJPanel getJContentViewPanel() {
		if (jContentViewPanel == null) {
			jContentViewPanel = new ContentViewJPanel( );
		}
		return jContentViewPanel;
	}

	/**
	 * Initializes and returns the Device network profile panel.
	 */
	public DeviceNetworkProfilePanel getDeviceNetworkProfilePanel() {
		if (deviceNetworkProfilePanel == null) {
			deviceNetworkProfilePanel = new DeviceNetworkProfilePanel();
		}
		return deviceNetworkProfilePanel;
	}
	/**
	 * Initializes jTCPFlowsPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJTCPFlowsPanel() {
		if (jTCPFlowsPanel == null) {
			jTCPFlowsPanel = new JPanel();
			jTCPFlowsPanel.setLayout(new BorderLayout());
			jTCPFlowsPanel.add(getTcpFlowsHeadingPanel(), BorderLayout.NORTH);
			jTCPFlowsPanel.add(getJTCPFlowsScrollPane(), BorderLayout.CENTER);
//			jTCPFlowsPanel.setPreferredSize(new Dimension(400, 200));
			jTCPFlowsPanel.setMinimumSize(new Dimension(400, 200));
		}
		return jTCPFlowsPanel;
	}
	/**
	 * Creates the TCP Flows heading panel.
	 */
	private JPanel getTcpFlowsHeadingPanel() {
		if (tcpFlowsHeadingPanel == null) {
			tcpFlowsHeadingPanel = new JPanel();
			tcpFlowsHeadingPanel.setLayout(new GridBagLayout());
			tcpFlowsHeadingPanel.add(getTcpFlowsLabel());
			tcpFlowsHeadingPanel.setPreferredSize(new Dimension(110, 15));
		}
		return tcpFlowsHeadingPanel;
	}
	
	/**
	 * Returns the TCP flows label.
	 */
	private JLabel getTcpFlowsLabel() {
		if (tcpFlowsLabel == null) {
			tcpFlowsLabel = new JLabel(ResourceBundleHelper.getMessageString("tcp.title"));
		}
		return tcpFlowsLabel;
	}
	/**
	 * Initializes and returns the TCPFlowsScrollPane.
	 */
	private JScrollPane getJTCPFlowsScrollPane() {

			jTCPFlowsScrollPane = new JScrollPane(getJTCPFlowsTable());
			jTCPFlowsScrollPane.setPreferredSize(new Dimension(100, 200));

		return jTCPFlowsScrollPane;
	}
	/**
	 * Initializes and returns the Scroll Pane for the TCP flows table.
	 */
	
	public TCPFlowsDataTable<Session> getJTCPFlowsTable(){
		if (tcpflowsTable == null) {
			tcpflowsTable = new TCPFlowsDataTable<Session>(jTcpUdpFlowsModel);
			tcpflowsTable.setAutoCreateRowSorter(true);
			tcpflowsTable.setGridColor(Color.LIGHT_GRAY);
			tcpflowsTable.getSelectionModel().addListSelectionListener(this);
			//Adding the table listner for getting the check box changes //greg story 
			tcpflowsTable.getModel().addTableModelListener(new TableModelListener() {				
				@Override
				public void tableChanged(TableModelEvent arg0) {
					graphPanel.setTraceAnalysis();								

				}
			});	
		}
		return tcpflowsTable;
		
	}

	/**
	 * Returns RequestResponseDetailsPanel.
	 * @return the jHttpReqResPanel
	 */
	public RequestResponseDetailsPanel getJHttpReqResPanel() {
		if (jHttpReqResPanel == null) {
			jHttpReqResPanel = new RequestResponseDetailsPanel();
		}
		return jHttpReqResPanel;
	}

	//get update info from core model
	@Override
	public void refresh(AROTraceData AnalyzerResult) {	
		analyzerResult = AnalyzerResult;
		setAroTraceData(analyzerResult);
		getDeviceNetworkProfilePanel().refresh(analyzerResult);
		jTcpUdpFlowsModel.refresh(analyzerResult);
		RowSorter<TCPUDPFlowsTableModel> sorter = new TableRowSorter<TCPUDPFlowsTableModel>(jTcpUdpFlowsModel);
		getJTCPFlowsTable().setRowSorter(sorter);

		sessionsSortedByTimestamp = analyzerResult.getAnalyzerResult().getSessionlist();
		setRequestResponseWithSession(buildHttpRequestResponseWithSession(
				analyzerResult.getAnalyzerResult().getSessionlist()));
		getGraphPanel().refresh(analyzerResult);
		//clear table
		jPacketViewTableModel.removeAllRows();
		getJHttpReqResPanel().getjRequestResponseTableModel().removeAllRows();
		getJContentViewPanel().getJContentTextArea().setText("");
		
	}
	
	public void setChartOptions(List<ChartPlotOptions> optionsSelected) {
		 getGraphPanel().setChartOptions(optionsSelected);
	}

	//get update info from tcp/udp flow table
	@Override
	public void valueChanged(ListSelectionEvent evt) {

		if(evt.getSource() instanceof ListSelectionModel){
			ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
			if(lsm.getMinSelectionIndex() !=-1){
				Session session = getJTCPFlowsTable().getSelectedItem();
				if(session==null){
					jPacketViewTableModel.removeAllRows();
					getJHttpReqResPanel().getjRequestResponseTableModel().removeAllRows();
					getJContentViewPanel().getJContentTextArea().setText("");

				}else{
	  				if(session.isUDP()){
	  					jPacketViewTableModel.setData(session.getUDPPackets());
	  					getJPacketViewTable().setGridColor(Color.LIGHT_GRAY);
						if (!session.getUDPPackets().isEmpty()) {
							getJPacketViewTable().getSelectionModel()
									.setSelectionInterval(0, 0);
						}
						if (jTCPFlowsContentTabbedPane
								.getSelectedComponent() == getJContentViewPanel()) {
							getJContentViewPanel().updateContext(session);
						}
						getJContentViewPanel().getJContentTextArea().setCaretPosition(0);
						getJHttpReqResPanel().updateTable(session);
	 				}else{
	 					jPacketViewTableModel.setData(session.getPackets());
	 					getJPacketViewTable().setGridColor(Color.LIGHT_GRAY);
	 					if(!session.getPackets().isEmpty()){
	 						getJPacketViewTable().getSelectionModel().setSelectionInterval(0, 0);
	 					}
	 					getJContentViewPanel().updateContext(session);
						getJContentViewPanel().getJContentTextArea().setCaretPosition(0);
	  					getJHttpReqResPanel().updateTable(session);
	 				}
				}
 			}
		}
	}
	
	public HttpRequestResponseInfo getRrAssoSession(Session session){
		HttpRequestResponseInfo reqInfo = null; 
		for (HttpRequestResponseInfoWithSession reqResSession : requestResponseWithSession) {
			if (reqResSession.getSession().equals(session)) {
				reqInfo =  reqResSession.getInfo() ;
  				break; 
			}
		}
		return reqInfo;
 	}
	
	/**
	 * TODO:  This belongs in core!  As a matter of fact, it's mostly copied and pasted from
	 * internal code in CacheAnalysisImpl
	 * 
	 * @param sessions
	 * @return
	 */
	private List<HttpRequestResponseInfoWithSession> buildHttpRequestResponseWithSession(
			List<Session> sessions) {
		List<HttpRequestResponseInfoWithSession> returnList =
				new ArrayList<HttpRequestResponseInfoWithSession>();
		for (Session session : sessions) {
			if(!session.isUDP()){
				for(HttpRequestResponseInfo item: session.getRequestResponseInfo()){
					HttpRequestResponseInfoWithSession itemsession =
							new HttpRequestResponseInfoWithSession();
					itemsession.setInfo(item);
					itemsession.setSession(session);
					returnList.add(itemsession);
				}
			} 
		}
		Collections.sort(returnList);
		return Collections.unmodifiableList(returnList);
	}

	private void setHighlightedPacket(Session session) {
		requestResponseWithSession.contains(session);
		for (HttpRequestResponseInfoWithSession reqResSession : requestResponseWithSession) {
			if (reqResSession.getSession().equals(session)) {
				getJPacketViewTable().selectItem(reqResSession.getInfo().getFirstDataPacket());
				break;
			}
		}
	}

	public  void setTimeLineLinkedComponents(double timeStamp,boolean isReset) {
			if (getAroTraceData() != null) {
				if (timeStamp < 0.0) {
					timeStamp = 0.0;
				}
				double traceDuration = getAroTraceData().getAnalyzerResult().getTraceresult().getTraceDuration();
				if (timeStamp > traceDuration) {
					timeStamp = traceDuration;
				}
				getGraphPanel().setGraphView(timeStamp,isReset);
			}
		
	}
	
	//old analyzer method name is setTimeLineLinkedComponents(double timeStamp,double dTimeRangeInterval)
	public void setTimeLineToTable(double timeStamp){
//		logger.info("enter setTimeLineTable()");
				if (getAroTraceData() == null) {
					logger.info("no analyze traces data");
				}else{
					
			 		boolean bTCPTimeStampFound = false;
					boolean bExactMatch = false;
			
					// Attempt to find corresponding packet for time.
					double packetTimeStamp = 0.0;
					double packetTimeStampDiff = 0.0;
					double previousPacketTimeStampDiff = 9999.0;
					Session bestMatchingTcpSession = null;
					PacketInfo bestMatchingPacketInfo = null;
//					logger.info("enter sesionlist for loop");
					for (Session tcpSess : getAroTraceData().getAnalyzerResult().getSessionlist()) {
						PacketInfo packetInfo = diagHelper.getBestMatchingPacketInTcpSession(
								tcpSess, bExactMatch, timeStamp, MATCH_SECONDS_RANGE);
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
//					logger.info("leave sesionlist for loop");

					if (bTCPTimeStampFound) {
						getJTCPFlowsTable().selectItem(bestMatchingTcpSession);
						getJPacketViewTable().selectItem(bestMatchingPacketInfo);
						getJPacketViewTable().setGridColor(Color.LIGHT_GRAY);
		 
					} else {
						getJTCPFlowsTable().selectItem(null);
						getJPacketViewTable().selectItem(null);						
	//					if (aroVideoPlayer != null) {
	//						bTCPPacketFound = false;
	//						aroVideoPlayer.setMediaDisplayTime(graphPanel
	//								.getCrosshair());
	//					}
	//				}					
					}			
		}		
//		logger.info("leave setTimeLineTable()");
	}
	public boolean getTCPPacketFoundStatus(){
		return bTCPPacketFound;
	}
	
	public void reSetTCPPacketFoundStatus(boolean val){
		bTCPPacketFound = val;
	}
	
	/**
	 * Highlights the specified TCP session in the TCP flows table.
	 * 
	 * @param tcpSession
	 *            - The TCPSession object to be highlighted.
	 */
	public void setHighlightedTCP(Session tcpSession) {
		
		getJTCPFlowsTable().selectItem(tcpSession);		
	
 	}

	public void setHighlightedTCP(HttpRequestResponseInfo reqResInfo) {
		for (HttpRequestResponseInfoWithSession reqResSession : requestResponseWithSession) {
			if (reqResSession.getInfo().equals(reqResInfo)) {
				Session sessionTemp = reqResSession.getSession();
				logger.info("local port = "+ sessionTemp.getLocalPort());
				setHighlightedTCP(reqResSession.getSession());
				jHttpReqResPanel.setHighlightedRequestResponse(reqResInfo);
				break;
			}
		}
	}
	
	//only UnnecessaryConnectionEntry table use this method 
	public void setHighlightedTCP(Double timestampParm) {
		if (timestampParm != null) {
			double timestamp = timestampParm.doubleValue();
			double timestampDiff = Double.MAX_VALUE;
			double lastTimestampDiff = timestampDiff;
			Session foundSession = null;
			for (Session tcpSess : sessionsSortedByTimestamp) {
				if (tcpSess != null) {
					double currentTimestampDiff = Math.abs(tcpSess.getSessionStartTime() -
							timestamp);
					if (currentTimestampDiff < timestampDiff) {
						timestampDiff = currentTimestampDiff;
						foundSession = tcpSess;
					}

					if (currentTimestampDiff > lastTimestampDiff) {
						break;
					}
					lastTimestampDiff = currentTimestampDiff;
				}
			}
			if (foundSession != null) {
				//setHighlightedTCP(foundSession, timestamp);
				setHighlightedTCP(foundSession);
			}
			else {
				logger.warn("No session found to route to Diagnostic Tab for timestamp " +
						timestamp);
			}
		}
		else {
			logger.warn("No timestamp for Diagnostic Tab routing");
		}
	}

	@Override
	public JPanel layoutDataPanel() {				
		return null;
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

	public void updateTcpTable(){
		
	}
}
