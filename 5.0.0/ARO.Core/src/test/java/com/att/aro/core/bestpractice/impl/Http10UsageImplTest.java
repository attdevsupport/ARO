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
import com.att.aro.core.bestpractice.pojo.Http10UsageResult;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

public class Http10UsageImplTest extends BaseTest{

	PacketAnalyzerResult tracedata;
	Http10UsageImpl httpUsageImpl;
	Session session01;
	Session session02;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;

	
	@Before
	public void setup(){

		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		session01 = Mockito.mock(Session.class);
		session02 = Mockito.mock(Session.class);
		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo02 = Mockito.mock(HttpRequestResponseInfo.class);

	}
	
	@Test
	public void runTest_resTypeIsPass(){
		Mockito.when(httpRequestInfo01.getVersion()).thenReturn(HttpRequestResponseInfo.HTTP11);
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);

		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);		
		httpUsageImpl = (Http10UsageImpl)context.getBean("http10Usage");
		
		AbstractBestPracticeResult result = httpUsageImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,result.getResultType() );
		
	}
	
	@Test
	public void runTest_resTypeIsFail(){
		Mockito.when(httpRequestInfo01.getVersion()).thenReturn(HttpRequestResponseInfo.HTTP10);
		Mockito.when(httpRequestInfo02.getVersion()).thenReturn(HttpRequestResponseInfo.HTTP10);
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);

		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);		
		httpUsageImpl = (Http10UsageImpl)context.getBean("http10Usage");
		
		AbstractBestPracticeResult result = httpUsageImpl.runTest(tracedata);
		assertEquals(BPResultType.WARNING,result.getResultType() );
		
	}

	
}
