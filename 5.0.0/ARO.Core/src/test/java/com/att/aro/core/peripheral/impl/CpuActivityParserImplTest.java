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
