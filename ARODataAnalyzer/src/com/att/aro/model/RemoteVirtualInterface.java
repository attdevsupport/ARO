/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
/**
 * For Mac OS machine. This class start/stop Remote Virtual Interface. 
 * It also start tcpdump which is capturing packet and save it as .pcap file.
 * This class require Sudo password to perform tcpdump command.
 */
public class RemoteVirtualInterface {
	private static final Logger logger = Logger.getLogger(RemoteVirtualInterface.class.getName());
	String serialNumber = "";
	String sudoPassword = "";//we need to ask user for password to run some command on Mac
	ExternalProcessRunner runner = null;
	Date startDate;
	String pcapfilepath;
	PcapHelper pcaphelp = null;
	public RemoteVirtualInterface(String sudoPassword){
		this.sudoPassword = sudoPassword;
		runner = new ExternalProcessRunner();
		pcaphelp = new PcapHelper(runner);
		startDate = new Date();
	}
	public RemoteVirtualInterface(String sudoPassword, ExternalProcessRunner runner){
		this.sudoPassword = sudoPassword;
		this.runner = runner;
		startDate = new Date();
		
	}
	/**
	 * Start Remote Virtual Interface (RVI) and run tcpdump command to capture packet data and save it as .pcap file.
	 */
	public boolean start(String serialNumber, String pcapFilePath) throws IOException{
		boolean success = false;
		this.serialNumber = serialNumber;
		String data = runner.runCmd(new String[]{"bash","-c","rvictl -s "+serialNumber});
		if(data != null && data.contains("[SUCCEEDED]")){
			success = true;
			logger.info("rvi is started ok.");
		}else{
			logger.warning("Failed to start RVI and now trying to load com.apple.rpmuxd");
			//failed? maybe because com.apple.rpmuxd is not loaded
			//try to load it
			runner.runCmd(new String[]{"bash","-c","echo "+this.sudoPassword+" | sudo -S launchctl load -w /System/Library/LaunchDaemons/com.apple.rpmuxd.plist"});
			data = runner.runCmd(new String[]{"bash","-c","rvictl -s "+serialNumber});
			if(data != null && data.contains("[SUCCEEDED]")){
				success = true;
				logger.info("RVI started after second attemp and loaded com.apple.rpmuxd");
			}else{
				logger.severe("Failed to start RVI event after second attempt to load com.apple.rpmuxd");
			}
		}
		if(success){
			logger.info("********** Starting tcpdump...");
			startTcpdump(pcapFilePath);
		}
		return success;
	}
	private void startTcpdump(String pcapFilePath) throws IOException{
		this.pcapfilepath = pcapFilePath;
		String cmd = "echo "+ this.sudoPassword +" | sudo -S tcpdump -i rvi0 -s 0 -w "+pcapFilePath;
		this.startDate = new Date();
		String data = runner.runCmd(new String[]{"bash","-c",cmd});
		logger.info("************  Tcpdump started.");
	}
	/**
	 * Stop RVI and stop packet capture.
	 */
	public void stop() throws IOException{
		String data = runner.runCmd(new String[]{"bash","-c","rvictl -x "+serialNumber});
		if(data != null && data.contains("[SUCCEEDED]")){
			logger.info("rvi is stopped successfully.");
		}else{
			logger.info("RVI failed to stop");
		}
		Date dt = pcaphelp.getFirstPacketDate(this.pcapfilepath);
		if(dt != null){
			this.startDate = dt;
			logger.info("RVI Set packet date to: "+dt.getTime());
		}
	}
	public Date getTcpdumpStartDate(){
		return this.startDate;
	}
}//end class
