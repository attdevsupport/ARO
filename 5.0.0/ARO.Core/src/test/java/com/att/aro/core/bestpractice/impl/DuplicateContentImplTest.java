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

public class DuplicateContentImplTest extends BaseTest{
	PacketAnalyzerResult tracedata;
	CacheEntry[] entryArray = new CacheEntry[5];
	CacheAnalysis cacheAnalysis;
	CacheControlImpl cacheControlImpl;
	long value = 1048576; //DUPLICATE_CONTENT_DENOMINATOR = 1048576
	DuplicateContentImpl duplicateContentImpl;
	@Before
	public void setUp(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		for(int i = 0; i < 5; i++){
			entryArray[i] = mock(CacheEntry.class);
		}		 
		cacheAnalysis = Mockito.mock(CacheAnalysis.class);		
	}
	
	
	@Test
	public void runTest_resTypeIsPass(){
		List<CacheEntry> duplicateContent = new ArrayList<CacheEntry>();

		for(int i = 0; i < 2; i++){
			duplicateContent.add(entryArray[i]);
		}
		
		Mockito.when(cacheAnalysis.getDuplicateContentBytes()).thenReturn(value);
		Mockito.when(cacheAnalysis.getDuplicateContentBytesRatio()).thenReturn(0.1);
		Mockito.when(cacheAnalysis.getTotalBytesDownloaded()).thenReturn(value);
		Mockito.when(cacheAnalysis.getDuplicateContent()).thenReturn(duplicateContent);
		Mockito.when(tracedata.getCacheAnalysis()).thenReturn(cacheAnalysis);

		duplicateContentImpl = (DuplicateContentImpl)context.getBean("duplicateContent");
		AbstractBestPracticeResult testResult = duplicateContentImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,testResult.getResultType());
	}
	@Test
	public void runTest_resTypeIsFail(){
		List<CacheEntry> duplicateContent = new ArrayList<CacheEntry>();


		for(int i = 0; i < 5; i++){
			duplicateContent.add(entryArray[i]);
		}
		
		Mockito.when(cacheAnalysis.getDuplicateContentBytes()).thenReturn(value);
		Mockito.when(cacheAnalysis.getDuplicateContentBytesRatio()).thenReturn(1.0);
		Mockito.when(cacheAnalysis.getDuplicateContent()).thenReturn(duplicateContent);
		Mockito.when(tracedata.getCacheAnalysis()).thenReturn(cacheAnalysis);

		duplicateContentImpl = (DuplicateContentImpl)context.getBean("duplicateContent");
		AbstractBestPracticeResult testResult = duplicateContentImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,testResult.getResultType());
	}

}
