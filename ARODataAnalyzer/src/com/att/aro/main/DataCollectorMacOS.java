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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

import com.att.aro.commonui.AROProgressDialog;
import com.att.aro.commonui.DataCollectorFolderDialog;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.interfaces.IOSDeviceStatus;
import com.att.aro.interfaces.ImageSubscriber;
import com.att.aro.model.ExternalDeviceMonitorIOS;
import com.att.aro.model.ExternalProcessRunner;
import com.att.aro.model.IOSDeviceInfo;
import com.att.aro.model.RemoteVirtualInterface;
import com.att.aro.model.TraceData;
import com.att.aro.model.UDIDReader;
import com.att.aro.model.XCodeInfo;
import com.att.aro.util.ImageHelper;
import com.att.aro.util.Util;
import com.att.aro.videocapture.VideoCaptureMacOS;

public class DataCollectorMacOS implements ImageSubscriber, IOSDeviceStatus {
	private static final Logger logger = Logger.getLogger(DataCollectorMacOS.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	/**
	 * ARO analyzer instance that is to be notified of data collector status
	 * updates
	 */
	private ApplicationResourceOptimizer mainAROAnalyzer;
	
	String sudoPassword = "";//sudo password is required for run some commands on Mac
	
	RemoteVirtualInterface rvi = null;
	
	ExternalDeviceMonitorIOS monitor = null;
	
	private LiveScreenViewDialog liveview;
	
	private VideoCaptureMacOS videoCapture;
	
	private XCodeInfo xcode = null;
	
	private boolean hasRVI = false;
	
	private boolean hasxCodeV = false;
	
	private SwingWorker<String,Object> videoworker;
	
	private SwingWorker<String,Object> packetworker;
	
	private String datadir = "";//dir to save pcap file, video, etc
	
	private File videofile;
	private IOSDeviceInfo deviceinfo;
	volatile boolean isDeviceConnected = false;
	/**
	 * Indicates local directory where trace results will be stored
	 */
	private File localTraceFolder;
	
	
	/**** progress box ****/
	final AROProgressDialog waitbox;
	
	public DataCollectorMacOS(ApplicationResourceOptimizer aro){
		this.mainAROAnalyzer = aro;
		liveview = new LiveScreenViewDialog();
		deviceinfo = new IOSDeviceInfo();
		waitbox = new AROProgressDialog(mainAROAnalyzer, "Please wait...");
	}
	/**
	 * Main method to start data collection for IOS device connected to Mac OS
	 */
	public void startCollector(){
		if(monitor == null){
			monitor = new ExternalDeviceMonitorIOS();
			monitor.subscribe(DataCollectorMacOS.this);
			monitor.start();
			logger.info("Started device monitoring");
		}
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
			if(!hasxCodeV){
				if(xcode.isXcodeAvailable()){
					hasxCodeV = xcode.isXcodeSupportedVersionInstalled(); 
					if(!hasxCodeV){
						MessageDialogFactory.showErrorDialog(null, "Installed xcode is not supported pl install latest version.");
						return;
					}
				}else{
					MessageDialogFactory.showErrorDialog(null, "xCode is not installed in the machine. pl install xcode.");
					return;
				}
				logger.info("Found rvictl command");
			}
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
			
			if(password == null){
				return; //clicked on cancel
			}else if(password.length() < 1){
				//Without sudo password, ARODataAnalyzer cannot do important task such as packet capture, setting up Remote Virutal Interface and so on.
				MessageDialogFactory.showErrorDialog(null, rb.getString("Error.nosudopassword"));
				return;
			}
			
			password = Util.escapeRegularExpressionChar(password);
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
		if(folder.isCancelled()){
			return;
		}
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
			
		}else{
			//ask user before overriding existing contents
			int answer = MessageDialogFactory.showConfirmDialog(mainAROAnalyzer, "Trace folder already exists, do you want to override it?",JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION){
				//removed existing contents
				String fnames = "";
				for(File file : localTraceFolder.listFiles()){
					file.delete();
					fnames += file.getAbsolutePath() + "\r\n";
				}
				//logger.info("Folder exist and user want to override => removed existing contents: "+fnames);
			}else{
				return;
			}
		}
		
		
		final String filepath = datadir + Util.FILE_SEPARATOR + Util.TRAFFIC_FILE; //"traffic.pcap";
		
		//device info
		String deviceinfofile = datadir + Util.FILE_SEPARATOR + "device_details";
		if(!deviceinfo.getDeviceInfo(udid, deviceinfofile)){
			MessageDialogFactory.showErrorDialog(null, "Is your device unlocked and turned on? we failed to get your device info.");
			return;
		}else{
			String version = deviceinfo.getDeviceVersion(); //get device version number
			logger.info("Version :" +version);
			if(version != null && version.length() > 0){
				int versionNumber =0;
				int dotIndex = 0;
				try{
					dotIndex = version.indexOf(".");
					versionNumber = Integer.parseInt(version.substring(0, dotIndex));
					logger.info("Parsed Version Number : "+versionNumber);
				}catch(NumberFormatException nfe){
					MessageDialogFactory.showErrorDialog(null, " Not able to get connected device version.");
					return;
				}
				if(versionNumber < 5){
					MessageDialogFactory.showErrorDialog(mainAROAnalyzer, " ARO supports only iOS 5 and above devices.");
					return;
				}
			}
					
		}
		
		if(rvi == null){
			rvi = new RemoteVirtualInterface(this.sudoPassword);
		}
		final String serialNumber = udid;
		
		try {
			if(!rvi.setup(serialNumber, filepath)){
				MessageDialogFactory.showErrorDialog(null, rvi.getErrorMessage());
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			MessageDialogFactory.showErrorDialog(null, e1.getMessage());
			return;
		}
		
		packetworker = new SwingWorker<String,Object>(){

			@Override
			protected String doInBackground() throws Exception {
				rvi.startCapture();
				return null;
			}
			@Override
			protected void done(){
				try{
					String res = get();
				}catch(Exception ex){
					logger.info("Error thrown by packetworker: "+ex.getMessage());
				}
			}
		};
		packetworker.execute();
		
		if(folder.isCaptureVideo()){
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
				@Override
				protected void done(){
					try{
						String res = get();
					}catch(Exception ex){
						logger.info("Error thrown by videoworker: "+ex.getMessage());
					}
				}
			};
			videoworker.execute();
		}
		
		
		mainAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.STARTED);
		
