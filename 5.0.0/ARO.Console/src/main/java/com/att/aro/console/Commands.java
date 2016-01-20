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

package com.att.aro.console;

import com.beust.jcommander.Parameter;

public class Commands {
	@Parameter(names = {"--help","-h","-?"}, description="show help menu", help=true)
	private boolean help = false;
	
	@Parameter(names= "--startcollector", description="start collector on device or emulator")
	private String startcollector = null;
	
	@Parameter(names = "--analyze", description="analyze trace file or folder")
	private String analyze = null;
	
	@Parameter(names="--output", description="provide output location of report")
	private String output = null;
	
	@Parameter(names="--format", description="format of report: json or html")
	private String format = "json";
	
	@Parameter(names="--deviceid", description="device id or serial number for device to run collector on")
	private String deviceid = null;
	
	@Parameter(names="--video", description="yes or no - record video while capturing trace")
	private String video = "no";
	
	@Parameter(names="--sudo", description="admin password, OSX only")
	private String sudo = "";
	
	public String getSudo() {
		return sudo;
	}

	public void setSudo(String sudo) {
		this.sudo = sudo;
	}

	@Parameter(names="--listcollector", description="list available data collector")
	private boolean listcollector = false;

	@Parameter(names="--verbose", description="verbose output - more than just the important messages")
	private boolean verbose = false;

	
	public boolean isListcollector() {
		return listcollector;
	}

	public void setListcollector(boolean listcollector) {
		this.listcollector = listcollector;
	}

	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public String getStartcollector() {
		return startcollector;
	}

	public void setStartcollector(String startcollector) {
		this.startcollector = startcollector;
	}

	public String getAnalyze() {
		return analyze;
	}

	public void setAnalyze(String analyze) {
		this.analyze = analyze;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public boolean isVerbose() {
		return verbose;
	}


}
