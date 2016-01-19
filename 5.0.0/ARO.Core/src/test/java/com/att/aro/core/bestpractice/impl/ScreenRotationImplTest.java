package com.att.aro.core.bestpractice.impl;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.ScreenRotationResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetreader.pojo.Packet;

public class ScreenRotationImplTest extends BaseTest{
	
	ScreenRotationImpl screenRotationImpl;
	Packet packet;
	Burst burst01;
	PacketAnalyzerResult tracedata;
	BurstCollectionAnalysisData burstCollectionAnalysisData;

	@Test   
	public void runTest_resIsNoErr_Fail(){	
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);
		burst01 = mock(Burst.class);
		Mockito.when(burst01.getBurstCategory()).thenReturn(BurstCategory.SCREEN_ROTATION);
		Mockito.when(burst01.getBeginTime()).thenReturn(1.0);
		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		screenRotationImpl = (ScreenRotationImpl)context.getBean("screenRotation");
		AbstractBestPracticeResult result = screenRotationImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType());
	}
	@Test   
	public void runTest_resIsNoErr_Pass(){	
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);
		burst01 = mock(Burst.class);
		Mockito.when(burst01.getBurstCategory()).thenReturn(BurstCategory.UNKNOWN);
		Mockito.when(burst01.getBeginTime()).thenReturn(1.0);
		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		screenRotationImpl = (ScreenRotationImpl)context.getBean("screenRotation");
		AbstractBestPracticeResult result = screenRotationImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,result.getResultType());

	}


}
