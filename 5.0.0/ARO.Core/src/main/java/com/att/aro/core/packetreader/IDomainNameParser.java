package com.att.aro.core.packetreader;

import com.att.aro.core.packetreader.pojo.DomainNameSystem;
import com.att.aro.core.packetreader.pojo.UDPPacket;

public interface IDomainNameParser {
	DomainNameSystem parseDomainName(UDPPacket packet);
}
