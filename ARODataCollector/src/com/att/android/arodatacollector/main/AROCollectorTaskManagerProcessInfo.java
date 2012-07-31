/*
 * Copyright 2012 AT&T
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

package com.att.android.arodatacollector.main;

import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

import com.att.android.arodatacollector.utils.AROCollectorUtils;

/**
 * Represents task killer process information for the ARO Data Collector. This
 * class maintains a collection of AROCollectorTaskManagerProcessInfo.PsRow
 * classes that contain native shell command (PS) output.
 */

public class AROCollectorTaskManagerProcessInfo {

	/** Logging string for the ARO Collector TaskManagerProcessInfo class. */
	public static final String TAG = "AROCollectorTaskManagerProcessInfo";

	/**
	 * The boolean value to enable logs depending on if production build or
	 * debug build
	 */
	private static boolean mIsProduction = true;

	/**
	 * A boolean value that indicates whether or not to enable logging for this
	 * class in a debug build of the ARO Data Collector.
	 */
	public static boolean DEBUG = !mIsProduction;

	/** process list list array */
	private ArrayList<PsRow> pslist;

	/** ARO Data Collector utilities class object */
	private AROCollectorUtils mAROUtlis;

	/**
	 * Initializes a new instance of the AROCollectorTaskManagerProcessInfo
	 * class.
	 */
	public AROCollectorTaskManagerProcessInfo() {
		mAROUtlis = new AROCollectorUtils();
		ps();
	}

	/**
	 * Runs the shell PS command to get the all running processes
	 */
	private void ps() {
		final String ps = mAROUtlis.runCommand("ps");
		final String[] lines = ps.split("\n");
		pslist = new ArrayList<PsRow>();
		for (String line : lines) {
			final PsRow row = new PsRow(line);
			if (row.pid != null)
				pslist.add(row);
		}
	}

	/**
	 * Returns a value indicating whether or not tcpdump is running on the
	 * native shell.
	 * 
	 * @return A boolean value that is "true" if tcpdump is running on the
	 *         native shell, and is "false" otherwise.
	 */
	public boolean pstcpdump() {

		boolean isTcpDumpRunning = false;
		try {
			isTcpDumpRunning = (mAROUtlis.getProcessID("tcpdump") > 0 ? true : false);

			if (DEBUG) {
				Log.d(TAG, "isTcpDumpRunning: " + isTcpDumpRunning);
			}
			return (isTcpDumpRunning);
		} catch (IOException e) {
			Log.e(TAG, e.getClass().getName() + " thrown by pstcpdump()");
		} catch (InterruptedException e) {
			Log.e(TAG, e.getClass().getName() + " thrown by pstcpdump()");
		}
		return isTcpDumpRunning;
	}

	/**
	 * Returns the specified row of task killer process information. Each row
	 * represents the output from a native shell command.
	 * 
	 * @param cmd
	 *            A string containing the native shell command to return the
	 *            output for.
	 * @return An AROCollectorTaskManagerProcessInfo.PsRow object containing the
	 *         output for the specified native shell command.
	 */
	public PsRow getPsRow(String cmd) {
		for (PsRow row : pslist) {
			if (cmd.equals(row.cmd)) {
				return row;
			}
		}
		return null;
	}

	/**
	 * Represents one row of PS (native shell command) data for the ARO Data
	 * Collector task killer.
	 * 
	 */
	public static class PsRow {
		String pid = null;
		String cmd;
		String ppid;
		String user;
		int mem;

		/**
		 * Initializes a new instance of the
		 * AROCollectorTaskManagerProcessInfo.PsRow class by parsing the
		 * specified native shell command output string.
		 * 
		 * @param line
		 *            The native shell command output string to be parsed.
		 */
		public PsRow(String line) {
			if (line == null)
				return;
			final String[] p = line.split("[\\s]+");
			if (p.length != 9)
				return;
			user = p[0];
			pid = p[1];
			ppid = p[2];
			cmd = p[8];
			mem = AROCollectorUtils.parseInt(p[4]);
		}

	}

}
