package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IRadioInfoReader;
import com.att.aro.core.peripheral.pojo.RadioInfo;

public class RadioInfoReaderImplTest extends BaseTest {

	RadioInfoReaderImpl radioInfoReader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		radioInfoReader = (RadioInfoReaderImpl)context.getBean(IRadioInfoReader.class);
		radioInfoReader.setFileReader(filereader);
	}

	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.41383675837E9 -42",       //  0
				"1.413836771449E9 -51",      //  1
				"1.413836784233E9 -53",      //  2
				"1.41383678679E9 -50",       //  3
				"1.413836807271E9 -51",      //  4
				"1.413836809831E9 -51",      //  5
				"1.413836822639E9 -51",      //  6
				"1.413836825194E9 -51",      //  7
				"1.413836837992E9 -51",      //  8
				"1.413836840548E9 -51",      //  9
				"1.413836845669E9 -41",      // 10
				"1.41383685336E9 -51",       // 11
				"1.41383685592E9 -51",       // 12
				"1.413836861023E9 -51",      // 13
				"1.413836868708E9 -51",      // 14
				"1.413836871277E9 -36",      // 15
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		radioInfoReader.setFileReader(filereader);
		List<RadioInfo> listRadioInfo = radioInfoReader.readData("/", 0);
	
		assertEquals(16, listRadioInfo.size(), 0);
		
		assertEquals(-42, listRadioInfo.get(0).getSignalStrength(), 0);	
		assertEquals(-51, listRadioInfo.get(1).getSignalStrength(), 0);	
		assertEquals(-41, listRadioInfo.get(10).getSignalStrength(), 0);	
		assertEquals(-36, listRadioInfo.get(15).getSignalStrength(), 0);	
		
		assertEquals(1.41383675837E9, listRadioInfo.get(0).getTimeStamp(), 0);	
		assertEquals(1.41383685336E9, listRadioInfo.get(11).getTimeStamp(), 0);	
		assertEquals(1.413836871277E9, listRadioInfo.get(15).getTimeStamp(), 0);	

		
		/*
		 * test LTE data
		 * note: ri.getSignalStrength() >= 0.0
		 * ques: how can signal strength be greater than 0 ???
		 * possible: >=0.0 may just be a check for no signal (0.0)
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				 "1.412720866732E9 25 -86 -6 300 2147483647"
				,"1.412720867835E9 25 -87 -7 300 2147483647"
				,"1.412720870315E9 23 -92 -7 300 2147483647"
				,"1.412720873837E9 21 -94 -7 300 2147483647"
				,"1.412720876836E9 21 -95 -7 300 2147483647"

		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		radioInfoReader.setFileReader(filereader);
		listRadioInfo = radioInfoReader.readData("/", 0);
		
		assertEquals(5, listRadioInfo.size(), 0);
		
		/*
		 * a single line
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				 "1.394754474555E9 -51"

		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		radioInfoReader.setFileReader(filereader);
		listRadioInfo = radioInfoReader.readData("/", 0);
		
		assertEquals(1, listRadioInfo.size(), 0);
		
		/*
		 * a single line, no signal strength
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				 "1.394754474555E9 0"

		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		radioInfoReader.setFileReader(filereader);
		listRadioInfo = radioInfoReader.readData("/", 0);
		
		assertEquals(1, listRadioInfo.size(), 0);
		
		/*
		 * an empty line
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				 ""
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		radioInfoReader.setFileReader(filereader);
		listRadioInfo = radioInfoReader.readData("/", 0);
		
		assertEquals(0, listRadioInfo.size(), 0);
		
		/*
		 * a no signal
		 * each regained signal increases count by 1
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				 "1.414601817968E9 0"
				,"1.414601897874E9 -99"
				,"1.414601903563E9 -99"
				,"1.414601916514E9 -105"
				,"1.414601920485E9 -105"
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		radioInfoReader.setFileReader(filereader);
		listRadioInfo = radioInfoReader.readData("/", 0);
		
		assertEquals(6, listRadioInfo.size(), 0);
		
	}
}
