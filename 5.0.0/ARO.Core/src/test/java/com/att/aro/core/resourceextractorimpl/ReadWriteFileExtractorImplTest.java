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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.resourceextractor.IReadWriteFileExtractor;

public class ReadWriteFileExtractorImplTest extends BaseTest{
	
	ReadWriteFileExtractorImpl rwFileExtractor;
	IFileManager filemanager;
	ClassLoader loader;
	InputStream stream;
	@Before
	public void setUp(){
		rwFileExtractor = (ReadWriteFileExtractorImpl)context.getBean(IReadWriteFileExtractor.class);
		filemanager = Mockito.mock(IFileManager.class);
		loader = Mockito.mock(ClassLoader.class);
		
		
	}
	
	@Test
	public void exctactFilesFailTest() throws IOException{
		Mockito.doAnswer(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}}).when(filemanager).saveFile(Mockito.any(InputStream.class), Mockito.anyString());
		rwFileExtractor.setFileManager(filemanager);
		
		
		//stream = new ByteArrayInputStream("hello world".getBytes());
		Mockito.when(loader.getResourceAsStream(Mockito.anyString())).thenReturn(stream);
		
		String resourceName = "tcpdump";
		
		boolean fileCreation = true;

		fileCreation = rwFileExtractor.extractFiles("/Users", resourceName, loader);
		
		assertEquals(false, fileCreation);
	}
	@Test
	public void exctactFilesPassTest() throws IOException{
		Mockito.doAnswer(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}}).when(filemanager).saveFile(Mockito.any(InputStream.class), Mockito.anyString());
		rwFileExtractor.setFileManager(filemanager);
		
		
		stream = new ByteArrayInputStream("hello world".getBytes());
		Mockito.when(loader.getResourceAsStream(Mockito.anyString())).thenReturn(stream);
		
		String resourceName = "tcpdump";
		
		boolean fileCreation = true;

		fileCreation = rwFileExtractor.extractFiles("/Users", resourceName, loader);
		
		assertEquals(true, fileCreation);
	}
	@Test
	public void exctactFilesWithExceptionTest() throws IOException{
		Mockito.doAnswer(new Answer(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				throw new IOException();
			}}).when(filemanager).saveFile(Mockito.any(InputStream.class), Mockito.anyString());
		rwFileExtractor.setFileManager(filemanager);
		
		stream = new ByteArrayInputStream("hello world".getBytes());
		Mockito.when(loader.getResourceAsStream(Mockito.anyString())).thenReturn(stream);
		
		String resourceName = "tcpdump";
		
		boolean fileCreation = true;

		fileCreation = rwFileExtractor.extractFiles("/Users", resourceName, loader);
		
		assertEquals(false, fileCreation);
	}
}
