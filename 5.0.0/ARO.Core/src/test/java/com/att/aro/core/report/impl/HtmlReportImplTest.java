package com.att.aro.core.report.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.AccessingPeripheralResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.CacheControlResult;
import com.att.aro.core.bestpractice.pojo.ConnectionOpeningResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.report.IReport;
import com.att.aro.core.util.Util;

public class HtmlReportImplTest extends BaseTest{

	@InjectMocks
	IReport htmlReportImpl;
	
	@Mock
	IFileManager filereader;
//	static TemporaryFolder _tempFolder2; 
	@Rule
	 public TemporaryFolder folder= new TemporaryFolder();
 
    @Before 
    public void setUp()
    {	
    	htmlReportImpl = (HtmlReportImpl)context.getBean("htmlgenerate");
    	MockitoAnnotations.initMocks(this);
    }
    @After
    public void after() {
//        _tempFolder2 = folder;
//        System.out.println(_tempFolder2.getRoot().exists());
    }

    @AfterClass
    public static void  reset(){
//    	System.out.println(_tempFolder2.getRoot().exists());
    }
    
    @Test
    public void reportGenerator_retunrIsTrue(){
    	AccessingPeripheralResult access = new AccessingPeripheralResult();
    	access.setResultType(BPResultType.PASS);
    	CacheControlResult cache = new CacheControlResult();
    	cache.setResultType(BPResultType.FAIL);
    	List<AbstractBestPracticeResult> bpResults = new ArrayList<AbstractBestPracticeResult>();
    	bpResults.add(access);
    	bpResults.add(cache);
    	
    	File tempFile = null;
		try {
			tempFile = folder.newFile("abc.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tempFile.deleteOnExit();
		when(filereader.createFile(any(String.class))).thenReturn(tempFile);
		
    	AROTraceData results = new AROTraceData();
    	PacketAnalyzerResult analyzerResult = new PacketAnalyzerResult();
    	TraceDirectoryResult tracedirresult = new TraceDirectoryResult();
    	EnergyModel energyModel = new EnergyModel();    	
    	Statistic statistic = new Statistic();
    	statistic.setTotalByte(123);
    	statistic.setTotalHTTPSByte(123);
    	tracedirresult.setTraceDirectory("temp.txt");
    	analyzerResult.setTraceresult(tracedirresult);
    	analyzerResult.setEnergyModel(energyModel);
    	analyzerResult.setStatistic(statistic);
    	results.setAnalyzerResult(analyzerResult);
    	results.setBestPracticeResults(bpResults);   	
    	
    	assertTrue(htmlReportImpl.reportGenerator("abc.html", results));
    }
     
    @Test
    public  void reportGenerator_retunrIsFalse(){
    	assertFalse(htmlReportImpl.reportGenerator("abc.html", null));
    }

}
