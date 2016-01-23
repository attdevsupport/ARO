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
