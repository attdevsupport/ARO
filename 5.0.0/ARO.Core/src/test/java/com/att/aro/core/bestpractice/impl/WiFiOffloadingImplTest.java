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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetreader.pojo.Packet;

public class WiFiOffloadingImplTest extends BaseTest{

	WiFiOffloadingImpl wifiOffloadingImpl;
	Burst burst01;
	Burst burst02;
	PacketAnalyzerResult tracedata;
	BurstCollectionAnalysisData burstCollectionAnalysisData;
	
	@Before
	public void setup(){
		burst01 = Mockito.mock(Burst.class);
		burst02 = Mockito.mock(Burst.class);
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);		
		
	}

	@Test
	public void runTest_resTypeIsPass(){
		Mockito.when(burstCollectionAnalysisData.getLongBurstCount()).thenReturn(2);
		Mockito.when(burst01.getBeginTime()).thenReturn(2.0);
		Mockito.when(burst01.getEndTime()).thenReturn(4.0);

		Mockito.when(burst02.getBeginTime()).thenReturn(3.0);
		Mockito.when(burst02.getEndTime()).thenReturn(4.0);
		
		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		burstCollection.add(burst02);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		wifiOffloadingImpl = (WiFiOffloadingImpl) context.getBean("wifiOffloading");
		AbstractBestPracticeResult result = wifiOffloadingImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,result.getResultType());

	}
	@Test
	public void runTest_resTypeIsFail(){
		Mockito.when(burstCollectionAnalysisData.getLongBurstCount()).thenReturn(5);
		Mockito.when(burst01.getBeginTime()).thenReturn(0.0);
		Mockito.when(burst01.getEndTime()).thenReturn(1.0);
		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		wifiOffloadingImpl = (WiFiOffloadingImpl) context.getBean("wifiOffloading");
		AbstractBestPracticeResult result = wifiOffloadingImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType());

	}

}
