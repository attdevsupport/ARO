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
package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IDeviceInfoReader;

public class DeviceInfoReaderImplTest extends BaseTest {

	DeviceInfoReaderImpl reader;

	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		reader = (DeviceInfoReaderImpl)context.getBean(IDeviceInfoReader.class);
		reader.setFileReader(filereader);
	}

	@Test
	public void readData() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { 
				  "10.171.214.81"
				, "fe80::1a87:96ff:fe80:1735%wlan0"
				, "" 
			});

		Set<InetAddress> info = reader.readData(traceFolder);
		assertTrue(info.contains(InetAddress.getByName("10.171.214.81")));
		assertTrue(info.contains(InetAddress.getByName("fe80::1a87:96ff:fe80:1735")));
		assertTrue(info.contains(InetAddress.getByName("")));

	}
	
	@Test
	public void readData_UnknownHost() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(new String[] { 
				  "mangled 10.171.214.blah"
				, "mangled fe80::1a87:96ff:fe80:1735%wlan0"
				, "mangled" 
			});

		Set<InetAddress> info = reader.readData(traceFolder);
		assertTrue(info.size() == 0);

	}

	@Test
	public void readData_NoFile() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);

		Set<InetAddress> info = reader.readData(traceFolder);
		assertTrue(info.size() == 0);

	}
	
}
