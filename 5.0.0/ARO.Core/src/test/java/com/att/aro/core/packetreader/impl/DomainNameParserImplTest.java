package com.att.aro.core.packetreader.impl;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import com.att.aro.core.BaseTest;
import com.att.aro.core.packetreader.IDomainNameParser;
import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.UDPPacket;

public class DomainNameParserImplTest extends BaseTest {

	DomainNameParserImpl parser;
	byte[] dnsresponsedata = new byte[]{0,0,2,18,0,0,0,0,0,0,0,0,0,0,8,0,69,88,1,16,-92,-60,64,0,-4,17,50,-91,-84,26,38,1,10,77,-55,-78,0,53,30,108,0,-4,-34,74,29,-28,-127,-128,0,1,0,12,0,0,0,0,7,97,110,100,114,111,105,100,7,99,108,105,101,110,116,115,6,103,111,111,103,108,101,3,99,111,109,0,0,1,0,1,-64,12,0,5,0,1,0,0,0,2,0,
			12,7,97,110,100,114,111,105,100,1,108,-64,28,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-27,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-31,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-30,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-28,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-25,-64,56,0,1,0,1,0,
			0,0,6,0,4,74,125,-30,-32,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-24,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-29,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-18,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-23,-64,56,0,1,0,1,0,0,0,6,0,4,74,125,-30,-26};
	@Test
	public void parseDomain(){
		parser = (DomainNameParserImpl) context.getBean(IDomainNameParser.class);
		UDPPacket packet = new UDPPacket(1393515429, 547730, 288, 16, dnsresponsedata);
		DomainNameSystem dns = parser.parseDomainName(packet);
		assertNotNull(dns);
		
		
	}
}
