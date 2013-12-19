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
package com.att.aro.main;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import com.att.aro.commonui.DataCollectorFolderDialog;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.interfaces.ImageSubscriber;
import com.att.aro.model.ExternalProcessRunner;
import com.att.aro.model.IOSDeviceInfo;
import com.att.aro.model.RemoteVirtualInterface;
import com.att.aro.model.TraceData;
import com.att.aro.model.UDIDReader;
import com.att.aro.model.XCodeInfo;
import com.att.aro.util.ImageHelper;
import com.att.aro.util.Util;
import com.att.aro.videocapture.VideoCaptureMacOS;

public class DataCollectorMacOS implements ImageSubscriber {
	private static final Logger logger = Logger.getLogger(DataCollectorMacOS.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	/**
	 * ARO analyzer instance that is to be notified of data collector status
	 * updates
	 */
	private ApplicationResourceOptimizer mainAROAnalyzer;
	
	String sudoPassword = "";//sudo password is required for run some commands on Mac
	
	RemoteVirtualInterface rvi = null;
	
	private LiveScreenViewDialog liveview;
	
	private VideoCaptureMacOS videoCapture;
	
	private XCodeInfo xcode = null;
	
	private boolean hasRVI = false;
	
	private SwingWorker<String,Object> videoworker;
	
	private SwingWorker<String,Object> packetworker;
	
	private String datadir = "";//dir to save pcap file, video, etc
	
	private File videofile;
	private IOSDeviceInfo deviceinfo;
	
	/**
	 * Indicates local directory where trace results will be stored
	 */
	private File localTraceFolder;
	
	public DataCollectorMacOS(ApplicationResourceOptimizer aro){
		this.mainAROAnalyzer = aro;
		liveview = new LiveScreenViewDialog();
		deviceinfo = new IOSDeviceInfo();
	}
	/**
	 * Main method to start data collection for IOS device connected to Mac OS
	 * @throws InterruptedException 
	 */
	public void startCollector(){
		if(xcode == null){
			xcode = new XCodeInfo();
		}
		if(!hasRVI){
			hasRVI = xcode.isRVIAvailable();
		}
		if(!hasRVI){
			//please install the latest version of XCode to continue.
			MessageDialogFactory.showErrorDialog(null, rb.getString("Error.xcoderequired"));
			return;
		}else{
			logger.info("Found rvictl command");
		}
		UDIDReader reader = new UDIDReader();
		String udid = null;
		try {
			udid = reader.getSerialNumber();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("Error occured trying to get Serial Number of device. "+e.getMessage());
			MessageDialogFactory.showErrorDialog(null, rb.getString("Error.getserialnumberdevice"));
			return;
		}
		if(udid == null || udid.length() < 2){
			//Failed to get Serial Number of Device, connect an IOS device to start.
			MessageDialogFactory.showErrorDialog(null, rb.getString("Error.getserialnumberconnectios"));
			return;
		}
		
		if(this.sudoPassword == ""){
			//Enter sudo password for ARODataAnalyer to perform some important functions: 
			String password = MessageDialogFactory.showInputPassword(null, rb.getString("Message.adminrightrequired"), rb.getString("Message.entersudopassword"));
			
			if(password == null || password.length() < 1){
				//Without sudo password, ARODataAnalyzer cannot do important task such as packet capture, setting up Remote Virutal Interface and so on.
				MessageDialogFactory.showErrorDialog(null, rb.getString("Error.nosudopassword"));
				return;
			}
			if(isValidSudoPassword(password)){
				this.sudoPassword = password;
			}else{
				//There was a problem with password. Try again.
				MessageDialogFactory.showErrorDialog(null, rb.getString("Error.sudopasswordissue"));
				return;
			}
		}
		//ask user where to save pcap file to
		
		DataCollectorFolderDialog folder = new DataCollectorFolderDialog(this.mainAROAnalyzer);
		folder.setVisible(true);
		
		String dirname = folder.getDirectoryName();
		if(dirname.length() < 1){
			//Folder/Directory name is required to save packet data to. Try again.
			MessageDialogFactory.showMessageDialog(null, rb.getString("Error.foldernamerequired"));
			return;
		}
		String dirroot = Util.getAROTraceDirIOS();
		datadir = dirroot + Util.FILE_SEPARATOR + dirname;
		localTraceFolder = new File(datadir);
		if(!localTraceFolder.exists()){
			if(!localTraceFolder.mkdirs()){
				datadir = "";
				//There was an error creating directory: 
				MessageDialogFactory.showErrorDialog(null, rb.getString("Error.createdirectory")+datadir);
				return;
			}
			
		}
		
		
		final String filepath = datadir + Util.FILE_SEPARATOR + Util.TRAFFIC_FILE; //"traffic.pcap";
		rvi = new RemoteVirtualInterface(this.sudoPassword);
		final String serialNumber = udid;
		
		//device info
		
		
		packetworker = new SwingWorker<String,Object>(){

			@Override
			protected String doInBackground() throws Exception {
				rvi.start(serialNumber, filepath);
				return null;
			}
			
		};
		packetworker.execute();
		
		
		if(videoCapture == null){
			final String videofilepath = datadir + Util.FILE_SEPARATOR + TraceData.VIDEO_MOV_FILE;
			videofile = new File(videofilepath);
			try {
				videoCapture = new VideoCaptureMacOS(videofile);
			} catch (IOException e) {
				e.printStackTrace();
				logger.severe(e.getMessage());
				MessageDialogFactory.showErrorDialog(null,"Failed to create video capture: "+e.getMessage());
			}
			videoCapture.setWorkingFolder(datadir);
			videoCapture.addSubscriber(this);
		}
		videoworker = new SwingWorker<String,Object>(){
			@Override
			protected String doInBackground() throws Exception {
				if(videoCapture != null){
					videoCapture.start();
				}
				return null;
			}
			
		};
		videoworker.execute();
		
		//device info
		String deviceinfofile = datadir + Util.FILE_SEPARATOR + "device_details";
		deviceinfo.getDeviceInfo(udid, deviceinfofile);
		
		mainAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.STARTED);
		liveview.setAlwaysOnTop(true);
		liveview.setVisible(true);
		//user stop video capture by closing the preview screen
		this.stopCollector();
		
			
	}
	/**
	 * Check if the password provided is correct
	 * @param pass sudoer password
	 * @return true if password is correct otherwise false
	 */
	private boolean isValidSudoPassword(String pass){
		ExternalProcessRunner runner = new ExternalProcessRunner();
		String cmd = "echo "+pass+" | sudo -k -S cat /etc/sudoers";
		String data = null;
		try {
			data = runner.runCmd(new String[]{"bash","-c",cmd});
		} catch (IOException e) {
			e.printStackTrace();
			//There was an error validating password.
			MessageDialogFactory.showErrorDialog(null, rb.getString("Error.validatepassword"));
			return false;
		}
		if(data != null){
			data = data.trim();
		}
		if(data != null && data.length() > 1 && !data.contains("incorrect password attempts")){
			return true;
		}
		return false;
	}
	public void stopCollector(){
		
		if(rvi !=  null){
			try {
				rvi.stop();
			} catch (IOException e) {
				e.printStackTrace();
				logger.severe("Failed to stop RVI. Error: "+e.getMessage());
			}
		}
		if(packetworker != null){
			packetworker.cancel(true);
			packetworker = null;
			logger.info("disposed packetworker");
		}
		if(videoCapture != null){
			videoCapture.stopCapture();
			
			//create video timestamp file that will sync with pcap file
			BufferedWriter videoTimeStampWriter = null;
			
			
			try {
				logger.info("Writing video time to file");
				videoTimeStampWriter = new BufferedWriter(
						new FileWriter(new File(localTraceFolder,TraceData.VIDEO_TIME_FILE)));
				// Writing a video time in file.
				String timestr = Double.toString(videoCapture.getVideoStartTime().getTime()/1000.0);
				//append time from tcpdump
				timestr += " " + Double.toString(rvi.getTcpdumpStartDate().getTime()/1000.0);
				videoTimeStampWriter.write(timestr);
				
			} catch (IOException e) {
				e.printStackTrace();
				logger.info("Error writing vide time to file: "+e.getMessage());
			} finally {
				try {
					videoTimeStampWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(videoCapture.isAlive()){
				videoCapture.interrupt();
			}
			videoCapture = null;
			logger.info("disposed videoCapture");
		}
		if(videoworker != null){
			videoworker.cancel(true);
			videoworker = null;
			logger.info("disposed videoworker");
		}
		mainAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.READY);
		if(datadir.length() > 1){
			File folder = new File(datadir);
			if(folder.exists()){
				logger.info("opening trace folder: "+datadir);
				mainAROAnalyzer.openTraceFolder(folder);
			}
		}
	}
	@Override
	public void receiveImage(BufferedImage image) {
		if(liveview.isVisible()){
			BufferedImage newimg = ImageHelper.resize(image, liveview.getViewWidth(),liveview.getViewHeight());
			liveview.setImage(newimg);
		}
	}
}//end class
