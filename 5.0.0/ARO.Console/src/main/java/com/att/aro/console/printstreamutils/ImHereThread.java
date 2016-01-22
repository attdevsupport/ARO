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

package com.att.aro.console.printstreamutils;

import java.io.PrintStream;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Implements a terminal/console-based "I'm here" indicator.
 * 
 * @author Nathan F Syfrig
 *
 */
public class ImHereThread implements Runnable {
	private final Logger logger;
	private final Thread imHereThread;
	private final OutSave outSave;

	private final int sleepTime = 250;
	private boolean running = false;
	private boolean runflag = false;
	private boolean paused = false;

	private String[] sequence = {
			"*\b",
			"-\b",
			"\\\b",
			"|\b",
			"/\b",
			"-\b"
	};
	private byte sequenceIndex = 0;

	public ImHereThread(PrintStream printStream, Logger logger) {
		this.logger = logger;
		outSave = new OutSave(printStream, logger.getLevel());

		imHereThread = new Thread(this);
		imHereThread.start();
	}

	@Override
	public void run() {
		PrintStream outPrintStream = outSave.getOut();
		runflag = true;
		running = true;
		while (running) {
			logger.setLevel(Level.OFF);
			outPrintStream.print(sequence[sequenceIndex++]);
			logger.setLevel(outSave.getLevel());
			sequenceIndex %= sequence.length;
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				running = false;
			}
		}
		runflag = false;
	}

	public void endIndicator() {
		running = false;
		imHereThread.interrupt();
	}

	public boolean isRunning() {
		return runflag;
	}


	@Override
	public String toString() {
		return "ImHereThread [logger=" + logger + ", imHereThread="
				+ imHereThread + ", outSave=" + outSave + ", sleepTime="
				+ sleepTime + ", running=" + running + ", runflag=" + runflag
				+ ", paused=" + paused + ", sequence="
				+ Arrays.toString(sequence) + ", sequenceIndex="
				+ sequenceIndex + "]";
	}

}
