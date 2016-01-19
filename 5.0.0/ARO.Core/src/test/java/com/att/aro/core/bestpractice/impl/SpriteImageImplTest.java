package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
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

public class SpriteImageImplTest extends BaseTest{

	PacketAnalyzerResult tracedata;
	Session session01;
	Session session02;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;
	HttpRequestResponseInfo httpRequestInfo03 ;
	HttpRequestResponseInfo httpRequestInfo04 ;

	PacketInfo pktInfo01;
	PacketInfo pktInfo02;
	SpriteImageImpl spriteImageImpl;
	IHttpRequestResponseHelper reqhelper;
	String htmlString = "<html><head><title>First parse</title><script src=\"myscripts.js\"></script>"
			+ "<a href=\"\">Visit our HTML tutorial</a>"
			+ "</head>"
			  + "<body><p>Parsed HTML into a doc.</p></body></html>";
	
	String htmlString2 = "<html><head><title>First parse</title><script src=></script>"
			+ "<a href= >Visit our HTML tutorial</a>"
			+ "</head>"
			  + "<body><p>Parsed HTML into a doc.</p></body></html>";


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
		spriteImageImpl = (SpriteImageImpl)context.getBean("spriteImage");
	}
	
	@Test
	public void runTest_resIsPass(){
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("abc");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(6145);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);		
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);

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
		AbstractBestPracticeResult testResult = spriteImageImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,testResult.getResultType() );
	}	
	
	@Test 
	public void runTest_resIsFail(){
		Date date = new Date();
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getFirstDataPacket()).thenReturn(pktInfo01);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("image/");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn(null);
		Mockito.when(httpRequestInfo02.getContentLength()).thenReturn(0);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo03.getFirstDataPacket()).thenReturn(pktInfo02);
		Mockito.when(httpRequestInfo03.getContentType()).thenReturn("image/");
		Mockito.when(httpRequestInfo03.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo04.getFirstDataPacket()).thenReturn(pktInfo01);
		Mockito.when(httpRequestInfo04.getContentType()).thenReturn("image/");
		Mockito.when(httpRequestInfo04.getContentLength()).thenReturn(3);

		Mockito.when(pktInfo01.getTimeStamp()).thenReturn((date.getTime())/1000+0.0);
		Mockito.when(pktInfo02.getTimeStamp()).thenReturn((date.getTime()/1000)+1.0);
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

		AbstractBestPracticeResult testResult = spriteImageImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,testResult.getResultType() );

	} 

}
