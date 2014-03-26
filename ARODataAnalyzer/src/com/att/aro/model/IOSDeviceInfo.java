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
	boolean foundrealscreensize = false;
	String currentFilepath = "";
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
	 * check if a device is password protected
	 * @param deviceId
	 * @return
	 * @throws IOException
	 */
	public boolean isPasswordProtected(String deviceId) throws IOException{
		String data = getDeviceData(deviceId);
		readData(data);
		String pass = list.get("PasswordProtected");
		if(pass != null && pass.toLowerCase().trim().equals("true")){
			return true;
		}
		return false;
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
		String data = null;
		this.currentFilepath = filepath;
		try {
			data = getDeviceData(deviceId);
			readData(data);
			writeData(filepath);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		return false;
	}
	private String getDeviceData(String deviceId) throws IOException{
		String[] cmds = new String[]{"bash","-c", exepath,"-u "+deviceId};
		String data = runner.runCmd(cmds);
		return data;
	}
	
	/**
	 * For getting the Device Version. Used for prompt a message to the user.
	 * @return
	 */
	public String getDeviceVersion(){
		String deviceVersion = "0";
		if(list != null){
			deviceVersion = list.get("ProductVersion"); //Since list get when device info called
		}
		return deviceVersion;
	}
	
	private void readData(String data){
		String[] arr = data.split("\\r?\\n");
		String[] tokens;
		String key,value;
		for(String line : arr){
			tokens = line.split(":");
			if(tokens.length > 1){
				key = tokens[0].trim();
				value = tokens[1].trim();
				list.put(key, value);
			}
		}
	}
	private void writeData(String filepath){
		try {
			File file = new File(filepath);
			if(file.exists()){
				file.delete();
			}
			FileWriter writer = new FileWriter(file,false);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write("ARO Analyzer/IOS");
            bw.newLine();
            
            String deviceType;
            String val;
    		deviceType = list.get("ProductType");
    		if(deviceType != null){
    			bw.write(deviceType);
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
    		
    		val = list.get("ScreenResolution");
    		if(val == null){
    			bw.write(getScreensize(deviceType));
    		}else{
    			bw.write(val);
    		}
    		bw.newLine();
    		
            bw.close();
		} catch (Exception e) {
		}
		
		
	}
	public void updateScreensize(int width, int height){
		if(width > height){
			list.put("ScreenResolution", height + "*"+width);
		}else{
			list.put("ScreenResolution", width + "*"+height);
		}
		writeData(this.currentFilepath);
		this.foundrealscreensize = true;
	}
	public boolean foundScreensize(){
		return this.foundrealscreensize;
	}
	private String getScreensize(String deviceType){
		this.foundrealscreensize = true;
		if(deviceType.contains("iPhone5") || deviceType.contains("iPhone6")){
			return "640*1136";
		}else if(deviceType.contains("iPhone4")){
			return "640*960";
		}else if(deviceType.contains("iPad2")){
			return "768*1024";
		}else if(deviceType.contains("iPad3")){
			return "1536*2048";
		}
		this.foundrealscreensize = false;
		return "640*960";
	}
}
