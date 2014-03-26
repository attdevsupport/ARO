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
	ExternalTcpdumpExecutor tcpdumpexe;
	int totalPacketCaptured = 0;
	String errorMsg;
	volatile boolean hasSetup = false;
	
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
	public String getErrorMessage(){
		return this.errorMsg;
	}
	/**
	 * Start Remote Virtual Interface (RVI), which is required before tcpdump can capture packet in device
	 * @throws Exception 
	 */
	public boolean setup(String serialNumber, String pcapFilePath) throws Exception{
		boolean success = false;
		this.pcapfilepath = pcapFilePath;
		//avoid setup RVI again for the same device => reuse previous session
		//multiple start/stop RVI cause device to start multiple pcap services and then deny access
		if(this.serialNumber.equals(serialNumber) && this.hasSetup){
			return true;
		}
		this.serialNumber = serialNumber;
		
		String data = runner.runCmd(new String[]{"bash","-c","rvictl -s "+serialNumber});
		if(data != null && data.contains("[SUCCEEDED]")){
			success = true;
			logger.info("RVI is started ok.");
		}else{
			logger.info("Failed to start RVI, now attempt to close previously open RVI first.");
			if(!stopRVI()){
				//sometime it fail to stop or it need sometime
				//in this case, we give it sometime and try again
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!stopRVI()){
					this.errorMsg = "Failed to stop previously running packet capture session, please restart ARO. \r\n If this is your second time seeing this message, please try restarting Machine.";
					return success;
				}
			}
			data = runner.runCmd(new String[]{"bash","-c","rvictl -s "+serialNumber});
			if(data != null && data.contains("[SUCCEEDED]")){
				success = true;
				logger.info("rvi setup is ok after second attempt.");
			}else{
				logger.warning("Failed to start RVI and now trying to load com.apple.rpmuxd");
				//failed? maybe because com.apple.rpmuxd is not loaded
				//try to load it
				runner.runCmd(new String[]{"bash","-c","echo "+this.sudoPassword+" | sudo -S launchctl load -w /System/Library/LaunchDaemons/com.apple.rpmuxd.plist"});
				data = runner.runCmd(new String[]{"bash","-c","rvictl -s "+serialNumber});
				if(data != null && data.contains("[SUCCEEDED]")){
					success = true;
					logger.info("RVI started after third attempt and loaded com.apple.rpmuxd");
				}else{
					this.errorMsg = "Failed to connect to device. \r\nTry disconnect your device and reconnect it back. \r\n If problem still exit, try again or restarting ARO or Machine.";
					logger.severe(this.errorMsg);
				}
			}
			
		}
		this.hasSetup = success;
		return success;
	}
	/**
	 * start new tcpdump command in background thread
	 * @throws Exception
	 */
	public void startCapture() throws Exception{
		logger.info("********** Starting tcpdump... **********");
		startDate = new Date();
		tcpdumpexe = new ExternalTcpdumpExecutor(this.pcapfilepath, sudoPassword, this.runner);
		tcpdumpexe.start();
		logger.info("************  Tcpdump started in background. ****************");
	}
	/**
	 * stop background thread that run tcpdump and destroy thread
	 */
	public void stopCapture(){
		//kill tcpdump process first
		if(tcpdumpexe != null){
			try{
				tcpdumpexe.stopTcpdump();
				this.totalPacketCaptured = tcpdumpexe.getTotalPacketCaptured();
				tcpdumpexe.interrupt();
			}catch(Exception ex){}
			tcpdumpexe = null;

			logger.info("destroyed tcpdump executor thread");
			
			
			if(this.totalPacketCaptured > 0){
				Date dt = pcaphelp.getFirstPacketDate(this.pcapfilepath);
				if(dt != null){
					this.startDate = dt;
					logger.info("RVI Set packet date to: "+dt.getTime());
				}
			}
			
		}
	}
	/**
	 * Stop RVI and stop packet capture.
	 */
	public void stop() throws IOException{
		
		this.stopCapture();
		
		if(!stopRVI()){
			//sometime it fail to stop or it need sometime
			//in this case, we give it sometime and try again
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stopRVI();
		}
		this.hasSetup = false;
	}
	boolean stopRVI() throws IOException{
		boolean success = false;
		String data = runner.runCmd(new String[]{"bash","-c","rvictl -x "+serialNumber});
		if(data != null && data.contains("[SUCCEEDED]")){
			logger.info("rvi is stopped successfully.");
			success = true;
		}else{
			logger.info("RVI failed to stop");
		}
		return success;
	}
	public Date getTcpdumpStartDate(){
		return this.startDate;
	}
	public int getTotalPacketCaptured(){
		return this.totalPacketCaptured;
	}
}//end class
