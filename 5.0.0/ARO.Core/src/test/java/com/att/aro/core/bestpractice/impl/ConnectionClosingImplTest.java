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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

public class ConnectionClosingImplTest extends BaseTest{
	Burst burst01;
	PacketAnalyzerResult tracedata;
	BurstCollectionAnalysisData burstCollectionAnalysisData;
	ConnectionClosingImpl connClsImpl;
	@Before
	public void setup(){
		burst01 = Mockito.mock(Burst.class);
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);		
	}

	@Test
	public void runTest_resIsNoErrTypeIsFail(){
		Mockito.when(burst01.getBurstCategory()).thenReturn(BurstCategory.TCP_PROTOCOL);
		Mockito.when(burst01.getEnergy()).thenReturn(1.0);
		Mockito.when(burst01.getBeginTime()).thenReturn(2.0);
		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(burstCollectionAnalysisData.getTotalEnergy()).thenReturn(1.0);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		connClsImpl = (ConnectionClosingImpl)context.getBean("connectionClosing");
		AbstractBestPracticeResult result = connClsImpl.runTest(tracedata);		
		assertEquals(BPResultType.FAIL, result.getResultType());
	}
	
	@Test
	public void runTest_resIsNoErrTypeIsPass(){
		Mockito.when(burstCollectionAnalysisData.getTotalEnergy()).thenReturn(0.0);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		connClsImpl = (ConnectionClosingImpl)context.getBean("connectionClosing");
		AbstractBestPracticeResult result = connClsImpl.runTest(tracedata);		
		assertEquals(BPResultType.PASS, result.getResultType());
	}
	
}
