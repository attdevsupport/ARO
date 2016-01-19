package com.att.aro.core.bestpractice.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public class FileOrderImplTest extends BaseTest{

	FileOrderImpl fileOrderImpl;
	IHttpRequestResponseHelper reqhelper;
	
	HttpRequestResponseInfo httpRequestResponseInfo01;
	HttpRequestResponseInfo httpRequestResponseInfo02;
	HttpRequestResponseInfo httpRequestResponseInfo03;
	HttpRequestResponseInfo httpRequestResponseInfo04;
	HttpRequestResponseInfo httpRequestResponseInfo05;
	HttpRequestResponseInfo httpRequestResponseInfo06;
	PacketAnalyzerResult tracedata;
	Session session01;
	PacketInfo pktInfo01;
	String htmlString = "<html><head><title>First parse</title><style type=\"text/css\">"
			+"header, section, footer, aside, nav, article, figure, figcaption {style= \"display:none\"}"
			+ "<a href=\"\">Visit our HTML tutorial</a>"
			+"<embed width=\"400\" height=\"50\" src=\"bookmark.swf\">"
			+"<object width=\"400\" height=\"50\" data=\"bookmark.swf\"></object></head>"
			  + "<body><p>Parsed HTML into a doc.</p></body></html>";
	
	String htmlString2 =  "<head><link rel=\"stylesheet\" type=\"text/css\" href=\"mystyle.css\">"

			+ "<script src=\"/files/tutorial/browser/script/rabbits.js\"></script>"
			+"<link rel=\"stylesheet\" type=\"text/css\" href=\"mystyle.css\">"
			+"<link rel=\"stylesheet\" type=\"text/css\" href=\"mystyle.css\">"
			+ " <script src=\"myscripts.js\"></script> <script type=\"text/css\" src=\"script.js\"></script></head>"
			  + "<body><p>Parsed HTML into a doc.</p></body></html>";

	
	
	@Before
	public void setUp(){
		tracedata = mock(PacketAnalyzerResult.class);
		session01 = mock(Session.class);
		reqhelper = mock(IHttpRequestResponseHelper.class);
		httpRequestResponseInfo01 = mock(HttpRequestResponseInfo.class);
		httpRequestResponseInfo02 = mock(HttpRequestResponseInfo.class);
		httpRequestResponseInfo03 = mock(HttpRequestResponseInfo.class);
		httpRequestResponseInfo04 = mock(HttpRequestResponseInfo.class);
		httpRequestResponseInfo05 = mock(HttpRequestResponseInfo.class);
		httpRequestResponseInfo06 = mock(HttpRequestResponseInfo.class);

		fileOrderImpl = (FileOrderImpl)context.getBean("fileorder");
		
	}
	

	@Test
	public void runTest_resultIsPass(){
		fileOrderImpl.setHttpRequestResponseHelper(reqhelper);
		when(httpRequestResponseInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		when(httpRequestResponseInfo01.getContentType()).thenReturn("text/css");
		when(httpRequestResponseInfo01.getContentLength()).thenReturn(1);
		
		when(httpRequestResponseInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		when(httpRequestResponseInfo02.getContentType()).thenReturn("script");
		when(httpRequestResponseInfo02.getContentLength()).thenReturn(2);
		
		try {
			when(reqhelper.isHtml(any(String.class))).thenReturn(true);
			when(reqhelper.getContentString(any(HttpRequestResponseInfo.class),any(Session.class)))
				.thenReturn(htmlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestResponseInfo01);
		value.add(httpRequestResponseInfo02);

		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session01);
		
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		
		AbstractBestPracticeResult testResult = fileOrderImpl.runTest(tracedata);
		assertEquals(BPResultType.PASS,testResult.getResultType() );

	}
	
	
	@Test
	public void runTest_resultIsFail(){
		fileOrderImpl.setHttpRequestResponseHelper(reqhelper);
		when(httpRequestResponseInfo01.getDirection()).thenReturn(HttpDirection.RESPONSE);
		when(httpRequestResponseInfo01.getContentType()).thenReturn("text/css");
		when(httpRequestResponseInfo01.getContentLength()).thenReturn(1);
		when(httpRequestResponseInfo01.getObjName()).thenReturn(htmlString2);
		when(httpRequestResponseInfo02.getDirection()).thenReturn(HttpDirection.REQUEST);
		when(httpRequestResponseInfo02.getContentType()).thenReturn("script");
		when(httpRequestResponseInfo02.getContentLength()).thenReturn(2);
		when(httpRequestResponseInfo02.getObjName()).thenReturn(htmlString2);
		when(httpRequestResponseInfo03.getDirection()).thenReturn(HttpDirection.RESPONSE);	
		when(httpRequestResponseInfo03.getContentType()).thenReturn("text/css");
		when(httpRequestResponseInfo03.getContentLength()).thenReturn(1);
		when(httpRequestResponseInfo03.getObjName()).thenReturn(htmlString2);
		when(httpRequestResponseInfo04.getDirection()).thenReturn(HttpDirection.RESPONSE);
		when(httpRequestResponseInfo04.getContentType()).thenReturn("text/css");
		when(httpRequestResponseInfo04.getContentLength()).thenReturn(2);
		when(httpRequestResponseInfo04.getObjName()).thenReturn(htmlString);
		when(httpRequestResponseInfo05.getDirection()).thenReturn(HttpDirection.RESPONSE);
		when(httpRequestResponseInfo05.getContentType()).thenReturn("text/css");
		when(httpRequestResponseInfo05.getContentLength()).thenReturn(2);
		when(httpRequestResponseInfo05.getObjName()).thenReturn(htmlString);
		when(httpRequestResponseInfo06.getDirection()).thenReturn(HttpDirection.RESPONSE);
		when(httpRequestResponseInfo06.getContentType()).thenReturn("script");
		when(httpRequestResponseInfo06.getContentLength()).thenReturn(4);
		when(httpRequestResponseInfo06.getObjName()).thenReturn(htmlString2);
		try {
			when(reqhelper.isHtml(any(String.class))).thenReturn(true);
			when(reqhelper.getContentString(any(HttpRequestResponseInfo.class),any(Session.class)))
				.thenReturn(htmlString2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		value.add(httpRequestResponseInfo01);
		value.add(httpRequestResponseInfo02);
		value.add(httpRequestResponseInfo03);
		value.add(httpRequestResponseInfo04);
		value.add(httpRequestResponseInfo05);


		Mockito.when(session01.getRequestResponseInfo()).thenReturn(value);
		List<Session> sessionList = new ArrayList<Session>();

		Mockito.when(session01.getDomainName()).thenReturn("www.google.com");
		sessionList.add(session01);
		
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionList);
		
		AbstractBestPracticeResult testResult = fileOrderImpl.runTest(tracedata);
		assertEquals(BPResultType.FAIL,testResult.getResultType() );


	}
	
}
