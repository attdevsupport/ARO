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

package com.att.aro.model;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import com.att.aro.interfaces.Settings;
import com.att.aro.util.Util;

public class SettingsImpl implements Settings {
	Properties prop;
	public SettingsImpl(){
		prop = new Properties();
		loadSettings();
	}
	public SettingsImpl(Properties prop){
		this.prop = prop;
		loadSettings();
	}
	private String getFilePath(){
		String dir = getDir(); //Util.getCurrentRunningDir();
		String filepath = dir + Util.FILE_SEPARATOR + "config.properties";
		return filepath;
	}
	private String getDir(){
		return System.getProperty("user.home") + Util.FILE_SEPARATOR + "AROConfig";
	}
	void loadSettings(){
		String filepath = getFilePath();
		File file = new File(filepath);
		if(!file.exists()){
			return;
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(stream != null){
			try {
				prop.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/* (non-Javadoc)
	 * @see com.att.aro.model.Settings#addNewOrUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public void addNewOrUpdate(String key, String value){
		prop.setProperty(key, value);
	}
	/* (non-Javadoc)
	 * @see com.att.aro.model.Settings#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key){
		return prop.getProperty(key);
	}
	/* (non-Javadoc)
	 * @see com.att.aro.model.Settings#Save()
	 */
	@Override
	public void save() throws IOException{
		String dir = getDir();
		File dirinfo = new File(dir);
		if(!dirinfo.exists()){
			dirinfo.mkdir();
		}
		String filepath = getFilePath();
		File configFile = new File(filepath);
		if(!configFile.exists()) {
		    configFile.createNewFile();
		} 
		FileOutputStream stream = new FileOutputStream(filepath, false);
		prop.store(stream, null);
	}
	
}//end class
