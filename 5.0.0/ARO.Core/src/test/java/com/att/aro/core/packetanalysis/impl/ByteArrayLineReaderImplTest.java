package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.IByteArrayLineReader;

public class ByteArrayLineReaderImplTest extends BaseTest{
	@InjectMocks
	private IByteArrayLineReader storageReader;
	
	@Before
	public void setUp(){
		
		storageReader = context.getBean(IByteArrayLineReader.class);
		MockitoAnnotations.initMocks(this);
		
	}
	
	@Test
	public void Test_toString_IsReturnString(){
		byte[] dataTest = { 97, 98, 99 };
		storageReader.init(dataTest);
		String testResult = storageReader.toString();
		assertEquals("index :" + 0 + "\n"+"abc", testResult);
		
	}

	
}
