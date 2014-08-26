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

package com.att.aro.main;


import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncService;
import com.android.ddmlib.SyncService.SyncResult;
import com.att.aro.analytics.AnalyticFactory;
import com.att.aro.commonui.DataCollectorFolderDialog;
import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.interfaces.ImageSubscriber;
import com.att.aro.model.MobileDevice;
import com.att.aro.model.TraceData;
import com.att.aro.util.ImageHelper;
import com.att.aro.util.Util;
import com.att.aro.videocapture.VideoCaptureThread;

public class DataCollectorNoRootVpn implements ImageSubscriber {
	private static final Logger logger = Logger.getLogger(DataCollectorNoRootVpn.class.getName());
	private static final ResourceBundle rb = ResourceBundleManager.getDefaultBundle();
	/**
	 * Currently selected Android device
	 */
	private IDevice device;

	/**
	 * Thread that is used to collect video
	 */
	private VideoCaptureThread videoCapture;
	
	/**
	 * Indicates local directory where trace results will be stored
	 */
	private File localTraceFolder;
	
	
	long startTime;
	
	/**
	 * ARO analyzer instance that is to be notified of data collector status
	 * updates
	 */
	private ApplicationResourceOptimizer mAROAnalyzer;
	
	private SwingWorker<Object, Object> backgroundWorker;
	
	private File videofile;
	
	private LiveScreenViewDialog liveview;
	
	DataCollectorFolderDialog folder = null;

 /*
  * VPN based non rooted packet capture
  */
    VPNPacketCapture vpnPacketCapture;
	private FileHandler logFile;
	private String traceFolder = null;
	
	public DataCollectorNoRootVpn(ApplicationResourceOptimizer mApp){
		//initLogFile();
		this.mAROAnalyzer = mApp;
	//	liveview = new LiveScreenViewDialog();
	}
	
	//used to setup for unit test
	public DataCollectorNoRootVpn(ApplicationResourceOptimizer mainARO
								, DataCollectorFolderDialog folderDialog
								, LiveScreenViewDialog liveview
								, IDevice device
								, VideoCaptureThread videoCapture){
		//initLogFile();
		this.mAROAnalyzer = mainARO;
		this.folder = folderDialog;
		this.liveview = liveview;
		this.device = device;
		this.videoCapture = videoCapture;
	}

	/**
	 * Create logfile to capture Logger data
	 */
	private void initLogFile() {
		if (logFile == null) {
			try {
				logFile = new FileHandler("ARO_debug.log");
			} catch (SecurityException e) {
				logger.info("Cannot create "+logFile + " SecurityException:"+e.getMessage());
				return;
			} catch (IOException e) {
				logger.info("Cannot create "+logFile + " IOException:"+e.getMessage());
				return;
			}
			logger.addHandler(logFile);
		       SimpleFormatter formatter = new SimpleFormatter();
		       logFile.setFormatter(formatter);  
		}
	}

