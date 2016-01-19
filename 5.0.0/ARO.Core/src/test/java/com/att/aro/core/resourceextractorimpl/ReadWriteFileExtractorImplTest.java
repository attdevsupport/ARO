/**
 * 
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

/**
 * @author Harikrishna 
 *
 */
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
