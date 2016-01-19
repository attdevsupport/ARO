package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public class ScriptsImplTest extends BaseTest{
	PacketAnalyzerResult tracedata;
	Session session01;
	Session session02;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;
	HttpRequestResponseInfo httpRequestInfo03 ;
	HttpRequestResponseInfo httpRequestInfo04 ;

	PacketInfo pktInfo01;
	PacketInfo pktInfo02;
	ScriptsImpl scriptImpl;
	IHttpRequestResponseHelper reqhelper;


	String htmlString = 
			"<html><head><title>First parse</title><script src=\"myscripts.js\"></script>"
			+"<script src=\"http://domain.tld/abs/folder/Site.js?20090928090308453\" type=\"text/javascript\"></script>"
			+"<script src=\"http://domain.tld/abs/folder/Site.js?20090928090308453\" type=\"text/javascript\"></script>"
			+"<script src=\"myscripts.js\"></script>"
			+"</head>"+ "<body><p>Parsed HTML into a doc.</p></body></html>";

	@Before
	public void setup(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		session01 = Mockito.mock(Session.class);
		session02 = Mockito.mock(Session.class);		 
		pktInfo01 = Mockito.mock(PacketInfo.class);
		pktInfo02 = Mockito.mock(PacketInfo.class);
		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo02 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo03 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo04 = Mockito.mock(HttpRequestResponseInfo.class);
		reqhelper = Mockito.mock(IHttpRequestResponseHelper.class);
		scriptImpl = (ScriptsImpl)context.getBean("scripts");

	} 

	@Test
	public void runTest_resIsPass(){
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);		
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(reqhelper.isHtml(any(String.class))).thenReturn(false);

		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		value.add(httpRequestInfo03);
		value.add(httpRequestInfo04);
		
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session01);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		scriptImpl.setHttpRequestResponseHelper(reqhelper);

		AbstractBestPracticeResult testResult = scriptImpl.runTest(tracedata);
		scriptImpl.setHttpRequestResponseHelper(null);
		assertEquals(BPResultType.PASS,testResult.getResultType() );
		
	}

	@Test
	public void runTest_resIsFail(){
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn(null);
		Mockito.when(httpRequestInfo02.getContentLength()).thenReturn(0);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);	
		Mockito.when(httpRequestInfo03.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo03.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo04.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo04.getContentLength()).thenReturn(3);
		
		Mockito.when(reqhelper.isHtml(any(String.class))).thenReturn(true);
		try {
			Mockito.when(reqhelper.getContentString(any(HttpRequestResponseInfo.class), any(Session.class)))
			.thenReturn(htmlString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		value.add(httpRequestInfo03);
		value.add(httpRequestInfo04);
		
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session01);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		scriptImpl.setHttpRequestResponseHelper(reqhelper);
		AbstractBestPracticeResult testResult = scriptImpl.runTest(tracedata);
		scriptImpl.setHttpRequestResponseHelper(null);
		assertEquals(BPResultType.FAIL,testResult.getResultType() );

	}
	
	@Test
	public void getContentStringIsNullThrowException()throws Exception{
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn(null);
		Mockito.when(httpRequestInfo02.getContentLength()).thenReturn(0);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);	
		Mockito.when(httpRequestInfo03.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo03.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo04.getContentType()).thenReturn("html");
		Mockito.when(httpRequestInfo04.getContentLength()).thenReturn(3);
		
		Mockito.when(reqhelper.isHtml(any(String.class))).thenReturn(true);

		Mockito.when(reqhelper.getContentString(any(HttpRequestResponseInfo.class), any(Session.class)))
			.thenThrow(new Exception());
		
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo01);
		value.add(httpRequestInfo02);
		value.add(httpRequestInfo03);
		value.add(httpRequestInfo04); 
		
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session01);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		scriptImpl.setHttpRequestResponseHelper(reqhelper);
		AbstractBestPracticeResult testResult = scriptImpl.runTest(tracedata);
		scriptImpl.setHttpRequestResponseHelper(null);
		assertEquals(BPResultType.PASS,testResult.getResultType() );
	}

}
