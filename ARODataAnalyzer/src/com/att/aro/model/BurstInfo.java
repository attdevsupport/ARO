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
 * Different Burst types.
 */
public enum BurstInfo {
	/**
	 * Back log Burst.
	 */
	BURST_BKG,
	/**
	 * Finishing Burst.
	 */
	BURST_FIN,
	/**
	 * Synchronize Burst.
	 */
	BURST_SYN,
	/**
	 * Burst Reset.
	 */
	BURST_RST,
	/**
	 * Alive Burst.
	 */
	BURST_KEEPALIVE,
	/**
	 * ZeroWin Burst State.
	 */
	BURST_ZEROWIN,
	/**
	 * Window update Burst.
	 */
	BURST_WINUPDATE,
	/**
	 * Recovered Burst.
	 */
	BURST_LOSS_RECOVER,
	/**
	 * Duplicate Burst.
	 */
	BURST_LOSS_DUP,
	/**
	 * Burst initiated because of User input.
	 */
	BURST_USER_INPUT,
	/**
	 * Server delayed Burst.
	 */
	BURST_SERVER_DELAY,
	/**
	 * Client delayed Burst.
	 */
	BURST_CLIENT_DELAY,
	/**
	 * Long Burst transfers heavy data.
	 */
	BURST_LONG,
	/**
	 * CPU buys Burst.
	 */
	BURST_CPU_BUSY,
	/**
	 * Periodical Burst which keep on repeats in some delay.
	 */
	BURST_PERIODICAL,
	/**
	 * User input Burst 1.
	 */
	BURST_USERDEF1,
	/**
	 * User input Burst 2.
	 */
	BURST_USERDEF2,
	/**
	 * User input Burst 3.
	 */
	BURST_USERDEF3,
	/**
	 * Unknown Burst state.
	 */
	BURST_UNKNOWN;
}
