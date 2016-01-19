package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;

public class ConnectionOpeningImplTest extends BaseTest{

	ConnectionOpeningImpl connOpenImpl;
	PacketAnalyzerResult tracedata;
	
	@Test
	public void runTest_resIsNoError(){
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		connOpenImpl = (ConnectionOpeningImpl)context.getBean("connectionOpening");
		AbstractBestPracticeResult result = connOpenImpl.runTest(tracedata);
		assertEquals(result.getResultType(), BPResultType.SELF_TEST);
	}

}
