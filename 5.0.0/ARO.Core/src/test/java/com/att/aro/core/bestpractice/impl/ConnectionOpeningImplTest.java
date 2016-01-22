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
package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

public class ConnectionOpeningImplTest extends BaseTest{

	ConnectionOpeningImpl connOpenImpl;
	PacketAnalyzerResult tracedata;
	
	@Test
	public void runTest_resIsNoError(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		connOpenImpl = (ConnectionOpeningImpl)context.getBean("connectionOpening");
		AbstractBestPracticeResult result = connOpenImpl.runTest(tracedata);
		assertEquals(result.getResultType(), BPResultType.SELF_TEST);
	}

}