		if(folder.isCaptureVideo()){
			liveview.setAlwaysOnTop(true);
			liveview.setVisible(true);
			
			if(isDeviceConnected){
				logger.info("device is connected");
			}else{
				logger.info("Device not connected");
			}
			this.stopCollector();
		}
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
		showWaitBox();
		this.stopWorkers();
		hideWaitBox();
		if(datadir.length() > 1){
			File folder = new File(datadir);
			if(folder.exists()){
				//if video data is zero, java media player will throw exception
				if(videofile != null && videofile.exists() && videofile.length() < 2){
					videofile.delete();
					logger.info("deleted empty video file");
				}
				//now check for pcap existence otherwise there will be popup error.
				File pcapfile = new File(datadir + Util.FILE_SEPARATOR + Util.TRAFFIC_FILE);
				if(pcapfile.exists()){
					logger.info("opening trace folder: "+datadir);
					
					try{
						mainAROAnalyzer.openTraceFolder(folder);
					}catch(Exception ex){
						logger.severe("Failed to open trace first time, will try again in 2 s, error: "+ex.getMessage());
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mainAROAnalyzer.openTraceFolder(folder);
					}
				}else{
					MessageDialogFactory.showMessageDialog(null, "No Data Packet was captured.");
				}
			}
		}
	}
	void stopWorkers(){
		if(videoCapture != null){
			videoCapture.signalStop();//no waiting for now
		}
		if(rvi !=  null){
			rvi.stopCapture();
		}
		if(packetworker != null){
			packetworker.cancel(true);
			packetworker = null;
			logger.info("disposed packetworker");
		}
		if(videoCapture != null){
			videoCapture.stopCapture();//blocking till video capture engine fully stop
			
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
	}
	@Override
	public void receiveImage(BufferedImage image) {
		if(liveview.isVisible()){
			BufferedImage newimg = ImageHelper.resize(image, liveview.getViewWidth(),liveview.getViewHeight());
			liveview.setImage(newimg);
		}
		if(!deviceinfo.foundScreensize()){
			deviceinfo.updateScreensize(image.getWidth(), image.getHeight());
			logger.info("xxxxxxxxxxxxxx Update screen resolution => "+image.getWidth() +"x"+image.getHeight()+" xxxxxxxxxxxxxxxxxx");
		}
	}
	protected void finalize() throws Throwable{
		this.Dispose();
	}
	/**
	 * clean up background task and resources before exit
	 */
	public void Dispose(){
		if(this.monitor != null){
			this.monitor.stopMonitoring();
			this.monitor.shutDown();
		}
		if(rvi != null){
			try {
				rvi.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("cleaned up background task => Device Monitor");
	}
	@Override
	public void onConnected() {
		this.isDeviceConnected = true;
	}
	@Override
	public void onDisconnected() {
		showWaitBox();
		
		this.isDeviceConnected = false;
		if(liveview.isVisible()){
			liveview.setVisible(false);
		}
		if(packetworker != null){
			this.stopWorkers();
		}
		if(rvi != null){
			try {
				rvi.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		hideWaitBox();
		MessageDialogFactory.showErrorDialog(null, "Device disconnected. Please reconnect device.");
	}
	void showWaitBox(){
		
		waitbox.setAlwaysOnTop(true);
		waitbox.pack();
		waitbox.setVisible(true);
	}
	void hideWaitBox(){
		logger.info("hidding wait box");
		waitbox.setVisible(false);
	}
}//end class
