package com.att.aro.core.fileio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reading file as a whole string, string array of line, byte array etc.
 * @author Borey Sao
 * Date: April 18, 2014
 */
public interface IFileManager {
	/**
	 * read file line by line and return an array of string
	 * @param filepath full path of file to read
	 * @return array of String of line
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	String[] readAllLine(String filepath) throws IOException;
	boolean isFile(String filepath);
	boolean fileExist(String path);
	boolean directoryExist(String directoryPath);
	boolean directoryExistAndNotEmpty(String directoryPath);
	boolean fileDirExist(String filepath);
	String[] list(String directoryPath, FilenameFilter filter);
	long getLastModified(String filepath);
	String getDirectory(String filepath);
	File createFile(String filepath);
	File createFile(String parent, String child); 
	void mkDir(String path);
	void closeFile(FileOutputStream fileOutputStream) throws IOException;
	InputStream getFileInputStream(String filepath) throws FileNotFoundException;
	OutputStream getFileOutputStream(String filepath) throws FileNotFoundException;
	void saveFile(InputStream iStream, String location) throws IOException;
	boolean deleteFile(String path);
	boolean renameFile(File origFileName, String newName);

}
