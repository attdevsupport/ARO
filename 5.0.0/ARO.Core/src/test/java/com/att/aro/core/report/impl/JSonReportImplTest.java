package com.att.aro.core.report.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.pojo.ErrorCode;
import com.att.aro.core.report.IReport;

public class JSonReportImplTest extends BaseTest{
	
	@InjectMocks
	IReport jsonreport;
	
	@Mock
	IFileManager filereader;
	
	static TemporaryFolder _tempFolder2; 

	@Rule
	public TemporaryFolder folder= new TemporaryFolder();

    @Before 
    public void setUp()
    {	
    	jsonreport = (JSonReportImpl)context.getBean("jsongenerate");
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
	public void reportGenerator_PathIsNullandTraceisNull(){
		boolean testResult = jsonreport.reportGenerator(null, null);
		assertFalse(testResult);
	}
    
	@Test
	public void reportGenerator_NoError(){

		AROTraceData results = new AROTraceData();
		results.setSuccess(true);
		try {
			when(filereader.createFile(any(String.class))).thenReturn(folder.newFile("abc.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean testResult = jsonreport.reportGenerator("abc.json",results);
		assertTrue(testResult);
	}
	
	@Test
	public void reportGenerator_IOException() throws IOException{

		AROTraceData results = new AROTraceData();
		results.setSuccess(true);
		when(filereader.createFile(any(String.class))).thenThrow(IOException.class);
		boolean testResult = jsonreport.reportGenerator("abc.json",results);
		assertFalse(testResult);		

	}
	@Test
	public void reportGenerator_JsonException() throws JsonGenerationException{

		AROTraceData results = new AROTraceData();
		results.setSuccess(true);
		when(filereader.createFile(any(String.class))).thenThrow(JsonGenerationException.class);
		boolean testResult = jsonreport.reportGenerator("abc.json",results);
		assertFalse(testResult);		

	}
	
	@Test
	public void reportGenerator_JsonMappingException() throws JsonMappingException{

		AROTraceData results = new AROTraceData();
		results.setSuccess(true);
		when(filereader.createFile(any(String.class))).thenThrow(JsonMappingException.class);
		boolean testResult = jsonreport.reportGenerator("abc.json",results);
		assertFalse(testResult);		

	}

	
}
