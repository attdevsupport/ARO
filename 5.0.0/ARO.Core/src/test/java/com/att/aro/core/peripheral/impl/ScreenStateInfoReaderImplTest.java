package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IScreenStateInfoReader;
import com.att.aro.core.peripheral.pojo.ScreenStateInfo;

public class ScreenStateInfoReaderImplTest extends BaseTest {

	ScreenStateInfoReaderImpl traceDataReader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		traceDataReader = (ScreenStateInfoReaderImpl)context.getBean(IScreenStateInfoReader.class);
		traceDataReader.setFileReader(filereader);
	}


	@Test
	public void readData() throws IOException {
		String[] arr;
		List<ScreenStateInfo> listScreenStateInfo;
		
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				"1.414092110985E9 ON 15 30.0",    //  0
				"1.414092182923E9 OFF",           //  1
				"1.414092195631E9 ON 15 32.0",    //  2
				"1.414092264469E9 ON 15 33.0",    //  3
				"1.414092292153E9 OFF",           //  4
				"1.414092303535E9 ON 15 35.0"     //  5
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		traceDataReader.setFileReader(filereader);
		listScreenStateInfo = traceDataReader.readData("/", 0, 0);
				
		assertEquals(6, listScreenStateInfo.size(), 0);
		
		assertEquals(1.414092110985E9, listScreenStateInfo.get(0).getBeginTimeStamp(), 0);
		assertEquals("SCREEN_ON", listScreenStateInfo.get(0).getScreenState().toString());			
		assertEquals(15, listScreenStateInfo.get(0).getScreenTimeout(), 0);			
		assertEquals("30.0", listScreenStateInfo.get(0).getScreenBrightness());		

		assertEquals(1.414092182923E9, listScreenStateInfo.get(1).getBeginTimeStamp(), 0);
		assertEquals("SCREEN_OFF", listScreenStateInfo.get(1).getScreenState().toString());		

		assertEquals(1.414092303535E9, listScreenStateInfo.get(5).getBeginTimeStamp(), 0);
		assertEquals("SCREEN_ON", listScreenStateInfo.get(5).getScreenState().toString());			
		assertEquals(15, listScreenStateInfo.get(5).getScreenTimeout(), 0);			
		assertEquals("35.0", listScreenStateInfo.get(5).getScreenBrightness());		

		
		/*
		 * starts with OFF
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				"1.414092110985E9 OFF",   	
				"1.414092182923E9 ON 15 30.0",  
				"1.414092195631E9 ON 15 32.0"  
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		traceDataReader.setFileReader(filereader);
		listScreenStateInfo = traceDataReader.readData("/", 0, 0);

		/*
		 * starts with an unknown screen state
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				"1.414092110986E9 UNKNOWN",   	
				"1.414092182923E9 ON 15 30.0",  
				"1.414092195631E9 ON 15 32.0"  
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		traceDataReader.setFileReader(filereader);
		listScreenStateInfo = traceDataReader.readData("/", 0, 0);
		assertEquals(3, listScreenStateInfo.size(), 0);


		/*
		 * has embedded UNKNOWN and short line
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				"1.414092110985E9 OFF",   	
				"1.414092182923E9 ON 15 30.0",  
				"1.414092264469E9 UNKNOWN",  
				"1.414092292153E9",  
				"1.414092264469E9 blah"  
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		traceDataReader.setFileReader(filereader);
		listScreenStateInfo = traceDataReader.readData("/", 0, 0);

		
		/*
		 * starts with blank line
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				"",   	
				"1.414092182923E9 ON 15 30.0",  
				"1.414092264469E9 UNKNOWN",  
				"1.414092292153E9 OFF",  
				"1.414092264469E9 blah"  
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		traceDataReader.setFileReader(filereader);
		listScreenStateInfo = traceDataReader.readData("/", 0, 0);

		
		/*
		 * starts with OFF
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		arr = new String[] {
				"1.414092110985E9 OFF",  
				"1.414092264469E9 blah"
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		traceDataReader.setFileReader(filereader);
		listScreenStateInfo = traceDataReader.readData("/", 0, 0);
		
		assertEquals(0, listScreenStateInfo.size(), 0);
		
	}
}
