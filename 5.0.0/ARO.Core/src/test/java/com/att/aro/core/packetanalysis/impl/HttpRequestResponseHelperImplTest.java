package com.att.aro.core.packetanalysis.impl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetreader.pojo.PacketDirection;

public class HttpRequestResponseHelperImplTest extends BaseTest {
	
	@Spy
	IHttpRequestResponseHelper httpRequestResponseHelper;

	@Before
	public void setup() {
		httpRequestResponseHelper = context.getBean(IHttpRequestResponseHelper.class);
	}
 
	byte[] storage = {12,12,16};
	@Test
	public void contentTypeTests(){
		assertTrue(httpRequestResponseHelper.isCss("text/css"));
		assertTrue(!httpRequestResponseHelper.isCss("text/html"));
		
		assertTrue(httpRequestResponseHelper.isHtml("text/html"));
		assertTrue(!httpRequestResponseHelper.isHtml("text/css"));
		
		assertTrue(httpRequestResponseHelper.isJSON("application/json"));
		assertTrue(!httpRequestResponseHelper.isJSON("text/css"));
		
		assertTrue(httpRequestResponseHelper.isJavaScript("application/ecmascript"));
		assertTrue(httpRequestResponseHelper.isJavaScript("application/javascript"));
		assertTrue(httpRequestResponseHelper.isJavaScript("text/javascript"));
	}

