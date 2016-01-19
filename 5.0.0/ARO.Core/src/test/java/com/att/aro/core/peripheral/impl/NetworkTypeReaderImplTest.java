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
import com.att.aro.core.packetanalysis.pojo.NetworkBearerTypeInfo;
import com.att.aro.core.peripheral.INetworkTypeReader;
import com.att.aro.core.peripheral.pojo.NetworkType;
import com.att.aro.core.peripheral.pojo.NetworkTypeObject;

public class NetworkTypeReaderImplTest extends BaseTest {

	NetworkTypeReaderImpl networkTypeReader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		networkTypeReader = (NetworkTypeReaderImpl)context.getBean(INetworkTypeReader.class);
		networkTypeReader.setFileReader(filereader);
	}


	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {
				"1.358205538539E9 10",       //  0
				"1.358205540396E9 15",       //  1
				"1.35820554882E9 3",         //  2
				"1.358205549444E9 1",       //  3
				"1.358205552859E9 9",       //  4
				"1.358205564577E9 3",        //  5
				"1.358205565208E9 10",       //  6
				"1.358205565834E9 15",       //  7
				"1.358205572238E9 3",        //  8
				"1.358205572969E9 8",       //  9
				"1.358205584581E9 3",        // 10
				"1.358205586095E9 13",       // 11
				"1.358205590906E9 3",        // 12
				"1.358205591561E9 2",       // 13
				"1.358205594481E9 5",       // 14
				"1.358205605874E9 3",        // 15
				"1.358205606144E9 0",       // 16
				"1.358205607302E9 15",       // 17
				"1.358205614199E9 -1",       // 18
				"1.358205623225E9 -1",       // 19
				"1.358205763101E9 15",       // 20
				"1.358205779064E9 3",        // 21
				"1.358205779663E9 33",       // 22
				"1.358205782276E9 fe",       // 23
				"1.358205790737E9 3",        // 24
				"1.358205791067E9 10",       // 25
				"1.358205801382E9 15",       // 26
			};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		networkTypeReader.setFileReader(filereader);
		NetworkTypeObject info = networkTypeReader.readData("/", 0, 0);

		List<NetworkType> listNetworkType = info.getNetworkTypesList();
		assertEquals(11, listNetworkType.size(), 0);
		
		List<NetworkBearerTypeInfo> listNetworkBearerTypeInfo = info.getNetworkTypeInfos();
		assertEquals(27, listNetworkBearerTypeInfo.size(), 0);	
		assertEquals(1.358205538539E9, listNetworkBearerTypeInfo.get(0).getBeginTimestamp(), 0);
		assertEquals(1.358205801382E9, listNetworkBearerTypeInfo.get(26).getBeginTimestamp(), 0);
		assertEquals("HSPAP", listNetworkBearerTypeInfo.get(26).getNetworkType().toString());

		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		info = networkTypeReader.readData("/", 0, 0);
		assertTrue(listNetworkType != null);

		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		arr = new String[] {
				"1.358205538539E9 bd",
				"1.358205540396E9 15"
		};
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		info = networkTypeReader.readData("/", 0, 0);
		listNetworkType = info.getNetworkTypesList();
		assertEquals(2, listNetworkType.size(), 0);

		
	}
}
