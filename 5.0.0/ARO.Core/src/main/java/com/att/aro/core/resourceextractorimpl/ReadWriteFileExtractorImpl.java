/**
 * 
 */
package com.att.aro.core.resourceextractorimpl;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;

/**
 * @author Harikrishna
 *
 */
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