	/**
	 * Start VPN based Collector on device
	 */
	public void startCollector() {

		liveview = new LiveScreenViewDialog();
		traceFolder  = setupTraceFolder();
		if (traceFolder == null) {
			return;
		}

		// For getting the device details
		MobileDevice connectedDevice = new MobileDevice();
		device = connectedDevice.getFirstAndroidDevice();
		writeDeviceDetailsToFile();

		final String filepath = traceFolder + Util.FILE_SEPARATOR + TraceData.VIDEO_MOV_FILE;

		videofile = new File(filepath);
		if (device == null) {
			MobileDevice mdevice = new MobileDevice();
			device = mdevice.getFirstAndroidDevice();
		}
		if (device == null) {
			MessageDialogFactory.showErrorDialog(null, rb.getString("Error.nodevicefound"));
			return;
		}
		AnalyticFactory.getGoogleAnalytics().sendAnalyticsEvents(rb.getString("ga.request.event.category.collector"), rb.getString("ga.request.event.collector.action.starttrace")); //end of GA Req

		vpnPacketCapture = new VPNPacketCapture();
		
		Thread pcapThread = new Thread(new Runnable() {
			public void run() {
				//String tracePath = dirpath + Util.FILE_SEPARATOR + Util.TRAFFIC_FILE;
				vpnPacketCapture.startPacketCapture(device, traceFolder);
			}
		});
		pcapThread.start();

		// only do this if user want to capture video
		if (folder.isCaptureVideo()) {
			// create a new thread of VideoCapture for performing in background later
			try {
				if (videoCapture == null) {
					videoCapture = new VideoCaptureThread(device, null, videofile);
					videoCapture.addSubscriber(this);
				}
			} catch (IOException e) {
				e.printStackTrace();
				// There was an error creating Video Capture engine:
				MessageDialogFactory.showErrorDialog(null, rb.getString("Error.createvideocaptureengine") + e.getMessage());
				return;
			}

			// create background worker that will do all the work silently
			backgroundWorker = new SwingWorker<Object, Object>() {

				@Override
				protected Object doInBackground() throws Exception {

					
					// Delaying the video capture until VPN starts
					while (true) {
						Thread.sleep(500);

						if (vpnPacketCapture.isVpnActivated()) {
							vpnPacketCapture.gotoAndroidHomeScreen();
							System.out.println("got VPN");
							break;
						}
					}
					logger.info("Check flag for the video capture :" + vpnPacketCapture.isCancelFlag());
					if (!vpnPacketCapture.isCancelFlag()) {
						doBackgroundTask();
					} else {
						liveview.setVisible(false);
					}
					return null;
				}

				@Override
				protected void done() {
					super.done();
		            logger.info("done()");

					//TODO 
				}
			};
			
			if (!vpnPacketCapture.isCancelFlag()) {
				backgroundWorker.execute();
				mAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.STARTED);
				liveview.setAlwaysOnTop(true);
				liveview.setVisible(true); // sits here until [stop] is pressed
				// Collector should stop now
				//vpnPacketCapture.stopPacketCapture();
			} else {
				mAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.STOPPED);
			}

			
			pcapThread = null; // For thread to close
			// user stop video capture by closing the preview screen
			try {
				this.stopCollector();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// for now there is nothing to do beside capturing video.
			// in the future: capture packet, user event etc.
			// MessageDialogFactory.showMessageDialog(null, "Nothing to do when you are not capturing video.");
			mAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.STARTED);

		}
	}
	
	/**
	 * Get user input for a Trace Folder name
	 * `
	 * @return
	 */
	private String setupTraceFolder() {
		//ask user for directory to save file to
		if(folder == null){
			folder = new DataCollectorFolderDialog(this.mAROAnalyzer);
		}
		folder.setVisible(true);
		String foldername = folder.getDirectoryName();
		
		//Added for cancel
		if(folder.isCancelled()){
			folder.setCancelled(false);
			return null;
		}
		
		if(foldername.length() < 1){
			//Folder/Directory path is required to save data to. Try again.
			MessageDialogFactory.showMessageDialog(null, rb.getString("Error.foldernamerequired"));
			return null;
		}
		String dirroot = Util.getAROTraceDirAndroid();
		final String dirpath = dirroot + Util.FILE_SEPARATOR + foldername;
		localTraceFolder = new File(dirpath);
		if(!localTraceFolder.exists()){
			if(!localTraceFolder.mkdirs()){
				//There was an error creating directory: 
				MessageDialogFactory.showErrorDialog(null, rb.getString("Error.createdirectory")+dirpath);
				return null;
			}
			
		} else {
            //ask user before overriding existing contents
            int answer = MessageDialogFactory.showConfirmDialog(mAROAnalyzer, "Trace folder already exists, do you want to override it?", JOptionPane.YES_NO_OPTION);
            if(answer == JOptionPane.YES_OPTION){
                //removed existing contents
                for(File file : localTraceFolder.listFiles()){
                    file.delete();
                }
                //logger.info("Folder exist and user want to override => removed existing contents: "+fnames);
            }else{
                return null;
            }
        }
		return dirpath;
	}
	
	void doBackgroundTask(){
        logger.info("Inside the background process ");
		startTime = System.currentTimeMillis();
		videoCapture.start();
	}
	
	/**
	 * Stop the collector on device
	 * @throws IOException
	 */
	public void stopCollector() throws IOException{
		AnalyticFactory.getGoogleAnalytics().sendAnalyticsEvents(rb.getString("ga.request.event.category.collector"), rb.getString("ga.request.event.collector.action.endtrace")); //end of GA Req
        //Stop collecting the trace
        if(vpnPacketCapture != null){
            vpnPacketCapture.stopPacketCapture();
        }
		if(videoCapture != null && !vpnPacketCapture.isCancelFlag()){
			videoCapture.stopRecording();
			//create video timestamp file that will sync with pcap file
			BufferedWriter videoTimeStampWriter = new BufferedWriter(
					new FileWriter(new File(localTraceFolder, TraceData.VIDEO_TIME_FILE)));
			try {
				// Writing a video time in file.
				Date stTime = videoCapture.getVideoStartTime();
				if (stTime != null) {
					videoTimeStampWriter.write(Double.toString(stTime.getTime() / 1000.0));
					if (vpnPacketCapture.getPcapStartTime() != null) {
						videoTimeStampWriter.write(" " + Double.toString(((Date) vpnPacketCapture.getPcapStartTime()).getTime() / 1000.0));
					}
				}

			} finally {
				videoTimeStampWriter.close();
			}

			videoCapture = null;
			logger.info("cleaned up videocapture");
		}
		
		// force stop the APK, prevent orphan processes from continuing the VPN
		System.out.println("forceStop the APK");
		vpnPacketCapture.forceStopAPK();

		vpnPacketCapture.pullTraceData();
		pullTaceData(vpnPacketCapture);
		vpnPacketCapture.deleteTraceData();
		
        vpnPacketCapture = null;
        logger.info("cleaned up pcap");

		if(backgroundWorker != null){
			backgroundWorker.cancel(true);
			backgroundWorker = null;
			logger.info("cleaned up background worker");
		}
		this.mAROAnalyzer.dataCollectorStatusCallBack(DatacollectorBridge.Status.READY);

        //open trace automatically when we stop the collector
        //File pcapTrace = new File(localTraceFolder + Util.FILE_SEPARATOR + Util.TRAFFIC_FILE);
        if(localTraceFolder.exists()){

            try {
                this.mAROAnalyzer.openTrace(localTraceFolder);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                logger.info(e.getMessage());
               // MessageDialogFactory.showErrorDialog(null, " NO VIDEO FILE EXISTS ");
                MessageDialogFactory.showMessageDialog(null, " NO VIDEO FILE EXISTS ");
            }
        }
    }
	
	private static final String[] mDataDeviceCollectorTraceFileNames = {
		TraceData.CPU_FILE,
		TraceData.APPID_FILE,
		TraceData.APPNAME_FILE,
		TraceData.TIME_FILE,
		TraceData.USER_EVENTS_FILE,
		TraceData.ACTIVE_PROCESS_FILE,
		TraceData.BATTERY_FILE,
		TraceData.BLUETOOTH_FILE,
		TraceData.CAMERA_FILE,
		TraceData.DEVICEDETAILS_FILE,
		TraceData.DEVICEINFO_FILE,
		TraceData.GPS_FILE,
		TraceData.NETWORKINFO_FILE,
		TraceData.PROP_FILE,
		TraceData.RADIO_EVENTS_FILE,
		TraceData.SCREEN_STATE_FILE,
		TraceData.SCREEN_ROTATIONS_FILE,
		TraceData.TIME_FILE,
		TraceData.USER_INPUT_LOG_EVENTS_FILE,
		TraceData.ALARM_START_FILE,
		TraceData.ALARM_END_FILE,
		TraceData.BATTERYINFO_FILE,
		TraceData.KERNEL_LOG_FILE,
		TraceData.VIDEO_TIME_FILE,
		TraceData.WIFI_FILE,
		TraceData.PCAP_FILE,
		TraceData.SSLKEY_FILE
};

	/**
	 * Pulls all existing files
	 * 
	 * @param vpnPacketCapture
	 * @return
	 */
	private String pullTaceData(VPNPacketCapture vpnPacketCapture) {
		String rez = null;
		try {
			SyncService service = vpnPacketCapture.getAndroidDevice().getSyncService();
			String srcFile = null;
			String dstFile = null;
			SyncResult result = null;
			for (String file : mDataDeviceCollectorTraceFileNames) {
				srcFile = "/sdcard/ARO" + "/" + file;
				dstFile = new File(localTraceFolder, file).getAbsolutePath();

				result = service.pullFile(srcFile, dstFile, SyncService.getNullProgressMonitor());

				logger.info(result.getCode() + " : " + srcFile);

				if (result.getCode() == SyncService.RESULT_OK) {
					System.out.println("Copied :" + srcFile);
				}else{
					service.close();
					service = vpnPacketCapture.getAndroidDevice().getSyncService();
					System.out.println("Failed :" + srcFile);
				}
			}
		} catch (IOException e) {
			rez = "Error :" + e.getMessage();
			e.printStackTrace();
		}
		return rez;
	}
	

	
	@Override
	public void receiveImage(BufferedImage image) {
		if(liveview.isVisible()){
			BufferedImage newimg = ImageHelper.resize(image, liveview.getViewWidth(),liveview.getViewHeight());
			liveview.setImage(newimg);
		}
	}
	
	/**
    * Getting the device info and writing the details into device_details text file.
    */
   private void writeDeviceDetailsToFile(){
       try {
           BufferedWriter devicedetailsWriter = new BufferedWriter(
                   new FileWriter(new File(localTraceFolder,
                           TraceData.DEVICEDETAILS_FILE)));
           try {
               // Writing device details in file.
               final String eol = System
                       .getProperty("line.separator");
               final String collector = rb
                       .getString("Emulator.datacollectorpath")
                       .substring(
                               rb.getString(
                                       "Emulator.datacollectorpath")
                                       .lastIndexOf("/") + 1);
               devicedetailsWriter.write(collector + eol);
               /*devicedetailsWriter.write(rb
                       .getString("bridge.device") + eol);*/
               String deviceModel = device.getProperty("ro.product.model");
               devicedetailsWriter
                       .write((deviceModel != null ? deviceModel
                               : "")
                               + eol);
               String deviceManufacturer = device.getProperty("ro.product.manufacturer");
               devicedetailsWriter
                       .write((deviceManufacturer != null ? deviceManufacturer
                               : "")
                               + eol);
               devicedetailsWriter.write(device.getProperty(device.PROP_BUILD_CODENAME) + eol);
               devicedetailsWriter.write(rb
                       .getString("bridge.platform") + " / ");
               devicedetailsWriter
                       .write(device.getProperty("ro.build.version.release")
                               + eol);
               devicedetailsWriter.write(" " + eol);

               final int deviceNetworkType = rb
                       .getString("bridge.network.UMTS")
                       .equalsIgnoreCase(
                               device.getProperty("gsm.network.type")) ? 3
                       : -1;
               devicedetailsWriter.write(deviceNetworkType
                       + eol);
           } finally {
               devicedetailsWriter.close();
           }
       } catch (IOException e) {
           if(!CommandLineHandler.getInstance().IsCommandLineEvent()) {
               MessageDialogFactory.showUnexpectedExceptionDialog(mAROAnalyzer, e);
           } else {
               CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.ErrorInPropFile"), e.getLocalizedMessage());
               CommandLineHandler.getInstance().UpdateTraceInfoFile(rb.getString("cmdline.Status"), rb.getString("cmdline.status.failed"));
           }
       }
   }
}//end class
