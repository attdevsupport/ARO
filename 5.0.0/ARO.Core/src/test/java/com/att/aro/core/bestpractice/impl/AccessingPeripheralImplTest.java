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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;

public class AccessingPeripheralImplTest extends BaseTest{
	Burst burst01;
	PacketAnalyzerResult tracedata;
	TimeRange timeRange;
	TraceDirectoryResult traceResult ;
	AccessingPeripheralImpl acPrphlImpl;
	AnalysisFilter analysisFilter;
	BurstCollectionAnalysisData burstCollectionAnalysisData;
	@Before
	public void setup(){
		burst01 = Mockito.mock(Burst.class);
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		timeRange = Mockito.mock(TimeRange.class);
		traceResult = Mockito.mock(TraceDirectoryResult.class);
		analysisFilter = Mockito.mock(AnalysisFilter.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);		
	}

	@Test
	public void runTest_resIsNoErrResultTypeIsPass(){
		Mockito.when(tracedata.getFilter()).thenReturn(analysisFilter);
		Mockito.when(traceResult.getTraceResultType()).thenReturn(TraceResultType.TRACE_FILE);
		Mockito.when((TraceDirectoryResult)tracedata.getTraceresult()).thenReturn(traceResult);
		acPrphlImpl = (AccessingPeripheralImpl)context.getBean("accessingPeripheral");	
		AbstractBestPracticeResult testResult = acPrphlImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,testResult.getResultType());
	}
	@Test
	public void runTest_resIsNoErrResultTypeIsFail(){
		Mockito.when(tracedata.getFilter()).thenReturn(analysisFilter);
		Mockito.when(timeRange.getBeginTime()).thenReturn(0.0);
		Mockito.when(timeRange.getEndTime()).thenReturn(100.0);
		Mockito.when(traceResult.getCameraActiveDuration()).thenReturn(10.0);
		Mockito.when(traceResult.getBluetoothActiveDuration()).thenReturn(10.0);
		Mockito.when(traceResult.getGpsActiveDuration()).thenReturn(10.0);
		Mockito.when(traceResult.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when((TraceDirectoryResult)tracedata.getTraceresult()).thenReturn(traceResult);
		Mockito.when(analysisFilter.getTimeRange()).thenReturn(timeRange);
		acPrphlImpl = (AccessingPeripheralImpl)context.getBean("accessingPeripheral");	
		AbstractBestPracticeResult testResult = acPrphlImpl.runTest(tracedata);
		assertEquals(BPResultType.WARNING,testResult.getResultType());
	}

}
