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

package com.att.aro.datacollector.ioscollector.utilities;

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.datacollector.ioscollector.reader.ExternalProcessRunner;
import com.att.aro.datacollector.ioscollector.reader.ExternalTcpdumpExecutor;
import com.att.aro.datacollector.ioscollector.reader.PcapHelper;

/**
 * For Mac OS machine. This class start/stop Remote Virtual Interface. It also
 * start tcpdump which is capturing packet and save it as .pcap file. This class
 * require Sudo password to perform tcpdump command.
 */
public class RemoteVirtualInterface {
	private ILogger log = new LoggerImpl("IOSCollector");
	String serialNumber = "";
	String sudoPassword = "";//we need to ask user for password to run some command on Mac
	ExternalProcessRunner runner = null;
	Date initDate;
	Date startDate;
	Date stopDate;
	String pcapfilepath;
	PcapHelper pcaphelp = null;
	ExternalTcpdumpExecutor tcpdumpExecutor;
	int totalPacketCaptured = 0;
	String errorMsg;
	volatile boolean hasSetup = false;
	private boolean launchDaemonsExecuted;
	private String rviName;

	@Autowired
	public void setLogger(ILogger logger) {
		this.log = logger;
	}

	public RemoteVirtualInterface(String sudoPassword) {
		this.sudoPassword = sudoPassword;
		runner = new ExternalProcessRunner();
		pcaphelp = new PcapHelper(runner);
		startDate = new Date();
		initDate = startDate;
	}

	public RemoteVirtualInterface(String sudoPassword, ExternalProcessRunner runner) {
		this.sudoPassword = sudoPassword;
		this.runner = runner;
		startDate = new Date();
		initDate = startDate;

	}

	public String getErrorMessage() {
		return this.errorMsg;
	}

	/**
	 * <pre>
	 * Start Remote Virtual Interface (RVI), which is required before tcpdump
	 * can capture packet in device. 
	 * 
	 * sequence:
	 * 	1 very LaunchDaemons are launched
	 *  2 make 10 attempts at connecting
	 *  
	 *  Failed attempts cause a disconnect and a pause before attempting another connect
	 * 
	 * @throws Exception
	 */
	public boolean setup(String serialNumber, String pcapFilePath) throws Exception {
		

		launchDaemons();
		
		boolean success = false;
		this.pcapfilepath = pcapFilePath;
		// avoid setup RVI again for the same device => reuse previous session
		// multiple start/stop of RVI causes device to start multiple pcap services and then deny access
		if (this.serialNumber.equals(serialNumber) && this.hasSetup) {
			return true;
		}
		
		this.serialNumber = serialNumber;
		
		String connect = "-s";
		String disconnect = "-x";

		for (int attempt = 0; attempt < 10; attempt++) {
			if (rviConnect(connect, serialNumber)) {
				success = true;
				log.info("RVI is started ok.");
				break;
			} else {
				log.info("RVI failed :" + attempt + " time(s)");
				rviConnect(disconnect, serialNumber);
				Thread.sleep(500);
			}
		}		
		
		if (!success){
			this.errorMsg = "Failed to connect to device. \r\nTry disconnect your device and reconnect it back. \r\n If problem still exit, try again or restarting ARO or Machine.";
			log.error(this.errorMsg);
		}
		
		this.hasSetup = success;
		return success;
	}

	private void launchDaemons() {
		if (!launchDaemonsExecuted) {
			String[] cmnd = new String[] { "bash", "-c", "echo " + this.sudoPassword 
					+ " | sudo -S launchctl load -w /System/Library/LaunchDaemons/com.apple.rpmuxd.plist" };
			try {
				runner.runCmd(cmnd);
				launchDaemonsExecuted = true;
			} catch (IOException e) {
				log.error("IOException", e);
			}
		}
	}

	/**
	 * 
	 * Start or Stop rvictl connection to device
	 * 
	 * @param mode
	 *            -s start, -x close
	 * @param serialNumber
	 *            of iOS device
	 * @return true if succeeded, false if failed
	 */
	private boolean rviConnect(String mode, String serialNumber) {
		String cmdResponse;
		try {
			cmdResponse = runner.runCmd("rvictl " + mode + " " + serialNumber );
		} catch (IOException e) {
			log.error("IOException", e);
			return false;
		}
		if (cmdResponse != null && cmdResponse.contains("[SUCCEEDED]")) {
			//data.trim();
			String[] splitResponse = cmdResponse.split("interface");
			if (splitResponse.length > 1) {
				setRviName(splitResponse[1].trim());
			}else{
				setRviName(findDeviceInRvictl(serialNumber));
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Disconnect iOSdevice serialnumber from all rvi network interfaces
	 * 
	 * @param serialNumber
	 * @return 
	 */
	public void disconnectFromRvi(String serialNumber) {
		// TODO needs to handle multiple device serialnumbers
		String response = null;
		try {
			do {
				rviConnect("-x", serialNumber);
				response = runner.runCmd("rvictl -l");
			} while (!response.trim().equals("Could not get list of devices"));
		} catch (IOException e) {
			log.error("IOException", e);
		}
	}

	/**
	 * Execute list of rvi devices and filter for serialnumber
	 * 
	 * @param serialNumber
	 * @return rvi device associated with serialNumber
	 */
	private String findDeviceInRvictl(String serialNumber) {
		String data;
		try {
			data = runner.runCmd("rvictl -l | grep " + serialNumber + " |awk -F \"interface \" '{print $2}'");
		} catch (IOException e) {
			log.error("IOException", e);
			return null;
		}
		return data;
	}

	/**
	 * start new tcpdump command in background thread
	 * 
	 * @throws Exception
	 */
	public void startCapture() throws Exception {
		
		log.info("********** Starting tcpdump... **********");
		
		startDate = new Date();
		initDate = startDate;
		tcpdumpExecutor = new ExternalTcpdumpExecutor(this.pcapfilepath, sudoPassword, this.runner);
		tcpdumpExecutor.start();
		
		log.info("************  Tcpdump started in background. ****************");
	}

	/**
	 * stop background thread that run tcpdump and destroy thread
	 */
	public void stopCapture() {
		
		log.info("********** Stop tcpdump... **********");

		if (tcpdumpExecutor != null) {
			try {
				tcpdumpExecutor.stopTcpdump();
				stopDate = new Date();
				this.totalPacketCaptured = tcpdumpExecutor.getTotalPacketCaptured();
				tcpdumpExecutor.interrupt();
			} catch (Exception ex) {
			}
			tcpdumpExecutor = null;

			log.info("destroyed tcpdump executor thread");

			if (this.totalPacketCaptured > 0) {
				Date dt = pcaphelp.getFirstPacketDate(this.pcapfilepath);
				if (dt != null) {
					this.startDate = dt;
					log.info("RVI Set packet date to: " + dt.getTime());
				}
			}

		}
	}

	/**
	 * Stop RVI and stop packet capture.
	 */
	public void stop() throws IOException {

		this.stopCapture();
		disconnectFromRvi(serialNumber);
		this.hasSetup = false;
	}

	public Date getTcpdumpInitDate() {
		return this.initDate;
	}

	public Date getTcpdumpStartDate() {
		return this.startDate;
	}

	public Date getTcpdumpStopDate() {
		return this.stopDate;
	}

	public int getTotalPacketCaptured() {
		return this.totalPacketCaptured;
	}

	public String getRviName() {
		return rviName;
	}

	public void setRviName(String rviName) {
		this.rviName = rviName;
	}
}//end class
