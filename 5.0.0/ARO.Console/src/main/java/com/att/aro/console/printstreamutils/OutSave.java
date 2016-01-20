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

import org.apache.log4j.Level;

public class OutSave {
	private final PrintStream out;
	private final Level level;

	public OutSave(PrintStream out, Level level) {
		this.out = out;
		this.level = level;
	}

	public PrintStream getOut() {
		return out;
	}
	public Level getLevel() {
		return level;
	}


	@Override
	public String toString() {
		return "OutSave [out=" + out + ", level=" + level + "]";
	}
}
