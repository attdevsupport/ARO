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
import com.att.aro.core.bestpractice.pojo.HttpCode3xxEntry;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;

public class Http3xxCodeImplTest extends BaseTest{
	PacketAnalyzerResult tracedata;
	Session session01;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;
	HttpRequestResponseInfo httpRequestInfo03 ;
	HttpRequestResponseInfo httpRequestInfo04 ;


	Http3xxCodeImpl http3xxCodeImpl;
	
	
	@Before
	public void setup(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		session01 = Mockito.mock(Session.class);
		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo02 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo03 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo04 = Mockito.mock(HttpRequestResponseInfo.class);

	}
	
	@Test
	public void runTest_resTypeIsPass(){
		
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getScheme()).thenReturn(HttpRequestResponseInfo.HTTP_SCHEME);
		Mockito.when(httpRequestInfo01.getStatusCode()).thenReturn(200);
		
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		

		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		http3xxCodeImpl = (Http3xxCodeImpl) context.getBean("http3xx");	
		AbstractBestPracticeResult result = http3xxCodeImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,result.getResultType() );

	} 
  
	@Test
	public void runTest_resTypeIsFail(){ 
		
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getScheme()).thenReturn(HttpRequestResponseInfo.HTTP_SCHEME);
		Mockito.when(httpRequestInfo01.getStatusCode()).thenReturn(304);
		Mockito.when(httpRequestInfo01.getObjName()).thenReturn("");
		
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo02.getObjName()).thenReturn("");
		
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo03.getScheme()).thenReturn(HttpRequestResponseInfo.HTTP_SCHEME);
		Mockito.when(httpRequestInfo03.getStatusCode()).thenReturn(304);
		Mockito.when(httpRequestInfo03.getObjName()).thenReturn("");
		
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo04.getObjName()).thenReturn("");

		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		value.add(httpRequestInfo03);
		value.add(httpRequestInfo04);

		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		http3xxCodeImpl = (Http3xxCodeImpl) context.getBean("http3xx");	
		AbstractBestPracticeResult result = http3xxCodeImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType() );

	}

}
