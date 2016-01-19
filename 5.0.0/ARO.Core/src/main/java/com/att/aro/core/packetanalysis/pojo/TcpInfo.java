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

/**
 * ENUM to maintain the Packets TCP state information.
 */
public enum TcpInfo {
	/**
	 * TCP data information.
	 */
	TCP_DATA,
	/**
	 * TCP acknowledge.
	 */
	TCP_ACK,
	/**
	 * TCP establish.
	 */
	TCP_ESTABLISH,
	/**
	 * TCP close packet.
	 */
	TCP_CLOSE,
	/**
	 * TCP reset packet.
	 */
	TCP_RESET,
	/**
	 * TCP duplicate data.
	 */
	TCP_DATA_DUP,
	/**
	 * TCP duplicate acknowledge.
	 */
	TCP_ACK_DUP,
	/**
	 * TCP keep alive.
	 */
	TCP_KEEP_ALIVE,
	/**
	 * TCP keep alive acknowledge.
	 */
	TCP_KEEP_ALIVE_ACK,
	/**
	 * TCP zero window.
	 */
	TCP_ZERO_WINDOW,
	/**
	 * TCP window update.
	 */
	TCP_WINDOW_UPDATE,
	/**
	 * TCP data recover. 
	 */
	TCP_DATA_RECOVER,
	/**
	 * TCP acknowledge recover.
	 */
	TCP_ACK_RECOVER
}
