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

public class FlashImplTest extends BaseTest{

	PacketAnalyzerResult tracedata;
	Session session01;
	Session session02;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;
	HttpRequestResponseInfo httpRequestInfo03 ;
	HttpRequestResponseInfo httpRequestInfo04 ;
	HttpRequestResponseInfo httpRequestInfo05 ;


	PacketInfo pktInfo01;
	PacketInfo pktInfo02;
	FlashImpl flashImpl;
	IHttpRequestResponseHelper reqhelper;
	String htmlString = "<html><head><title>First parse</title><script src=\"myscripts.js\"></script>"
			+ "<a href=\"\">Visit our HTML tutorial</a>"
			+"<embed width=\"400\" height=\"50\" src=\"bookmark.swf\">"
			+"<object width=\"400\" height=\"50\" data=\"bookmark.swf\"></object>"
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
		httpRequestInfo05 = Mockito.mock(HttpRequestResponseInfo.class);

		reqhelper = Mockito.mock(IHttpRequestResponseHelper.class);
		flashImpl = (FlashImpl)context.getBean("flash");
	}
	

	@Test
	public void runTest_resIsFail(){
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("application/x-shockwave-flash");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo01.getAssocReqResp()).thenReturn(httpRequestInfo01);
		Mockito.when(httpRequestInfo01.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn("");
		Mockito.when(httpRequestInfo02.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo02.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo02.getAssocReqResp()).thenReturn(httpRequestInfo02);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);	
		Mockito.when(httpRequestInfo03.getContentType()).thenReturn("video/x-flv");
		Mockito.when(httpRequestInfo03.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo03.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo03.getAssocReqResp()).thenReturn(httpRequestInfo03);
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo04.getContentType()).thenReturn("text/css");
		Mockito.when(httpRequestInfo04.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo04.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo04.getAssocReqResp()).thenReturn(httpRequestInfo04);
		Mockito.when(httpRequestInfo05.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo05.getContentType()).thenReturn("text/css");
		Mockito.when(httpRequestInfo05.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo05.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo05.getAssocReqResp()).thenReturn(httpRequestInfo04);
		Mockito.when(reqhelper.isHtml(any(String.class))).thenReturn(true);
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
		
		try {
			Mockito.when(reqhelper.getContentString(any(HttpRequestResponseInfo.class), any(Session.class)))
			.thenReturn(htmlString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		flashImpl.setHttpRequestResponseHelper(reqhelper);
		
		AbstractBestPracticeResult testResult = flashImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,testResult.getResultType() );
	}
	
	@Test
	public void runTest_resIsPass(){
		
			Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.REQUEST);
			Mockito.when(httpRequestInfo01.getContentType()).thenReturn(null);
			Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
			Mockito.when(httpRequestInfo01.getAssocReqResp()).thenReturn(httpRequestInfo01);
			Mockito.when(httpRequestInfo01.getObjName()).thenReturn(htmlString);
			Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
			Mockito.when(httpRequestInfo02.getContentType()).thenReturn("");
			Mockito.when(httpRequestInfo02.getContentLength()).thenReturn(2);
			Mockito.when(httpRequestInfo02.getObjName()).thenReturn(htmlString);
			Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.REQUEST);	
			Mockito.when(httpRequestInfo03.getContentType()).thenReturn("video/x-flv");
			Mockito.when(httpRequestInfo03.getContentLength()).thenReturn(1);
			Mockito.when(httpRequestInfo03.getObjName()).thenReturn(htmlString);
			Mockito.when(httpRequestInfo03.getAssocReqResp()).thenReturn(httpRequestInfo03);
			Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.REQUEST);
			Mockito.when(httpRequestInfo04.getContentType()).thenReturn("text/css");
			Mockito.when(httpRequestInfo04.getContentLength()).thenReturn(2);
			Mockito.when(httpRequestInfo04.getObjName()).thenReturn(htmlString);
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
			
			try {
				Mockito.when(reqhelper.getContentString(any(HttpRequestResponseInfo.class), any(Session.class)))
				.thenReturn(htmlString2);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			flashImpl.setHttpRequestResponseHelper(reqhelper);
			
			AbstractBestPracticeResult testResult = flashImpl.runTest(tracedata);
			assertEquals(BPResultType.PASS,testResult.getResultType() );
	}

}
