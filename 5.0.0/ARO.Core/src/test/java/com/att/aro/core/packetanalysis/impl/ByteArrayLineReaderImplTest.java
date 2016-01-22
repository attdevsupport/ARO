/**
 * Copyright 2016 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
