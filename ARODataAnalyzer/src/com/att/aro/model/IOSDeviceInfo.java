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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.att.aro.main.ResourceBundleManager;
import com.att.aro.util.Util;

public class IOSDeviceInfo {
	private static Logger logger = Logger.getLogger(IOSDeviceInfo.class.getName());
	String exepath = "";
	ExternalProcessRunner runner;
	Map<String,String> list;
	String buildversion;
	public IOSDeviceInfo(){
		list = new HashMap<String,String>();
		String dir = Util.getCurrentRunningDir();
		File dirfile = new File(dir);
		dir = dirfile.getParent();
		exepath = dir + Util.FILE_SEPARATOR +"bin" + Util.FILE_SEPARATOR + "libimobiledevice" + Util.FILE_SEPARATOR + "ideviceinfo";
		logger.info("set ideviceinfo path: "+exepath);
		
		runner = new ExternalProcessRunner();
		
		ResourceBundle buildBundle = ResourceBundleManager.getBuildBundle();
		buildversion = buildBundle.getString("build.majorversion");
	}
	/**
	 * Get device info by UDID
	 * @param deviceId is UDID for IOS device
	 * @return
	 */
	public boolean getDeviceInfo(String deviceId, String filepath){
		File exefile = new File(exepath);
		if(!exefile.exists()){
			logger.info("bin/libimobiledevice/ideviceinfo is not found.");
			return false;
		}
		String[] cmds = new String[]{"bash","-c", exepath,"-u "+deviceId};
		String data = null;
		try {
			data = runner.runCmd(cmds);
			//logger.info(data);
			readData(data);
			writeData(filepath);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	private void readData(String data){
		String[] arr = data.split("\\r?\\n");
		String[] tokens;
		String key,value;
		for(String line : arr){
			tokens = line.split(":");
			key = tokens[0].trim();
			value = tokens[1].trim();
			list.put(key, value);
		}
	}
	private void writeData(String filepath){
		try {
			File file = new File(filepath);
			FileWriter writer = new FileWriter(file,false);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write("ARO Analyzer/IOS");
            bw.newLine();
            
            String val;
    		val = list.get("ProductType");
    		if(val != null){
    			bw.write(val);
    		}else{
    			bw.write("Unknown");
    		}
    		bw.newLine();
    		
    		bw.write("Apple");
    		bw.newLine();
    		
    		bw.write("IOS");
    		bw.newLine();
    		
    		val = list.get("ProductVersion");
    		if(val != null){
    			bw.write(val);
    		}else{
    			bw.write("Unknown");
    		}
    		bw.newLine();
    		
    		bw.write(buildversion);
    		bw.newLine();
    		
    		bw.write("0");
    		bw.newLine();
    		
            bw.close();
		} catch (Exception e) {
		}
		
		
	}
}
