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
package com.att.aro.core.resourceextractorimpl;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;

public class ReadWriteFileExtractorImpl implements IReadWriteFileExtractor{

	@InjectLogger
	private static ILogger logger;
	
	IFileManager fileManager;
	@Autowired
	public void setFileManager(IFileManager filemanager){
		this.fileManager = filemanager;
	}
	
	public boolean extractFiles(String saveToFile, String filename, ClassLoader aroClassloader){
		boolean isFileCreated = true;
		InputStream iStream = null;
		iStream = aroClassloader.getResourceAsStream(filename);
		
		if(iStream == null){
			logger.debug("resource not found: "+filename);
			return false;
		}
		try{
			fileManager.saveFile(iStream, saveToFile);
			iStream.close();
			isFileCreated = true;
		} catch(IOException ioExp){
			isFileCreated = false;
		} catch (NullPointerException nullExp){
			isFileCreated = false;
		}
		return isFileCreated;
		
	}
	
}
