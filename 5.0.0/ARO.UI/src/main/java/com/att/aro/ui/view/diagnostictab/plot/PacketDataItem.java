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
package com.att.aro.ui.view.diagnostictab.plot;

import java.text.MessageFormat;
import java.util.List;

import org.jfree.data.xy.YIntervalDataItem;

import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class PacketDataItem extends YIntervalDataItem{
 
 
	private static final long serialVersionUID = 3220866154637112824L;
	private static final String TOOLTIP_PREFIX = ResourceBundleHelper.getMessageString("packet.tooltip.prefix");
	private static final String PACKET_TOOLTIP = ResourceBundleHelper.getMessageString("packet.tooltip.packet");
	private static final String SESSION_TOOLTIP = ResourceBundleHelper.getMessageString("packet.tooltip.session");
	private static final String RR_TOOLTIP = ResourceBundleHelper.getMessageString("packet.tooltip.reqresp");
	private static final String TOOLTIP_SUFFIX = ResourceBundleHelper.getMessageString("packet.tooltip.suffix");

	private String tooltip;
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * Returns the tooltip text for the plot.
	 * 
	 * @return The tooltip text for the plot.
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Initializes a new instance of the PacketPlots class.
	 * 
	 * @param packet
	 *            The PacketInfo instance containing the the various
	 *            informations about the packet to be plotted.
	 */
	public PacketDataItem(Session session,PacketInfo packet) {
		super(packet.getTimeStamp(), 0, 0, 1);

		// Build tooltip message
		StringBuffer displayInfo = new StringBuffer(1000);
		displayInfo.append(TOOLTIP_PREFIX);
		// Packet info for tooltip
		displayInfo.append(MessageFormat.format(PACKET_TOOLTIP,
				packet.getPacketId(), packet.getTimeStamp(),
				checkNull(packet.getAppName())));

		// Session info for tooltip
		if (session != null) {
			List<PacketInfo> packets = session.getPackets();
			double beginTime = packets.get(0).getTimeStamp();
			double endTime = packets.get(packets.size() - 1).getTimeStamp();

			displayInfo.append(MessageFormat.format(SESSION_TOOLTIP,
					beginTime, endTime, session.getRemoteIP()
							.getHostAddress(),
					 Integer.toString(session.getRemotePort()),
					 Integer.toString(session.getLocalPort())));
		}
		
		// Delete the httpinfo from the 4.1.1 code,verified redundancy.
		
		displayInfo.append(TOOLTIP_SUFFIX);
		setTooltip(displayInfo.toString());

	}
	private static String checkNull(String s) {
		return s != null ? s : "";
	}

}
