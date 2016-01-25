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

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.att.aro.android.arocollector.SessionManager;

/**
 * Publish packet data to subscriber who implements interface IReceivePacket
 * @author Borey Sao
 * Date: June 15, 2014
 */
public class SocketDataPublisher implements Runnable {
	List<IReceivePacket> subscribers;
	SocketData data;
	private volatile boolean isshuttingdown = false;
	public SocketDataPublisher(){
		data = SocketData.getInstance();
		subscribers = new ArrayList<IReceivePacket>();
	}
	/**
	 * register a subscriber who wants to receive packet data
	 * @param subscriber
	 */
	public void subscribe(IReceivePacket subscriber){
		if(!subscribers.contains(subscriber)){
			subscribers.add(subscriber);
		}
	}
	@Override
	public void run() {
		Log.d(SessionManager.TAG,"BackgroundWriter starting...");
		
		while(!isshuttingdown){
			byte[] packetdata = data.getData();
			if(packetdata != null){
				for(IReceivePacket subscriber: subscribers){
					subscriber.receive(packetdata);
				}
				//dispose it after use
				packetdata = null;
			}else{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		Log.d(SessionManager.TAG,"BackgroundWriter ended");
	}
	public boolean isShuttingdown() {
		return isshuttingdown;
	}
	public void setIsshuttingdown(boolean isshuttingdown) {
		this.isshuttingdown = isshuttingdown;
	}

	
}
