package com.att.aro.datacollector.rootedandroidcollector.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.att.aro.core.android.IAndroid;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/plugins.xml"})

public class TcpdumpRunnerTest {

	

	@Test
	public void testSuppression(){
		
	}
	
	@Mock
	IAndroid android;
	TcpdumpRunner tcprunner; 
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);		
		tcprunner = new TcpdumpRunner(null, null, android, false);
	}

	@Test
	public void testRun(){
		
		tcprunner.run();
	}

}
