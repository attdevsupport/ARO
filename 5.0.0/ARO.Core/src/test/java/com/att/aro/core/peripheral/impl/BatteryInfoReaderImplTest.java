package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IBatteryInfoReader;
import com.att.aro.core.peripheral.pojo.BatteryInfo;

public class BatteryInfoReaderImplTest extends BaseTest{
	
	BatteryInfoReaderImpl batteryreader;
	
	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		batteryreader = (BatteryInfoReaderImpl)context.getBean(IBatteryInfoReader.class);
		batteryreader.setFileReader(filereader);
	}

	@Test
	public void readData() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] { "1.389038277403E9 33 37 false", "1.389038308374E9 32 37 false", "1.389038365419E9 32 37 false" });
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 2);
	}
	
	@Test
	public void duplicateState() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] { 
						  "1.389038277403E9 33 37 false"
						, "1.389038308374E9 32 37 false"
						, "1.389038308384E9 32 37 false"
						, "1.389038365419E9 32 37 false" });
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 2);
	}
	
	@Test
	public void invalidLength() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] { "1.389038277403E9 3 3 37 false", "1.389038308374E9 32 37 false", "1.389038365419E9 32 37 false" });
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 1);
	}
	
	@Test
	public void badIntegerParse() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] { "1.38903827740zE9 33 37 false", "1.389038308374E9 32 37 false", "1.389038365419E9 32 37 false" });
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 1);
	}
	
	@Test
	public void readNull() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(null);
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 0);
	}

	@Test
	public void readNoLines() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] {});
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 0);
	}

	@Test
	public void readAllLineException() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("failed on purpose"));
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 0);
	}
	
	@Test
	public void noFile() throws IOException{
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(
				new String[] { "1.389038277403E9 33 37 false", "1.389038308374E9 32 37 false", "1.389038365419E9 32 37 false" });
		List<BatteryInfo> batteryInfos = batteryreader.readData("/", 0);
		assertTrue(batteryInfos.size() == 0);
	}
}
