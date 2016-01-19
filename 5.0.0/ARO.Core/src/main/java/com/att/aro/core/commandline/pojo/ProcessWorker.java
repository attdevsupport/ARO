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
package com.att.aro.core.commandline.pojo;

public class ProcessWorker implements Runnable {

	private final Process process;
	long timeout = 100;
	volatile boolean exit = false;
	int maxcount = 1;

	public ProcessWorker(Process process, long timeout) {
		this.process = process;
		if (timeout > this.timeout) {
			this.timeout = timeout;
			maxcount = (int) (timeout / 100);
		}

		this.exit = false;
	}

	public void setExit() {
		this.exit = true;
	}

	@Override
	public void run() {
		int counter = 0;
		while (!exit) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			counter++;
			if (counter >= maxcount) {
				if (process != null) {
					process.destroy();
				}
				break;
			}
		}
	}

	/**
	 * getStatus, true = running, false = stopped or stopping
	 * @return
	 */
	public boolean isRunning(){
		return !this.exit;
	}
}
