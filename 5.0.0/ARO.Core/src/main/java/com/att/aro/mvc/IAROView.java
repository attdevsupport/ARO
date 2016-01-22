/**
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
package com.att.aro.mvc;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import com.android.ddmlib.IDevice;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.pojo.CollectorStatus;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;

/**
 * This is the View part of the ARO MVC implementation that is referenced by the core product.
 * An implementation of this interface is passed in the constructor of <em>AROController</em>.
 * @see {@link AROController} 
 */

public interface IAROView {
//	/**
//	 * As part of the UI initialization, declare an instance of the AROController, and pass a 
//	 * handle of this view to the MVC controller.
//	 */
//	void initialize();
	
	/**
	 * Modify the path of the analyzed trace file/folder
	 * @param path: file path of the trace file/folder
	 */
	void updateTracePath(File path);
	
	/**
	 * Sets the device profile that is used for analysis.
	 * 
	 * @param profile: The device profile to be set.
	 */
	void updateProfile(Profile profile);
	
	/**
	 * Modify the path of the report file for the analyzed trace file/folder
	 * @param path: file path of the report file
	 */
	void updateReportPath(File path);
	
	/**
	 * Modify the filter associated with the ARO model
	 * @param AnalysisFilter: filter containing time range, applications, processes selection
	 */
	void updateFilter(AnalysisFilter filter);
	String getTracePath();

	/**
	 * ARO can generate HTML reports of a trace analysis to a file system.  This is a callback to
	 * tell the report generation where to write the report to.
	 * 
	 * @return The FQPN of the file to write the ARO-generated report to.
	 */
	String getReportPath();
	
//	/**
//	 * Update all property change listeners on changes in property
//	 * @param property: the property that changes
//	 * @param oldValue: the old value of the property
//	 * @param newValue: the new value of the property
//	 */
//	void notifyPropertyChangeListeners(String property, Object oldValue, Object newValue);
//
//	/**
//	 * Notify listeners to actions of interest
//	 * @param key: type of action
//	 * @param command: command 
//	 */
//	void notifyActionListeners(int key, String command);
//
	/**
	 * Callback that allows an application to register ARO's PropertyChangeListener.
	 * 
	 * @param listener ARO's PropertyChangeListener
	 */
	void addAROPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * Callback that allows an application to registers ARO's AWT ActionListener implementation
	 * 
	 * @param listener
	 */
	void addAROActionListener(ActionListener listener);
	
	/**
	 * refresh UI components
	 */
	void refresh();
	
	/**
	 * Action to start an Android collector 
	 * @param deviceId: the phone id attached to the computer
	 * @param traceFolderName: the specified folder name
	 * @param captureVideo: boolean to capture video or not
	 */
	void startCollector(int deviceId, String traceFolderName, boolean captureVideo);
	
	/**
	 * Action to start an iOS collector
	 * @param iOsCollector: iOSCollector instance
	 * @param udid: device id
	 * @param tracePath: trace path chosen by the user 
	 * @param videoCapture: boolean to capcture or not video
	 */
	void startCollectorIos(IDataCollector iOsCollector, String udid, String tracePath, boolean videoCapture);

	/**
	 * Action to stop a running collector
	 */
	void stopCollector();

	/**
	 * Action to force an Android collector apk to be stopped
	 */
	void haltCollector();
	
	/**
	 * Return the list of connected devices
	 * @return: list of connected devices
	 */
	IDevice[] getConnectedDevices();

	/**
	 * Return the list of available collectors (iOS, Rooted Android, NonRooted)
	 * @return: list of collectors
	 */
	List<IDataCollector> getAvailableCollectors();

	/**
	 * Callback for ARO to register the current status of a collection and if it's completed
	 * the results of the collection.
	 * 
	 * @param status The current collector state
	 * @param result If the current collector process is completed, the status of the collection
	 */
	void updateCollectorStatus(CollectorStatus status, StatusResult result);
	
	/**
	 * Return the status of a collector (READY, STARTING, STARTED, STOPPING, STOPPED, PULLING)
	 * @return
	 */
	CollectorStatus getCollectorStatus();
	
	/**
	 * Callback for when data collection is started and video capture is requested.
	 * 
	 * @param collector The collector used
	 */
	void liveVideoDisplay(IDataCollector collector);


}
