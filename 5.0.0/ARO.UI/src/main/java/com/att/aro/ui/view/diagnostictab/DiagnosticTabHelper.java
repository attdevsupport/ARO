package com.att.aro.ui.view.diagnostictab;

import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public class DiagnosticTabHelper {
	public DiagnosticTabHelper(){}
	/**
	 * Provides the Best matching packet info from the provided tcp session.
	 */
	PacketInfo getBestMatchingPacketInTcpSession( Session tcpSession,
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

}
