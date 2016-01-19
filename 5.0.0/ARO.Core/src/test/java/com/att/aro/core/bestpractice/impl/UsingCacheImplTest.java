package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.Diagnosis;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;

public class UsingCacheImplTest extends BaseTest{
	UsingCacheImpl usingCacheImpl;
	PacketAnalyzerResult tracedata;
	CacheEntry[] entryArray = new CacheEntry[5];
	CacheAnalysis cacheAnalysis;
	PacketInfo pktInfo01;

	
	@Before
	public void setUp(){
		pktInfo01 = Mockito.mock(PacketInfo.class);
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		for(int i = 0; i < 5; i++){
			entryArray[i] = mock(CacheEntry.class);
		}		 
		cacheAnalysis = Mockito.mock(CacheAnalysis.class);	
		usingCacheImpl = (UsingCacheImpl)context.getBean("usingCache");
	}

	@Test
	public void runTest_(){
		List<CacheEntry> diagnosisResults = new ArrayList<CacheEntry>();
		Mockito.when(entryArray[0].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_REQUEST_NOT_FOUND);
		Mockito.when(entryArray[1].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_INVALID_OBJ_NAME);
		Mockito.when(entryArray[2].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_INVALID_REQUEST);
		Mockito.when(entryArray[3].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_INVALID_RESPONSE);

		for(int i = 0; i < 4; i++){
			diagnosisResults.add(entryArray[i]);
		}
		Mockito.when(cacheAnalysis.getDiagnosisResults()).thenReturn(diagnosisResults);
		Mockito.when(tracedata.getCacheAnalysis()).thenReturn(cacheAnalysis);
		
		AbstractBestPracticeResult testResult = usingCacheImpl.runTest(tracedata);		
		assertEquals(BPResultType.PASS, testResult.getResultType());
	}
	@Test
	public void runTest_Fail(){
		List<CacheEntry> diagnosisResults = new ArrayList<CacheEntry>();
		
		Mockito.when(entryArray[0].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_NOT_CACHABLE);
		Mockito.when(entryArray[0].hasCacheHeaders()).thenReturn(true);
		Mockito.when(entryArray[1].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_CACHE_MISSED);
		Mockito.when(entryArray[1].hasCacheHeaders()).thenReturn(false);
		Mockito.when(entryArray[1].getSessionFirstPacket()).thenReturn(pktInfo01);
		Mockito.when(entryArray[2].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT);
		Mockito.when(entryArray[2].hasCacheHeaders()).thenReturn(true);
		Mockito.when(entryArray[3].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP);
		Mockito.when(entryArray[3].hasCacheHeaders()).thenReturn(true);
		Mockito.when(entryArray[4].getDiagnosis()).thenReturn(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304);
		Mockito.when(entryArray[4].hasCacheHeaders()).thenReturn(false);
		
		for(int i = 0; i < 5; i++){
			diagnosisResults.add(entryArray[i]);
		}
		Mockito.when(cacheAnalysis.getDiagnosisResults()).thenReturn(diagnosisResults);
		Mockito.when(tracedata.getCacheAnalysis()).thenReturn(cacheAnalysis);
		
		AbstractBestPracticeResult testResult = usingCacheImpl.runTest(tracedata);		
		assertEquals(BPResultType.WARNING, testResult.getResultType());
	}

	@Test
	public void runTest_returnIsNull(){
		Mockito.when(tracedata.getCacheAnalysis()).thenReturn(null);
		AbstractBestPracticeResult testResult = usingCacheImpl.runTest(tracedata);		
		assertNull(testResult);
	}

}
