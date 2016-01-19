/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.view.menu.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedHashSet;
import java.util.Set;

import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * <p>
 * This encapsulates the check for missing files in a trace.
 * </p><p>
 * 	<strong>NOTE:</strong>  This does not belong here!!  It most likely belongs in core.
 * Furthermore, this really does the wrong thing by directly altering the data model's
 * <em>missingFiles</em> attribute.  <b><u>THIS SHOULD NOT BE DONE FROM THE UI!!!</u></b>.
 * It's currently done here for time and lack of model alteration forethought reasons (found
 * out the hard way this functionality was not previously implemented).
 * </p><p>
 * TODO:  FIX this direct modification to the model from the UI - and the actual process
 * of determining missing files while we're at it!
 * </p>
 * 
 * @author Nathan F Syfrig
 *
 */
public class MissingTraceFiles {
	private final File tracePath;
	private final AROTraceData model;

	private final String[] VideoSuffixes = {
		".mp4",
		".wmv",
		".qt",
		".wma",
		".mpeg",
		".3gp",
		".asf",
		".avi",
		".dv",
		".mkv",
		".mpg",
		".rmvb",
		".vob",
		".mov"
	};

	public MissingTraceFiles(File tracePath, AROTraceData model) {
		this.tracePath = tracePath;
		this.model = model;
	}
	public MissingTraceFiles(File tracePath) {
		this(tracePath, null);
	}


	public String formatMissingFiles(Set<File> missingFiles) {
		StringBuilder missingFilesString = new StringBuilder();
		boolean firstTime = true;
		for (File missingFile : missingFiles) {
			if (!firstTime) {
				missingFilesString.append("\n");
			}
			missingFilesString.append(missingFile.getName());
			firstTime = false;
		}
		return missingFilesString.toString();
	}

	public Set<String> getModelMissingFiles(Set<File> missingFiles) {
		Set<String> modelMissingFiles = new LinkedHashSet<String>();
		for (File missingFile : missingFiles) {
			modelMissingFiles.add(missingFile.getName());
		}
		return modelMissingFiles;
	}

	private boolean isFilePresent(String traceDataFileName) {
		return new File(tracePath, traceDataFileName).exists();
	}

	private void addMissingFileMaybe(String traceDataFileName,
			Set<File> missingFiles) {
		File fileExistCheck = new File(tracePath, traceDataFileName);
		if (!isFilePresent(traceDataFileName)) {
			missingFiles.add(fileExistCheck);
		}
	}

	/**
	 * Checks for external video source (assumes it's not a pcap file).
	 * 
	 * @param nativeVideoSourcefile
	 * 			the native video source file i.e video.mp4 
	 * @return boolean
	 * 			return false if only native video file is present , otherwise true.
	 */			
	private boolean isExternalVideoSourceFilePresent(String nativeVideoFileOnDevice,
			String nativeVideoDisplayfile){
		
		String[] matches = tracePath.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean suffixMatch = false;
				for (String suffix : VideoSuffixes) {
					if (name.toLowerCase().endsWith(suffix)) {
						suffixMatch = true;
						break;
					}
				}
				return suffixMatch;
			}
		});

		boolean externalVideoSource = false;
		if(matches!= null) {
			if(matches.length == 1 && !nativeVideoFileOnDevice.equals(matches[0]) &&
					!nativeVideoDisplayfile.equals(matches[0])) {
				// If trace directory contains any one file video.mp or video.mov , we allow normal native video flow.
				externalVideoSource = true;
			}
			else if (matches.length == 2 && 
					(nativeVideoFileOnDevice.equals(matches[0]) ||
						nativeVideoDisplayfile.equals(matches[0])) &&
					(nativeVideoFileOnDevice.equals(matches[1]) ||
						nativeVideoDisplayfile.equals(matches[1]))) {
				// If the trace directory contains video.mp4 and video.mov , we allow normal native video flow.
				;
			}
			else if (matches.length > 1) {
				for(int index = 0; index < matches.length; ++index){
					// if trace directory contains video.mp4 or video.mov along with external video file, we give preference to external video file.
					if(nativeVideoFileOnDevice.equals(matches[index]) ||
							nativeVideoDisplayfile.equals(matches[index])){
						externalVideoSource = true;
						break;
					}
				}
			}
		}
		return externalVideoSource;
	}

	public Set<File> retrieveMissingFiles() {
		Set<File> missingFiles = new LinkedHashSet<File>();
		addMissingFileMaybe(TraceDataConst.FileName.APPNAME_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.DEVICEINFO_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.DEVICEDETAILS_FILE, missingFiles);
//		addMissingFileMaybe(TraceDataConst.FileName.NETWORKINFO_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.CPU_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.GPS_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.BLUETOOTH_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.WIFI_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.CAMERA_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.SCREEN_STATE_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.USER_EVENTS_FILE, missingFiles);
//		addMissingFileMaybe(TraceDataConst.FileName.SCREEN_ROTATIONS_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.ALARM_END_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.ALARM_START_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.KERNEL_LOG_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.BATTERY_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.BATTERYINFO_FILE, missingFiles);
		addMissingFileMaybe(TraceDataConst.FileName.RADIO_EVENTS_FILE, missingFiles);
		if (isFilePresent(ResourceBundleHelper.getMessageString("video.videoDisplayFile")) ||
				isFilePresent(ResourceBundleHelper.getMessageString(
					"video.videoFileOnDevice"))) {
			addMissingFileMaybe(TraceDataConst.FileName.VIDEO_TIME_FILE, missingFiles);
		}
		if (isFilePresent(ResourceBundleHelper.getMessageString("video.exVideoDisplayFile")) ||
				isExternalVideoSourceFilePresent("video.mp4", "video.mov")){
			addMissingFileMaybe(TraceDataConst.FileName.EXVIDEO_TIME_FILE, missingFiles);
		}
//		addMissingFileMaybe(TraceDataConst.FileName.SSLKEY_FILE, missingFiles);

		// TODO:  Move this to Core (preferably immutable through a builder and no setters)!
		// UI shoule not be doing this!!!
		if (model != null) {
			AbstractTraceResult traceResult = model.getAnalyzerResult().getTraceresult();
			if (traceResult instanceof TraceDirectoryResult) {
				((TraceDirectoryResult) traceResult).setMissingFiles(
						getModelMissingFiles(missingFiles));
			}
		}
		return missingFiles;
	}


	@Override
	public String toString() {
		return "MissingTraceFiles [tracePath=" + tracePath + "]";
	}
}
