package com.att.aro.core.peripheral.impl;


import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.ICpuActivityReader;
import com.att.aro.core.peripheral.pojo.CpuActivityList;

public class CpuActivityReaderImplTest extends BaseTest {
	CpuActivityReaderImpl reader;

	@Test
	public void readData() throws IOException {
		reader = (CpuActivityReaderImpl)context.getBean(ICpuActivityReader.class);
		IFileManager filereader = Mockito.mock(IFileManager.class);
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] arr = new String[] {"1.413913535E9 73 /sbin/adbd=14 com.att.android.arodatacollector=5 system_server=4 /system/bin/surfaceflinger=2 com.android.systemui=2 top=1 com.google.process.gapps=1",
				"1.41391354E9 49 /sbin/adbd=24 /system/bin/surfaceflinger=4 system_server=4 com.android.settings=3 com.android.htcdialer=1 top=1 com.att.android.arodatacollector=1",
				"1.413913545E9 46 /sbin/adbd=22 system_server=5 com.android.systemui=5 /system/bin/surfaceflinger=4 top=1",
				"1.41391355E9 60 /sbin/adbd=19 system_server=5 /system/bin/surfaceflinger=4 com.htc.launcher=2 ./data/data/com.att.android.arodatacollector/tcpdump=1 top=1",
				"1.413913556E9 87 com.android.browser=35 /sbin/adbd=18 ./data/data/com.att.android.arodatacollector/tcpdump=13 /system/bin/surfaceflinger=2 logcat=1 RX_Thread=1 kswapd0=1 system_server=1",
				"1.413913561E9 88 com.android.browser=38 /sbin/adbd=21 ./data/data/com.att.android.arodatacollector/tcpdump=3 /system/bin/surfaceflinger=2 system_server=1 top=1",
				"1.413913567E9 85 com.android.browser=38 /sbin/adbd=23 ./data/data/com.att.android.arodatacollector/tcpdump=11 /system/bin/surfaceflinger=2 top=1",
				"1.413913572E9 51 /sbin/adbd=30 com.android.browser=7 /system/bin/surfaceflinger=3 top=1",
				"1.413913577E9 41 /sbin/adbd=23 /system/bin/surfaceflinger=5 system_server=2 com.android.browser=1 top=1",
				"1.413913583E9 38 /sbin/adbd=18 com.android.browser=11 /system/bin/surfaceflinger=2 top=1",
				"1.413913588E9 93 com.android.browser=42 /sbin/adbd=20 ./data/data/com.att.android.arodatacollector/tcpdump=5 /system/bin/surfaceflinger=1 RX_Thread=1 logcat=1 top=1 system_server=1",
				"1.413913594E9 92 com.android.browser=23 /sbin/adbd=23 /system/bin/surfaceflinger=3 ./data/data/com.att.android.arodatacollector/tcpdump=2 system_server=1 top=1",
				"1.413913599E9 81 com.android.browser=22 /sbin/adbd=21 com.android.systemui=6 /system/bin/surfaceflinger=6 system_server=5 ./data/data/com.att.android.arodatacollector/tcpdump=1 top=1",
				"1.413913604E9 56 /sbin/adbd=29 com.android.systemui=9 /system/bin/surfaceflinger=4 system_server=3 top=1",
				"1.41391361E9 35 /sbin/adbd=18 /system/bin/surfaceflinger=4 system_server=2 top=1",
				"1.413913615E9 51 /sbin/adbd=41 /system/bin/surfaceflinger=4 top=2 system_server=1 kworker/0:2=1",
				"1.413913621E9 47 /sbin/adbd=23 /system/bin/surfaceflinger=5 com.android.settings=4 system_server=3 top=2",
				"1.413913626E9 56 /sbin/adbd=25 com.android.systemui=7 /system/bin/surfaceflinger=5 system_server=3 top=1",
				"1.413913632E9 87 com.android.browser=47 /sbin/adbd=21 ./data/data/com.att.android.arodatacollector/tcpdump=4 /system/bin/surfaceflinger=2 top=1 com.att.android.arodatacollector=1",
				"1.413913637E9 86 com.android.browser=45 /sbin/adbd=24 /system/bin/surfaceflinger=3 ./data/data/com.att.android.arodatacollector/tcpdump=1 top=1",
				"1.413913642E9 80 com.android.browser=38 /sbin/adbd=26 ./data/data/com.att.android.arodatacollector/tcpdump=9 /system/bin/surfaceflinger=3 top=1",
				"1.413913648E9 92 com.android.browser=45 /sbin/adbd=20 ./data/data/com.att.android.arodatacollector/tcpdump=8 /system/bin/surfaceflinger=4 system_server=2 top=1 kswapd0=1",
				"1.413913654E9 51 /sbin/adbd=34 /system/bin/surfaceflinger=2 top=1 com.android.browser=1 kworker/0:2=1",""
		};

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		reader.setFileReader(filereader);
		CpuActivityList info = reader.readData("/", 0.0);
		assertTrue(info.getCpuActivities().size() == 23);

		assertTrue("what", info.isProcessSelected("./data/data/com.att.android.arodatacollector"));
		
		info.recalculateTotalCpu();
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		reader.setFileReader(filereader);
		info = reader.readData("/", 0.0);
		assertTrue(info.getCpuActivities().size() == 0);
		
	}
	
	@Test
	public void readData_IOException() throws IOException {
		reader = (CpuActivityReaderImpl)context.getBean(ICpuActivityReader.class);
		IFileManager filereader = Mockito.mock(IFileManager.class);
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);

		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("test exception"));
		reader.setFileReader(filereader);
		CpuActivityList info = reader.readData("/", 0.0);

		assertTrue(info.getCpuActivities().size() == 0);
		
	}

}
