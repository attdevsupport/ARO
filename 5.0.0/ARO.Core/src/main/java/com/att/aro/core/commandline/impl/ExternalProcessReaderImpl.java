/*
 *  Copyright 2015 AT&T
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
package com.att.aro.core.commandline.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.commandline.IExternalProcessReader;
import com.att.aro.core.commandline.IExternalProcessReaderSubscriber;

public class ExternalProcessReaderImpl implements Runnable, IExternalProcessReader {

	private InputStream inputStream;
	private List<IExternalProcessReaderSubscriber> pubsub;
	private volatile boolean willStop = false;

	@Override
	public void setInputStream(InputStream stream) {
		this.inputStream = stream;
		pubsub = new ArrayList<IExternalProcessReaderSubscriber>();
	}

	@Override
	public void addSubscriber(IExternalProcessReaderSubscriber subscriber) {
		this.pubsub.add(subscriber);
	}

	@Override
	public void setStop() {
		this.willStop = true;
	}


	@Override
	public void out(String message) {
		for (IExternalProcessReaderSubscriber sub : pubsub) {
			sub.newMessage(message);
		}
	}

	private void notifyExit() {
		for (IExternalProcessReaderSubscriber sub : pubsub) {
			sub.willExit();
		}
	}

	@Override
	public void removeSubscriber(IExternalProcessReaderSubscriber sub) {
		if (this.pubsub.contains(sub)) {
			this.pubsub.remove(sub);
		}
	}

	@Override
	public void run() {
		String line = null;
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			while (!willStop) {
				line = bufferedReader.readLine();

				if (line == null) {
					break;
				} else {
					line = line.trim();
					if (line.length() > 0) {
						out(line);
					}
				}
			}
		} catch (IOException exception) {
			out("Error: " + exception.getMessage());
		}
		notifyExit();
	}

}