	@Test
	public void getContent(){
		Session session = null;
		session = mock(Session.class);
		
		HttpRequestResponseInfo req = null;
		req = mock(HttpRequestResponseInfo.class);

		String stringData = "this was compressed";
		byte[] data = stringData.getBytes();	
		byte[] gzipped_data = null;
		
	    try {
			ByteArrayOutputStream byteOutput=new ByteArrayOutputStream(data.length / 2);
			GZIPOutputStream gzipOutput=new GZIPOutputStream(byteOutput);
			gzipOutput.write(data);
			gzipOutput.close();
			gzipped_data = byteOutput.toByteArray();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		SortedMap<Integer, Integer> contentOffsetTreeMap = new TreeMap<Integer,Integer>();
		contentOffsetTreeMap.put(0, gzipped_data.length);

		Mockito.when(req.getContentEncoding()).thenReturn("gzip");
		Mockito.when(req.getContentOffsetLength()).thenReturn(contentOffsetTreeMap);
		Mockito.when(req.getPacketDirection()).thenReturn(PacketDirection.DOWNLINK);
		Mockito.when(session.getStorageDl()).thenReturn(gzipped_data);
		Mockito.when(session.getStorageUl()).thenReturn(gzipped_data);
		
		try {
			assertEquals(stringData, httpRequestResponseHelper.getContentString(req, session));

			Mockito.when(session.getStorageDl()).thenReturn(data);
			String thisWillFail = httpRequestResponseHelper.getContentString(req, session);
			
		} catch (Exception e) {
			assertEquals("The content may be corrupted.", e.getMessage());
		}
		
		// bad gzip data
		gzipped_data[20]= 42;
		Mockito.when(session.getStorageDl()).thenReturn(gzipped_data);
		Mockito.when(session.getStorageUl()).thenReturn(gzipped_data);

		try {
			String thisWasFromCorruptedZip = httpRequestResponseHelper.getContentString(req, session);
			assertEquals("this was bwmpressed", thisWasFromCorruptedZip);
		} catch (Exception e) {
		}
	}

	@Test
	public void isSameContent_resultIsTrue(){
		HttpRequestResponseInfo reqLeft = new HttpRequestResponseInfo();
		HttpRequestResponseInfo reqRight = new HttpRequestResponseInfo();
		SortedMap<Integer,Integer> testMap = new TreeMap<Integer,Integer>();
		testMap.put(1, 1);
		testMap.put(2, 2);
		reqLeft.setContentLength(1);
		reqLeft.setContentOffsetLength(testMap);
		reqLeft.setPacketDirection(PacketDirection.DOWNLINK);
		reqRight.setContentLength(1);
		reqRight.setContentOffsetLength(testMap);
		reqRight.setPacketDirection(PacketDirection.DOWNLINK);

		Session sessionRight = new Session(null, 0, 0);
		sessionRight.setStorageDl(storage);
		Session sessionLeft = new Session(null, 0, 0);
		sessionLeft.setStorageDl(storage);
		
		boolean testResult = httpRequestResponseHelper.isSameContent(reqLeft, reqRight, sessionLeft, sessionRight);
		assertTrue(testResult);
	}
	
	@Test
	public void isSameContent_resultIsFalse(){
		HttpRequestResponseInfo reqLeft = new HttpRequestResponseInfo();
		HttpRequestResponseInfo reqRight = new HttpRequestResponseInfo();
		SortedMap<Integer,Integer> testMap = new TreeMap<Integer,Integer>();
		testMap.put(1, 1);
		testMap.put(2, 2);
		reqLeft.setContentLength(1);
		reqLeft.setContentOffsetLength(testMap);
		reqLeft.setPacketDirection(PacketDirection.DOWNLINK);
		reqRight.setContentLength(1);
		reqRight.setContentOffsetLength(testMap);
		reqRight.setPacketDirection(PacketDirection.UPLINK);

		Session sessionRight = new Session(null, 0, 0);
		sessionRight.setStorageDl(storage);
		Session sessionLeft = new Session(null, 0, 0);
		sessionLeft.setStorageDl(storage);
		
		httpRequestResponseHelper.isSameContent(reqLeft, reqRight, sessionLeft, sessionRight);
		boolean testResult = httpRequestResponseHelper.isSameContent(reqLeft, reqRight, sessionLeft, sessionRight);
		assertFalse(testResult);

	}
	

	
	@Ignore
	@Test
	public void isSameContent(){
		String left = "<h1>The droids were just here 12 seconds ago.</h1>\n\n\n<h1>lastAccess   = Thu Feb 20 10:45:01 PST 2014</h1>\n\n\n<h3>---------------------------------------</h3>\n<h3>lastEventSent  = 12</h3>\n<h3>lastRemoteAddr = 166.137.185.60</h3>\n<h3>lastRemoteHost = 166.137.185.60</h3>\n<h3>lastRemoteUser = null</h3>";
		String right = "<h1>The droids were just here 13 seconds ago.</h1>\n\n\n<h1>lastAccess   = Thu Feb 20 10:45:00 PST 2014</h1>\n\n\n<h3>---------------------------------------</h3>\n<h3>lastEventSent  = 12</h3>\n<h3>lastRemoteAddr = 166.137.185.60</h3>\n<h3>lastRemoteHost = 166.137.185.60</h3>\n<h3>lastRemoteUser = null</h3>";

		Session session =  mock(Session.class);

		HttpRequestResponseInfo reqLeft = mock(HttpRequestResponseInfo.class);
		HttpRequestResponseInfo reqRight = mock(HttpRequestResponseInfo.class);
		
		long lv = (long)left.length();
		SortedMap<Integer, Integer> contentOffsetTreeMap = new TreeMap<Integer,Integer>();
		contentOffsetTreeMap.put(1, 2);

		byte[] dataLeft = left.getBytes();	
		byte[] dataRight = right.getBytes();	
		
		Mockito.when(reqLeft.getContentOffsetLength()).thenReturn(contentOffsetTreeMap);
		Mockito.when(reqLeft.getPacketDirection()).thenReturn(PacketDirection.DOWNLINK);

		Mockito.when(reqRight.getContentOffsetLength()).thenReturn(contentOffsetTreeMap);
		Mockito.when(reqRight.getPacketDirection()).thenReturn(PacketDirection.UPLINK);

		Mockito.when(session.getStorageDl()).thenReturn(dataLeft);
		Mockito.when(session.getStorageUl()).thenReturn(dataRight);
		
		//assertEquals(false, httpRequestResponseHelper.isSameContent(reqLeft, reqRight, session));

		Mockito.when(session.getStorageUl()).thenReturn(dataLeft);
		//assertEquals(true, httpRequestResponseHelper.isSameContent(reqLeft, reqRight, session));	
		
	}
}
