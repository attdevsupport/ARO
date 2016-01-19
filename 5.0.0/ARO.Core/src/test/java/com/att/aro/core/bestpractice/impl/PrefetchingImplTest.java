package com.att.aro.core.bestpractice.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.PrefetchingResult;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.configuration.pojo.ProfileType;
import com.att.aro.core.packetanalysis.pojo.AbstractRrcStateMachine;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.RrcStateMachineType;
import com.att.aro.core.packetreader.pojo.Packet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doThrow;


public class PrefetchingImplTest extends BaseTest{

	PrefetchingImpl preFetching;
	Burst burst01;
	PacketAnalyzerResult tracedata;
	BurstCollectionAnalysisData burstCollectionAnalysisData;
	
	@Before
	public void setup(){
		burst01 = Mockito.mock(Burst.class);
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);
		
		
	}
	
	@Test 
	public void runTest_resTypeIsPass(){
		Mockito.when(burst01.getBurstCategory()).thenReturn(BurstCategory.UNKNOWN);
		List<Burst> burstCollection = new ArrayList<Burst>();
		burstCollection.add(burst01);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		preFetching = (PrefetchingImpl)context.getBean("prefetching");
		AbstractBestPracticeResult result = preFetching.runTest(tracedata);		
		assertEquals(BPResultType.PASS, result.getResultType());
	}
	
	@Test
	public void runTest_resTypeIsFail(){
		Mockito.when(burst01.getBurstCategory()).thenReturn(BurstCategory.USER_INPUT);
		List<Burst> burstCollection = new ArrayList<Burst>();
		for(int i=0;i<6;i++){
			burstCollection.add(burst01);
		}
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		preFetching = (PrefetchingImpl)context.getBean("prefetching");
		AbstractBestPracticeResult result = preFetching.runTest(tracedata);		
		assertEquals(BPResultType.FAIL, result.getResultType());		
	}


}
