package com.att.aro.core.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.junit.Assert.*;

import com.att.aro.core.BaseTest;
import com.att.aro.core.IAROService;
import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.bestpractice.pojo.PeriodicTransferResult;
import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.core.packetanalysis.ICacheAnalysis;
import com.att.aro.core.packetanalysis.IPacketAnalyzer;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceFileResult;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.pojo.VersionInfo;
import com.att.aro.core.util.Util;

public class AROServiceImplTest extends BaseTest {

	@InjectMocks
	AROServiceImpl aro;
	@Mock
	IPacketAnalyzer packetanalyzer;	
	@Mock
	ICacheAnalysis cacheAnalyzer;
	@Mock
	IBestPractice worker;
	@Mock
	transient VersionInfo info;

	@Mock(name = "periodicTransfer")
	IBestPractice periodicTransfer;
	@Mock(name = "unnecessaryConnection")
	IBestPractice unnecessaryConnection;
	@Mock(name = "connectionOpening")
	IBestPractice connectionOpening;
	@Mock(name = "connectionClosing")
	IBestPractice connectionClosing;
	@Mock(name = "wifiOffloading")
	IBestPractice wifiOffloading;
	@Mock(name = "screenRotation")
	IBestPractice screenRotation;
	@Mock(name = "prefetching")
	IBestPractice prefetching;
	@Mock(name = "accessingPeripheral")
	IBestPractice accessingPeripheral;
	@Mock(name = "combineCsJss")
	IBestPractice combineCsJss;
	@Mock(name = "http10Usage")
	IBestPractice http10Usage;
	@Mock(name = "cacheControl")
	IBestPractice cacheControl;
	@Mock(name = "usingCache")
	IBestPractice usingCache;
	@Mock(name = "duplicateContent")
	IBestPractice duplicateContent;
	@Mock(name = "http4xx5xx")
	IBestPractice http4xx5xx;
	@Mock(name = "http3xx")
	IBestPractice http3xx;
	@Mock(name = "textFileCompression")
	IBestPractice textFileCompression;
	@Mock(name = "imageSize")
	IBestPractice imageSize;
	@Mock(name = "minify")
	IBestPractice minify;
	@Mock(name = "emptyUrl")
	IBestPractice emptyUrl;
	@Mock(name = "flash")
	IBestPractice flash;
	@Mock(name = "spriteImage")
	IBestPractice spriteImage;
	@Mock(name = "scripts")
	IBestPractice scripts;
	@Mock(name = "async")
	IBestPractice async;
	@Mock(name = "displaynoneincss")
	IBestPractice displaynoneincss;
	@Mock(name = "fileorder")
	IBestPractice fileorder;

	@Before
	public void setup(){
		aro = new AROServiceImpl();
		MockitoAnnotations.initMocks(this);		
	}
	
	@After
	public void reset(){
		Mockito.reset(packetanalyzer);
		Mockito.reset(cacheAnalyzer);
		Mockito.reset(worker);
	}
	@Test
	public void getNameTest(){
		when(info.getName()).thenReturn("ARO");
		String name = aro.getName();
		assertNotNull(name);
	}
	
	@Test
	public void getVersionTest(){
		when(info.getVersion()).thenReturn("5.0");
		String version = aro.getVersion();
		assertNotNull(version);
	}
	  
	@Test
	public void analyzeFileTest() throws IOException{
		PacketAnalyzerResult analyze = new PacketAnalyzerResult();
		TraceFileResult traceresult = new TraceFileResult();
		List<PacketInfo> allpackets = new ArrayList<PacketInfo>();
		allpackets.add(new PacketInfo(new Packet(0, 0, 0, 0, null)));
		traceresult.setAllpackets(allpackets);
		analyze.setTraceresult(traceresult);
		PeriodicTransferResult periodicTransferResult = new PeriodicTransferResult();
		List<BestPracticeType> req = new ArrayList<BestPracticeType>();		
		req.add(BestPracticeType.UNNECESSARY_CONNECTIONS);
		req.add(BestPracticeType.CONNECTION_CLOSING);
		req.add(BestPracticeType.CONNECTION_OPENING);
		req.add(BestPracticeType.PERIODIC_TRANSFER);
		req.add(BestPracticeType.SCREEN_ROTATION);
		req.add(BestPracticeType.ACCESSING_PERIPHERALS);
		req.add(BestPracticeType.COMBINE_CS_JSS);
		req.add(BestPracticeType.HTTP_1_0_USAGE);
		req.add(BestPracticeType.CACHE_CONTROL);
		req.add(BestPracticeType.USING_CACHE);
		req.add(BestPracticeType.DUPLICATE_CONTENT);
		req.add(BestPracticeType.HTTP_4XX_5XX);
		req.add(BestPracticeType.HTTP_3XX_CODE);
		req.add(BestPracticeType.FILE_COMPRESSION);
		req.add(BestPracticeType.IMAGE_SIZE);
		req.add(BestPracticeType.MINIFICATION);
		req.add(BestPracticeType.EMPTY_URL);
		req.add(BestPracticeType.FLASH);
		req.add(BestPracticeType.SPRITEIMAGE);
		req.add(BestPracticeType.SCRIPTS_URL);
		req.add(BestPracticeType.ASYNC_CHECK);
		req.add(BestPracticeType.DISPLAY_NONE_IN_CSS);
		req.add(BestPracticeType.FILE_ORDER);
		packetanalyzer = Mockito.mock(IPacketAnalyzer.class);

		aro.setPacketAnalyzer(packetanalyzer);
	
		when(packetanalyzer.analyzeTraceFile(any(String.class), any(Profile.class), any(AnalysisFilter.class)))
		.thenReturn(analyze);
		when(worker.runTest(any(PacketAnalyzerResult.class))).thenReturn(periodicTransferResult);
		AROTraceData testResult 
		= aro.analyzeFile(req, "traffic.cap");
		
		assertEquals(23,testResult.getBestPracticeResults().size());
	}
	 
