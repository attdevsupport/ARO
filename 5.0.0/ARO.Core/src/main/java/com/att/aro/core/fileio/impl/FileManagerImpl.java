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
package com.att.aro.core.fileio.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;

/**
 * Helper class for reading file. Return result as String, array of string or
 * byte array
 *
 */
public class FileManagerImpl implements IFileManager {

	@InjectLogger
	private static ILogger logger;
	
	@Override
	public String[] readAllLine(String filepath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filepath));

		String[] arrlist = readAllLine(reader);

		return arrlist;
	}

	public String[] readAllLine(BufferedReader reader) {
		List<String> list = new ArrayList<String>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
		} catch (IOException e) {
			logger.error("error reading data from BufferedReader", e);
		}
		String[] arrlist = (String[]) list.toArray(new String[list.size()]);

		return arrlist;
	}

	@Override
	public boolean fileExist(String path) {
		File file = new File(path);
		return file.exists();
	}

	@Override
	public boolean isFile(String filepath) {
		File file = new File(filepath);
		return file.isFile();
	}

	@Override
	public File createFile(String filepath) {
		return new File(filepath);
	}

	@Override
	public File createFile(String parent, String child) {
		return new File(parent, child);
	}

	@Override
	public void mkDir(String path) {
		mkDir(new File(path));
	}

	public void mkDir(File dirinfo) {
		if (!dirinfo.exists()) {
			dirinfo.mkdirs();
		}
	}

	@Override
	public boolean directoryExist(String directoryPath) {
		File dir = new File(directoryPath);
		return (dir.exists() && dir.isDirectory());
	}

	@Override
	public boolean directoryExistAndNotEmpty(String directoryPath) {
		return directoryExistAndNotEmpty(new File(directoryPath));
	}

	public boolean directoryExistAndNotEmpty(File direct) {
		if (direct.exists() && direct.isDirectory()) {
			//any file?
			return direct.list().length > 0;
		}
		return false;
	}

	@Override
	public String[] list(String directoryPath, FilenameFilter filter) {
		File dir = new File(directoryPath);
		if (dir.exists() && dir.isDirectory()) {
			return dir.list(filter);
		}
		return new String[0];
	}

	@Override
	public long getLastModified(String filepath) {
		File file = new File(filepath);
		return file.lastModified();
	}

	@Override
	public String getDirectory(String filepath) {
		File file = new File(filepath);
		if (file.exists()) {
			if (file.isDirectory()) {
				return filepath;
			} else {
				return file.getParent();
			}
		}
		return null;
	}

	@Override
	public boolean fileDirExist(String filepath) {
		File targetFile = new File(filepath);
		File parent = targetFile.getParentFile();
		if (parent.exists()) {
			return true;
		}
		return false;
	}

	@Override
	public InputStream getFileInputStream(String filepath) throws FileNotFoundException {
		return new FileInputStream(filepath);
	}

	@Override
	public OutputStream getFileOutputStream(String filepath) throws FileNotFoundException {
		return new FileOutputStream(filepath);
	}

	/**
	 * flush and close the OutputStream
	 * 
	 * @param outputStream
	 * @throws IOException
	 */
	@Override
	public void closeFile(FileOutputStream fileOutputStream) throws IOException {
		fileOutputStream.flush();
		fileOutputStream.close();
	}

	/**
	 * Write an input stream to a given location.
	 */
	@Override
	public void saveFile(InputStream iStream, String location) throws IOException {

		this.saveFile(iStream, getFileOutputStream(location));
	}

	private void saveFile(InputStream iStream, OutputStream oStream) throws IOException {

		try {
			byte[] buffer = new byte[4096];
			int length;
			while ((length = iStream.read(buffer)) != -1) {
				oStream.write(buffer, 0, length);
			}

		} finally {
			if (oStream != null) {
				oStream.close();
			}
		}
	}

	@Override
	public boolean deleteFile(String path) {
		boolean success = false;
		if(fileExist(path)){
			success = createFile(path).delete();
		}
		return success;
	}

	/**
	 * rename a File with a newName
	 * 
	 * @param origFileName
	 *            a File
	 * @param newName
	 *            a String containing a new name
	 * @return
	 */
	@Override
	public boolean renameFile(File origFileName, String newName) {
		String path = origFileName.getAbsolutePath().substring(0, origFileName.getAbsolutePath().length() - origFileName.getName().length());
		File renameFile = new File(path, newName);
		return (!renameFile.exists() && origFileName.renameTo(renameFile));
	}
}
