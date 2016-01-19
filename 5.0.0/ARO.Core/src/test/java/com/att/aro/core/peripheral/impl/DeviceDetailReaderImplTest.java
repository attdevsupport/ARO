package com.att.aro.core.peripheral.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IDeviceDetailReader;
import com.att.aro.core.peripheral.pojo.DeviceDetail;

public class DeviceDetailReaderImplTest extends BaseTest {
	
	DeviceDetailReaderImpl reader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		reader = (DeviceDetailReaderImpl)context.getBean(IDeviceDetailReader.class);
		reader.setFileReader(filereader);
	}

	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"com.att.android.arodatacollector",
				"HTC One X",
				"HTC",
				"android",
				"4.0.4",
				"3.1.1.7",
				"-1",
				"720*1184",
				""};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		
		DeviceDetail info = reader.readData("/");
		assertEquals( "com.att.android.arodatacollector",  info.getCollectorName());
		assertEquals( "HTC One X",                         info.getDeviceModel());
		assertEquals( "HTC",                               info.getDeviceMake());
		assertEquals( "android",                           info.getOsType());
		assertEquals( "4.0.4",                             info.getOsVersion());
		assertEquals( "3.1.1.7",                           info.getCollectorVersion());
		assertEquals( "720*1184",                          info.getScreenSize());
		assertTrue( info.getTotalLines()>0);
		
	}
}
