package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.att.aro.core.BaseTest;
import com.att.aro.core.peripheral.ICpuActivityParser;
import com.att.aro.core.peripheral.pojo.CpuActivity;

public class CpuActivityParserImplTest extends BaseTest {
	CpuActivityParserImpl reader;

	@Test
	public void readData() throws IOException {
		reader = (CpuActivityParserImpl)context.getBean(ICpuActivityParser.class);

		CpuActivity cpuActivity = reader.parseCpuLine("1.413913604E9 56 /sbin/adbd=29 com.android.systemui=9 /system/bin/surfaceflinger=4 system_server=3 top=1", 1.413913604E9);
		double usage = cpuActivity.getTotalCpuUsage();
		assertEquals(56, usage, 0);

	}

}
