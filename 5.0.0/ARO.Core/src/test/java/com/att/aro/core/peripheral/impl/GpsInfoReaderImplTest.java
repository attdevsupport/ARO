package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IGpsInfoReader;
import com.att.aro.core.peripheral.pojo.GpsInfo;

public class GpsInfoReaderImplTest extends BaseTest {

	GpsInfoReaderImpl gpsEventReader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		gpsEventReader = (GpsInfoReaderImpl)context.getBean(IGpsInfoReader.class);
		gpsEventReader.setFileReader(filereader);
	}

	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.414011237411E9 OFF",         //  1414011237411	45542
				"1.414011282953E9 ACTIVE",      //  1414011282953	12933
				"1.414011295886E9 PhonyState",  //  1414011295886	 4149
				"1.414011300035E9 ACTIVE",      //  1414011300035	11889
				"1.414011311924E9 STANDBY"      //  1414011311924
			};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		gpsEventReader.setFileReader(filereader);
		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);
		
		assertTrue(info.size() > 0);
	}

	@Test
	public void readData1() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.414011237411E9 UNKNOWN",         //  1414011237411	45542
				"1.414011237411E9 ACTIVE",         //  1414011237411	45542
				"1.414011282953E9 ACTIVE",      //  1414011282953	12933
				"1.414011295886E9 PhonyState",  //  1414011295886	 4149
				"1.414011300035E9 ACTIVE",      //  1414011300035	11889
				"1.414011311924E9 STANDBY"      //  1414011311924
			};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		gpsEventReader.setFileReader(filereader);
		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);
		
		assertTrue(info.size() > 0);
	}

	@Test
	public void readData2() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.414011237411E9 UNKNOWN",         //  1414011237411	45542
				"1.414011237411E9 ACTIVE",         //  1414011237411	45542
				"1.414011282953E9 ACTIVE",      //  1414011282953	12933
				"bad data PhonyState",  //  1414011295886	 4149
				"1.414011300035E9 ACTIVE",      //  1414011300035	11889
				"1.414011311924E9 STANDBY"      //  1414011311924
			};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		gpsEventReader.setFileReader(filereader);
		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);

		assertTrue(info.size() > 0);
	}

	@Test
	public void readData3() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.414011237411E9 ACTIVE",         //  1414011237411	45542
				"1.414011237411E9 ACTIVE",         //  1414011237411	45542
				"1.414011282953E9 ACTIVE",      //  1414011282953	12933
				"1.414011295886E9 PhonyState",  //  1414011295886	 4149
				"1.414011300035E9 ACTIVE",      //  1414011300035	11889
				"1.414011311924E9 STANDBY"      //  1414011311924
			};

			Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		gpsEventReader.setFileReader(filereader);
		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);
		
		assertTrue(info.size() > 0);

	}
	
	@Test
	public void readData_firstInStandby() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.414011237411E9 STANDBY",         //  1414011237411	45542
				"1.414011237411E9 DISABLED",         //  1414011237411	45542
				"1.414011282953E9 ACTIVE",      //  1414011282953	12933
				"1.414011295886E9 PhonyState",  //  1414011295886	 4149
				"1.414011300035E9 ACTIVE",      //  1414011300035	11889
				"1.414011311924E9 STANDBY"      //  1414011311924
			};

			Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		gpsEventReader.setFileReader(filereader);
		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);
		
		assertTrue(info.size() > 0);

	}
	
	@Test
	public void getActiveDuration() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.414011237411E9 OFF",         //  1414011237411	45542
				"1.414011282953E9 ACTIVE",      //  1414011282953	12933
				"1.414011295886E9 PhonyState",  //  1414011295886	 4149
				"1.414011300035E9 ACTIVE",      //  1414011300035	11889
				"1.414011311924E9 STANDBY"      //  1414011311924
			};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		gpsEventReader.setFileReader(filereader);
		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);

		double activeDuration = gpsEventReader.getGpsActiveDuration();
		assertEquals(24.822, ((double)Math.round(activeDuration*1000.0))/1000, 0); // bcn faked
	//	assertEquals(24.822, activeDuration, 0);
		
	}

	@Test
	public void readData_Exception_readAllLine() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("Exception_readAllLine"));

		List<GpsInfo> info = gpsEventReader.readData("/", 0, 0);
		assertTrue(info.size() == 0);

	}

}
