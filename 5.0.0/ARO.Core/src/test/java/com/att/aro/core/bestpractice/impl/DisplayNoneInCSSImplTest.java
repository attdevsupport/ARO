package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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

public class DisplayNoneInCSSImplTest extends BaseTest{
	

	IHttpRequestResponseHelper reqhelper;
	DisplayNoneInCSSImpl displayNoneInCSSImpl;
	PacketAnalyzerResult tracedata;
	Session session01;
	Session session02;
	Session session03;
	HttpRequestResponseInfo httpRequestInfo01 ;
	HttpRequestResponseInfo httpRequestInfo02 ;
	HttpRequestResponseInfo httpRequestInfo03 ;
	HttpRequestResponseInfo httpRequestInfo04 ;
	HttpRequestResponseInfo httpRequestInfo05 ;
	HttpRequestResponseInfo httpRequestInfo06 ;


	PacketInfo pktInfo01;
	PacketInfo pktInfo02;
	PacketInfo pktInfo03;

	String htmlString = "<html><head><title>First parse</title><style type=\"text/css\">"
			+"header, section, footer, aside, nav, article, figure, figcaption {style= \"display:none\"}"
			+ "<a href=\"\">Visit our HTML tutorial</a>"
			+"<embed width=\"400\" height=\"50\" src=\"bookmark.swf\">"
			+"<object width=\"400\" height=\"50\" data=\"bookmark.swf\"></object>"
			+ "</head>"
			  + "<body><p>Parsed HTML into a doc.</p></body></html>";
	String htmlString2 = "<html><head><title>HTML5 Layout</title>"+
			  "<style type=\"text/css\">"
			  + "header, section, footer, aside, nav, article, figure, figcaption "
			  + "{display: block;}</style><!--[if lt IE 9]>"
			  +"<script src=\"http://html5shiv.googlecode.com/svn/trunk/html5.js\"></script>"
			  +"<![endif]--></head>"
			  ;

	@Before
	public void setup(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		session01 = Mockito.mock(Session.class);
		session02 = Mockito.mock(Session.class);
		session03 = Mockito.mock(Session.class);
		pktInfo01 = Mockito.mock(PacketInfo.class);
		pktInfo02 = Mockito.mock(PacketInfo.class);
		pktInfo03 = Mockito.mock(PacketInfo.class);
		httpRequestInfo01 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo02 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo03 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo04 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo05 = Mockito.mock(HttpRequestResponseInfo.class);
		httpRequestInfo06 = Mockito.mock(HttpRequestResponseInfo.class);
		
		displayNoneInCSSImpl = (DisplayNoneInCSSImpl)context.getBean("displaynoneincss");
		reqhelper = Mockito.mock(IHttpRequestResponseHelper.class);
	}
	
	@Test
	public void runTest_ResultIsFail(){
		Mockito.when(httpRequestInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo01.getContentType()).thenReturn("text/html");
		Mockito.when(httpRequestInfo01.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo01.getAssocReqResp()).thenReturn(httpRequestInfo01);
		Mockito.when(httpRequestInfo01.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		Mockito.when(httpRequestInfo02.getContentType()).thenReturn("");
		Mockito.when(httpRequestInfo02.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo02.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo02.getAssocReqResp()).thenReturn(httpRequestInfo02);
		Mockito.when(httpRequestInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);	
		Mockito.when(httpRequestInfo03.getContentType()).thenReturn("text/html");
		Mockito.when(httpRequestInfo03.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo03.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo03.getAssocReqResp()).thenReturn(httpRequestInfo03);
		Mockito.when(httpRequestInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo04.getContentType()).thenReturn("text/html");
		Mockito.when(httpRequestInfo04.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo04.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo04.getAssocReqResp()).thenReturn(httpRequestInfo04);
		Mockito.when(httpRequestInfo05.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo05.getContentType()).thenReturn("text/css");
		Mockito.when(httpRequestInfo05.getContentLength()).thenReturn(2);
		Mockito.when(httpRequestInfo05.getObjName()).thenReturn(htmlString);
		Mockito.when(httpRequestInfo05.getAssocReqResp()).thenReturn(httpRequestInfo04);
		displayNoneInCSSImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			Mockito.when(reqhelper.getContentString(any(HttpRequestResponseInfo.class),any(Session.class)))
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
		value.add(httpRequestInfo05);
		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session01);
		
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		
		AbstractBestPracticeResult testResult = displayNoneInCSSImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,testResult.getResultType() );
	}
	@Test
	public void runTest_ResultIsPass(){
		Mockito.when(httpRequestInfo06.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(httpRequestInfo06.getContentType()).thenReturn("text/html");
		Mockito.when(httpRequestInfo06.getContentLength()).thenReturn(1);
		Mockito.when(httpRequestInfo06.getAssocReqResp()).thenReturn(httpRequestInfo01);
		Mockito.when(httpRequestInfo06.getObjName()).thenReturn(htmlString);
		displayNoneInCSSImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			Mockito.when(reqhelper.getContentString(any(HttpRequestResponseInfo.class),any(Session.class)))
					.thenReturn(htmlString2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestInfo06);
		Mockito.when(session03.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session03.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session03);
		
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		
		AbstractBestPracticeResult testResult = displayNoneInCSSImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,testResult.getResultType() );

	}
	
}
