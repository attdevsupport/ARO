
/**
 *  Copyright 2016 AT&T
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
