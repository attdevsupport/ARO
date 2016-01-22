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
package com.att.aro.core.fileio.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.util.Util;

public class ReadFileImplTest extends BaseTest {
	
	@Spy	
	FileManagerImpl reader;
	boolean isfileclose = false;

	@Before
	public void setup() {
		reader = (FileManagerImpl) context.getBean(IFileManager.class);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getFileInputStream() throws Exception{
		FileInputStream fileInputStream = Mockito.mock(FileInputStream.class);
		
		PowerMockito.whenNew(FileInputStream.class).withArguments(Mockito.anyString()).thenReturn(fileInputStream);
		boolean result = reader.fileDirExist("parent/mockPath");
	}
	
	
	@Test
	public void fileDirExist() throws Exception{
		File file = Mockito.mock(File.class);
		File targetFile = Mockito.mock(File.class);
		
		PowerMockito.whenNew(File.class).withArguments(Mockito.anyString()).thenReturn(file);
		Mockito.when(file.getParentFile()).thenReturn(targetFile);
		Mockito.when(targetFile.exists()).thenReturn(true);
		boolean result = reader.fileDirExist("parent/mockPath");
	}
	
	
	@Test
	public void mkDirTest() {
		File file = Mockito.mock(File.class);
		Mockito.when(file.exists()).thenReturn(false);
		Mockito.doAnswer(new Answer() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}
		}).when(file).mkdir();
		reader.mkDir(file);
	}

	@Test
	public void dirExistNotEmptyTest() {
		String[] files = new String[0];
		File dir = Mockito.mock(File.class);
		Mockito.when(dir.exists()).thenReturn(true);
		Mockito.when(dir.isDirectory()).thenReturn(true);
		Mockito.when(dir.list()).thenReturn(files);
		boolean exist = reader.directoryExistAndNotEmpty(dir);
		assertEquals(false, exist);

		files = new String[1];
		Mockito.when(dir.list()).thenReturn(files);
		exist = reader.directoryExistAndNotEmpty(dir);
		assertEquals(true, exist);

		Mockito.when(dir.exists()).thenReturn(false);
		exist = reader.directoryExistAndNotEmpty(dir);
		assertEquals(false, exist);
	}

	@Test
	public void closeFileTest() throws IOException {
		FileOutputStream output = Mockito.mock(FileOutputStream.class);
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				isfileclose = true;
				return null;
			}
		}).when(output).close();
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				isfileclose = true;
				return null;
			}
		}).when(output).flush();
		reader.closeFile(output);
		assertTrue(isfileclose);
	}

	@Test
	public void readAllLineTest() throws IOException {

		BufferedReader buffreader = Mockito.mock(BufferedReader.class);
		Mockito.when(buffreader.readLine()).thenReturn("line1").thenReturn("line2").thenReturn(null);
		String[] lines = reader.readAllLine(buffreader);
		int count = lines.length;
		assertEquals(2, count);
	}

	@Test
	public void fileExistTest() {
		String currentdir = Util.getCurrentRunningDir();
		boolean exist = reader.fileExist(currentdir);
		assertTrue(exist);
		exist = reader.directoryExist(currentdir);
		assertTrue(exist);
		boolean isfile = reader.isFile(currentdir);
		assertFalse(isfile);
	}

	@Test
	public void listTest() {
		String currentdir = Util.getCurrentRunningDir();
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};
		String[] list = reader.list(currentdir, filter);
		boolean exist = list.length > 0;
		assertTrue(exist);
		list = reader.list(currentdir + "-not-found", filter);
		assertTrue(list.length == 0);
	}

	@Test
	public void getLastModifyTest() {
		String currentdir = Util.getCurrentRunningDir();
		long date = reader.getLastModified(currentdir);
		boolean ok = date > 0;
		assertTrue(ok);
	}

	@Test
	public void getDirectoryTest() {
		String currentdir = Util.getCurrentRunningDir();
		String dir = reader.getDirectory(currentdir);
		boolean found = dir != null;
		assertTrue(found);
		dir = reader.getDirectory(currentdir + "-not-found");
		found = dir == null;
		assertTrue(found);

		//get dir from a file
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};
		String[] list = reader.list(currentdir, filter);
		String file = list[0];
		dir = reader.getDirectory(currentdir + Util.FILE_SEPARATOR + file);
		found = dir != null;
		assertTrue(found);
	}
	
	
	@Test
	public void saveFile_resultIsNoError() throws IOException{
		InputStream istream = new ByteArrayInputStream(new byte[]{1,2});
		File mockFile = Mockito.mock(File.class);
		
		doReturn(mockFile).when(reader).createFile(any(String.class));
		Mockito.when(mockFile.exists()).thenReturn(true);
		Mockito.when(mockFile.createNewFile()).thenReturn(true);
		FileOutputStream outputStreamMock = mock(FileOutputStream.class);
		doReturn(outputStreamMock).when(reader).getFileOutputStream(any(String.class));
		
		reader.saveFile(istream, Util.getCurrentRunningDir());
	}
	
	@Test
	public void deleteFile_testresultIsFalse(){
		File mockFile = Mockito.mock(File.class);
		doReturn(mockFile).when(reader).createFile(any(String.class));
		doReturn(true).when(reader).fileExist(any(String.class));
		boolean testResult = reader.deleteFile(Util.getCurrentRunningDir());
		assertFalse(testResult);
	}
	
	@Test
	public void deleteFile_testresultIsTrue(){
		File mockFile = Mockito.mock(File.class);
		doReturn(mockFile).when(reader).createFile(any(String.class));
		doReturn(true).when(reader).fileExist(any(String.class));
		Mockito.when(mockFile.delete()).thenReturn(true);
		boolean testResult = reader.deleteFile(Util.getCurrentRunningDir());
		assertTrue(testResult);
	}

}
