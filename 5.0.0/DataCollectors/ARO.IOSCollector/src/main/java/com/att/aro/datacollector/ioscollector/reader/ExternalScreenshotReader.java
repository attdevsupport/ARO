/*
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

package com.att.aro.datacollector.ioscollector.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.datacollector.ioscollector.IScreenshotPubSub;

public class ExternalScreenshotReader extends Thread {
	

	LoggerImpl log = new LoggerImpl(this.getClass().getName());
	
	private InputStream is;
	List<IScreenshotPubSub> pubsub;

	public ExternalScreenshotReader(InputStream stream) {
		this.is = stream;
		pubsub = new ArrayList<IScreenshotPubSub>();
	}

	public void addSubscriber(IScreenshotPubSub subscriber) {
		pubsub.add(subscriber);
	}

	@Override
	public void run() {
		String line = null;
		try {
			InputStreamReader reader = new InputStreamReader(is);
			BufferedReader bufferedReader = new BufferedReader(reader);

			// connection recognized here
			while (true) {
				line = bufferedReader.readLine();
				log.debug("    >>"+line);
				if (line == null) {
					break;
				} else {
					line = line.trim();
					if (line.length() > 0) {
						updateSubsribers(line);
					}
				}
			}
			
		} catch (IOException exception) {
			log.error("IOException - updateSubsribers :", exception);
			updateSubsribers("Error: " + exception.getMessage());
		}
		updateSubsribers(">Exited ExternalProcessReader");
		notifyExit();
	}

	void updateSubsribers(String str) {
		for (IScreenshotPubSub pub : pubsub) {
		//	log.debug("pub to subscriber :"+pub.getClass().getName()+ " str:"+str);
			pub.newMessage(str);
		}
	}

	void notifyExit() {
		for (IScreenshotPubSub pub : pubsub) {
		//	log.debug("notify willExit :"+pub.getClass().getName());
			pub.willExit();
		}
	}
}
