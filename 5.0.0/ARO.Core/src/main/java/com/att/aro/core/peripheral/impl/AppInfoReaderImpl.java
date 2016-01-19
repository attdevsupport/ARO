/*
 *  Copyright 2014 AT&T
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

import java.io.IOException;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.TraceDataConst;
import com.att.aro.core.peripheral.IAppInfoReader;
import com.att.aro.core.peripheral.pojo.AppInfo;
import com.att.aro.core.util.Util;

/**
 * Reads the application names from the appinfo trace file.
 * @author EDS team
 * Refactored by Borey Sao
 * Date: October 1, 2014
 *
 */
public class AppInfoReaderImpl extends PeripheralBase implements IAppInfoReader {
	
	@InjectLogger
	private static ILogger logger;
	
	@Override
	public AppInfo readData(String directory) {
		AppInfo app = new AppInfo();
		String filepath = directory + Util.FILE_SEPARATOR + TraceDataConst.FileName.APPNAME_FILE;
		
		if (!filereader.fileExist(filepath)) {
			logger.warn("Application info file does not exists.");
			return app;
		}
		
		String[] lines = null;
		try {
			lines = filereader.readAllLine(filepath);
		} catch (IOException e) {
			logger.error("failed to open appname file: "+filepath);
		}
		if(lines != null && lines.length > 0){
			for (String line : lines) {
				String strFields[];
				String appName;
				String appVer = "n/a";
				if (line.charAt(0) == '"') {
					// Application name is surrounded by double quotes
					strFields = line.split("\"");
					appName = strFields[1];
					if (strFields.length > 2) {
						appVer = strFields[2];
						app.getAppVersionMap().put(appName, appVer);
					}
				} else {
					// Application name is surrounded by spaces
					strFields = line.split(" ");
					appName = strFields[0];
					if (strFields.length > 1) {
						appVer = strFields[1];
						app.getAppVersionMap().put(appName, appVer);
					}
				}
				app.getAppInfos().add(appName);
				
			}
		}
		return app;
	}

}
