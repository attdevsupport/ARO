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
package com.att.aro.core.peripheral.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IVideoTimeReader;
import com.att.aro.core.peripheral.pojo.VideoTime;
import com.att.aro.core.util.Util;

/**
 * Method to read times from the video time trace file and store video time
 * variables.
 */
public class VideoTimeReaderImpl extends PeripheralBase implements IVideoTimeReader {

	@InjectLogger
	private static ILogger logger;
	
	@Override
	public VideoTime readData(String directory, Date traceDateTime) {
		boolean exVideoFound = false;
		boolean exVideoTimeFileNotFound = false;
		double videoStartTime = 0.0;
		boolean nativeVideo = false;
		String exVideoDisplayFileName = "exvideo.mov";
		String filepath = directory + Util.FILE_SEPARATOR + exVideoDisplayFileName;
		String[] lines = null;
		String nativeVideoFileOnDevice = "video.mp4";
		String nativeVideoDisplayfile = "video.mov";
		if (filereader.fileExist(filepath) || isExternalVideoSourceFilePresent(nativeVideoFileOnDevice,nativeVideoDisplayfile,false, directory)){
		
			 exVideoFound = true;
			 exVideoTimeFileNotFound = false;
			 filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.EXVIDEO_TIME_FILE;
			 
			 if (!filereader.fileExist(filepath)) {
					exVideoTimeFileNotFound =true;
					exVideoFound = false;
			}else {
					
				videoStartTime += readVideoStartTime(filepath, traceDateTime);
					
				}
		}else{
		
			exVideoFound = false;
			exVideoTimeFileNotFound = false;
			nativeVideo = true;
			filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.VIDEO_TIME_FILE;
			if(filereader.fileExist(filepath)){
				videoStartTime += readVideoStartTime(filepath, traceDateTime);
					
			}	
			
		}
		VideoTime vtime = new VideoTime();
		vtime.setExVideoFound(exVideoFound);
		vtime.setExVideoTimeFileNotFound(exVideoTimeFileNotFound);
		vtime.setNativeVideo(nativeVideo);
		vtime.setVideoStartTime(videoStartTime);
		return vtime;
	}
	double readVideoStartTime(String filepath, Date traceDateTime){
		double videoStartTime = 0;
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e1) {
			logger.error("failed reading video time file", e1);
		}
		if (lines != null && lines.length > 0) {
			String line = lines[0];
			String[] strValues = line.split(" ");
			if (strValues.length > 0) {
				try {
					videoStartTime = Double.parseDouble(strValues[0]);
				} catch (NumberFormatException e) {
					logger.error("Cannot determine actual video start time", e);
				}
				if (strValues.length > 1) {
					// For emulator only, tcpdumpLocalStartTime is
					// start
					// time started according to local pc/laptop.
					// getTraceDateTime is time according to
					// emulated device
					// -- the tcpdumpDeviceVsLocalTimeDetal is
					// difference
					// between the two and is added as an offset
					// to videoStartTime so that traceEmulatorTime
					// and
					// videoStartTime are in sync.
					double tcpdumpLocalStartTime = Double.parseDouble(strValues[1]);
					double tcpdumpDeviceVsLocalTimeDelta = (traceDateTime
							.getTime() / 1000.0) - tcpdumpLocalStartTime;
					videoStartTime += tcpdumpDeviceVsLocalTimeDelta;
				}
			}
		}
		return videoStartTime;
	}
	/**
	 * Checks for external video source.
	 * 
	 * @param nativeVideoSourcefile,isPcap
	 * 			the native video source file i.e video.mp4 
	 * 			pcap file loaded or not.
	 * @return boolean
	 * 			return false if only native video file is present , otherwise true.
	 */			
	boolean isExternalVideoSourceFilePresent(String nativeVideoFileOnDevice, String nativeVideoDisplayfile,boolean isPcap, String traceDirectory){
		
		int index =0;
		String[] matches;
		if(isPcap){
			matches = filereader.list(traceDirectory, new FilenameFilter()
			{
				public boolean accept(File dir, String name) {			
					return isVideoFile(name);
				  }
			});
		}else{
			matches = filereader.list(traceDirectory, new FilenameFilter()
			{
				public boolean accept(File dir, String name) {
					
					return isVideoFile(name);
				  }
			});
		}
		
		if(matches!= null){
			while(index < matches.length){
				if(matches.length == 1){
					// If trace directory contains any one file video.mp or video.mov , we allow normal native video flow.
/*					if(nativeVideoFileOnDevice.equals(matches[index]) || nativeVideoDisplayfile.equals(matches[index])){
						return false;
					}else{
						return true;
					}*/
					return (!(nativeVideoFileOnDevice.equals(matches[index]) || nativeVideoDisplayfile.equals(matches[index])));
				}else {
					// If the trace directory contains video.mp4 and video.mov , we allow normal native video flow.
					if((matches.length == 2) && ((index + 1)!=2)
						&& (nativeVideoFileOnDevice.equals(matches[index]) || nativeVideoDisplayfile.equals(matches[index]))
						&& (nativeVideoFileOnDevice.equals(matches[index+1]) || nativeVideoDisplayfile.equals(matches[index+1]))	){
						return false;
					}
					else{
						// if trace directory contains video.mp4 or video.
						//mov along with external video file, we give preference to external video file.
						if(nativeVideoFileOnDevice.equals(matches[index]) || nativeVideoDisplayfile.equals(matches[index])){
							return true;
						}
					}
				}
				
				index+=1;	
			}
		}
		return false;
	}
	boolean isVideoFile(String name){
		return (name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".wmv")
				||name.toLowerCase().endsWith(".qt") || name.toLowerCase().endsWith(".wma")
				|| name.toLowerCase().endsWith(".mpeg") || name.toLowerCase().endsWith(".3gp")
				|| name.toLowerCase().endsWith(".asf") || name.toLowerCase().endsWith(".avi")
				|| name.toLowerCase().endsWith(".dv") || name.toLowerCase().endsWith(".mkv")
				|| name.toLowerCase().endsWith(".mpg") || name.toLowerCase().endsWith(".rmvb")
				|| name.toLowerCase().endsWith(".vob") || name.toLowerCase().endsWith(".mov"));
	}
}//end
