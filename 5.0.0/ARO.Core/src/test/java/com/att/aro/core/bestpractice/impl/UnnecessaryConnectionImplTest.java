package com.att.aro.core.bestpractice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionResult;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCategory;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetreader.pojo.Packet;

public class UnnecessaryConnectionImplTest extends BaseTest{

	UnnecessaryConnectionImpl unConnImpl;
	
	Packet packet;
	Burst[] burstArray = new Burst[26];

	
	PacketAnalyzerResult tracedata;
	BurstCollectionAnalysisData burstCollectionAnalysisData;
	
	@Before
	public void setUp(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);
		
		for(int i = 0; i < 26; i++){
			burstArray[i] = mock(Burst.class);
		}

	}
	
	@Test
	public void runTest_resTypeIsFail(){
		Date date = new Date(); 
		for(int j = 0 ; j < 20 ; j++){
			double timeLine = 0.0;
			when(burstArray[j].getBurstCategory()).thenReturn(BurstCategory.TCP_LOSS_OR_DUP);
			when(burstArray[j].getBeginTime()).thenReturn((date.getTime()/1000)+4.0+timeLine);
			when(burstArray[j].getEndTime()).thenReturn((date.getTime()/1000)+10.0+timeLine);
	
			when(burstArray[j+1].getBurstCategory()).thenReturn(BurstCategory.PERIODICAL);
			when(burstArray[j+1].getBeginTime()).thenReturn((date.getTime()/1000)+11.0+timeLine);
			when(burstArray[j+1].getEndTime()).thenReturn((date.getTime()/1000)+23.0+timeLine);
	
			when(burstArray[j+2].getBurstCategory()).thenReturn(BurstCategory.LONG);
			when(burstArray[j+2].getBeginTime()).thenReturn((date.getTime()/1000)+25.0+timeLine);
			when(burstArray[j+2].getEndTime()).thenReturn((date.getTime()/1000)+40.0+timeLine);
			
			when(burstArray[j+3].getBurstCategory()).thenReturn(BurstCategory.CPU);
			when(burstArray[j+3].getBeginTime()).thenReturn((date.getTime()/1000)+41.0+timeLine);
			when(burstArray[j+3].getEndTime()).thenReturn((date.getTime()/1000)+50.0+timeLine);
	
			when(burstArray[j+4].getBurstCategory()).thenReturn(BurstCategory.SERVER_NET_DELAY);
			when(burstArray[j+4].getBeginTime()).thenReturn((date.getTime()/1000)+51.0+timeLine);
			when(burstArray[j+4].getEndTime()).thenReturn((date.getTime()/1000)+65.0+timeLine);
			
			j=j+5;
			timeLine = timeLine +20.0;
		}

		List<Burst> burstCollection = new ArrayList<Burst>();
		for(int i = 0; i < 26; i++){
		burstCollection.add(burstArray[i]);
		}
		
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		unConnImpl = (UnnecessaryConnectionImpl) context.getBean("unnecessaryConnection");
		unConnImpl.runTest(tracedata);
		AbstractBestPracticeResult result = unConnImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType());

	}

	@Test
	public void runTest_resTypeIsPass(){
		when(burstArray[0].getBurstCategory()).thenReturn(BurstCategory.SCREEN_ROTATION);
		when(burstArray[0].getBeginTime()).thenReturn(1.0);		
		when(burstArray[0].getEndTime()).thenReturn(5.0);
		when(burstArray[1].getBurstCategory()).thenReturn(BurstCategory.CPU);
		
		when(burstArray[1].getEndTime()).thenReturn(6.0);
		List<Burst> burstCollection = new ArrayList<Burst>();
		
		burstCollection.add(burstArray[0]);
		burstCollection.add(burstArray[1]);
		Mockito.when(burstCollectionAnalysisData.getBurstCollection()).thenReturn(burstCollection);
		Mockito.when(tracedata.getBurstcollectionAnalysisData()).thenReturn(burstCollectionAnalysisData);
		unConnImpl = (UnnecessaryConnectionImpl) context.getBean("unnecessaryConnection");
		unConnImpl.runTest(tracedata);
		AbstractBestPracticeResult result = unConnImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,result.getResultType());

	}

}
