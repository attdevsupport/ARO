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

package com.att.aro.console.util;

import java.io.PrintStream;

public final class UtilOut {
	private PrintStream out = System.out;
	private PrintStream err = System.err;
	private MessageThreshold threshold = MessageThreshold.Verbose;

	public enum MessageThreshold {
		Quiet,
		Normal,
		Verbose
	}

	/**
	 * print string to console
	 * @param str
	 */
	public static void out(String str){
		System.out.print(str);
	}
	/**
	 * print string to console with new line char
	 * @param str
	 */
	public static void outln(String str){
		System.out.println(str);
	}

	public static void err(String str) {
		System.err.print(str);
	}

	public static void errln(String str) {
		System.err.println(str);
	}


	public UtilOut(MessageThreshold threshold) {
		this.threshold = threshold;
	}
	public UtilOut() {
		this(MessageThreshold.Verbose);
	}

	public MessageThreshold getThreshold() {
		return threshold;
	}


	private static void outIfAppropriate(String str, PrintStream out, MessageThreshold threshold) {
		if (threshold != null && threshold != MessageThreshold.Quiet && threshold.ordinal() >= threshold.ordinal()) {
			out.print(str);
		}
	}
	private static void outlnIfAppropriate(String str, PrintStream out,
			MessageThreshold threshold) {
		if (threshold != null && threshold != MessageThreshold.Quiet && threshold.ordinal() >= threshold.ordinal()) {
			out.println(str);
		}
	}

	public void outMessage(String str, MessageThreshold threshold) {
		outIfAppropriate(str, out, threshold);
	}
	public void outMessage(String str) {
		outMessage(str, MessageThreshold.Verbose);
	}

	public void outMessageln(String str, MessageThreshold threshold) {
		outlnIfAppropriate(str, out, threshold);
	}
	public void outMessageln(String str) {
		outMessageln(str, MessageThreshold.Verbose);
	}

	public void errMessage(String str) {
		err.print(str);
	}
	public void errMessageln(String str) {
		err.println(str);
	}
}
