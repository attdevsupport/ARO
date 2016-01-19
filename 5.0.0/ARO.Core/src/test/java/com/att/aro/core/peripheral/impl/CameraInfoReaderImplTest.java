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
import com.att.aro.core.peripheral.ICameraInfoReader;
import com.att.aro.core.peripheral.pojo.CameraInfo;

public class CameraInfoReaderImplTest extends BaseTest {
		
	CameraInfoReaderImpl cameraReader;
	
	List<CameraInfo> info;
	
	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		cameraReader = (CameraInfoReaderImpl)context.getBean(ICameraInfoReader.class);

		cameraReader.setFileReader(filereader);
		traceFolder = "traceFolder";
	}

	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 OFF"             //   1413913591.806 - 1413913535.177 = 56.629	
				,"1.413913591806E9 ON"              //   1413913610.613 - 1413913591.806 = 18.807	18.807
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 ON"              //   1413913620.883 - 1413913620.683 =  0.200	19.007
				,"1.413913620883E9 OFF"             //   1413913622.533 - 1413913620.883 =  1.650	
				,"1.413913622533E9 OFF"             //   1413913622.533
				,""
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 6);
	}

	@Test
	public void readData1() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 ON"
				,""
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 1);
	}

	@Test
	public void readData2() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 UNKNOWN"
				,"1.413913620683E9 ON"
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 2);
	}

	@Test
	public void readData3() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 ON"
				,"1.413913620683E9 UNKNOWN"
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 2);

	}

	@Test
	public void readData_badData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413badtime5177E9 ON"
				,"1.moreBadData683E9 UNKNOWN"
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 1);

	}

	@Test
	public void readData4() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 OFF"
				,"1.413913620683E9 OFF"
				,"1.413913620783E9 ON" 
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 3);

	}

	@Test
	public void readData_emptyData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 0);
	}

	@Test
	public void readData_nullData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(null);
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 0);
	}

	@Test
	public void readData_Unrecognized() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] {
				 "1.413913535177E9 unrecogized event"
				,"1.413913620683E9 O0f"
				,"1.413913620783E9 wrong" 
		});
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 3);

	}

	@Test
	public void readData_NoFile() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 0);

	}

	@Test
	public void readData_Exception_readAllLine() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("Exception_readAllLine"));

		info = cameraReader.readData(traceFolder, 0, 0);
		assertTrue(info.size() == 0);

	}

	@Test
	public void getActiveDuration() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { //                                                   delta    +delta (sum)
				 "1.413913535177E9 OFF"             //   1413913591.806 - 1413913535.177 = 56.629	
				,"1.413913591806E9 ON"              //   1413913610.613 - 1413913591.806 = 18.807	18.807
				,"1.413913610613E9 OFF"             //   1413913620.683 - 1413913610.613 = 10.070
				,"1.413913620683E9 ON"              //   1413913620.883 - 1413913620.683 =  0.200	19.007
				,"1.413913620883E9 OFF"             //   1413913622.533 - 1413913620.883 =  1.650	
				,"1.413913622533E9 OFF"             //   1413913622.533
				,""
		});
		info = cameraReader.readData(traceFolder, 0, 0);

		double activeDuration = cameraReader.getActiveDuration();
		
		assertEquals(19.007, ((double)Math.round(activeDuration*1000.0))/1000, 0); // bcn faked
	//	assertEquals(19.007, activeDuration, 0);
		
	}
}
