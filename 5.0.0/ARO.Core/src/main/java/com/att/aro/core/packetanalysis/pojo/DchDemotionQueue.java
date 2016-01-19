/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.packetanalysis.pojo;

import com.att.aro.core.configuration.pojo.Profile3G;
import com.att.aro.core.packetreader.pojo.PacketDirection;

/**
 * Helper class for RrcStateRange to handles the calculation related to DCH State and DCH Tail State.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 29, 2014
 */
public class DchDemotionQueue {
	private int dchDemotionQueueUL = -1;
	private int dchDemotionQueueDL = -1;
	private double dchTimerResetTS;
	private double dchLastPktTSUL;
	private double dchLastPktTSDL;
	private Profile3G profile;
	/**
	 * Constructor
	 * 
	 * @param profile
	 */
	public DchDemotionQueue(Profile3G profile) {
		this.profile = profile;
	}

	/**
	 * Initialize the DCH state information.
	 * 
	 * @param tStamp
	 *            time stamp where DCH starts.
	 * @param size
	 *            size of the consumed during that DCH.
	 * @param dir
	 *            Direction of the DCH UPLINK/DOWNLINK.
	 */
	public void init(double tStamp, int size, PacketDirection dir) {
		if (dir == PacketDirection.UPLINK) {
			dchDemotionQueueUL = size;
			dchDemotionQueueDL = 0;
			dchLastPktTSUL = tStamp;
			dchLastPktTSDL = -9999.0f;
		} else if (dir == PacketDirection.DOWNLINK) {
			dchDemotionQueueUL = 0;
			dchDemotionQueueDL = size;
			dchLastPktTSDL = tStamp;
			dchLastPktTSUL = -9999.0f;
		}

		dchTimerResetTS = tStamp;
	}

	/**
	 * Updates the RRC information in existing RRC state.
	 * 
	 * @param tStamp
	 * @param size
	 * @param dir
	 */
	public void update(double tStamp, int size, PacketDirection dir) {
		if (dir == PacketDirection.UPLINK) {
			if (tStamp > dchLastPktTSUL + profile.getDchTimerResetWin()) {
				dchDemotionQueueUL = size;
			} else {
				dchDemotionQueueUL += size;
			}
			if (tStamp > dchLastPktTSDL + profile.getDchTimerResetWin()) {
				dchDemotionQueueDL = 0;
			}
			dchLastPktTSUL = tStamp;
		}

		else if (dir == PacketDirection.DOWNLINK) {
			if (tStamp > dchLastPktTSDL + profile.getDchTimerResetWin()) {
				dchDemotionQueueDL = size;
			} else {
				dchDemotionQueueDL += size;
			}
			if (tStamp > dchLastPktTSUL + profile.getDchTimerResetWin()) {
				dchDemotionQueueUL = 0;
			}
			dchLastPktTSDL = tStamp;

		}

		if ((dchDemotionQueueUL >= profile.getDchTimerResetSize())
				|| (dchDemotionQueueDL >= profile.getDchTimerResetSize())) {
			dchTimerResetTS = tStamp;
		}
	}

	public double getDCHTail(double tStamp) {
		double lastTS = Math.max(dchLastPktTSDL, dchLastPktTSUL);
		
		return profile.getDchFachTimer() - (lastTS - dchTimerResetTS);
	}
}
