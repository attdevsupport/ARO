package com.att.aro.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//helpful example: https://code.google.com/p/powermock/source/browse/trunk/modules/module-test/powermockito/junit4/src/test/java/samples/powermockito/junit4/system/SystemClassUserTest.java?r=1364

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AROConfig.class)
public class BaseTestWithPowerMock {
	@Autowired
	protected ApplicationContext context;
	
	//this blank method need to be here to avoid error
	@Test
	public void testSuppression(){
		
	}
}
