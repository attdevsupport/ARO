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
package com.att.aro.model;

/**
 * The BurstInfo Enumeration specifies constant values that describe the different 
 * categories of bursts that occur when data is transferred. 
 */
public enum BurstInfo {
	/**
	 * A back log burst. 
	 */
	BURST_BKG,
	/**
	 * A finishing burst. 
	 */
	BURST_FIN,
	/**
	 * A synchronize burst. 
	 */
	BURST_SYN,
	/**
	 * A Reset burst. 
	 */
	BURST_RST,
	/**
	 * A KeepAlive burst. 
	 */
	BURST_KEEPALIVE,
	/**
	 * ZeroWin Burst State.
	 */
	BURST_ZEROWIN,
	/**
	 * A ZeroWin burst.
	 */
	BURST_WINUPDATE,
	/**
	 * A Recovered burst. This is part of the Loss burst category that consists of the 
	 * packets that are lost while being transferred.
	 */
	BURST_LOSS_RECOVER,
	/**
	 * A Duplicate burst. This is part of the Loss burst category that consists of the 
	 * packets that are lost while being transferred. 
	 */
	BURST_LOSS_DUP,
	/**
	 * A burst initiated by user input. 
	 */
	BURST_USER_INPUT,
	/**
	 * Burst initiated because of User input.
	 */
	BURST_SCREEN_ROTATION_INPUT,
	/**
	 * A server delayed burst. 
	 */
	BURST_SERVER_DELAY,
	/**
	 * A Client delayed burst. 
	 */
	BURST_CLIENT_DELAY,
	/**
	 * A Long burst has a duration of more than 5 seconds, and typically transfers large 
	 * amounts of data.
	 */
	BURST_LONG,
	/**
	 * A burst that occurs when the CPU is busy 
	 */
	BURST_CPU_BUSY,
	/**
	 * A Periodical Burst. If the Internet Addresses, host names, or object names are the 
	 * same for the packets in a set burst over a period of time, then those bursts are 
	 * considered Periodical bursts. 
	 */
	BURST_PERIODICAL,
	/**
	 * User defined burst type 1.
	 */
	BURST_USERDEF1,
	/**
	 * User defined burst type 2.
	 */
	BURST_USERDEF2,
	/**
	 * User defined burst type 3.
	 */
	BURST_USERDEF3,
	/**
	 * A burst of an unknown type. 
	 */
	BURST_UNKNOWN;
}
