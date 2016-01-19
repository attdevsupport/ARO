package com.att.aro.core.packetanalysis.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.ICacheAnalysis;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.util.Util;

public class CacheAnalysisImplTest extends BaseTest{
	
	@InjectMocks
	ICacheAnalysis cacheAnalysis;
	@Mock
	IHttpRequestResponseHelper rrhelper;

	Session session01;
	Session session02;
	Date date = new Date();

	HttpRequestResponseInfo[] httpRequestInfoArray = new HttpRequestResponseInfo[60];

	PacketInfo pktInfo01;
	PacketInfo pktInfo02;

	@Before
	public void setup(){
		cacheAnalysis = (CacheAnalysisImpl)context.getBean(ICacheAnalysis.class);
		session01 = Mockito.mock(Session.class);
		session02 = Mockito.mock(Session.class);	
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void analyze_(){

		List<PacketInfo> packets = new ArrayList<PacketInfo>();
		pktInfo01 = Mockito.mock(PacketInfo.class);
		pktInfo02 = Mockito.mock(PacketInfo.class);
		
		for(int i =0;i<60;i++){
			httpRequestInfoArray[i] = mock(HttpRequestResponseInfo.class);
		}
		
		for(int i =0;i<20;i++){
			
			when(httpRequestInfoArray[i].isNoStore()).thenReturn(true);
			if(i%2==1){
				when(httpRequestInfoArray[i].getDirection()).thenReturn(HttpDirection.REQUEST);
				when(httpRequestInfoArray[i].getHostName()).thenReturn("www.google.com");
				when(httpRequestInfoArray[i].getObjName()).thenReturn(Util.getCurrentRunningDir());
				
			}else{
				when(httpRequestInfoArray[i].getDirection()).thenReturn(HttpDirection.RESPONSE);
				when(httpRequestInfoArray[18].getStatusCode()).thenReturn(304);
			}
		} 

		
		for(int i = 20;i<60;i++){
			when(httpRequestInfoArray[i].isNoStore()).thenReturn(false);

			when(httpRequestInfoArray[i].getHostName()).thenReturn("www.google.com"+i);
			when(httpRequestInfoArray[i].getObjName()).thenReturn(Util.getCurrentRunningDir());
			when(httpRequestInfoArray[i-2].isRangeResponse()).thenReturn(true);
			if((i-20)%2==1){				
				when(httpRequestInfoArray[i].getDirection()).thenReturn(HttpDirection.REQUEST);
			}else{
				when(httpRequestInfoArray[i].getDirection()).thenReturn(HttpDirection.RESPONSE);
				when(httpRequestInfoArray[i].getStatusCode()).thenReturn(206);
			}
		}

		
		when(httpRequestInfoArray[2].getAssocReqResp()).thenReturn(httpRequestInfoArray[1]);
		when(httpRequestInfoArray[4].getAssocReqResp()).thenReturn(httpRequestInfoArray[3]);
		when(httpRequestInfoArray[6].getAssocReqResp()).thenReturn(httpRequestInfoArray[5]);
		when(httpRequestInfoArray[8].getAssocReqResp()).thenReturn(httpRequestInfoArray[7]);
		when(httpRequestInfoArray[10].getAssocReqResp()).thenReturn(httpRequestInfoArray[9]);
		when(httpRequestInfoArray[12].getAssocReqResp()).thenReturn(httpRequestInfoArray[11]);
		when(httpRequestInfoArray[14].getAssocReqResp()).thenReturn(httpRequestInfoArray[13]);
		
		for(int i=18;i<60;i++){
			when(httpRequestInfoArray[i].getAssocReqResp()).thenReturn(httpRequestInfoArray[i-1]);
			
		}

		for(int i = 50;i<60;i++){
			when(httpRequestInfoArray[i].isRangeResponse()).thenReturn(false);
			when(httpRequestInfoArray[i].isChunked()).thenReturn(true);
		}
		
		for(int i = 0;i<50;i++){
			when(httpRequestInfoArray[i].isRangeResponse()).thenReturn(true);
		}
		 
		when(httpRequestInfoArray[7].getHostName()).thenReturn(null);
		when(httpRequestInfoArray[7].getObjName()).thenReturn(null);
		
		
		when(httpRequestInfoArray[0].getStatusCode()).thenReturn(400);
		when(httpRequestInfoArray[2].getStatusCode()).thenReturn(200);
		when(httpRequestInfoArray[4].getStatusCode()).thenReturn(200);
		when(httpRequestInfoArray[6].getStatusCode()).thenReturn(200);
		when(httpRequestInfoArray[8].getStatusCode()).thenReturn(206);
		when(httpRequestInfoArray[10].getStatusCode()).thenReturn(206);
		when(httpRequestInfoArray[12].getStatusCode()).thenReturn(206);
		when(httpRequestInfoArray[14].getStatusCode()).thenReturn(304);
		when(httpRequestInfoArray[16].getStatusCode()).thenReturn(304);
		when(httpRequestInfoArray[18].getStatusCode()).thenReturn(304);

		when(httpRequestInfoArray[1].getRequestType()).thenReturn(HttpRequestResponseInfo.UTF8);
		when(httpRequestInfoArray[3].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_POST);
		when(httpRequestInfoArray[5].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_PUT);
		when(httpRequestInfoArray[7].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_GET);
		when(httpRequestInfoArray[9].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_POST);
		when(httpRequestInfoArray[11].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_GET);
		when(httpRequestInfoArray[13].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_GET);
		when(httpRequestInfoArray[15].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_POST);
		when(httpRequestInfoArray[17].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_PUT);
		when(httpRequestInfoArray[19].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_PUT);
		when(httpRequestInfoArray[20].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_GET);
		when(httpRequestInfoArray[21].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_GET);
		for(int i=22; i<60;i++){
			if(i%2==1)
			when(httpRequestInfoArray[i].getRequestType()).thenReturn(HttpRequestResponseInfo.HTTP_GET);
		}
		Date date01 = new Date(1421384400000L); // Friday, January 16, 2015 5:00:00 AM GMT
		Date date02 = new Date(1421384400000L-76400000L);
		Date date03 = new Date(1421384400000L-86400000L);
		Date date04 = new Date(1421383400000L-176400000L);
		
		for(int i = 0;i<60;i++){

			when(httpRequestInfoArray[i].getMaxAge()).thenReturn(86400000L);

		}
		for(int i = 0;i<60;i++){
			if(i%2==1){
				when(httpRequestInfoArray[i].getExpires()).thenReturn(date03);
				when(httpRequestInfoArray[i].getDate()).thenReturn(date02);
				when(httpRequestInfoArray[i].getAbsTimeStamp()).thenReturn(date02);
				when(httpRequestInfoArray[i].getLastModified()).thenReturn(date);
			}else{
				 
				if(i<40){
					when(httpRequestInfoArray[i].getLastModified()).thenReturn(date01);
					when(httpRequestInfoArray[i].getRangeFirst()).thenReturn(0);
					when(httpRequestInfoArray[i].getRangeLast()).thenReturn(5);
					when(httpRequestInfoArray[i].getAbsTimeStamp()).thenReturn(date02);
					when(httpRequestInfoArray[i].getExpires()).thenReturn(date);
					when(httpRequestInfoArray[i].getDate()).thenReturn(date03);	

				}else{
					when(httpRequestInfoArray[i].getLastModified()).thenReturn(date02);
					when(httpRequestInfoArray[i].getExpires()).thenReturn(date04);
					when(httpRequestInfoArray[i].getDate()).thenReturn(date);
					when(httpRequestInfoArray[i].getAbsTimeStamp()).thenReturn(date);
					when(httpRequestInfoArray[i].getRangeFirst()).thenReturn(0);
					when(httpRequestInfoArray[i].getRangeLast()).thenReturn(13);


				}
			}
		}
		when(httpRequestInfoArray[30].isRangeResponse()).thenReturn(false);
		when(httpRequestInfoArray[31].isRangeResponse()).thenReturn(false);

		when(rrhelper.getActualByteCount(any(HttpRequestResponseInfo.class), any(Session.class))).thenReturn(2L);
		
		List<HttpRequestResponseInfo> value = new ArrayList<HttpRequestResponseInfo>();
		for(int i =0;i<60;i++){
			value.add(httpRequestInfoArray[i]);
		}

		packets.add(pktInfo01);
		packets.add(pktInfo02);
		
		when(session01.getRequestResponseInfo()).thenReturn(value);	
		when(session01.isUDP()).thenReturn(false);
		when(session01.getPackets()).thenReturn(packets);
		
		when(session02.isUDP()).thenReturn(false);
		when(session02.getRequestResponseInfo()).thenReturn(value); 
		when(session02.getPackets()).thenReturn(packets);
		
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.add(session01);
		sessionList.add(session02);
		
		CacheAnalysis testResult = cacheAnalysis.analyze(sessionList);
		assertEquals(4,testResult.getCacheExpirationResponses().size());		
		assertEquals(60,testResult.getDiagnosisResults().size());		
		assertEquals(0,testResult.getDuplicateContentBytes());		
		assertEquals(0,testResult.getDuplicateContentBytesRatio(),0.0);		
		assertEquals(18,testResult.getDuplicateContentWithOriginals().size());		
	

	}

}
