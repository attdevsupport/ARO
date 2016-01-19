package com.att.aro.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.att.aro.core.BaseTest;
import com.att.aro.core.ILogger;


public class LoggerImplTest extends BaseTest {
	ILogger logger;
	Throwable throwable;
	@Before
	public void setUp(){
		logger = context.getBean(ILogger.class);
		throwable = Mockito.mock(Throwable.class);
	}
	
	@Test
	public void debug(){
		logger.debug("test debug");
		logger.debug("test debug2",throwable);
	}
	@Test
	public void error(){
		logger.error("test error");
		logger.error("test error2", throwable);
	}
	@Test
	public void info(){
		logger.info("test info");
		logger.info("test info2",throwable);
	}
	@Test
	public void warn(){
		logger.warn("test warning");
		logger.warn("test warning2",throwable);
	}
}
