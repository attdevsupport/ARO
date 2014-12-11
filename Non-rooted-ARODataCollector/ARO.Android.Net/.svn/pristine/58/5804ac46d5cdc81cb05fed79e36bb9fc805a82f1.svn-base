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
package com.att.aro.android.arocollector.socket;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Singleton data structure for storing packet data in queue. Data is pushed into this queue from 
 * VpnService as well as background worker that pull data from remote socket.
 * @author Borey Sao
 * Date: May 12, 2014
 */
public class SocketData {
	private static Object syncObj = new Object();
	private static Object syncData = new Object();
	private volatile static SocketData instance = null;
	private Queue<byte[]> data;
	public static SocketData getInstance(){
		if(instance == null){
			synchronized(syncObj){
				if(instance == null){
					instance = new SocketData();
				}
			}
		}
		return instance;
	}
	private SocketData(){
		data = new LinkedList<byte[]>();
	}
	public void addData(byte[] packet){
		synchronized(syncData){
			byte[] copy = new byte[packet.length];
			System.arraycopy(packet, 0, copy, 0, packet.length);
			try{
				data.add(copy);
			}catch(IllegalStateException ex){
				
			}catch(NullPointerException ex1){
				
			}catch(Exception ex2){}
		}
	}
	public byte[] getData(){
		byte[] packet = null;
		synchronized(syncData){
			packet = data.poll();
		}
		return packet;
	}
}//end class
