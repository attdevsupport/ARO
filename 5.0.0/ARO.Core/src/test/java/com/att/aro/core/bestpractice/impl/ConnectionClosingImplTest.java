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
