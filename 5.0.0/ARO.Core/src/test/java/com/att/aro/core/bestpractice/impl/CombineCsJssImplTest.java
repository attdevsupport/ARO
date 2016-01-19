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
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public class CombineCsJssImplTest extends BaseTest{
	
	CombineCsJssImpl csjsImpl;
	PacketAnalyzerResult tracedata;
	Session session01;
	Session session02;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;
	PacketInfo pktInfo01;
	PacketInfo pktInfo02;
	
	@Before
	public void setup(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		session01 = Mockito.mock(Session.class);
		session02 = Mockito.mock(Session.class);		 
		pktInfo01 = Mockito.mock(PacketInfo.class);
		pktInfo02 = Mockito.mock(PacketInfo.class);

	}

	@Test
	public void runTest_resIsNoErrResultTypeIsFail(){
		Mockito.when(pktInfo01.getTimeStamp()).thenReturn(1.0);
		Mockito.when(pktInfo02.getTimeStamp()).thenReturn(2.0);

		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("text/css");
		Mockito.when(httpRequestInfo01.getFirstDataPacket()).thenReturn(pktInfo01);
		Mockito.when(httpRequestInfo01.getObjName()).thenReturn("test1.css");
		
		httpRequestInfo02 = Mockito.mock(HttpRequestResponseInfo.class);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn("text/javascript");
		Mockito.when(httpRequestInfo02.getFirstDataPacket()).thenReturn(pktInfo02);
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		Mockito.when(session02.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);
		sessionList.add(session02);

		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		csjsImpl = (CombineCsJssImpl) context.getBean("combineCsJss");	
		AbstractBestPracticeResult result = csjsImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType() );

	}
	
	@Test
	public void runTest_resIsNoErrResultTypeIsFailConsCssJsFirstPacket(){
		Mockito.when(pktInfo01.getTimeStamp()).thenReturn(1.0);
		Mockito.when(pktInfo02.getTimeStamp()).thenReturn(2.0);

		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("text/javascript");
		Mockito.when(httpRequestInfo01.getFirstDataPacket()).thenReturn(pktInfo01);
		Mockito.when(httpRequestInfo01.getObjName()).thenReturn("test2.css");
		
		httpRequestInfo02 = Mockito.mock(HttpRequestResponseInfo.class);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn("application/x-javascript");
		Mockito.when(httpRequestInfo02.getFirstDataPacket()).thenReturn(pktInfo02);
		Mockito.when(httpRequestInfo02.getObjName()).thenReturn("test3.css");
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		Mockito.when(session02.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);
		sessionList.add(session02);

		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		csjsImpl = (CombineCsJssImpl) context.getBean("combineCsJss");	
		AbstractBestPracticeResult result = csjsImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,result.getResultType() );

	}

	@Test
	public void runTest_resIsNoErrResultTypeIsPass(){
		
		Mockito.when(pktInfo01.getTimeStamp()).thenReturn(1.0);
		Mockito.when(pktInfo02.getTimeStamp()).thenReturn(2.0);
		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo01.getFirstDataPacket()).thenReturn(pktInfo01);

		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);

		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		Mockito.when(session02.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);
		sessionList.add(session02);

		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		csjsImpl = (CombineCsJssImpl) context.getBean("combineCsJss");	
		AbstractBestPracticeResult result = csjsImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,result.getResultType() );

	}


}
