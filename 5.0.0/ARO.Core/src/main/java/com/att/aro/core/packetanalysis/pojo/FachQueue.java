/**
 *  Copyright 2016 AT&T
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
 */

public class FachQueue {
	private Profile3G profile;
	private int ulQueue, dlQueue;
	private double ulTS, dlTS;

	/**
	 * Constructor.
	 * 
	 * @param profile
	 */
	public FachQueue(Profile3G profile) {
		this.profile = profile;
	}

	/**
	 * Initialize the FACH data.
	 */
	public void init() {
		ulQueue = dlQueue = 0;
		ulTS = dlTS = -1000.0f;
	}

	/**
	 * return true if it triggers a FACH->DCH promotion
	 * 
	 * @param tStamp
	 * @param dir
	 * @param size
	 * @return
	 */
	public boolean simFACH(double tStamp, PacketDirection dir, int size) {
		if (dir == PacketDirection.UPLINK) {
			// MyAssert(ts >= ulTS, 113);
			double queueConsumptionTime = (ulQueue * ulQueue * profile.getRlcUlRateP2()
					+ ulQueue * profile.getRlcUlRateP1() + profile.getRlcUlRateP0()) / 1000.0f;
			if (tStamp - ulTS > queueConsumptionTime) {
				ulQueue = 0;
			}

			// if (ts - ulTS > 0.2f) ulQueue = 0;

			ulQueue += size;
			ulTS = tStamp;

		}

		else if (dir == PacketDirection.DOWNLINK) {
			// MyAssert(ts >= dlTS, 114);
			double queueConsumptionTime = (dlQueue * dlQueue * profile.getRlcDlRateP2()
					+ dlQueue * profile.getRlcDlRateP1() + profile.getRlcDlRateP0()) / 1000.0f;
			if (tStamp - dlTS > queueConsumptionTime) {
				dlQueue = 0;
			}

			dlQueue += size;
			dlTS = tStamp;

		}

		return ((dlQueue > profile.getRlcDlTh()) || (ulQueue > profile.getRlcUlTh()));
	}
}
