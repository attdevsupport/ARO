//package com.att.aro.bp.smallrequest;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//
//import com.att.aro.model.HttpRequestResponseInfo;
//import com.att.aro.model.HttpRequestResponseInfo.Direction;
//import com.att.aro.model.PacketInfo;
//import com.att.aro.model.TCPSession;
//
//public class SmallRequestAnalysis {
//	private static Logger LOGGER = Logger.getLogger(SmallRequestAnalysis.class.getName());
//
//	private static final int FILE_SIZE_LIMIT = 300;
//	private List<SmallRequestEntry> analysisResults = new ArrayList<SmallRequestEntry>();
//
//	/**
//	 * Represents SmallRequest analysis.
//	 * 
//	 */
//	public SmallRequestAnalysis(List<TCPSession> tcpSessions) {
//
//		if (null != tcpSessions) {
//			// loop through TCP session
//			for (TCPSession tcpSession : tcpSessions) {
//				
//				double jscssLastTimeStamp = 0.0;
//				HttpRequestResponseInfo prevReqRessInfo = null;
//				
//				// loop through HTTP requests and responses
//				for (HttpRequestResponseInfo reqRessInfo : tcpSession.getRequestResponseInfo()) {
//					if (reqRessInfo.getDirection() == Direction.RESPONSE && reqRessInfo.getContentType() != null) {
//						PacketInfo pktInfo = reqRessInfo.getFirstDataPacket();
//						if (pktInfo != null) {
//							if (reqRessInfo.getContentLength() < FILE_SIZE_LIMIT 
//									&& (reqRessInfo.getContentType().equalsIgnoreCase("text/css")
//									|| reqRessInfo.getContentType().equalsIgnoreCase("text/javascript") 
//									|| reqRessInfo.getContentType().equalsIgnoreCase("application/x-javascript")
//									|| reqRessInfo.getContentType().equalsIgnoreCase("application/javascript"))) {
//								if (jscssLastTimeStamp == 0.0) {
//									jscssLastTimeStamp = pktInfo.getTimeStamp();
//									prevReqRessInfo = reqRessInfo;
//								} else {
//									if ((pktInfo.getTimeStamp() - jscssLastTimeStamp) <= 10.0) {
//										if (prevReqRessInfo != null) {
//											analyzeContent(prevReqRessInfo);											
//										}
//										analyzeContent(reqRessInfo);
//										prevReqRessInfo = null;
//									} else {
//										jscssLastTimeStamp = pktInfo.getTimeStamp();
//										prevReqRessInfo = reqRessInfo;
//									}
//								}
//							}
//						}
//					}		
//				}
//			}
//		}
//	}
//
//	private void analyzeContent(HttpRequestResponseInfo rr) {
//		this.analysisResults.add(new SmallRequestEntry(rr));
//	}
//
//	public boolean isTestPassed() {
//		return this.analysisResults.size() > 0 ? false : true;   
//	}
//
//	public int getNumberOfSmallRequests() {
//		return this.analysisResults.size();
//	}
//
//	public List<SmallRequestEntry> getResults() {
//		return this.analysisResults;
//	}
//}