	@Test
	public void analyzeFileTest_resultIsNull()throws IOException{
		when(packetanalyzer.analyzeTraceFile(any(String.class), any(Profile.class), any(AnalysisFilter.class)))
		.thenReturn(null);
		List<BestPracticeType> req = new ArrayList<BestPracticeType>();		

		AROTraceData testResult	= aro.analyzeFile(req, "traffic.cap");		
		assertEquals(104,testResult.getError().getCode());
		assertFalse(testResult.isSuccess());

	}
	
	@Test
	public void analyzeDirectoryTest()throws IOException{
		TraceDirectoryResult traceresult = new TraceDirectoryResult();
		List<PacketInfo> allpackets = new ArrayList<PacketInfo>();
		allpackets.add(new PacketInfo(new Packet(0, 0, 0, 0, null)));
		int tempsize = allpackets.size();
		traceresult.setAllpackets(allpackets);
		PacketAnalyzerResult analyze = new PacketAnalyzerResult();
		analyze.setTraceresult(traceresult);
		CacheAnalysis cacheAnalysis = new CacheAnalysis();
		
		PeriodicTransferResult periodicTransferResult = new PeriodicTransferResult();
		List<BestPracticeType> req = new ArrayList<BestPracticeType>();
		req.add(BestPracticeType.UNNECESSARY_CONNECTIONS);
		req.add(BestPracticeType.CONNECTION_CLOSING);
		req.add(BestPracticeType.CONNECTION_OPENING);
		req.add(BestPracticeType.PERIODIC_TRANSFER);
		req.add(BestPracticeType.SCREEN_ROTATION);
		req.add(BestPracticeType.ACCESSING_PERIPHERALS);
		req.add(BestPracticeType.COMBINE_CS_JSS);
		req.add(BestPracticeType.HTTP_1_0_USAGE);
		req.add(BestPracticeType.CACHE_CONTROL);
		req.add(BestPracticeType.USING_CACHE);
		req.add(BestPracticeType.DUPLICATE_CONTENT);
		req.add(BestPracticeType.HTTP_4XX_5XX);
		req.add(BestPracticeType.HTTP_3XX_CODE);
		req.add(BestPracticeType.FILE_COMPRESSION);
		req.add(BestPracticeType.IMAGE_SIZE);
		req.add(BestPracticeType.MINIFICATION);
		req.add(BestPracticeType.EMPTY_URL);
		req.add(BestPracticeType.FLASH); 
		req.add(BestPracticeType.SPRITEIMAGE);
		req.add(BestPracticeType.SCRIPTS_URL);
		req.add(BestPracticeType.ASYNC_CHECK);
		req.add(BestPracticeType.DISPLAY_NONE_IN_CSS);
		req.add(BestPracticeType.FILE_ORDER);
//		aro.setPacketAnalyzer(packetanalyzer);
		when(packetanalyzer.analyzeTraceDirectory(any(String.class), any(Profile.class), any(AnalysisFilter.class)))
		.thenReturn(analyze);
		when(worker.runTest(any(PacketAnalyzerResult.class))).thenReturn(periodicTransferResult);

		when(cacheAnalyzer.analyze(anyListOf(Session.class))).thenReturn(cacheAnalysis);
		
		AROTraceData testResult 
		= aro.analyzeDirectory(req,  Util.getCurrentRunningDir());		
		assertEquals(23,testResult.getBestPracticeResults().size());
	}
	
	@Test
	public void analyzeDirectoryTest_resultIsNull()throws IOException{
		List<BestPracticeType> req = new ArrayList<BestPracticeType>();
		when(packetanalyzer.analyzeTraceDirectory(any(String.class), any(Profile.class), any(AnalysisFilter.class)))
		.thenReturn(null);
		AROTraceData testResult 
		= aro.analyzeDirectory(req,  Util.getCurrentRunningDir());		
		assertEquals(103,testResult.getError().getCode());
		assertFalse(testResult.isSuccess());

	}
}
