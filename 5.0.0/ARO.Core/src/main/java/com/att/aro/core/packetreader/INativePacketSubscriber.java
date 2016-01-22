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
package com.att.aro.core.packetreader;

/**
 * receive packet from the native lib
 */
public interface INativePacketSubscriber {

	/**
	 * Receive raw packet data as read from traffic file.
	 * 
	 * @param datalink
	 * @param seconds
	 * @param microSeconds
	 * @param len
	 * @param data
	 */
	void receive(int datalink, long seconds, long microSeconds, int len, byte[] data);
}
